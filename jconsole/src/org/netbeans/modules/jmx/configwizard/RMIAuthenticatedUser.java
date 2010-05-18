/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.configwizard;

import java.io.ByteArrayInputStream;
import java.util.Properties;

/**
 * Class used for authenticate table
 * @author jfdenise
 */
public class RMIAuthenticatedUser {

    private String name;
    private String password;
    private String access = "readonly";// NOI18N

    /** Creates a new instance of RMIAuthenticatedUser */
    public RMIAuthenticatedUser() {
    }

    private static boolean isValidKey(String key) {
       if(key == null || key.equals("")) return false;// NOI18N
        
       boolean precedingBackslash = false;
       char[] val = key.toCharArray();
       int offset = 0;
       while(offset < val.length) {
           char c = val[offset];
           if ((c > 59) && (c < 127)) {
               if (c == '\\') {
                   char aChar = val[offset++];
                   if(aChar == 'u') {
                       // Read the xxxx
                       int value=0;
                       for (int i=0; i<4; i++) {
                           aChar = val[offset++];
                           switch (aChar) {
                               case '0': case '1': case '2': case '3': case '4':
                               case '5': case '6': case '7': case '8': case '9':
                               case 'a': case 'b': case 'c':
                               case 'd': case 'e': case 'f':
                               case 'A': case 'B': case 'C':
                               case 'D': case 'E': case 'F':
                                   break;
                               default:
                                   return false;
                           }
                       }
                   }
                   precedingBackslash = !precedingBackslash;
               } else
                   precedingBackslash = false;
               
               offset++;
               continue;
           }
          
          switch(c) {
              case ':':
              case '=':
              case '#':
              case ' ':    
                if(!precedingBackslash) return false;
          }
          offset++;
       }
       return true;
    }
    
    private String cleanPassword() {  
       if(password == null || password.equals("")) return null; // NOI18N
       return password;
    }
    
    /**
     * Returns if the authenticate user model is valid : there is no empty password 
     * and no empty role name.
     * @return <CODE>boolean</CODE> true if the authenticate user model is valid
     */
    public boolean isValid() {
        return (isValidKey(name)) && (cleanPassword() != null);
    }
    
    /**
     * Returns the :
     *  - role name of this autenticate user if col = 0
     *  - password of this autenticate user if col = 1
     *  - access of this autenticate user if col = 2
     * @param col <CODE>int</CODE> index of column of the authenticate table
     * @return <CODE>String</CODE>
     */
    public Object getValueAt(int col) {
        switch(col) {
            case 0: return name;
            case 1: return password;
            case 2: return access;
        }
        return null;
    }
    
    /**
     * Sets the :
     *  - role name of this autenticate user if col = 0
     *  - password of this autenticate user if col = 1
     *  - access of this autenticate user if col = 2
     * @param value <CODE>Object</CODE> value to set (must be String)
     * @param col <CODE>int</CODE> index of authenticate table column
     */
    public void setValueAt(Object value, int col) {
        switch(col) {
            case 0:  name = (String) value;
            break;
            case 1:  password = (String) value;
            break;
            case 2:  access = (String) value;
            break;
        }
    }
    
    /**
     * Returns the authenticate user name.
     * @return <CODE>String</CODE> user name
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the authenticate user password.
     * @return <CODE>String</CODE> user password
     */
    public String getPassword() {
        return password;
    }
    /**
     * Returns the authenticate user access.
     * @return <CODE>String</CODE> user access
     */
    public String getAccess() {
        return access;
    }
    
}
