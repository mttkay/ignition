package com.github.ignition.core.tasks;

import java.util.concurrent.Callable;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

/**
 * An extension to AsyncTask that removes some of the boilerplate that's typically involved. Some of
 * the things handled for you by this class are:
 * <ul>
 * <li>handles a {@link Context} reference for you, which is passed to all status callbacks</li>
 * <li>allows worker method to throw exceptions, which will be passed to
 * {@link #onTaskFailed(Context, Exception)}</li>
 * <li>allows re-using a task as a skeleton that delegates to different worker implementations via
 * {@link IgnitedAsyncTaskCallable}</li>
 * <li>allows a {@link Context} to register itself as a callback handler via
 * {@link IgnitedAsyncTaskContextHandler}</li>
 * <li>allows any arbitrary object to register itself as a callback handler via
 * {@link IgnitedAsyncTaskHandler}</li>
 * </ul>
 * <p>
 * Since this class keeps a reference to a Context (either directly or indirectly through a callback
 * handler), you MUST ensure that this reference is cleared when the Context gets destroyed. You can
 * handle Context and handler connection and disconnection using the {@link #connect} and
 * {@link #disconnect} methods. For Activities, a good place to call them is onCreate and onDestroy
 * respectively.
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
        extends AsyncTask<ParameterT, ProgressT, ReturnT> implements
        IgnitedAsyncTaskHandler<ContextT, ProgressT, ReturnT> {

    public interface IgnitedAsyncTaskCallable<ContextT extends Context, ParameterT, ProgressT, ReturnT> {
        public ReturnT call(IgnitedAsyncTask<ContextT, ParameterT, ProgressT, ReturnT> task)
                throws Exception;
    }

    private ContextT context;
    private IgnitedAsyncTaskContextHandler<ProgressT, ReturnT> contextHandler;
    private IgnitedAsyncTaskHandler<ContextT, ProgressT, ReturnT> delegateHandler;
    private IgnitedAsyncTaskCallable<ContextT, ParameterT, ProgressT, ReturnT> callable;

    private Exception error;

    public IgnitedAsyncTask() {
    }

    /**
     * Connects a context object to this task. Use this method immediately after first creating the
     * task, and again in {@link Activity#onCreate} to re-connect tasks that you retained via
     * {@link Activity#onRetainNonConfigurationInstance()}. Make sure to always
     * {@link #disconnect()} a context in {@link Activity#onDestroy}.
     * 
     * <p>
     * As long as this context is not null, it will be passed to any handler callbacks associated
     * with this task, as well as to the task's own callbacks. If the context you pass here
     * implements {@link IgnitedAsyncTaskContextHandler}, it will be registered to receive callbacks
     * itself. If instead it implements the more generic {@link IgnitedAsyncTaskHandler}, <b>and if
     * no other handler of this type has been connected before (!)</b>, then the context will also
     * receive these callbacks. If however an ordinary POJO had already been connected to this task
     * as a {@link IgnitedAsyncTaskHandler}, then the context will not receive callbacks via this
     * interface (this is because a context must be disconnected from the task in onDestroy, while
     * ordinary handlers that are not Contexts will be retained by the task instance).
     * </p>
     */
    @SuppressWarnings("unchecked")
    public void connect(ContextT context) {
        this.context = context;
        if (context instanceof IgnitedAsyncTaskContextHandler) {
            this.contextHandler = (IgnitedAsyncTaskContextHandler<ProgressT, ReturnT>) context;
        } else if (delegateHandler == null && context instanceof IgnitedAsyncTaskHandler) {
            this.delegateHandler = (IgnitedAsyncTaskHandler<ContextT, ProgressT, ReturnT>) context;
        }
        if (delegateHandler != null) {
            delegateHandler.setContext(context);
        }
    }

    /**
     * Connects an {@link IgnitedAsyncTaskHandler} to this task to receive callbacks. The context
     * instance wrapped by this handler will be passed to {@link #connect(Context)}.
     * 
     * @param handler
     *            the handler object to receive task callbacks
     */
    public void connect(IgnitedAsyncTaskHandler<ContextT, ProgressT, ReturnT> handler) {
        this.delegateHandler = handler;
        connect(handler.getContext());
    }

    /**
     * Disconnects all context references, also those that are both a Context and handler at the
     * same time. Call this method in {@link Context#onDestroy}. <b>Note that this will NOT
     * disconnect handlers which are not Contexts but plain POJOs, so make sure you do not leak an
     * implicit reference to the same context from any handlers you have connected.</b>
     */
    public void disconnect() {
        this.contextHandler = null;
        this.context = null;
        if (delegateHandler != null) {
            if (delegateHandler instanceof Context) {
                delegateHandler = null;
            } else {
                delegateHandler.setContext(null);
            }
        }
    }

    /**
     * Returns the task's current context reference. Can be null. Handling the context reference is
     * done in {@link #connect(Context)} and {@link #disconnect()}.
     */
    @Override
    public ContextT getContext() {
        return context;
    }

    /**
     * DON'T use this to handle the task's context reference. Use {@link #connect(Context)} and
     * {@link #disconnect()} instead.
     */
    @Override
    public void setContext(ContextT context) {
        this.context = context;
    }

    /**
     * If you have connected a Context which implements the {@link IgnitedAsyncTaskContextHandler}
     * interface, this instance will be returned. Null otherwise.
     */
    public IgnitedAsyncTaskContextHandler<ProgressT, ReturnT> getContextHandler() {
        return contextHandler;
    }

    /**
     * If you have connected a Context or POJO which implements the {@link IgnitedAsyncTaskHandler}
     * interface, this instance will be returned. Null otherwise.
     */
    public IgnitedAsyncTaskHandler<ContextT, ProgressT, ReturnT> getDelegateHandler() {
        return delegateHandler;
    }

    /**
     * If you rely on a valid context reference, override {@link #onTaskStarted(Context)} instead.
     */
    @Override
    protected void onPreExecute() {
        if (context != null) {
            onTaskStarted(context);
            if (contextHandler != null) {
                contextHandler.onTaskStarted();
            }
            if (delegateHandler != null) {
                delegateHandler.onTaskStarted(context);
            }
        }
    }

    /**
     * Override this method to prepare task execution. The default implementation does nothing.
     * 
     * @see {@link AsyncTask#onPreExecute}
     * @param context
     *            The most recent instance of the Context that executed this IgnitedAsyncTask
     */
    @Override
    public void onTaskStarted(ContextT context) {
    }

    /**
     * If you rely on a valid context reference, override {@link #onProgress} instead.
     */
    @Override
    protected void onProgressUpdate(ProgressT... values) {
        if (context != null) {
            onTaskProgress(context, values);
            if (contextHandler != null) {
                contextHandler.onTaskProgress(values);
            }
            if (delegateHandler != null) {
                delegateHandler.onTaskProgress(context, values);
            }
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
    @Override
    public void onTaskProgress(ContextT context, ProgressT... progress) {
    }

    /**
     * Wrapper around {@link AsyncTask#execute} that does not take a varargs argument, but a single
     * parameter of type {@code ParameterT}. This is useful if you only ever need to pass one
     * argument to your tasks, and want to avoid running into compiler warning if the argument type
     * is itself parameterized (i.e. a generic type). However, for a single argument, this is
     * actually typesafe.
     * 
     * @param arg
     *            the single argument you want to pass to the task's {@link #run} method.
     */
    @SuppressWarnings("unchecked")
    public AsyncTask<ParameterT, ProgressT, ReturnT> execute(ParameterT arg) {
        return super.execute(arg);
    }

    /**
     * Wrapper around {@link AsyncTask#execute} that does not take any arguments. This is useful if
     * arguments to your tasks are nullable, and you want to avoid running into compiler warnings if
     * the argument type is itself parameterized (i.e. a generic type).
     */
    @SuppressWarnings("unchecked")
    public AsyncTask<ParameterT, ProgressT, ReturnT> execute() {
        return super.execute();
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
     * Override this method to define your task execution if your task takes either zero or more
     * than one argument. <b>NOTE</b> that if you override this method and still want to use this
     * variant of the method tu run jobs with a single argument If your task logic is pluggable, but
     * shares progress reporting or pre/post execute hooks, you can also set a {@link Callable} via
     * {@link #setCallable(IgnitedAsyncTaskCallable)}. By default, this method delegates to
     * {@link #run(ParameterT)} with the first element of params, or null if params is null or
     * empty.
     * 
     * @see {@link AsyncTask#doInBackground}
     * @param params
     *            the parameters for your task
     * @return the result of your task
     * @throws Exception
     */
    protected ReturnT run(ParameterT... params) throws Exception {
        return run(params.length > 0 ? params[0] : null);
    }

    /**
     * Override this method to define your task execution if your task takes a single argument. If
     * your task logic is pluggable, but shares progress reporting or pre/post execute hooks, you
     * can also set a {@link Callable} via {@link #setCallable(IgnitedAsyncTaskCallable)}. By
     * default, this method returns null.
     * 
     * @see {@link AsyncTask#doInBackground}
     * @param arg
     *            the single argument to your task
     * @return the result of your task
     * @throws Exception
     */
    protected ReturnT run(ParameterT arg) throws Exception {
        return null;
    }

    /**
     * If you rely on a valid context reference, override {@link #onCompleted}, {@link #onSuccess},
     * and {@link #onError} instead.
     */
    @Override
    protected void onPostExecute(ReturnT result) {
        if (context != null) {
            onTaskCompleted(context, result);
            if (contextHandler != null) {
                contextHandler.onTaskCompleted(result);
            }
            if (delegateHandler != null) {
                delegateHandler.onTaskCompleted(context, result);
            }
            if (failed()) {
                onTaskFailed(context, error);
                if (contextHandler != null) {
                    contextHandler.onTaskFailed(error);
                }
                if (delegateHandler != null) {
                    delegateHandler.onTaskFailed(context, error);
                }
            } else {
                onTaskSuccess(context, result);
                if (contextHandler != null) {
                    contextHandler.onTaskSuccess(result);
                }
                if (delegateHandler != null) {
                    delegateHandler.onTaskSuccess(context, result);
                }
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
    @Override
    public void onTaskCompleted(ContextT context, ReturnT result) {
    }

    /**
     * Implement this method to handle a successful task execution. The default implementation does
     * nothing.
     * 
     * @param context
     *            The most recent instance of the Context that executed this IgnitedAsyncTask
     * @param result
     */
    @Override
    public void onTaskSuccess(ContextT context, ReturnT result) {
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
    @Override
    public void onTaskFailed(ContextT context, Exception error) {
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
