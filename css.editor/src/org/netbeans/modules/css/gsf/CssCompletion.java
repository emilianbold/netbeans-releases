/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.api.ElementHandle.UrlHandle;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.PropertyModel.Element;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.css.editor.CssHelpResolver;
import org.netbeans.modules.css.editor.CssProjectSupport;
import org.netbeans.modules.css.editor.CssPropertyValue;
import org.netbeans.modules.css.editor.LexerUtils;
import org.netbeans.modules.css.editor.Property;
import org.netbeans.modules.css.editor.PropertyModel;
import org.netbeans.modules.css.editor.model.HtmlTags;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.indexing.CssIndex;
import org.netbeans.modules.css.indexing.CssIndexer;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.css.lexer.api.CssTokenId;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Marek Fukala <mfukala@netbeans.org>
 * 
 */
public class CssCompletion implements CodeCompletionHandler {

    private final PropertyModel PROPERTIES = PropertyModel.instance();
    private static final Collection<String> AT_RULES = Arrays.asList(new String[]{"@media", "@page", "@import", "@charset", "@font-face"}); //NOI18N
    private static char firstPrefixChar; //read getPrefix() comment!

    private static final String RELATED_SELECTOR_COLOR = "007c00"; //NOI18N
    private static String GRAY_COLOR_CODE = Integer.toHexString(Color.GRAY.getRGB()).substring(2);


    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        CssParserResult info = (CssParserResult) context.getParserResult();
        Snapshot snapshot = info.getSnapshot();
        FileObject file = snapshot.getSource().getFileObject();

        int caretOffset = context.getCaretOffset();
        String prefix = context.getPrefix() != null ? context.getPrefix() : "";

        //read getPrefix() comment!
        if(firstPrefixChar != 0) {
            prefix = firstPrefixChar + prefix;
        }

        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());

        assert ts != null;

        int offset = caretOffset - prefix.length();
        int astOffset = snapshot.getEmbeddedOffset(offset);
        boolean unmappableClassOrId = false;
        if (astOffset == -1) {
            if((prefix.length() == 1 && prefix.charAt(0) == '.') || (prefix.length() > 0 && prefix.charAt(0) == '#')) {
                //this happens if completion is invoked in empty css embedding,
                //for example in <div class="|"/>. The virtual source contains doesn't
                //map the position do the document, se we need to hack it
                unmappableClassOrId = true;
            } else {
                //cannot map the offset
                return null;
            }
        }

        ts.move(astOffset);
        boolean hasNext = ts.moveNext();


        SimpleNode root = info.root();
        if (root == null) {
            //broken source
            return CodeCompletionResult.NONE;
        }

        int astCaretOffset = snapshot.getEmbeddedOffset(caretOffset);

        char charAfterCaret = snapshot.getText().length() > (astCaretOffset + 1) ?
            snapshot.getText().subSequence(astCaretOffset, astCaretOffset + 1).charAt(0) :
            ' '; //NOI18N

        SimpleNode node = SimpleNodeUtil.findDescendant(root, astCaretOffset);
        if (node == null) {
            //the parse tree is likely broken by some text typed, 
            //but we still need to provide the completion in some cases

            if (hasNext && ts.token().text().charAt(0) == '@') { //NOI18N
                //complete rules
                return wrapRAWValues(AT_RULES, CompletionItemKind.VALUE, ts.offset());
            }

            return CodeCompletionResult.NONE; //no parse tree, just quit
        }

        int originalNodeKind = node.kind();
        if (node.kind() == CssParserTreeConstants.JJTREPORTERROR) {
            node = (SimpleNode) node.jjtGetParent();
            if (node == null) {
                return CodeCompletionResult.NONE;
            }
        }
