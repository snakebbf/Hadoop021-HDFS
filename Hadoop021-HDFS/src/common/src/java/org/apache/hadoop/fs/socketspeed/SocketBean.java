package com.coship.hadoop.hdfs.server.socketspeed;

public class SocketBean {

	private String type = "";
	private String localAddress = "" ;
	private String remoteAddress = "" ;
	private String remoteAddressPort = "" ;
	private long startTime = 0 ;
	private long endTime = 0 ;
	private long speed = 0 ;
	private long fileSize = 0 ;
	public SocketBean(String localAddress , String remoteAddress){
		changAddress(localAddress , remoteAddress);
	}
	
	private void changAddress(String localAddress , String remoteAddress){
		String[] local = localAddress.split(":");
		this.localAddress = local[0].substring(1);
	    String[] remote = remoteAddress.split(":");
	    this.remoteAddress = remote[0].substring(1);
	    this.remoteAddressPort = remote[1];
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime2) {
		this.startTime = startTime2;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getSpeed() {
		return speed;
	}

	public void setSpeed(long speed) {
		this.speed = speed;
	}
	
	public void addFileSize(long size){
		fileSize += size ;
	}

	public long getFileSize() {
		return fileSize;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getRemoteAddressPort() {
		return remoteAddressPort;
	}

	public void setRemoteAddressPort(String remoteAddressPort) {
		this.remoteAddressPort = remoteAddressPort;
	}
	
}
