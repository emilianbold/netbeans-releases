/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.latte.completion.LatteCompletionProposal.CompletionRequest;
import org.netbeans.modules.php.latte.completion.LatteElement.HelperParameter;
import org.netbeans.modules.php.latte.completion.LatteElement.Parameter;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.php.latte.parser.LatteParser.LatteParserResult;
import org.netbeans.modules.php.latte.utils.LatteLexerUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteCompletionHandler implements CodeCompletionHandler {
    private static final Collection<Character> AUTOPOPUP_STOP_CHARS = new TreeSet<>(
            Arrays.asList('=', ';', '+', '-', '*', '/', '%', '(', ')', '[', ']', '{', '}', '?', ' ', '\t', '\n'));
    static final Set<LatteElement> MACROS = new HashSet<>();
    static {
        MACROS.add(LatteElement.Factory.createMacro("link", "Presenter:action", "link ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("plink", "Presenter:action", "plink ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("continueIf", "true", "continueIf ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("breakIf", "true", "breakif ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("if", "true", "if ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("else")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("elseif", "true", "elseif ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("ifset", "$var", "ifset ${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("ifset", "#block", "ifset #${block}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("elseifset", "$var", "elseifset ${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("ifCurrent", "Presenter:action", "ifCurrent ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("for", "init; cond; exec", "for ${init}; ${cond}; ${exec}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("foreach", "$array as $item", "foreach ${array} as ${item}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("while", "true", "while ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("include", "'file.latte'", "include '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("include", "#block", "include #{block}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("extends", "'file.latte'", "extends '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("layout", "'file.latte'", "layout '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("control", "name", "control ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("cache", "$key", "cache ${key}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("snippet", "$name", "snippet ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("block", "#name", "block #${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("define", "#name", "define #${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("includeblock", "'file.latte'", "includeblock '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("contentType", "$type", "contentType ${type}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("status", "$code", "status ${code}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("capture", "$var", "capture ${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("assign")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("default", "$name = $value", "default ${name} = ${value}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("var", "$name = $value", "var ${name} = ${value}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("dump", "$var", "dump ${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("syntax", "mode", "syntax ${mode}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("use", "Class", "use ${Class}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("form", "$name", "form ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("label", "$name", "label ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("input", "$name", "input ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.createMacro("debugbreak", "$cond", "debugbreak ${cond}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("l")); //NOI18N
        MACROS.add(LatteElement.Factory.create("r")); //NOI18N
        MACROS.add(LatteElement.Factory.create("first")); //NOI18N
        MACROS.add(LatteElement.Factory.create("last")); //NOI18N
        MACROS.add(LatteElement.Factory.create("sep")); //NOI18N
        MACROS.add(LatteElement.Factory.create("_")); //NOI18N
        MACROS.add(LatteElement.Factory.create("!")); //NOI18N
        MACROS.add(LatteElement.Factory.create("!_")); //NOI18N
        MACROS.add(LatteElement.Factory.create("=")); //NOI18N
        MACROS.add(LatteElement.Factory.create("#")); //NOI18N
    }

    static final Set<LatteElement> HELPERS = new HashSet<>();
    static {
        HELPERS.add(LatteElement.Factory.create("truncate", Arrays.asList(new Parameter[] {new HelperParameter("length"), new HelperParameter("append", "'…'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("substr", Arrays.asList(new Parameter[] {new HelperParameter("offset"), new HelperParameter("length", "stringLegth")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("trim", Arrays.asList(new Parameter[] {new HelperParameter("charlist", "' \\t\\n\\r\\0\\x0B\\xC2\\xA0'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("striptags")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("strip")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("webalize", Arrays.asList(new Parameter[] {new HelperParameter("charlist", "NULL"), new HelperParameter("lower", "true")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("toAscii")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("indent", Arrays.asList(new Parameter[] {new HelperParameter("level", "1"), new HelperParameter("char", "'\\t'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("replace", Arrays.asList(new Parameter[] {new HelperParameter("search"), new HelperParameter("replace", "''")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("replaceRE", Arrays.asList(new Parameter[] {new HelperParameter("pattern"), new HelperParameter("replace", "''")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("padLeft", Arrays.asList(new Parameter[] {new HelperParameter("length"), new HelperParameter("pad", "' '")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("padRight", Arrays.asList(new Parameter[] {new HelperParameter("length"), new HelperParameter("pad", "' '")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("repeat", Arrays.asList(new Parameter[] {new HelperParameter("count")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("implode", Arrays.asList(new Parameter[] {new HelperParameter("glue", "''")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("nl2br")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("lower")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("upper")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("firstLower")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("capitalize")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("date", Arrays.asList(new Parameter[] {new HelperParameter("'format'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("number", Arrays.asList(new Parameter[] {new HelperParameter("decimals", "0"), new HelperParameter("decPoint", "'.'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("bytes", Arrays.asList(new Parameter[] {new HelperParameter("precision", "2")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("dataStream", Arrays.asList(new Parameter[] {new HelperParameter("mimetype", "NULL")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("url")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("length")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("null")); //NOI18N
    }

    static final Set<LatteElement> KEYWORDS = new HashSet<>();
    static {
        KEYWORDS.add(LatteElement.Factory.create("true")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("false")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("null")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("and")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("or")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("xor")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("clone")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("new")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("instanceof")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("return")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("continue")); //NOI18N
        KEYWORDS.add(LatteElement.Factory.create("break")); //NOI18N
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        final List<CompletionProposal> completionProposals = new ArrayList<>();
        ParserResult parserResult = context.getParserResult();
        if (parserResult instanceof LatteParserResult) {
            LatteParserResult latteParserResult = (LatteParserResult) parserResult;
            CompletionRequest request = new CompletionRequest();
            int caretOffset = context.getCaretOffset();
            request.prefix = context.getPrefix();
            String properPrefix = getPrefix(latteParserResult, caretOffset, true);
            request.anchorOffset = caretOffset - (properPrefix == null ? 0 : properPrefix.length());
            request.parserResult = latteParserResult;
            LatteCompletionContext completionContext = LatteCompletionContextFinder.find(request.parserResult, caretOffset);
            completionContext.complete(completionProposals, request);
        }
        return new DefaultCompletionResult(completionProposals, false);
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        String result = null;
        if (element instanceof LatteElement) {
            result = ((LatteElement) element).getDocumentationText();
        }
        return result;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return PrefixResolver.create(info, caretOffset, upToOffset).resolve();
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        QueryType result = QueryType.ALL_COMPLETION;
        if (typedText.length() == 0) {
            result = QueryType.NONE;
        } else {
            char lastChar = typedText.charAt(typedText.length() - 1);
            if (AUTOPOPUP_STOP_CHARS.contains(Character.valueOf(lastChar))) {
                result = QueryType.STOP;
            } else {
                Document document = component.getDocument();
                int offset = component.getCaretPosition();
                TokenSequence<? extends LatteMarkupTokenId> ts = LatteLexerUtils.getLatteMarkupTokenSequence(document, offset);
                if (ts == null) {
                    result = QueryType.STOP;
                }
            }
        }
        return result;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    private static final class PrefixResolver {
        private final ParserResult info;
        private final int offset;
        private final boolean upToOffset;
        private String result = "";

        static PrefixResolver create(ParserResult info, int offset, boolean upToOffset) {
            return new PrefixResolver(info, offset, upToOffset);
        }

        private PrefixResolver(ParserResult info, int offset, boolean upToOffset) {
            this.info = info;
            this.offset = offset;
            this.upToOffset = upToOffset;
        }

        String resolve() {
            TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
            if (th != null) {
                processHierarchy(th);
            }
            return result;
        }

        private void processHierarchy(TokenHierarchy<?> th) {
            TokenSequence<LatteTopTokenId> tts = th.tokenSequence(LatteTopTokenId.language());
            if (tts != null) {
                processTopSequence(tts);
            }
        }

        private void processTopSequence(TokenSequence<LatteTopTokenId> tts) {
            tts.move(offset);
            if (tts.moveNext()) {
                TokenSequence<LatteMarkupTokenId> embedded = tts.embedded(LatteMarkupTokenId.language());
                if (embedded == null && tts.movePrevious()) {
                    embedded = tts.embedded(LatteMarkupTokenId.language());
                }
                processSequence(embedded);
            }
        }

        private void processSequence(TokenSequence<LatteMarkupTokenId> ts) {
            if (ts != null) {
                processValidSequence(ts);
            }
        }

        private void processValidSequence(TokenSequence<LatteMarkupTokenId> ts) {
            ts.move(offset);
            if (ts.moveNext() || ts.movePrevious()) {
                processToken(ts);
            }
        }

        private void processToken(TokenSequence<LatteMarkupTokenId> ts) {
            if (ts.offset() == offset) {
                ts.movePrevious();
            }
            Token<LatteMarkupTokenId> token = ts.token();
            if (token != null) {
                processSelectedToken(ts);
            }
        }

        private void processSelectedToken(TokenSequence<LatteMarkupTokenId> ts) {
            LatteMarkupTokenId id = ts.token().id();
            if (isValidTokenId(id)) {
                createResult(ts);
            }
        }

        private void createResult(TokenSequence<LatteMarkupTokenId> ts) {
            if (upToOffset) {
                String text = ts.token().text().toString();
                result = text.substring(0, offset - ts.offset());
            }
        }

        private static boolean isValidTokenId(LatteMarkupTokenId id) {
            return LatteMarkupTokenId.T_SYMBOL.equals(id) || LatteMarkupTokenId.T_MACRO_START.equals(id) || LatteMarkupTokenId.T_MACRO_END.equals(id);
        }

    }

}