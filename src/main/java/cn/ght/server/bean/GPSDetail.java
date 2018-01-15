package cn.ght.server.bean;

import java.util.List;

/**
 * Created by zxd on 2018/1/15.
 */

public class GPSDetail {


    /**
     * status : 200
     * msg : ok
     * count : 1
     * result : [{"id":"460-000-09339-000003600","lat":"22.49792633","lng":"113.91327905","radius":"711","address":"广东省深圳市南山区招商街道科技大厦2期","roads":"南海大道西南约55米","lats":"22.494896","lngs":"113.918154","rid":"440305","rids":"440305006000"}]
     * latitude : 22.49792633
     * longitude : 113.91327905
     * match : 1
     */

    private int status;
    private String msg;
    private int count;
    private double latitude;
    private double longitude;
    private String match;
    private List<ResultBean> result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * id : 460-000-09339-000003600
         * lat : 22.49792633
         * lng : 113.91327905
         * radius : 711
         * address : 广东省深圳市南山区招商街道科技大厦2期
         * roads : 南海大道西南约55米
         * lats : 22.494896
         * lngs : 113.918154
         * rid : 440305
         * rids : 440305006000
         */

        private String id;
        private String lat;
        private String lng;
        private String radius;
        private String address;
        private String roads;
        private String lats;
        private String lngs;
        private String rid;
        private String rids;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public String getRadius() {
            return radius;
        }

        public void setRadius(String radius) {
            this.radius = radius;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getRoads() {
            return roads;
        }

        public void setRoads(String roads) {
            this.roads = roads;
        }

        public String getLats() {
            return lats;
        }

        public void setLats(String lats) {
            this.lats = lats;
        }

        public String getLngs() {
            return lngs;
        }

        public void setLngs(String lngs) {
            this.lngs = lngs;
        }

        public String getRid() {
            return rid;
        }

        public void setRid(String rid) {
            this.rid = rid;
        }

        public String getRids() {
            return rids;
        }

        public void setRids(String rids) {
            this.rids = rids;
        }
    }
}
