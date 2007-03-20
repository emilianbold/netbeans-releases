/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.designer.jsf.text;


import java.util.EventListener;
import java.net.URL;
import javax.swing.event.EventListenerList;

import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.DomDocumentEvent;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.DomDocumentListener;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.DomDocument;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;

import org.openide.ErrorManager;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.ranges.DocumentRange;



/**
 * XXX Moved from designer/../Document.
 *
 *  Wrapper object for dom documents; holds some additional
 *  state and document methods.
 *
 * @todo Refactor the EventListener into an adapter class
 * @todo IMPORTANT: I need to cache the font lookup stuff so
 *    I don't keep recomputing it!
 * @author Tor Norbye
 */
public class DomDocumentImpl implements HtmlDomProvider.DomDocument {
//    // DEBUG:
//    // Log info pertaining to document events
//    static final boolean debugevents = false;
//    private WebForm webform;
    private JsfForm jsfForm;

//    private ImageCache imageCache;
//    private DocumentCache frameCache;

//    /** For testsuite use only!!! */
//    public URL testBase;

//    /**
//     * The event listener list for the document.
//     */
//    protected EventListenerList listenerList = new EventListenerList();
    

    // --- Document locking ----------------------------------
//    private UndoEvent undoEvent;

    public DomDocumentImpl(/*WebForm webform*/ JsfForm jsfForm) {
//        this.webform = webform;
        this.jsfForm = jsfForm;

        // Ensure we've got a model
        // TODO - this should be cleaned up
//        webform.getDomSynchronizer();
    }

    public String toString() {
//        if ((webform != null) && (webform.getModel() != null) && (webform.getMarkup() != null) &&
//                (webform.getMarkup().getFileObject() != null)) {
//            return "Document[" + webform.getMarkup().getFileObject().toString() + "]";
//        }

//        return super.toString() + "[webForm=" + webform + "]"; // NOI18N
        return super.toString() + "[jsfForm=" + jsfForm + "]"; // NOI18N
    }

//    public WebForm getWebForm() {
//        return webform;
//    }

//    /**
//     * Returns the location to resolve relative URLs against.  By
//     * default this will be the document's URL if the document
//     * was loaded from a URL.  If a base tag is found and
//     * can be parsed, it will be used as the base location.
//     *
//     * @return the base location
//     */
//    public URL getBase() {
////        URL url = webform.getMarkup().getBase();
//        URL url = webform.getBaseUrl();
//
//        if (url != null) {
//            return url;
//        }
//
//        return testBase;
//    }

    // XXX Moved to Webform.
//    /**
//     * Return a cache of images for this document
//     */
//    public ImageCache getImageCache() {
//        if (imageCache == null) {
//            imageCache = new ImageCache();
//        }
//
//        return imageCache;
//    }
//
//    /**
//     * Return a cache of webform boxes associated with this document
//     * @todo Rename; it's no longer a box cache but rather a document
//     *   cache!
//     */
//    public DocumentCache getFrameBoxCache() {
//        if (frameCache == null) {
//            frameCache = new DocumentCache();
//        }
//
//        return frameCache;
//    }
//
//    /**
//     * Return true iff the document has cached frame boxes
//     */
//    public boolean hasCachedFrameBoxes() {
//        return (frameCache != null) && (frameCache.size() > 0);
//    }
//
//    /**
//     * Clear out caches for a "refresh" operation
//     */
//    public void flushCaches() {
//        if (frameCache != null) {
//            frameCache.flush();
//        }
//
//        if (imageCache != null) {
//            imageCache.flush();
//        }
//    }

//    public void insertString(/*DesignerCaret caret,*/ Position pos, String str) {
    public void insertString(/*DesignerCaret caret,*/ DomPosition pos, String str) {
        // TODO: If you're pressing shift while hitting Enter, we should force a <br/>,
        // and otherwise we should split the current block tag (if there is one, and
        // that block tag is not a <div> or a <body> (for these we always use <br>).
//        assert (pos != null) && (pos != Position.NONE);
        if (pos == null || pos == DomPosition.NONE) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalArgumentException("Invalid position, pos=" + pos)); // NOI18N
            return;
        }

