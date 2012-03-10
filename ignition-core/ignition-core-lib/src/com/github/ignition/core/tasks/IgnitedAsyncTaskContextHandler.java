package com.github.ignition.core.tasks;

import android.content.Context;

/**
 * To be implemented by {@link Context} classes that whish to receive callbacks about task status
 * and progress updates.
 * 
 * @author Matthias Kaeppler
 * 
 * @param <ReturnT>
 */
public interface IgnitedAsyncTaskContextHandler<ProgressT, ReturnT> {

    void onTaskStarted();

    void onTaskProgress(ProgressT... progress);

    void onTaskCompleted(ReturnT result);

    void onTaskSuccess(ReturnT result);

    void onTaskFailed(Exception error);
}
