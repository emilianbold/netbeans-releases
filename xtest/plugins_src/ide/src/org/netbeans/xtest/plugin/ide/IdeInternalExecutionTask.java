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
package org.netbeans.xtest.plugin.ide;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.openide.util.Lookup;

/** Task intended to run functional test class in IDE's JVM. It gets
 * IDE's system class loader, adds given classpath and invokes main method
 * of given class.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class IdeInternalExecutionTask extends Task {
    
    /** Execution classpath. */
    protected Path classpath;
    /** Fully qualified name of class to be run. */
    protected String classname;
    
    
    /** Sets classpath property.
     * @param classpath new value
     */    
    public void setClasspath(Path classpath) {
        this.classpath = classpath;
    }
    
    /** Creates classpath
     * @return created path
     */    
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return classpath.createPath();
    }
    
    /** Sets classname property.
     * @param classname fully-qualified class name
     */    
    public void setClassname(String classname) {
        this.classname = classname;
    }
    
    /** Gets IDE's system class loader, adds given classpath and invokes main 
     * method of given class.
     * @throws BuildException when something's wrong
     */    
    public void execute() throws BuildException {
        try {
            ClassLoader systemClassloader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            URL[] urls = classpathToURL(classpath);
            URLClassLoader testClassLoader = new TestClassLoader(urls, systemClassloader);
            Class classToRun = testClassLoader.loadClass(this.classname);
            Method method = classToRun.getDeclaredMethod("main", new Class[] {String[].class});
            method.invoke(null, new Object[] {null});
        } catch (Exception e) {
            throw new BuildException(e.getClass().getName()+": "+e.getMessage(), e);
        }
    }
    
    /**
     * Converts given classpath to an array of URLs.
     * @param classpath classpath to be converted
     * @return array of URLs
     * @throws MalformedURLException thrown when a path is wrong
     */
    public static URL[] classpathToURL(Path classpath) throws MalformedURLException {
        String[] list = classpath.list();
        URL[] urls = new URL[list.length];
        for(int i=0;i<list.length;i++) {
            urls[i] = new File(list[i]).toURI().toURL();
        }
        return urls;
    }
    
    /** Classloder with overriden getPermissions method because it doesn't
     * have sufficient permissions when run from IDE.
     */
    private static class TestClassLoader extends URLClassLoader {
        
        public TestClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        
        protected PermissionCollection getPermissions(CodeSource codesource) {
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            permissions.setReadOnly();
            
            return permissions;
        }
    }
}
