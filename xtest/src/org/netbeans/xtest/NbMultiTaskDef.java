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
    private ClassLoader loader = null;

    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
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
    
    private static boolean isJDK14orHigher() {
        String javaVersion = System.getProperty("java.specification.version","1.0");
        if (javaVersion.compareTo("1.4") < 0 ) {
            return false;
        } else {
            return true;
        }
    }
    
    public void execute () throws BuildException {
        if (!isJDK14orHigher()) {
            throw new BuildException("XTest requires JDK 1.4 or higher to run tests.");
        }
        if (null != System.getProperty("test.ant.file")) {
            log("Using Netbeans classloader.", Project.MSG_DEBUG);
            loader = Thread.currentThread().getContextClassLoader();
            System.out.println("NetBeans class loader: "+loader);
        }

        if (classpath != null) {
            log("Using Ant classloader.", Project.MSG_DEBUG);
            AntClassLoader al = new AntClassLoader(loader, getProject(), classpath,
                                                 true);
            // need to load Task via system classloader or the new
            // task we want to define will never be a Task but always
            // be wrapped into a TaskAdapter.
            al.addSystemPackageRoot("org.apache.tools.ant");
            
            // need to load parsers defintion from current loader instead of 
            // system loader (packages java, javax are by default loaded using
            // system classloader)
            //al.addLoaderPackageRoot("javax.xml");
            //al.addLoaderPackageRoot("org.w3c.dom");
            //al.addLoaderPackageRoot("org.xml.sax");
            ///al.addLoaderPackageRoot("org.apache.xerces");
            //al.addLoaderPackageRoot("org.apache.html");
            //al.addLoaderPackageRoot("org.apache.wml");
            //al.addLoaderPackageRoot("org.apache.xml");
            //al.addLoaderPackageRoot("org.apache.xalan");
            //al.addLoaderPackageRoot("org.apache.xpath"); 
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
            throw new BuildException(msg, cnfe, getLocation());
        } catch (NoClassDefFoundError ncdfe) {
            String msg = "taskdef class " + classname +
                " cannot be found";
            throw new BuildException(msg, ncdfe, getLocation());
        }

        getProject().addTaskDefinition(taskname, taskClass);
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
