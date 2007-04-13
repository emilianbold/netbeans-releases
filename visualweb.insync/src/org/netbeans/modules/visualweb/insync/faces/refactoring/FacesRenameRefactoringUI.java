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
package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * 
 */
final class FacesRenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private RenameRefactoring refactoring;
    private String originalName;
    private String oldName;
    private String dispOldName;
    private String newName;
    private FacesRenamePanel panel;
    private boolean newNameSpecified;
    private FileObject byPassFolder;
    private boolean byPassPakageRename;

    FacesRenameRefactoringUI(FileObject jspFileObject, String newName) {
        refactoring = new RenameRefactoring(Lookups.singleton(jspFileObject));
        originalName = jspFileObject.getName();
        if (newName == null) {
            dispOldName = oldName = jspFileObject.getName();
        } else {
            dispOldName = oldName = newName;
            newNameSpecified = true;
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(FacesRenamePanel.class, "LBL_Rename");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(FacesRenamePanel.class, "DSC_RenamePage", dispOldName, newName);
    }

    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new FacesRenamePanel(
                            parent,
                            oldName,
                            NbBundle.getMessage(FacesRenamePanel.class, "LBL_RenamePageNamed", originalName),
                            !newNameSpecified);
        }
        return panel;
    }
    
    public boolean hasParameters() {
        return true;
    }

    public Problem setParameters() {
        newName = panel.getNameValue();
        refactoring.setNewName(newName);
        refactoring.setSearchInComments(panel.searchJavadoc());            
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        newName = panel.getNameValue();
        refactoring.setNewName(newName);
        return refactoring.fastCheckParameters();
    }
    
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    
    public void doRefactoringBypass() throws IOException {
        DataObject dob = null;
        if (byPassFolder != null) {
            dob = DataFolder.findFolder(byPassFolder);
        } else {
            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
        }
        dob.rename(panel.getNameValue());
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
