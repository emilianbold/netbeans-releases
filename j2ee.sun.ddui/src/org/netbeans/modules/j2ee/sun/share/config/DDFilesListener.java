/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.config;

import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.ConfigSupport;

import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;


/**
 * Listen on Deployment Descriptor files, mainly to detect the 
 * need to save configuration.
 *
 * @author nn136682
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
        if (ddFiles != null)
            return ddFiles;

        SourceFileMap sfm = provider.getSourceFileMap();
        FileObject[] roots = sfm.getSourceRoots();
        if (roots == null || roots.length < 1) {
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
        if (fo == null)
            return false;
        
        getTargetFiles();
        for (int i=0; i<ddFiles.length; i++) {
            if (fo.getNameExt().equalsIgnoreCase(ddFiles[i].getName())) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isTarget(String fileName) {
        if (fileName == null)
            return false;
        
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
            cs.autoSave();
        }
    }
    
    protected void targetDeleted(FileObject fo) {
    }
    
    protected void targetChanged(FileObject fo) {
        ConfigurationStorage cs = config.getStorage();
        if (cs != null) {
            cs.autoSave();
        }
    }
}
