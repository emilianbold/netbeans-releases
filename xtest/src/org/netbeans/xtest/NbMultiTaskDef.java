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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
