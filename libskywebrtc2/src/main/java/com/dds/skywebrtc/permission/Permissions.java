package com.dds.skywebrtc.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.M;

/**
 * Permission-related helpers
 */
public class Permissions {


    /**
     * @param callback will be called if request is not canceled, with either
     *                 {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}
     */
    public static void request(Activity activity, String permission, Consumer<Integer> callback) {
        if (Build.VERSION.SDK_INT >= M) {
            request2(activity, permission, callback);
        } else {
            if (has(activity, permission)) {
                callback.accept(0);
            } else {
                callback.accept(-1);
            }
        }

    }

    /**
     * @param callback will be called if request is not canceled, with either
     *                 {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}
     */
    public static void request(Activity activity, String[] permissions, Consumer<Integer> callback) {
        if (Build.VERSION.SDK_INT >= M) {
            request2(activity, permissions, callback);
        } else {
            if (has(activity, permissions)) {
                callback.accept(0);
            } else {
                callback.accept(-1);
            }

        }

    }

    @RequiresApi(M)
    public static void request2(Activity activity, String permission, Consumer<Integer> callback) {
        final FragmentManager fm = activity.getFragmentManager();
        if (!has(activity, permission)) {
            fm.beginTransaction().add(new PermissionRequestFragment(new String[]{permission}, callback), null).commitAllowingStateLoss();
        } else {
            callback.accept(PERMISSION_GRANTED);
        }
    }

    @RequiresApi(M)
    public static void request2(Activity activity, String[] permissions, Consumer<Integer> callback) {
        final FragmentManager fm = activity.getFragmentManager();
        if (!has(activity, permissions)) {
            fm.beginTransaction().add(new PermissionRequestFragment(permissions, callback), null).commitAllowingStateLoss();
        } else {
            callback.accept(PERMISSION_GRANTED);
        }
    }

    private static boolean has(Context activity, String... permissions) {
        List<String> mPermissionListDenied = new ArrayList<>();
        for (String permission : permissions) {
            int result = checkPermission(activity, permission);
            if (result != PERMISSION_GRANTED) {
                mPermissionListDenied.add(permission);
            }
        }
        return mPermissionListDenied.size() == 0;
    }

    private static boolean has(Context context, String permission) {
        return context.checkPermission(permission, Process.myPid(), Process.myUid()) == PERMISSION_GRANTED;
    }

    private static int checkPermission(Context activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission);
    }

    @RequiresApi(M)
    public static class PermissionRequestFragment extends Fragment {

        @SuppressLint("ValidFragment")
        public PermissionRequestFragment(@NonNull final String[] permissions, @NonNull final Consumer<Integer> callback) {
            mPermissions = permissions;
            mCallback = callback;
        }

        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (mPermissions != null) requestPermissions(mPermissions, 0);
        }

        @Override
        public void onRequestPermissionsResult(final int request, @NonNull final String[] permissions, @NonNull final int[] results) {
            getFragmentManager().beginTransaction().remove(this).commit();
            if (mCallback == null || results.length == 0/* canceled */) return;
            boolean isGrant = true;
            for (int result : results) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isGrant = false;
                    break;
                }
            }
            mCallback.accept(isGrant ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED);
        }

        public PermissionRequestFragment() {
            mPermissions = null;
            mCallback = null;
        }

        private final @Nullable
        String[] mPermissions;
        private final @Nullable
        Consumer<Integer> mCallback;
    }
}
