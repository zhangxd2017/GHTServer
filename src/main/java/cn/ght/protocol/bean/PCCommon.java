package cn.ght.protocol.bean;

/**
 * Created by zxd on 2018/1/20.
 */

public class PCCommon {

    private String name;
    private String extraData;

    public PCCommon() {
    }

    public PCCommon(String name, String extraData) {
        this.name = name;
        this.extraData = extraData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
