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
