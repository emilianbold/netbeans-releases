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
 * PPLine.java
 *
 * Created on August 12, 2005, 10:22 AM
 */
package org.netbeans.mobility.antext.preprocessor;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Adam Sotona
 */
public final class PPLine
{
    
    public final class Error
    {
        
        public final String message;
        public final PPToken token;
        public final boolean warning;
        
        Error(String message, PPToken token, boolean warning)
        {
            this.warning = warning;
            this.message = message;
            this.token = token;
        }
        
        public String toString()
        {
            final StringBuffer sb = new StringBuffer();
            for (int i=0;i < token.getColumn(); i++) sb.append(' ');
            sb.append('^');
            return MessageFormat.format(bundle.getString(warning ? "FMT_WARNING_OUTPUT" : "FMT_ERROR_OUTPUT"), new Object[] {String.valueOf(PPLine.this.getLineNumber()), message, PPLine.this.toString(), sb.toString()}); //NOI18N
        }
    }
    
    public static final int IF          =  0;
    public static final int ENDIF       =  1;
    public static final int IFDEF       =  2;
    public static final int IFNDEF      =  3;
    public static final int ELSE        =  4;
    public static final int ELIF        =  5;
    public static final int ELIFDEF     =  6;
    public static final int ELIFNDEF    =  7;
    public static final int DEBUG       =  8;
    public static final int MDEBUG      =  9;
    public static final int ENDDEBUG    = 10;
    public static final int DEFINE      = 11;
    public static final int UNDEFINE    = 12;
    public static final int OLDIF       = 13;
    public static final int OLDENDIF    = 14;
    public static final int COMMENTED   = 15;
    public static final int UNCOMMENTED = 16;
    public static final int CONDITION   = 17;
    public static final int UNKNOWN     = -1;
    
    protected final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.mobility.antext.preprocessor.Bundle"); //NOI18N
    
    private final ArrayList<PPToken> tokens;
    private int type;
    private ArrayList<Error> errors = null;
    private boolean hasValue;
    private boolean hasErrors;
    private boolean value;
    private int lineNumber;
    private PPBlockInfo block;
    
    PPLine()
    {
        this.tokens = new ArrayList<PPToken>();
        this.type = UNKNOWN;
        this.value = false;
        this.hasValue = false;
        this.lineNumber = -1;
        this.hasErrors = false;
    }
    
    public List<PPToken> getTokens()
    {
        return Collections.unmodifiableList(tokens);
    }
    
    public int getType()
    {
        return type;
    }
    
    public PPBlockInfo getBlock()
    {
        return block;
    }
    
    public List<Error> getErrors()
    {
        return errors == null ? (List<Error>)Collections.EMPTY_LIST : Collections.unmodifiableList(errors);
    }
    
    public boolean hasErrors()
    {
        return hasErrors;
    }
    
    public boolean hasValue()
    {
        return hasValue;
    }
    
    public boolean getValue()
    {
        return value;
    }
    
    public int getLineNumber()
    {
        return lineNumber;
    }
    
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        final Iterator<PPToken> it = tokens.iterator();
        while (it.hasNext())
        {
            final PPToken t = it.next();
            sb.append(t.getPadding());
            if (t.getType() != LineParserTokens.END_OF_LINE) sb.append(t.getText());
        }
        return sb.toString();
    }
    
    /* package-private methods to setup PPLine during parsing */
    
    void addToken(final PPToken token)
    {
        if (lineNumber < 0) lineNumber = token.getLine();
        tokens.add(token);
    }
    
    void addError(final String bundleKey)
    {
        addError(bundleKey, tokens.get(0));
    }
    
    void addError(final String bundleKey, final PPToken token)
    {
        if (errors == null) errors = new ArrayList<Error>();
        int i = 0, c = token.getColumn();
        final Iterator<Error> it = errors.iterator();
        while (it.hasNext() && it.next().token.getColumn() <= c) i++;
        errors.add(i, new Error(bundle.getString(bundleKey), token, false));
        hasErrors = true;
    }
    
    void addWarning(final String bundleKey, final PPToken token)
    {
        if (errors == null) errors = new ArrayList<Error>();
        errors.add(new Error(bundle.getString(bundleKey), token, true));
    }
    
    void setType(final int type)
    {
        this.type = type;
    }
    
    void setValue(final boolean value)
    {
        this.value = value;
        this.hasValue = true;
    }
    
    void setBlock(final PPBlockInfo block)
    {
        this.block = block;
    }
}
