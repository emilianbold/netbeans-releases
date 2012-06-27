/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.nashorn.ir.Block;
import com.oracle.nashorn.ir.FunctionNode;
import com.oracle.nashorn.ir.LiteralNode;
import com.oracle.nashorn.ir.Node;
import com.oracle.nashorn.ir.ObjectNode;
import com.oracle.nashorn.ir.PropertyKeyNode;
import com.oracle.nashorn.ir.PropertyNode;
import com.oracle.nashorn.parser.Token;
import com.oracle.nashorn.parser.TokenType;
import com.oracle.nashorn.runtime.Source;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Hejl
 */
public class JsonParser extends SanitizingParser {

    public JsonParser() {
        super();
    }

    @Override
    public String getDefaultScriptName() {
        return "json.json"; // NOI18N
    }
    
    protected com.oracle.nashorn.ir.FunctionNode parseSource(Snapshot snapshot, String name, String text, JsErrorManager errorManager) throws Exception {
        com.oracle.nashorn.runtime.Source source = new com.oracle.nashorn.runtime.Source(name, text);
        
        // dummy compiler setup
        com.oracle.nashorn.runtime.options.Options options = new com.oracle.nashorn.runtime.options.Options("nashorn");
        options.process(new String[] {
            "--parse-only=true", // NOI18N
            "--empty-statements=true", // NOI18N
            "--debug-lines=false"}); // NOI18N

        errorManager.setLimit(0);
        com.oracle.nashorn.runtime.Context contextN = new com.oracle.nashorn.runtime.Context(options, errorManager);
        com.oracle.nashorn.runtime.Context.setContext(contextN);
        com.oracle.nashorn.codegen.Compiler compiler = new com.oracle.nashorn.codegen.Compiler(source, contextN);
        
        com.oracle.nashorn.ir.FunctionNode node = null;
        TokenSequence<? extends JsTokenId> ts = snapshot.getTokenHierarchy().tokenSequence(JsTokenId.jsonLanguage());
        if (ts != null) {
            ObjectNode object = parseObject(source, ts, errorManager);
            node = new FunctionNode(source, 0, text.length(), compiler, null, null, "runScript"); // NOI18N
            node.setKind(FunctionNode.Kind.SCRIPT);
            node.setStatements(Collections.<Node>singletonList(object));
        }
        return node;
    }
    
    private ObjectNode parseObject(Source source, TokenSequence<? extends JsTokenId> ts,
            JsErrorManager errorManager) {

        int start = expect(source, JsTokenId.BRACKET_LEFT_CURLY, ts, errorManager);
        List<Node> members = Collections.emptyList();
        if (nextToken(ts)) {
            JsTokenId id = ts.token().id();
            ts.movePrevious();
            if (id != JsTokenId.BRACKET_RIGHT_CURLY) {
                members = parseMembers(source, ts, errorManager);
            }
        }
        
        int end = expect(source, JsTokenId.BRACKET_RIGHT_CURLY, ts, errorManager);
        // this is dummy block needed to prevent NPE in visitors
        Block context = new Block(source,
                Token.toDesc(TokenType.LBRACE, start, 1),
                start,
                null,
                null);
        return new ObjectNode(source,
                Token.toDesc(TokenType.LBRACE, start, 1),
                end + 1,
                context,
                members);
    }
    
    private List<Node> parseMembers(Source source, TokenSequence<? extends JsTokenId> ts,
            JsErrorManager errorManager) {
        
        List<Node> ret = new ArrayList<Node>();
        ret.add(parsePair(source, ts, errorManager));
        if (nextToken(ts)) {
            if (ts.token().id() == JsTokenId.OPERATOR_COMMA) {
                ret.addAll(parseMembers(source, ts, errorManager));
            } else {
                ts.movePrevious();
            }
        }
        return ret;
    }
    
    private PropertyNode parsePair(Source source, TokenSequence<? extends JsTokenId> ts,
            JsErrorManager errorManager) {
        int start = expect(source, JsTokenId.STRING_BEGIN, ts, errorManager);
        String val = "";
        if (nextToken(ts)) {
            if (ts.token().id() == JsTokenId.STRING) {
                val = ts.token().text().toString();
            } else {
                ts.movePrevious();
            }
        }
        int end = expect(source, JsTokenId.STRING_END, ts, errorManager);
        
        PropertyKeyNode key = LiteralNode.newInstance(source, Token.toDesc(TokenType.STRING, start + 1, val.length()), end, val);
        expect(source, JsTokenId.OPERATOR_COLON, ts, errorManager);
        
        Node value = parseValue(source, ts, errorManager);
        return new PropertyNode(source,
                Token.toDesc(TokenType.STRING, start + 1, val.length()),
                value != null ? value.getFinish() : -1,
                key,
                value);
    }
    
