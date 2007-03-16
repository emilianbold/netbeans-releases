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
import org.netbeans.api.languages.ASTToken;
import java.util.*;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language.TokenType;


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
            TokenType r = (TokenType) it.next ();
            p.add (r);
        }
        return p;
    }
    
    private Map<Integer,Pattern<TokenType>> stateToPattern = new HashMap<Integer,Pattern<TokenType>> ();
    private Map<String,Integer> nameToState = new HashMap<String,Integer> ();
    private Map<Integer,String> stateToName = new HashMap<Integer,String> ();
    private int counter = 1;
    {
        nameToState.put (DEFAULT_STATE, -1);
        stateToName.put (-1, DEFAULT_STATE);
    }
    
    
    private void add (TokenType tt) {
        if (tt.getPattern () == null) return;
        String startState = tt.getStartState ();
        if (startState == null) startState = DEFAULT_STATE;
        int state = 0;
        if (nameToState.containsKey (startState))
            state = nameToState.get (startState);
        else {
            state = counter++;
            nameToState.put (startState, state);
            stateToName.put (state, startState);
        }
        Pattern<TokenType> pattern = tt.getPattern (); 
        pattern.mark (tt.getPriority (), tt);
        if (stateToPattern.containsKey (state))
            stateToPattern.put (
                state,
                stateToPattern.get (state).merge (pattern)
            );
        else
            stateToPattern.put (state, pattern);
    }
    
    public ASTToken read (Cookie cookie, CharInput input, String mimeType) {
        if (input.eof ()) return null;
        int originalIndex = input.getIndex ();
        Pattern pattern = stateToPattern.get (cookie.getState ());
        if (pattern == null) return null;
        TokenType tokenType = (TokenType) pattern.read (input);
        if (tokenType == null) {
            return null;
        }
        cookie.setProperties (tokenType.getProperties ());
        String endState = tokenType.getEndState ();
        int state = -1;
        if (endState != null)
            state = getState (endState);
        cookie.setState (state);
        return ASTToken.create (
            mimeType,
            tokenType.getType (),
            input.getString (originalIndex, input.getIndex ()),
            originalIndex
        );
    }
    
    public int getState (String stateName) {
        Integer i = nameToState.get (stateName);
        if (i == null)
            throw new IllegalArgumentException ("Unknown lexer state: " + stateName);
        return i.intValue ();
    } 
    
    private Map<String,Pattern> patterns = new HashMap<String,Pattern> ();
    
    
    public String toString () {
        StringBuffer sb = new StringBuffer ();
        Iterator<String> it = patterns.keySet ().iterator ();
        while (it.hasNext ()) {
            String state = it.next ();
            sb.append (state).append (":").append (patterns.get (state));
        }
        return sb.toString ();
    }
    
    
    // innerclasses ............................................................
    
    public interface Cookie {
        public abstract int getState ();
        public abstract void setState (int state);
        public abstract void setProperties (Feature tokenProperties);
    }
}
