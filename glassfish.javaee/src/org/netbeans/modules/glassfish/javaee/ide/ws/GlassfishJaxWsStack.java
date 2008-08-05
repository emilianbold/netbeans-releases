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

package org.netbeans.modules.glassfish.javaee.ide.ws;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.websvc.serverapi.api.WSStackFeature;
import org.netbeans.modules.websvc.serverapi.api.WSStack;
import org.netbeans.modules.websvc.serverapi.api.WSUriDescriptor;
import org.netbeans.modules.websvc.serverapi.spi.WSStackSPI;

/**
 *
 * @author mkuchtiak
 */
public class GlassfishJaxWsStack implements WSStackSPI {
    
    private String gfRootStr;
    public GlassfishJaxWsStack(String gfRootStr) {
        this.gfRootStr = gfRootStr;
    }
    
    public String getName() {
        return WSStack.STACK_JAX_WS;
    }
    
    public String getVersion() {
        return "2.1.3"; //NOI18N
    }

    public Set<String> getSupportedTools() {
        Set<String> supportedTools = new HashSet<String>();
        supportedTools.add(WSStack.TOOL_WSGEN);
        supportedTools.add(WSStack.TOOL_WSIMPORT);
        return supportedTools;
    }

    public File[] getToolClassPathEntries(String toolName) {

        if (WSStack.TOOL_WSGEN.equals(toolName) || WSStack.TOOL_WSIMPORT.equals(toolName)) {
            String[] entries = new String[] {"javax.javaee", 
                                             "webservices-api", 
                                             "webservices-rt", 
                                             "webservices-tools", 
                                             "jsr109-impl"};
            List<File> cPath = new ArrayList<File>();
            for (String entry : entries) {
                File f = ServerUtilities.getJarName(gfRootStr, entry);
                if ((f != null) && (f.exists())) {
                    cPath.add(f);
                }
            }
            return cPath.toArray(new File[cPath.size()]);
        }
        
        return new File[0];
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

            public String getDescriptorUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl"; //NOI18N
            }

            public String getTesterPageUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?Tester"; //NOI18N
            }
            
        };
    }

    public Set<WSStackFeature> getServiceFeatures() {
        Set<WSStackFeature> wsFeatures = new HashSet<WSStackFeature>();
//        wsFeatures.add(WSStackFeature.JSR_109);
//        wsFeatures.add(WSStackFeature.SERVICE_REF_INJECTION);
        wsFeatures.add(WSStackFeature.TESTER_PAGE);
        wsFeatures.add(WSStackFeature.WSIT);
        return wsFeatures;
    }

}
