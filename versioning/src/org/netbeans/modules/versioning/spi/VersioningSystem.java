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
package org.netbeans.modules.versioning.spi;

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
 * @author Maros Sandor
 */
public abstract class VersioningSystem {

    /**
     * Indicates to the Versioning manager that the layout of versioned files may have changed. Previously unversioned 
     * files became versioned, versioned files became unversioned or the versioning system for some files changed.
     * The manager will flush any caches that may be holding such information.  
     * A versioning system usually needs to fire this after an Import action. 
     */
    public static final String PROP_VERSIONED_ROOTS = "null VCS.VersionedFilesChanged";

    /**
     * The NEW value is a Set of Files whose versioning status changed. This event is used to re-annotate files, re-fetch
     * original content of files and generally refresh all components that are connected to these files.
     */
    public static final String PROP_STATUS_CHANGED = "Set<File> VCS.StatusChanged";

    /**
     * Used to signal the Versioning manager that some annotations changed. Note that this event is NOT required in case
     * the status of the file changes in which case annotations are updated automatically. Use this event to force annotations
     * refresh in special cases, for example when the format of annotations changes.
     * Use null as new value to force refresh of all annotations.
     */
    public static final String PROP_ANNOTATIONS_CHANGED = "Set<File> VCS.AnnotationsChanged";
    
    protected final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Short name of the versioning system, it will be used as popup menu label, label in tooltips, etc.
     * Examples: CVS, Subversion, Mercurial, Teamware, SourceSafe, VSS, Clearcase, Local History
     * 
     * @return String short display name of the versioning system.
     */
    public abstract String getDisplayName();
    
    /**
     * Tests whether the file is managed by this versioning system. If it is, the method should return the topmost 
     * parent of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its parents or null if the supplied file is NOT managed by this versioning system
     */
    public File getTopmostManagedParent(File file) {
        return null;
    }
    
    public VCSAnnotator getVCSAnnotator() {
        return null;
    }

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

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    protected final void fireAnnotationsChanged(Set<File> files) {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, files);
    }
    
    protected final void fireStatusChanged(Set<File> files) {
        support.firePropertyChange(PROP_STATUS_CHANGED, null, files);
    }

    protected final void fireVersionedFilesChanged() {
        support.firePropertyChange(PROP_VERSIONED_ROOTS, null, null);
    }
    
    protected final void fireStatusChanged(File file) {
        fireStatusChanged(new HashSet<File>(Arrays.asList(new File[] { file })));
    }
}
