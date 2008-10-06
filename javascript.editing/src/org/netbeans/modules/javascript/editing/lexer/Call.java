/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.javascript.editing.lexer;


import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.javascript.editing.JsUtils;
import org.openide.util.Exceptions;

/**
 * Class which represents a Call in the source
 */
public class Call {

    public static final Call LOCAL = new Call(null, null, false, false, 0);
    public static final Call NONE = new Call(null, null, false, false, 0);
    public static final Call UNKNOWN = new Call(null, null, false, false, 0);
    private final String type;
    private final String lhs;
    private final boolean isStatic;
    private final boolean methodExpected;
    private int prevCallParenPos = -1;
    private int beginOffset;

    public Call(String type, String lhs, boolean isStatic, boolean methodExpected, int beginOffset) {
        super();

        this.type = type;
        this.lhs = lhs;
        this.methodExpected = methodExpected;
        if (lhs == null) {
            lhs = type;
        }
        this.isStatic = isStatic;
        this.beginOffset = beginOffset;
    }

    public int getBeginOffset() {
        return beginOffset;
    }

    public String getType() {
        return type;
    }

    public String getLhs() {
        return lhs;
    }

    public boolean isStatic() {
        return isStatic;
    }
    
    public int getPrevCallParenPos() {
        return prevCallParenPos;
    }

    public boolean isSimpleIdentifier() {
        if (lhs == null) {
            return false;
        }
        // TODO - replace with the new JsUtil validations
        for (int i = 0, n = lhs.length(); i < n; i++) {
            char c = lhs.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                continue;
            }
            if ((c == '@') || (c == '$')) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (this == LOCAL) {
            return "LOCAL"; // NOI18N
        } else if (this == NONE) {
            return "NONE"; // NOI18N
        } else if (this == UNKNOWN) {
            return "UNKNOWN"; // NOI18N
        } else {
            return "Call(" + type + "," + lhs + "," + isStatic + "," + prevCallParenPos + ")"; // NOI18N
        }
    }

    /** foo.| or foo.b|  -> we're expecting a method call. For Foo:: we don't know. */
    public boolean isMethodExpected() {
        return this.methodExpected;
    }
    
    /**
     * Determine whether the given offset corresponds to a method call on another
     * object. This would happen in these cases:
     *    Foo::|, Foo::Bar::|, Foo.|, Foo.x|, foo.|, foo.x|
     * and not here:
     *   |, Foo|, foo|
     * The method returns the left hand side token, if any, such as "Foo", Foo::Bar",
     * and "foo". If not, it will return null.
     * Note that "self" and "super" are possible return values for the lhs, which mean
     * that you don't have a call on another object. Clients of this method should
     * handle that return value properly (I could return null here, but clients probably
     * want to distinguish self and super in this case so it's useful to return the info.)
     *
     * This method will also try to be smart such that if you have a block or array
     * call, it will return the relevant classnames (e.g. for [1,2].x| it returns "Array").
     */
    @SuppressWarnings("fallthrough")
    @NonNull
    public static Call getCallType(BaseDocument doc, TokenHierarchy<Document> th, int offset) {
        TokenSequence<?extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, offset);

        if (ts == null) {
            return Call.NONE;
        }

        ts.move(offset);

        boolean methodExpected = false;

        if (!ts.moveNext() && !ts.movePrevious()) {
            return Call.NONE;
        }

        if (ts.offset() == offset) {
            // We're looking at the offset to the RIGHT of the caret
            // position, which could be whitespace, e.g.
            //  "foo.x| " <-- looking at the whitespace
            ts.movePrevious();
        }

        Token<?extends JsTokenId> token = ts.token();

