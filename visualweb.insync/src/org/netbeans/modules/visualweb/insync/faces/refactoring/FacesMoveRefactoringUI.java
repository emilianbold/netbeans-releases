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
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author sc32560
 */
final class FacesMoveRefactoringUI implements RefactoringUI, RefactoringUIBypass {    
    private MoveRefactoring refactoring;
    private FileObject pageFileObject;    
    private FacesMovePanel panel;
    private boolean disable;
    private FileObject targetFolder;
    private PasteType pasteType;
    
    /**
     * 
     * @param pageFileObject 
     * @param targetFolder 
     * @param pasteType 
     */
    FacesMoveRefactoringUI (FileObject pageFileObject, FileObject targetFolder, PasteType pasteType) {
        refactoring = new MoveRefactoring(Lookups.singleton(pageFileObject));
        this.disable = targetFolder != null ;
        this.targetFolder = targetFolder;
        this.pageFileObject = pageFileObject;
        this.pasteType = pasteType;
    }
    
    public String getName() {
        return NbBundle.getMessage(FacesMoveRefactoringUI.class, "LBL_Move"); // NOI18N
    }
     
    public String getDescription() {
        return NbBundle.getMessage(FacesMoveRefactoringUI.class, "DSC_MovePage", pageFileObject.getName(), panel.getTargeFolder().getPath()); // NOI18N
    }
    
    public boolean isQuery() {
        return false;
    }
        
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new FacesMovePanel(
                            parent,
                            NbBundle.getMessage(FacesMoveRefactoringUI.class, "LBL_MovePageNamed", pageFileObject.getName()), // NOI18N
                            targetFolder != null ? targetFolder : (pageFileObject != null ? pageFileObject: null));
            panel.setCombosEnabled(!disable);
        }
        return panel;
    }
    
    public boolean hasParameters() {
        return true;
    }
    
    public Problem checkParameters() {
        return setParameters(true);
    }
    
    public Problem setParameters() {
        return setParameters(false);
    }
    
    private Problem setParameters(boolean checkOnly) {
        if (panel==null)
            return null;

        URL url = URLMapper.findURL(panel.getTargeFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm())));
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(FacesMoveRefactoringUI.class);
    }
    
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }   
    
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    
    public void doRefactoringBypass() throws IOException {
        pasteType.paste();
    }
}
