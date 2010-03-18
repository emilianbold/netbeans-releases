/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.completion.doxygensupport;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.spi.editor.completion.CompletionDocumentation;

/**
 *
 * @author Jan Lahoda
 */
public class DoxygenDocumentation {

    private static final Pattern STRIP_STARS = Pattern.compile("^[ \t]*\\*[ \t]*", Pattern.MULTILINE); // NOI18N
    private static final String[] formatItalic = new String[]{"<i>", "</i>"}; // NOI18N

    static String doxygen2HTML(String doxygen) {
        doxygen = doxygen.substring(3, doxygen.length() - 2);
        doxygen = STRIP_STARS.matcher(doxygen).replaceAll("");
        doxygen = doxygen.trim();

        StringBuilder output = new StringBuilder();
        List<String> wordEnd = new LinkedList<String>();
        List<String> lineEnd = new LinkedList<String>();
        List<String> parEnd = new LinkedList<String>();
        String[] nextWordFormat = null;

        for (Token t : lex(doxygen)) {
            //System.out.println("---" + t.id + " " + t.image); // NOI18N
            switch (t.id) {
                case WHITESPACE:
                    output.append(t.image);
                    break;
                case WORD:
                    if (nextWordFormat != null) {
                        output.append(nextWordFormat[0]);
                    }
                    output.append(t.image);
                    for (String s : wordEnd) {
                        output.append(s);
                    }
                    wordEnd.clear();
                    if (nextWordFormat != null) {
                        output.append(nextWordFormat[1]);
                    }
                    nextWordFormat = null;
                    break;
                case LINE_END:
                    for (String s : wordEnd) {//should be empty...
                        output.append(s);
                    }
                    wordEnd.clear();
                    for (String s : lineEnd) {
                        output.append(s);
                    }
                    lineEnd.clear();
                    output.append(t.image);
                    output.append("</p><p>\n"); // NOI18N
                    break;
                case PAR_END:
                    for (String s : wordEnd) {//should be empty...
                        output.append(s);
                    }
                    wordEnd.clear();
                    for (String s : lineEnd) {//should be empty...
                        output.append(s);
                    }
                    for (String s : parEnd) {
                        output.append(s);
                    }
                    lineEnd.clear();
                    parEnd.clear();
                    output.append("</p><p>\n"); // NOI18N
                    break;
                case COMMAND:
                    CommandDescription cd = commands.get(t.image);
                    if (cd == null) {
                        System.err.println("unknown command: " + t.image); // NOI18N
                        break;
                    }
                    output.append(cd.htmlStart);
                    switch (cd.end) {
                        case WORD:
                            wordEnd.add(cd.htmlEnd);
                            break;
                        case LINE:
                            lineEnd.add(cd.htmlEnd);
                            break;
                        case PAR:
                            parEnd.add(cd.htmlEnd);
                            break;
                    }
                    if (t.image.equals("\\param")) { // NOI18N
                        nextWordFormat = formatItalic;
                    }
                    break;
            }
        }

        return "<html><body><p>" + output.toString() + "</p>"; // NOI18N
    }
    private static final Map<String, CommandDescription> commands = new HashMap<String, CommandDescription>();

    static {
        commands.put("\\fn", new CommandDescription(EndsOn.LINE, "<strong>", "</strong></p><p>")); // NOI18N
        commands.put("\\c", new CommandDescription(EndsOn.WORD, "<tt>", "</tt>")); // NOI18N
        commands.put("\\author", new CommandDescription(EndsOn.PAR, "<strong>Author:</strong><br>&nbsp; ", "")); // NOI18N
        commands.put("\\return", new CommandDescription(EndsOn.PAR, "<strong>Returns:</strong><br>&nbsp; ", "")); // NOI18N
        commands.put("\\param", new CommandDescription(EndsOn.PAR, "<strong>Parameter:</strong><br>&nbsp; ", "")); // NOI18N
        commands.put("\\sa", new CommandDescription(EndsOn.PAR, "<strong>See Also:</strong><br>&nbsp; ", "")); // NOI18N
        commands.put("\\brief", new CommandDescription(EndsOn.PAR, "", "")); // NOI18N
        commands.put("\\code", new CommandDescription(EndsOn.NONE, "<pre>", ""));//XXX: does not work properly - the content will still be processed, '<', '>' will not be escaped. // NOI18N
        commands.put("\\endcode", new CommandDescription(EndsOn.NONE, "</pre>", "")); // NOI18N
        commands.put("\\n", new CommandDescription(EndsOn.NONE, "<br/>", "")); // NOI18N
    }

