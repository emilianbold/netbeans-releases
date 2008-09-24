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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.javadoc;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 * XXX try to merge with hints.JavadocUtilities
 * 
 * @author Jan Pokorsky
 */
final class JavadocCompletionUtils {
    
    static final Pattern JAVADOC_LINE_BREAK = Pattern.compile("\\n[ \\t]*\\**[ \\t]*\\z"); // NOI18N
    static final Pattern JAVADOC_WHITE_SPACE = Pattern.compile("[^ \\t]"); // NOI18N
    static final Pattern JAVADOC_FIRST_WHITE_SPACE = Pattern.compile("[ \\t]*\\**[ \\t]*"); // NOI18N
    private static Set<JavaTokenId> IGNORE_TOKES = EnumSet.of(
            JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT);
    
    /**
     * Checks if the offset is part of some javadoc block. The javadoc content
     * is considered as everything between <code>/**</code>
     * and <code>&#42;/</code> except
     * indentation prefixes <code>'&#32;&#32;&#32;&#32;*'</code>
     * on each line.
     * <p>Note: the method takes a document lock.</p>
     * 
     * @param doc a document to search
     * @param offset an offset in document
     * @return <code>true</code> if the offset refers to a javadoc content
     */
    public static boolean isJavadocContext(final Document doc, final int offset) {
        final boolean[] result = {false};
        doc.render(new Runnable() {

            public void run() {
                result[0] = isJavadocContext(TokenHierarchy.get(doc), offset);
            }
        });
        return result[0];
    }
    
    static boolean isJavadocContext(TokenHierarchy hierarchy, int offset) {
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(hierarchy, offset);
        if (!movedToJavadocToken(ts, offset)) {
            return false;
        }
        
        TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
        if (jdts == null) {
            return false;
        } else if (jdts.isEmpty()) {
            return isEmptyJavadoc(ts.token(), offset - ts.offset());
        }
        
        jdts.move(offset);
        if (!jdts.moveNext() && !jdts.movePrevious()) {
            return false;
        }
        
        // this checks /** and */ headers
        return isInsideToken(jdts, offset) && !isInsideIndent(jdts.token(), offset - jdts.offset());
    }
    
    public static Doc findJavadoc(CompilationInfo javac, Document doc, int offset) {
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(javac.getTokenHierarchy(), offset);
        if (ts == null || !movedToJavadocToken(ts, offset)) {
            return null;
        }
        
        int offsetBehindJavadoc = ts.offset() + ts.token().length();

        while (ts.moveNext()) {
            TokenId tid = ts.token().id();
            if (tid == JavaTokenId.BLOCK_COMMENT) {
                if ("/**/".contentEquals(ts.token().text())) { // NOI18N
                    // see #147533
                    return null;
                }
            }
            if (!IGNORE_TOKES.contains(tid)) {
                offsetBehindJavadoc = ts.offset();
                // it is magic for TreeUtilities.pathFor
                ++offsetBehindJavadoc;
                break;
            }
        }

        TreePath tp = javac.getTreeUtilities().pathFor(offsetBehindJavadoc);
        Tree leaf = tp.getLeaf();
        Kind kind = leaf.getKind();
        SourcePositions positions = javac.getTrees().getSourcePositions();

        while (kind != Kind.CLASS && kind != Kind.METHOD && kind != Kind.VARIABLE && kind != Kind.COMPILATION_UNIT) {
            tp = tp.getParentPath();
            if (tp == null) {
                leaf = null;
                kind = null;
                break;
            }
            leaf = tp.getLeaf();
            kind = leaf.getKind();
        }
                
        if (leaf == null || kind == Kind.COMPILATION_UNIT
                || positions.getStartPosition(javac.getCompilationUnit(), leaf) < offset) {
            // not a class member javadoc -> ignore
            return null;
        }
        
        Element el = javac.getTrees().getElement(tp);
        return el != null? javac.getElementUtilities().javaDocFor(el): null;
    }
    
    static TokenSequence<JavadocTokenId> findJavadocTokenSequence(CompilationInfo javac, int offset) {
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(javac.getTokenHierarchy(), offset);
        if (ts == null || !movedToJavadocToken(ts, offset)) {
            return null;
        }
        
        TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
        if (jdts == null) {
            return null;
        }
        
        jdts.move(offset);
        return jdts;
    }
    
    /**
     * Finds javadoc token sequence.
     * @param javac compilation info
     * @param e element for which the tokens are queried
     * @return javadoc token sequence or null.
     */
    static TokenSequence<JavadocTokenId> findJavadocTokenSequence(CompilationInfo javac, Element e) {
        if (e == null || javac.getElementUtilities().isSynthetic(e) || javac.getElements().getDocComment(e) == null)
            return null;
        
        Tree tree = javac.getTrees().getTree(e);
        if (tree == null)
            return null;
        
        int elementStartOffset = (int) javac.getTrees().getSourcePositions().getStartPosition(javac.getCompilationUnit(), tree);
        TokenSequence<JavaTokenId> s = SourceUtils.getJavaTokenSequence(javac.getTokenHierarchy(), elementStartOffset);
        if (s == null) {
            return null;
        }
        s.move(elementStartOffset);
        Token<JavaTokenId> token = null;
        while (s.movePrevious()) {
            token = s.token();
            if (token.id() == JavaTokenId.BLOCK_COMMENT) {
                if ("/**/".contentEquals(token.text())) { // NOI18N
                    // see #147533
                    break;
                }
            }
            if (!IGNORE_TOKES.contains(token.id())) {
                break;
            }
        }
        if (token == null || token.id() != JavaTokenId.JAVADOC_COMMENT) {
            return null;
        }

        return s.embedded(JavadocTokenId.language());
    }

