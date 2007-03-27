/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * UseSuperTypeRefactoringUI.java
 *
 * Created on June 20, 2005
 *
 */

package org.netbeans.modules.refactoring.java.ui;

import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * UseSuperTypeRefactoringUI.java
 *
 * Created on June 20, 2005, 7:23 PM
 *
 * @author Bharath Ravi Kumar
 */
public class UseSuperTypeRefactoringUI implements RefactoringUI{
    
    private final TreePathHandle subType;
    private final UseSuperTypeRefactoring refactoring;
    private UseSuperTypePanel panel;
    private ElementHandle superType;
    /**
     * Creates a new instance of UseSuperTypeRefactoringUI
     * @param selectedElement The sub type being used
     */
    public UseSuperTypeRefactoringUI(TreePathHandle selectedElement) {
        this.subType = selectedElement;
        refactoring = new UseSuperTypeRefactoring(subType);
    }
    
    /**
     * Returns the name of the refactoring
     * @return 
     */
    public String getName() {
        return NbBundle.getMessage(UseSuperTypeRefactoringUI.class, "LBL_UseSuperType"); // NOI18N
    }
    
    /**
     * Returns the description of the refactoring
     * @return 
     */
    public String getDescription() {
        return NbBundle.getMessage(UseSuperTypeRefactoringUI.class, "DSC_UseSuperType", refactoring.getTypeElement()); // NOI18N
    }
    
    /**
     * return false
     * @return 
     */
    public boolean isQuery() {
        return false;
    }
    
    /**
     * Sets the target super type on the underlying refactoring
     * @return 
     */
    public Problem setParameters() {
        superType = panel.getSuperType();
        refactoring.setTargetSuperType(superType);
        return refactoring.checkParameters();
    }
    
    /**
     * Calls fastCheckParameters on the underlying refactoring
     * @return 
     */
    public Problem checkParameters() {
        superType = panel.getSuperType();
        refactoring.setTargetSuperType(superType);
        return refactoring.fastCheckParameters();
    }
    
    /**
     * Returns true
     * @return 
     */
    public boolean hasParameters() {
        return true;
    }
    
    /**
     * Returns the use super type refactoring
     * @return 
     */
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }
    
    /**
     * Returns the relevant Helpctx
     * @return 
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(UseSuperTypeRefactoringUI.class.getName());
    }
    
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if(panel == null)
            panel = new UseSuperTypePanel(refactoring);
        return panel;
    }
    
}