//        Element body = webform.getHtmlBody();
        Element body = jsfForm.getHtmlBody();

        if ((pos.getNode() == body) ||
                ((pos.getNode().getParentNode() == body) &&
                (pos.getNode().getNodeType() == Node.TEXT_NODE) &&
                JsfSupportUtilities.onlyWhitespace(pos.getNode().getNodeValue()))) {
            // Trying to insert text right at the top <body> level.
            // Insert text in a paragraph instead!
            Node next = null;

            if (pos.getNode() == body) {
                next = body.getChildNodes().item(pos.getOffset());
            } else {
                next = pos.getNode().getNextSibling();
            }

            // XXX TODO get rid of using xhtml directly, 
            // it should be shielded by api.
            Element p = createElement(org.netbeans.modules.visualweb.xhtml.P.class.getName(), body, next);
            createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(), p, null);
            
//            pos.setLocation(p, 0, Bias.FORWARD);
//            caret.setDot(new Position(p, 0, Bias.FORWARD));
            fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(p, 0, Bias.FORWARD)));
        }

        if (str.equals("\n") || str.equals("\r\n")) {
            insertNewline(/*caret,*/ pos.getNode(), pos.getOffset());

            return;
        }

        // Can't put "&" directly in source!
//        if (str.equals("&") && !FacesSupport.isHtmlNode(webform, pos.getNode())) {
        if (str.equals("&") && !isHtmlNode(/*webform*/ jsfForm, pos.getNode())) { // NOI18N
            str = "&amp;"; // NOI18N
        }

        Node node = pos.getNode();
        int offset = pos.getOffset();
        Node targetNode = node; // Node to move caret to when we're done
        int targetOffset = offset;

        // TODO - replace <, >, ", etc. with entities
        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            org.w3c.dom.Text text = (org.w3c.dom.Text)node;

            if ((str.length() == 1) && (str.charAt(0) == ' ')) {
                insertSpace(/*caret,*/ pos.getNode(), pos.getOffset());

                // XXX check that this works on Windows too - or do they
                // use \r\n ?
                return;
            } else {
                text.insertData(offset, str);
                targetNode = text;
                targetOffset += str.length();
            }

//            caret.setDot(new Position(targetNode, targetOffset, pos.getBias()));
            fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(targetNode, targetOffset, pos.getBias())));
        } else if ((node.getNodeType() == Node.ELEMENT_NODE) ||
                (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)) {
            NodeList list = node.getChildNodes();
            int len = list.getLength();

            if ((len == 0) || (offset >= len)) {
                // We have a 0 index but no children, e.g. we're
                // inside an empty element.
                // XXX what if it's a newline??
                // Add a text node into the list, as the first
                // child
                // XXX for the <body> tag I should auto insert a <p>
                // too around the text...
//                org.w3c.dom.Document dom = webform.getJspDom();
                org.w3c.dom.Document dom = jsfForm.getJspDom();
                
                Node text = dom.createTextNode(str);

//                if (((RaveRenderNode)node).isJspx()) {
                if (MarkupService.isJspxNode(node)) {
                    MarkupService.markJspxSource(text);
                }

                node.appendChild(text);
                
//                caret.setDot(new Position(text, str.length(), Bias.FORWARD));
                fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(text, str.length(), Bias.FORWARD)));

                return;
            } else if (offset < len) {
                // Insert text before the given sibling;
                // if prev is a text node append to
                // that, otherwise insert a text node there
                if ((offset > 0) && (list.item(offset - 1) instanceof Text)) {
                    org.w3c.dom.CharacterData text =
                        (org.w3c.dom.CharacterData)list.item(offset - 1);
                    text.appendData(str);
                    
//                    caret.setDot(new Position(text, text.getLength(), pos.getBias()));
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(text, text.getLength(), pos.getBias())));

                    return;
                } else {
//                    org.w3c.dom.Document dom = webform.getJspDom();
                    org.w3c.dom.Document dom = jsfForm.getJspDom();
                    
                    Node text = dom.createTextNode(str);

//                    if (((RaveRenderNode)node).isJspx()) {
                    if (MarkupService.isJspxNode(node)) {
                        MarkupService.markJspxSource(text);
                    }

                    Node before = list.item(offset);
                    node.insertBefore(text, before);
                    
//                    caret.setDot(new Position(text, str.length(), Bias.FORWARD));
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(text, str.length(), Bias.FORWARD)));

                    return;
                }
            }
        } else {
            ErrorManager.getDefault().log("Unexpected node: " + offset + ", str=" + str);
        }
    }
    
    // XXX Moved from FacesSupport
    private static boolean isHtmlNode(/*WebForm webform*/ JsfForm jsfForm, Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            node = node.getParentNode();
        }

        if ((node == null) || !(node instanceof Element)) {
            return false;
        }

