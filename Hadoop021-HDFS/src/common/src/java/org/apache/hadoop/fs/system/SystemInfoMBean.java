package com.coship.hadoop.hdfs.server.system;

public interface SystemInfoMBean {
	
	public String getMachineType();
	
	public String getOsName();
	
	public String getOsRelease();
	
	public int getCpuNum();
	
	public float getCpuMHz();
	
	public String getCpuName();
	
	public long getMemTotal();
	
	public long getDiskTotal();
	
}
