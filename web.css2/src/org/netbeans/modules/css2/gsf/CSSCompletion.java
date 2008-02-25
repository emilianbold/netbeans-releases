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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.css2.editor.Css;
import org.netbeans.modules.css2.editor.LexerUtils;
import org.netbeans.modules.css2.editor.Property;
import org.netbeans.modules.css2.editor.PropertyModel;
import org.netbeans.modules.css2.lexer.api.CSSTokenId;
import org.netbeans.modules.css2.parser.ASCII_CharStream;
import org.netbeans.modules.css2.parser.CSSParser;
import org.netbeans.modules.css2.parser.CSSParserTreeConstants;
import org.netbeans.modules.css2.parser.NodeVisitor;
import org.netbeans.modules.css2.parser.SimpleNode;
import org.netbeans.modules.css2.parser.SimpleNodeUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 * 
 * @todo extend the GSF code completion item so it can handle case like
 *       background-image: url(|) - completing url() and setting the caret 
 *       between the braces
 */
public class CSSCompletion implements Completable {

    private static final Logger LOGGER = Logger.getLogger(CSSCompletion.class.getName());
    
    private final PropertyModel PROPERTIES = PropertyModel.instance();

    public List<CompletionProposal> complete(CompilationInfo info, int caretOffset, String prefix, NameKind kind, QueryType queryType, boolean caseSensitive, HtmlFormatter formatter) {
        try {

//            try {
//                String source = "/* X ";
//                CSSParser parser = new CSSParser(new ASCII_CharStream(new StringReader(source)));
//                parser.styleSheet();
//
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }

//            System.out.println("completion");
//            System.out.println("compilation info: " + info);
//            System.out.println("prefix: '" + prefix + "'");
//            System.out.println("kind: " + kind.name());
//            System.out.println("query type: " + queryType.name());

            if (prefix == null) {
                return null;
            }

            TokenSequence ts = LexerUtils.getCssTokenSequence(info.getDocument(), caretOffset);
            ts.move(caretOffset - prefix.length());
            ts.moveNext();


            //so far the css parser always parses the whole css content
            ParserResult presult = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator().next();
            TranslatedSource source = presult.getTranslatedSource();
            SimpleNode root = ((CSSParserResult) presult).root();

            if(root == null) {
                //broken source
                return null;
            }
            
            int astCaretOffset = source == null ? caretOffset : source.getAstOffset(caretOffset);

            SimpleNode node = SimpleNodeUtil.findDescendant(root, astCaretOffset);
            if (node == null) {
                LOGGER.info("cannot find any AST node for position " + caretOffset);
                return null;
            }

//            System.out.println("AST node kind = " + CSSParserTreeConstants.jjtNodeName[node.kind()]);


            //Why we need the (prefix.length() > 0 || astCaretOffset == node.startOffset())???
            //
            //We need to filter out situation when the node contains some whitespaces
            //at the end. For example:
            //    h1 { color     : red;}
            // the color property node contains the whole text to the colon
            //
            //In such case the prefix is empty and the cc would offer all 
            //possible values there
            //
            if (node.kind() == CSSParserTreeConstants.JJTPROPERTY && (prefix.length() > 0 || astCaretOffset == node.startOffset())) {
                //css property name completion with prefix
                Collection<Property> possibleProps = filterProperties(PROPERTIES.properties(), prefix);
                return wrapProperties(possibleProps, CompletionItemKind.PROPERTY, AstUtils.documentPosition(node.startOffset(), source), formatter);

            } else if (node.kind() == CSSParserTreeConstants.JJTSTYLERULE) {
                //should be no prefix 
                return wrapProperties(PROPERTIES.properties(), CompletionItemKind.PROPERTY, caretOffset, formatter);
            } else if (node.kind() == CSSParserTreeConstants.JJTDECLARATION) {
                //value cc without prefix
                //find property node

                final SimpleNode[] result = new SimpleNode[1];
                NodeVisitor propertySearch = new NodeVisitor() {

                    public void visit(SimpleNode node) {
                        if (node.kind() == CSSParserTreeConstants.JJTPROPERTY) {
                            result[0] = node;
                        }
                    }
                };
                node.visitChildren(propertySearch);

                SimpleNode property = result[0];

                Property prop = PROPERTIES.getProperty(property.image());
                if (prop != null) {
                    //known property
                    Collection<String> values = prop.values();
                    return wrapValues(values, CompletionItemKind.VALUE, caretOffset, formatter);
                }

            //Why we need the (prefix.length() > 0 || astCaretOffset == node.startOffset())???
            //please refer to the comment above
            } else if (node.kind() == CSSParserTreeConstants.JJTTERM && (prefix.length() > 0 || astCaretOffset == node.startOffset())) {
                //value cc with prefix
                //find property node

                //1.find declaration node first

                final SimpleNode[] result = new SimpleNode[1];
                NodeVisitor declarationSearch = new NodeVisitor() {

                    public void visit(SimpleNode node) {
                        if (node.kind() == CSSParserTreeConstants.JJTDECLARATION) {
                            result[0] = node;
                        }
                    }
                };
                SimpleNodeUtil.visitAncestors(node, declarationSearch);
                SimpleNode declaratioNode = result[0];

                //2.find the property node
                result[0] = null;
                NodeVisitor propertySearch = new NodeVisitor() {

                    public void visit(SimpleNode node) {
                        if (node.kind() == CSSParserTreeConstants.JJTPROPERTY) {
                            result[0] = node;
                        }
                    }
                };
                SimpleNodeUtil.visitChildren(declaratioNode, propertySearch);

                SimpleNode property = result[0];

                Property prop = PROPERTIES.getProperty(property.image());
                Collection<String> values = prop.values();
                return wrapValues(filterValues(values, prefix), CompletionItemKind.VALUE, AstUtils.documentPosition(node.startOffset(), source), formatter);


            }



        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private List<CompletionProposal> wrapValues(Collection<String> props, CompletionItemKind kind, int anchor, HtmlFormatter formatter) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (String p : props) {
            CompletionProposal proposal = new CSSCompletionItem(p, kind, anchor, formatter);
            proposals.add(proposal);
        }
        return proposals;
    }

