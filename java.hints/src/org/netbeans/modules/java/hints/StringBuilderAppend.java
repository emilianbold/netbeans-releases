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
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class StringBuilderAppend extends AbstractHint {

    public StringBuilderAppend() {
        super(true, false, HintSeverity.WARNING);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(StringBuilderAppend.class, "DSC_StringBuilderAppend");
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath tp) {
        Element el = info.getTrees().getElement(tp);

        if (el == null || el.getKind() != ElementKind.METHOD || el.getModifiers().contains(Modifier.STATIC) || !el.getSimpleName().contentEquals("append")) {
            return null;
        }

        ExecutableElement ee = (ExecutableElement) el;
        TypeElement jlString = info.getElements().getTypeElement("java.lang.String");

        if (ee.getParameters().size() != 1 || !info.getTypes().isSameType(ee.getParameters().get(0).asType(), jlString.asType())) {
            return null;
        }

        if (el.getEnclosingElement().getKind() != ElementKind.CLASS) {
            return null;
        }

        TypeElement clazz = (TypeElement) el.getEnclosingElement();
        Name fqn = clazz.getQualifiedName();

        if (!SUPPORTED_CLASSES.contains(fqn.toString())) {
            return null;
        }

        MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
        ExpressionTree param = mit.getArguments().get(0);
        List<List<TreePath>> sorted = sortOut(info, linearize(new TreePath(tp, param)));

        if (sorted.size() > 1) {
            int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), param);
            int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), param);
            List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(info.getSnapshot().getSource(), TreePathHandle.create(tp, info)));
            String error = NbBundle.getMessage(StringBuilderAppend.class, "ERR_StringBuilderAppend", clazz.getSimpleName().toString());
            ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), error, fixes, info.getFileObject(), start, end);
            return Collections.singletonList(ed);
        }

        return null;
    }

    public String getId() {
        return StringBuilderAppend.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(StringBuilderAppend.class, "DN_StringBuilderAppend");
    }

    public void cancel() {}

    private static Set<String> SUPPORTED_CLASSES = new HashSet<String>(Arrays.asList("java.lang.StringBuilder", "java.lang.StringBuffer"));

    private static List<TreePath> linearize(TreePath tree) {
        List<TreePath> todo = new LinkedList<TreePath>();
        List<TreePath> result = new LinkedList<TreePath>();

        todo.add(tree);

        while (!todo.isEmpty()) {
            TreePath tp = todo.remove(0);

            if (tp.getLeaf().getKind() != Kind.PLUS) {
                result.add(tp);
                continue;
            }

            BinaryTree bt = (BinaryTree) tp.getLeaf();

            todo.add(0, new TreePath(tp, bt.getRightOperand()));
            todo.add(0, new TreePath(tp, bt.getLeftOperand()));
        }

        return result;
    }

    private static List<List<TreePath>> sortOut(CompilationInfo info, List<TreePath> trees) {
        List<List<TreePath>> result = new LinkedList<List<TreePath>>();
        List<TreePath> currentCluster = new LinkedList<TreePath>();

        for (TreePath t : trees) {
            if (constant(info, t)) {
                currentCluster.add(t);
            } else {
                if (!currentCluster.isEmpty()) {
                    result.add(currentCluster);
                    currentCluster = new LinkedList<TreePath>();
                }
                result.add(new LinkedList<TreePath>(Collections.singletonList(t)));
            }
        }

        if (!currentCluster.isEmpty()) {
            result.add(currentCluster);
        }
        
        return result;
    }

    private static boolean constant(CompilationInfo info, TreePath tp) {
        if (tp.getLeaf().getKind() == Kind.STRING_LITERAL) return true;

        Element el = info.getTrees().getElement(tp);

        return el != null && el.getKind() == ElementKind.FIELD && ((VariableElement) el).getConstantValue() instanceof String;
    }

    private static final class FixImpl implements Fix {

        private final Source source;
        private final TreePathHandle tph;

        public FixImpl(Source source, TreePathHandle tph) {
            this.source = source;
            this.tph = tph;
        }

        public String getText() {
            return NbBundle.getMessage(StringBuilderAppend.class, "FIX_StringBuilderAppend");
        }

        public ChangeInfo implement() throws Exception {
            ModificationResult.runModificationTask(Collections.singletonList(source), new UserTask() {
                public void run (ResultIterator it) throws Exception {
                    WorkingCopy copy = WorkingCopy.get(it.getParserResult());//XXX: resolve in a correct position

                    if (copy == null) {
                        //XXX: log
                        return ;
                    }
                    
                    copy.toPhase(Phase.RESOLVED);

                    TreePath tp = tph.resolve(copy);

                    if (tp == null) {
                        return ;
                    }

                    MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
                    ExpressionTree param = mit.getArguments().get(0);
                    List<List<TreePath>> sorted = sortOut(copy, linearize(new TreePath(tp, param)));
                    ExpressionTree site = ((MemberSelectTree) mit.getMethodSelect()).getExpression();
                    TreeMaker make = copy.getTreeMaker();

                    for (List<TreePath> cluster : sorted) {
                        ExpressionTree arg = (ExpressionTree) cluster.remove(0).getLeaf();

                        while (!cluster.isEmpty()) {
                            arg = make.Binary(Kind.PLUS, arg, (ExpressionTree) cluster.remove(0).getLeaf());
                        }

                        while (arg.getKind() == Kind.PARENTHESIZED) {
                            arg = ((ParenthesizedTree) arg).getExpression();
                        }
                        
                        site = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(site, "append"), Collections.singletonList(arg));
                    }

                    copy.rewrite(mit, site);
                }
            }).commit();

            return null;
        }
        
    }

}
