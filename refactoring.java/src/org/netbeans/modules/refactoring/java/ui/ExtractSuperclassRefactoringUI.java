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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Extract Super Class refactoring.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public class ExtractSuperclassRefactoringUI implements RefactoringUI {
    // reference to refactoring this UI object corresponds to
    private final ExtractSuperclassRefactoring refactoring;
    // source type
    private final TreePathHandle sourceType;
    // UI panel for collecting parameters
    private ExtractSuperclassPanel panel;
    private final String name;
    
    public ExtractSuperclassRefactoringUI(TreePathHandle selectedHandle, CompilationInfo info) {
        TreePath path = selectedHandle.resolve(info);
        Tree selectedTree = path.getLeaf();
        while (selectedTree.getKind() != Tree.Kind.CLASS) {
            path = path.getParentPath();
            if (path == null) {
                selectedTree = info.getCompilationUnit().getTypeDecls().get(0);
                path = info.getTrees().getPath(info.getCompilationUnit(), selectedTree);
                break;
            }
            selectedTree = path.getLeaf();
        }
        
        this.name = UiUtils.getHeader(path, info, UiUtils.PrintPart.NAME);
        this.sourceType = TreePathHandle.create(path, info);
        this.refactoring = new ExtractSuperclassRefactoring(sourceType);
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new ExtractSuperclassPanel(refactoring, parent);
        }
        return panel;
    }

    public Problem setParameters() {
        captureParameters();
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        captureParameters();
        return refactoring.fastCheckParameters();
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return NbBundle.getMessage(ExtractSuperclassAction.class, "DSC_ExtractSC", name); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(ExtractSuperclassAction.class, "LBL_ExtractSC"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ExtractSuperclassRefactoringUI.class.getName());
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        refactoring.setSuperClassName(panel.getSuperClassName());
        refactoring.setMembers(panel.getMembers());
    }
}