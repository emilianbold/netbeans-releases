/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.xml.parsers.DocumentInputSource;
import org.netbeans.modules.xml.core.lib.Convertors;
import org.netbeans.modules.xml.spi.model.GrammarEnvironment;
import org.netbeans.modules.xml.spi.model.GrammarQuery;
import org.netbeans.modules.xml.spi.model.GrammarQueryManager;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.SyntaxNode;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.enum.QueueEnumeration;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Manages grammar to text editor association. It is able to
 * dynamically switch among providers.
 *
 * @author  Petr Kuzel <petr.kuzel@sun.com>
 */
class GrammarManager implements DocumentListener {

    // last invalidation time
    private long timestamp = System.currentTimeMillis();
    private int  delay = 0;

    // current cache state
    private int state = INVALID;

    static final int VALID = 1;
    static final int LOADING = 2;
    static final int INVALID = 3;

    // cache entry
    private GrammarQuery grammar;  

    // noop loader
    private static final RequestProcessor.Task EMPTY_LOADER =
        RequestProcessor.createRequest(Task.EMPTY);

    // current loader
    private RequestProcessor.Task loader = EMPTY_LOADER;

    // grammar is provided for this document
    private final XMLSyntaxSupport syntax;        
    private final Document doc;

    // guarded positions pairs
    private Position[] guarded;

    // maximal gurded offset
    private Position maxGuarded;

    /**
     * Create new manager.
     */
    public GrammarManager(Document doc, XMLSyntaxSupport syntax) {
        this.doc = doc;
        this.syntax = syntax;
    }
    
    /**
     * Return any suitable grammar that you can get 
     * till expires given timeout.
     */
    public synchronized GrammarQuery getGrammar(int timeout) {

        switch (state) {
            case VALID:
                return grammar;

            case INVALID:
                state = LOADING;
                loadGrammar();  // async

            case LOADING:
                waitLoaded(timeout); // possible thread switch !!!

                //??? return last loaded grammar (use option?)
                if (grammar != null) return grammar;

            default:                    
                return EmptyQuery.INSTANCE;
        }
    }


    /**
     * Notification from invalidator thread, the grammar need to be reloaded.
     */
    public synchronized void invalidateGrammar() {

        // make current loader a zombie
        loader.cancel();
        loader = EMPTY_LOADER;
        if (state == LOADING || state == VALID) {
            notifyProgress(loader, Util.THIS.getString("MSG_loading_cancel"));
        }

        // optimalize reload policy
        delay = (System.currentTimeMillis() - timestamp) < 1000 ? 500 : 0;
        timestamp = System.currentTimeMillis();

        doc.removeDocumentListener(this);

        guarded = new Position[0];
        state = INVALID;
    }

    public void insertUpdate(DocumentEvent e) {
        // !!! handle that adding new SyntaxElement at root
        // level may change enableness rule, syntax element
        // count should be enough

        if (isGuarded(e.getOffset(), e.getLength())) {
            invalidateGrammar();
        }
    }

    public void removeUpdate(DocumentEvent e) {
        if (isGuarded(e.getOffset(), e.getLength())) {
            invalidateGrammar();
        }
    }

    public void changedUpdate(DocumentEvent e) {
        // not interested
    }

    private boolean isGuarded(int offset, int length) {

        // optimalization for common case
        if (offset > maxGuarded.getOffset()) {
            return false;
        }

        // slow loop matchibng range overlaps
        for (int i = 0; i<guarded.length; i+=2) {
            int start = guarded[i].getOffset();
            int end = guarded[i+1].getOffset();
            int changeEnd = offset + length;
            if (offset < start && start < changeEnd) {
                return true;
            }
            if (offset < end &&  end < changeEnd) {
                return true;
            }            
        }

        return false;
    }


