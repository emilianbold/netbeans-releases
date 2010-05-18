/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
/*
 * EjbModuleInstaller.java
 *
 * Created on May 4, 2004, 10:20 AM
 */

package org.netbeans.modules.visualweb.ejb;
import java.io.File;
import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;

/**
 * This class is to initialize the necessary information when
 * the Ejb data source module gets first installed or restored.
 *
 * @author  cao
 */
public class EjbDataSourceModuleInstaller extends ModuleInstall {
    /*public void installed()
    {
        restored();
    }*/
    
    public void restored() {
        // First, copy the samples
        //copySamples();
        
        // Load the ejbs
        try {
            EjbDataSourceManager.getInstance().load();
        } catch( Exception e ) {
            // Problem load the ejb data source.
            // Log a waring and go on
            ErrorManager.getDefault().getInstance("org.netbeans.modules.visualweb.ejb.EjbDataSourceModuleInstaller").log( ErrorManager.WARNING, "Failed to load ejbdatasource.xml when restoring ejb module" );
            e.printStackTrace();
        }
    }
    
    public void close() {
        // Save all the information to a xml file
        EjbDataSourceManager.getInstance().save();
    }
    
    public void uninstalled() {
        close();
    }
    
//    private void copySamples() {
//        String resetWm = System.getProperty("resetWindowManager");
//        
//        // Copy ejbsource.xml to the user dir
//        File ejbDir = new File(System.getProperty("netbeans.user"), "ejb-datasource"); // NOI18N
//        File sampleEjbDir = InstalledFileLocator.getDefault().locate("samples/ejb/ejb-datasource", null, false ); // NOI18N
//        
//        if ("true".equals(resetWm))
//            deleteDirectory(ejbDir);
//        
//        if( !ejbDir.exists()) {
//            File[] files = sampleEjbDir.listFiles();
//            for (int i = 0; i < files.length; i++) {
//                if( files[i].getName().indexOf( ".xml" ) != -1 )
//                    copyFile(files[i],  new File(ejbDir, files[i].getName()));
//            }
//        }
//    }
//    
//    private void copyFile(File src, File dest) {
//        try {
//            RaveFileCopy.fileCopy(src, dest);
//        } catch (IOException ioe) {
//            ErrorManager.getDefault().notify(ioe);
//        }
//    }
//    
//    
//    private void deleteDirectory(File dir){
//        if (!dir.delete()) {
//            if (dir.isDirectory()) {
//                java.io.File list[] = dir.listFiles();
//                for (int i=0; i < list.length ; i++) {
//                    deleteDirectory(list[i]);
//                }
//            }
//            dir.delete();
//        }
//    }
    
}
