/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * DeviceAnywhereDeploymentPlugin.java
 *
 */

package org.netbeans.modules.deployment.deviceanywhere;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 * @author Petr Suchomel
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.deployment.DeploymentPlugin.class, position=100)
public class DeviceAnywhereDeploymentPlugin implements DeploymentPlugin, CustomizerPanel {
    
    static final String PROP_DEVICE = "deployment.deviceanywhere.device"; //NOI18N
    static final String PROP_AVAILABLE_DEVICES = "deployment.deviceanywhere.availabledevices"; //NOI18N
    static final String PROP_USERID = "deployment.deviceanywhere.userid"; //NOI18N
    static final String PROP_PASSWORD = "deployment.deviceanywhere.password"; //NOI18N
    static final String PROP_CAREER = "deployment.deviceanywhere.career"; //NOI18N
    
    final Map<String, Object> propertyDefValues;
    final Map<String, Object> globalPropertyDefValues;
    
    private ProjectProperties props;
    private String configuration;
    
    private PropertyEvaluator evaluator = new PropertyEvaluator();
    /**
     * Creates a new instance of DeviceAnywhereDeploymentPlugin
     */
    public DeviceAnywhereDeploymentPlugin() {
        HashMap<String, Object> m = new HashMap<String, Object>();
        m.put(PROP_USERID, "");//NOI18N
        m.put(PROP_PASSWORD, "");//NOI18N
        globalPropertyDefValues = Collections.unmodifiableMap(m);
        m = new HashMap<String, Object>();
        m.put(PROP_DEVICE, "");//NOI18N
        m.put(PROP_CAREER, "");//NOI18N
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
        return new DeviceAnywhereCustomizerPanel(evaluator);
    }
    
    public Map<String, Object> getProjectPropertyDefaultValues() {
        return propertyDefValues;
    }
    
    public Map<String, Object> getGlobalPropertyDefaultValues() {
        return globalPropertyDefValues;
    }
    
    public Component createGlobalCustomizerPanel() {
        return new DeviceAnywhereGlobalCustomizerPanel();
    }

    public void initValues(ProjectProperties props, String configuration) {
        this.props = props;
        this.configuration = configuration;
    }
    
    class PropertyEvaluator {
        String evaluateProperty (String propertyName) {
            assert props != null : "Project Properties can not be null!"; //NOI18N
            if (configuration == null)
                return (String) props.get(propertyName);
            String value = (String) props.get("configs." + configuration + "." + propertyName); // NOI18N
            return value != null ? value : evaluateProperty (propertyName, null);
        }

        private String evaluateProperty (String propertyName, String configuration) {
            //deployment.instance
            if (configuration == null)
                return (String) props.get(propertyName);
            String value = (String) props.get("configs." + configuration + "." + propertyName); // NOI18N
            return value != null ? value : evaluateProperty (propertyName, null);
        }
        
        String evaluateGlobalProperty (String propertyName, String instanceName) {
            EditableProperties ep = PropertyUtils.getGlobalProperties();
            String prefix = "deployments.DeviceAnywhere.";
            String evaluatedPropertyName = prefix + ((instanceName == null) ? "default" : instanceName) + '.' + propertyName; //NOI18N
            String value = ep.getProperty(evaluatedPropertyName); // NOI18N
            return value != null ? value : evaluateGlobalProperty (propertyName, null);
        }
    }    
}
