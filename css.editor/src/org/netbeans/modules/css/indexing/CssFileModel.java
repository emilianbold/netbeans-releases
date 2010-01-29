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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.CssParserConstants;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.css.parser.Token;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class CssFileModel {

    private static final Pattern URI_PATTERN = Pattern.compile("url\\(\\s*(.*)\\s*\\)");
    
    private Collection<Entry> classes, ids, htmlElements, imports;
    private boolean isParserBased;
    private FileObject fileObject;
    private Snapshot snapshot;

    public CssFileModel(Source source) throws ParseException {
        this.fileObject = source.getFileObject();
        isParserBased = true;
        ParserManager.parse(Collections.singletonList(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator cssRi = Css.getResultIterator(resultIterator, Css.CSS_MIME_TYPE);
                if(cssRi != null) {
                    snapshot = cssRi.getSnapshot();
                    init((CssParserResult)cssRi.getParserResult());
                }
            }
        });
    }

    public CssFileModel(CssParserResult parserResult) {
        fileObject = parserResult.getSnapshot().getSource().getFileObject();
        snapshot = parserResult.getSnapshot();
        isParserBased = true;
        init(parserResult);
    }

    //usually index based model
    CssFileModel(FileObject fileObject, Collection<String> classes, Collection<String> ids, Collection<String> htmlElements, Collection<String> imports ) {
        this.fileObject = fileObject;
        this.classes = createBlindEntries(classes);
        this.ids = createBlindEntries(ids);
        this.htmlElements = createBlindEntries(htmlElements);
        this.imports = createBlindEntries(imports);
        isParserBased = false;
    }

    public boolean isParserBased() {
        return isParserBased;
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

    /**
     *
     * @return true if the model is empty - nothing interesting found in the page.
     */
    public boolean isEmpty() {
        return null == classes && null == ids && null == htmlElements && null == imports;
    }

    //single threaded - called from constructor only, no need for synch
    private Collection<Entry> getClassesCollectionInstance() {
        if(classes == null) {
            classes = new ArrayList<Entry>();
        }
        return classes;
    }
    
    private Collection<Entry> getIdsCollectionInstance() {
        if(ids == null) {
            ids = new ArrayList<Entry>();
        }
        return ids;
    }

    private Collection<Entry> getHtmlElementsCollectionInstance() {
        if(htmlElements == null) {
            htmlElements = new ArrayList<Entry>();
        }
        return htmlElements;
    }

    private Collection<Entry> getImportsCollectionInstance() {
        if(imports == null) {
            imports = new ArrayList<Entry>();
        }
        return imports;
    }

    private void init(CssParserResult parserResult) {
        SimpleNodeUtil.visitChildren(parserResult.root(), new AstVisitor());
    }

    private Collection<Entry> createBlindEntries(Collection<String> elements) {
        Collection<Entry> entries = new ArrayList<Entry>(elements.size());
        for(String elementName : elements) {
            entries.add(new Entry(elementName, OffsetRange.NONE));
        }
        return entries;
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
            buf.append(c);
            buf.append(',');
        }
        for (Entry c : getIds()) {
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
                if(entry != null) {
                    getImportsCollectionInstance().add(entry);
                }
            } else if (node.kind() == CssParserTreeConstants.JJT_CLASS) {
                getClassesCollectionInstance().add(new Entry(node.image(), new OffsetRange(node.startOffset(), node.endOffset())));
            } else if (node.kind() == CssParserTreeConstants.JJTHASH) {
                getIdsCollectionInstance().add(new Entry(node.image(), new OffsetRange(node.startOffset(), node.endOffset())));
            } else if (node.kind() == CssParserTreeConstants.JJTELEMENTNAME) {
                getHtmlElementsCollectionInstance().add(new Entry(node.image(), new OffsetRange(node.startOffset(), node.endOffset())));
            }

        }

        private Entry getImportedEntry(SimpleNode node) {
            //@import "resources/global.css";
            Token token = SimpleNodeUtil.getNodeToken(node, CssParserConstants.STRING);
            if(token != null) {
                String image = token.image;
                boolean quoted = SimpleNodeUtil.isValueQuoted(image);
                return new Entry(SimpleNodeUtil.unquotedValue(image),
                        new OffsetRange(token.offset + (quoted ? 1 : 0),
                        token.offset + image.length() - (quoted ? 1 : 0)));
            }

            //@import url("another.css");
            token = SimpleNodeUtil.getNodeToken(node, CssParserConstants.URI);
            if(token != null) {
                Matcher m = URI_PATTERN.matcher(token.image);
                if(m.matches()) {
                    int groupIndex = 1;
                    String content = m.group(groupIndex);
                    boolean quoted = SimpleNodeUtil.isValueQuoted(content);
                    int from = m.start(groupIndex);
                    int to = m.end(groupIndex);
                    return new Entry(SimpleNodeUtil.unquotedValue(content),
                        new OffsetRange(from + (quoted ? 1 : 0), to - (quoted ? 1 : 0)));
                }
            }
            
            return null;
        }
    }

    public class Entry {
        private String name;
        private OffsetRange astRange;
        private OffsetRange documentRange;

        private Entry(String name, OffsetRange range) {
            this.name = name;
            this.astRange = range;
            this.documentRange = new OffsetRange(snapshot.getOriginalOffset(range.getStart()),
                    snapshot.getOriginalOffset(range.getEnd()));
        }

        public String getName() {
            return name;
        }

        public OffsetRange getDocumentRange() {
            return documentRange;
        }

        public OffsetRange getRange() {
            return astRange;
        }

        @Override
        public String toString() {
            return "Entry[" + getName() + "; " + getRange().getStart() + " - " + getRange().getEnd() + "]";
        }


    }
    
}
