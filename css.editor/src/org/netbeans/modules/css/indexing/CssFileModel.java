/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.lib.editor.util.CharSubSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Instances of this class represents a css model associated with a snapshot of the file content.
 *
 * TODO: make it CssIndexModel so it uses the generic mechanism.
 * 
 * @author mfukala@netbeans.org
 */
public class CssFileModel {

    private static final Logger LOGGER = Logger.getLogger(CssIndex.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private static final Pattern URI_PATTERN = Pattern.compile("url\\(\\s*(.*)\\s*\\)"); //NOI18N
    private Collection<Entry> classes, ids, htmlElements, imports, colors;
    private final Snapshot snapshot;
    private final Snapshot topLevelSnapshot;

    public static CssFileModel create(Source source) throws ParseException {
        final AtomicReference<CssParserResult> result = new AtomicReference<CssParserResult>();
        final AtomicReference<Snapshot> snapshot = new AtomicReference<Snapshot>();
        ParserManager.parse(Collections.singletonList(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator cssRi = WebUtils.getResultIterator(resultIterator, CssLanguage.CSS_MIME_TYPE);
                snapshot.set(resultIterator.getSnapshot());
                result.set(cssRi == null ? null : (CssParserResult) cssRi.getParserResult());
            }
        });

        assert snapshot.get() != null; //at least the top level snapshot should always be available

        return result.get() == null ? new CssFileModel(snapshot.get()) : new CssFileModel(result.get(), snapshot.get());
    }

    public static CssFileModel create(CssParserResult result) {
        return new CssFileModel(result, null);
    }

    private CssFileModel(Snapshot topLevelSnapshot) {
        this.snapshot = this.topLevelSnapshot = topLevelSnapshot;
    }

