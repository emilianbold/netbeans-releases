/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.refactoring.api.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.gsf.CssLanguage;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.CssParserConstants;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.css.parser.Token;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class CssFileModel {

    private static final Logger LOGGER = Logger.getLogger(CssIndex.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    //private static final Pattern URI_PATTERN = Pattern.compile("url\\(\\s*[\\u0022]?([^\\u0022]*)[\\u0022]?\\s*\\)"); //NOI18N
    private static final Pattern URI_PATTERN = Pattern.compile("url\\(\\s*(.*)\\s*\\)");
    private Collection<Entry> classes, ids, htmlElements, imports, colors;
    private CssParserResult parserResult;

    public CssFileModel(Source source) throws ParseException {
        ParserManager.parse(Collections.singletonList(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator cssRi = WebUtils.getResultIterator(resultIterator, CssLanguage.CSS_MIME_TYPE);
                if (cssRi != null) {
                    parserResult = (CssParserResult)cssRi.getParserResult();
                    init();
                }
            }
        });
    }

    public CssFileModel(CssParserResult parserResult) {
        this.parserResult = parserResult;
        init();
    }

    public CssParserResult getParserResult() {
        return parserResult;
    }

    public Snapshot getSnapshot() {
        return parserResult.getSnapshot();
    }

    public FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public Collection<Entry> get(RefactoringElementType type) {
        switch(type) {
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

    private void init() {
        SimpleNode root = parserResult.root();
        if(root != null) {
            SimpleNodeUtil.visitChildren(root, new AstVisitor());
        } //else completely broken source, no parser result
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(super.toString());
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

    private class AstVisitor implements NodeVisitor {

        @Override
        public void visit(SimpleNode node) {
            if (node.kind() == CssParserTreeConstants.JJTIMPORTRULE) {
                Entry entry = getImportedEntry(node);
                if (entry != null) {
                    getImportsCollectionInstance().add(entry);
                }
            } else if (node.kind() == CssParserTreeConstants.JJT_CLASS
                    || node.kind() == CssParserTreeConstants.JJTHASH
                    || node.kind() == CssParserTreeConstants.JJTELEMENTNAME) {

                Collection<Entry> collection;
                int start_offset_diff;
                //find the selector body range if possible
                SimpleNode styleRuleNode = SimpleNodeUtil.getAncestorByType(node, CssParserTreeConstants.JJTSTYLERULE);
                OffsetRange body = null;
                if(styleRuleNode != null) {
                    //find the opening left curly bracket {
                    Token first = styleRuleNode.jjtGetFirstToken();
                    Token last = styleRuleNode.jjtGetLastToken();
                    int from = -1;
                    do {
                        if(first.kind == CssParserConstants.LBRACE) {
                            from = first.offset + 1;
                            break;
                        }
                    } while((first = first.next) != last);

                    //get the closing right curly bracket }
                    int to = styleRuleNode.jjtGetLastToken().offset;

                    if(from != -1 && to != -1) {
                        body = new OffsetRange(from, to);
                    }
                }

                switch (node.kind()) {
                    case CssParserTreeConstants.JJT_CLASS:
                        collection = getClassesCollectionInstance();
                        start_offset_diff = 1; //cut off the dot (.)
                        break;
                    case CssParserTreeConstants.JJTHASH:
                        collection = getIdsCollectionInstance();
                        start_offset_diff = 1; //cut of the hash (#)
                        break;
                    case CssParserTreeConstants.JJTELEMENTNAME:
                        collection = getHtmlElementsCollectionInstance();
                        start_offset_diff = 0;
                        break;
                    default:
                        collection = null; //cannot happen
                        start_offset_diff = 0;
                }

                String image = node.image().substring(start_offset_diff);
                OffsetRange range = new OffsetRange(node.startOffset() + start_offset_diff, node.endOffset());

                //check if the real start offset can be translated to the original offset
                boolean isVirtual = getSnapshot().getOriginalOffset(node.startOffset()) == -1;

                Entry e = createEntry(image, range, body, isVirtual);
                if(e != null) {
                    collection.add(e);
                }

            } else if(node.kind() == CssParserTreeConstants.JJTHEXCOLOR) {
                String image = node.image();
                int[] wsLens = getTextWSPreAndPostLens(image);
                image = image.substring(wsLens[0], image.length() - wsLens[1]);
                OffsetRange range = new OffsetRange(node.startOffset() + wsLens[0], node.endOffset() - wsLens[1]);
                Entry e = createEntry(image, range, false);
                if(e != null) {
                    getColorsCollectionInstance().add(e);
                }
            }
        }

        private Entry getImportedEntry(SimpleNode node) {
            //@import "resources/global.css";
            Token token = SimpleNodeUtil.getNodeToken(node, CssParserConstants.STRING);
            if (token != null) {
                String image = token.image;
                boolean quoted = WebUtils.isValueQuoted(image);
                return createEntry(WebUtils.unquotedValue(image),
                        new OffsetRange(token.offset + (quoted ? 1 : 0),
                        token.offset + image.length() - (quoted ? 1 : 0)),
                        false);
            }

            //@import url("another.css");
            token = SimpleNodeUtil.getNodeToken(node, CssParserConstants.URI);
            if (token != null) {
                Matcher m = URI_PATTERN.matcher(token.image);
                if (m.matches()) {
                    int groupIndex = 1;
                    String content = m.group(groupIndex);
                    boolean quoted = WebUtils.isValueQuoted(content);
                    int from = token.offset + m.start(groupIndex) + (quoted ? 1 : 0);
                    int to = token.offset + m.end(groupIndex) - (quoted ? 1 : 0);
                    return createEntry(WebUtils.unquotedValue(content),
                            new OffsetRange(from, to),
                            false);
                }
            }

            return null;
        }
    }

    public Entry createEntry(String name, OffsetRange range, boolean isVirtual) {
        return createEntry(name, range, null, isVirtual);
    }

    public Entry createEntry(String name, OffsetRange range, OffsetRange bodyRange, boolean isVirtual) {
        int documentFrom = getSnapshot().getOriginalOffset(range.getStart());
        int documentTo = getSnapshot().getOriginalOffset(range.getEnd());

        OffsetRange documentRange = null;
        OffsetRange documentBodyRange = null;
        String elementLineText = null;
        String elementText = null;
        int lineOffset = -1;
        if (documentFrom == -1 || documentTo == -1) {
            if(LOG) {
                LOGGER.finer("Ast offset range " + range.toString() +
                        ", text='" + getSnapshot().getText().subSequence(range.getStart(), range.getEnd())+ "', "
                        + " cannot be properly mapped to source offset range: ["
                        + documentFrom + "," + documentTo + "] in file "
                        + getFileObject().getPath()); //NOI18N
            }
        } else {
            documentRange = new OffsetRange(documentFrom, documentTo);
            try {
                //extract element text
                elementText = getSnapshot().getText().subSequence(range.getStart(), range.getEnd()).toString();

                //extract element line text
                int astLineStart = GsfUtilities.getRowStart(getSnapshot().getText(), range.getStart());
                int astLineEnd = GsfUtilities.getRowEnd(getSnapshot().getText(), range.getStart());
                elementLineText = getSnapshot().getText().subSequence(astLineStart, astLineEnd).toString();

                //try to compute line number of document start offset, this "needs" to run on document.
                final Document doc = getSnapshot().getSource().getDocument(true);
                if(doc == null) {
                    //strange, this should not normally happen
                    lineOffset = -1; //cannot determine the value
                    
                } else {
                    final AtomicInteger ret = new AtomicInteger();
                    final int offset = documentFrom;
                    doc.render(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                ret.set(Utilities.getLineOffset((BaseDocument) doc, offset));
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }

                    });
                    lineOffset = ret.get();
                }

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            if(bodyRange != null) {
                int bodyDocFrom = getSnapshot().getOriginalOffset(bodyRange.getStart());
                int bodyDocTo = getSnapshot().getOriginalOffset(bodyRange.getEnd());
                if(bodyDocFrom != -1 && bodyDocTo != -1) {
                    documentBodyRange = new OffsetRange(bodyDocFrom, bodyDocTo);
                }
            }
        }

        return new Entry(name, range, documentRange, bodyRange, documentBodyRange,
                lineOffset, elementText, elementLineText, isVirtual);
    }

    private static int[] getTextWSPreAndPostLens(String text) {
        int preWSlen = 0;
        int postWSlen = 0;

        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(Character.isWhitespace(c)) {
                preWSlen++;
            } else {
                break;
            }
        }

        for(int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if(Character.isWhitespace(c)) {
                postWSlen++;
            } else {
                break;
            }
        }

        return new int[]{preWSlen, postWSlen};
    }
}
