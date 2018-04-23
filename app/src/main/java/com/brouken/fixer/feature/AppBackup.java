package com.brouken.fixer.feature;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.provider.DocumentFile;

import com.brouken.fixer.Prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.brouken.fixer.Utils.log;

public class AppBackup extends AsyncTask<Void, Void, Boolean> {

    public static final int REQUEST_SD_ACCESS = 10;
    private static final String SD_CARD_FOLDER = "apk";

    private final Context mContext;

    public AppBackup(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    protected Boolean doInBackground(final Void... values) {
        return backupApps();
    }

    public static void setup(Activity activity) {
        StorageManager storageManager = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();

        for (StorageVolume storageVolume : storageVolumes) {
            if (storageVolume.isRemovable()) {
                log(storageVolume.toString());

                Intent intent = storageVolume.createAccessIntent(null);
                activity.startActivityForResult(intent, REQUEST_SD_ACCESS);
            }
        }
    }

    private boolean backupApps() {
        String sd = (new Prefs(mContext)).getSdRoot();

        DocumentFile root = DocumentFile.fromTreeUri(mContext, Uri.parse(sd));

        DocumentFile apk = root.findFile(SD_CARD_FOLDER);
        if (apk == null)
            apk = root.createDirectory(SD_CARD_FOLDER);

        PackageManager packageManager = mContext.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                continue;
            }

            final String name = applicationInfo.loadLabel(packageManager).toString().trim();
            final String pkg = applicationInfo.packageName.trim();
            final String version = packageInfo.versionName.trim();
            final int versionCode = packageInfo.versionCode;

            final String filename = name + "-" + pkg + "-" + version + "-" + versionCode + ".apk";

            DocumentFile out = apk.findFile(filename);
            if (out != null)
                continue;

            // getMimeTypeFromExtension
            out = apk.createFile("application/vnd.android.package-archive", filename);
            File in = new File(applicationInfo.publicSourceDir);

            log(filename);

            try {
                copy(mContext, in, out.getUri());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void copy(Context context, File input, Uri output) throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(output, "w");
        FileOutputStream outStream = new FileOutputStream(pfd.getFileDescriptor());
        InputStream inStream = new FileInputStream(input);

        byte[] buf = new byte[16 * 1024];
        int size = -1;
        while ((size = inStream.read(buf)) != -1) {
            outStream.write(buf, 0, size);
        }
    }

    public static void schedule(Context context) {
        // DEBUG: adb shell cmd jobscheduler run -f com.brouken.fixer 0
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
        jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(context, AppBackupJobService.class))
                .setPersisted(true)
                .setPeriodic(TimeUnit.HOURS.toMillis(8), TimeUnit.HOURS.toMillis(4))
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .build());
    }

    public static void unschedule(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }
}