    private CssFileModel(CssParserResult parserResult, Snapshot topLevelSnapshot) {
        this.snapshot = parserResult.getSnapshot();
        this.topLevelSnapshot = topLevelSnapshot;
        if ( parserResult.getParseTree() != null) {
            ParseTreeVisitor visitor = new ParseTreeVisitor();
            visitor.visitChildren(parserResult.getParseTree());
        } //else broken source, no parse tree

    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public Snapshot getTopLevelSnapshot() {
        return topLevelSnapshot;
    }

    public FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public Collection<Entry> get(RefactoringElementType type) {
        switch (type) {
            case CLASS:
                return getClasses();
            case ID:
                return getIds();
            case COLOR:
                return getColors();
            case ELEMENT:
                return htmlElements;
            case IMPORT:
                return imports;
        }

        return null;
    }

    public Collection<Entry> getClasses() {
        return classes == null ? Collections.<Entry>emptyList() : classes;
    }

    public Collection<Entry> getIds() {
        return ids == null ? Collections.<Entry>emptyList() : ids;
    }

    public Collection<Entry> getHtmlElements() {
        return htmlElements == null ? Collections.<Entry>emptyList() : htmlElements;
    }

    public Collection<Entry> getImports() {
        return imports == null ? Collections.<Entry>emptyList() : imports;
    }

    public Collection<Entry> getColors() {
        return colors == null ? Collections.<Entry>emptyList() : colors;
    }

    /**
     *
     * @return true if the model is empty - nothing interesting found in the page.
     */
    public boolean isEmpty() {
        return null == classes && null == ids && null == htmlElements && null == imports && null == colors;
    }

    //single threaded - called from constructor only, no need for synch
    private Collection<Entry> getClassesCollectionInstance() {
        if (classes == null) {
            classes = new ArrayList<Entry>();
        }
        return classes;
    }

    private Collection<Entry> getIdsCollectionInstance() {
        if (ids == null) {
            ids = new ArrayList<Entry>();
        }
        return ids;
    }

    private Collection<Entry> getHtmlElementsCollectionInstance() {
        if (htmlElements == null) {
            htmlElements = new ArrayList<Entry>();
        }
        return htmlElements;
    }

    private Collection<Entry> getImportsCollectionInstance() {
        if (imports == null) {
            imports = new ArrayList<Entry>();
        }
        return imports;
    }

    private Collection<Entry> getColorsCollectionInstance() {
        if (colors == null) {
            colors = new ArrayList<Entry>();
        }
        return colors;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(":");
        for (Entry c : getImports()) {
            buf.append(" imports=");
            buf.append(c);
            buf.append(',');
        }
        for (Entry c : getClasses()) {
            buf.append('.');
            buf.append(c);
            buf.append(',');
        }
        for (Entry c : getIds()) {
            buf.append('#');
            buf.append(c);
            buf.append(',');
        }
        for (Entry c : getHtmlElements()) {
            buf.append(c);
            buf.append(',');
        }

        return buf.toString();
    }

    private class ParseTreeVisitor extends NodeVisitor {

        private int[] currentBodyRange;
        
        @Override
        public boolean visit(Node node) {
            if (node.type() == NodeType.importItem) {
                getImportsCollectionInstance().addAll(getImports(node));
            } else if (node.type() == NodeType.rule) {
                currentBodyRange = NodeUtil.getRuleBodyRange(node);
            } else if (NodeUtil.isSelectorNode(node)) {

                if(!NodeUtil.containsError(node)) {
                    Collection<Entry> collection;
                    int start_offset_diff;

                    switch (node.type()) {
                        case cssClass:
                            collection = getClassesCollectionInstance();
                            start_offset_diff = 1; //cut off the dot (.)
                            break;
                        case cssId:
                            collection = getIdsCollectionInstance();
                            start_offset_diff = 1; //cut of the hash (#)
                            break;
                        case elementName:
                            collection = getHtmlElementsCollectionInstance();
                            start_offset_diff = 0;
                            break;
                        default:
                            throw new IllegalStateException();
                    }

                    CharSequence image = node.image().subSequence(start_offset_diff, node.image().length());
                    OffsetRange range = new OffsetRange(node.from() + start_offset_diff, node.to());

                    //check if the real start offset can be translated to the original offset
                    boolean isVirtual = getSnapshot().getOriginalOffset(node.from()) == -1;

                    OffsetRange body = currentBodyRange != null ? new OffsetRange(currentBodyRange[0], currentBodyRange[1]) : OffsetRange.NONE;
                    Entry e = createEntry(image.toString(), range, body, isVirtual);
                    if (e != null) {
                        collection.add(e);
                    }
                }

            } else if (node.type() == NodeType.hexColor) {
                CharSequence image = node.image();
                int[] wsLens = getTextWSPreAndPostLens(image);
                image = image.subSequence(wsLens[0], image.length() - wsLens[1]);
                OffsetRange range = new OffsetRange(node.from() + wsLens[0], node.to() - wsLens[1]);
                Entry e = createEntry(image.toString(), range, false);
                if (e != null) {
                    getColorsCollectionInstance().add(e);
                }
            }
            return false;
        }

        private Collection<Entry> getImports(Node node) {
            Collection<Entry> imports = new ArrayList<Entry>();
            //@import "resources/global.css";
            Node[] resourceIdentifiers = NodeUtil.getChildrenByType(node, NodeType.resourceIdentifier);
            //scss import can contain several resourece separated by comma
            for(Node resourceIdentifier : resourceIdentifiers) {
                Node token = NodeUtil.getChildTokenNode(resourceIdentifier, CssTokenId.STRING);
                if (token != null) {
                    CharSequence image = token.image();
                    boolean quoted = WebUtils.isValueQuoted(image);
                    imports.add(createEntry(WebUtils.unquotedValue(image),
                            new OffsetRange(token.from() + (quoted ? 1 : 0),
                            token.to() - (quoted ? 1 : 0)),
                            false));
                }

                //@import url("another.css");
                token = NodeUtil.getChildTokenNode(resourceIdentifier, CssTokenId.URI);
                if (token != null) {
                    Matcher m = URI_PATTERN.matcher(token.image());
                    if (m.matches()) {
                        int groupIndex = 1;
                        String content = m.group(groupIndex);
                        boolean quoted = WebUtils.isValueQuoted(content);
                        int from = token.from() + m.start(groupIndex) + (quoted ? 1 : 0);
                        int to = token.from() + m.end(groupIndex) - (quoted ? 1 : 0);
                        imports.add(createEntry(WebUtils.unquotedValue(content),
                                new OffsetRange(from, to),
                                false));
                    }
                }
            }

            return imports;
        }
    }

    private Entry createEntry(String name, OffsetRange range, boolean isVirtual) {
        return createEntry(name, range, null, isVirtual);
    }

    private Entry createEntry(String name, OffsetRange range, OffsetRange bodyRange, boolean isVirtual) {
        //do not create entries for virtual generated code
//        if (CssGSFParser.containsGeneratedCode(name)) {
//            return null;
//        }

        return new LazyEntry(name, range, bodyRange, isVirtual);
    }

    private static int[] getTextWSPreAndPostLens(CharSequence text) {
        int preWSlen = 0;
        int postWSlen = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                preWSlen++;
            } else {
                break;
            }
        }

        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                postWSlen++;
            } else {
                break;
            }
        }

        return new int[]{preWSlen, postWSlen};
    }

    public class LazyEntry implements Entry {

        private final String name;
        private final OffsetRange range, bodyRange;
        private final boolean isVirtual;

        //computed lazily
        private OffsetRange documentRange, documentBodyRange;
        private CharSequence elementText, elementLineText;
        private int lineOffset = -1;

        public LazyEntry(String name, OffsetRange range, OffsetRange bodyRange, boolean isVirtual) {
            this.name = name;
            this.range = range;
            this.bodyRange = bodyRange;
            this.isVirtual = isVirtual;
        }

        @Override
        public boolean isVirtual() {
            return isVirtual;
        }

        @Override
        public boolean isValidInSourceDocument() {
            return getDocumentRange() != OffsetRange.NONE;
        }

        @Override
        public synchronized int getLineOffset() {
            if(lineOffset == -1) {
                if (topLevelSnapshot != null && isValidInSourceDocument()) {
                    try {
                        lineOffset = LexerUtils.getLineOffset(topLevelSnapshot.getText(), getDocumentRange().getStart());
                    } catch (BadLocationException ex) {
                        //no-op
                    }
                }
            }
            return lineOffset;
        }

        @Override
        public synchronized CharSequence getText() {
            if(elementText == null) {
                //delegate to the underlying source charsequence, do not duplicate any chars!
                elementText = new CharSubSequence(getSnapshot().getText(),  range.getStart(), range.getEnd());
            }
            return elementText;
        }

        @Override
        public synchronized CharSequence getLineText() {
            if(elementLineText == null) {
                try {
                    int astLineStart = GsfUtilities.getRowStart(getSnapshot().getText(), range.getStart());
                    int astLineEnd = GsfUtilities.getRowEnd(getSnapshot().getText(), range.getStart());

                    elementLineText = astLineStart != -1 && astLineEnd != -1
                            ? getSnapshot().getText().subSequence(astLineStart, astLineEnd)
                            : null;

                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return elementLineText;

        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public synchronized OffsetRange getDocumentRange() {
            if(documentRange == null) {
                int documentFrom = getSnapshot().getOriginalOffset(range.getStart());
                int documentTo = getSnapshot().getOriginalOffset(range.getEnd());

                documentRange = documentFrom != -1 && documentTo != -1 ? new OffsetRange(documentFrom, documentTo) : OffsetRange.NONE;
            }
            return documentRange;
        }

        @Override
        public OffsetRange getRange() {
            return range;
        }

        @Override
        public OffsetRange getBodyRange() {
            return bodyRange;
        }

        @Override
        public synchronized OffsetRange getDocumentBodyRange() {
            if (documentBodyRange == null) {
                if (bodyRange != null) {
                    int bodyDocFrom = getSnapshot().getOriginalOffset(bodyRange.getStart());
                    int bodyDocTo = getSnapshot().getOriginalOffset(bodyRange.getEnd());

                    documentBodyRange = bodyDocFrom != -1 && bodyDocTo != -1
                            ? new OffsetRange(bodyDocFrom, bodyDocTo)
                            : OffsetRange.NONE;
                }
            }

            return documentBodyRange;
        }

        @Override
        public String toString() {
            return "Entry[" + (!isValidInSourceDocument() ? "INVALID! " : "") + getName() + "; " + getRange().getStart() + " - " + getRange().getEnd() + "]"; //NOI18N
        }

    }

}
