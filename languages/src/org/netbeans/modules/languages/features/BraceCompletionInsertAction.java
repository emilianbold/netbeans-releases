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

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Jancura
 */
public class BraceCompletionInsertAction extends ExtDefaultKeyTypedAction {

    protected void insertString (
        BaseDocument doc, int dotPos,
        Caret caret, String str,
        boolean overwrite
    ) throws BadLocationException {
        try {
            String mimeType = (String) doc.getProperty ("mimeType");
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
            TokenHierarchy th = TokenHierarchy.get (doc);
            TokenSequence ts = th.tokenSequence ();
            ts.move (caret.getDot ());
            if (!ts.moveNext() && !ts.movePrevious()) { // no tokens at all
                super.insertString (doc, dotPos, caret, str, overwrite);
                return;
            }
            Object[] indentValue = (Object[]) l.getProperty (Language.COMPLETE);

            if (indentValue == null) {
                super.insertString (doc, dotPos, caret, str, overwrite);
                return;
            }
            List l1 = (List) indentValue [0];
            List l2 = (List) indentValue [1];
            Evaluator e = (Evaluator) indentValue [2];
            int i, k = l2.size ();
            for (i = 0; i < k; i++) {
                String ss = doc.getText (
                    caret.getDot (), 
                    ((String) l2.get (i)).length ()
                );
                if (ss.equals (l2.get (i)) && str.equals (ss)) {
                    caret.setDot (caret.getDot () + 1);
                    return;
                }
            }
            boolean beg = ts.offset () == caret.getDot ();

            super.insertString (doc, dotPos, caret, str, overwrite);

            ts = th.tokenSequence ();
            ts.move (caret.getDot ());
            if (e != null) {
                if (caret.getDot () < doc.getLength ())
                    ts.movePrevious ();
                String s = (String) ((Evaluator.Method) e).evaluate (Context.create (doc, ts));
                if (s != null) {
                    int pos = caret.getDot ();
                    doc.insertString (pos, s, null);
                    caret.setDot (pos);
                }
            }
            if (!ts.moveNext ()) return;
            if (!beg && 
                ts.token ().id ().name ().indexOf ("whitespace") < 0
            ) return;
            StyledDocument sdoc = (StyledDocument) doc;
            int ln = NbDocument.findLineNumber (sdoc, caret.getDot ());
            int ls = NbDocument.findLineOffset (sdoc, ln);
            String text = sdoc.getText (ls, caret.getDot () - ls);
            k = l1.size ();
            for (i = 0; i < k; i++) {
                if (text.endsWith ((String) l1.get (i))) {
                    int pos = caret.getDot ();
                    doc.insertString (pos, (String) l2.get (i), null);
                    caret.setDot (pos);
                    return;
                }
            }
        } catch (ParseException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
/*
    protected void replaceSelection (
        JTextComponent target,  
        int dotPos, 
        Caret caret,
        String str, 
        boolean overwrite
    ) throws BadLocationException {
        System.out.println ("replaceSelection " + str);
        super.replaceSelection (target, dotPos, caret, str, overwrite);
    }
*/        
}
