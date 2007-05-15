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
package org.netbeans.modules.refactoring.java.ui;

import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.java.ui.InnerToOuterAction;
import org.netbeans.modules.refactoring.java.ui.InnerToOuterPanel;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Move Inner To Outer Level refactoring.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class InnerToOuterRefactoringUI implements RefactoringUI {
    // reference to pull up refactoring this UI object corresponds to
    private final InnerToOuterRefactoring refactoring;
    // UI panel for collecting parameters
    private InnerToOuterPanel panel;
    
    /** Creates a new instance of InnerToOuterRefactoringUI
     * @param selectedElements Elements the refactoring action was invoked on.
     */
    public InnerToOuterRefactoringUI(TreePathHandle sourceType, CompilationInfo info) {
        refactoring = new InnerToOuterRefactoring(sourceType);
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new InnerToOuterPanel(refactoring, parent);
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
        return "TODO";
        //TODO:
        //return NbBundle.getMessage(InnerToOuterAction.class, "DSC_InnerToOuter", sourceType.getSimpleName()); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(InnerToOuterAction.class, "LBL_InnerToOuter"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(InnerToOuterRefactoringUI.class.getName());
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        refactoring.setClassName(panel.getClassName());
        refactoring.setReferenceName(panel.getReferenceName());
    }
}