    private Collection<String> filterValues(Collection<String> props, String propertyNamePrefix) {
        List<String> filtered = new ArrayList<String>();
        for (String p : props) {
            if (p.startsWith(propertyNamePrefix)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    private List<CompletionProposal> wrapProperties(Collection<Property> props, CompletionItemKind kind, int anchor, HtmlFormatter formatter) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (Property p : props) {
            CompletionProposal proposal = new CSSCompletionItem(p.name(), kind, anchor, formatter);
            proposals.add(proposal);
        }
        return proposals;
    }

    private Collection<Property> filterProperties(Collection<Property> props, String propertyNamePrefix) {
        List<Property> filtered = new ArrayList<Property>();
        for (Property p : props) {
            if (p.name().startsWith(propertyNamePrefix)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public String document(CompilationInfo info, ElementHandle element) {
        return "doc not supported yet";
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        try {
            TokenSequence ts = LexerUtils.getCssTokenSequence(info.getDocument(), caretOffset);

            //we are out of any css
            if (ts == null) {
                return null;
            }

            int diff = ts.move(caretOffset);
            if (diff == 0) {
                if (!ts.movePrevious()) {
                    //beginning of the token sequence, cannot get any prefix
                    return "";
                }
            } else {
                if (!ts.moveNext()) {
                    return null;
                }
            }
            Token t = ts.token();

            if (t.id() == CSSTokenId.COLON) {
                return "";
            } else {
                return t.text().subSequence(0, diff == 0 ? t.text().length() : diff).toString().trim();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;

    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        if (typedText == null || typedText.length() == 0) {
            return QueryType.NONE;
        }
        char c = typedText.charAt(0);
        switch (c) {
            case '\n':
            case '}':
            case ';': {
                return QueryType.STOP;
            }
            case ':': {
                return QueryType.COMPLETION;
            }
        }
        return QueryType.NONE;
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return "";
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        return null;
    }

    private static enum CompletionItemKind {

        PROPERTY, VALUE;
    }

    /**
     * @todo support for more completion type providers - like colors => subclass this class, remove the kind field, it's just temp. hack
     * 
     */
    private class CSSCompletionItem implements CompletionProposal {

        private static final String CSS_PROPERTY = "org/netbeans/modules/css2/resources/methodPublic.png"; //NOI18N
        private static final String CSS_VALUE = "org/netbeans/modules/css2/resources/fieldPublic.png"; //NOI18N
          
          
        private   ImageIcon 
         propertyIcon ,   valueIcon ;
        private   

             int anchorOffset;   
              
              
              
              
        

        private   String 
             value;
        

        private  HtmlFormatter formatter 
             ;
        

        private  CompletionItemKind kind 
             ;

        

        private  CSSCompletionItem (
                 
             String
        

         value  
             
        

           
                
                   , CompletionItemKind kind, int anchorOffset, HtmlFormatter formatter) {
            this.anchorOffset = anchorOffset;
            this.value = value;
            this.kind = kind;
            this.formatter = formatter;
        }

        public int getAnchorOffset() {
            return anchorOffset;
        }

        public String getName() {
            return value;
        }

        public String getInsertPrefix() {
            return getName();
        }

        public String getSortText() {
            return getName();
        }


        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        public ImageIcon getIcon() {
            if (kind == CompletionItemKind.PROPERTY) {
                if (propertyIcon == null) {
                    propertyIcon = new ImageIcon(org.openide.util.Utilities.loadImage(CSS_PROPERTY));
                }
                return propertyIcon;
            } else if (kind == CompletionItemKind.VALUE) {
                if (valueIcon == null) {
                    valueIcon = new ImageIcon(org.openide.util.Utilities.loadImage(CSS_VALUE));
                }
                return valueIcon;
            }
            return null;
        }

        public String getLhsHtml() {
            formatter.reset();
            formatter.appendText(getName());
            return formatter.getText();
        }

        public String getRhsHtml() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public String toString() {
            return getName();
        }

        public boolean isSmart() {
            return false;
        }

        public List<String> getInsertParams() {
            return null;
        }

        public String[] getParamListDelimiters() {
            return new String[]{"(", ")"}; // NOI18N
        }

        public String getCustomInsertTemplate() {
            return null;
        }

        public ElementHandle getElement() {
            return null;
        }
    }
}
