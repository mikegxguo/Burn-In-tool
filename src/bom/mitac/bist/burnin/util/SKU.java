package bom.mitac.bist.burnin.util;

import android.util.Log;
import bom.mitac.bist.burnin.module.BISTApplication;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-5-7
 * Time: 5:21PM
 */
public class SKU {
    private static final File DEFAULT_FILE = new File(BISTApplication.BASE_PATH, "sku.xml");

    public static class Filter {
        @Attribute
        public int ID;
        @Element
        public boolean Vibrator;
        @Element
        public boolean Battery;
        @Element
        public boolean BT;
        @Element
        public boolean NFC;
        @Element
        public boolean SD;
        @Element
        public boolean iNAND;
        @Element
        public boolean Wifi;
        @Element
        public boolean Video;
        @Element
        public boolean BKL;
        @Element
        public boolean Cellular;
        @Element
        public boolean BCR;
        @Element
        public boolean Camera_back;
        @Element
        public boolean Camera_front;
        @Element
        public boolean Sensor;
        @Element
        public boolean GPS;
        @Element
        public boolean Flash;
        @Element
        public boolean Suspend;
        @Element
        public boolean USB;
    }

    @ElementList
    private List<Filter> filterList = new ArrayList<Filter>(10);

    public static SKU read() {
        SKU sku = null;
        if (DEFAULT_FILE.exists()) {
            Serializer serializer = new Persister();
            try {
                sku = serializer.read(SKU.class, DEFAULT_FILE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sku;
    }

    public Filter getFilter() {
        for (Filter filter : filterList) {
            if (filter.ID == SystemInformation.getSKUID()) {
                return filter;
            }
        }
        return null;
    }

    public synchronized void write() {
        Serializer serializer = new Persister();
        try {
            serializer.write(this, DEFAULT_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
