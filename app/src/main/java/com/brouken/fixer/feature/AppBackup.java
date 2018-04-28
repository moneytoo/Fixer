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

public class AppBackup extends AsyncTask<String, Void, Boolean> {

    public static final int REQUEST_SD_ACCESS = 10;
    private static final String SD_CARD_FOLDER = "apk";

    private final Context mContext;
    private PackageManager mPackageManager;
    private DocumentFile apkDir;

    public AppBackup(Context context) {
        mContext = context.getApplicationContext();
        mPackageManager = mContext.getPackageManager();
        prepareDir();
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

    private void prepareDir() {
        String sd = (new Prefs(mContext)).getSdRoot();

        DocumentFile root = DocumentFile.fromTreeUri(mContext, Uri.parse(sd));

        apkDir = root.findFile(SD_CARD_FOLDER);
        if (apkDir == null)
            apkDir = root.createDirectory(SD_CARD_FOLDER);
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

        DocumentFile out = apkDir.findFile(filename);
        if (out != null)
            return;

        // getMimeTypeFromExtension
        out = apkDir.createFile("application/vnd.android.package-archive", filename);
        File in = new File(applicationInfo.publicSourceDir);

        log(filename);

        try {
            copy(mContext, in, out.getUri());
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
        // DEBUG:
        // adb shell cmd jobscheduler run -f com.brouken.fixer 0
        // adb dumpsys jobscheduler
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
