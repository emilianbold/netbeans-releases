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

package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata.Kind;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class Tiny {

    @Hint(category="general", hintKind=Kind.SUGGESTION, severity=HintSeverity.CURRENT_LINE_WARNING)
    @TriggerPattern(value="$this.equals($other)",
                    constraints={
                        @Constraint(variable="$this", type="java.lang.Object"),
                        @Constraint(variable="$other", type="java.lang.Object")
                    })
    public static ErrorDescription flipEquals(HintContext ctx) {
        int caret = CaretAwareJavaSourceTaskFactory.getLastPosition(ctx.getInfo().getFileObject());
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        ExpressionTree select = mit.getMethodSelect();
        int selectStart;
        int selectEnd;

        switch (select.getKind()) {
            case MEMBER_SELECT:
                int[] span = ctx.getInfo().getTreeUtilities().findNameSpan((MemberSelectTree) select);

                if (span == null) {
                    return null;
                }

                selectStart = span[0];
                selectEnd = span[1];
                break;
            case IDENTIFIER:
                selectStart = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), select);
                selectEnd   = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getInfo().getCompilationUnit(), select);
                break;
            default:
                Logger.getLogger(Tiny.class.getName()).log(Level.FINE, "flipEquals: unexpected method select kind: {0}", select.getKind());
                return null;
        }

        if (selectStart > caret || selectEnd < caret) {
            return null;
        }

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_flipEquals");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_flipEquals");
        String fixPattern;

        if (ctx.getVariables().containsKey("$this")) {
            fixPattern = "$other.equals($this)";
        } else {
            fixPattern = "$other.equals(this)";
        }

        Fix fix = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), fixPattern);
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fix);
    }
}
