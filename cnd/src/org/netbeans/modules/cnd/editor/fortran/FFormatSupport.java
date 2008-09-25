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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.editor.fortran;

import javax.swing.text.Document;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;

/**
 * Fortran indentation services are located here
 *
 * duped from editor/libsrc/org/netbeans/editor/ext/java/JavaFormatSupport.java
 */

public class FFormatSupport extends ExtFormatSupport {
    
    private TokenContextPath tokenContextPath;
    
    public FFormatSupport(FormatWriter formatWriter) {
        this(formatWriter, FTokenContext.contextPath);
    }
    
    public FFormatSupport(FormatWriter formatWriter, TokenContextPath tokenContextPath) {
        super(formatWriter);
        this.tokenContextPath = tokenContextPath;
    }
    
    public TokenContextPath getTokenContextPath() {
        return tokenContextPath;
    }
    
    @Override
    public boolean isComment(TokenItem token, int offset) {
        TokenID tokenID = token.getTokenID();
        return (token.getTokenContextPath() == tokenContextPath
        && (tokenID == FTokenContext.LINE_COMMENT));
    }
    
    /** Determine if the given token is a "Free Format" Fortran comment.
     */
    public boolean isFreeFormatComment(TokenItem token) {
        return token != null && token.getImage().startsWith("!"); // NOI18N
    }
    
    /** Determine if the given token is a "Fixed Format" Fortran comment.
     */
    public boolean isFixedFormatComment(TokenItem token) {
        return token != null &&
        (token.getImage().startsWith("C") || token.getImage().startsWith("c")) && // NOI18N
        getTokenIndent(token) == 0 && !getFreeFormat();
    }
    
    public boolean isPreprocessor(TokenItem token){
        return token != null && token.getImage().startsWith("#") && // NOI18N
        getVisualColumnOffset(getPosition(token,0)) == 0;
    }
    
    public boolean isFixedFormatLabel(TokenItem token) {
        if( token != null && token.getTokenID() == FTokenContext.NUM_LITERAL_INT && !getFreeFormat()){
            FormatTokenPosition tp = getPosition(token,0);
            if( (getVisualColumnOffset(tp) + token.getImage().length())<=5 ) return true;
        }
        return false;
    }
    
    public boolean isFixedFormatLineContinuation(TokenItem token){
        return token != null && (token.getTokenID() == FTokenContext.OP_PLUS
        || token.getTokenID() == FTokenContext.OP_MINUS)
        && !getFreeFormat() && getVisualColumnOffset(getPosition(token,0)) == 5;
    }
    
    @Override
    public TokenID getWhitespaceTokenID() {
        return FTokenContext.WHITESPACE;
    }
    
    @Override
    public TokenContextPath getWhitespaceTokenContextPath() {
        return tokenContextPath;
    }
    
    @Override
    public boolean canModifyWhitespace(TokenItem inToken) {
        if (inToken.getTokenContextPath() == FTokenContext.contextPath) {
            switch (inToken.getTokenID().getNumericID()) {
                case FTokenContext.WHITESPACE_ID:
                    return true;
            }
        }
        return false;
    }
    
    /** Find the starting token in the line of code, given a particular token
     * @param token the starting point token
     * @return token the token at the start of the line of code
     */
    public TokenItem findLineStartToken(TokenItem token) {
        if (token != null) {
            FormatTokenPosition pos = getPosition(token, 0);
            pos = findLineStart(pos);
            token = pos.getToken();
            while (token.getTokenID() == FTokenContext.WHITESPACE)
                token = token.getNext();
        }
        return token;
    }
    
    /** Determine if this if statement is a single line if statement or
     *  if it is a multiline if statement. A multiline if statement will
     *  always end with the "then" keyword.
     * @param startToken the starting token for this line of code
     * @return true if this is a multiline if statement.
     */
    public boolean isIfThenStatement(TokenItem startToken) {
        FormatTokenPosition pos = getPosition(startToken, 0);
        pos = findLineEnd(pos);
        TokenItem lastToken = pos.getToken();
        lastToken = lastToken.getPrevious();
        while (lastToken.getTokenID() == FTokenContext.WHITESPACE) {
            lastToken = lastToken.getPrevious();
        }
        if (lastToken.getTokenID() == FTokenContext.KW_THEN) {
            return true;
        }
        return false;
    }
    
