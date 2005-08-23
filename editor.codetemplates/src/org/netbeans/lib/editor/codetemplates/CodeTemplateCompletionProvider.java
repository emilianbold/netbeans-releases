/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 * Implemenation of the code template description.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateCompletionProvider implements CompletionProvider {

    public CompletionTask createTask(int type, JTextComponent component) {
        return new AsyncCompletionTask(new Query(), component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static final class Query extends AsyncCompletionQuery
    implements ChangeListener {

        private String identifierBeforeCursor;

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CodeTemplateManager manager = CodeTemplateManager.get(doc);
            identifierBeforeCursor = null;
            if (doc instanceof AbstractDocument) {
                AbstractDocument adoc = (AbstractDocument)doc;
                adoc.readLock();
                try {
                    if (adoc instanceof BaseDocument) {
                        identifierBeforeCursor = Utilities.getIdentifierBefore(
                                (BaseDocument)adoc, caretOffset);
                    }
                } catch (BadLocationException e) {
                    // leave identifierBeforeCursor null
                } finally {
                    adoc.readUnlock();
                }
            }
            if (!manager.isLoaded()) {
                synchronized (this) {
                    manager.registerLoadedListener(this);
                    // Listener could be fired synchronously so re-check
                    if (!manager.isLoaded()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }

            Collection cts = (identifierBeforeCursor != null)
                ? CodeTemplateApiPackageAccessor.get().getOperation(manager).find(identifierBeforeCursor, true)
                : Collections.EMPTY_LIST;
            List items = new ArrayList(cts.size());
            for (Iterator it = cts.iterator(); it.hasNext();) {
                CodeTemplate ct = (CodeTemplate)it.next();
                items.add(new CodeTemplateCompletionItem(ct));
            }
            resultSet.addAllItems(items);
            resultSet.finish();
        }

        public void stateChanged(ChangeEvent evt) {
            synchronized (this) {
                notify();
            }
        }

    }

}