//        RaveElement element = (RaveElement)node;
//        if (element.isRendered()) {
//            return true;
//        }
        if (MarkupService.isRenderedNode(node)) {
            return true;
        }

//        if (webform.getManager().isInlineEditing()) {
        if (jsfForm.isInlineEditing()) {
            // In inline editing mode we'return modifying an already rendered
            // fragment. It is not marked rendered since in terms of source mapping
            // it's serving as a source dom, returned from FacesPageUnit's render
            // operations etc.
            return true;
        }

        return false;
    }


    /** Any more inline content on this line? */
    private static boolean haveMoreInlineContent(Node node) {
        while (node != null) {
            if (node instanceof Text) {
                return true;
            } else if (node instanceof Element) {
                Element element = (Element)node;
                HtmlTag tag = HtmlTag.getTag(element.getTagName());

                if (tag == HtmlTag.BR) {
                    return true;
                }

//                Value display = CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX);
                CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX);

//                if (ContainerBox.isInlineTag(cssDisplay, element, tag)) {
                if (CssProvider.getValueService().isInlineTag(cssDisplay, element, tag)) {
                    return true;
                }
            }

            node = node.getNextSibling();
        }

        return false;
    }

    /** Insert a newline at the given position: if we're inside a list this
     * means to add a new list item, otherwise we add a &lt;br&gt; (or two, in
     * a few scenarios.)
     */
    private void insertNewline(/*DesignerCaret caret,*/ Node node, int offset) {
        org.w3c.dom.Text text = null;
        Node parent = null;
        Node next = null;

        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            text = (org.w3c.dom.Text)node;
            next = text.getNextSibling();
            parent = node.getParentNode();
        } else {
            parent = node;

            NodeList list = node.getChildNodes();
            int len = list.getLength();

            if ((len == 0) || (offset >= len)) {
                // We have a 0 index but no children, e.g. we're
                // inside an empty element.
                // XXX Can we do anything here?
                next = null;
            } else if (offset < len) {
                next = list.item(offset);
            }
        }

        // Inserted newline: should split string and insert a
        // <br/>  (unless we're inside a list, in that case create
        // a new bullet)
//        Element list = DesignerUtils.getListItemParent(node);
        Element list = getListItemParent(node);

        if (list != null) {
            // XXX TODO get rid of using xhtml directly, 
            // it should be shielded by api.
            Element li = createElement(org.netbeans.modules.visualweb.xhtml.Li.class.getName(),
                    list.getParentNode(), list.getNextSibling());
//            DocumentRange dom = (DocumentRange)webform.getJspDom();
            DocumentRange dom = (DocumentRange)jsfForm.getJspDom();
            
            org.w3c.dom.ranges.Range domRange = dom.createRange();
            domRange.setStart(node, offset);

            // Locate end of the current list item
//            Position listItemEnd = Position.create(list, true);
            DomPosition listItemEnd = DomPositionImpl.create(list, true);
            domRange.setEnd(listItemEnd.getNode(), listItemEnd.getOffset());

            DocumentFragment df = domRange.extractContents();
            domRange.detach();

            NodeList nl = df.getChildNodes();

            if ((nl.getLength() > 0) && nl.item(0) instanceof Element &&
                    ((Element)nl.item(0)).getTagName().equals(HtmlTag.LI.name)) {
                nl = nl.item(0).getChildNodes();
            }

            for (int ch = 0, len = nl.getLength(); ch < len; ch++) {
                Node n = nl.item(ch);

                if (n == null) {
                    continue;
                }

                if (((n.getNodeType() == Node.TEXT_NODE) ||
                        (n.getNodeType() == Node.CDATA_SECTION_NODE)) &&
                        JsfSupportUtilities.onlyWhitespace(n.getNodeValue())) {
                    continue;
                }

                li.appendChild(n);
            }

            if (li.getChildNodes().getLength() == 0) {
                // XXX TODO get rid of using xhtml directly, 
                // it should be shielded by api.
                createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(), li, null);
            }

            // It's possible that we've moved EVERYTHING from the current item
            // in which case we need to put a <br> in there.
            if (node.getChildNodes().getLength() == 0) {
                // XXX TODO get rid of using xhtml directly, 
                // it should be shielded by api.
                createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(), node, null);
            }

