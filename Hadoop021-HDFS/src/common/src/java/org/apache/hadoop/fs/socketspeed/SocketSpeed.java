package com.coship.hadoop.hdfs.server.socketspeed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.metrics2.util.MBeans;
import org.mortbay.util.ajax.JSON;

public class SocketSpeed extends NotificationBroadcasterSupport implements
		SocketSpeedMBean {

	public static LinkedList<SocketBean> list = null;
	public static boolean isStart  = false ;
	private class myThread extends Thread {
		private SocketSpeed ss;

		public myThread(SocketSpeed ss) {
			this.ss = ss;
		}

		public void run() {
			list = new LinkedList<SocketBean>();
			setStart();
			while (true) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					setEnd();
				}
				send();
			}
		}
		
		private void setStart(){
			isStart = true ;
		}
		private void setEnd(){
			isStart = false ;
		}

	}

	private void send() {
		
		final List<Map> maplist = new ArrayList<Map>();
		int size = list.size();
		int i = 0 ;
		while(!list.isEmpty() && i++ < size ){
			SocketBean socketBean = list.remove();
			final Map<String, Object> info = new HashMap<String, Object>();
			info.put("type", socketBean.getType());
			info.put("startTime", socketBean.getStartTime());
			info.put("endTime", socketBean.getEndTime());
			info.put("fileSize", socketBean.getFileSize());
			info.put("speed", socketBean.getSpeed());
			info.put("localAddress", socketBean.getLocalAddress());
			info.put("remoteAddress", socketBean.getRemoteAddress());
			info.put("remoteAddressPort", socketBean.getRemoteAddressPort());
			maplist.add(info);
		}
		if(maplist.isEmpty()){
			return ;
		}else{
			String information = JSON.toString(maplist);
			Notification n = new Notification("socketBean", mxBean, 1, System
					.currentTimeMillis(), information);
			sendNotification(n);
		}
		
	}
	
	public static synchronized void addSocketBean(SocketBean socketBean){
		list.add(socketBean);
	}
	
	public SocketSpeed(Configuration conf) {
		new myThread(this).start();
		registerMBean(conf);
	}
	
	private ObjectName mxBean = null;
	void registerMBean(Configuration conf) {
		mxBean = MBeans.register("System", "socketBean", this);
	}
	
}
