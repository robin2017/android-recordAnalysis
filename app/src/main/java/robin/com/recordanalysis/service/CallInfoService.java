package robin.com.recordanalysis.service;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import robin.com.recordanalysis.domain.CallInfo;
import robin.com.recordanalysis.domain.ContactInfo;

public class CallInfoService {

    private static final String TAG = "CallInfoService";
    private static String[] permissionList = new String[]{
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };

    public static final int MY_PERMISSIONS_REQUESTS = 0;      // 批量申请多个权限：读取通话记录、打电话、发短信

    /**
     * 获取读取通话记录、打电话、发短信的权限
     *
     * @param activity 用于弹窗申请权限的Activity
     */
    public static void getPermissions(Activity activity) {
        ArrayList<String> list = new ArrayList<String>();
        // 循环判断所需权限中有哪个尚未被授权
        for (int i = 0; i < permissionList.length; i++) {
            if (ActivityCompat.checkSelfPermission(activity, permissionList[i]) != PackageManager.PERMISSION_GRANTED)
                list.add(permissionList[i]);
        }

        ActivityCompat.requestPermissions(activity, list.toArray(new String[list.size()]), MY_PERMISSIONS_REQUESTS);
    }

    /**
     * 请求获取通话记录
     *
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
                CallLog.Calls.TYPE ,   // 类型：来电、去电、未接
                CallLog.Calls.DURATION
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "授权失败，无法获取通话记录！");
            return null;
        }


        Cursor cursor = resolver.query(uri, projection, null, null, null);

        HashMap<String, ContactInfo> hashmap = getContacts(context);
        while (cursor.moveToNext()) {
            String number = cursor.getString(0);
            long date = cursor.getLong(1);
            int type = cursor.getInt(2);
            int duration = cursor.getInt(3);
            //时间为8月27日开始
            if (date > 1535299202000L && date < 1536940802000L) {
                if (hashmap.get(number) == null) {
                    infos.add(new CallInfo(number, date, type, 0, "null",duration));
                } else {
                    infos.add(new CallInfo(number, date, type, 0, hashmap.get(number).getName(),duration));

                }
            }
        }
        cursor.close();
        Collections.sort(infos);


        int length = infos.size();
        int cnt = 1;
        for (int i = 0; i < length; i++) {
            infos.get(i).setCnt(cnt++);
        }

        List<Integer> tmp = new ArrayList<>();

        for (int i = 0; i < length - 2; i++) {
            tmp.add((int) (infos.get(i).getDate() - infos.get(i + 1).getDate()));
        }

        return infos;
    }

    private static HashMap getContacts(Context context) {

        HashMap<String, ContactInfo> map = new HashMap();


        ContentResolver resolver = context.getContentResolver();

        //联系人的Uri，也就是content://com.android.contacts/contacts
        Uri uri = ContactsContract.Contacts.CONTENT_URI;

//
//          Uri contactUri =ContactsContract.Contacts.CONTENT_URI;
//
//        Cursor cursorDD =resolver.query(contactUri, null,null, null,null);
//        int columnNumber = cursorDD.getColumnCount();
//        for(int i = 0; i <columnNumber; i++) {
//            String temp = cursorDD.getColumnName(i);
//            System.out.println("listColumnNames " + i + "\t" + temp);
//
//        }


        //指定获取_id和display_name两列数据，display_name即为姓名
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                "company"
        };
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "授权失败，无法获取通讯录！");
            return null;
        }
        //根据Uri查询相应的ContentProvider，cursor为获取到的数据集
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        String[] arr = new String[cursor.getCount()];
        int i = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ContactInfo item = new ContactInfo();
                Long id = cursor.getLong(0);
                //获取姓名
                String name = cursor.getString(1);
                String company = cursor.getString(2);
                //指定获取NUMBER这一列数据
                String[] phoneProjection = new String[]{
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };

                item.setName(name);
                item.setCompany(company);
                //根据联系人的ID获取此人的电话号码
                Cursor phonesCusor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        phoneProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                        null,
                        null);
                String num = "";
                //因为每个联系人可能有多个电话号码，所以需要遍历
                if (phonesCusor != null && phonesCusor.moveToFirst()) {
                    do {
                        num = phonesCusor.getString(0).replaceAll(" ","");
//                        arr[i] += " , 电话号码：" + num;
                        item.setNumber(num);
                    } while (phonesCusor.moveToNext());
                }
                i++;
                map.put(num, item);
            } while (cursor.moveToNext());
        }


        return map;
    }


}