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

package org.netbeans.modules.cnd.modelimpl.trace;

import antlr.Token;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPParser;

/**
 * Misc trace-related utilities
 * @author Vladimir Kvasihn
 */
public class TraceUtils {

    public static String getTokenTypeName(Token token) {
        return getTokenTypeName(token.getType());
    }

    public static String getTokenTypeName(AST ast) {
        return getTokenTypeName(ast.getType());
    }

    public static String getTokenTypeName(int tokenType) {
        try {
            return CPPParser._tokenNames[tokenType];
        }
        catch( Exception e ) {
            return "";
        }
    }
    
}
