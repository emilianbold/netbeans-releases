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
