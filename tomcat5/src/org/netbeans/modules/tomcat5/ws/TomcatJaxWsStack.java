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

package org.netbeans.modules.tomcat5.ws;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.websvc.serverapi.api.WSStackFeature;
import org.netbeans.modules.websvc.serverapi.api.WSStack;
import org.netbeans.modules.websvc.serverapi.api.WSUriDescriptor;
import org.netbeans.modules.websvc.serverapi.spi.WSStackSPI;

/**
 *
 * @author mkuchtiak
 */
public class TomcatJaxWsStack implements WSStackSPI {
    
    private static final String WSIT_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",   // NOI18N
        "shared/lib/webservices-tools.jar" // NOI18N
    };
    
    private static final String KEYSTORE_LOCATION = "certs/server-keystore.jks";  //NOI18N
    private static final String TRUSTSTORE_LOCATION = "certs/server-truststore.jks";  //NOI18N
    private static final String KEYSTORE_CLIENT_LOCATION = "certs/client-keystore.jks";  //NOI18N
    private static final String TRUSTSTORE_CLIENT_LOCATION = "certs/client-truststore.jks";  //NOI18N
    
    File catalinaHome;
    
    public TomcatJaxWsStack(File catalinaHome) {
        this.catalinaHome = catalinaHome;
    }
    
    public String getName() {
        return WSStack.STACK_JAX_WS;
    }
    
    public String getVersion() {
        return "1.2";
    }

    public Set<String> getSupportedTools() {
        Set<String> supportedTools = new HashSet<String>();
        if (isWsit()) {
            supportedTools.add(WSStack.TOOL_WSGEN);
            supportedTools.add(WSStack.TOOL_WSIMPORT);
        }
        if (isKeystore()) supportedTools.add(WSStack.TOOL_KEYSTORE);
        if (isTruststore()) supportedTools.add(WSStack.TOOL_TRUSTSTORE);
        if (isKeystoreClient()) supportedTools.add(WSStack.TOOL_KEYSTORE_CLIENT);
        if (isTruststoreClient()) supportedTools.add(WSStack.TOOL_TRUSTSTORE_CLIENT);
        return supportedTools;
    }

    public File[] getToolClassPathEntries(String toolName) {
        if (WSStack.TOOL_WSGEN.equals(toolName) || WSStack.TOOL_WSIMPORT.equals(toolName)) {
            if (isWsit()) {
                File[] retValue = new File[WSIT_LIBS.length];
                for (int i = 0; i < WSIT_LIBS.length; i++) {
                    retValue[i] = new File(catalinaHome, WSIT_LIBS[i]);
                }
                return retValue; 
            }                     
        } else if (WSStack.TOOL_KEYSTORE.equals(toolName) && isKeystore()) {
            return new File[]{new File(catalinaHome, KEYSTORE_LOCATION)};
        } else if (WSStack.TOOL_TRUSTSTORE.equals(toolName) && isTruststore()) {
            return new File[]{new File(catalinaHome, TRUSTSTORE_LOCATION)};
        } else if (WSStack.TOOL_KEYSTORE_CLIENT.equals(toolName) && isKeystoreClient()) {
            return new File[]{new File(catalinaHome, KEYSTORE_CLIENT_LOCATION)};
        } else if (WSStack.TOOL_TRUSTSTORE_CLIENT.equals(toolName) && isTruststoreClient()) {
            return new File[]{new File(catalinaHome, TRUSTSTORE_CLIENT_LOCATION)};
        }
        
        return new File[]{};
    }
    
    public WSUriDescriptor getServiceUriDescriptor() {
        return new WSUriDescriptor() {

            public String getServiceUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return applicationRoot+"/"+serviceName; //NOI18N
            }

            public String getWsdlUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl"; //NOI18N
            }
            
            public String getTesterPageUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return applicationRoot+"/"+serviceName; //NOI18N
            }
            
        };
    }

    public Set<WSStackFeature> getServiceFeatures() {
        Set<WSStackFeature> wsFeatures = new HashSet<WSStackFeature>();
        wsFeatures.add(WSStackFeature.TESTER_PAGE);
        if (isWsit()) {
            wsFeatures.add(WSStackFeature.WSIT);
        }
        return wsFeatures;
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
    
    private boolean isKeystore() {
        if (new File(catalinaHome, KEYSTORE_LOCATION).exists()) return true;
        else return false;
    }
    private boolean isKeystoreClient() {
        if (new File(catalinaHome, KEYSTORE_CLIENT_LOCATION).exists()) return true;
        else return false;
    }
    
    private boolean isTruststore() {
        if (new File(catalinaHome, TRUSTSTORE_LOCATION).exists()) return true;
        else return false;
    }
    private boolean isTruststoreClient() {
        if (new File(catalinaHome, TRUSTSTORE_CLIENT_LOCATION).exists()) return true;
        else return false;
    }
    
}
