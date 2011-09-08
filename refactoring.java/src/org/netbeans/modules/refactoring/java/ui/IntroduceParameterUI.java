/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui;

import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.IntroduceParameterRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class IntroduceParameterUI implements RefactoringUI {
    
    private TreePathHandle expression;
    private IntroduceParameterPanel panel;
    private IntroduceParameterRefactoring refactoring;
    
    /** Creates a new instance of IntroduceParameterUI */
    private IntroduceParameterUI(TreePathHandle expression, CompilationInfo info) {
        this.refactoring = new IntroduceParameterRefactoring(expression);
        this.expression = expression;
    }
    
    public static IntroduceParameterUI create(TreePathHandle expression, CompilationInfo info) {
        return new IntroduceParameterUI(expression, info);
    }
    
    public String getDescription() {
        return NbBundle.getMessage(IntroduceParameterUI.class, 
                                        "DSC_IntroduceParameterRootNode", refactoring.getParameterName()); // NOI18N
    }
    
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            //TODO:
            //parent.setPreviewEnabled(true);
            panel = new IntroduceParameterPanel(expression, parent);
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
        Problem problem = null;
        refactoring.setFinal(panel.isDeclareFinal());
        refactoring.setParameterName(panel.getParameterName());
        refactoring.setOverloadMethod(panel.isCompatible());
        refactoring.setReplaceAll(panel.isReplaceAll());
        refactoring.getContext().add(panel.getJavadoc());
        if (checkOnly) {
            problem = refactoring.fastCheckParameters();
        } else {
            problem = refactoring.checkParameters();
        }
        return problem;
    }
    
    public String getName() {
        return NbBundle.getMessage(IntroduceParameterUI.class, "LBL_IntroduceParameter");
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
        return new HelpCtx(IntroduceParameterUI.class);
    }
}
