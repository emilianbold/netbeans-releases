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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModuleFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Peter Williams
 */
public class JavaEEServerModuleFactory implements GlassfishModuleFactory {

    private static JavaEEServerModuleFactory singleton = new JavaEEServerModuleFactory();
    
    private JavaEEServerModuleFactory() {
    }
    
    public static GlassfishModuleFactory getDefault() {
        return singleton;
    }
    
    public boolean isModuleSupported(String glassfishHome, Properties asenvProps) {
        boolean result = false;

        // Do some moderate sanity checking to see if this v3 build looks ok.
        File javaEEJar = new File(glassfishHome, "lib/javaee-5.0.jar"); // lib folder for 2007 builds of V3
        if(javaEEJar.exists()) {
            File webTierJar = new File(glassfishHome, "lib/webtier-10.0-SNAPSHOT.jar");
            if(webTierJar.exists()) {
                result = true;
            }
        } else {
            javaEEJar = new File(glassfishHome, "modules/javaee-5.0.jar"); // Jan/Feb 2008 builds used this name
            if(!javaEEJar.exists()) {
                javaEEJar = new File(glassfishHome, "modules/javaee-5.0-SNAPSHOT.jar"); // Name in V3P2M2 buld.
            }
            if(javaEEJar.exists()) {
                File webTierJar = new File(glassfishHome, "modules/web/webtier-10.0-SNAPSHOT.jar");
                if(webTierJar.exists()) {
                    result = true;
                }
            }
        }

        return result;
    }

    public Object createModule(Lookup instanceLookup) {
        // When creating JavaEE support, also ensure this instance is added to j2eeserver
        InstanceProperties ip = null;
        GlassfishModule commonModule = instanceLookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            try {
                Map<String, String> props = commonModule.getInstanceProperties();
                String url = props.get(InstanceProperties.URL_ATTR);
                ip = InstanceProperties.getInstanceProperties(url);
                if(ip == null) {
                    String username = props.get(InstanceProperties.USERNAME_ATTR);
                    String password = props.get(InstanceProperties.PASSWORD_ATTR);
                    String displayName = props.get(InstanceProperties.DISPLAY_NAME_ATTR);
                    ip = InstanceProperties.createInstancePropertiesWithoutUI(
                            url, username, password, displayName, props);
                    
                    if(ip == null) {
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, 
                                "Unable to create/locate J2EE InstanceProperties for " + url);
                    }
                }
            } catch(InstanceCreationException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, null, ex);
            }
        }

        return (ip != null) ? new JavaEEServerModule(instanceLookup, ip) : null;
    }

}
