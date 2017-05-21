package com.itant.processkiller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		new KillTask().execute();
	}
	
	private class KillTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			int thisPid = android.os.Process.myPid();
			Process process = null;
			try {
				process = Runtime.getRuntime().exec("su");
		        DataOutputStream os = new DataOutputStream(process.getOutputStream()); 
		        
		        // 获取后台进程
		        ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				List<ActivityManager.RunningAppProcessInfo> mRunningProcess = mActivityManager.getRunningAppProcesses();
				
				// 获取已安装的非系统应用的UID
				List<Integer> apkUIDs = new ArrayList<>();
				List<ApplicationInfo> applicationInfos = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
				for (ApplicationInfo applicationInfo : applicationInfos) {
					if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						// 非系统应用
						apkUIDs.add(applicationInfo.uid);
					}
				}
				
				// 后台进程的进程名包含有非系统应用的包名则可以杀掉
				for (ActivityManager.RunningAppProcessInfo processInfo : mRunningProcess) {
					if (processInfo.pid != thisPid) {
						// 不是本应用
						for (Integer uid : apkUIDs) {
							// 属于非系统应用
							if (processInfo.uid == uid) {
								os.writeBytes("kill -9 " + processInfo.pid + "\n");
								os.flush();
								break;
							}
						}
					}
				}
				
				os.writeBytes( "exit\n");
	            os.flush();
				os.close();
		    } catch (Exception e) {  
		            e.printStackTrace();  
		    } finally {
		    	if (process != null) {
		    		try {
		    			process.getInputStream().close();
		    			process.getOutputStream().close();
		    			process.destroy();
		    			process = null;
		    		} catch (IOException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
		    	}
		    }
			
			return null;
		}
	}
}
