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
     */
    public void closeProject(IProject proj) {
        UMLSupport.getUMLSupport().closeProject(proj);
    }

    /* (non-Javadoc)
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
