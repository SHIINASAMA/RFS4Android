package pers.kaoru.rfsclient.service;

import java.io.File;

import pers.kaoru.rfsclient.core.MD5Utils;

public class TaskRecord {

    private final String host;
    private transient int port;
    private transient String token;

    private long current;
    private long length;

    private final String remoteUrl;
    private final String localUrl;
    private final String name;
    private final TaskType type;

    private final long createTime;
    private String uid;

    public TaskRecord(String host, int port, String token, String remoteUrl, String localUrl, TaskType type) {
        this.host = host;
        this.port = port;
        this.token = token;
        this.remoteUrl = remoteUrl;
        this.localUrl = localUrl;
        this.type = type;
        createTime = System.currentTimeMillis();
        if (type == TaskType.DOWNLOAD) {
            this.name = new File(remoteUrl).getName();
        } else {
            this.name = new File(localUrl).getName();
        }
    }

    public String getUid() {
        if (uid == null) {
            uid = MD5Utils.GenerateMD5(createTime + type.name());
        }
        return uid;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getToken() {
        return token;
    }

    public long getCurrent() {
        return current;
    }

    public long getLength() {
        return length;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public String getName() {
        return name;
    }

    public TaskType getType() {
        return type;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public void setLength(long length){
        this.length = length;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
