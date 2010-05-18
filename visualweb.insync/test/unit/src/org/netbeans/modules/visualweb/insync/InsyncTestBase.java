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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.insync;

import com.sun.rave.designtime.Constants;
import java.beans.BeanInfo;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import junit.framework.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.core.startup.layers.NbinstURLStreamHandlerFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.web.project.WebProject;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sc32560, jdeva
 * 
 */
public class InsyncTestBase extends NbTestCase {
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

    public FacesModelSet createFacesModelSet() {
        FileObject file = getProject().getProjectDirectory();
        FacesModelSet set = FacesModelSet.getInstance(file);
        return set;
    }       
    
    protected int getBeansCount() {
        return getPageBeans().length + getNonPageBeansCount();
    }
    
    protected int getNonPageBeansCount() {
        return getRequestBeans().length + 
               getSessionBeans().length + 
               getApplicationBeans().length;
    }
    
    protected String[] getBeanNames() {
        String[] str = new String[getBeansCount()];
        int i = 0;
        for(String s : getPageBeans()) {
            str[i++] = s;
        }
        for (String s : getRequestBeans()) {
            str[i++] = s;
        }
        for (String s : getSessionBeans()) {
            str[i++] = s;
        }    
        for (String s : getApplicationBeans()) {
            str[i++] = s;
        }         
        return str;
    }
    
    protected List<Bean> createBeans(String[] types) {                
        FacesModelSet modelSet = createFacesModelSet();
        FacesModel model = modelSet.getFacesModel(getJavaFile(getPageBeans()[0]));
        model.sync();
        BeansUnit bu = model.getBeansUnit();
        List<Bean> beans = new ArrayList<Bean>();
        for(String type : types) {
            beans.add(createBean(bu, type));
        }
        return beans;
    }

    private Bean createBean(BeansUnit bu, String type) {
        Constructor ctor = null;
        java.lang.reflect.Method m = null;
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class clazz = Class.forName("org.netbeans.modules.visualweb.insync.beans.Bean");
            ctor = clazz.getDeclaredConstructor(BeansUnit.class, BeanInfo.class, String.class);
            ctor.setAccessible(true);
            clazz = Class.forName("org.netbeans.modules.visualweb.insync.beans.BeansUnit");
            m = clazz.getDeclaredMethod("nextNameForType", String.class);
            m.setAccessible(true);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            Thread.currentThread().setContextClassLoader(bu.getClassLoader());
            Class beanClass = bu.getBeanClass(type);
            if (beanClass != null) {
                BeanInfo beanInfo = BeansUnit.getBeanInfo(beanClass, bu.getClassLoader());
                String name = (String) beanInfo.getBeanDescriptor().getValue(Constants.BeanDescriptor.INSTANCE_NAME);
                if(name == null) {
                    name = (String)m.invoke(bu, type);
                }
                return (Bean) ctor.newInstance(bu, beanInfo, name);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
        return null;
    }
    
    protected FileObject getJavaFile(String name) {
        FileObject[] roots = ((WebProject)getProject()).getSourceRoots().getRoots();
        for(FileObject f : roots) {
            FileObject result = findFileObject(f, name);
            if(result != null) {
                return result;
            }
        }
        return null;
    }
    
    protected FileObject findFileObject(FileObject f, String name) {
        FileObject result = null;
        if(f.isFolder()) {
            for(FileObject child : f.getChildren()) {
                result = findFileObject(child, name);
                if(result != null) {
                    return result;
                }
            }
        }else {
            if(name.equals(f.getName())) {
                result = f;
            }
        }
        return result;
    }

    protected String getFQN(String className) {
        FileObject[] roots = ((WebProject)getProject()).getSourceRoots().getRoots();
        for(FileObject f : roots) {
            FileObject result = findFileObject(f, className);
            if(result != null) {
                String filePath = result.getPath().substring(0, result.getPath().lastIndexOf("."));
                String relativePath = filePath.replace(f.getPath(), "");
                return relativePath.replace('/', '.').substring(1);
            }
        }
        return null;
    }

    protected String getPackageName(String className) {
        String relativePath = getFQN(className);
        int lastDotIndex = relativePath.lastIndexOf('.');
        return relativePath.substring(0, lastDotIndex);
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
