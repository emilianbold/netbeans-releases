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
/*
 * Manager.java
 *
 * Created on February 8, 2001, 3:33 PM
 */

package org.netbeans.junit;

import java.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.netbeans.junit.diff.*;
/**
 *
 * @author  vstejskal
 */
public class Manager extends Object {

    /** Creates new Manager */
    private Manager() {
    }
    
    public static final String JUNIT_PROPERTIES_FILENAME = "junit.properties";
    public static final String JUNIT_PROPERTIES_LOCATION_PROPERTY = "junit.properties.file";

    protected static final String PROP_DIFF_IMPL        = "nbjunit.diff.impl";
    protected static final String DEFAULT_DIFF_IMPL     = "org.netbeans.junit.diff.SimpleDiff";    
    protected static Diff systemDiff         = null;
    protected static Properties fPreferences            = null;
    
    
    // workdir stuff
        /**
     * name of the system property defining root workdir direcory
     * - it must be set before running tests using workdir
     * - in the case of running tests from XTest framework, this 
     *   property is set by the framework itself
     * - otherwise the default is ${java.io.tmpdir}/tests
     */
    public static final String NBJUNIT_WORKDIR = "nbjunit.workdir";
    
    // nbjunit home dir - directory where nbjunit.jar and other files are stored
    public static final String NBJUNIT_HOME = "nbjunit.home";
    
    
    static {
        fPreferences = new Properties();
        fPreferences.put(PROP_DIFF_IMPL, DEFAULT_DIFF_IMPL);
    }
    
    public static Diff getSystemDiff() {
        String diffImplName;
                
        if (null == systemDiff) {
            readProperties();
            diffImplName = fPreferences.getProperty(PROP_DIFF_IMPL);            
            systemDiff = instantiateDiffImpl(diffImplName);
            if (null == systemDiff && !diffImplName.equals(DEFAULT_DIFF_IMPL)) {
                systemDiff = instantiateDiffImpl(DEFAULT_DIFF_IMPL);
            }
        }        
        return systemDiff;
    }
    
    public static String getWorkDirPath() {
        String path = System.getProperty(NBJUNIT_WORKDIR);
                
        if (path == null) {            
            // try to get property from user's settings
            readProperties();            
            path = fPreferences.getProperty(NBJUNIT_WORKDIR);
        }
        if (path != null) {
            path = path.replace('/', File.separatorChar);
        } else {
            // Fallback value, guaranteed to be defined.
            path = System.getProperty("java.io.tmpdir") + File.separatorChar + "tests";
        }
        return path;
    }
    
    public static String getNbJUnitHomePath() throws IOException {
        String path = System.getProperty(NBJUNIT_HOME);
                
        if (path == null) {            
            // try to get property from user's settings
            readProperties();            
            path = fPreferences.getProperty(NBJUNIT_HOME);
        }
        if (path != null) {
            path = path.replace('/', File.separatorChar);
            return path;
        } else {
            throw new IOException("Cannot determine NbJUnit home. Please make sure you have "+NBJUNIT_HOME
                        +" propery set in your "+JUNIT_PROPERTIES_FILENAME+" file.");
        }

    }    
    
    
    public static File getNbJUnitHome() throws IOException {
        File nbJUnitHome = new File(getNbJUnitHomePath());
        if (nbJUnitHome.isDirectory()) {            
            return nbJUnitHome;
        } else {
            throw new IOException("Property "+NBJUNIT_HOME+" does not point to nbjunit home.");
        }
    }
    
    protected static Diff instantiateDiffImpl(String diffImplName) {
        Diff     impl = null;
        Class               clazz;
        Object              diffImpl = null;
        Class []            prmString = null;
        Method              method;
        Enumeration         propNames;
        
        try {
            prmString = new Class [] { Class.forName("java.lang.String") };
            
            // instantiate the diff class
            clazz = Class.forName(diffImplName);
            diffImpl = clazz.newInstance();

            if (diffImpl instanceof Diff) {
                impl = (Diff) diffImpl;
            
                propNames = fPreferences.propertyNames();
                while (propNames.hasMoreElements()) {
                    String propName = (String) propNames.nextElement();
                    
                    if (propName.equals(PROP_DIFF_IMPL) || !propName.startsWith(PROP_DIFF_IMPL))
                        continue;
                    
                    String setter = "set" + propName.substring(PROP_DIFF_IMPL.length() + 1);
                    try {
                        method = clazz.getMethod(setter, prmString);
                    }
                    catch (NoSuchMethodException e) {
                        System.out.println("The method " + setter + " not fond in class " + diffImplName + ".");
                        method = null;
                    }
                    if (null != method)
                        method.invoke(impl, new Object [] { fPreferences.getProperty(propName, "") });
                }
            }
        }
        catch (Exception e) {
            // ignore exception
        }
        return impl;
    }
    
    private static File getPreferencesFile() {
        String junitPropertiesLocation = System.getProperty(Manager.JUNIT_PROPERTIES_LOCATION_PROPERTY);
        if (junitPropertiesLocation != null) {
            File propertyFile = new File(junitPropertiesLocation);
            if (propertyFile.exists()) {
                return propertyFile;
            }
        }
        // property file was not found - lets fall back to defaults
        String home= System.getProperty("user.home");
        return new File(home, Manager.JUNIT_PROPERTIES_FILENAME);
    }
    
    protected static void readProperties() {
        InputStream is= null;
        try {
            File propFile = getPreferencesFile();
            is= new FileInputStream(propFile);
            fPreferences= new Properties(fPreferences);
            fPreferences.load(is);
        } 
        catch (IOException e) {
            try {
                if (is != null)
                    is.close();
            } 
            catch (IOException e1) {
            }
        }
    }
}
