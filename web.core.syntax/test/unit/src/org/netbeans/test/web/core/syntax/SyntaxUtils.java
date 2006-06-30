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

package org.netbeans.test.web.core.syntax;

import java.io.PrintStream;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/** Syntax utils class.
 *
 * @author  mf100882
 */
public class SyntaxUtils {

    /** Dumps token context names for given {@link org.netbeans.editor.TokenContextPath} */
    public static void dumpTokenContextPath(TokenContextPath tcp, PrintStream out) {
        TokenContext[] tcs = tcp.getContexts();
        for(int i = 0; i < tcs.length; i++ ) {
            String tcClassName = tcs[i].getClass().getName();
            tcClassName = tcClassName.substring(tcClassName.lastIndexOf(".") + 1);
            out.print(tcClassName + ( (i < (tcs.length - 1)) ? ", " : ""));
        }
    }
    
    /** converts \n to <NL> \t to <TAB> etc... */
    public static String normalize(String s, String[][] translationTable) {
        StringBuffer normalized = new StringBuffer();
        for(int i = 0; i < s.length(); i++) {
            String ch = s.substring(i,i+1);
            for(int j = 0; j < normalizeTable.length; j++) {
                if(ch.equals(normalizeTable[j][0])) ch = normalizeTable[j][1];
            }
            normalized.append(ch);
        }
        return normalized.toString();
    }
    
    /** the some as {@ling normalize(String s, String[][] translationTable)} 
     * but uses default translation table. */
    public static String normalize(String s) {
        return normalize(s, normalizeTable);
    }
    
    public static final String[][] normalizeTable = {{"\n", "<NL>"},
                                                      {"\t", "<TAB>"}};
    
}
