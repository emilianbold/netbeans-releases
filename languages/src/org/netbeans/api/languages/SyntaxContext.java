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
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;


/**
 * Represents context for methods called from nbs files. This version of context
 * contains syntax information too.
 *
 * @author Jan Jancura
 */
public abstract class SyntaxContext extends Context { 

    
    /**
     * Returns current AST path.
     * 
     * @return current AST path
     */
    public abstract ASTPath getASTPath ();
    
    /**
     * Creates a new SyntaxContext.
     * 
     * @return a new SyntaxContext
     */
    public static SyntaxContext create (Document doc, ASTPath path) {
        return new CookieImpl (doc, path);
    }
    
    private static class CookieImpl extends SyntaxContext {
        
        private Document        doc;
        private ASTPath         path;
        private JTextComponent  component;
        private TokenSequence   tokenSequence;
        
        CookieImpl (
            Document doc,
            ASTPath path
        ) {
            this.doc = doc;
            this.path = path;
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
        
        public ASTPath getASTPath () {
            return path;
        }
        
        public Document getDocument () {
            return doc;
        }
        
        public TokenSequence getTokenSequence () {
            if (tokenSequence == null) {
                TokenHierarchy th = TokenHierarchy.get (doc);
                tokenSequence = th.tokenSequence ();
            }
            Object leaf = path.getLeaf ();
            if (leaf instanceof ASTToken)
                tokenSequence.move (((ASTToken) leaf).getOffset ());
            else
                tokenSequence.move (((ASTNode) leaf).getOffset ());
            tokenSequence.moveNext();
            return tokenSequence;
        }
    }
}


