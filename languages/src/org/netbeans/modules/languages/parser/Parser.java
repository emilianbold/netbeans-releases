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

import java.util.*;

/**
 *
 * @author Jan Jancura
 */
public class Parser {
    
    public static final String DEFAULT_STATE = "DEFAULT";

    private Parser () {
    }
    
    public static Parser create (List rules) {
        Parser p = new Parser ();
        Iterator it = rules.iterator ();
        while (it.hasNext ()) {
            Rule r = (Rule) it.next ();
            p.add (
                r.getStartState (), 
                r.getPattern (), 
                r.getToken (), 
                r.getEndState ()
            );
        }
        return p;
    }
    
    public static Rule create (
        String      startState,
        Pattern     pattern,
        SToken      token,
        String      endState
    ) {
        return new Rule (
            startState,
            pattern,
            token,
            endState
        );
    }

    private int priority = 1;
    
    private void add (
        String startState, 
        Pattern pattern, 
        SToken tokenID, 
        String endState
    ) {
        if (startState == null) startState = DEFAULT_STATE;
        if (endState == null) endState = DEFAULT_STATE;
        Pattern p = getPattern (startState);
        mark (pattern, tokenID, priority++, endState);
        tokens.add (tokenID);
        patterns.put (startState, p.merge (pattern));
    }
    
    private HashSet tokens = new HashSet ();
    
    public Set getTokens () {
        return Collections.unmodifiableSet (tokens);
    }
    
    public SToken read (Cookie cookie, Input input) {
        if (input.eof ()) return null;
        int originalState = cookie.getState ();
        int originalIndex = input.getIndex ();
        Pattern pattern = getPattern (originalState);
        if (pattern == null) return null;
        Object node = getNode (originalState);
        
        int     lastIndex = -1;
        TT      lastTT = null;
        
        while (!input.eof ()) {
            
            // compute next 
            Character edge = new Character (input.next ());
            Object nnode = pattern.getDG ().getNode (node, edge);
            if (nnode == null) {
                edge = Pattern.STAR;
                nnode = pattern.getDG ().getNode (node, edge);
            }
            
            if (input.getIndex () > originalIndex) {
                TT bestTT = getBestTT (pattern, node);
                if (bestTT != null) {
                    lastTT = bestTT;
                    lastIndex = input.getIndex ();
                }
            }
            
            if (nnode == null ||
                ( pattern.getDG ().getEdges (nnode).isEmpty () &&
                  pattern.getDG ().getProperties (nnode).isEmpty ()
                )
            ) {
                if (lastTT == null) {
                    // error => reset position in CURRENT pattern (state)
                    cookie.setState (
                        getState (pattern.getDG ().getStartNode (), pattern)
                    );
                    return null;
                }
                Pattern newPattern = getPattern (lastTT.state);
                cookie.setState (
                    getState (newPattern.getDG ().getStartNode (), newPattern)
                );
                input.setIndex (lastIndex);
                return SToken.create (
                    lastTT.token.getMimeType (),
                    lastTT.token.getType (),
                    input.getString (originalIndex, lastIndex),
                    originalIndex
                );
            }
            
            input.read ();
            node = nnode;
        }
        
        TT bestTT = getBestTT (pattern, node);
        if (bestTT != null) {
            lastTT = bestTT;
            lastIndex = input.getIndex ();
        }
        
        cookie.setState (getState (node, pattern));
        if (lastTT == null) {
            return null;
        }
        return SToken.create (
            lastTT.token.getMimeType (),
            lastTT.token.getType (),
            input.getString (originalIndex, lastIndex),
            originalIndex
        );
    }
    
    private static TT getBestTT (Pattern pattern, Object node) {
        Map tts = (Map) pattern.getDG ().getProperties (node);
        TT best = null;
        Iterator it = tts.keySet ().iterator ();
        while (it.hasNext ()) {
            Integer i = (Integer) it.next ();
            TT tt = (TT) tts.get (i);
            if (best == null || best.priority > tt.priority)
                best = tt;
        }
        return best;
    }
    
