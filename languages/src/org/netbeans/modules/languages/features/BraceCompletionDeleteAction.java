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
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Jancura
 */
public class BraceCompletionDeleteAction extends ExtDeleteCharAction {

    public BraceCompletionDeleteAction () {
        super ("delete-previous", false);
    }

    protected void charBackspaced (
        BaseDocument doc, int dotPos, Caret caret, char ch
    ) throws BadLocationException {
        try {
            String mimeType = (String) doc.getProperty ("mimeType");
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
            TokenHierarchy th = TokenHierarchy.get (doc);
            TokenSequence ts = th.tokenSequence ();
            ts.move (caret.getDot ());
            if (!ts.moveNext () && !ts.movePrevious ()) return;
            Token token = ts.token ();
            Object indentValue = l.getProperty (Language.COMPLETE);
            if (indentValue == null) return;

            if (indentValue instanceof List[]) {
                List[] s = (List[]) indentValue;
                int i, k = s [0].size ();
                for (i = 0; i < k; i++) {
                    if (((String) s [0].get (i)).length () > 1) continue;
                    String ss = doc.getText (
                        caret.getDot (), 
                        ((String) s [1].get (i)).length ()
                    );
                    if (ss.equals (s [1].get (i)) && 
                        ((String) s [0].get (i)).charAt (0) == ch
                    ) {
                        doc.remove (caret.getDot (), ((String) s [1].get (i)).length ());
                        return;
                    }
                }
            }
        } catch (ParseException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
}
