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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.includes;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionTestPerformer;
import java.io.*;
import java.util.Collection;
import javax.swing.JEditorPane;
import org.netbeans.editor.*;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Vladimir Voskresensky
 */
public class IncludesCompletionTestPerformer extends CompletionTestPerformer {
    
    public IncludesCompletionTestPerformer() {
        
    }
    
    @Override
    protected CompletionItem[] completionQuery(
            PrintWriter  log,
            JEditorPane  editor,
            BaseDocument doc,
            int caretOffset,
            boolean      unsorted,
            int queryType
            ) {
        doc = doc == null ? Utilities.getDocument(editor) : doc;
        Collection<CsmIncludeCompletionItem> items = null;
        if (doc != null) {
            items = CsmIncludeCompletionProvider.getFilteredData(doc, caretOffset, queryType);
        }
        CompletionItem[] array =  (items == null) ? new CompletionItem[0] : items.toArray(new CompletionItem[items.size()]);
        assert array != null;
        return array;
    }
}
