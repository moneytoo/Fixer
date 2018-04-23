package com.brouken.fixer.feature;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.brouken.fixer.Utils.log;

public class AppBackup {

    public static final int REQUEST_SD_ACCESS = 10;

    private static final String SD_CARD_FOLDER = "apk";

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

    public static void backupApps(Context context, String sd) {
        DocumentFile root = DocumentFile.fromTreeUri(context, Uri.parse(sd));

        DocumentFile apk = root.findFile(SD_CARD_FOLDER);
        if (apk == null)
            apk = root.createDirectory(SD_CARD_FOLDER);

        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                continue;
            }

            final String name = applicationInfo.loadLabel(packageManager).toString();
            final String pkg = applicationInfo.packageName;
            final String version = packageInfo.versionName;
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
                copy(context, in, out.getUri());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copy(Context context, File input, Uri output) throws IOException {
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(output, "w");
        FileOutputStream outStream = new FileOutputStream(pfd.getFileDescriptor());
        InputStream inStream = new FileInputStream(input);

        byte[] buf = new byte[16 * 1024];
        int size = -1;
        while ((size = inStream.read(buf)) != -1) {
            outStream.write(buf, 0, size);
        }
    }
}
