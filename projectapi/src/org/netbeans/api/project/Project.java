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

import java.awt.Image;
import java.beans.PropertyChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Represents one IDE project in memory.
 * <p>
 * <strong>Never cast a project instance</strong> to any subtype. The project
 * manager is free to wrap any project in an unspecified proxy for its own
 * purposes. For extensibility, use {@link #getLookup}.
 * </p>
 * <div class="nonnormative">
 * <p>Note that this API is primarily of interest to project type provider
 * modules, and to infrastructure and generic GUI. Most other modules providing
 * tools or services should <em>not</em> need to explicitly model projects, and
 * should not be using this API much or at all.</p>
 * </div>
 * @author Jesse Glick
 */
public interface Project extends Lookup.Provider {
    
    /** Property name fired when the code name changes. */
    String PROP_NAME = "name"; // NOI18N
    
    /** Property name fired when the display name changes. */
    String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    
    /**
     * Gets an associated directory where the project metadata and possibly sources live.
     * In the case of a typical Ant project, this is the top directory, not the
     * project metadata subdirectory.
     * @return a directory
     */
    FileObject getProjectDirectory();
    
    /**
     * Get any optional abilities of this project.
     * <div class="nonnormative">
     * <p>If you are <em>providing</em> a project, there are a number of interfaces
     * which you should consider implementing and including in lookup, some of which
     * are described below. If you are <em>using</em> a project from another module,
     * there are some cases where you will want to ask a project for a particular
     * object in lookup (e.g. <code>ExtensibleMetadataProvider</code>) but in most
     * cases you should not; in the case of queries, always call the static query
     * API helper method, rather than looking for the query implementation objects
     * yourself.</p>
     * <p>The following abilities are recommended:</p>
     * <ol>
     * <li><a href="@PROJECTS/PROJECTUIAPI@/org/netbeans/spi/project/ui/LogicalViewProvider.html"><code>LogicalViewProvider</code></a></li>
     * <li><a href="@PROJECTS/PROJECTUIAPI@/org/netbeans/spi/project/ui/CustomizerProvider.html"><code>CustomizerProvider</code></a></li>
     * <li>{@link org.netbeans.spi.project.ActionProvider}</li>
     * <li>{@link org.netbeans.spi.project.SubprojectProvider}</li>
     * <li>{@link org.netbeans.spi.project.AuxiliaryConfiguration}</li>
     * <li>{@link org.netbeans.spi.project.CacheDirectoryProvider}</li>
     * </ol>
     * <p>You might also have e.g.:</p>
     * <ol>
     * <li>{@link org.netbeans.spi.queries.FileBuiltQueryImplementation}</li>
     * <li><a href="@PROJECTS/PROJECTUIAPI@/org/netbeans/spi/project/ui/ProjectOpenedHook.html"><code>ProjectOpenedHook</code></a></li>
     * <li><a href="@JAVA/API@/org/netbeans/spi/java/classpath/ClassPathProvider.html"><code>ClassPathProvider</code></a></li>
     * <li><a href="@JAVA/API@/org/netbeans/spi/java/queries/SourceForBinaryQueryImplementation.html"><code>SourceForBinaryQueryImplementation</code></a></li>
     * <li><a href="@JAVA/API@/org/netbeans/spi/java/queries/JavadocForBinaryQueryImplementation.html"><code>JavadocForBinaryQueryImplementation</code></a></li>
     * <li><a href="@JAVA/API@/org/netbeans/spi/java/queries/AccessibilityQueryImplementation.html"><code>AccessibilityQueryImplementation</code></a></li>
     * <li><a href="@ANT/PROJECT@/org/netbeans/spi/project/support/ant/ProjectXmlSavedHook.html"><code>ProjectXmlSavedHook</code></a></li>
     * <li><a href="@ANT/PROJECT@/org/netbeans/spi/project/ant/AntArtifactProvider.html"><code>AntArtifactProvider</code></a></li>
     * <li><a href="@JAVA/API@/org/netbeans/spi/java/queries/UnitTestForSourceQueryImplementation.html"><code>UnitTestForSourceQueryImplementation</code></a></li>
     * </ol>
     * <p>Typical implementation:</p>
     * <pre>
     * private final Lookup lookup = Lookups.fixed(new Object[] {
     *     new MyAbility1(this),
     *     // ...
     * });
     * public Lookup getLookup() {
     *     return lookup;
     * }
     * </pre>
     * </div>
     * @return a set of abilities
     */
    Lookup getLookup();
    
    /**
     * Get a programmatic code name suitable for use in build scripts or other
     * references.
     * <p>
     * Project names should typically be distinctive enough to distinguish
     * between different projects with some kind of relationships, <em>but</em>
     * any usage of this name must take into account that they are not forced
     * to be unique.
     * <p>
     * Should not contain odd characters; should be usable as a directory name
     * on disk, as (part of) an Ant property name, etc.
     * XXX precise format - at least conforms to XML NMTOKEN or ID
     * @return a code name
     */
    String getName();
    
    /**
     * Get a human-readable display name for the project.
     * May contain spaces, international characters, etc.
     * XXX precise format - probably XML PCDATA
     * @return a display name for the project
     */
    String getDisplayName();
    
    /** 
     * Gets icon for given project. Usually determined by the 
     * project type
     * @return icon of the project.
     */
    Image getIcon();
    
    /**
     * Add a listener to property changes.
     * Only {@link #PROP_NAME} and {@link #PROP_DISPLAY_NAME} may be fired.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove a listener to property changes.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
}
