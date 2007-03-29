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
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Flaska
 */
public class ChangeParametersUI implements RefactoringUI {
    
    TreePathHandle refactoredObj;
    ChangeParametersPanel panel;
    ChangeParametersRefactoring refactoring;
    
    /** Creates a new instance of ChangeMethodSignatureRefactoring */
    public ChangeParametersUI(TreePathHandle refactoredObj) {
        this.refactoring = new ChangeParametersRefactoring(refactoredObj);
        this.refactoredObj = refactoredObj;
    }
    
    public String getDescription() {
        return "TODO";
//TODO:        
//        String msg = NbBundle.getMessage(ChangeParametersUI.class, 
//                                        "DSC_ChangeParsRootNode"); // NOI18N
//        CallableFeature callable = ((CallableFeature) refactoredObj);
//        boolean isMethod = callable instanceof Method;
//        String name = isMethod ? callable.getName() : ((JavaClass) callable.getDeclaringClass()).getSimpleName();
//        return new MessageFormat(msg).format(new Object[] { 
//            name,
//            NbBundle.getMessage(ChangeParametersUI.class, "DSC_ChangeParsRootNode" + (isMethod ? "Method" : "Constr")),
//            panel.genDeclarationString()
//       });
    }
    
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            //TODO:
            //parent.setPreviewEnabled(true);
            panel = new ChangeParametersPanel(refactoredObj, parent);
        }
        return panel;
    }
    
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public boolean isQuery() {
        return false;
    }
    
    private Problem setParameters(boolean checkOnly) {
        return null;
        //TODO:
//        List data = (List) panel.getTableModel().getDataVector();
//        ChangeParametersRefactoring.ParameterInfo[] paramList = new ChangeParametersRefactoring.ParameterInfo[data.size()];
//        JavaModelPackage pckg = (JavaModelPackage) refactoredObj.refImmediatePackage();
//        int counter = 0;
//        Problem problem = null;
//        for (Iterator rowIt = data.iterator(); rowIt.hasNext(); ++counter) {
//            List row = (List) rowIt.next();
//            int origIndex = ((Integer) row.get(3)).intValue();
//            String name = null;
//            String defaultVal = null;
//            Type type = null;
//            if (origIndex == -1) {
//                name = (String) row.get(0);
//                type = pckg.getType().resolve((String) row.get(1));
//                defaultVal = (String) row.get(2);
//            }
//            paramList[counter] = new ChangeParametersRefactoring.ParameterInfo(origIndex, name, type, defaultVal);
//        }
//        int modifier = panel.getModifier();
//        refactoring.setParameterInfo(paramList);
//        refactoring.setModifiers(modifier);
//        if (checkOnly) {
//            problem = refactoring.fastCheckParameters();
//        } else {
//            problem = refactoring.checkParameters();
//        }
//        return problem;
    }
    
    public String getName() {
        return NbBundle.getMessage(ChangeParametersUI.class, "LBL_ChangeMethodSignature");
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
        return new HelpCtx(ChangeParametersUI.class);
    }
}
