/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * NbPath.java
 *
 * Created on February 14, 2001, 6:26 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.*;

/**
 *
 * @author  vstejskal
 * @version 1.0
 */
public class NbPath extends Task {

    public static void main(String args[]) throws Exception {
    }
    
    public void setNbHome(String home) {
        this.nbhome = checkPath(home);
    }
    
    public void setXTHome(String home) {
        this.xthome = checkPath(home);
    }

    public void setAntHome(String home) {
        this.anthome = checkPath(home);
    }

    public void execute () throws BuildException {
        File jar;
        
        if (null == nbhome || null == xthome)
            throw new BuildException("Use nbhome attribute to set up the netbeans home directory.");
  
        // prepare netbeans.test.... path
        StringBuffer list = new StringBuffer(1024);
        if (null == getProject().getProperty(NB_LIBRARY_PATH)) {
            listJars(nbhome, "lib/patches", list);
            listJars(nbhome, "lib", list);
            listJars(nbhome, "lib/ext", list);
            listJars(nbhome, "modules", list);
            listJars(nbhome, "modules/ext", list);
            getProject().setProperty(NB_LIBRARY_PATH, list.toString());
        }
        
        if (null == getProject().getProperty(NB_CLASS_PATH)) {
            list.setLength(0);
            listJars(nbhome, "lib/patches", list);
            listJars(nbhome, "lib", list);
            listJars(nbhome, "lib/ext", list);
            listJars(System.getProperty("java.home"), "lib", list);
            getProject().setProperty(NB_CLASS_PATH, list.toString());
        }
        
        if (null == getProject().getProperty(NB_BOOTCLASS_PATH)) {
            list.setLength(0);
            listJars(System.getProperty("java.home"), "lib", list);
            listJars(System.getProperty("Env-JAVA_HOME"), "lib", list);
            getProject().setProperty(NB_BOOTCLASS_PATH, list.toString());
        }
        
        // prepare ant.class.path property
        String ant_path = getProject().getProperty(ANT_PATH);
        
        if (null == ant_path) {
            if (null != anthome)
                ant_path = lookupAnt(anthome);

            if (null == ant_path && null != System.getProperty("ant.home"))
                ant_path = lookupAnt(System.getProperty("ant.home"));

            if (null == ant_path)
                // will find both optional.jar & ant-optional.jar
                ant_path = lookupJarsFromPath(getProject().getProperty(NB_LIBRARY_PATH), 
                                              new String [] { "ext/ant.jar", "optional.jar" });

            if (null == ant_path)
                ant_path = lookupJarsFromPath(System.getProperty("java.class.path", ""), 
                                              new String [] { "ant.jar", "optional.jar" });

            if (null == ant_path)
                ant_path = "";

            getProject().setProperty(ANT_PATH, ant_path);
        }
        
        // find junit.jar
        String junit_jar = getProject().getProperty(JUNIT_JAR);
        if (null == junit_jar) {
            String junit_jars [] = new String [] { "junit.jar" };
            File f = new File(xthome, "lib/junit.jar");
            if (f.exists())
                junit_jar = f.getAbsolutePath().replace(File.separatorChar, '/');
            if (null == junit_jar)
                junit_jar = lookupJarsFromPath(System.getProperty("java.class.path", ""), junit_jars);
            if (null == junit_jar)
                junit_jar = lookupJarsFromPath(getProject().getProperty(NB_LIBRARY_PATH), junit_jars);
            if (null == junit_jar)
                junit_jar = "";
            getProject().setProperty(JUNIT_JAR, junit_jar);
        }
        
        // find xalan.jar
        String xalan_jar = getProject().getProperty(XALAN_JAR);
        if (null == xalan_jar) {
            String xalan_jars [] = new String [] { "xalan.jar" };
            File f = new File(xthome, "lib/xalan.jar");
            if (f.exists())
                xalan_jar = f.getAbsolutePath().replace(File.separatorChar, '/');
            if (null == xalan_jar)
                xalan_jar = lookupJarsFromPath(System.getProperty("java.class.path", ""), xalan_jars);
            if (null == xalan_jar)
                xalan_jar = lookupJarsFromPath(getProject().getProperty(NB_LIBRARY_PATH), xalan_jars);
            if (null == xalan_jar)
                xalan_jar = "";
            getProject().setProperty(XALAN_JAR, xalan_jar);
        }
        
        // find xerces.jar
        String xerces_jar = getProject().getProperty(XERCES_JAR);
        if (null == xerces_jar) {
            String xerces_jars [] = new String [] { "xerces.jar" };
            File f = new File(xthome, "lib/xerces.jar");
            if (f.exists())
                xerces_jar = f.getAbsolutePath().replace(File.separatorChar, '/');
            if (null == xerces_jar)
                xerces_jar = lookupJarsFromPath(System.getProperty("java.class.path", ""), xerces_jars);
            if (null == xerces_jar)
                xerces_jar = lookupJarsFromPath(getProject().getProperty(NB_LIBRARY_PATH), xerces_jars);
            if (null == xerces_jar)
                xerces_jar = "";
            getProject().setProperty(XERCES_JAR, xerces_jar);
        }
        
        // prepare junit.path property
        String junit_path = getProject().getProperty(JUNIT_PATH);
        if (null == junit_path) {
            list.setLength(0);
            addPath(list, appendSlash(xthome) + "lib/patches/junit-extensions.jar");
            addPath(list, junit_jar);
            junit_path = list.toString();
            getProject().setProperty(JUNIT_PATH, junit_path);
        }

        // prepare xerces.path property
        String xerces_path = getProject().getProperty(XERCES_PATH);
        if (null == xerces_path) {
            list.setLength(0);
            addPath(list, xerces_jar);
            xerces_path = list.toString();
            getProject().setProperty(XERCES_PATH, xerces_path);
        }

        // prepare xalan.path property
        String xalan_path = getProject().getProperty(XALAN_PATH);
        if (null == xalan_path) {
            list.setLength(0);
            addPath(list, xalan_jar);
            addPath(list, xerces_jar);
            xalan_path = list.toString();
            getProject().setProperty(XALAN_PATH, xalan_path);
        }

        // prepare xtest.path property
        if (null == getProject().getProperty(XTEST_PATH)) {
            list.setLength(0);
            addPath(list, appendSlash(xthome) + "lib/xtest.jar");
            addPath(list, appendSlash(xthome) + "lib/xtest-ext.jar");
            getProject().setProperty(XTEST_PATH, list.toString());
        }

        // prepare jdkhome property
        if (null == getProject().getProperty(JDK_HOME)) {
            String jdkhome = lookupJdk();
            if (jdkhome != null)
                getProject().setProperty(JDK_HOME, jdkhome);
        }        
/*        
        // prepare xtest.path property
        if (null == getProject().getProperty(XTEST_PATH)) {
            list.setLength(0);
            addPath(list, appendSlash(xthome) + "lib/xtest.jar");
            addPath(list, appendSlash(xthome) + "lib/xtest-ext.jar");
            addPath(list, ant_path);
            addPath(list, junit_path);
            addPath(list, xalan_jar);
            addPath(list, xerces_jar);
            getProject().setProperty(XTEST_PATH, list.toString());
        }
 */
    }