    private Node parseValue(Source source, TokenSequence<? extends JsTokenId> ts,
                JsErrorManager errorManager) {
        if (!nextToken(ts)) {
            errorManager.error(source.getName() + ":" + "0:0:" + ts.offset()
                    + ": Expected value but eof found");
            return null;
        }
        JsTokenId id = ts.token().id();
        
        if (id == JsTokenId.KEYWORD_TRUE) {
            return LiteralNode.newInstance(source,
                    Token.toDesc(TokenType.TRUE, ts.offset(), id.fixedText().length()),
                    ts.offset() + id.fixedText().length(),
                    true);
        } else if (id == JsTokenId.KEYWORD_FALSE) {
            return LiteralNode.newInstance(source,
                    Token.toDesc(TokenType.FALSE, ts.offset(), id.fixedText().length()),
                    ts.offset() + id.fixedText().length(),
                    false);
        } else if (id == JsTokenId.KEYWORD_NULL) {
            return LiteralNode.newInstance(source,
                    Token.toDesc(TokenType.NULL, ts.offset(), id.fixedText().length()),
                    ts.offset() + id.fixedText().length());
        } else if (id == JsTokenId.STRING_BEGIN) {
            int start = ts.offset();
            String val = "";
            if (nextToken(ts)) {
                if (ts.token().id() == JsTokenId.STRING) {
                    val = ts.token().text().toString();
                } else {
                    ts.movePrevious();
                }
            }
            int end = expect(source, JsTokenId.STRING_END, ts, errorManager);
            return LiteralNode.newInstance(source,
                    Token.toDesc(TokenType.STRING, start + 1, val.length()),
                    end,
                    val);
        } else if (id == JsTokenId.NUMBER) {
            return LiteralNode.newInstance(source,
                    Token.toDesc(TokenType.FLOATING, ts.offset(), ts.token().text().length()),
                    ts.offset() + ts.token().text().length(),
                    Double.valueOf(ts.token().text().toString()));
        } else if (id == JsTokenId.BRACKET_LEFT_CURLY) {
            ts.movePrevious();
            return parseObject(source, ts, errorManager);
        } else if (id == JsTokenId.BRACKET_LEFT_BRACKET) {
            ts.movePrevious();
            return parseArray(source, ts, errorManager);
        }
        errorManager.error(source.getName() + ":" + "0:0:" + ts.offset()
                + ": Expected value but " + ts.token().text().toString() + " found");          
        return null;
    }
    
    private LiteralNode<List<Node>> parseArray(Source source, TokenSequence<? extends JsTokenId> ts,
                JsErrorManager errorManager) {
        int start = expect(source, JsTokenId.BRACKET_LEFT_BRACKET, ts, errorManager);
        List<Node> values = Collections.emptyList();
        if (nextToken(ts)) {
            JsTokenId id = ts.token().id();
            ts.movePrevious();
            if (id != JsTokenId.BRACKET_RIGHT_BRACKET) {
                values = parseValues(source, ts, errorManager);
            }
        }
        
        int end = expect(source, JsTokenId.BRACKET_RIGHT_BRACKET, ts, errorManager);
        return LiteralNode.newInstance(source,
                Token.toDesc(TokenType.LBRACKET, start, start + 1),
                end,
                values);
    }
    
    private List<Node> parseValues(Source source, TokenSequence<? extends JsTokenId> ts,
                JsErrorManager errorManager) {
        List<Node> ret = new ArrayList<Node>();
        ret.add(parseValue(source, ts, errorManager));
        if (nextToken(ts)) {
            if (ts.token().id() == JsTokenId.OPERATOR_COMMA) {
                ret.addAll(parseValues(source, ts, errorManager));
            } else {
                ts.movePrevious();
            }
        }
        return ret;
    }

    private int expect(Source source, JsTokenId expected,
            TokenSequence<? extends JsTokenId> ts, JsErrorManager errorManager) {
        if (!nextToken(ts)) {
            errorManager.error(source.getName() + ":" + "0:0:" + (source.getLength() - 1)
                    + ": Expected " + (expected.fixedText() != null ? expected.fixedText() : expected.toString()) + " but eof found");
            return -1;
        }
        JsTokenId id = ts.token().id();
        if (id != expected) {
            errorManager.error(source.getName() + ":" + "0:0:" + ts.offset()
                    + ": Expected " + (expected.fixedText() != null ? expected.fixedText() : expected.toString())  + " but " + ts.token().text().toString() + " found");          
            return -1;
        }
        return ts.offset();
    }
    
    private boolean nextToken(TokenSequence<? extends JsTokenId> ts) {
        boolean ret = false;
        while (ret = ts.moveNext()) {
            if (ts.token().id() != JsTokenId.WHITESPACE && ts.token().id() != JsTokenId.EOL) {
                break;
            }
        }
        return ret;
    }

}
