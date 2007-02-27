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

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;


/**
 * Support for semantic highlighting.
 * 
 * @author Jan Jancura
 */
public class Highlighting {
    
    
    private static Map<Document,WeakReference<Highlighting>> highlightings = new WeakHashMap<Document,WeakReference<Highlighting>> ();
    
    {
        //Utils.startTest("Highlighting.highlightings", highlightings);
    }
    
    /**
     * Returns Highlighting for given document.
     * 
     * @param document a document
     */
    public static Highlighting getHighlighting (Document document) {
        WeakReference<Highlighting> wr = highlightings.get (document);
        Highlighting highlighting = wr == null ? null : wr.get ();
        if (highlighting == null) {
            highlighting = new Highlighting ();
            highlightings.put (document, new WeakReference<Highlighting> (highlighting));
        }
        return highlighting;
    }
    
    
    
    private Map<ASTNode,AttributeSet> highlights = new HashMap<ASTNode,AttributeSet> ();
    private Map<Integer,Map<String,AttributeSet>> tokens = new HashMap<Integer,Map<String,AttributeSet>> ();
    
    private Highlighting () {}
    
    /**
     * Defines highlighting for given item.
     * 
     * @param item a item
     * @param as set of highlighting attributes
     */
    public void highlight (ASTItem item, AttributeSet as) {
        if (item instanceof ASTNode) {
            highlights.put ((ASTNode) item, as);
            return;
        }
        ASTToken token = (ASTToken) item;
        Integer id = new Integer (token.getOffset ());
        Map<String,AttributeSet> m = tokens.get (id);
        if (m == null) {
            m = new HashMap<String,AttributeSet> ();
            tokens.put (id, m);
        }
        m.put (token.getIdentifier (), as);
    }
    
    /**
     * Removes highlightings from given item.
     * 
     * @param item a item
     */
    public void removeHighlight (ASTItem item) {
        if (item instanceof ASTNode) {
            highlights.remove ((ASTNode) item);
            return;
        }
        ASTToken token = (ASTToken) item;
        Integer id = new Integer (token.getOffset ());
        Map m = (Map) tokens.get (id);
        if (m == null) return;
        m.remove (token.getIdentifier ());
        if (m.isEmpty ())
            tokens.remove (id);
    }
    
    /**
     * Returns highlighting for given AST item.
     * 
     * @param highlighting for given AST item
     */
    public AttributeSet get (ASTItem item) {
        if (item instanceof ASTNode)
            return (AttributeSet) highlights.get ((ASTNode) item);
        ASTToken token = (ASTToken) item;
        Integer id = new Integer (token.getOffset ());
        Map m = (Map) tokens.get (id);
        if (m == null) return null;
        return (AttributeSet) m.get (token.getIdentifier ());
    }
}
