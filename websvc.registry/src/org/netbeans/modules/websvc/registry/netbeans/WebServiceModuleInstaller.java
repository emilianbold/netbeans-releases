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

package org.netbeans.modules.websvc.registry.netbeans;

import java.io.*;

import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;

/** class WebServiceModuleInstaller
 *
 *  ModuleInstall for the web service registry module.  Handles reading
 *  the registry on module startup and saving any changes on module shutdown.
 */
public class WebServiceModuleInstaller extends ModuleInstall /*implements InstanceListener*/ {
    
    private static ExtensionClassLoader specialLoader = null;
    private static boolean registryInstalled = false;
        
    @Override
    public void close() {
        if(registryInstalled) {
            try {
                PersistenceManagerInterface persistenceManager =(PersistenceManagerInterface)
                specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance(); //NOI18N
                persistenceManager.save(specialLoader);
            } catch (ClassNotFoundException cnfe){
                // nothing to do in this case, this server does not support wscompile or web services
                // see bug 55323 
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            } finally {
            }
        }
    }

    @Override
    public void uninstalled() {
        close();
    }
    
    public static void restoreds() {
        if(specialLoader == null) {
            try {
                specialLoader = new ExtensionClassLoader(WebServiceModuleInstaller.class.getClassLoader());
                updatesSpecialLoader(specialLoader);
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
    }

    public static ClassLoader getExtensionClassLoader() {
        if (!registryInstalled) {
            restoreds();
        }
        if (registryInstalled) {
            try {
                PersistenceManagerInterface persistenceManager = (PersistenceManagerInterface)
                specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance(); //NOI18N
                persistenceManager.load(specialLoader);
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        
        return specialLoader;
    }
 
    private static String JAXRPC_16 [] = {
        "modules/ext/jaxrpc16/activation.jar",
        "modules/ext/jaxrpc16/jax-qname.jar",
        "modules/ext/jaxrpc16/jaxp-api.jar",
        "modules/ext/jaxrpc16/FastInfoset.jar",
        "modules/ext/jaxrpc16/jaxrpc-api.jar",
        "modules/ext/jaxrpc16/jaxrpc-impl.jar",
        "modules/ext/jaxrpc16/jaxrpc-spi.jar",
        "modules/ext/jaxrpc16/jsr173_api.jar",
        "modules/ext/jaxrpc16/mail.jar",
        "modules/ext/jaxrpc16/relaxngDatatype.jar",
        "modules/ext/jaxrpc16/saaj-api.jar",
        "modules/ext/jaxrpc16/saaj-impl.jar",
        "modules/ext/jaxrpc16/xsdlib.jar"
    };
        
    public static void updatesSpecialLoader(ExtensionClassLoader loader) throws Exception {
        try {
                
                InstalledFileLocator locator = InstalledFileLocator.getDefault();
                
                File f = locator.locate("modules/ext/websvcregistry.jar", null, true); // NOI18N
                if(f != null) {
                    registryInstalled = true;
                    loader.addURL(f);
                    loadLocaleSpecificJars(f, loader);
                } else {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot locate file modules/ext/websvcregistry.jar");
                }
                
                // Add correct jars from the installed application server.
//                SJSASVersion appServerVersion = SJSASVersion.getSJSAppServerVersion();
                String [] registryRuntimeJars = JAXRPC_16;
                
                for(int i = 0; i < registryRuntimeJars.length; i++) {
                    File jarFile = locator.locate(registryRuntimeJars[i], null, false);
                    if (jarFile != null) {
                        loader.addURL(jarFile);
                    } else {
                        System.out.println("Cannot load jar: " + registryRuntimeJars[i]);
                    }
                }
//            }
        } catch(Exception ex) {
            throw new Exception(ex.getLocalizedMessage(), ex);
        }
    }
    private static void loadLocaleSpecificJars(File file, ExtensionClassLoader loader) {
        File parentDir = file.getParentFile();
        //System.out.println("parentDir: " + parentDir);
        File localeDir = new File(parentDir, "locale"); //NOI18N
        if(localeDir.exists()){
            File[] localeFiles = localeDir.listFiles();
            File localeFile = null;
            String localeFileName = null;
            String fileName = file.getName();
            fileName = getFileNameWithoutExt(fileName);
            //System.out.println("fineName: " + fileName);
            assert(fileName.length() > 0);
            for(int i=0; i<localeFiles.length; i++){
                localeFile = localeFiles[i];
                localeFileName = localeFile.getName();
                //System.out.println("localeFileName: " + localeFileName);
                assert(localeFileName.length() > 0);
                if(localeFileName.startsWith(fileName)){
                    try{
                        loader.addURL(localeFile);
                    }catch (Exception ex2) {
                        System.out.println(ex2.getLocalizedMessage());
                    }
                }
            }
        }
    }
    
    private static String getFileNameWithoutExt(String fileName){
        int index = fileName.lastIndexOf("."); //NOI18N
        if(index != -1){
            fileName = fileName.substring(0, index);
        }
        return fileName;
    }    

}
