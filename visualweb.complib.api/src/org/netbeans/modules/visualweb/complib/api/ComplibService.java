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
