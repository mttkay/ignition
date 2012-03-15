package com.github.ignition.core.tasks;

import android.content.Context;

/**
 * To be implemented by classes that whish to receive callbacks about task status and progress
 * updates. Note that if this class is a {@link Context}, you can use the simpler
 * {@link IgnitedAsyncTaskContextHandler} instead.
 * 
 * @author Matthias Kaeppler
 * 
 * @param <ContextT>
 * @param <ProgressT>
 * @param <ReturnT>
 */
public interface IgnitedAsyncTaskHandler<ContextT extends Context, ProgressT, ReturnT> {

    ContextT getContext();

    void onTaskStarted(ContextT context);

    void onTaskProgress(ContextT context, ProgressT... progress);

    void onTaskCompleted(ContextT context, ReturnT result);

    void onTaskSuccess(ContextT context, ReturnT result);

    void onTaskFailed(ContextT context, Exception error);
}
