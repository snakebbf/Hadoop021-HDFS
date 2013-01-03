package com.coship.hadoop.hdfs.server.namenode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.metrics2.util.MBeans;
import org.mortbay.util.ajax.JSON;

//import com.alibaba.jmx.test.mbean.HelloWorld;
public class SystemActivity extends NotificationBroadcasterSupport implements
		SystemActivityMBean {

	private long mTotal = 0, mIdle = 0;
	private long memFree = 0, memBuffer = 0, memCached = 0;
	private float loadOne = 0, loadFive = 0, loadFifteen = 0;
	private long lastInTime = 0, lastOutTime = 0, lastInByte = 0,
			lastOutByte = 0;
	private ObjectName mxBean = null;

	private class SystemActivityThread extends Thread {
		private SystemActivity sys;
		public SystemActivityThread(SystemActivity sys) {
			this.sys = sys;
		}

		public void run() {
			while (true) {
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				send();
			}
		}

	}

	public SystemActivity(Configuration conf) {
		new SystemActivityThread(this).start();
		registerMBean(conf);
	}

	public float getCpuUsed() {
		// TODO Auto-generated method stub
		return FindCpu();
	}

	public float getLoadFifteen() {
		FindLoad();
		return loadFifteen;
	}

	public float getLoadFive() {
		// TODO Auto-generated method stub
		return loadFive;
	}

	public float getLoadOne() {
		// TODO Auto-generated method stub
		return loadOne;
	}

	public long getMemBuffer() {
		// TODO Auto-generated method stub
		return memBuffer * 1024;
	}

	public long getMemFree() {

		FindMem();
		return memFree * 1024;

	}

	public long getMemCached() {

		return memCached * 1024;

	}

	public long getDiskUsed() {
		// TODO Auto-generated method stub
		return FindDisk() * 1024;
	}

	public float getInNetByte() {
		// TODO Auto-generated method stub
		return FindInNet();
	}

	public float getOutNetByte() {
		return FindOutNet();
	}

	private float FindCpu() {

		StringTokenizer token;
		BufferedReader br = openFile("/proc/stat");
		float used = 0;
		try {
			token = new StringTokenizer(br.readLine());
			token.nextToken();
			long user = Long.parseLong(token.nextToken());
			long nice = Long.parseLong(token.nextToken());
			long sys = Long.parseLong(token.nextToken());
			long idle = Long.parseLong(token.nextToken());
			long iowait = Long.parseLong(token.nextToken());
			long irq = Long.parseLong(token.nextToken());
			long softirq = Long.parseLong(token.nextToken());
			long total = user + nice + sys + idle + iowait + irq + softirq;
			used = ((total - mTotal) - (idle - mIdle))
					/ (float) (total - mTotal) * 100;
			mTotal = total;
			mIdle = idle;
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			closeFile(br);
		}
		return used;
	}

	private void FindMem() {

		File file = new File("/proc/meminfo");
		BufferedReader br = null;
		StringTokenizer token;
		String str;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));
			br.readLine();
			while ((str = br.readLine()) != null) {
				token = new StringTokenizer(str);
				String s = token.nextToken();
				if (s.equals("MemFree:")) {
					memFree = Long.parseLong(token.nextToken());
				} else if (s.equals("Buffers:")) {
					memBuffer = Long.parseLong(token.nextToken());

				} else if (s.equals("Cached:")) {
					memCached = Long.parseLong(token.nextToken());
					break;
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void FindLoad() {
		BufferedReader br = openFile("/proc/loadavg");
		StringTokenizer token;
		String str;
		try {
			str = br.readLine();
			token = new StringTokenizer(str);
			loadOne = Float.parseFloat(token.nextToken());
			loadFive = Float.parseFloat(token.nextToken());
			loadFifteen = Float.parseFloat(token.nextToken());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeFile(br);
		}
	}

	private long FindDisk() {

		BufferedReader br = null;
		StringTokenizer token;
		long diskUsed = 0;
		try {
			Process process = Runtime.getRuntime().exec("df");
			br = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			String str;
			br.readLine();
			int i = 1;
			while ((str = br.readLine()) != null) {
				token = new StringTokenizer(str);
				if (token.hasMoreTokens()) {
					if (i == 0) {
						token.nextToken();
						diskUsed += Long.parseLong(token.nextToken());
						i = 1;
						continue;
					} else {
						token.nextToken();
					}

				}

				if (token.hasMoreTokens()) {
					token.nextToken();
					diskUsed += Long.parseLong(token.nextToken());
					i = 1;
				} else {
					i = 0;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeFile(br);
		}
		return diskUsed;
	}

	private float FindInNet() {

		BufferedReader br = openFile("/proc/net/dev");
		StringTokenizer token;
		String str;
		long newInByte = 0;
		long inByte = 0;
		try {
			br.readLine();
			br.readLine();
			while ((str = br.readLine()) != null) {
				if (str.startsWith("eth", 2)) {
					token = new StringTokenizer(str);
					while (token.hasMoreTokens()) {
						String s = token.nextToken();
						if (s.startsWith("eth")) {
							String[] s1 = s.split(":");
							if (s1.length < 2) {
								break;
							}
							newInByte += Long.parseLong(s1[1]);
						}
					}
				}
			}
			inByte = newInByte - lastInByte;
			lastInByte = newInByte;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeFile(br);
		}
		long time = System.currentTimeMillis();
		long avgtime = time - lastInTime;
		lastInTime = time;
		return inByte / (avgtime / (float) 1000);
	}

	private float FindOutNet() {
		BufferedReader br = openFile("/proc/net/dev");
		StringTokenizer token;
		String str;
		long outByte = 0;
		long newOutByte = 0;
		try {
			br.readLine();
			br.readLine();
			while ((str = br.readLine()) != null) {
				if (str.startsWith("eth", 2)) {
					token = new StringTokenizer(str);
					for (int i = 0; i < 8; i++) {
						token.nextToken();
					}
					newOutByte += Long.parseLong(token.nextToken());
				}

			}
			outByte = newOutByte - lastOutByte;
			lastOutByte = newOutByte;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeFile(br);
		}
		long time = System.currentTimeMillis();
		long avgtime = time - lastOutTime;
		lastOutTime = time;
		return outByte / (avgtime / (float) 1000);
	}

	private BufferedReader openFile(String path) {
		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return br;
	}

	private void closeFile(BufferedReader br) {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	void registerMBean(Configuration conf) {
		mxBean = MBeans.register("System", "SystemActivity", this);
	}

	private void send() {

		final Map<String, Object> info = new HashMap<String, Object>();
		info.put("cpuUsed", FindCpu());
		FindMem();
		info.put("memFree", memFree * 1024);
		info.put("memBuffer", memBuffer * 1024);
		info.put("memCached", memCached * 1024);
		FindLoad();
		info.put("loadOne", loadOne);
		info.put("loadFive", loadFive);
		info.put("loadFifteen", loadFifteen);
		info.put("inNet", FindInNet());
		info.put("diskUsed", FindDisk() * 1024);
		info.put("outNet", FindOutNet());
		info.put("SocketNum", 0);
		String information = JSON.toString(info);
		Notification n = new Notification("SystemActivity", mxBean, 1, System
				.currentTimeMillis(), information);
		sendNotification(n);
	}

}
