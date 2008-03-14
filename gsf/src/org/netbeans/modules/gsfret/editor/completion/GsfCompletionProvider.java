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
package org.netbeans.modules.gsfret.editor.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JToolTip;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.Completable.QueryType;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.SourceUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.gsf.GsfEditorKitFactory;
import org.netbeans.modules.gsf.GsfHtmlFormatter;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Code completion provider - delegates to language plugin for actual population of result set.
 * Based on JavaCompletionProvider by Dusan Balek.
 * 
 * @todo I may be able to rip out the code I had in here to work around the
 *   automatic completion vs "No Suggestions" issue; see 
 *    http://hg.netbeans.org/main?cmd=changeset;node=6740db8e6988
 *
 * @author Tor Norbye
 */
public class GsfCompletionProvider implements CompletionProvider {
    
    private static final String COMMENT_CATEGORY_NAME = "comment";
    
    /** 
     * Flag which is set when we're in a query that was initiated 
     * automatically rather than through an explicit gesture 
     */
    private static boolean isAutoQuery;
    private static boolean expectingCreateTask;
        
    public static Completable getCompletable(CompilationInfo info, int offset) {
        try {
            return getCompletable(info.getDocument(), offset);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return null;
        }
    }
    
    static Completable getCompletable(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument)doc;
        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
        for (Language l : list) {
            if (l.getCompletionProvider() != null) {
                return l.getCompletionProvider();
            }
        }

