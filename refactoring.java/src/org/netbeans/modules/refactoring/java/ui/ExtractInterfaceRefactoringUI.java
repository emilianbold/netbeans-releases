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

import java.util.Iterator;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.ui.ExtractInterfaceAction;
import org.netbeans.modules.refactoring.java.ui.ExtractInterfacePanel;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Extract Interface refactoring.
 *
 * @author Martin Matula, Jan Becicka
 */
public class ExtractInterfaceRefactoringUI implements RefactoringUI {
    // reference to extract interface refactoring this UI object corresponds to
    private final ExtractInterfaceRefactoring refactoring;
    // source type
    private final TreePathHandle sourceType;
    // UI panel for collecting parameters
    private ExtractInterfacePanel panel;
    
    /** Creates a new instance of ExtractInterfaceRefactoringUI
     * @param selectedElements Elements the refactoring action was invoked on.
     */
    public ExtractInterfaceRefactoringUI(TreePathHandle selectedElement, CompilationInfo info) {
        // compute source type
        //sourceType = getSourceType(selectedElement);
        sourceType = selectedElement;
        // create an instance of pull up refactoring object
        refactoring = new ExtractInterfaceRefactoring(sourceType);
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new ExtractInterfacePanel(refactoring, parent);
        }
        return panel;
    }

    public Problem setParameters() {
        //TODO:
        //captureParameters();
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        //TODO:
        //captureParameters();
        return refactoring.fastCheckParameters();
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return NbBundle.getMessage(ExtractInterfaceAction.class, "DSC_ExtractInterface", "TODO: getName()"/* sourceType.getName()*/); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(ExtractInterfaceAction.class, "LBL_ExtractInterface"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ExtractInterfaceRefactoringUI.class.getName());
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
//    /** Gets parameters from the refactoring panel and sets them
//     * to the refactoring object.
//     */
//    private void captureParameters() {
//        refactoring.setIfcName(panel.getIfcName());
//        refactoring.setMembers(panel.getMembers());
//    }
//    
//    static TreePathHandle getSourceType(TreePathHandle element) {
//        JavaClass result = null;
//        // iterate through the containers of the element (until we get to null
//        // or a resource)
//        while (element != null && !(element instanceof Resource)) {
//            if (element instanceof JavaClass) {
//                result = (JavaClass) element;
//                break;
//            }
//            element = (Element) element.refImmediateComposite();
//        }
//        if (result == null && element instanceof Resource) {
//            String name = ((Resource) element).getName();
//            int start = name.lastIndexOf('/') + 1;
//            int end = name.indexOf('.', start);
//            if (end < 0) end = name.length();
//            name = name.substring(start, end);
//            for (Iterator it = ((Resource) element).getClassifiers().iterator(); it.hasNext();) {
//                JavaClass cls = (JavaClass) it.next();
//                result = cls;
//                // if the class of a same name is found, exit the loop
//                if (name.equals(cls.getSimpleName())) break;
//            }
//            // if no class of the same name is found, then the last class in
//            // the resource is taken as the selected one
//        }
//        return result;
//    }
}