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

import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import java.util.*;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.editor.ext.html.dtd.*;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.html.editor.HtmlPreferences;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
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

    private static final String SCRIPT_TAG_NAME = "script"; //NOI18N
    private static final String STYLE_TAG_NAME = "style"; //NOI18N
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
        Snapshot snapshot = parserResult.getSnapshot();
        int embeddedOffset = snapshot.getEmbeddedOffset(offset);
        String resultMimeType = parserResult.getSnapshot().getMimeType();
        if (resultMimeType.equals("text/html")) {
            //proceed only on html content
            this.completionResult = query((HtmlParserResult) parserResult);
        } else if(resultMimeType.equals("text/javascript")) {
            //complete the </script> end tag
            this.completionResult = queryHtmlEndTagInEmbeddedCode(snapshot, embeddedOffset, SCRIPT_TAG_NAME);
        } else if(resultMimeType.equals("text/x-css")) {
            //complete the </style> end tag
            this.completionResult = queryHtmlEndTagInEmbeddedCode(snapshot, embeddedOffset, STYLE_TAG_NAME);
        }
    }

    private CompletionResult queryHtmlEndTagInEmbeddedCode(final Snapshot snapshot, final int embeddedOffset, final String endTagName) {
        // End tag autocompletion support
        // We want the end tag autocompletion to appear just after <style> and <script> tags.
        // Since there is css language as leaf languge, this needs to be treated separately.
        final Document doc = snapshot.getSource().getDocument(false);
        if(doc != null) {
            final AtomicReference<CompletionResult> result = new AtomicReference<CompletionResult>();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    int documentItemOffset = snapshot.getOriginalOffset(embeddedOffset);
                    TokenSequence ts = Utils.getJoinedHtmlSequence(doc, documentItemOffset - 1);
                    if(ts != null)  {
                        if(ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL && CharSequenceUtilities.equals(ts.token().text(), ">")) {
                            Token openTagToken = Utils.findTagOpenToken(ts);
                            if (openTagToken != null && CharSequenceUtilities.equals(openTagToken.text(), endTagName)) {

                                List<? extends CompletionItem> items = Collections.singletonList(
                                        HtmlCompletionItem.createAutocompleteEndTag(endTagName, documentItemOffset));
                                result.set(new CompletionResult(items, offset));
                            }
                        }
                    }
                }
            });
            if(result.get() != null) {
                return result.get();
            }

        }

        String expectedCode = "</" + endTagName;
        // Common end tag completion

        //get searched area before caret size
        int patternSize = Math.max(embeddedOffset, embeddedOffset - expectedCode.length());

        CharSequence pattern = snapshot.getText().subSequence(embeddedOffset - patternSize, embeddedOffset);

        //find < in the pattern
        int ltIndex = CharSequenceUtilities.lastIndexOf(pattern, '<');
        if(ltIndex == -1) {
            //no acceptable prefix
            return null;
        }

        boolean match = true;
        //now compare the pattern with the expected text
        for(int i = ltIndex; i < pattern.length(); i++) {
            if(pattern.charAt(i) != expectedCode.charAt(i - ltIndex)) {
                match = false;
                break;
            }
        }

        if(match) {
            int itemOffset = embeddedOffset - patternSize + ltIndex;

            //convert back to document offsets
            int documentItemOffset = snapshot.getOriginalOffset(itemOffset);

            List<? extends CompletionItem> items = Collections.singletonList(HtmlCompletionItem.createEndTag(endTagName, documentItemOffset, null, -1, HtmlCompletionItem.EndTag.Type.DEFAULT));
            return new CompletionResult(items, offset);
        }

        return null;
    }

    private CompletionResult query(HtmlParserResult result) {
        return query(result, result.dtd());
    }

    //for unit tests, allows to use different DTD than specified in the parser result
    CompletionResult query(HtmlParserResult parserResult, DTD dtd) {

        Snapshot snapshot = parserResult.getSnapshot();
        String sourceMimetype = snapshot.getSource().getMimeType();
        int astOffset = snapshot.getEmbeddedOffset(offset);
        lowerCase = usesLowerCase(parserResult, astOffset);
        isXHtml = parserResult.getHtmlVersion().isXhtml();

        TokenHierarchy<?> hi = snapshot.getTokenHierarchy();
        TokenSequence<HTMLTokenId> ts = hi.tokenSequence(HTMLTokenId.language());
        assert ts != null; //should be ensured by the parsing.api that we always get html token sequence from the snapshot

        int diff = ts.move(astOffset);
        boolean backward = false;
        if (ts.moveNext()) {
            if (diff == 0 && (ts.token().id() == HTMLTokenId.TEXT || ts.token().id() == HTMLTokenId.WS ||
                    ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL || ts.token().id() == HTMLTokenId.TAG_OPEN_SYMBOL)) {
                //looks like we are on a boundary of a text or whitespace, need the previous token
                //or we are just before tag closing symbol
                backward = true;
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
        Token<HTMLTokenId> item = ts.token();
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

        AstNode node = parserResult.findLeaf(searchAstOffset, backward);
        if (node == null) {
            return null;
        }
        AstNode root = node.getRootNode();

        //namespace is null for html content
        String namespace = (String) root.getProperty(AstNode.NAMESPACE_PROPERTY);
        boolean queryHtmlContent = namespace == null || namespace.equals(parserResult.getHtmlVersion().getDefaultNamespace());

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
                result = translateCharRefs(documentItemOffset, dtd.getCharRefList(preText.length() > 0 ? preText.substring(1) : ""));
            }
        } else if (id == HTMLTokenId.TAG_OPEN) { // NOI18N
            //complete open tags with prefix
            anchor = documentItemOffset;
            //we are inside a tagname, the real content is the position before the tag
            astOffset -= (preText.length() + 1); // +"<" len

            result = new ArrayList<CompletionItem>();

            if (queryHtmlContent) {
                Collection<DTD.Element> openTags = AstNodeUtils.getPossibleOpenTagElements(root, astOffset);

                result.addAll(translateTags(documentItemOffset - 1,
                        filterElements(openTags, preText),
                        filterElements(dtd.getElementList(null),
                        preText)));
            }

            //extensions
            HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, documentItemOffset - 1, preText, itemText);
            for (HtmlExtension e : HtmlExtension.getRegisteredExtensions(sourceMimetype)) {
                result.addAll(e.completeOpenTags(context));
            }


        } else if ((id != HTMLTokenId.BLOCK_COMMENT && preText.endsWith("<")) || 
                (id == HTMLTokenId.TAG_OPEN_SYMBOL && "<".equals(item.text().toString()))) { // NOI18N
            //complete open tags with no prefix
            anchor = offset;
            result = new ArrayList<CompletionItem>();

            if (queryHtmlContent) {
                Collection<DTD.Element> openTags = AstNodeUtils.getPossibleOpenTagElements(root, astOffset);
                result.addAll(translateTags(offset - 1, openTags, dtd.getElementList(null)));
                if(HtmlPreferences.completionOffersEndTagAfterLt()) {
                    result.addAll(getPossibleEndTags(node, offset, ""));
                }
            }

            //extensions
            HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, offset - 1, "", "");
            for (HtmlExtension e : HtmlExtension.getRegisteredExtensions(sourceMimetype)) {
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

            if (!queryHtmlContent) {
                //extensions
                Collection<CompletionItem> items = new ArrayList<CompletionItem>();
                HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, anchor, prefix, itemText, node);
                for (HtmlExtension e : HtmlExtension.getRegisteredExtensions(sourceMimetype)) {
                    items.addAll(e.completeAttributes(context));
                }
                result = items;
            } else {
                if (node.type() == AstNode.NodeType.UNKNOWN_TAG ||
                        node.type() == AstNode.NodeType.DECLARATION ||
                        node.type() == AstNode.NodeType.ROOT) {
                    //nothing to complete in an unknown tag or declaration
                    return null;
                }
                //should be open tag if not unknown or root in case of the text being broken
                //that the parser cannot recognize the tag node
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
                            (!existingAttrsNames.contains(isXHtml ? aName : aName.toUpperCase(Locale.ENGLISH)) &&
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

                DTD.Element tag = node.getDTDElement();
                DTD.Attribute arg = tag == null ? null : tag.getAttribute(argName);

                result = new ArrayList<CompletionItem>();

                if (id != HTMLTokenId.VALUE) {
                    anchor = offset;
                    if (arg != null) {
                        result.addAll(translateValues(anchor, arg.getValueList("")));
                        AttrValuesCompletion valuesCompletion = AttrValuesCompletion.getSupport(node.name(), argName);
                        if (valuesCompletion != null) {
                            result.addAll(valuesCompletion.getValueCompletionItems(document, offset, ""));
                        }
                    }

                    HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, anchor, "", itemText, node, argName, false);
                    for (HtmlExtension e : HtmlExtension.getRegisteredExtensions(sourceMimetype)) {
                        result.addAll(e.completeAttributeValue(context));
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

                    if (arg != null) {
                        result.addAll(translateValues(documentItemOffset, arg.getValueList(prefix), quotationChar));
                        AttrValuesCompletion valuesCompletion = AttrValuesCompletion.getSupport(node.name(), argName);
                        if (valuesCompletion != null) {
                            result.addAll(valuesCompletion.getValueCompletionItems(document, offset, prefix));
                        }
                    }

                    HtmlExtension.CompletionContext context = new HtmlExtension.CompletionContext(parserResult, itemOffset, astOffset, anchor, prefix, itemText, node, argName, quotationChar != null);
                    for (HtmlExtension e : HtmlExtension.getRegisteredExtensions(sourceMimetype)) {
                        result.addAll(e.completeAttributeValue(context));
                    }

                }
            }
        }
        
        return result == null ? null : new CompletionResult(result, anchor);

    }

    private boolean usesLowerCase(HtmlParserResult result, int astOffset) {
        //find first open tag for the given offset and check its name case
        AstNode node = AstNodeUtils.getTagNode(result.root(), astOffset);
        return node != null ? Character.isLowerCase(node.name().charAt(0)) : true;
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

    private List<CompletionItem> translateCharRefs(int offset, List refs) {
        List<CompletionItem> result = new ArrayList<CompletionItem>(refs.size());
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

                //check if the tag needs to have a matching tag and if is matched already
                if (leaf.needsToHaveMatchingTag() && leaf.getMatchingTag() == null) {
                    //if not, any of its parent cannot be closed here
                    break;
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
        name = isXHtml ? name : (lowerCase ? name.toLowerCase(Locale.ENGLISH) : name.toUpperCase(Locale.ENGLISH));
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
            return Collections.emptyList();
        }
        List<HtmlCompletionItem> result = new ArrayList<HtmlCompletionItem>(values.size());
        if (quotationChar != null) {
            offset++; //shift the offset after the quotation
        }
        for (Iterator i = values.iterator(); i.hasNext();) {
            result.add(HtmlCompletionItem.createAttributeValue(((DTD.Value) i.next()).getName(), offset));
        }
        return result;
    }

    public static class CompletionResult {

        private Collection<? extends CompletionItem> items;
        int anchor;

        CompletionResult(Collection<? extends CompletionItem> items, int anchor) {
            this.items = items;
            this.anchor = anchor;
        }

        public int getAnchor() {
            return anchor;
        }

        public Collection<? extends CompletionItem> getItems() {
            return items;
        }
    }
}

