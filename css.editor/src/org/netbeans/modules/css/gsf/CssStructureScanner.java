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
package org.netbeans.modules.css.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marek
 */
public class CssStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(final ParserResult info) {
        Node root = ((CssParserResultCslWrapper) info).getParseTree();
        final Snapshot snapshot = info.getSnapshot();

        if (root == null) {
            //serious error in the source, no results
            return Collections.emptyList();
        }

        final List<StructureItem> items = new ArrayList<StructureItem>();
        final CharSequence topLevelSnapshotText = snapshot.getSource().createSnapshot().getText();
        
        NodeVisitor rulesSearch = new NodeVisitor() {

            @Override
            public boolean visit(Node node) {
//                if (node.type() == NodeType.selectorsGroup) {
//                    //get parent - style rule
//                    Node ruleNode = (Node) node.jjtGetParent();
//                    assert ruleNode.kind() == CssParserTreeConstants.JJTSTYLERULE;
//                    int so = snapshot.getOriginalOffset(ruleNode.from());
//                    int eo = snapshot.getOriginalOffset(ruleNode.to());
//                    if (eo != so) {
//
//                        StringBuilder selectorsListText = new StringBuilder();
//                        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//                            Node n = (Node) node.jjtGetChild(i);
//                            if (n.kind() == CssParserTreeConstants.JJTSELECTOR) {
//                                StringBuilder content = new StringBuilder();
//                                //found selector
//                                for (int j = 0; j < n.jjtGetNumChildren(); j++) {
//                                    Node n2 = (Node) n.jjtGetChild(j);
//                                    //append simpleselectors and combinators
//                                    if (n2.kind() == CssParserTreeConstants.JJTSIMPLESELECTOR ||
//                                            n2.kind() == CssParserTreeConstants.JJTCOMBINATOR) {
//                                        if (n2.image().trim().length() > 0) {
//                                                CharSequence nodeText = topLevelSnapshotText.subSequence(
//                                                        snapshot.getOriginalOffset(n2.from()),
//                                                        snapshot.getOriginalOffset(n2.to()));
//                                                content.append(nodeText);
//                                                content.append(' ');
//
//                                        }
//                                    }
//                                }
//                                //filter out inlined style definitions - they have just virtual selector which
//                                //is mapped to empty string
//
//                                if (content.length() > 0) {
//                                    selectorsListText.append(content.substring(0, content.length() - 1)); //cut last space
//                                    selectorsListText.append(", "); //append selectos separator //NOI18N
//                                }
//                            }
//                        }
//                        //filter empty(virtual) selector lists
//                        if (selectorsListText.length() > 2 /* ", ".length() */) {
//
//                            //possibly remove last space and comma
//                            if (selectorsListText.charAt(selectorsListText.length() - 2) == ',') {
//                                selectorsListText.deleteCharAt(selectorsListText.length() - 2);
//                            }
//
//                            items.add(new CssRuleStructureItem(selectorsListText.toString(), CssAstElement.createElement(ruleNode), snapshot));
//                        }
//
//                    }
//                }
                
                return false;
            }
        };
        
        rulesSearch.visit(root);
        return items;
    }

//    private String extractDocumentText(Node node, CompilationInfo ci, TranslatedSource source) {
//        int documentSO = AstUtils.documentPosition(node.from(), source);
//        int documentEO = AstUtils.documentPosition(node.to(), source);
//        return ci.getText().substring(documentSO, documentEO);
//    }
    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        final BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return Collections.emptyMap();
        }

//        //so far the css parser always parses the whole css content
//        Iterator<? extends ParserResult> presultIterator = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator();
//        if (!presultIterator.hasNext()) {
//            return Collections.emptyMap();
//        }
//
//        ParserResult presult = presultIterator.next();
//        final TranslatedSource source = presult.getTranslatedSource();
        Node root = ((CssParserResultCslWrapper) info).getParseTree();
        final Snapshot snapshot = info.getSnapshot();

        if (root == null) {
            //serious error in the source, no results
            return Collections.emptyMap();
        }

        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        final List<OffsetRange> foldRange = new ArrayList<OffsetRange>();

//        NodeVisitor foldsSearch = new NodeVisitor() {
//
//            @Override
//            public void visit(Node node) {
//                if (node.kind() == CssParserTreeConstants.JJTSTYLERULE) {
//                    int so = snapshot.getOriginalOffset(node.from());
//                    int eo = snapshot.getOriginalOffset(node.to());
//                    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//                        Node n = (Node) node.jjtGetChild(i);
//                        if (n.kind() == CssParserTreeConstants.JJTSELECTORLIST) {
//                            // shift fold start to the end of rule name:
//                            so = snapshot.getOriginalOffset(n.to());
//                            break;
//                        }
//                    }
//                    try {
//                        //document is not locked so need to check current boundaries
//                        if (so >= 0 && eo >= 0 && eo < doc.getLength() && Utilities.getLineOffset(doc, so) < Utilities.getLineOffset(doc, eo)) {
//                            //do not creare one line folds
//                            //XXX this logic could possibly seat in the GSF folding impl.
//                            foldRange.add(new OffsetRange(so, eo));
//                        }
//                    } catch (BadLocationException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                }
//            }
//        };
//        root.visitChildren(foldsSearch);
//        folds.put("codeblocks", foldRange); //NOI18N

        return folds;

    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }

//    private static class CssRuleStructureItem implements StructureItem {
//
//        private String name;
//        private CssAstElement element;
//        private int from,  to;
//
//        private static String escape(String s) {
//            s = s.replace("<", "&lt;");
//            s = s.replace(">", "&gt;");
//            return s;
//        }
//
//        private CssRuleStructureItem(String name, CssAstElement element, Snapshot source) {
//            this.name = name;
//            this.element = element;
//            this.from = source.getOriginalOffset(element.node().from());
//            this.to = source.getOriginalOffset(element.node().to());
//        }
//
//        @Override
//        public String getName() {
//            return name;
//        }
//
//        @Override
//        public String getSortText() {
//            return getName();
//        }
//
//        @Override
//        public String getHtml(HtmlFormatter formatter) {
//            return escape(getName());
//        }
//
//        @Override
//        public ElementHandle getElementHandle() {
//            return element;
//        }
//
//        @Override
//        public ElementKind getKind() {
//            return ElementKind.RULE;
//        }
//
//        @Override
//        public Set<Modifier> getModifiers() {
//            return Collections.emptySet();
//        }
//
//        @Override
//        public boolean isLeaf() {
//            return true;
//        }
//
//        //TODO - could I put rules here???
//        @Override
//        public List<? extends StructureItem> getNestedItems() {
//            return Collections.emptyList();
//        }
//
//        @Override
//        public long getPosition() {
//            return from;
//        }
//
//        @Override
//        public long getEndPosition() {
//            return to;
//        }
//
//        @Override
//        public ImageIcon getCustomIcon() {
//            return null;
//        }
//    }
}
