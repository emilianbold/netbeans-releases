/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.editor.indent;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;

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
// XXX: formatting infra cleanup
//        indentImpl.refresh();
        return indentImpl;
    }
    
    private final Document doc;
    
    private Indent indent;
    
    private Reformat reformat;
    
    private TaskHandler indentHandler;
    
    private TaskHandler reformatHandler;

// XXX: formatting infra cleanup
//    private Formatter defaultFormatter;
    
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
    
// XXX: formatting infra cleanup
//    void setDefaultFormatter(Formatter defaultFormatter) {
//        this.defaultFormatter = defaultFormatter;
//    }
//
//    void refresh() {
//        if (defaultFormatter == null) {
//            if (doc instanceof BaseDocument) {
//                defaultFormatter = ((BaseDocument)doc).getLegacyFormatter();
//            }
//        }
//    }
    
    public synchronized void indentLock() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("indentLock() on " + this);
        }
        if (indentHandler != null)
            throw new IllegalStateException("Already locked");
        TaskHandler handler = new TaskHandler(true, doc);
        if (handler.collectTasks()) {
            handler.lock();
        }
        indentHandler = handler;
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
        TaskHandler handler = new TaskHandler(false, doc);
        if (handler.collectTasks()) {
            handler.lock();
        }
        reformatHandler = handler;
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

    public int reindent(int startOffset, int endOffset, int caretOffset, boolean indentNewLine) throws BadLocationException {
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " > endOffset=" + endOffset); // NOI18N
        }
        boolean runUnlocked = false;
        if (indentHandler == null) {
            LOG.log(Level.SEVERE, "Not locked. Use Indent.lock().", new Exception()); // NOI18N
            runUnlocked = true;
            // Attempt to call the tasks unlocked since now it's too late to lock (doc's lock already taken).
            indentHandler = new TaskHandler(true, doc);
        }
        try {
            if (runUnlocked) {
                indentHandler.collectTasks();
            }
            indentHandler.setCaretOffset(caretOffset);
            // Find begining of line
            Element lineRootElem = lineRootElement(doc);
            // Correct the start offset to point to the begining of the start line
// XXX: formatting infra cleanup
//            boolean done = false;
            if (indentHandler.hasItems()) {
                // When indenting newline first insert a plain newline
                if (indentNewLine) {
                    doc.insertString(startOffset, "\n", null);
                    // Adjust start and end offsets after the inserted newline
                    startOffset++;
                    endOffset++;
                    // Fix for Enter on first line - if it would have offset 0 the position would stay
                    // at the begining of the document
                    if (indentHandler.caretOffset() == 0) {
                        indentHandler.setCaretOffset(1);
                    }
                }

                int startLineIndex = lineRootElem.getElementIndex(startOffset);
                Element lineElem = lineRootElem.getElement(startLineIndex);
                int startLineOffset = lineElem.getStartOffset();
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
                        doc.createPosition(lineElem.getEndOffset() - 1));

                // Perform whole reindent on top and possibly embedded levels
                indentHandler.runTasks();
// XXX: formatting infra cleanup
//                done = true;
            }

// XXX: formatting infra cleanup
//            // Fallback to Formatter
//            if (!done && doc instanceof BaseDocument && defaultFormatter != null) {
//                if (indentNewLine) {
//                    if (LOG.isLoggable(Level.FINE)) {
//                        LOG.fine("Defaulting reindent() to indentNewLine() in legacy formatter " + // NOI18N
//                                defaultFormatter + '\n');
//                    }
//                    // Fallback to indentNewLine() will insert '\n'
//                    int newCaretOffset = defaultFormatter.indentNewLine(doc, caretOffset);
//                    indentHandler.setCaretOffset(newCaretOffset);
//                } else { // Indent line
//                    // Original formatter does not have reindentation of multiple lines
//                    // so reformat start line and continue for each line.
//                    Position endPos = doc.createPosition(endOffset);
//                    if (LOG.isLoggable(Level.FINE)) {
//                        LOG.fine("Defaulting reindent() to indentLine() in legacy formatter " + // NOI18N
//                                defaultFormatter + '\n');
//                    }
//                    do {
//                        startOffset = defaultFormatter.indentLine(doc, startOffset);
//                        int startLineIndex = lineRootElem.getElementIndex(startOffset) + 1;
//                        if (startLineIndex >= lineRootElem.getElementCount())
//                            break;
//                        Element lineElem = lineRootElem.getElement(startLineIndex);
//                        startOffset = lineElem.getStartOffset(); // Move to next line
//                    } while (startOffset < endPos.getOffset());
//                }
//            }

            return indentHandler.caretOffset();
        } finally {
            if (runUnlocked)
                indentHandler = null;
        }
    }

    public void reformat(int startOffset, int endOffset) throws BadLocationException {
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " > endOffset=" + endOffset); // NOI18N
        }
        boolean runUnlocked = false;
        if (reformatHandler == null) { // 
            LOG.log(Level.SEVERE, "Not locked. Use Reformat.lock().", new Exception()); // NOI18N
            // Attempt to call the tasks unlocked since now it's too late to lock (doc's lock already taken).
            runUnlocked = true;
            reformatHandler = new TaskHandler(false, doc);
        }
        try {
            if (runUnlocked) {
                reformatHandler.collectTasks();
            }
// XXX: formatting infra cleanup
//            boolean done = false;
            if (reformatHandler.hasItems()) {
                reformatHandler.setGlobalBounds(
                        doc.createPosition(startOffset),
                        doc.createPosition(endOffset));

                // Run top and embedded reformatting
                reformatHandler.runTasks();

                // Perform reformatting of the top section and possible embedded sections
// XXX: formatting infra cleanup
//                done = true;
            }

// XXX: formatting infra cleanup
//            // Fallback to Formatter
//            if (!done && doc instanceof BaseDocument && defaultFormatter != null) {
//                if (LOG.isLoggable(Level.FINE)) {
//                    LOG.fine("Defaulting reformat() to reformat() in legacy formatter " + // NOI18N
//                            defaultFormatter + '\n');
//                }
//                BaseDocument bdoc = (BaseDocument)doc;
//                defaultFormatter.reformat(bdoc, startOffset, endOffset);
//            }
        } finally {
            if (runUnlocked)
                reformatHandler = null;
        }
    }
    
    public static Element lineRootElement(Document doc) {
        return (doc instanceof StyledDocument)
            ? ((StyledDocument)doc).getParagraphElement(0).getParentElement()
            : doc.getDefaultRootElement();
    }

    public static void checkOffsetInDocument(Document doc, int offset) throws BadLocationException {
        if (offset < 0)
            throw new BadLocationException("offset=" + offset + " < 0", offset); // NOI18N
        if (offset > doc.getLength())
            throw new BadLocationException("offset=" + offset + " > doc.getLength()=" + doc.getLength(), offset);
    }

}
