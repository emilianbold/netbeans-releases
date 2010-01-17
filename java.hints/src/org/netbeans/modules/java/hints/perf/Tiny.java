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

package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.MatcherUtilities;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.support.OneCheckboxCustomizerProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class Tiny {

    static final String SC_IGNORE_SUBSTRING = "ignore.substring";
    static final boolean SC_IGNORE_SUBSTRING_DEFAULT = true;
    
    @Hint(category="performance", customizerProvider=StringConstructorCustomizerProviderImpl.class)
    @TriggerPattern(value="new java.lang.String($original)",
                    constraints=@Constraint(variable="$original", type="java.lang.String"))
    public static ErrorDescription stringConstructor(HintContext ctx) {
        TreePath original = ctx.getVariables().get("$original");

        if (ctx.getPreferences().getBoolean(SC_IGNORE_SUBSTRING, SC_IGNORE_SUBSTRING_DEFAULT)) {
            if (   MatcherUtilities.matches(ctx, original, "$str1.substring($s)", true)
                || MatcherUtilities.matches(ctx, original, "$str2.substring($s, $e)", true)) {
                TreePath str = ctx.getVariables().get("$str1") != null ? ctx.getVariables().get("$str1") : ctx.getVariables().get("$str2");

                assert str != null;

                TypeMirror type = ctx.getInfo().getTrees().getTypeMirror(str);

                if (type != null && type.getKind() == TypeKind.DECLARED) {
                    TypeElement te = (TypeElement) ((DeclaredType) type).asElement();

                    if (te.getQualifiedName().contentEquals("java.lang.String")) {
                        return null;
                    }
                }
            }
        }

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_StringConstructor");
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$original");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_StringConstructor");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }

    private static final String CONF_SC_CHECKBOX_LABEL = NbBundle.getMessage(SizeEqualsZero.class, "CONF_LBL_SizeEqualsZero");
    private static final String CONF_SC_CHECKBOX_TP = NbBundle.getMessage(SizeEqualsZero.class, "CONF_TP_SizeEqualsZero");

    public static final class StringConstructorCustomizerProviderImpl extends OneCheckboxCustomizerProvider {

        public StringConstructorCustomizerProviderImpl() {
            super(CONF_SC_CHECKBOX_LABEL, CONF_SC_CHECKBOX_TP, SC_IGNORE_SUBSTRING, SC_IGNORE_SUBSTRING_DEFAULT);
        }

    }


    @Hint(category="performance", enabled=false)
    @TriggerPattern(value="$string.equals(\"\")",
                    constraints=@Constraint(variable="$string", type="java.lang.String"))
    public static ErrorDescription stringEqualsEmpty(HintContext ctx) {
        Fix f;
        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_6) >= 0) {
            String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_StringEqualsEmpty16");
            f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$string.isEmpty()");
        } else {
            boolean not = ctx.getPath().getParentPath().getLeaf().getKind() == Kind.LOGICAL_COMPLEMENT;
            String fixDisplayName = NbBundle.getMessage(Tiny.class, not ? "FIX_StringEqualsEmptyNeg" : "FIX_StringEqualsEmpty");
            f = JavaFix.rewriteFix(ctx, fixDisplayName, not ? ctx.getPath().getParentPath() : ctx.getPath(), not ? "$string.length() != 0" : "$string.length() == 0");
        }
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_StringEqualsEmpty");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }
}
