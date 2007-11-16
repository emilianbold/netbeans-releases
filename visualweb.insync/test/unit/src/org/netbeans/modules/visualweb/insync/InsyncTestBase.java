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
package org.netbeans.modules.visualweb.insync;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.regex.Pattern;
import junit.framework.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.core.startup.layers.NbinstURLStreamHandlerFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.web.jsf.JSFConfigLoader;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sc32560, jdeva
 * 
 */
public class InsyncTestBase extends NbTestCase {
    private static final String SYS_PROP_SAX_PARSER_FACTORY = "javax.xml.parsers.SAXParserFactory"; // NOI18N
    private static final String SYS_PROP_DOM_PARSER_FACTORY = "javax.xml.parsers.DocumentBuilderFactory"; // NO18N
    
    public InsyncTestBase(String name) {
        super(name);
        _setUp();
    }
    
    String[] pageBeans, requestBeans, sessionBeans, applicationBeans, facesConfigs;
    Project project;
    
    static {
        setupServices();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //_setUp();
    }
    
    private void _setUp() {
        //Set up the system properties
        System.setProperty("jdk.home", System.getProperty("java.home"));
        try {

            System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "user").getAbsolutePath());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        //Load the required libraries. This allows evaluation of project properties
        //ex:- ${libs.jsf12-support.classpath}
        LibraryManager.getDefault().getLibraries();
        openProject();
        pageBeans = System.getProperty(
                "visualweb.project.pagebeans").split(Pattern.quote(System.getProperty("path.separator")));
        requestBeans = System.getProperty(
                "visualweb.project.requestbeans").split(Pattern.quote(System.getProperty("path.separator")));
        sessionBeans = System.getProperty(
                "visualweb.project.sessionbeans").split(Pattern.quote(System.getProperty("path.separator")));
        applicationBeans = System.getProperty(
                "visualweb.project.applicationbeans").split(Pattern.quote(System.getProperty("path.separator")));
        facesConfigs = System.getProperty(
                "visualweb.project.facesconfig").split(Pattern.quote(System.getProperty("path.separator")));        
        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
    }       
    
    String oldSaxParser, oldDomParser;
    
    public void setUpForFacesModelSet() {
        // Needed for faces container initialization
        System.setProperty(SYS_PROP_SAX_PARSER_FACTORY, "org.netbeans.core.startup.SAXFactoryImpl");
        System.setProperty(SYS_PROP_DOM_PARSER_FACTORY, "org.netbeans.core.startup.DOMFactoryImpl");        
    }
    
    public FacesModelSet createFacesModelSet() {
        FileObject file = getProject().getProjectDirectory();
        //setUpForFacesModelSet();
        FacesModelSet set = FacesModelSet.getInstance(file);
        return set;
    }

    public Project openProject() {
        String projectName = System.getProperty("visualweb.project.name");
        String relativeProjectPath = "projects" + File.separator + projectName;
        //Check for a directory and then a zip file by project name
        File f = new File(getDataDir().getAbsolutePath(), relativeProjectPath);
        if (!f.exists()) {
            f = new File(getDataDir().getAbsolutePath(), relativeProjectPath + ".zip");
        }
        try {
            project = ProjectUtils.openProject(getWorkDir(), f.getAbsolutePath(), projectName);
            //Necessary for FacesConfigModel
            JSFConfigUtils.setUp(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return project;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void destroyProject() throws IOException {
        ProjectUtils.destroyProject(project);
    }

    public String[] getApplicationBeans() {
        return applicationBeans;
    }

    public String[] getPageBeans() {
        return pageBeans;
    }

    public String[] getRequestBeans() {
        return requestBeans;
    }

    public String[] getSessionBeans() {
        return sessionBeans;
    }

    public String[] getFacesConfigs() {
        return facesConfigs;
    }
    
    private static void setupServices() {
        URLStreamHandlerFactory urlStreamHandlerFactory = new NbinstURLStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);        
        MockLookup.setInstances(
            new RepositoryImpl(),
            new InsyncMimeResolver(),
            urlStreamHandlerFactory
        );
    }    
}
