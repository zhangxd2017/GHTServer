package cn.ght.server.manager;

import cn.ght.protocol.GHTMessage;
import cn.ght.server.model.Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModelManager {

    private static final Object locker = new Object();

    private static ModelManager ourInstance = new ModelManager();

    public static ModelManager getInstance() {
        return ourInstance;
    }

    private ModelManager() {
        modelList = new ArrayList<>();
    }

    private List<Model> modelList;

    public void addModel(Model model) {
        synchronized (locker) {
            for (int i=0;i < modelList.size(); i++) {
                if (modelList.get(i).IMEI.equalsIgnoreCase(model.IMEI)){
                    modelList.remove(i);
                }
            }
            modelList.add(model);
        }
    }

    public void removeModel(Model model) {
        synchronized (locker) {
            for (Model model1 : modelList) {
                if (model1.IMEI.equalsIgnoreCase(model.IMEI)) {
                    modelList.remove(model1);
                }
            }
        }
    }

    public void clearModel() {
        synchronized (locker) {
            for (Model model : modelList) {
                modelList.remove(model);
            }
        }
    }

    public byte[] getList() {
        if (modelList.size() > 0) {
            synchronized (locker) {
                List<Byte> data = new ArrayList<>();
                for (int i = 0; i < modelList.size(); i++) {
                    data.add((byte) (i + 1));
                    int lengthIMEI = modelList.get(i).IMEI.length();
                    data.add((byte) lengthIMEI);
                    byte[] imeiBytes = modelList.get(i).IMEI.getBytes();
                    for (int j = 0; j < lengthIMEI; j++) {
                        data.add(imeiBytes[j]);
                    }
                    int lengthCCID = modelList.get(i).CCID.length();
                    data.add((byte) lengthCCID);
                    byte[] ccidBytes = modelList.get(i).CCID.getBytes();
                    for (int k = 0; k < lengthCCID; k++) {
                        data.add(ccidBytes[k]);
                    }
                    String lac = modelList.get(i).latitude + "*" + modelList.get(i).longitude;
                    data.add((byte) lac.length());
                    byte[] lacBytes = lac.getBytes();
                    for (int l = 0; l < lac.length(); l++) {
                        data.add(lacBytes[l]);
                    }
                }
                byte[] bytes = new byte[data.size()];
                int i = 0;
                Iterator<Byte> iterator = data.iterator();
                while (iterator.hasNext()) {
                    bytes[i] = iterator.next();
                    i++;
                }
                return bytes;
            }
        } else {
            return new byte[]{0};
        }
    }

    public void sendMessage(String imei, GHTMessage message) {
        synchronized (locker) {
            for (Model model : modelList) {
                if (model.IMEI.equalsIgnoreCase(imei)) {
                    model.context.writeAndFlush(message);
                }
            }
        }
    }
}