        if (token != null) {
            JsTokenId id = token.id();

            if (id == JsTokenId.WHITESPACE) {
                return Call.LOCAL;
            }

//            // We're within a String that has embedded JavaScript. Drop into the
//            // embedded language and iterate the Js tokens there.
//            if (id == JsTokenId.EMBEDDED_Js) {
//                ts = (TokenSequence)ts.embedded();
//                assert ts != null;
//                ts.move(offset);
//
//                if (!ts.moveNext() && !ts.movePrevious()) {
//                    return Call.NONE;
//                }
//
//                token = ts.token();
//                id = token.id();
//            }

            // See if we're in the identifier - "x" in "foo.x"
            // I could also be a keyword in case the prefix happens to currently
            // match a keyword, such as "next"
            // However, if we're at the end of the document, x. will lex . as an
            // identifier of text ".", so handle this case specially
            if ((id == JsTokenId.IDENTIFIER) ||
                    id.primaryCategory().equals(JsLexer.KEYWORD_CAT)) {
                String tokenText = token.text().toString();

                if (".".equals(tokenText)) {
                    // Special case - continue - we'll handle this part next
                    methodExpected = true;
                } else if ("::".equals(tokenText)) {
                    // Special case - continue - we'll handle this part next
                } else {
                    methodExpected = true;

                    if (Character.isUpperCase(tokenText.charAt(0))) {
                        methodExpected = false;
                    }

                    if (!ts.movePrevious()) {
                        return Call.LOCAL;
                    }
                }

                token = ts.token();
                id = token.id();
            }

            // If we're not in the identifier we need to be in the dot (in "foo.x").
            // I can't just check for tokens DOT and COLON3 because for unparseable source
            // (like "File.|") the lexer will return the "." as an identifier.
            if (id == JsTokenId.DOT) {
                methodExpected = true;
            } else if (id == JsTokenId.IDENTIFIER) {
                String t = token.text().toString();

                if (t.equals(".")) {
                    methodExpected = true;
                } else if (!t.equals("::")) {
                    return Call.LOCAL;
                }
            } else {
                return Call.LOCAL;
            }

            int lastSeparatorOffset = ts.offset();
            int beginOffset = lastSeparatorOffset;
            int lineStart = 0;

            try {
                if (offset > doc.getLength()) {
                    offset = doc.getLength();
                }

                lineStart = Utilities.getRowStart(doc, offset);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
            
            // Find the beginning of the expression. We'll go past keywords, identifiers
            // and dots or double-colons
    searchBackwards:
            while (ts.movePrevious()) {
                // If we get to the previous line we're done
                if (ts.offset() < lineStart) {
                    break searchBackwards;
                }

                token = ts.token();
                id = token.id();

                String tokenText = null;
                if (id == JsTokenId.ANY_KEYWORD) {
                    tokenText = token.text().toString();
                }
                

                switch (id) {
                    case WHITESPACE:
                        break searchBackwards;
                    //    case RBRACKET:
                    //    // Looks like we're operating on an array, e.g.
                    //    //  [1,2,3].each|
                    //
                    //    // No, it's more likely that we have something like this:  foo[0] -- which is not an array, it's an element of an array of unknown type
                    //    return new Call("Array", null, false, methodExpected);
                    case STRING_LITERAL:
                    case STRING_END:
                        return new Call("String", null, false, methodExpected, beginOffset); // NOI18N
                    case REGEXP_LITERAL:
                    case REGEXP_END:
                        return new Call("RegExp", null, false, methodExpected, beginOffset); // NOI18N
                    case FLOAT_LITERAL:
                        return new Call("Number", null, false, methodExpected, beginOffset); // NOI18N
                    case LPAREN:
                    case LBRACE:
                    case LBRACKET:
                        // It's an expression for example within a parenthesis, e.g.
                        // yield(^File.join())
                        // in this case we can do top level completion
                        // TODO: There are probably more valid contexts here
                        break searchBackwards;
                    case RPAREN: {
                        Call call = new Call(null, null, false, false, beginOffset);
                        call.prevCallParenPos = ts.offset();
                        // The starting offset is more accurate for finding the AST node
                        // corresponding to the call
                        OffsetRange matching = LexUtilities.findBwd(doc, ts, JsTokenId.LPAREN, JsTokenId.RPAREN);
                        if (matching != OffsetRange.NONE){
                            call.prevCallParenPos = matching.getStart();
                        }

                        return call;
                    }
                    case RBRACKET: { // Parenthesis
                        Call call = new Call(null, null, false, false, beginOffset);
                        call.prevCallParenPos = ts.offset();
                        // The starting offset is more accurate for finding the AST node
                        // corresponding to the call
                        OffsetRange matching = LexUtilities.findBwd(doc, ts, JsTokenId.LBRACKET, JsTokenId.LBRACKET);
                        if (matching != OffsetRange.NONE){
                            call.prevCallParenPos = matching.getStart();
                        }

                        return call;
                    }
                    case NONUNARY_OP:
                    case ANY_OPERATOR:
                        // We're in an expression, e.g.  x+y.
                        // and here we can stop when we get to the terminator
                        break searchBackwards;
                        
                    case IDENTIFIER:
                    case DOT:
                    case THIS:
                        // We're building up a potential expression such as "Test::Unit" so continue looking
                        beginOffset = ts.offset();

                        continue searchBackwards;
                    case ANY_KEYWORD: {
                        if ("true".equals(tokenText)) { // NOI18N
                            return new Call("Boolean", null, false, methodExpected, beginOffset);
                        } else if ("false".equals(tokenText)) { // NOI18N
                            return new Call("Boolean", null, false, methodExpected, beginOffset);
                        }
                        // fallthrough
                    }
                    default: {
                        if (id.primaryCategory().equals(JsLexer.KEYWORD_CAT)) { // NOI18N
                            // We're building up a potential expression such as "Test::Unit" so continue looking
                            beginOffset = ts.offset();

                            continue searchBackwards;
                        }
                        
                        
                        // Something else - such as "getFoo().x|" - at this point we don't know the type
                        // so we'll just return unknown
                        return Call.UNKNOWN;
                    }
                }
            }

            if (beginOffset < lastSeparatorOffset) {
                try {
                    String lhs = doc.getText(beginOffset, lastSeparatorOffset - beginOffset);

                    if (lhs.equals("super") || lhs.equals("this")) { // NOI18N
                        return new Call(lhs, lhs, false, true, beginOffset);
                    } else if (Character.isUpperCase(lhs.charAt(0))) {
                        
                        // Detect type references of the form
                        //   Spry.Data.Region.prototype.process
                        // but not "foo.bar"
                        String type = null;
                        boolean valid = true;
                        String[] classParts = lhs.split("\\.");
                        for (int i = 0; i < classParts.length; i++) {
                            String c = classParts[i];
                            if (!JsUtils.isValidJsClassName(c)) {
                                if (i == classParts.length-1 &&
                                        "prototype".equals(c)) {
                                    type = lhs.substring(0,lhs.length()-".prototype".length());
                                } else {
                                    valid = false;
                                }
                            }
                        }
                        if (valid && type == null) {
                            type = lhs;
                        }
                        
                        return new Call(type, lhs, true, methodExpected, beginOffset);
                    } else {
                        return new Call(null, lhs, false, methodExpected, beginOffset);
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            } else {
                return Call.UNKNOWN;
            }
        }

        return Call.LOCAL;
    }
    
    public static String getCallExpression(BaseDocument doc, int offset) throws BadLocationException {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        Call call = getCallType(doc, th, offset);
        if (call.getLhs() != null) {
            int end = Utilities.getWordEnd(doc, offset);
            int begin = call.getBeginOffset();
            return doc.getText(begin, end - begin);
        } else {
            return Utilities.getIdentifier(doc, offset);
        }
    }
}
