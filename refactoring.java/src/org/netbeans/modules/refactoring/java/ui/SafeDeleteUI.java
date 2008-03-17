/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.refactoring.java.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A CustomRefactoringUI subclass that represents Safe Delete
 * @author Bharath Ravikumar
 */
public class SafeDeleteUI implements RefactoringUI, RefactoringUIBypass{
    
    private final SafeDeleteRefactoring refactoring;
    
    private Object[] elementsToDelete;
    
    private SafeDeletePanel panel;
    
    private ResourceBundle bundle;
    
    private boolean regulardelete = false;
    /**
     * Creates a new instance of SafeDeleteUI
     * @param selectedElements An array of selected Elements that need to be 
     * safely deleted
     */
    public SafeDeleteUI(FileObject[] selectedElements, Collection<TreePathHandle> handles, boolean regulardelete) {
        this.elementsToDelete = selectedElements;
        refactoring = new SafeDeleteRefactoring(new ProxyLookup(Lookups.fixed(elementsToDelete), Lookups.fixed(handles.toArray(new Object[handles.size()]))));
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(selectedElements));
        this.regulardelete = regulardelete;
    }

    /**
     * Creates a new instance of SafeDeleteUI
     * @param selectedElements An array of selected Elements that need to be 
     * safely deleted
     */
    public SafeDeleteUI(TreePathHandle[] selectedElements, CompilationInfo info) {
        this.elementsToDelete = selectedElements;
        refactoring = new SafeDeleteRefactoring(Lookups.fixed(elementsToDelete));
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(selectedElements[0]));
    }

    public SafeDeleteUI(NonRecursiveFolder nonRecursiveFolder) {
        refactoring = new SafeDeleteRefactoring(Lookups.fixed(nonRecursiveFolder));
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(nonRecursiveFolder.getFolder()));
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
        NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        if (folder != null) {
            return NbBundle.getMessage(SafeDeleteUI.class, "DSC_SafeDelPkg", folder); // NOI18N
        }
        
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
            panel = new SafeDeletePanel(refactoring, regulardelete, parent);
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
    
    public boolean isRefactoringBypassRequired() {
        return panel.isRegularDelete();
    }

    public void doRefactoringBypass() throws IOException {
        for (FileObject file:getRefactoring().getRefactoringSource().lookupAll(FileObject.class)) {
            DataObject.find(file).delete();
        }
    }
}
