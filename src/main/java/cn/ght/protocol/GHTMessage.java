package cn.ght.protocol;

public class GHTMessage {

    public static final int MESSAGE_TYPE_REGISTER = 1;

    public static final int MESSAGE_TYPE_REGISTER_RESULT = 2;

    public static final int MESSAGE_TYPE_TRANSPARENT = 3;

    public static final int MESSAGE_TYPE_TRANSPARENT_ERROR = 4;

    public static final int MESSAGE_TYPE_HEART_BEAT = 0;

    public static final int MESSAGE_TYPE_PHONE_LOGIN = 5;

    public static final int MESSAGE_TYPE_PHONE_LOGIN_RESPONSE = 6;

    public static final int MESSAGE_TYPE_PHONE_TRANSPARENT = 7;

    public static final int MESSAGE_TYPE_PHONE_TRANSPARENT_ERROR = 8;

    public static final GHTMessage MessageRegisterSuccess = new GHTMessage(2, 0);

    public static final GHTMessage MessageRegisterFail = new GHTMessage(2, 1);

    public static final GHTMessage MessageHeartBeat = new GHTMessage(0, 0);

    int messageType;
    Object messageContent;

    public GHTMessage() {
    }

    public GHTMessage(int messageType, Object messageContent) {
        this.messageType = messageType;
        this.messageContent = messageContent;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public Object getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(Object messageContent) {
        this.messageContent = messageContent;
    }
}
