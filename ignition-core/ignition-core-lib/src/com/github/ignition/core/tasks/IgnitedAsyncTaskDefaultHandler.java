package com.github.ignition.core.tasks;

import android.content.Context;

/**
 * Default implementation of the delegate handler interface. Sublcass this if you do not need to
 * implement all methods of {@link IgnitedAsyncTaskHandler}.
 * 
 * @author Matthias Kaeppler
 * 
 * @param <ContextT>
 * @param <ProgressT>
 * @param <ReturnT>
 */
public abstract class IgnitedAsyncTaskDefaultHandler<ContextT extends Context, ProgressT, ReturnT>
        implements IgnitedAsyncTaskHandler<ContextT, ProgressT, ReturnT> {

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
