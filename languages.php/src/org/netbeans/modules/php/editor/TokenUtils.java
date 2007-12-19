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
package org.netbeans.modules.php.editor;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.lexer.PhpTokenId;
import org.netbeans.modules.php.model.ModelAccess;
import org.netbeans.modules.php.model.PhpModel;


/**
 * @author ads
 *
 */
public class TokenUtils {
    
    public static String RBRACKET   = "]";                  // NOI18N
    
    public static String RPAREN     = ")";                  // NOI18N
    
    public static String LBRACKET   = "[";                  // NOI18N
    
    public static String LPAREN     = "(";                  // NOI18N
    
    public static String SEMICOLON  = ";";                  // NOI18N
    
    public static String LBRACE     = "{";                  // NOI18N
    
    public static String RBRACE     = "}";                  // NOI18N
    
    public static String FOR        = "for";                // NOI18N

    public static String CASE       = "case";                // NOI18N

    public static String DOT        = ".";                // NOI18N

    public static String COLON      = ":";                // NOI18N

    public static String COMMA      = ",";                // NOI18N

    public static String ARRAY_PAIR_MAPPER    = "=>";       // NOI18N
    
    
    public static String WHITESPACE     = "php_whitespace";     // NOI18N
    
    public static String STRING         = "php_string";         // NOI18N

    public static String BLOCK_COMMENT  = "php_comment";        // NOI18N
    
    public static String LINE_COMMENT   = "php_line_comment";   // NOI18N
    
    public static String EOD_STRING     = "php_eod_string";     // NOI18N
    
    public static String EOD_OPERATOR   = "php_eod_operator";   // NOI18N
    
    public enum PHPTokenName {
        OPERATOR("php_operator"),          // NOI18N
        WHITESPACE("php_whitespace"),      // NOI18N
        SEPARATOR("php_separator"),        // NOI18N
        BLOCK_COMMENT("php_comment"),      // NOI18N
        LINE_COMMENT("php_line_comment"),  // NOI18N
        EOD_STRING("php_eod_string"),      // NOI18N
        EOD_OPERATOR("php_eod_operator");  // NOI18N
        
        PHPTokenName(String value) { this.value = value; }
        private final String value;
        public String value() { return value; }
    }

    private TokenUtils(){
    }
    
    public static Token getToken( TokenSequence seq , int offset ){
        seq.move(offset);
        if ( !seq.moveNext() && !seq.movePrevious() ){
            return null;
        }
        return seq.token();
    }

    /**
     * Returns text of an entried token located nearly the caret. 
     * @param seq a token sequence 
     * @param offset location of the caret  
     * @param upToOffset If true, provide a text only up to the caretOffset. 
     * Otherwise, compute the text of the token under the caret. 
     * @return text of the token located at\before the specified 
     * <code>offset</code> or <code>null</code> if there are no tokens in both 
     * the backward direction and the forward direction.
     * @throws ConcurrentModificationException if this token sequence is no 
     * longer valid because of an underlying mutable input source modification.
     */
    public static String getEnteredTokenText( TokenSequence seq, int offset,
                    boolean upToOffset )
    {
        String text = null;
        int diff = seq.move(offset);
        if (diff == 0 ? seq.movePrevious() : seq.moveNext()) {
            Token token = seq.token();
            if(token != null) {
                text = token.text().toString();
                if(upToOffset && diff != 0) {
                    text = text.substring(0, diff);
                }
            }
        }
        return text;
    }
    
    /**
     * Returns an entried token located nearly the caret. 
     * @param seq a token sequence 
     * @param offset location of the caret  
     * @return the token located at\before the specified 
     * <code>offset</code> or <code>null</code> if there are no tokens in both 
     * the backward direction and the forward direction.
     * @throws ConcurrentModificationException if this token sequence is no 
     * longer valid because of an underlying mutable input source modification.
     */
    public static Token getEnteredToken( TokenSequence seq, int offset)
    {
        Token token = null;
        int diff = seq.move(offset);
        if (diff == 0 ? seq.movePrevious() : seq.moveNext()) {
            token = seq.token();
        }
        return token;
    }

