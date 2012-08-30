/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): Sebastian Hörl
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.php.smarty.editor.TplSyntax;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.openide.util.NbBundle;

/**
 * Tpl parser. Inspired by TwigParser.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplParser extends Parser {

    private TplParserResult result;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sme) throws ParseException {
        result = new TplParserResult(snapshot);

        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        LanguagePath tplMimePath = null;
        for (LanguagePath path : tokenHierarchy.languagePaths()) {
            if (path.mimePath().endsWith("x-tpl-inner")) { //NOI18N
                tplMimePath = path;
                break;
            }
        }

        if (tplMimePath != null) {

            List<TokenSequence<?>> tsList = tokenHierarchy.tokenSequenceList(tplMimePath, 0, Integer.MAX_VALUE);
            List<Function> functionList = new ArrayList<Function>();

            for (TokenSequence<?> ts : tsList) {
                while (ts.moveNext()) {
                    Token<TplTokenId> token = (Token<TplTokenId>) ts.token();

                    /* Parse Smarty Functions */
                    if (token.id() == TplTokenId.FUNCTION) {
                        Function function = new Function();
                        CharSequence functionName = token.text();

                        int startOffset = ts.offset();

                        StringBuilder textBuilder = new StringBuilder();
                        while (ts.moveNext()) {
                            token = (Token<TplTokenId>) ts.token();
                            textBuilder.append(token.text());
                        }
                        int endOffset = startOffset + ((startOffset == ts.offset()) ?  token.length() : ts.offset() - startOffset + token.length());
                        function.name = CharSequenceUtilities.toString(functionName);
                        function.offsetRange = new OffsetRange(startOffset, endOffset);
                        function.text = textBuilder.toString();

                        String name = functionName.toString().startsWith("/") ? functionName.toString().substring(1) : functionName.toString();
                        functionList.add(function);
                    }
                }
            }

            /* Analyse functionList structure */
            Stack<Function> tagStack = new Stack<Function>();
            for (Function tag : functionList) {
                if (TplSyntax.isEndingSmartyCommand(tag.name) || TplSyntax.isElseSmartyCommand(tag.name)) { //NOI18N
                    if (tagStack.empty()) { // End tag, but no more tokens on stack!
                        result.addError(
                                NbBundle.getMessage(TplParser.class, "ERR_Unopened_Tag", TplSyntax.getRelatedBaseCommand(tag.name)), //NOI18N
                                tag.offsetRange.getStart(),
                                tag.offsetRange.getLength());
                    } else if (TplSyntax.isInRelatedCommand(tag.name, tagStack.peek().name)) {
                        if (!TplSyntax.isElseSmartyCommand(tag.name)) {
                            Function beggining = tagStack.pop();
                            result.addTag(beggining.name, beggining.offsetRange.getStart(), beggining.offsetRange.getLength(), beggining.text);
                        }
                    } else {
                        // something wrong lies on the stack!
                        // assume that current token is invalid and let it stay on the stack
                        result.addError(
                                NbBundle.getMessage(TplParser.class, "ERR_Unexpected_Tag", new Object[]{tag.name, TplSyntax.getEndingCommand(tagStack.peek().name)}), //NOI18N
                                tag.offsetRange.getStart(),
                                tag.offsetRange.getLength());
                    }

                } else if (TplSyntax.isBlockCommand(tag.name)) {
                    tagStack.push(tag);
                }

            }

            // All instructions were parsed. Are there any left on the stack?
            if (!tagStack.empty()) {
                // they were never closed!
                while (!tagStack.empty()) {
                    Function function = tagStack.pop();
                    result.addError(
                            NbBundle.getMessage(TplParser.class, "ERR_Unclosed_Tag", function.name), //NOI18N
                            function.offsetRange.getStart(),
                            function.offsetRange.getLength());
                }
            }
        }

    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    /**
     * Factory to create new {@link TplParser}.
     */
    public static class Factory extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> clctn) {
            return new TplParser();
        }
    }

    private class Function {

        private String name = null;
        private OffsetRange offsetRange = new OffsetRange(0, 0);
        private String text;

    }
}
