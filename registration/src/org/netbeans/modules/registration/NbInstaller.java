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

package org.netbeans.modules.registration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.reglib.NbServiceTagSupport;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/**
 *
 * @author Marek Slama
 * 
 */
public class NbInstaller extends ModuleInstall {
    
    private static final String KEY_ENABLED = "nb.registration.enabled";
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.registration.NbInstaller"); // NOI18N
    
    public static String PRODUCT_ID = "nb";
    
    private static boolean moduleEnabled = true;
    
    @Override
    public void restored() {
        Object value = System.getProperty(KEY_ENABLED);
        if (value != null) {
            if ("false".equals(value)) {
                moduleEnabled = false;
                LOG.log(Level.FINE,"Set moduleEnabled: " + moduleEnabled);
            } else if ("true".equals(value)) {
                moduleEnabled = true;
                LOG.log(Level.FINE,"Set moduleEnabled: " + moduleEnabled);
            }
        } else {
            String s = NbBundle.getMessage(NbInstaller.class,KEY_ENABLED);
            if ("false".equals(s)) {
                moduleEnabled = false;
                LOG.log(Level.FINE,"Set moduleEnabled: " + moduleEnabled);
            } else if ("true".equals(s)) {
                moduleEnabled = true;
                LOG.log(Level.FINE,"Set moduleEnabled: " + moduleEnabled);
            }
        }        
        RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
        a.setEnabled(moduleEnabled);
        
        if (!isModuleEnabled()) {
            LOG.log(Level.FINE,"Module is disabled.");
            return;
        }
        
        try {
            NbServiceTagSupport.createNbServiceTag
            (NbServiceTagSupport.getProductName(),
             System.getProperty("java.version"));
            if (isCndShouldBeRegistered()) {
                NbServiceTagSupport.createCndServiceTag
                (NbServiceTagSupport.getProductName(),
                System.getProperty("java.version"));
            }

            //NbServiceTagSupport.createGfServiceTag("NetBeans IDE 6.0","","","","v2");
            //NbServiceTagSupport.createGfServiceTag("NetBeans IDE 6.0","","","","v3");
            //NbServiceTagSupport.createJdkServiceTag("NetBeans IDE 6.0");
            //NbServiceTagSupport.getRegistrationHtmlPage(PRODUCT_ID);
            NbConnection.init();
        } catch (IOException ex) {
            LOG.log(Level.INFO,"Error: Cannot create service tag:",ex);
        }
    }

    private static boolean isCndShouldBeRegistered() {
           //This return platfomX dir but we need install dir
        File f = new File(System.getProperty("netbeans.home"));
        
        File nbInstallDir = f.getParentFile();
        String[] cndlist = nbInstallDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("cnd");
            }
        });
        
        if (cndlist != null && cndlist.length != 0) {            
            LOG.log(Level.FINE,"cnd cluster should be registered");
            return true;
        }        
       return false;
    }
    
    static boolean isModuleEnabled () {
        return moduleEnabled;
    }
    
}
