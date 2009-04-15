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

import org.netbeans.editor.ext.html.*;
import java.util.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.html.dtd.*;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.ErrorManager;

/**
 * Html completion results finder
 *
 * @author Petr Nejedly
 * @author Marek Fukala
 * @version 1.10
 */
public class HtmlCompletionQuery {

    private static final String SCRIPT_TAG_NAME = "SCRIPT"; //NOI18N
    private static final String STYLE_TAG_NAME = "STYLE"; //NOI18N
    private static final String XHtml_PUBLIC_ID = "-//W3C//DTD XHtml 1.0 Strict//EN";
    private static boolean lowerCase;
    private static boolean isXHtml = false;
    private static HtmlCompletionQuery DEFAULT;

    public static HtmlCompletionQuery getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new HtmlCompletionQuery();
        }
        return DEFAULT;
    }

    /** Perform the query on the given component. The query usually
     * gets the component's document, the caret position and searches back
     * to examine surrounding context. Then it returns the result.
     * @param component the component to use in this query.
     * @param offset position in the component's document to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @return result of the query or null if there's no result.
     */
    public List<CompletionItem> query(JTextComponent component, int offset) {
        BaseDocument doc = (BaseDocument) component.getDocument();

        //temporarily disabled functionality since we do not have any UI in preferences to change it.
//        if (kitClass != null) {
//            lowerCase = SettingsUtil.getBoolean(kitClass,
//                    HtmlSettingsNames.COMPLETION_LOWER_CASE,
//                    HtmlSettingsDefaults.defaultCompletionLowerCase);
//        }
        lowerCase = true;

        if (doc.getLength() == 0) {
            return null; // nothing to examine
        }
        HtmlSyntaxSupport sup = HtmlSyntaxSupport.get(doc);

        if (sup == null) {
            return null;// No SyntaxSupport for us, no hint for user
        }
        DTD dtd = sup.getDTD();
        if (dtd == null) {
            return null; // We have no knowledge about the structure!
        }
        if (XHtml_PUBLIC_ID.equalsIgnoreCase(dtd.getIdentifier())) {
            //we are completing xhtml document
            isXHtml = true;
        }

        doc.readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(doc);
            TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
            if (ts == null) {
                //Html language is not top level one
                ts = hi.tokenSequence();
                ts.move(offset);
                if (ts.moveNext() || ts.movePrevious()) {
                    ts = ts.embedded(HTMLTokenId.language());
                } else { // no tokens
                    return null;
                }
            }

            if (ts == null) {
                //no Html token on the offset
                return null;
            }

            ts.move(offset);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return null; //no token found
            }

            Token item = ts.token();

            // are we inside token or between tokens
            boolean inside = ts.offset() < offset;

            if (!inside) { //use the previous token
                if (ts.movePrevious()) {
                    item = ts.token();
                } else {
                    return null; //no previous token - shouldn't happen
                }
            }

            Token tok = item;
            //scan the token chain before the
            while (!(tok.id() == HTMLTokenId.TAG_OPEN || tok.id() == HTMLTokenId.TAG_CLOSE) && ts.movePrevious()) {
                tok = ts.token();
            }

            //we found an open or close tag or encountered beginning of the file
            if (ts.index() > 0) {
                //found the tag
                String tagName = tok.text().toString();
                for (int i = 0; i < tagName.length(); i++) {
                    char ch = tagName.charAt(i);
                    if (Character.isLetter(ch)) {
                        lowerCase = isXHtml || !Character.isUpperCase(tagName.charAt(i));
                        break;
                    }
                }
            } //else use the setting value

            //rewind token sequence back
            ts.move(item.offset(hi));

            //get text before cursor
            int itemOffset = item.offset(hi);
            int diff = offset - itemOffset;
            String preText = item.text().toString();

            if (diff < preText.length()) {
                preText = preText.substring(0, offset - itemOffset);
            }
            TokenId id = item.id();

            List<CompletionItem> result = null;
            int len = 1;

            /* Character reference finder */
            int ampIndex = preText.lastIndexOf('&'); //NOI18N
            if ((id == HTMLTokenId.TEXT || id == HTMLTokenId.VALUE) && ampIndex > -1) {
                len = preText.length() - ampIndex;
                String refNamePrefix = preText.substring(ampIndex + 1);
                result = translateCharRefs(offset - len, len, dtd.getCharRefList(refNamePrefix));

            } else if (id == HTMLTokenId.CHARACTER) {
                if (inside || !preText.endsWith(";")) { // NOI18N
                    len = offset - itemOffset;
                    result = translateCharRefs(offset - len, len, dtd.getCharRefList(preText.substring(1)));
                }
                /* Tag finder */
            } else if (id == HTMLTokenId.TAG_OPEN) { // NOI18N
                len = offset - itemOffset + 1; // minus the < char length
                result = translateTags(itemOffset - 1, len, dtd.getElementList(preText));

//                //test whether there is only one item in the CC list
//                if (result.size() == 1) {
//                    //test whether the CC is trying to complete an already COMPLETE token - the problematic situation
//                    TagItem ti = (TagItem) result.get(0); //there should only one item
//                    String itemText = ti.getItemText();
//                    //itemText = itemText.substring(1, itemText.length() - 1); //remove the < > from the tag name
//
//                    if (preText.equals(itemText)) {
//                        //now I have to look ahead to get know whether
//                        //there are some attributes or an end of the tag
//
//                        ts.move(offset);
//                        ts.moveNext();
//                        Token t = ts.token();
//
//                        //test if next token is a whitespace and the next a tag token or an attribute token
//                        if (t.id() == HTMLTokenId.WS) {
//                            if (ts.moveNext()) {
//                                t = ts.token();
//                                if ((t.id() == HTMLTokenId.TAG_CLOSE || t.id() == HTMLTokenId.ARGUMENT)) {
//                                    //do not put the item into CC - otherwise it will break the completed tag
//                                    result = null;
//                                }
//                            }
//                        }
//                    }
//                }


            } else if (id != HTMLTokenId.BLOCK_COMMENT && preText.endsWith("<")) { // NOI18N
                // There will be lookup for possible StartTags, in SyntaxSupport
                //                l = translateTags( offset-len, len, sup.getPossibleStartTags ( offset-len, "" ) );
                result = translateTags(offset - len, len, dtd.getElementList(""));

                /* EndTag finder */
            } else if (id == HTMLTokenId.TEXT && preText.endsWith("</")) { // NOI18N
                len = 2;
                result = getPossibleEndTags(doc, offset, "");
            } else if (id == HTMLTokenId.TAG_OPEN_SYMBOL && preText.endsWith("</")) { // NOI18N
                len = 2;
                result = getPossibleEndTags(doc, offset, "");
            } else if (id == HTMLTokenId.TAG_CLOSE) { // NOI18N
                len = offset - itemOffset;
                result = getPossibleEndTags(doc, offset, preText);

                /*Argument finder */
                /* TBD: It is possible to have arg just next to quoted value of previous
                 * arg, these rules doesn't match start of such arg this case because
                 * of need for matching starting quote
                 */
            } else if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                result = getAutocompletedEndTag(doc, offset);

            } else if (id == HTMLTokenId.WS || id == HTMLTokenId.ARGUMENT) {
                SyntaxElement elem = null;
                try {
                    elem = sup.getElementChain(offset);
                    // #BUGFIX 25261 At the end of document the element is
                    // automatically null but that does not mean that the
                    // completion should return null. Only if element is null
                    // also for offset-1...
                    // + bugfix of #52909 - the > is recognized as SyntaxElement.TAG so we need to
                    // get a syntax element before, when cc is called before > in a tag e.g. <table w|>
                    if (elem == null || (elem.getType() == SyntaxElement.TYPE_TAG && ">".equals(elem.getText()))) { // NOI18N
                        elem = sup.getElementChain(offset - 1);
                    }

                } catch (BadLocationException e) {
                    return null;
                }

                if (elem == null) {
                    return null;
                }

                if (elem.getElementOffset() == offset) {
                    //we are at the border between two syntax elements, 
                    //but need to use the previous one
                    //
                    //for example: < a hre|<td>...</td>

                    elem = sup.getElementChain(offset - 1);
                }

                if (elem == null) {
                    return null;
                }

                if (elem.getType() == SyntaxElement.TYPE_TAG) { // not endTags
                    SyntaxElement.Tag tagElem = (SyntaxElement.Tag) elem;

                    String tagName = tagElem.getName().toUpperCase();
                    DTD.Element tag = dtd.getElement(tagName);

                    if (tag == null) {
                        return null; // unknown tag
                    }
                    String prefix = (id == HTMLTokenId.ARGUMENT) ? preText : "";
                    len = prefix.length();
                    List possible = tag.getAttributeList(prefix); // All attribs of given tag
                    Collection<SyntaxElement.TagAttribute> existing = tagElem.getAttributes(); // Attribs already used
                    Collection<String> existingAttrsNames = new ArrayList<String>(existing.size());
                    for (SyntaxElement.TagAttribute ta : existing) {
                        existingAttrsNames.add(ta.getName());
                    }

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
                        if (aName.equals(prefix) || (!existingAttrsNames.contains(aName.toUpperCase()) && !existingAttrsNames.contains(aName.toLowerCase(Locale.ENGLISH))) || (wordAtCursor.equals(aName) && prefix.length() > 0)) {
                            attribs.add(attr);
                        }
                    }
                    result = translateAttribs(offset - len, len, attribs, tag);
                }

                /* Value finder */
                /* Suggestion - find special-meaning attributes ( IMG src, A href,
                 * color,.... - may be better resolved by attr type, may be moved
                 * to propertysheet
                 */
            } else if (id == HTMLTokenId.VALUE || id == HTMLTokenId.OPERATOR || id == HTMLTokenId.WS) {

                if (id == HTMLTokenId.WS) {
                    //is the token before an operator? '<div color= |red>'
                    ts.move(item.offset(hi));
                    ts.movePrevious();
                    Token t = ts.token();
                    if (t.id() != HTMLTokenId.OPERATOR) {
                        return null;
                    }
                }

                SyntaxElement elem = null;
                try {
                    elem = sup.getElementChain(offset);
                } catch (BadLocationException e) {
                    return null;
                }

                if (elem == null) {
                    return null;
                }

                // between Tag and error - common state when entering OOTL, e.g. <BDO dir=>
                if (elem.getType() == SyntaxElement.TYPE_ERROR) {
                    elem = elem.getPrevious();
                    if (elem == null) {
                        return null;
                    }
                }
                if (elem.getType() == SyntaxElement.TYPE_TAG) {
                    SyntaxElement.Tag tagElem = (SyntaxElement.Tag) elem;

                    String tagName = tagElem.getName().toUpperCase();
                    DTD.Element tag = dtd.getElement(tagName);
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
                    String argName = argItem.text().toString().toLowerCase(Locale.ENGLISH);

                    DTD.Attribute arg = tag.getAttribute(argName);
                    if (arg == null || arg.getType() != DTD.Attribute.TYPE_SET) {
                        return null;
                    }

                    if (id != HTMLTokenId.VALUE) {
                        len = 0;
                        result = translateValues(offset - len, len, arg.getValueList(""));
                    } else {
                        len = offset - itemOffset;

                        String quotationChar = null;
                        if (preText != null && preText.length() > 0) {
                            if (preText.substring(0, 1).equals("'")) {
                                quotationChar = "'"; // NOI18N
                            }
                            if (preText.substring(0, 1).equals("\"")) {
                                quotationChar = "\""; // NOI18N
                            }
                        }

                        result = translateValues(offset - len, len, arg.getValueList(quotationChar == null ? preText : preText.substring(1)), quotationChar);
                    }
                }
            } else if (id == HTMLTokenId.SCRIPT) {
                result = addEndTag(SCRIPT_TAG_NAME, preText, offset);
            } else if (id == HTMLTokenId.STYLE) {
                result = addEndTag(STYLE_TAG_NAME, preText, offset);
            }

            return result;

        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        } finally {
            doc.readUnlock();
        }

        return null;
    }

    private List<CompletionItem> addEndTag(String tagName, String preText, int offset) {
        int commonLength = getLastCommonCharIndex("</" + tagName + ">", preText.toUpperCase().trim()); //NOI18N
        if (commonLength == -1) {
            commonLength = 0;
        }
        if (commonLength == preText.trim().length()) {
            ArrayList<CompletionItem> items = new ArrayList<CompletionItem>(1);
            tagName = lowerCase ? tagName.toLowerCase(Locale.ENGLISH) : tagName;
            items.add(HtmlCompletionItem.createEndTag(tagName, offset - commonLength, null, -1));
            return items;
        }
        return null;
    }

    public List getPossibleEndTags(Document doc, int offset, String prefix) throws BadLocationException {
        prefix = prefix.toUpperCase();
        int prefixLen = prefix.length();
        HtmlSyntaxSupport sup = HtmlSyntaxSupport.get(doc);
        SyntaxElement elem = sup.getElementChain(offset);
        Stack stack = new Stack();
        List result = new ArrayList();
        Set found = new HashSet();
        DTD dtd = sup.getDTD();

        if (elem == null) {
            if (offset > 0) {
                elem = sup.getElementChain(offset - 1);
                if (elem == null) {
                    return result;
                }
            } else {
                return result;
            }
        }

        int itemsCount = 0;
        for (elem = elem.getPrevious(); elem != null; elem = elem.getPrevious()) {
            if (elem.getType() == SyntaxElement.TYPE_ENDTAG) { // NOI18N
                String tagName = ((SyntaxElement.Named) elem).getName().trim();
                //HACK: if there is just close tag opening symbol (</) a syntax element of
                //end tag is created for it. Such SE has empty name
                if (tagName.length() == 0) {
                    continue;
                }
                DTD.Element tag = dtd.getElement(tagName.toUpperCase());
                if (tag != null) {
                    stack.push(((SyntaxElement.Named) elem).getName().toUpperCase());
                } else {
                    stack.push(tagName); //non-html tag, store it with the original case
                }
            } else if (elem.getType() == SyntaxElement.TYPE_TAG) {
                if (((SyntaxElement.Tag) elem).isEmpty()) {
                    //ignore empty tags: <div/>
                    continue;
                }

                String tagName = ((SyntaxElement.Named) elem).getName();
                DTD.Element tag = dtd.getElement(tagName.toUpperCase());

                if (tag != null) {
                    tagName = tag.getName();

                    if (tag.isEmpty()) {
                        continue; //forbidden end tag
                    }
                }

                if (stack.empty()) {           // empty stack - we are on the same tree deepnes - can close this tag
                    if (tagName.startsWith(prefix) && !found.contains(tagName)) {    // add only new items
                        found.add(tagName);
                        if (tag != null) {
                            //html item
                            tagName = lowerCase ? tagName.toLowerCase(Locale.ENGLISH) : tagName;
                            result.add(HtmlCompletionItem.createEndTag(tagName, offset - 2 - prefixLen, tagName, itemsCount));
                        } else {
                            //non html item
                            //do not change the case, keep the original 
                            result.add(HtmlCompletionItem.createEndTag(tagName, offset - 2 - prefixLen, null, itemsCount));
                        }
                    }
                    if (tag != null && !tag.hasOptionalEnd()) {
                        break;  // If this tag have required EndTag, we can't go higher until completing this tag
                    }
                } else {                        // not empty - we match content of stack
                    if (stack.peek().equals(tagName)) { // match - close this branch of document tree
                        stack.pop();
                    } else if (tag != null && !tag.hasOptionalEnd()) {
                        break; // we reached error in document structure, give up
                    }
                }
            }
        }

        return result;
    }

    public List getAutocompletedEndTag(Document doc, int offset) {
        List l = new ArrayList();
        HtmlSyntaxSupport sup = HtmlSyntaxSupport.get(doc);
        try {
            SyntaxElement elem = sup.getElementChain(offset - 1);
            if (elem != null && elem.getType() == SyntaxElement.TYPE_TAG) {
                String tagName = ((SyntaxElement.Named) elem).getName();
                //check if the tag has required endtag
                Element dtdElem = sup.getDTD().getElement(tagName.toUpperCase());
                if (!((SyntaxElement.Tag) elem).isEmpty() && (dtdElem == null || !dtdElem.isEmpty())) {
                    if(dtdElem != null) {
                        //change case
                        tagName = lowerCase ? tagName.toLowerCase(Locale.ENGLISH) : tagName.toUpperCase(Locale.ENGLISH);
                    }

                    CompletionItem eti = HtmlCompletionItem.createAutocompleteEndTag(tagName, offset);
                    l.add(eti);
                }
            }
        } catch (BadLocationException e) {
            //just ignore
        }
        return l;
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

    List<CompletionItem> translateCharRefs(int offset, int length, List refs) {
        List result = new ArrayList(refs.size());
        String name;
        for (Iterator i = refs.iterator(); i.hasNext();) {
            DTD.CharRef chr = (DTD.CharRef) i.next();
            name = chr.getName();
            result.add(HtmlCompletionItem.createCharacterReference(name, chr.getValue(), offset, name));
        }
        return result;
    }

    List<CompletionItem> translateTags(int offset, int length, List tags) {
        List result = new ArrayList(tags.size());
        String name;
        for (Iterator i = tags.iterator(); i.hasNext();) {
            name = ((DTD.Element) i.next()).getName();
            name = lowerCase ? name.toLowerCase(Locale.ENGLISH) : name.toUpperCase();
            result.add(HtmlCompletionItem.createTag(name, offset, name));
        }
        return result;
    }

    List<CompletionItem> translateAttribs(int offset, int length, List<DTD.Attribute> attribs, DTD.Element tag) {
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

    List<CompletionItem> translateValues(int offset, int length, List values) {
        return translateValues(offset, length, values, null);
    }

    List<CompletionItem> translateValues(int offset, int length, List values, String quotationChar) {
        if (values == null) {
            return new ArrayList(0);
        }
        List result = new ArrayList(values.size());
        if(quotationChar != null) {
            offset++; //shift the offset after the quotation
        }
        for (Iterator i = values.iterator(); i.hasNext();) {
            result.add(HtmlCompletionItem.createAttributeValue(((DTD.Value) i.next()).getName(), offset));
        }
        return result;
    }

}

