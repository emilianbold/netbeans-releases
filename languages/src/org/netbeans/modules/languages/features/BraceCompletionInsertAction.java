/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.languages.features;

import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Feature.Type;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
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
            TokenHierarchy th = TokenHierarchy.get (doc);
            if (th == null) {
                super.insertString (doc, dotPos, caret, str, overwrite);
                return;
            }
            TokenSequence ts = th.tokenSequence ();
            if (ts == null) {
                super.insertString (doc, dotPos, caret, str, overwrite);
                return;
            }
            int offset = caret.getDot ();
            while (true) {
                ts.move (caret.getDot ());
                if (!ts.moveNext ())
                    break;
                offset = ts.offset ();
                TokenSequence ts2 = ts.embedded ();
                if (ts2 == null) break;
                ts = ts2;
            }
            String mimeType = ts.language ().mimeType ();
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            List<Feature> completes = l.getFeatureList ().getFeatures ("COMPLETE");
            if (completes == null) {
                super.insertString (doc, dotPos, caret, str, overwrite);
                return;
            }
            Feature methodCall = null;
            Iterator<Feature> it = completes.iterator ();
            while (it.hasNext ()) {
                Feature complete = it.next ();
                if (complete.getType () == Type.METHOD_CALL) {
                    methodCall = complete;
                    continue;
                }
                String s = (String) complete.getValue ();
                int i = s.indexOf (':');
                String ss = doc.getText (
                    caret.getDot (), 
                    s.length () - i - 1
                );
                if (s.endsWith (ss) && str.equals (ss)) {
                    // skip closing bracket / do not write it again
                    caret.setDot (caret.getDot () + 1);
                    return;
                }
            }
            boolean beg = offset == caret.getDot ();

            super.insertString (doc, dotPos, caret, str, overwrite);

            th = TokenHierarchy.get (doc);
            ts = th.tokenSequence ();
            while (true) {
                ts.move (caret.getDot ());
                if (!ts.moveNext ())
                    break;
                TokenSequence ts2 = ts.embedded ();
                if (ts2 == null) break;
                ts = ts2;
            }
            if (methodCall != null) {
                if (caret.getDot () < doc.getLength ())
                    ts.movePrevious ();
                String s = (String) methodCall.getValue (Context.create (doc, caret.getDot ()));
                if (s != null) {
                    int pos = caret.getDot ();
                    doc.insertString (pos, s, null);
                    caret.setDot (pos);
                }
            }
            char firstCharOfTokenText = ts.token().text().charAt(0);
            boolean withLeadingWS = firstCharOfTokenText == '\n' || firstCharOfTokenText == ' ';
            if (!beg && 
                ts.token ().id ().name ().indexOf ("whitespace") < 0 && !withLeadingWS
            ) return;
            StyledDocument sdoc = (StyledDocument) doc;
            int ln = NbDocument.findLineNumber (sdoc, caret.getDot ());
            int ls = NbDocument.findLineOffset (sdoc, ln);
            String text = sdoc.getText (ls, caret.getDot () - ls);
            it = completes.iterator ();
            while (it.hasNext ()) {
                Feature complete = it.next ();
                if (complete.getType () == Type.METHOD_CALL) continue;
                String s = (String) complete.getValue ();
                int i = s.indexOf (':');
                if (text.endsWith (s.substring (0, i))) {
                    int pos = caret.getDot ();
                    doc.insertString (pos, s.substring (i + 1), null);
                    caret.setDot (pos);
                    return;
                }
            }
        } catch (LanguageDefinitionNotFoundException ex) {
            // ignore the exception
            super.insertString (doc, dotPos, caret, str, overwrite);
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
        S ystem.out.println ("replaceSelection " + str);
        super.replaceSelection (target, dotPos, caret, str, overwrite);
    }
*/        
}
