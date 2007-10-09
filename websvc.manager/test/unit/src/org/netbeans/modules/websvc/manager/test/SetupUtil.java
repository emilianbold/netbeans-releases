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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.manager.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author quynguyen
 */
public class SetupUtil {
    private static final String WORKDIR_SPACES = "user directory/config/WebServices";
    private static final String WORKDIR = "userdirectory/config/WebServices";
    private static final String TEST_WSDL = "../resources/uszip.asmx.wsdl";
    private static final String TEST_CATALOG_DIR = "../resources/uszip-asmx-catalog";
    
    public static SetupData commonSetUp(File workingDir) throws Exception {
        SetupData data = new SetupData();
        
        String workDirByOS = 
                System.getProperty("os.name").startsWith("Windows") ? WORKDIR_SPACES : WORKDIR;
        
        File websvcHome = new File(workingDir, workDirByOS);
        data.setWebsvcHome(websvcHome);
        
        WebServiceDescriptor.WEBSVC_HOME = websvcHome.getAbsolutePath();
        WebServiceManager.WEBSVC_HOME = websvcHome.getAbsolutePath();
        
        File websvcUserDir = new File(WebServiceManager.WEBSVC_HOME);
        websvcUserDir.mkdirs();
        
        File wsdlFile = new File(SetupUtil.class.getResource(TEST_WSDL).toURI());
        File catalogDir = new File(SetupUtil.class.getResource(TEST_CATALOG_DIR).toURI());
        
        copy(wsdlFile, websvcUserDir);
        copy(wsdlFile, workingDir);
        copy(catalogDir, websvcUserDir);
        
        System.getProperties().setProperty("netbeans.user", websvcUserDir.getParentFile().getParentFile().getAbsolutePath());
        
        data.setLocalWsdlFile(new File(websvcUserDir, wsdlFile.getName()));
        data.setLocalCatalogFile(new File(websvcUserDir, catalogDir.getName() + "/catalog.xml"));
        data.setLocalOriginalWsdl(new File(workingDir, wsdlFile.getName()));
        
        MockServices.setServices(DialogDisplayerNotifier.class, InstalledFileLocatorImpl.class);
        
        return data;
    }

    public static void commonTearDown() throws Exception {
        DialogDisplayer dd = DialogDisplayer.getDefault();
        if (dd instanceof DialogDisplayerNotifier) {
            ((DialogDisplayerNotifier)dd).removeAllListeners();
        }
        
        MockServices.setServices();
    }
    
    public static void copy(File src, File target) throws Exception {        
        if (src.isFile()) {
            File targetFile = new File(target, src.getName());
            
            FileInputStream is = new FileInputStream(src);
            FileOutputStream os = new FileOutputStream(targetFile);
            
            FileChannel inputChannel = is.getChannel();
            FileChannel outputChannel = os.getChannel();
            
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            outputChannel.close();
        }else {
            File newDir = new File(target, src.getName());
            newDir.mkdirs();
            
            File[] dirFiles = src.listFiles();
            if (dirFiles != null) {
                for (int i = 0; i < dirFiles.length; i++) {
                    copy(dirFiles[i], newDir);
                }
            }
        }
    }
}