    static final class CommandDescription {
//        final String command;

        final EndsOn end;
        final String htmlStart;
        final String htmlEnd;

        public CommandDescription(/*String command, */EndsOn end, String htmlStart, String htmlEnd) {
//            this.command = command;
            this.end = end;
            this.htmlStart = htmlStart;
            this.htmlEnd = htmlEnd;
        }
    }

    enum EndsOn {

        WORD, LINE, PAR, NONE;
    }

    public static CompletionDocumentation create(CsmObject csmObject) {
        if (!(csmObject instanceof CsmOffsetable)) {
            return null;
        }

        CsmOffsetable csmOffsetable = (CsmOffsetable) csmObject;
        TokenHierarchy<?> h = TokenHierarchy.create(csmOffsetable.getContainingFile().getText(), CppTokenId.languageHeader());
        TokenSequence<CppTokenId> ts = h.tokenSequence(CppTokenId.languageHeader());

        ts.move(csmOffsetable.getStartOffset());

        String docText = null;

        OUTER:
        while (ts.movePrevious()) {
            switch (ts.token().id()) {
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case WHITESPACE:
                case NEW_LINE:
                    continue;
                case DOXYGEN_COMMENT:
                    docText = ts.token().text().toString();
                    break OUTER;
                case SEMICOLON:
                case RBRACKET:
                    break OUTER;
                default:
                    continue;
            }
        }

        if (docText == null) {
            return null;
        }

        String htmlDocText = doxygen2HTML(docText);

        return new CompletionDocumentationImpl(htmlDocText);
    }

    static Collection<Token> lex(String text) {
        Collection<Token> result = new LinkedList<Token>();
        StringBuilder img = new StringBuilder();
        int i = 0;
        boolean wasContent = true;

        OUTER:
        while (i < text.length()) {
            switch (text.charAt(i)) {
                case '\n': // NOI18N
                    if (i < text.length()-1 && (text.charAt(i+1) == '@' || text.charAt(i+1) == '\\')) {
                        result.add(new Token(wasContent ? TokenId.LINE_END : TokenId.PAR_END, "\n")); // NOI18N
                        wasContent = false;
                    }
                    i++;
                    break;
                case ' ': // NOI18N
                case '\t': // NOI18N
                    img.append(text.charAt(i++));
                    while (i < text.length() && (text.charAt(i) == ' ' || text.charAt(i) == '\t')) { // NOI18N
                        img.append(text.charAt(i++));
                    }
                    result.add(new Token(TokenId.WHITESPACE, img.toString()));
                    img = new StringBuilder();
                    break;
                case '@':
                case '\\': // NOI18N
                    img.append('\\');
                    i++;
                    while (i < text.length() && Character.isLetter(text.charAt(i))) {
                        img.append(text.charAt(i++));
                    }
                    result.add(new Token(TokenId.COMMAND, img.toString()));
                    img = new StringBuilder();
                    wasContent = true;
                    break;
                default:
                    img.append(text.charAt(i++));
                    while (i < text.length() && (text.charAt(i) != ' ' && text.charAt(i) != '\t' && text.charAt(i) != '\n' && text.charAt(i) != '\\')) { // NOI18N
                        img.append(text.charAt(i++));
                    }
                    result.add(new Token(TokenId.WORD, img.toString()));
                    img = new StringBuilder();
                    wasContent = true;
                    break;
            }
        }

        return result;
    }

    static class Token {

        final TokenId id;
        final String image;

        public Token(TokenId id, String image) {
            this.id = id;
            this.image = image;
        }

        @Override
        public String toString() {
            return id + ":" + image; // NOI18N
        }
    }

    enum TokenId {

        COMMAND, WHITESPACE, PAR_END, LINE_END, WORD//, LINE_START;
    }

    private static final class CompletionDocumentationImpl implements CompletionDocumentation {

        private final String text;

        public CompletionDocumentationImpl(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public URL getURL() {
            return null;
        }

        public CompletionDocumentation resolveLink(String link) {
            return null;
        }

        public Action getGotoSourceAction() {
            return null;
        }
    }
}
