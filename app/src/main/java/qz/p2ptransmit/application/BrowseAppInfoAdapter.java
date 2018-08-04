package qz.p2ptransmit.application;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import qz.p2ptransmit.R;

/**
 * Created by Qz on 2017/2/16.
 */

//自定义适配器类，提供给listView的自定义view
public class BrowseAppInfoAdapter extends BaseAdapter {
    private List<AppInfo> mlistAppInfo = null;


    LayoutInflater infater = null;

    public BrowseAppInfoAdapter(Context context,  List<AppInfo> apps) {
        infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistAppInfo = apps ;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //System.out.println("size" + mlistAppInfo.size());
        return mlistAppInfo.size();
    }
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mlistAppInfo.get(position);
    }
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public View getView(int position, View convertview, ViewGroup arg2) {
        //System.out.println("getView at " + position);
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {

            view = infater.inflate(R.layout.item_app_info, null);

            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        else{
            view = convertview ;
            holder = (ViewHolder) convertview.getTag() ;
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.tvAppLabel.setText(appInfo.getAppLabel());
        holder.tvPkgName.setText(appInfo.getPackageName());
        return view;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
        TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.imageview_applist_icon);
            this.tvAppLabel = (TextView) view.findViewById(R.id.txt_applist_label);
            this.tvPkgName = (TextView) view.findViewById(R.id.txt_applist_packagename);
        }
    }
}