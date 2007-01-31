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

package org.netbeans.modules.languages.lexer;

import org.netbeans.api.languages.CharInput;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.spi.lexer.LexerInput;


class InputBridge extends CharInput {

    private LexerInput input;
    private int index = 0;
    private Pattern start;
    private Pattern end;

    
    InputBridge (LexerInput input) {
        this.input = input;
    }

    public char read () {
        index++;
        return (char) input.read ();
    }

    public void setIndex (int index) {
        while (this.index < index)
            read ();
        input.backup (this.index - index);
        this.index = index;
    }

    public int getIndex () {
        return index;
    }

    public char next () {
        char ch = (char) input.read ();
        input.backup (1);
        return ch;
    }

    public boolean eof () {
        return next () == (char) input.EOF;
    }

    public String getString (int from, int to) {
        return input.readText ().toString ();
    }

    public String toString () {
        return input.readText ().toString ();
    }
}