    /**
     * Nofification from grammar loader thread, new valid grammar.
     * @param grammar grammar or <code>null</code> if cannot load.
     */
    private synchronized void grammarLoaded(Task loader, GrammarQuery grammar) {

        try {
            // eliminate zombie loader
            if (this.loader != loader) return;

            String status = (grammar != null) ? Util.THIS.getString("MSG_loading_done") 
                : Util.THIS.getString("MSG_loading_failed");

            this.grammar = grammar == null ? EmptyQuery.INSTANCE : grammar;
            state = VALID;

            notifyProgress(loader, status);            
        } finally {
            notifyAll();
        }
    }

    /**
     * Notify loader progress filtering out messages from zombies
     */
    private void notifyProgress(Task loader, String msg) {
        if (this.loader != loader) return;
        TopManager.getDefault().setStatusText(msg);
    }

    /**
     * Async grammar fetching
     */
    private void loadGrammar() {

        class LoaderTask extends Task {

            // my represenetation in RQ as others see it
            private RequestProcessor.Task self;

            public void run() {

                GrammarQuery loaded = null;                    
                try {

                    String status = Util.THIS.getString("MSG_loading");
                    notifyProgress(self, status);

                    // prepare grammar environment
                    
                    try {

                        QueueEnumeration ctx = new QueueEnumeration();
                        SyntaxElement first = syntax.getElementChain(1);
                        while (true) {
                            if (first == null) break;
                            if (first instanceof SyntaxNode) {
                                SyntaxNode node = (SyntaxNode) first;
                                ctx.put(node);
                                if (node.ELEMENT_NODE == node.getNodeType()) {
                                    break;
                                }
                            }
                            first = first.getNext();
                        }
                        
                        InputSource inputSource = new DocumentInputSource(doc);
                        FileObject fileObject = null;
                        Object obj = doc.getProperty(Document.StreamDescriptionProperty);        
                        if (obj instanceof DataObject) {
                            DataObject dobj = (DataObject) obj;
                            fileObject = dobj.getPrimaryFile();
                        }                        
                        GrammarEnvironment env = new GrammarEnvironment(ctx, inputSource, fileObject);
                        
                        // lookup for grammar

                        GrammarQueryManager g = GrammarQueryManager.getDefault();                        
                        Enumeration en = g.enabled(env);
                        if (en == null) return;

                        // set guarded regions

                        List positions = new ArrayList(10);
                        int max = 0;

                        while (en.hasMoreElements()) {
                            Node next = (Node) en.nextElement();
                            if (next instanceof SyntaxNode) {
                                SyntaxNode node = (SyntaxNode) next;
                                int start = node.getElementOffset();
                                int end = start + node.getElementLength();
                                if (end > max) max = end;
                                Position startPosition =
                                    NbDocument.createPosition(doc, start, Position.Bias.Forward);
                                positions.add(startPosition);
                                Position endPosition = 
                                    NbDocument.createPosition(doc, end, Position.Bias.Backward);
                                positions.add(endPosition);                                
                            }                            
                        }

                        guarded = (Position[]) positions.toArray(new Position[positions.size()]);
                        maxGuarded = NbDocument.createPosition(doc, max, Position.Bias.Backward);

                        
                        // retrieve the grammar and start invalidation listener
                        
                        loaded = g.getGrammar(env);
                        doc.addDocumentListener(GrammarManager.this);

                    } catch (BadLocationException ex) {
                        loaded = null;
                    }


                    //!!! hardcoded DTD grammar, replaced with lookup                        
//                        InputSource in = Convertors.documentToInputSource(doc);
//                        loaded = new org.netbeans.modules.xml.text.completion.dtd.DTDParser().parse(in);

                } finally {
                    grammarLoaded(self, loaded);
                    notifyFinished();
                }

            }
        }

        // we need a fresh thread per loader (some requests may block)
        RequestProcessor rp = RequestProcessor.getDefault();
        LoaderTask task = new LoaderTask();
        loader = rp.create(task);
        task.self = loader;

        // do not allow too many loaders if just editing invalidation area
        loader.schedule(delay);
    }

    /**
     * Wait till grammar is loaded or given timeout expires
     */
    private void waitLoaded(int timeout) {
        try {
            if (state == LOADING) wait(timeout);
        } catch (InterruptedException ex) {
        }
    }

}

