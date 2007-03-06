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

import java.util.Map;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.CharInput;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.languages.Language.TokenType;
import org.netbeans.modules.languages.parser.StringInput;

public class Pattern <V> {
    
    private static final Character STAR = new Character ((char) 0);
    private static NodeFactory<Integer> nodeFactory = new NodeFactory<Integer> () {
        private int counter = 1;
        public Integer createNode () {
            return Integer.valueOf (counter++);
        }
    };
    
    public static <V> Pattern<V> create () {
        return new Pattern<V> ();
    }
    
    public static <V> Pattern<V> create (String input) throws ParseException {
        if (input.length () == 0) throw new ParseException ();
        return create (new StringInput (input, ""));
    }
    
    public static <V> Pattern<V> create (CharInput input) throws ParseException {
        Pattern<V> p = createIn (input);
        DG<Integer,Character,Integer,V> ndg = DGUtils.<Integer,Character,Integer,V>reduce (p.dg, nodeFactory);
        return new Pattern<V> (ndg);
    }
    
    private static <V> Pattern<V> createIn (CharInput input) throws ParseException {
        Pattern<V> pattern = new Pattern<V> ();
        Pattern<V> last = null;
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
                    last = createIn (input);
                    if (input.read () != ')')
                        throw new ParseException (") expected: " + input);
                    break;
//                case '<':
//                    input.read ();
//                    if (last != null) pattern = pattern.append (last);
//                    last = new Pattern (readToken (input));
//                    if (input.read () != '>')
//                        throw new ParseException ("> expected: " + input);
//                    break;
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
                                    last = last.append (new Pattern<V> (
                                        new Character ('\\')
                                    ));
                                    sb.append ('\\');
                                    break;
                                case 'n':
                                    input.read ();
                                    last = last.append (new Pattern<V> (
                                        new Character ('\n')
                                    ));
                                    sb.append ('\n');
                                    break;
                                case 'r':
                                    input.read ();
                                    last = last.append (new Pattern<V> (
                                        new Character ('\r')
                                    ));
                                    sb.append ('\r');
                                    break;
                                case 't':
                                    input.read ();
                                    last = last.append (new Pattern<V> (
                                        new Character ('\t')
                                    ));
                                    sb.append ('\t');
                                    break;
                                case '"':
                                    input.read ();
                                    last = last.append (new Pattern<V> (
                                        new Character ('"')
                                    ));
                                    sb.append ('"');
                                    break;
                                case '\'':
                                    input.read ();
                                    last = last.append (new Pattern<V> (
                                        new Character ('\'')
                                    ));
                                    sb.append ('\'');
                                    break;
                                default:
                                    throw new ParseException ("Unknown character after \\:" + input.toString ());
                            }
                        } else {
                            Character charr = new Character (input.read ());
                            last = last.append (new Pattern<V> (charr));
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
                    pattern = pattern.merge (Pattern.<V>createIn (input));
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
                    Character edge = new Character (input.next ());
                    last = new Pattern<V> (true, Collections.<Character>singleton (edge));
                    last = last.star ().append (new Pattern<V> (edge));
                    input.read ();
                    ch = input.next ();
                    while (ch != '\'' && ch != '"') {
                        if (ch == 0)
                            throw new ParseException (input.toString ());
                        last = last.plus ();
                        Integer endN = last.dg.getEnds ().iterator ().next ();
                        Integer newE = last.nodeFactory.createNode ();
                        last.dg.addNode (newE);
                        last.dg.addEdge (endN, newE, new Character (input.next ()));
                        last.dg.setEnds (Collections.singleton (newE));
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
                    last = new Pattern<V> (Pattern.STAR);
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
                    Set<Character> set = new HashSet<Character> ();
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
//                            case '<':
//                                input.read ();
//                                if (minus) throw new ParseException (input.toString ());
//                                if (l != 0) 
//                                    set.add (new Character (l));
//                                set.add (readToken (input));
//                                if (input.read () != '>')
//                                    throw new ParseException ("> expected: " + input);
//                                break;
                            default:
                                throw new ParseException (input.toString ());
                        } // switch
                        ch = input.next ();
                    } // while
                    if (minus) throw new ParseException ();
                    if (l != 0) 
                        set.add (new Character (l));
                    input.read ();
                    last = new Pattern<V> (not, set);
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

