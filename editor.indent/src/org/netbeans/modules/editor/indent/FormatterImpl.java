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

package org.netbeans.modules.editor.indent;

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.spi.editor.indent.Context;

/**
 * Indentation and code reformatting services for a swing text document.
 *
 * @author Miloslav Metelka
 */
public final class FormatterImpl extends ExtFormatter {
    
    private Formatter defaultFormatter;
    
    private IndentImpl indentImpl;
    
    FormatterImpl(Formatter defaultFormatter, Document doc) {
        super(defaultFormatter.getKitClass());
        this.indentImpl = IndentImpl.get(doc);
        this.defaultFormatter = defaultFormatter;
        indentImpl.setDefaultFormatter(defaultFormatter);
        
    }
    
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        return (defaultFormatter instanceof ExtFormatter)
                ? ((ExtFormatter)defaultFormatter).getReformatBlock(target, typedText)
                : null;
    }
    
    public void indentLock() {
        indentImpl.indentLock();
    }
    
    public void indentUnlock() {
        indentImpl.indentUnlock();
    }
    
    public void reformatLock() {
        indentImpl.reformatLock();
    }
    
    public void reformatUnlock() {
        indentImpl.reformatUnlock();
    }

    public int indentLine(Document doc, int offset) {
        try {
            Position pos = doc.createPosition(offset);
            indentImpl.reindent(offset, offset);
            return pos.getOffset();
        } catch (GuardedException e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
        return offset;
    }

    /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    public int indentNewLine(Document doc, int offset) {
        try {
            doc.insertString(offset, "\n", null); // NOI18N
            offset++;
            return indentLine(doc, offset);
        } catch (GuardedException e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
        return offset;
    }

    public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
    boolean indentOnly) throws BadLocationException, IOException {
        // TBD delegate somehow
        return (defaultFormatter instanceof ExtFormatter)
                ? ((ExtFormatter)defaultFormatter).reformat(doc, startOffset, endOffset, indentOnly)
                : null;
    }
    
    public int reformat(BaseDocument doc, int startOffset, int endOffset)
    throws BadLocationException {
        if (doc != indentImpl.document())
            return endOffset - startOffset; // should not happen in reality
        indentImpl.reformat(startOffset, endOffset);
        TaskHandler handler = indentImpl.reformatHandler();
        return (handler != null)
                ? Math.max(handler.endPos().getOffset() - startOffset, 0)
                : endOffset - startOffset;
    }

}
