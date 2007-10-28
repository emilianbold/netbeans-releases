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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages;

import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;

/**
 *
 * @author hanz
 */
public class TokenType {
        
    private String  startState;
    private Pattern pattern;
    private String  type;
    private int     typeID;
    private String  endState;
    private int     priority;
    private Feature properties;

    public TokenType (
        String      startState,
        Pattern     pattern,
        String      type,
        int         typeID,
        String      endState,
        int         priority,
        Feature     properties
    ) {
        this.startState = startState == null ? Parser.DEFAULT_STATE : startState;
        this.pattern = pattern;
        this.type = type;
        this.typeID = typeID;
        this.endState = endState == null ? Parser.DEFAULT_STATE : endState;
        this.priority = priority;
        this.properties = properties;
    }

    public String getType () {
        return type;
    }

    public int getTypeID () {
        return typeID;
    }

    public String getStartState () {
        return startState;
    }

    public String getEndState () {
        return endState;
    }

    public Pattern getPattern () {
        return pattern;
    }

    public int getPriority () {
        return priority;
    }

    public Feature getProperties () {
        return properties;
    }

    public String toString () {
        return "Rule " + startState + " : type " + type + " : " + endState;
    }
}
