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
package org.netbeans.modules.php.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.php.lexer.State.States;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;


/**
 * @author ads
 *
 */
abstract class InsidePhpStateStrategy extends AbstractStateStrategy 
    implements StateStrategy 
{
    private static final char DOUBLE_QUOTE              = '"';
    
    private static final char QUOTE                     = '\'';
    
    private static final char BACK_QUOTE                = '`';
    
    private static final char DOCUMENT_HERE_SYMBOL      = '<'; 
    
    private static final char SLASH                     = '/';
    
    private static final char BACK_SLASH                = '\\';
    
    private static final char STAR                      = '*';
    
    private static final char DIEZ                      = '#';
    
    private static final Pattern IDENTIFIER_PATTERN     = 
        Pattern.compile("\\p{Alpha}\\w*");                         // NOI18N
    
    private static final char LINE_FEED                 = '\n';
    
    private static final char LINE_SEP                  = '\r';
    
    

    /*
     *  initial state identifier : just inside php block, not inside
     *  some php string 
     */
    private static final Integer IS_SUB_INIT        = 0 ;
    
    /*
     * after slash "/" was found
     */
    private static final Integer ISA_SUB_SLASH      = 1;
    
    /*
     * after start of C-comment "/*"
     */
    private static final Integer ISA_SUB_C_COMMENT  = 2;
    
    /*
     * inside C-comment, after "*" is found
     */
    private static final Integer ISI_SUB_STAR       = 3;
    
    /*
     * after '//' or '#'( inside line comment ) 
     */
    private static final Integer ISA_LINE_COMMENT   = 4;
    
    /*
     * state that signal about was found non-comment directive symbol 
     * and previously red symbols was backed up. 
     */
    private static final Integer 
                    IS_SUB_NOT_COMMENT_SYMBOL       = 5;
    
    /*
     * after single quote ' was found
     */
    private static final Integer ISA_SUB_QUOTE      = 6;
    
    /*
     * after back slash ( with previous state ISA_SUB_QUOTE )
     */
    private static final Integer ISA_SUB_QUOTE_SLASH= 7;
    
    /*
     * after single back quote ` was found
     */
    private static final Integer ISA_SUB_BACK_QUOTE = 8;
    
    /*
     * after back slash ( with previous state ISA_SUB_BACK_QUOTE )
     */
    private static final Integer 
                           ISA_SUB_BACK_QUOTE_SLASH = 9;
    
    /*
     * after double quote " was found
     */
    private static final Integer ISA_SUB_DQUOTE     = 10;
    
    /*
     * after back slash ( with previous state ISA_SUB_DQUOTE )
     */
    private static final Integer 
                            ISA_SUB_DQUOTE_SLASH    = 11;
    
    /*
     * after symbol "<" was found
     */
    private static final Integer ISA_SUB_LT         = 12;

    /*
     * after string "<<" was found
     */
    private static final Integer ISA_SUB_LTLT       = 13;
    
    
    /*
     * inside end of document ( after string "<<<" was found ), 
     * inside identifier recognition
     */
    private static final Integer ISI_SUB_IDENTIFIER = 14;
    
    /*
     * after good identifier is found ( it matches to IDENTIFIER_PATTERN )
     */
    private static final Integer ISP_SUB_IDENTIFIER = 15;
    
    /*
     * after identifier is found
     */
    private static final Integer ISP_SUB_BAD_IDENTIFIER 
                                                    = 16;
    
    /*
     * after new line symbol
     */
    private static final Integer ISA_SUB_NEWLINE    = 17;
    
    /*
     * inside identifier comparison with saved identifier
     */
    private static final Integer ISI_SUB_ID_COMP    = 18;

    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
     */
    public States getState() {
        return States.ISI_PHP;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int, org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
     */
    public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
            State state, TokenFactory<PhpTokenId> factory )
    {
        if ( !isEndSymbolFound ( state, currentChar , input )){
            if ( handleComments( state , currentChar , input )){
                return null;
            }
            if ( handleQuoteStateRelated( state , currentChar)){
                return null;
            }
           
            if ( currentChar == DOCUMENT_HERE_SYMBOL ){
                handleLeftAngleBracket(input, state);
            }
            else if  ( stateRelatesToEOD(state) ){
                handleSymbol(currentChar, input, state);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
     */
    public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
            TokenFactory<PhpTokenId> factory ) 
    {
        state.setCurrentState( States.INIT );
        return token(PhpTokenId.PHP , input , state.getCurrentState() , 
                factory);
    }
    

    private boolean handleComments( State state, int currentChar , 
            LexerInput input) 
    {
        if ( state.getSubstate() == IS_SUB_NOT_COMMENT_SYMBOL ){
            state.setSubstate( IS_SUB_INIT );
        }
        else if ( isCommentRelated( state )){
            switch( currentChar ){
                case SLASH:
                    return handleSlashInComments(state, currentChar);
                case STAR:
                    return handleStarInComments( state , currentChar );
                case DIEZ :
                    if ( isInitState(state) ){
                        state.setSubstate( ISA_LINE_COMMENT );
                        return true;
                    }
                    else {
                        return false;
                    }
                default:
                    return handleDefaultCharInComments( state , currentChar , 
                            input ); 
            }
        }
        return false;
    }
    
    private boolean handleDefaultCharInComments( State state, int currentChar,
            LexerInput input )
    {
        if ( isLineEnd(currentChar) && 
                state.getSubstate() ==  ISA_LINE_COMMENT)
        {
            state.setSubstate( IS_SUB_INIT );
            return true;
        }
        if ( state.getSubstate() == ISA_SUB_SLASH ){
            state.setSubstate( IS_SUB_NOT_COMMENT_SYMBOL );
            // backup current symbol and previous "/"
            input.backup( 2 );
        }
        else if ( state.getSubstate() == ISI_SUB_STAR ){
            state.setSubstate( ISA_SUB_C_COMMENT );
        }
        if ( !isInitState(state) )
        {
            return true;
        }
        return false;
    }

    private boolean handleStarInComments( State state, int currentChar ) {
        if ( state.getSubstate() == ISA_SUB_SLASH ){
            state.setSubstate( ISA_SUB_C_COMMENT );
            return true;
        }
        else if ( state.getSubstate() == ISA_SUB_C_COMMENT ){
            state.setSubstate( ISI_SUB_STAR );
            return true;
        }
        return false;
    }

    private boolean handleSlashInComments( State state, int currentChar ){
        if ( isInitState(state) )
        {
            state.setSubstate( ISA_SUB_SLASH);
            return true;
        }
        else if (  state.getSubstate() == ISI_SUB_STAR ){
            state.setSubstate( IS_SUB_INIT );
            return true;
        }
        else if ( state.getSubstate()  == ISA_SUB_SLASH ){
            state.setSubstate( ISA_LINE_COMMENT );
            return true;
        }
        return false;
    }
    
    private boolean isEndSymbolFound( State state, int currentChar , 
            LexerInput input) 
    {
        boolean result = false;
        if (currentChar == '?'
                && state.getCurrentState() == States.ISI_PHP
                && (isInitState(state) || state.getSubstate() == ISA_LINE_COMMENT))
        {
            int nextChar = input.read();
            if ( nextChar == '>' ){
                state.setCurrentState(States.ISP_PHP);
                result = true;
            }
            input.backup( 1 );
        }
        else if (currentChar == '%'
                && state.getCurrentState() == States.ISI_SCRIPTLET
                && (isInitState(state) || state.getSubstate() == ISA_LINE_COMMENT))
        {
            int nextChar = input.read();
            if ( nextChar == '>' ){
                state.setCurrentState(States.ISP_SCRIPTLET_PC);
                result = true;
            }
            input.backup( 1 );
        }
        return result;
    }


    /**
     * @return
     */
    private boolean handleQuoteStateRelated( State state, int currentChar) {
        if ( isQuote(currentChar) ) 
        {
            if ( getStateByQuote(currentChar).equals(state.getSubstate())) {
                state.setSubstate( IS_SUB_INIT );
                return true;
            }
            else if ( isInitState(state) )
            {
                state.setSubstate( getStateByQuote(currentChar) );
                return true;
            }
        }
        else if ( currentChar == BACK_SLASH ){
            boolean isSecondSlash = defaultHandleQuoteState(state);
            if ( isSecondSlash ){
                return true;
            }
            
            Object newState = getSlashState(state);
            if ( newState != null ){
                state.setSubstate( newState );
                return true;
            }
        }
        return defaultHandleQuoteState(state);
    }
    
    private boolean defaultHandleQuoteState( State state ){
        Object newState = getStateByBackSlashState(state);
        if ( newState != null ){
            state.setSubstate( newState );
            return true;
        }
        return false;
    }
    
    private boolean isInitState(State state){
        return state.getSubstate() == null || state.getSubstate() == IS_SUB_INIT;
    }
    
    private boolean isQuote( int ch ) {
        return ch == QUOTE || ch == BACK_QUOTE || ch == DOUBLE_QUOTE;
    }
    
    private boolean isCommentRelated( State state ) {
        Object subState = state.getSubstate();
        if ( subState == null ){
            return true;
        }
        assert subState instanceof Integer;
        int num = ((Integer)subState).intValue();
        return num >= IS_SUB_INIT && num < IS_SUB_NOT_COMMENT_SYMBOL;
    }
    
    private Integer getStateByQuote( int ch ) {
        switch (ch) {
            case QUOTE:
                return ISA_SUB_QUOTE;
            case BACK_QUOTE:
                return ISA_SUB_BACK_QUOTE;
            case DOUBLE_QUOTE:
                return ISA_SUB_DQUOTE;
            default:
                assert false;
        }
        return -1;
    }
    
    private Integer getSlashState( State state ) {
        Object subState = state.getSubstate();
        if ( ISA_SUB_BACK_QUOTE == subState ){
            return ISA_SUB_BACK_QUOTE_SLASH;
        }
        if ( ISA_SUB_DQUOTE == subState ){
            return ISA_SUB_DQUOTE_SLASH;
        }
        if ( ISA_SUB_QUOTE == subState ){
            return ISA_SUB_QUOTE_SLASH;
        }
        return null;
    }
    
    private Object getStateByBackSlashState( State state ){
        Object subState = state.getSubstate();
        if ( ISA_SUB_BACK_QUOTE_SLASH == subState ){
            return ISA_SUB_BACK_QUOTE;
        }
        if ( ISA_SUB_DQUOTE_SLASH == subState ){
            return ISA_SUB_DQUOTE;
        }
        if ( ISA_SUB_QUOTE_SLASH == subState ){
            return ISA_SUB_QUOTE;
        }
        return null;
    }
    
    private void handleLeftAngleBracket( LexerInput input, State state) {
        if ( isInitState(state) )
        {
            state.setSubstate( ISA_SUB_LT );
        }
        else if ( ISA_SUB_LT.equals( state.getSubstate() )) {
            state.setSubstate( ISA_SUB_LTLT );
        }
        else if ( ISA_SUB_LTLT.equals( state.getSubstate() )) {
            state.setSubstate( ISI_SUB_IDENTIFIER );
        }
    }
    

    private void handleSymbol( int currentChar, LexerInput input, State state ) {
        if ( ISA_SUB_LT.equals( state.getSubstate()) || 
                ISA_SUB_LTLT.equals( state.getSubstate()) )
        {
            state.setSubstate( IS_SUB_INIT );
        }
        else if (ISI_SUB_IDENTIFIER.equals(state.getSubstate())) {
            handleIdentifierRecordState(currentChar, state);
        }
        else if ( !ISP_SUB_BAD_IDENTIFIER.equals( state.getSubstate() )
                && isLineEnd(currentChar) ) 
        {
            state.setCurrentData( null );
            state.setSubstate( ISA_SUB_NEWLINE );
        }
        else if ( ISA_SUB_NEWLINE.equals( state.getSubstate()) ||
                ISI_SUB_ID_COMP.equals( state.getSubstate()) ) 
        {
            handleComparisonAgainstIdentifier(currentChar, state);
        }
        
    }

    private void handleComparisonAgainstIdentifier( int currentChar, State state ) {
        IdentifierChecker checker = new IdentifierChecker( state , 
                currentChar);
        if ( checker.isIdentifier() ) {
            state.setCurrentData( null );
            state.setSavedData( null );
            state.setSubstate( IS_SUB_INIT );
        }
        else if ( checker.matches() ) {
            // just substring of identifier, proceed with next symbol
            state.getCurrentData().append( (char)currentChar );
            state.setSubstate( ISI_SUB_ID_COMP );
        }
        else {
            // no match, no identifier, we are inside EOD string , proceed
            // search of EOD symbol further
            state.setCurrentData( null );
            state.setSubstate(ISP_SUB_IDENTIFIER );
        }
    }

    private void handleIdentifierRecordState( int currentChar, State state ) {
        if (isLineEnd(currentChar)) {
            /*
             * After ISI_SUB_IDENTIFIER state we put state to 
             * ISA_SUB_NEWLINE, not ISP_SUB_IDENTIFIER,
             * because identifier could be right away after new line.
             * state.setSubstate( ISP_SUB_IDENTIFIER );
             */
            if (checkFoundIdenifier(state)) {
                state.setSubstate( ISA_SUB_NEWLINE );
            }
            else {
                // this is bad identifier , we should not found it as EOD
                state.setSubstate( ISP_SUB_BAD_IDENTIFIER ); 
            }
        }
        else {
            StringBuilder builder = state.getSavedData();
            if (builder == null) {
                builder = new StringBuilder();
                state.setSavedData(builder);
            }
            builder.append((char) currentChar);
        }
    }

    private boolean checkFoundIdenifier( State state ) {
        StringBuilder builder = state.getSavedData();
        if ( builder == null ) {
            return false;
        }
        String id = builder.toString();
        String trimed = id.trim();
        int indx = id.indexOf( trimed );
        String tail = id.substring(indx);
        if ( !tail.equals( trimed ) ) {
            // id contains spaces at the end, but shouldn't 
            return false;
        }
        Matcher matcher = IDENTIFIER_PATTERN.matcher( trimed );
        return matcher.matches();
    }
    
    private boolean stateRelatesToEOD( State state ) {
        Integer subState = (Integer)state.getSubstate();
        if ( subState == null ) {
            return false;
        }
        return subState > ISA_SUB_DQUOTE;
    }
    
    
    private static boolean isLineEnd( int ch) {
        return ch == LINE_FEED || ch == LINE_SEP;
    }
    
}
