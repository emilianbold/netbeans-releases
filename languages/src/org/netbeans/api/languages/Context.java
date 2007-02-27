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

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

/**
 * Represents context for methods called from nbs files.
 *
 * @author Jan Jancura
 */
public abstract class Context { 

    
    /**
     * Returns instance of editor.
     * 
     * @return instance of editor
     */
    public abstract JTextComponent getJTextComponent ();
    
    /**
     * Returns instance of {@link javax.swing.text.Document}.
     * 
     * @return instance of {@link javax.swing.text.Document}
     */
    public abstract Document getDocument ();
    
    /**
     * Returns instance of {@link org.netbeans.api.lexer.TokenSequence}.
     * 
     * @return instance of {@link org.netbeans.api.lexer.TokenSequence}
     */
    public abstract TokenSequence getTokenSequence ();
    
    /**
     * Creates a new Context.
     * 
     * @return a new Context
     */
    public static Context create (Document doc, TokenSequence tokenSequence) {
        return new CookieImpl (doc, tokenSequence);
    }
    
    private static class CookieImpl extends Context {
        
        private Document        doc;
        private JTextComponent  component;
        private TokenSequence   tokenSequence;
        
        CookieImpl (
            Document        doc,
            TokenSequence   tokenSequence
        ) {
            this.doc = doc;
            this.tokenSequence = tokenSequence;
        }
        
        public JTextComponent getJTextComponent () {
            if (component == null) {
                DataObject dob = NbEditorUtilities.getDataObject (doc);
                EditorCookie ec = (EditorCookie) dob.getLookup ().lookup (EditorCookie.class);
                if (ec.getOpenedPanes ().length > 0)
                    component = ec.getOpenedPanes () [0];
            }
            return component;
        }
        
        public Document getDocument () {
            return doc;
        }
        
        public TokenSequence getTokenSequence () {
            return tokenSequence;
        }
    }
}


