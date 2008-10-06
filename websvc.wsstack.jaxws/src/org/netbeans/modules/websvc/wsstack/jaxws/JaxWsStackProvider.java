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

package org.netbeans.modules.websvc.wsstack.jaxws;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.wsstack.jaxws.impl.IdeJaxWsStack;
import org.netbeans.modules.websvc.wsstack.jaxws.impl.JdkJaxWsStack;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsStackProvider {
    
    private static WSStack<JaxWs> jdkJaxWsStack, ideJaxWsStack;
    
    public static WSStack<JaxWs> getJaxWsStack(J2eePlatform j2eePlatform) {
        return WSStack.findWSStack(j2eePlatform.getLookup(), JaxWs.class);
    }
    
    public static WSTool getJaxWsStackTool(J2eePlatform j2eePlatform, JaxWs.Tool toolId) {
        WSStack wsStack = WSStack.findWSStack(j2eePlatform.getLookup(), JaxWs.class);
        if (wsStack != null) {
            return wsStack.getWSTool(toolId);
        } else {
            return null;
        }
    }
    
    public static synchronized WSStack<JaxWs> getJdkJaxWsStack() {
        if (jdkJaxWsStack == null) {
            String jaxWsVersion = getJaxWsStackVersion(System.getProperty("java.version")); //NOI18N
            if (jaxWsVersion != null) {
                jdkJaxWsStack = WSStackFactory.createWSStack(JaxWs.class, new JdkJaxWsStack(jaxWsVersion), WSStack.Source.JDK);
            }
        }
        return jdkJaxWsStack;
    }
    
    public static synchronized WSStack<JaxWs> getIdeJaxWsStack() {
        if (ideJaxWsStack == null) {
            ideJaxWsStack =  WSStackFactory.createWSStack(JaxWs.class, new IdeJaxWsStack(), WSStack.Source.IDE);
        }
        return ideJaxWsStack;
    }
            
    private static String getJaxWsStackVersion(String java_version) {
        if (java_version.startsWith("1.6")) { //NOI18N
            int index = java_version.indexOf("_"); //NOI18N
            if (index > 0) {
                String releaseVersion = java_version.substring(index+1);
                try {
                    Integer rv = Integer.valueOf(releaseVersion);
                    if (rv >=4) return "2.1.1"; //NOI18N
                    else return "2.0"; //NOI18N
                } catch (NumberFormatException ex) {
                    // return null for some strange jdk versions
                    return null;
                }
            } else {
                // return null for some strange jdk versions
                return null;
            }
        } else {
            try {
                Float version = Float.valueOf(java_version.substring(0,3));
                if (version > 1.6) return "2.1.3"; //NOI18N
                else return null;
            } catch (NumberFormatException ex) {
                // return null for some strange jdk versions
                return null;
            }
        }
    }
}
