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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Pattern {
    
    static final Character STAR = new Character ((char) 0);
    private static int counter = 1;

    
    public static Pattern create () {
        return new Pattern ();
    }
    
    public static Pattern create (String input, String mimeType) throws ParseException {
        if (input.length () == 0) throw new ParseException ();
        return create (Input.create (input, ""), mimeType);
    }
    
    public static Pattern create (Input input, String mimeType) throws ParseException {
        Pattern p = createIn (input, mimeType);
        DG ndg = DGUtils.reduce (p.dg);
        return new Pattern (ndg);
    }
    
    private static Pattern createIn (Input input, String mimeType) throws ParseException {
        Pattern pattern = new Pattern ();
        Pattern last = null;
        char ch = input.next ();
        while (ch != 0) {
            switch (ch) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    input.read ();
                    break;
                case '*':
                    input.read ();
                    if (last == null) throw new ParseException ();
                    last = last.star ();
                    break;
                case '?':
                    input.read ();
                    if (last == null) throw new ParseException ();
                    last = last.question ();
                    break;
                case '+':
                    input.read ();
                    if (last == null) throw new ParseException ();
                    last = last.plus ();
                    break;
                case '(':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = createIn (input, mimeType);
                    if (input.read () != ')')
                        throw new ParseException (") expected: " + input);
                    break;
                case '<':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = new Pattern (readToken (input, mimeType));
                    if (input.read () != '>')
                        throw new ParseException ("> expected: " + input);
                    break;
                case '\'':
                case '"':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = Pattern.create ();
                    StringBuilder sb = new StringBuilder ();
                    ch = input.next ();
                    while (ch != '"' && ch != '\'') {
                        if (ch == 0)
                            throw new ParseException (sb.toString ());
                        if (ch == '\\') {
                            input.read ();
                            switch (input.next ()) {
                                case '\\':
                                    input.read ();
                                    last = last.append (new Pattern (
                                        new Character ('\\')
                                    ));
                                    sb.append ('\\');
                                    break;
                                case 'n':
                                    input.read ();
                                    last = last.append (new Pattern (
                                        new Character ('\n')
                                    ));
                                    sb.append ('\n');
                                    break;
                                case 'r':
                                    input.read ();
                                    last = last.append (new Pattern (
                                        new Character ('\r')
                                    ));
                                    sb.append ('\r');
                                    break;
                                case 't':
                                    input.read ();
                                    last = last.append (new Pattern (
                                        new Character ('\t')
                                    ));
                                    sb.append ('\t');
                                    break;
                                case '"':
                                    input.read ();
                                    last = last.append (new Pattern (
                                        new Character ('"')
                                    ));
                                    sb.append ('"');
                                    break;
                                case '\'':
                                    input.read ();
                                    last = last.append (new Pattern (
                                        new Character ('\'')
                                    ));
                                    sb.append ('\'');
                                    break;
                                default:
                                    throw new ParseException ("Unknown character after \\:" + input.toString ());
                            }
                        } else {
                            Character charr = new Character (input.read ());
                            last = last.append (new Pattern (charr));
                            sb.append (charr.charValue ());
                        }
                        ch = input.next ();
                    }
                    input.read ();
                    break;
                case '|':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = null;
                    pattern = pattern.merge (createIn (input, mimeType));
                    return pattern;
                case '-':
                    if (last != null) pattern = pattern.append (last);
                    input.read ();
                    skipWhitespaces (input);
                    ch = input.next ();
                    if (ch != '\'' && ch != '"')
                        throw new ParseException (input.toString ());
                    input.read ();
                    ch = input.next ();
                    if (ch == '\'' || ch == '"')
                        throw new ParseException (input.toString ());
                    Object edge = new Character (input.next ());
                    last = new Pattern (true, Collections.singleton (edge));
                    last = last.star ().append (new Pattern (edge));
                    input.read ();
                    ch = input.next ();
                    while (ch != '\'' && ch != '"') {
                        if (ch == 0)
                            throw new ParseException (input.toString ());
                        last = last.plus ();
                        Object endN = last.getDG ().getEnds ().iterator ().next ();
                        Object newE = last.createNode ();
                        last.getDG ().addEdge (endN, newE, new Character (input.next ()));
                        last.getDG ().setEnds (Collections.singleton (newE));
                        input.read ();
                        ch = input.next ();
                    }
                    input.read ();
                    break;
                case ')':
                    if (last != null) pattern = pattern.append (last);
                    return pattern;
                case '.':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    last = new Pattern (Pattern.STAR);
                    break;
                case '[':
                    input.read ();
                    if (last != null) pattern = pattern.append (last);
                    boolean not = false;
                    ch = input.next ();
                    if (ch == '^') {
                        input.read ();
                        ch = input.next ();
                        not = true;
                    }
                    Set set = new HashSet ();
                    char l = (char) 0;
                    boolean minus = false;
                    ch = input.next ();
                    while (ch != ']' && ch != 0) {
                        switch (ch) {
                            case ' ':
                            case '\t':
                            case '\n':
                            case '\r':
                                input.read ();
                                break;
                            case '\'':
                            case '"':
                                char ol = l;
                                if (l != 0 && !minus) 
                                    set.add (new Character (l));
                                input.read ();
                                ch = input.next ();
                                if (ch == '\\') {
                                    input.read ();
                                    ch = input.next ();
                                    switch (ch) {
                                        case 'n':
                                            l = '\n';
                                            break;
                                        case 't':
                                            l = '\t';
                                            break;
                                        case 'r':
                                            l = '\r';
                                            break;
                                        case '\'':
                                            l = '\'';
                                            break;
                                        case '\\':
                                            l = '\\';
                                            break;
                                        case '"':
                                            l = '"';
                                            break;
                                        default:
                                            throw new ParseException (input.toString ());
                                    } // switch
                                    input.read ();
                                } else // if '\\'
                                    l = input.read ();
                                ch = input.next ();
                                if (ch != '"' && ch != '\'')
                                    throw new ParseException (input.toString ());
                                input.read ();
                                if (minus) {
                                    addInterval (set, ol, l);
                                    l = 0;
                                }
                                minus = false;
                                break; // case '"'
                            case '-':
                                input.read ();
                                if (l == 0) throw new ParseException (input.toString ());
                                minus = true;
                                break;
                            case '<':
                                input.read ();
                                if (minus) throw new ParseException (input.toString ());
                                if (l != 0) 
                                    set.add (new Character (l));
                                set.add (readToken (input, mimeType));
                                if (input.read () != '>')
                                    throw new ParseException ("> expected: " + input);
                                break;
                            default:
                                throw new ParseException (input.toString ());
                        } // switch
                        ch = input.next ();
                    } // while
                    if (minus) throw new ParseException ();
                    if (l != 0) 
                        set.add (new Character (l));
                    input.read ();
                    last = new Pattern (not, set);
                    break;
                default:
                    throw new ParseException ("Unexpected char '" + input.next () + ":" + input.toString ());
//                    input.read ();
//                    if (last != null) pattern = pattern.append (last);
//                    last = new Pattern (new Character (ch));
            } // switch
            ch = input.next ();
        } // while
        if (last != null) pattern = pattern.append (last);
        return pattern;
    }

    private static SToken readToken (Input input, String mimeType) throws ParseException {
        StringBuilder sb = new StringBuilder ();
        char ch = input.next ();
        while (ch != ',' && ch != '>') {
            if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
            sb.append (ch);
            input.read ();
            ch = input.next ();
        }
        ch = input.next ();
        String type = sb.toString ().trim ();
        if (ch == '>') return SToken.create (mimeType, type, null);
        input.read ();
        skipWhitespaces (input);
        sb = new StringBuilder ();
        ch = input.next ();
        boolean read = ch != '"' && ch != '\'';
        if (!read) {
            input.read ();
            ch = input.next ();
        }
        while (ch != '>' && ch != '"' && ch != '\'' && ch != ',') {
            if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
            sb.append (ch);
            input.read ();
            ch = input.next ();
        }
        if (read && (ch == '"' || ch == '\'')) throw new ParseException ("Unexpected \":" + input.toString ());
        if (!read) input.read ();
        String identifier = null;
        String name = null;
        if (read) name = sb.toString ();
        else identifier = sb.toString ();
        if (!read && ch == ',') {
            ch = input.next ();
            sb = new StringBuilder ();
            while (ch != '>') {
                if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
                sb.append (ch);
                input.read ();
                ch = input.next ();
            }
            name = sb.toString ();
        }
        return SToken.create (mimeType, type, identifier);
    }
    
    private static Set whitespace = new HashSet ();
    static {
        whitespace.add (new Character (' '));
        whitespace.add (new Character ('\n'));
        whitespace.add (new Character ('\r'));
        whitespace.add (new Character ('\t'));
    }
    
    private static void skipWhitespaces (Input input) {
        while (whitespace.contains (new Character (input.next ())))
            input.read ();
    }
    
    private static void addInterval (Set set, char from, char to) 
    throws ParseException {
        if (from > to) throw new ParseException ();
        do {
            set.add (new Character (from));
            from++;
        } while (from <= to);
    }
    
    private DG dg = DG.createDG ();
    
    private Pattern (DG dg) {
        this.dg = dg;
    }
    
    private Pattern () {
        Object start = createNode ();
        dg.setStart (start);
        dg.addEnd (start);
    }

    private Pattern (Object edge) {
        Object start = createNode ();
        Object end = createNode ();
        dg.addEdge (start, end, edge);
        dg.setStart (start);
        dg.addEnd (end);
    }

    private Pattern (boolean not, Set edges) {
        Object start = createNode ();
        Object end = createNode ();
        dg.setStart (start);
        Iterator it = edges.iterator ();
        while (it.hasNext ()) {
            Object edge = it.next ();
            dg.addEdge (start, end, edge);
        }
        if (not) {
            Object failedState = createNode ();
            dg.addEdge (start, failedState, Pattern.STAR);
            dg.addEnd (failedState);
        } else 
            dg.addEnd (end);
    }
    
    private Object createNode () {
        Object node = new Integer (counter ++);
        dg.addNode (node);
        return node;
    }
    
    public Pattern clonePattern () {
        Pattern p = new Pattern (getDG ().cloneDG (false));
        return p;
    }

    public Pattern star () {
        DG ndg = DGUtils.plus (dg);
        ndg = DGUtils.merge (DG.createDG (createNode ()), ndg);
        Pattern p = new Pattern (ndg);
        p.fix ();
        return p;
    }

    public Pattern plus () {
        DG ndg = DGUtils.plus (dg);
        Pattern p = new Pattern (ndg);
        p.fix ();
        return p;
    }

    public Pattern question () {
        DG ndg = dg.cloneDG (true);
        ndg.addEnd (ndg.getStartNode ());
        Pattern p = new Pattern (ndg);
        p.fix ();
        return p;
    }

    public Pattern merge (Pattern parser) {
        DG ndg = DGUtils.merge (dg, parser.dg);
        Pattern p = new Pattern (ndg);
        p.fix ();
        return p;
    }

    public Pattern append (Pattern parser) {
        DG ndg = DGUtils.append (dg, parser.dg);
        Pattern p = new Pattern (ndg);
        p.fix ();
        return p;
    }

    private void fix () {
        Set oldEnds = dg.getEnds ();
        dg.setEnds (new HashSet ());
        Iterator it = new HashSet (dg.getNodes ()).iterator ();
        while (it.hasNext ()) {
            Object state = it.next ();
            Object newState = createNode ();
            dg.changeKey (state, newState);
            if (oldEnds.contains (state))
                dg.addEnd (newState);
            if (dg.getStartNode ().equals (state))
                dg.setStart (newState);
        }
    }
    
    private void replaceNode (Object state, Object by) {
        replaceNode (dg.getStartNode (), state, by, new HashSet ());
        dg.removeEnd (state);
        dg.removeNode (state);
    }

    private void replaceNode (
        Object next, 
        Object replace, 
        Object by, 
        Set resolved
    ) {
        if (resolved.contains (next)) return;
        resolved.add (next);
        Iterator it = dg.getEdges (next).iterator ();
        while (it.hasNext ()) {
            Object edge = it.next ();
            Object dest = dg.getNode (next, edge);
            if (dest == replace)
                dg.addEdge (next, by, edge);
            if (dest != null)
                replaceNode (dest, replace, by, resolved);
        }
    }

    boolean matches (String text) {
        int i = 0;
        Object state = dg.getStartNode ();
        while (i < text.length ()) {
            state = dg.getNode (state, new Character (text.charAt (i++)));
            if (state == null) return false;
        }
        return dg.getEnds ().contains (state);
    }

    public Object next (Input input) {
        return next (dg.getStartNode (), input);
    }
    
    public Object next (Object state, Input input) {
        int lastIndex = input.getIndex ();
        int originalIndex = lastIndex;
        Object lastState = null;
        while (state != null) {
            if (dg.getEnds ().contains (state)) {
                lastState = state;
                lastIndex = input.getIndex ();
            }
            if (input.eof ()) break;
            Object newState = dg.getNode (state, new Character (input.next ()));
            if (newState != null)
                state = newState;
            else
                state = dg.getNode (state, new Character ((char) 0));
            if (state != null) input.read ();
        }
        input.setIndex (lastIndex);
        return lastState;
    }

    public String toString () {
        return dg.toString ();
    }
    
//    public Object getValue (Object state, Object key) {
//        return dg.getProperty (state, key);
//    }
    
    DG getDG () {
        return dg;
    }
}
