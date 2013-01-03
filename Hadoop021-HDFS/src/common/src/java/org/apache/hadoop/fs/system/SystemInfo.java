package com.coship.hadoop.hdfs.server.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.hadoop.metrics2.util.MBeans;

public class SystemInfo implements SystemInfoMBean {

	private int cpuNum = 0;
	private float cpuMHz = 0;
	private String cpuName = "";
	private long memTotal = 0;

	public SystemInfo() {
		FindMem();
		FindCpu();
		registerMBean();
	}

	public String getMachineType() {
		// TODO Auto-generated method stub
		return System.getProperty("os.arch");
	}

	public String getOsName() {
		// TODO Auto-generated method stub
		return System.getProperty("os.name");
	}

	public String getOsRelease() {
		// TODO Auto-generated method stub
		return System.getProperty("os.version");
	}

	public float getCpuMHz() {
		// TODO Auto-generated method stub
		return cpuMHz;
	}

	public String getCpuName() {
		// TODO Auto-generated method stub
		return cpuName;
	}

	public int getCpuNum() {
		// TODO Auto-generated method stub
		return cpuNum;
	}

	public long getMemTotal() {
		// TODO Auto-generated method stub
		return memTotal * 1024;
	}

	public long getDiskTotal() {
		// TODO Auto-generated method stub
		return FindDisk() * 1024;
	}

	void FindCpu() {
		String str;
		BufferedReader br = openFile("/proc/cpuinfo");
		int num = 0;
		try {
			while ((str = br.readLine()) != null) {
				if (str.startsWith("processor"))
					num++;
				if (cpuMHz == 0) {
					if (str.startsWith("cpu MHz")) {
						cpuMHz = Float.parseFloat(str.split(":")[1]);
					}
				}
				if (cpuName.isEmpty()) {
					if (str.startsWith("model name")) {
						cpuName = str.split(":")[1];
					}
				}
			}
			cpuNum = num;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeFile(br);
		}
	}

	private void FindMem() {

		BufferedReader br = openFile("/proc/meminfo");
		StringTokenizer token;
		try {
			token = new StringTokenizer(br.readLine());
			token.nextToken();
			memTotal = Long.parseLong(token.nextToken());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeFile(br);
		}
	}

	private long FindDisk() {

		BufferedReader br = null;
		StringTokenizer token;
		long diskBlock = 0;
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
						diskBlock += Long.parseLong(token.nextToken());
						i = 1;
						continue;
					} else {
						token.nextToken();
					}

				}

				if (token.hasMoreTokens()) {
					diskBlock += Long.parseLong(token.nextToken());
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
		return diskBlock;
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

	void registerMBean() {
		StandardMBean bean;
		try {
			bean = new StandardMBean(this, SystemInfoMBean.class);
			MBeans.register("System", "SystemInfo", bean);
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}
	}
}
