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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Flaska, Jan Becicka
 */
public class ChangeParametersUI implements RefactoringUI {
    
    TreePathHandle refactoredObj;
    ChangeParametersPanel panel;
    ChangeParametersRefactoring refactoring;
    private final ChangeParametersRefactoring.ParameterInfo[] preConfiguration;
    
    /** Creates a new instance of ChangeMethodSignatureRefactoring */
    private ChangeParametersUI(TreePathHandle refactoredObj, CompilationInfo info, ChangeParametersRefactoring.ParameterInfo[] preConfiguration) {
        this.refactoring = new ChangeParametersRefactoring(refactoredObj);
        this.refactoredObj = refactoredObj;
        this.preConfiguration = preConfiguration;
    }
    
    public static ChangeParametersUI create(TreePathHandle refactoredObj, CompilationInfo info, ChangeParametersRefactoring.ParameterInfo[] preConfiguration) {
        TreePath path = refactoredObj.resolve(info);
        Kind kind;
        while (path != null && (kind = path.getLeaf().getKind()) != Kind.METHOD && kind != Kind.METHOD_INVOCATION) {
            path = path.getParentPath();
        }
        
        return path != null
                ? new ChangeParametersUI(TreePathHandle.create(path, info), info, preConfiguration)
                : null;
    }
    
    public String getDescription() {
        String msg = NbBundle.getMessage(ChangeParametersUI.class, 
                                        "DSC_ChangeParsRootNode"); // NOI18N
        String name = RetoucheUtils.getSimpleName(refactoredObj);
        boolean isMethod = RetoucheUtils.getElementKind(refactoredObj).equals(ElementKind.METHOD);
        return new MessageFormat(msg).format(new Object[] { 
            name,
            NbBundle.getMessage(ChangeParametersUI.class, "DSC_ChangeParsRootNode" + (isMethod ? "Method" : "Constr")),
            panel.genDeclarationString()
       });
    }
    
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            //TODO:
            //parent.setPreviewEnabled(true);
            panel = new ChangeParametersPanel(refactoredObj, parent, preConfiguration);
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
        List data = (List) panel.getTableModel().getDataVector();
        ChangeParametersRefactoring.ParameterInfo[] paramList = new ChangeParametersRefactoring.ParameterInfo[data.size()];
        int counter = 0;
        Problem problem = null;
        for (Iterator rowIt = data.iterator(); rowIt.hasNext(); ++counter) {
            List row = (List) rowIt.next();
            int origIndex = ((Integer) row.get(3)).intValue();
            String type = (String) row.get(0);
            String name = (String) row.get(1);
            String defaultVal = (String) row.get(2);
            paramList[counter] = new ChangeParametersRefactoring.ParameterInfo(origIndex, name, type, defaultVal);
        }
        Set<Modifier> modifier = panel.getModifier();
        refactoring.setParameterInfo(paramList);
        refactoring.setModifiers(modifier);
        refactoring.getContext().add(panel.getJavadoc());
        refactoring.setMethodName(panel.getMethodName());
        refactoring.setReturnType(panel.getReturnType());
        refactoring.setOverloadMethod(panel.isCompatible());
        if (checkOnly) {
            problem = refactoring.fastCheckParameters();
        } else {
            problem = refactoring.checkParameters();
        }
        return problem;
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
