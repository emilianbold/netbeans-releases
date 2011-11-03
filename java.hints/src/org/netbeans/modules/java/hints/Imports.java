/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.semantic.SemanticHighlighter;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * Implementation of all hints for import statements
 *
 * @author phrebejk
 * @author Max Sauer
 */
public class Imports {
  
    private static final String DEFAULT_PACKAGE = "java.lang"; // NOI18N
    private String IMPORTS_ID = "Imports_"; // NOI18N
    

    @Hint(category="imports", id="Imports_STAR", enabled=false)
    @TriggerTreeKind(Kind.IMPORT)
    public static ErrorDescription starImport(HintContext ctx) {
        ImportTree it = (ImportTree) ctx.getPath().getLeaf();

        if (it.isStatic() || !(it.getQualifiedIdentifier() instanceof MemberSelectTree)) {
            return null; // XXX
        }

        MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();

        if (!"*".equals(ms.getIdentifier().toString())) return null;

        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), NbBundle.getMessage(Imports.class, "DN_Imports_STAR"));
    }

    @Hint(category="imports", id="Imports_DEFAULT_PACKAGE")
    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> defaultImport(HintContext ctx) {
        return importMultiHint(ctx, ImportHintKind.DEFAULT_PACKAGE, getAllImportsOfKind(ctx.getInfo(), ImportHintKind.DEFAULT_PACKAGE));
    }
    
    @Hint(category="imports", id="Imports_UNUSED")
    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> unusedImport(HintContext ctx) throws IOException {
        return importMultiHint(ctx, ImportHintKind.UNUSED, SemanticHighlighter.computeUnusedImports(ctx.getInfo()));
    }

    @Hint(category="imports", id="Imports_SAME_PACKAGE")
    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> samePackage(HintContext ctx) throws IOException {
        return importMultiHint(ctx, ImportHintKind.SAME_PACKAGE, getAllImportsOfKind(ctx.getInfo(), ImportHintKind.SAME_PACKAGE));
    }

    private static List<ErrorDescription> importMultiHint(HintContext ctx, ImportHintKind kind, List<TreePathHandle> violatingImports) {
        // Has to be done in order to provide 'remove all' fix
        Fix allFix = null;
        if (ctx.isBulkMode() && !violatingImports.isEmpty()) {
            Fix af = JavaFix.toEditorFix(new ImportsFix(violatingImports, kind));
            return Collections.singletonList(ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), NbBundle.getMessage(Imports.class, "DN_Imports_" + kind.toString()), af));
        }
        if (violatingImports.size() > 1) {
            allFix = JavaFix.toEditorFix(new ImportsFix(violatingImports, kind));
        }
        List<ErrorDescription> result = new ArrayList<ErrorDescription>(violatingImports.size());
        for (TreePathHandle it : violatingImports) {
            TreePath resolvedIt = it.resolve(ctx.getInfo());
            if (resolvedIt == null) continue; //#204580
            List<Fix> fixes = new ArrayList<Fix>();
            fixes.add(JavaFix.toEditorFix(new ImportsFix(Collections.singletonList(it), kind)));
            if (allFix != null) {
                fixes.add(allFix);
            }
            result.add(ErrorDescriptionFactory.forTree(ctx, resolvedIt, NbBundle.getMessage(Imports.class, "DN_Imports_" + kind.toString()), fixes.toArray(new Fix[0])));
        }

        return result;
    }

    @Hint(category="imports", id="Imports_EXCLUDED")
    @TriggerTreeKind(Kind.IMPORT)
    public static ErrorDescription exlucded(HintContext ctx) throws IOException {
        ImportTree it = (ImportTree) ctx.getPath().getLeaf();

        if (it.isStatic() || !(it.getQualifiedIdentifier() instanceof MemberSelectTree)) {
            return null; // XXX
        }

        MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();
        String pkg = ms.getExpression().toString();
        String klass = ms.getIdentifier().toString();
        String exp = pkg + "." + (!klass.equals("*") ? klass : ""); //NOI18N
        if (Utilities.isExcluded(exp)) {
            return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), NbBundle.getMessage(Imports.class, "DN_Imports_EXCLUDED"));
        }

        return null;
    }

    private static List<TreePathHandle> getAllImportsOfKind(CompilationInfo ci, ImportHintKind kind) {
        //allow only default and samepackage
        assert (kind == ImportHintKind.DEFAULT_PACKAGE || kind == ImportHintKind.SAME_PACKAGE);

        CompilationUnitTree cut = ci.getCompilationUnit();
        TreePath topLevel = new TreePath(cut);
        List<TreePathHandle> result = new ArrayList<TreePathHandle>(3);

        List<? extends ImportTree> imports = cut.getImports();
        for (ImportTree it : imports) {
            if (it.isStatic()) {
                continue; // XXX
            }
            if (it.getQualifiedIdentifier() instanceof MemberSelectTree) {
                MemberSelectTree ms = (MemberSelectTree) it.getQualifiedIdentifier();
                if (kind == ImportHintKind.DEFAULT_PACKAGE) {
                    if (ms.getExpression().toString().equals(DEFAULT_PACKAGE)) {
                        result.add(TreePathHandle.create(new TreePath(topLevel, it), ci));
                    }
                }
                if (kind == ImportHintKind.SAME_PACKAGE) {
                    ExpressionTree packageName = cut.getPackageName();
                    if (packageName != null &&
                        ms.getExpression().toString().equals(packageName.toString())) {
                        result.add(TreePathHandle.create(new TreePath(topLevel, it), ci));
                    }
                }
            }
        }
        return result;
    }

    // Private methods ---------------------------------------------------------
    
    private static enum ImportHintKind {

        DELEGATE,
        UNUSED,
        DUPLICATE,
        SAME_PACKAGE,
        DEFAULT_PACKAGE,
        EXCLUDED,
        STAR;

        boolean defaultOn() {

            switch (this) {
                case DELEGATE:
                case EXCLUDED:
                case SAME_PACKAGE:
                case DEFAULT_PACKAGE:
                case UNUSED:
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class ImportsFix extends JavaFix {

        List<TreePathHandle> tphList;
        ImportHintKind ihk;
        
        public ImportsFix(List<TreePathHandle> tphList, ImportHintKind ihk) {
            super(tphList.get(0));
            this.tphList = tphList;
            this.ihk = ihk;
        }
        
        public String getText() {
            if ( tphList.size() == 1 ) {
                return NbBundle.getMessage(Imports.class, "LBL_Imports_Fix_One_" + ihk.toString()); // NOI18N
            }
            else {
                return NbBundle.getMessage(Imports.class, "LBL_Imports_Fix_All_" + ihk.toString()); // NOI18N
            }
        }

        @Override
        protected void performRewrite(WorkingCopy copy, TreePath tp, boolean canShowUI) {
            CompilationUnitTree cut = copy.getCompilationUnit();
            
            TreeMaker make = copy.getTreeMaker();
            
            CompilationUnitTree newCut = cut;
            for (TreePathHandle tph : tphList) {
                TreePath path = tph.resolve(copy);
                if ( path != null && path.getLeaf() instanceof ImportTree) {
                     newCut = make.removeCompUnitImport(newCut, (ImportTree)path.getLeaf());
                }
            }
            copy.rewrite(cut, newCut);
                        
        }
                
    }

    
    
}
