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
 * Use {@link ProjectUtils#getSources} as a client.
 * Use {@link Project#getLookup} as a provider.
 * <p class="nonnormative">
 * May be used by the New File wizard, Find in Files, to-do task scanning,
 * the Files tab, etc.
 * </p>
 * @see <a href="@ANT/PROJECT@/org/netbeans/spi/project/support/ant/SourcesHelper.html"><code>SourcesHelper</code></a>
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
     * @param type a kind of folder, e.g. {@link #TYPE_GENERIC} or
     *             {@link org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA}
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
