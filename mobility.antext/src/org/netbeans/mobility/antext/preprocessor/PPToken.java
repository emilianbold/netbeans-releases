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

/*
 * PPToken.java
 *
 * Created on August 12, 2005, 10:48 AM
 *
 */
package org.netbeans.mobility.antext.preprocessor;

/**
 *
 * @author Adam Sotona
 */
public final class PPToken implements LineParserTokens
{
    
    private final int type;
    private final int line;
    private final int column;
    private final String padding;
    private final String text;
    private boolean value;
    private boolean hasValue;
    
    /** Creates an empty new instance of PPToken - used in LineScanner */
    PPToken()
    {
        this(END_OF_FILE, 0, 0, "", ""); //NOI18N
    }
    
    /** Creates a new instance of PPToken */
    PPToken(int type, int line, int column, String padding, String text)
    {
        this.type = type;
        this.line = line;
        this.column = column;
        this.padding = padding;
        this.text = text;
        this.value = false;
        this.hasValue = false;
    }
    
    public int getType()
    {
        return type;
    }
    
    public int getLine()
    {
        return line;
    }
    
    public int getColumn()
    {
        return column;
    }
    
    public String getPadding()
    {
        return padding;
    }
    
    public String getText()
    {
        return text;
    }
    
    public boolean hasValue()
    {
        return hasValue;
    }
    
    public boolean getValue()
    {
        return value;
    }
    
    public String toString()
    {
        return LineParser.yyname[type] + "(" + String.valueOf(line) + ", " + String.valueOf(column) + ", \"" + padding + "\", \"" + text  + (hasValue ? "\", " + String.valueOf(value) + ")" : "\")"); //NOI18N
    }
    
    /* package-private methods to setup PPLine during parsing */
    
    void setValue(final boolean value)
    {
        this.value = value;
        this.hasValue = true;
    }
}