//            caret.setDot(new Position(li, 0, Bias.FORWARD));
            fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(li, 0, Bias.FORWARD)));
        } else {
            // TODO - if the offset is 0, or end, we don't have
            // to split!
            Element br;

            if ((text == null) || (offset == text.getLength())) {
                // Insert after our (possibly text-)node
                // null for next is okay - will be
                // appended to the end of the list
                boolean isLastInline = !haveMoreInlineContent(next);
                boolean after = true;
                // XXX TODO get rid of using xhtml directly, 
                // it should be shielded by api.
                br = createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(),
                        parent, next);

                // We may have to insert an additional <br> here.
                // Let's say you have this: "<p>Hello^</p>" and you
                // press Return. If we just insert a <br/> you get
                // "<p>Hello<br/>^</p>" which is still only a single line;
                // but the user is expecting to see a new blank line
                // to edit: "<p>Hello<br/>^<br/></p>".  Of course we
                // can't blindly insert one; we have to know that
                // there aren't any other inline tags following the
                // br in line context; e.g. if we press return here:
                // "<p>Hello^<span>foo</span></p>" we should end up with
                // "<p>Hello<br/>^<span>foo</span></p>".
                if (isLastInline) {
                    // XXX TODO get rid of using xhtml directly, 
                    // it should be shielded by api.
                    br = createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(),
                            parent, next);
                    after = false;
                }

                if (next instanceof Element) {
//                    caret.setDot(Position.create((Element)next, false));
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create((Element)next, false)));
                } else {
//                    caret.setDot(Position.create(br, after));
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(br, after)));
                }
            } else if (offset == 0) {
                // Insert before our text node
                // XXX TODO get rid of using xhtml directly, 
                // it should be shielded by api.
                br = createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(),
                        parent, text);
                
//                caret.setDot(new Position(text, 0, Bias.FORWARD));
                fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(text, 0, Bias.FORWARD)));
            } else {
                // Insert in the middle of the text node; split it
                org.w3c.dom.Text secondHalf = text.splitText(offset);
                // XXX TODO get rid of using xhtml directly, 
                // it should be shielded by api.
                br = createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(),
                        text.getParentNode(), secondHalf);

                if (JsfSupportUtilities.onlyWhitespace(secondHalf.getNodeValue())) {
                    boolean isLastInline = true;

                    if (secondHalf.getNextSibling() != null) {
                        isLastInline = !haveMoreInlineContent(secondHalf.getNextSibling());
                    }

                    if (isLastInline) {
                        // XXX TODO get rid of using xhtml directly, 
                        // it should be shielded by api.
                        br = createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(),
                                text.getParentNode(), secondHalf.getNextSibling());
                        
//                        caret.setDot(Position.create(br, false));
                        fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(br, false)));

                        return;
                    }
                }

                //caret.setDot(Position.create(br, true));
