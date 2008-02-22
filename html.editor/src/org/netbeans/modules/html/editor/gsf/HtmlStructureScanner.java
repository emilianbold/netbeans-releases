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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
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
import org.netbeans.modules.editor.html.HTMLKit;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 */
public class HtmlStructureScanner implements StructureScanner {

    private static final Logger LOGGER = Logger.getLogger(HtmlStructureScanner.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private HtmlFormatter formatter;

    public List<? extends StructureItem> scan(final CompilationInfo info, HtmlFormatter formatter) {

        ParserResult presult = info.getEmbeddedResults(HTMLKit.HTML_MIME_TYPE).iterator().next();
        final TranslatedSource source = presult.getTranslatedSource();
        AstNode root = ((HtmlParserResult) presult).root();


        if (LOG) {
            LOGGER.log(Level.FINE, "HTML parser tree output:");
            LOGGER.log(Level.FINE, root.toString());
        }

        //return the root children
        List<StructureItem> elements = new  ArrayList<StructureItem>(1);
        elements.addAll(new CSSStructureItem(root, source).getNestedItems());
        
        return elements;
        
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        try {
            //so far the css parser always parses the whole css content
            ParserResult presult = info.getEmbeddedResults("text/html").iterator().next();
            final TranslatedSource source = presult.getTranslatedSource();
            final BaseDocument doc = (BaseDocument) info.getDocument();
            AstNode root = ((HtmlParserResult) presult).root();

            final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
            final List<OffsetRange> foldRange = new ArrayList<OffsetRange>();

            AstNodeVisitor foldsSearch = new AstNodeVisitor() {

                public void visit(AstNode node) {
                    if (node.type() == AstNode.NodeType.TAG) {
                        try {
                            int so = documentPosition(node.startOffset(), source);
                            int eo = documentPosition(node.endOffset(), source);
                            
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
            AstNodeUtils.visitChildren(root, foldsSearch);
            folds.put("codeblocks", foldRange);

            return folds;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;

    }

    public static int documentPosition(int astOffset, TranslatedSource source) {
        return source == null ? astOffset : source.getLexicalOffset(astOffset);
    }
    
    private static final class CSSStructureItem implements StructureItem {

        private TranslatedSource source;
        private AstNode node;

        private CSSStructureItem(AstNode node, TranslatedSource source) {
            this.node = node;
            this.source= source;
            //find selectors and generate item name
//            final StringBuffer selectors = new StringBuffer();
//            NodeVisitor selectorSearch = new NodeVisitor() {
//
//                public void visit(SimpleNode node2) {
//                    if (node2.kind() == CSSParserTreeConstants.JJTSELECTOR) {
//                        String selectorName = node2.jjtGetFirstToken().image;
//                        selectors.append(selectorName);
//                        selectors.append(',');
//                        selectors.append(' ');
//                    }
//                }
//            };
//            selectorsList.visitChildren(selectorSearch);
//            name = selectors.toString();
//            //cut off the last ", "
//            if(name.length() > 0) {
//                name = name.substring(0, name.length() - 2);
//            }

//            name = node.image();

        }

        public String getName() {
            return node.name();
        }

        public String getHtml() {
            return getName();
        }

        public ElementHandle getElementHandle() {
            return null;
        }

        public ElementKind getKind() {
            return ElementKind.TAG;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return node.children().isEmpty();
        }

        public List<? extends StructureItem> getNestedItems() {
            List<StructureItem> list = new  ArrayList<StructureItem>(node.children().size());
            for(AstNode child : node.children()) {
                if(child.type() == AstNode.NodeType.TAG 
                        || child.type() == AstNode.NodeType.UNMATCHED_TAG) {
                    list.add(new CSSStructureItem(child, source));
                }
            }
            return list;
        }

        public long getPosition() {
            return HtmlStructureScanner.documentPosition(node.startOffset(), source);
        }

        public long getEndPosition() {
            return HtmlStructureScanner.documentPosition(node.endOffset(), source);
        }

    }
}
