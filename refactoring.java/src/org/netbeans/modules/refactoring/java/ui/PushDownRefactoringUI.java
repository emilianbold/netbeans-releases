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

import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PushDownRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/** Refactoring UI object for Push Down refactoring.
 *
 * @author Pavel Flaska, Jan Becicka
 */
public class PushDownRefactoringUI implements RefactoringUI {
    // reference to pull up refactoring this UI object corresponds to
    private final PushDownRefactoring refactoring;
    // initially selected members
    private final Set initialMembers;
    // UI panel for collecting parameters
    private PushDownPanel panel;
    
    private String description;
    
    /** Creates a new instance of PushDownRefactoringUI
     * @param selectedElements Elements the refactoring action was invoked on.
     */
    public PushDownRefactoringUI(TreePathHandle[] selectedElements, CompilationInfo info) {
        initialMembers = new HashSet();
        initialMembers.add(new MemberInfo(selectedElements[0].resolveElement(info),info));
        // compute source type and members that should be pre-selected from the
        // set of elements the action was invoked on
        
       // create an instance of push down refactoring object
        Element selected = selectedElements[0].resolveElement(info);
        if (!(selected instanceof TypeElement))
            selected = SourceUtils.getEnclosingTypeElement(selected);
        TreePath tp = SourceUtils.pathFor(info, selected);
        TreePathHandle sourceType = TreePathHandle.create(tp, info);
        description = UiUtils.getHeader(tp, info, UiUtils.PrintPart.NAME);
        refactoring = new PushDownRefactoring(Lookups.singleton(sourceType));
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(sourceType));
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new PushDownPanel(refactoring, initialMembers, parent);
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
        return NbBundle.getMessage(PushDownRefactoringUI.class, "DSC_PushDown", description); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(PushDownRefactoringUI.class, "LBL_PushDown"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PushDownRefactoringUI.class.getName());
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        refactoring.setMembers(panel.getMembers());
    }

}