//                caret.setDot(new Position(secondHalf, 0, Bias.FORWARD));
                fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(secondHalf, 0, Bias.FORWARD)));
            }
        }
    }
    
    // XXX Moved from DesignerUtils.
    /** For the given node, locate a parent list item element, or return
     * null if no such parent is found.
     */
    private static Element getListItemParent(Node node) {
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                
                if (element.getTagName().equals(HtmlTag.LI.name)) {
                    return element;
                }
            }
            
            node = node.getParentNode();
        }
        
        return null;
    }

    private void insertSpace(/*DesignerCaret caret,*/ Node node, int offset) {
        org.w3c.dom.Text text = null;
        Node parent = null;
        Node next = null;
        String str = " ";

        if ((node.getNodeType() == Node.TEXT_NODE) ||
                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            text = (org.w3c.dom.Text)node;
        } else {
            parent = node;

            NodeList list = node.getChildNodes();
            int len = list.getLength();

            if ((len == 0) || (offset >= len)) {
                // We have a 0 index but no children, e.g. we're
                // inside an empty element.
                // XXX Can we do anything here?
                next = null;
            } else if (offset < len) {
                next = list.item(offset);
            }

//            org.w3c.dom.Document dom = webform.getJspDom();
            org.w3c.dom.Document dom = jsfForm.getJspDom();
            
            text = dom.createTextNode(str);

//            if (((RaveRenderNode)node).isJspx()) {
            if (MarkupService.isJspxNode(node)) {
                MarkupService.markJspxSource(text);
            }

            parent.insertBefore(text, next);
            
//            caret.setDot(new Position(text, str.length(), Bias.FORWARD));
            fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(text, str.length(), Bias.FORWARD)));

            return;
        }

        // Special handling for spaces! If the user hits the space
        // bar multiple times, html will simply compress the spaces
        // to a single character. So we have to convert this to
        // a &nbsp;. However, we don't want ALL spaces to become
        // &nbsp; since "nbsp" stands for "non breaking space" -
        // this would totally break word wrapping - so we only
        // convert the second and subsequent character in the
        // sequence.
        // But even that's not enough. We want the nonbreaking spaces
        // to be located with the PREVIOUS word, not the next word,
        // so we have to do a little magic.
        // In particular (^ denotes caret/insert location for space):
        //  "foo^bar" -> "foo bar"
        //  "foo ^bar" -> "foo&nbsp; bar"
        //  "foo&nbsp; ^bar" -> "foo&nbsp;&nbsp; bar"
        //  "foo&nbsp;&nbsp; ^bar" -> "foo&nbsp;&nbsp;&nbsp; bar"
        //  "foo&nbsp;^ bar" -> "foo&nbsp;&nbsp;^ bar"
        // Algorithm:
        //  if the character before the caret is a space
        //   then insert an NBSP -before- it
        //  else if the character after the caret is a space
        //   then insert an NBSP
        //  else insert a space
        String data = text.getData();
        int len = text.getLength();
        assert offset <= len;

        boolean before = (offset > 0) && (data.charAt(offset - 1) == ' ');
        boolean after = (offset < len) && (data.charAt(offset) == ' ');
        Node targetNode = node; // Node to move caret to when we're done
        int targetOffset = offset;

        if (before || after) {
            if (before) {
                offset--;
            }

            //final String NBSP_STRING = "\u00A0";
            final String NBSP_STRING;

//            if (((RaveRenderNode)node).isJspx()) {
            if (MarkupService.isJspxNode(node)) {
                NBSP_STRING = "&nbsp;"; // JSPX source is "escaped"
            } else { // html - put it right into source. Should I try to insert

                // an entity reference here instead? Might not serialize well.
                // Make sure AttributeInlineEditor checks for this!
                NBSP_STRING = "\u00A0";
            }

            str = NBSP_STRING;
            text.insertData(offset, str);
            targetNode = text;
            targetOffset += str.length();

            // Insert an entity instead

            /*
            // XXX No, that doesn't work right. The parser seems to
            // convert entities into character data, and not do the
            // reverse translation, so this doesn't buy us anything.
            EntityReference er = webform.getDom().createEntityReference("nbsp");
            if (offset == 0) {
                // Insert before our text node
                text.getParentNode().insertBefore(er, text);
                targetNode = text;
                targetOffset = 1;
            } else if (offset == text.getLength()) {
                // Insert after our text node
                // null for text.getNextSibling() is okay - will be
                // appended to the end of the list
                text.getParentNode().insertBefore(er, text.getNextSibling());
                targetNode = text;
                targetOffset += str.length();
            } else {
                // Insert in the middle of the text node; split it
                org.w3c.dom.Text secondHalf = text.splitText(offset);
                text.getParentNode().insertBefore(er, secondHalf);
                targetNode = secondHalf;
                targetOffset = 0;
            }
             */
        } else {
            text.insertData(offset, str);
            targetNode = text;
            targetOffset += str.length();
        }

        // XXX check that this works on Windows too - or do they
        // use \r\n ?
