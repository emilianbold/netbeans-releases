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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.completion.cplusplus;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JToolTip;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmSyntaxSupport;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.modelutil.MethodParamsTipPaintComponent;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.NbBundle;

/**
 * this is the modified copy of JavaCompletionProvider
 * @author Vladimir Voskresensky 
 */
public class CsmCompletionProvider implements CompletionProvider {

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        CsmSyntaxSupport sup = (CsmSyntaxSupport) Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
        final int dot = component.getCaret().getDot();
        if (CsmCompletionQuery.checkCondition(sup, dot)) {
            try {
                if (sup.needShowCompletionOnText(component, typedText)) {
                    return COMPLETION_QUERY_TYPE;
                }
            } catch (BadLocationException ex) {
                // skip
            }
        }
        return 0;
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        CsmSyntaxSupport sup = (CsmSyntaxSupport) Utilities.getSyntaxSupport(component).get(CsmSyntaxSupport.class);
        final int dot = component.getCaret().getDot();
        // disable code templates for smart mode of completion
        //CsmCodeTemplateFilter.enableAbbreviations(((queryType & COMPLETION_ALL_QUERY_TYPE) == COMPLETION_ALL_QUERY_TYPE));
        CsmResultItem.setEnableInstantSubstitution(true);

        // do not work together with include completion
        if (CsmCompletionQuery.checkCondition(sup, dot)) {
            if ((queryType & COMPLETION_QUERY_TYPE) == COMPLETION_QUERY_TYPE) {
                return new AsyncCompletionTask(new Query(dot, queryType), component);
            } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                return new AsyncCompletionTask(new DocQuery(null), component);
            } else if (queryType == TOOLTIP_QUERY_TYPE) {
                return new AsyncCompletionTask(new ToolTipQuery(), component);
            }
        }
        return null;
    }

    public static final CsmCompletionQuery getCompletionQuery() {
        return new NbCsmCompletionQuery(null, CsmCompletionQuery.QueryScope.GLOBAL_QUERY);
    }
    
    public static final CsmCompletionQuery getCompletionQuery(CsmFile csmFile, CsmCompletionQuery.QueryScope queryScope) {
        return new NbCsmCompletionQuery(csmFile, queryScope);
    }

    static final class Query extends AsyncCompletionQuery {

        private JTextComponent component;
        private NbCsmCompletionQuery.CsmCompletionResult queryResult;
        private int creationCaretOffset;
        private int queryAnchorOffset;
        private String filterPrefix;
        private boolean caseSensitive = false;
        private CsmCompletionQuery.QueryScope queryScope;

        Query(int caretOffset, int queryType) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
            if ((queryType & COMPLETION_ALL_QUERY_TYPE) != COMPLETION_ALL_QUERY_TYPE) {
                this.queryScope = CsmCompletionQuery.QueryScope.SMART_QUERY;
            } else {
                this.queryScope = CsmCompletionQuery.QueryScope.GLOBAL_QUERY;
            }
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            Class kitClass = Utilities.getKitClass(component);
            caseSensitive = kitClass != null ? getCaseSensitive(kitClass) : false;
            if (creationCaretOffset > 0 && caretOffset >= creationCaretOffset) {
                try {
                    if (isCppIdentifierPart(doc.getText(creationCaretOffset, caretOffset - creationCaretOffset))) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }
            Completion.get().hideCompletion();
        }
        private static final int MAX_ITEMS_TO_DISPLAY;
        

        static {
            int val = 256;
            if (System.getProperty("cnd.completion.items") != null) {
                try {
                    val = Integer.parseInt(System.getProperty("cnd.completion.items"));
                } catch (NumberFormatException numberFormatException) {
                    val = 256;
                }
            }
            MAX_ITEMS_TO_DISPLAY = val;
        }

        private void addItems(CompletionResultSet resultSet, Collection<CompletionItem> items) {
            boolean limit = (queryScope == CsmCompletionQuery.QueryScope.GLOBAL_QUERY) && queryResult.isSimpleVariableExpression() && (items.size() > MAX_ITEMS_TO_DISPLAY);
            if (!limit) {
                CsmResultItem.setEnableInstantSubstitution(queryScope == CsmCompletionQuery.QueryScope.GLOBAL_QUERY);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            } else {
                resultSet.estimateItems(MAX_ITEMS_TO_DISPLAY + 1, -1);
                int count = 0;
                for (CompletionItem item : items) {
                    resultSet.addItem(item);
                    if (++count > MAX_ITEMS_TO_DISPLAY) {
                        break;
                    }
                }
                // need fake item
                resultSet.addItem(lastItem);
            }
        }

        private boolean getCaseSensitive(Class kitClass) {
            return SettingsUtil.getBoolean(kitClass,
                    ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                    ExtSettingsDefaults.defaultCompletionCaseSensitive);
        }

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            boolean hide = (caretOffset > creationCaretOffset) && (filterPrefix == null);
            if (!hide) {
                SyntaxSupport syntSupp = Utilities.getSyntaxSupport(component);
                if (syntSupp != null) {
                    CsmSyntaxSupport sup = (CsmSyntaxSupport) syntSupp.get(CsmSyntaxSupport.class);
                    NbCsmCompletionQuery query = (NbCsmCompletionQuery) getCompletionQuery(null, queryScope);
                    NbCsmCompletionQuery.CsmCompletionResult res = (NbCsmCompletionQuery.CsmCompletionResult) query.query(component, caretOffset, sup);
                    if (res == null || (res.getData().isEmpty() && (queryScope == CsmCompletionQuery.QueryScope.SMART_QUERY))) {
                        // switch to global context
                        queryScope = CsmCompletionQuery.QueryScope.GLOBAL_QUERY;
                        if (res == null || res.isSimpleVariableExpression()) {
                            // try once more for non dereferenced expressions
                            query = (NbCsmCompletionQuery) getCompletionQuery(null, queryScope);
                            res = (NbCsmCompletionQuery.CsmCompletionResult) query.query(component, caretOffset, sup);
                        }
                    }
                    if (res != null) {
                        if (queryScope == CsmCompletionQuery.QueryScope.SMART_QUERY && 
                                !res.isSimpleVariableExpression()) {
                            // change to global mode
                            queryScope = CsmCompletionQuery.QueryScope.GLOBAL_QUERY;
                        }
                        queryAnchorOffset = res.getSubstituteOffset();
                        Collection items = res.getData();
                        // no more title in NB 6 in completion window
                        //resultSet.setTitle(res.getTitle());
                        resultSet.setAnchorOffset(queryAnchorOffset);
                        resultSet.setHasAdditionalItems(queryScope == CsmCompletionQuery.QueryScope.SMART_QUERY);
                        queryResult = res;
                        addItems(resultSet, items);
                    }
                }
            } else {
                Completion.get().hideCompletion();
            }
            resultSet.finish();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset > queryAnchorOffset) {
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                    if (!isCppIdentifierPart(filterPrefix)) {
                        filterPrefix = null;
                    }
                } catch (BadLocationException e) {
                    // filterPrefix stays null -> no filtering
                }
            }
            return (filterPrefix != null);
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && queryResult != null) {
                // no more title in NB 6 in completion window
                //resultSet.setTitle(getFilteredTitle(queryResult.getTitle(), filterPrefix));
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection items = getFilteredData(queryResult.getData(), filterPrefix);
                addItems(resultSet, items);
            }
            resultSet.finish();
        }

        private boolean isCppIdentifierPart(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(CndLexerUtilities.isCppIdentifierPart(text.charAt(i)))) {
                    return false;
                }
            }
            return true;
        }

        private Collection getFilteredData(Collection data, String prefix) {
            List ret = new ArrayList();
            boolean camelCase = prefix.length() > 1 && prefix.equals(prefix.toUpperCase());
            for (Iterator it = data.iterator(); it.hasNext();) {
                CompletionQuery.ResultItem itm = (CompletionQuery.ResultItem) it.next();
                // TODO: filter
                if (matchPrefix(itm.getItemText(), prefix, caseSensitive) /* || prefix.length() == 0*/) {
//                if (JMIUtils.startsWith(itm.getItemText(), prefix)
//                        || (camelCase && (itm instanceof NbJMIResultItem.ClassResultItem) && JMIUtils.matchesCamelCase(itm.getItemText(), prefix)))
                    ret.add(itm);
                }
            }
            return ret;
        }

        private boolean matchPrefix(String text, String prefix, boolean caseSensitive) {
            return caseSensitive ? text.startsWith(prefix) : text.toLowerCase().startsWith(prefix.toLowerCase());
        }

        private String getFilteredTitle(String title, String prefix) {
            int lastIdx = title.lastIndexOf('.');
            String ret = lastIdx == -1 ? prefix : title.substring(0, lastIdx + 1) + prefix;
            if (title.endsWith("*")) {// NOI18N
                ret += "*"; // NOI18N
            }
            return ret;
        }
    }

    static class DocQuery extends AsyncCompletionQuery {

        private Object item;
        private JTextComponent component;

        DocQuery(Object item) {
            this.item = item;
        }

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item == null) {
                item = CompletionUtilities.findItemAtCaretPos(component, caretOffset);
            }
            if (item != null) {
                resultSet.setDocumentation(new DocItem(getAssociatedObject(item),
                        ExtUtilities.getExtEditorUI(component)));
            }
            resultSet.finish();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        private Object getAssociatedObject(Object item) {
            Object ret = item;
            if (item instanceof NbCsmResultItem) {
                ret = ((NbCsmResultItem) item).getAssociatedObject();
            }
//            if (ret instanceof Feature)
//                ret = JMIUtils.getDefintion((Feature)ret);
//            if (ret instanceof ClassDefinition)
//                ret = JMIUtils.getSourceElementIfExists((ClassDefinition)ret);
            return ret;
        }

        private class DocItem implements CompletionDocumentation {

            private String text;
            private MyJavaDoc doc;
            private Action goToSource = null;
            private ExtEditorUI ui;
            private URL url;

            public DocItem(final Object item, ExtEditorUI ui) {
                this.ui = ui;
                doc = new MyJavaDoc(ui);
                doc.setItem(item);
                this.url = getURL(item);
//                Resource res = (item instanceof Element && ((Element)item).isValid()) ? ((Element)item).getResource() : null;
//                if (res != null && res.getName().endsWith(".java")) //NOI18N
//                    goToSource = new AbstractAction() {
//                        public void actionPerformed(ActionEvent e) {
//                            JMIUtils.openElement((Element)item);
//                            if (e != null) {
//                                Completion.get().hideDocumentation();
//                            }
//                        }
//                    };
            }

            public CompletionDocumentation resolveLink(String link) {
                Object item = doc.parseLink(link, (CsmClass) null);
                return item != null ? new DocItem(item, ui) : null;
            }

            public String getText() {
                return text;
            }

            public URL getURL() {
                return url;
            }

            private URL getURL(Object item) {
                return doc.getURL(item);
            }

            public Action getGotoSourceAction() {
                return goToSource;
            }

            private class MyJavaDoc extends NbCompletionJavaDoc {

                private MyJavaDoc(ExtEditorUI ui) {
                    super(ui);
                }

                private void setItem(Object item) {
                    new MyJavaDocParser(item).run();
                }

                private URL getURL(Object item) {
                    URL[] urls = getCsmSyntaxSupport().getJavaDocURLs(item);
                    return (urls == null || urls.length < 1) ? null : urls[0];
                }

                private class MyJavaDocParser extends NbCompletionJavaDoc.ParsingThread {

                    private MyJavaDocParser(Object content) {
                        super(content);
                    }

                    @Override
                    protected void showJavaDoc(final String preparedText) {
                        text = preparedText;
                    }
                }
            }
        }
    }

    static class ToolTipQuery extends AsyncCompletionQuery {

        private JTextComponent component;
        private int queryCaretOffset;
        private int queryAnchorOffset;
        private JToolTip queryToolTip;
        /** Method/constructor '(' position for tracking whether the method is still
         * being completed.
         */
        private Position queryMethodParamsStartPos = null;
        private boolean otherMethodContext;

        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            Position oldPos = queryMethodParamsStartPos;
            queryMethodParamsStartPos = null;
            NbCsmCompletionQuery query = (NbCsmCompletionQuery) getCompletionQuery();
            BaseDocument bdoc = (BaseDocument) doc;
            //NbCsmCompletionQuery.CsmCompletionResult res = null;// (NbCsmCompletionQuery.CsmCompletionResult)query.tipQuery(component, caretOffset, bdoc.getSyntaxSupport(), false);
//            NbCsmCompletionQuery query = new NbCsmCompletionQuery();
            NbCsmCompletionQuery.CsmCompletionResult res = (NbCsmCompletionQuery.CsmCompletionResult) query.query(component, caretOffset, bdoc.getSyntaxSupport(), true, false);
            if (res != null) {
                queryCaretOffset = caretOffset;
                List list = new ArrayList();
                int idx = -1;
                boolean checked = false;
                for (Iterator it = res.getData().iterator(); it.hasNext();) {
                    Object o = it.next();
                    if (o instanceof CsmResultItem.ConstructorResultItem) {
                        CsmResultItem.ConstructorResultItem item = (CsmResultItem.ConstructorResultItem) o;

                        if (!checked) {
                            CsmCompletionExpression exp = item.getExpression();
                            int idxLast = exp.getTokenCount() - 1;
                            if (idxLast >= 0) {
                                if (exp.getExpID() == CsmCompletionExpression.METHOD &&
                                        exp.getTokenID(idxLast) == CCTokenContext.RPAREN) {
                                    // check if query offset is after closing ")"
                                    if (exp.getTokenOffset(idxLast) + exp.getTokenLength(idxLast) <= caretOffset) {
                                        resultSet.finish();
                                        return;
                                    }
                                } else if (exp.getExpID() == CsmCompletionExpression.VARIABLE) {
                                    if (exp.getTokenOffset(0) + exp.getTokenLength(0) >= caretOffset) {
                                        resultSet.finish();
                                        return;
                                    }
                                }
                                try {
                                    queryMethodParamsStartPos = bdoc.createPosition(exp.getTokenOffset(0));
                                } catch (BadLocationException ble) {
                                }
                            }
                            checked = true;
                        }

                        List parms = item.createParamsList();
                        if (parms.size() > 0) {
                            idx = item.getCurrentParamIndex();
                        } else {
                            parms.add(NbBundle.getMessage(CsmCompletionProvider.class, "CC-no-parameters"));
                        }
                        list.add(parms);
                    }
                }

                resultSet.setAnchorOffset(queryAnchorOffset = res.getSubstituteOffset() + 1);
                resultSet.setToolTip(queryToolTip = new MethodParamsTipPaintComponent(list, idx));
            }
            resultSet.finish();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            String text = null;
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            try {
                if (caretOffset - queryCaretOffset > 0) {
                    text = doc.getText(queryCaretOffset, caretOffset - queryCaretOffset);
                } else if (caretOffset - queryCaretOffset < 0) {
                    text = doc.getText(caretOffset, queryCaretOffset - caretOffset);
                } else {
                    text = "";
                } //NOI18N
            } catch (BadLocationException e) {
            }
            if (text == null) {
                return false;
            }

            boolean filter = true;
            int balance = 0;
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                switch (ch) {
                    case ',':
                        filter = false;
                        break;
                    case '(':
                        balance++;
                        filter = false;
                        break;
                    case ')':
                        balance--;
                        filter = false;
                        break;
                }
                if (balance < 0) {
                    otherMethodContext = true;
                }
            }
            if (otherMethodContext && balance < 0) {
                otherMethodContext = false;
            }
            if (queryMethodParamsStartPos == null || caretOffset <= queryMethodParamsStartPos.getOffset()) {
                filter = false;
            }
            return otherMethodContext || filter;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (!otherMethodContext) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                resultSet.setToolTip(queryToolTip);
            }
            resultSet.finish();
        }
    }
    private static final CompletionItem lastItem = new LastResultItem();

    private final static class LastResultItem extends CsmResultItem {

        private final String str;
        private final static CsmPaintComponent.StringPaintComponent stringComponent = new CsmPaintComponent.StringPaintComponent();

        public LastResultItem() {
            super(null, Integer.MAX_VALUE);
            this.str = NbBundle.getBundle(CsmCompletionProvider.class).getString("last-item-text"); // NOI18N
        }

        public java.awt.Component getPaintComponent(boolean isSelected) {
            stringComponent.setString(str);
            return stringComponent;
        }

        @Override
        public void defaultAction(JTextComponent component) {
            // do nothing
        }

        @Override
        public String getItemText() {
            return str;
        }
    }
}

