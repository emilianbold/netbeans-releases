/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.lexer;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.RubyPredefinedVariable;
import org.netbeans.modules.ruby.RubyType;
import org.netbeans.modules.ruby.RubyUtils;

/**
 * Class which represents a Call in the source
 */
public class Call {

    /**
     * Pattern for recognizing constructor calls.
     */
    private static final Pattern CALL_TO_NEW = Pattern.compile(".+(\\.new(\\z|\\(.*\\)))"); //NOI18N

    /**
     * Pattern for recognizing whether a method invocation chain contains a call to new
     */
    private static final Pattern CALL_TO_NEW_IN_CHAIN = Pattern.compile(".+(\\.new(\\z|\\(.*\\)\\.?\\w*|\\.\\w?.*))"); //NOI18N

    public static final Call LOCAL = new Call(RubyType.unknown(), null, false, false);
    public static final Call NONE = new Call(RubyType.unknown(), null, false, false);
    public static final Call UNKNOWN = new Call(RubyType.unknown(), null, false, false);

    private final RubyType type;
    private final String lhs;
    private final boolean isStatic;
    private final boolean methodExpected;
    private final boolean constantExpected;
    private boolean isLHSConstant;
    /**
     * A token to use as LHS when we know the type already as a null
     * LHS indicates the code completer to add all the local variables / methods
     * from the current class, which we don't want since we already know the type,
     * and it would be incorrect to include items from the current
     * class. For example in the following example all instance vars from Foo would
     * be listed in CC if LHS is null:
     * <pre>
     * class Foo
     *  .. instance vars
     *  def bar
     *   "some string".|
     *  end
     * end
     * </pre>
     * Null was previously used in <code>tryLiteral</code> which caused the kind of
     * problems described above.
     * 
     * Note that this is toked is never returned from <code>getLHS</code>.
     * 
     * TODO: Need to improve the infrastucture to have a better way of doing this.
     *
     */
    private static final String EMPTY_LHS = "EMPTY_LHS";

    private Call(RubyType type, String lhs, boolean isStatic, boolean methodExpected) {
        this(type, lhs, isStatic, methodExpected, false);
    }
    
    private Call(RubyType type, String lhs, boolean isStatic, boolean methodExpected, boolean constantExpected) {
        super();
        this.type = type;
        this.lhs = lhs;
        this.methodExpected = methodExpected;
        if (lhs == null && type.isKnown()) {
            assert type.isSingleton() : "should be singleton, was: " + type;
            lhs = type.first();
        }
        this.isStatic = isStatic;
        this.constantExpected = constantExpected;
    }

    private void setLHSConstant(boolean isLHSConstant) {
        this.isLHSConstant = isLHSConstant;
    }

    public RubyType getType() {
        return type;
    }