        return null;
    }
    private static boolean isInCompletion(JTextComponent component) {
        Object o = component.getClientProperty("completion-active"); // NOI18N
        return o == Boolean.TRUE;
    }
    
    public static int autoQueryTypes(JTextComponent component, String typedText) {
        if (typedText.length() > 0) {
            Completable provider = getCompletable(component.getDocument(), component.getCaretPosition());
            if (provider != null) {
                QueryType autoQuery = provider.getAutoQuery(component, typedText);
                switch (autoQuery) {
                case NONE: return 0;
                case STOP: {
                    isAutoQuery = false;
                    Completion.get().hideAll();
                    return 0;
                }
                case COMPLETION: return COMPLETION_QUERY_TYPE;
                case DOCUMENTATION: return DOCUMENTATION_QUERY_TYPE;
                case TOOLTIP: return TOOLTIP_QUERY_TYPE;
                case ALL_COMPLETION: return COMPLETION_ALL_QUERY_TYPE;
                }
            }
        }

        return 0;
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        boolean isCompleting = isInCompletion(component);
        int type = autoQueryTypes(component, typedText);
        if (!isCompleting) {
            isAutoQuery = (type != 0);
            expectingCreateTask = (type != 0); // I get createTask even during editing (or just when matches==0?)
        }
        
        return type;
    }
    
    // From Utilities
    public static boolean isJavaContext(final JTextComponent component, final int offset) {
        Document doc = component.getDocument();
        org.netbeans.api.lexer.Language language = (org.netbeans.api.lexer.Language)doc.getProperty(org.netbeans.api.lexer.Language.class);
        if (language == null) {
            return true;
        }
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            TokenSequence ts = TokenHierarchy.get(component.getDocument()).tokenSequence();

            if (ts == null) {
                return false;
            }
            if (!ts.moveNext() || ts.move(offset) == 0) {
                return true;
            }
            if (!ts.moveNext()) { // Move to the next token after move(offset)
                return false;
            }

            TokenId tokenId = ts.token().id();
            
            Set s = language.tokenCategories().contains(COMMENT_CATEGORY_NAME) 
                    ? language.tokenCategoryMembers(COMMENT_CATEGORY_NAME) 
                    : null;
            
            return s == null || !s.contains(tokenId); //NOI18N
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
    }

    public static boolean startsWith(String theString, String prefix) {
        if (theString == null || theString.length() == 0)
            return false;
        if (prefix == null || prefix.length() == 0)
            return true;
        return isCaseSensitive() ? theString.startsWith(prefix) :
            theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    public CompletionTask createTask(int type, JTextComponent component) {
        if (!expectingCreateTask) {
            isAutoQuery = false;
        }
        
        if (((type & COMPLETION_QUERY_TYPE) != 0) || (type == TOOLTIP_QUERY_TYPE) ||
                (type == DOCUMENTATION_QUERY_TYPE)) {
            return new AsyncCompletionTask(new JavaCompletionQuery(type,
                    component.getSelectionStart()), component);
        }

        return null;
    }

    static CompletionTask createDocTask(ElementHandle element, CompilationInfo info) { // TODO - use ComObjectHandle ??
        JavaCompletionQuery query = new JavaCompletionQuery(DOCUMENTATION_QUERY_TYPE, -1);
        query.element = element;

        return new AsyncCompletionTask(query, Registry.getMostActiveComponent());
    }

    static final class JavaCompletionQuery extends AsyncCompletionQuery implements CancellableTask<CompilationController> {
        private Collection<CompletionItem> results;
        private JToolTip toolTip;
        private CompletionDocumentation documentation;
        private int anchorOffset;
        //private int toolTipOffset;
        private JTextComponent component;
        private int queryType;
        private int caretOffset;
        private String filterPrefix;
        private ElementHandle element;
        private Source source;
        /** The compilation info that the Element was generated for */

        private JavaCompletionQuery(int queryType, int caretOffset) {
            this.queryType = queryType;
            this.caretOffset = caretOffset;
        }

        @Override
        protected void preQueryUpdate(JTextComponent component) {
            int newCaretOffset = component.getSelectionStart();

            if (newCaretOffset >= caretOffset) {
                try {
                    if (isJavaIdentifierPart(component.getDocument()
                                                          .getText(caretOffset,
                                    newCaretOffset - caretOffset))) {
                        return;
                    }
                } catch (BadLocationException e) {
                }
            }

            Completion.get().hideCompletion();
        }

        @Override
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                this.caretOffset = caretOffset;
                if (queryType == TOOLTIP_QUERY_TYPE || queryType == DOCUMENTATION_QUERY_TYPE || isJavaContext(component, caretOffset)) {
                    results = null;
                    documentation = null;
                    toolTip = null;
                    anchorOffset = -1;
                    Source js = Source.forDocument(doc);
                    if (js == null) {
                        FileObject fo = null;
                        if (element != null) {
                            fo = element.getFileObject();
                            if (fo != null) {
                                js = Source.forFileObject(fo);
                            }
                        }
                    }
                    //if (queryType == DOCUMENTATION_QUERY_TYPE && element != null) {
                    //    FileObject fo = SourceUtils.getFile(element, js.getClasspathInfo());
                    //    if (fo != null)
                    //        js = Source.forFileObject(fo);
                    //}
                    if (js != null) {
                        if (SourceUtils.isScanInProgress())
                            resultSet.setWaitText(NbBundle.getMessage(GsfCompletionProvider.class, "scanning-in-progress")); //NOI18N
                        js.runUserActionTask(this, true);
                        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                            if (results != null)
                                resultSet.addAllItems(results);
                        } else if (queryType == TOOLTIP_QUERY_TYPE) {
                            if (toolTip != null)
                                resultSet.setToolTip(toolTip);
                        } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                            if (documentation != null)
                                resultSet.setDocumentation(documentation);
                        }
                        if (anchorOffset > -1)
                            resultSet.setAnchorOffset(anchorOffset);
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                resultSet.finish();
            }
        }
        
        @Override
        protected boolean canFilter(JTextComponent component) {
            filterPrefix = null;

            int newOffset = component.getSelectionStart();

            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                if (newOffset >= caretOffset) {
                    if (anchorOffset > -1) {
                        try {
                            String prefix =
                                component.getDocument()
                                         .getText(anchorOffset, newOffset - anchorOffset);

                            if (isJavaIdentifierPart(prefix)) {
                                filterPrefix = prefix;
                            }
                        } catch (BadLocationException e) {
                        }
                    }
                }

                return filterPrefix != null;
            } else if (queryType == TOOLTIP_QUERY_TYPE) {
                try {
                    if (newOffset == caretOffset)
                        filterPrefix = "";
                    else if (newOffset - caretOffset > 0)
                        filterPrefix = component.getDocument().getText(caretOffset, newOffset - caretOffset);
                    else if (newOffset - caretOffset < 0)
                        filterPrefix = component.getDocument().getText(newOffset, caretOffset - newOffset);
                } catch (BadLocationException ex) {}
                return (filterPrefix != null && filterPrefix.indexOf(',') == -1 && filterPrefix.indexOf('(') == -1 && filterPrefix.indexOf(')') == -1); // NOI18N
            }

            return false;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                    if (results != null) {
                        if (filterPrefix != null) {
                            resultSet.addAllItems(getFilteredData(results, filterPrefix));
                        } else {
                            Completion.get().hideDocumentation();
                            Completion.get().hideCompletion();
                        }
                    }
                } else if (queryType == TOOLTIP_QUERY_TYPE) {
                    resultSet.setToolTip(toolTip);
                }

                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            resultSet.finish();
        }

        public void run(CompilationController controller)
            throws Exception {
            if (controller.getDocument() == null) {
                return;
            }

            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                resolveCompletion(controller);
            } else if (queryType == TOOLTIP_QUERY_TYPE) {
                resolveToolTip(controller);
            } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                resolveDocumentation(controller);
            }
            GsfCompletionItem.tipProposal = null;
        }

        public void cancel() {
        }
        
        private void resolveToolTip(final CompilationController controller) throws IOException {
            CompletionProposal proposal = GsfCompletionItem.tipProposal;
            Env env = getCompletionEnvironment(controller, false);
            Completable completer = env.getCompletable();

            if (completer != null) {
                int offset = env.getOffset();
                ParameterInfo info = completer.parameters(controller, offset, proposal);
                if (info != ParameterInfo.NONE) {
                    
                    List<String> params = info.getNames();
             
                    // Take the parameter list, and balance them out into
                    // a "2d" set of lists used by the method params tip component:
                    // a list of lists - one for each row, and each row is a list
                    // for the clumn
                    int MAX_WIDTH = 50; // Max width before wrapping to the next line
                    int column = 0;
                    List<List<String>> parameterList = new ArrayList<List<String>>();
                    List<String> p = new ArrayList<String>();
                    for (int length = params.size(), i = 0; i < length; i++) {
                        String parameter = params.get(i);
                        if (i < length-1) {
                            parameter = parameter + ", ";
                        }
                        p.add(parameter);
                        
                        column += parameter.length();
                        if (column > MAX_WIDTH) {
                            column = 0;
                            parameterList.add(p);
                            p = new ArrayList<String>();
                            
                        }
                    }
                    if (p.size() > 0) {
                        parameterList.add(p);
                    }
            
                    int index = info.getCurrentIndex();
                    anchorOffset = info.getAnchorOffset();
                    toolTip = new MethodParamsTipPaintComponent(parameterList, index, component);
                    //startPos = (int)sourcePositions.getEndPosition(env.getRoot(), mi.getMethodSelect());
                    //String text = controller.getText().substring(startPos, offset);
                    //anchorOffset = startPos + controller.getPositionConverter().getOriginalPosition(text.indexOf('(')); //NOI18N
                    //toolTipOffset = startPos + controller.getPositionConverter().getOriginalPosition(text.lastIndexOf(',')); //NOI18N
                    //if (toolTipOffset < anchorOffset)
                    //    toolTipOffset = anchorOffset;
                    return;

                }
            }
        }        

        private void resolveDocumentation(CompilationController controller)
            throws IOException {
            controller.toPhase(Phase.RESOLVED);

            if (element != null) {
                documentation = GsfCompletionDoc.create(controller, element);
            } else {
                Env env = getCompletionEnvironment(controller, false);
                int offset = env.getOffset();
                String prefix = env.getPrefix();
                results = new ArrayList<CompletionItem>();
                anchorOffset = env.getOffset() - ((prefix != null) ? prefix.length() : 0);

                Completable completer = env.getCompletable();

                if (completer != null) {
                    List<CompletionProposal> proposals =
                        completer.complete(controller, offset, prefix, NameKind.EXACT_NAME, QueryType.DOCUMENTATION,
                            isCaseSensitive(), new CompletionFormatter());

                    if (proposals != null) {
                        for (CompletionProposal proposal : proposals) {
                            ElementHandle element = proposal.getElement();
                            if (element != null) {
                                documentation = GsfCompletionDoc.create(controller, element);
                                // TODO - find some way to show the multiple overloaded methods?
                                if (documentation.getText() != null && documentation.getText().length() > 0) {
                                    // Make sure we at least pick an alternative that has documentation
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        private void resolveCompletion(CompilationController controller)
            throws IOException {
            Env env = getCompletionEnvironment(controller, true);
            int offset = env.getOffset();
            String prefix = env.getPrefix();
            results = new ArrayList<CompletionItem>();
            anchorOffset = env.getOffset() - ((prefix != null) ? prefix.length() : 0);

            Completable completer = env.getCompletable();

            if (completer != null) {
                List<CompletionProposal> proposals =
                    completer.complete(controller, offset, prefix, 
                    isCaseSensitive() ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX, 
                    QueryType.COMPLETION, isCaseSensitive(), new CompletionFormatter());

                if (proposals != null) {
                    for (CompletionProposal proposal : proposals) {
                        GsfCompletionItem item = GsfCompletionItem.createItem(proposal, controller);

                        if (item != null) {
                            results.add(item);
                        }
                    }
                }

                // If we automatically queried, and there were no hits, take it down
                if (isAutoQuery && (proposals == null || proposals.size() == 0)) {
                    Completion.get().hideCompletion();
                    expectingCreateTask = false;
                }
            }
        }

        // TODO - delegate to language support!
        private boolean isJavaIdentifierPart(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i)))) {
                    return false;
                }
            }

            return true;
        }

        private Collection getFilteredData(Collection<CompletionItem> data, String prefix) {
            if (prefix.length() == 0) {
                return data;
            }

            List ret = new ArrayList();

            for (Iterator<CompletionItem> it = data.iterator(); it.hasNext();) {
                CompletionItem itm = it.next();

                if (startsWith(itm.getInsertPrefix().toString(), prefix)) {
                    ret.add(itm);
                }

                //                else if (itm instanceof LazyTypeCompletionItem && Utilities.startsWith(((LazyTypeCompletionItem)itm).getItemText(), prefix))
                //                    ret.add(itm);
            }

            return ret;
        }

        /**
         * 
         * @param upToOffset If set, complete only up to the given caret offset, otherwise complete
         *   the full symbol at the offset
         */
        private Env getCompletionEnvironment(CompilationController controller, boolean upToOffset)
            throws IOException {
            // If you invoke code completion while indexing is in progress, the
            // completion job (which stores the caret offset) will be delayed until
            // indexing is complete - potentially minutes later. When the job
            // is finally run we need to make sure the caret position is still valid. (93017)
            Document doc = controller.getDocument();
            int length = doc != null ? doc.getLength() : (int)controller.getFileObject().getSize();
            if (caretOffset > length) {
                caretOffset = length;
            }
            
            int offset = caretOffset;
            String prefix = null;

            // 
            // TODO - handle the upToOffset parameter
            // Look at the parse tree, and find the corresponding end node
            // offset...
            
            Completable completer = getCompletable(controller, offset);
            try {
                // TODO: use the completion helper to get the contxt
                if (completer != null) {
                    prefix = completer.getPrefix(controller, offset, upToOffset);
                }
                if (prefix == null) {
                    int[] blk =
                        org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)controller.getDocument(),
                            offset);

                    if (blk != null) {
                        int start = blk[0];

                        if (start < offset ) {
                            if (upToOffset) {
                                prefix = controller.getDocument().getText(start, offset - start);
                            } else {
                                prefix = controller.getDocument().getText(start, blk[1]-start);
                            }
                        }
                    }
                }
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            
            controller.toPhase(Phase.PARSED);

            return new Env(offset, prefix, controller, completer);
        }
        
        private class Env {
            private int offset;
            private String prefix;
            private CompilationController controller;
            private Completable completable;
            private boolean autoCompleting;

            private Env(int offset, String prefix, CompilationController controller, Completable completable) {
                this.offset = offset;
                this.prefix = prefix;
                this.controller = controller;
                this.completable = completable;
            }

            public int getOffset() {
                return offset;
            }

            public String getPrefix() {
                return prefix;
            }
            
            public boolean isAutoCompleting() {
                return autoCompleting;
            }
            
            public void setAutoCompleting(boolean autoCompleting) {
                this.autoCompleting = autoCompleting;
            }

            public CompilationController getController() {
                return controller;
            }
            
            public Completable getCompletable() {
                return completable;
            }
        }
    }
    
    /** Format parameters in orange etc. */
    private static class CompletionFormatter extends GsfHtmlFormatter {
        private static final String METHOD_COLOR = "<font color=#000000>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#a06001>"; //NOI18N
        private static final String END_COLOR = "</font>"; // NOI18N
        private static final String CLASS_COLOR = "<font color=#560000>"; //NOI18N
        private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
        private static final String KEYWORD_COLOR = "<font color=#000099>"; //NOI18N
        private static final String FIELD_COLOR = "<font color=#008618>"; //NOI18N
        private static final String VARIABLE_COLOR = "<font color=#00007c>"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = "<font color=#b28b00>"; //NOI18N
        private static final String INTERFACE_COLOR = "<font color=#404040>"; //NOI18N
        private static final String PARAMETERS_COLOR = "<font color=#808080>"; //NOI18N
        private static final String ACTIVE_PARAMETER_COLOR = "<font color=#000000>"; //NOI18N

        @Override
        public void parameters(boolean start) {
            assert start != isParameter;
            isParameter = start;

            if (isParameter) {
                sb.append(PARAMETER_NAME_COLOR);
            } else {
                sb.append(END_COLOR);
            }
        }
        
        @Override
        public void active(boolean start) {
            if (start) {
                sb.append(ACTIVE_PARAMETER_COLOR);
                sb.append("<b>");
            } else {
                sb.append("</b>");
                sb.append(END_COLOR);
            }
        }
        
        @Override
        public void name(ElementKind kind, boolean start) {
            assert start != isName;
            isName = start;

            if (isName) {
                switch (kind) {
                case CONSTRUCTOR:
                    sb.append(CONSTRUCTOR_COLOR);
                    break;
                case CALL:
                    sb.append(PARAMETERS_COLOR);
                    break;
                case DB:
                case METHOD:
                    sb.append(METHOD_COLOR);
                     break;
                case CLASS:
                    sb.append(CLASS_COLOR);
                    break;
                case FIELD:
                    sb.append(FIELD_COLOR);
                    break;
                case MODULE:
                    sb.append(PKG_COLOR);
                    break;
                case KEYWORD:
                    sb.append(KEYWORD_COLOR);
                    sb.append("<b>");
                    break;
                case VARIABLE:
                    sb.append(VARIABLE_COLOR);
                    sb.append("<b>");
                    break;
                default:
                    sb.append("<font>");
                }
            } else {
                switch (kind) {
                case KEYWORD:
                case VARIABLE:
                    sb.append("</b>");
                    break;
                }
                sb.append(END_COLOR);
            }
        }
        
    }
    
    // From Utilities
    private static boolean caseSensitive = true;
    private static boolean inited;

    public static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    private static class SettingsListener implements SettingsChangeListener {

        public void settingsChange(SettingsChangeEvent evt) {
            setCaseSensitive(SettingsUtil.getBoolean(GsfEditorKitFactory.GsfEditorKit.class,
                    ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                    ExtSettingsDefaults.defaultCompletionCaseSensitive));
        }
    }

    private static SettingsChangeListener settingsListener = new SettingsListener();

    public static void setCaseSensitive(boolean b) {
        lazyInit();
        caseSensitive = b;
    }

    private static void lazyInit() {
        if (!inited) {
            inited = true;
            Settings.addSettingsChangeListener(settingsListener);
            setCaseSensitive(SettingsUtil.getBoolean(GsfEditorKitFactory.GsfEditorKit.class,
                    ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                    ExtSettingsDefaults.defaultCompletionCaseSensitive));
        }
    }
}
