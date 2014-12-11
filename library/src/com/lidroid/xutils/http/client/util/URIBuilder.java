/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.http.client.util;

import android.text.TextUtils;
import com.lidroid.xutils.util.LogUtils;
import org.apache.http.NameValuePair;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * URI生成器
 */
public class URIBuilder {

    private String scheme;
    private String encodedSchemeSpecificPart;
    private String encodedAuthority;
    private String userInfo;
    private String encodedUserInfo;
    private String host;
    private int port;
    private String path;
    private String encodedPath;
    private String encodedQuery;
    private List<NameValuePair> queryParams;
    private String fragment;
    private String encodedFragment;

    /**
     * 构造URI生成器
     */
    public URIBuilder() {
        this.port = -1;
    }
    /**
     * 构造URI生成器
     * @param uri URI
     * @see java.net.URI
     */
    public URIBuilder(final String uri) {
        try {
            digestURI(new URI(uri));
        } catch (URISyntaxException e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
    /**
     * 构造URI生成器
     * @param uri {@link java.net.URI}
     * @see java.net.URI
     */
    public URIBuilder(final URI uri) {
        digestURI(uri);
    }

    private void digestURI(final URI uri) {
        this.scheme = uri.getScheme();
        this.encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
        this.encodedAuthority = uri.getRawAuthority();
        this.host = uri.getHost();
        this.port = uri.getPort();
        this.encodedUserInfo = uri.getRawUserInfo();
        this.userInfo = uri.getUserInfo();
        this.encodedPath = uri.getRawPath();
        this.path = uri.getPath();
        this.encodedQuery = uri.getRawQuery();
        this.queryParams = parseQuery(uri.getRawQuery());
        this.encodedFragment = uri.getRawFragment();
        this.fragment = uri.getFragment();
    }

    private List<NameValuePair> parseQuery(final String query) {
        if (!TextUtils.isEmpty(query)) {
            return URLEncodedUtils.parse(query);
        }
        return null;
    }

    /**
     * 生成一个{@link java.net.URI}实例
     * @param charset 字符编码
     */
    public URI build(Charset charset) throws URISyntaxException {
        return new URI(buildString(charset));
    }

    private String buildString(Charset charset) {
        final StringBuilder sb = new StringBuilder();
        if (this.scheme != null) {
            sb.append(this.scheme).append(':');
        }
        if (this.encodedSchemeSpecificPart != null) {
            sb.append(this.encodedSchemeSpecificPart);
        } else {
            if (this.encodedAuthority != null) {
                sb.append("//").append(this.encodedAuthority);
            } else if (this.host != null) {
                sb.append("//");
                if (this.encodedUserInfo != null) {
                    sb.append(this.encodedUserInfo).append("@");
                } else if (this.userInfo != null) {
                    sb.append(encodeUserInfo(this.userInfo, charset)).append("@");
                }
                if (InetAddressUtils.isIPv6Address(this.host)) {
                    sb.append("[").append(this.host).append("]");
                } else {
                    sb.append(this.host);
                }
                if (this.port >= 0) {
                    sb.append(":").append(this.port);
                }
            }
            if (this.encodedPath != null) {
                sb.append(normalizePath(this.encodedPath));
            } else if (this.path != null) {
                sb.append(encodePath(normalizePath(this.path), charset));
            }
            if (this.encodedQuery != null) {
                sb.append("?").append(this.encodedQuery);
            } else if (this.queryParams != null) {
                sb.append("?").append(encodeQuery(this.queryParams, charset));
            }
        }
        if (this.encodedFragment != null) {
            sb.append("#").append(this.encodedFragment);
        } else if (this.fragment != null) {
            sb.append("#").append(encodeFragment(this.fragment, charset));
        }
        return sb.toString();
    }

    private String encodeUserInfo(final String userInfo, Charset charset) {
        return URLEncodedUtils.encUserInfo(userInfo, charset);
    }

    private String encodePath(final String path, Charset charset) {
        return URLEncodedUtils.encPath(path, charset).replace("+", "20%");
    }

    private String encodeQuery(final List<NameValuePair> params, Charset charset) {
        return URLEncodedUtils.format(params, charset);
    }

    private String encodeFragment(final String fragment, Charset charset) {
        return URLEncodedUtils.encFragment(fragment, charset);
    }

    /**
     * 设置URI的协议方案
     * @param scheme 协议方案（如：http、https）
     * @return 当前实例
     */
    public URIBuilder setScheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * 设置URI的用户信息（可能包含非ASCII的未转义的预期值）
     * @param userInfo 用户信息
     * @return 当前实例
     */
    public URIBuilder setUserInfo(final String userInfo) {
        this.userInfo = userInfo;
        this.encodedSchemeSpecificPart = null;
        this.encodedAuthority = null;
        this.encodedUserInfo = null;
        return this;
    }

    /**
     * 设置URI的用户信息（可能包含非ASCII的未转义的预期值）
     * @param username 用户名
     * @param password 密码
     * @return 当前实例
     */
    public URIBuilder setUserInfo(final String username, final String password) {
        return setUserInfo(username + ':' + password);
    }

    /**
     * 设置URI的主机HOST
     * @param host 主机HOST（如：www.baidu.com）
     * @return 当前实例
     */
    public URIBuilder setHost(final String host) {
        this.host = host;
        this.encodedSchemeSpecificPart = null;
        this.encodedAuthority = null;
        return this;
    }

    /**
     * 设置URI的端口
     * @param port 端口（如：80）
     * @return 当前实例
     */
    public URIBuilder setPort(final int port) {
        this.port = port < 0 ? -1 : port;
        this.encodedSchemeSpecificPart = null;
        this.encodedAuthority = null;
        return this;
    }

    /**
     * 设置URI的路径
     * @param path 路径（可能包含非ASCII的未转义的预期值）（如：/api/login）
     * @return 当前实例
     */
    public URIBuilder setPath(final String path) {
        this.path = path;
        this.encodedSchemeSpecificPart = null;
        this.encodedPath = null;
        return this;
    }

    /**
     * 设置URI的参数数据
     * @param query 编码的表单数据预期值（如：name=admin&pass=admin888）
     * @return 当前实例
     */
    public URIBuilder setQuery(final String query) {
        this.queryParams = parseQuery(query);
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        return this;
    }

    /**
     * 添加URI参数
     * @param param 参数名
     * @param value 参数的值（可能包含非ASCII的未转义的预期值）
     * @return 当前实例
     */
    public URIBuilder addParameter(final String param, final String value) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<NameValuePair>();
        }
        this.queryParams.add(new BasicNameValuePair(param, value));
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        return this;
    }

