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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Element;
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

    private HtmlFormatter formatter;

    public List<? extends StructureItem> scan(final CompilationInfo info, HtmlFormatter formatter) {
        //hack
        EditorAwareSourceTaskSupport.instance().fire(info);
        //eof hack
        
        //so far the css parser always parses the whole css content
        ParserResult presult = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator().next();
        final TranslatedSource source = presult.getTranslatedSource();
        SimpleNode root = ((CSSParserResult) presult).root();

        if (root == null) {
            //serious error in the source, no results
            return Collections.emptyList();
        }

        final List<StructureItem> items = new ArrayList<StructureItem>();
        this.formatter = formatter;

        NodeVisitor rulesSearch = new NodeVisitor() {

            public void visit(SimpleNode node) {
                if (node.kind() == CSSParserTreeConstants.JJTSELECTORLIST) {

                    //do not create structure items for virtual selectors
                    int so = AstUtils.documentPosition(node.startOffset(), source);
                    int eo = AstUtils.documentPosition(node.endOffset(), source);
                    if (eo != so) {
                        items.add(new CSSStructureItem(new CssElementHandle(node, info), source));
                    }
                }
            }
        };
        root.visitChildren(rulesSearch);
        return items;
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        try {
            //so far the css parser always parses the whole css content
            ParserResult presult = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator().next();
            final TranslatedSource source = presult.getTranslatedSource();
            final BaseDocument doc = (BaseDocument) info.getDocument();
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
            folds.put("codeblocks", foldRange);

            return folds;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return Collections.emptyMap();
        
    }

    private static final class CSSStructureItem implements StructureItem {

        private TranslatedSource source;
        private CssElementHandle handle;
        private String name;

        private CSSStructureItem(CssElementHandle handle, TranslatedSource source) {
            this.handle = handle;
            this.source = source;
        }

        public String getName() {
            return handle.getName();
        }

        public String getSortText() {
            return getName();
        }

        public String getHtml() {
            return getName();
        }

        public ElementHandle getElementHandle() {
            return handle;
        }

        public ElementKind getKind() {
            return ElementKind.FIELD;
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
            return AstUtils.documentPosition(handle.node().startOffset(), source);
        }

        public long getEndPosition() {
            return AstUtils.documentPosition(handle.node().endOffset(), source);
        }

        public ImageIcon getCustomIcon() {
            return null;
        }
    }
}
