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

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.support.OneCheckboxCustomizerProvider;
import org.netbeans.modules.java.hints.perf.SizeEqualsZero.CustomizerProviderImpl;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(category="performance", customizerProvider=CustomizerProviderImpl.class, suppressWarnings="SizeReplaceableByIsEmpty")
public class SizeEqualsZero {

    static final String CHECK_NOT_EQUALS = "check.not.equals";
    static final boolean CHECK_NOT_EQUALS_DEFAULT = true;

    @TriggerPattern(value="$subj.size() == 0")
    public static ErrorDescription sizeEqualsZero(HintContext ctx) {
        return sizeEqualsZeroHint(ctx, false);
    }

    @TriggerPattern(value="$subj.size() != 0")
    public static ErrorDescription sizeNotEqualsZero(HintContext ctx) {
        if (!ctx.getPreferences().getBoolean(CHECK_NOT_EQUALS, CHECK_NOT_EQUALS_DEFAULT)) {
            return null;
        }
        return sizeEqualsZeroHint(ctx, true);
    }

    public static ErrorDescription sizeEqualsZeroHint(HintContext ctx, boolean not) {
        TreePath subj = ctx.getVariables().get("$subj");
        TypeMirror subjType = ctx.getInfo().getTrees().getTypeMirror(subj);

        if (subjType == null || subjType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        
        Element el = ((DeclaredType) subjType).asElement();

        if (el == null || (!el.getKind().isClass() && !el.getKind().isInterface())) {
            return null;
        }

        boolean hasIsEmpty = false;

        for (ExecutableElement method : ElementFilter.methodsIn(el.getEnclosedElements())) {
            if (method.getSimpleName().contentEquals("isEmpty") && method.getParameters().isEmpty() && method.getTypeParameters().isEmpty()) {
                hasIsEmpty = true;
                break;
            }
        }

        if (!hasIsEmpty) {
            return null;
        }

        String fixDisplayName = NbBundle.getMessage(SizeEqualsZero.class, not ? "FIX_UseIsEmptyNeg" : "FIX_UseIsEmpty");
        Fix f = JavaFix.rewriteFix(ctx, fixDisplayName, ctx.getPath(), not ? "!$subj.isEmpty()" : "$subj.isEmpty()");
        String displayName = NbBundle.getMessage(SizeEqualsZero.class, not ? "ERR_SizeEqualsZeroNeg" : "ERR_SizeEqualsZero");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }

    private static final String CONF_CHECKBOX_LABEL = NbBundle.getMessage(SizeEqualsZero.class, "CONF_LBL_SizeEqualsZero");
    private static final String CONF_CHECKBOX_TP = NbBundle.getMessage(SizeEqualsZero.class, "CONF_TP_SizeEqualsZero");

    public static final class CustomizerProviderImpl extends OneCheckboxCustomizerProvider {

        public CustomizerProviderImpl() {
            super(CONF_CHECKBOX_LABEL, CONF_CHECKBOX_TP, CHECK_NOT_EQUALS, CHECK_NOT_EQUALS_DEFAULT);
        }

    }
}
