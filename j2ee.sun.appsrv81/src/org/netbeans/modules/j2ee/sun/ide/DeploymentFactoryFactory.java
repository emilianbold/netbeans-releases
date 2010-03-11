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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.sun.ide;

import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;

public class DeploymentFactoryFactory {
    
    private static DeploymentFactory facadeDF = null;
    private static DeploymentFactory facadeDFGlassFishV1 = null;
    private static DeploymentFactory facadeDFGlassFishV2 = null;
    private static DeploymentFactory facadeDFJavaEEPlusSIP = null;
    
//    private static final String PROP_FIRST_RUN = "first_run";
    
    /** Factory method to create DeploymentFactory for s1as.
     */
    public static synchronized Object create() {
        if (facadeDF == null){
            //this is our JSR88 factory lazy init, only when needed via layer.
            facadeDF =  new org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory();
        }
        return facadeDF;
    }
    
    /** Factory method to create DeploymentFactory for V1.
     */
    public static synchronized Object createGlassFishV1() {
        if (facadeDFGlassFishV1 == null){
            //this is our JSR88 factory lazy init, only when needed via layer.
            facadeDFGlassFishV1 =  new org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory(NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_GlassFishV1"));
        }
        return facadeDFGlassFishV1;
    }
    
    /** Factory method to create DeploymentFactory for V2.
     */
    public static synchronized Object createGlassFishV2() {
        if (facadeDFGlassFishV2 == null){
            //this is our JSR88 factory lazy init, only when needed via layer.
            facadeDFGlassFishV2 =  new org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory(NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_GlassFishV2"));
        }
        return facadeDFGlassFishV2;
    }    
    
    /** Factory method to create DeploymentFactory for SailFin V1.
     */
    public static synchronized Object createJavaEEPlusSIP() {
        if (facadeDFJavaEEPlusSIP == null){
            //this is our JSR88 factory lazy init, only when needed via layer.
            facadeDFJavaEEPlusSIP =  new org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory(NbBundle.getMessage(DeploymentFactoryFactory.class, "LBL_JavaEEPlusSIP"));
        }
        return facadeDFJavaEEPlusSIP;
    }  
            
//    private static class PrepareEnvironment implements Runnable {
//        public void run() {
//            // if the domain hasn't been created successfully previously
//            if (!NbPreferences.forModule(DomainCreator.class).getBoolean(PROP_FIRST_RUN, false)) {
//                String prop = System.getProperty(ServerLocationManager.INSTALL_ROOT_PROP_NAME);
//
//                if (null != prop && prop.trim().length() > 0) {
//                    // There is a possible root directory for the AS
//                    File platformRoot = new File(prop);
//                    ClassLoader cl = ServerLocationManager.getNetBeansAndServerClassLoader(platformRoot);
//                    if (null != cl && !Utils.canWrite(platformRoot)) {
//                        createDomainAndRecord(platformRoot);
//                    }
//                    RegisterDatabase.getDefault().setupDerby(prop);
//                }
//            }
//        }
//    }

//    static private void createDomainAndRecord(final File propFile) {
//        // The root directory is valid
//        // Domain can be created
//        InstanceProperties ip = DomainCreator.createPersonalDefaultDomain(propFile.getAbsolutePath());
//            // Sets domain creation performed flag to true
//            NbPreferences.forModule(DomainCreator.class).putBoolean(PROP_FIRST_RUN, true);
//    }
}
