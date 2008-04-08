/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.css.engine;

import java.io.IOException;
import java.io.StringReader;
// <nb>
import java.lang.ref.WeakReference;
// </nb>
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.batik.css.engine.sac.CSSConditionFactory;
import org.apache.batik.css.engine.sac.CSSSelectorFactory;
import org.apache.batik.css.engine.sac.ExtendedSelector;
import org.apache.batik.css.engine.value.ComputedValue;
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.ParsedURL;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
// <rave>
// BEGIN RAVE MODIFICATIONS
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.apache.batik.css.engine.value.AbstractValue;
import org.apache.batik.css.parser.AbstractAttributeCondition;
import org.apache.batik.css.engine.sac.CSSClassCondition;
import org.apache.batik.css.engine.sac.CSSConditionalSelector;
// END RAVE MODIFICATIONS
// </rave>

/**
 * This is the base class for all the CSS engines.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 * @author Tor Norbye (see // BEGIN RAVE MODIFICATIONS markers)
 */
public abstract class CSSEngine {

// <rave>
    // BEGIN RAVE MODIFICATIONS
    /** Rule filtering will attempt to quickly filter out rules that don't need to be considered when adding matching
     * rules for an element - that's currently the slowest operation in the designer. 
     */
    //public static boolean RULE_FILTERING = true;
    public static final boolean RULE_FILTERING = System.getProperty("rave.nocssfiltering") == null;
    public static final boolean DEBUG_FILTERING = false;
    // END RAVE MODIFICATIONS
// </rave>
    /**
     * List of StyleMap objects, one for each @font-face rule
     * encountered by this CSSEngine.
     */
    protected List fontFaces = new LinkedList();

    /**
     * Get's the StyleMaps generated by @font-face rules
     * encountered by this CSSEngine thus far.
     */
    public List getFontFaces() { return fontFaces; }

    CSSEngineUserAgent userAgent = null;

