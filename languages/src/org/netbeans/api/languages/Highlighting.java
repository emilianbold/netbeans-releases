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
import org.netbeans.api.languages.SToken;


/**
 *
 * @author Jan Jancura
 */
public class Highlighting {
    
    
    private static Map highlightings = new WeakHashMap ();
    
    {
        //Utils.startTest("Highlighting.highlightings", highlightings);
    }
    
    public static Highlighting getHighlighting (Document doc) {
        WeakReference wr = (WeakReference) highlightings.get (doc);
        Highlighting highlighting = wr == null ? null : (Highlighting) wr.get ();
        if (highlighting == null) {
            highlighting = new Highlighting ();
            highlightings.put (doc, new WeakReference (highlighting));
        }
        return highlighting;
    }
    
    
    
    private Map highlights = new HashMap ();
    private Map tokens = new HashMap ();
    
    private Highlighting () {}
    
    public void highlight (SToken token, AttributeSet as) {
        Integer id = new Integer (token.getOffset ());
        Map m = (Map) tokens.get (id);
        if (m == null) {
            m = new HashMap ();
            tokens.put (id, m);
        }
        m.put (token.getIdentifier (), as);
    }
    
    public void removeHighlight (SToken token) {
        Integer id = new Integer (token.getOffset ());
        Map m = (Map) tokens.get (id);
        if (m == null) return;
        m.remove (token.getIdentifier ());
        if (m.isEmpty ())
            tokens.remove (id);
    }
    
   public AttributeSet get (SToken token) {
        Integer id = new Integer (token.getOffset ());
        Map m = (Map) tokens.get (id);
        if (m == null) return null;
        return (AttributeSet) m.get (token.getIdentifier ());
    }
    
    public void highlight (ASTNode node, AttributeSet as) {
        highlights.put (node, as);
    }
    
    public void removeHighlight (ASTNode node) {
        highlights.remove (node);
    }
    
    public AttributeSet get (ASTNode node) {
        return (AttributeSet) highlights.get (node);
    }
}
