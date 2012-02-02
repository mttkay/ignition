package com.github.ignition.samples.ignitedasynctask;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.github.ignition.core.tasks.IgnitedAsyncTask;

public class IgnitedAsyncTaskActivity extends Activity {

    private SampleTask task;
    private ProgressBar progressBar;
    private Button restartButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
        protected void onStart(IgnitedAsyncTaskActivity context) {
            publishProgress(0);
        }

        @Override
        protected void onProgress(IgnitedAsyncTaskActivity context, Integer... values) {
            context.updateProgress(values[0]);
        }

        @Override
        protected String run(Void... params) throws Exception {
            int progress = 0;
            while (progress < 100) {
                progress += 10;
                SystemClock.sleep(1000);
                publishProgress(progress);
            }
            return "All done!";
        }

        @Override
        protected void onCompleted(IgnitedAsyncTaskActivity context, String result) {
            context.resetTask();
        }

        @Override
        protected void onSuccess(IgnitedAsyncTaskActivity context, String result) {
            context.showSuccess(result);
        }

        @Override
        protected void onError(IgnitedAsyncTaskActivity context, Exception error) {
            super.onError(context, error);
        }
    }

}