//    private static ASTToken readToken (CharInput input) throws ParseException {
//        StringBuilder sb = new StringBuilder ();
//        char ch = input.next ();
//        while (ch != ',' && ch != '>') {
//            if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
//            sb.append (ch);
//            input.read ();
//            ch = input.next ();
//        }
//        ch = input.next ();
//        String type = sb.toString ().trim ();
//        if (ch == '>') return ASTToken.create (type, null);
//        input.read ();
//        skipWhitespaces (input);
//        sb = new StringBuilder ();
//        ch = input.next ();
//        boolean read = ch != '"' && ch != '\'';
//        if (!read) {
//            input.read ();
//            ch = input.next ();
//        }
//        while (ch != '>' && ch != '"' && ch != '\'' && ch != ',') {
//            if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
//            sb.append (ch);
//            input.read ();
//            ch = input.next ();
//        }
//        if (read && (ch == '"' || ch == '\'')) throw new ParseException ("Unexpected \":" + input.toString ());
//        if (!read) input.read ();
//        String identifier = null;
//        String name = null;
//        if (read) name = sb.toString ();
//        else identifier = sb.toString ();
//        if (!read && ch == ',') {
//            ch = input.next ();
//            sb = new StringBuilder ();
//            while (ch != '>') {
//                if (ch == 0) throw new ParseException ("Unexpected end." + input.toString ());
//                sb.append (ch);
//                input.read ();
//                ch = input.next ();
//            }
//            name = sb.toString ();
//        }
//        return ASTToken.create (type, identifier);
//    }
    
    private static Set<Character> whitespace = new HashSet<Character> ();
    static {
        whitespace.add (new Character (' '));
        whitespace.add (new Character ('\n'));
        whitespace.add (new Character ('\r'));
        whitespace.add (new Character ('\t'));
    }
    
    private static void skipWhitespaces (CharInput input) {
        while (whitespace.contains (new Character (input.next ())))
            input.read ();
    }
    
    private static void addInterval (Set<Character> set, char from, char to) 
    throws ParseException {
        if (from > to) throw new ParseException ();
        do {
            set.add (new Character (from));
            from++;
        } while (from <= to);
    }
    
    private DG<Integer,Character,Integer,V> dg;// = DG.<Integer,Character,K,V>createDG ();
    
    private Pattern (DG<Integer,Character,Integer,V> dg) {
        this.dg = dg;
    }
    
    private Pattern () {
        dg = DG.<Integer,Character,Integer,V>createDG (nodeFactory.createNode ());
//        Integer start = nodeFactory.createNode ();
//        dg.addNode (start);
//        dg.setStart (start);
//        dg.addEnd (start);
    }

    private Pattern (Pattern<V> p) {
        dg = DGUtils.<Integer,Character,Integer,V>cloneDG (p.dg, false, nodeFactory);
    }
    
    private Pattern (Character edge) {
        Integer start = nodeFactory.createNode ();
        dg = DG.<Integer,Character,Integer,V>createDG (start);
        Integer end = nodeFactory.createNode ();
        dg.addNode (end);
        dg.addEdge (start, end, edge);
        dg.setEnds (Collections.<Integer>singleton (end));
    }

    private Pattern (boolean not, Set<Character> edges) {
        Integer start = nodeFactory.createNode ();
        dg = DG.<Integer,Character,Integer,V>createDG (start);
        Integer end = nodeFactory.createNode ();
        dg.addNode (end);
        dg.setStart (start);
        dg.setEnds (Collections.<Integer>emptySet ());
        Iterator<Character> it = edges.iterator ();
        while (it.hasNext ()) {
            Character edge = it.next ();
            dg.addEdge (start, end, edge);
        }
        if (not) {
            Integer failedState = nodeFactory.createNode ();
            dg.addNode (failedState);
            dg.addEdge (start, failedState, Pattern.STAR);
            dg.addEnd (failedState);
        } else 
            dg.addEnd (end);
    }
    
    public Pattern<V> clonePattern () {
        return new Pattern<V> (this);
    }

    public Pattern<V> star () {
        DG<Integer,Character,Integer,V> ndg = DGUtils.<Integer,Character,Integer,V>plus (dg, STAR, nodeFactory);
        ndg = DGUtils.<Integer,Character,Integer,V>merge (DG.<Integer,Character,Integer,V>createDG (nodeFactory.createNode ()), ndg, STAR, nodeFactory);
        Pattern<V> p = new Pattern<V> (ndg);
        return p;
    }

    public Pattern<V> plus () {
        DG<Integer,Character,Integer,V> ndg = DGUtils.<Integer,Character,Integer,V>plus (dg, STAR, nodeFactory);
        Pattern<V> p = new Pattern<V> (ndg);
        return p;
    }

    public Pattern<V> question () {
        DG<Integer,Character,Integer,V> ndg = DGUtils.<Integer,Character,Integer,V>cloneDG (dg, true, nodeFactory);
        ndg.addEnd (ndg.getStartNode ());
        Pattern<V> p = new Pattern<V> (ndg);
        return p;
    }

    public Pattern<V> merge (Pattern<V> parser) {
        DG<Integer,Character,Integer,V> ndg = DGUtils.<Integer,Character,Integer,V>merge (dg, parser.dg, STAR, nodeFactory);
        Pattern<V> p = new Pattern<V> (ndg);
        return p;
    }

    public Pattern<V> append (Pattern<V> parser) {
        DG<Integer,Character,Integer,V> ndg = DGUtils.<Integer,Character,Integer,V>append (dg, parser.dg, STAR, nodeFactory);
        Pattern<V> p = new Pattern<V> (ndg);
        return p;
    }

    boolean matches (String text) {
        int i = 0;
        Integer state = dg.getStartNode ();
        while (i < text.length ()) {
            state = dg.getNode (state, new Character (text.charAt (i++)));
            if (state == null) return false;
        }
        return dg.getEnds ().contains (state);
    }

    public Integer next (CharInput input) {
        return next (dg.getStartNode (), input);
    }
    
    public Integer next (Integer state, CharInput input) {
        int lastIndex = input.getIndex ();
        Integer lastState = null;
        while (state != null) {
            if (dg.getEnds ().contains (state)) {
                lastState = state;
                lastIndex = input.getIndex ();
            }
            if (input.eof ()) break;
            Integer newState = dg.getNode (state, new Character (input.next ()));
            if (newState != null)
                state = newState;
            else
                state = dg.getNode (state, STAR);
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
    
//    DG<Integer,Character,K,V> getDG () {
//        return dg;
//    }
    
    
    public Object read (CharInput input) {
        if (input.eof ()) return null;
        int originalIndex = input.getIndex ();
        int lastIndex = -1;
        TokenType    lastTT = null;
        Integer node = dg.getStartNode ();
        while (!input.eof ()) {
            Character edge = new Character (input.next ());
            Integer nnode = dg.getNode (node, edge);
            if (nnode == null) {
                edge = Pattern.STAR;
                nnode = dg.getNode (node, edge);
            }
            
            if (input.getIndex () > originalIndex) {
                TokenType bestTT = getBestTT (node);
                if (bestTT != null) {
                    lastTT = bestTT;
                    lastIndex = input.getIndex ();
                }
            }
            
            if (nnode == null ||
                ( dg.getEdges (nnode).isEmpty () &&
                  dg.getProperties (nnode).isEmpty ()
                )
            ) {
                if (lastTT == null) {
                    // error => reset position in CURRENT pattern (state)
                    return null;
                }
                input.setIndex (lastIndex);
                return lastTT;
            }
            
            input.read ();
            node = nnode;
        }
        
        TokenType bestTT = getBestTT (node);
        if (bestTT != null) {
            lastTT = bestTT;
            lastIndex = input.getIndex ();
        }
        
        if (lastTT == null) return null;
        return lastTT;
    }
    
    private TokenType getBestTT (Integer node) {
        Map tts = dg.getProperties (node);
        TokenType best = null;
        Iterator it = tts.keySet ().iterator ();
        while (it.hasNext ()) {
            Integer i = (Integer) it.next ();
            TokenType tt = (TokenType) tts.get (i);
            if (best == null || best.getPriority () > tt.getPriority ())
                best = tt;
        }
        return best;
    }
    
    void mark (int priority, V r) {
        Iterator<Integer> it = dg.getEnds ().iterator ();
        while (it.hasNext ()) {
            Integer s = it.next ();
            dg.setProperty (
                s, 
                priority, 
                r
            );
        }
    }
}
