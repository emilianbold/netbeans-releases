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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.VersioningManager;

import java.io.File;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * Base class for a versioning system that integrates into IDE.
 *
 * A versioning system provides these services:
 * - annotations (coloring, actions)
 * - file system handler
 * - diff provider
 * 
 * Versioning system registration is done via META-INF/services (default Lookup). Create a file named
 * "org.netbeans.modules.versioning.spi.VersioningSystem" and place it inside your module's META-INF/services folder. 
 * The file should only contain one the name of your VS implementation class, eg "org.mymodule.MyVersioningSystem".  
 *
 * @author Maros Sandor
 */
public abstract class VersioningSystem {

    /**
     * Short name of the versioning system, it will be used as popup menu label, label in tooltips, etc.
     * Examples: CVS, Subversion, Mercurial, Teamware, SourceSafe, VSS, Clearcase, Local History.
     * @see #getProperty(String) 
     * @see #putProperty(String, Object)  
     */
    public static final String PROP_DISPLAY_NAME = "String VCS.DisplayName";

    /**
     * Short name of the versioning system, it will be used as menu label and it should define a mnemonic key.
     * Examples: &CVS, &Subversion, &Mercurial, &Teamware, &SourceSafe, &VSS, &Clearcase, Local &History.
     * @see #getProperty(String) 
     * @see #putProperty(String, Object)  
     */
    public static final String PROP_MENU_LABEL = "String VCS.MenuLabel";
    
    /**
     * Marker property for a Versioning system that operates in Local History mode. Local History is a special versioning
     * system with these properties:
     * 
     * - there is only one local history module active at any one time, the first encoutered module wins
     * - local history module is not exclusive with other regsitered 'normal' versioning systems. This means that 
     *   filesystems events may be processed both by Local history module and by some other versioning system module
     * 
     * NOTE: Local History is implemented by default, use this only if you are writing a replacement module 
     */
    public static final String PROP_LOCALHISTORY_VCS = "Boolean VCS.LocalHistory";
        
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private final Map<String, Object> properties = Collections.synchronizedMap(new HashMap<String, Object>());
    
    /**
     * Protected constructor, does nothing.   
     */
    protected VersioningSystem() {
    }

    /**
     * Gets a general property of a Versioning system.
     * 
     * @param key property key
     * @return Object property value, may be null
     * @see #PROP_DISPLAY_NAME  
     * @see #PROP_MENU_LABEL  
     */
    public final Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Sets a general property of a Versioning system.
     * 
     * @param key property key, must NOT be null
     * @param value property value, may be null
     * @see #PROP_DISPLAY_NAME  
     * @see #PROP_MENU_LABEL  
     */
    protected final void putProperty(String key, Object value) {
        if (key == null) throw new IllegalArgumentException("Property name is null");
        properties.put(key, value);
    }
    
    /**
     * Tests whether the file is managed by this versioning system. If it is, the method should return the topmost 
     * ancestor of the file that is still versioned.
     * For example (for CVS) if all your CVS checkouts are in a directory /home/johndoe/projects/cvscheckouts/... then for all files
     * that are under "cvscheckouts" directory and for the directory itselft this method should 
     * return "/home/johndoe/projects/cvscheckouts/" and for all other files return null.
     *  
     * @param file a file
     * @return File the file itself or one of its ancestors or null if the supplied file is NOT managed by this versioning system
     */
    public File getTopmostManagedAncestor(File file) {
        return null;
    }
    
    /**
     * Retrieves a VCSAnnotator implementation if this versioning system provides one. 
     * 
     * @return a VCSAnnotator implementation or null
     */ 
    public VCSAnnotator getVCSAnnotator() {
        return null;
    }

    /**
     * Retrieves a VCSInterceptor implementation if this versioning system provides one. 
     * 
     * @return a VCSInterceptor implementation or null
     */ 
    public VCSInterceptor getVCSInterceptor() {
        return null;
    }

    /**
     * Provides the diff algorithm with the original content of a file.
     * For version control systems that support keyword expansion, the returned stream must expand all keywords so the
     * diff will not report any differences in keywords.
     * An implementing class must only return null if it does not provide original content for the file in which
     * case the system queries other providers. For CVS provider, for example, this means that it should return null if
     * the file is not managed by CVS and return a valid instance of OriginalContent otherwise. Later when diff asks for the original
     * content Reader and the content is not available, it can return null.
     * 
     * @param workingCopy a File in the working copy  
     * @return OriginalContent a wrapper for the original content of the working file or null if this file is not managed by this versioning system
     */ 
    public OriginalContent getVCSOriginalContent(File workingCopy) {
        return null;
    }

    /**
     * Adds a listener for change events.
     * 
     * @param listener a PropertyChangeListener 
     */ 
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a listener for change events.
     * 
     * @param listener a PropertyChangeListener 
     */ 
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Helper method to signal that annotations of a set of files changed. Do NOT fire this event when changes in
     * annotations are caused by changes of status. Status change event will refresh annotations automatically.
     *  
     * @param files set of files whose annotations changed or null if the change affects all files 
     */ 
    protected final void fireAnnotationsChanged(Set<File> files) {
        support.firePropertyChange(VersioningManager.EVENT_ANNOTATIONS_CHANGED, null, files);
    }
    
    /**
     * Helper method to signal that status of a set of files changed. Status change event will refresh annotations automatically.
     *  
     * @param files set of files whose status changed or null if all files changed status 
     */ 
    protected final void fireStatusChanged(Set<File> files) {
        support.firePropertyChange(VersioningManager.EVENT_STATUS_CHANGED, null, files);
    }

    /**
     * Helper method to signal that the versioning system started to manage some previously unversioned files 
     * (those files were imported into repository).
     */ 
    protected final void fireVersionedFilesChanged() {
        support.firePropertyChange(VersioningManager.EVENT_VERSIONED_ROOTS, null, null);
    }
    
    /**
     * Helper method that calls fireStatusChanged(Collections.singleton(file)). 
     *  
     * @param file a file whose status changed
     * @see #fireStatusChanged(java.util.Set)  
     */ 
    protected final void fireStatusChanged(File file) {
        fireStatusChanged(Collections.singleton(file));
    }
}
