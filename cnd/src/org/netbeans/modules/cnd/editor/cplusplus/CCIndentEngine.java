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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.cplusplus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.MIMENames;

/** C++ indentation engine that delegates to C++ formatter */
public class CCIndentEngine extends FormatterIndentEngine {

    public static final String CC_FORMAT_NEWLINE_BEFORE_BRACE_PROP
        = "CCFormatNewlineBeforeBrace"; // NOI18N

    public static final String CC_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP
        = "CCFormatSpaceBeforeParenthesis"; // NOI18N

    public static final String CC_FORMAT_SPACE_AFTER_COMMA_PROP
        = "CCFormatSpaceAfterComma"; // NOI18N
    
    public static final String CC_FORMAT_PREPROCESSOR_AT_LINE_START_PROP
        = "CCFormatPreprocessorAtLineStart"; // NOI18N   
    
    public static final String CC_FORMAT_LEADING_STAR_IN_COMMENT_PROP
        = "CCFormatLeadingStarInComment"; // NOI18N  
    
    public static final String CC_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP
        = "CCFormatStatementContinuationIndent"; // NOI18N

    public CCIndentEngine() {
        setAcceptedMimeTypes(new String[] { MIMENames.CPLUSPLUS_MIME_TYPE });
    }

    protected ExtFormatter createFormatter() {
	return (CCFormatter) Formatter.getFormatter(CCKit.class);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Welcome_opt_indent_cpp"); // NOI18N
    }

    public boolean getCCFormatSpaceBeforeParenthesis() {
        Boolean b = (Boolean)getValue(CCSettingsNames.CC_FORMAT_SPACE_BEFORE_PARENTHESIS);
        if (b == null) {
            b = CCSettingsDefaults.defaultCCFormatSpaceBeforeParenthesis;
        }
        return b.booleanValue();
    }

    public void setCCFormatSpaceBeforeParenthesis(boolean b) {
        setValue(CCSettingsNames.CC_FORMAT_SPACE_BEFORE_PARENTHESIS,
		b ? Boolean.TRUE : Boolean.FALSE, CC_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP);
    }

    public boolean getCCFormatNewlineBeforeBrace() {
        Boolean b = (Boolean)getValue(CCSettingsNames.CC_FORMAT_NEWLINE_BEFORE_BRACE);
        if (b == null) {
            b = CCSettingsDefaults.defaultCCFormatNewlineBeforeBrace;
        }
        return b.booleanValue();
    }

    public void setCCFormatNewlineBeforeBrace(boolean b) {
        setValue(CCSettingsNames.CC_FORMAT_NEWLINE_BEFORE_BRACE,
		b ? Boolean.TRUE : Boolean.FALSE, CC_FORMAT_NEWLINE_BEFORE_BRACE_PROP);
    }
    
    public boolean getCCFormatSpaceAfterComma() {
        Boolean b = (Boolean)getValue(CCSettingsNames.CC_FORMAT_SPACE_AFTER_COMMA);
        if (b == null) {
            b = CCSettingsDefaults.defaultCCFormatSpaceAfterComma;
        }
        return b.booleanValue();
    }

    public void setCCFormatSpaceAfterComma(boolean b) {
        setValue(CCSettingsNames.CC_FORMAT_SPACE_AFTER_COMMA,
		b ? Boolean.TRUE : Boolean.FALSE, CC_FORMAT_SPACE_AFTER_COMMA_PROP);
    }
    
    public boolean getCCFormatPreprocessorAtLineStart() {
        Boolean b = (Boolean)getValue(CCSettingsNames.CC_FORMAT_PREPROCESSOR_AT_LINE_START);
        if (b == null) {
            b = CCSettingsDefaults.defaulCCtFormatPreprocessorAtLineStart;
        }
        return b.booleanValue();
    }

    public void setCCFormatPreprocessorAtLineStart(boolean b) {
        setValue(CCSettingsNames.CC_FORMAT_PREPROCESSOR_AT_LINE_START,
		b ? Boolean.TRUE : Boolean.FALSE, CC_FORMAT_PREPROCESSOR_AT_LINE_START_PROP);
    }
    
