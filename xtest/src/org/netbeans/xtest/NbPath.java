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
        NbPath nb = new NbPath();
        
        nb.setNbHome("c:/sw/nb126");
        nb.execute();
    }
    
    public void setNbHome(String home) {
        this.home = home;
    }
    
    public void execute () throws BuildException {
        if (null == home)
            throw new BuildException("Use nbhome attribute to set up the netbeans home directory.");
        
        StringBuffer list = new StringBuffer(1024);
        listJars(home, "lib/patches", list);
        listJars(home, "lib", list);
        listJars(home, "lib/ext", list);
        listJars(home, "modules", list);
        listJars(home, "modules/ext", list);
        getProject().setProperty(NB_LIBRARY_PATH, list.toString());

        list.setLength(0);
        listJars(home, "lib/patches", list);
        listJars(home, "lib", list);
        listJars(home, "lib/ext", list);
        listJars(System.getProperty("java.home"), "lib", list);
        getProject().setProperty(NB_CLASS_PATH, list.toString());

        list.setLength(0);
        listJars(System.getProperty("java.home"), "lib", list);
        listJars(System.getProperty("Env-JAVA_HOME"), "lib", list);
        getProject().setProperty(NB_BOOTCLASS_PATH, list.toString());
        
        list.setLength(0);
        if (null != System.getProperty("ant.home")) {
            listJars(System.getProperty("ant.home"), "lib/patch", list);
            listJars(System.getProperty("ant.home"), "lib", list);
        }
        else {
            File jar = new File(System.getProperty("netbeans.home"), "modules/ext/ant.jar");
            if (jar.exists())
                addPath(list, jar.getAbsolutePath());
            jar = new File(System.getProperty("netbeans.home"), "modules/ext/optional.jar");
            if (jar.exists())
                addPath(list, jar.getAbsolutePath());
            listJars(System.getProperty("netbeans.home"), "lib/ext", list);
        }
        getProject().setProperty(ANT_CLASS_PATH, list.toString());
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
        if (0 != list.length()) 
            list.append(";");
        list.append(path.replace('\\', '/'));
    }
    
    private String appendSlash(String src) {
        String newS = new String(src);
        if (!src.endsWith("/"))
            newS += "/";
        
        return newS;
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
    
    private String home = null;
    private static String NB_LIBRARY_PATH     = "netbeans.test.library.path";
    private static String NB_CLASS_PATH       = "netbeans.test.class.path";
    private static String NB_BOOTCLASS_PATH   = "netbeans.test.bootclass.path";
    private static String ANT_CLASS_PATH      = "ant.class.path";
}
