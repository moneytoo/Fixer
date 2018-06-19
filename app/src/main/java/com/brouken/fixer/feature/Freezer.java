package com.brouken.fixer.feature;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.brouken.fixer.AdminReceiver;
import com.brouken.fixer.Sammy;

public class Freezer {

    private static class DeviceOwner {
        private static boolean switchAppState(Context context, String pkg) {
            ComponentName adminComponentName = new ComponentName(context, AdminReceiver.class);
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

            boolean isAppHidden = devicePolicyManager.isApplicationHidden(adminComponentName, pkg);
            if (devicePolicyManager.setApplicationHidden(adminComponentName, pkg, !isAppHidden))
                return !isAppHidden;
            else
                return isAppHidden;
        }
    }

    public static boolean switchAppState(Context context, String pkg) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager.isDeviceOwnerApp(context.getPackageName()))
            return DeviceOwner.switchAppState(context, pkg);
        else
            return Sammy.switchAppState(context, pkg);
    }

    public static boolean isAppEnabled(Context context, String pkg) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(pkg, 0);
            return applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void launchAppIfEnabled(Context context, String pkg) {
        if (isAppEnabled(context, pkg))
            context.startActivity(context.getPackageManager().getLaunchIntentForPackage(pkg));
    }
}
