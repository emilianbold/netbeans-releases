/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class MissingReturnStatement implements ErrorRule<Void> {

    private static final Set<String> CODES = new HashSet<String>(Arrays.asList("compiler.err.missing.ret.stmt"));

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        TreePath method = null;
        TreePath tp = compilationInfo.getTreeUtilities().pathFor(offset); //XXX: the passed treePath is for offset + 1

        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            if (tp.getLeaf().getKind() == Kind.METHOD) {
                method = tp;
                break;
            }

            tp = tp.getParentPath();
        }

        if (method == null) {
            return null;
        }

        MethodTree mt = (MethodTree) tp.getLeaf();

        if (mt.getReturnType() == null) {
            return null;
        }

        List<Fix> result = new ArrayList<Fix>(2);

        result.add(new FixImpl(compilationInfo.getSnapshot().getSource(), TreePathHandle.create(tp, compilationInfo)));
        result.add(new ChangeMethodReturnType.FixImpl(compilationInfo, tp, TypeMirrorHandle.create(compilationInfo.getTypes().getNoType(TypeKind.VOID)), "void").toEditorFix());

        return result;
    }

    @Override
    public String getId() {
        return MissingReturnStatement.class.getCanonicalName();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MissingReturnStatement.class, "DN_MissingReturnStatement");
    }

    @Override
    public void cancel() {}

    private static final class FixImpl implements Fix {

        private final Source source;
        private final TreePathHandle methodHandle;

        public FixImpl(Source source, TreePathHandle methodHandle) {
            this.source = source;
            this.methodHandle = methodHandle;
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(MissingReturnStatement.class, "FIX_AddReturnStatement");
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ModificationResult mr = ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {
                @Override public void run(ResultIterator resultIterator) throws Exception {
                    WorkingCopy wc = WorkingCopy.get(resultIterator.getParserResult());

                    wc.toPhase(Phase.RESOLVED);

                    TreePath method = methodHandle.resolve(wc);
                    Element methodEl = method != null ? wc.getTrees().getElement(method) : null;

                    if (methodEl == null || methodEl.getKind() != ElementKind.METHOD) {
                        return ;
                    }

                    assert method.getLeaf().getKind() == Kind.METHOD;

                    BlockTree body = ((MethodTree) method.getLeaf()).getBody();
                    TypeMirror type = ((ExecutableElement) methodEl).getReturnType();
                    TypeKind kind = type.getKind();
                    Object value;
                    if (kind.isPrimitive()) {
                        if (kind == TypeKind.BOOLEAN) {
                            value = false;
                        }
                        else {
                            value = 0;
                        }
                    }
                    else {
                        value = null;
                    }

                    TreeMaker make = wc.getTreeMaker();
                    LiteralTree nullValue = make.Literal(value);

                    wc.tag(nullValue, Utilities.TAG_SELECT);
                    wc.rewrite(body, make.addBlockStatement(body, make.Return(nullValue)));
                }
            });

            return Utilities.commitAndComputeChangeInfo(source.getFileObject(), mr);
        }

    }
}
