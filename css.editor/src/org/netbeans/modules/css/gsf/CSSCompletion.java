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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.css.editor.PropertyModel.Element;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CompletionProposal;
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
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.editor.CssHelpResolver;
import org.netbeans.modules.css.editor.CssPropertyValue;
import org.netbeans.modules.css.editor.LexerUtils;
import org.netbeans.modules.css.editor.Property;
import org.netbeans.modules.css.editor.PropertyModel;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.css.parser.CSSParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.spi.DefaultCompletionResult;

/**
 *
 * @author marek
 * 
 * @todo extend the GSF code completion item so it can handle case like
 *       background-image: url(|) - completing url() and setting the caret 
 *       between the braces
 */
public class CSSCompletion implements CodeCompletionHandler {

    private static final Logger LOGGER = Logger.getLogger(CSSCompletion.class.getName());
    private final PropertyModel PROPERTIES = PropertyModel.instance();
    private static final Collection<String> AT_RULES = Arrays.asList(new String[]{"@media", "@page", "@import", "@charset", "@font-face"}); //NOI18N

    public CodeCompletionResult complete(CodeCompletionContext context) {
        CompilationInfo info = context.getInfo();
        int caretOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        NameKind kind = context.getNameKind();
        QueryType queryType = context.getQueryType();
        boolean caseSensitive = context.isCaseSensitive();
    
//        try {
//            String source = "/* X ";
//            CSSParser parser = new CSSParser(new ASCII_CharStream(new StringReader(source)));
//            parser.styleSheet();
//
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//
//        System.out.println("completion");
//        System.out.println("compilation info: " + info);
//        System.out.println("prefix: '" + prefix + "'");
//        System.out.println("kind: " + kind.name());
//        System.out.println("query type: " + queryType.name());

        Document document = info.getDocument();
        if (document == null) {
            return CodeCompletionResult.NONE;
        }

        TokenSequence ts = LexerUtils.getCssTokenSequence(document, caretOffset);
        if (ts == null || prefix == null) {
            // No CSS tokens here: perhaps we're doing code completion in an
            // empty CSS context like an empty HTML style attribute; in that case,
            // just offer the CSS properties.
            return wrapProperties(PROPERTIES.properties(), CompletionItemKind.PROPERTY, caretOffset);
        }

        ts.move(caretOffset - prefix.length());
        boolean hasNext = ts.moveNext();


        //so far the css parser always parses the whole css content
        ParserResult presult = info.getEmbeddedResults(Css.CSS_MIME_TYPE).iterator().next();
        TranslatedSource source = presult.getTranslatedSource();
        SimpleNode root = ((CSSParserResult) presult).root();

        if (root == null) {
            //broken source
            return CodeCompletionResult.NONE;
        }

        int astCaretOffset = source == null ? caretOffset : source.getAstOffset(caretOffset);

        SimpleNode node = SimpleNodeUtil.findDescendant(root, astCaretOffset);
        if (node == null) {
            //the parse tree is likely broken by some text typed, 
            //but we still need to provide the completion in some cases

            if (hasNext && "@".equals(ts.token().text().toString())) { //NOI18N
                //complete rules
                return wrapRAWValues(AT_RULES, CompletionItemKind.VALUE, ts.offset());
            }

            return CodeCompletionResult.NONE; //no parse tree, just quit
        }

        if (node.kind() == CSSParserTreeConstants.JJTREPORTERROR) {
            node = (SimpleNode) node.jjtGetParent();
            if (node == null) {
                return CodeCompletionResult.NONE;
            }
        }
//            root.dump("");
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
        if (node.kind() == CSSParserTreeConstants.JJTSTYLESHEETRULELIST) {
            //complete at keywords without prefix
            return wrapRAWValues(AT_RULES, CompletionItemKind.VALUE, caretOffset);
        } else if (node.kind() == CSSParserTreeConstants.JJTSKIP) {
            //complete at keywords with prefix - parse tree broken
            SimpleNode parent = (SimpleNode) node.jjtGetParent();
            if (parent != null && parent.kind() == CSSParserTreeConstants.JJTUNKNOWNRULE) {  //test the parent node
                Collection<String> possibleValues = filterStrings(AT_RULES, prefix);
                return wrapRAWValues(possibleValues, CompletionItemKind.VALUE, AstUtils.documentPosition(parent.startOffset(), source));
            }
        } else if (node.kind() == CSSParserTreeConstants.JJTIMPORTRULE || node.kind() == CSSParserTreeConstants.JJTMEDIARULE || node.kind() == CSSParserTreeConstants.JJTPAGERULE || node.kind() == CSSParserTreeConstants.JJTCHARSETRULE || node.kind() == CSSParserTreeConstants.JJTFONTFACERULE) {
            //complete at keywords with prefix - parse tree OK
            TokenId id = ts.token().id();
            if (id == CSSTokenId.IMPORT_SYM || id == CSSTokenId.MEDIA_SYM || id == CSSTokenId.PAGE_SYM || id == CSSTokenId.CHARSET_SYM || id == CSSTokenId.FONT_FACE_SYM) {
                //we are on the right place in the node

                Collection<String> possibleValues = filterStrings(AT_RULES, prefix);
                return wrapRAWValues(possibleValues, CompletionItemKind.VALUE, AstUtils.documentPosition(node.startOffset(), source));
            }

        } else if (node.kind() == CSSParserTreeConstants.JJTPROPERTY && (prefix.length() > 0 || astCaretOffset == node.startOffset())) {
            //css property name completion with prefix
            Collection<Property> possibleProps = filterProperties(PROPERTIES.properties(), prefix);
            return wrapProperties(possibleProps, CompletionItemKind.PROPERTY, AstUtils.documentPosition(node.startOffset(), source));

        } else if (node.kind() == CSSParserTreeConstants.JJTSTYLERULE) {
            //should be no prefix 
            return wrapProperties(PROPERTIES.properties(), CompletionItemKind.PROPERTY, caretOffset);
        } else if (node.kind() == CSSParserTreeConstants.JJTDECLARATION) {
            //value cc without prefix
            //find property node
            
            final SimpleNode[] result = new SimpleNode[2];
            NodeVisitor propertySearch = new NodeVisitor() {

                public void visit(SimpleNode node) {
                    if (node.kind() == CSSParserTreeConstants.JJTPROPERTY) {
                        result[0] = node;
                    } else if(node.kind() == CSSParserTreeConstants.JJTERROR_SKIPDECL) {
                        result[1] = node;
                    }
                }
            };
            node.visitChildren(propertySearch);

            SimpleNode property = result[0];

            String expressionText = ""; //NOI18N
            if(result[1] != null) {
                //error in the property value
                //we need to extract the value from the property node image
                
                String propertyImage = node.image().trim();
                //if the property is the last one in the rule then the error
                //contains the closing rule bracket
                if(propertyImage.endsWith("}")) { //NOI18N
                    propertyImage = propertyImage.substring(0, propertyImage.length() - 1);
                }
                
                int colonIndex = propertyImage.indexOf(':'); //NOI18N
                if(colonIndex >= 0) {
                    expressionText = propertyImage.substring(colonIndex + 1);
                }
               
                //use just the current line, if the expression spans to multiple
                //lines it is likely because of parsing error
                int eolIndex = expressionText.indexOf('\n');
                if (eolIndex > 0) {
                    expressionText = expressionText.substring(0, eolIndex);
                }

            }

            Property prop = PROPERTIES.getProperty(property.image());
            if (prop != null) {

                CssPropertyValue propVal = new CssPropertyValue(prop, expressionText);

                Collection<Element> alts = propVal.alternatives();

                Collection<Element> filteredByPrefix = filterElements(alts, prefix);

                int completionItemInsertPosition = prefix.trim().length() == 0
                        ? caretOffset
                        : AstUtils.documentPosition(node.startOffset(), source);

                //test the situation when completion is invoked just after a valid token
                //like color: rgb|
                //in such case the parser offers ( alternative which is valid
                //so we must not use the prefix for filtering the results out.
                if (alts.size() > 0 && filteredByPrefix.size() == 0) {
                    completionItemInsertPosition = caretOffset; //complete on the position of caret
                    filteredByPrefix = alts; //prefix is empty, do not filter at all
                }

                return wrapPropertyValues(prop,
                        filteredByPrefix,
                        CompletionItemKind.VALUE,
                        completionItemInsertPosition,
                        false);


            }

        //Why we need the (prefix.length() > 0 || astCaretOffset == node.startOffset())???
        //please refer to the comment above
//        } else if (node.kind() == CSSParserTreeConstants.JJTTERM && (prefix.length() > 0 || astCaretOffset == node.startOffset())) {
        } else if (node.kind() == CSSParserTreeConstants.JJTTERM) {
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
            if (prop == null) {
                return CodeCompletionResult.NONE;
            }

            SimpleNode expression = (SimpleNode)node.jjtGetParent();
            String expressionText = expression.image();
            
            //use just the current line, if the expression spans to multiple
            //lines it is likely because of parsing error
            int eolIndex = expressionText.indexOf('\n');
            if(eolIndex > 0) {
                expressionText = expressionText.substring(0, eolIndex);
            }
            
            CssPropertyValue propVal = new CssPropertyValue(prop, expressionText);
            
            Collection<Element> alts = propVal.alternatives();
                    
            Collection<Element> filteredByPrefix = filterElements(alts, prefix);

            int completionItemInsertPosition = prefix.trim().length() == 0
                    ? caretOffset
                    : AstUtils.documentPosition(node.startOffset(), source);

            //test the situation when completion is invoked just after a valid token
            //like color: rgb|
            //in such case the parser offers ( alternative which is valid
            //so we must not use the prefix for filtering the results out.
            if (alts.size() > 0 && filteredByPrefix.size() == 0) {
                completionItemInsertPosition = caretOffset; //complete on the position of caret
                filteredByPrefix = alts; //prefix is empty, do not filter at all
            }

            return wrapPropertyValues(prop, 
                    filteredByPrefix, 
                    CompletionItemKind.VALUE, 
                    completionItemInsertPosition,
                    false);


        }

        return CodeCompletionResult.NONE;
    }

