/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.editor.completion;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import javax.lang.model.element.ElementKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.completion.db.DBCompletionContextResolver;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * see NNCompletionProvider and NNCompletionQuery as nb 5.5 precursors for this class 
 * @author sp153251
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class)//NOI18N
public class JPACodeCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new JPACodeCompletionQuery(queryType, component, component.getSelectionStart(), true), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;//will not appear automatically
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        org.netbeans.editor.ext.ExtSyntaxSupport f = new org.netbeans.editor.ext.ExtSyntaxSupport(null);
        return -1;
    }

    class JPACodeCompletionQuery extends AsyncCompletionQuery {

        private ArrayList<CompletionContextResolver> resolvers;
        private List<JPACompletionItem> results;
        private byte hasAdditionalItems = 0; //no additional items
        private CompletionDocumentation documentation;
        private int anchorOffset;
        private int toolTipOffset;
        private JTextComponent component;
        private int queryType;
        private int caretOffset;
        private String filterPrefix;
        private ElementHandle element;
        private boolean hasTask;

        public JPACodeCompletionQuery(int queryType, JTextComponent component, int caretOffset, boolean hasTask) {
            this.queryType = queryType;
            this.caretOffset = caretOffset;
            this.hasTask = hasTask;
            this.component = component;
            initResolvers();
        }

        private void initResolvers() {
            //XXX temporary - should be registered somehow better
            resolvers = new ArrayList<CompletionContextResolver>();
            resolvers.add(new DBCompletionContextResolver());
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            {
                try {
                    this.caretOffset = caretOffset;
                    //if (queryType == TOOLTIP_QUERY_TYPE || Utilities.isJavaContext(component, caretOffset)) 
                    {
                        results = null;
                        documentation = null;
                        anchorOffset = -1;
                        Source source = Source.create(doc);
                        if (source != null) {
                            Future<Void> f = ParserManager.parseWhenScanFinished(Collections.singletonList(source), getTask());
                            if (!f.isDone()) {
                                component.putClientProperty("completion-active", Boolean.FALSE); //NOI18N
                                resultSet.setWaitText(NbBundle.getMessage(JPACodeCompletionProvider.class, "scanning-in-progress")); //NOI18N
                                f.get();
                            }
                            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                                if (results != null) {
                                    resultSet.addAllItems(results);
                                }
                                resultSet.setHasAdditionalItems(hasAdditionalItems > 0);
                                if (hasAdditionalItems == 1) {
                                    resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JPACodeCompletionProvider.class, "JCP-imported-items")); //NOI18N
                                }
                                if (hasAdditionalItems == 2) {
                                    resultSet.setHasAdditionalItemsText(NbBundle.getMessage(JPACodeCompletionProvider.class, "JCP-instance-members")); //NOI18N
                                }
                            }
                            if (anchorOffset > -1) {
                                resultSet.setAnchorOffset(anchorOffset);
                            }
                        }
                    }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    resultSet.finish();
                }
            }

        }

        private UserTask getTask() {
            return new Task();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            return false;
//            filterPrefix = null;
//            int newOffset = component.getSelectionStart();
//            if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
//                int offset = Math.min(anchorOffset, caretOffset);
//                if (offset > -1) {
//                    if (newOffset < offset)
//                        return true;
//                    if (newOffset >= caretOffset) {
//                        try {
//                            String prefix = component.getDocument().getText(offset, newOffset - offset);
//                            filterPrefix = isJavaIdentifierPart(prefix) ? prefix : null;
//                            if (filterPrefix == null) {
//                            } else if (filterPrefix.length() == 0) {
//                                anchorOffset = newOffset;
//                            }
//                        } catch (BadLocationException e) {}
//                        return true;
//                    }
//                }
//                return false;
//            } else if (queryType == TOOLTIP_QUERY_TYPE) {
//                try {
//                    if (newOffset == caretOffset)
//                        filterPrefix = EMPTY;
//                    else if (newOffset - caretOffset > 0)
//                        filterPrefix = component.getDocument().getText(caretOffset, newOffset - caretOffset);
//                    else if (newOffset - caretOffset < 0)
//                        filterPrefix = newOffset > toolTipOffset ? component.getDocument().getText(newOffset, caretOffset - newOffset) : null;
//                } catch (BadLocationException ex) {}
//                return (filterPrefix != null && filterPrefix.indexOf(',') == -1 && filterPrefix.indexOf('(') == -1 && filterPrefix.indexOf(')') == -1); // NOI18N
//            }
//            return false;
        }
        
        @Override
        protected void filter(CompletionResultSet resultSet) {
            try {
                if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
                    if (results != null) {
                        if (filterPrefix != null) {
                            resultSet.addAllItems(getFilteredData(results, filterPrefix));
                            resultSet.setHasAdditionalItems(hasAdditionalItems > 0);
                        } else {
                            Completion.get().hideDocumentation();
                            Completion.get().hideCompletion();
                        }
                    }
                } 
                resultSet.setAnchorOffset(anchorOffset);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            resultSet.finish();
        }
        private Collection getFilteredData(Collection<JPACompletionItem> data, String prefix) {
            if (prefix.length() == 0)
                return data;
            List ret = new ArrayList();
            boolean camelCase = isCamelCasePrefix(prefix);
            for (Iterator<JPACompletionItem> it = data.iterator(); it.hasNext();) {
                CompletionItem itm = it.next();
                if (itm.getInsertPrefix().toString().startsWith(prefix))
                    ret.add(itm);
            }
            return ret;
        }
        private void run(CompilationController controller) {
            String filter = null;
            int startOffset = caretOffset;
            Iterator resolversItr = resolvers.iterator();
            TreePath env = null;
            try {
                env = getCompletionTreePath(controller, caretOffset, CompletionProvider.COMPLETION_QUERY_TYPE);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (env == null) {
                return;
            }
            results = new ArrayList<JPACompletionItem>();
            while (resolversItr.hasNext()) {
                CompletionContextResolver resolver = (CompletionContextResolver) resolversItr.next();
                Context ctx = new Context(component, controller, startOffset, false);
                if (ctx.getEntityMappings() == null) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "No EnitityMappings defined.");
                    break;
                } else {
                    results.addAll(resolver.resolve(ctx));
                }
            }
        }

        private class Task extends UserTask {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult(caretOffset);
                CompilationController controller = result != null ? CompilationController.get(result) : null;
                if (controller != null) {
                    JPACodeCompletionQuery.this.run(controller);
                }
            }
        }
    }
    //

    private TreePath getCompletionTreePath(CompilationController controller, int curOffset, int queryType) throws IOException {
        controller.toPhase(Phase.PARSED);
        int offset = controller.getSnapshot().getEmbeddedOffset(curOffset);
        if (offset < 0) {
            return null;
        }
        boolean complQuery = (queryType & COMPLETION_QUERY_TYPE) != 0;
        String prefix = null;
        if (offset > 0) {
            if (complQuery) {
                TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                // When right at the token end move to previous token; otherwise move to the token that "contains" the offset
                if (ts.move(offset) == 0 || !ts.moveNext()) {
                    ts.movePrevious();
                }
                int len = offset - ts.offset();
                if (len > 0 && (ts.token().id() == JavaTokenId.IDENTIFIER
                        || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                        ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                        ts.token().id().primaryCategory().equals("literal")) //NOI18N
                        && ts.token().length() >= len) { //TODO: Use isKeyword(...) when available
                    prefix = ts.token().toString().substring(0, len);
                    offset = ts.offset();
                }
            } else if (queryType == DOCUMENTATION_QUERY_TYPE) {
                TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                // When right at the token start move offset to the position "inside" the token
                ts.move(offset);
                if (!ts.moveNext()) {
                    ts.movePrevious();
                }
                if (ts.offset() == offset && ts.token().length() > 0
                        && (ts.token().id() == JavaTokenId.IDENTIFIER
                        || ts.token().id().primaryCategory().startsWith("keyword") || //NOI18N
                        ts.token().id().primaryCategory().startsWith("string") || //NOI18N
                        ts.token().id().primaryCategory().equals("literal"))) { //NOI18N
                    offset++;
                }
            }
        }
        TreePath path = controller.getTreeUtilities().pathFor(offset);
        return path;
    }
    private static boolean isJavaIdentifierPart(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!(Character.isJavaIdentifierPart(text.charAt(i))))
                return false;
        }
        return true;
    }
    private static boolean isCamelCasePrefix(String prefix) {
        if (prefix == null || prefix.length() < 2 || prefix.charAt(0) == '"')
            return false;
        for (int i = 1; i < prefix.length(); i++) {
            if (Character.isUpperCase(prefix.charAt(i)))
                    return true;                
        }
        return false;
    }
    public final class Context {

        /** Text component */
        private JTextComponent component;
        CompilationController controller;
        /** End position of the scanning - usually the caret position */
        private int endOffset;
        private PersistenceUnit[] pus;
        private EntityMappings emaps;
        /** True when code completion is invoked by auto popup. In such case, code completion returns no result
         * after "new ". To get a result, code completion has to be invoked manually (using Ctrl-Space). */ // NOI18N
        private boolean autoPopup;
        private String completedMemberName, completedMemberJavaClassName;
        private CCParser CCParser;
        private CCParser.CC parsednn = null;

        public Context(JTextComponent component, CompilationController controller, int endOffset, boolean autoPopup) {
            this.component = component;
            this.controller = controller;
            this.endOffset = endOffset;
            this.autoPopup = autoPopup;


            FileObject documentFO = getFileObject();
            if (documentFO != null) {
                try {
                    this.pus = PersistenceUtils.getPersistenceUnits(documentFO);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                this.emaps = PersistenceUtils.getEntityMappings(documentFO);
            }

            this.CCParser = new CCParser(controller);
        }

        /** Must be run under MDR transaction! */
        public javax.lang.model.element.Element getJavaClass() {
            TreePath path = null;
            try {
                path = getCompletionTreePath(controller, endOffset, COMPLETION_QUERY_TYPE);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            javax.lang.model.element.Element el = null;
            try {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            while ((el == null || !(ElementKind.CLASS == el.getKind() || ElementKind.INTERFACE == el.getKind())) && path != null) {
                path.getCompilationUnit().getTypeDecls();
                el = controller.getTrees().getElement(path);
                path = path.getParentPath();
            }
            return el;
        }

        public BaseDocument getBaseDocument() {
            BaseDocument doc = (BaseDocument) component.getDocument();
            return doc;
        }

        public FileObject getFileObject() {
            try {
                return URLMapper.findFileObject(controller.getCompilationUnit().getSourceFile().toUri().toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        /** @return an arrat of PUs which this sourcefile belongs to. */
        public PersistenceUnit[] getPersistenceUnits() {
            return this.pus;
        }

        public EntityMappings getEntityMappings() {
            return this.emaps;
        }

        public int getCompletionOffset() {
            return endOffset;
        }

        public CCParser.CC getParsedAnnotation() {
            synchronized (CCParser) {
                if (parsednn == null) {
                    parsednn = CCParser.parseAnnotation(getCompletionOffset());
                }
                return parsednn;
            }
        }

        public String getCompletedMemberClassName() {
            if (completedMemberJavaClassName == null) {
                initCompletedMemberContext();
            }
            return completedMemberJavaClassName;
        }

        public String getCompletedMemberName() {
            if (completedMemberName == null) {
                initCompletedMemberContext();
            }
            return completedMemberName;
        }

        private void initCompletedMemberContext() {
            //parse the text behind the cursor and try to find identifiers.
            //it seems to be impossible to use JMI model for this since it havily
            //relies on the state of the source (whether it contains errors, which types etc.)
            String type = null;
            String genericType = null;
            String propertyName = null;
            CCParser nnp = new CCParser(controller); //helper parser

            TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            ts.move(getCompletionOffset() + 1);
            nextNonWhitespaceToken(ts);
            Token<JavaTokenId> ti = ts.token();
            while (ti != null && propertyName == null) {
                javax.lang.model.element.Element el = null;
                try {
                    el = controller.getTrees().getElement(getCompletionTreePath(controller, ts.offset() + 1, CompletionProvider.COMPLETION_QUERY_TYPE));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                //skip all annotations between the CC offset and the completed member
                if (el.getKind() == ElementKind.ANNOTATION_TYPE) {
                    //parse to find NN end
                    CCParser.CC parsed = nnp.parseAnnotation(ts.offset() + 1);
                    if (parsed != null) {
                        //parse after the NN end (skip)
                        ts.move(parsed.getEndOffset());
                        ti = ts.token();
                        continue;
                    }
                }

                //test whether we have just found a type and '<' character after
                if (genericType != null && ti.id() == JavaTokenId.LT) {
                    //maybe a start of generic
                    ts.moveNext();
                    Token<JavaTokenId> ti2 = ts.token();
                    if (ti2.id() == JavaTokenId.IDENTIFIER) {
                        //found generic
                        //genericType = ti2.getImage();
                        //ti = ti.getNext(); //skip the next IDENTIFIER token so it is not considered as property name
                    } else {
                        //false alarm
                        genericType = null;
                    }
                } else if (ti.id() == JavaTokenId.IDENTIFIER) {
                    if (type == null) {
                        //type = ti.getImage();
                        genericType = type;
                    } else {
                        //propertyName = ti.getImage();
                    }
                }
                ts.moveNext();
                ti = ts.token();
            }

            completedMemberName = propertyName;
            completedMemberJavaClassName = genericType == null ? type : genericType;
        }

        private TokenSequence<JavaTokenId> nextNonWhitespaceToken(TokenSequence<JavaTokenId> ts) {
            while (ts.moveNext()) {
                int offset = ts.offset();
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case JAVADOC_COMMENT:
                        break;
                    default:
                        return ts;
                }
            }
            return null;
        }
    }
    
    private static final String EMPTY = ""; //NOI18N
}