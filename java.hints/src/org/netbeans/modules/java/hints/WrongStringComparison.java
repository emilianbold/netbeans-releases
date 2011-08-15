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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.lang.model.type.TypeMirror;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.introduce.CopyFinder;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Hint catching comparsion of Strings with <code>==</code> or <code>!=</code>
 * @author phrebejk
 */
public class WrongStringComparison extends AbstractHint {

    private static final String TERNARY_NULL_CHECK = "ternary-null-check"; // NOI18N
    private static final String STRING_LITERALS_FIRST = "string-literals-first"; //NOI18N
    private static final String STRING_TYPE = "java.lang.String";  // NOI18N
    private static final Set<Tree.Kind> TREE_KINDS = 
            EnumSet.<Tree.Kind>of( Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO );

    private AtomicBoolean cancel = new AtomicBoolean();
    
    public WrongStringComparison() {
        super(true, true, AbstractHint.HintSeverity.WARNING);
    }

    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        cancel.set(false);
        
        Tree t = treePath.getLeaf();
        
        if (!getTreeKinds().contains(t.getKind())) {
            return null;
        }
        
        BinaryTree bt = (BinaryTree) t;
        
        TreePath left = new TreePath(treePath, bt.getLeftOperand() );
        TreePath right = new TreePath(treePath, bt.getRightOperand() );
        
        Trees trees = info.getTrees(); 
        TypeMirror leftType = left == null ? null : trees.getTypeMirror(left);
        TypeMirror rightType = right == null ? null : trees.getTypeMirror(right);

        if ( leftType != null && rightType != null && 
             STRING_TYPE.equals(leftType.toString()) && 
             STRING_TYPE.equals(rightType.toString())) {
            
            if (checkInsideGeneratedEquals(info, treePath, left.getLeaf(), right.getLeaf())) {
                return null;
            }

            FileObject file = info.getFileObject();
            TreePathHandle tph = TreePathHandle.create(treePath, info);
            ArrayList<Fix> fixes = new ArrayList<Fix>();
            boolean reverseOperands = false;
            if (bt.getLeftOperand().getKind() != Tree.Kind.STRING_LITERAL) {
                if (bt.getRightOperand().getKind() == Tree.Kind.STRING_LITERAL) {
                    if (getStringLiteralsFirst()) {
                        reverseOperands = true;
                    } else {
                        fixes.add(JavaFix.toEditorFix(new WrongStringComparisonFix(tph, WrongStringComparisonFix.Kind.NULL_CHECK)));
                    }
                } else {
                    fixes.add(JavaFix.toEditorFix(new WrongStringComparisonFix(tph, WrongStringComparisonFix.Kind.ternaryNullCheck(getTernaryNullCheck()))));
                }
            }
            fixes.add(JavaFix.toEditorFix(new WrongStringComparisonFix(tph, WrongStringComparisonFix.Kind.reverseOperands(reverseOperands))));
            return Collections.<ErrorDescription>singletonList(
                ErrorDescriptionFactory.createErrorDescription(
                    getSeverity().toEditorSeverity(), 
                    getDisplayName(), 
                    Collections.unmodifiableList(fixes),
                    info.getFileObject(),
                    (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t),
                    (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t)) );

        }
        
