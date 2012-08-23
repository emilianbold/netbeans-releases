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
package org.netbeans.modules.php.twig.editor.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.netbeans.modules.php.twig.editor.completion.TwigCompletionProposal.CompletionRequest;
import org.netbeans.modules.php.twig.editor.completion.TwigElement.Parameter;
import org.netbeans.modules.php.twig.editor.lexer.TwigTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;
import org.netbeans.modules.php.twig.editor.parsing.TwigParserResult;
import org.openide.util.NbBundle;

public class TwigCompletionHandler implements CodeCompletionHandler {

    private static final Set<TwigElement> TAGS = new HashSet<TwigElement>();
    static {
        TAGS.add(TwigElement.Factory.create("autoescape")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endautoescape")); //NOI18N
        TAGS.add(TwigElement.Factory.create("block", "block ${name}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endblock")); //NOI18N
        TAGS.add(TwigElement.Factory.create("do")); //NOI18N
        TAGS.add(TwigElement.Factory.create("embed", "embed \"${template.twig}\"")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endembed")); //NOI18N
        TAGS.add(TwigElement.Factory.create("extends", "extends \"${template.twig}\"")); //NOI18N
        TAGS.add(TwigElement.Factory.create("filter", "filter ${name}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endfilter")); //NOI18N
        TAGS.add(TwigElement.Factory.create("flush")); //NOI18N
        TAGS.add(TwigElement.Factory.create("for", "for ${item} in ${array}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endfor")); //NOI18N
        TAGS.add(TwigElement.Factory.create("from")); //NOI18N
        TAGS.add(TwigElement.Factory.create("if", "if ${true}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("else")); //NOI18N
        TAGS.add(TwigElement.Factory.create("elseif", "elseif ${true}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endif")); //NOI18N
        TAGS.add(TwigElement.Factory.create("import", "import '${page.html}' as ${alias}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("include", "include '${page.html}'")); //NOI18N
        TAGS.add(TwigElement.Factory.create("macro", "macro ${name}()")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endmacro")); //NOI18N
        TAGS.add(TwigElement.Factory.create("raw")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endraw")); //NOI18N
        TAGS.add(TwigElement.Factory.create("sandbox")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endsandbox")); //NOI18N
        TAGS.add(TwigElement.Factory.create("set", "set ${variable}")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endset")); //NOI18N
        TAGS.add(TwigElement.Factory.create("spaceless")); //NOI18N
        TAGS.add(TwigElement.Factory.create("endspaceless")); //NOI18N
        TAGS.add(TwigElement.Factory.create("use", "use \"${page.html}\"")); //NOI18N
    }

    private static final Set<TwigElement> FILTERS = new HashSet<TwigElement>();
    static {
        FILTERS.add(TwigElement.Factory.create("abs")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("capitalize")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("convert_encoding", Arrays.asList(new Parameter[] {new Parameter("'to'"), new Parameter("'from'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("date", Arrays.asList(new Parameter[] {new Parameter("'format'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("date_modify", Arrays.asList(new Parameter[] {new Parameter("'modifier'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("default", Arrays.asList(new Parameter[] {new Parameter("'value'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("escape", Arrays.asList(new Parameter[] {new Parameter("'html'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("format", Arrays.asList(new Parameter[] {new Parameter("var")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("join", Arrays.asList(new Parameter[] {new Parameter("'separator'")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("json_encode", Collections.EMPTY_LIST)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("keys")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("length")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("lower")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("merge", Arrays.asList(new Parameter[] {new Parameter("array")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("nl2br")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("number_format")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("raw")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("replace", Collections.EMPTY_LIST)); //NOI18N
        FILTERS.add(TwigElement.Factory.create("reverse")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("slice", Arrays.asList(new Parameter[] {new Parameter("start"), new Parameter("length")}))); //NOI18N
        FILTERS.add(TwigElement.Factory.create("sort")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("striptags")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("title")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("trim")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("upper")); //NOI18N
        FILTERS.add(TwigElement.Factory.create("url_encode", Collections.EMPTY_LIST)); //NOI18N
    }

    private static final Set<TwigElement> FUNCTIONS = new HashSet<TwigElement>();
    static {
        FUNCTIONS.add(TwigElement.Factory.create("attribute", Arrays.asList(new Parameter[] {new Parameter("object"), new Parameter("method"), new Parameter("arguments", Parameter.Need.OPTIONAL)}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("block", Arrays.asList(new Parameter[] {new Parameter("'name'")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("constant", Arrays.asList(new Parameter[] {new Parameter("'name'")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("cycle", Arrays.asList(new Parameter[] {new Parameter("array"), new Parameter("i")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("date", Arrays.asList(new Parameter[] {new Parameter("'date'"), new Parameter("'timezone'", Parameter.Need.OPTIONAL)}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("dump", Arrays.asList(new Parameter[] {new Parameter("variable", Parameter.Need.OPTIONAL)}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("parent", Collections.EMPTY_LIST)); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("random", Arrays.asList(new Parameter[] {new Parameter("'value'")}))); //NOI18N
        FUNCTIONS.add(TwigElement.Factory.create("range", Arrays.asList(new Parameter[] {new Parameter("start"), new Parameter("end"), new Parameter("step", Parameter.Need.OPTIONAL)}))); //NOI18N
    }

    private static final Set<TwigElement> TESTS = new HashSet<TwigElement>();
    static {
        TESTS.add(TwigElement.Factory.create("constant", Arrays.asList(new Parameter[] {new Parameter("'const'")}))); //NOI18N
        TESTS.add(TwigElement.Factory.create("defined")); //NOI18N
        TESTS.add(TwigElement.Factory.create("divisibleby", Arrays.asList(new Parameter[] {new Parameter("number")}))); //NOI18N
        TESTS.add(TwigElement.Factory.create("empty")); //NOI18N
        TESTS.add(TwigElement.Factory.create("even")); //NOI18N
        TESTS.add(TwigElement.Factory.create("iterable")); //NOI18N
        TESTS.add(TwigElement.Factory.create("null")); //NOI18N
        TESTS.add(TwigElement.Factory.create("odd")); //NOI18N
        TESTS.add(TwigElement.Factory.create("sameas", Arrays.asList(new Parameter[] {new Parameter("variable")}))); //NOI18N
    }

    private static final Set<TwigElement> OPERATORS = new HashSet<TwigElement>();
    static {
        OPERATORS.add(TwigElement.Factory.create("in")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("as")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("is")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("and")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("or")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("not")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("b-and")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("b-or")); //NOI18N
        OPERATORS.add(TwigElement.Factory.create("b-xor")); //NOI18N
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext codeCompletionContext) {
        final List<CompletionProposal> completionProposals = new ArrayList<CompletionProposal>();
        int caretOffset = codeCompletionContext.getCaretOffset();
        TwigParserResult parserResult = (TwigParserResult) codeCompletionContext.getParserResult();
        CompletionRequest request = new CompletionRequest();
        request.prefix = codeCompletionContext.getPrefix();
        request.anchorOffset = caretOffset - getPrefix(parserResult, caretOffset, true).length();
        request.parserResult = parserResult;
        request.context = TwigCompletionContextFinder.find(request.parserResult, caretOffset);
        doCompletion(completionProposals, request);
        return new TwigCompletionResult(completionProposals, false);
    }

    private void doCompletion(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        switch (request.context) {
            case INSTRUCTION:
                completeAll(completionProposals, request);
                break;
            case VARIABLE:
                completeFilters(completionProposals, request);
                completeFunctions(completionProposals, request);
                break;
            case NONE:
                break;
            default:
                completeAll(completionProposals, request);
        }
    }

    private void completeAll(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        completeTags(completionProposals, request);
        completeFilters(completionProposals, request);
        completeFunctions(completionProposals, request);
        completeTests(completionProposals, request);
        completeOperators(completionProposals, request);
    }

    private void completeTags(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement tag : TAGS) {
            if (startsWith(tag.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.TagCompletionProposal(tag, request));
            }
        }
    }

    private void completeFilters(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement parameterizedItem : FILTERS) {
            if (startsWith(parameterizedItem.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.FilterCompletionProposal(parameterizedItem, request));
            }
        }
    }

    private void completeFunctions(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement parameterizedItem : FUNCTIONS) {
            if (startsWith(parameterizedItem.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.FunctionCompletionProposal(parameterizedItem, request));
            }
        }
    }

    private void completeTests(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement test : TESTS) {
            if (startsWith(test.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.TestCompletionProposal(test, request));
            }
        }
    }

    private void completeOperators(final List<CompletionProposal> completionProposals, final CompletionRequest request) {
        for (TwigElement operator : OPERATORS) {
            if (startsWith(operator.getName(), request.prefix)) {
                completionProposals.add(new TwigCompletionProposal.OperatorCompletionProposal(operator, request));
            }
        }
    }

    @Override
    @NbBundle.Messages("MSG_NoDocumentation=Documentation not found.")
    public String document(ParserResult pr, ElementHandle eh) {
        return Bundle.MSG_NoDocumentation();
    }

    @Override
    public ElementHandle resolveLink(String string, ElementHandle eh) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int offset, boolean upToOffset) {
        return PrefixResolver.create(info, offset, upToOffset).resolve();
    }

    @Override
    public QueryType getAutoQuery(JTextComponent jtc, String string) {
        return QueryType.ALL_COMPLETION;
    }

    @Override
    public String resolveTemplateVariable(String string, ParserResult pr, int i, String string1, Map map) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document dcmnt, int i, int i1) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult pr, int i, CompletionProposal cp) {
        return new ParameterInfo(new ArrayList<String>(), 0, 0);
    }

    private static boolean startsWith(String theString, String prefix) {
        return prefix.length() == 0 ? true : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private static class TwigCompletionResult extends DefaultCompletionResult {

        public TwigCompletionResult(List<CompletionProposal> list, boolean truncated) {
            super(list, truncated);
        }

    }

    private static class PrefixResolver {
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
            TokenSequence<TwigTopTokenId> tts = th.tokenSequence(TwigTopTokenId.language());
            if (tts != null) {
                processTopSequence(tts);
            }
        }

        private void processTopSequence(TokenSequence<TwigTopTokenId> tts) {
            tts.move(offset);
            if (tts.moveNext() || tts.movePrevious() ) {
                processSequence(tts.embedded(TwigTokenId.language()));
            }
        }

        private void processSequence(TokenSequence<TwigTokenId> ts) {
            if (ts != null) {
                processValidSequence(ts);
            }
        }

        private void processValidSequence(TokenSequence<TwigTokenId> ts) {
            ts.move(offset);
            if (ts.moveNext() || ts.movePrevious()) {
                processToken(ts);
            }
        }

        private void processToken(TokenSequence<TwigTokenId> ts) {
            if (ts.offset() == offset) {
                ts.movePrevious();
            }
            Token<TwigTokenId> token = ts.token();
            if (token != null) {
                processSelectedToken(ts);
            }
        }

        private void processSelectedToken(TokenSequence<TwigTokenId> ts) {
            TwigTokenId id = ts.token().id();
            if (isValidTokenId(id)) {
                createResult(ts);
            }
        }

        private void createResult(TokenSequence<TwigTokenId> ts) {
            if (upToOffset) {
                String text = ts.token().text().toString();
                result = text.substring(0, offset - ts.offset());
            }
        }

        private static boolean isValidTokenId(TwigTokenId id) {
            return TwigTokenId.T_TWIG_FUNCTION.equals(id) || TwigTokenId.T_TWIG_NAME.equals(id);
        }

    }

}
