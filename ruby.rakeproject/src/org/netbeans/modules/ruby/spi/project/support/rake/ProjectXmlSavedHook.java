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

package org.netbeans.modules.ruby.spi.project.support.rake;

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
