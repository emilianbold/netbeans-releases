/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
@Hint(category = "imports")
public class OrganizeImports {

    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    public static List<ErrorDescription> checkImports(final HintContext context) {
        Source source = context.getInfo().getSnapshot().getSource();
        final List<ErrorDescription> ret = new ArrayList<ErrorDescription>();
        final OrganizeImportsFix fix = new OrganizeImportsFix();
        ModificationResult result = null;
        try {
            result = ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                    copy.toPhase(Phase.RESOLVED);
                    Trees trees = copy.getTrees();
                    CompilationUnitTree cu = copy.getCompilationUnit();
                    Set<Element> toImport = getUsedElements(cu, trees);
                    if (!toImport.isEmpty() && !cu.getImports().isEmpty()) {
                        CompilationUnitTree cut = copy.getTreeMaker().CompilationUnit(cu.getPackageAnnotations(), cu.getPackageName(), Collections.<ImportTree>emptyList(), cu.getTypeDecls(), cu.getSourceFile());
                        CompilationUnitTree ncu = GeneratorUtilities.get(copy).addImports(cut, toImport);
                        copy.rewrite(cu, ncu);
                        ret.add(ErrorDescriptionFactory.forTree(context, cu.getImports().get(0), NbBundle.getMessage(OrganizeImports.class, "MSG_OragnizeImports"), fix)); //NOI18N
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        List<? extends Difference> diffs = result != null ? result.getDifferences(source.getFileObject()) : null;
        if (diffs != null && !diffs.isEmpty()) {
            fix.result = result;
            return ret;
        }
        return null;
    }

    private static Set<Element> getUsedElements(final CompilationUnitTree cut, final Trees trees) {
        final Set<Element> ret = new HashSet<Element>();
        new TreePathScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element element = trees.getElement(getCurrentPath());
                if (element != null && (element.getKind().isClass() || element.getKind().isInterface())) {
                    ret.add(element);
                }
                return null;
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                return scan(node.getTypeDecls(), p);
            }
        }.scan(cut, null);
        return ret;
    }


    private static final class OrganizeImportsFix implements Fix {

        private ModificationResult result = null;

        @Override
        public String getText() {
            return NbBundle.getMessage(OrganizeImports.class, "FIX_OrganizeImports"); //NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {
            if (result != null) {
                result.commit();
            }
            return null;
        }
    }
}
