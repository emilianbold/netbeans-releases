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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.indent.Indent;
import org.netbeans.api.editor.indent.Reformat;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.spi.editor.indent.Context;

/**
 * Indentation and code reformatting services for a swing text document.
 *
 * @author Miloslav Metelka
 */
public final class IndentImpl {
    
    // -J-Dorg.netbeans.modules.editor.indent.IndentImpl=FINE
    private static final Logger LOG = Logger.getLogger(IndentImpl.class.getName());
    
    public static IndentImpl get(Document doc) {
        IndentImpl indentImpl = (IndentImpl)doc.getProperty(IndentImpl.class);
        if (indentImpl == null) {
            indentImpl = new IndentImpl(doc);
            doc.putProperty(IndentImpl.class, indentImpl);
        }
        return indentImpl;
    }
    
    private final Document doc;
    
    private Indent indent;
    
    private Reformat reformat;
    
    private TaskHandler indentHandler;
    
    private TaskHandler reformatHandler;
    
    public IndentImpl(Document doc) {
        this.doc = doc;
    }
    
    public Document document() {
        return doc;
    }
    
    public Indent getIndent() {
        return indent;
    }
    
    public void setIndent(Indent indent) {
        this.indent = indent;
    }

    public Reformat getReformat() {
        return reformat;
    }
    
    public void setReformat(Reformat reformat) {
        this.reformat = reformat;
    }
    
    public boolean hasIndentOrReformatFactories() {
        return new TaskHandler(true, doc).hasFactories();
    }
    
    public synchronized void indentLock() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("indentLock() on " + this);
        }
        if (indentHandler != null)
            throw new IllegalStateException("Already locked");
        indentHandler = new TaskHandler(true, doc);
        if (indentHandler.collectTasks()) {
            indentHandler.lock();
        }
    }
    
    public synchronized void indentUnlock() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("indentUnlock() on " + this);
        }
        if (indentHandler == null)
            throw new IllegalStateException("Already unlocked");
        indentHandler.unlock();
        indentHandler = null;
    }
    
    public TaskHandler indentHandler() {
        return indentHandler;
    }
    
    public synchronized void reformatLock() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("reformatLock() on " + this);
        }
        if (reformatHandler != null)
            throw new IllegalStateException("Already locked");
        reformatHandler = new TaskHandler(false, doc);
        if (reformatHandler.collectTasks()) {
            reformatHandler.lock();
        }
    }
    
    public synchronized void reformatUnlock() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("reformatUnlock() on " + this);
        }
        if (reformatHandler == null)
            throw new IllegalStateException("Already unlocked");
        reformatHandler.unlock();
        reformatHandler = null;
    }
    
    public TaskHandler reformatHandler() {
        return reformatHandler;
    }

    public void reindent(int startOffset, int endOffset) throws BadLocationException {
        assert (indentHandler != null) : "Not locked. Use Indent.lock()"; // NOI18N
        assert (startOffset <= endOffset) : "startOffset=" + startOffset + " > endOffset=" + endOffset; // NOI18N
        // Find begining of line
        Element lineRootElem = lineRootElement(doc);
        // Correct the start offset to point to the begining of the start line
        int startLineIndex = lineRootElem.getElementIndex(startOffset);
        if (startLineIndex < 0)
            return; // Invalid line index => do nothing
        Element lineElem = lineRootElem.getElement(startLineIndex);
        int startLineOffset = lineElem.getStartOffset();
        boolean done = false;
        if (indentHandler.hasItems()) {
            // Find ending line element - by default use the same as for start offset
            if (endOffset > lineElem.getEndOffset()) { // need to get a different line element
                int endLineIndex = lineRootElem.getElementIndex(endOffset);
                lineElem = lineRootElem.getElement(endLineIndex);
                // Check if the given endOffset ends right after line's newline (in fact at the begining of the next line)
                if (endLineIndex > 0 && lineElem.getStartOffset() == endOffset) {
                    endLineIndex--;
                    lineElem = lineRootElem.getElement(endLineIndex);
                }
            }

            // Create context from begining of the start line till the end of the end line.
            indentHandler.setGlobalBounds(
                    doc.createPosition(startLineOffset),
                    doc.createPosition(lineElem.getEndOffset()));

            // Perform whole reindent on top and possibly embedded levels
            indentHandler.runTasks();
            done = true;
        }

        // Fallback to Formatter
        Formatter formatter;
        if (!done && doc instanceof BaseDocument
                && !((formatter = ((BaseDocument)doc).getFormatter()) instanceof FormatterImpl)
        ) {
            // Original formatter does not have reindentation of multiple lines
            // so reformat start line and continue for each line.
            do {
                startOffset = ((BaseDocument)doc).getFormatter().indentLine(doc, startOffset);
                startLineIndex = lineRootElem.getElementIndex(startOffset) + 1;
                if (startLineIndex >= lineRootElem.getElementCount())
                    break;
                lineElem = lineRootElem.getElement(startLineIndex);
                startOffset = lineElem.getStartOffset(); // Move to next line
            } while (startOffset < endOffset);

        }
    }

    public void reformat(int startOffset, int endOffset) throws BadLocationException {
        assert (reformatHandler != null) : "Not locked. Use Reformat.lock()"; // NOI18N
        assert (startOffset <= endOffset) : "startOffset=" + startOffset + " > endOffset=" + endOffset; // NOI18N
        boolean done = false;
        if (reformatHandler.hasItems()) {
            reformatHandler.setGlobalBounds(
                    doc.createPosition(startOffset),
                    doc.createPosition(endOffset));
            
            // Run top and embedded reformatting
            reformatHandler.runTasks();
            
            // Perform reformatting of the top section and possible embedded sections
            done = true;
        }
        
        // Fallback to Formatter
        if (!done && doc instanceof BaseDocument) {
            BaseDocument bdoc = (BaseDocument)doc;
            bdoc.getFormatter().reformat(bdoc, startOffset, endOffset);
        }
    }
    
    public static Element lineRootElement(Document doc) {
        return (doc instanceof StyledDocument)
            ? ((StyledDocument)doc).getParagraphElement(0).getParentElement()
            : doc.getDefaultRootElement();
    }

}
