package com.brouken.fixer.feature;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.brouken.fixer.Prefs;

import static com.brouken.fixer.Utils.log;

public class AppBackupJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        log("onStartJob()");
        String sd = (new Prefs(getApplicationContext())).getSdRoot();
        boolean success = AppBackup.backupApps(getApplicationContext(), sd);
        jobFinished(params, !success);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
