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

/*
 * File         : IIDEManager.java
 * Version      : 1.0
 * Description  : Interface to manage IDE housekeeping
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;

import org.netbeans.modules.uml.integration.ide.dialogs.IProgressIndicator;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

/**
 *  Interface that an IDE integration must implement so that Describe can ask
 * the IDE to do various housekeeping tasks.
 *
 * @author  Darshan
 * @version 1.0
 */
public interface IIDEManager {
    public boolean  isPropertyEditorVisible();
    public void     setPropertyEditorVisible(boolean vis);
    public void     saveCurrentProject();
    public boolean  isProjectDirty();
    public void     setProjectDirty(boolean dirty);
    public String   getProjectToInsert();

    /**
     *  Return the top-level Frame of the IDE. If the IDE is not AWT-based,
     * null is an acceptable return value. This is used by
     * getProgressIndicator() to make the progress indicator modal to the IDE's
     * top-level window.
     *
     * @return The top-level <code>Frame</code> or <code>null</code> if there
     *         is no appropriate <code>Frame</code>.
     */
    public Frame    getTopFrame();

    /**
     *  Constructs and returns an IProgressIndicator instance, modal to the
     * IDE's top-level Frame.
     *
     * @return An <code>IProgressIndicator</code> appropriate to the IDE
     *         concerned.
     */
    public IProgressIndicator getProgressIndicator();

    /**
     *  Returns the diagram kind of the given diagram.
     * @param diagram An IDiagram, which may be null.
     * @return The diagram kind, one of the constants in DiagramKind.
     */
    public int getDiagramKind(IDiagram diagram);

    /**
     *  Synchronizes open editors on the given files with the file system.
     */
    public void synchronizeFiles(ArrayList files);

    /**
     *  Revive all cached Describe objects.
     */
    public void reviveDescribeObjects();

    public void invokeLater(Runnable r);

    /**
     * Invitation to the IDE integration to rename the project named 'oldName'
     * to 'newName'.
     *
     * @param oldName The old name of the Describe project.
     * @param newName The new name of the Describe project.
     */
    public void renameProject(String oldName, String newName);

    /**
     * Opens the IDE project specified by 'project' associated with the given
     * Describe project.
     * @param describeProject The Describe IProject associated with the IDE
     *                        project to be opened.
     * @param project         A String that uniquely identifies the IDE project
     *                        to be opened.
     */
    public void openProject(IProject describeProject, String project);

    /**
     * Closes the given Describe project and the associated IDE project, if any.
     * While it's strongly recommended that the given Describe project be
     * closed, nothing evil will happen (apart from confusing the user) if this
     * isn't done. It's left to the IDE to discover what IDE project is
     * associated with the given Describe project, if any.
     *
     * @param proj The Describe project to close.
     */
    public void closeProject(IProject proj);


    /**
     * Handles the removal of a Describe project. When this happens we need to locate
     * the associated IDE project and make it 'virginal' again.
     *
     * @param proj The Describe project that is going to be removed.
     */
    public void handleRemoveDescribeProject(IProject proj);


    /**
     * Activates the IDE project associated with the given Describe project.
     * Note that we assume here that the IDE can work out what the associated
     * IDE project is, unlike openProject().
     *
     * @param describeProject The Describe project that is the subject of
     *                        roundtrip activity.
     * @return <code>false</code> to veto the roundtrip change notification.
     */
    public boolean activateIDEProject(IProject describeProject);

    /**
     * Returns the directory that will contain the default workspace - usually
     * the parent directory under which directories will be created for each
     * IDE project.
     *
     * @return The absolute path of the default workspace directory.
     */
    public String getDefaultWorkspaceDirectory();

    /**
     * Deletes the given file from the filesystem, also closing any open IDE
     * editors that are editing the file, <em>without affecting any model
     * elements defined in the file</code>.
     *
     * @param file The File to delete, guaranteed to be non-null and to exist.
     */
    public void deleteFile(File file);

    /**
     * Confirms the delete of a given file from the filesystem.
     *
     * @param file The File to delete, guaranteed to be non-null and to exist.
     */
    public void confirmDeleteSourceFile(File file);

    /**
     * Returns the type of IDE.
     *
     */
    public int getIDEType();
}
