/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.completion;

import javax.swing.text.Document;
import org.netbeans.modules.xml.core.actions.XMLUpdateDocumentAction;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;

/**
 * Reset internal state after user have changed external one (entities).
 * It resets grammar cache. It is registered at module layer and utilized
 * by {@link XMLUpdateDocumentAction}.
 *
 * @author  Petr Kuzel
 */
public final class ReloadActionPerformer implements XMLUpdateDocumentAction.Performer {
    
    public void perform(Node node) {
        if (node == null) return;
        EditorCookie editor = (EditorCookie) node.getCookie(EditorCookie.class);
        if (editor == null) return;
        Document doc = editor.getDocument();
        if (doc == null) return;
        GrammarManager cache = (GrammarManager)
            doc.getProperty(XMLCompletionQuery.DOCUMENT_GRAMMAR_BINDING_PROP);        
        if (cache == null) return;
        
        cache.invalidateGrammar();        
    }
}
