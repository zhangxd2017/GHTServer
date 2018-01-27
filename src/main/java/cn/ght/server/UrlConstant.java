package cn.ght.server;

import cn.ght.util.StringUtils;

import java.util.Locale;

public class UrlConstant {

    public static final String GPSSPG_URL = "http://api.gpsspg.com/bs/?";

    public static String getParams(String lbsInfo) {

        String MCC = "";
        String MNC = "";
        String LAC = "";
        String CI = "";

        String[] infos = lbsInfo.split(",");
        for (int i = 0; i < infos.length; i++) {
            String info = infos[i];
            if (info.startsWith("MCC")) {
                MCC = info.substring(info.indexOf(":") + 1);
            } else if (info.startsWith("MNC")) {
                MNC = info.substring(info.indexOf(":") + 1);
            } else if (info.startsWith("LAC")) {
                LAC = info.substring(info.indexOf(":") + 1);
            } else if (info.startsWith("CELL_ID")) {
                CI = info.substring(info.indexOf(":") + 1);
                if (CI.startsWith("0")) {
                    CI = CI.substring(1);
                }
            }
        }

        if (StringUtils.isEmpty(MCC) || StringUtils.isEmpty(MNC) || StringUtils.isEmpty(LAC) || StringUtils.isEmpty(CI)) {
            return "";
        }

        return String.format(Locale.getDefault(), "oid=7242&key=CD4761DA18F87458756EB8BF538636DB&bs=%s,%s,%s,%s&hex=16&output=json", MCC, MNC, LAC, CI);
    }

    public static String getUrl(String lbsInfo) {
        String params = getParams(lbsInfo);
        return StringUtils.isEmpty(params) ? "" : GPSSPG_URL + params;
    }
}
