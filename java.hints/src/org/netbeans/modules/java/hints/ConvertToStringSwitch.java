/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertToStringSwitch extends AbstractHint {

    public ConvertToStringSwitch() {
        super(true, false, HintSeverity.WARNING);
    }

    @Override
    public String getDescription() {
        return "ConvertToStringSwitch";
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.IF);
    }

    static boolean checkSourceLevel = true; // for tests only!

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        if (treePath.getLeaf().getKind() != Kind.IF
                || treePath.getParentPath().getLeaf().getKind() == Kind.IF
                || (checkSourceLevel && info.getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0)) {
            return null;
        }
        
        TreePath[] value = new TreePath[1];
        TreePath tp = treePath;
        Map<TreePathHandle, TreePathHandle> literal2Statement = new LinkedHashMap<TreePathHandle, TreePathHandle>();
        TreePathHandle defaultStatement = null;

        while (true) {
            if (tp.getLeaf().getKind() == Kind.IF) {
                IfTree it = (IfTree) tp.getLeaf();
                TreePath lt = isStringComparison(info, new TreePath(tp, it.getCondition()), value);

                if (lt == null) {
                    return null;
                }

                literal2Statement.put(TreePathHandle.create(lt, info), TreePathHandle.create(new TreePath(tp, it.getThenStatement()), info));

                if (it.getElseStatement() == null) {
                    break;
                }

                tp = new TreePath(tp, it.getElseStatement());
            } else {
                defaultStatement = TreePathHandle.create(tp, info);
                break;
            }
        }

        if (literal2Statement.size() <= 1) {
            return null;
        }

        Fix convert = new ConvertToSwitch(info.getFileObject(),
                                          TreePathHandle.create(treePath, info),
                                          TreePathHandle.create(value[0], info),
                                          literal2Statement,
                                          defaultStatement);
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), treePath.getLeaf());
        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(),
                                                                             "Convert to switch",
                                                                             Collections.singletonList(convert),
                                                                             info.getFileObject(),
                                                                             start,
                                                                             start + 1);

        return Collections.singletonList(ed);
    }

    public String getId() {
        return ConvertToStringSwitch.class.getName();
    }

    public String getDisplayName() {
        return "ConvertToStringSwitch";
    }

    public void cancel() {
    }

    private static TreePath isStringComparison(CompilationInfo info, TreePath tp, TreePath[] value) {
        Tree leaf = tp.getLeaf();

        while (leaf.getKind() == Kind.PARENTHESIZED) {
            tp = new TreePath(tp, ((ParenthesizedTree) leaf).getExpression());
            leaf = tp.getLeaf();
        }

        TreePath literal = null;
        TreePath val = null;

        if (leaf.getKind() == Kind.EQUAL_TO) {
            BinaryTree test = (BinaryTree) leaf;
            ExpressionTree left = test.getLeftOperand();
            TreePath leftPath = new TreePath(tp, left);
            if (left.getKind() == Kind.STRING_LITERAL) {
                literal = leftPath;
            } else {
                VariableElement ve = getVarElement(info.getTrees(), leftPath);
                if (ve == null)
                    return null;
                if (ve.getConstantValue() != null) {
                    literal = leftPath;
                } else {
                    val = leftPath;
                }
            }
            ExpressionTree right = test.getRightOperand();
            TreePath rightPath = new TreePath(tp, right);
            if (right.getKind() == Kind.STRING_LITERAL) {
                if (literal != null)
                    return null;
                literal = rightPath;
            } else {
                VariableElement ve = getVarElement(info.getTrees(), rightPath);
                if (ve == null)
                    return null;
                if (ve.getConstantValue() != null) {
                    if (literal != null)
                        return null;
                    literal = rightPath;
                } else {
                    if (val != null)
                        return null;
                    val = rightPath;
                }
            }
        } else if (leaf.getKind() == Kind.METHOD_INVOCATION) {
            MethodInvocationTree test = (MethodInvocationTree) leaf;
            ExpressionTree sel = test.getMethodSelect();
            if (sel.getKind() != Kind.MEMBER_SELECT)
                return null;
            MemberSelectTree mst = (MemberSelectTree) sel;
            Name name = mst.getIdentifier();
            if (!"equals".contentEquals(name) && !"contentEquals".contentEquals(name)) //NOI18N
                return null;
            ExpressionTree left = mst.getExpression();
            TreePath leftPath = new TreePath(new TreePath(tp, sel), left);
            if (left.getKind() == Kind.STRING_LITERAL) {
                literal = leftPath;
            } else {
                VariableElement ve = getVarElement(info.getTrees(), leftPath);
                if (ve == null)
                    return null;
                if (ve.getConstantValue() != null) {
                    literal = leftPath;
                } else {
                    val = leftPath;
                }
            }
            Iterator<? extends ExpressionTree> it = test.getArguments().iterator();
            ExpressionTree right = it.hasNext() ? it.next() : null;
            if (right == null || it.hasNext())
                return null;
            TreePath rightPath = new TreePath(tp, right);
            if (right.getKind() == Kind.STRING_LITERAL) {
                if (literal != null)
                    return null;
                literal = rightPath;
            } else {
                VariableElement ve = getVarElement(info.getTrees(), rightPath);
                if (ve == null)
                    return null;
                if (ve.getConstantValue() != null) {
                    if (literal != null)
                        return null;
                    literal = rightPath;
                } else {
                    if (val != null)
                        return null;
                    val = rightPath;
                }
            }
        }

        if (literal == null || val == null)
            return null;

        if (value[0] == null) {
            value[0] = val;
        } else {
            Element e = info.getTrees().getElement(value[0]);
            Element valE = info.getTrees().getElement(val);
            if (e != valE)
                return null;
        }

        return literal;
    }

    private static VariableElement getVarElement(Trees trees, TreePath tp) {
        Element el = trees.getElement(tp);
        if (el == null)
            return null;
        switch (el.getKind()) {
            case FIELD:
            case LOCAL_VARIABLE:
            case PARAMETER:
                TypeMirror tm = el.asType();
                if (tm.getKind() == TypeKind.DECLARED && "java.lang.String".contentEquals(((TypeElement)((DeclaredType)tm).asElement()).getQualifiedName())) //NOI18N
                    return (VariableElement) el;
        }
        return null;
    }

    private static final class ConvertToSwitch implements Fix {

        private final FileObject fileObject;
        private final TreePathHandle create;
        private final TreePathHandle value;
        private final Map<TreePathHandle, TreePathHandle> literal2Statement;
        private final TreePathHandle defaultStatement;

        public ConvertToSwitch(FileObject fileObject, TreePathHandle create, TreePathHandle value, Map<TreePathHandle, TreePathHandle> literal2Statement, TreePathHandle defaultStatement) {
            this.fileObject = fileObject;
            this.create = create;
            this.value = value;
            this.literal2Statement = literal2Statement;
            this.defaultStatement = defaultStatement;
        }

        public String getText() {
            return "Convert to switch";
        }

        public ChangeInfo implement() throws Exception {
            JavaSource.forFileObject(fileObject).runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy copy) throws Exception {
                    copy.toPhase(JavaSource.Phase.UP_TO_DATE);

                    TreePath it = create.resolve(copy);

                    if (it == null) {
                        return ;
                    }
                    
                    TreeMaker make = copy.getTreeMaker();
                    List<CaseTree> cases = new LinkedList<CaseTree>();

                    for (Entry<TreePathHandle, TreePathHandle> e : ConvertToSwitch.this.literal2Statement.entrySet()) {
                        TreePath l = e.getKey().resolve(copy);
                        TreePath s = e.getValue().resolve(copy);

                        if (l == null || s == null) {
                            return ;
                        }

                        List<StatementTree> statements = new LinkedList<StatementTree>();
                        Tree then = s.getLeaf();

                        if (then.getKind() == Kind.BLOCK) {
                            //XXX: should verify declarations inside the blocks
                            statements.addAll(((BlockTree) then).getStatements());
                        } else {
                            statements.add((StatementTree) then);
                        }

                        statements.add(make.Break(null));

                        cases.add(make.Case((ExpressionTree) l.getLeaf(), statements));
                    }

                    if (defaultStatement != null) {
                        TreePath s = defaultStatement.resolve(copy);

                        if (s == null) {
                            return ;
                        }

                        List<StatementTree> statements = new LinkedList<StatementTree>();
                        Tree then = s.getLeaf();

                        if (then.getKind() == Kind.BLOCK) {
                            //XXX: should verify declarations inside the blocks
                            statements.addAll(((BlockTree) then).getStatements());
                        } else {
                            statements.add((StatementTree) then);
                        }

                        statements.add(make.Break(null));

                        cases.add(make.Case(null, statements));
                    }

                    TreePath value = ConvertToSwitch.this.value.resolve(copy);

                    SwitchTree s = make.Switch((ExpressionTree) value.getLeaf(), cases);

                    copy.rewrite(it.getLeaf(), s); //XXX
                }
            }).commit();

            return null;
        }

    }

}
