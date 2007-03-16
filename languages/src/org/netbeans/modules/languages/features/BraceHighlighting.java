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

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.BadLocationException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.lexer.STokenId;


/**
 *
 * @author Dan Prusa
 */
public class BraceHighlighting extends ExtSyntaxSupport {

    public BraceHighlighting (BaseDocument doc) {
        super (doc);
    }

    public int[] findMatchingBlock (int offset, boolean simpleSearch) throws BadLocationException {
        try {
            BaseDocument doc = getDocument ();
            TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.<BaseDocument>get (doc);
            TokenSequence tokens = tokenHierarchy.tokenSequence();
            tokens.move(offset);
            tokens.moveNext ();
            Token<STokenId> token = tokens.token ();
            String mimeType = (String) doc.getProperty("mimeType"); // NOI18N
            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
            Map<String,String>[] bracesValue = getBraces (language);
            if (bracesValue == null) {
                return super.findMatchingBlock(offset, simpleSearch);
            }

            CharSequence text = token.text().toString();
            boolean moveToRight = false;
            String bracket = bracesValue [0].get (text);
            if (bracket != null) {
                moveToRight = true;
            } else {
                bracket = bracesValue [1].get (text);
                if (bracket == null) {
                    return null;
                }
            }
            String focusedBracket = text.toString();
            int focusedBracketLength = focusedBracket.length();
            int length = bracket.length();
            int depth = 1;
            while (moveToRight ? tokens.moveNext() : tokens.movePrevious()) {
                token = tokens.token();
                text = token.text();
                if (text.length() == length && bracket.equals(text.toString())) {
                    depth--;
                    if (depth == 0) {
                        int position = token.offset(tokenHierarchy);
                        return new int[] {position, position + token.length()};
                    }
                } else if (text.length() == focusedBracketLength && focusedBracket.equals(text.toString())) {
                    depth++;
                }
            }
        } catch (ConcurrentModificationException e) {
        } catch (ParseException e) {
        }
        return null;
    }
    
    private static Map<Language,Map<String,String>[]> braces = new WeakHashMap<Language,Map<String,String>[]> ();
    
    private static Map<String,String>[] getBraces (Language l) {
        if (!braces.containsKey (l)) {
            Map<String,String> startToEnd = new HashMap<String,String> ();
            Map<String,String> endToStart = new HashMap<String,String> ();
            
            List<Feature> indents = l.getFeatures ("BRACE");
            Iterator<Feature> it = indents.iterator ();
            while (it.hasNext ()) {
                Feature indent = it.next ();
                String s = (String) indent.getValue ();
                int i = s.indexOf (':');
                String start = s.substring (0, i);
                String end = s.substring (i + 1);
                startToEnd.put (start, end);
                endToStart.put (end, start);
            }
            braces.put (
                l,
                new Map[] {startToEnd, endToStart}
            );
        }
        return braces.get (l);
    }
}

