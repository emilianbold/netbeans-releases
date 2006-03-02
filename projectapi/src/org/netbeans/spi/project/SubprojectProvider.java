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

package org.netbeans.spi.project;

import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

/**
 * Optional ability of projects which may have a list of "subprojects".
 * The exact interpretation of this term is at the discretion of the project,
 * but typically subprojects would be "built" as part of this project or somehow
 * used in it as dependencies; or they may just be contained or agglomerated in
 * it somehow.
 * @see Project#getLookup
 * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/ReferenceHelper.html#createSubprojectProvider()"><code>ReferenceHelper.createSubprojectProvider</code></a>
 * @author Jesse Glick
 */
public interface SubprojectProvider {
    
    /**
     * Get a set of projects which this project can be considered to depend upon somehow.
     * This information is likely to be used only for UI purposes.
     * Only direct subprojects need be listed, not all recursive subprojects.
     * There may be no direct or indirect cycles in the project dependency graph
     * but it may be a DAG, i.e. two projects may both depend on the same subproject.
     * @return an immutable and unchanging set of {@link Project}s
     * @see org.netbeans.api.project.ProjectUtils#hasSubprojectCycles
     */
    Set/*<Project>*/ getSubprojects();
    
    /**
     * Add a listener to changes in the set of subprojects.
     * @param listener a listener to add
     */
    void addChangeListener(ChangeListener listener);
    
    /**
     * Remove a listener to changes in the set of subprojects.
     * @param listener a listener to remove
     */
    void removeChangeListener(ChangeListener listener);
    
}
