package cn.ght.server.model;

import io.netty.channel.ChannelHandlerContext;

public class Model {

    public String IMEI;

    public String CCID;

    public long latitude;

    public long longitude;

    public ChannelHandlerContext context;

    public Model(String IMEI, String CCID,String location, ChannelHandlerContext context) {
        this.IMEI = IMEI;
        this.CCID = CCID;
        this.context = context;
    }
}