    public String getLhs() {
        if (EMPTY_LHS.equals(lhs)) {
            return "";
        }
        return lhs;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isLHSConstant() {
        return isLHSConstant;
    }

    public boolean isSimpleIdentifier() {
        if (lhs == null || lhs.equals(EMPTY_LHS)) {
            return false;
        }
        // TODO - replace with the new RubyUtil validations
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

    public boolean isConstantExpected() {
        return constantExpected;
    }

    /** foo.| or foo.b|  -> we're expecting a method call. For Foo:: we don't know. */
    public boolean isMethodExpected() {
        return methodExpected;
    }

    @Override
    public String toString() {
        if (this == LOCAL) {
            return "LOCAL";
        } else if (this == NONE) {
            return "NONE";
        } else if (this == UNKNOWN) {
            return "UNKNOWN";
        } else {
            return "Call(type: " + type + ", lhs: " + lhs + ", isStatic: " +
                    isStatic + ", isLHSConstant: " + isLHSConstant + ')';
        }
    }

    /**
     * Determine whether the given offset corresponds to a method call on
     * another object. This would happen in these cases:
     * 
     * <pre>
     *   Foo::|, Foo::Bar::|, Foo.|, Foo.x|, foo.|, foo.x|
     * </pre>
     *
     * and not here:
     *
     * <pre>
     *   |, Foo|, foo|
     * </pre>
     * The method returns the left hand side token, if any, such as "Foo", Foo::Bar",
     * and "foo". If not, it will return null.<br>
     * Note that "self" and "super" are possible return values for the lhs, which mean
     * that you don't have a call on another object. Clients of this method should
     * handle that return value properly (I could return null here, but clients probably
     * want to distinguish self and super in this case so it's useful to return the info.)
     * <p>
     * This method will also try to be smart such that if you have a block or array
     * call, it will return the relevant classnames (e.g. for [1,2].x| it returns "Array").
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static Call getCallType(BaseDocument doc, TokenHierarchy<Document> th, int offset) {
        TokenSequence<?extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(th, offset);

        if (ts == null) {
            return Call.NONE;
        }

        ts.move(offset);

        boolean methodExpected = false;
        boolean constantExpected = false;

        if (!ts.moveNext() && !ts.movePrevious()) {
            return Call.NONE;
        }

        if (ts.offset() == offset) {
            // We're looking at the offset to the RIGHT of the caret
            // position, which could be whitespace, e.g.
            //  "foo.x| " <-- looking at the whitespace
            ts.movePrevious();
        }

        Token<?extends RubyTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            if (id == RubyTokenId.WHITESPACE) {
                return Call.LOCAL;
            }

            // We're within a String that has embedded Ruby. Drop into the
            // embedded language and iterate the ruby tokens there.
            if (id == RubyTokenId.EMBEDDED_RUBY) {
                ts = (TokenSequence)ts.embedded();
                assert ts != null;
                ts.move(offset);

                if (!ts.moveNext() && !ts.movePrevious()) {
                    return Call.NONE;
                }

                token = ts.token();
                id = token.id();
            }

            // See if we're in the identifier - "x" in "foo.x"
            // I could also be a keyword in case the prefix happens to currently
            // match a keyword, such as "next"
            // However, if we're at the end of the document, x. will lex . as an
            // identifier of text ".", so handle this case specially
            if ((id == RubyTokenId.IDENTIFIER) || (id == RubyTokenId.CONSTANT) ||
                    id.primaryCategory().equals("keyword")) {
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
            if (id == RubyTokenId.DOT) {
                methodExpected = true;
            } else if (id == RubyTokenId.COLON3) {
            } else if (id == RubyTokenId.IDENTIFIER) {
                String t = token.text().toString();

                if (t.equals(".")) {
                    methodExpected = true;
                } else if (t.equals("::")) {
                    constantExpected = true;
                } else {
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
                // do nothing - see #154991
            }

            boolean dotted = false; // is this dotted expression? (e.g. foo.boo.)
            int inParens = 0; // how many unmatched ')' did we encounter

            // Find the beginning of the expression. We'll go past keywords, identifiers
            // and dots or double-colons
            while (ts.movePrevious()) {
                // If we get to the previous line we're done
                if (ts.offset() < lineStart) {
                    break;
                }

                token = ts.token();
                id = token.id();

                if ((id == RubyTokenId.LPAREN)) {
                    if (inParens > 0) {
                        inParens--;
                        continue;
                    } else {
                        break; // unmatched left parenthesis?
                    }
                } else if ((id == RubyTokenId.RPAREN)) {
                    inParens++;
                    continue;
                }

                if (inParens > 0) { // ignore content inside of bracked
                    continue;
                }

                String tokenText = null;
                if (id == RubyTokenId.ANY_KEYWORD) {
                    tokenText = token.text().toString();
                }

                if (id == RubyTokenId.WHITESPACE) {
                    break;
                }

                // do not evaluate e.g. '1.even?.' expression to Fixnum type
                if (!dotted) {
                    Call call = tryLiteral(id, methodExpected, tokenText);
                    if (call != null) {
                        return call;
                    }
                }

                if (((id == RubyTokenId.GLOBAL_VAR) || (id == RubyTokenId.INSTANCE_VAR) ||
                        (id == RubyTokenId.CLASS_VAR) || (id == RubyTokenId.IDENTIFIER)) ||
                        id.primaryCategory().equals("keyword") || (id == RubyTokenId.DOT) ||
                        (id == RubyTokenId.COLON3) || (id == RubyTokenId.CONSTANT) ||
                        (id == RubyTokenId.SUPER) || (id == RubyTokenId.SELF) ||
                        (isLiteral(id))) {
                    
                    // We're building up a potential expression such as "Test::Unit" so continue looking
                    beginOffset = ts.offset();
                    if (id == RubyTokenId.DOT) {
                        dotted = true;
                    }
                } else if ((id == RubyTokenId.LBRACE) || (id == RubyTokenId.LBRACKET)) {
                    // It's an expression for example within a parenthesis, e.g.
                    // yield(^File.join())
                    // in this case we can do top level completion
                    // TODO: There are probably more valid contexts here
                    break;
                } else {
                    // Something else - such as "getFoo().x|" - at this point we don't know the type
                    // so we'll just return unknown
                    return Call.UNKNOWN;
                }
            }

            if (beginOffset < lastSeparatorOffset) {
                try {
                    String lhs = doc.getText(beginOffset, lastSeparatorOffset - beginOffset);

                    if (lhs.equals("super") || lhs.equals("self")) { // NOI18N
                        return new Call(RubyType.create(lhs), lhs, false, true);
                    } else if (Character.isUpperCase(lhs.charAt(0))) {
                        
                        // Detect constructor calls of the form String.new.^
                        int constructorCallLength = isCallToNew(lhs);
                        if (constructorCallLength != -1) {
                            // See if it looks like a type prior to that
                            String type = lhs.substring(0, lhs.length() - constructorCallLength);
                            if (RubyUtils.isValidConstantFQN(type)) {
                                return new Call(RubyType.create(type), lhs, false, methodExpected);
                            }
                        }

                        RubyType type = RubyPredefinedVariable.getType(lhs);
                         // predefined vars are instances
                        // also if it was a call to a constructor, the call is not static
                        boolean isStatic = 
                                (type == null && !containsCallToNew(lhs) && !isChainedCall(lhs))
                                || constantExpected;

                        boolean isLHSConstant = RubyUtils.isValidConstantFQN(lhs);
                        if (type == null /* not predef. var */ && isLHSConstant) {
                            type = RubyType.create(lhs);
                        }

                        RubyType rubyType = type == null ? RubyType.unknown() : type;
                        Call call = new Call(rubyType, lhs, isStatic, methodExpected, constantExpected);
                        call.setLHSConstant(isLHSConstant);

                        return call;
                    } else {
                        // try __FILE__ or __LINE__
                        RubyType typeS = RubyPredefinedVariable.getType(lhs);

                        RubyType type = typeS == null ? RubyType.unknown() : typeS;
                        return new Call(type, lhs, false, methodExpected, constantExpected);
                    }
                } catch (BadLocationException ble) {
                    // do nothing - see #154991
                }
            } else {
                return Call.UNKNOWN;
            }
        }

        return Call.LOCAL;
    }

    /**
     * Checks whether the given lhs represents a constructor invocation.
     * 
     * @param lhs
     * @return the length of the contructor call or <code>-1</code> if
     *  the given <code>lhs</code> did not represent a constructor call.
     */
    private static int isCallToNew(String lhs) {
        Matcher matcher = CALL_TO_NEW.matcher(lhs);
        if (!matcher.matches()) {
            return -1;
        }
        return matcher.group(1).length();
    }

    private static boolean containsCallToNew(String lhs) {
        return CALL_TO_NEW_IN_CHAIN.matcher(lhs).matches();
    }

    private static boolean isChainedCall(String lhs) {
        return lhs.indexOf('.') > 0; //NOI18N
    }

    private static Call tryLiteral(final TokenId id, final boolean methodExpected, final String tokenText) {
        if (id == RubyTokenId.RBRACKET) {
            // Looks like we're operating on an array, e.g.
            //  [1,2,3].each|
            return new Call(RubyType.ARRAY, EMPTY_LHS, false, methodExpected);
        } else if (id == RubyTokenId.RBRACE) { // XXX uh oh, what about blocks?  {|x|printx}.| ? type="Proc"
            // Looks like we're operating on a hash, e.g.
            //  {1=>foo,2=>bar}.each|
            return new Call(RubyType.HASH, EMPTY_LHS, false, methodExpected);
        } else if ((id == RubyTokenId.STRING_END) || (id == RubyTokenId.QUOTED_STRING_END)) {
            return new Call(RubyType.STRING, EMPTY_LHS, false, methodExpected);
        } else if (id == RubyTokenId.REGEXP_END) {
            return new Call(RubyType.REGEXP, EMPTY_LHS, false, methodExpected);
        } else if (id == RubyTokenId.INT_LITERAL) {
            return new Call(RubyType.FIXNUM, EMPTY_LHS, false, methodExpected); // Or Bignum?
        } else if (id == RubyTokenId.FLOAT_LITERAL) {
            return new Call(RubyType.FLOAT, EMPTY_LHS, false, methodExpected);
        } else if (id == RubyTokenId.TYPE_SYMBOL) {
            return new Call(RubyType.SYMBOL, EMPTY_LHS, false, methodExpected);
        } else if (id == RubyTokenId.RANGE) {
            return new Call(RubyType.RANGE, EMPTY_LHS, false, methodExpected);
        } else if ((id == RubyTokenId.ANY_KEYWORD) && "nil".equals(tokenText)) { // NOI18N
            return new Call(RubyType.NIL_CLASS, EMPTY_LHS, false, methodExpected);
        } else if ((id == RubyTokenId.ANY_KEYWORD) && "true".equals(tokenText)) { // NOI18N
            return new Call(RubyType.TRUE_CLASS, EMPTY_LHS, false, methodExpected);
        } else if ((id == RubyTokenId.ANY_KEYWORD) && "false".equals(tokenText)) { // NOI18N
            return new Call(RubyType.FALSE_CLASS, EMPTY_LHS, false, methodExpected);
        } else {
            return null;
        }
    }

    private static boolean isLiteral(final TokenId id) {
        return id == RubyTokenId.RBRACKET ||
                id == RubyTokenId.RBRACE ||
                id == RubyTokenId.STRING_END ||
                id == RubyTokenId.REGEXP_END ||
                id == RubyTokenId.INT_LITERAL ||
                id == RubyTokenId.FLOAT_LITERAL ||
                id == RubyTokenId.TYPE_SYMBOL ||
                id == RubyTokenId.RANGE ||
                id == RubyTokenId.ANY_KEYWORD ||
                id == RubyTokenId.ANY_KEYWORD ||
                id == RubyTokenId.ANY_KEYWORD;
    }
}
