/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AttribParser.java
 *
 * Created on October 23, 2001, 9:40 AM
 */

package org.netbeans.xtest.harness;

import org.netbeans.xtest.harness.AttribTokenizer;
import org.netbeans.xtest.harness.AttribTokenizer.AToken;

import java.util.Collection;

/**
 *
 * @author  mk97936
 * @version
 */
public class AttribParser implements AttribTypes {

    private AttribTokenizer tokenizer;
    private AToken at;
    private Collection attribs;

    /** Creates new AttribParser */
    public AttribParser( String str, Collection attrs ) {
        tokenizer = new AttribTokenizer( str );
        attribs = attrs;
    }
    
    public boolean parse() {        
        return S();        
    }
    
    private boolean S() {
        
        boolean G_res = false;
        
        G_res = G(); 
        
        if ( G_res && compareNext( EOF ) ) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean T() {
        
        boolean G_res = false, T_res = false, ret = false;
        
        at = tokenizer.getPeakToken();        
        switch ( at.type ) {
            case OP_PAR: compareNext( OP_PAR ); G_res = G(); compareNext( CL_PAR );
                         ret = G_res;
                break;
            case ATTR: String rattr = tokenizer.getNextToken().value;                       
                       if ( attribs.contains( rattr ) ) {                           
                           ret = true;
                       } else {
                           ret = false;
                       }
                break;
            case LOG_NOT: compareNext( LOG_NOT ); T_res = T();
                          ret = !T_res;
            default : ;
        }
        
        return ret;
        
    }
    
    private boolean F() {
        
        boolean F_res = false, T_res = false, ret = false;

        T_res = T();
        ret = T_res;
        
        at = tokenizer.getPeakToken();
        switch ( at.type ) {
            case LOG_AND: compareNext( LOG_AND ); F_res = F();
                          if ( T_res && F_res ) {                              
                              ret = true;
                          } else {
                              ret = false;
                          }
                break;
            case LOG_OR: compareNext( LOG_OR ); F_res = F();
                         if ( T_res || F_res ) {                             
                             ret = true;
                         } else {
                             ret = false;
                         } 
                break;
            default : ;
        }
        
        return ret;
        
    }
    
    private boolean G() {
        
        boolean F_res = false, ret = false;                
        
        at = tokenizer.getPeakToken();
        switch ( at.type ) {
            case LOG_NOT: compareNext( LOG_NOT ); F_res = F();
                          ret = !F_res;
                break;
            case OP_PAR:
            case ATTR:
            default : F_res = F();
                      ret = F_res;
        }
        
        return ret;
    }
    
    private boolean compareNext ( int ct ) {
        
        at = tokenizer.getNextToken();
        
        if ( at.type == ct ) {            
            return true;
        } else {            
            return false;
        }
        
    }
        
    public static void main ( String [] args ) {        
        //java.util.ArrayList attriblist = new java.util.ArrayList ();
        //attriblist.add("stable");
        //attriblist.add("code");
        //attriblist.add("oracle");
        //System.out.println("Result = " + new AttribParser( "(stable OR code) AND not oracle", attriblist ).parse () );
    }
    
}
