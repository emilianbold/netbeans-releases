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
 * $Id$
 */

package org.netbeans.installer.utils.system.launchers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;

/**
 *
 * @author Dmitry Lipin
 */
public class LauncherProperties implements Cloneable {
    
    protected File stubFile;    
    protected List<LauncherResource> jars;    
    protected List<LauncherResource> jvms;
    protected HashMap <String, PropertyResourceBundle> i18nMap;
    protected LauncherResource testJVMFile;
    protected File outputFile;
    protected boolean addExtenstion;
    protected String [] jvmArguments;
    protected String [] appArguments;
    protected String mainClass;
    protected String testJVMClass;
    protected List <JavaCompatibleProperties> compatibleJava;
    
    public LauncherResource getTestJVMFile() {
        return testJVMFile;
    }
    
    public LauncherProperties(LauncherProperties nl) {
        appArguments = nl.appArguments;
        jvmArguments = nl.jvmArguments;
        i18nMap = nl.i18nMap;
        jars = nl.jars;        
        jvms = nl.jvms;        
        outputFile = nl.outputFile;
        addExtenstion = nl.addExtenstion;
        compatibleJava = nl.compatibleJava;
        mainClass = nl.mainClass;
        testJVMClass = nl.testJVMClass;
        stubFile = nl.stubFile;
        testJVMFile = nl.testJVMFile;
    }
    public LauncherProperties() {
        compatibleJava = new ArrayList <JavaCompatibleProperties> ();
        jvmArguments = new String [] {};
        appArguments = new String [] {};
        i18nMap = new HashMap <String, PropertyResourceBundle>();        
        jars = new ArrayList <LauncherResource> ();
        jvms = new ArrayList <LauncherResource> ();
    }
    public void setLauncherStub(File launcherStub) {
        this.stubFile = launcherStub;
    }
    
    public void addJar(LauncherResource file) {
        jars.add(file);
    }
    public String getMainClass() {
        return mainClass;
    }
    public String getTestJVMClass() {
        return testJVMClass;
    }
    
    public void setJvmArguments(String[] jvmArguments) {
        this.jvmArguments = jvmArguments;
    }
    
    public void setI18n(File i18nDir) throws IOException {
        loadPropertiesMap(getPropertiesFiles(i18nDir));
    }
    
    public void setI18n(File [] files) throws IOException  {
        loadPropertiesMap(files);
    }
    
    public void setI18n(String [] resources) throws IOException {
        loadPropertiesMap(resources);
    }
    
    public void setI18n(List <String>resources) throws IOException {
        loadPropertiesMap(resources);
    }
    
    public void setOutput(File output) {
        setOutput(output, false);
    }
    
    public void setOutput(File output, boolean addExt) {
        this.outputFile    = output;
        this.addExtenstion = addExt;
    }
    
    public void setTestJVM(LauncherResource testJVM) {
        this.testJVMFile = testJVM;
    }
    
    public void addCompatibleJava(JavaCompatibleProperties javaProp) {
        compatibleJava.add(javaProp);
    }
    
    public void setAppArguments(String [] appArguments) {
        this.appArguments = appArguments;
    }
    
    public File getOutputFile() {
        return outputFile;
    }
    
    public List<LauncherResource> getJars() {
        return jars;
    }
    public String[] getAppArguments() {
        return appArguments;
    }
    
    public String[] getJvmArguments() {
        return jvmArguments;
    }
    
    public List <JavaCompatibleProperties> getJavaCompatibleProperties() {
        return compatibleJava;
    }
    
    public File getStubFile() {
        return stubFile;
    }
   
    public void addJVM(LauncherResource location) {
        jvms.add(location);
    }
    public List<LauncherResource> getJVMs() {
        return jvms;
    }
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
    public void setTestJVMClass(String testClass) {
        this.testJVMClass = testClass;
    }
    HashMap <String, PropertyResourceBundle> getI18nMap() {
        return i18nMap;
    }
    
    private String getLocaleName(String name) {
        String loc = "";
        int idx = name.indexOf("_");
        int end = name.indexOf(FileUtils.PROPERTIES_EXTENSION);
        if(idx!=-1) {
            loc = name.substring(idx+1,end);
        }
        return loc;
    }
    
    private PropertyResourceBundle getBundle(File file) throws IOException {
        return getBundle(file.getPath(), new FileInputStream(file));
    }
    
    private PropertyResourceBundle getBundle(String dest, InputStream is) throws IOException {
        if(is==null) {
            throw new IOException("Can`t load bundle from " + dest); //NOI18N
        }
        
        try {
            return new PropertyResourceBundle(is);
        } catch (IOException ex) {
            throw new IOException("Can`t load bundle from " + dest);//NOI18N
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                ex=null;
            }
        }
    }
    
// resources should be in form of <dir>/<dir>/<dir>/<file>
    private void loadPropertiesMap(String [] resources) throws IOException {
        i18nMap.clear();
        for(String resource: resources) {
            String loc = getLocaleName(ResourceUtils.getResourceFileName(resource));
            i18nMap.put(loc, getBundle(resource, ResourceUtils.getResource(resource)));
        }
    }
    private void loadPropertiesMap(List<String> resources) throws IOException {
        String [] array = new String [resources.size()];
        for(int i=0;i<resources.size();i++) {
            array [i] = resources.get(i);
        }
        loadPropertiesMap(array);
    }
    
    private File[] getPropertiesFiles(File dir) throws IOException {
        if(!dir.exists()) {
            throw new IOException("Directory " + dir + " doesn`t exists");
        }
        if(!dir.isDirectory()) {
            throw  new IOException(dir + " is not a directory");
        }
        
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File filename) {
                return filename.getName().endsWith(FileUtils.PROPERTIES_EXTENSION); }
        }
        );
        
        if(files==null) {
            throw  new IOException("There is no files in " + dir);
        }
        if(files.length==0) {
            throw  new IOException("There is no files in " + dir);
        }
        return files;
    }
    
    private void loadPropertiesMap(File [] files) throws IOException {
        i18nMap.clear();
        for(File f: files) {
            String loc = getLocaleName(f.getName());
            LogManager.log("Adding bundle with locale [" + loc + "] using file " + f);
            i18nMap.put(loc,getBundle(f));
        }
    }
}
