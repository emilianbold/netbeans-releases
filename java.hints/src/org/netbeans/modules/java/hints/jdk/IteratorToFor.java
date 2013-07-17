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

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle.Messages;

@Hint(displayName="#DN_IteratorToFor", description="#DESC_IteratorToFor", category="rules15", suppressWarnings={"", "ForLoopReplaceableByForEach", "WhileLoopReplaceableByForEach"})
@Messages({
    "DN_IteratorToFor=Use JDK 5 for-loop",
    "DESC_IteratorToFor=Replaces simple uses of Iterator with a corresponding for-loop.",
    "ERR_IteratorToFor=Use of Iterator for simple loop",
    "ERR_IteratorToForArray=Use enhanced for loop to iterate over the array",
    "FIX_IteratorToFor=Convert to for-loop"
})
public class IteratorToFor {

    @TriggerPattern("java.util.Iterator $it = $coll.iterator(); while ($it.hasNext()) {$type $elem = ($type) $it.next(); $rest$;}")
    public static ErrorDescription whileIdiom(HintContext ctx) {
        if (uses(ctx, ctx.getMultiVariables().get("$rest$"), ctx.getVariables().get("$it"))) {
            return null;
        }
        if (!iterable(ctx, ctx.getVariables().get("$coll"), ctx.getVariables().get("$type"))) {
            return null;
        }
        Tree highlightTarget = ctx.getPath().getLeaf();
        TreePath elem = ctx.getVariables().get("$elem");
        if (elem.getParentPath() != null && elem.getParentPath().getLeaf().getKind() == Kind.BLOCK) elem = elem.getParentPath();
        if (elem.getParentPath() != null && elem.getParentPath().getLeaf().getKind() == Kind.WHILE_LOOP) highlightTarget = elem.getParentPath().getLeaf();
        return ErrorDescriptionFactory.forName(ctx, highlightTarget, Bundle.ERR_IteratorToFor(),
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_IteratorToFor(), ctx.getPath(), "for ($type $elem : $coll) {$rest$;}"));
    }

    @TriggerPatterns({
        @TriggerPattern("for (java.util.Iterator $it = $coll.iterator(); $it.hasNext(); ) {$type $elem = ($type) $it.next(); $rest$;}"),
        @TriggerPattern("for (java.util.Iterator<$typaram> $it = $coll.iterator(); $it.hasNext(); ) {$type $elem = ($type) $it.next(); $rest$;}"),
        @TriggerPattern("for (java.util.Iterator<$typaram> $it = $coll.iterator(); $it.hasNext(); ) {$type $elem = $it.next(); $rest$;}")
    })
    public static ErrorDescription forIdiom(HintContext ctx) {
        if (uses(ctx, ctx.getMultiVariables().get("$rest$"), ctx.getVariables().get("$it"))) {
            return null;
        }
        if (!iterable(ctx, ctx.getVariables().get("$coll"), ctx.getVariables().get("$type"))) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_IteratorToFor(),
                JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_IteratorToFor(), ctx.getPath(), "for ($type $elem : $coll) {$rest$;}"));
    }
    
    @TriggerPattern(value="for (int $index = 0; $index < $arr.length; $index++) $statement", constraints=@ConstraintVariableType(variable="$arr", type="Object[]"))
    public static ErrorDescription forIndexedArray(final HintContext ctx) {
        final List<TreePath> toReplace = new ArrayList<>();
        final boolean[] unsuitable = new boolean[1];
        new CancellableTreePathScanner<Void, Void>() {
            @Override public Void visitArrayAccess(ArrayAccessTree node, Void p) {
                if (MatcherUtilities.matches(ctx, getCurrentPath(), "$arr[$index]")) {
                    toReplace.add(getCurrentPath());
                    return null;
                }
                return super.visitArrayAccess(node, p);
            }
            @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                if (MatcherUtilities.matches(ctx, getCurrentPath(), "$index")) {
                    unsuitable[0] = true;
                    cancel();
                    return null;
                }
                return super.visitIdentifier(node, p);
            }
            @Override protected boolean isCanceled() {
                return ctx.isCanceled() || super.isCanceled();
            }
        }.scan(ctx.getVariables().get("$statement"), null);
        
        if (unsuitable[0]) return null;
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_IteratorToForArray(), new ReplaceIndexedForEachLoop(ctx.getInfo(), ctx.getPath(), ctx.getVariables().get("$arr"), toReplace).toEditorFix());
    }

    // adapted from org.netbeans.modules.java.hints.declarative.conditionapi.Matcher.referencedIn
    private static boolean uses(final HintContext ctx, Collection<? extends TreePath> statements, TreePath var) {
        final Element e = ctx.getInfo().getTrees().getElement(var);
        for (TreePath tp : statements) {
            boolean occurs = Boolean.TRUE.equals(new TreePathScanner<Boolean, Void>() {
                @Override public Boolean scan(Tree tree, Void p) {
                    if (tree == null) {
                        return false;
                    }
                    TreePath currentPath = new TreePath(getCurrentPath(), tree);
                    Element currentElement = ctx.getInfo().getTrees().getElement(currentPath);
                    if (e.equals(currentElement)) {
                        return true;
                    }
                    return super.scan(tree, p);
                }
                @Override public Boolean reduce(Boolean r1, Boolean r2) {
                    if (r1 == null) {
                        return r2;
                    }
                    if (r2 == null) {
                        return r1;
                    }
                    return r1 || r2;
                }
            }.scan(tp, null));
            if (occurs) {
                return true;
            }
        }
        return false;
    }

    private static boolean iterable(HintContext ctx, TreePath collection, TreePath type) {
        TypeMirror collectionType = ctx.getInfo().getTrees().getTypeMirror(collection);
        TypeElement iterable = ctx.getInfo().getElements().getTypeElement("java.lang.Iterable");
        if (collectionType == null || iterable == null) return false;
        TypeMirror iterableType = ctx.getInfo().getTypes().getDeclaredType(iterable, ctx.getInfo().getTypes().getWildcardType(ctx.getInfo().getTrees().getTypeMirror(type), null));
        TypeMirror bogusIterableType = ctx.getInfo().getTypes().getDeclaredType(iterable, ctx.getInfo().getTypes().getNullType());
        return ctx.getInfo().getTypes().isAssignable(collectionType, iterableType) && !ctx.getInfo().getTypes().isAssignable(collectionType, bogusIterableType);
    }
    
    private static final class ReplaceIndexedForEachLoop extends JavaFix {

        private final TreePathHandle arr;
        private final List<TreePathHandle> toReplace;
        
        public ReplaceIndexedForEachLoop(CompilationInfo info, TreePath tp, TreePath arr, List<TreePath> toReplace) {
            super(info, tp);
            this.arr = TreePathHandle.create(arr, info);
            this.toReplace = new ArrayList<>();
            
            for (TreePath tr : toReplace) {
                this.toReplace.add(TreePathHandle.create(tr, info));
            }
        }

        @Override
        protected String getText() {
            return Bundle.FIX_IteratorToFor();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            Tree loop = GeneratorUtilities.get(ctx.getWorkingCopy()).importComments(ctx.getPath().getLeaf(), ctx.getPath().getCompilationUnit());
            TreePath $arr = arr.resolve(ctx.getWorkingCopy());
            
            if ($arr == null) {
                //TODO: why? what can be done?
                return;
            }
            
            TypeMirror arrType = ctx.getWorkingCopy().getTrees().getTypeMirror($arr);
            
            if (arrType.getKind() != TypeKind.ARRAY) {
                //TODO: can happen?
                return ;
            }
            
            String treeName = Utilities.getName($arr.getLeaf());
            String variableName = treeName;
            
            if (variableName != null && variableName.endsWith("s")) variableName = variableName.substring(0, variableName.length() - 1);
            if (variableName == null || variableName.isEmpty()) variableName = "item";
            
            CodeStyle cs = CodeStyle.getDefault(ctx.getWorkingCopy().getFileObject());
            
            if (variableName.equals(treeName) && cs.getLocalVarNamePrefix() == null && cs.getLocalVarNameSuffix() == null) {
                if(Character.isAlphabetic(variableName.charAt(0))) {
                    StringBuilder nameSb = new StringBuilder(variableName);
                    nameSb.setCharAt(0, Character.toUpperCase(nameSb.charAt(0)));
                    nameSb.indexOf("a");
                    variableName = nameSb.toString();
                }
            }
            
            variableName = Utilities.makeNameUnique(ctx.getWorkingCopy(), ctx.getWorkingCopy().getTrees().getScope(ctx.getPath()), variableName, cs.getLocalVarNamePrefix(), cs.getLocalVarNameSuffix());
            
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            EnhancedForLoopTree newLoop = make.EnhancedForLoop(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), variableName, make.Type(((ArrayType) arrType).getComponentType()), null), (ExpressionTree) $arr.getLeaf(), ((ForLoopTree) ctx.getPath().getLeaf()).getStatement());
            
            ctx.getWorkingCopy().rewrite(loop, newLoop);
            
            for (TreePathHandle tr : toReplace) {
                TreePath tp = tr.resolve(ctx.getWorkingCopy());
                
                ctx.getWorkingCopy().rewrite(tp.getLeaf(), make.Identifier(variableName));
            }
        }
        
    }

}
