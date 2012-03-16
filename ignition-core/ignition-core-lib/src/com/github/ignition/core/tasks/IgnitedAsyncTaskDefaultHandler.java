package com.github.ignition.core.tasks;

import android.content.Context;

/**
 * Default implementation of the delegate handler interface with all callbacks methods defined to
 * have empty bodies. Subclass this if you do not need to implement all methods of
 * {@link IgnitedAsyncTaskHandler}. This class also handles a Context reference for you, so it's
 * always preferred to use this class over implementing the handler interface yourself.
 * 
 * @author Matthias Kaeppler
 * 
 * @param <ContextT>
 * @param <ProgressT>
 * @param <ReturnT>
 */
public abstract class IgnitedAsyncTaskDefaultHandler<ContextT extends Context, ProgressT, ReturnT>
        implements IgnitedAsyncTaskHandler<ContextT, ProgressT, ReturnT> {

    private ContextT context;

    public IgnitedAsyncTaskDefaultHandler(ContextT context) {
        this.context = context;
    }

    @Override
    public final ContextT getContext() {
        return context;
    }

    @Override
    public final void setContext(ContextT context) {
        this.context = context;
    }

    @Override
    public void onTaskStarted(ContextT context) {
    }

    @Override
    public void onTaskProgress(ContextT context, ProgressT... progress) {
    }

    @Override
    public void onTaskCompleted(ContextT context, ReturnT result) {
    }

    @Override
    public void onTaskSuccess(ContextT context, ReturnT result) {
    }

    @Override
    public void onTaskFailed(ContextT context, Exception error) {
    }

}
