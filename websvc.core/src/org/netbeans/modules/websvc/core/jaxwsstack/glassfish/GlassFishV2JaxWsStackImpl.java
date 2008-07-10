/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.core.jaxwsstack.glassfish;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.modules.websvc.core.jaxwsstack.JaxWs;
import org.netbeans.modules.websvc.core.jaxwsstack.WSUriDescriptor;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;

/**
 *
 * @author mkuchtiak
 */
public class GlassFishV2JaxWsStackImpl implements WSStackImplementation<JaxWs> {

    private JaxWs jaxWs;
    private File root;
    private WSStackVersion version;
    
    public GlassFishV2JaxWsStackImpl(File root) {
        this.root = root;
        try {
            version = WSStackFactory.createWSStackVersion(resolveImplementationVersion());
            if (version == null) {
                // Default Version
                version = WSStackVersion.valueOf(2,1,3,0);
            }
        } catch (IOException ex) {
            // Default Version
            version = WSStackVersion.valueOf(2,1,3,0);
        };
    }
    
    public void initJaxWsDescriptor() {
        jaxWs = new JaxWs();
        WSUriDescriptor wsUriDescriptor = new WSUriDescriptor() {

            public String getServiceUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                if (isEjb) {
                    return serviceName+"/"+portName; //NOI18N
                } else {
                    return applicationRoot+"/"+serviceName; //NOI18N
                }
            }

            public String getDescriptorUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl"; //NOI18N
            }

            public String getTesterPageUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?Tester"; //NOI18N
            }

        };
        jaxWs.setWSUriDescriptor(wsUriDescriptor);
    }
    
    public JaxWs get() {
        return jaxWs;
    }

    public WSStackVersion getVersion() {
        return version;
    }

    public WSTool getWSTool(Tool toolId) {
        return WSStackFactory.createWSTool(new GlassFishV2JaxWsTool(root, toolId));
    }

    public boolean isFeatureSupported(Feature feature) {
        return (feature == JaxWs.Feature.JSR_109 ||
                feature == JaxWs.Feature.SERVICE_REF_INJECTION || 
                feature == JaxWs.Feature.TESTER_PAGE || 
                feature == JaxWs.Feature.WSIT);
    }
    
    private String resolveImplementationVersion() throws IOException {
        // take webservices-tools.jar file
        File wsToolsJar = new File(root, GlassFishV2JaxWsTool.WEBSERVICES_TOOLS_JAR);
        // alternatively take appserv-ws.jar file
        if (!wsToolsJar.exists()) wsToolsJar = new File(root, GlassFishV2JaxWsTool.APPSERV_WS_JAR);
        
        if (wsToolsJar.exists()) {            
            JarFile jarFile = new JarFile(wsToolsJar);
            JarEntry entry = jarFile.getJarEntry("com/sun/tools/ws/version.properties"); //NOI18N
            if (entry != null) {
                InputStream is = jarFile.getInputStream(entry);
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                String ln = null;
                String ver = null;
                while ((ln=r.readLine()) != null) {
                    String line = ln.trim();
                    if (line.startsWith("major-version=")) { //NOI18N
                        ver = line.substring(14);
                    }
                }
                r.close();
                return ver;
            }           
        }
        return null;
    }

}