        return null;
    }

    public void cancel() {
        cancel.set(true);
    }
    
    public String getId() {
        return "Wrong_String_Comparison"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WrongStringComparison.class, "LBL_WrongStringComparison"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(WrongStringComparison.class, "DSC_WrongStringComparison"); // NOI18N
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return new WrongStringComparisonCustomizer(node);
    }

    private boolean checkInsideGeneratedEquals(CompilationInfo info, TreePath treePath, Tree left, Tree right) {
        TreePath sourcePathParent = treePath.getParentPath();

        if (sourcePathParent.getLeaf().getKind() != Kind.CONDITIONAL_AND) { //performance
            return false;
        }
        
        SourcePositions sp = info.getTrees().getSourcePositions();
        Scope s = info.getTrees().getScope(sourcePathParent);
        
        String leftText = info.getText().substring((int) sp.getStartPosition(info.getCompilationUnit(), left), (int) sp.getEndPosition(info.getCompilationUnit(), left) + 1);
        String rightText = info.getText().substring((int) sp.getStartPosition(info.getCompilationUnit(), right), (int) sp.getEndPosition(info.getCompilationUnit(), right) + 1);
        String code = leftText + " != " + rightText + " && (" + leftText + "== null || !" + leftText + ".equals(" + rightText + "))"; // NOI18N
        ExpressionTree correct = info.getTreeUtilities().parseExpression(code, new SourcePositions[1]);

        info.getTreeUtilities().attributeTree(correct, s);

        TreePath correctPath = new TreePath(sourcePathParent.getParentPath(), correct);
        
        String originalCode = info.getText().substring((int) sp.getStartPosition(info.getCompilationUnit(), sourcePathParent.getLeaf()), (int) sp.getEndPosition(info.getCompilationUnit(), sourcePathParent.getLeaf()) + 1);
        ExpressionTree original = info.getTreeUtilities().parseExpression(originalCode, new SourcePositions[1]);
        
        info.getTreeUtilities().attributeTree(original, s);

        TreePath originalPath = new TreePath(sourcePathParent.getParentPath(), original);

        return CopyFinder.isDuplicate(info, originalPath, correctPath, cancel);
    }

    boolean getTernaryNullCheck() {
        return getTernaryNullCheck(getPreferences(null));
    }

    boolean getStringLiteralsFirst() {
        return getStringLiteralsFirst(getPreferences(null));
    }

    static boolean getTernaryNullCheck(Preferences p) {
        return p.getBoolean(TERNARY_NULL_CHECK, true);
    }

    static boolean getStringLiteralsFirst(Preferences p) {
        return p.getBoolean(STRING_LITERALS_FIRST, true);
    }

    static void setTernaryNullCheck(Preferences p, boolean selected) {
        p.putBoolean(TERNARY_NULL_CHECK, selected);
    }

    static void setStringLiteralsFirst(Preferences p, boolean selected) {
        p.putBoolean(STRING_LITERALS_FIRST, selected);
    }

    static class WrongStringComparisonFix extends JavaFix {

        protected final Kind kind;

        public WrongStringComparisonFix(TreePathHandle tph, Kind kind) {
            super(tph);
            this.kind = kind;
        }

        public String getText() {
            switch (kind) {
                case REVERSE_OPERANDS:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_ReverseOperands"); // NOI18N
                case NO_NULL_CHECK:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_NoNullCheck"); // NOI18N
                case NULL_CHECK_TERNARY:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_TernaryNullCheck"); // NOI18N
                default:
                    return NbBundle.getMessage(WrongStringComparison.class, "FIX_WrongStringComparison_NullCheck"); // NOI18N
            }
        }

        @Override
        protected void performRewrite(WorkingCopy copy, TreePath path, boolean canShowUI) {
            if (path != null) {
                TreeMaker make = copy.getTreeMaker();
                BinaryTree oldTree = (BinaryTree) path.getLeaf();
                ExpressionTree left = oldTree.getLeftOperand();
                ExpressionTree right = oldTree.getRightOperand();
                ExpressionTree newTree;
                if (kind == Kind.REVERSE_OPERANDS) {
                    // "str2".equals(str1)
                    ExpressionTree rightEquals = make.MemberSelect(right, "equals"); // NOI18N
                    ExpressionTree rightEqualsLeft = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), rightEquals, Collections.singletonList(left));
                    rightEqualsLeft = matchSign(make, oldTree, rightEqualsLeft);
                    newTree = rightEqualsLeft;
                } else {
                    ExpressionTree leftEquals = make.MemberSelect(left, "equals"); // NOI18N
                    ExpressionTree leftEqualsRight = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), leftEquals, Collections.singletonList(right));
                    leftEqualsRight = matchSign(make, oldTree, leftEqualsRight);
                    if (kind == Kind.NO_NULL_CHECK) {
                        // str1.equals(str2)
                        newTree = leftEqualsRight;
                    } else {
                        ExpressionTree leftEqNull  = make.Binary(Tree.Kind.EQUAL_TO, left, make.Identifier("null")); // NOI18N
                        ExpressionTree rightEqNull = make.Binary(oldTree.getKind(), right, make.Identifier("null")); // NOI18N
                        if (kind == Kind.NULL_CHECK_TERNARY) {
                            // str1 == null ? str2 == null : str1.equals(str2)
                            newTree = make.ConditionalExpression(leftEqNull, rightEqNull, leftEqualsRight);
                        } else {
                            ExpressionTree leftNeNull = make.Binary(Tree.Kind.NOT_EQUAL_TO, left, make.Identifier("null")); // NOI18N
                            ExpressionTree leftNeNullAndLeftEqualsRight = make.Binary(Tree.Kind.CONDITIONAL_AND, leftNeNull, leftEqualsRight);
                            if (right.getKind() == Tree.Kind.STRING_LITERAL) {
                                // str1 != null && str1.equals("str2")
                                newTree = leftNeNullAndLeftEqualsRight;
                            } else {
                                // (str1 == null && str2 == null) || (str1 != null && str1.equals(str2))
                                ExpressionTree leftEqNullAndRightEqNull  = make.Binary(Tree.Kind.CONDITIONAL_AND, leftEqNull, rightEqNull);
                                newTree = make.Binary(Tree.Kind.CONDITIONAL_OR, make.Parenthesized(leftEqNullAndRightEqNull), make.Parenthesized(leftNeNullAndLeftEqualsRight));
                            }
                        }
                        if (path.getParentPath().getLeaf().getKind() != Tree.Kind.PARENTHESIZED) {
                            newTree = make.Parenthesized(newTree);
                        }
                    }
                }
                copy.rewrite(oldTree, newTree);
            }
        }

        ExpressionTree matchSign(TreeMaker make, BinaryTree oldTree, ExpressionTree et) {
            if (oldTree.getKind() == Tree.Kind.NOT_EQUAL_TO) {
                return make.Unary(Tree.Kind.LOGICAL_COMPLEMENT, et);
            } else {
                return et;
            }
        }

        enum Kind {
            REVERSE_OPERANDS,
            NO_NULL_CHECK,
            NULL_CHECK,
            NULL_CHECK_TERNARY;

            public static Kind ternaryNullCheck(boolean ternary) {
                return ternary ? NULL_CHECK_TERNARY : NULL_CHECK;
            }

            public static Kind reverseOperands(boolean reverseOperands) {
                return reverseOperands ? REVERSE_OPERANDS : NO_NULL_CHECK;
            }

        }

    }

}
