package qz.p2ptransmit.application;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AppInfoScanner类，可以根据filter的值来扫描一定范围的APP的信息，比如所有app或者系统app
 * @author QinZhen
 * @Time 2017-2-16
 *
 */
public class AppInfoScanner {
    private static String TAG = "AppInfoScanner";
    public static final int ALL_APP = 0;
    public static final int SYSTEM_APP = 1;
    public static final int THIRD_PARTY_APP = 2;


    public static List<AppInfo> getAllInstalledAppInfo(PackageManager packageManager, int filter)
    {
        List<ApplicationInfo> applicationInfoList = packageManager
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES); //也可以用0，未验证其中的区别
        Collections.sort(applicationInfoList,
                new ApplicationInfo.DisplayNameComparator(packageManager));
        List<AppInfo> installedAppInfos = new ArrayList<AppInfo>();     //保存查询到的AppInfo
        installedAppInfos.clear();
        switch (filter)
        {
            case ALL_APP:
                for (ApplicationInfo applicationInfo : applicationInfoList)
                {
                    installedAppInfos.add(applicationInfo2AppInfo(applicationInfo, packageManager));
                }
                return installedAppInfos;
            case SYSTEM_APP:
                for(ApplicationInfo applicationInfo : applicationInfoList)
                {
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                    {
                        installedAppInfos.add(applicationInfo2AppInfo(applicationInfo, packageManager));
                    }
                }
                return installedAppInfos;
            case THIRD_PARTY_APP:
                for(ApplicationInfo applicationInfo : applicationInfoList)
                {
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)
                    {
                        installedAppInfos.add(applicationInfo2AppInfo(applicationInfo, packageManager));
                    }
                    else if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
                    {
                        installedAppInfos.add(applicationInfo2AppInfo(applicationInfo, packageManager));
                    }
                }
                return installedAppInfos;
            default:
                return null;
        }
    }


    //从ApplicationInfo转换成AppInfo
    private static AppInfo applicationInfo2AppInfo(ApplicationInfo applicationInfo, PackageManager packageManager)
    {
        AppInfo appInfo = new AppInfo(
                (String)applicationInfo.loadLabel(packageManager), applicationInfo.packageName);
        appInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
        return appInfo;
    }
}