    /**
     * 添加URI参数（参数名已存在则覆盖）
     * @param param 参数名
     * @param value 参数的值（可能包含非ASCII的未转义的预期值）
     * @return 当前实例
     */
    public URIBuilder setParameter(final String param, final String value) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<NameValuePair>();
        }
        if (!this.queryParams.isEmpty()) {
            for (final Iterator<NameValuePair> it = this.queryParams.iterator(); it.hasNext(); ) {
                final NameValuePair nvp = it.next();
                if (nvp.getName().equals(param)) {
                    it.remove();
                }
            }
        }
        this.queryParams.add(new BasicNameValuePair(param, value));
        this.encodedQuery = null;
        this.encodedSchemeSpecificPart = null;
        return this;
    }

    /**
     * 设置URI的锚点
     * @param fragment 锚点（可能包含非ASCII的未转义的预期值）（如：#bottom，参数不需包含#）
     * @return
     */
    public URIBuilder setFragment(final String fragment) {
        this.fragment = fragment;
        this.encodedFragment = null;
        return this;
    }

    /**
     * 获取协议方案
     * @return 协议方案（如：http、https）
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    public String getUserInfo() {
        return this.userInfo;
    }

    /**
     * 获取主机HOST
     * @return 主机HOST（如：www.baidu.com）
     */
    public String getHost() {
        return this.host;
    }

    /**
     * 获取端口
     * @return 端口（如：80）
     */
    public int getPort() {
        return this.port;
    }

    /**
     * 获取路径
     * @return 路径（如：/api/login）
     */
    public String getPath() {
        return this.path;
    }

    /**
     * 获取参数集
     * @return 参数集合{@link java.util.List}
     */
    public List<NameValuePair> getQueryParams() {
        if (this.queryParams != null) {
            return new ArrayList<NameValuePair>(this.queryParams);
        } else {
            return new ArrayList<NameValuePair>();
        }
    }

    /**
     * 获取锚点
     * @return 锚点（如：#bottom，参数不需包含#）
     */
    public String getFragment() {
        return this.fragment;
    }

    private static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        int n = 0;
        for (; n < path.length(); n++) {
            if (path.charAt(n) != '/') {
                break;
            }
        }
        if (n > 1) {
            path = path.substring(n - 1);
        }
        return path;
    }

}