    /** Find the matching token for the supplied token. This will always
     *  do a backward search.
     * @param token - the token that ends the block of code, ie,
     *  "endselect", "end", "enddo", etc.
     * @param matchTokenID - the token numeric ID that you are trying to match,
     * ie, KW_SELECT_ID if you are trying to match the "select" token
     * @param matchEndKeywordID - the token numeric ID of an "end..." token,
     * ie, KW_ENDSELECT_ID if you are trying to match the "endselect" token
     * @return corresponding token that begins the block
     */
    public TokenItem findMatchingToken(TokenItem token, int matchTokenID, int matchEndKeywordID) {
        int depth = 0; // depth of multiple "end select" stmts
        TokenItem startToken;
        while (true) {
            TokenItem impToken = findImportantToken(token, null, true);
            startToken = token = findLineStartToken(impToken);
            if (token == null) {
                return null;
            }
            if (isFixedFormatLabel(startToken)){
                // in fixed format: labels are not treated as start tokens
                //   line cont.can be ignored because a starting matchToken
                //   will not be found on a continuated line.
                do{
                    startToken = startToken.getNext();
                }while( startToken.getTokenID() == FTokenContext.WHITESPACE );
            }
            int tokenNumericID = startToken.getTokenID().getNumericID();
            if (tokenNumericID == FTokenContext.KW_END_ID) {
                
                // is this "end" token is really an "end..." token then
                TokenItem tokenAfterEnd = startToken.getNext();
                while (tokenAfterEnd.getTokenID() == FTokenContext.WHITESPACE)
                    tokenAfterEnd = tokenAfterEnd.getNext();
                if (tokenAfterEnd == null)
                    return null;
                
                if (tokenAfterEnd.getTokenID().getNumericID() == matchTokenID)
                    depth++;
                
            } else if (tokenNumericID == matchEndKeywordID) {
                depth++;
                
            } else if (tokenNumericID == matchTokenID) {
                if( matchTokenID == FTokenContext.KW_IF_ID && matchEndKeywordID == FTokenContext.KW_ENDIF_ID ){
                    // there must be a 'THEN' on this line to be a valid 'IF' match
                    TokenItem nextToken = startToken;
                    do{
                        nextToken = nextToken.getNext();
                        if( nextToken == null ) return null;
                        if( nextToken.getImage().indexOf('\n') > -1 ){
                            // break, unless the next line is a continuation
                            TokenItem t = nextToken.getNext();
                            if(!isFixedFormatLineContinuation(findLineStartToken(t)))
                                break;
                        }
                    }while( nextToken.getTokenID() != FTokenContext.KW_THEN );
                    if( nextToken.getImage().indexOf('\n') > -1 ) continue;
                }
                if (depth-- == 0) {
                    return token; // successful search
                }
            }
        }// end while
    }// end findMatchingToken()
    
    /** Get the indentation for the given token.
     * @param token token for which the indent is being searched.
     *  The token itself is ignored and the previous token
     *  is used as a base for the search.
     */
    public int getTokenIndent(TokenItem token) {
        
        FormatTokenPosition tp = getPosition(token, 0);
        FormatTokenPosition fnw = findLineFirstNonWhitespace(tp);
        
        if (fnw != null) { // valid first non-whitespace
            TokenItem t = fnw.getToken();
            if( isFixedFormatLabel(t) || isFixedFormatLineContinuation(t)){
                do{
                    t = t.getNext();
                }while(  t != null && t.getTokenID() == getWhitespaceTokenID() );
                fnw = (t==null && t.getImage().length()>0) ? null : getPosition(t,0);
            }
            if (fnw != null) tp = fnw;
        }
        return getVisualColumnOffset(tp);
    }
    
