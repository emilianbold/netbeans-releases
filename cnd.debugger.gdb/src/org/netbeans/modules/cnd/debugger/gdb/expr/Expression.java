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

package org.netbeans.modules.cnd.debugger.gdb.expr;


/**
 *
 * @author gordonp
 */
public class Expression {
    
    public static final     String LANGUAGE_CPLUSPLUS = "C++"; // NOI18N
    public static final     String LANGAUGE_C = "C"; // NOI18N
    
    private String          strExpression;
    private String          language;
    
    public static Expression parse(String expr, String language) throws ParseException {
        System.err.println("Expression.parse: expr = " + expr); // NOI18N
        if (expr == null) {
            throw new ParseException();
        }
        
        return null;
    }
    
    /** Creates a new instance of Expression */
    private Expression(String strExpression, String language) {
        this.strExpression = strExpression;
        this.language = language;
    }
    
    public String getExpression() {
        return strExpression;
    }
    
    public String getLanguage() {
        return language;
    }
}
