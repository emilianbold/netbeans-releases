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
package org.netbeans.modules.css2.gsf;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.css2.editor.Css;
import org.netbeans.modules.css2.parser.CSSParserTreeConstants;
import org.netbeans.modules.css2.parser.NodeVisitor;
import org.netbeans.modules.css2.parser.SimpleNode;

/**
 *
 * @author marek
 */
public class CSSSemanticAnalyzer implements SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, ColoringAttributes> semanticHighlights;

    public Map<OffsetRange, ColoringAttributes> getHighlights() {
        return semanticHighlights;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo ci) throws Exception {

        if (cancelled) {
            return;
        }
        
        final Map<OffsetRange, ColoringAttributes> highlights = new HashMap<OffsetRange, ColoringAttributes>();

        //XXX fixthis - the css parser always parses the whole css content!
        ParserResult presult = ci.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator().next();
        final TranslatedSource source = presult.getTranslatedSource();
        SimpleNode root = ((CSSParserResult) presult).root();
        
        if(root == null) {
            //serious error in the source, no results
            semanticHighlights = highlights;
            return;
        }
        
        NodeVisitor visitor = new NodeVisitor() {

            //XXX using the ColoringAttributes.YYY java specific codes should
            //be changed to something more meaningful
            
            public void visit(SimpleNode node) {
                if (node.kind() == CSSParserTreeConstants.JJTELEMENTNAME || node.kind() == CSSParserTreeConstants.JJT_CLASS || node.kind() == CSSParserTreeConstants.JJTPSEUDO || node.kind() == CSSParserTreeConstants.JJTHASH || node.kind() == CSSParserTreeConstants.JJTATTRIB) {
                    AstOffsetRange range = new AstOffsetRange(node.startOffset(), node.endOffset(), source);
                    //filter virtual nodes
                    if (!range.isEmpty()) {
                        highlights.put(range, ColoringAttributes.METHOD);
                    }
                } else if (node.kind() == CSSParserTreeConstants.JJTPROPERTY) {
                    //check vendor speficic property
                    AstOffsetRange range = new AstOffsetRange(node.startOffset(), node.endOffset(), source);

                    if (!range.isEmpty()) { //filter virtual nodes

                        String propertyName = node.image().trim();
                        if (CssAnalyser.isVendorSpecificProperty(propertyName)) {
                            //special highlight for vend. spec. properties
                            highlights.put(range, ColoringAttributes.PARAMETER_USE);
                        } else {
                            highlights.put(range, ColoringAttributes.PARAMETER);
                        }
                    }
                }
            }
        };

        root.visitChildren(visitor);


//        for (int i = 0; i < sheet.getCssRules().getLength(); i++) {
//            if (cancelled) {
//                return;
//            }
//
//            CSSRule rule = sheet.getCssRules().item(i);
//            if (rule.getType() == CSSRule.STYLE_RULE) {
//                CSSStyleRule styleRule = (CSSStyleRule) rule;
//
//                CSSStyleDeclaration declaration = styleRule.getStyle();
//                System.out.println("rule " + styleRule.getCssText() + ": " + declaration.getCssText());
//
//                //TODO compute the range - need to hack the parser so it provides offsets
//                OffsetRange range = new OffsetRange(0, 20);
//                highlights.put(range, ColoringAttributes.PARAMETER);
//            }
//        }

        semanticHighlights = highlights;

    }

    public static class AstOffsetRange extends OffsetRange {

        private TranslatedSource source;

        public AstOffsetRange(int start, int end, TranslatedSource source) {
            super(start, end);
            this.source = source;
        }

        public int getStart() {
            return source == null ? super.getStart() : source.getLexicalOffset(super.getStart());
        }

        public int getEnd() {
            return source == null ? super.getEnd() : source.getLexicalOffset(super.getEnd());
        }

        public boolean isEmpty() {
            return getEnd() - getStart() == 0;
        }
    }
}
