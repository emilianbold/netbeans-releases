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
        System.out.println("System:"+NbPath.isJDK14AndOver());
    }
    
      
    
    public void setXTestHome(String xtesthome) {
        this.xtesthome = checkPath(xtesthome);
    }

    public void setAntHome(String home) {
        this.anthome = checkPath(home);
    }
    
    public static boolean isJDK14AndOver() {
        String specificationVersion = System.getProperty("java.specification.version");
        if (specificationVersion != null) {
            try {
                float version = Float.parseFloat(specificationVersion);
                if (version >= 1.4F) {
                    //System.out.println("NbPath:isJDK14AndOver(): returning true");
                    return true;
                }
            } catch (NumberFormatException nfe) {
                // cannot parse - probably not a 1.4 or greater
            }
        }
        //System.out.println("NbPath:isJDK14AndOver(): returning false");
        return false;
    }

    public void execute () throws BuildException {
        File jar;
        
        if (null == xtesthome)
            throw new BuildException("Use xtesthome attribute to set xtest.home directory.");  
        
        
        StringBuffer list = new StringBuffer(1024);
        
        // prepare ant.class.path property
        String ant_path = getProject().getProperty(ANT_PATH);
        
        if (null == ant_path) {
            if (null != anthome)
                ant_path = lookupAnt(anthome);

            if (null == ant_path && null != System.getProperty("ant.home"))
                ant_path = lookupAnt(System.getProperty("ant.home"));
    

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
            File f = new File(xtesthome, "lib/junit.jar");
            if (f.exists())
                junit_jar = f.getAbsolutePath().replace(File.separatorChar, '/');
            if (null == junit_jar)
                junit_jar = lookupJarsFromPath(System.getProperty("java.class.path", ""), junit_jars);
            if (null == junit_jar)
                junit_jar = "";
            getProject().setProperty(JUNIT_JAR, junit_jar);
        }  
        
        // prepare junit.path property
        String junit_path = getProject().getProperty(JUNIT_PATH);
        if (null == junit_path) {
            list.setLength(0);
            addPath(list, appendSlash(xtesthome) + "lib/nbjunit.jar");
            addPath(list, junit_jar);
            junit_path = list.toString();
            getProject().setProperty(JUNIT_PATH, junit_path);
        } 
        
        // prepare xtest.path property
        if (null == getProject().getProperty(XTEST_PATH)) {
            list.setLength(0);
            addPath(list, appendSlash(xtesthome) + "lib/xtest.jar");
            addPath(list, appendSlash(xtesthome) + "lib/xtest-junit-testrunner.jar");
            getProject().setProperty(XTEST_PATH, list.toString());
        }

        // prepare jdkhome property
        if (null == getProject().getProperty(JDK_HOME)) {
            String jdkhome = lookupJdk();
            if (jdkhome != null)
                getProject().setProperty(JDK_HOME, jdkhome);
        }        

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
            if (!System.getProperty("os.name").startsWith("Mac OS X")) {
                File tmp = new File(jdk, "lib/tools.jar");
                if (!tmp.exists ()) {
                    jdk = jdk.getParentFile ();
                
                    tmp = new File(jdk, "lib/tools.jar");
                    if (!tmp.exists ())
                        return null;
                }
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
    
    
    private String xtesthome = null;
    private String anthome = null;
    
    private static String XTEST_PATH          = "xtest.path";
    private static String ANT_PATH            = "ant.path";
    private static String JUNIT_JAR           = "junit.jar";
    private static String JUNIT_PATH          = "junit.path";
    private static String JDK_HOME            = "jdkhome";
}
