/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cordova.updatetask;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jan Becicka
 */
public class CordovaPlugin {
    private String id;
    private String url;
    
    private static final Map<String, String> wellKnowNames = new HashMap<String, String>();
    
    static {
        wellKnowNames.put("cordova-plugin-device", "Device API");
        wellKnowNames.put("cordova-plugin-network-information", "Network Connection");
        wellKnowNames.put("cordova-plugin-battery-status", "Battery Events");
        wellKnowNames.put("cordova-plugin-device-motion", "Acceleromatter");
        wellKnowNames.put("cordova-plugin-device-orientation", "Compass");
        wellKnowNames.put("cordova-plugin-geolocation", "Geolocation");
        wellKnowNames.put("cordova-plugin-camera", "Camera");
        wellKnowNames.put("cordova-plugin-media-capture", "Media Capture");
        wellKnowNames.put("cordova-plugin-media", "Media");
        wellKnowNames.put("cordova-plugin-file", "File API");
        wellKnowNames.put("cordova-plugin-file-transfer", "File Transfer");
        wellKnowNames.put("cordova-plugin-dialogs", "Dialogs (Notifications)");
        wellKnowNames.put("cordova-plugin-vibration", "Vibration");
        wellKnowNames.put("cordova-plugin-contacts", "Contacts");
        wellKnowNames.put("cordova-plugin-globalization", "Globalization");
        wellKnowNames.put("cordova-plugin-splashscreen", "Splashscreen");
        wellKnowNames.put("cordova-plugin-console", "Debugger Console");
   }

    public CordovaPlugin(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
    
    public String getName() {
        String name = wellKnowNames.get(id);
        return name!=null?name:getId();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CordovaPlugin other = (CordovaPlugin) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
