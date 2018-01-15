package cn.ght.protocol;

public class MessageData {

    private String cmd;

    private String data;

    public MessageData() {
    }

    public MessageData(String cmd, String data) {
        this.cmd = cmd;
        this.data = data;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
