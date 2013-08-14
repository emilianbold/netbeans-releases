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
        wellKnowNames.put("org.apache.cordova.core.device", "Device API");
        wellKnowNames.put("org.apache.cordova.core.network-information", "Network Connection");
        wellKnowNames.put("org.apache.cordova.core.battery-status", "Battery Events");
        wellKnowNames.put("org.apache.cordova.core.device-motion", "Acceleromatter");
        wellKnowNames.put("org.apache.cordova.core.device-orientation", "Compass");
        wellKnowNames.put("org.apache.cordova.core.geolocation", "Geolocation");
        wellKnowNames.put("org.apache.cordova.core.camera", "Camera");
        wellKnowNames.put("org.apache.cordova.core.media-capture", "Media Capture");
        wellKnowNames.put("org.apache.cordova.core.AudioHandler", "Media Playback");
        wellKnowNames.put("org.apache.cordova.core.file", "File API");
        wellKnowNames.put("org.apache.cordova.core.file-transfer", "File Transfer");
        wellKnowNames.put("org.apache.cordova.core.dialogs", "Dialogs (Notifications)");
        wellKnowNames.put("org.apache.cordova.core.vibration", "Vibration");
        wellKnowNames.put("org.apache.cordova.core.contacts", "Contacts");
        wellKnowNames.put("org.apache.cordova.core.globalization", "Globalization");
        wellKnowNames.put("org.apache.cordova.core.splashscreen", "Splashscreen");
        wellKnowNames.put("org.apache.cordova.core.console", "Debugger Console");
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