    /**
     * returns offset of token which starts on or inclides specified offset.
     */
    public static int getTokenOffset( TokenSequence seq , int offset ){
        seq.move(offset);
        if ( !seq.moveNext() && !seq.movePrevious() ){
            return -1;
        }
        return seq.offset();
    }
    
    /**
     * returns offset of token which starts on or inclides specified offset.
     */
    public static int getTokenOffset( BaseDocument doc , int offset ){
        TokenSequence sequence = getTokenSequence(doc);
        return getTokenOffset(sequence, offset);
    }
    
    public static boolean checkPhp( Document doc , int offset ) {
        Token token = TokenUtils.getToken( doc, offset);
        return token!= null && token.id() == PhpTokenId.PHP;
    }
    
    public static TokenSequence getEmbeddedTokenSequence( Document doc , 
            int offset ) 
    {
        assert doc instanceof AbstractDocument;
        AbstractDocument document = ( AbstractDocument) doc;
        document.readLock();
        try {
            TokenHierarchy hierarchy = TokenHierarchy.get(doc);
            TokenSequence seq = hierarchy.tokenSequence();
            seq.move(offset);
            if( !seq.moveNext() && !seq.movePrevious() ){
                return null; 
            }
            return seq.embedded();
        }
        finally {
            document.readUnlock();
        }
    }
    
    public static Token getToken( Document document , int offset ){
        assert document instanceof AbstractDocument;
        AbstractDocument doc = ( AbstractDocument) document;
        doc.readLock();
        try {
            TokenHierarchy hierarchy = TokenHierarchy.get(doc);
            TokenSequence seq = hierarchy.tokenSequence();
            return getToken( seq , offset);
        }
        finally {
            doc.readUnlock();
        }
    }
    
    public static Token getPhpToken( BaseDocument document , int offset ){
        TokenSequence sequence = getTokenSequence(document);
        return getToken(sequence, offset);
    }
    
    public static Token getEmbeddedToken( Document document , int offset ){
        TokenSequence sequence = getEmbeddedTokenSequence(document, offset);
        return getToken(sequence, offset);
    }
    
    public static String getEnteredEmbeddedTokenText( Document document , 
            int offset,   boolean upToOffset ){
        TokenSequence sequence = getEmbeddedTokenSequence(document, offset);
        return getEnteredTokenText(sequence, offset, upToOffset);
    }
    
    public static TokenSequence getTokenSequence( BaseDocument doc )
    {
        PhpModel model = ModelAccess.getAccess().getModel(doc);
        model.writeLock();
        try {
            model.sync();
            return  model.getLookup().lookup( TokenSequence.class );
        }
        finally {
            model.writeUnlock();
        }
    }
    
    public static ASTNode getRoot( BaseDocument doc ){
        PhpModel model = ModelAccess.getAccess().getModel(doc);
        model.writeLock();
        try {
            model.sync();
            return model.getLookup().lookup( ASTNode.class );
        }
        finally {
            model.writeUnlock();
        }
    }
    
    public static String getTokenType( Token token ) {
        return token.id().name();
    }
    
    /**
     * Returns text of the nearest <code>BLOCK_COMMENT</code> token that is located 
     * before the specified <code>offset</code> and whose text is started with 
     * "/**".
     * @param document a target document
     * @param offset an offest 
     * @return the text of the <code>BLOCK_COMMENT</code> token if it is possible,
     * otherwise <code>null</code>.
     */
    public static String getDocComentText(Document document , int offset) {
        TokenSequence ts = getEmbeddedTokenSequence(document, offset);
        if(ts == null) {
            return null;
        }
        ts.move(offset);
        while( ts.movePrevious() ) {
            Token token = ts.token();
            if(token==null) {
                return null;
            }
            String tokenType = getTokenType(token);
            if(WHITESPACE.equals(tokenType)) {
                continue;
            }
            else if(BLOCK_COMMENT.equals(tokenType)) {
                String text = token.text().toString();
                if(text.startsWith("/**")) {
                    String docCommentText = text;
                    return docCommentText;
                }
            }
            break;
        }
        return null;
    }

}