    /** Map (String (state) > Pattern) */
    private Map patterns = new HashMap ();
    
    public String getState (int state) {
        Pattern p = getPattern (state);
        if (p == null) return null;
        Iterator it = patterns.keySet ().iterator ();
        while (it.hasNext ()) {
            String name = (String) it.next ();
            if (patterns.get (name) == p) return name;
        }
        return null;
    }
    
    private Pattern getPattern (String state) {
        Pattern p = (Pattern) patterns.get (state);
        if (p == null) {
            p = Pattern.create ();
            patterns.put (state, p);
        }
        return p;
    }
    
    private static void mark (
        Pattern pattern, 
        SToken token, 
        int priority,
        String state
    ) {
        Iterator it = pattern.getDG ().getEnds ().iterator ();
        while (it.hasNext ()) {
            Object s = it.next ();
            pattern.getDG ().setProperty (
                s, 
                new Integer (priority), 
                new TT (state, priority, token)
            );
        }
    }
    
    private Pattern getPattern (int state) {
        Integer id = new Integer (state);
        Pattern pattern = (Pattern) stateToPattern.get (id);
        if (pattern == null && state == -1) {
            pattern = getPattern (DEFAULT_STATE);
            Object node = pattern.getDG ().getStartNode ();
            Map nodes = new HashMap ();
            nodes.put (node, id);
            patternToNodes.put (pattern, nodes);
            stateToPattern.put (id, pattern);
            stateToNode.put (id, node);
        }
        return pattern;
    }
    
    private Object getNode (int state) {
        Integer id = new Integer (state);
        Object node = stateToNode.get (id);
        if (node == null && state == -1) {
            Pattern pattern = getPattern (DEFAULT_STATE);
            node = pattern.getDG ().getStartNode ();
            Map nodes = new HashMap ();
            nodes.put (node, id);
            patternToNodes.put (pattern, nodes);
            stateToPattern.put (id, pattern);
            stateToNode.put (id, node);
        }
        return node;
    }
    
    private int stateCounter = 10;
    private Map stateToPattern = new HashMap ();
    private Map stateToNode = new HashMap ();
    private Map patternToNodes = new HashMap ();
    