    /**
     * Returns the next stylable parent of the given element.
     */
    public static CSSStylableElement getParentCSSStylableElement(Element elt) {
// <rave>
// BEGIN RAVE MODIFICATIONS
        if (elt instanceof StyleElementLink &&
            ((StyleElementLink)elt).getStyleParent() != null) {
            return ((StyleElementLink)elt).getStyleParent();
        }
// END RAVE MODIFICATIONS
// </rave>
        Element e = getParentElement(elt);
        while (e != null) {
            if (e instanceof CSSStylableElement) {
                return (CSSStylableElement)e;
            }
            e = getParentElement(e);
        }
        return null;
    }

// <rave>
// BEGIN RAVE MODIFICATIONS
    public interface StyleElementLink {
        /** Return a parent to use for style purposes, if the regular
         * parent is null.  This is used in rave to connect markup in
         * DocumentFragments to the "master" document where the source
         * jsf element is located. Only the topmost element in the
         * rendered jsf document fragment will return non-null.
         */
        CSSStylableElement getStyleParent(); 
        /** Set a parent to use for style purposes. See getStyleParent. */    
        void setStyleParent(CSSStylableElement parent);     
    }
// END RAVE MODIFICATIONS
// </rave>
    /**
     * Returns the next parent element of the given element, from the
     * CSS point of view.
     */
    public static Element getParentElement(Element elt) {
        Node n = elt.getParentNode();
        while (n != null) {
            n = getLogicalParentNode(n);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element)n;
            }
            n = n.getParentNode();
        }
        return null;
    }

    /**
     * Returns the logical parent of a node, given its physical parent.
     */
    public static Node getLogicalParentNode(Node parent) {
        Node node = parent;
        if (node != null) {
            if (node instanceof CSSImportedElementRoot) {
                return ((CSSImportedElementRoot)node).getCSSParentElement();
            } else {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns the imported child of the given node, if any.
     */
    public static CSSImportedElementRoot getImportedChild(Node node) {
        if (node instanceof CSSImportNode) {
            CSSImportNode inode = (CSSImportNode)node;
            CSSImportedElementRoot r = inode.getCSSImportedElementRoot();
            return r;
        }
        return null;
    }

    /**
     * The CSS context.
     */
    protected CSSContext cssContext;
    
    /**
     * The associated document.
     */
    protected Document document;

    /**
     * The document URI.
     */
    protected URL documentURI;

    /**
     * The property/int mappings.
     */
    protected StringIntMap indexes;

    /**
     * The shorthand-property/int mappings.
     */
    protected StringIntMap shorthandIndexes;

    /**
     * The value managers.
     */
    protected ValueManager[] valueManagers;

    /**
     * The shorthand managers.
     */
    protected ShorthandManager[] shorthandManagers;

    /**
     * The CSS parser.
     */
    protected ExtendedParser parser;

    /**
     * The pseudo-element names.
     */
    protected String[] pseudoElementNames;

    /**
     * The font-size property index.
     */
    protected int fontSizeIndex = -1;

    /**
     * The line-height property index.
     */
    protected int lineHeightIndex = -1;

    /**
     * The color property index.
     */
    protected int colorIndex = -1;

    /**
     * The user-agent style-sheet.
     */
    protected StyleSheet userAgentStyleSheet;

    /**
     * The user style-sheet.
     */
    protected StyleSheet userStyleSheet;

    /**
     * The media to use to cascade properties.
     */
    protected SACMediaList media;

    /**
     * The DOM nodes which contains StyleSheets.
     */
// <nb> #125149 Memory leak, the style sheet nodes need to be held weakly.
//    protected List styleSheetNodes;
// ===
    protected List<WeakReference<CSSStyleSheetNode>> styleSheetNodes;
// </nb>

    /**
     * The style attribute namespace URI.
     */
    protected String styleNamespaceURI;

    /**
     * The style attribute local name.
     */
    protected String styleLocalName;
    
    /**
     * The class attribute namespace URI.
     */
    protected String classNamespaceURI;

    /**
     * The class attribute local name.
     */
    protected String classLocalName;
    
    /**
     * The non CSS presentational hints.
     */
    protected Set nonCSSPresentationalHints;

    /**
     * The non CSS presentational hints namespace URI.
     */
    protected String nonCSSPresentationalHintsNamespaceURI;

    /**
     * The style declaration document handler.
     */
    protected StyleDeclarationDocumentHandler styleDeclarationDocumentHandler =
        new StyleDeclarationDocumentHandler();

    /**
     * The style declaration update handler.
     */
    protected StyleDeclarationUpdateHandler styleDeclarationUpdateHandler;

    /**
     * The style sheet document handler.
     */
    protected StyleSheetDocumentHandler styleSheetDocumentHandler =
        new StyleSheetDocumentHandler();

    /**
     * The style declaration document handler used to build a
     * StyleDeclaration object.
     */
    protected StyleDeclarationBuilder styleDeclarationBuilder =
        new StyleDeclarationBuilder();

    /**
     * The current element.
     */
    protected CSSStylableElement element;

    /**
     * The current base URI.
     */
    protected URL cssBaseURI;

    /**
     * The alternate stylesheet title.
     */
    protected String alternateStyleSheet;

    /**
     * The DOMAttrModified event listener.
     */
    protected EventListener domAttrModifiedListener;

    /**
     * The DOMNodeInserted event listener.
     */
    protected EventListener domNodeInsertedListener;

    /**
     * The DOMNodeRemoved event listener.
     */
    protected EventListener domNodeRemovedListener;

    /**
     * The DOMSubtreeModified event listener.
     */
    protected EventListener domSubtreeModifiedListener;

    /**
     * The DOMCharacterDataModified event listener.
     */
    protected EventListener domCharacterDataModifiedListener;

    /**
     * Whether a style sheet as been removed from the document.
     */
    protected boolean styleSheetRemoved;

    /**
     * The right sibling of the last removed node.
     */
    protected Node removedStylableElementSibling;

    /**
     * The listeners.
     */
    protected List listeners = Collections.synchronizedList(new LinkedList());

    /**
     * The attributes found in stylesheets selectors.
     */
    protected Set selectorAttributes;

    /**
     * Used to fire a change event for all the properties.
     */
    protected final int[] ALL_PROPERTIES;

    /**
     * The CSS condition factory.
     */
    protected CSSConditionFactory cssConditionFactory;

    /**
     * Creates a new CSSEngine.
     * @param doc The associated document.
     * @param uri The document URI.
     * @param p The CSS parser.
     * @param vm The property value managers.
     * @param sm The shorthand properties managers.
     * @param pe The pseudo-element names supported by the associated
     *           XML dialect. Must be null if no support for pseudo-
     *           elements is required.
     * @param sns The namespace URI of the style attribute.
     * @param sln The local name of the style attribute.
     * @param cns The namespace URI of the class attribute.
     * @param cln The local name of the class attribute.
     * @param hints Whether the CSS engine should support non CSS
     *              presentational hints.
     * @param hintsNS The hints namespace URI.
     * @param ctx The CSS context.
     */
    protected CSSEngine(Document doc,
                        URL uri,
                        ExtendedParser p,
                        ValueManager[] vm,
                        ShorthandManager[] sm,
                        String[] pe,
                        String sns,
                        String sln,
                        String cns,
                        String cln,
                        boolean hints,
                        String hintsNS,
                        CSSContext ctx) {
        document = doc;
        documentURI = uri;
        parser = p;
        pseudoElementNames = pe;
        styleNamespaceURI = sns;
        styleLocalName = sln;
        classNamespaceURI = cns;
        classLocalName = cln;
        cssContext = ctx;

        cssConditionFactory = new CSSConditionFactory(cns, cln, null, "id");

// <rave>
// BEGIN RAVE MODIFICATIONS
//        int len = vm.length;
//        indexes = new StringIntMap(len);
//        valueManagers = vm;
//
//        for (int i = len - 1; i >= 0; --i) {
//            String pn = vm[i].getPropertyName();
//            indexes.put(pn, i);
//            if (fontSizeIndex == -1 &&
//                pn.equals(CSSConstants.CSS_FONT_SIZE_PROPERTY)) {
//                fontSizeIndex = i;
//            }
//            if (lineHeightIndex == -1 &&
//                pn.equals(CSSConstants.CSS_LINE_HEIGHT_PROPERTY)) {
//                lineHeightIndex = i;
//            }
//            if (colorIndex == -1 &&
//                pn.equals(CSSConstants.CSS_COLOR_PROPERTY)) {
//                colorIndex = i;
//            }
//        }
//
//        len = sm.length;
//        shorthandIndexes = new StringIntMap(len);
//        shorthandManagers = sm;
//        for (int i = len - 1; i >= 0; --i) {
//            shorthandIndexes.put(sm[i].getPropertyName(), i);
//        }
        // Comment this stuff out, and do it in XhtmlCssEngine where we
        // can do a more efficient job (we know the size property indexes
        // directly, and more importantly, since it knows that it's sharing
        // the value manager and shorthand manager indices between the 
        // engines, it might as well share the index maps as well!)
        // We have to do this to compensate though:
        valueManagers = vm;
        shorthandManagers = sm;
// END RAVE MODIFICATIONS
// </rave>

// <rave>
// BEGIN RAVE MODIFICATIONS
//           This support was useless, it was simply aliasing CSS names
//           to properties! But usually it's not a straight map!
//           For example, the HTML attribute "background" maps to the
//           CSS property "background-image" ! And the attribute "bgcolor"
//           maps to the CSS property "background-color" !  
//           So instead we'll handle this in subclasses where specific
//           logic can be written (the attribute value often has to be
//           special handled too so I can't just provide a map to CSSEngine;
//           e.g. for "background" for example I need to change the string
//           value into a URI.)
//        if (hints) {
//            len = vm.length;
//            nonCSSPresentationalHints = new HashSet();
//            nonCSSPresentationalHintsNamespaceURI = hintsNS;
//            for (int i = len - 1; i >= 0; --i) {
//                String pn = vm[i].getPropertyName();
//                nonCSSPresentationalHints.add(pn);
//            }
//        }
// END RAVE MODIFICATIONS
// </rave>

        if (cssContext.isDynamic() &&
            (document instanceof EventTarget)) {
            // Attach the mutation events listeners.
            EventTarget et = (EventTarget)document;
            domAttrModifiedListener = new DOMAttrModifiedListener();
            et.addEventListener("DOMAttrModified",
                                domAttrModifiedListener,
                                false);
            domNodeInsertedListener = new DOMNodeInsertedListener();
            et.addEventListener("DOMNodeInserted",
                                domNodeInsertedListener,
                                false);
            domNodeRemovedListener = new DOMNodeRemovedListener();
            et.addEventListener("DOMNodeRemoved",
                                domNodeRemovedListener,
                                false);
            domSubtreeModifiedListener = new DOMSubtreeModifiedListener();
            et.addEventListener("DOMSubtreeModified",
                                domSubtreeModifiedListener,
                                false);
            domCharacterDataModifiedListener =
                new DOMCharacterDataModifiedListener();
            et.addEventListener("DOMCharacterDataModified",
                                domCharacterDataModifiedListener,
                                false);
            styleDeclarationUpdateHandler =
                new StyleDeclarationUpdateHandler();
        }

        ALL_PROPERTIES = new int[getNumberOfProperties()];
        for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
            ALL_PROPERTIES[i] = i;
        }
    }

    /**
     * Disposes the CSSEngine and all the attached resources.
     */
    public void dispose() {
        setCSSEngineUserAgent(null);
        disposeStyleMaps(document.getDocumentElement());
        if (document instanceof EventTarget) {
            // Detach the mutation events listeners.
            EventTarget et = (EventTarget)document;
            et.removeEventListener("DOMAttrModified",
                                   domAttrModifiedListener,
                                   false);
            et.removeEventListener("DOMNodeInserted",
                                   domNodeInsertedListener,
                                   false);
            et.removeEventListener("DOMNodeRemoved",
                                   domNodeRemovedListener,
                                   false);
            et.removeEventListener("DOMSubtreeModified",
                                   domSubtreeModifiedListener,
                                   false);
            et.removeEventListener("DOMCharacterDataModified",
                                   domCharacterDataModifiedListener,
                                   false);
        }
    }

// <rave>
// BEGIN RAVE MODIFICATIONS
//    private void disposeStyleMaps(Node node) {
    protected void disposeStyleMaps(Node node) {
// END RAVE MODIFICATIONS
// </rave>
        if (node instanceof CSSStylableElement) {
            ((CSSStylableElement)node).setComputedStyleMap(null, null);
        }
        for (Node n = node.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                disposeStyleMaps(n);
            }
            Node c = getImportedChild(n);
            if (c != null) {
                disposeStyleMaps(c);
            }
        }
    }

    /**
     * Returns the CSS context.
     */
    public CSSContext getCSSContext() {
        return cssContext;
    }

    /**
     * Returns the document associated with this engine.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the font-size property index.
     */
    public int getFontSizeIndex() {
        return fontSizeIndex;
    }

    /**
     * Returns the line-height property index.
     */
    public int getLineHeightIndex() {
        return lineHeightIndex;
    }

    /**
     * Returns the color property index.
     */
    public int getColorIndex() {
        return colorIndex;
    }

    /**
     * Returns the number of properties.
     */
    public int getNumberOfProperties() {
        return valueManagers.length;
    }

    /**
     * Returns the property index, or -1.
     */
    public int getPropertyIndex(String name) {
        return indexes.get(name);
    }

    /**
     * Returns the shorthand property index, or -1.
     */
    public int getShorthandIndex(String name) {
        return shorthandIndexes.get(name);
    }

    /**
     * Returns the name of the property at the given index.
     */
    public String getPropertyName(int idx) {
        return valueManagers[idx].getPropertyName();
    }

    public void setCSSEngineUserAgent(CSSEngineUserAgent userAgent) {
        this.userAgent = userAgent;
    }

    public CSSEngineUserAgent getCSSEngineUserAgent() {
        return userAgent;
    }

    /**
     * Sets the user agent style-sheet.
     */
    public void setUserAgentStyleSheet(StyleSheet ss) {
        userAgentStyleSheet = ss;
    }

    /**
     * Sets the user style-sheet.
     */
    public void setUserStyleSheet(StyleSheet ss) {
        userStyleSheet = ss;
    }

    /**
     * Returns the ValueManagers.
     */
    public ValueManager[] getValueManagers() {
        return valueManagers;
    }

    /**
     * Sets the media to use to compute the styles.
     */
    public void setMedia(String str) {
        try {
            media = parser.parseMedia(str);
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";
            String s =Messages.formatMessage
                ("media.error", new Object[] { str, m });
            throw new DOMException(DOMException.SYNTAX_ERR, s);
        }
    }

    /**
     * Sets the alternate style-sheet title.
     */
    public void setAlternateStyleSheet(String str) {
        alternateStyleSheet = str;
    }

    /**
     * Recursively imports the cascaded style from a source element
     * to an element of the current document.
     */
    public void importCascadedStyleMaps(Element src,
                                        CSSEngine srceng,
                                        Element dest) {
        if (src instanceof CSSStylableElement) {
            CSSStylableElement csrc  = (CSSStylableElement)src;
            CSSStylableElement cdest = (CSSStylableElement)dest;

            StyleMap sm = srceng.getCascadedStyleMap(csrc, null);
            sm.setFixedCascadedStyle(true);
            cdest.setComputedStyleMap(null, sm);

            if (pseudoElementNames != null) {
                int len = pseudoElementNames.length;
                for (int i = 0; i < len; i++) {
                    String pe = pseudoElementNames[i];
                    sm = srceng.getCascadedStyleMap(csrc, pe);
                    cdest.setComputedStyleMap(pe, sm);
                }
            }
        }

        for (Node dn = dest.getFirstChild(), sn = src.getFirstChild();
             dn != null;
             dn = dn.getNextSibling(), sn = sn.getNextSibling()) {
            if (sn.getNodeType() == Node.ELEMENT_NODE) {
                importCascadedStyleMaps((Element)sn, srceng, (Element)dn);
            }
        }
    }

    /**
     * Returns the current base-url.
     */
    public URL getCSSBaseURI() {
        if (cssBaseURI == null) {
// <rave>
// BEGIN RAVE MODIFICATIONS
// We sometimes call the parser when there's no URI available (for example
// as part of the CSS string parsing service.)  Make sure this isn't a 
// big problem by providing a silly URL - resources won't be found in this
// case. I should consider requiring a URL for the CSS parse requests...
            //cssBaseURI = element.getCSSBase();
            if (element == null) {
                // No valid URL (such as at style editing time when we're just
                // editing a stylesheet
                // Use dummy
                try {
                    return new URL("http", "localhost", 80, "dummy");
                } catch (MalformedURLException mfue) {
                }
            } else {
                cssBaseURI = element.getCSSBase();
            }
// ELSE RAVE MODIFICATIONS
// </rave>
        }
        return cssBaseURI;
    }

    /**
     * Returns the cascaded style of the given element/pseudo-element.
     * @param elt The stylable element.
     * @param pseudo Optional pseudo-element string (null if none).
     */
    public StyleMap getCascadedStyleMap(CSSStylableElement elt,
                                        String pseudo) {
        int props = getNumberOfProperties();
        StyleMap result = new StyleMap(props);

        // Apply the user-agent style-sheet to the result.
        if (userAgentStyleSheet != null) {
            List rules = new ArrayList();
            addMatchingRules(rules, userAgentStyleSheet, elt, pseudo);
            addRules(elt, pseudo, result, rules, StyleMap.USER_AGENT_ORIGIN);
        }

        // Apply the user properties style-sheet to the result.
        if (userStyleSheet != null) {
            List rules = new ArrayList();
            addMatchingRules(rules, userStyleSheet, elt, pseudo);
            addRules(elt, pseudo, result, rules, StyleMap.USER_ORIGIN);
        }

        element = elt;
        try {
        // Apply the non-CSS presentational hints to the result.
// <rave>
// BEGIN RAVE MODIFICATIONS
            applyNonCSSPresentationalHints(elt, result);
//               This support was useless, it was simply aliasing CSS names
//               to properties! But usually it's not a straight map!
//               For example, the HTML attribute "background" maps to the
//               CSS property "background-image" ! And the attribute "bgcolor"
//               maps to the CSS property "background-color" !  
//               So instead we'll handle this in subclasses where specific
//               logic can be written (the attribute value often has to be
//               special handled too so I can't just provide a map to CSSEngine;
//               e.g. for "background" for example I need to change the string
//               value into a URI.)
//            if (nonCSSPresentationalHints != null) {
//                NamedNodeMap attrs = elt.getAttributes();
//                int len = attrs.getLength();
//                for (int i = 0; i < len; i++) {
//                    Node attr = attrs.item(i);
//                    String an = attr.getNodeName();
//                    if (nonCSSPresentationalHints.contains(an)) {
//                        try {
//                            LexicalUnit lu;
//                            int idx = getPropertyIndex(an);
//                            lu = parser.parsePropertyValue
//                                (attr.getNodeValue());
//                            ValueManager vm = valueManagers[idx];
//                            Value v = vm.createValue(lu, this);
//                            putAuthorProperty(result, idx, v, false,
//                                              StyleMap.NON_CSS_ORIGIN);
//                        } catch (Exception e) {
//                            String m = e.getMessage();
//                            if (m == null) m = "";
//                            String u = ((documentURI == null)?"<unknown>":
//                                        documentURI.toString());
//                            String s = Messages.formatMessage
//                                ("property.syntax.error.at",
//                                 new Object[] { u, an, attr.getNodeValue(),m});
//                            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
//                            de.initCause(e);
//                            if (userAgent == null) throw de;
//                            userAgent.displayError(de);
//                        }
//                    }
//                }
//            }
// END RAVE MODIFICATIONS
// </rave>

            // Apply the document style-sheets to the result.
// <nb
//            List snodes = getStyleSheetNodes();
// ===
            List<WeakReference<CSSStyleSheetNode>> snodes = getStyleSheetNodes();
// </nb>
            int slen = snodes.size();
            if (slen > 0) {
                List rules = new ArrayList();
                for (int i = 0; i < slen; i++) {
// <nb>
//                    CSSStyleSheetNode ssn = (CSSStyleSheetNode)snodes.get(i);
// ===
                    WeakReference<CSSStyleSheetNode> ssnWRef = snodes.get(i);
                    CSSStyleSheetNode ssn = ssnWRef == null ? null : ssnWRef.get();
                    if (ssn == null) {
                        continue;
                    }
// </nb>
                    StyleSheet ss = ssn.getCSSStyleSheet();
                    if (ss != null &&
                        (!ss.isAlternate() ||
                         ss.getTitle() == null ||
                         ss.getTitle().equals(alternateStyleSheet)) &&
                        mediaMatch(ss.getMedia())) {
                        addMatchingRules(rules, ss, elt, pseudo);
                    }
                }
                addRules(elt, pseudo, result, rules, StyleMap.AUTHOR_ORIGIN);
            }
// <rave>
            // BEGIN RAVE MODIFICATIONS
            // Check if we have any transient stylesheet nodes to
            // consider
            if (transientStyleSheetNodes != null) {
                List rules = new ArrayList(transientStyleSheetNodes.size());
                for (int i = 0; i < transientStyleSheetNodes.size(); i++) {
                    CSSStyleSheetNode ssn = (CSSStyleSheetNode)transientStyleSheetNodes.get(i);
                    StyleSheet ss = ssn.getCSSStyleSheet();
                    if (ss != null &&
                        (!ss.isAlternate() ||
                         ss.getTitle() == null ||
                         ss.getTitle().equals(alternateStyleSheet)) &&
                        mediaMatch(ss.getMedia())) {
                        addMatchingRules(rules, ss, elt, pseudo);
                    }
                }
                addRules(elt, pseudo, result, rules, StyleMap.AUTHOR_ORIGIN);
            }
            // END RAVE MODIFICATIONS
// </rave>

            // Apply the inline style to the result.
            if (styleLocalName != null) {
                String style = elt.getAttributeNS(styleNamespaceURI,
                                                  styleLocalName);
                if (style.length() > 0) {
                    try {
                        parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
                        parser.setConditionFactory(cssConditionFactory);
                        styleDeclarationDocumentHandler.styleMap = result;
                        parser.setDocumentHandler
                            (styleDeclarationDocumentHandler);
// <rave>
                        // BEGIN RAVE MODIFICATIONS
                        styleDeclarationDocumentHandler.location = elt;
                        styleDeclarationDocumentHandler.lineno = -1;
                        // END RAVE MODIFICATIONS
// </rave>
                        parser.parseStyleDeclaration(style);
                        styleDeclarationDocumentHandler.styleMap = null;
                    } catch (Exception e) {
                        String m = e.getMessage();
                        if (m == null) m = "";
                        String u = ((documentURI == null)?"<unknown>":
                                    documentURI.toString());
                        String s = Messages.formatMessage
                            ("style.syntax.error.at",
                             new Object[] { u, styleLocalName, style, m});
                        DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
                        de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
                        if (userAgent == null) throw de;
                        userAgent.displayError(de);
                    }
                }
            }
        } finally {
            element = null;
            cssBaseURI = null;
        }

        return result;
    }

// <rave>
// BEGIN RAVE MODIFICATIONS
    /** Some error occurred while parsing stylesheet - display it to
     * the developer */
    protected void displayError(DOMException e, Object location, int lineno, int column) {
        // Overridden in subclasses
    }
    protected void displayMissingStyleSheet(String uri) {
        // Overridden in subclasses
    }
    
    /** 
     * Shorthand property being set. Set when we're processing a shorthand property
     * such that individual value managers can figure out that they're part of a shorthand
     * property expansion (useful when creating error messages for example 
     */
    private int expandingShorthandProperty = -1;
    
    /**
     * Return the shorthand property being set. Set when we're processing a shorthand property
     * such that individual value managers can figure out that they're part of a shorthand
     * property expansion (useful when creating error messages for example. Returns null
     * if no shorthand property is being processed.
     */
    public String getExpandingShorthandProperty() {
        if (expandingShorthandProperty == -1) {
            return null;
        } else {
            return shorthandManagers[expandingShorthandProperty].getPropertyName();
        }
    }

    /** Set the given element's value for a given attribute; if value is
     * null, remove the attribute. */
    protected void setAttributeValue(Element elt, String name, String value) {
        // This sometimes throws ClassCastExceptions in Xerces -- why?
        //elt.setAttributeNS(styleNamespaceURI, styleLocalName, value);
        if (value != null) {
            elt.setAttribute(name, value);
        } else {
            elt.removeAttribute(name);
        }
        /* CAST: Ah I think I know now: in the method they have this comment:

        // This case may happen if user calls:
        //      elem.setAttribute("name", "value");
        //      elem.setAttributeNS(null, "name", "value");
        // This case is not defined by the DOM spec, we choose
        // to create a new attribute in this case and remove an old one from the tree
        // note this might cause events to be propagated or user data to be lost 
        
        and then they proceed to cast to a CoreDocumentImpl... I bet
        it's just not safe to mix setAttribute and setAttributeNS and
        that's what we must have been doing ... so let's just switch.
        */
    }


    /**
     * Scan attributes for an element and transcribe deprecated
     * style-type attributes into real style properties
     */
    protected void applyNonCSSPresentationalHints(CSSStylableElement elt, 
                                                  StyleMap map) {
        // Overridden in subclasses
    }


    /**
     * For use by the implementation of applyNonCSSPresentationalHints;
     * apply a non-presentational CSS hint as the given CSS value for
     * the given CSS property.
     */
    protected void applyNonCSSPresentationalHint(CSSStylableElement elt, StyleMap map, int idx, String value) {
        try {
            LexicalUnit lu = parser.parsePropertyValue(value);
            ValueManager vm = valueManagers[idx];
            try {
                Value v = vm.createValue(lu, this);

                if (v instanceof AbstractValue) { // Should I add to Value interface???
                    AbstractValue av = (AbstractValue)v;
                    av.setLocation(elt);
                    av.setLineNumber(-1);
                }

                putAuthorProperty(map, idx, v, false,
                                  StyleMap.NON_CSS_ORIGIN);
            } catch (DOMException e) {
                // Something bad happened
                displayError(e, elt, 0, 0);
                // Continue processing - we're supposed to ignore
                // errant declarations!!!
            }

        } catch (Exception e) {
            // No longer used???
            String m = e.getMessage();
            if (m == null) m = "";
            String u = ((documentURI == null)?"<unknown>":
                        documentURI.toString());
            String s = Messages.formatMessage // TODO: make our own message here
                ("property.syntax.error.at",
                 new Object[] { u, "?", value,m});
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
            de.initCause(e);
            if (userAgent == null) throw de;
            userAgent.displayError(de);
        }
    }

    /**
     * For use by the implementation of applyNonCSSPresentationalHints;
     * apply a non-presentational CSS hint as the given CSS value for
     * the given CSS property.
     */
    protected void applyNonCSSPresentationalHint(CSSStylableElement elt, StyleMap map, int idx, Value v) {
        ValueManager vm = valueManagers[idx];
        try {
            putAuthorProperty(map, idx, v, false,
                              StyleMap.NON_CSS_ORIGIN);
        } catch (DOMException e) {
            // Something bad happened
            displayError(e, elt, 0, 0);
            // Continue processing - we're supposed to ignore
            // errant declarations!!!
        }
    }

    /** 
     * Return true iff the given CSS property for the given element
     * is not set, e.g. the default value would be returned instead.
     * Note - it's not the same as asking if the property has the same
     * value as its default property; this method will still return false
     * in that case since it has a value set, so it doesn't have to pull
     * the default value.
     */
    public boolean isDefaultValue(CSSStylableElement elt, String pseudo, 
                                  int propidx) {
        StyleMap sm = elt.getComputedStyleMap(pseudo);
        if (sm == null) {
            sm = getCascadedStyleMap(elt, pseudo);
            elt.setComputedStyleMap(pseudo, sm);
        }

        Value value = sm.getValue(propidx);
        if (value != null) {
            return false;
        }
        ValueManager vm = valueManagers[propidx];
        CSSStylableElement p = getParentCSSStylableElement(elt);
        if (value == null && (!vm.isInheritedProperty() || p == null)) {
            return true;
        }
        return false;
    }

    /** 
     * Return true iff the given CSS property for the given element
     * was inherited, as opposed to referenced directly by some rule.
     */
    public boolean isInheritedValue(CSSStylableElement elt, int propidx) {
        // Make sure it's computed first
        Value v = getComputedStyle(elt, null, propidx);
        StyleMap sm = elt.getComputedStyleMap(null);
        return sm.isInherited(propidx);
    }
    
    /** 
     * Return a list of style classes defined in the stylesheets for
     * this engine.  This will not include any styleclasses defined in
     * the user agent stylesheet.
     */
    public Collection getStyleClasses() {
        //ArrayList styleClasses = new ArrayList();
        TreeSet styleClasses = new TreeSet();

        // We don't want to list any styleclasses in the user agent stylesheet
        if (userStyleSheet != null) {
            List rules = new ArrayList();
            addStyleClasses(styleClasses, userStyleSheet);
        }

        // Apply the document style-sheets to the result.
// <nb>
//        List snodes = getStyleSheetNodes();
// ===
        List<WeakReference<CSSStyleSheetNode>> snodes = getStyleSheetNodes();
// </nb>
        int slen = snodes.size();
        if (slen > 0) {
            for (int i = 0; i < slen; i++) {
// <nb>
//                CSSStyleSheetNode ssn = (CSSStyleSheetNode)snodes.get(i);
// ===
                WeakReference<CSSStyleSheetNode> ssnWRef = snodes.get(i);
                CSSStyleSheetNode ssn = ssnWRef == null ? null : ssnWRef.get();
                if (ssn == null) {
                    continue;
                }
// </nb>
                StyleSheet ss = ssn.getCSSStyleSheet();
                if (ss != null &&
                    (!ss.isAlternate() ||
                     ss.getTitle() == null ||
                     ss.getTitle().equals(alternateStyleSheet)) &&
                    mediaMatch(ss.getMedia())) {
                    addStyleClasses(styleClasses, ss);
                }
            }
        }
        if (transientStyleSheetNodes != null) {
            for (int i = 0; i < transientStyleSheetNodes.size(); i++) {
                CSSStyleSheetNode ssn = (CSSStyleSheetNode)transientStyleSheetNodes.get(i);
                StyleSheet ss = ssn.getCSSStyleSheet();
                if (ss != null &&
                    (!ss.isAlternate() ||
                     ss.getTitle() == null ||
                     ss.getTitle().equals(alternateStyleSheet)) &&
                    mediaMatch(ss.getMedia())) {
                    addStyleClasses(styleClasses, ss);
                }
            }
        }
        // Suppress duplicates
        return styleClasses;
    }

    private void addStyleClasses(Collection styleClasses, StyleSheet ss) {
        int len = ss.getSize();
        for (int i = 0; i < len; i++) {
            Rule r = ss.getRule(i);
            switch (r.getType()) {
            case StyleRule.TYPE:
                StyleRule style = (StyleRule)r;
                SelectorList sl = style.getSelectorList();
                int slen = sl.getLength();
                for (int j = 0; j < slen; j++) {
                    ExtendedSelector s = (ExtendedSelector)sl.item(j);
                    if (s instanceof CSSConditionalSelector) {
                        CSSConditionalSelector cs = (CSSConditionalSelector)s;
                        if (cs.getCondition() instanceof CSSClassCondition) {
                            CSSClassCondition ck = (CSSClassCondition)cs.getCondition();
                            styleClasses.add(ck.getValue());
                        }
                    }
                }
                break;

            case MediaRule.TYPE:
            case ImportRule.TYPE:
                MediaRule mr = (MediaRule)r;
                if (mediaMatch(mr.getMediaList())) {
                    addStyleClasses(styleClasses,mr);
                }
                break;
            }
        }
    }

    /** Return a display of the matching rules for the given element */
    public String getMatchingRules(Element elt, boolean includeAgentRules) {
        String pseudo = "";
        StringBuffer sb = new StringBuffer(2000);

        if (includeAgentRules) {
            // Apply the user-agent style-sheet to the result.
            if (userAgentStyleSheet != null) {
                List rules = new ArrayList();
                addMatchingRules(rules, userAgentStyleSheet, elt, pseudo);
                Iterator it = rules.iterator();
                if (it.hasNext()) {
                    sb.append("Default User Agent Styles:\n");
                }
                while (it.hasNext()) {
                    Rule rule = (Rule)it.next();
                    sb.append(rule.toString(this));
                    sb.append("\n");
                }
            }
        }

        // Apply the user properties style-sheet to the result.
        if (userStyleSheet != null) {
            List rules = new ArrayList();
            addMatchingRules(rules, userStyleSheet, elt, pseudo);
            Iterator it = rules.iterator();
            if (it.hasNext()) {
                sb.append("Default User Styles:\n");
            }
            while (it.hasNext()) {
                Rule rule = (Rule)it.next();
                sb.append(rule.toString(this));
                sb.append("\n");
            }
        }

        // Apply the document style-sheets to the result.
// <nb>
//        List snodes = getStyleSheetNodes();
// ===
        List<WeakReference<CSSStyleSheetNode>> snodes = getStyleSheetNodes();
// </nb>
        int slen = snodes.size();
        if (slen > 0) {
            for (int i = 0; i < slen; i++) {
// <nb>
//                CSSStyleSheetNode ssn = (CSSStyleSheetNode)snodes.get(i);
// ===
                WeakReference<CSSStyleSheetNode> ssnWRef = snodes.get(i);
                CSSStyleSheetNode ssn = ssnWRef == null ? null : ssnWRef.get();
                if (ssn == null) {
                    continue;
                }
// </nb>
                StyleSheet ss = ssn.getCSSStyleSheet();
                if (ss != null &&
                    (!ss.isAlternate() ||
                     ss.getTitle() == null ||
                     ss.getTitle().equals(alternateStyleSheet)) &&
                    mediaMatch(ss.getMedia())) {
                    List rules = new ArrayList();
                    addMatchingRules(rules, ss, elt, pseudo);
                    Iterator it = rules.iterator();
                    if (it.hasNext()) {
                        sb.append(ss.getTitle() + ":\n");
                    }
                    while (it.hasNext()) {
                        Rule rule = (Rule)it.next();
                        sb.append(rule.toString(this));
                        sb.append("\n");
                    }
                }
            }
        }

        // Check if we have any transient stylesheet nodes to
        // consider
        if (transientStyleSheetNodes != null) {
            for (int i = 0; i < transientStyleSheetNodes.size(); i++) {
                CSSStyleSheetNode ssn = (CSSStyleSheetNode)transientStyleSheetNodes.get(i);
                StyleSheet ss = ssn.getCSSStyleSheet();
                if (ss != null &&
                    (!ss.isAlternate() ||
                     ss.getTitle() == null ||
                     ss.getTitle().equals(alternateStyleSheet)) &&
                    mediaMatch(ss.getMedia())) {
                    List rules = new ArrayList();
                    addMatchingRules(rules, ss, elt, pseudo);
                    Iterator it = rules.iterator();
                    if (it.hasNext()) {
                        sb.append(ss.getTitle() + ":\n");
                    }
                    while (it.hasNext()) {
                        Rule rule = (Rule)it.next();
                        sb.append(rule.toString(this));
                        sb.append("\n");
                    }
                }
            }
        }

        String style = elt.getAttribute(styleLocalName);
        if (style.length() > 0) {
            sb.append("style=\"" + style + "\"\n");
        }

        return sb.toString();
    }

    /**
     * Updates the local styles for an element. First any style settings
     * in the set parameter array are applied, then any properties
     * pointed to by the remove array are applied.
     * @return The new style string
     */
    public String getUpdatedLocalStyleValues(CSSStylableElement elt, 
    StyleSetting[] stylesToSet, StyleSetting[] stylesToRemove) throws Exception {
        String style = elt.getAttributeNS(styleNamespaceURI, styleLocalName);
        StringBuffer styleBuffer = new StringBuffer(200);
        if (style.length() > 0) {
            styleBuffer.append(style);
        }
        if (stylesToSet != null) {
            for (int i = 0; i < stylesToSet.length; i++) {
                StyleSetting setting = stylesToSet[i];
                if (styleBuffer.length() > 0) {
                    styleBuffer.append("; ");
                }
                styleBuffer.append(getPropertyName(setting.getIndex()));
                styleBuffer.append(":");
                styleBuffer.append(setting.getValue());
            }
        }
        style = styleBuffer.toString();
        
        CSSStylableElement old = element;
        try {
            int props = getNumberOfProperties();
            StyleMap result = new StyleMap(props);
            element = (CSSStylableElement)elt;

            unknownPropertyNames = new ArrayList();
            unknownPropertyValues = new ArrayList();
            parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
            parser.setConditionFactory(cssConditionFactory);
            styleDeclarationDocumentHandler.styleMap = result;
            parser.setDocumentHandler(styleDeclarationDocumentHandler);
            styleDeclarationDocumentHandler.location = elt;
            styleDeclarationDocumentHandler.lineno = -1;
            parser.parseStyleDeclaration(style);
            styleDeclarationDocumentHandler.styleMap = null;

            // Perform deletions
            if (stylesToRemove != null) {
                StyleMap sm = elt.getComputedStyleMap(null); // XXX what do we use for pseudo?
                for (int i = 0; i < stylesToRemove.length; i++) {
                    StyleSetting setting = stylesToRemove[i];
                    // Remove properties
                    result.putValue(setting.getIndex(), null);
                    if (sm != null) {
                        sm.putValue(setting.getIndex(), null);
                    }
                }
            }
            
            // Get stylemap back as a string...
            //String s = result.toStyleString(this);
            String s = toMinimalStyleString(result);

            // Append "unknown" styles
            if (unknownPropertyNames.size() > 0) {
                StringBuffer sb = new StringBuffer(s.length()+unknownPropertyNames.size()*30);
                sb.append(s);
                for (int i = 0, n = unknownPropertyNames.size(); i < n; i++) {
                    if (sb.length() > 0) {
                        sb.append(';');
                        sb.append(' ');
                    }
                    sb.append(unknownPropertyNames.get(i).toString());
                    sb.append(':');
                    sb.append(' ');
                    sb.append(unknownPropertyValues.get(i).toString());
                }
                s = sb.toString();
            }
            
            if (s.length() == 0) {
                s = null;
            }
            
// <removing design bean manipulation in engine>
//            setAttributeValue(elt, styleLocalName, s);
// </removing design bean manipulation in engine>
                
            return s;
            
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null)
                m = "";
            String u =
                ((documentURI == null) ? "<unknown>" : documentURI.toString());
            String s =
                Messages.formatMessage(
                    "style.syntax.error.at",
                    new Object[] { u, styleLocalName, style, m });
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
            de.initCause(e);
            if (userAgent == null)
                throw de;
            userAgent.displayError(de);
            
// <removing design bean manipulation in engine>
//            return null;
// ====
            throw de;
// </removing design bean manipulation in engine>
        } finally {
            unknownPropertyNames = null;            
            unknownPropertyValues = null;            
            element = old;
        }
    }

    /** When non null, stores a list of properties that were not recognized.
     * This is only currently done for parsing of style expressions, not style
     * sheets, since it's used for local style element manipulation.
     */
    protected List unknownPropertyNames;
    protected List unknownPropertyValues;

    /** Given a lexical unit value, produce its CSS value expression
     * string by appending into the given StringBuffer. This is a
     * "toString()" like method I think LexicalUnit should have
     * offered. I really just want the "r-value" from a style
     * declaration, but the parser doesn't keep it around so we have to
     * reconstruct it from lexical tokens.
     */
    private void appendCss(StringBuffer sb, LexicalUnit value) {
        while (value != null) {
            switch (value.getLexicalUnitType()) {
 
            case LexicalUnit.SAC_OPERATOR_COMMA: 
                sb.append(","); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_PLUS: 
                sb.append("+"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_MINUS:
                sb.append("-"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_MULTIPLY:
                sb.append("*"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_SLASH:
                sb.append("/"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_MOD:
                sb.append("%"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_EXP:
                sb.append("^"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_LT:
                sb.append("<"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_GT:
                sb.append(">"); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_LE:
                sb.append("<="); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_GE:
                sb.append(">="); break; // NOI18N
            case LexicalUnit.SAC_OPERATOR_TILDE:
                sb.append("~"); break; // NOI18N
            case LexicalUnit.SAC_INHERIT:
                sb.append("inherit"); break; // NOI18N
            case LexicalUnit.SAC_INTEGER:
                sb.append(Integer.toString(value.getIntegerValue())); break;
            case LexicalUnit.SAC_REAL:
                sb.append(Float.toString(value.getFloatValue())); break;
            case LexicalUnit.SAC_EM:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_EX:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_PIXEL:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_INCH:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_CENTIMETER:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_MILLIMETER:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_POINT:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_PICA:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_PERCENTAGE:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_URI:
                sb.append("uri(" + value.getStringValue() + ")"); break; // NOI18N
            case LexicalUnit.SAC_DEGREE:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_GRADIAN:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_RADIAN:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_MILLISECOND:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_SECOND:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_HERTZ:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_KILOHERTZ:
                sb.append(Float.toString(value.getFloatValue()) + value.getDimensionUnitText()); break;
            case LexicalUnit.SAC_IDENT:
                sb.append(value.getStringValue()); break;
            case LexicalUnit.SAC_STRING_VALUE:
                sb.append("\"" + value.getStringValue() + "\""); break;
            case LexicalUnit.SAC_ATTR:
                sb.append(value.getStringValue()); break;
            case LexicalUnit.SAC_DIMENSION:
                sb.append(Float.toString(value.getFloatValue())); break;
                
                // XXX not yet supported, SAC doesn't yet handle
            case LexicalUnit.SAC_UNICODERANGE:
                break; // NOI18N
                
            case LexicalUnit.SAC_SUB_EXPRESSION: 
                sb.append("("); // NOI18N
                appendCss(sb, value.getSubValues());
                sb.append(")"); // NOI18N
                break;

            case LexicalUnit.SAC_RGBCOLOR:
                // TODO  - convert to hex instead? e.g. #FFFFFF
                sb.append("rgb"); // NOI18N
                sb.append("("); // NOI18N
                appendCss(sb, value.getParameters());
                sb.append(")"); // NOI18N
                break;
            case LexicalUnit.SAC_COUNTER_FUNCTION:
                sb.append("counter"); // NOI18N
                sb.append("("); // NOI18N
                appendCss(sb, value.getParameters());
                sb.append(")"); // NOI18N
                break;
            case LexicalUnit.SAC_COUNTERS_FUNCTION:
                sb.append("counters"); // NOI18N
                sb.append("("); // NOI18N
                appendCss(sb, value.getParameters());
                sb.append(")"); // NOI18N
                break;
            case LexicalUnit.SAC_RECT_FUNCTION:
                sb.append("rect"); // NOI18N
                sb.append("("); // NOI18N
                appendCss(sb, value.getParameters());
                sb.append(")"); // NOI18N
                break;
            case LexicalUnit.SAC_FUNCTION:
                sb.append(value.getFunctionName());
                sb.append("("); // NOI18N
                appendCss(sb, value.getParameters());
                sb.append(")"); // NOI18N
                break;
            }
            
            value = value.getNextLexicalUnit();
            if (value != null) {
                sb.append(' ');
            }
        }
    }

    /** Notify the engine that a new style element has been 
     * created
     */
    public void addTransientStyleSheetNode(CSSStyleSheetNode elt) {
        if (transientStyleSheetNodes == null) {
            transientStyleSheetNodes = new ArrayList();
        }
        transientStyleSheetNodes.add(elt);
    }

    public void clearTransientStyleSheetNodes() {
        transientStyleSheetNodes = null;
    }

    private ArrayList transientStyleSheetNodes;
    
    protected void warnCircularReference(URL uri, Object location) {
        System.err.println("Circular reference: " + uri);
    }
    
    /** For statistics gathering during development */
    public static int styleLookupCount = 0 ;

    /**
     * Constant which indicates that the given stylesheet is being parsed. Used
     * to avoid circular references.
     */
    public final static StyleSheet PARSING_SHEET = new StyleSheet();
// END RAVE MODIFICATIONS
// </rave>
    /**
     * Returns the computed style of the given element/pseudo for the
     * property corresponding to the given index.
     */
    public Value getComputedStyle(CSSStylableElement elt,
                                  String pseudo,
                                  int propidx) {
// <rave>
// BEGIN RAVE MODIFICATIONS
        styleLookupCount++;    
// END RAVE MODIFICATIONS
// </rave>

        StyleMap sm = elt.getComputedStyleMap(pseudo);
        if (sm == null) {
            sm = getCascadedStyleMap(elt, pseudo);
            elt.setComputedStyleMap(pseudo, sm);
        }

        Value value = sm.getValue(propidx);
        if (!sm.isComputed(propidx)) {
            Value result = value;
            ValueManager vm = valueManagers[propidx];
            CSSStylableElement p = getParentCSSStylableElement(elt);
            if (value == null && (!vm.isInheritedProperty() || p == null)) {
                result = vm.getDefaultValue();
            } else if (value != null &&
                       (value == InheritValue.INSTANCE) &&
                       p != null) {
                result = null;
            }
            if (result == null) {
                // Value is 'inherit' and p != null.
                // The pseudo class is not propagated.
                result = getComputedStyle(p, null, propidx);
                sm.putParentRelative(propidx, true);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                sm.putInherited(propidx, true);
                // END RAVE MODIFICATIONS
// </rave>
            } else {
                // Maybe is it a relative value.
                result = vm.computeValue(elt, pseudo, this, propidx,
                                         sm, result);
            }
            if (value == null) {
                sm.putValue(propidx, result);
                sm.putNullCascaded(propidx, true);
// <rave>
                // BEGIN RAVE MODIFICATIONS
            } else if (value == InheritValue.INSTANCE) {
                // Do nothing; rather than produce a new ComputedValue,
                // just inherit the value directly. That way we don't
                // end up with "wrapped" versions of values that are
                // part of an IDENT list, such as Vertical Alignments.
                // This breaks checking value == CssValueConstants.FOO_VALUE
                // for example.
                sm.putValue(propidx, result);
                sm.putInherited(propidx, true);
                // END RAVE MODIFICATIONS
// </rave>
            } else if (result != value) {
                ComputedValue cv = new ComputedValue(value);
                cv.setComputedValue(result);
                sm.putValue(propidx, cv);
                result = cv;
            }
            sm.putComputed(propidx, true);
            value = result;
        }
        return value;
    }

    /**
     * Returns the document CSSStyleSheetNodes in a list. This list is
     * updated as the document is modified.
     */
// <nb>
//    public List getStyleSheetNodes() {
// ===
    public List<WeakReference<CSSStyleSheetNode>> getStyleSheetNodes() {
// </nb>
        // XXX #126462 Checking style sheet nodes cache.
        boolean discardOldCache = false;
        if (styleSheetNodes != null) {
            for (WeakReference<CSSStyleSheetNode> styleSheetNodeWRef : styleSheetNodes) {
                CSSStyleSheetNode ssnode = styleSheetNodeWRef == null ? null : styleSheetNodeWRef.get();
                if (ssnode == null) {
                    // The node was garbaged, refresh.
                    discardOldCache = true;
                    break;
                } else if (ssnode instanceof Element) {
                    Element ssElement = (Element)ssnode;
                    if (ssElement.getParentNode() == null) {
                        // The node was probably removed from the document, refresh.
                        discardOldCache = true;
                        break;
                    }
                    // XXX TODO It seems there is always source document present,
                    // even the rendered elements are computed here. (Investigate!).
//                        else if (ssElement.getOwnerDocument() != document) {
//                            // The node doesn't belong to this document, refresh.
//                            discardOldCache = true;
//                            break;
//                        }
                }
            }
        }
        if (discardOldCache) {
            styleSheetNodes = null;
        }
        
        if (styleSheetNodes == null) {
// <nb>
//            styleSheetNodes = new ArrayList();
// ===
            styleSheetNodes = new ArrayList<WeakReference<CSSStyleSheetNode>>();
// </nb>
// <rave>
            if (document == null) {
                return styleSheetNodes;
            }
// </rave>
            selectorAttributes = new HashSet();
            // Find all the style-sheets in the document.
// <rave>
//            findSylesSheetNodes(document);
            findStyleSheetNodes();
// </rave>
            int len = styleSheetNodes.size();
            for (int i = 0; i < len; i++) {
                CSSStyleSheetNode ssn;
// <nb>
//                ssn = (CSSStyleSheetNode)styleSheetNodes.get(i);
// ===
                WeakReference<CSSStyleSheetNode> ssnWRef = styleSheetNodes.get(i);
                ssn = ssnWRef == null ? null : ssnWRef.get();
// </nb
                StyleSheet ss = ssn.getCSSStyleSheet();
                if (ss != null) {
                    findSelectorAttributes(selectorAttributes, ss);
                }
            }
        }
        return styleSheetNodes;
    }

// <rave>
    protected void findStyleSheetNodes() {
        findStyleSheetNodes(document);
    }

    
    /**
     * Try to create a minimal (as short as possible) style string
     * from this map, by using shortcuts when possible.
     * This will for example compress border-color-left/right/bottom/top
     * into border-color: one two three four, and so on.
     *
     * The default implementation just passes serializes the map directly.
     */
    protected String toMinimalStyleString(StyleMap map) {
        return map.toStyleString(this);
    }
// </rave>
    /**
     * An auxiliary method for getStyleSheets().
     */
    protected void findStyleSheetNodes(Node n) {
        if (n instanceof CSSStyleSheetNode) {
// <nb>
//            styleSheetNodes.add(n);
// ===
            styleSheetNodes.add(new WeakReference<CSSStyleSheetNode>((CSSStyleSheetNode)n));
// </nb>
        }
        for (Node nd = n.getFirstChild();
             nd != null;
             nd = nd.getNextSibling()) {
            findStyleSheetNodes(nd);
        }
    }

    /**
     * Finds the selector attributes in the given stylesheet.
     */
    protected void findSelectorAttributes(Set attrs, StyleSheet ss) {
        int len = ss.getSize();
        for (int i = 0; i < len; i++) {
            Rule r = ss.getRule(i);
            switch (r.getType()) {
            case StyleRule.TYPE:
                StyleRule style = (StyleRule)r;
                SelectorList sl = style.getSelectorList();
                int slen = sl.getLength();
                for (int j = 0; j < slen; j++) {
                    ExtendedSelector s = (ExtendedSelector)sl.item(j);
                    s.fillAttributeSet(attrs);
                }
                break;

            case MediaRule.TYPE:
            case ImportRule.TYPE:
                MediaRule mr = (MediaRule)r;
                if (mediaMatch(mr.getMediaList())) {
                    findSelectorAttributes(attrs, mr);
                }
                break;
            }
        }
    }

    /**
     * Interface for people interesting in having 'primary' properties
     * set.  Shorthand properties will be expanded "automatically".
     */
    public interface MainPropertyReceiver {
        /**
         * Called with a non-shorthand property name and it's value.
         */
        public void setMainProperty(String name, Value v, boolean important);
    };

    public void setMainProperties
        (CSSStylableElement elt, final MainPropertyReceiver dst,
         String pname, String value, boolean important){
        try {
            element = elt;
            LexicalUnit lu = parser.parsePropertyValue(value);
            ShorthandManager.PropertyHandler ph =
                new ShorthandManager.PropertyHandler() {
                    public void property(String pname, LexicalUnit lu,
                                         boolean important) {
                        int idx = getPropertyIndex(pname);
                        if (idx != -1) {
                            ValueManager vm = valueManagers[idx];
                            Value v = vm.createValue(lu, CSSEngine.this);
                            dst.setMainProperty(pname, v, important);
                            return;
                        }
                        idx = getShorthandIndex(pname);
                        if (idx == -1)
                            return; // Unknown property...
                        // Shorthand value
// <rave>
// BEGIN RAVE MODIFICATIONS
                        expandingShorthandProperty = idx;
// END RAVE MODIFICATIONS
// </rave>
                        shorthandManagers[idx].setValues
                            (CSSEngine.this, this, lu, important);
// <rave>
// BEGIN RAVE MODIFICATIONS
                        expandingShorthandProperty = -1;
// END RAVE MODIFICATIONS
// </rave>
                    }
                };
            ph.property(pname, lu, important);
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";
            String u = ((documentURI == null)?"<unknown>":
                        documentURI.toString());
            String s = Messages.formatMessage
                ("property.syntax.error.at",
                 new Object[] { u, pname, value, m});
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
        } finally {
            element = null;
            cssBaseURI = null;
        }
    }

    /**
     * Parses and creates a property value from elt.
     * @param elt  The element property is from.
     * @param prop The property name.
     * @param value The property value.
     */
    public Value parsePropertyValue(CSSStylableElement elt,
                                    String prop, String value) {
        int idx = getPropertyIndex(prop);
        if (idx == -1) return null;
        ValueManager vm = valueManagers[idx];
        try {
            element = elt;
            LexicalUnit lu;
            lu = parser.parsePropertyValue(value);
            return vm.createValue(lu, this);
            // BEGIN RAVE MODIFICATIONS
            // XXX when is this used?
            // END RAVE MODIFICATIONS
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";
            String u = ((documentURI == null)?"<unknown>":
                        documentURI.toString());
            String s = Messages.formatMessage
                ("property.syntax.error.at",
                 new Object[] { u, prop, value, m});
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
        } finally {
            element = null;
            cssBaseURI = null;
        }
        return vm.getDefaultValue();
    }

    /**
     * Parses and creates a style declaration.
     * @param value The style declaration text.
     */
    public StyleDeclaration parseStyleDeclaration(CSSStylableElement elt,
                                                  String value) {
        styleDeclarationBuilder.styleDeclaration = new StyleDeclaration();
        try {
            element = elt;
            parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
            parser.setConditionFactory(cssConditionFactory);
            parser.setDocumentHandler(styleDeclarationBuilder);
// <rave>
            // BEGIN RAVE MODIFICATIONS
            styleDeclarationBuilder.location = elt;
            styleDeclarationBuilder.lineno = -1;
            // END RAVE MODIFICATIONS
// </rave>
            parser.parseStyleDeclaration(value);
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";
            String u = ((documentURI == null)?"<unknown>":
                        documentURI.toString());
            String s = Messages.formatMessage
                ("syntax.error.at", new Object[] { u, m });
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
        } finally {
            element = null;
            cssBaseURI = null;
        }
        return styleDeclarationBuilder.styleDeclaration;
    }

    /**
     * Parses and creates a new style-sheet.
     * @param uri The style-sheet URI.
     * @param media The target media of the style-sheet.
     */
// <rave>
    // BEGIN RAVE MODIFICATIONS
//    public StyleSheet parseStyleSheet(URL uri, String media)
    public StyleSheet parseStyleSheet(URL uri, String media, Object location)
    // END RAVE MODIFICATIONS
// </rave>
        throws DOMException {
// <rave>
// BEGIN RAVE MODIFICATIONS
        //StyleSheet ss = new StyleSheet();
        StyleSheetCache cache = StyleSheetCache.getInstance();
        StyleSheet ss = cache.get(uri);
        if (ss == PARSING_SHEET) {
            warnCircularReference(uri, location);
            return new StyleSheet();
        } else if (ss != null) {
            return ss;
        }
        StyleSheet parsedSheet = null;
        ss = new StyleSheet();
        try {
            cache.put(uri, PARSING_SHEET);
// END RAVE MODIFICATIONS
// </rave>
        try {
            ss.setMedia(parser.parseMedia(media));
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";
            String u = ((documentURI == null)?"<unknown>":
                        documentURI.toString());
            String s = Messages.formatMessage
                ("syntax.error.at", new Object[] { u, m });
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
            return ss;
        }
// <rave>
// BEGIN RAVE MODIFICATIONS
//        parseStyleSheet(ss, uri);
        parseStyleSheet(ss, uri, location);
            parsedSheet = ss;
        } finally {
            cache.put(uri, parsedSheet);
        }
// END RAVE MODIFICATIONS
// </rave>
        return ss;
    }

    /**
     * Parses and creates a new style-sheet.
     * @param is The input source used to read the document.
     * @param uri The base URI.
     * @param media The target media of the style-sheet.
     */
// <rave>
// BEGIN RAVE MODIFICATIONS
//    public StyleSheet parseStyleSheet(InputSource is, URL uri, String media)
    public StyleSheet parseStyleSheet(InputSource is, URL uri, String media, Object location)
// END RAVE MODIFICATIONS
// </rave>
        throws DOMException {

// <rave>
// BEGIN RAVE MODIFICATIONS
        //StyleSheet ss = new StyleSheet();
        StyleSheetCache cache = StyleSheetCache.getInstance();
        StyleSheet ss = cache.get(uri);
        if (ss == PARSING_SHEET) {
            warnCircularReference(uri, location);
            return new StyleSheet();
        } else if (ss != null) {
            return ss;
        }
        StyleSheet parsedSheet = null;
        ss = new StyleSheet();
        cache.put(uri, PARSING_SHEET);
// END RAVE MODIFICATIONS
// </rave>
        try {
            ss.setMedia(parser.parseMedia(media));
// <rave>
    // BEGIN RAVE MODIFICATIONS
//            parseStyleSheet(ss, is, uri);
            parseStyleSheet(ss, is, uri, location);
            parsedSheet = ss;
// END RAVE MODIFICATIONS
// </rave>
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";
            String u = ((documentURI == null)?"<unknown>":
                        documentURI.toString());
            String s = Messages.formatMessage
                ("syntax.error.at", new Object[] { u, m });
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
// <rave>
        } finally {
            cache.put(uri, parsedSheet);
// </rave>
        }
        return ss;
    }

    /**
     * Parses and fills the given style-sheet.
     * @param ss The stylesheet to fill.
     * @param uri The base URI.
     */
// <rave>
    // BEGIN RAVE MODIFICATIONS
//    public void parseStyleSheet(StyleSheet ss, URL uri) throws DOMException {
    public void parseStyleSheet(StyleSheet ss, URL uri, Object location) throws DOMException {
    // END RAVE MODIFICATIONS
// </rave>
        if (uri == null) {
            String s = Messages.formatMessage
                ("syntax.error.at",
                 new Object[] { "Null Document reference", "" });
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
            if (userAgent == null) throw de;
            userAgent.displayError(de);
            return;
        }

	try {
            // Check that access to the uri is allowed
// <rave>
    // BEGIN RAVE MODIFICATIONS
            // We don't check external resources so no need to parse
            // and process url
//             ParsedURL pDocURL = null;
//             if (documentURI != null) {
//                 pDocURL = new ParsedURL(documentURI);
//             }
//             ParsedURL pURL = new ParsedURL(uri);
//             cssContext.checkLoadExternalResource(pURL, pDocURL);
    // END RAVE MODIFICATIONS
// </rave>
             
// <rave>
    // BEGIN RAVE MODIFICATIONS
//             parseStyleSheet(ss, new InputSource(uri.toString()), uri);
             parseStyleSheet(ss, new InputSource(uri.toString()), uri, location);
    // END RAVE MODIFICATIONS
// </rave>
	} catch (SecurityException e) {
            throw e; 
        } catch (Exception e) {
// <rave>
            if (e instanceof CSSException && ((CSSException)e).getException() instanceof java.io.IOException) {
                displayMissingStyleSheet(uri.toString());
                return;
            }
// </rave>
            String m = e.getMessage();
            if (m == null) m = "";
            String s = Messages.formatMessage
                ("syntax.error.at", new Object[] { uri.toString(), m });
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
        }
    }

    /**
     * Parses and creates a new style-sheet.
     * @param rules The style-sheet rules to parse.
     * @param uri The style-sheet URI.
     * @param media The target media of the style-sheet.
     */
// <rave>
    // BEGIN RAVE MODIFICATIONS
//    public StyleSheet parseStyleSheet(String rules, URL uri, String media)
    public StyleSheet parseStyleSheet(String rules, URL uri, String media, Object location)
    // END RAVE MODIFICATIONS
// </rave>
        throws DOMException {
        StyleSheet ss = new StyleSheet();
// <rave>
// BEGIN RAVE MODIFICATIONS
        // Can we cache by string values too?
        //StyleSheet ss = StyleSheetCache.getInstance().get(rules);
        //if (ss != null) {
        //    return ss;
        //}
        //ss = new StyleSheet();
// END RAVE MODIFICATIONS
// </rave>
        try {
            ss.setMedia(parser.parseMedia(media));
        } catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";
            String u = ((documentURI == null)?"<unknown>":
                        documentURI.toString());
            String s = Messages.formatMessage
                ("syntax.error.at", new Object[] { u, m });
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
            return ss;
        }
// <rave>
    // BEGIN RAVE MODIFICATIONS
//        parseStyleSheet(ss, rules, uri);
        parseStyleSheet(ss, rules, uri, location);
        //StyleSheetCache.getInstance().put(rules, ss);
// END RAVE MODIFICATIONS
// </rave>
        return ss;
    }

    /**
     * Parses and fills the given style-sheet.
     * @param ss The stylesheet to fill.
     * @param rules The style-sheet rules to parse.
     * @param uri The base URI.
     */
    public void parseStyleSheet(StyleSheet ss,
                                String rules,
// <rave>
    // BEGIN RAVE MODIFICATIONS
//                                URL uri) throws DOMException {
                                URL uri, 
                                Object location) throws DOMException {
    // END RAVE MODIFICATIONS
// </rave>
        try {
            parseStyleSheet(ss, new InputSource(new StringReader(rules)), uri
            // BEGIN RAVE MODIFICATIONS
            , location
            // END RAVE MODIFICATIONS
            );
	} catch (Exception e) {
            String m = e.getMessage();
            if (m == null) m = "";

// <rave>
//            String s = Messages.formatMessage
//                    ("stylesheet.syntax.error",
//                    new Object[] { uri.toString(), rules, m });
            String s = "";
            if(uri == null){
                s = Messages.formatMessage
                    ("stylesheet.syntax.error",
                     new Object[] { "None", rules, m });
            }else{     
                s = Messages.formatMessage
                    ("stylesheet.syntax.error",
                     new Object[] { uri.toString(), rules, m });
            }
// </rave>
            DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
            de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
            if (userAgent == null) throw de;
            userAgent.displayError(de);
        }
    }

    /**
     * Parses and fills the given style-sheet.
     * @param ss The stylesheet to fill.
     * @param uri The base URI.
     */
// <rave>
    // BEGIN RAVE MODIFICATIONS
//    protected void parseStyleSheet(StyleSheet ss, InputSource is, URL uri)
    protected void parseStyleSheet(StyleSheet ss, InputSource is, URL uri, Object location)
    // END RAVE MODIFICATIONS
// </rave>
        throws IOException {
        parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
        parser.setConditionFactory(cssConditionFactory);
        try {
            cssBaseURI = uri;
            styleSheetDocumentHandler.styleSheet = ss;
            parser.setDocumentHandler(styleSheetDocumentHandler);
            // BEGIN RAVE MODIFICATIONS
            // Set up some context. This will allow values to track their
            // source file and line number
            //styleSheetDocumentHandler.location = uri;
            styleSheetDocumentHandler.location = location;
            styleSheetDocumentHandler.lineno = 0;
            // END RAVE MODIFICATIONS
            parser.parseStyleSheet(is);

            // Load the imported sheets.
            int len = ss.getSize();
            for (int i = 0; i < len; i++) {
                Rule r = ss.getRule(i);
                if (r.getType() != ImportRule.TYPE) {
                    // @import rules must be the first rules.
                    break;
                }
                ImportRule ir = (ImportRule)r;
// <rave>
// BEGIN RAVE MODIFICATIONS
                StyleSheetCache cache = StyleSheetCache.getInstance();
                StyleSheet sheet = cache.get(ir.getURI());
                if (sheet == PARSING_SHEET) {
                    warnCircularReference(ir.getURI(), location);
                    continue;
                }
                // TODO - just populate the cached styles from
                // sheet into this stylesheet? (ss)
//                parseStyleSheet(ir, ir.getURI());
                parseStyleSheet(ir, ir.getURI(), ir.getURI());
// END RAVE MODIFICATIONS
// </rave>
            }
        } finally {
            cssBaseURI = null;
        }
    }

    /**
     * Puts an author property from a style-map in another style-map,
     * if possible.
     */
    protected void putAuthorProperty(StyleMap dest,
                                     int idx,
                                     Value sval,
                                     boolean imp,
                                     short origin) {
        Value   dval = dest.getValue(idx);
        short   dorg = dest.getOrigin(idx);
        boolean dimp = dest.isImportant(idx);

        boolean cond = dval == null;
        if (!cond) {
            switch (dorg) {
            case StyleMap.USER_ORIGIN:
                cond = !dimp;
                break;
            case StyleMap.AUTHOR_ORIGIN:
                cond = !dimp || imp;
                break;
            default:
                cond = true;
            }
        }

        if (cond) {
            dest.putValue(idx, sval);
            dest.putImportant(idx, imp);
            dest.putOrigin(idx, origin);
        }
    }
// <rave>    
    // BEGIN RAVE MODIFICATIONS
    private void addCandidateRules(List rules, Map map, String key, Element elt, String pseudo) {
        ExtendedSelector[] rs = (ExtendedSelector[])map.get(key);
        if (rs != null) {
            for (int i = 0; i < rs.length; i++) {
                ExtendedSelector s = rs[i];
                if (s.match(elt, pseudo)) {
                    Rule r = s.getRule();
                    assert r.getType() == StyleRule.TYPE;
                    rules.add(r);
                }
            }
        }
    }

    private Comparator ruleComparator = new Comparator() {
        public int compare(Object object1, Object object2) {
            Rule rule1 = (Rule)object1;
            Rule rule2 = (Rule)object2;
            return rule1.getPosition() - rule2.getPosition();
        }
    };
    // END RAVE MODIFICATIONS
// </rave>
    /**
     * Adds the rules matching the element/pseudo-element of given style
     * sheet to the list.
     */
    protected void addMatchingRules(List rules,
                                    StyleSheet ss,
                                    Element elt,
                                    String pseudo) {
// <rave>
        // BEGIN RAVE MODIFICATIONS
        // addMatchingRules was the hottest method in a full page refresh.
        // On a Braveheart page, it's extremely long. The reason for that
        // of course is that there's a huge number of rules in the stylesheet -
        // over 700, where EACH has a number of selectors, and each selector
        // may even do node iteration, and these are then checked for every
        // document element! For complicated html pages like MyYahoo, it
        // spent 40 seconds in rule matching for the dom elements!
        // This clearly requires an optimization. I'm using one described by
        // David Hyatt in a blog, where the rule set is partitioned into
        // rules that can be quickly checked based on the tag name, the class
        // attribute, and the element id. Rules that don't fit in these buckets
        // are then checked in the normal, linear way. For the tag/class/id
        // rules, they are stored in hashmaps keyed by the tagname/class/id,
        // so for a given element we can quickly look up all rules that affect
        // that tag - and more importantly, ignore rules that pertain only
        // to OTHER tags!
        // This yields a huge performance benefit -- 7x-8x in my measurements.
        // There are some complications to worry about, such as needing to
        // sort the rules according to their stylesheet order, to preserve the
        // right cascade semantics. These are handled below.
        // Note that the stylesheet initialization code, which sets up these
        // data structures, is run only once. I've deliberately done more 
        // computation there which should help speed up this hot method; e.g.
        // the data structures are typed arrays rather than generic Object
        // collections, etc.
        if (RULE_FILTERING) {
            List newRules = new ArrayList(ss.getSize());
            
            Map tagMap = ss.getTagMap();
            if (tagMap != null) {
                String tagName = elt.getTagName();
                addCandidateRules(newRules, tagMap, tagName, elt, pseudo);
            }

            Map classMap = ss.getClassMap();
            if (classMap != null) {
                String styleClass = elt.getAttribute("class");
                if (styleClass.length() == 0) {
                } else if (styleClass.indexOf(' ') == -1) {
                    addCandidateRules(newRules, classMap, styleClass, elt, pseudo);
                } else {
                    int begin = 0;
                    int length = styleClass.length();
                    while (begin < length) {
                        while (begin < length && Character.isSpace(styleClass.charAt(begin))) {
                            begin++;
                        }
                        int end = begin+1;
                        while (end < length && !Character.isSpace(styleClass.charAt(end))) {
                            end++;
                        }
                        if (begin < length && end > begin) {
                            addCandidateRules(newRules, classMap, styleClass.substring(begin, end), elt, pseudo);
                        }
                        begin = end+1;
                    }
                }
            }

            Map idMap = ss.getIdMap();
            if (idMap != null) {
                String id = elt.getAttribute("id");
                if (id.length() > 0) {
                    addCandidateRules(newRules, idMap, id, elt, pseudo);
                }
            }
    
            Rule[] rs = ss.getRemainingRules();
            if (rs != null) {
                for (int i = 0; i < rs.length; i++) {
                    Rule r = rs[i];
                    switch (r.getType()) {
                    case StyleRule.TYPE:
                        StyleRule style = (StyleRule)r;
                        SelectorList sl = style.getSelectorList();
                        int slen = sl.getLength();
                        for (int j = 0; j < slen; j++) {
                            ExtendedSelector s = (ExtendedSelector)sl.item(j);
                            if (s.match(elt, pseudo)) {
                                newRules.add(style);
                            }
                        }
                        break;

                    case MediaRule.TYPE:
                    case ImportRule.TYPE:
                        MediaRule mr = (MediaRule)r;
                        if (mediaMatch(mr.getMediaList())) {
                            addMatchingRules(newRules, mr, elt, pseudo);
                        }
                        break;
                    }
                }
                
                // Sort the rules by order, since we've split up the order when
                // separating out tag name, attributes, etc.
                int size = newRules.size();
                if (size > 0) {
                    if (size > 1) {
                        Collections.sort(newRules, ruleComparator);
                    }
                    
                    Object prev = null;
                    Iterator it = newRules.iterator();
                    while (it.hasNext()) {
                        Object o = it.next();
                        if (o != prev) {
                            rules.add(o);
                            prev = o;
                        }
                    }
                }
                
                return;
            }
        }
        // END RAVE MODIFICATIONS
// </rave>
        int len = ss.getSize();
        for (int i = 0; i < len; i++) {
            Rule r = ss.getRule(i);
            switch (r.getType()) {
            case StyleRule.TYPE:
                StyleRule style = (StyleRule)r;
                SelectorList sl = style.getSelectorList();
                int slen = sl.getLength();
                for (int j = 0; j < slen; j++) {
                    ExtendedSelector s = (ExtendedSelector)sl.item(j);
                    if (s.match(elt, pseudo)) {
                        rules.add(style);
                    }
                }
                break;

            case MediaRule.TYPE:
            case ImportRule.TYPE:
                MediaRule mr = (MediaRule)r;
                if (mediaMatch(mr.getMediaList())) {
                    addMatchingRules(rules, mr, elt, pseudo);
                }
                break;
            }
        }
    }

    /**
     * Adds the rules contained in the given list to a stylemap.
     */
    protected void addRules(Element elt,
                            String pseudo,
                            StyleMap sm,
                            List rules,
                            short origin) {
        sortRules(rules, elt, pseudo);
        int rlen = rules.size();

        if (origin == StyleMap.AUTHOR_ORIGIN) {
            for (int r = 0; r < rlen; r++) {
                StyleRule sr = (StyleRule)rules.get(r);
                StyleDeclaration sd = sr.getStyleDeclaration();
                int len = sd.size();
                for (int i = 0; i < len; i++) {
                    putAuthorProperty(sm,
                                      sd.getIndex(i),
                                      sd.getValue(i),
                                      sd.getPriority(i),
                                      origin);
                }
            }
        } else {
            for (int r = 0; r < rlen; r++) {
                StyleRule sr = (StyleRule)rules.get(r);
                StyleDeclaration sd = sr.getStyleDeclaration();
                int len = sd.size();
                for (int i = 0; i < len; i++) {
                    int idx = sd.getIndex(i);
                    sm.putValue(idx, sd.getValue(i));
                    sm.putImportant(idx, sd.getPriority(i));
                    sm.putOrigin(idx, origin);
                }
            }
        }
    }

    /**
     * Sorts the rules matching the element/pseudo-element of given style
     * sheet to the list.
     */
    protected void sortRules(List rules, Element elt, String pseudo) {
        int len = rules.size();
        for (int i = 0; i < len - 1; i++) {
            int idx = i;
            int min = Integer.MAX_VALUE;
            for (int j = i; j < len; j++) {
                StyleRule r = (StyleRule)rules.get(j);
                SelectorList sl = r.getSelectorList();
                int spec = 0;
                int slen = sl.getLength();
                for (int k = 0; k < slen; k++) {
                    ExtendedSelector s = (ExtendedSelector)sl.item(k);
                    if (s.match(elt, pseudo)) {
                        int sp = s.getSpecificity();
                        if (sp > spec) {
                            spec = sp;
                        }
                    }
                }
                if (spec < min) {
                    min = spec;
                    idx = j;
                }
            }
            if (i != idx) {
                Object tmp = rules.get(i);
                rules.set(i, rules.get(idx));
                rules.set(idx, tmp);
            }
        }
    }

    /**
     * Whether the given media list matches the media list of this
     * CSSEngine object.
     */
    protected boolean mediaMatch(SACMediaList ml) {
	if (media == null ||
            ml == null ||
            media.getLength() == 0 ||
            ml.getLength() == 0) {
	    return true;
	}
	for (int i = 0; i < ml.getLength(); i++) {
            if (ml.item(i).equalsIgnoreCase("all"))
                return true;
	    for (int j = 0; j < media.getLength(); j++) {
		if (media.item(j).equalsIgnoreCase("all") ||
                    ml.item(i).equalsIgnoreCase(media.item(j))) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * To parse a style declaration.
     */
    protected class StyleDeclarationDocumentHandler
        extends DocumentAdapter
        implements ShorthandManager.PropertyHandler {
        public StyleMap styleMap;
        
// <rave>
        // BEGIN RAVE MODIFICATIONS
        Object location;
        int lineno;
        // END RAVE MOFIFICATIONS
// </rave>
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#property(String,LexicalUnit,boolean)}.
         */
        public void property(String name, LexicalUnit value, boolean important)
            throws CSSException {
            int i = getPropertyIndex(name);
            if (i == -1) {
                i = getShorthandIndex(name);
                if (i == -1) {
                    // Unknown property
// <rave>
                    // BEGIN RAVE MODIFICATIONS
                    if (unknownPropertyNames != null) {
                        unknownPropertyNames.add(name);
                        StringBuffer sb = new StringBuffer(50);
                        appendCss(sb, value);
                        unknownPropertyValues.add(sb.toString());
                    }
                    // END RAVE MODIFICATIONS
// </rave>
                    return;
                }
// <rave>
                // BEGIN RAVE MODIFICATIONS
                try {
                    expandingShorthandProperty = i;
                // END RAVE MODIFICATIONS
// </rave>
                shorthandManagers[i].setValues(CSSEngine.this,
                                               this,
                                               value,
                                               important);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                    expandingShorthandProperty = -1;
                } catch (DOMException e) {
                    // Something bad happened
                    displayError(e, location, lineno+parser.getLine()-1,
                                 parser.getColumn());
                    // Continue processing - we're supposed to ignore
                    // errant declarations!!!
                }
                // END RAVE MODIFICATIONS
// </rave>
            } else {
// <rave>
                // BEGIN RAVE MODIFICATIONS
                // Add error handling: don't abort, and send errors to
                // an output window instead via the engine
                try {
                // END RAVE MODIFICATIONS
// </rave>

                Value v = valueManagers[i].createValue(value, CSSEngine.this);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                // Track the value source
                if (v instanceof AbstractValue) { // Should I add to Value interface???
                    AbstractValue av = (AbstractValue)v;
                    av.setLocation(location);
                    av.setLineNumber(lineno+parser.getLine()-1);
                }
                // END RAVE MODIFICATIONS
// </rave>
                putAuthorProperty(styleMap, i, v, important,
                                  StyleMap.INLINE_AUTHOR_ORIGIN);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                } catch (DOMException e) {
                    // Something bad happened
                    displayError(e, location, lineno+parser.getLine()-1,
                                 parser.getColumn());
                    // Continue processing - we're supposed to ignore
                    // errant declarations!!!
                }
                // END RAVE MODIFICATIONS
// </rave>
            }
        }
    }

    /**
     * To build a StyleDeclaration object.
     */
    protected class StyleDeclarationBuilder
        extends DocumentAdapter
        implements ShorthandManager.PropertyHandler {
        public StyleDeclaration styleDeclaration;
// <rave>
        // BEGIN RAVE MODIFICATIONS
        Object location;
        int lineno;
        // ENBD RAVE MODIFICATIONS
// </rave>
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#property(String,LexicalUnit,boolean)}.
         */
        public void property(String name, LexicalUnit value, boolean important)
            throws CSSException {
            int i = getPropertyIndex(name);
            if (i == -1) {
                i = getShorthandIndex(name);
                if (i == -1) {
                    // Unknown property
// <rave>
                    // BEGIN RAVE MODIFICATIONS
                    if (unknownPropertyNames != null) {
                        unknownPropertyNames.add(name);
                        StringBuffer sb = new StringBuffer(50);
                        appendCss(sb, value);
                        unknownPropertyValues.add(sb.toString());
                    }
                    // END RAVE MODIFICATIONS
// </rave>
                    return;
                }
// <rave>
                // BEGIN RAVE MODIFICATIONS
                try {
                    expandingShorthandProperty = i;
                // END RAVE MODIFICATIONS
// </rave>
                shorthandManagers[i].setValues(CSSEngine.this,
                                               this,
                                               value,
                                               important);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                    expandingShorthandProperty = -1;
                } catch (DOMException e) {
                    // Something bad happened
                    displayError(e, location, lineno+parser.getLine()-1,
                                 parser.getColumn());
                    // Continue processing - we're supposed to ignore
                    // errant declarations!!!
                }
                // END RAVE MODIFICATIONS
// </rave>                
            } else {
// <rave>
                // BEGIN RAVE MODIFICATIONS
                // Add error handling: don't abort, and send errors to
                // an output window instead via the engine
                try {
                // END RAVE MODIFICATIONS
// </rave>

                Value v = valueManagers[i].createValue(value, CSSEngine.this);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                // Track the value source
                if (v instanceof AbstractValue) { // Should I add to Value interface???
                    AbstractValue av = (AbstractValue)v;
                    av.setLocation(location);
                    av.setLineNumber(lineno+parser.getLine()-1);
                }
                // END RAVE MODIFICATIONS
// </rave>
                styleDeclaration.append(v, i, important);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                } catch (DOMException e) {
                    // Something bad happened
                    displayError(e, location, lineno+parser.getLine()-1,
                                 parser.getColumn());
                    // Continue processing - we're supposed to ignore
                    // errant declarations!!!
                }
                // END RAVE MODIFICATIONS
// </rave>
            }
        }
    }

    /**
     * To parse a style sheet.
     */
    protected class StyleSheetDocumentHandler
        extends DocumentAdapter
        implements ShorthandManager.PropertyHandler {
        public StyleSheet styleSheet;
// <rave>
        // BEGIN RAVE MODIFICATIONS
        Object location;
        int lineno;
        // END RAVE MODIFICATIONS
// </rave>
        protected StyleRule styleRule;
        protected StyleDeclaration styleDeclaration;

        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#startDocument(InputSource)}.
         */
        public void startDocument(InputSource source)
            throws CSSException {
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#endDocument(InputSource)}.
         */
        public void endDocument(InputSource source) throws CSSException {
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#ignorableAtRule(String)}.
         */
        public void ignorableAtRule(String atRule) throws CSSException {
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#importStyle(String,SACMediaList,String)}.
         */
        public void importStyle(String       uri,
                                SACMediaList media, 
                                String       defaultNamespaceURI)
            throws CSSException {
            ImportRule ir = new ImportRule();
// <rave>
            // BEGIN RAVE MODIFICATIONS
            ir.setRelativeUri(uri);
            // END RAVE MODIFICATIONS
// </rave>
            ir.setMediaList(media);
            ir.setParent(styleSheet);
            try {
                URL base = getCSSBaseURI();
                URL url;
                if (base == null) url = new URL(uri);
                else              url = new URL(base, uri);
                ir.setURI(url);
            } catch (MalformedURLException e) {
            }
            styleSheet.append(ir);
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#startMedia(SACMediaList)}.
         */
        public void startMedia(SACMediaList media) throws CSSException {
            MediaRule mr = new MediaRule();
            mr.setMediaList(media);
            mr.setParent(styleSheet);
            styleSheet.append(mr);
            styleSheet = mr;
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#endMedia(SACMediaList)}.
         */
        public void endMedia(SACMediaList media) throws CSSException {
            styleSheet = styleSheet.getParent();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#startPage(String,String)}.
         */    
        public void startPage(String name, String pseudo_page)
            throws CSSException {
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#endPage(String,String)}.
         */
        public void endPage(String name, String pseudo_page)
            throws CSSException {
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#startFontFace()}.
         */
        public void startFontFace() throws CSSException {
            styleDeclaration = new StyleDeclaration();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#endFontFace()}.
         */
        public void endFontFace() throws CSSException {
// <rave>
            // BEGIN RAVE MODIFICATIONS
            // Add error handling: don't abort, and send errors to
            // an output window instead via the engine
            try {
            // END RAVE MODIFICATIONS
// </rave>

            StyleMap sm = new StyleMap(getNumberOfProperties());
            int len = styleDeclaration.size();
            for (int i=0; i<len; i++) {
                int idx = styleDeclaration.getIndex(i);
                sm.putValue(idx, styleDeclaration.getValue(i));
                sm.putImportant(idx, styleDeclaration.getPriority(i));
                // Not sure on this..
                sm.putOrigin(idx, StyleMap.AUTHOR_ORIGIN); 
            }
            styleDeclaration = null;

            int pidx = getPropertyIndex(CSSConstants.CSS_FONT_FAMILY_PROPERTY);
            Value fontFamily = sm.getValue(pidx);
            if (fontFamily == null) return;

            URL base = getCSSBaseURI();
            ParsedURL purl = null;
            if (base != null) purl = new ParsedURL(base);
            fontFaces.add(new FontFaceRule(sm, purl));
// <rave>
            // BEGIN RAVE MODIFICATIONS
            } catch (DOMException e) {
                // Something bad happened
                //displayError(e, location, lineno);
                displayError(e, location, lineno+parser.getLine()-1,
                             parser.getColumn());

                // Continue processing - we're supposed to ignore
                // errant declarations!!!
            }
            // END RAVE MODIFICATIONS
// </rave>
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#startSelector(SelectorList)}.
         */
        public void startSelector(SelectorList selectors) throws CSSException {
            styleRule = new StyleRule();
            styleRule.setSelectorList(selectors);
            styleDeclaration = new StyleDeclaration();
            styleRule.setStyleDeclaration(styleDeclaration);
            styleSheet.append(styleRule);
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * org.w3c.css.sac.DocumentHandler#endSelector(SelectorList)}.
         */
        public void endSelector(SelectorList selectors) throws CSSException {
            styleRule = null;
            styleDeclaration = null;
        }

        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#property(String,LexicalUnit,boolean)}.
         */
        public void property(String name, LexicalUnit value, boolean important)
            throws CSSException {
            int i = getPropertyIndex(name);
            if (i == -1) {
                i = getShorthandIndex(name);
                if (i == -1) {
                    // Unknown property
                    return;
                }
// <rave>
                // BEGIN RAVE MODIFICATIONS
                try {
                    expandingShorthandProperty = i;
                // END RAVE MODIFICATIONS
// </rave>
                shorthandManagers[i].setValues(CSSEngine.this,
                                               this,
                                               value,
                                               important);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                    expandingShorthandProperty = -1;
                } catch (DOMException e) {
                    // Something bad happened
                    displayError(e, location, lineno+parser.getLine()-1,
                                 parser.getColumn());
                    // Continue processing - we're supposed to ignore
                    // errant declarations!!!
                }
                // END RAVE MODIFICATIONS
// </rave>
            } else {
// <rave>
                // BEGIN RAVE MODIFICATIONS
                // Add error handling: don't abort, and send errors to
                // an output window instead via the engine
                try {
                // END RAVE MODIFICATIONS
// </rave>

                Value v = valueManagers[i].createValue(value, CSSEngine.this);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                // Track the value source
                if (v instanceof AbstractValue) { // Should I add to Value interface???
                    AbstractValue av = (AbstractValue)v;
                    av.setLocation(location);
                    av.setLineNumber(lineno+parser.getLine()-1);
                }
                // END RAVE MODIFICATIONS
// </rave>
                styleDeclaration.append(v, i, important);

// <rave>
                // BEGIN RAVE MODIFICATIONS
                } catch (DOMException e) {
                    // Something bad happened
                    displayError(e, location, lineno+parser.getLine()-1,
                                 parser.getColumn());
                    // Continue processing - we're supposed to ignore
                    // errant declarations!!!
                }
                // END RAVE MODIFICATIONS
// </rave>
            }
        }
    }

    /**
     * Provides an adapter for the DocumentHandler interface.
     */
    protected static class DocumentAdapter implements DocumentHandler {

        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#startDocument(InputSource)}.
         */
        public void startDocument(InputSource source)
            throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#endDocument(InputSource)}.
         */
        public void endDocument(InputSource source) throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#comment(String)}.
         */
        public void comment(String text) throws CSSException {
            // We always ignore the comments.
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#ignorableAtRule(String)}.
         */
        public void ignorableAtRule(String atRule) throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#namespaceDeclaration(String,String)}.
         */
        public void namespaceDeclaration(String prefix, String uri) 
            throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#importStyle(String,SACMediaList,String)}.
         */
        public void importStyle(String       uri,
                                SACMediaList media, 
                                String       defaultNamespaceURI)
            throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#startMedia(SACMediaList)}.
         */
        public void startMedia(SACMediaList media) throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#endMedia(SACMediaList)}.
         */
        public void endMedia(SACMediaList media) throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#startPage(String,String)}.
         */    
        public void startPage(String name, String pseudo_page)
            throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#endPage(String,String)}.
         */
        public void endPage(String name, String pseudo_page)
            throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link DocumentHandler#startFontFace()}.
         */
        public void startFontFace() throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link DocumentHandler#endFontFace()}.
         */
        public void endFontFace() throws CSSException {
            throw new InternalError();
        }
        
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#startSelector(SelectorList)}.
         */
        public void startSelector(SelectorList selectors) throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#endSelector(SelectorList)}.
         */
        public void endSelector(SelectorList selectors) throws CSSException {
            throw new InternalError();
        }
    
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#property(String,LexicalUnit,boolean)}.
         */
        public void property(String name, LexicalUnit value, boolean important)
            throws CSSException {
            throw new InternalError();
        }
    }

    // CSS events /////////////////////////////////////////////////////////
    
    protected final static CSSEngineListener[] LISTENER_ARRAY =
        new CSSEngineListener[0];

    /**
     * Adds a CSS engine listener.
     */
    public void addCSSEngineListener(CSSEngineListener l) {
        listeners.add(l);
    }

    /**
     * Removes a CSS engine listener.
     */
    public void removeCSSEngineListener(CSSEngineListener l) {
        listeners.remove(l);
    }

    /**
     * Fires a CSSEngineEvent, given a list of modified properties.
     */
    protected void firePropertiesChangedEvent(Element target, int[] props) {
        CSSEngineListener[] ll =
            (CSSEngineListener[])listeners.toArray(LISTENER_ARRAY);

        int len = ll.length;
        if (len > 0) {
            CSSEngineEvent evt = new CSSEngineEvent(this, target, props);
            for (int i = 0; i < len; i++) {
                ll[i].propertiesChanged(evt);
            }
        }
    }

    // Dynamic updates ////////////////////////////////////////////////////
    
    /**
     * Called when the inline style of the given element has been updated.
     */
    protected void inlineStyleAttributeUpdated(CSSStylableElement elt,
                                               StyleMap style,
                                               MutationEvent evt) {
        boolean[] updated = styleDeclarationUpdateHandler.updatedProperties;
        for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
            updated[i] = false;
        }

        switch (evt.getAttrChange()) {
        case MutationEvent.ADDITION:
        case MutationEvent.MODIFICATION:
            String decl = evt.getNewValue();
            // System.err.println("Inline Style Update: '" + decl + "'");
            if (decl.length() > 0) {
                element = elt;
                try {
                    parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
                    parser.setConditionFactory(cssConditionFactory);
                    styleDeclarationUpdateHandler.styleMap = style;
                    parser.setDocumentHandler(styleDeclarationUpdateHandler);
// <rave>
                    // BEGIN RAVE MODIFICATIONS
                    styleDeclarationUpdateHandler.location = elt;
                    styleDeclarationUpdateHandler.lineno = -1;
                    // END RAVE MODIFICATIONS
// </rave>
                    parser.parseStyleDeclaration(decl);
                    styleDeclarationUpdateHandler.styleMap = null;
                } catch (Exception e) {
                    String m = e.getMessage();
                    if (m == null) m = "";
                    String u = ((documentURI == null)?"<unknown>":
                                documentURI.toString());
                    String s = Messages.formatMessage
                        ("style.syntax.error.at",
                         new Object[] { u, styleLocalName, decl, m });
                    DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
                    de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
                    if (userAgent == null) throw de;
                    userAgent.displayError(de);
                } finally {
                    element = null;
                    cssBaseURI = null;
                }
            }

            // Fall through
        case MutationEvent.REMOVAL:
            boolean removed = false;

            if (evt.getPrevValue() != null &&
                evt.getPrevValue().length() > 0) {
                // Check if the style map has cascaded styles which
                // come from the inline style attribute.
                for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
                    if (style.isComputed(i) &&
                        style.getOrigin(i) == StyleMap.INLINE_AUTHOR_ORIGIN &&
                        !updated[i]) {
                        removed = true;
                        updated[i] = true;
                    }
                }
            }

            if (removed) {
                invalidateProperties(elt, null, updated, true);
            } else {
                int count = 0;
                // Invalidate the relative values
                boolean fs = (fontSizeIndex == -1)
                    ? false
                    : updated[fontSizeIndex];
                boolean lh = (lineHeightIndex == -1)
                    ? false
                    : updated[lineHeightIndex];
                boolean cl = (colorIndex == -1)
                    ? false
                    : updated[colorIndex];
                
                for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
                    if (updated[i]) {
                        count++;
                    } 
                    else if ((fs && style.isFontSizeRelative(i)) ||
                             (lh && style.isLineHeightRelative(i)) ||
                             (cl && style.isColorRelative(i))) {
                        updated[i] = true;
                        clearComputedValue(style, i);
                        count++;
                    }
                }

                if (count > 0) {
                    int[] props = new int[count];
                    count = 0;
                    for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
                        if (updated[i]) {
                            props[count++] = i;
                        }
                    }
                    invalidateProperties(elt, props, null, true);
                }
            }
            break;

        default:
            // Must not happen
            throw new InternalError("Invalid attrChangeType");
        }
    }

    private static void clearComputedValue(StyleMap style, int n) {
        if (style.isNullCascaded(n)) {
            style.putValue(n, null);
        } else {
            Value v = style.getValue(n);
            if (v instanceof ComputedValue) {
                ComputedValue cv = (ComputedValue)v;
                v = cv.getCascadedValue();
                style.putValue(n, v);
            }
        }
        style.putComputed(n, false);
    }


    /**
     * Invalidates all the properties of the given node.
     * 
     */
    protected void invalidateProperties(Node node, 
                                        int [] properties, 
                                        boolean [] updated,
                                        boolean recascade) {

        if (!(node instanceof CSSStylableElement))
            return;  // Not Stylable sub tree

        CSSStylableElement elt = (CSSStylableElement)node;
        StyleMap style = elt.getComputedStyleMap(null);
        if (style == null)
            return;  // Nothing to invalidate.
        
        boolean [] diffs = new boolean[getNumberOfProperties()];
        if (updated != null) {
            for (int i=0; i< updated.length; i++) {
                diffs[i] = updated[i];
            }
        }
        if (properties != null) {
            for (int i=0; i<properties.length; i++) {
                diffs[properties[i]] = true;
            }
        }
        int count =0;
        if (!recascade) {
            for (int i=0; i<diffs.length; i++) {
                if (diffs[i])
                    count++;
            }
        } else {
            StyleMap newStyle = getCascadedStyleMap(elt, null);
            elt.setComputedStyleMap(null, newStyle);
            for (int i=0; i<diffs.length; i++) {
                if (diffs[i]) {
                    count++;
                    continue; // Already marked changed.
                }

                // Value nv = getComputedStyle(elt, null, i);
                Value nv = newStyle.getValue(i);
                Value ov = null;
                if (!style.isNullCascaded(i)) {
                    ov = style.getValue(i);
                    if (ov instanceof ComputedValue) {
                        ov = ((ComputedValue)ov).getCascadedValue();
                    }
                }

                if (nv == ov) continue;
                if ((nv != null) && (ov != null)) {
                    if (nv.equals(ov)) continue;
                    String ovCssText = ov.getCssText();
                    String nvCssText = nv.getCssText();
                    if ((nvCssText == ovCssText) ||
                        ((nvCssText != null) && nvCssText.equals(ovCssText)))
                        continue;
                }
                count++;
                diffs[i] = true;
            }
        }
        int []props = null;
        if (count != 0) {
            props = new int[count];
            count = 0;
            for (int i=0; i<diffs.length; i++) {
                if (diffs[i])
                    props[count++] = i;
            }
        }
        propagateChanges(elt, props, recascade);
    }

    /**
     * Propagates the changes that occurs on the parent of the given node.
     * Props is a list of known 'changed' properties.
     * If recascade is true then the stylesheets will be applied
     * again to see if the any new rules apply (or old rules don't
     * apply).
     */
    protected void propagateChanges(Node node, int[] props, 
                                    boolean recascade) {
        if (!(node instanceof CSSStylableElement))
            return;
        CSSStylableElement elt = (CSSStylableElement)node;
        StyleMap style = elt.getComputedStyleMap(null);
        if (style != null) {
            boolean[] updated =
                styleDeclarationUpdateHandler.updatedProperties;
            for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
                updated[i] = false;
            }
            if (props != null) {
                for (int i = props.length - 1; i >= 0; --i) {
                    int idx = props[i];
                    updated[idx] = true;
                }
            }

            // Invalidate the relative values
            boolean fs = (fontSizeIndex == -1)
                ? false
                : updated[fontSizeIndex];
            boolean lh = (lineHeightIndex == -1)
                ? false
                : updated[lineHeightIndex];
            boolean cl = (colorIndex == -1)
                ? false
                : updated[colorIndex];

            int count = 0;
            for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
                if (updated[i]) {
                    count++;
                }
                else if ((fs && style.isFontSizeRelative(i)) ||
                         (lh && style.isLineHeightRelative(i)) ||
                         (cl && style.isColorRelative(i))) {
                    updated[i] = true;
                    clearComputedValue(style, i);
                    count++;
                }
            }

            if (count == 0) {
                props = null;
            } else {
                props = new int[count];
                count = 0;
                for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
                    if (updated[i]) {
                        props[count++] = i;
                    }
                }
                firePropertiesChangedEvent(elt, props);
            }
        }

        int [] inherited = props;
        if (props != null) {
            // Filter out uninheritable properties when we 
            // propogate to children.
            int count = 0;
            for (int i=0; i<props.length; i++) {
                ValueManager vm = valueManagers[props[i]];
                if (vm.isInheritedProperty()) count++;
                else props[i] = -1;
            }
            
            if (count == 0) {
                // nothing to propogate for sure
                inherited = null;
            } else {
                inherited = new int[count];
                count=0;
                for (int i=0; i<props.length; i++)
                    if (props[i] != -1)
                        inherited[count++] = props[i];
            }
        }

        CSSImportedElementRoot ier = getImportedChild(node);
        if (ier != null) {
            Node c = ier.getFirstChild();
            // Don't recascade trees that have been imported.
            // If you do it will use the stylesheets from this
            // document instead of the original document.  Also
            // currently there isn't any supported way to modify
            // the content imported from the other document so
            // the cascade can't change.
            invalidateProperties(c, inherited, null, ier.getIsLocal());
        }
        for (Node n = node.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            invalidateProperties(n, inherited, null, recascade);
        }
    }

    /**
     * To parse a style declaration and update a StyleMap.
     */
    protected class StyleDeclarationUpdateHandler
        extends DocumentAdapter
        implements ShorthandManager.PropertyHandler {
        public StyleMap styleMap;
        public boolean[] updatedProperties =
            new boolean[getNumberOfProperties()];

// <rave>
        // BEGIN RAVE MODIFICATIONS
        Object location;
        int lineno;
        // ENBD RAVE MODIFICATIONS
// </rave>
        /**
         * <b>SAC</b>: Implements {@link
         * DocumentHandler#property(String,LexicalUnit,boolean)}.
         */
        public void property(String name, LexicalUnit value, boolean important)
            throws CSSException {
            int i = getPropertyIndex(name);
            if (i == -1) {
                i = getShorthandIndex(name);
                if (i == -1) {
                    // Unknown property
                    return;
                }
// <rave>
                // BEGIN RAVE MODIFICATIONS
                try {
                    expandingShorthandProperty = i;
                // END RAVE MODIFICATIONS
// </rave>
                shorthandManagers[i].setValues(CSSEngine.this,
                                               this,
                                               value,
                                               important);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                    expandingShorthandProperty = -1;
                } catch (DOMException e) {
                    // Something bad happened
                    displayError(e, location, lineno+parser.getLine()-1,
                                 parser.getColumn());
                    // Continue processing - we're supposed to ignore
                    // errant declarations!!!
                }
                // END RAVE MODIFICATIONS
// </rave>
            } else {
                if (styleMap.isImportant(i)) {
                    // The previous value is important, and a value
                    // from a style attribute cannot be important...
                    return;
                }

                updatedProperties[i] = true;

                Value v = valueManagers[i].createValue(value, CSSEngine.this);
// <rave>
                // BEGIN RAVE MODIFICATIONS
                // Track the value source
                if (v instanceof AbstractValue) { // Should I add to Value interface???
                    AbstractValue av = (AbstractValue)v;
                    av.setLocation(location);
                    av.setLineNumber(lineno+parser.getLine()-1);
                }
                // END RAVE MODIFICATIONS
// </rave>
                styleMap.putMask(i, (short)0);
                styleMap.putValue(i, v);
                styleMap.putOrigin(i, StyleMap.INLINE_AUTHOR_ORIGIN);
            }
        }
    }

    /**
     * Called when a non-CSS presentational hint has been updated.
     */
    protected void nonCSSPresentationalHintUpdated(CSSStylableElement elt,
                                                   StyleMap style,
                                                   String property,
                                                   MutationEvent evt) {
        // System.err.println("update: " + property);
        int idx = getPropertyIndex(property);

        if (style.isImportant(idx)) {
            // The current value is important, and a value
            // from an XML attribute cannot be important...
            return;
        }

        switch (style.getOrigin(idx)) {
        case StyleMap.AUTHOR_ORIGIN:
        case StyleMap.INLINE_AUTHOR_ORIGIN:
            // The current value has a greater priority
            return;
        }
        
        switch (evt.getAttrChange()) {
        case MutationEvent.ADDITION:
        case MutationEvent.MODIFICATION:
            element = elt;
            try {
                LexicalUnit lu;
                lu = parser.parsePropertyValue(evt.getNewValue());
                ValueManager vm = valueManagers[idx];
                Value v = vm.createValue(lu, CSSEngine.this);
                style.putMask(idx, (short)0);
                style.putValue(idx, v);
                style.putOrigin(idx, StyleMap.NON_CSS_ORIGIN);
            } catch (Exception e) {
                String m = e.getMessage();
                if (m == null) m = "";
                String u = ((documentURI == null)?"<unknown>":
                            documentURI.toString());
                String s = Messages.formatMessage
                    ("property.syntax.error.at",
                     new Object[] { u, property, evt.getNewValue(), m });
                DOMException de = new DOMException(DOMException.SYNTAX_ERR, s);
// <rave>
// BEGIN RAVE MODIFICATIONS
                de.initCause(e);
// END RAVE MODIFICATIONS
// </rave>
                if (userAgent == null) throw de;
                userAgent.displayError(de);
            } finally {
                element = null;
                cssBaseURI = null;
            }
            break;

        case MutationEvent.REMOVAL: 
            {
                int [] invalid = { idx };
                invalidateProperties(elt, invalid, null, true);
                return;
            }
        }

        boolean[] updated = styleDeclarationUpdateHandler.updatedProperties;
        for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
            updated[i] = false;
        }
        updated[idx] = true;

        // Invalidate the relative values
        boolean fs = idx == fontSizeIndex;
        boolean lh = idx == lineHeightIndex;
        boolean cl = idx == colorIndex;
        int count = 0;

        for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
            if (updated[i]) {
                count++;
            }
            else if ((fs && style.isFontSizeRelative(i)) ||
                     (lh && style.isLineHeightRelative(i)) ||
                     (cl && style.isColorRelative(i))) {
                updated[i] = true;
                clearComputedValue(style, i);
                count++;
            }
        }

        int[] props = new int[count];
        count = 0;
        for (int i = getNumberOfProperties() - 1; i >= 0; --i) {
            if (updated[i]) {
                props[count++] = i;
            }
        }

        invalidateProperties(elt, props, null, true);
    }

    /**
     * To handle the insertion of a CSSStyleSheetNode in the
     * associated document.
     */
    protected class DOMNodeInsertedListener implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget et = evt.getTarget();
            if (et instanceof CSSStyleSheetNode) {
                styleSheetNodes = null;
                // Invalidate all the CSSStylableElements in the document.
                invalidateProperties(document.getDocumentElement(), 
                                     null, null, true);
                return;
            }
            if (et instanceof CSSStylableElement) {
                // Invalidate the CSSStylableElement siblings, to
                // correctly match the adjacent selectors and
                // first-child pseudo-class.
                for (Node n = ((Node)evt.getTarget()).getNextSibling();
                     n != null;
                     n = n.getNextSibling()) {
                    invalidateProperties(n, null, null, true);
                }
            }
        }
    }

    /**
     * To handle the removal of a CSSStyleSheetNode from the
     * associated document.
     */
    protected class DOMNodeRemovedListener implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget et = evt.getTarget();
            if (et instanceof CSSStyleSheetNode) {
                // Wait for the DOMSubtreeModified to do the invalidations
                // because at this time the node is in the tree.
                styleSheetRemoved = true;
            } else if (et instanceof CSSStylableElement) {
                // Wait for the DOMSubtreeModified to do the invalidations
                // because at this time the node is in the tree.
                removedStylableElementSibling = ((Node)et).getNextSibling();
            }
            // Clears the computed styles in the removed tree.
            disposeStyleMaps((Node)et);
        }
    }

    /**
     * To handle the removal of a CSSStyleSheetNode from the
     * associated document.
     */
    protected class DOMSubtreeModifiedListener implements EventListener {
        public void handleEvent(Event evt) {
            if (styleSheetRemoved) {
                styleSheetRemoved = false;
                styleSheetNodes = null;

                // Invalidate all the CSSStylableElements in the document.
                invalidateProperties(document.getDocumentElement(), 
                                     null, null, true);
            } else if (removedStylableElementSibling != null) {
                // Invalidate the CSSStylableElement siblings, to
                // correctly match the adjacent selectors and
                // first-child pseudo-class.
                for (Node n = removedStylableElementSibling;
                     n != null;
                     n = n.getNextSibling()) {
                    invalidateProperties(n, null, null, true);
                }
                removedStylableElementSibling = null;
            }
        }
    }

    /**
     * To handle the modification of a CSSStyleSheetNode.
     */
    protected class DOMCharacterDataModifiedListener implements EventListener {
        public void handleEvent(Event evt) {
            Node n = (Node)evt.getTarget();
            if (n.getParentNode() instanceof CSSStyleSheetNode) {
                styleSheetNodes = null;
                // Invalidate all the CSSStylableElements in the document.
                invalidateProperties(document.getDocumentElement(), 
                                     null, null, true);
            }
        }
    }

    /**
     * To handle the element attributes modification in the associated
     * document.
     */
    protected class DOMAttrModifiedListener implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget et = evt.getTarget();
            if (!(et instanceof CSSStylableElement)) {
                // Not a stylable element.
                return;
            }

            MutationEvent mevt = (MutationEvent)evt;
            if (mevt.getNewValue().equals(mevt.getPrevValue()))
                return;  // no change really...

            Node attr = mevt.getRelatedNode();
            String attrNS = attr.getNamespaceURI();
            String name = (attrNS == null)
                ? attr.getNodeName()
                : attr.getLocalName();
            
            CSSStylableElement elt = (CSSStylableElement)et;
// <rave>
// BEGIN RAVE MODIFICATIONS

            try {

// END RAVE MODIFICATIONS
// </rave>
            StyleMap style = elt.getComputedStyleMap(null);
            if (style != null) {
                if ((attrNS == null && styleNamespaceURI == null) ||
                    (attrNS != null && attrNS.equals(styleNamespaceURI))) {
                    if (name.equals(styleLocalName)) {
                        // The style declaration attribute has been modified.
                        inlineStyleAttributeUpdated(elt, style, mevt);
                        return;
                    }
                }
// <rave>
// BEGIN RAVE MODIFICATIONS
                // Note - we've gotta update this code to do proper
                // nonpresentational hints updates!
// END RAVE MODIFICATIONS
// </rave>
                if (nonCSSPresentationalHints != null) {
                    if ((attrNS == null &&
                         nonCSSPresentationalHintsNamespaceURI == null) ||
                        (attrNS != null &&
                         attrNS.equals(nonCSSPresentationalHintsNamespaceURI))) {
                        if (nonCSSPresentationalHints.contains(name)) {
                            // The 'name' attribute which represents a non CSS
                            // presentational hint has been modified.
                            nonCSSPresentationalHintUpdated(elt, style, name,
                                                            mevt);
                            return;
                        }
                    }
                }
            }

            if (selectorAttributes != null &&
                selectorAttributes.contains(name)) {
                // An attribute has been modified, invalidate all the
                // properties to correctly match attribute selectors.
                invalidateProperties(elt, null, null, true);
                for (Node n = elt.getNextSibling();
                     n != null;
                     n = n.getNextSibling()) {
                    invalidateProperties(n, null, null, true);
                }
            }
// <rave>
// BEGIN RAVE MODIFICATIONS
            } catch (DOMException e) {
                // Something bad happened
                displayError(e, elt, parser.getLine()-1, parser.getColumn());
                // Continue processing - we're supposed to ignore
                // errant declarations!!!

            }
// END RAVE MODIFICATIONS
// </rave>
        }
    }
}
