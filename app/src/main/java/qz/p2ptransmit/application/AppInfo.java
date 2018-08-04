package qz.p2ptransmit.application;

import android.graphics.drawable.Drawable;

/**
 * AppInfo类，保存了app的信息
 * @author QinZhen
 * @Time 2017-2-16
 *
 */

public class AppInfo {
    private Drawable appIcon;               //图标
    private String packageName;             //包名
    private String appVersion;              //版本号
    private String appLabel;                //app标签
    private String lastTime="NotFound";
    private long foregroundTimeDay = 0;
    private long foregroundTimeWeek = 0;
    private long foregroundTimeMonth = 0;
    public final static int DAY = 0;
    public final static int WEEK = 1;
    public final static int MONTH = 2;
    //函数定义
    public AppInfo(){};


    public AppInfo(String appLabel, String packageName)
    {
        this.appLabel = appLabel;
        this.packageName = packageName;
    }


    public AppInfo(String appLabel, String packageName, Drawable appIcon)
    {
        this.appLabel = appLabel;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }


    public String getPackageName()
    {
        return this.packageName;
    }


    public String getAppVersion()
    {
        return this.appVersion;
    }


    public void setAppIcon(Drawable icon)
    {
        this.appIcon = icon;
    }


    public void setAppVersion(String version)
    {
        this.appVersion = version;
    }


    public Drawable getAppIcon()
    {
        return this.appIcon;
    }


    public String getAppLabel()
    {
        return this.appLabel;
    }


    public void setLastTime(String lastTime)
    {
        this.lastTime = lastTime;
    }


    public String getLastTime()
    {
        return this.lastTime;
    }


    public void setForegroundTimeDay(long foregroundTime)
    {
        this.foregroundTimeDay = foregroundTime;
    }


    public long getForegroundTimeDay()
    {
        return this.foregroundTimeDay;
    }


    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }


    public void setAppLabel(String label)
    {
        this.appLabel = label;
    }


    public String getStringForegroundTime(int choice)
    {
        String val = "";
        long time = 0;
        switch (choice)
        {
            case DAY:
                val = "最近24小时运行了:";
                time = this.foregroundTimeDay;
                break;
            case WEEK:
                val = "最近一周运行了:";
                time = this.foregroundTimeWeek;
                break;
            case MONTH:
                val = "最近一月运行了:";
                time = this.foregroundTimeMonth;
                break;
        }

        if(time >= 3600)
        {
            val += (time / 3600) + "时";
            val += (time % 3600) / 60 + "分";
            val += (time % 60) + "秒";
        }
        else if(time >= 60 && time < 3600)
        {
            val += (time / 60) + "分";
            val += (time % 60) + "秒";
        }
        else if(time < 60)
        {
            val += time + "秒";
        }
        return val;
    }


    public void setForegroundTimeWeek(long foregroundTimeWeek1)
    {
        this.foregroundTimeWeek = foregroundTimeWeek1;
    }


    public long getForegroundTimeWeek()
    {
        return this.foregroundTimeWeek;
    }


    public void setForegroundTimeMonth(long foregroundTimeMonth1)
    {
        this.foregroundTimeMonth = foregroundTimeMonth1;
    }


    public long getForegroundTimeMonth()
    {
        return this.foregroundTimeMonth;
    }


}
