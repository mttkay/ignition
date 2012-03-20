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

    /**
     * Return true from this method if you want to swallow the event; it will then not be passed on
     * to the task itself.
     */
    boolean onTaskStarted();

    /**
     * Return true from this method if you want to swallow the event; it will then not be passed on
     * to the task itself.
     */
    boolean onTaskProgress(ProgressT... progress);

    /**
     * Return true from this method if you want to swallow the event; it will then not be passed on
     * to the task itself.
     */
    boolean onTaskCompleted(ReturnT result);

    /**
     * Return true from this method if you want to swallow the event; it will then not be passed on
     * to the task itself.
     */
    boolean onTaskSuccess(ReturnT result);

    /**
     * Return true from this method if you want to swallow the event; it will then not be passed on
     * to the task itself.
     */
    boolean onTaskFailed(Exception error);
}
