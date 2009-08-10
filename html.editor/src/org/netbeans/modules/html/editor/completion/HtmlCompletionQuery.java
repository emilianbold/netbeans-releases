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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.html.editor.completion;

import java.util.*;
import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.editor.ext.html.dtd.*;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.html.editor.gsf.api.HtmlExtension;
import org.netbeans.modules.html.editor.gsf.api.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 * Html completion results finder
 *
 * @author Marek Fukala
 * @author Petr Nejedly
 *
 * @version 2.0
 */
public class HtmlCompletionQuery extends UserTask {

    private static final String SCRIPT_TAG_NAME = "SCRIPT"; //NOI18N
    private static final String STYLE_TAG_NAME = "STYLE"; //NOI18N
    private static boolean lowerCase;
    private static boolean isXHtml = false;
    private Document document;
    private int offset;
    private CompletionResult completionResult;

    public HtmlCompletionQuery(Document document, int offset) {
        this.document = document;
        this.offset = offset;
    }

    public CompletionResult query() throws ParseException {
        Source source = Source.create(document);
        ParserManager.parse(Collections.singleton(source), this);

        return this.completionResult;
    }

    @Override
    public void run(ResultIterator resultIterator) throws Exception {
        Parser.Result parserResult = resultIterator.getParserResult(offset);
        if (parserResult == null) {
            return;
        }
        String resultMimeType = parserResult.getSnapshot().getMimeType();
        if (resultMimeType.equals("text/html")) {
            //proceed only on html content
            this.completionResult = query((HtmlParserResult) parserResult);
        }
    }

    private CompletionResult query(HtmlParserResult result) {
        return query(result, result.dtd());
    }

