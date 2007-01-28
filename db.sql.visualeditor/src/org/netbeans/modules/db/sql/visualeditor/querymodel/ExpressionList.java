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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.db.sql.visualeditor.querymodel;

/*
 * ExpressionList.java
 *
 * Created on March 23, 2005, 2:11 PM
 */

// AND and OR operators are defined as list of expressions.
// The reason is mostly due to the need of the editor to see expressions in a "linear" form and changing this to be a binary expression (which it should be)
// is too much of a change
// The api here is very similar to that of a List though it is properly typed.
public interface ExpressionList extends Expression {
    public int size();
    public Expression getExpression(int i);
    public void addExpression(int index, Expression expression);
    public void addExpression(Expression expression);
    public void replaceExpression(int index, Expression expression);
    public void removeExpression(int index);
    public void removeTable(String tableSpec);
}
