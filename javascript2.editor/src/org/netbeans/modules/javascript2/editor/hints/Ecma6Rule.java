/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.hints;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.JsPreferences;
import static org.netbeans.modules.javascript2.editor.hints.JsAstRule.JS_OTHER_HINTS;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

public class Ecma6Rule extends EcmaLevelRule {

    private static final List<JsTokenId> ECMA6LIST = Arrays.asList(
            JsTokenId.KEYWORD_CLASS,
            JsTokenId.KEYWORD_CONST,
            JsTokenId.KEYWORD_EXTENDS,
            JsTokenId.KEYWORD_EXPORT,
            JsTokenId.KEYWORD_IMPORT,
            JsTokenId.KEYWORD_SUPER,
            JsTokenId.KEYWORD_YIELD,
            JsTokenId.OPERATOR_ARROW,
            JsTokenId.TEMPLATE_BEGIN,
            JsTokenId.TEMPLATE,
            JsTokenId.TEMPLATE_END,
            JsTokenId.TEMPLATE_EXP_BEGIN,
            JsTokenId.TEMPLATE_EXP_END,
            JsTokenId.RESERVED_AWAIT
            );

    @Override
    void computeHints(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        if (JsPreferences.isPreECMAScript6(FileOwnerQuery.getOwner(context.getJsParserResult().getSnapshot().getSource().getFileObject()))) {
            Snapshot snapshot = context.getJsParserResult().getSnapshot();
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot, context.lexOffset);
            OffsetRange returnOffsetRange;
            if (ts != null) {
                while (ts.moveNext()) {
                    Token<? extends JsTokenId> token = LexUtilities.findNextIncluding(ts, ECMA6LIST);
                    if (token != null && token.length() >= 1) {
                        returnOffsetRange = new OffsetRange(ts.offset(), ts.offset() + token.length());
                        addHint(context, hints, offset, JS_OTHER_HINTS, returnOffsetRange);
                    }
                }
            }
        }
    }

    private void addHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, String name, OffsetRange range) throws BadLocationException {
        hints.add(new Hint(this, Bundle.Ecma6Desc(),
                context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                ModelUtils.documentOffsetRange(context.getJsParserResult(),
                        range.getStart(), range.getEnd()), null, 600));
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "ecma6.hint";
    }

    @NbBundle.Messages("Ecma6Desc=ECMA6 feature used in pre-ECMA6 source")
    @Override
    public String getDescription() {
        return Bundle.Ecma6Desc();
    }

    @NbBundle.Messages("Ecma6DisplayName=ECMA6 feature used")
    @Override
    public String getDisplayName() {
        return Bundle.Ecma6DisplayName();
    }

}