    //for unit tests, allows to use different DTD than specified in the parser result
    CompletionResult query(HtmlParserResult parserResult, DTD dtd) {

        Snapshot snapshot = parserResult.getSnapshot();
        int astOffset = snapshot.getEmbeddedOffset(offset);
        lowerCase = usesLowerCase(parserResult, astOffset);
        isXHtml = parserResult.getHtmlVersion().isXhtml();

        TokenHierarchy hi = snapshot.getTokenHierarchy();
        TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
        assert ts != null; //should be ensured by the parsing.api that we always get html token sequence from the snapshot

        int diff = ts.move(astOffset);
        if (ts.moveNext()) {
            if (diff == 0 && (ts.token().id() == HTMLTokenId.TEXT || ts.token().id() == HTMLTokenId.WS ||
                    ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL)) {
                //looks like we are on a boundary of a text or whitespace, need the previous token
                //or we are just before tag closing symbol
                if (!ts.movePrevious()) {
                    //we cannot get previous token
                    return null;
                }
            }
        } else {
            if (!ts.movePrevious()) {
                //can't get previous token
                return null;
            }
        }

        int anchor = -1;

        //get text before cursor
        Token item = ts.token();
        int itemOffset = item.offset(hi);
        int documentItemOffset = snapshot.getOriginalOffset(itemOffset);
        String preText = item.text().toString();
        String itemText = preText;
        if (diff < preText.length()) {
            preText = preText.substring(0, astOffset - itemOffset);
        }
        TokenId id = item.id();
        boolean inside = ts.offset() < astOffset; // are we inside token or between tokens?

        Collection<CompletionItem> result = null;
        int len = 1;

        //adjust the astOffset if at the end of the file
        int searchAstOffset = astOffset == snapshot.getText().length() ? astOffset - 1 : astOffset;

        AstNode node = parserResult.findLeaf(searchAstOffset);
        if(node == null) {
            return null;
        }
        AstNode root = node.getRootNode();

        //namespace is null for html content
        String namespace = (String) root.getProperty(AstNode.NAMESPACE_PROPERTY);

        /* Character reference finder */
        int ampIndex = preText.lastIndexOf('&'); //NOI18N
        if ((id == HTMLTokenId.TEXT || id == HTMLTokenId.VALUE) && ampIndex > -1) {
            //complete character references
            String refNamePrefix = preText.substring(ampIndex + 1);
            anchor = offset;
            result = translateCharRefs(offset - len, dtd.getCharRefList(refNamePrefix));

        } else if (id == HTMLTokenId.CHARACTER) {
            //complete character references
            if (inside || !preText.endsWith(";")) { // NOI18N
                anchor = documentItemOffset + 1; //plus "&" length
                result = translateCharRefs(documentItemOffset, dtd.getCharRefList(preText.substring(1)));
            }
        } else if (id == HTMLTokenId.TAG_OPEN) { // NOI18N
            //complete open tags with prefix
            anchor = documentItemOffset;
            //we are inside a tagname, the real content is the position before the tag
            astOffset -= (preText.length() + 1); // +"<" len

            result = new ArrayList<CompletionItem>();

            if (namespace == null) {
                Collection<DTD.Element> openTags = AstNodeUtils.getPossibleOpenTagElements(root, astOffset);

                result.addAll(translateTags(documentItemOffset - 1,
                        filterElements(openTags, preText),
                        filterElements(dtd.getElementList(null),
                        preText)));
            }
                
            //extensions
            HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, documentItemOffset - 1, preText, itemText);
            for (HtmlExtension e : HtmlExtension.getRegisteredExtensions()) {
                result.addAll(e.completeOpenTags(context));
            }


        } else if (id != HTMLTokenId.BLOCK_COMMENT && preText.endsWith("<")) { // NOI18N
            //complete open tags with no prefix
            anchor = offset;
            result = new ArrayList<CompletionItem>();

            if (namespace == null) {
                Collection<DTD.Element> openTags = AstNodeUtils.getPossibleOpenTagElements(root, astOffset);
                result.addAll(translateTags(offset - 1, openTags, dtd.getElementList(null)));
            }
            
            //extensions
            HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, offset - 1, "", "");
            for (HtmlExtension e : HtmlExtension.getRegisteredExtensions()) {
                Collection<CompletionItem> items = e.completeOpenTags(context);
                result.addAll(items);
            }


        } else if ((id == HTMLTokenId.TEXT && preText.endsWith("</")) ||
                (id == HTMLTokenId.TAG_OPEN_SYMBOL && preText.endsWith("</"))) { // NOI18N
            //complete end tags without prefix
            anchor = offset;
            result = getPossibleEndTags(node, offset, "");

        } else if (id == HTMLTokenId.TAG_CLOSE) { // NOI18N
            //complete end tags with prefix
            anchor = documentItemOffset;
            result = getPossibleEndTags(node, offset, preText);

        } else if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
            anchor = offset;
            result = getAutocompletedEndTag(node, astOffset, offset);
        } else if (id == HTMLTokenId.WS || id == HTMLTokenId.ARGUMENT) {
            /*Argument finder */
            String prefix = (id == HTMLTokenId.ARGUMENT) ? preText : "";
            len = prefix.length();
            anchor = offset - len;

            if (namespace != null) {
                //extensions
                Collection<CompletionItem> items = new ArrayList<CompletionItem>();
                HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, anchor, prefix, itemText, node);
                for (HtmlExtension e : HtmlExtension.getRegisteredExtensions()) {
                    items.addAll(e.completeAttributes(context));
                }
                result = items;
            } else {
                if (node.type() == AstNode.NodeType.UNKNOWN_TAG) {
                    //nothing to complete in an unknown tag
                    return null;
                }
                //should be open tag if not unknown
                assert node.type() == AstNode.NodeType.OPEN_TAG : "Unexpecet node type " + node.type();

                DTD.Element tag = node.getDTDElement();
                List possible = tag.getAttributeList(prefix); // All attribs of given tag
                Collection<String> existingAttrsNames = node.getAttributeKeys();

                String wordAtCursor = (item == null) ? null : item.text().toString();
                // #BUGFIX 25261 because of being at the end of document the
                // wordAtCursor must be checked for null to prevent NPE
                // below
                if (wordAtCursor == null) {
                    wordAtCursor = "";
                }

                List<DTD.Attribute> attribs = new ArrayList<DTD.Attribute>();
                for (Iterator i = possible.iterator(); i.hasNext();) {
                    DTD.Attribute attr = (DTD.Attribute) i.next();
                    String aName = attr.getName();
                    if (aName.equals(prefix) ||
                            (!existingAttrsNames.contains(isXHtml ? aName : aName.toUpperCase()) &&
                            !existingAttrsNames.contains(isXHtml ? aName : aName.toLowerCase(Locale.ENGLISH))) || (wordAtCursor.equals(aName) && prefix.length() > 0)) {
                        attribs.add(attr);
                    }
                }

                result = translateAttribs(anchor, attribs, tag);
            }


        } else if (id == HTMLTokenId.VALUE || id == HTMLTokenId.OPERATOR || id == HTMLTokenId.WS) {
            /* Value finder */
            if (id == HTMLTokenId.WS) {
                //is the token before an operator? '<div color= |red>'
                ts.move(item.offset(hi));
                ts.movePrevious();
                Token t = ts.token();
                if (t.id() != HTMLTokenId.OPERATOR) {
                    return null;
                }
            }

            if (node.type() == AstNode.NodeType.OPEN_TAG) {
                DTD.Element tag = node.getDTDElement();
                if (tag == null) {
                    return null; // unknown tag
                    }

                ts.move(item.offset(hi));
                ts.moveNext();
                Token argItem = ts.token();
                while (argItem.id() != HTMLTokenId.ARGUMENT && ts.movePrevious()) {
                    argItem = ts.token();
                }

                if (argItem.id() != HTMLTokenId.ARGUMENT) {
                    return null; // no ArgItem
                    }
                String argName = argItem.text().toString();
                if (!isXHtml) {
                    argName = argName.toLowerCase(Locale.ENGLISH);
                }

                DTD.Attribute arg = tag.getAttribute(argName);
                if (arg == null /*|| arg.getType() != DTD.Attribute.TYPE_SET*/) {
                    return null;
                }

                result = new ArrayList<CompletionItem>();

                if (id != HTMLTokenId.VALUE) {
                    anchor = offset;
                    result.addAll(translateValues(anchor, arg.getValueList("")));
                    AttrValuesCompletion valuesCompletion = AttrValuesCompletion.getSupport(node.name(), argName);
                    if (valuesCompletion != null) {
                        result.addAll(valuesCompletion.getValueCompletionItems(document, offset, ""));
                    }
                } else {
                    String quotationChar = null;
                    if (preText != null && preText.length() > 0) {
                        if (preText.substring(0, 1).equals("'")) {
                            quotationChar = "'"; // NOI18N
                            }
                        if (preText.substring(0, 1).equals("\"")) {
                            quotationChar = "\""; // NOI18N
                            }
                    }
                    String prefix = quotationChar == null ? preText : preText.substring(1);

                    anchor = documentItemOffset + (quotationChar != null ? 1 : 0);

                    result.addAll(translateValues(documentItemOffset, arg.getValueList(prefix), quotationChar));
                    AttrValuesCompletion valuesCompletion = AttrValuesCompletion.getSupport(node.name(), argName);
                    if (valuesCompletion != null) {
                        result.addAll(valuesCompletion.getValueCompletionItems(document, offset, prefix));
                    }
                }
            }
        } else if (id == HTMLTokenId.SCRIPT) {
            result = addEndTag(SCRIPT_TAG_NAME, preText, offset);
        } else if (id == HTMLTokenId.STYLE) {
            result = addEndTag(STYLE_TAG_NAME, preText, offset);
        }

        return result == null ? null : new CompletionResult(result, anchor);

    }

    

    private boolean usesLowerCase(HtmlParserResult result, int astOffset) {
        //find first open tag for the given offset and check its name case
        AstNode node = AstNodeUtils.getTagNode(result.root(), astOffset);
        return node != null ? Character.isLowerCase(node.name().charAt(0)) : true;
    }

    private List<CompletionItem> addEndTag(String tagName, String preText, int offset) {
        int commonLength = getLastCommonCharIndex("</" + tagName + ">", isXHtml ? preText.trim() : preText.toUpperCase().trim()); //NOI18N
        if (commonLength == -1) {
            commonLength = 0;
        }
        if (commonLength == preText.trim().length()) {
            tagName = isXHtml ? tagName : (lowerCase ? tagName.toLowerCase(Locale.ENGLISH) : tagName);
            return Collections.singletonList((CompletionItem) HtmlCompletionItem.createEndTag(tagName, offset - commonLength, null, -1, HtmlCompletionItem.EndTag.Type.DEFAULT));
        }
        return null;
    }

    public List<CompletionItem> getAutocompletedEndTag(AstNode node, int astOffset, int documentOffset) {
        //check for open tags only
        //the test node.endOffset() == astOffset is required since the given node
        //is the most leaf OPEN TAG node for the position. But if there is some
        //unresolved (no-DTD) node at the position it would autocomplete the open
        //tag: <div> <bla>| + ACC would complete </div>
        if (node.type() == AstNode.NodeType.OPEN_TAG && node.endOffset() == astOffset) {
            //I do not check if the tag is closed already since
            //when more tags of the same type are nested,
            //the matches can be created so the current node
            //appear to be matched even if the user just typed it

            //test if the tag is an empty tag <div/> and whether the open tag has forbidden end tag
            if (!node.isEmpty() && !AstNodeUtils.hasForbiddenEndTag(node)) {
                return Collections.singletonList((CompletionItem) HtmlCompletionItem.createAutocompleteEndTag(node.name(), documentOffset));
            }
        }
        return Collections.emptyList();
    }

    private int getLastCommonCharIndex(String base, String pattern) {
        int i = 0;
        for (; i < base.length() && i < pattern.length(); i++) {
            if (base.charAt(i) != pattern.charAt(i)) {
                i--;
                break;
            }
        }
        return i;
    }

    private List<CompletionItem> translateCharRefs(int offset, List refs) {
        List result = new ArrayList(refs.size());
        String name;
        for (Iterator i = refs.iterator(); i.hasNext();) {
            DTD.CharRef chr = (DTD.CharRef) i.next();
            name = chr.getName();
            result.add(HtmlCompletionItem.createCharacterReference(name, chr.getValue(), offset, name));
        }
        return result;
    }

    private List<CompletionItem> getPossibleEndTags(AstNode leaf, int offset, String prefix) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        int order = 0;
        for (;;) {
            if (leaf.type() == AstNode.NodeType.ROOT) {
                break;
            }
            //if dtd element and doesn't have forbidden end tag
            if ((leaf.getDTDElement() == null || !AstNodeUtils.hasForbiddenEndTag(leaf)) &&
                    leaf.type() == AstNode.NodeType.OPEN_TAG) {
                String tagName = leaf.name();
                if (tagName.startsWith(prefix.toLowerCase(Locale.ENGLISH))) {
                    //TODO - distinguish unmatched and matched tags in the completion!!!
                    //TODO - mark required and optional end tags somehow
                    items.add(HtmlCompletionItem.createEndTag(tagName, offset - 2 - prefix.length(), tagName, order++, getEndTagType(leaf)));
                }
            }
            leaf = leaf.parent();

            assert leaf != null;

        }

        return items;
    }

    private HtmlCompletionItem.EndTag.Type getEndTagType(AstNode leaf) {
        if (leaf.getMatchingTag() != null) {
            //matched
            return leaf.needsToHaveMatchingTag() ? HtmlCompletionItem.EndTag.Type.REQUIRED_EXISTING : HtmlCompletionItem.EndTag.Type.OPTIONAL_EXISTING;
        } else {
            //unmatched
            return leaf.needsToHaveMatchingTag() ? HtmlCompletionItem.EndTag.Type.REQUIRED_MISSING : HtmlCompletionItem.EndTag.Type.OPTIONAL_MISSING;
        }

    }

    private Collection<DTD.Element> filterElements(Collection<DTD.Element> elements, String elementNamePrefix) {
        List<DTD.Element> filtered = new ArrayList<DTD.Element>();
        elementNamePrefix = elementNamePrefix.toLowerCase(Locale.ENGLISH);
        for (DTD.Element e : elements) {
            if (e.getName().toLowerCase(Locale.ENGLISH).startsWith(elementNamePrefix)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    List<CompletionItem> translateTags(int offset, Collection<DTD.Element> possible, Collection<DTD.Element> all) {
        List<CompletionItem> result = new ArrayList<CompletionItem>(all.size());
        all.removeAll(possible); //remove possible elements
        for (DTD.Element e : possible) {
            result.add(item4Element(e, offset, true));
        }
        for (DTD.Element e : all) {
            result.add(item4Element(e, offset, false));
        }
        return result;
    }

    private HtmlCompletionItem item4Element(DTD.Element e, int offset, boolean possible) {
        String name = e.getName();
        name = isXHtml ? name : (lowerCase ? name.toLowerCase(Locale.ENGLISH) : name.toUpperCase());
        return HtmlCompletionItem.createTag(name, offset, name, possible);
    }

    List<CompletionItem> translateAttribs(int offset, List<DTD.Attribute> attribs, DTD.Element tag) {
        List<CompletionItem> result = new ArrayList<CompletionItem>(attribs.size());
        String tagName = tag.getName() + "#"; // NOI18N
        for (DTD.Attribute attrib : attribs) {
            String name = attrib.getName();
            switch (attrib.getType()) {
                case DTD.Attribute.TYPE_BOOLEAN:
                    result.add(HtmlCompletionItem.createBooleanAttribute(name, offset, attrib.isRequired(), tagName + name));
                    break;
                case DTD.Attribute.TYPE_SET:
                case DTD.Attribute.TYPE_BASE:
                    result.add(HtmlCompletionItem.createAttribute(name, offset, attrib.isRequired(), tagName + name));
                    break;
            }
        }
        return result;
    }

    List<HtmlCompletionItem> translateValues(int offset, List values) {
        return translateValues(offset, values, null);
    }

    List<HtmlCompletionItem> translateValues(int offset, List values, String quotationChar) {
        if (values == null) {
            return new ArrayList(0);
        }
        List result = new ArrayList(values.size());
        if (quotationChar != null) {
            offset++; //shift the offset after the quotation
        }
        for (Iterator i = values.iterator(); i.hasNext();) {
            result.add(HtmlCompletionItem.createAttributeValue(((DTD.Value) i.next()).getName(), offset));
        }
        return result;
    }

    public static class CompletionResult {

        private Collection<CompletionItem> items;
        int anchor;

        CompletionResult(Collection<CompletionItem> items, int anchor) {
            this.items = items;
            this.anchor = anchor;
        }

        public int getAnchor() {
            return anchor;
        }

        public Collection<CompletionItem> getItems() {
            return items;
        }
    }
}

