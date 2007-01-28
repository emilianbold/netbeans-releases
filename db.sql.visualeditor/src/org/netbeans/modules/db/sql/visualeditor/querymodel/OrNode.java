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

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

/**
 * Represents a SQL Or term in a clause
 * Example Form: ((a.x = b.y) OR (c.w = d.v))
 */
public class OrNode extends BooleanExpressionList implements Or {

    //
    // Constructors
    //
    public OrNode(ArrayList expressions) {
       _expressions = new ArrayList();
       BooleanExpressionList.flattenExpression(expressions, OrNode.class, _expressions);
    }

    //
    // Methods
    //

    // Return the Where clause as a SQL string
    public String genText() {
        if (_expressions==null)
            return "";    // NOI18N
        String res = " ( " + ((Expression)_expressions.get(0)).genText();    // NOI18N

        for (int i=1; i<_expressions.size(); i++)
            res += " OR " + ((Expression)_expressions.get(i)).genText();  // NOI18N

        res += " ) ";    // NOI18N

        return res;
    }

    public String toString() {
        return "";    // NOI18N
    }

}
