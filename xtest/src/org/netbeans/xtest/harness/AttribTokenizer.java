/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AttribTokenizer.java
 *
 * Created on October 22, 2001, 1:40 PM
 */

package org.netbeans.xtest.harness;

import java.io.StringReader;
import java.io.PushbackReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author  mk97936
 * @version
 */
public class AttribTokenizer extends PushbackReader implements AttribTypes {                
    
    private ArrayList tokens = new ArrayList();
    private int curpos = -1;
    
    /** Creates new AttribTokenizer */
    public AttribTokenizer( String str ) {

        super ( new StringReader(str) );
        
        AToken atok = getNextToken0();
        tokens.add( atok );    
        do {
            atok = getNextToken0(); 
            tokens.add( atok );
        } while ( atok.type != EOF );
                
    }
    
    public AToken getNextToken() {
        AToken rat = (AToken) tokens.get( ++curpos );
        return ( rat ) ;
    }
    
    public AToken getPeakToken() {
        AToken rat = ( AToken ) tokens.get( curpos + 1 );
        return rat;
    }
    
    private AToken getNextToken0() {
        
        String retstr = null;
        int rettp = 0;
        
        try {
            
            int irc = read ();
            char rc = (char) irc;
            
            if ( Character.isWhitespace( rc ) ) {
                // ignore whitespaces
                while ( Character.isWhitespace( rc ) ) {
                    rc = (char) read();                    
                }
            }
            
            if (irc == -1 || irc == 65535) { // EOF
                                
                retstr = new String( "EOF" );
                rettp = EOF;
                
            } else if ( rc == '(' ) { // OP_PAR
                
                retstr = new String( "(" );
                rettp = OP_PAR;
                
            } else if ( rc == ')' ) { // CL_PAR
                
                retstr = new String( ")" );
                rettp = CL_PAR;
                
            } else if ( rc == '!' ) { // LOG_NOT
                
                retstr = new String( "!" );
                rettp = LOG_NOT;
                
            } else if ( rc == '|' ) { // LOG_OR
                
                rc = (char) read();                
                if ( rc == '|') {
                    retstr = new String( "||" );
                    rettp = LOG_OR;
                } else {
                    unread(rc);                    
                }
                
            } else if ( rc == '&' ) { // LOG_AND
                
                rc = (char) read();                
                if ( rc == '&') {
                    retstr = new String( "&&" );
                    rettp = LOG_AND;
                } else {
                    unread(rc);                    
                }
                
            } else if ( rc == ',' ) { // LOG_OR
                
                char [] chp = new char [] { ',' };
                retstr = new String( chp );
                rettp = LOG_OR;
                
            } else if ( (rc >= 'a' && rc <= 'z') ||
                        (rc >= 'A' && rc <= 'Z') ||
                        (rc >= '0' && rc <= '9') || rc == '-' || rc == '_' ) { // ATTRIBUTE
                
                StringBuffer strb = new StringBuffer();
                while ( (rc >= 'a' && rc <= 'z') ||
                        (rc >= 'A' && rc <= 'Z') ||
                        (rc >= '0' && rc <= '9') || rc == '-' || rc == '_') {
                    strb.append(rc);
                    rc = (char) read();
                }
                unread(rc);
                
                if ( strb.toString().equalsIgnoreCase("and") ) {
                    rettp = LOG_AND;
                    retstr = strb.toString();
                } else if ( strb.toString().equalsIgnoreCase("or") ) {
                    rettp = LOG_OR;
                    retstr = strb.toString();
                } else if ( strb.toString().equalsIgnoreCase("not") ) {
                    rettp = LOG_NOT;
                    retstr = strb.toString();                
                } else {
                    rettp = ATTR;
                    retstr = strb.toString();
                }
                
            } else {
                retstr = null;
                rettp = 0;
            }
            
            return new AToken( rettp, retstr );
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return null;
    }

    public class AToken {
        
        public int type;
        public String value;
        
        public AToken(int tp, String val) {
            type = tp;
            value = val;
        }
        
    }
    
    public static void main(String [] args) {
        
        //AttribTokenizer at = new AttribTokenizer("(!((NOT(amos)and(jouda OR smoula AND not prd)||(trouba))))");        
        //AToken atok = at.getNextToken();
        //while ( atok.type != EOF ) {
        //    
        //    switch ( atok.type ) {
        //        case LOG_AND: System.out.println( "LOG_AND: " + atok.value ); break;
        //        case LOG_OR:  System.out.println( "LOG_OR: " + atok.value ); break;
        //        case LOG_NOT: System.out.println( "LOG_NOT: " + atok.value ); break;
        //        case OP_PAR:  System.out.println( "OP_PAR: " + atok.value ); break;
        //        case CL_PAR:  System.out.println( "CL_PAR: " + atok.value ); break;
        //        case ATTR:    System.out.println( "ATTR: " + atok.value ); break;
        //        case ERR:     System.out.println( "ERR: " + atok.value ); break;
        //        case EOF:     System.out.println( "EOF: " + atok.value ); break;
        //   }
        //               
        //    atok = at.getNextToken();            
        //    
        //}
    }
    
}
