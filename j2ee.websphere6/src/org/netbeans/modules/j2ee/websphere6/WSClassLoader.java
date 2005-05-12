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
package org.netbeans.modules.j2ee.websphere6;

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Kirill Sorokin
 */
public class WSClassLoader extends URLClassLoader {
    
    private static Map instances = new HashMap();
    
    public static WSClassLoader getInstance(String serverRoot, String domainRoot) {
        if (WSDebug.isEnabled())
            WSDebug.notify(WSClassLoader.class, "getInstance(" + serverRoot + ", " + domainRoot + ")");
        
        WSClassLoader instance = (WSClassLoader) instances.get(domainRoot);
        
        if (instance == null) {
            instance = new WSClassLoader(serverRoot, domainRoot);
            instances.put(domainRoot, instance);
        }
        return instance;
    }
    
    
    private String serverRoot;
    private String domainRoot;
    
    private WSClassLoader(String serverRoot, String domainRoot) {
        // ksorokin - fix for 
        //super(new URL[0], Thread.currentThread().getContextClassLoader());
        super(new URL[0]);
        
        this.serverRoot = serverRoot;
        this.domainRoot = domainRoot;

        File[] dirs = new File[] {
            new File(serverRoot + "/lib/"), // NOI18N
            new File(serverRoot + "/java/jre/lib/"), // NOI18N
            new File(serverRoot + "/java/jre/lib/ext/") // NOI18N
        };
        
        for (int i = 0; i < dirs.length; i++) {
            File dir = dirs[i];
            if (dir.exists() && dir.isDirectory()) {
                File[] children = dir.listFiles(new JarFileFilter());
                for (int j = 0; j < children.length; j++) {
                    try {
                        addURL(children[j].toURL());
                    } catch (MalformedURLException e) {
                        // do nothing just skip this jar
                    }
                }
            }
            try {
                addURL(dir.toURL());
            } catch (MalformedURLException e) {
                // do nothing just skip this directory
            }

        }
    }
    
    private ClassLoader oldLoader;
    
    public void updateLoader() {
        if (WSDebug.isEnabled())
            WSDebug.notify(getClass(), "updateLoader()");
        
        System.setProperty("websphere.home", serverRoot); // NOI18N
        System.setProperty("was.install.root", serverRoot); // NOI18N
        System.setProperty("was.repository.root", domainRoot + File.separator + "config");
        
        if (WSDebug.isEnabled())
            System.setProperty("traceSettingsFile", "TraceSettings.properties");
        
        if (!Thread.currentThread().getContextClassLoader().equals(this)) {
            oldLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this);
        }
    }
    
    public void restoreLoader() {
        if (WSDebug.isEnabled())
            WSDebug.notify(getClass(), "restoreLoader()");
        
        if (oldLoader != null) {
            Thread.currentThread().setContextClassLoader(oldLoader);
            oldLoader = null;
        }
    }
    
    /**
     * File filter that accepts only .jar files
     */
    private static class JarFileFilter implements FileFilter {
        public boolean accept(File file) {
            if (file.getName().endsWith(".jar")) { // NOI18N
                return true;
            } else {
                return false;
            }
        }
    }
}
