/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * WebDAVDeploymentPlugin.java
 *
 */
package org.netbeans.modules.mobility.deployment.webdav;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.deployment.DeploymentPlugin.class, position=40)
public class WebDAVDeploymentPlugin implements DeploymentPlugin {
    
    static final String PROP_SERVER = "deployment.webdav.server"; //NOI18N
    static final String PROP_PORT = "deployment.webdav.port"; //NOI18N
    static final String PROP_REMOTEDIR = "deployment.webdav.remotedir"; //NOI18N
    static final String PROP_USERID = "deployment.webdav.userid"; //NOI18N
    static final String PROP_PASSWORD = "deployment.webdav.password"; //NOI18N
    
    final Map<String,Object> propertyDefValues;
    
    /** Creates a new instance of WebDAVDeploymentPlugin */
    public WebDAVDeploymentPlugin() {
        HashMap<String,Object> m = new HashMap<String,Object>();
        m.put(PROP_SERVER, "");//NOI18N
        m.put(PROP_PORT, Integer.valueOf("80"));
        m.put(PROP_USERID, "");//NOI18N
        m.put(PROP_PASSWORD, "");//NOI18N
        propertyDefValues = Collections.unmodifiableMap(m);
    }
    
    public String getAntScriptLocation() {
        return "modules/scr/deploy-webdav-impl.xml"; // NOI18N
    }
    
    public String getDeploymentMethodName() {
        return "WebDAV"; // NOI18N
    }
    
    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(WebDAVDeploymentPlugin.class, "LBL_WebDAVTypeName"); //NOI18N
    }
    
    public synchronized Component createProjectCustomizerPanel() {
        return new WebDAVProjectCustomizerPanel();
    }
    
    public Map<String,Object> getProjectPropertyDefaultValues() {
        return Collections.singletonMap(PROP_REMOTEDIR, (Object)"");//NOI18N
    }

    public Map<String, Object> getGlobalPropertyDefaultValues() {
        return propertyDefValues;
    }

    public Component createGlobalCustomizerPanel() {
        return new WebDAVCustomizerPanel();
    }
}
