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
 * File         : IDEManagerAdapter.java
 * Version      : 1.0
 * Description  : Stub implementation of the IDE manager interface, suitable
 *                for subclassing by IDE integrations.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.netbeans.modules.uml.integration.ide.dialogs.IProgressIndicator;
import org.netbeans.modules.uml.integration.ide.dialogs.ProgressIndicator;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

/**
 *  Stub implementation of the IDE manager interface, suitable for subclassing
 * by IDE integrations. If the integration doesn't subclass this, an instance
 * of this will be used as the IDE manager.
 *
 * @author  Darshan
 * @version 1.0
 */
public class IDEManagerAdapter implements IIDEManager {
    public boolean isPropertyEditorVisible() {
        return true;
    }

    public void setPropertyEditorVisible(boolean vis) {
    }

    public void saveCurrentProject() {
    }

    public boolean isProjectDirty() {
        return false;
    }

    public void setProjectDirty(boolean dirty) {
    }

    public String getProjectToInsert() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(UMLSupport.getString("Dialog.InsertProject.Title"));

        fc.setFileFilter(new FileFilter() {
            public String getDescription() {
                return UMLSupport.getString(
                        "Dialog.InsertProject.FileFilter.Description");
            }
            public boolean accept(File f) {
                return f.toString().endsWith(UMLSupport.getString(
                                "Dialog.InsertProject.ProjectExtension"))
                                || f.isDirectory();
            }
        });
        int retVal = fc.showOpenDialog( null );
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            return selectedFile.toString();
        }
        return null;
    }

    /**
     *  Returns the top window Frame of the IDE. AWT-based IDEs should override
     * this method to return the main IDE window; other IDEs can return
     * <code>null</code> and override getProgressIndicator() instead.
     *
     * @see IIDEManager#getTopFrame()
     * @return <code>null</code> always.
     */
    public Frame getTopFrame() {
        return null;
    }

    /**
     *  Returns an <code>IProgressIndicator</code> instance
     *  (<code>JDialog</code> implementation), modal to the <code>Frame</code>
     *  returned by <code>getTopFrame()</code> if non-null, or a free-floating
     *  modal dialog if <code>getTopFrame()</code> returns <code>null</code>.
     *
     * @return An <code>IProgressIndicator</code>
     */
    public IProgressIndicator getProgressIndicator() {
        Frame topFrame = getTopFrame();
        ProgressIndicator pi = null;
        if (topFrame != null)
            pi = new ProgressIndicator(topFrame,
                UMLSupport.getString("Dialog.RoundtripProgress.Title"));
        else
            pi = new ProgressIndicator(
                UMLSupport.getString("Dialog.RoundtripProgress.Title"));
        pi.setModal(true);
        return pi;
    }

    /**
     *  Returns the diagram kind of the given diagram.
     * @param diagram An IDiagram, which may be null.
     * @return The diagram kind, one of the constants in DiagramKind.
     */
    public int getDiagramKind(IDiagram diagram) {
        if (diagram == null) return DiagramKind.DK_DIAGRAM;
        return diagram.getDiagramKind();
    }

    public void synchronizeFiles(ArrayList files) {
    }

    public void reviveDescribeObjects() {
    }

    public void invokeLater(Runnable r) {
        SwingUtilities.invokeLater(r);
    }

    public void renameProject(String oldName, String newName) {
        // Do nothing.
    }

    public void openProject(IProject describeProject, String project) {
    }

    /* (non-Javadoc)
     * @see com.embarcadero.integration.IIDEManager#closeProject(com.embarcadero.describe.structure.IProject)
     */
    public void closeProject(IProject proj) {
        UMLSupport.getUMLSupport().closeProject(proj);
    }

    /* (non-Javadoc)
     * @see com.embarcadero.integration.IIDEManager#activateIDEProject(com.embarcadero.describe.structure.IProject)
     */
    public boolean activateIDEProject(IProject describeProject) {
        return true;
    }

    public void loadPreferences() {
        Preferences.readPreferences();
    }

    public String getDefaultWorkspaceDirectory() {
        return null;
    }

    public void handleRemoveDescribeProject(IProject proj) {
    }

    /* (non-Javadoc)
     * @see com.embarcadero.integration.IIDEManager#deleteFile(java.io.File)
     */
    public void deleteFile(File file) {
        // IDEs will certainly want to override this default behavior.
        file.delete();
    }

    public void confirmDeleteSourceFile(File file) {
        // IDEs will certainly want to override this default behavior.
    }

    public int getIDEType() {
        return UMLSupport.SU_IDE_NONE;
    }

}