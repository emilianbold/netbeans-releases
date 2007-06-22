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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
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
public final class TaskHandler {
    
    // -J-Dorg.netbeans.modules.editor.indent.TaskHandler=FINE
    private static final Logger LOG = Logger.getLogger(TaskHandler.class.getName());
    
    private boolean indent;
    
    private Document doc;

    private Context context;

    private List<MimeItem> items;

    private Map<MimePath,MimeItem> mime2Item;

    private int maxMimePathSize;

    /**
     * Start position of the whole formatting.
     */
    private Position globalStartPos;

    /**
     * End position of the whole formatting.
     */
    private Position globalEndPos;

    /**
     * Start position of the currently formatted chunk.
     */
    private Position startPos;

    /**
     * End position of the currently formatted chunk.
     */
    private Position endPos;
    
    private boolean docModified;

    TaskHandler(boolean indent, Document doc) {
        this.indent = indent;
        this.doc = doc;
        context = IndentSpiPackageAccessor.get().createContext(this);
    }

    public boolean isIndent() {
        return indent;
    }

    public Document document() {
        return doc;
    }
    
    public Position startPos() {
        return startPos;
    }
    
    public Position endPos() {
        return endPos;
    }

    Context context() {
        return context;
    }

    int maxMimePathSize() {
        return maxMimePathSize;
    }

    void setGlobalBounds(Position globalStartPos, Position globalEndPos) {
        this.globalStartPos = globalStartPos;
        this.globalEndPos = globalEndPos;
        
        this.startPos = globalStartPos;
        this.endPos = globalEndPos;
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
        
        TokenHierarchy hi = TokenHierarchy.get(doc);
        if (hi == null)
            return;
        TokenHierarchyL listener = new TokenHierarchyL();
        hi.addTokenHierarchyListener(listener);
        try {

            // Continue by reformatting the embedded sections from top-level to embedded
            if (items.size() > 1) {
                // Walk over the top-level token sequence and go into embedded section
                // as deep as possible
                endPos = startPos;
                int lastLevel = 0; // Which nesting level was last processed
                List<TokenSequence<?>> tsStack = new ArrayList<TokenSequence<?>>(4);
                while (true) {
                    int nextOffset = endPos.getOffset();
                    if (nextOffset >= globalEndPos.getOffset())
                        break;
                    TokenSequence<?> ts = hi.tokenSequence();
                    ts.move(endPos.getOffset());
                    if (!ts.moveNext())
                        break;
                    // Position to the appropriate nesting
                    while (tsStack.size() < lastLevel) {
                        TokenSequence<?> eTS = ts.embedded();
                        if (eTS == null)
                            break;
                        eTS.move(nextOffset);
                        if (!eTS.moveNext())
                            break;
                        tsStack.add(ts);
                        ts = eTS;
                    }
                    // Check if the nextOffset does not point into middle of token
                    // If so move to next token
                    if (ts.offset() != nextOffset) {
                        if (!ts.moveNext())
                            break;
                        nextOffset = ts.offset();
                        if (nextOffset >= globalEndPos.getOffset())
                            break;
                    }

                    // Go through tokens and check whether there are any tokens
                    // with embedding that have registered reformatter
                    while (true) {
                        TokenSequence eTS = ts.embedded();
                        if (eTS != null) {
                            MimePath mimePath = MimePath.parse(eTS.languagePath().mimePath());
                            MimeItem item = mime2Item.get(mimePath);
                            if (item != null) {
                                startPos = doc.createPosition(eTS.offset());
                                nextOffset = eTS.offset() + eTS.token().length();
                                endPos = doc.createPosition(nextOffset);
                                listener.modified = false;
                                item.runTask();
                                if (listener.modified) {
                                    tsStack.clear();
                                    break; // Need to restore token sequences stack
                                } else { // No reformatting done by the task
                                    nextOffset = endPos.getOffset();
                                }
                            }
                        }

                    }
                }
            }
        } finally {
            hi.removeTokenHierarchyListener(listener);
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
    
    private final class TokenHierarchyL implements TokenHierarchyListener {
        
        boolean modified;
        
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            modified = true;
        }
        
    }
    
}
