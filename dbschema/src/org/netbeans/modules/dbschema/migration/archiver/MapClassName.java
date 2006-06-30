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
