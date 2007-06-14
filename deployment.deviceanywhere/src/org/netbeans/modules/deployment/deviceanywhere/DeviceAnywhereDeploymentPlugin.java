/*
 * DeviceAnywhereDeploymentPlugin.java
 *
 */

package org.netbeans.modules.deployment.deviceanywhere;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * @author Adam Sotona
 */
public class DeviceAnywhereDeploymentPlugin implements DeploymentPlugin {
    
    static final String PROP_DEVICE = "deployment.deviceanywhere.device"; //NOI18N
    static final String PROP_AVAILABLE_DEVICES = "deployment.deviceanywhere.availabledevices"; //NOI18N
    static final String PROP_USERID = "deployment.deviceanywhere.userid"; //NOI18N
    static final String PROP_PASSWORD = "deployment.deviceanywhere.password"; //NOI18N
    
    final Map propertyDefValues;
    
    private DeviceAnywhereCustomizerPanel panel = null;
    
    /**
     * Creates a new instance of DeviceAnywhereDeploymentPlugin
     */
    public DeviceAnywhereDeploymentPlugin() {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put(PROP_DEVICE, "");//NOI18N
        m.put(PROP_USERID, "");//NOI18N
        m.put(PROP_PASSWORD, "");//NOI18N
        m.put(PROP_AVAILABLE_DEVICES, "");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }

    public String getAntScriptLocation() {
        return "modules/scr/deploy-deviceanywhere-impl.xml"; // NOI18N
    }

    public String getDeploymentMethodName() {
        return "DeviceAnywhere"; // NOI18N
    }

    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(DeviceAnywhereDeploymentPlugin.class, "LBL_DeviceAnywhereTypeName"); //NOI18N
    }

    public Component createProjectCustomizerPanel() {
        return new DeviceAnywhereCustomizerPanel();
    }
    
    public Map<String, Object> getProjectPropertyDefaultValues() {
        return propertyDefValues;
    }
    
    public Map<String, Object> getGlobalPropertyDefaultValues() {
        return Collections.EMPTY_MAP;
    }
    
    public Component createGlobalCustomizerPanel() {
        return null;
    }
}
