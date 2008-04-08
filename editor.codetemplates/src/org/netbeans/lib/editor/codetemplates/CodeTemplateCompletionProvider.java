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
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
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
        private List<CodeTemplateCompletionItem> queryResult;
        
        private String filterPrefix;
        
        protected @Override void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected @Override boolean canFilter(JTextComponent component) {
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
        
        protected @Override void filter(CompletionResultSet resultSet) {
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
        
        private Collection<? extends CompletionItem> getFilteredData(
            Collection<? extends CompletionItem> data, 
            String prefix
        ) {
            List<CompletionItem> ret = new ArrayList<CompletionItem>();
            for (CompletionItem itm : data) {
                if (itm.getInsertPrefix().toString().startsWith(prefix)) {
                    ret.add(itm);
                }
            }
            return ret;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            String langPath = null;
            String identifierBeforeCursor = null;
            if (doc instanceof AbstractDocument) {
                AbstractDocument adoc = (AbstractDocument)doc;
                adoc.readLock();
                try {
                    try {
                        if (adoc instanceof BaseDocument) {
                            identifierBeforeCursor = Utilities.getIdentifierBefore((BaseDocument)adoc, caretOffset);
                        }
                    } catch (BadLocationException e) {
                        // leave identifierBeforeCursor null
                    }
                    List<TokenSequence<?>> list = TokenHierarchy.get(doc).embeddedTokenSequences(caretOffset, true);
                    if (list.size() > 1) {
                        langPath = list.get(list.size() - 1).languagePath().mimePath();
                    }
                } finally {
                    adoc.readUnlock();
                }
            }
            
            if (langPath == null) {
                langPath = NbEditorUtilities.getMimeType(doc);
            }

            queryCaretOffset = caretOffset;
            queryAnchorOffset = (identifierBeforeCursor != null) ? caretOffset - identifierBeforeCursor.length() : caretOffset;
            if (langPath != null && identifierBeforeCursor != null) {
                boolean ignoreCase = !SettingsUtil.getBoolean(component.getUI().getEditorKit(component).getClass(), ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                        ExtSettingsDefaults.defaultCompletionCaseSensitive);
                
                CodeTemplateManagerOperation op = CodeTemplateManagerOperation.get(MimePath.parse(langPath));
                op.waitLoaded();
                
                Collection<? extends CodeTemplate> cts = op.findByParametrizedText(identifierBeforeCursor, ignoreCase);
                Collection<? extends CodeTemplateFilter> filters = CodeTemplateManagerOperation.getTemplateFilters(component, queryAnchorOffset);
                
                queryResult = new ArrayList<CodeTemplateCompletionItem>(cts.size());
                for (CodeTemplate ct : cts) {
                    if (accept(ct, filters)) {
                        queryResult.add(new CodeTemplateCompletionItem(ct));
                    }
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
