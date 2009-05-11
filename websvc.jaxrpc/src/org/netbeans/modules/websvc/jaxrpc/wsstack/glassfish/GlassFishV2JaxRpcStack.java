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

package org.netbeans.modules.websvc.jaxrpc.wsstack.glassfish;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.jaxrpc.JaxRpc;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;


/**
 *
 * @author mkuchtiak
 */
public class GlassFishV2JaxRpcStack implements WSStackImplementation<JaxRpc> {
    private static final String WEBSERVICES_TOOLS_JAR = "lib/webservices-tools.jar"; //NOI18N
    private static final String WEBSERVICES_RT_JAR = "lib/webservices-rt.jar"; //NOI18N
    
    private static final String TOOLS_JAR = "lib/tools.jar"; //NOI18N
    private static final String JSTL_JAR =  "lib/appserv-jstl.jar"; //NOI18N
    private static final String JAVA_EE_JAR = "lib/javaee.jar"; //NOI18N
    private static final String APPSERV_WS_JAR = "lib/appserv-ws.jar"; //NOI18N
    private static final String MAIL_JAR =  "lib/mail.jar"; //NOI18N
    private static final String ACTIVATION_JAR = "lib/activation.jar"; //NOI18N
    
    private File root;
    private String version;
    private JaxRpc jaxRpc;
    
    public GlassFishV2JaxRpcStack(File root) {
        this.root = root;
        version = resolveImplementationVersion();
        if (version == null) {
            // Default Version
            version = "1.0.0"; // NOI18N
        }
        jaxRpc = new JaxRpc();
    }

    public JaxRpc get() {
        return jaxRpc;
    }

    public WSStackVersion getVersion() {
        return WSStackFactory.createWSStackVersion(version);
    }

    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxRpc.Tool.WCOMPILE) {
            return WSStackFactory.createWSTool(new JaxRpcTool(JaxRpc.Tool.WCOMPILE));
        }else {
            return null;
        }
    }

    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxRpc.Feature.JSR109) {
            return true;
        } else {
            return false;
        }    
    }
    
    private String resolveImplementationVersion() {
        // take webservices-tools.jar file
        File wsToolsJar = new File(root, WEBSERVICES_TOOLS_JAR);
        File appservWsJar = new File(root, APPSERV_WS_JAR);
       
        if (wsToolsJar.exists() || appservWsJar.exists()) {
            return "1.1.3"; //NOI18N
        }
        return null;
        
    }
    
    private class JaxRpcTool implements WSToolImplementation {
        JaxRpc.Tool tool;
        JaxRpcTool(JaxRpc.Tool tool) {
            this.tool = tool;
        }

        public String getName() {
            return tool.getName();
        }

        public URL[] getLibraries() {
            File wsToolsJar = new File(root, WEBSERVICES_TOOLS_JAR);  //NOI18N
            try {
                if (wsToolsJar.exists()) { // WSIT installed on top
                    return new URL[] {
                        wsToolsJar.toURI().toURL(),     // NOI18N
                        new File(root, WEBSERVICES_RT_JAR).toURI().toURL(),           // NOI18N
                        new File(root, JSTL_JAR).toURI().toURL(),       //NOI18N
                        new File(root, JAVA_EE_JAR).toURI().toURL(),    //NOI18N
                        new File(root, APPSERV_WS_JAR).toURI().toURL(), //NOI18N
                        new File(root, MAIL_JAR).toURI().toURL(),       //NOI18N
                        new File(root, ACTIVATION_JAR).toURI().toURL()  //NOI18N
                    };
                } else {                                                // regular appserver
                    return new URL[] {
                        new File(root, JSTL_JAR).toURI().toURL(),         //NOI18N
                        new File(root, JAVA_EE_JAR).toURI().toURL(),      //NOI18N
                        new File(root, APPSERV_WS_JAR).toURI().toURL(),   //NOI18N
                        new File(root, MAIL_JAR).toURI().toURL(),         //NOI18N
                        new File(root, ACTIVATION_JAR).toURI().toURL()    //NOI18N
                    };
                }
            } catch (MalformedURLException ex) {
                return new URL[0];
            } 
        }
        
    }

}
