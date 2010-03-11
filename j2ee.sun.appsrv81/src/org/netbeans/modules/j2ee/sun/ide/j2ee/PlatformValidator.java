/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.ide.DeploymentFactoryFactory;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory;
import org.openide.util.NbBundle;

public class PlatformValidator {
    public static final String APPSERVERSJS = "SJS"; //NOI18N
    public static final String GLASSFISH_V2 = "GF_V2"; //NOI18N
    public static final String GLASSFISH_V1 = "GF_V1"; //NOI18N
    public static final String SAILFIN_V1 = "SIP_V1"; //NOI18N
    
    public boolean isGoodAppServerLocation(File loc) {
        return ServerLocationManager.isGoodAppServerLocation(loc);
    }
    
    public boolean isDescriminatorPresent(File loc, String serverVersion) {
        boolean retVal = ServerLocationManager.isGoodAppServerLocation(loc);
        if (retVal) {
            File sipDescriminator = new File(loc, "lib/comms-appserv-rt.jar"); //NOI18N
            if(serverVersion.equals(SAILFIN_V1)){
                if (!sipDescriminator.exists() || !sipDescriminator.isFile()) { 
                   return false;
                } else {
                    return retVal;
                }
            } else {
                //V1 or V2 or SJSAS
                if (sipDescriminator.exists() || sipDescriminator.isFile()) { 
                    retVal = false;
                }
            }
            if (retVal) {
                if (ServerLocationManager.isGlassFish(loc)) {
                    File versionDescriminator = new File(loc, "lib/shoal-gms.jar"); //NOI18N
                    if (serverVersion.equals(GLASSFISH_V1)) {
                        if (versionDescriminator.exists() || versionDescriminator.isFile()) {
                            retVal = false;
                        }
                    } else if (serverVersion.equals(GLASSFISH_V2)) {
                        if (!versionDescriminator.exists() || !versionDescriminator.isFile()) {
                            retVal = false;
                        }
                    } else {
                        //GlassFish match but no descriminator match
                        //should not come here
                        retVal = false;
                    }
                }else{
                    File versionDescriminator = new File(loc, "lib/dtds/sun-domain_1_1.dtd"); // NOI18N
                    if (serverVersion.equals(APPSERVERSJS)) {
                        if (! versionDescriminator.exists() || ! versionDescriminator.isFile()) {
                            retVal = false;
                        }
                    }else{
                        //All Server types checked but no descriminator match
                        //should not come here
                        retVal = false;
                    }
                }    
            }
        }
        return retVal;
    }
    
    public String getServerTypeName(String serverVersion){
        //serverVersion.equals(APPSERVERSJS)
        String serverType = NbBundle.getMessage(SunDeploymentFactory.class, "FACTORY_DISPLAYNAME"); //NOI18N
        if(serverVersion.equals(GLASSFISH_V1)){
            serverType = NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_GlassFishV1"); //NOI18N
        }else if(serverVersion.equals(GLASSFISH_V2)){
            serverType = NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_GlassFishV2"); //NOI18N
        }else if(serverVersion.equals(SAILFIN_V1)){
            serverType = NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_JavaEEPlusSIP"); //NOI18N
        }
        return serverType;
    }
    
    public String getServerVersionByName(String serverName){
        String serverVersion = null;
        if(serverName.equals(NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_GlassFishV1"))){ //NOI18N
            serverVersion = GLASSFISH_V1;
        }else if(serverName.equals(NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_GlassFishV2"))){ //NOI18N
            serverVersion = GLASSFISH_V2;
        }else if(serverName.equals(NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_JavaEEPlusSIP"))){ //NOI18N
            serverVersion = SAILFIN_V1;
        }else if(serverName.equals(NbBundle.getMessage(SunDeploymentFactory.class, "FACTORY_DISPLAYNAME"))){ //NOI18N
            serverVersion = APPSERVERSJS;
        }
        
        return serverVersion;
    }

}
