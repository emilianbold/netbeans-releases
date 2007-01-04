/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.io.*;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.MIMENames;

/**
* Java indentation engine that delegates to java formatter
*
* duped from editor/src/org/netbeans/modules/editor/java/JavaIndentEngine.java
*/

public class CCIndentEngine extends FormatterIndentEngine {

    public static final String FORMAT_NEWLINE_BEFORE_BRACE_PROP
        = "FormatNewlineBeforeBrace"; // NOI18N

    public static final String FORMAT_SPACE_BEFORE_PARENTHESIS_PROP
        = "FormatSpaceBeforeParenthesis"; // NOI18N

    public static final String FORMAT_SPACE_AFTER_COMMA_PROP
        = "FormatSpaceAfterComma"; // NOI18N
    
    public static final String FORMAT_PREPROCESSOR_AT_LINE_START_PROP
        = "FormatPreprocessorAtLineStart"; // NOI18N    

    public CCIndentEngine() {
        setAcceptedMimeTypes(new String[] { MIMENames.CPLUSPLUS_MIME_TYPE
	                                  });
    }

    protected ExtFormatter createFormatter() {
	return (CCFormatter)Formatter.getFormatter(CCKit.class);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_indent_cpp"); // NOI18N
    }

    public boolean getFormatSpaceBeforeParenthesis() {
        Boolean b = (Boolean)getValue(CCSettingsNames.FORMAT_SPACE_BEFORE_PARENTHESIS);
        if (b == null) {
            b = CCSettingsDefaults.defaultFormatSpaceBeforeParenthesis;
        }
        return b.booleanValue();
    }

    public void setFormatSpaceBeforeParenthesis(boolean b) {
        setValue(CCSettingsNames.FORMAT_SPACE_BEFORE_PARENTHESIS,
		b ? Boolean.TRUE : Boolean.FALSE, null);
    }

    public boolean getFormatNewlineBeforeBrace() {
        Boolean b = (Boolean)getValue(CCSettingsNames.FORMAT_NEWLINE_BEFORE_BRACE);
        if (b == null) {
            b = CCSettingsDefaults.defaultFormatNewlineBeforeBrace;
        }
        return b.booleanValue();
    }

    public void setFormatNewlineBeforeBrace(boolean b) {
        setValue(CCSettingsNames.FORMAT_NEWLINE_BEFORE_BRACE,
		b ? Boolean.TRUE : Boolean.FALSE, null);
    }
    
    public boolean getFormatSpaceAfterComma() {
        Boolean b = (Boolean)getValue(CCSettingsNames.FORMAT_SPACE_AFTER_COMMA);
        if (b == null) {
            b = CCSettingsDefaults.defaultFormatSpaceAfterComma;
        }
        return b.booleanValue();
    }

    public void setFormatSpaceAfterComma(boolean b) {
        setValue(CCSettingsNames.FORMAT_SPACE_AFTER_COMMA,
		b ? Boolean.TRUE : Boolean.FALSE, null);
    }
    
    public boolean getFormatPreprocessorAtLineStart() {
        Boolean b = (Boolean)getValue(CCSettingsNames.FORMAT_PREPROCESSOR_AT_LINE_START);
        if (b == null) {
            b = CCSettingsDefaults.defaultFormatPreprocessorAtLineStart;
        }
        return b.booleanValue();
    }

    public void setFormatPreprocessorAtLineStart(boolean b) {
        setValue(CCSettingsNames.FORMAT_PREPROCESSOR_AT_LINE_START,
		b ? Boolean.TRUE : Boolean.FALSE, null);
    }    

    // Serialization ------------------------------------------------------------
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField(FORMAT_NEWLINE_BEFORE_BRACE_PROP, Boolean.TYPE),
        new ObjectStreamField(FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, Boolean.TYPE),
        new ObjectStreamField(FORMAT_SPACE_AFTER_COMMA_PROP, Boolean.TYPE),
        new ObjectStreamField(FORMAT_PREPROCESSOR_AT_LINE_START_PROP, Boolean.TYPE)
    };
    
    static final long serialVersionUID = -794367501912140446L;
    
    private void readObject(java.io.ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = ois.readFields();
        setFormatNewlineBeforeBrace(fields.get(FORMAT_NEWLINE_BEFORE_BRACE_PROP,
            getFormatNewlineBeforeBrace()));
	setFormatSpaceBeforeParenthesis(fields.get(FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
            getFormatSpaceBeforeParenthesis()));
        setFormatSpaceAfterComma(fields.get(FORMAT_SPACE_AFTER_COMMA_PROP,
            getFormatSpaceAfterComma()));
        setFormatPreprocessorAtLineStart(fields.get(FORMAT_PREPROCESSOR_AT_LINE_START_PROP,
            getFormatPreprocessorAtLineStart()));        
    }

    private void writeObject(java.io.ObjectOutputStream oos)
    throws IOException, ClassNotFoundException {
        ObjectOutputStream.PutField fields = oos.putFields();
        fields.put(FORMAT_NEWLINE_BEFORE_BRACE_PROP, getFormatNewlineBeforeBrace());
        fields.put(FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, getFormatSpaceBeforeParenthesis());
        fields.put(FORMAT_SPACE_AFTER_COMMA_PROP, getFormatSpaceAfterComma());
        fields.put(FORMAT_PREPROCESSOR_AT_LINE_START_PROP, getFormatPreprocessorAtLineStart());
        oos.writeFields();
    }

}

