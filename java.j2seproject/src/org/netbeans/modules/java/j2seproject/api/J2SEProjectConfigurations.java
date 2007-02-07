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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Properties;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Utility class for creating project run configuration files
 * 
 * @author Milan Kubec
 * @since 1.10
 */
public final class J2SEProjectConfigurations {
    
    J2SEProjectConfigurations() {}
    
    /**
     * Creates property files for run configuration and writes passed properties.
     * Shared properties are written to nbproject/configs folder and private properties
     * are written to nbproject/private/configs folder. The property file is not created
     * if null is passed for either shared or private properties.
     * 
     * @param prj project under which property files will be created
     * @param configName name of the config file, '.properties' is apended
     * @param sharedProps properties to be written to shared file; is allowed to
     *        contain special purpose properties starting with $ (e.g. $label)
     * @param privateProps properties to be written to private file
     */
    public static void createConfigurationFiles(Project prj, String configName, 
            final Properties sharedProps, final Properties privateProps) throws IOException, IllegalArgumentException {
        
        if (prj == null || configName == null || "".equals(configName)) {
            throw new IllegalArgumentException();
        }
        
        final String configFileName = configName + ".properties"; // NOI18N
        final FileObject prjDir = prj.getProjectDirectory();
        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    prjDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            generateConfig(prjDir, "nbproject/configs/" + configFileName, sharedProps); // NOI18N
                            generateConfig(prjDir, "nbproject/private/configs/" + configFileName, privateProps); // NOI18N
                        }
                    });
                    return null;
                }
            });
        } catch (MutexException ex) {
            throw (IOException) ex.getException();
        }
        
    }
    
    private static void generateConfig(FileObject prjDir, String cfgFilePath, Properties propsToWrite) throws IOException {
        
        if (propsToWrite == null) {
            // do not create anything if props is null
            return;
        }
        FileObject jwsConfigFO = FileUtil.createData(prjDir, cfgFilePath);
        Properties props = new Properties();
        InputStream is = jwsConfigFO.getInputStream();
        props.load(is);
        is.close();
        if (props.equals(propsToWrite)) {
            // file already exists and props are the same
            return;
        }
        OutputStream os = jwsConfigFO.getOutputStream();
        propsToWrite.store(os, null);
        os.close();
        
    }
    
}
