package com.brouken.fixer.feature;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.brouken.fixer.AdminReceiver;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

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

    private static class Root {
        private static boolean switchAppState(Context context, String pkg) {
            try {
                boolean enabled = isAppEnabled(context, pkg);
                String newState;

                if (enabled) {
                    newState = "disable";
                } else {
                    newState = "enable";
                }

                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(new Command(0, false,
                        "pm " + newState + " " + pkg));

                return !enabled;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public static boolean switchAppState(Context context, String pkg) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager.isDeviceOwnerApp(context.getPackageName()))
            return DeviceOwner.switchAppState(context, pkg);
        else
            return Root.switchAppState(context, pkg);
    }

    public static boolean isAppEnabled(Context context, String pkg) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(pkg, 0);
            return applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
