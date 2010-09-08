/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@Hint(category="rules15", suppressWarnings="ConvertToARM")  //NOI18N
public class ConvertToARM {
    
    private static final String AUTO_CLOSEABLE = "java.lang.AutoCloseable"; //NOI18N
    private static boolean checkAutoCloseable = true;

    @TriggerPatterns(
        {
            @TriggerPattern(value="$CV $var = $init; $stms$; $var.close();"),   //NOI18N
            @TriggerPattern(value="final $CV $var = $init; $stms$; $var.close();"),   //NOI18N
            @TriggerPattern(value="$CV $var = $init; try { $stms$; } catch $catches$ finally {$var.close(); $finstms$;}"),  //NOI18N
            @TriggerPattern(value="final $CV $var = $init; try { $stms$; } catch $catches$ finally {$var.close(); $finstms$;}"),  //NOI18N
            @TriggerPattern(value="$CV $var = null; try { $var = $init; $stms$; } catch $catches$ finally {if ($var != null) $var.close(); $finstms$;}") //NOI18N
        }
    )
    public static List<ErrorDescription> hint(HintContext ctx) {
        Parameters.notNull("ctx", ctx); //NOI18N        
        final Map<String,TreePath> vars = ctx.getVariables();
        final TreePath varVar = vars.get("$var");    //NOI18N
        assert varVar != null;
        final TreePath typeVar = vars.get("$CV");    //NOI18N
        assert typeVar != null;
        final CompilationInfo info = ctx.getInfo();        
        final TypeMirror type = info.getTrees().getTypeMirror(typeVar);
        final List<ErrorDescription> result = new ArrayList<ErrorDescription>(1);
        if (type != null && type.getKind() == TypeKind.DECLARED) {
            final Element autoCloseable = info.getElements().getTypeElement(AUTO_CLOSEABLE);
            if (!checkAutoCloseable || (autoCloseable != null && info.getTypes().isSubtype(type, autoCloseable.asType()))) {
                final Map<String,Collection<? extends TreePath>> multiVars = ctx.getMultiVariables();
                result.add(ErrorDescriptionFactory.forName(
                    ctx,
                    varVar,
                    NbBundle.getMessage(ConvertToARM.class, "TXT_ConvertToARM"),
                    JavaFix.toEditorFix(new ConvertToARMFix(
                        info,
                        ctx.getPath(),
                        varVar,
                        multiVars.get("$stms$"),        //NOI18N
                        multiVars.get("$catches$"),     //NOI18N
                        multiVars.get("$finstms$")))    //NOI18N
            ));
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    private static final class ConvertToARMFix extends JavaFix {
        
        private final Collection<? extends TreePath> statementsPaths;
        private final Collection<? extends TreePath> catchesPaths;
        private final TreePath var;
        private final Collection<? extends TreePath> finStatementsPath;
        
        private ConvertToARMFix(
                final CompilationInfo info,
                final TreePath owner,
                final TreePath var,
                final Collection<? extends TreePath> statements,
                final Collection<? extends TreePath> catches,
                final Collection<? extends TreePath> finStatementsPath) {
            super(info, owner);
            this.statementsPaths = statements;
            this.catchesPaths = catches;
            this.var = var;
            this.finStatementsPath = finStatementsPath;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(ConvertToARM.class, "TXT_ConvertToARM");
        }

        @Override
        protected void performRewrite(
                final WorkingCopy wc,
                final TreePath tp,
                final UpgradeUICallback callback) {
            final TreeMaker tm = wc.getTreeMaker();
            final BlockTree block = tm.Block(ConvertToARMFix.<StatementTree>asList(statementsPaths), false);
            final VariableTree varTree = (VariableTree) var.getLeaf();
            final ModifiersTree oldMods = varTree.getModifiers();
            if (oldMods != null && oldMods.getFlags().contains(Modifier.FINAL)) {
                final ModifiersTree newMods = tm.removeModifiersModifier(oldMods, Modifier.FINAL);
                wc.rewrite(oldMods, newMods);
            }
            final TryTree tryTree = tm.Try(
                    Collections.singletonList(varTree),
                    block,
                    ConvertToARMFix.<CatchTree>asList(catchesPaths),
                    rewriteFinallyBlock(tm,finStatementsPath));
            wc.rewrite(tp.getLeaf(), rewriteTryBlock(
                    tm,
                    ((BlockTree)tp.getLeaf()).getStatements(),
                    (StatementTree)var.getLeaf(),
                    tryTree));
        }
        
        @SuppressWarnings("unchecked")
        private static <R extends Tree> List<? extends R> asList(final Collection<? extends TreePath> data) {
            if (data == null) {
                return Collections.<R>emptyList();
            }
            final List<R> result = new ArrayList<R>(data.size());
            for (TreePath element : data) {
                result.add((R)element.getLeaf());
            }
            return result;
        }
        
        private static BlockTree rewriteTryBlock(
                final TreeMaker tm,
                final List<? extends StatementTree> originalStatements,
                final StatementTree var,
                final TryTree newTry) {
            final List<StatementTree> statements = new ArrayList<StatementTree>(originalStatements.size());
            int state = 0;  //0 - ordinary,1 - replace by try, 2 - remove 
            for (StatementTree statement : originalStatements) {
                if (var == statement) {
                    state = 1;
                    continue;
                } else if (state == 1) {
                    state = statement.getKind() == Kind.TRY ? 0 : 2;
                    statement = newTry;
                } else if (state == 2) {
                    state = 0;
                    continue;
                }
                statements.add(statement);
            }
            return tm.Block(statements, false);
        }
        
        private static BlockTree rewriteFinallyBlock(
                final TreeMaker tm,
                final Collection<? extends TreePath> paths) {
            if (paths == null || paths.isEmpty()) {
                return null;
            }
            final List<StatementTree> statements = new ArrayList<StatementTree>(paths.size());
            for (TreePath stp : paths) {
                statements.add((StatementTree)stp.getLeaf());
            }
            final BlockTree result = tm.Block(statements, false);
            return result;
        }
    }
}
