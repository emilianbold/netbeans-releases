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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;

/**
 * Hook run when <code>nbproject/project.xml</code> is saved.
 * An instance should be placed into a project's lookup to register it.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public abstract class ProjectXmlSavedHook {
    
    /**
     * Default constructor for subclasses.
     */
    protected ProjectXmlSavedHook() {}
    
    /**
     * Called when shared project metadata (<code>project.xml</code>) has been modified.
     * <p>
     * Also called the first the time a project created by {@link ProjectGenerator}
     * is saved.
     * This is called during a project save event and so runs with write access.
     * </p>
     * <p class="nonnormative">
     * Typically the project's <code>build.xml</code> and/or <code>nbproject/build-impl.xml</code>
     * may need to be (re-)generated; see {@link GeneratedFilesHelper} for details.
     * </p>
     * @throws IOException if running the hook failed for some reason
     */
    protected abstract void projectXmlSaved() throws IOException;
    
}
