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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPatterns;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author vita
 */
@Hint(category="logging", suppressWarnings={"NonConstantLogger"}) //NOI18N
public class LoggerNotStaticFinal {

    @TriggerPatterns({
        @TriggerPattern(value="$mods$ java.util.logging.Logger $LOG;"), //NOI18N
        @TriggerPattern(value="$mods$ java.util.logging.Logger $LOG = $init;") //NOI18N
    })
    public static ErrorDescription checkLoggerDeclaration(HintContext ctx) {
        Element e = ctx.getInfo().getTrees().getElement(ctx.getPath());
        if (e.getEnclosingElement().getKind() == ElementKind.CLASS &&
            (!e.getModifiers().contains(Modifier.STATIC) || !e.getModifiers().contains(Modifier.FINAL))
        ) {
            return ErrorDescriptionFactory.forName(
                    ctx,
                    ctx.getPath(),
                    NbBundle.getMessage(LoggerNotStaticFinal.class, "MSG_LoggerNotStaticFinal_checkLoggerDeclaration", e), //NOI18N
                    new LoggerNotStaticFinalFix(NbBundle.getMessage(LoggerNotStaticFinal.class, "MSG_LoggerNotStaticFinal_checkLoggerDeclaration_fix", e), TreePathHandle.create(e, ctx.getInfo())), //NOI18N
                    FixFactory.createSuppressWarningsFix(ctx.getInfo(), ctx.getPath(), "NonConstantLogger") //NOI18N
            );
        } else {
            return null;
        }
    }

    private static final class LoggerNotStaticFinalFix implements Fix {

        private final String text;
        private final TreePathHandle loggerFieldHandle;

        public LoggerNotStaticFinalFix(String text, TreePathHandle loggerFieldHandle) {
            this.text = text;
            this.loggerFieldHandle = loggerFieldHandle;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            JavaSource.forFileObject(loggerFieldHandle.getFileObject()).runModificationTask(new Task<WorkingCopy>() {

                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.RESOLVED);
                    TreePath tp = loggerFieldHandle.resolve(wc);

                    if (tp == null)
                        return ;

                    VariableTree vt = (VariableTree) tp.getLeaf();
                    ModifiersTree mt = vt.getModifiers();
                    Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

                    modifiers.addAll(mt.getFlags());
                    modifiers.add(Modifier.FINAL);
                    modifiers.add(Modifier.STATIC);

                    ModifiersTree newMod = wc.getTreeMaker().Modifiers(modifiers, mt.getAnnotations());

                    wc.rewrite(mt, newMod);
                }
            }).commit();
            return null;
        }

    } // End of FixImpl class
}
