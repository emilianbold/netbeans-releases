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

package org.netbeans.modules.languages.features;

import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.SyntaxContext;
import javax.swing.JEditorPane;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.Context;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class ToolTipAnnotation extends Annotation {

    
    public String getShortDescription () {
        try {
            Part lp = (Part) getAttachedAnnotatable();
            Line line = lp.getLine ();
            DataObject dob = DataEditorSupport.findDataObject (line);
            EditorCookie ec = (EditorCookie) dob.getCookie (EditorCookie.class);
            NbEditorDocument doc = (NbEditorDocument) ec.getDocument ();
            String mimeType = (String) doc.getProperty ("mimeType");
            int offset = NbDocument.findLineOffset ( 
                    ec.getDocument (),
                    lp.getLine ().getLineNumber ()
                ) + lp.getColumn ();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
            tokenSequence.move (offset);
            if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) return null;
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage 
                (mimeType);
            Token token = tokenSequence.token ();
            Feature tooltip = l.getFeature (Language.TOOLTIP, token.id ().name ());
            if (tooltip != null) {
                String s = c ((String) tooltip.getValue (Context.create (doc, tokenSequence)));
                return s;
            }
            ASTNode ast = null;
            try {
                ast = ParserManagerImpl.get (doc).getAST ();
            } catch (ParseException ex) {
                ast = ex.getASTNode ();
            }
            if (ast == null) return null;
            ASTPath path = ast.findPath (offset);
            if (path == null) return null;
            int i, k = path.size ();
            for (i = 0; i < k; i++) {
                ASTPath p = path.subPath (i);
                tooltip = l.getFeature (Language.TOOLTIP, p);
                if (tooltip == null) continue;
                String s = c ((String) tooltip.getValue (SyntaxContext.create (doc, p)));
                return s;
            }
        } catch (ParseException ex) {
        }
        return null;
    }

    public String getAnnotationType () {
        return null; // Currently return null annotation type
    }
    
    private static String c (String s) {
        if (s == null) return null;
        s = s.replace ("\\n", "\n");
        s = s.replace ("\\r", "\r");
        s = s.replace ("\\t", "\t");
        return s;
    }
}

