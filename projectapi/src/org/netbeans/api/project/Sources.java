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

package org.netbeans.api.project;

import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;

/**
 * Optional interface for a project to enumerate folders containing sources
 * of various kinds.
 * If not present in the project's lookup, a sensible default is
 * {@link org.netbeans.spi.project.support.GenericSources#genericOnly}.
 * <p class="nonnormative">
 * May be used by new-from-template wizards, find-in-files, etc.
 * </p>
 * @see org.netbeans.api.project.Project#getLookup
 * @see <a href="@ANT/PROJECT@/org/netbeans/spi/project/support/ant/SourcesHelper.html"><code>SourcesHelper</code></a>
 * @see org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA
 * @author Jesse Glick
 */
public interface Sources {
    
    /**
     * Generic source folders containing any source files at all.
     * Generally should be a superset of all other kinds of source folders.
     * Usually the project directory is the only such folder listed.
     */
    String TYPE_GENERIC = "generic"; // NOI18N
    
    /**
     * Find all root source folders matching a given type.
     * @param type a kind of folder, e.g. {@link #TYPE_GENERIC}
     * @return a list of top-level source folders of that kind (may be empty but not null)
     */
    SourceGroup[] getSourceGroups(String type);
    
    /**
     * Add a listener to changes in the source groups.
     * Any change in the result of {@link #getSourceGroups} should
     * cause a change event to be fired.
     * @param listener a listener to add
     */
    public void addChangeListener(ChangeListener listener);
    
    /**
     * Remove a listener to changes in the source groups.
     * @param listener a listener to remove
     */
    public void removeChangeListener(ChangeListener listener);
    
}