    public boolean getCCFormatLeadingStarInComment() {
        Boolean b = (Boolean) getValue(CCSettingsNames.CC_FORMAT_LEADING_STAR_IN_COMMENT);
        if (b == null) {
            b = CCSettingsDefaults.defaultCCFormatLeadingStarInComment;
        }
        return b.booleanValue();
    }        
    public void setCCFormatLeadingStarInComment(boolean b) {
        setValue(CCSettingsNames.CC_FORMAT_LEADING_STAR_IN_COMMENT,
                b ? Boolean.TRUE : Boolean.FALSE, CC_FORMAT_LEADING_STAR_IN_COMMENT_PROP);
    }
    
    public int getCCFormatStatementContinuationIndent() {
        Integer i = (Integer) getValue(CCSettingsNames.CC_FORMAT_STATEMENT_CONTINUATION_INDENT);
	if (i == null) {
	    i = CCSettingsDefaults.defaultCCFormatStatementContinuationIndent;
	}
	return i.intValue();
    }
    
    public void setCCFormatStatementContinuationIndent(Integer continuationIndent) {
	setValue(CCSettingsNames.CC_FORMAT_STATEMENT_CONTINUATION_INDENT,
            new Integer(continuationIndent), CC_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP);
    }

    // Serialization ------------------------------------------------------------
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField(CC_FORMAT_NEWLINE_BEFORE_BRACE_PROP, Boolean.TYPE),
        new ObjectStreamField(CC_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, Boolean.TYPE),
        new ObjectStreamField(CC_FORMAT_SPACE_AFTER_COMMA_PROP, Boolean.TYPE),
        new ObjectStreamField(CC_FORMAT_PREPROCESSOR_AT_LINE_START_PROP, Boolean.TYPE),
        new ObjectStreamField(CC_FORMAT_LEADING_STAR_IN_COMMENT_PROP, Boolean.TYPE),
        new ObjectStreamField(CC_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP, Integer.TYPE),
    };
    
    static final long serialVersionUID = -794367501912140446L;
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = ois.readFields();
        setCCFormatNewlineBeforeBrace(fields.get(CC_FORMAT_NEWLINE_BEFORE_BRACE_PROP,
            getCCFormatNewlineBeforeBrace()));
	setCCFormatSpaceBeforeParenthesis(fields.get(CC_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
            getCCFormatSpaceBeforeParenthesis()));
        setCCFormatSpaceAfterComma(fields.get(CC_FORMAT_SPACE_AFTER_COMMA_PROP,
            getCCFormatSpaceAfterComma()));
        setCCFormatPreprocessorAtLineStart(fields.get(CC_FORMAT_PREPROCESSOR_AT_LINE_START_PROP,
            getCCFormatPreprocessorAtLineStart()));
        setCCFormatLeadingStarInComment(fields.get(CC_FORMAT_LEADING_STAR_IN_COMMENT_PROP,
            getCCFormatLeadingStarInComment()));
        setCCFormatStatementContinuationIndent(fields.get(CC_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP,
            getCCFormatStatementContinuationIndent()));
    }

    private void writeObject(ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        ObjectOutputStream.PutField fields = oos.putFields();
        fields.put(CC_FORMAT_NEWLINE_BEFORE_BRACE_PROP, getCCFormatNewlineBeforeBrace());
        fields.put(CC_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, getCCFormatSpaceBeforeParenthesis());
        fields.put(CC_FORMAT_SPACE_AFTER_COMMA_PROP, getCCFormatSpaceAfterComma());
        fields.put(CC_FORMAT_PREPROCESSOR_AT_LINE_START_PROP, getCCFormatPreprocessorAtLineStart());
        fields.put(CC_FORMAT_LEADING_STAR_IN_COMMENT_PROP, getCCFormatLeadingStarInComment());
        fields.put(CC_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP, getCCFormatStatementContinuationIndent());
        oos.writeFields();
    }
}

