package com.brouken.fixer.feature;

import android.app.job.JobParameters;
import android.app.job.JobService;

import static com.brouken.fixer.Utils.log;

public class AppBackupJobService extends JobService {

    static AppBackup mAppBackup;

    @Override
    public boolean onStartJob(final JobParameters params) {
        log("onStartJob()");

        mAppBackup = new AppBackup(this) {
            @Override
            protected void onPostExecute(Boolean success) {
                jobFinished(params, !success);
            }
        };
        mAppBackup.execute();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
