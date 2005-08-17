/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor;

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.openide.ErrorManager;

/**
 * Formatter for SQL
 *
 * @author Jesse Beaumont
 */
public class SQLFormatter extends ExtFormatter {
    
    /** 
     * Creates a new instance of SQLFormater 
     */
    public SQLFormatter(Class kitClass) {
        super(kitClass);        
    }
    
    /**
     * Determines whether the specified syntax is supported by this formatter
     */
    protected boolean acceptSyntax(Syntax syntax) {
	return (syntax instanceof SQLSyntax);
    }
        
    /**
     * Reformats a portion of the document
     */
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset, boolean indentOnly) 
    throws BadLocationException, IOException {
	return super.reformat(doc, startOffset, endOffset,  indentOnly);
    }
    
    /**
     * Reformats a block of text
     */
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        return super.getReformatBlock(target, typedText);
    }
}
