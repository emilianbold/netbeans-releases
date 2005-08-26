/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.xml.parsers.DocumentInputSource;
import org.netbeans.modules.xml.core.lib.Convertors;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.SyntaxNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.openide.awt.StatusDisplayer;

/**
 * Manages grammar to text editor association. It is able to
 * dynamically switch among providers.
 *
 * @author  Petr Kuzel
 */
class GrammarManager implements DocumentListener {

    // current cache state
    private int state = INVALID;

    static final int VALID = 1;
    static final int INVALID = 3;

    // cache entry
    private GrammarQuery grammar;  

    // grammar is provided for this document
    private final XMLSyntaxSupport syntax;        
    private final Document doc;

    // guarded positions pairs
    private Position[] guarded;

    // maximal gurded offset
    private Position maxGuarded;

    private int environmentElementsCount = -1;
    
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
    public synchronized GrammarQuery getGrammar() {

        switch (state) {
            case VALID:
                return grammar;

            case INVALID:
                loadGrammar();
                return grammar;

            default:
                throw new IllegalStateException();
        }
    }


    /**
     * Notification from invalidator thread, the grammar need to be reloaded.
     */
    public synchronized void invalidateGrammar() {

        // make current loader a zombie
        if (state == VALID) {
            String msg = Util.THIS.getString("MSG_loading_cancel");
            StatusDisplayer.getDefault().setStatusText(msg);
        }

        doc.removeDocumentListener(this);

        guarded = new Position[0];
        state = INVALID;
    }

    public void insertUpdate(DocumentEvent e) {
        //test whether there is a change in the grammar environment - e.g. is a grammar
        //declaration was added and so.
        checkDocumentEnvironment(e);
        
        if (isGuarded(e.getOffset(), e.getLength())) {
            invalidateGrammar();
        }
    }

    public void removeUpdate(DocumentEvent e) {
        //test whether there is a change in the grammar environment - e.g. is a grammar
        //declaration was removed and so.
        checkDocumentEnvironment(e);
        
        if (isGuarded(e.getOffset(), e.getLength())) {
            invalidateGrammar();
        }
    }

    private void checkDocumentEnvironment(DocumentEvent e) {
        long current = System.currentTimeMillis();

        try {
            LinkedList ll = getEnvironmentElements();
            if(ll.size() != environmentElementsCount) {
                invalidateGrammar();
                environmentElementsCount = ll.size();
            }
        }catch(BadLocationException ble) {}
        
    }
    
    public void changedUpdate(DocumentEvent e) {
        // not interested
    }

    private boolean isGuarded(int offset, int length) {

        // optimalization for common case
        if ((maxGuarded != null) && (offset > maxGuarded.getOffset())) {
            return false;
        }

        // slow loop matchibng range overlaps
        for (int i = 0; i<guarded.length; i+=2) {
            int start = guarded[i].getOffset();
            int end = guarded[i+1].getOffset();
            if (start < offset && offset < end) {
                return true;
            }
            int changeEnd = offset + length;
            if (offset < start && start < changeEnd) {
                return true;
            }
        }

        return false;
    }


    /**
     * Nofification from grammar loader thread, new valid grammar.
     * @param grammar grammar or <code>null</code> if cannot load.
     */
    private synchronized void grammarLoaded(GrammarQuery grammar) {

        String status = (grammar != null) ? Util.THIS.getString("MSG_loading_done")
            : Util.THIS.getString("MSG_loading_failed");

        this.grammar = grammar == null ? EmptyQuery.INSTANCE : grammar;
        state = VALID;

        StatusDisplayer.getDefault().setStatusText(status);
    }


    /**
     * Async grammar fetching
     */
    private void loadGrammar() {


        GrammarQuery loaded = null;
        try {

            String status = Util.THIS.getString("MSG_loading");
            StatusDisplayer.getDefault().setStatusText(status);

            // prepare grammar environment

            try {

                LinkedList ctx = getEnvironmentElements();
                InputSource inputSource = new DocumentInputSource(doc);
                FileObject fileObject = null;
                Object obj = doc.getProperty(Document.StreamDescriptionProperty);
                if (obj instanceof DataObject) {
                    DataObject dobj = (DataObject) obj;
                    fileObject = dobj.getPrimaryFile();
                }
                GrammarEnvironment env = new GrammarEnvironment(Collections.enumeration (ctx), inputSource, fileObject);

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
                

            } catch (BadLocationException ex) {
                loaded = null;
            }

        } finally {
            
            doc.addDocumentListener(GrammarManager.this);
            
            grammarLoaded(loaded);
        }
    }
    
    private LinkedList getEnvironmentElements() throws BadLocationException {
        LinkedList ctx = new LinkedList ();
        SyntaxElement first = syntax.getElementChain(1);
        while (true) {
            if (first == null) break;
            if (first instanceof SyntaxNode) {
                SyntaxNode node = (SyntaxNode) first;
                ctx.add (node);
                if (node.ELEMENT_NODE == node.getNodeType()) {
                    break;
                }
            }
            first = first.getNext();
        }
        return ctx;
    }
}

