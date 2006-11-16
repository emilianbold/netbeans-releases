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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A CustomRefactoringUI subclass that represents Safe Delete
 * @author Bharath Ravikumar
 */
public class SafeDeleteUI implements RefactoringUI{
    
    private final Object[] elementsToDelete;
    
    private final SafeDeleteRefactoring refactoring;
    
    private SafeDeletePanel panel;
    
    private ResourceBundle bundle;
    
    /**
     * Creates a new instance of SafeDeleteUI
     * @param selectedElements An array of selected Elements that need to be 
     * safely deleted
     */
    public SafeDeleteUI(FileObject[] selectedElements) {
        this.elementsToDelete = selectedElements;
        refactoring = new SafeDeleteRefactoring(elementsToDelete);
    }

    /**
     * Creates a new instance of SafeDeleteUI
     * @param selectedElements An array of selected Elements that need to be 
     * safely deleted
     */
    public SafeDeleteUI(TreePathHandle[] selectedElements, CompilationInfo info) {
        this.elementsToDelete = selectedElements;
        refactoring = new SafeDeleteRefactoring(elementsToDelete);
        refactoring.getContext().add(info.getClasspathInfo());
    }
    
    /**
     * Delegates to the fastCheckParameters of the underlying
     * refactoring
     * @return Returns the result of fastCheckParameters of the
     * underlying refactoring
     */
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        refactoring.setCheckInComments(panel.isSearchInComments());
        return refactoring.fastCheckParameters();
    }
    
    public String getDescription() {
        //TODO: Check bounds here. Might throw an OutofBoundsException otherwise.
//        if (elementsToDelete[0] instanceof JavaClass) {
//            return getString("DSC_SafeDelClasses", elementsToDelete);// NOI18N
//        } else {
//            if (elementsToDelete[0] instanceof ExecutableElement) {
//                if (elementsToDelete.length > 1) 
//                    return getString("DSC_SafeDelMethods");// NOI18N
//                else 
//                    return getString("DSC_SafeDelMethod", elementsToDelete[0]);// NOI18N
//            }
//            
//        }
//        if(elementsToDelete[0] instanceof Resource){
//                return NbBundle.getMessage(SafeDeleteUI.class, "DSC_SafeDel", 
//                        ((Resource)elementsToDelete[0]).getName()); // NOI18N
//        }
        return NbBundle.getMessage(SafeDeleteUI.class, "DSC_SafeDel", elementsToDelete); // NOI18N
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        
        return new HelpCtx(SafeDeleteUI.class.getName());
    }
    
    public String getName() {
        
        return NbBundle.getMessage(SafeDeleteUI.class, "LBL_SafeDel"); // NOI18N
    }
    
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        //TODO:Do you want to just use Arrays.asList?
        if(panel == null)
            panel = new SafeDeletePanel(refactoring, Arrays.asList(elementsToDelete));
        return panel;
    }
    
    public AbstractRefactoring getRefactoring() {
        
        return refactoring;
    }
    
    public boolean hasParameters() {
        
        return true;
    }
    /**
     * Returns false, since this refactoring is not a query.
     * @return false
     */
    public boolean isQuery() {
        return false;
    }
    
    public Problem setParameters() {
        refactoring.setCheckInComments(panel.isSearchInComments());
        return refactoring.checkParameters();
    }
    
    //Helper methods------------------
    
    private String getString(String key) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(SafeDeleteUI.class);
        }
        return bundle.getString(key);
    }
    
    private String getString(String key, Object value) {
        return new MessageFormat(getString(key)).format(new Object[] {value});
    }
    
    
}