    /** Find the indentation for the first token on the line.
     * The given token is also examined in some cases.
     */
    public int findIndent(TokenItem token) {
        int indent = -1; // assign invalid indent
        // First check the given token
        if (token == null) return 0;
        TokenItem nextToken;
        TokenItem matchToken = null;
        switch (token.getTokenID().getNumericID()) {
            
            case FTokenContext.KW_CASE_ID:
            case FTokenContext.KW_DEFAULT_ID:
                matchToken = findMatchingToken(token,
                FTokenContext.KW_SELECT_ID,
                FTokenContext.KW_ENDSELECT_ID);
                if (matchToken != null) {
                    indent = getTokenIndent(matchToken) + getShiftWidth();
                }
                break;
                
            case FTokenContext.KW_PROGRAM_ID:
            case FTokenContext.KW_ENDPROGRAM_ID:
                indent = 0;
                break;
                
            case FTokenContext.KW_END_ID:
                // usually the next token after an "END" token is
                // the type of block this is.
                TokenItem lookupToken = null;
                nextToken = token.getNext();
                if (nextToken == null) {
                    if( getFreeFormat() ) indent = 0;
                    else indent = 6;
                    break;
                }
                if (nextToken.getTokenID() == FTokenContext.WHITESPACE ) {
                    nextToken = nextToken.getNext();
                }
                if (nextToken == null) {
		    indent = 0;
		    break;
		}
                if (nextToken.getImage().compareTo(BaseDocument.LS_LF) == 0) {
                    // We're at the end of the program
                    indent = 0;
                } else {
                    // We're at a two word end statement,ie, end if
                    switch (nextToken.getTokenID().getNumericID()) {
                        case FTokenContext.KW_IF_ID:
                        case FTokenContext.KW_ELSE_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_IF_ID,
                            FTokenContext.KW_ENDIF_ID);
                            break;
                            
                        case FTokenContext.KW_BLOCK_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_BLOCK_ID,
                            FTokenContext.KW_ENDBLOCK_ID);
                            break;
                            
                        case FTokenContext.KW_BLOCKDATA_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_BLOCKDATA_ID,
                            FTokenContext.KW_ENDBLOCKDATA_ID);
                            break;
                            
                        case FTokenContext.KW_DO_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_DO_ID,
                            FTokenContext.KW_ENDDO_ID);
                            break;
                            