//        caret.setDot(new Position(targetNode, targetOffset, Bias.FORWARD));
        fireInsertUpdate(new DefaultDomDocumentEvent(this, DomPositionImpl.create(targetNode, targetOffset, Bias.FORWARD)));
    }

//    /** XXX Copy also in insync/FacesDnDSupport
//     * Create a new bean of the given type, positioned below parent
//     * before the given node. Returns the created element. */
//    private DesignBean createBean(String className, Node parent, Node before) {
////        MarkupPosition pos = new MarkupPosition(parent, before);
////        DesignBean parentBean = /*FacesSupport.*/Util.findParentBean(parent);
////        LiveUnit unit = webform.getModel().getLiveUnit();
////        DesignBean bean = unit.createBean(className, parentBean, pos);
////
////        return bean;
//        return webform.createBean(className, parent, before);
//    }

//    /** XXX Copy also in insync/FacesDnDSupport.
//     * Given a node, return the nearest DesignBean that "contains" it */
//    private static DesignBean findParentBean(Node node) {
//        while (node != null) {
////            if (node instanceof RaveElement) {
////                RaveElement element = (RaveElement)node;
//            if (node instanceof Element) {
//                Element element = (Element)node;
//
////                if (element.getDesignBean() != null) {
////                    return element.getDesignBean();
////                }
////                MarkupDesignBean markupDesignBean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                MarkupDesignBean markupDesignBean = WebForm.getHtmlDomProviderService().getMarkupDesignBeanForElement(element);
//                if (markupDesignBean != null) {
//                    return markupDesignBean;
//                }
//            }
//
//            node = node.getParentNode();
//        }
//
//        return null;
//    }

    /** Create a new element of the given type, positioned below parent
     * before the given node. Returns the created element. */
    private /*public*/ Element createElement(String className, Node parent, Node before) {
        // XXX TODO Pass in the tag name too. If we're editing in a DocumentFragment
        // (e.g. inline editing) create elements directly rather than going
        // through the designtime API.
//        DesignBean bean = createBean(className, parent, before);
//        DesignBean bean = webform.createBean(className, parent, before);
//        return webform.createComponent(className, parent, before);
        return jsfForm.createComponent(className, parent, before);

//        if (bean == null) {
//            return null;
//        }

//        return FacesSupport.getMarkupBean(bean).getElement();
//        return Util.getMarkupBean(bean).getElement();
//        return WebForm.getHtmlDomProviderService().getMarkupBeanElement(bean);
    }

    // XXX Moved to GridHandler.
