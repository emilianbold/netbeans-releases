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
 * NbMultiTaskDef.java
 *
 * Created on May 3, 2001, 1:19 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import java.util.LinkedList;
import java.util.Iterator;

import org.openide.TopManager;

/**
 *
 * @author  vs124454
 * @version 
 */
public class NbMultiTaskDef extends Task {
    private Path classpath = null;
    private LinkedList taskdefs = new LinkedList();
    private String name = null;
    private String classname = null;
    
    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(project);
        }
        return this.classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public void addTaskDef(NbTaskDef def) {
        taskdefs.add(def);
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setClassname(String name) {
        this.classname = name;
    }
    
    public void execute () throws BuildException {
        ClassLoader loader = null;
        if (null != System.getProperty("netbeans.user")) {
            log("Using Netbeans classloader.", Project.MSG_DEBUG);
            loader = TopManager.getDefault().currentClassLoader();
        }
        
        if (loader == null && classpath != null) {
            log("Using Ant classloader.", Project.MSG_DEBUG);
            AntClassLoader al = new AntClassLoader(project, classpath,
                                                   false);
            // need to load Task via system classloader or the new
            // task we want to define will never be a Task but always
            // be wrapped into a TaskAdapter.
            al.addSystemPackageRoot("org.apache.tools.ant");
            
            // need to load parsers defintion from current loader instead of 
            // system loader (packages java, javax are by default loaded using
            // system classloader)
            al.addLoaderPackageRoot("javax.xml");
            loader = al;
        }

        if (loader == null)
            log("Using default classloader.", Project.MSG_DEBUG);
            
        if (null != name && null != classname)
            defineTask(classname, name, loader);
        
        Iterator i = taskdefs.iterator();
        while(i.hasNext()) {
            NbTaskDef def = (NbTaskDef)i.next();
            defineTask(def.getClassname(), def.getName(), loader);
        }
    }
    
    private void defineTask(String classname, String taskname, ClassLoader loader) throws BuildException {
        Class taskClass = null;

        try {
            if (loader != null) {
                taskClass = loader.loadClass(classname);
            } else {
                taskClass = Class.forName(classname);
            }
        } catch (ClassNotFoundException cnfe) {
            String msg = "taskdef class " + classname +
                " cannot be found";
            throw new BuildException(msg, cnfe, location);
        } catch (NoClassDefFoundError ncdfe) {
            String msg = "taskdef class " + classname +
                " cannot be found";
            throw new BuildException(msg, ncdfe, location);
        }

        project.addTaskDefinition(taskname, taskClass);
    }
    
    public static class NbTaskDef {
        private String name;
        private String classname;
        
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getClassname() {
            return classname;
        }

        public void setClassname(String name) {
            classname = name;
        }
    }
}
