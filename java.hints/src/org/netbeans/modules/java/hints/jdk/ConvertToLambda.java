/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): Lyle Franklin <lylejfranklin@gmail.com>
 *
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_Javac_canUseLambda", description = "#DESC_Javac_canUseLambda", id = ConvertToLambda.ID, category = "rules15", suppressWarnings="Convert2Lambda")
public class ConvertToLambda {

    public static final String ID = "Javac_canUseLambda";
    public static final Set<String> CODES = new HashSet<String>(Arrays.asList("compiler.note.potential.lambda.found"));

    @TriggerPatterns({
        @TriggerPattern("new $clazz($params$) { $method; }")
    })
    public static ErrorDescription compute(HintContext ctx) {
        ClassTree clazz = ((NewClassTree) ctx.getPath().getLeaf()).getClassBody();
        long start = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), clazz);

        OUTER:
        for (Diagnostic<?> d : ctx.getInfo().getDiagnostics()) {
            if (start != d.getStartPosition()) {
                continue;
            }
            if (!CODES.contains(d.getCode())) {
                continue;
            }

            FixImpl fix = new FixImpl(ctx.getInfo(), ctx.getPath());

            if (cannotBeConverted(ctx.getInfo(), ctx.getPath())) {
                return null;
            }

            return ErrorDescriptionFactory.forTree(ctx, ((NewClassTree) ctx.getPath().getLeaf()).getIdentifier(), d.getMessage(null), fix.toEditorFix());
        }
        return null;
    }

    public static boolean cannotBeConverted(CompilationInfo info, TreePath path) {
        ConvertToLambdaPreconditionChecker preconditionChecker =
                new ConvertToLambdaPreconditionChecker(path, info);

        return !preconditionChecker.passesFatalPreconditions();
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath path) {
            super(info, path);
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(ConvertToLambda.class, "FIX_ConvertToLambda");
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws IOException {

            WorkingCopy copy = ctx.getWorkingCopy();
            copy.toPhase(Phase.RESOLVED);

            TreePath tp = ctx.getPath();

            if (tp.getLeaf().getKind() != Kind.NEW_CLASS) {
                //XXX: warning
                return;
            }

            ConvertToLambdaConverter converter = new ConvertToLambdaConverter(tp, copy);
            converter.performRewrite();
        }
    }
}