//    /** Transfer the given element such that it's parented at the given position */
//    public boolean reparent(DesignBean bean, Element element, Position pos) {
//        if (pos == Position.NONE) {
//            return false;
//        }
//
//        // First see where it's currently located
//        Position currPos = Position.create(element, false);
//
//        if (pos.equals(currPos)) {
//            return true; // Already in the right place - done
//        }
//
////        if (pos.isRendered()) {
//        if (MarkupService.isRenderedNode(pos.getNode())) {
//            pos = pos.getSourcePosition();
//        }
//
//        if (pos == Position.NONE) {
//            return false;
//        }
//
//        Node node = pos.getNode();
//
//        // Ensure the node is not in a DocumentFragment - if it is, moving
//        // an element here is going to remove it from the jsp!!
//        Node curr = node;
//
//        while (curr.getParentNode() != null) {
//            curr = curr.getParentNode();
//        }
//
//        //if (curr instanceof DocumentFragment) {
//        if (curr != webform.getJspDom()) {
//            return false;
//        }
//
//        Node parentNode = node;
//        Node before = null;
//
//        if (node instanceof Text) {
//            parentNode = node.getParentNode();
//
//            if (pos.getOffset() == 0) {
//                before = node;
//            } else {
//                Text txt = (Text)node;
//
//                if (pos.getOffset() < txt.getLength()) {
//                    before = txt.splitText(pos.getOffset());
//                } else {
//                    // Ugh, what if it's the last node here??
//                    // XXX won't work right!
//                    before = txt.getNextSibling();
//                }
//            }
//        } else {
//            before = parentNode.getFirstChild();
//
//            for (int i = 0, n = pos.getOffset(); i < n; i++) {
//                if (before == null) {
//                    break;
//                }
//
//                before = before.getNextSibling();
//            }
//        }
//
//        if (before == element) {
//            return true;
//        }
//
////        LiveUnit lu = webform.getModel().getLiveUnit();
////        MarkupPosition markupPos = new MarkupPosition(parentNode, before);
////        DesignBean parentBean = null;
////        Node e = parentNode;
////
////        while (e != null) {
//////            if (e instanceof RaveElement) {
//////                parentBean = ((RaveElement)e).getDesignBean();
////            if (e instanceof Element) {
//////                parentBean = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)e);
////                parentBean = WebForm.getHtmlDomProviderService().getMarkupDesignBeanForElement((Element)e);
////                
////                if (parentBean != null) {
////                    break;
////                }
////            }
////
////            e = e.getParentNode();
////        }
////
////        if (bean == parentBean) {
////            return false;
////        }
////
////        boolean success = lu.moveBean(bean, parentBean, markupPos);
//        boolean success = webform.moveBean(bean, parentNode, before);
//
//        if (webform.getPane().getCaret() != null) {
//            pos = ModelViewMapper.getFirstDocumentPosition(webform, false);
//            webform.getPane().getCaret().setDot(pos);
//        }
//
//        return success;
//    }

//    /**
//     * Acquires a lock to begin mutating the document this lock
//     * protects.  There can be no writing, notification of changes, or
//     * reading going on in order to gain the lock.  Additionally a thread is
//     * allowed to gain more than one <code>writeLock</code>,
//     * as long as it doesn't attempt to gain additional <code>writeLock</code>s
//     * from within document notification.
//     * @param description Description of the task being initiated
//     */
//    public final synchronized void writeLock(String description) {
//        undoEvent = webform.getModel().writeLock(description);
//    }
//
//    /**
//     * Releases a write lock previously obtained via <code>writeLock</code>.
//     * After decrementing the lock count if there are no oustanding locks
//     * this will allow a new writer, or readers.
//     *
//     * @see #writeLock
//     */
//    public final synchronized void writeUnlock() {
//        webform.getModel().writeUnlock(undoEvent);
//        undoEvent = null;
//    }

//    /**
//     * Acquires a lock to begin reading some state from the
//     * document.  There can be multiple readers at the same time.
//     * Writing blocks the readers until notification of the change
//     * to the listeners has been completed.  This method should
//     * be used very carefully to avoid unintended compromise
//     * of the document.  It should always be balanced with a
//     * <code>readUnlock</code>.
//     *
//     * @todo Consider making this API protected
//     * @see #readUnlock
//     */
//    public final synchronized void readLock() {
//        webform.getMarkup().readLock();
//    }
//
//    /**
//     * Does a read unlock.  This signals that one
//     * of the readers is done.  If there are no more readers
//     * then writing can begin again.  This should be balanced
//     * with a readLock, and should occur in a finally statement
//     * so that the balance is guaranteed.  The following is an
//     * example.
//     * <pre><code>
//     * &nbsp;   readLock();
//     * &nbsp;   try {
//     * &nbsp;       // do something
//     * &nbsp;   } finally {
//     * &nbsp;       readUnlock();
//     * &nbsp;   }
//     * </code></pre>
//     *
//     * @todo Consider making this API protected
//     * @see #readLock
//     */
//    public final synchronized void readUnlock() {
//        webform.getMarkup().readUnlock();
//    }


    // XXX Moved to WebForm.isGridModeDocument.
