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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.IntroduceParameterRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class IntroduceParameterUI implements RefactoringUI, JavaRefactoringUIFactory {
    
    private TreePathHandle expression;
    private IntroduceParameterPanel panel;
    private IntroduceParameterRefactoring refactoring;
    private Lookup lookup;
    
    /** Creates a new instance of IntroduceParameterUI */
    private IntroduceParameterUI(TreePathHandle expression, CompilationInfo info) {
        this.refactoring = new IntroduceParameterRefactoring(expression);
        this.expression = expression;
    }

    private IntroduceParameterUI(Lookup lookup) {
        this.lookup = lookup;
    }
    
    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        JEditorPane textC = NbDocument.findRecentEditorPane(ec);
        int startOffset = textC.getSelectionStart();
        int endOffset = textC.getSelectionEnd();
        TreePath tp = validateSelection(info, startOffset, endOffset);
        if (tp == null) {
            return null;
        }
        return new IntroduceParameterUI(TreePathHandle.create(tp, info), info);
    }
    
    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new IntroduceParameterUI(lookup);
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(IntroduceParameterUI.class, 
                                        "DSC_IntroduceParameterRootNode", refactoring.getParameterName()); // NOI18N
    }
    
    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new IntroduceParameterPanel(expression, parent);
        }
        return panel;
    }
    
    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
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
    
    @Override
    public String getName() {
        return NbBundle.getMessage(IntroduceParameterUI.class, "LBL_IntroduceParameter");
    }
    
    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }

    @Override
    public Problem setParameters() {
        return setParameters(false);
    }
    
    @Override
    public boolean hasParameters() {
        return true;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.IntroduceParameterUI"); // NOI18N
    }
    
    private static final Set<TypeKind> NOT_ACCEPTED_TYPES = EnumSet.of(TypeKind.ERROR, TypeKind.NONE, TypeKind.OTHER, TypeKind.VOID, TypeKind.EXECUTABLE);
 
    private static TreePath validateSelection(CompilationInfo ci, int start, int end) {
        return validateSelection(ci, start, end, NOT_ACCEPTED_TYPES);
    }

    private static TreePath validateSelection(CompilationInfo ci, int start, int end, Set<TypeKind> ignoredTypes) {
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);

        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();

            if (   !ExpressionTree.class.isAssignableFrom(leaf.getKind().asInterface())
                && (leaf.getKind() != Tree.Kind.VARIABLE || ((VariableTree) leaf).getInitializer() == null)) {
                continue;
            }

            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);

            if (!(treeStart <= start) || !(treeEnd >= end)) {
                continue;
            }

            TypeMirror type = ci.getTrees().getTypeMirror(tp);

            if (type != null && type.getKind() == TypeKind.ERROR) {
                type = ci.getTrees().getOriginalType((ErrorType) type);
            }

            if (type == null || ignoredTypes.contains(type.getKind())) {
                continue;
            }

            if(tp.getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                continue;
            }

            if (tp.getLeaf().getKind() == Tree.Kind.ANNOTATION) {
                continue;
            }

            if (!isInsideClass(tp)) {
                return null;
            }

            TreePath candidate = tp;

            tp = tp.getParentPath();

            while (tp != null) {
                switch (tp.getLeaf().getKind()) {
                    case VARIABLE:
                        VariableTree vt = (VariableTree) tp.getLeaf();
                        if (vt.getInitializer() == leaf) {
                            return candidate;
                        } else {
                            return null;
                        }
                    case NEW_CLASS:
                        NewClassTree nct = (NewClassTree) tp.getLeaf();
                        
                        if (nct.getIdentifier().equals(candidate.getLeaf())) { //avoid disabling hint ie inside of anonymous class higher in treepath
                            for (Tree p : nct.getArguments()) {
                                if (p == leaf) {
                                    return candidate;
                                }
                            }

                            return null;
                        }
                }
                leaf = tp.getLeaf();
                tp = tp.getParentPath();
            }
            return candidate;
        }
        return null;
    }

    private static boolean isInsideClass(TreePath tp) {
        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                return true;
            }

            tp = tp.getParentPath();
        }

        return false;
    }    
}
