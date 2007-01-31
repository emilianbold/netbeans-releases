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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.languages.CharInput;
import org.netbeans.modules.languages.lexer.SLexer.Vojta;
import org.netbeans.modules.languages.parser.Pattern;


class DelegatingInputBridge extends CharInput {

    private InputBridge     input;
    private Pattern         start;
    private Pattern         end;
    private String          tokenType;
    private List            embeddings = new ArrayList ();

    DelegatingInputBridge (
        InputBridge         input, 
        Pattern             start,
        Pattern             end,
        String              tokenType
    ) {
        this.input =        input;
        this.start =        start;
        this.end   =        end;
        this.tokenType =    tokenType;
    }

    public char read () {
        readEmbeddings ();
        return input.read ();
    }

    public void setIndex (int index) {
        input.setIndex (index);
    }

    public int getIndex () {
        return input.getIndex ();
    }

    public char next () {
        readEmbeddings ();
        return input.next ();
    }

    public boolean eof () {
        readEmbeddings ();
        return input.eof ();
    }

    public String getString (int from, int to) {
        return input.getString (from, to);
    }

    public String toString () {
        return input.toString ();
    }
    
    public List getEmbeddings () {
        List e = embeddings;
        embeddings = new ArrayList ();
        return e;
    }
    
    private void readEmbeddings () {
        int startIndex = input.getIndex ();
        if (!input.eof () && start.next (input) != null) {
            while (!input.eof () && end.next (input) == null)
                input.read ();
            embeddings.add (new Vojta (tokenType, startIndex, input.getIndex ()));
        }
    }
}


