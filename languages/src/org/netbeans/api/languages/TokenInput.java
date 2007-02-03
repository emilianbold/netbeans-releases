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

package org.netbeans.api.languages;

import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.SToken;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Parser.Cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class TokenInput {

    public static TokenInput create (
        Parser      parser, 
        CharInput   input, 
        Set         skip
    ) {
        return new TokenInputImpl (
            new TokenReader (parser, input, skip),
            input
        );
    }

    public static TokenInput create (SToken[] array) {
        return new ArrayInput (array);
    }

    public static TokenInput create (List list) {
        return new ListInput (list);
    }

    public abstract SToken read ();

    public abstract void setIndex (int index);

    public abstract int getIndex ();

    public abstract int getOffset ();

    public abstract SToken next (int index);

    public abstract boolean eof ();

    
    // innerclasses ............................................................

    private static class TokenReader {
        
        private Parser      parser;
        private CharInput   input;
        private Set         skip;
        private int         state = -1;
        private Cookie      cookie = new MyCookie ();


        private TokenReader (Parser parser, CharInput input, Set skip) {
            this.parser = parser;
            this.input = input;
            this.skip = skip;
        }

        private SToken next;

        public SToken nextToken (CharInput input) {
            if (next == null)
                next = readToken (input);
            return next;
        }

        public SToken readToken (CharInput input) {
            if (next != null) {
                SToken p = next;
                next = null;
                return p;
            }
            SToken token = null;
            do {
                int start = input.getIndex ();
                token = parser.read (cookie, input);
                if (token == null) return null;
            } while (skip.contains (token.getType ()));
            return token;
        }
    
        private class MyCookie implements Cookie {
            public int getState () {
                return state;
            }

            public void setState (int state) {
                TokenReader.this.state = state;
            }
            
            public void setProperties(Map properties) {
            }
        }
    }

    private static class TokenInputImpl extends TokenInput {

        private TokenReader     tokenReader;
        private List            tokens = new ArrayList ();
        private int             index = 0;
        private CharInput       input;


        TokenInputImpl (TokenReader tokenReader, CharInput input) {
            this.input = input;
            this.tokenReader = tokenReader;
        }

        public SToken next (int i) {
            while (index + i - 1 >= tokens.size ())
                tokens.add (tokenReader.readToken (input));
            return (SToken) tokens.get (index + i - 1);
        }

        public boolean eof () {
            return next (1) == null;
        }

        public int getIndex () {
            return index;
        }

        public int getOffset () {
            SToken t = null;
            if (eof ()) {
                if (getIndex () == 0) return 0;
                t = ((SToken) tokens.get (tokens.size () - 1));
                return t.getOffset () + t.getLength ();
            } else {
                t = (SToken) next (1);
                return t.getOffset ();
            }
        }

        public SToken read () {
            SToken t = next (1);
            if (t != null) index++;
            return t;
        }

        public void setIndex (int index) {
            if (index > tokens.size ()) 
                throw new IndexOutOfBoundsException ();
            this.index = index;
        }

        public String getString (int from) {
            throw new InternalError ();
        }
        
        public String toString () {
            return input.toString ();
        }
    }

    private static class ArrayInput extends TokenInput {

        private SToken[] array;
        private int index = 0;
        private int length;

        private ArrayInput (SToken[] array) {
            this.array = array;
            length = array.length;
        }

        public SToken read () {
            if (index < length)
                return array [index++];
            return null;
        }

        public void setIndex (int index) {
            this.index = index;
        }

        public int getIndex () {
            return index;
        }

        public int getOffset () {
            SToken t = null;
            if (eof ()) {
                if (getIndex () == 0) return 0;
                t = (SToken) array [array.length - 1];
                return t.getOffset () + t.getLength ();
            } else {
                t = (SToken) next (1);
                return t.getOffset ();
            }
        }
        
        public boolean eof () {
            return index >= length;
        }

        public SToken next (int i) {
            if (index + i - 1 < length)
                return array [index + i - 1];
            return null;
        }

        public String toString () {
            StringBuilder sb = new StringBuilder ();
            int i = index, j = 0;
            while (j < 10 && i < length) {
                sb.append (array [i]).append (" ");
                i++; j++;
            }
            return sb.toString ();
        }
    }

    private static class ListInput extends TokenInput {

        private List list;
        private int index = 0;
        private int length;

        private ListInput (List list) {
            this.list = list;
            length = list.size ();
        }

        public SToken read () {
            if (index < length)
                return (SToken) list.get (index++);
            return null;
        }

        public void setIndex (int index) {
            this.index = index;
        }

        public int getIndex () {
            return index;
        }

        public int getOffset () {
            SToken t = null;
            if (eof ()) {
                if (getIndex () == 0) return 0;
                t = ((SToken) list.get (list.size () - 1));
                return t.getOffset () + t.getLength ();
            } else {
                t = (SToken) next (1);
                return t.getOffset ();
            }
        }

        public boolean eof () {
            return index >= length;
        }

        public SToken next (int i) {
            if (index + i - 1 < length)
                return (SToken) list.get (index + i - 1);
            return null;
        }

        public String toString () {
            StringBuilder sb = new StringBuilder ();
            int i = index, j = 0;
            while (j < 10 && i < length) {
                sb.append (list.get (i)).append (" ");
                i++; j++;
            }
            return sb.toString ();
        }
    }
}