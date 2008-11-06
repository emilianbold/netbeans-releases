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
 * The Original Software is the Nokia Deployment.                      
 * The Initial Developer of the Original Software is Nokia Corporation.
 * Portions created by Nokia Corporation Copyright 2005, 2007.         
 * All Rights Reserved.                                                
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

package org.netbeans.modules.mobility.deployment.nokia;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import com.nokia.phone.deploy.*;
import java.util.Collections;
import javax.swing.JLabel;

/**
 * Provides deployment functionality to Nokia devices.
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.deployment.DeploymentPlugin.class, position=104)
public class NokiaDeploymentPlugin implements DeploymentPlugin {

    static final String PROP_DEPLOY_TO_ALL_DEVICES = "deployment.nokia.deploytoalldevices"; //NOI18N
    static final String PROP_DEPLOY_TO_SELECTED_DEVICES = "deployment.nokia.deploytoselecteddevices"; //NOI18N
    static final String PROP_SELECTED_DEVICES = "deployment.nokia.selecteddevices"; //NOI18N
    final Map<String,Object> propertyDefValues;

    Deployer deployer = null;
    
    /** Creates a new instance of TestDeploymentPlugin */
    public NokiaDeploymentPlugin() {
        deployer = new Deployer();
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(PROP_DEPLOY_TO_ALL_DEVICES, "yes");//NOI18N
        m.put(PROP_SELECTED_DEVICES, "");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }
    
    public String getDeploymentMethodName() {
        return "NokiaDeployment"; //NOI18N
    }

    public String getDeploymentMethodDisplayName() {
        return org.openide.util.NbBundle.getMessage(NokiaDeploymentPlugin.class, "DeploymentPlugin.MethodDisplayName");
    }

    public String getAntScriptLocation() {
        return "modules/scr/deploy-nokia-impl.xml"; //NOI18N
    }

    public Map<String,Object> getProjectPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Map<String,Object> getGlobalPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Component createProjectCustomizerPanel() {
        return null;
    }

    public Component createGlobalCustomizerPanel() {
        if(!deployer.isOSSupportsDeployment()) {
              return new JLabel(org.openide.util.NbBundle.getMessage(NokiaDeploymentPlugin.class,
                                                                     "NokiaDeploymentPlugin.OSNotSupported"));
        } else if(!deployer.isPCSuiteInstalled() || !deployer.isConnected()) {
              return new JLabel(org.openide.util.NbBundle.getMessage(NokiaDeploymentPlugin.class,
                                                                     "NokiaDeploymentPlugin.PCSuiteNotAvailable"));
        } else {
            return new NokiaProjectCustomizerPanel(deployer);
        }
    }
}
