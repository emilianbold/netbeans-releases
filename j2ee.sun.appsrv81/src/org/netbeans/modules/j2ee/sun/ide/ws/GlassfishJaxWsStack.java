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

package org.netbeans.modules.j2ee.sun.ide.ws;

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
public class GlassfishJaxWsStack implements WSStackSPI {
    private static final String WEBSERVICES_TOOLS_JAR = "lib/webservices-tools.jar"; //NOI18N
    private static final String WEBSERVICES_RT_JAR = "lib/webservices-rt.jar"; //NOI18N
    
    private static final String TOOLS_JAR = "lib/tools.jar"; //NOI18N
    private static final String JSTL_JAR =  "lib/appserv-jstl.jar"; //NOI18N
    private static final String JAVA_EE_JAR = "lib/javaee.jar"; //NOI18N
    private static final String APPSERV_WS_JAR = "lib/appserv-ws.jar"; //NOI18N
    private static final String MAIL_JAR =  "lib/mail.jar"; //NOI18N
    private static final String ACTIVATION_JAR = "lib/activation.jar"; //NOI18N
    
    private File root;
    public GlassfishJaxWsStack(File root) {
        this.root = root;
    }
    
    public String getName() {
        return WSStack.STACK_JAX_WS;
    }
    
    public String getVersion() {
        return "1.2";
    }

    public Set<String> getSupportedTools() {
        Set<String> supportedTools = new HashSet<String>();
        supportedTools.add(WSStack.TOOL_WSGEN);
        supportedTools.add(WSStack.TOOL_WSIMPORT);
        return supportedTools;
    }

    public File[] getToolClassPathEntries(String toolName) {
        if (WSStack.TOOL_WSGEN.equals(toolName) || WSStack.TOOL_WSIMPORT.equals(toolName)) {
            File wsToolsJar = new File(root, WEBSERVICES_TOOLS_JAR);  //NOI18N
            if (wsToolsJar.exists()) { // WSIT installed on top
                return new File[] {
                    new File(root, WEBSERVICES_TOOLS_JAR),     // NOI18N
                    new File(root, WEBSERVICES_RT_JAR),           // NOI18N
                    new File(root, TOOLS_JAR),      //NOI18N
                    new File(root, JSTL_JAR),       //NOI18N
                    new File(root, JAVA_EE_JAR),    //NOI18N
                    new File(root, APPSERV_WS_JAR), //NOI18N
                    new File(root, MAIL_JAR),       //NOI18N
                    new File(root, ACTIVATION_JAR)  //NOI18N
                };
            } else {                                                // regular appserver
                return new File[] {
                    new File(root, TOOLS_JAR),        //NOI18N
                    new File(root, JSTL_JAR),         //NOI18N
                    new File(root, JAVA_EE_JAR),      //NOI18N
                    new File(root, APPSERV_WS_JAR),   //NOI18N
                    new File(root, MAIL_JAR),         //NOI18N
                    new File(root, ACTIVATION_JAR)    //NOI18N
                };
            }
        }
        return new File[]{};
    }

    public WSUriDescriptor getServiceUriDescriptor() {
        return new WSUriDescriptor() {

            public String getServiceUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                if (isEjb) {
                    return serviceName+"/"+portName; //NOI18N
                } else {
                    return applicationRoot+"/"+serviceName; //NOI18N
                }
            }

            public String getWsdlUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl"; //NOI18N
            }

            public String getTesterPageUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?Tester"; //NOI18N
            }
            
        };
    }

    public Set<WSStackFeature> getServiceFeatures() {
        Set<WSStackFeature> wsFeatures = new HashSet<WSStackFeature>();
        wsFeatures.add(WSStackFeature.JSR_109);
        wsFeatures.add(WSStackFeature.SERVICE_REF_INJECTION);
        wsFeatures.add(WSStackFeature.TESTER_PAGE);
        wsFeatures.add(WSStackFeature.WSIT);
        return wsFeatures;
    }

}
