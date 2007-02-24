/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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