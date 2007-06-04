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

/*
 * CompletionSupport.
 * 
 * @author Jan Jancura
 */
public class CompletionItem {

    public static enum Type {
        INTERFACE,
        CLASS,
        METHOD,
        FIELD,
        CONSTANT,
        CONSTRUCTOR,
        PARAMETER,
        LOCAL,
        KEYWORD
    };
    
    public static CompletionItem create (
        String          text
    ) {
        return new CompletionItem (text, null, null, null, 200);
    }
    
    public static CompletionItem create (
        String          text,
        String          description,
        String          library,
        Type            type,
        int             priority
    ) {
        return new CompletionItem (text, description, library, type, priority);
    }

    private String      text;
    private String      description;
    private String      library;
    private Type        type;
    private int         priority;

    public CompletionItem (
        String          text, 
        String          description, 
        String          library,
        Type            type, 
        int             priority
    ) {
        this.text =     text;
        this.description = description;
        this.library =  library;
        this.type =     type;
        this.priority = priority;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public String getLibrary() {
        return library;
    }

    public Type getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }
}


