package com.brouken.fixer.feature;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.brouken.fixer.Utils.log;

public class AppBackup extends AsyncTask<String, Void, Boolean> {

    private final Context mContext;
    private PackageManager mPackageManager;

    public AppBackup(Context context) {
        mContext = context.getApplicationContext();
        mPackageManager = mContext.getPackageManager();
    }

    @Override
    protected Boolean doInBackground(final String... pkgs) {
        if (pkgs == null || pkgs.length < 1)
            return backupApps();
        else {
            backupApp(pkgs[0]);
            return true;
        }
    }

    private boolean backupApps() {
        List<PackageInfo> packageInfos = mPackageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            backupApp(packageInfo);
        }
        return true;
    }

    private void backupApp(PackageInfo packageInfo) {
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;

        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 &&
                (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
            return;
        }

        final String name = applicationInfo.loadLabel(mPackageManager).toString().trim();
        final String pkg = applicationInfo.packageName.trim();
        final String version = packageInfo.versionName.trim();
        final int versionCode = packageInfo.versionCode;

        String filename = name + "-" + pkg + "-" + version + "-" + versionCode + ".apk";
        // Because of Win FS limitations
        filename = filename.replaceAll("[?<>\\:*|\"]", "_");

        File out = new File(mContext.getExternalFilesDir(null) + "/" + filename);

        if (out.exists())
            return;

        log(filename);

        File in = new File(applicationInfo.publicSourceDir);

        try {
            Files.copy(in.toPath(), out.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void backupApp(String pkg) {
        try {
            backupApp(mPackageManager.getPackageInfo(pkg, 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void schedule(Context context) {
        // DEBUG:
        // adb shell cmd jobscheduler run -f com.brouken.fixer 0
        // adb shell dumpsys jobscheduler
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
        jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(context, AppBackupJobService.class))
                .setPersisted(true)
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .build());
    }

    public static void unschedule(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }
}
