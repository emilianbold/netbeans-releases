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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class IndentAction extends InsertBreakAction {

    protected void afterBreak (
        JTextComponent target, 
        BaseDocument doc, 
        Caret caret, 
        Object cookie
    ) {
        try {
            TokenHierarchy th = TokenHierarchy.get (doc);
            TokenSequence ts = th.tokenSequence ();
            ts.move (caret.getDot ());
            ts.moveNext ();
            while (ts.embedded () != null) {
                ts = ts.embedded ();
                ts.move (caret.getDot ());
                if (!ts.moveNext ()) break;
            }
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (ts.language ().mimeType ());
            Token token = ts.token ();
            Object indentValue = l.getProperty (Language.INDENT);

            if (indentValue == null) return;
            if (indentValue instanceof Object[]) {
                Object[] params = (Object[]) indentValue;
                int ln = NbDocument.findLineNumber ((StyledDocument) doc, caret.getDot () - 1);
                int start = NbDocument.findLineOffset ((StyledDocument) doc, ln);
                int end = NbDocument.findLineOffset ((StyledDocument) doc, ln + 1);
                String line = doc.getText (start, end - start);
                int indent = getIndent (line);
                ts.move (start);
                ts.moveNext ();
                int ni = getIndent (line, ts, end, params);
                if (ni > 0)
                    indent += 4;
                else
                if (ni == 0 && ln > 0) {
                    int start1 = NbDocument.findLineOffset ((StyledDocument) doc, ln - 1);
                    line = doc.getText (start1, start - start1);
                    ts.move (start1);
                    ts.moveNext ();
                    ni = getIndent (line, ts, start, params);
                    if (ni == 2)
                        indent -= 4;
                }
                try {
                    start = NbDocument.findLineOffset ((StyledDocument) doc, ln + 1);
                    end = NbDocument.findLineOffset ((StyledDocument) doc, ln + 2);
                    line = doc.getText (start, end - start);
                } catch (IndexOutOfBoundsException ex) {
                    line = null;
                }
                indent (doc, caret.getDot (), indent);
                if ( line != null && 
                     ((Set) params [2]).contains (line.trim ())
                ) {
                    indent -= 4;
                    int offset = caret.getDot ();
                    doc.insertString (offset, "\n", null);
                    indent (doc, caret.getDot (), indent);
                    caret.setDot (offset);
                }
            } else
            if (indentValue instanceof Evaluator.Method) {
                Evaluator.Method m = (Evaluator.Method) indentValue;
                m.evaluate (Context.create (doc, ts));
            }
        } catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }

    private static int getIndent (String line) {
        int i = 0, k = line.length () - 1;
        while (i < k && Character.isWhitespace (line.charAt (i)))
            i++;
        return i;
    }

    private static int getIndent (
        String line, 
        TokenSequence ts, 
        int end, 
        Object[] params
    ) {
        Map p = new HashMap ();
        do {
            Token t = ts.token ();
            String id = t.text ().toString ();
            if (((Set) params [1]).contains (id)) {
                Integer i = (Integer) p.get (id);
                if (i == null) {
                    i = Integer.valueOf (1);
                } else
                    i = Integer.valueOf (i.intValue () + 1);
                p.put (id, i);
            }
            if (((Set) params [2]).contains (t.text ().toString ())) {
                id = (String) ((Map) params [3]).get (id);
                Integer i = (Integer) p.get (id);
                if (i == null) {
                    i = Integer.valueOf (-1);
                } else
                    i = Integer.valueOf (i.intValue () - 1);
                p.put (id, i);
            }
            if (!ts.moveNext ()) break;
        } while (ts.offset () < end);
        Iterator it = p.values ().iterator ();
        while (it.hasNext ()) {
            int i = ((Integer) it.next ()).intValue ();
            if (i > 0) return 1;
            if (i < 0) return -1;
        }
        it = ((List) params [0]).iterator ();
        while (it.hasNext ()) {
            Pattern pattern = (Pattern) it.next ();
            if (pattern.matcher (line).matches ())
                return 2;
        }
        return 0;
    }

    private static void indent (Document doc, int offset, int i) {
        StringBuilder sb = new StringBuilder ();
        while (i > 0) {
            sb.append (' ');i--;
        }
        try {
            doc.insertString (offset, sb.toString (), null);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
}
