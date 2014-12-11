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

package com.lidroid.xutils.task;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lidroid.xutils.util.LogUtils;

/**
 * 可调度优先级的异步任务
 * 
 * <pre>
 * Author: wyouflf
 * Date: 14-5-23
 * Time: 上午11:25
 * </pre>
 * 
 * @author wyouflf
 */
public abstract class PriorityAsyncTask<Params, Progress, Result> implements TaskHandler {

    private static final int MESSAGE_POST_RESULT = 0x1;
    private static final int MESSAGE_POST_PROGRESS = 0x2;

    private static final InternalHandler sHandler = new InternalHandler();
    /**
     * 默认线程池（可调度优先级的线程池）
     */
    public static final Executor sDefaultExecutor = new PriorityExecutor();
    private final WorkerRunnable<Params, Result> mWorker;
    private final FutureTask<Result> mFuture;

    private volatile boolean mExecuteInvoked = false;

    private final AtomicBoolean mCancelled = new AtomicBoolean();
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();

    private Priority priority;

    /**
     * 获取线程优先级
     * @return 线程优先级{@link com.lidroid.xutils.task.Priority}
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * 设置线程优先级
     * @param priority 线程优先级{@link com.lidroid.xutils.task.Priority}
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * 构造可调度优先级的异步任务
     * 
     * <pre>
     * 创建一个新的异步任务，此构造函数必须在UI线程中调用。
     * </pre>
     * 
     * <pre>
     * 原文：
     * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
     * </pre>
     */
    public PriorityAsyncTask() {
        mWorker = new WorkerRunnable<Params, Result>() {
            public Result call() throws Exception {
                mTaskInvoked.set(true);

                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                //noinspection unchecked
                return postResult(doInBackground(mParams));
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    LogUtils.d(e.getMessage());
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occured while executing doInBackground()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                }
            }
        };
    }

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    private Result postResult(Result result) {
        @SuppressWarnings("unchecked")
        Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT,
                new AsyncTaskResult<Result>(this, result));
        message.sendToTarget();
        return result;
    }

    /**
     * 后台任务处理
     * 
     * <pre>
     * 重写此方法，以便在后台执行处理；
     * 接收的参数通过调用方法{@link #execute(Object...)}时传入；
     * 这个方法会调用方法{@link #publishProgress(Object...)}在UI线程通知进度更新。
     * </pre>
     * 
     * <pre>
     * 原文：
     * Override this method to perform a computation on a background thread.
     * The specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     * </pre>
     * 
     * @param params 接收的参数
     * @return 返回的数据，由该类的子类定义
     * @see #onPreExecute()
     * @see #onPostExecute(Object)
     * @see #publishProgress(Object...)
     */
    @SuppressWarnings("unchecked")
    protected abstract Result doInBackground(Params... params);

    /**
     * 在方法{@link #doInBackground(Object...)}调用之前，UI线程上执行
     * @see #onPostExecute(Object)
     * @see #doInBackground(Object...)
     */
    protected void onPreExecute() {
    }

    /**
     * 在方法{@link #doInBackground(Object...)}调用之后，UI线程上执行
     * 
     * <pre>
     * Runs on the UI thread after {@link #doInBackground}.
     * The specified result is the value returned by {@link #doInBackground}.
     * This method won't be invoked if the task was cancelled.
     * </pre>
     *
     * @param result 接收的参数（由方法{@link #doInBackground(Object...)}返回的数据）
     * @see #onPreExecute()
     * @see #doInBackground(Object...)
     * @see #onCancelled(Object)
     */
    protected void onPostExecute(Result result) {
    }

    /**
     * 在方法{@link #publishProgress(Object...)}调用之后，UI线程上执行
     * 
     * <pre>
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     * </pre>
     *
     * @param values 标识进度的值
     * @see #publishProgress(Object...)
     * @see #doInBackground(Object...)
     */
    @SuppressWarnings("unchecked")
    protected void onProgressUpdate(Progress... values) {
    }

    /**
     * 在方法{@link #cancel(boolean)}调用之后，UI线程上执行
     * 
     * <pre>
     * Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.
     * 
     * The default implementation simply invokes {@link #onCancelled()} and
     * ignores the result. If you write your own implementation, do not call
     * <code>super.onCancelled(result)</code>.
     * </pre>
     *
     * @param result 接收的参数(可能为空)（由方法{@link #doInBackground(Object...)}返回的数据）
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    protected void onCancelled(Result result) {
        onCancelled();
    }

    /**
     * {@link #onCancelled(Object)}的默认实现
     * 
     * <pre>
     * 最好重写该方法，它是{@link #onCancelled(Object)}的默认实现
     * </pre>
     * 
     * <pre>
     * <p>Applications should preferably override {@link #onCancelled(Object)}.
     * This method is invoked by the default implementation of
     * {@link #onCancelled(Object)}.</p>
     * <p/>
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.</p>
     * </pre>
     *
     * @see #onCancelled(Object)
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    protected void onCancelled() {
    }

    /**
     * 判断任务是否已取消
     * 
     * <pre>
     * 如果在这个任务完成之前取消它，返回true；
     * 如果你调用{@link #cancel(boolean)}，该方法的返回值应该在{@link #doInBackground(Object...)}定时检查，以便尽快结束任务。
     * </pre>
     * 
     * <pre>
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally. If you are calling {@link #cancel(boolean)} on the task,
     * the value returned by this method should be checked periodically from
     * {@link #doInBackground(Object[])} to end the task as soon as possible.
     * </pre>
     *
     * @return 如果任务完成之前取消，返回true
     * @see #cancel(boolean)
     */
    @Override
    public final boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     * 取消任务
     * @param mayInterruptIfRunning true:任务在执行则中断；false:任务可以继续执行完成
     * @return false:任务不能取消（通常是任务已执行完成）
     * @see #isCancelled()
     * @see #onCancelled(Object)
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        return mFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean supportPause() {
        return false;
    }

    @Override
    public boolean supportResume() {
        return false;
    }

    @Override
    public boolean supportCancel() {
        return true;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void cancel() {
        this.cancel(true);
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    /**
     * 等待任务完成，获取执行结果
     * 
     * <pre>
     * Waits if necessary for the computation to complete,
     * and then retrieves its result.
     * </pre>
     *
     * @return 执行结果
     * @throws java.util.concurrent.CancellationException 如果任务被取消了
     * @throws java.util.concurrent.ExecutionException    如果任务执行出现异常
     * @throws InterruptedException                       如果当前任务中断了
     */
    public final Result get() throws InterruptedException, ExecutionException {
        return mFuture.get();
    }

    /**
     * 给定的时间内等待任务完成，获取执行结果
     * 
     * <pre>
     * Waits if necessary for at most the given time for the computation to complete,
     * and then retrieves its result.
     * </pre>
     *
     * @param timeout 取消操作前的等待时间
     * @param unit 超时时间单位
     * @return 执行结果
     * @throws java.util.concurrent.CancellationException 如果任务被取消了
     * @throws java.util.concurrent.ExecutionException    如果任务执行出现异常
     * @throws InterruptedException                       如果当前任务中断了
     */
    public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return mFuture.get(timeout, unit);
    }

    /**
     * 执行任务处理
     * 
     * <pre>
     * 每个实例只能调用一次；
     * 除首次调用外，均抛出异常{@link java.lang.IllegalStateException}；
     * 实际调用{@code #executeOnExecutor(sDefaultExecutor, params)}
     * </pre>
     * 
     * @param params 任务参数
     * @return 当前实例
     * @throws IllegalStateException 当前正在执行任务时，抛出异常
     * @see #executeOnExecutor(Executor, Object...)
     * @see #execute(Runnable)
     */
    @SuppressWarnings("unchecked")
    public final PriorityAsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(sDefaultExecutor, params);
    }

    /**
     * 执行任务处理
     * 
     * <pre>
     * 每个实例只能调用一次；
     * 除首次调用外，均抛出异常{@link java.lang.IllegalStateException}。
     * </pre>
     * 
     * @param exec 任务处理线程池{@link java.util.concurrent.Executor}
     * @param params 任务参数
     * @return 当前实例
     * @throws IllegalStateException 当前正在执行任务时，抛出异常
     * @see #execute(Object...)
     */
    @SuppressWarnings("unchecked")
    public final PriorityAsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
                                                                               Params... params) {
        if (mExecuteInvoked) {
            throw new IllegalStateException("Cannot execute task:"
                    + " the task is already executed.");
        }

        mExecuteInvoked = true;

        onPreExecute();

        mWorker.mParams = params;
        exec.execute(new PriorityRunnable(priority, mFuture));

        return this;
    }

    /**
     * 方便地执行{@link java.lang.Runnable}
     * 
     * <pre>
     * Convenience version of {@link #execute(Object...)} for use with
     * a simple Runnable object. See {@link #execute(Object[])} for more
     * information on the order of execution.
     * </pre>
     *
     * @see #execute(Object...)
     * @see #executeOnExecutor(java.util.concurrent.Executor, Object...)
     */
    public static void execute(Runnable runnable) {
        execute(runnable, Priority.DEFAULT);
    }

    /**
     * 方便地执行{@link java.lang.Runnable}
     * 
     * <pre>
     * Convenience version of {@link #execute(Object...)} for use with
     * a simple Runnable object. See {@link #execute(Object[])} for more
     * information on the order of execution.
     * </pre>
     *
     * @see #execute(Object...)
     * @see #executeOnExecutor(java.util.concurrent.Executor, Object...)
     */
    public static void execute(Runnable runnable, Priority priority) {
        sDefaultExecutor.execute(new PriorityRunnable(priority, runnable));
    }

    /**
     * 在UI线程通知进度更新
     * 
     * <pre>
     * This method can be invoked from {@link #doInBackground} to
     * publish updates on the UI thread while the background computation is
     * still running. Each call to this method will trigger the execution of
     * {@link #onProgressUpdate} on the UI thread.
     * <p/>
     * {@link #onProgressUpdate} will note be called if the task has been
     * canceled.
     * </pre>
     *
     * @param values 标识更新UI的进度
     * @see #onProgressUpdate(Object...)
     * @see #doInBackground(Object...)
     */
    @SuppressWarnings("unchecked")
    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            sHandler.obtainMessage(MESSAGE_POST_PROGRESS,
                    new AsyncTaskResult<Progress>(this, values)).sendToTarget();
        }
    }

    private void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
        } else {
            onPostExecute(result);
        }
    }

    private static class InternalHandler extends Handler {

        private InternalHandler() {
            super(Looper.getMainLooper());
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult<?> result = (AsyncTaskResult<?>) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    // There is only one result
                    result.mTask.finish(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
            }
        }
    }

    private static abstract class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }

    private static class AsyncTaskResult<Data> {
        @SuppressWarnings("rawtypes")
        final PriorityAsyncTask mTask;
        final Data[] mData;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        AsyncTaskResult(PriorityAsyncTask task, Data... data) {
            mTask = task;
            mData = data;
        }
    }
}
