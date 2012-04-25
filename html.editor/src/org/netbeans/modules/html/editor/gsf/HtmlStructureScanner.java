/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.gsf;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.*;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.Pair;
import org.openide.util.Exceptions;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HtmlStructureScanner implements StructureScanner {

    private static final Logger LOGGER = Logger.getLogger(HtmlStructureScanner.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private static final long MAX_SNAPSHOT_SIZE = 4 * 1024 * 1024;
    private Reference<Pair<ParserResult, List<HtmlStructureItem>>> cache;

    private boolean isOfSupportedSize(ParserResult info) {
        Snapshot snapshot = info.getSnapshot();
        int slen = snapshot.getText().length();
        return slen < MAX_SNAPSHOT_SIZE;
    }

    @Override
    public List<? extends StructureItem> scan(final ParserResult info) {
        //temporary workaround for 
        //Bug 211139 - HtmlStructureScanner.scan() called twice with the same ParserResult 
        //so it is easier to debug
        if (cache != null) {
            Pair<ParserResult, List<HtmlStructureItem>> pair = cache.get();
            if (pair != null) {
                if (info == pair.getA()) {
                    return pair.getB();
                }
            }
        }

        if (!isOfSupportedSize(info)) {
            return Collections.emptyList();
        }

        HtmlParserResult presult = (HtmlParserResult) info;
        Node root = ((HtmlParserResult) presult).root();

        if (LOG) {
            LOGGER.log(Level.FINE, "HTML parser tree output:");
            LOGGER.log(Level.FINE, root.toString());
        }


        //return the root children
        HtmlElementHandle rootHandle = new HtmlElementHandle(root, info.getSnapshot().getSource().getFileObject());
        HtmlStructureItem rootSI = new HtmlStructureItem(root, rootHandle, info.getSnapshot());
        List<StructureItem> elements = new ArrayList<StructureItem>(rootSI.getNestedItems());

        //cache
        Pair<ParserResult, List<HtmlStructureItem>> pair = new Pair(info, elements);
        cache = new WeakReference<Pair<ParserResult, List<HtmlStructureItem>>>(pair);

        return elements;

    }

    @Override
    public Map<String, List<OffsetRange>> folds(final ParserResult info) {
        if (!isOfSupportedSize(info)) {
            return Collections.emptyMap();
        }

        final BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return Collections.emptyMap();
        }

        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        final List<OffsetRange> tags = new ArrayList<OffsetRange>();
        final List<OffsetRange> comments = new ArrayList<OffsetRange>();

        ElementVisitor foldsSearch = new ElementVisitor() {
            @Override
            public void visit(Element node) {
                if (node.type() == ElementType.OPEN_TAG
                        || node.type() == ElementType.COMMENT) {
                    try {

                        int from = node.from();
                        int to = node.type() == ElementType.OPEN_TAG
                                ? ((OpenTag) node).semanticEnd()
                                : node.to();

                        int so = documentPosition(from, info.getSnapshot());
                        int eo = documentPosition(to, info.getSnapshot());

                        if (so == -1 || eo == -1) {
                            //cannot be mapped back properly
                            return;
                        }

                        if (eo > doc.getLength()) {
                            eo = doc.getLength();
                            if (so > eo) {
                                so = eo;
                            }
                        }

                        if (Utilities.getLineOffset(doc, so) < Utilities.getLineOffset(doc, eo)) {
                            //do not creare one line folds
                            //XXX this logic could possibly seat in the GSF folding impl.
                            if (node.type() == ElementType.OPEN_TAG) {
                                tags.add(new OffsetRange(so, eo));
                            } else {
                                comments.add(new OffsetRange(so, eo));
                            }
                        }
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
                }
            }
        };

        //the document is touched during the ast tree visiting, we need to lock it
        doc.readLock();
        try {
            Collection<Node> roots = ((HtmlParserResult) info).roots().values();
            for (Node root : roots) {
                ElementUtils.visitChildren(root, foldsSearch);
            }
        } finally {
            doc.readUnlock();
        }
        folds.put("tags", tags);
        folds.put("comments", comments);

        return folds;
    }

    private static int documentPosition(int astOffset, Snapshot snapshot) {
        return snapshot.getOriginalOffset(astOffset);
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(false, false, 0);
    }

    private static final class HtmlStructureItem implements StructureItem {

        private HtmlElementHandle handle;
        private OffsetRange documentOffsetRange;
        private String idAttributeValue, classAttributeValue;
        private List<StructureItem> items;
        //remember from what mime path the snapshot came from
        private MimePath mimePath;

        private HtmlStructureItem(Node node, HtmlElementHandle handle, Snapshot snapshot) {
            this.handle = handle;

            int dfrom = snapshot.getOriginalOffset(node.from());
            int dto = snapshot.getOriginalOffset(node.to());

            this.documentOffsetRange = dfrom != -1 && dto != -1
                    ? new OffsetRange(dfrom, dto)
                    : OffsetRange.NONE;

            this.idAttributeValue = getAttributeValue(node, "id"); //NOI18N
            this.classAttributeValue = getAttributeValue(node, "class"); //NOI18N

            this.mimePath = snapshot.getMimePath();

        }

        @Override
        public ElementHandle getElementHandle() {
            return handle;
        }

        @Override
        public String getName() {
            return handle.getName();
        }

        @Override
        public String getSortText() {
            //return getName();
            // Use position-based sorting text instead; alphabetical sorting in the
            // outline (the default) doesn't really make sense for HTML tag names
            return Integer.toHexString(10000 + (int) getPosition());
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.appendHtml(getName());

            if (idAttributeValue != null) {
                formatter.appendHtml("&nbsp;<font color=808080>id=");
                formatter.appendText(idAttributeValue);
                formatter.appendHtml("</font>"); //NOI18N
            }
            if (classAttributeValue != null) {
                formatter.appendHtml("&nbsp;<font color=808080>class=");
                formatter.appendText(classAttributeValue);
                formatter.appendHtml("</font>"); //NOI18N
            }

            return formatter.getText();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof HtmlStructureItem)) {
                return false;
            }
            HtmlStructureItem item = (HtmlStructureItem) o;

            return item.getElementHandle().equals(getElementHandle());
        }

        @Override
        public int hashCode() {
            return getElementHandle().hashCode();

        }

        @Override
        public ElementKind getKind() {
            return ElementKind.TAG;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean isLeaf() {
            //The child if empty if it hasn't any nested items. If it has only text it's empty.
            return getNestedItems().isEmpty();
        }

        @Override
        public synchronized List<? extends StructureItem> getNestedItems() {
            if (items == null) {
                try {
                    //lazy load the nested items
                    //we need a parser result to be able to find Element for the ElementHandle
                    Source source = Source.create(getElementHandle().getFileObject());
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            HtmlParserResult result;
                            if (mimePath.size() == 1) {
                                result = (HtmlParserResult) resultIterator.getParserResult();
                            } else {
                                for (int i = 0; i < mimePath.size(); i++) {
                                    String mimeType = mimePath.getMimeType(i);
                                    Iterator<Embedding> embeddings = resultIterator.getEmbeddings().iterator();
                                    while (embeddings.hasNext()) {
                                        Embedding embedding = embeddings.next();
                                        if (embedding.getMimeType().equals(mimeType)) {
                                            resultIterator = resultIterator.getResultIterator(embedding);
                                        }

                                    }
                                }
                                result = (HtmlParserResult) resultIterator.getParserResult();
                            }

                            Node node = handle.resolve(result);
                            if (node != null) {
                                items = new ArrayList<StructureItem>();
                                List<Node> nonVirtualChildren = gatherNonVirtualChildren(node);
                                for (Node child : nonVirtualChildren) {
                                    HtmlElementHandle childHandle = new HtmlElementHandle(child, handle.getFileObject());
                                    items.add(new HtmlStructureItem(child, childHandle, result.getSnapshot()));
                                }
                            }
                        }
                    });


                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return items;
        }

        @Override
        public long getPosition() {
            return documentOffsetRange.getStart();
        }

        @Override
        public long getEndPosition() {
            return documentOffsetRange.getEnd();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }

        //private
        private String getAttributeValue(Element node, String key) {
            String value = _getAttributeValue(node, key.toUpperCase(Locale.ENGLISH));
            if (value == null) {
                return _getAttributeValue(node, key.toLowerCase(Locale.ENGLISH));
            } else {
                return value;
            }
        }

        private String _getAttributeValue(Element node, String key) {
            if (node.type() != ElementType.OPEN_TAG) {
                return null;
            }
            OpenTag t = (OpenTag) node;
            Attribute attr = t.getAttribute(key); //try lowercase
            if (attr == null) {
                return null;
            }
            return attr.unquotedValue().toString();
        }
        
        private List<Node> gatherNonVirtualChildren(Node element) {
            List<Node> items = new LinkedList<Node>();
            for (Node child : element.children(Node.class)) {
                if (child.type() == ElementType.OPEN_TAG) {
                    if (!ElementUtils.isVirtualNode(child)) {
                        items.add(child);
                    } else {
                        items.addAll(gatherNonVirtualChildren(child));
                    }
                }
            }
            return items;
        }
        
    }

}