    private int getState (Object node, Pattern pattern) {
        Map nodes = (Map) patternToNodes.get (pattern);
        if (nodes == null) {
            nodes = new HashMap ();
            patternToNodes.put (pattern, nodes);
        }
        Integer id = (Integer) nodes.get (node);
        if (id == null) {
            id = new Integer (stateCounter ++);
            nodes.put (node, id);
            stateToPattern.put (id, pattern);
            stateToNode.put (id, node);
        }
        return id.intValue ();
    }
    
//    public String generateCode () {
//        DG dg = pattern.getDG ();
//        int indent = 4;
//        int depth = 4;
//        StringBuffer sb = new StringBuffer ();
//        generate (sb, 0, "class Pattern {\n");
//        sb.append ("\n");
//        generate (sb, depth, "private int state;\n");
//        generate (sb, depth, "private int offset;\n");
//        generate (sb, depth, "private int stopOffset\n");
//        generate (sb, depth, "private char buffer[]\n");
//        sb.append ("\n");
//        generate (sb, depth, "int parseToken () {\n");
//        depth += indent;
//        generate (sb, depth, "char ch;\n");
//        generate (sb, depth, "while(offset < stopOffset) {\n");
//        depth += indent;
//        generate (sb, depth, "ch = buffer [offset];\n");
//        generate (sb, depth, "switch (state) {\n");
//        depth += indent;
//        Set states = dg.getNodes ();
//        Iterator it = states.iterator ();
//        while (it.hasNext ()) {
//            Object state = it.next ();
//            generate (sb, depth, "case ").
//                append (state).append (":\n");
//            depth += indent;
//            generate (sb, depth, "switch (ch) {\n");
//            depth += indent;
//            Object star = null;
//            Iterator it2 = dg.getEdges (state).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Object end = dg.getNode (state, edge);
//                if (((Character) edge).charValue () == 0) {
//                    star = end;
//                    continue;
//                }
//                generate (sb, depth, "case ").
//                    append (convert (edge)).append (":\n");
//                depth += indent;
//                Object token = dg.getProperty (end, "token");
//                if (token != null) {
//                    generate (sb, depth, "state = ").
//                        append (dg.getStartNode ()).append (";\n");
//                    generate (sb, depth, "offset++;\n");
//                    generate (sb, depth, "return ").
//                        append (token).append (";\n");
//                } else {
//                    generate (sb, depth, "state = ").
//                        append (end).append (";\n");
//                    generate (sb, depth, "offset++;\n");
//                    generate (sb, depth, "break;\n");
//                }
//                depth -= indent;
//            }
//            if (star != null) {
//                generate (sb, depth, "default:\n");
//                depth += indent;
//                Object token = dg.getProperty (star, "token");
//                if (token != null) {
//                    generate (sb, depth, "state = ").
//                        append (dg.getStartNode ()).append (";\n");
//                    generate (sb, depth, "offset++;\n");
//                    generate (sb, depth, "return ").
//                        append (token).append (";\n");
//                } else {
//                    generate (sb, depth, "state = ").
//                        append (star).append (";\n");
//                    generate (sb, depth, "offset++;\n");
//                    generate (sb, depth, "break;\n");
//                }
//                depth -= indent;
//            } else {
//                generate (sb, depth, "default:\n");
//                depth += indent;
//                generate (sb, depth, "state = ").
//                    append (dg.getStartNode ()).append (";\n");
//                generate (sb, depth, "offset++;\n");
//                generate (sb, depth, "return ").
//                    append ("ERROR").append (";\n");
//                depth -= indent;
//            }
//            depth -= indent;
//            generate (sb, depth, "}; // switch (ch)\n");
//            generate (sb, depth, "break;\n");
//            depth -= indent;
//        }
//        
//        depth -= indent;
//        generate (sb, depth, "} // switch (state)\n");
//        depth -= indent;
//        generate (sb, depth, "} // while\n");
//        depth -= indent;
//        generate (sb, depth, "} // parse token\n");
//        depth -= indent;
//        generate (sb, depth, "}\n");
//        return sb.toString ();
//    }
    
//    private static StringBuffer generate (StringBuffer sb, int indent, String text) {
//        int i, k = indent;
//        for (i = 0; i < k; i++) sb.append (' ');
//        sb.append (text);
//        return sb;
//    }
//    
//    private static String convert (Object edge) {
//        char ch = ((Character) edge).charValue ();
//        switch (ch) {
//            case '\n':return "\'\\n\'";
//            case 0:return "0";
//            default:return "\'" + ch + "\'";
//        }
//    }
    
    public String toString () {
        StringBuffer sb = new StringBuffer ();
        Iterator it = patterns.keySet ().iterator ();
        while (it.hasNext ()) {
            String state = (String) it.next ();
            sb.append (state).append (":").append (patterns.get (state));
        }
        return sb.toString ();
    }
    
    
    // innerclasses ............................................................
    
    public static final class Rule {
        
        private String  startState;
        private Pattern pattern;
        private SToken   token;
        private String  endState;
        
        Rule (
            String      startState,
            Pattern     pattern,
            SToken       token,
            String      endState
        ) {
            this.startState = startState;
            this.pattern = pattern;
            this.token = token;
            this.endState = endState;
        }
        
        public String getStartState () {
            return startState;
        }
        
        public Pattern getPattern () {
            return pattern;
        }

        public SToken getToken () {
            return token;
        }

        public String getEndState () {
            return endState;
        }
        
        public String toString () {
            return "Rule " + startState + " : " + token + " : " + endState;
        }
    }
    
    public interface Cookie {
        public abstract int getState ();
        public abstract void setState (int state);
    }
    
    private static class TT {
        private String state;
        private int priority;
        private SToken token;
        
        TT (String state, int priority, SToken token) {
            this.state = state;
            this.priority = priority;
            this.token = token;
        }
        
        public String toString () {
            return "TT " + state + " : " + priority + " : " + token;
        }
    }
}
