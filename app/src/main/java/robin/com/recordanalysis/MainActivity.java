package robin.com.recordanalysis;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import robin.com.recordanalysis.domain.CallInfo;
import robin.com.recordanalysis.service.CallInfoService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MyAdapter adapter;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取条目显示的控件
        lv = (ListView) findViewById(R.id.lv);
        // 尝试获所有需要的授权
        CallInfoService.getPermissions(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CallInfoService.MY_PERMISSIONS_REQUESTS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // 授权成功，开始获取通话记录
                    Log.i(TAG, "所需权限授权成功！");
                    List<CallInfo> infos = CallInfoService.getCallInfo(this);

                    // 显示条目
                    adapter = new MyAdapter(infos);
                    lv.setAdapter(adapter);

                    // 条目设置长按事件，弹出一个列表对话框
                    lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            // 获取条目对应的号码
                            CallInfo info = (CallInfo) adapter.getItem(position);
                            final String number = info.number;

                            String[] items = new String[]{
                                    "复制号码到拨号盘",
                                    "拨号",
                                    "发送短信"
                            };
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("操作")
                                    .setItems(items, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    // 复制号码到拨号盘
                                                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
                                                    break;
                                                case 1:
                                                    // 拨号
                                                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                                        Log.i(TAG, "没有授权拨号！");
                                                        return;
                                                    }
                                                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
                                                    break;
                                                case 2:
                                                    // 发送短信
                                                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                                        Log.i(TAG, "没有授权发短信！");
                                                        return;
                                                    }
                                                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + number)));
                                                    break;

                                            }
                                        }
                                    }).show();
                            return false;
                        }
                    });
                } else {
                    Log.i(TAG, "所需权限授权失败！");
                }
                break;
            default:
                break;
        }
    }

    private class MyAdapter extends BaseAdapter {

        private List<CallInfo> infos;
        private LayoutInflater mInflater;

        public MyAdapter(List<CallInfo> infos) {
            super();
            this.infos = infos;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 加载布局
            View view = mInflater.inflate(R.layout.calllog_item, null);
            // 获取控件
            TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
            TextView tv_date = (TextView) view.findViewById(R.id.tv_date);
            TextView tv_type = (TextView) view.findViewById(R.id.tv_type);
            // 设置控件内容
            CallInfo info = infos.get(position);
            // 号码
            tv_number.setText(info.number);
            // 日期
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString = format.format(info.date);
            tv_date.setText(dateString);
            // 类型
            String type = null;
            int textColor = 0;
            switch (info.type){
                case CallLog.Calls.INCOMING_TYPE: // 来电，字体蓝色
                    type = "来电";
                    textColor = Color.BLUE;
                    break;
                case CallLog.Calls.OUTGOING_TYPE: // 去电，字体绿色
                    type = "去电";
                    textColor = Color.GREEN;
                    break;
                case CallLog.Calls.MISSED_TYPE:   // 未接，字体红色
                    type = "未接";
                    textColor = Color.RED;
                    break;
            }
            tv_type.setText(type);
            tv_type.setTextColor(textColor);

            return view;
        }
    }


}
