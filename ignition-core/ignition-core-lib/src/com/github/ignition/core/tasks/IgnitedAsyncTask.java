package com.github.ignition.core.tasks;

import java.util.concurrent.Callable;

import android.content.Context;
import android.os.AsyncTask;

/**
 * An extension to AsyncTask that removes some of the boilerplate that's typically involved. Some of
 * the things handled for you by this class are:
 * <ul>
 * <li>allows worker method to throw exceptions, which will be passed to {@link #onError}</li>
 * <li>it handles a {@link Context} reference for you, which is also passed to {@link #onStart},
 * {@link #onSuccess}, and {@link #onError}</li>
 * <li>allows re-using a task as a skeleton that delegates to different worker implementations via
 * {@link IgnitedAsyncTaskCallable}</li>
 * </ul>
 * <p>
 * Since this class keeps a reference to a Context, you MUST ensure that this reference is cleared
 * when the Context gets destroyed. You can handle Context connection and disconnection using the
 * {@link #connect(Context)} and {@link #disconnect()} methods. For Activities, a good place to call
 * them is onRestoreInstanceState and onDestroy respectively.
 * </p>
 * <p>
 * Please note that the callbacks added by this class will ONLY be called if the context reference
 * is valid. You can still receive AsyncTask's callbacks by overriding the callbacks defined in that
 * class.
 * </p>
 * 
 * @author Matthias Kaeppler
 * 
 * @param <ContextT>
 * @param <ParameterT>
 * @param <ProgressT>
 * @param <ReturnT>
 */
public abstract class IgnitedAsyncTask<ContextT extends Context, ParameterT, ProgressT, ReturnT>
        extends AsyncTask<ParameterT, ProgressT, ReturnT> {

    public interface IgnitedAsyncTaskCallable<ContextT extends Context, ParameterT, ProgressT, ReturnT> {
        public ReturnT call(IgnitedAsyncTask<ContextT, ParameterT, ProgressT, ReturnT> task)
                throws Exception;
    }

    private ContextT context;
    private Exception error;
    private IgnitedAsyncTaskCallable<ContextT, ParameterT, ProgressT, ReturnT> callable;

    public IgnitedAsyncTask() {
    }

    public IgnitedAsyncTask(ContextT context) {
        this.context = context;
    }

    public void connect(ContextT context) {
        this.context = context;
    }

    public void disconnect() {
        this.context = null;
    }

    public ContextT getContext() {
        return context;
    }

    /**
     * If you rely on a valid context reference, override {@link #onStart(Context)} instead.
     */
    @Override
    protected void onPreExecute() {
        if (context != null) {
            onStart(context);
        }
    }

    /**
     * Override this method to prepare task execution. The default implementation does nothing.
     * 
     * @see {@link AsyncTask#onPreExecute}
     * @param context
     *            The most recent instance of the Context that executed this IgnitedAsyncTask
     */
    protected void onStart(ContextT context) {
    }

    /**
     * If you rely on a valid context reference, override {@link #onProgress} instead.
     */
    @Override
    protected void onProgressUpdate(ProgressT... values) {
        if (context != null) {
            onProgress(context, values);
        }
    }

    /**
     * Override this method to update progress elements on the UI thread. The default implementation
     * does nothing.
     * 
     * @see {@link AsyncTask#publishProgress}
     * @see {@link AsyncTask#onProgressUpdate}
     * @param context
     *            The most recent instance of the Context that executed this IgnitedAsyncTask
     * @param progress
     *            the progress values
     */
    protected void onProgress(ContextT context, ProgressT... progress) {
    }

    /**
     * No, you want to override {@link #run(Object...)} instead.
     */
    @Override
    protected final ReturnT doInBackground(ParameterT... params) {
        ReturnT result = null;
        try {
            if (callable != null) {
                result = callable.call(this);
            } else {
                result = run(params);
            }
        } catch (Exception e) {
            this.error = e;
        }
        return result;
    }

    /**
     * Implement this method to define your task execution. If your task logic is pluggable, but
     * shares progress reporting or pre/post execute hooks, you can also set a {@link Callable} via
     * {@link #setCallable(IgnitedAsyncTaskCallable)}
     * 
     * @see {@link AsyncTask#doInBackground}
     * @param params
     *            the parameters for your task
     * @return the result of your task
     * @throws Exception
     */
    protected ReturnT run(ParameterT... params) throws Exception {
        return null;
    }

    /**
     * If you rely on a valid context reference, override {@link #onCompleted}, {@link #onSuccess},
     * and {@link #onError} instead.
     */
    @Override
    protected void onPostExecute(ReturnT result) {
        if (context != null) {
            onCompleted(context, result);
            if (failed()) {
                onError(context, error);
            } else {
                onSuccess(context, result);
            }
        }
    }

    /**
     * Implement this method to handle a completed task execution, regardless of outcome. The
     * default implementation does nothing.
     * 
     * @see {@link AsyncTask#onPostExecute}
     * @param context
     *            The most recent instance of the Context that executed this IgnitedAsyncTask
     * @param result
     */
    protected void onCompleted(ContextT context, ReturnT result) {
    }

    /**
     * Implement this method to handle a successful task execution. The default implementation does
     * nothing.
     * 
     * @param context
     *            The most recent instance of the Context that executed this IgnitedAsyncTask
     * @param result
     */
    protected void onSuccess(ContextT context, ReturnT result) {
    }

    /**
     * Override this method to handle an error that occurred during task execution in a graceful
     * manner. The default implementation prints a stack trace.
     * 
     * @param context
     *            The most recent instance of the Context that executed this IgnitedAsyncTask
     * @param error
     *            The exception that was thrown during task execution
     */
    protected void onError(ContextT context, Exception error) {
        error.printStackTrace();
    }

    /**
     * @return true if an Exception was thrown in {@link #run}
     */
    public boolean failed() {
        return error != null;
    }

    /**
     * @see AsyncTask.Status.PENDING
     */
    public boolean isPending() {
        return getStatus().equals(AsyncTask.Status.PENDING);
    }

    /**
     * @see AsyncTask.Status.RUNNING
     */
    public boolean isRunning() {
        return getStatus().equals(AsyncTask.Status.RUNNING);
    }

    /**
     * @see AsyncTask.Status.FINISHED
     */
    public boolean isFinished() {
        return getStatus().equals(AsyncTask.Status.FINISHED);
    }

    /**
     * Use an {@link IgnitedAsyncTaskCallable} instead of overriding {@link #run}. This can be
     * useful when creating task skeletons that need to implement the worker method differently.
     * 
     * @param callable
     */
    public void setCallable(
            IgnitedAsyncTaskCallable<ContextT, ParameterT, ProgressT, ReturnT> callable) {
        this.callable = callable;
    }
}
