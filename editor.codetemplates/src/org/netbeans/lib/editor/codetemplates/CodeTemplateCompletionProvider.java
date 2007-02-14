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

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
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
        return type != COMPLETION_QUERY_TYPE || isAbbrevDisabled(component) ? null : new AsyncCompletionTask(new Query(), component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static boolean isAbbrevDisabled(JTextComponent component) {
        return org.netbeans.editor.Abbrev.isAbbrevDisabled(component);
    }
    
    private static final class Query extends AsyncCompletionQuery
    implements ChangeListener {

        private JTextComponent component;
        
        private int queryCaretOffset;
        private int queryAnchorOffset;
        private List queryResult;
        
        private String filterPrefix;
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getSelectionStart();
            Document doc = component.getDocument();
            filterPrefix = null;
            if (caretOffset >= queryCaretOffset) {
                if (queryAnchorOffset < queryCaretOffset) {
                    try {
                        filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                        if (!isJavaIdentifierPart(filterPrefix)) {
                            filterPrefix = null;
                        }
                    } catch (BadLocationException e) {
                        // filterPrefix stays null -> no filtering
                    }
                }
            }
            return (filterPrefix != null);
        }
        
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && queryResult != null) {
                resultSet.addAllItems(getFilteredData(queryResult, filterPrefix));
            }
            resultSet.finish();
        }
        
        private boolean isJavaIdentifierPart(CharSequence text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
                    return false;
                }
            }
            return true;
        }
        
        private Collection getFilteredData(Collection data, String prefix) {
            List ret = new ArrayList();
            for (Iterator it = data.iterator(); it.hasNext();) {
                CodeTemplateCompletionItem itm = (CodeTemplateCompletionItem) it.next();
                if (itm.getInsertPrefix().toString().startsWith(prefix))
                    ret.add(itm);
            }
            return ret;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            CodeTemplateManagerOperation op = CodeTemplateManagerOperation.get(doc);
            String identifierBeforeCursor = null;
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
            
            op.waitLoaded();

            queryCaretOffset = caretOffset;
            queryAnchorOffset = (identifierBeforeCursor != null) ? caretOffset - identifierBeforeCursor.length() : caretOffset;
            if (identifierBeforeCursor != null) {
                boolean ignoreCase = !SettingsUtil.getBoolean(component.getUI().getEditorKit(component).getClass(), ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                        ExtSettingsDefaults.defaultCompletionCaseSensitive);
                Collection cts = op.findByParametrizedText(identifierBeforeCursor, ignoreCase);
                Collection/*<CodeTemplateFilter>*/ filters = op.getTemplateFilters(component, queryAnchorOffset);
                queryResult = new ArrayList(cts.size());
                for (Iterator it = cts.iterator(); it.hasNext();) {
                    CodeTemplate ct = (CodeTemplate)it.next();
                    if (accept(ct, filters))
                        queryResult.add(new CodeTemplateCompletionItem(ct));
                }
                resultSet.addAllItems(queryResult);
            }
            resultSet.setAnchorOffset(queryAnchorOffset);
            resultSet.finish();
        }

        public void stateChanged(ChangeEvent evt) {
            synchronized (this) {
                notify();
            }
        }
        
        private static boolean accept(CodeTemplate template, Collection/*<CodeTemplateFilter>*/ filters) {
            for(Iterator it = filters.iterator(); it.hasNext();) {
                CodeTemplateFilter filter = (CodeTemplateFilter)it.next();
                if (!filter.accept(template))
                    return false;                
            }
            return true;
        }
        
    }

}
