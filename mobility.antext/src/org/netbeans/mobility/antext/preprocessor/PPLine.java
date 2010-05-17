/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
