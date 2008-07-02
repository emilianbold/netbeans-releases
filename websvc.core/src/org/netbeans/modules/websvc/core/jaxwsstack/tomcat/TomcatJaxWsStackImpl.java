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

package org.netbeans.modules.websvc.core.jaxwsstack.tomcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
public class TomcatJaxWsStackImpl implements WSStackImplementation<JaxWs> {
    
    private static final String WSIT_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",   // NOI18N
        "shared/lib/webservices-tools.jar" // NOI18N
    };
    
    private static final String KEYSTORE_LOCATION = "certs/server-keystore.jks";  //NOI18N
    private static final String TRUSTSTORE_LOCATION = "certs/server-truststore.jks";  //NOI18N
    private static final String KEYSTORE_CLIENT_LOCATION = "certs/client-keystore.jks";  //NOI18N
    private static final String TRUSTSTORE_CLIENT_LOCATION = "certs/client-truststore.jks";  //NOI18N

    private JaxWs jaxWs;
    private File catalinaHome;
    private WSStackVersion version;
    
    public TomcatJaxWsStackImpl(File catalinaHome) {
        this.catalinaHome = catalinaHome;
        initJaxWsDescriptor();
        try {
            version = WSStackFactory.createWSStackVersion(resolveImplementationVersion());
            if (version == null) {
                // Default Version
                version = WSStackVersion.valueOf(2,1,4,0);
            }
        } catch (IOException ex) {
            // Default Version
            version = WSStackVersion.valueOf(2,1,4,0);
        };
    }
    
    public void initJaxWsDescriptor() {
        jaxWs = new JaxWs();
        WSUriDescriptor wsUriDescriptor = new WSUriDescriptor() {

            public String getServiceUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return applicationRoot+"/"+serviceName; //NOI18N
            }

            public String getDescriptorUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl"; //NOI18N
            }

            public String getTesterPageUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return applicationRoot+"/"+serviceName; //NOI18N
            }

        };
        jaxWs.setWSUriDescriptor(wsUriDescriptor);
        File keystore = new File(catalinaHome, KEYSTORE_LOCATION);
        if (keystore.exists()) jaxWs.setKeystore(keystore);
        keystore = new File(catalinaHome, KEYSTORE_CLIENT_LOCATION);
        if (keystore.exists()) jaxWs.setKeystoreClient(keystore);
        keystore = new File(catalinaHome, TRUSTSTORE_LOCATION);
        if (keystore.exists()) jaxWs.setTruststore(keystore);
        keystore = new File(catalinaHome, TRUSTSTORE_CLIENT_LOCATION);
        if (keystore.exists()) jaxWs.setTruststoreClient(keystore);
    }
    
    public JaxWs get() {
        return jaxWs;
    }

    public WSStackVersion getVersion() {
        return version;
    }

    public WSTool getWSTool(Tool toolId) {
        if (isWsit()) {
                URL[] libraries = new URL[WSIT_LIBS.length];
                for (int i = 0; i < WSIT_LIBS.length; i++) {
                    try {
                        libraries[i] = new File(catalinaHome, WSIT_LIBS[i]).toURI().toURL();
                    } catch (MalformedURLException ex) {}
                }
                return WSStackFactory.createWSTool(new TomcatJaxWsTool(toolId, libraries));
        }
        return null;
    }

    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxWs.Feature.WSIT) return isWsit();
        else if (feature == JaxWs.Feature.TESTER_PAGE) return true;
        return false;
    }
    
    private String resolveImplementationVersion() throws IOException {
        File wsToolsJar = new File(catalinaHome, "shared/lib/webservices-tools.jar"); //NOI18N
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
    
    private boolean isWsit() {
        boolean wsit = true;
        for (int i = 0; i < WSIT_LIBS.length; i++) {
            if (!new File(catalinaHome, WSIT_LIBS[i]).exists()) {
                wsit = false;
            }
        }
        return wsit;
    }

}