//            root.dump("");
//            System.out.println("AST node kind = " + CssParserTreeConstants.jjtNodeName[node.kind()]);


        //Why we need the (prefix.length() > 0 || astCaretOffset == node.startOffset())???
        //
        //We need to filter out situation when the node contains some whitespaces
        //at the end. For example:
        //    h1 { color     : red;}
        // the color property node contains the whole text to the colon
        //
        //In such case the prefix is empty and the cc would offer all 
        //possible values there
        
        if(node.kind() == CssParserTreeConstants.JJT_CLASS || 
                (unmappableClassOrId || originalNodeKind == CssParserTreeConstants.JJTREPORTERROR) && prefix.length() == 1 && prefix.charAt(0) == '.') {
            //complete class selectors
            CssProjectSupport sup = CssProjectSupport.findFor(file);
            if(sup != null) {
                CssIndex index = sup.getIndex();
                DependenciesGraph deps = index.getDependencies(file);
                Collection<FileObject> refered = deps.getAllReferedFiles();

                //adjust prefix - if there's just . before the caret, it is returned
                //as a prefix. If there are another characters, the dot is ommited
                if(prefix.length() == 1 && prefix.charAt(0) == '.') {
                    prefix = "";
                    offset++; //offset point to the dot position, we need to skip it
                }
                //get map of all fileobject declaring classes with the prefix
                Map<FileObject, Collection<String>> search = index.findClassesByPrefix(prefix); 
                Collection<String> refclasses = new HashSet<String>();
                Collection<String> allclasses = new HashSet<String>();
                for(FileObject fo : search.keySet()) {
                    allclasses.addAll(search.get(fo));
                    //is the file refered by the current file?
                    if(refered.contains(fo)) {
                        //yes - add its classes
                        refclasses.addAll(search.get(fo));
                    }
                }
 
                //lets create the completion items
                List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(refclasses.size());
                for(String clazz : allclasses) {
                   proposals.add(new SelectorCompletionItem(new CssElement(clazz),
                        clazz,
                        CompletionItemKind.VALUE,
                        offset,
                        refclasses.contains(clazz)));
                }
                if (proposals.size() > 0) {
                    return new DefaultCompletionResult(proposals, false);
                }

            }
        } else if (prefix.length() > 0 && (node.kind() == CssParserTreeConstants.JJTHASH
                || (unmappableClassOrId || originalNodeKind == CssParserTreeConstants.JJTERROR_SKIP_TO_WHITESPACE ||
                originalNodeKind == CssParserTreeConstants.JJTERROR_SKIPBLOCK) && prefix.charAt(0) == '#')) {
            //complete class selectors
            CssProjectSupport sup = CssProjectSupport.findFor(file);
            if (sup != null) {
                CssIndex index = sup.getIndex();
                DependenciesGraph deps = index.getDependencies(file);
                Collection<FileObject> refered = deps.getAllReferedFiles();

                //adjust prefix - if there's just # before the caret, it is returned as a prefix
                //if there is some text behind the prefix the hash is part of the prefix
                if (prefix.length() == 1 && prefix.charAt(0) == '#') {
                    prefix = "";
                } else {
                    prefix = prefix.substring(1); //cut off the #
                }
                offset++; //offset point to the hash position, we need to skip it
                
                //get map of all fileobject declaring classes with the prefix
                Map<FileObject, Collection<String>> search = index.findIdsByPrefix(prefix); //cut off the dot (.)
                Collection<String> allids = new HashSet<String>();
                Collection<String> refids = new HashSet<String>();
                for (FileObject fo : search.keySet()) {
                    allids.addAll(search.get(fo));
                    //is the file refered by the current file?
                    if (refered.contains(fo)) {
                        //yes - add its classes
                        refids.addAll(search.get(fo));
                    }
                }

                //lets create the completion items
                List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(allids.size());
                for (String id : allids) {
                    proposals.add(new SelectorCompletionItem(new CssElement(id),
                            id,
                            CompletionItemKind.VALUE,
                            offset,
                            refids.contains(id)));
                }
                if (proposals.size() > 0) {
                    return new DefaultCompletionResult(proposals, false);
                }
            }
        } else if (node.kind() == CssParserTreeConstants.JJTSTYLESHEETRULELIST) {
            List<CompletionProposal> all = new ArrayList<CompletionProposal>();
            //complete at keywords without prefix
            all.addAll(wrapRAWValues(AT_RULES, CompletionItemKind.VALUE, caretOffset).getItems());
            //complete html selector names
            all.addAll(completeHtmlSelectors(prefix, caretOffset));
            return new DefaultCompletionResult(all, false);

        } else if (node.kind() == CssParserTreeConstants.JJTMEDIARULE) {
            return new DefaultCompletionResult(completeHtmlSelectors(prefix, caretOffset), false);

        } else if (node.kind() == CssParserTreeConstants.JJTSKIP) {
            //complete at keywords with prefix - parse tree broken
            SimpleNode parent = (SimpleNode) node.jjtGetParent();
            if (parent != null && parent.kind() == CssParserTreeConstants.JJTUNKNOWNRULE) {  //test the parent node
                Collection<String> possibleValues = filterStrings(AT_RULES, prefix);
                return wrapRAWValues(possibleValues, CompletionItemKind.VALUE, snapshot.getOriginalOffset(parent.startOffset()));
            }
        } else if (node.kind() == CssParserTreeConstants.JJTIMPORTRULE || node.kind() == CssParserTreeConstants.JJTMEDIARULE || node.kind() == CssParserTreeConstants.JJTPAGERULE || node.kind() == CssParserTreeConstants.JJTCHARSETRULE || node.kind() == CssParserTreeConstants.JJTFONTFACERULE) {
            //complete at keywords with prefix - parse tree OK
            if (hasNext) {
                TokenId id = ts.token().id();
                if (id == CssTokenId.IMPORT_SYM || id == CssTokenId.MEDIA_SYM || id == CssTokenId.PAGE_SYM || id == CssTokenId.CHARSET_SYM || id == CssTokenId.FONT_FACE_SYM) {
                    //we are on the right place in the node

                    Collection<String> possibleValues = filterStrings(AT_RULES, prefix);
                    return wrapRAWValues(possibleValues, CompletionItemKind.VALUE, snapshot.getOriginalOffset(node.startOffset()));
                }
            }

        } else if (node.kind() == CssParserTreeConstants.JJTPROPERTY && (prefix.length() > 0 || astCaretOffset == node.startOffset())) {
            //css property name completion with prefix
            Collection<Property> possibleProps = filterProperties(PROPERTIES.properties(), prefix);
            return wrapProperties(possibleProps, CompletionItemKind.PROPERTY, snapshot.getOriginalOffset(node.startOffset()));

        } else if (node.kind() == CssParserTreeConstants.JJTSTYLERULE) {
            //should be no prefix 
            return wrapProperties(PROPERTIES.properties(), CompletionItemKind.PROPERTY, caretOffset);
        } else if (node.kind() == CssParserTreeConstants.JJTDECLARATION) {
            //value cc without prefix
            //find property node

            final SimpleNode[] result = new SimpleNode[2];
            NodeVisitor propertySearch = new NodeVisitor() {

                @Override
                public void visit(SimpleNode node) {
                    if (node.kind() == CssParserTreeConstants.JJTPROPERTY) {
                        result[0] = node;
                    } else if (node.kind() == CssParserTreeConstants.JJTERROR_SKIPDECL) {
                        result[1] = node;
                    }
                }
            };
            node.visitChildren(propertySearch);

            SimpleNode property = result[0];

            String expressionText = ""; //NOI18N
            if (result[1] != null) {
                //error in the property value
                //we need to extract the value from the property node image

                String propertyImage = node.image().trim();
                //if the property is the last one in the rule then the error
                //contains the closing rule bracket
                if (propertyImage.endsWith("}")) { //NOI18N
                    propertyImage = propertyImage.substring(0, propertyImage.length() - 1);
                }

                int colonIndex = propertyImage.indexOf(':'); //NOI18N
                if (colonIndex >= 0) {
                    expressionText = propertyImage.substring(colonIndex + 1);
                }

                //use just the current line, if the expression spans to multiple
                //lines it is likely because of parsing error
                int eolIndex = expressionText.indexOf('\n');
                if (eolIndex > 0) {
                    expressionText = expressionText.substring(0, eolIndex);
                }

            }

            Property prop = PROPERTIES.getProperty(property.image().trim());
            if (prop != null) {

                CssPropertyValue propVal = new CssPropertyValue(prop, expressionText);

                Collection<Element> alts = propVal.alternatives();

                Collection<Element> filteredByPrefix = filterElements(alts, prefix);

                int completionItemInsertPosition = prefix.trim().length() == 0
                        ? caretOffset
                        : snapshot.getOriginalOffset(node.startOffset());

                //test the situation when completion is invoked just after a valid token
                //like color: rgb|
                //in such case the parser offers ( alternative which is valid
                //so we must not use the prefix for filtering the results out.
                //do that only if the completion is not called in the middle of a text,
                //there must be a whitespace after the caret
                boolean addSpaceBeforeItem = false;
                if (alts.size() > 0 && filteredByPrefix.size() == 0 && Character.isWhitespace(charAfterCaret)) {
                    completionItemInsertPosition = caretOffset; //complete on the position of caret
                    filteredByPrefix = alts; //prefix is empty, do not filter at all
                    addSpaceBeforeItem = true;
                }

                return wrapPropertyValues(context,
                        prefix,
                        prop,
                        filteredByPrefix,
                        CompletionItemKind.VALUE,
                        completionItemInsertPosition,
                        false,
                        addSpaceBeforeItem,
                        false);


            }

            //Why we need the (prefix.length() > 0 || astCaretOffset == node.startOffset())???
            //please refer to the comment above
//        } else if (node.kind() == CssParserTreeConstants.JJTTERM && (prefix.length() > 0 || astCaretOffset == node.startOffset())) {
        } else if (node.kind() == CssParserTreeConstants.JJTTERM || 
                (node.kind() == CssParserTreeConstants.JJTERROR_SKIPDECL &&
                ((SimpleNode)node.jjtGetParent()).kind() == CssParserTreeConstants.JJTDECLARATION)) {
            //value cc with prefix
            //a. for term nodes
            //b. for error skip declaration nodes with declaration parent,
            //for example if user types color: # and invokes the completion

            //find property node

            //1.find declaration node first

            final SimpleNode[] result = new SimpleNode[1];
            NodeVisitor declarationSearch = new NodeVisitor() {

                @Override
                public void visit(SimpleNode node) {
                    if (node.kind() == CssParserTreeConstants.JJTDECLARATION) {
                        result[0] = node;
                    }
                }
            };
            SimpleNodeUtil.visitAncestors(node, declarationSearch);
            SimpleNode declaratioNode = result[0];

            //2.find the property node
            result[0] = null;
            NodeVisitor propertySearch = new NodeVisitor() {

                @Override
                public void visit(SimpleNode node) {
                    if (node.kind() == CssParserTreeConstants.JJTPROPERTY) {
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

            String expressionText;
            if(node.kind() == CssParserTreeConstants.JJTTERM) {
                SimpleNode expression = (SimpleNode) node.jjtGetParent();
                expressionText = expression.image();
            } else {
                //error skip decl - no expression to parse
                expressionText = "";
            }

            //use just the current line, if the expression spans to multiple
            //lines it is likely because of parsing error
            int eolIndex = expressionText.indexOf('\n');
            if (eolIndex > 0) {
                expressionText = expressionText.substring(0, eolIndex);
            }

            CssPropertyValue propVal = new CssPropertyValue(prop, expressionText);

            Collection<Element> alts = propVal.alternatives();

            Collection<Element> filteredByPrefix = filterElements(alts, prefix);

            int completionItemInsertPosition = prefix.trim().length() == 0
                    ? caretOffset
                    : snapshot.getOriginalOffset(node.startOffset());

            //test the situation when completion is invoked just after a valid token
            //like color: rgb|
            //in such case the parser offers ( alternative which is valid
            //so we must not use the prefix for filtering the results out.
            //do that only if the completion is not called in the middle of a text,
            //there must be a whitespace after the caret
            boolean addSpaceBeforeItem = false;
            if (alts.size() > 0 && filteredByPrefix.size() == 0 && Character.isWhitespace(charAfterCaret)) {
                completionItemInsertPosition = caretOffset; //complete on the position of caret
                filteredByPrefix = alts; //prefix is empty, do not filter at all
                addSpaceBeforeItem = true;
            }

            //hack for color: #| completion >>>
            boolean extendedItemsOnly = false;
            if(prefix.equals("#")) {
                completionItemInsertPosition--;
                extendedItemsOnly = true; //do not add any default alternatives items
            }
            //<<<

            return wrapPropertyValues(context,
                    prefix,
                    prop,
                    filteredByPrefix,
                    CompletionItemKind.VALUE,
                    completionItemInsertPosition,
                    false,
                    addSpaceBeforeItem,
                    extendedItemsOnly);


        } else if (node.kind() == CssParserTreeConstants.JJTELEMENTNAME) {
            //complete selector's element name
            List<CompletionProposal> proposals = completeHtmlSelectors(prefix, snapshot.getOriginalOffset(node.startOffset()));
            if (proposals.size() > 0) {
                return new DefaultCompletionResult(proposals, false);
            }
        } else if (node.kind() == CssParserTreeConstants.JJTSELECTORLIST ||
                node.kind() == CssParserTreeConstants.JJTCOMBINATOR ||
                node.kind() == CssParserTreeConstants.JJTSELECTOR) {
            //complete selector list without prefix in selector list e.g. BODY, | { ... }
//            assert prefix.length() == 0;

            List<CompletionProposal> proposals = completeHtmlSelectors(prefix, caretOffset);
            if (proposals.size() > 0) {
                return new DefaultCompletionResult(proposals, false);
            }
        } 

        return CodeCompletionResult.NONE;
    }

    private List<CompletionProposal> completeHtmlSelectors(String prefix, int offset) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(20);
        for (String tagName : HtmlTags.getTags()) {
            if (tagName.startsWith(prefix.toLowerCase(Locale.ENGLISH))) {
                proposals.add(new SelectorCompletionItem(new CssElement(tagName),
                        tagName,
                        CompletionItemKind.VALUE,
                        offset));
            }
        }
        return proposals;

    }

    private CodeCompletionResult wrapRAWValues(Collection<String> props, CompletionItemKind kind, int anchor) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        for (String value : props) {
            CssElement handle = new CssElement(value);
            CompletionProposal proposal = createCompletionItem(handle, value, kind, anchor, false);
            proposals.add(proposal);
        }
        return new DefaultCompletionResult(proposals, false);
    }

    private CodeCompletionResult wrapPropertyValues(CodeCompletionContext context,
            String prefix,
            Property property,
            Collection<Element> props,
            CompletionItemKind kind,
            int anchor,
            boolean addSemicolon,
            boolean addSpaceBeforeItem,
            boolean extendedItemsOnly) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(props.size());
        boolean colorChooserAdded = false;
        for (Element e : props) {
            if (e instanceof PropertyModel.ValueElement) {
                if (((PropertyModel.ValueElement) e).isUnit()) {
                    continue; //skip units
                }
            }
            CssValueElement handle = new CssValueElement(property, e);
            String origin = e.getResolvedOrigin();
            if("color".equals(origin)) { //NOI18N
                if(!colorChooserAdded) {
                    //add color chooser item
                    proposals.add(new ColorChooserItem(anchor, origin, addSemicolon));
                    //add used colors items
                    proposals.addAll(getUsedColorsItems(context, prefix, handle, origin, kind, anchor, addSemicolon, addSpaceBeforeItem));
                    colorChooserAdded = true;
                }
                if(!extendedItemsOnly) {
                    proposals.add(createColorValueCompletionItem(handle, e, kind, anchor, addSemicolon, addSpaceBeforeItem));
                }
            } else {
                if(!extendedItemsOnly) {
                    proposals.add(createValueCompletionItem(handle, e, kind, anchor, addSemicolon, addSpaceBeforeItem));
                }
            }
        }
        return new DefaultCompletionResult(proposals, false);
    }

    private Collection<CompletionProposal> getUsedColorsItems(CodeCompletionContext context, String prefix,
            CssElement element, String origin, CompletionItemKind kind, int anchor, boolean addSemicolon,
            boolean addSpaceBeforeItem) {
        FileObject current = context.getParserResult().getSnapshot().getSource().getFileObject();
        if(current == null) {
            return Collections.emptyList();
        }
        CssProjectSupport support = CssProjectSupport.findFor(current);
        CssIndex index = support.getIndex();
        Map<FileObject, Collection<String>> result = index.findAll(CssIndexer.COLORS_KEY);

        //resort the files collection so the current file it first
        //we need that to ensure the color from current file has precedence
        //over the others
        List<FileObject> resortedKeys = new ArrayList<FileObject>(result.keySet());
        if(resortedKeys.remove(current)) {
            resortedKeys.add(0, current);
        }
        Collection<CompletionProposal> proposals = new HashSet<CompletionProposal>();
        for(FileObject file : resortedKeys) {
            Collection<String> colors = result.get(file);
            boolean usedInCurrentFile = file.equals(current);
            for(String color : colors) {
                if(color.startsWith(prefix)) {
                    proposals.add(new HashColorCompletionItem(element, color, origin,
                            kind, anchor, addSemicolon, addSpaceBeforeItem, usedInCurrentFile));
                }
            }
        }
        return proposals;
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
            if (!p.name().startsWith("-")) { //NOI18N
                CssElement handle = new CssPropertyElement(p);
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

    @Override
    public String document(ParserResult info, ElementHandle element) {
        if (element instanceof CssValueElement) {
            CssValueElement e = (CssValueElement) element;

//            System.out.println("property = " + e.property().name() + "\n value = " + e.value());
            return CssHelpResolver.instance().getPropertyHelp(e.property().name());

        } else if (element instanceof CssPropertyElement) {
            CssPropertyElement e = (CssPropertyElement) element;
//            System.out.println("property = " + e.property().name());
            return CssHelpResolver.instance().getPropertyHelp(e.property().name());
        }
        // fix for #137696
        else if ( element instanceof ElementHandle.UrlHandle){
            try {
                return CssHelpResolver.instance().getHelpText(new URL(element.getName()));
            }
            catch( MalformedURLException e ){
                assert false;
            }
        }
        return null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle elementHandle) {
        return CssHelpResolver.getHelpZIPURLasString() == null ? null :
            new ElementHandle.UrlHandle(CssHelpResolver.getHelpZIPURLasString() +
                    normalizeLink( elementHandle, link));
    }

    @Override
    public String getPrefix(ParserResult info, final int caretOffset, boolean upToOffset) {
        Snapshot snapshot = info.getSnapshot();
        TokenHierarchy hi = snapshot.getTokenHierarchy();
        String prefix = getPrefix(hi.tokenSequence(), snapshot.getEmbeddedOffset(caretOffset));

        //really ugly handling of class or id selector prefix:
        //Since the getPrefix() method is parser result based it is supposed
        //to work on top of the snapshot, while GsfCompletionProvider$Task.canFilter()
        //should be fast and hence operates on document, there arises a big contradiction -
        //For the virtually generated class and id selectors, the dot or hash chars
        //are part of the virtual source and hence becomes a part of the prefix in
        //this method call, while in the real html document they are invisible and an
        //attribute quote resides on their place.
        //So if a GsfCompletionProvider$CompletionEnvironment is created, an anchor
        //is computed from the caret offset and prefix lenght (prefix returned from
        //this method). After subsequent user's keystrokes the canFilter() method
        //gets text from this anchor to the caret from the edited document! So the
        //prefix contains the attribute quotation and any css items cannot be filtered.

        //this is a poor and hacky solution to this issue, some bug may appear for
        //non class or id elements starting with dot or hash?!?!?

        if(prefix.length() > 0 && (prefix.charAt(0) == '.' || prefix.charAt(0) == '#')) {
            firstPrefixChar = prefix.charAt(0);
            return prefix.substring(1);
        } else {
            firstPrefixChar = 0;
            return prefix;
        }

    }
    
    private String normalizeLink(ElementHandle handle , String link){
        if ( link.startsWith("." )|| link.startsWith("/" )){ // NOI18N
            return normalizeLink(handle, link.substring( 1 ));
        }
        int index = link.lastIndexOf('#');
        if ( index !=-1 ){
            if ( index ==0 || link.charAt(index-1) =='/'){
                String helpZipUrl = CssHelpResolver.getHelpZIPURL().getPath();
                if ( handle instanceof CssPropertyElement ){
                    String name = ((CssPropertyElement)handle).property().name();
                    URL propertyHelpURL = CssHelpResolver.instance().
                        getPropertyHelpURL(name);
                    String path = propertyHelpURL.getPath();
                    if ( path.startsWith( helpZipUrl )){
                        path = path.substring(helpZipUrl.length());
                    }
                    return path+link.substring( index );
                }
                else if (handle instanceof UrlHandle){
                    String url = handle.getName();
                    int anchorIndex = url.lastIndexOf('#');
                    if ( anchorIndex!= -1 ){
                        url = url.substring( 0, anchorIndex);
                    }
                    //"normalize" the URL - use just the "path" part
                    try {
                        URL _url = new URL(url);
                        url = _url.getPath();
                    } catch(MalformedURLException mue) {
                        Logger.getLogger("global").log(Level.INFO, null, mue);
                    }

                    if ( url.startsWith( helpZipUrl)){
                        url = url.substring(helpZipUrl.length());
                    }
                    return url+link.substring( index );
                }
            }
        }
        return link;
    }

    private String getPrefix(TokenSequence<CssTokenId> ts, int caretOffset) {
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

        if (t.id() == CssTokenId.COLON) {
            return ""; //NOI18N
        } else {
            return t.text().subSequence(0, diff == 0 ? t.text().length() : diff).toString().trim();
        }
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        int offset = component.getCaretPosition();
        if (typedText == null || typedText.length() == 0) {
            return QueryType.NONE;
        }
        char c = typedText.charAt(typedText.length() - 1);

        TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(component.getDocument(), offset);
        if (ts != null) {
            int diff = ts.move(offset);
            TokenId currentTokenId = null;
            if (ts != null) {
                if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                    currentTokenId = ts.token().id();
                }
            }

            if (currentTokenId == CssTokenId.IDENT) {
                return QueryType.COMPLETION;
            }

            //#177306  Eager CSS CC
            //
            //open completion when a space is pressed, but only
            //if typed by user by pressing the spacebar.
            //
            //1) filters out tabs which are converted to spaces
            //   before being put into the document
            //2) filters out newline keystrokes which causes the indentation
            //   to put some spaces on the newline
            //3) filters out typing spaces in comments
            //
            if (typedText.length() == 1 && c == ' ' && currentTokenId != CssTokenId.COMMENT) {
                return QueryType.COMPLETION;
            }
        }

        switch (c) {
            case '\n':
            case '}':
            case ';': {
                return QueryType.STOP;
            }
            case '.':
            case '#':
            case ':':
            case ',': {
                return QueryType.COMPLETION;
            }
        }
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return ""; //NOI18N
    }

    @Override
    public Set<String> getApplicableTemplates(ParserResult info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    private static enum CompletionItemKind {

        PROPERTY, VALUE;
    }

    private CssCompletionItem createValueCompletionItem(CssValueElement element,
            Element value,
            CompletionItemKind kind,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ValueCompletionItem(element, value.toString(), value.getResolvedOrigin(), kind, anchorOffset, addSemicolon, addSpaceBeforeItem);
    }

    private CssCompletionItem createColorValueCompletionItem(CssValueElement element,
            Element value,
            CompletionItemKind kind,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ColorCompletionItem(element, value.toString(), value.getResolvedOrigin(), kind, anchorOffset, addSemicolon, addSpaceBeforeItem);

    }

    private CssCompletionItem createPropertyNameCompletionItem(CssElement element,
            String value,
            CompletionItemKind kind,
            int anchorOffset,
            boolean addSemicolon) {

        return new PropertyCompletionItem(element, value, kind, anchorOffset, addSemicolon);
    }

    private CssCompletionItem createCompletionItem(CssElement element,
            String value,
            CompletionItemKind kind,
            int anchorOffset,
            boolean addSemicolon) {

        return new CssCompletionItem(element, value, kind, anchorOffset, addSemicolon);
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

    private class ValueCompletionItem extends CssCompletionItem {

        private String origin; //property name to which this value belongs
        private boolean addSpaceBeforeItem;

        private ValueCompletionItem(CssElement element,
                String value,
                String origin,
                CompletionItemKind kind,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem) {

            super(element, value, kind, anchorOffset, addSemicolon);
            this.origin = origin;
            this.addSpaceBeforeItem = addSpaceBeforeItem;
        }

        @Override
        public String getInsertPrefix() {
            return (addSpaceBeforeItem && textsStartsWith(getName()) ? " " : "") + getName() + (addSemicolon ? ";" : ""); //NOI18N
        }

        private boolean textsStartsWith(String text) {
            char ch = text.charAt(0);
            return Character.isLetterOrDigit(ch);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + origin + "</font>"; //NOI18N
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            Property owningProperty = ((CssValueElement) getElement()).property();
            String initialValue = owningProperty.initialValue();
            if (initialValue != null && initialValue.equals(getName())) {
                //initial value
                return "<i>" + super.getLhsHtml(formatter) + "</i>"; //NOI18N
            }

            return super.getLhsHtml(formatter);
        }
    }

    private static final byte COLOR_ICON_SIZE = 16; //px
    private static final byte COLOR_RECT_SIZE = 10; //px

    //XXX fix the CssCompletionItem class so the Value and Property normally subclass it!!!!!!!!!
    private class ColorCompletionItem extends ValueCompletionItem {


        private ColorCompletionItem(CssElement element,
                String value,
                String origin,
                CompletionItemKind kind,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem) {

            super(element, value, origin, kind, anchorOffset, addSemicolon, addSpaceBeforeItem);
        }

        @Override
        public ImageIcon getIcon() {
            return createIcon(colors().get(getName()));
        }
    }

    private class HashColorCompletionItem extends ColorCompletionItem {

        private boolean usedInCurrentFile;

        private HashColorCompletionItem(CssElement element,
                String value,
                String origin,
                CompletionItemKind kind,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem,
                boolean usedInCurrentFile) {

            super(element, value, origin, kind, anchorOffset, addSemicolon, addSpaceBeforeItem);
            this.usedInCurrentFile = usedInCurrentFile;
        }

        @Override
        public ImageIcon getIcon() {
            return createIcon(getName().substring(1)); //strip off the hash
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return new StringBuilder().append(usedInCurrentFile ? "" : "<font color=999999>").
                    append(getName()).append(usedInCurrentFile ? "" : "</font>").toString();
        }

        @Override
        public int getSortPrioOverride() {
            return super.getSortPrioOverride() + (usedInCurrentFile ? 1 : 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HashColorCompletionItem other = (HashColorCompletionItem) obj;

            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            return hash;
        }

        


    }

    private static final JColorChooser COLOR_CHOOSER = new JColorChooser();

    private static ImageIcon createIcon(String colorCode) {
        BufferedImage i = new BufferedImage(COLOR_ICON_SIZE, COLOR_ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g = i.createGraphics();

            boolean defaultIcon = colorCode == null;
            if (defaultIcon) {
                //unknown color code, we still want a generic icon
                colorCode = "ffffff"; //NOI18N
            }

            Color transparent = new Color(0x00ffffff, true);
            g.setColor(transparent);
            g.fillRect(0, 0, COLOR_ICON_SIZE, COLOR_ICON_SIZE);

            g.setColor(Color.decode("0x" + colorCode)); //NOI18N
            g.fillRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE,
                    COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                    COLOR_RECT_SIZE - 1,
                    COLOR_RECT_SIZE - 1);

            g.setColor(Color.DARK_GRAY);
            g.drawRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                    COLOR_ICON_SIZE - COLOR_RECT_SIZE - 2,
                    COLOR_RECT_SIZE,
                    COLOR_RECT_SIZE);

            if (defaultIcon) {
                //draw the X inside the icon
                g.drawLine(COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                        COLOR_ICON_SIZE - 2,
                        COLOR_ICON_SIZE - 1,
                        COLOR_ICON_SIZE - COLOR_RECT_SIZE - 2);
            }

            return new ImageIcon(i);
    }

    private class ColorChooserItem extends DefaultCompletionProposal {

        private Color color;
        private boolean addSemicolon;
        private String origin;

        public ColorChooserItem(int anchor, String origin, boolean addSemicolon) {
            this.anchorOffset = anchor;
            this.addSemicolon = addSemicolon;
            this.origin = origin;
        }

        @Override
        public boolean beforeDefaultAction() {
            JDialog dialog = JColorChooser.createDialog(EditorRegistry.lastFocusedComponent(), 
                    NbBundle.getMessage(CssCompletion.class, "MSG_Choose_Color"), //NOI18N
                    true, COLOR_CHOOSER, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    color = COLOR_CHOOSER.getColor();
                }
            }, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    color = null;
                }
            });
            dialog.setVisible(true);
            dialog.dispose();

            return color == null;
        }

        @Override
        public int getAnchorOffset() {
            return anchorOffset;
        }

        @Override
        public ElementHandle getElement() {
            return new CssElement(null);
        }

        @Override
        public ElementKind getKind() {
            return getElement().getKind();
        }

        @Override
        public ImageIcon getIcon() {
            Color c = COLOR_CHOOSER.getColor();
            String colorCode = c == null ? "ffffff" : WebUtils.toHexCode(c).substring(1); //strip off the hash
            return createIcon(colorCode);
        }

        @Override
        public String getName() {
            return color == null ? "" : (WebUtils.toHexCode(color) + (addSemicolon ? ";" : "")); //NOI18N
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return "<b>"+ NbBundle.getMessage(CssCompletion.class, "MSG_OpenColorChooser") +"</b>"; //NOI18N
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + origin + "</font>"; //NOI18N
        }

        @Override
        public boolean isSmart() {
            return true;
        }

    }

    private class PropertyCompletionItem extends CssCompletionItem {

        private PropertyCompletionItem(CssElement element,
                String value,
                CompletionItemKind kind,
                int anchorOffset,
                boolean addSemicolon) {

            super(element, value, kind, anchorOffset, addSemicolon);
        }

        @Override
        public String getInsertPrefix() {
            return super.getInsertPrefix() + ": "; //NOI18N
        }
    }

    private class SelectorCompletionItem extends CssCompletionItem {

        private boolean related;

        private SelectorCompletionItem(CssElement element,
                String value,
                CompletionItemKind kind,
                int anchorOffset) {
            this(element, value, kind, anchorOffset, true);
        }

        private SelectorCompletionItem(CssElement element,
                String value,
                CompletionItemKind kind,
                int anchorOffset,
                boolean related) {
            super(element, value, kind, anchorOffset, false);
            this.related = related;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            StringBuilder buf = new StringBuilder();
            if(related) {
                buf.append("<b><font color=#");
                buf.append(RELATED_SELECTOR_COLOR);
            } else {
                buf.append("<font color=#");
                buf.append(GRAY_COLOR_CODE);
            }
            buf.append(">");
            buf.append(getName());
            buf.append("</font>");
            if(related) {
                buf.append("</b>");
            }

            formatter.appendHtml(buf.toString());
            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            return null;
        }

        @Override
        public int getSortPrioOverride() {
            return super.getSortPrioOverride() + (related ? 1 : 0);
        }



    }

    /**
     * @todo support for more completion type providers - like colors => subclass this class, remove the kind field, it's just temp. hack
     * 
     */
    private class CssCompletionItem implements CompletionProposal {

        private int anchorOffset;
        private String value;
        protected CompletionItemKind kind;
        private CssElement element;
        protected boolean addSemicolon;

        private CssCompletionItem() {
        }

        private CssCompletionItem(
                CssElement element, String value, CompletionItemKind kind, int anchorOffset, boolean addSemicolon) {
            this.anchorOffset = anchorOffset;
            this.value = value;
            this.kind = kind;
            this.element = element;
            this.addSemicolon = addSemicolon;
        }

        @Override
        public int getAnchorOffset() {
            return anchorOffset;
        }

        @Override
        public String getName() {
            return value;
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getSortText() {
            return getName();
        }

        @Override
        public ImageIcon getIcon() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            switch (kind) {
                case PROPERTY:
                    return ElementKind.METHOD;
                case VALUE:
                    return ElementKind.FIELD;
                default:
                    return ElementKind.OTHER;
            }
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.appendText(getName());
            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public boolean isSmart() {
            return false;
        }

        @Override
        public String getCustomInsertTemplate() {
            return null;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public int getSortPrioOverride() {
            return 0;
        }
    }
}
