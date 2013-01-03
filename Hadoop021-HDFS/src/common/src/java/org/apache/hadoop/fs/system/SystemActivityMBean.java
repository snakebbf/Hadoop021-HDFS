package com.coship.hadoop.hdfs.server.system;

public interface SystemActivityMBean {

	public float getCpuUsed();
	
	public float getLoadOne();
	
	public float getLoadFive();
	
	public float getLoadFifteen();
	
	public long getMemFree();
	
	public long getMemBuffer();
	
	public long getMemCached();
	
	public long getDiskUsed();
	
	public float getInNetByte();

	public float getOutNetByte();
}