    static boolean isInsideIndent(Token<JavadocTokenId> token, int offset) {
        int indent = -1;
        if (token.id() == JavadocTokenId.OTHER_TEXT) {
            CharSequence text = token.text();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    if (i <= offset) {
                        // new line; reset status
                        indent = -1;
                        if (i < offset) {
                            continue;
                        }
                    }
                    // stop, line inspection is ready
                    break;
                } else if (i == 0) {
                    // token must start with \n otherwise it is not indentation
                    break;
                }

                if (c == '*' && indent < 0) {
                    indent = i;
                    if (offset <= i) {
                        // stop, offset is inside indentation
                        break;
                    }
                }
            }
        }
        return indent >= offset;
    }
    
    /**
     * Is javadoc line break?
     * @param token token to test
     * @return {@code true} in case the token is something like {@code "\n\t*"}
     */
    public static boolean isLineBreak(Token<JavadocTokenId> token) {
        return isLineBreak(token, token.length());
    }
    
    /**
     * Tests if the token part before {@code pos} is a javadoc line break.
     * @param token a token to test
     * @param pos position in the token
     * @return {@code true} in case the token is something like {@code "\n\t* |\n\t*"}
     */
    public static boolean isLineBreak(Token<JavadocTokenId> token, int pos) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }
        
        if (pos < 0 || pos > token.length()) {
            throw new IndexOutOfBoundsException("pos: " + pos + ", token.length: " + token.length());
        }

        CharSequence text = token.text();
        boolean result = pos > 0
                && JAVADOC_LINE_BREAK.matcher(text.subSequence(0, pos)).find()
                && (pos == token.length() || !isInsideIndent(token, pos));
        return result;
    }
    
    public static boolean isWhiteSpace(CharSequence text) {
        return text != null && text.length() > 0 && !JAVADOC_WHITE_SPACE.matcher(text).find();
    }
    
    public static boolean isWhiteSpace(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }

        CharSequence text = token.text();
        boolean result = !JAVADOC_WHITE_SPACE.matcher(text).find();
        return result;
    }
    
    /**
     * enhanced {@link #isWhiteSpace(org.netbeans.api.lexer.Token) isWhiteSpace}
     * @param token "\t" or "\t**\t"
     * @return same value as isWhiteSpace
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=131826">#131826</a>
     */
    public static boolean isFirstWhiteSpaceAtFirstLine(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }

        CharSequence text = token.text();
        boolean result = JAVADOC_FIRST_WHITE_SPACE.matcher(text).matches();
        return result;
    }
    
    public static boolean isWhiteSpaceFirst(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT || token.length() < 1) {
            return false;
        }

        CharSequence text = token.text();
        char c = text.charAt(0);
        return c == ' ' || c == '\t';
    }
    
    public static boolean isWhiteSpaceLast(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT || token.length() < 1) {
            return false;
        }

        CharSequence text = token.text();
        char c = text.charAt(text.length() - 1);
        return c == ' ' || c == '\t';
    }
    
    public static boolean isInlineTagStart(Token<JavadocTokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }

        CharSequence text = token.text();
        boolean result = text.charAt(text.length() - 1) == '{';
        return result;
    }
    
    public static boolean isBlockTag(Tag tag) {
        Doc holder = tag.holder();
        Tag[] tags = holder.tags();
        for (int i = 0; i < tags.length; i++) {
            if (tag == tags[i]) {
                return true;
            }
        }

        return false;
    }
    
    static CharSequence getCharSequence(Document doc) {
        CharSequence cs = (CharSequence) doc.getProperty(CharSequence.class);
        if (cs == null) {
            try {
                cs = doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                // throw the same exception as CharSequence.subSequence
                throw (IndexOutOfBoundsException) new IndexOutOfBoundsException().initCause(ex);
            }
        }
        return cs;
    }
    
    static CharSequence getCharSequence(Document doc, int begin, int end) {
        CharSequence cs = (CharSequence) doc.getProperty(CharSequence.class);
        if (cs != null) {
            cs = cs.subSequence(begin, end);
        } else {
            try {
                cs = doc.getText(begin, end - begin);
            } catch (BadLocationException ex) {
                // throw the same exception as CharSequence.subSequence
                throw (IndexOutOfBoundsException) new IndexOutOfBoundsException().initCause(ex);
            }
        }
        return cs;
    }
    
    private static boolean isInsideToken(TokenSequence<?> ts, int offset) {
        return offset >= ts.offset() && offset <= ts.offset() + ts.token().length();
    }
    
    /** enhanced moveNext & movePrevious */
    private static boolean movedToJavadocToken(TokenSequence<JavaTokenId> ts, int offset) {
        if (ts == null || !ts.moveNext() && !ts.movePrevious()) {
            return false;
        }
        
        if (ts.token().id() != JavaTokenId.JAVADOC_COMMENT) {
            return false;
        }
        
        return isInsideToken(ts, offset);
    }
    
    /**
     * Checks special case of empty javadoc <code>/**|&#42;/</code>. 
     * @param token javadoc token
     * @param offset offset <B>INSIDE</B> jvadoc token
     * @return <code>true</code> in case of empty javadoc and the proper position
     */
    private static boolean isEmptyJavadoc(Token<JavaTokenId> token, int offset) {
        if (token != null && token.id() == JavaTokenId.JAVADOC_COMMENT) {
            CharSequence text = token.text();
            // check special case /**|*/
            return offset == 3 && "/***/".contentEquals(text); //NOI18N
        }
        return false;
    }
    
}
