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

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.CharInput;

public class StringInput extends CharInput {

    private String sourceName;
    private String expression;
    private int index = 0;
    private int length;

    public StringInput (String expression, String sourceName) {
        this.expression = expression;
        length = expression.length ();
        this.sourceName = sourceName;
    }

    public char read () {
        if (index < length)
            return expression.charAt (index++);
        return 0;
    }

    public void setIndex (int index) {
        this.index = index;
    }

    public int getIndex () {
        return index;
    }

    public boolean eof () {
        return index >= length;
    }

    public char next () {
        if (index < length)
            return expression.charAt (index);
        return 0;
    }

    public String getString (int from, int to) {
        return expression.substring (from, to);
    }

    public String toString () {
//            String s = expression.substring (
//                index,
//                Math.min (index + 100, expression.length ())
//            );
        int lineNumber = 1;
        int i = expression.indexOf ('\n');
        int offset = 0;
        while (i >= 0 && i < index) {
            lineNumber++;
            i = expression.indexOf ('\n', i + 1);
            if (i >= 0 && (i < index)) offset = i;
        }
        return sourceName + ":" + lineNumber + ',' + (index - offset);
    }
}