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

import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
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
            TokenHierarchy th = TokenHierarchy.get (doc);
            TokenSequence ts = th.tokenSequence ();
            while (true) {
                ts.move (caret.getDot ());
                if (!ts.moveNext ()) return;
                TokenSequence ts2 = ts.embedded ();
                if (ts2 == null) break;
                ts = ts2;
            }
            mimeType = ts.language ().mimeType ();
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            List<Feature> completes = l.getFeatures ("COMPLETE");
            Iterator<Feature> it = completes.iterator ();
            while (it.hasNext ()) {
                Feature complete = it.next ();
                if (complete.getType () != Feature.Type.STRING)
                    continue;
                String s = (String) complete.getValue ();
                int i = s.indexOf (':');
                if (i != 1) continue;
                String ss = doc.getText (
                    caret.getDot (), 
                    s.length () - i - 1
                );
                if (s.endsWith (ss) && 
                    s.charAt (0) == ch
                ) {
                    doc.remove (caret.getDot (), s.length () - i - 1);
                    return;
                }
            }
        } catch (ParseException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
}
