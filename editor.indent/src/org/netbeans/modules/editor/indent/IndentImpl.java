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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.indent.Indent;
import org.netbeans.api.editor.indent.Reformat;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;
import org.netbeans.spi.editor.indent.ReformatTask;
import org.openide.util.Lookup;

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
        if (indentHandler != null)
            throw new IllegalStateException("Already locked");
        indentHandler = new TaskHandler(true, doc);
        if (indentHandler.collectTasks()) {
            indentHandler.lock();
        }
    }
    
    public synchronized void indentUnlock() {
        if (indentHandler == null)
            throw new IllegalStateException("Already unlocked");
        indentHandler.unlock();
        indentHandler = null;
    }
    
    public Context indentContext() {
        return indentHandler.context();
    }
    
    public synchronized void reformatLock() {
        if (reformatHandler != null)
            throw new IllegalStateException("Already locked");
        reformatHandler = new TaskHandler(false, doc);
        if (reformatHandler.collectTasks()) {
            reformatHandler.lock();
        }
    }
    
    public synchronized void reformatUnlock() {
        if (reformatHandler == null)
            throw new IllegalStateException("Already unlocked");
        reformatHandler.unlock();
        reformatHandler = null;
    }
    
    public Context reformatContext() {
        return reformatHandler.context();
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
        if (!done && doc instanceof BaseDocument) {
            // Original formatter does not have reindentation of multiple lines
            // so reformat start line and continue for each line.
            do {
                ((BaseDocument)doc).getFormatter().indentLine(doc, startOffset);
                startOffset = lineElem.getEndOffset(); // Move to next line
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

    private static final class TaskHandler {
        
        private boolean indent;
        
        private Context context;
        
        private List<MimeItem> items;
        
        private Map<MimePath,MimeItem> mime2Item;
        
        private int maxMimePathSize;
        
        private Position globalStartPos;
        
        private Position globalEndPos;
        
        TaskHandler(boolean indent, Document doc) {
            this.indent = indent;
            context = IndentSpiPackageAccessor.get().createContext(indent, doc);
        }
        
        boolean isIndent() {
            return indent;
        }
        
        Context context() {
            return context;
        }
        
        Document document() {
            return context.document();
        }
        
        int maxMimePathSize() {
            return maxMimePathSize;
        }
        
        void setGlobalBounds(Position globalStartPos, Position globalEndPos) {
            this.globalStartPos = globalStartPos;
            this.globalEndPos = globalEndPos;
            // Set the initial bounds into the context as well
            IndentSpiPackageAccessor.get().resetBounds(context, globalStartPos, globalEndPos);
        }
        
        boolean collectTasks() {
            String mimeType = docMimeType();
            if (mimeType != null) {
                // Get base indent task for the document.
                // Only if it exists also get the ones for possible embedded sections.
                MimePath mimePath = MimePath.get(mimeType);
                if (addItem(mimePath)) {
                    // Also add the embedded ones
                    TokenHierarchy<?> hi = TokenHierarchy.get(document());
                    if (hi != null) {
                        Set<LanguagePath> languagePaths = hi.languagePaths();
                        for (LanguagePath lp : languagePaths) {
                            mimePath = MimePath.parse(lp.mimePath());
                            addItem(mimePath);
                        }
                    }
                }
            }
            return (items != null);
        }

        void lock() {
            for (MimeItem item : items) {
                item.lock();
            }
        }

        void unlock() {
            for (MimeItem item : items) {
                item.unlock();
            }
        }
        
        boolean hasFactories() {
            String mimeType = docMimeType();
            return (mimeType != null && new MimeItem(this, MimePath.get(mimeType)).hasFactories());
        }
        
        boolean hasItems() {
            return (items != null);
        }
        
        void runTasks() throws BadLocationException {
            // Run top-level task and possibly embedded tasks according to the context
            if (items == null) // Do nothing for no items
                return;
            
            // Start with the doc's mime type's task
            item(0).runTask();

            // Continue by reformatting the embedded sections from top-level to embedded
            int mimePathSize = 2;
            while (mimePathSize < maxMimePathSize) {
                // TBD walk the top-level token sequence and go into it
                mimePathSize++;
            }

        }
        
        private boolean addItem(MimePath mimePath) {
            maxMimePathSize = Math.max(maxMimePathSize, mimePath.size());
            MimeItem item = new MimeItem(this, mimePath);
            if (item.createTask()) {
                if (items == null) {
                    items = new ArrayList<MimeItem>();
                    mime2Item = new HashMap<MimePath,MimeItem>();
                }
                // Only add if not added yet (doc's mime-type always added as first)
                if (!mime2Item.containsKey(item.mimePath())) {
                    items.add(item);
                    mime2Item.put(item.mimePath(), item);
                    if (LOG.isLoggable(Level.FINE)) {
                        StringBuilder sb = new StringBuilder(isIndent() ? "INDENT" : "REFRMAT");
                        sb.append(": ");
                        sb.append(item);
                        LOG.fine(sb.toString());
                    }
                }
                return true;
            }
            return false;
        }
        
        private MimeItem item(int index) {
            return items.get(index);
        }
        
        private String docMimeType() {
            return (String)document().getProperty("mimeType");
        }
        
    }
    
    /**
     * Item that services indentation/reformatting for a single mime-path.
     */
    private static final class MimeItem {
        
        private final TaskHandler handler;
        
        private final MimePath mimePath;
        
        private IndentTask indentTask;
        
        private ReformatTask reformatTask;
        
        private ExtraLock extraLock;
        
        MimeItem(TaskHandler handler, MimePath mimePath) {
            this.handler = handler;
            this.mimePath = mimePath;
        }

        MimePath mimePath() {
            return mimePath;
        }

        boolean hasFactories() {
            Lookup lookup = MimeLookup.getLookup(mimePath);
            return (lookup.lookup(IndentTask.Factory.class) != null)
                || (lookup.lookup(ReformatTask.Factory.class) != null);
        }
        
        boolean createTask() {
            Lookup lookup = MimeLookup.getLookup(mimePath);
            if (!handler.isIndent()) { // Attempt reformat task first
                ReformatTask.Factory factory = lookup.lookup(ReformatTask.Factory.class);
                if (factory != null && (reformatTask = factory.createTask(handler.context())) != null) {
                    extraLock = reformatTask.reformatLock();
                    return true;
                }
            }
            
            if (handler.isIndent() || reformatTask == null) { // Possibly fallback to reindent for reformatting
                IndentTask.Factory factory = lookup.lookup(IndentTask.Factory.class);
                if (factory != null && (indentTask = factory.createTask(handler.context())) != null) {
                    extraLock = indentTask.indentLock();
                    return true;
                }
            }
            return false;
        }
        
        void lock() {
            if (extraLock != null)
                extraLock.lock();
        }
        
        void runTask() throws BadLocationException {
            if (indentTask != null) {
                indentTask.reindent();
            } else {
                reformatTask.reformat();
            }
        }
        
        void unlock() {
            if (extraLock != null)
                extraLock.unlock();
        }
        
        @Override 
        public String toString() {
            return mimePath + ": " +
                    ((indentTask != null)
                        ? "IT: " + indentTask
                        : "RT: " + reformatTask);
        }

    }

}
