/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Micro//S ystems, Inc. Portions Copyright 1997-2001 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Set;

/**
 * Defines source path for debugger. It translates relative path 
 * (like "java/lang/Thread.java", or class name) to url 
 * ("file:///C:/Sources/java/lang/Thread.java"). It allows to define 
 * and modify source path.
 * All instances of this class should be registerred in
 * "Meta-inf/debugger/<DebuggerEngine ID>/org.netbeans.spi.debugger.jpda.EngineContextProvider"
 * files. There should be at least one instance installed.
 *
 * @author Maros Sandor, Jan Jancura
 */
public abstract class SourcePathProvider {

    /** Property name constant. */
    public static final String PROP_SOURCE_ROOTS = "sourceRoots";
    
    /**
     * Returns relative path (java/lang/Thread.java) for given url 
     * ("file:///C:/Sources/java/lang/Thread.java").
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    public abstract String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
     );

    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    public abstract String getURL (String relativePath, boolean global);
    
    /**
     * Returns array of source roots.
     */
    public abstract String[] getSourceRoots ();
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    public abstract void setSourceRoots (String[] sourceRoots);
    
    /**
     * Returns set of original source roots.
     *
     * @return set of original source roots
     */
    public abstract String[] getOriginalSourceRoots ();
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );
}

