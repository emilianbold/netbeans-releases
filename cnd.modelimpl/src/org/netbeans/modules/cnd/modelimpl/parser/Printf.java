/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelimpl.parser;

/**
 *
 * @author Vladimir Kvashin
 */
public class Printf {

    public static void printf(String pattern, Object[] args) {
        StringBuffer sb = new StringBuffer();
        int from = 0;
        int pos = pattern.indexOf('%');
        int argNumber = 0;
        while( pos >= 0 ) {
            sb.append(pattern.substring(from,  pos));
            from = pos + 2;
            if( argNumber < args.length ) {
                sb.append(args[argNumber] == null ? "null" : args[argNumber].toString()); // NOI18N
            }
            argNumber++;
            pos = pattern.indexOf('%',  from);
        }
        if( from < pattern.length() ) {
            sb.append(pattern.substring(from));
        }
        System.out.print(sb.toString());
    }
    
}
