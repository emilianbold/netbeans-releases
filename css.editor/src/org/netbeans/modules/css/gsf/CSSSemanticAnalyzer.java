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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.parser.CSSParserTreeConstants;
import org.netbeans.modules.css.parser.CssParserAccess;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;

/**
 *
 * @author marek
 */
public class CSSSemanticAnalyzer implements SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo ci) throws Exception {
        cancelled = false;
        
        if (cancelled) {
            return;
        }
        
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<OffsetRange, Set<ColoringAttributes>>();

        //XXX fixthis - the css parser always parses the whole css content!
        
        Iterator<? extends ParserResult> presultIterator = ci.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator();
        if(!presultIterator.hasNext()) {
            return;
        }
        
        ParserResult presult = presultIterator.next();
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
                    int dso = AstUtils.documentPosition(node.startOffset(), source);
                    int deo = AstUtils.documentPosition(node.endOffset(), source);
                    OffsetRange range = new OffsetRange(dso, deo);
                    //filter out generated and inlined style definitions - they have just virtual selector which
                    //is mapped to empty string
                    if (!range.isEmpty() && deo > dso) {
                        highlights.put(range, ColoringAttributes.METHOD_SET);
                    }
                } else if (node.kind() == CSSParserTreeConstants.JJTPROPERTY) {
                    //check vendor speficic property
                    OffsetRange range = getOffsetRange(node.startOffset(), node.endOffset(), source);

                    if (!range.isEmpty()) { //filter virtual nodes

                        String propertyName = node.image().trim();
                        if(CssParserAccess.containsGeneratedCode(propertyName)) {
                            return;
                        }
                        
                        if (CssAnalyser.isVendorSpecificProperty(propertyName)) {
                            //special highlight for vend. spec. properties
                            highlights.put(range, ColoringAttributes.CUSTOM2_SET);
                        } else {
                            highlights.put(range, ColoringAttributes.CUSTOM1_SET);
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
    
    private OffsetRange getOffsetRange(int start, int end, TranslatedSource source) {
        if (source != null) {
            int length = end-start;
            start = source.getLexicalOffset(start);
            if (start == -1) {
                start = 0;
            }
            // We assume that the start and end are always mapped to the same delta,
            // e.g. tags don't span embedding regions.
            // If not, we could call getLexicalOffset(end) as well
            end = start+length;
        }
        return new OffsetRange(start, end);
    }
}
