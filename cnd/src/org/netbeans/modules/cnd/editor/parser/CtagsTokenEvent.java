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

package  org.netbeans.modules.cnd.editor.parser;

public class CtagsTokenEvent {
    private String token = null;
    private char kind = (char)0;
    private String scope = null;
    private int scopeKind = -1;
    private int lineNo = 0;

    public static final int SCOPE_CLASS = 0; // C, C++
    public static final int SCOPE_STRUCT = 1; // C, C++
    public static final int SCOPE_UNION = 2; // C, C++
    public static final int SCOPE_NAMESPACE = 3; // C, C++
    public static final int SCOPE_MODULE = 4; // Fortran
    public static final int SCOPE_TYPE = 5; // Fortran
    public static final int SCOPE_SUBROUTINE = 6; // Fortran
    public static final int SCOPE_BLOCK_DATA = 7; // Fortran
    
    /** Creates a new instance of CtagsParserEvent */
    public CtagsTokenEvent(String token, int lineNo) {
        this.token = token;
        this.lineNo = lineNo;
    }

    
    /** Creates a new instance of CtagsParserEvent */
    public CtagsTokenEvent(String token, char kind, String scope, int scopeKind, int lineNo) {
        this.token = token;
        this.kind = kind;
        this.scope = scope;
        this.scopeKind = scopeKind;
        this.lineNo = lineNo;
    }
    
    public String getToken() {
        return token;
    }
    
    public char getKind() {
        return kind;
    }
    
    public String getScope() {
        return scope;
    }
    
    public int getScopeKind() {
        return scopeKind;
    }
    
    public int getLineNo() {
        return lineNo;
    }
}
