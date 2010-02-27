/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.encapsulation;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class ReturnEncapsulation {

    private static final String COLLECTION = "java.util.Collection";    //NOI18N
    private static final String MAP = "java.util.Map";                  //NOI18N
    private static final String DATE = "java.util.Date";                //NOI18N
    private static final String CALENDAR="java.util.Calendar";          //NOI18N
    private static final String A_OBJ = "java.lang.Object[]";           //NOI18N
    private static final String A_BOOL = "boolean[]";                   //NOI18N
    private static final String A_BYTE = "byte[]";                      //NOI18N
    private static final String A_CHAR = "char[]";                      //NOI18N
    private static final String A_SHORT = "short[]";                    //NOI18N
    private static final String A_INT = "int[]";                        //NOI18N
    private static final String A_LONG = "long[]";                      //NOI18N
    private static final String A_FLOAT = "float[]";                    //NOI18N
    private static final String A_DOUBLE = "double[]";                  //NOI18N

    @Hint(category="encapsulation",suppressWarnings="ReturnOfCollectionOrArrayField", enabled=false) //NOI18N
    @TriggerPatterns({
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=COLLECTION)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=MAP)   //NOI18N
            }
        )
    })
    public static ErrorDescription collection(final HintContext ctx) {
        assert ctx != null;
        return create(ctx,
               NbBundle.getMessage(ReturnEncapsulation.class, "TXT_ReturnCollection"),
               "ReturnOfCollectionOrArrayField",    //NOI18N
               new CollectionFix());   //NOI18N
    }

    @Hint(category="encapsulation",suppressWarnings="ReturnOfCollectionOrArrayField", enabled=false) //NOI18N
    @TriggerPatterns ({
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_OBJ)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_BOOL)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_BYTE)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_CHAR)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_SHORT)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_INT)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_LONG)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_FLOAT)   //NOI18N
            }
        ),
        @TriggerPattern(value="return $expr",    //NOI18N
            constraints={
                @Constraint(variable="$expr",type=A_DOUBLE)   //NOI18N
            }
        )
    })
    public static ErrorDescription array(final HintContext ctx) {
        assert ctx != null;
        return create(ctx,
            NbBundle.getMessage(ReturnEncapsulation.class, "TXT_ReturnArray"),
            "ReturnOfCollectionOrArrayField");  //NOI18N
    }

    @Hint(category="encapsulation", suppressWarnings={"ReturnOfDateField"}, enabled=false) //NOI18N
    @TriggerPatterns({
        @TriggerPattern(value="return $expr",   //NOI18N
            constraints={
                @Constraint(variable="$expr",type=DATE)   //NOI18N
        }),
        @TriggerPattern(value="return $expr",   //NOI18N
            constraints={
                @Constraint(variable="$expr",type=CALENDAR)   //NOI18N
        })
    })
    public static ErrorDescription date(final HintContext ctx) {
        assert ctx != null;
        return create(ctx, NbBundle.getMessage(ReturnEncapsulation.class, "TXT_ReturnDate"),
            "ReturnOfDateField");   //NOI18N
    }

    private static ErrorDescription create(final HintContext ctx,
            final String description,
            final String suppressWarnings,
            FixProvider ... providers) {
        assert ctx != null;
        assert suppressWarnings != null;
        final CompilationInfo info = ctx.getInfo();
        final TreePath tp = ctx.getPath();
        final TreePath exprPath = ctx.getVariables().get("$expr");      //NOI18N
        final Element exprElement = info.getTrees().getElement(exprPath);
        final TypeMirror exprType = info.getTrees().getTypeMirror(exprPath);
        if (exprElement == null ||
            exprType == null ||
            exprElement.getKind() != ElementKind.FIELD ||
            exprType.getKind() == TypeKind.ERROR) {
            return null;
        }
        final Element enclMethod = getElementOrNull(info,findEnclosingMethod(tp));
        if (enclMethod == null || enclMethod.getEnclosingElement() != exprElement.getEnclosingElement()) {
            return null;
        }
        final Fix swFix = FixFactory.createSuppressWarningsFix(info, tp, suppressWarnings);
        final Fix[] fixes;
        if (providers.length == 0) {
            fixes = new Fix[] {swFix};
        } else {
            fixes = new Fix[providers.length+1];
            for (int i=0; i<providers.length; i++) {
                fixes[i]=providers[i].fixFor(info,exprPath);
            }
            fixes[providers.length] = swFix;
        }
        return ErrorDescriptionFactory.forTree(ctx, tp,
            description,
            fixes);
    }

    private static final TreePath findEnclosingMethod(TreePath tp) {
        while (tp != null && tp.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
            if (tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                return tp;
            }
            tp = tp.getParentPath();
        }
        return null;
    }

    private static Element getElementOrNull(final CompilationInfo info, final TreePath path) {
        return path == null ? null : info.getTrees().getElement(path);
    }

    private static interface FixProvider {
        public Fix fixFor(CompilationInfo info, TreePath tp);
    }

    private static class CollectionFix implements Fix, FixProvider {

        private static final String UNMOD_COL = "unmodifiableCollection";   //NOI18N
        private static final String UNMOD_LIST = "unmodifiableList";        //NOI18N
        private static final String UNMOD_SET = "unmodifiableSet";          //NOI18N
        private static final String UNMOD_MAP = "unmodifiableMap";          //NOI18N

        private static final String LIST = "java.util.List";                //NOI18N
        private static final String SET = "java.util.Set";                  //NOI18N
        private static final String COLLECTIONS = "java.util.Collections";  //NOI18N

        private TreePathHandle handle;
        private String method;
        private String field;

        private CollectionFix() {
        }

        @Override
        public String getText() {
            assert handle != null;
            return NbBundle.getMessage(ReturnEncapsulation.class, "FIX_ReplaceWithUC",method,field);
        }

        @Override
        public ChangeInfo implement() throws Exception {
            assert handle != null;
            final FileObject file = handle.getFileObject();
            if (file == null) {
                return null;
            }
            JavaSource.forFileObject(file).runModificationTask(new Task<WorkingCopy>() {
                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    final TreePath tp = handle.resolve(wc);
                    if (tp == null) {
                        return;
                    }
                    final TreeMaker tm = wc.getTreeMaker();
                    final String collectionsName = SourceUtils.resolveImport(wc, tp, COLLECTIONS);
                    wc.rewrite(tp.getLeaf(), tm.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            tm.MemberSelect(tm.Identifier(collectionsName), method),
                            Collections.singletonList((ExpressionTree)tp.getLeaf())));
                }
            }).commit();
            return null;
        }

        @Override
        public Fix fixFor(final CompilationInfo info, final TreePath tp) {
            assert info != null;
            assert tp != null;
            assert handle == null;
            handle = TreePathHandle.create(tp, info);
            field = info.getTrees().getElement(tp).getSimpleName().toString();
            final Types types = info.getTypes();
            final Elements elements = info.getElements();
            TypeMirror fieldTypeEr = types.erasure(info.getTrees().getTypeMirror(tp));
            final TypeElement list = elements.getTypeElement(LIST);
            final TypeElement set = elements.getTypeElement(SET);
            final TypeElement map = elements.getTypeElement(MAP);
            if (list != null && types.isSubtype(fieldTypeEr, types.erasure(list.asType()))) {
                method = UNMOD_LIST;
            }
            else if (set != null && types.isSubtype(fieldTypeEr, types.erasure(set.asType()))) {
                method = UNMOD_SET;
            }
            else if (map != null && types.isSubtype(fieldTypeEr, types.erasure(map.asType()))) {
                method = UNMOD_MAP;
            }
            else {
                method = UNMOD_COL;
            }
            return this;
        }
    }

}
