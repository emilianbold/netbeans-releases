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
 * ScpDeploymentPlugin.java
 *
 */
package org.netbeans.modules.mobility.deployment.ftpscp;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * @author Adam Sotona
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.deployment.DeploymentPlugin.class, position=30)
public class ScpDeploymentPlugin implements DeploymentPlugin {
    
    static final String PROP_SERVER = "deployment.scp.server"; //NOI18N
    static final String PROP_PORT = "deployment.scp.port"; //NOI18N
    static final String PROP_REMOTEDIR = "deployment.scp.remotedir"; //NOI18N
    static final String PROP_USERID = "deployment.scp.userid"; //NOI18N
    static final String PROP_PASSWORD = "deployment.scp.password"; //NOI18N
    static final String PROP_PASSPHRASE = "deployment.scp.passphrase"; //NOI18N
    static final String PROP_USE_KEYFILE = "deployment.scp.usekeyfile"; //NOI18N
    static final String PROP_KEYFILE = "deployment.scp.keyfile"; //NOI18N
    
    final Map<String,Object> propertyDefValues;
    
    /** Creates a new instance of ScpDeploymentPlugin */
    public ScpDeploymentPlugin() {
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(PROP_SERVER, "");//NOI18N
        m.put(PROP_PORT, Integer.valueOf("22"));
        m.put(PROP_USERID, "");//NOI18N
        m.put(PROP_PASSWORD, "");//NOI18N
        m.put(PROP_PASSPHRASE, "");//NOI18N
        m.put(PROP_KEYFILE, new File(""));//NOI18N
        m.put(PROP_USE_KEYFILE, "no");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }
    
    public String getAntScriptLocation() {
        return "modules/scr/deploy-scp-impl.xml"; // NOI18N
    }
    
    public String getDeploymentMethodName() {
        return "Scp";
    }
    
    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(ScpDeploymentPlugin.class, "LBL_ScpTypeName"); //NOI18N
    }
    
    public synchronized Component createProjectCustomizerPanel() {
        return new ScpProjectCustomizerPanel();
    }
    
    public Map<String,Object> getProjectPropertyDefaultValues() {
        return Collections.singletonMap(PROP_REMOTEDIR, (Object)"");//NOI18N
    }

    public Map<String, Object> getGlobalPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Component createGlobalCustomizerPanel() {
        return new ScpCustomizerPanel();
    }
}
