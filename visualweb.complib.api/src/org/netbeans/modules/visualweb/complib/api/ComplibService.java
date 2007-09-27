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

package org.netbeans.modules.visualweb.complib.api;

import java.io.File;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.nodes.Node;

/**
 * Complib service.
 * 
 * @author Edwin Goei
 */
public interface ComplibService {

    /**
     * Creates complib <code>PaletteFilter</code> for specified <code>Project</code>.
     * 
     * @param project
     * @return
     */
    public PaletteFilter createComplibPaletteFilter(Project project);

    /**
     * Creates PaletteActions to add custom items on the palette UI
     * 
     * @return
     */
    public PaletteActions createComplibPaletteActions();

    /**
     * Add a listener for Complib events
     * 
     * @param listener
     */
    public void addComplibListener(ComplibListener listener);

    /**
     * Remove a listener for Complib events
     * 
     * @param listener
     */
    public void removeComplibListener(ComplibListener listener);

    /**
     * Return a root node to represent the complibs for a project. For example in the project
     * navigator, this root node represents all the complibs in a project with each child node
     * corresponding to an embedded complib in the project.
     * 
     * @param project
     * @return
     */
    public Node getComplibsRootNode(Project project);

    /**
     * Install the complib file into the IDE so that it is available for use by projects. This is
     * also known as importing a complib file.
     * 
     * @param complibFile
     *            complib file, e.g. represents "acme-1.2.1.complib" on disk
     * @param overwrite
     *            If true, then any existing component library with the same namespace and version
     *            will be overwritten. If false, then the existing component library will not be
     *            replaced.
     * @throws ComplibException
     *             if there are problems with complib file or install failed
     */
    public void installComplibFile(File complibFile, boolean overwrite) throws ComplibException;

    /**
     * Initialize all complibs associated with a project
     * 
     * @param project
     */
    public void initProjectComplibs(Project project);

    /**
     * Clean up any resources that are used by the complibs associated with a project
     * 
     * @param project
     */
    public void cleanUpProjectComplibs(Project project);

    /**
     * Package private method designed to be called from BrokenLibraryRefFilter. Note: project
     * migration should have been a separate step under explicit user control but other parts of the
     * IDE were not done that way for some unknown reason.
     * 
     * @param project
     * @return list of legacy library reference names to be removed from project
     */
    public List<String> getLibRefNamesToRemove(Project project);
}
