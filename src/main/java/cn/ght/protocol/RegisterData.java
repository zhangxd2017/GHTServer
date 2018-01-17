package cn.ght.protocol;

public class RegisterData {

    public static final int STATE_SUCCESS = 0;

    public static final int STATE_FAIL = 1;

    private int state;

    private String reason;

    public RegisterData() {
    }

    public RegisterData(int state, String reason) {
        this.state = state;
        this.reason = reason;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