    private void listJars(String root, String folder, StringBuffer list) {
        if (null == root)
            return;
        
        File            fld = new File(Path.translateFile(appendSlash(root) + folder));
        File []      childs = fld.listFiles(new FileExtFilter("jar"));
        
        if (null == childs)
            return;
        
        for(int i = 0; i < childs.length; i++)
            addPath(list, childs[i].getAbsolutePath());
    }
    
    private void addPath(StringBuffer list, String path) {
        if (0 != list.length() && File.pathSeparatorChar != list.charAt(list.length() - 1))
            list.append(File.pathSeparatorChar);
        list.append(path.replace('\\', '/'));
    }
    
    private String appendSlash(String src) {
        String newS = new String(src);
        if (!src.endsWith("/"))
            newS += "/";
        
        return newS;
    }

    private String lookupAnt(String antHome) {
        StringBuffer list;
        File jar1 = new File(antHome, "lib/ant.jar");
        File jar2 = new File(antHome, "lib/optional.jar");
        
        if (!jar2.exists())
            jar2 = new File(antHome, "lib/ant-optional.jar");
        
        if (!jar1.exists() || !jar2.exists())
            return null;
        
        list = new StringBuffer(256);
        addPath(list, jar1.getAbsolutePath());
        addPath(list, jar2.getAbsolutePath());
        
        return list.toString();
    }
    
    private String lookupJarsFromPath(String path, String jars[]) {
        StringBuffer list = new StringBuffer();
        
        for(int i = 0; i < jars.length; i++) {
            String jarPath;
            if (null == (jarPath = getJarPath(path, jars[i])))
                return null;
            
            addPath(list, jarPath);
        }
        
        return list.toString();
    }

    private String getJarPath(String path, String jar) {
        int iJar        = path.indexOf(jar);
        int iBegin;
        int iEnd;
        
        if (-1 == iJar)
            return null;
        
        iBegin = path.lastIndexOf(File.pathSeparatorChar, iJar);
        iBegin = -1 == iBegin ? 0 : iBegin + 1;
        iEnd = path.indexOf(File.pathSeparatorChar, iJar);
        iEnd = -1 == iEnd ? path.length() : iEnd;
        return path.substring(iBegin, iEnd);
    }
    
    private String checkPath(String path) {
        File f = new File(path);
        if (!f.isAbsolute())
            f = getProject().resolveFile(path);
        return f.getAbsolutePath();
    }

    private String lookupJdk() {
        File jdk = new File(getProject().getProperty("java.home"));
        if (jdk != null && jdk.exists ()) {
            File tmp = new File(jdk, "lib/tools.jar");
            if (!tmp.exists ()) {
                jdk = jdk.getParentFile ();
                
                tmp = new File(jdk, "lib/tools.jar");
                if (!tmp.exists ())
                    return null;
            }
            
            return jdk.getAbsolutePath ().replace ('\\', '/');
        }
        return null;
    }
    
    private class FileExtFilter implements FileFilter {
        protected String extension = null;
        public FileExtFilter(String extension) {
            this.extension = extension;
        }
        public boolean accept(File pathname) {
            String  ext = null;
            int     i;
            
            if (-1 != (i = pathname.getPath().lastIndexOf('.')))
                ext = pathname.getPath().substring(i + 1);
            
            if (null == ext)
                return (null == extension);
            
            return extension.equals(ext);
        }
    }
    
    private String nbhome = null;
    private String xthome = null;
    private String anthome = null;
    
    private static String NB_LIBRARY_PATH     = "netbeans.test.library.path";
    private static String NB_CLASS_PATH       = "netbeans.test.class.path";
    private static String NB_BOOTCLASS_PATH   = "netbeans.test.bootclass.path";
    private static String XTEST_PATH          = "xtest.path";
    private static String ANT_PATH            = "ant.path";
    private static String JUNIT_JAR           = "junit.jar";
    private static String JUNIT_PATH          = "junit.path";
    private static String XALAN_JAR           = "xalan.jar";
    private static String XALAN_PATH          = "xalan.path";
    private static String XERCES_JAR          = "xerces.jar";
    private static String XERCES_PATH         = "xerces.path";
    private static String JDK_HOME            = "jdkhome";
}
