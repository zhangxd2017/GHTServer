package cn.ght.server.manager;

import cn.ght.protocol.GHTMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class MobileManager {
    private static MobileManager ourInstance = new MobileManager();

    public static MobileManager getInstance() {
        return ourInstance;
    }

    private MobileManager() {
        mobileList = new ArrayList<>();
    }

    private List<ChannelHandlerContext> mobileList;

    public void sendMessage(GHTMessage message) {

    }

    public void addMobile(ChannelHandlerContext channelHandlerContext) {
    }
}
