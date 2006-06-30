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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
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
     * Returns the source root (if any) for given url.
     *
     * @param url a url of resource file
     *
     * @return the source root or <code>null</code> when no source root was found.
     * @since 2.6
     */
    public String getSourceRoot(String url) {
        return null;
    }
        
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

