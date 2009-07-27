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

import java.util.Collection;
import org.netbeans.modules.html.editor.gsf.api.HtmlParserResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.AstPath;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HtmlStructureScanner implements StructureScanner {

    private static final Logger LOGGER = Logger.getLogger(HtmlStructureScanner.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    @Override
    public List<? extends StructureItem> scan(final ParserResult info) {
        HtmlParserResult presult = (HtmlParserResult)info;
        AstNode root = ((HtmlParserResult) presult).root();

        if (LOG) {
            LOGGER.log(Level.FINE, "HTML parser tree output:");
            LOGGER.log(Level.FINE, root.toString());
        }

        //return the root children
        List<StructureItem> elements = new  ArrayList<StructureItem>(1);
        elements.addAll(new HtmlStructureItem(new HtmlElementHandle(root, info.getSnapshot().getSource().getFileObject()), info.getSnapshot()).getNestedItems());
        
        return elements;
        
    }

    @Override
    public Map<String, List<OffsetRange>> folds(final ParserResult info) {
        final BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return Collections.emptyMap();
        }

        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        final List<OffsetRange> tags = new ArrayList<OffsetRange>();
        final List<OffsetRange> comments = new ArrayList<OffsetRange>();

        AstNodeVisitor foldsSearch = new AstNodeVisitor() {

            public void visit(AstNode node) {
                if (node.type() == AstNode.NodeType.OPEN_TAG
                        || node.type() == AstNode.NodeType.COMMENT) {
                    try {
                        int[] logicalRange = node.getLogicalRange();
                        int from = logicalRange[0];
                        int to = logicalRange[1];
                        
                        int so = documentPosition(from, info.getSnapshot());
                        int eo = documentPosition(to, info.getSnapshot());
                        
                        if (eo > doc.getLength()) {
                            eo = doc.getLength();
                            if (so > eo) {
                                so = eo;
                            }
                        }

                        if (Utilities.getLineOffset(doc, so) < Utilities.getLineOffset(doc, eo)) {
                            //do not creare one line folds
                            //XXX this logic could possibly seat in the GSF folding impl.
                            if(node.type() == AstNode.NodeType.TAG) {
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
            Collection<AstNode> roots = ((HtmlParserResult) info).roots().values();
            for (AstNode root : roots) {
                AstNodeUtils.visitChildren(root, foldsSearch);
            }
        } finally {
            doc.readUnlock();
        }
        folds.put("tags", tags);
        folds.put("comments", comments);

        return folds;
    }

    public static int documentPosition(int astOffset, Snapshot snapshot) {
        return snapshot.getOriginalOffset(astOffset);
    }
    
    @Override
    public Configuration getConfiguration() {
        return new Configuration(false, false, 0);
    }

    private static final class HtmlStructureItem implements StructureItem {

        private Snapshot snapshot;
        private HtmlElementHandle handle;
        private int myIndexInParent = -1;
        private List<StructureItem> items = null;
        
        private HtmlStructureItem(HtmlElementHandle handle, Snapshot snapshot) {
            this.handle = handle;
            this.snapshot = snapshot;
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
            return Integer.toHexString(10000+(int)getPosition());
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.appendHtml(getName());

            AstNode node = handle.node();
            String idAttr = getAttributeValue(node, "id"); //NOI18N
            String classAttr = getAttributeValue(node, "class"); //NOI18N

            if(idAttr != null) {
                formatter.appendHtml("&nbsp;<font color=808080>id=" + idAttr + "</font>"); //NOI18N
            }
            if(classAttr != null) {
                formatter.appendHtml("&nbsp;<font color=808080>class=" + classAttr + "</font>"); //NOI18N
            }
            
            return formatter.getText();
        }

        private String getAttributeValue(AstNode node, String key) {
            String value = (String)node.getAttribute(key.toLowerCase(Locale.ENGLISH)); //try lowercase
            if(value == null) {
                value = (String)node.getAttribute(key.toUpperCase(Locale.ENGLISH)); //try uppercase
            }
            return value;
        }

        @Override
        public ElementHandle getElementHandle() {
            return handle;
        }

        synchronized int indexInParent() {
            if(myIndexInParent == -1) {
                AstNode papa = handle.node().parent();
                myIndexInParent = papa == null ? -2 : AstPath.indexInSimilarNodes(papa, handle.node());
            }
            return myIndexInParent;
        }
        
        @Override
        public boolean equals(Object o) {
            if(!(o instanceof HtmlStructureItem)) {
                return false;
            }
            HtmlStructureItem item = (HtmlStructureItem)o;
            
            AstNode he = ((HtmlStructureItem)o).handle.node();
            AstNode me = handle.node();
            if(he.type() == me.type() && he.name().equals(me.name())) {
                return indexInParent() == item.indexInParent();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return handle.node().name().hashCode() + indexInParent();
        
        }
      
        @Override
        public ElementKind getKind() {
            return ElementKind.TAG;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            //potentialy incorrect workaround for ElementNode.updateRecursively(StructureItem) method.
            //If the StructureItem says it is a leaf then if a new node is created inside
            //the navigator representation - ElementNode still holds empty children list 
            //which is not an instance of ElementChildren and then the subnodes are not refreshed.
            //possible fix would be to modify the ElementNode constructor to always create 
            //ElementChildren even if the node is a leaf, but I am not sure whether it may 
            //have some bad influence on other things.
            return false;
            
            //return handle.node().children().isEmpty();
        }

        @Override
        public synchronized List<? extends StructureItem> getNestedItems() {
            if(items == null) {
                items = new  ArrayList<StructureItem>(handle.node().children().size());
                for(AstNode child : handle.node().children()) {
                    if(child.type() == AstNode.NodeType.OPEN_TAG) {
                        HtmlElementHandle childHandle = new HtmlElementHandle(child, handle.getFileObject());
                        items.add(new HtmlStructureItem(childHandle, snapshot));
                    }
                }
            }
            return items;
        }

        public long getPosition() {
            return HtmlStructureScanner.documentPosition(handle.from(), snapshot);
        }

        public long getEndPosition() {
            return HtmlStructureScanner.documentPosition(handle.to(), snapshot);
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }

    }
}
