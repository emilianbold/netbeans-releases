/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.config;

import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;


/**
 * Listen on Deployment Descriptor files, to detect changes affecting the standard
 * deployment descriptors (web.xml, ejb-jar.xml, application.xml, and webservices.xml
 * for now).
 * 
 * In particular it is used to detect the creation and deletion of webservices.xml
 * so that we can properly add and remove the WebServices root DConfigBean that
 * is bound to the DDBean at the root of webservices.xml.
 * 
 * Also used to be used to know when to save the configuration, but not anymore.
 * 
 * @author nn136682
 * @author Peter Williams
 */
public class DDFilesListener extends AbstractFilesListener {
    
    private SunONEDeploymentConfiguration config;
    private File[] ddFiles = null;
    
    public DDFilesListener(SunONEDeploymentConfiguration config, J2eeModuleProvider provider) {
        super(provider);
        this.config = config;
    }
    
    public static final File[] EMPTY_FILE_ARRAY = new File[0];
    
    protected File[] getTargetFiles() {
        if (ddFiles != null) {
            return ddFiles;
        }

        SourceFileMap sfm = provider.getSourceFileMap();
        FileObject[] roots = sfm.getSourceRoots();
        if (roots == null || roots.length < 1) {
            ddFiles = EMPTY_FILE_ARRAY;
            return ddFiles;
        }
        
        if(roots[0] == null) {
            ddFiles = EMPTY_FILE_ARRAY;
            return ddFiles;
        }

        File configFolder = FileUtil.toFile(roots[0]);
        String[] pathNames = ModuleDDSupport.getDDPaths(provider.getJ2eeModule().getModuleType());
        ddFiles = new File[pathNames.length];
        for (int i=0; i<pathNames.length; i++) {
            String fileName = pathNames[i];
            if (J2eeModule.WAR != provider.getJ2eeModule().getModuleType()) {
                fileName = fileName.substring(pathNames[i].lastIndexOf('/')+1); //always forward
            }
            ddFiles[i] = new File(configFolder, fileName);
        }
        return ddFiles;
    }
    
    protected boolean isTarget(FileObject fo) {
        if (fo == null) {
            return false;
        }
        
        getTargetFiles();
        for (int i=0; i<ddFiles.length; i++) {
            if (fo.getNameExt().equalsIgnoreCase(ddFiles[i].getName())) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isTarget(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        getTargetFiles();
        for (int i=0; i<ddFiles.length; i++) {
            if (fileName.equalsIgnoreCase(ddFiles[i].getName())) {
                return true;
            }
        }
        return true;
    }
    
    protected void targetCreated(FileObject fo) {
        ConfigurationStorage cs = config.getStorage();
        if (cs != null) {
            cs.updateDDRoot(fo);
//            cs.autoSave();
        }
    }
    
    protected void targetDeleted(FileObject fo) {
    }
    
    protected void targetChanged(FileObject fo) {
//        ConfigurationStorage cs = config.getStorage();
//        if (cs != null) {
//            cs.autoSave();
//        }
    }
}
