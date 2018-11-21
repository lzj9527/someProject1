package com.shiyou.tryapp2.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.extend.util.AndroidUtils;
import android.extend.util.LogUtil;

import com.shiyou.tryapp2.ResourceHelper;
import com.shiyou.tryapp2.ResourceHelper.OnResourceDownloadCallback;
import com.shiyou.tryapp2.app.login.LoginHelper;

/**
 * 
 * @ClassName: AlarmReceiver
 * @Description: 闹铃时间到了会进入这个广播，这个时候可以做一些该做的业务。
 * @author HuHood
 * @date 2013-11-25 下午4:44:30
 * 
 */
public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		LogUtil.i(context.getPackageName(), "闹铃响了, 可以做点事情了~~" + context + ","
				+ intent+","+LoginHelper.getUserKey(context));
		// MainActivity.instance.alarmManagerdel();
		// Toast.makeText(context, "闹铃响了, 可以做点事情了~~", Toast.LENGTH_LONG).show();
		new ResourceHelper(context, LoginHelper.getUserKey(context))
				.downloadResource(new OnResourceDownloadCallback() {
					@Override
					public void onDownloadFinished(Object data) {
						AndroidUtils.MainHandler.post(new Runnable() {
							@Override
							public void run() {
								LogUtil.i(context.getPackageName(), "资源更新完成");
							}
						});
					}
				});
	}
}
