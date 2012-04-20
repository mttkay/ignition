package com.github.ignition.samples.core;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ignition.core.tasks.IgnitedAsyncTask;
import com.github.ignition.core.tasks.IgnitedAsyncTaskContextHandler;

public class IgnitedAsyncTaskActivity extends Activity implements
        IgnitedAsyncTaskContextHandler<Integer, String> {

    private SampleTask task;
    private ProgressBar progressBar;
    private Button restartButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ignited_async_task_sample);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        restartButton = (Button) findViewById(R.id.restart_button);

        // try to obtain a reference to a task piped through from the previous
        // activity instance
        task = (SampleTask) getLastNonConfigurationInstance();

        // if there was no previous instance, create the task anew
        if (task == null) {
            resetTask();
        }

        startPendingTask(null);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // we leverage this method to "tunnel" the task object through to the next
        // incarnation of this activity in case of a configuration change
        return task;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // always disconnect the activity from the task here, in order to not risk
        // leaking a context reference
        task.disconnect();
    }

    public void resetTask() {
        task = new SampleTask();
        restartButton.setEnabled(true);
    }

    public void startPendingTask(View startButton) {
        // register this activity with the task
        task.connect(this);

        if (task.isPending()) {
            // task has not been started yet, start it
            task.execute();
        }

        restartButton.setEnabled(task.isFinished());
    }

    public void showSuccess(String result) {
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    public void updateProgress(Integer progress) {
        progressBar.setProgress(progress);
    }

    private static class SampleTask extends
            IgnitedAsyncTask<IgnitedAsyncTaskActivity, Void, Integer, String> {

        @Override
        public boolean onTaskStarted(IgnitedAsyncTaskActivity context) {
            publishProgress(0);
            return true;
        }

        @Override
        public String run(Void... params) throws Exception {
            int progress = 0;
            while (progress < 100) {
                progress += 10;
                SystemClock.sleep(1000);
                publishProgress(progress);
            }
            return "All done!";
        }

        @Override
        public boolean onTaskCompleted(IgnitedAsyncTaskActivity context, String result) {
            Log.d("Task", "task completed");
            return true;
        }

        @Override
        public boolean onTaskSuccess(IgnitedAsyncTaskActivity context, String result) {
            Log.d("Task", "task succeeded");
            return true;
        }
    }

    @Override
    public boolean onTaskStarted() {
        // returning false here means we have not consumed the task, and SampleTask#onTaskStarted
        // will be executed
        return false;
    }

    @Override
    public boolean onTaskProgress(Integer... progress) {
        updateProgress(progress[0]);
        // returning true here means that we have handled the event, and SampleTask#onTaskProgress
        // will not be executed
        return true;
    }

    @Override
    public boolean onTaskCompleted(String result) {
        resetTask();
        return true;
    }

    @Override
    public boolean onTaskSuccess(String result) {
        showSuccess(result);
        return false;
    }

    @Override
    public boolean onTaskFailed(Exception error) {
        return false;
    }

}