/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.VersioningManager;
import org.netbeans.spi.queries.CollocationQueryImplementation;

import java.io.File;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;

/**
 * Base class for a versioning system that integrates into IDE.
 *
 * A versioning system provides these services:
 * - annotations (coloring, actions)
 * - file system handler
 * - diff provider
 * 
 * Versioning system registration is done via {@link org.openide.util.lookup.ServiceProvider}.  
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
     * - local history module is not exclusive with other registered 'normal' versioning systems. This means that 
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
     * Get the original (unmodified) copy of a file. If the versioning system cannot provide it then this method should do nothing.
     * For version control systems that support keyword expansion, the original file must expand all keywords so the
     * diff sidebar support will not report any differences in keywords.
     * 
     * @param workingCopy a File in the working copy  
     * @param originalFile placeholder File for the original (unmodified) copy of the working file
     */ 
    public void getOriginalFile(File workingCopy, File originalFile) {
        // default implementation does nothing
    }

    /**
     * Retrieves a CollocationQueryImplementation if this versioning system provides one.
     * 
     * @return CollocationQueryImplementation a CollocationQueryImplementation instance or null if the system does not provide the service
     * @since 1.8
     */
    public CollocationQueryImplementation getCollocationQueryImplementation() {
        return null;
    }    

    /**
     * Retrieves a VCSVisibilityQuery implementation if this versioning system provides one.
     *
     * @return VCSVisibilityQuery a VCSVisibilityQuery instance or null if the system does not provide the service
     * @since 1.10
     */
    public VCSVisibilityQuery getVisibilityQuery() {
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
