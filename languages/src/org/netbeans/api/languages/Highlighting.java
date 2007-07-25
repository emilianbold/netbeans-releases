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

package org.netbeans.api.languages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map;
import javax.swing.event.EventListenerList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;


/**
 * Support for semantic highlighting.
 * 
 * @author Jan Jancura
 */
public class Highlighting {
    
    
    private static Map<Document,WeakReference<Highlighting>> highlightings = new WeakHashMap<Document,WeakReference<Highlighting>> ();

    
    /**
     * Returns Highlighting for given document.
     * 
     * @param document a document
     */
    public static Highlighting getHighlighting (Document document) {
        WeakReference<Highlighting> wr = highlightings.get (document);
        Highlighting highlighting = wr == null ? null : wr.get ();
        if (highlighting == null) {
            highlighting = new Highlighting (document);
            highlightings.put (document, new WeakReference<Highlighting> (highlighting));
        }
        return highlighting;
    }
    
    
    private Document                                document;
    private EventListenerList                       listeners;
    
    
    private Highlighting (Document document) {
        this.document = document;
    }
    
    /**
     * Defines highlighting for given item.
     * 
     * @param item a item
     * @param as set of highlighting attributes
     */
    public Highlight highlight (ASTItem item, AttributeSet as) {
        return highlight (item.getOffset (), item.getEndOffset (), as);
    }
    
    /**
     * Returns highlighting for given AST item.
     * 
     * @param highlighting for given AST item
     */
    public AttributeSet get (ASTItem item) {
        Highlight highlight = get (
            item.getOffset (), 
            item.getEndOffset ()
        );
        if (highlight == null) return null;
        return highlight.attributeSet;
    }
    
    private Set<Highlight> items = new HashSet<Highlight> ();
    
    private Highlight get (int start, int end) {
        Iterator<Highlight> it = items.iterator ();
        while (it.hasNext()) {
            Highlight item =  it.next();
            if (item.start.getOffset () == start && item.end.getOffset () == end)
                return item;
        }
        return null;
    }
    
    private Highlight highlight (int startOffset, int endOffset, AttributeSet as) {
        try {
            Highlight result = new Highlight (
                document.createPosition (startOffset),
                document.createPosition (endOffset),
                as
            );
            items.add (result);
            fire (startOffset, endOffset);
            return result;
        } catch (BadLocationException ex) {
            ex.printStackTrace ();
            return null;
        }
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        if (listeners == null)
            listeners = new EventListenerList ();
        listeners.add (PropertyChangeListener.class, l);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (listeners == null) return;
        listeners.remove (PropertyChangeListener.class, l);
    }
    
    protected void fire (int startOffset, int endOffset) {
        Object[] l = listeners.getListenerList ();
        PropertyChangeEvent event = null;
        for (int i = l.length-2; i>=0; i-=2) {
            if (event == null)
                event = new PropertyChangeEvent (this, null, startOffset, endOffset);
            ((PropertyChangeListener) l [i+1]).propertyChange (event);
        }
    }
     
    public class Highlight {
        private Position start, end;
        private AttributeSet attributeSet;
        
        private Highlight (Position start, Position end, AttributeSet attributeSet) {
            this.start = start;
            this.end = end;
            this.attributeSet = attributeSet;
        }
        
        public void remove () {
            items.remove (this);
            fire (start.getOffset (), end.getOffset ());
        }
    }
}


