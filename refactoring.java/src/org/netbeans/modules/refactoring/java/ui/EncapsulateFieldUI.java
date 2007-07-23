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

import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 *
 * @author  Tomas Hurka
 * @author  Pavel Flaska
 * @author  Jan Pokorsky
 */
public final class EncapsulateFieldUI implements RefactoringUI {

    private EncapsulateFieldPanel panel;
    private transient EncapsulateFieldsRefactoring refactoring;
    
    /** Creates new form RenamePanelName */
    public EncapsulateFieldUI(TreePathHandle selectedObject, CompilationInfo info) {

        refactoring = new EncapsulateFieldsRefactoring(resolveSourceType(selectedObject, info));
    }
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new EncapsulateFieldPanel(refactoring.getSelectedObject(), parent);
        }
        return panel;
    }

    private Problem setParameters(boolean checkOnly) {
        refactoring.setRefactorFields(panel.getAllFields());
        refactoring.setMethodModifiers(panel.getMethodModifiers());
        refactoring.setFieldModifiers(panel.getFieldModifiers());
        refactoring.setAlwaysUseAccessors(panel.isCheckAccess());
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        String name = panel.getClassname();
//        name = "<anonymous>"; // NOI18N
        return new MessageFormat(NbBundle.getMessage(EncapsulateFieldUI.class, "DSC_EncapsulateFields")).format (
                    new Object[] {name}
                );
    }

    public String getName() {
        return NbBundle.getMessage(EncapsulateFieldUI.class, "LBL_EncapsulateFields");
    }
    
    public Problem checkParameters() {
        return setParameters(true);
    }
    
    public Problem setParameters() {
        return setParameters(false);
    }

    public boolean hasParameters() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(EncapsulateFieldUI.class);
    }
    
    /**
     * returns field in case the selectedObject is field or enclosing class
     * in other cases.
     */
    private static TreePathHandle resolveSourceType(TreePathHandle selectedObject, CompilationInfo javac) {
        TreePath selectedField = selectedObject.resolve(javac);
        Element elm = javac.getTrees().getElement(selectedField);
        TypeElement encloser = null;
        if (elm != null && ElementKind.FIELD == elm.getKind()) {
            encloser = (TypeElement) elm.getEnclosingElement();
            if (ElementKind.INTERFACE != encloser.getKind() && NestingKind.ANONYMOUS != encloser.getNestingKind()) {
                // interface constants, local variables and annonymous declarations are unsupported
                TreePath tp = javac.getTrees().getPath(elm);
                return TreePathHandle.create(tp, javac);
            }
        }
        
        // neither interface, annotation type nor annonymous declaration
        TreePath tpencloser = RetoucheUtils.findEnclosingClass(javac, selectedField, true, false, true, false, false);
        return TreePathHandle.create(tpencloser, javac);
    }
}
