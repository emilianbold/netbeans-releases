/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    private String access = "readonly";
    
    /** Creates a new instance of RMIAuthenticatedUser */
    public RMIAuthenticatedUser() {
    }
    
    private static boolean isValidKey(String key) {
       if(key == null || key.equals("")) return false;
        
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
       if(password == null || password.equals("")) return null; 
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
