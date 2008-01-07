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
package org.netbeans.modules.languages.php;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.php.lexer.PhpTokenId;

/**
 * Implementation notes:
 * this class perform actions that are very similiar to lexer 
 * org.netbeans.modules.php.lexer.InsidePhpStateStrategy state strategy class.
 *  
 * @author ads
 *
 */
class EODTokenizer {
    
    
    /**
     * This is error token.
     */
    private static final String ERROR = "error";

    /**
     * This is type for eod string ( see this type in nbs file ).
     */
    private static final String PHP_EOD_STRING = "php_eod_string";
    
    

    private static final char LINE_FEED                 = '\n';
    
    private static final char LINE_SEP                  = '\r';
    
    private static final Pattern IDENTIFIER_PATTERN     = 
                    Pattern.compile("\\p{Alpha}\\w*");              // NOI18N
    
    
    /*
     * initial state , inside end of document ( after string "<<<" ), 
     * inside identifier recognition
     */
    private static final int IS_INIT            = 0;
    
    /*
     * after good identifier is found ( it matches to IDENTIFIER_PATTERN )
     */
    private static final int ISP_IDENTIFIER     = 1;
    
    /*
     * after identifier is found
     */
    private static final int ISP_BAD_IDENTIFIER = 2;
    
    /*
     * after new line symbol
     */
    private static final int ISA_NEWLINE        = 3;
    
    /*
     * inside identifier comparison with saved identifier
     */
    private static final int ISI_ID_COMP        = 4;
    
    /*
     * Final state : EOD recognized
     */
    private static final int IS_FOUND           = 5;
    
    EODTokenizer( CharInput input ){
        /*
         * input.read() method reads current symbol and increment counter 
         * ( next time this method returns next character )
         * input.next() method reads current symbol and don't update counter
         * ( next time this method returns the same character ).
         */
        myInput = input;
    }
    
    ASTToken getToken() {
        int indx = getInput().getIndex();
        setState( IS_INIT );
        
        while (!getInput().eof () && ( getState() !=IS_FOUND ))
        {
            char currentChar =getInput().read ();
            if (IS_INIT == getState()) {
                handleIdentifierRecordState(currentChar);
            }
            else if ( !(ISP_BAD_IDENTIFIER == getState() )
                    && isLineEnd(currentChar) ) 
            {
                getPretender().delete( 0 , getPretender().length() );
                setState( ISA_NEWLINE );
            }
            else if ( ISA_NEWLINE == getState() ||
                    ISI_ID_COMP == getState() ) 
            {
                handleComparisonAgainstIdentifier(currentChar);
            }
        }
        
        int curIndex = getInput().getIndex();
        
        if ( curIndex == indx ) {
            return null;
        }
        
        String text = getInput().getString(indx, curIndex );
        Language lang;
        try {
            lang = LanguagesManager.getDefault().getLanguage( 
                    PhpTokenId.EMBED_MIME_TYPE );
        }
        catch (LanguageDefinitionNotFoundException e) {
            assert false;
            Logger.getLogger( EODTokenizer.class.getName()).log( Level.SEVERE, 
                    null, e );
            return null;
        }
        if ( getState() == IS_FOUND ) {
            // need to return back ";" symbol that is not part of token
            getInput().setIndex( curIndex -1 );
            
            return ASTToken.create( lang, PHP_EOD_STRING, 
                    text, indx , text.length() , Collections.<ASTItem>emptyList() );
        }
        else {
            return ASTToken.create( lang, ERROR, 
                    text, indx , text.length() , Collections.<ASTItem>emptyList());
        }
    }
    
    private void handleIdentifierRecordState( char currentChar ) {
        if (isLineEnd(currentChar)) {
            /*
             * After ISI_SUB_IDENTIFIER state we put state to 
             * ISA_SUB_NEWLINE, not ISP_SUB_IDENTIFIER,
             * because identifier could be right away after new line.
             * state.setSubstate( ISP_SUB_IDENTIFIER );
             */
            if (checkFoundIdenifier()) {
                setState( ISA_NEWLINE );
            }
            else {
                // this is bad identifier , we should not found it as EOD
                setState(ISP_BAD_IDENTIFIER ); 
            }
        }
        else {
            getIdentifier().append(currentChar);
        }
    }
    
    private boolean checkFoundIdenifier( ) {
        StringBuilder builder = getIdentifier();
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
    
    private void handleComparisonAgainstIdentifier( char currentChar) {
        IdentifierChecker checker = new IdentifierChecker( getIdentifier() , 
                getPretender(), currentChar);
        if ( checker.isIdentifier() ) {
            getPretender().delete( 0 , getPretender().length());
            getIdentifier().delete( 0 , getIdentifier().length() );
            setState( IS_FOUND );
        }
        else if ( checker.matches() ) {
            // just substring of identifier, proceed with next symbol
            getPretender().append( currentChar );
            setState( ISI_ID_COMP );
        }
        else {
            // no match, no identifier, we are inside EOD string , proceed
            // search of EOD symbol further
            getPretender().delete( 0 , getPretender().length());
            setState(ISP_IDENTIFIER );
        }
    }
    
    private CharInput getInput() {
        return myInput;
    }
    
    private StringBuilder getIdentifier() {
        if ( myIdentifer == null ) {
            myIdentifer = new StringBuilder(0);
        }
        return myIdentifer;
    }
    
    private StringBuilder getPretender() {
        if ( myPretender == null ) {
            myPretender = new StringBuilder(0);
        }
        return myPretender;
    }
    
    private int getState() {
        return myState;
    }
    
    private void setState( int state ) {
        myState = state;
    }
    
    private boolean isLineEnd( int ch) {
        return ch == LINE_FEED || ch == LINE_SEP;
    }

    private int myState;
    
    private StringBuilder myIdentifer;
    
    private StringBuilder myPretender;
    
    private CharInput myInput;

}
