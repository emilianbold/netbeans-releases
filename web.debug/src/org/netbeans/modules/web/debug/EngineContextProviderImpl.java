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

package org.netbeans.modules.web.debug;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;


/**
 *
 * @author Jan Jancura
 */
public class EngineContextProviderImpl extends SourcePathProvider {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.enginecontextproviderimpl") != null;

    private static final Set virtualFolders = new HashSet (Arrays.asList (
        new String[] {
            "org",
            "org/apache",
            "org/apache/jsp"
        }
    ));

    
    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    public String getURL (String relativePath, boolean global) {
        if (verbose) System.out.println ("ECPI(JSP): getURL " + relativePath + " global " + global);
        if ((relativePath == null) || (relativePath.endsWith(".java"))) {
           return null; 
        }
        if (virtualFolders.contains (relativePath) || relativePath.startsWith("org/apache/jsp")) {
            if (verbose) System.out.println ("ECPI(JSP):  fo virtual folder");
            return "virtual folder";
        }
        return null;
    }

    
    /**
     * Returns relative path for given url.
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    public String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
    ) {
        return null;
    }
    
    /**
     * Returns set of original source roots.
     *
     * @return set of original source roots
     */
    public String[] getOriginalSourceRoots () {
        return new String [0];
    }
    
    /**
     * Returns array of source roots.
     *
     * @return array of source roots
     */
    public String[] getSourceRoots () {
        return new String [0];
    }
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    public void setSourceRoots (String[] sourceRoots) {
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
    }
}
