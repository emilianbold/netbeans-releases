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
package org.netbeans.modules.css.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.parser.CSSParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 */
public class CSSStructureScanner implements StructureScanner {

    public List<? extends StructureItem> scan(final CompilationInfo info) {
         //so far the css parser always parses the whole css content
        Iterator<? extends ParserResult> presultIterator = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator();
        if(!presultIterator.hasNext()) {
            return Collections.emptyList();
        }
        
        ParserResult presult = presultIterator.next();
        final TranslatedSource source = presult.getTranslatedSource();
        SimpleNode root = ((CSSParserResult) presult).root();

        if (root == null) {
            //serious error in the source, no results
            return Collections.emptyList();
        }

        final List<StructureItem> items = new ArrayList<StructureItem>();

        NodeVisitor rulesSearch = new NodeVisitor() {

            public void visit(SimpleNode node) {
                if (node.kind() == CSSParserTreeConstants.JJTSELECTORLIST) {
                    //get parent - style rule
                    SimpleNode ruleNode = (SimpleNode) node.jjtGetParent();
                    assert ruleNode.kind() == CSSParserTreeConstants.JJTSTYLERULE;
                    int so = AstUtils.documentPosition(ruleNode.startOffset(), source);
                    int eo = AstUtils.documentPosition(ruleNode.endOffset(), source);
                    if (eo != so) {

                        StringBuffer selectorsListText = new StringBuffer();
                        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                            SimpleNode n = (SimpleNode) node.jjtGetChild(i);
                            if (n.kind() == CSSParserTreeConstants.JJTSELECTOR) {
                                StringBuffer content = new StringBuffer();
                                //found selector
                                for (int j = 0; j < n.jjtGetNumChildren(); j++) {
                                    SimpleNode n2 = (SimpleNode) n.jjtGetChild(j);
                                    //append simpleselectors and combinators
                                    if (n2.kind() == CSSParserTreeConstants.JJTSIMPLESELECTOR ||
                                            n2.kind() == CSSParserTreeConstants.JJTCOMBINATOR) {
                                        if(n2.image().trim().length() > 0) {
                                            String nodeText = extractDocumentText(n2, info, source).trim();
                                            content.append(nodeText);
                                            content.append(' ');
                                        }
                                    }
                                }
                                //filter out inlined style definitions - they have just virtual selector which
                                //is mapped to empty string

                                if (content.length() > 0) {
                                    selectorsListText.append(content.substring(0, content.length() - 1)); //cut last space
                                    selectorsListText.append(", "); //append selectos separator //NOI18N
                                }
                            }
                        }
                        //filter empty(virtual) selector lists
                        if (selectorsListText.length() > 2 /* ", ".length() */) {
                            
                            //possibly remove last space and comma
                            if (selectorsListText.charAt(selectorsListText.length() - 2) == ',') {
                                selectorsListText.deleteCharAt(selectorsListText.length() - 2);
                            }

                            items.add(new CssRuleStructureItem(selectorsListText.toString(), CssAstElement.createElement(ruleNode), source));
                        }

                    }
                }
            }
        };
        root.visitChildren(rulesSearch);
        return items;
    }

    private String extractDocumentText(SimpleNode node, CompilationInfo ci, TranslatedSource source) {
        int documentSO = AstUtils.documentPosition(node.startOffset(), source);
        int documentEO = AstUtils.documentPosition(node.endOffset(), source);
        return ci.getText().substring(documentSO, documentEO);
    }
    
    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        final BaseDocument doc = (BaseDocument) info.getDocument();
        if (doc == null) {
            return Collections.emptyMap();
        }

        //so far the css parser always parses the whole css content
        Iterator<? extends ParserResult> presultIterator = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator();
        if (!presultIterator.hasNext()) {
            return Collections.emptyMap();
        }

        ParserResult presult = presultIterator.next();
        final TranslatedSource source = presult.getTranslatedSource();
        SimpleNode root = ((CSSParserResult) presult).root();

        if (root == null) {
            //serious error in the source, no results
            return Collections.emptyMap();
        }

        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
        final List<OffsetRange> foldRange = new ArrayList<OffsetRange>();

        NodeVisitor foldsSearch = new NodeVisitor() {

            public void visit(SimpleNode node) {
                if (node.kind() == CSSParserTreeConstants.JJTSTYLERULE) {
                    int so = AstUtils.documentPosition(node.startOffset(), source);
                    int eo = AstUtils.documentPosition(node.endOffset(), source);
                    try {
                        if (Utilities.getLineOffset(doc, so) < Utilities.getLineOffset(doc, eo)) {
                            //do not creare one line folds
                            //XXX this logic could possibly seat in the GSF folding impl.
                            foldRange.add(new OffsetRange(so, eo));
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        };
        root.visitChildren(foldsSearch);
        folds.put("codeblocks", foldRange); //NOI18N

        return folds;
        
    }
    
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }
    
    private static class CssRuleStructureItem implements StructureItem {

        private String name;
        private CssAstElement element;
        private int from, to;

        private static String escape(String s) {
            s = s.replace("<", "&lt;");
            s = s.replace(">", "&gt;");
            return s;
        }
        
        private CssRuleStructureItem(String name, CssAstElement element, TranslatedSource source) {
            this.name = name;
            this.element = element;
            this.from = AstUtils.documentPosition(element.node().startOffset(), source);
            this.to = AstUtils.documentPosition(element.node().endOffset(), source);
        }

        public String getName() {
            return name;
        }

        public String getSortText() {
            return getName();
        }

        public String getHtml(HtmlFormatter formatter) {
            return escape(getName());
        }

        public ElementHandle getElementHandle() {
            return element;
        }

        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return true;
        }

        //TODO - could I put rules here???
        public List<? extends StructureItem> getNestedItems() {
            return Collections.emptyList();
        }

        public long getPosition() {
            return from;
        }

        public long getEndPosition() {
            return to;
        }

        public ImageIcon getCustomIcon() {
            return null;
        }
    }
}
