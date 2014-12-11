/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.lidroid.xutils.bitmap;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.lidroid.xutils.task.TaskHandler;

/**
 * 滚动暂停监听器
 */
public class PauseOnScrollListener implements OnScrollListener {

    private TaskHandler taskHandler;

    private final boolean pauseOnScroll;
    private final boolean pauseOnFling;
    private final OnScrollListener externalListener;

    /**
     * 构造滚动暂停监听器
     *
     * @param taskHandler 任务处理接口{@link com.lidroid.xutils.task.TaskHandler}
     * @param pauseOnScroll 触摸滚动是否暂停加载
     * @param pauseOnFling 快速滚动是否暂停加载
     */
    public PauseOnScrollListener(TaskHandler taskHandler, boolean pauseOnScroll, boolean pauseOnFling) {
        this(taskHandler, pauseOnScroll, pauseOnFling, null);
    }

    /**
     * 构造滚动暂停监听器
     *
     * @param taskHandler 任务处理接口{@link com.lidroid.xutils.task.TaskHandler}
     * @param pauseOnScroll 触摸滚动是否暂停加载
     * @param pauseOnFling 快速滚动是否暂停加载
     * @param customListener 滑动暂停监听器{@link android.widget.AbsListView.OnScrollListener}
     */
    public PauseOnScrollListener(TaskHandler taskHandler, boolean pauseOnScroll, boolean pauseOnFling, OnScrollListener customListener) {
        this.taskHandler = taskHandler;
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
        externalListener = customListener;
    }

    /**
     * 滚动状态改变
     * @param view 列表控件
     * @param scrollState 滚动状态
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                taskHandler.resume();
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                if (pauseOnScroll) {
                    taskHandler.pause();
                }
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                if (pauseOnFling) {
                    taskHandler.pause();
                }
                break;
        }
        if (externalListener != null) {
            externalListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * 滚动事件
     * @param view 列表控件
     * @param firstVisibleItem 当前显示的第一项的索引
     * @param visibleItemCount 当前显示项的总数
     * @param totalItemCount 可显示项总数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (externalListener != null) {
            externalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
    
}
