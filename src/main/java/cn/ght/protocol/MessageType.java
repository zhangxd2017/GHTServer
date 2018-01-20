package cn.ght.protocol;

public class MessageType {


    /**
     * 服务器通用协议CMD字段
     */
    public static final String REGISTER = "register";

    /**
     * 服务器与模块的协议CMD字段
     */
    public static final String POWER_ON_PC = "power_on_pc";
    public static final String REPORT_LBS = "report_lbs";
    public static final String REPORT_STATE = "report_state";

    /**
     * 服务器与PC客户端协议CMD字段
     */
    public static final String FILE_LIST = "file_list";
    public static final String DELETE_FILE = "delete_file";
    public static final String POWER_OFF_PC = "power_off_pc";

    /**
     * 服务器与移动客户端协议CMD字段
     */
    public static final String GET_MODULE_LIST = "get_module_list";
    public static final String SET_POWER_ON_PC = "set_power_on_pc";
    public static final String GET_FILE_LIST = "get_file_list";
    public static final String SET_DELETE_FILE = "set_delete_file";
    public static final String GET_LOCATION_INFO = "get_location_info";
    public static final String SET_POWER_OFF_PC = "set_power_off_pc";
}