//    // XXX Also in insync/FacesDnDSupport
//    /**
//     *  Return true if this document is in "grid mode" (objects
//     *  should be positioned by absolute coordinates instead of in
//     *  "flow" order.
//     *
//     *  @return true iff the document should be in grid mode
//     */
//    public boolean isGridMode() {
//        Element b = webform.getHtmlBody();
//
//        if (b == null) {
//            return false;
//        }
//
////        Value val = CssLookup.getValue(b, XhtmlCss.RAVELAYOUT_INDEX);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(b, XhtmlCss.RAVELAYOUT_INDEX);
//
////        return val == CssValueConstants.GRID_VALUE;
//        return CssProvider.getValueService().isGridValue(cssValue);
//    }

//    public Element findComponent(String id) {
//        // Hack for now
//        return findElement(webform.getHtmlBody(), id);
//    }
//
//    public Element findElement(String id) {
//        // Hack for now
//        return findElement(webform.getHtmlBody(), id);
//    }
//
//    private Element findElement(Element element, String id) {
//        String eid = element.getAttribute(HtmlAttribute.ID);
//
//        if ((eid != null) && (eid.equals(id))) {
//            return element;
//        }
//
//        NodeList list = element.getChildNodes();
//        int len = list.getLength();
//
//        for (int i = 0; i < len; i++) {
//            Node child = list.item(i);
//
//            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
//                element = findElement((Element)child, id);
//
//                if (element != null) {
//                    return element;
//                }
//            }
//        }
//
//        return null;
//    }

    // XXX Moved to InteractionManager.
//    /**
//     * Report whether the given node is in a read-only region of
//     * the document or not.
//     */
//    public static boolean isReadOnlyRegion(Position pos) {
//        Node node = pos.getNode();
//
//        // Determine if this node is in a DocumentFragment which means
//        // it's read only
//        while (node != null) {
//            node = node.getParentNode();
//
//            if (node instanceof org.w3c.dom.Document) {
//                break;
//            }
//        }
//
//        return node == null;
//    }

    // >>> Listening support.
    // XXX Temporarily here, then after moved with document interface to the API together don't make it nested class.
    
    private void fireInsertUpdate(DomDocumentEvent evt) {
        for (DomDocumentListener l : getDomDocumentListeners()) {
            l.insertUpdate(evt);
        }
    }
    
    
    private final EventListenerList listenerList = new EventListenerList();
    
    public void addDomDocumentListener(DomDocumentListener l) {
        listenerList.add(DomDocumentListener.class, l);
    }
    
    public void removeDomDocumentListener(DomDocumentListener l) {
        listenerList.remove(DomDocumentListener.class, l);
    }
    
    private DomDocumentListener[] getDomDocumentListeners() {
        return listenerList.getListeners(DomDocumentListener.class);
    }

    
    public HtmlDomProvider.DomPosition createDomPosition(Node node, int offset, DomPosition.Bias bias) {
        return DomPositionImpl.create(node, offset, bias);
    }

    public HtmlDomProvider.DomPosition createDomPosition(Node node, boolean after) {
        return DomPositionImpl.create(node, after);
    }

    public HtmlDomProvider.DomRange createRange(Node dotNode, int dotOffset, Node markNode, int markOffset) {
        return DomRangeImpl.create(jsfForm, dotNode, dotOffset, markNode, markOffset);
    }

    public int compareBoudaryPoints(Node endPointA, int offsetA, Node endPointB, int offsetB) {
        return DomPositionImpl.compareBoundaryPoints(endPointA, offsetA, endPointB, offsetB);
    }

    public HtmlDomProvider.DomPosition first(HtmlDomProvider.DomPosition dot, HtmlDomProvider.DomPosition mark) {
        return DomPositionImpl.first(dot, mark);
    }

    public HtmlDomProvider.DomPosition last(HtmlDomProvider.DomPosition dot, HtmlDomProvider.DomPosition mark) {
        return DomPositionImpl.last(dot, mark);
    }
    
    
    private static class DefaultDomDocumentEvent implements DomDocumentEvent {
        private final DomDocument document;
        private final DomPosition position;
        
        public DefaultDomDocumentEvent(DomDocument document, DomPosition position) {
            this.document = document;
            this.position = position;
        }
        
        public DomDocument getDomDocument() {
            return document;
        }
        
        public DomPosition getDomPosition() {
            return position;
        }
    } // End of DefaultDomDocumentEvent.
    // <<< Listening support.
    
}