                        case FTokenContext.KW_FORALL_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_FORALL_ID,
                            FTokenContext.KW_ENDFORALL_ID);
                            break;
                            
                        case FTokenContext.KW_FUNCTION_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_FUNCTION_ID,
                            FTokenContext.KW_ENDFUNCTION_ID);
                            break;
                            
                        case FTokenContext.KW_INTERFACE_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_INTERFACE_ID,
                            FTokenContext.KW_ENDINTERFACE_ID);
                            break;
                            
                        case FTokenContext.KW_MAP_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_MAP_ID,
                            FTokenContext.KW_ENDMAP_ID);
                            break;
                            
                        case FTokenContext.KW_MODULE_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_MODULE_ID,
                            FTokenContext.KW_ENDMODULE_ID);
                            break;
                            
                        case FTokenContext.KW_PROGRAM_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_PROGRAM_ID,
                            FTokenContext.KW_ENDPROGRAM_ID);
                            break;

                        case FTokenContext.KW_SELECT_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_SELECT_ID,
                            FTokenContext.KW_ENDSELECT_ID);
                            break;
                            
                        case FTokenContext.KW_STRUCTURE_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_STRUCTURE_ID,
                            FTokenContext.KW_ENDSTRUCTURE_ID);
                            break;
                            
                        case FTokenContext.KW_SUBROUTINE_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_SUBROUTINE_ID,
                            FTokenContext.KW_ENDSUBROUTINE_ID);
                            break;
                            
                        case FTokenContext.KW_TYPE_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_TYPE_ID,
                            FTokenContext.KW_ENDTYPE_ID);
                            break;
                            
                        case FTokenContext.KW_UNION_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_UNION_ID,
                            FTokenContext.KW_ENDUNION_ID);
                            break;
                            
                        case FTokenContext.KW_WHERE_ID:
                            matchToken = findMatchingToken(token,
                            FTokenContext.KW_WHERE_ID,
                            FTokenContext.KW_ENDWHERE_ID);
                            break;
                    }// END SWITCH
                    if (matchToken != null) {
                        indent = getTokenIndent(matchToken);
                    }
                }//end else
                break;
                
            default:
                switch (token.getTokenID().getNumericID()) {
                    case FTokenContext.KW_ELSE_ID:
                    case FTokenContext.KW_ELSEIF_ID:
                    case FTokenContext.KW_ENDIF_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_IF_ID,
                        FTokenContext.KW_ENDIF_ID);
                        break;
                        
                    case FTokenContext.KW_ENDBLOCK_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_BLOCK_ID,
                        FTokenContext.KW_ENDBLOCK_ID);
                        break;
                        
                    case FTokenContext.KW_ENDBLOCKDATA_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_BLOCKDATA_ID,
                        FTokenContext.KW_ENDBLOCKDATA_ID);
                        break;
                        
                    case FTokenContext.KW_ENDDO_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_DO_ID,
                        FTokenContext.KW_ENDDO_ID);
                        break;
                        
                    case FTokenContext.KW_ENDFORALL_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_FORALL_ID,
                        FTokenContext.KW_ENDFORALL_ID);
                        break;
                        
                    case FTokenContext.KW_ENDFUNCTION_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_FUNCTION_ID,
                        FTokenContext.KW_ENDFUNCTION_ID);
                        break;
                        
                    case FTokenContext.KW_ENDINTERFACE_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_INTERFACE_ID,
                        FTokenContext.KW_ENDINTERFACE_ID);
                        break;
                        
                    case FTokenContext.KW_ENDMAP_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_MAP_ID,
                        FTokenContext.KW_ENDMAP_ID);
                        break;
                        
                    case FTokenContext.KW_ENDMODULE_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_MODULE_ID,
                        FTokenContext.KW_ENDMODULE_ID);
                        break;
                        
                    case FTokenContext.KW_ENDSELECT_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_SELECT_ID,
                        FTokenContext.KW_ENDSELECT_ID);
                        break;
                        
                    case FTokenContext.KW_ENDSTRUCTURE_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_STRUCTURE_ID,
                        FTokenContext.KW_ENDSTRUCTURE_ID);
                        break;
                        
                    case FTokenContext.KW_ENDSUBROUTINE_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_SUBROUTINE_ID,
                        FTokenContext.KW_ENDSUBROUTINE_ID);
                        break;
                        
                    case FTokenContext.KW_ENDTYPE_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_TYPE_ID,
                        FTokenContext.KW_ENDTYPE_ID);
                        break;
                        
                    case FTokenContext.KW_ENDUNION_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_UNION_ID,
                        FTokenContext.KW_ENDUNION_ID);
                        break;
                        
                    case FTokenContext.KW_ENDWHERE_ID:
                    case FTokenContext.KW_ELSEWHERE_ID:
                        matchToken = findMatchingToken(token,
                        FTokenContext.KW_WHERE_ID,
                        FTokenContext.KW_ENDWHERE_ID);
                        break;
                }//end second switch
                if (matchToken != null) {
                    indent = getTokenIndent(matchToken);
                }
                break; //end default case
        }//end first switch
        //}//end if
        
        // If indent not found, search back for the first important token
        if (indent < 0) { // if not yet resolved
            if( token == null ) return 0;
            //TokenItem matchToken;
            TokenItem impToken  = findImportantToken(token, null, true);
            TokenItem startToken = findLineStartToken(impToken);
            // in fixed format: line cont. and preprocessors are not treated as important tokens
            if( startToken == null ) return 0;
            while( isFixedFormatLineContinuation(startToken) || isPreprocessor(startToken) || startToken.getTokenID() == FTokenContext.KW_ENTRY){
                impToken = findImportantToken(startToken, null, true);
                startToken = findLineStartToken(impToken);
                if( startToken == null ) return 0;
            }
            if (impToken != null) { // valid important token
                // in fixed format: labels are not treated as start tokens
                while( isFixedFormatLabel(startToken) || startToken.getTokenID() == FTokenContext.WHITESPACE ){
                    startToken = startToken.getNext();
                    if( startToken == null ) return 0;
                }
                //startToken = findLineStartToken(impToken);
                switch (startToken.getTokenID().getNumericID()) {
                    
                    case FTokenContext.KW_DO_ID:
                        if( !getFreeFormat() ){
                            // DO ITERATOR or DO LABEL
                            TokenItem nexToken = startToken.getNext();
                            while( nexToken.getTokenID() == FTokenContext.WHITESPACE ){
                                nexToken = nexToken.getNext();
                            }
                            if( nexToken.getTokenID() == FTokenContext.NUM_LITERAL_INT ){
                                // Don't indent inside DO-LABEL for now
                                indent = getTokenIndent(startToken);
                                break;
                            }
                        }
                        indent = getTokenIndent(startToken) + getShiftWidth();
                        break;
                        
                    case FTokenContext.KW_ELSE_ID:
                    case FTokenContext.KW_CASE_ID:
                    case FTokenContext.KW_WHERE_ID:
                    case FTokenContext.KW_ELSEWHERE_ID:
                    case FTokenContext.KW_BLOCK_ID:
                    case FTokenContext.KW_BLOCKDATA_ID:
                    case FTokenContext.KW_SELECT_ID:
                    case FTokenContext.KW_SELECTCASE_ID:
                    case FTokenContext.KW_PROGRAM_ID:
                    case FTokenContext.KW_SUBROUTINE_ID:
                    case FTokenContext.KW_STRUCTURE_ID:
                    case FTokenContext.KW_INTERFACE_ID:
                    case FTokenContext.KW_FUNCTION_ID:
                    case FTokenContext.KW_MODULE_ID:
                    case FTokenContext.KW_UNION_ID:
                    case FTokenContext.KW_TYPE_ID:
                    case FTokenContext.KW_MAP_ID:
                        indent = getTokenIndent(startToken) + getShiftWidth();
                        break;
                        
                    case FTokenContext.KW_IF_ID:
                    case FTokenContext.KW_ELSEIF_ID:
                        if (isIfThenStatement(startToken)) {
                            indent = getTokenIndent(startToken) +
                            getShiftWidth();
                        } else {
                            indent = getTokenIndent(startToken);
                        }
                        break;
                        
                    default:
                        indent = getTokenIndent(startToken);
                        break;
                }
                
                if (indent < 0) { // no indent found yet
                    indent = getTokenIndent(impToken);
                }
            } // end if (impToken != null)
        }
        
        if (indent < 0) { // no important token found
            indent = 0;
        }
        return indent;
    }
    
    
    /** Determines how many characters the token (after a fixed token)
     * needs to be indented.
     * The indentation is hence done with spaces NOT tabs.
     **/
    public int findInlineSpacing(TokenItem token){
        // fill if short fixed format Label
        int additionalIndent =0;
        TokenItem startToken = findLineStartToken(token);
        if( isFixedFormatLabel(startToken) ){
            additionalIndent = 4 - token.getImage().length();
            startToken = startToken.getNext();
        }
        
        // Search backwards ...
        TokenItem indentToken = findImportantToken(token, null, true);
        startToken = findLineStartToken(indentToken);
        // in fixed format: line cont. and preprocessors are not treated as important tokens
        while( isFixedFormatLineContinuation(startToken) || isPreprocessor(startToken) || startToken.getTokenID() == FTokenContext.KW_ENTRY){
            indentToken = findImportantToken(startToken, null, true);
            startToken = findLineStartToken(indentToken);
        }
        
        // ignore whitespace && fixed format labels
        while( isFixedFormatLabel(startToken) || startToken.getTokenID() == FTokenContext.WHITESPACE ){
            startToken = startToken.getNext();
        }
        
        // check for END Tokens
        while( isFixedFormatLineContinuation(token) || isFixedFormatLabel(token) || token.getTokenID() == FTokenContext.WHITESPACE ){
            token = token.getNext();
        }
        if( token.getTokenID() == FTokenContext.KW_SUBROUTINE
        || token.getTokenID() == FTokenContext.KW_ENTRY
        || token.getTokenID() == FTokenContext.KW_FUNCTION )
            return 6;
        
        // although this is cheap and [PENDING] improvement
        // it was the quickest way without some re-engineering of this class :(
        if( (token.getImage().length() > 2 && token.getImage().substring(0,3).equalsIgnoreCase("end"))  //NOI18N
        || token.getTokenID() == FTokenContext.KW_ELSE || token.getTokenID() == FTokenContext.KW_ELSEIF )
            additionalIndent -= getShiftWidth();
        
        FormatTokenPosition tp1 = getPosition(startToken,0);
        return Math.max(6,getVisualColumnOffset(tp1) + additionalIndent);
    }
    
    public FormatTokenPosition indentLine(FormatTokenPosition pos) {
        int indent = 0; // Desired indent
        
        // Get the first non-whitespace position on the line
        FormatTokenPosition firstNWS = findLineFirstNonWhitespace(pos);
        if (firstNWS != null) { // some non-WS on the line
            
            if (isFixedFormatComment(firstNWS.getToken()) || isPreprocessor(firstNWS.getToken())){//&& getChar(getPreviousPosition(pos)) == '\n' ) {
                // leave indent at 0
            } else if (isFreeFormatComment(firstNWS.getToken())) {
                // comment is first on the line
                // this will do for now XXX
                indent = findIndent(firstNWS.getToken());
                
            } else if (isFixedFormatLabel(firstNWS.getToken()) ) { // fixed format label
                // indent after label token
                TokenItem nexToken = firstNWS.getToken().getNext();
                // remove spaces
                while(nexToken.getTokenID() == getWhitespaceTokenID()){
                    TokenItem nt = nexToken.getNext();
                    removeToken(nexToken);
                    nexToken = nt;
                }
                indent = findInlineSpacing(firstNWS.getToken());
                // add spaces
                for(int i=0; i < indent -5; ++i)
                    insertToken(nexToken, getValidWhitespaceTokenID(),getValidWhitespaceTokenContextPath(), " "); // NOI18N
                // the line's real indent
                indent = 1;
                
            } else if (isFixedFormatLineContinuation(firstNWS.getToken()) ){ // fixed format line continuation
                // indent after line cont. token
                TokenItem nexToken = firstNWS.getToken().getNext();
                // remove spaces
                while(nexToken.getTokenID() == getWhitespaceTokenID()){
                    TokenItem nt = nexToken.getNext();
                    removeToken(nexToken);
                    nexToken = nt;
                }
                indent = findInlineSpacing(firstNWS.getToken());
                // add spaces
                for(int i=0; i < indent - 5; ++i)
                    insertToken(nexToken, getValidWhitespaceTokenID(), getValidWhitespaceTokenContextPath(), " "); // NOI18N
                // the line's real indent
                indent = 5;
                
            } else if (!getFreeFormat() // subroutine, entry, and function always at 6 for fixed format
            && pos.getToken().getTokenID() == FTokenContext.KW_SUBROUTINE
            || pos.getToken().getTokenID() == FTokenContext.KW_ENTRY
            || pos.getToken().getTokenID() == FTokenContext.KW_FUNCTION ){
                indent = 6;
                
            } else { // first non-WS char is not comment
                
                indent = findIndent(firstNWS.getToken());
                if( !getFreeFormat() && indent < 6 ) indent = 6;
            }
        } else {
            // The whole line is WS
            // Can be empty line inside multi-line comment
            TokenItem token = pos.getToken();
            if (token == null) {
                token = findLineStart(pos).getToken();
                if (token == null) { // empty line
                    token = getLastToken();
                }
            }
            if (token != null) {
                indent = findIndent(token);
            } else {
                indent = findIndent(pos.getToken());
            }
        }
        
        // For indent-only always indent
        return changeLineIndent(pos, indent);
    }
    /*
      NO NEED FOR THESE YET
    public boolean getFormatSpaceAfterComma() {
        return getSettingBoolean(FSettingsNames.FORMAT_SPACE_AFTER_COMMA,
                                 FSettingsDefaults.defaultFormatSpaceAfterComma);
    }
     
     */
    public boolean getFreeFormat() {
        //return org.netbeans.modules.cnd.settings.CppSettings.getDefault().isFreeFormatFortran();
        return FortranCodeStyle.get(getFormatWriter().getDocument()).isFreeFormatFortran();
    }
}
