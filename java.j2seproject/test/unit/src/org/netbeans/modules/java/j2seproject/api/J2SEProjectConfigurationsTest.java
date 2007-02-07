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

import java.io.File;
import junit.framework.TestCase;
import junit.framework.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Properties;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Test of class org.netbeans.modules.java.j2seproject.api.J2SEProjectConfigurations
 * 
 * @author Milan Kubec
 */
public class J2SEProjectConfigurationsTest extends NbTestCase {
    
    public J2SEProjectConfigurationsTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of createConfigurationFiles method
     */
    public void testCreateConfigurationFiles() throws Exception {
        
        System.out.println("createConfigurationFiles");
        
        File proj = getWorkDir();
        clearWorkDir();
        
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.5"));
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "TestProject", null, "manifest.mf");
        
        Project prj = ProjectManager.getDefault().findProject(aph.getProjectDirectory());
        
        String configName = "TestConfig";
        
        Properties sharedProps = new Properties();
        sharedProps.put("sharedPropName", "sharedPropValue");
        sharedProps.put("$sharedPropNameSpecial", "sharedPropValueSpecial");
        sharedProps.put("sharedPropName2", "${sharedPropName}");
        
        Properties privateProps = new Properties();
        privateProps.put("privatePropName", "privatePropValue");
        privateProps.put("privatePropName2", "${privatePropName}");
        
        J2SEProjectConfigurations.createConfigurationFiles(prj, configName, sharedProps, privateProps);
        
        FileObject prjDirFO = prj.getProjectDirectory();
        
        FileObject sharedPropsFO = prjDirFO.getFileObject("nbproject/configs/" + configName + ".properties");
        Properties loadedSharedProps = new Properties();
        loadedSharedProps.load(sharedPropsFO.getInputStream());
        assertEquals(sharedProps, loadedSharedProps);
        
        FileObject privatePropsFO = prjDirFO.getFileObject("nbproject/private/configs/" + configName + ".properties");
        Properties loadedPrivateProps = new Properties();
        loadedPrivateProps.load(privatePropsFO.getInputStream());
        assertEquals(privateProps, loadedPrivateProps);
        
        configName = "Test_Config2";
        
        sharedProps = new Properties();
        sharedProps.put("sharedPropName", "sharedPropValue");
        sharedProps.put("$sharedPropNameSpecial", "sharedPropValueSpecial");
        sharedProps.put("sharedPropName2", "${sharedPropName}");
        
        J2SEProjectConfigurations.createConfigurationFiles(prj, configName, sharedProps, null);
        
        sharedPropsFO = prjDirFO.getFileObject("nbproject/configs/" + configName + ".properties");
        loadedSharedProps = new Properties();
        loadedSharedProps.load(sharedPropsFO.getInputStream());
        assertEquals(sharedProps, loadedSharedProps);
        
        privatePropsFO = prjDirFO.getFileObject("nbproject/private/configs/" + configName + ".properties");
        assertNull(privatePropsFO);
        
    }
    
}
