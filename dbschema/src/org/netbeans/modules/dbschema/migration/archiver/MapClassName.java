/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.migration.archiver;

/**
 *
 * @author  ludo
 */
public class MapClassName {
    static String LEGACYPREFIX= "com.sun.forte4j.modules.dbmodel.";
    static String CURRENTPREFIX= "org.netbeans.modules.dbschema.";
    
    static public String getClassNameToken(String realClassName){
        if (realClassName.startsWith(CURRENTPREFIX)){
            realClassName = LEGACYPREFIX + realClassName.substring(CURRENTPREFIX.length(),realClassName.length());
        }
        
        return realClassName;
        }
    
    static public String getRealClassName(String token){
        if (token.startsWith(LEGACYPREFIX)){
            token = CURRENTPREFIX + token.substring(LEGACYPREFIX.length(),token.length());
        }
        return token;
    }
    public static void main(String[] args){
        
        String S="org.netbeans.modules.dbschema.jdbcimpl.TEST";
        System.out.println(getClassNameToken(S));
    }
    
}
