package robin.com.recordanalysis.service;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import robin.com.recordanalysis.domain.CallInfo;
public class CallInfoService {

    private static final String TAG = "CallInfoService";
    private static String[] permissionList = new String[]{
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
    };

    public static final int MY_PERMISSIONS_REQUESTS = 0;      // 批量申请多个权限：读取通话记录、打电话、发短信

    /**
     * 获取读取通话记录、打电话、发短信的权限
     * @param activity 用于弹窗申请权限的Activity
     */
    public static void getPermissions(Activity activity) {
        ArrayList<String> list = new ArrayList<String>();
        // 循环判断所需权限中有哪个尚未被授权
        for (int i = 0; i < permissionList.length; i++){
            if (ActivityCompat.checkSelfPermission(activity, permissionList[i]) != PackageManager.PERMISSION_GRANTED)
                list.add(permissionList[i]);
        }

        ActivityCompat.requestPermissions(activity, list.toArray(new String[list.size()]), MY_PERMISSIONS_REQUESTS);
    }

    /**
     * 请求获取通话记录
     * @param context 上下文。通话记录需要从系统的【通话应用】中的内容提供者中获取，内容提供者需要上下文。
     *                通话记录保存在联系人数据库中：data/data/com.android.provider.contacts/databases/contacts2.db库中的calls表。
     * @return 一个包含所有通话记录的集合。
     */
    public static List<CallInfo> getCallInfo(Context context) {
        List<CallInfo> infos = new ArrayList<CallInfo>();
        ContentResolver resolver = context.getContentResolver();
        // uri的写法需要查看源码JB\packages\providers\ContactsProvider\AndroidManifest.xml中内容提供者的授权
        // 从清单文件可知该提供者是CallLogProvider，且通话记录相关操作被封装到了Calls类中
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[]{
                CallLog.Calls.NUMBER, // 号码
                CallLog.Calls.DATE,   // 日期
                CallLog.Calls.TYPE    // 类型：来电、去电、未接
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "授权失败，无法获取通话记录！");
            return null;
        }
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        while (cursor.moveToNext()){
            String number = cursor.getString(0);
            long date = cursor.getLong(1);
            int type = cursor.getInt(2);
            infos.add(new CallInfo(number, date, type));
        }
        cursor.close();

        return infos;
    }

}