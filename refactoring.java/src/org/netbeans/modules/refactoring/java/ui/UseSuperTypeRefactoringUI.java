/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.netbeans.modules.refactoring.java.RetoucheUtils;
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
        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(subType));
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
