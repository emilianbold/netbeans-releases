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

package org.netbeans.modules.javadoc.search;

import org.openide.windows.TopComponent;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;


/**
 * Tries to find actual focused java word.
 *
 * @author Petr Hrebejk
 */
final class GetJavaWord extends Object {


    static String getCurrentJavaWord() {
        Node[] n = TopComponent.getRegistry ().getActivatedNodes ();

        if (n.length == 1) {
            EditorCookie ec = (EditorCookie) n[0].getCookie (EditorCookie.class);
            if (ec != null) {
                JEditorPane[] panes = ec.getOpenedPanes ();
                if ( panes == null )
                    return null;
                if (panes.length > 0) {
                    return forPane(panes[0]);
                }
            }
        }

        return null;
    }
    
    static String forPane(JEditorPane p) {
        if (p == null) return null;
 
        String selection = p.getSelectedText ();
 
        if ( selection != null && selection.length() > 0 ) {
            return selection;
        } else {
 
            // try to guess which word is underneath the caret's dot.
 
            Document doc = p.getDocument();
            Element lineRoot;
 
            if (doc instanceof StyledDocument) {
                lineRoot = NbDocument.findLineRootElement((StyledDocument)doc);
            } else {
                lineRoot = doc.getDefaultRootElement();
            }
            int dot = p.getCaret().getDot();
            Element line = lineRoot.getElement(lineRoot.getElementIndex(dot));
 
            if (line == null) return null;
 
            String text = null;
            try {
                text = doc.getText(line.getStartOffset(),
                    line.getEndOffset() - line.getStartOffset());
            } catch (BadLocationException e) {
                return null;
            }
            
            if ( text == null )
                return null;
            int pos = dot - line.getStartOffset();

            if ( pos < 0 || pos >= text.length() )
                return null;

            int bix, eix;

            for( bix = Character.isJavaIdentifierPart( text.charAt( pos ) ) ? pos : pos - 1;
                    bix >= 0 && Character.isJavaIdentifierPart( text.charAt( bix ) ); bix-- );
            for( eix = pos; eix < text.length() && Character.isJavaIdentifierPart( text.charAt( eix )); eix++ );

            return bix == eix ? null : text.substring( bix + 1, eix  );
        }
    }
}