    private CodeCompletionResult wrapRAWValues(Collection<String> props, CompletionItemKind kind, int anchor) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (String value : props) {
            CSSElement handle = new CSSElement(value);
            CompletionProposal proposal = createCompletionItem(handle, value, kind, anchor, false);
            proposals.add(proposal);
        }
        return new DefaultCompletionResult(proposals, false);
    }

    private CodeCompletionResult wrapPropertyValues(Property property,
            Collection<Element> props,
            CompletionItemKind kind,
            int anchor,
            boolean addSemicolon) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (Element e : props) {
            if (e instanceof PropertyModel.ValueElement) {
                if (((PropertyModel.ValueElement) e).isUnit()) {
                    continue; //skip units
                }
            }
            CssValueElement handle = new CssValueElement(property, e);
            CompletionProposal proposal = createValueCompletionItem(handle, e, kind, anchor, addSemicolon);
            proposals.add(proposal);
        }
        return new DefaultCompletionResult(proposals, false);
    }

    private Collection<String> filterStrings(Collection<String> values, String propertyNamePrefix) {
        propertyNamePrefix = propertyNamePrefix.toLowerCase();
        List<String> filtered = new ArrayList<String>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(propertyNamePrefix)) {
                filtered.add(value);
            }
        }
        return filtered;
    }
    
    private Collection<Element> filterElements(Collection<Element> values, String propertyNamePrefix) {
        propertyNamePrefix = propertyNamePrefix.toLowerCase();
        List<Element> filtered = new ArrayList<Element>();
        for (Element value : values) {
            if (value.toString().toLowerCase().startsWith(propertyNamePrefix)) {
                filtered.add(value);
            }
        }
        return filtered;
    }

    private CodeCompletionResult wrapProperties(Collection<Property> props, CompletionItemKind kind, int anchor) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (Property p : props) {
            //filter out non-public properties
            if(!p.name().startsWith("-")) { //NOI18N
                CSSElement handle = new CssPropertyElement(p);
                CompletionProposal proposal = createPropertyNameCompletionItem(handle, p.name(), kind, anchor, false);
                proposals.add(proposal);
            }
        }
        return new DefaultCompletionResult(proposals, false);
    }

    private Collection<Property> filterProperties(Collection<Property> props, String propertyNamePrefix) {
        propertyNamePrefix = propertyNamePrefix.toLowerCase();
        List<Property> filtered = new ArrayList<Property>();
        for (Property p : props) {
            if (p.name().toLowerCase().startsWith(propertyNamePrefix)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public String document(CompilationInfo info, ElementHandle element) {
        if (element instanceof CssValueElement) {
            CssValueElement e = (CssValueElement) element;

//            System.out.println("property = " + e.property().name() + "\n value = " + e.value());
            return CssHelpResolver.instance().getPropertyHelp(e.property().name());

        } else if (element instanceof CssPropertyElement) {
            CssPropertyElement e = (CssPropertyElement) element;
//            System.out.println("property = " + e.property().name());
            return CssHelpResolver.instance().getPropertyHelp(e.property().name());
        }
        return null;
    }
   
    public ElementHandle resolveLink(String link, ElementHandle elementHandle) {
        return new ElementHandle.UrlHandle(CssHelpResolver.HELP_URL + link);
    }
    
    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        Document document = info.getDocument();
        if (document == null) {
            return null;
        }
        TokenSequence ts = LexerUtils.getCssTokenSequence(document, caretOffset);

        //we are out of any css
        if (ts == null) {
            return null;
        }

        int diff = ts.move(caretOffset);
        if (diff == 0) {
            if (!ts.movePrevious()) {
                //beginning of the token sequence, cannot get any prefix
                return ""; //NOI18N
            }
        } else {
            if (!ts.moveNext()) {
                return null;
            }
        }
        Token t = ts.token();

        if (t.id() == CSSTokenId.COLON) {
            return ""; //NOI18N
        } else {
            return t.text().subSequence(0, diff == 0 ? t.text().length() : diff).toString().trim();
        }
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        if (typedText == null || typedText.length() == 0) {
            return QueryType.NONE;
        }
        char c = typedText.charAt(typedText.length() - 1);
        switch (c) {
            case '\n':
            case '}':
            case ';': {
                return QueryType.STOP;
            }
            case ':':
            case ',': {
                return QueryType.COMPLETION;
            }
        }
        return QueryType.NONE;
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return ""; //NOI18N
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    private static enum CompletionItemKind {
        PROPERTY, VALUE;
    }

    private CSSCompletionItem createValueCompletionItem(CssValueElement element, 
            Element value, 
            CompletionItemKind kind, 
            int anchorOffset,
            boolean addSemicolon) {
            
            String origin = value.origin();
            
            assert origin != null : "Css value completion item has no origin!"; //NOI18N
            
            if(origin.startsWith("-")) { //NOI18N
                //artificial origin, get real origin from the first ancestor element with an origin
                Element parent = value;
                while((parent = parent.parent()) != null) {
                    if(parent.origin() != null && !parent.origin().startsWith("-")) {
                        origin = parent.origin();
                        break;
                    }
                }    
            }
            
            if("color".equals(origin)) { //NOI18N
                return new ColorCompletionItem(element, value.toString(), origin, kind, anchorOffset, addSemicolon);
            }
            
            return new ValueCompletionItem(element, value.toString(), origin, kind, anchorOffset, addSemicolon);
    }
    
    private CSSCompletionItem createPropertyNameCompletionItem(CSSElement element, 
            String value, 
            CompletionItemKind kind, 
            int anchorOffset, 
            boolean addSemicolon) {
        
        return new PropertyCompletionItem(element, value, kind, anchorOffset, addSemicolon);
    }
    
    private CSSCompletionItem createCompletionItem(CSSElement element, 
            String value, 
            CompletionItemKind kind, 
            int anchorOffset, 
            boolean addSemicolon) {
        
        return new CSSCompletionItem(element, value, kind, anchorOffset, addSemicolon);
    }
    
    
    private final HashMap<String, String> colors = new HashMap<String, String>(20);

    
    //TODO add support for non w3c standart colors, CSS3 seems to be more vague in checking the color values
    private synchronized HashMap<String, String> colors() {
        if (colors.isEmpty()) {
            colors.put("red", "ff0000"); //NOI18N
            colors.put("black", "000000"); //NOI18N
            colors.put("green", "00ff00"); //NOI18N
            colors.put("blue", "0000ff"); //NOI18N
            colors.put("silver", "C0C0C0"); //NOI18N
            colors.put("gray", "808080"); //NOI18N
            colors.put("white", "ffffff"); //NOI18N
            colors.put("maroon", "800000"); //NOI18N
            colors.put("purple", "800080"); //NOI18N
            colors.put("fuchsia", "ff00ff"); //NOI18N
            colors.put("lime", "00ff00"); //NOI18N
            colors.put("olive", "808000"); //NOI18N
            colors.put("orange", "ffa500"); //NOI18N
            colors.put("yellow", "ffff00"); //NOI18N
            colors.put("navy", "000080"); //NOI18N
            colors.put("teal", "008080"); //NOI18N
            colors.put("aqua", "00ffff"); //NOI18N
        }
        return colors;
    }

    private class ValueCompletionItem extends CSSCompletionItem {
        
        private String origin; //property name to which this value belongs
        
        private ValueCompletionItem(CSSElement element, 
                String value, 
                String origin,
                CompletionItemKind kind, 
                int anchorOffset, 
                boolean addSemicolon) {
            
                super(element, value, kind, anchorOffset, addSemicolon);
                this.origin = origin;
        }

        @Override
         public String getInsertPrefix() {
            return getName() + (addSemicolon ? ";" : ""); //NOI18N
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + origin + "</font>"; //NOI18N
        }
         
        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            Property owningProperty = ((CssValueElement)getElement()).property();
            String initialValue = owningProperty.initialValue();
            if(initialValue != null && initialValue.equals(getName())) {
                //initial value
                return "<i>" + super.getLhsHtml(formatter) + "</i>"; //NOI18N
            }
            
            return super.getLhsHtml(formatter);
        }
        
    }
    
    private class ColorCompletionItem extends ValueCompletionItem {

        final byte COLOR_ICON_SIZE = 16; //px
        final byte COLOR_RECT_SIZE = 10; //px
        
        private ColorCompletionItem(CSSElement element, 
                String value, 
                String origin,
                CompletionItemKind kind, 
                int anchorOffset, 
                boolean addSemicolon) {
            
                super(element, value, origin, kind, anchorOffset, addSemicolon);
        }
                
        @Override
        public ImageIcon getIcon() {
            BufferedImage i = new BufferedImage(COLOR_ICON_SIZE, COLOR_ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g = i.createGraphics();
            String colorCode = colors().get(getName());
            
            if(colorCode == null) {
                return  null; //unknown colo code
            }
            
            Color transparent = new Color( 0x00ffffff, true );
            g.setColor(transparent);
            g.fillRect(0, 0, COLOR_ICON_SIZE, COLOR_ICON_SIZE);
            
            g.setColor(Color.decode("0x"+colorCode)); //NOI18N
            g.fillRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE, 
                    COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1, 
                    COLOR_RECT_SIZE - 1, 
                    COLOR_RECT_SIZE - 1);
            
            g.setColor(Color.DARK_GRAY);
            g.drawRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1, 
                    COLOR_ICON_SIZE - COLOR_RECT_SIZE - 2, 
                    COLOR_RECT_SIZE, 
                    COLOR_RECT_SIZE);
            
            
            
            return new ImageIcon(i);
        }
    }
    
    private class PropertyCompletionItem extends CSSCompletionItem {
        
        private PropertyCompletionItem(CSSElement element, 
                String value, 
                CompletionItemKind kind, 
                int anchorOffset, 
                boolean addSemicolon) {
            
                super(element, value, kind, anchorOffset, addSemicolon);
        }

        @Override
         public String getInsertPrefix() {
            return super.getInsertPrefix() + ":"; //NOI18N
        }
        
    }

    /**
     * @todo support for more completion type providers - like colors => subclass this class, remove the kind field, it's just temp. hack
     * 
     */
    private class CSSCompletionItem implements CompletionProposal {

        private static final String CSS_PROPERTY = "org/netbeans/modules/css/resources/methodPublic.png"; //NOI18N
        private static final String CSS_VALUE = "org/netbeans/modules/css/resources/fieldPublic.png"; //NOI18N
        private  ImageIcon propertyIcon,   valueIcon ;
        private int anchorOffset;
        private String value;
        protected CompletionItemKind kind;
        private CSSElement element;
        protected boolean addSemicolon;

        private CSSCompletionItem() {
        }

        private CSSCompletionItem(
                CSSElement element, String value, CompletionItemKind kind, int anchorOffset, boolean addSemicolon) {
            this.anchorOffset = anchorOffset;
            this.value = value;
            this.kind = kind;
            this.element = element;
            this.addSemicolon = addSemicolon;
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

        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.appendText(getName());
            return formatter.getText();
        }

        public String getRhsHtml(HtmlFormatter formatter) {
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

        public String getCustomInsertTemplate() {
            return null;
        }

        public ElementHandle getElement() {
            return element;
        }

        public int getSortPrioOverride() {
            return 0;
        }
    }
}
