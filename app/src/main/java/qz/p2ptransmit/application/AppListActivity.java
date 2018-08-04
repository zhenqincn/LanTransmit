package qz.p2ptransmit.application;

import android.app.Activity;
import android.os.Bundle;

import java.util.List;

import qz.p2ptransmit.R;

public class AppListActivity extends Activity {
    private List<AppInfo> installedAppList;
    private BrowseAppInfoAdapter browseAppInfoAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
    }
}
