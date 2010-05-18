/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.designer.jsf.text;


import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;

import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocumentEvent;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocumentListener;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocument;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomRange;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.w3c.dom.DOMException;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;



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
public class DomDocumentImpl implements DomProvider.DomDocument {
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
    public boolean insertString(/*DesignerCaret caret,*/ Designer designer, DomRange domRange, String str) {
        if (domRange == null) {
            return false;
        }
//                    UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class, "InsertChar")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_InsertText")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_InsertText")); // NOI18N
        try {
        
//        if (hasSelection()) {
//            removeSelection();
        if (!domRange.isEmpty()) {
            deleteRangeContents(domRange);
        }

//        Position pos = getDot();
//        DomPosition pos = getDot();
        DomPosition pos = domRange.getDot();

//        if (editor == null) {
//        if (!component.getWebForm().isInlineEditing()) {
        if (!designer.isInlineEditing()) {
//            assert (pos == Position.NONE) || !pos.isRendered();
//            if (pos != Position.NONE && MarkupService.isRenderedNode(pos.getNode())) {
//            if (pos != DomPosition.NONE && MarkupService.isRenderedNode(pos.getNode())) {
            if (pos != DomPosition.NONE && isRenderedNode(pos.getNode())) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Node is expected to be not rendered, node=" + pos.getNode())); // NOI18N
                return false;
            }
        } // else: Stay in the DocumentFragment; don't jump to the source DOM (there is none)

//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
//            UIManager.getLookAndFeel().provideErrorFeedback(this);
            return false;
        }
        
        // TODO: If you're pressing shift while hitting Enter, we should force a <br/>,
        // and otherwise we should split the current block tag (if there is one, and
        // that block tag is not a <div> or a <body> (for these we always use <br>).
//        assert (pos != null) && (pos != Position.NONE);
        if (pos == null || pos == DomPosition.NONE) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalArgumentException("Invalid position, pos=" + pos)); // NOI18N
            return false;
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
            fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(p, 0, Bias.FORWARD)));
        }

        if (str.equals("\n") || str.equals("\r\n")) {
            insertNewline(/*caret,*/ pos.getNode(), pos.getOffset());

            return true;
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
                return true;
            } else {
                text.insertData(offset, str);
                targetNode = text;
                targetOffset += str.length();
            }

//            caret.setDot(new Position(targetNode, targetOffset, pos.getBias()));
            fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(targetNode, targetOffset, pos.getBias())));
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
                fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(text, str.length(), Bias.FORWARD)));

                return true;
            } else if (offset < len) {
                // Insert text before the given sibling;
                // if prev is a text node append to
                // that, otherwise insert a text node there
                if ((offset > 0) && (list.item(offset - 1) instanceof Text)) {
                    org.w3c.dom.CharacterData text =
                        (org.w3c.dom.CharacterData)list.item(offset - 1);
                    text.appendData(str);
                    
//                    caret.setDot(new Position(text, text.getLength(), pos.getBias()));
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(text, text.getLength(), pos.getBias())));

                    return true;
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
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(text, str.length(), Bias.FORWARD)));

                    return true;
                }
            }
        } else {
            ErrorManager.getDefault().log("Unexpected node: " + offset + ", str=" + str);
            return false;
        }
        return true;
        
        } finally {
//                        doc.writeUnlock();
//                        webform.getModel().writeUnlock(undoEvent);
            jsfForm.writeUnlock(writeLock);
        }

    }
    
    public boolean deleteRangeContents(DomRange domRange) {
        if (domRange instanceof DomRangeImpl) {
            DomRangeImpl domRangeImpl = (DomRangeImpl)domRange;
            
            DomPosition firstPosition = domRangeImpl.getFirstPosition();
            DomPosition lastPosition = domRangeImpl.getLastPosition();
            // XXX For now it works only over the source nodes. That has to be changes.
            if (!jsfForm.isInlineEditing()
//            && (MarkupService.isRenderedNode(firstPosition.getNode()) || MarkupService.isRenderedNode(lastPosition.getNode()))) {
            && (isRenderedNode(firstPosition.getNode()) || isRenderedNode(lastPosition.getNode()))) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("It is not inline editing, nor both positions are source ones," // NOI18N
                            + "\nstartPosition=" + firstPosition // NOI18N
                            + "\nendPosition=" + lastPosition)); // NOI18N
                return false;
            }
            
            deleteComponents(firstPosition, lastPosition);
            return domRangeImpl.deleteRangeContents();
        }
        return false;
    }
    
    public String getRangeText(DomRange domRange) {
        if (domRange instanceof DomRangeImpl) {
            DomRangeImpl domRangeImpl = (DomRangeImpl)domRange;
            
            DomPosition firstPosition = domRangeImpl.getFirstPosition();
            DomPosition lastPosition = domRangeImpl.getLastPosition();
            return getText(firstPosition, lastPosition);
        }
        
        return ""; // NOI18N
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
//        if (MarkupService.isRenderedNode(node)) {
        if (jsfForm.isRenderedNode(node)) {
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
            DomPosition listItemEnd = createNextDomPosition(list, true);
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
            fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(li, 0, Bias.FORWARD)));
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
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, createNextDomPosition((Element)next, false)));
                } else {
//                    caret.setDot(Position.create(br, after));
                    fireInsertUpdate(new DefaultDomDocumentEvent(this, createNextDomPosition(br, after)));
                }
            } else if (offset == 0) {
                // Insert before our text node
                // XXX TODO get rid of using xhtml directly, 
                // it should be shielded by api.
                br = createElement(org.netbeans.modules.visualweb.xhtml.Br.class.getName(),
                        parent, text);
                
//                caret.setDot(new Position(text, 0, Bias.FORWARD));
                fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(text, 0, Bias.FORWARD)));
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
                        fireInsertUpdate(new DefaultDomDocumentEvent(this, createNextDomPosition(br, false)));

                        return;
                    }
                }

                //caret.setDot(Position.create(br, true));
//                caret.setDot(new Position(secondHalf, 0, Bias.FORWARD));
                fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(secondHalf, 0, Bias.FORWARD)));
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
            fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(text, str.length(), Bias.FORWARD)));

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

            // XXX #115195 This is not OK for inline editing.
            if (!jsfForm.isInlineEditing()) {
    //            if (((RaveRenderNode)node).isJspx()) {
                if (MarkupService.isJspxNode(node)) {
                    NBSP_STRING = "&nbsp;"; // JSPX source is "escaped"
                } else { // html - put it right into source. Should I try to insert

                    // an entity reference here instead? Might not serialize well.
                    // Make sure AttributeInlineEditor checks for this!
                    NBSP_STRING = "\u00A0";
                }

                str = NBSP_STRING;
            }
            
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
        fireInsertUpdate(new DefaultDomDocumentEvent(this, createDomPosition(targetNode, targetOffset, Bias.FORWARD)));
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
//                MarkupDesignBean markupDesignBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
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
//        return WebForm.getDomProviderService().getMarkupBeanElement(bean);
    }

    
    // XXX Moved form DomRangeImpl.    
    /** Delete all the JSF components found in the given range */
//    private void deleteComponents() {
    private void deleteComponents(DomPosition first, DomPosition second) {
        // This will require a traversal, but probably not using the
        // DomTraversal class since we'll be deleting elements as
        // we're traversing
//        Position second = getLastPosition();
//        DomPosition second = getLastPosition();

//        if (second == Position.NONE) {
        if (second == DomPosition.NONE) {
            return;
        }

//        Position first = getFirstPosition();
//        DomPosition first = getFirstPosition();
//        assert first.isEarlierThan(first);

        Node firstNode = first.getNode();

        if (firstNode instanceof Element) {
            if (first.getOffset() < firstNode.getChildNodes().getLength()) {
                firstNode = firstNode.getChildNodes().item(first.getOffset());
            }
        }

        Node secondNode = second.getNode();

        if (first.equals(second)) {
            secondNode = firstNode;
        } else if (secondNode instanceof Element) {
            if ((second.getOffset() > 0) &&
                    (second.getOffset() <= secondNode.getChildNodes().getLength())) {
                secondNode = secondNode.getChildNodes().item(second.getOffset() - 1);
            } else if (second.getOffset() == 0) {
                // Gotta locate immediate inorder traversal neighbor to the left
                while ((secondNode != null) && (secondNode.getPreviousSibling() == null)) {
                    secondNode = secondNode.getParentNode();
                }

                if (secondNode == null) {
                    ErrorManager.getDefault().log("Unexpected second position " + second); // NOI18N

                    return;
                }

                secondNode = secondNode.getPreviousSibling();

                while (true) {
                    NodeList nl = secondNode.getChildNodes();

                    if (nl.getLength() > 0) {
                        secondNode = nl.item(nl.getLength() - 1);
                    } else {
                        break;
                    }
                }
            }
        }

        // Insert content for the first node
        if ((firstNode == secondNode) && firstNode instanceof Text) {
            // Common case - and we're done; no components to be deleted here
            return;
        }

        // Iterate over the range building up all the DesignBeans to be
        // destroyed
//        ArrayList beans = new ArrayList();
        List<Element> components = new ArrayList<Element>();

//        org.w3c.dom.Document dom = webform.getJspDom();
        org.w3c.dom.Document dom = jsfForm.getJspDom();

        if (!(dom instanceof DocumentTraversal)) {
            return;
        }

        DocumentTraversal trav = (DocumentTraversal)dom;

        // Iterating over all since we can't just limit ourselves to text nodes
        // in case the target node is not necessarily a text node!
        NodeIterator iterator = trav.createNodeIterator(dom, NodeFilter.SHOW_ALL, null, false);

        // The node iterator doesn't seem to have a way to jump to a
        // particular node, so we search for it ourselves
        Node curr = firstNode;

        while (curr != null) {
            try {
                curr = iterator.nextNode();

                if (curr == firstNode) {
                    break;
                }
            } catch (DOMException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);

                break;
            }
        }

        Node last = secondNode;

        while (curr != null) {
//            if (curr instanceof RaveElement) {
//                RaveElement element = (RaveElement)curr;
            if (curr instanceof Element) {
                Element element = (Element)curr;
//                DesignBean bean = element.getDesignBean();
//                DesignBean bean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
//                DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(element);
                Element componentRootElement = MarkupService.getRenderedElementForElement(element);
                
//                if ((bean != null) &&
                if ((componentRootElement != null) &&
                        ((element.getParentNode() == null) ||
//                        (element.getParentNode() instanceof RaveElement &&
//                        (((RaveElement)element.getParentNode()).getDesignBean() != bean)))) {
                        (element.getParentNode() instanceof Element
//                        && InSyncService.getProvider().getMarkupDesignBeanForElement((Element)element.getParentNode()) != bean))) {
//                        && WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)element.getParentNode()) != bean))) {
                        && MarkupService.getRenderedElementForElement((Element)element.getParentNode()) != componentRootElement))) {
//                    if (!beans.contains(bean)) {
//                        beans.add(bean);
//                    }
                    if (!components.contains(componentRootElement)) {
                        components.add(componentRootElement);
                    }
                }
            }

            if ((curr == null) || (curr == last)) {
                break;
            }

            try {
                curr = iterator.nextNode();
            } catch (DOMException ex) {
                ErrorManager.getDefault().notify(ex);

                break;
            }
        }

        iterator.detach();

//        FacesModel model = webform.getModel();
//        Document doc = webform.getDocument();

////        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
////        DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_DeleteText")); // NOI18N
//        try {
////            doc.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//
////            for (int i = 0; i < beans.size(); i++) {
////                DesignBean bean = (DesignBean)beans.get(i);
//            for (Element componentRootElement : components) {
//
////                if (!FacesSupport.isSpecialBean(/*webform, */bean)) {
////                if (!Util.isSpecialBean(bean)) {
////                if (bean instanceof MarkupDesignBean && !WebForm.getDomProviderService().isSpecialComponent(
////                        WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)bean))) {
////                if (!WebForm.getDomProviderService().isSpecialComponent(componentRootElement)) {
//                if (!JsfSupportUtilities.isSpecialComponent(componentRootElement)) {
////                    model.getLiveUnit().deleteBean(bean);
////                    webform.deleteBean(bean);
////                    webform.deleteComponent(componentRootElement);
//                    jsfForm.deleteComponent(componentRootElement);
//                }
//            }
//        } finally {
////            doc.writeUnlock();
////            webform.getModel().writeUnlock(undoEvent);
////            webform.writeUnlock(writeLock);
//            jsfForm.writeUnlock(writeLock);
//        }
        deleteComponents(components.toArray(new Element[components.size()]));
    }


    /** Return the text in the range (linearized to a String); this is only the
     * text nodes, not comment nodes, not markup, etc.
     */
    private String getText(DomPosition first, DomPosition second) {
        // Since we'll be iterating forwards, gotta make sure we know
        // which point is first
//        Position second = getLastPosition();
//        DomPosition second = getLastPosition();

//        if (second == Position.NONE) {
        if (second == DomPosition.NONE) {
            return "";
        }

//        Position first = getFirstPosition();
//        DomPosition first = getFirstPosition();
//        assert first.isEarlierThan(first);

        StringBuffer sb = new StringBuffer();

        Node firstNode = first.getNode();

        if (firstNode instanceof Element) {
            if (first.getOffset() < firstNode.getChildNodes().getLength()) {
                firstNode = firstNode.getChildNodes().item(first.getOffset());
            }
        }

        Node secondNode = second.getNode();

        if (first.equals(second)) {
            secondNode = firstNode;
        } else if (secondNode instanceof Element) {
            if ((second.getOffset() > 0) &&
                    (second.getOffset() <= secondNode.getChildNodes().getLength())) {
                secondNode = secondNode.getChildNodes().item(second.getOffset() - 1);
            } else if (second.getOffset() == 0) {
                // Gotta locate immediate inorder traversal neighbor to the left
                while ((secondNode != null) && (secondNode.getPreviousSibling() == null)) {
                    secondNode = secondNode.getParentNode();
                }

                if (secondNode == null) {
                    ErrorManager.getDefault().log("Unexpected second position " + second); // NOI18N

                    return "";
                }

                secondNode = secondNode.getPreviousSibling();

                while (true) {
                    NodeList nl = secondNode.getChildNodes();

                    if (nl.getLength() > 0) {
                        secondNode = nl.item(nl.getLength() - 1);
                    } else {
                        break;
                    }
                }
            }
        }

        // Insert content for the first node
        if (firstNode instanceof Text) {
            if (secondNode == firstNode) {
                String s = firstNode.getNodeValue();

                for (int i = first.getOffset(); i < second.getOffset(); i++) {
                    sb.append(s.charAt(i));
                }

                return sb.toString();
            } else {
                String s = firstNode.getNodeValue();

                for (int i = first.getOffset(), n = s.length(); i < n; i++) {
                    sb.append(s.charAt(i));
                }
            }
        }

        // Append content for all the nodes between first and second
//        org.w3c.dom.Document dom = webform.getJspDom();
        org.w3c.dom.Document dom = jsfForm.getJspDom();

        if (!(dom instanceof DocumentTraversal)) {
            return "";
        }

        DocumentTraversal trav = (DocumentTraversal)dom;

        // Iterating over all since we can't just limit ourselves to text nodes
        // in case the target node is not necessarily a text node!
        NodeIterator iterator = trav.createNodeIterator(dom, NodeFilter.SHOW_ALL, null, false);
        Node curr = firstNode;

        // The node iterator doesn't seem to have a way to jump to a particular node,
        // so we search for it ourselves
        while (curr != null) {
            try {
                curr = iterator.nextNode();

                if (curr == firstNode) {
                    break;
                }
            } catch (DOMException ex) {
                ErrorManager.getDefault().notify(ex);

                break;
            }
        }

        Node last = secondNode;

        while (curr != null) {
            try {
                curr = iterator.nextNode();
            } catch (DOMException ex) {
                ErrorManager.getDefault().notify(ex);

                break;
            }

            if ((curr == null) || (curr == last)) {
                break;
            }

            if (curr instanceof Text) {
                sb.append(curr.getNodeValue());
            }
        }

        iterator.detach();

        // Append content for the last node
        if (secondNode instanceof Text) {
            String s = secondNode.getNodeValue();

            for (int i = 0; i < second.getOffset(); i++) {
                sb.append(s.charAt(i));
            }
        }

        return sb.toString();
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
////                parentBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)e);
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
    
    private void fireComponentMoved(DomDocumentEvent evt) {
        for (DomDocumentListener l : getDomDocumentListeners()) {
            l.componentMoved(evt);
        }
    }
    
    private void fireComponentsMoved(DomDocumentEvent evt) {
        for (DomDocumentListener l : getDomDocumentListeners()) {
            l.componentsMoved(evt);
        }
    }
    
    private void fireComponentMovedTo(DomDocumentEvent evt) {
        for (DomDocumentListener l : getDomDocumentListeners()) {
            l.componentMovedTo(evt);
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

    
    public DomProvider.DomPosition createDomPosition(Node node, int offset, DomPosition.Bias bias) {
        if (node == null) {
            return DomPosition.NONE;
        }
        return DomPositionImpl.create(this, node, offset, bias);
    }

    public DomProvider.DomPosition createNextDomPosition(Node node, boolean after) {
        if (node == null) {
            return DomPosition.NONE;
        }
        return DomPositionImpl.createNext(this, node, after);
    }

    public DomProvider.DomRange createRange(Node dotNode, int dotOffset, Node markNode, int markOffset) {
        return DomRangeImpl.create(this, dotNode, dotOffset, markNode, markOffset);
    }

    public int compareBoudaryPoints(Node endPointA, int offsetA, Node endPointB, int offsetB) {
        return DomPositionImpl.compareBoundaryPoints(endPointA, offsetA, endPointB, offsetB);
    }

    public DomProvider.DomPosition first(DomProvider.DomPosition dot,org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition mark) {
        return DomPositionImpl.first(dot, mark);
    }

    public DomProvider.DomPosition last(DomProvider.DomPosition dot,org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition mark) {
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

    
    /** Transfer the given element such that it's parented at the given position */
//    private boolean reparent(DesignBean bean, Element element, Position pos, WebForm webform) {
//    private boolean reparentComponent(Element componentRootElement, /*Element element,*/ Position pos, WebForm webform) {
    private boolean reparentComponent(Element componentRootElement, /*Element element,*/ DomPosition pos /*, WebForm webform*/) {
//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
            return false;
        }

        // First see where it's currently located
//        Position currPos = Position.create(element, false);
//        Position currPos = Position.create(componentRootElement, false);
//        DomPosition currPos = webform.createDomPosition(componentRootElement, false);
        DomPosition currPos = createNextDomPosition(componentRootElement, false);

        if (pos.equals(currPos)) {
            return true; // Already in the right place - done
        }

//        if (pos.isRendered()) {
//        if (MarkupService.isRenderedNode(pos.getNode())) {
        if (isRenderedNode(pos.getNode())) {
            pos = pos.getSourcePosition();
        }

//        if (pos == Position.NONE) {
        if (pos == DomPosition.NONE) {
            return false;
        }

        Node node = pos.getNode();

        // Ensure the node is not in a DocumentFragment - if it is, moving
        // an element here is going to remove it from the jsp!!
        Node curr = node;

        while (curr.getParentNode() != null) {
            curr = curr.getParentNode();
        }

        //if (curr instanceof DocumentFragment) {
//        if (curr != webform.getJspDom()) {
        if (curr != jsfForm.getJspDom()) {
            return false;
        }

        Node parentNode = node;
        Node before = null;

        if (node instanceof Text) {
            parentNode = node.getParentNode();

            if (pos.getOffset() == 0) {
                before = node;
            } else {
                Text txt = (Text)node;

                if (pos.getOffset() < txt.getLength()) {
                    before = txt.splitText(pos.getOffset());
                } else {
                    // Ugh, what if it's the last node here??
                    // XXX won't work right!
                    before = txt.getNextSibling();
                }
            }
        } else {
            before = parentNode.getFirstChild();

            for (int i = 0, n = pos.getOffset(); i < n; i++) {
                if (before == null) {
                    break;
                }

                before = before.getNextSibling();
            }
        }

//        if (before == element) {
        // XXX Comparing rendered with source element can never fit.
        if (before == componentRootElement) {
            return true;
        }

//        LiveUnit lu = webform.getModel().getLiveUnit();
//        MarkupPosition markupPos = new MarkupPosition(parentNode, before);
//        DesignBean parentBean = null;
//        Node e = parentNode;
//
//        while (e != null) {
////            if (e instanceof RaveElement) {
////                parentBean = ((RaveElement)e).getDesignBean();
//            if (e instanceof Element) {
////                parentBean = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)e);
//                parentBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)e);
//                
//                if (parentBean != null) {
//                    break;
//                }
//            }
//
//            e = e.getParentNode();
//        }
//
//        if (bean == parentBean) {
//            return false;
//        }
//
//        boolean success = lu.moveBean(bean, parentBean, markupPos);
//        boolean success = webform.moveComponent(componentRootElement, parentNode, before);
        boolean success = jsfForm.moveComponent(componentRootElement, parentNode, before);

////        if (webform.getPane().getCaret() != null) {
//        if (webform.getPane().hasCaret()) {
//            pos = ModelViewMapper.getFirstDocumentPosition(webform, false);
////            webform.getPane().getCaret().setDot(pos);
//            webform.getPane().setCaretDot(pos);
//        }
        fireComponentMoved(new DefaultDomDocumentEvent(this, null));

        return success;
    }

    // XXX Moved from designer/../GridHandler
    // TODO 1) Refactor, there should be listener on the designer, informing about user actions.
    // TODO 2) This is very messy, simplify, devide to more methods.
    public void moveComponents(Designer designer, Box[] boxes, Point[] offsetPoints, DomPosition pos, int newX, int newY, boolean snapEnabled) {
        // Locate a grid layout parent
//        Document doc = editor.getDocument();
//        WebForm webform = doc.getWebForm();
//        WebForm webform = editor.getWebForm();
        
//        int numMoved = beans.size();
//        int numMoved = boxes.size();
        int numMoved = boxes.length;
        
//        Rectangle boundingBox = null;

        String description;
        if (numMoved > 1) {
//            description = NbBundle.getMessage(GridHandler.class, "MoveComponents");
            description = NbBundle.getMessage(DomDocumentImpl.class, "LBL_MoveComponents");
        } else {
            description = NbBundle.getMessage(DomDocumentImpl.class, "LBL_MoveComponent");
        }
//        UndoEvent undoEvent = webform.getModel().writeLock(description);
//        DomProvider.WriteLock writeLock = webform.writeLock(description);
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(description);
        UndoEvent writeLock = jsfForm.writeLock(description);
        try {
//            String description;
//
//            if (numMoved > 1) {
//                description = NbBundle.getMessage(GridHandler.class, "MoveComponents");
//            } else {
//                description = NbBundle.getMessage(GridHandler.class, "MoveComponent");
//            }
//
//            doc.writeLock(description);

            // Move the components
            for (int i = 0; i < numMoved; i++) {
//                MarkupDesignBean bean = beans.get(i);
//                Rectangle offset = offsetRectangles.get(i);
//                CssBox box = boxes.get(i);
//                CssBox box = boxes[i];
                Box box = boxes[i];
//                Rectangle offset = offsetRectangles[i];
                Point offset = offsetPoints[i];
                
//                Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
                Element componentRootElement = box.getComponentRootElement();
                        
//                Element e = box.getElement();
//
//                if (e == null) {
//                    e = bean.getElement();
//                }

//                int x = newX + offset.x;
//                int y = newY + offset.y;
//
//                if (!snapDisabled) {
//                    x = snapX(x, box.getPositionedBy());
//                    y = snapY(y, box.getPositionedBy());
//                }
                
                int x = newX + offset.x;
                int y = newY + offset.y;
                
                if (snapEnabled) {
                    x = designer.snapX(x, box.getPositionedBy());
                    y = designer.snapY(y, box.getPositionedBy());
                }

//                CssBox parentBox = box.getParent();
                 Box parentBox = box.getParent();

//                if (boundingBox == null) {
//                    boundingBox = new Rectangle(x, y, box.getWidth(), box.getHeight());
//                } else {
//                    boundingBox.add(x, y);
//                    boundingBox.add(x + box.getWidth(), y + box.getHeight());
//                }

                try {
                    boolean moveSucceeded = true;

//                    if ((pos != null) && (pos != Position.NONE)) {
                    if ((pos != null) && (pos != DomPosition.NONE)) {
                        // TODO: better batch handling here
//                        moveSucceeded = doc.reparent(bean, e, pos);
//                        moveSucceeded = reparentComponent(componentRootElement, /*e,*/ pos, webform);
//                        moveSucceeded = webForm.getDomDocument().reparentComponent(componentRootElement, /*e,*/ pos);
                        moveSucceeded = reparentComponent(componentRootElement, /*e,*/ pos);
                    } else if (!isAbsolutelyPositioned(componentRootElement)) {
                        // Looks like we've moved a flow position element
                        // out to grid
//                        CssBox pb = null;
                        Element parent = null;
//                        Element element = box.getDesignBean().getElement();
                        // XXX Possible NPE?
//                        Element element = CssBox.getMarkupDesignBeanForCssBox(box).getElement();
//                        Element boxComponentRootElement = CssBox.getElementForComponentRootCssBox(box);
                        Element boxComponentRootElement = componentRootElement;
                        // XXX Get rid of using source elements in the designer.
                        Element element = MarkupService.getSourceElementForElement(boxComponentRootElement);

                        if (element == null) {
                            // XXX #124560 Give one more try.
                            MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(boxComponentRootElement);
                            if (markupDesignBean != null) {
                                element = markupDesignBean.getElement();
                            }
                            
                            
                            // XXX #109112 This box is not to move.
                            if (element == null) {
                                continue;
                            }
                        }
                        
                        if (element.getParentNode() != null
                        && element.getParentNode().getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                        && element.getParentNode().getNodeName().equals(HtmlTag.FSUBVIEW.name)) {
//                            pb = parentBox;
                            parent = (Element)element.getParentNode();
//                        } else if ((parentBox != null) && (parentBox.getDesignBean() != null)) {
//                            MarkupDesignBean parentBean = parentBox.getDesignBean();
                        } else {
//                            MarkupDesignBean parentMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(parentBox);
//                            Element parentComponentRootElement = CssBox.getElementForComponentRootCssBox(parentBox);
                            Element parentComponentRootElement = parentBox == null ? null : parentBox.getComponentRootElement();
                            if (parentComponentRootElement != null) {
                                Element parentElement = MarkupService.getSourceElementForElement(parentComponentRootElement);

                                if ((parentElement != null) &&
                                        (parentElement.getTagName().equals(HtmlTag.FORM.name))) {
//                                    pb = parentBox;
//                                    parent = parentBox.getSourceElement();
                                    parent = parentElement;
                                }
                            }
                        }

//                        Designer[] designers = JsfForm.findDesigners(jsfForm);
//                        Designer designer = designers.length == 0 ? null : designers[0];
                        
//                        if (pb == null) {
                        if (parent == null) {
//                            CssBox currentBox = webform.getMapper().findBox(x, y);
//                            CssBox currentBox = ModelViewMapper.findBox(webform.getPane().getPageBox(), x, y);
                            Box currentBox = designer.findBox(x, y);
                            if (currentBox != null) {

//                                for (int j = 0, m = currentBox.getBoxCount(); j < m; j++) {
//                                    HtmlTag tag = currentBox.getBox(j).getTag();
                                for (Box child : currentBox.getChildren()) {
                                    HtmlTag tag = child.getTag();

                                    if (tag == HtmlTag.FORM) {
    //                                    pb = currentBox.getBox(j);
//                                        parent = currentBox.getSourceElement();
                                        // #102848 Get the form (not body).
                                        parent = child.getSourceElement();

                                        break;
                                    }
                                }

    //                            if (pb == null) {
    //                                pb = currentBox;
    //                            }
                                if (parent == null) {
                                    parent = currentBox.getSourceElement();
                                }
                            }
                        }

//                        if (parent == null) {
//                            parent = pb.getSourceElement();
//                        }

                        if (element.getParentNode() != parent) {
                            moveSucceeded =
//                                doc.reparent(bean, e, new Position(parent, 0, Bias.FORWARD));
//                                reparentComponent(componentRootElement, /*e,*/ new Position(parent, 0, Bias.FORWARD), webform);
//                                reparentComponent(componentRootElement, /*e,*/ Position.create(parent, 0, Bias.FORWARD), webform);
//                                reparentComponent(componentRootElement, /*e,*/ webForm.createDomPosition(parent, 0, Bias.FORWARD), webform);
//                                webForm.getDomDocument().reparentComponent(componentRootElement, /*e,*/ webForm.createDomPosition(parent, 0, Bias.FORWARD));
                                    reparentComponent(componentRootElement, /*e,*/ createDomPosition(parent, 0, Bias.FORWARD));
                        }

//                        parentBox = pb;
//                        CssBox pb = webForm.findCssBoxForElement(parent);
                        Box pb = designer.findBoxForSourceElement(parent);
                        if (pb != null) {
                            parentBox = pb;
                        }
                    }

                    // prevent multiple updates for the same element -
                    // only need a single refresh especially when just changing
                    // from one grid position to another
//                    webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//                    webform.setUpdatesSuspended(componentRootElement, true);
                    jsfForm.setUpdatesSuspended(componentRootElement, true);

                    List<StyleData> set = new ArrayList<StyleData>(3);
                    List<StyleData> remove = new ArrayList<StyleData>(3);

//                    if ((pos != null) && (pos != Position.NONE)) {
                    if ((pos != null) && (pos != DomPosition.NONE)) {
                        if (moveSucceeded) {
                            remove.add(new StyleData(XhtmlCss.POSITION_INDEX));
                            remove.add(new StyleData(XhtmlCss.LEFT_INDEX));
                            remove.add(new StyleData(XhtmlCss.TOP_INDEX));
                        } else {
                            java.awt.Toolkit.getDefaultToolkit().beep();
                        }
                    } else if (moveSucceeded) {
                        // Translate coordinates from absolute/viewport
                        // to absolute coordinates relative to the target
                        // grid container
                        set.add(new StyleData(XhtmlCss.POSITION_INDEX,
//                                CssConstants.CSS_ABSOLUTE_VALUE));
                                CssProvider.getValueService().getAbsoluteValue()));
                        set.add(getHorizontalCssSetting(x, box.getWidth(), box, parentBox, componentRootElement));
                        set.add(getVerticalCssSetting(y, box.getHeight(), box, parentBox, componentRootElement));
                    }

//                    XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
// <removing design bean manipulation in engine>
//                    engine.updateLocalStyleValues((RaveElement)e, set, remove);
// ====
//                    Util.updateLocalStyleValuesForElement(e,
//                            (StyleData[])set.toArray(new StyleData[set.size()]),
//                            (StyleData[])remove.toArray(new StyleData[remove.size()]));
//                    WebForm.getDomProviderService().updateLocalStyleValuesForElement(componentRootElement,
//                            set.toArray(new StyleData[set.size()]),
//                            remove.toArray(new StyleData[remove.size()]));
                    JsfSupportUtilities.updateLocalStyleValuesForElement(componentRootElement,
                            set.toArray(new StyleData[set.size()]),
                            remove.toArray(new StyleData[remove.size()]));
// </removing design bean manipulation in engine>
                } finally {
//                    webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//                    webform.setUpdatesSuspended(componentRootElement, false);
                    jsfForm.setUpdatesSuspended(componentRootElement, false);
                }
            }
        } finally {
//            doc.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//            webform.writeUnlock(writeLock);
            jsfForm.writeUnlock(writeLock);
        }

        // XXX #91531 User didn't want to have this kind of autoscroll behavior.
//        final Rectangle rect = boundingBox;
//	// #6331237 NPE.
//	if(rect != null) {
//	    SwingUtilities.invokeLater(new Runnable() {
//		public void run() {
//		    editor.scrollRectToVisible(rect);
//		}
//	    });
//	}
        fireComponentsMoved(new DefaultDomDocumentEvent(this, null));
    }

    // XXX Copy aldo in designer/../GridHandler.
    /** Report whether the given element is absolutely positioned */
    private boolean isAbsolutelyPositioned(Element element) {
        boolean absolute;
//        Value val = CssLookup.getValue(element, XhtmlCss.POSITION_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.POSITION_INDEX);

//        if ((val == CssValueConstants.ABSOLUTE_VALUE) || (val == CssValueConstants.FIXED_VALUE)) {
        if (CssProvider.getValueService().isAbsoluteValue(cssValue)
        || CssProvider.getValueService().isFixedValue(cssValue)) {
            absolute = true;
        } else {
            absolute = false;
        }

        return absolute;
    }

    /**
     * Given a target position (referring to the border top left corner) for
     * a box, update its horizontal CSS position properties (left/right) to
     * make the box appear at the target position.
     * This not only converts the coordinates to the margin edge (since the
     * CSS properties are relative to it), but also ensures that if a component
     * is for example only constrained on the right, the "right" property is
     * updated rather than "left".
     */
//    private StyleData getHorizontalCssSetting(int x, int newWidth, CssBox box, CssBox parentBox, Element e) {
    private StyleData getHorizontalCssSetting(int x, int newWidth, Box box, Box parentBox, Element e) {
//        int left = CssLookup.getLength(e, XhtmlCss.LEFT_INDEX);
//        int right = CssLookup.getLength(e, XhtmlCss.RIGHT_INDEX);
//        int left = CssUtilities.getCssLength(e, XhtmlCss.LEFT_INDEX);
//        int right = CssUtilities.getCssLength(e, XhtmlCss.RIGHT_INDEX);
        int left = CssProvider.getValueService().getCssLength(e, XhtmlCss.LEFT_INDEX);
        int right = CssProvider.getValueService().getCssLength(e, XhtmlCss.RIGHT_INDEX);

//        if ((left == CssBox.AUTO) && (right != CssBox.AUTO)) {
        if ((left == CssValue.AUTO) && (right != CssValue.AUTO)) {
//            int rx = right - (x - box.getX()) - (width - box.getWidth());
//            Point p = translateCoordinates(parentBox, rx, 0);
//            rx = p.x;
//
//            // The CSS "right" property is relative to the Margin edge
//            rx += box.getRightMargin();
            int rx = translateRight(right, x, newWidth, box, parentBox);

            return new StyleData(XhtmlCss.RIGHT_INDEX, Integer.toString(rx) + "px"); // NOI18N
        } else {
//            Point p = translateCoordinates(parentBox, x, 0);
//            x = p.x;
//
//            // The CSS "left" property is relative to the Margin edge
//            x -= box.getLeftMargin();
            int rx = translateLeft(x, box, parentBox);

//            return new StyleData(XhtmlCss.LEFT_INDEX, Integer.toString(x) + "px"); // NOI18N
            return new StyleData(XhtmlCss.LEFT_INDEX, Integer.toString(rx) + "px"); // NOI18N
        }
    }

    // XXX Copy also in designer/../GridHandler.
//    private int translateRight(int right, int x, int newWidth, CssBox box, CssBox parentBox) {
    private int translateRight(int right, int x, int newWidth, Box box, Box parentBox) {
        int rx = right - (x - box.getX()) - (newWidth - box.getWidth());
//        Point p = translateCoordinates(parentBox, rx, 0);
        Point p = JsfSupportUtilities.translateCoordinates(parentBox, rx, 0);
        rx = p.x;

        // The CSS "right" property is relative to the Margin edge
        rx += box.getRightMargin();
        return rx;
    }
    
    // XXX Copy also in designer/../GridHandler
//    private int translateLeft(int x, CssBox box, CssBox parentBox) {
    private int translateLeft(int x, Box box, Box parentBox) {
//        Point p = translateCoordinates(parentBox, x, 0);
        Point p = JsfSupportUtilities.translateCoordinates(parentBox, x, 0);
        x = p.x;

        // The CSS "left" property is relative to the Margin edge
        x -= box.getLeftMargin();
        return x;
    }

    // XXX Copy also in designer/../GridHandler.
    /** Same as setHorizontalCssPosition, but for the vertical dimension with
     * CSS top/bottom properties */
//    private StyleData getVerticalCssSetting(int y, int newHeight, CssBox box, CssBox parentBox, Element e) {
    private StyleData getVerticalCssSetting(int y, int newHeight, Box box, Box parentBox, Element e) {
//        int top = CssLookup.getLength(e, XhtmlCss.TOP_INDEX);
//        int bottom = CssLookup.getLength(e, XhtmlCss.BOTTOM_INDEX);
//        int top = CssUtilities.getCssLength(e, XhtmlCss.TOP_INDEX);
//        int bottom = CssUtilities.getCssLength(e, XhtmlCss.BOTTOM_INDEX);
        int top = CssProvider.getValueService().getCssLength(e, XhtmlCss.TOP_INDEX);
        int bottom = CssProvider.getValueService().getCssLength(e, XhtmlCss.BOTTOM_INDEX);

//        if ((top == CssBox.AUTO) && (bottom != CssBox.AUTO)) {
        if ((top == CssValue.AUTO) && (bottom != CssValue.AUTO)) {
//            int ry = bottom - (y - box.getY()) - (height - box.getHeight());
//            Point p = translateCoordinates(parentBox, 0, ry);
//            ry = p.y;
//
//            // The CSS "bottom" property is relative to the Margin edge
//            ry += box.getEffectiveTopMargin();
            int ry = translateBottom(bottom, y, newHeight, box, parentBox);

            return new StyleData(XhtmlCss.BOTTOM_INDEX, Integer.toString(ry) + "px"); // NOI18N
        } else {
//            Point p = translateCoordinates(parentBox, 0, y);
//            y = p.y;
//
//            // The CSS "top" property is relative to the Margin edge
//            y -= box.getEffectiveTopMargin();
            int ry = translateTop(y, box, parentBox);

//            return new StyleData(XhtmlCss.TOP_INDEX, Integer.toString(y) + "px"); // NOI18N
            return new StyleData(XhtmlCss.TOP_INDEX, Integer.toString(ry) + "px"); // NOI18N
        }
    }
    
    // XXX Copy also in designer/../GridHandler.
//    private int translateBottom(int bottom, int y, int newHeight, CssBox box, CssBox parentBox) {
    private int translateBottom(int bottom, int y, int newHeight, Box box, Box parentBox) {
        int ry = bottom - (y - box.getY()) - (newHeight - box.getHeight());
//        Point p = translateCoordinates(parentBox, 0, ry);
        Point p = JsfSupportUtilities.translateCoordinates(parentBox, 0, ry);
        ry = p.y;

        // The CSS "bottom" property is relative to the Margin edge
        ry += box.getEffectiveTopMargin();
        return ry;
    }
    
    // XXX Copy also in designer/../GridHandler.
//    private int translateTop(int y, CssBox box, CssBox parentBox) {
    private int translateTop(int y, Box box, Box parentBox) {
//        Point p = translateCoordinates(parentBox, 0, y);
        Point p = JsfSupportUtilities.translateCoordinates(parentBox, 0, y);
        y = p.y;

        // The CSS "top" property is relative to the Margin edge
        y -= box.getEffectiveTopMargin();
        return y;
    }
    
//    // XXX Copy aldo in designer/../CssUtilities.
//    // FIXME This is very suspicious, and should be revisited.
//    public static final int AUTO = Integer.MAX_VALUE - 1;
//    
//    /** XXX Copy also in insync/FacesDnDSupport.
//     * XXX Copy also in designer/../CssUtilities
//     * XXX Provides the auto value as <code>AUTO</code>, revise that, it looks very dangerous.
//     * TODO At least move into designer/cssengine.
//     */
//    private static int getCssLength(Element element, int property) {
////        Value val = getValue(element, property);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, property);
//        
//        // XXX #6460007 Possible NPE.
//        if (cssValue == null) {
//            // XXX What value to return?
//            return 0;
//        }
//        
////        if (val == CssValueConstants.AUTO_VALUE) {
//        if (CssProvider.getValueService().isAutoValue(cssValue)) {
//            return AUTO;
//        }
//        
////        return (int)val.getFloatValue();
//        return (int)cssValue.getFloatValue();
//    }

//    // XXX Copy also in designer/../GridHandler.
//    /** Given absolute coordinates x,y in the viewport, compute
//     * the CSS coordinates to assign to a box if it's parented by
//     * the given parentBox such that the coordinates will result
//     * in a box showing up at the absolute coordinates.
//     * That was a really convoluted explanation, so to be specific:
//     * If you have an absolutely positioned <div> at 100, 100,
//     * and you drag a button into it such that it's its child,
//     * and you drag it to screen coordinate 75, 150, then, in order
//     * for the button to be rendered at 75, 150 and be a child of
//     * the div its top/left coordinates must be -25, 50.
//     */
////    private Point translateCoordinates(CssBox parentBox, int x, int y) {
//    private Point translateCoordinates(Box parentBox, int x, int y) {
//        while (parentBox != null) {
////            if (parentBox.getBoxType().isPositioned()) {
//            if (parentBox.isPositioned()) {
//                x -= parentBox.getAbsoluteX();
//                y -= parentBox.getAbsoluteY();
//
//                return new Point(x, y);
//            }
//
//            if (parentBox.getPositionedBy() != null) {
//                parentBox = parentBox.getPositionedBy();
//            } else {
//                parentBox = parentBox.getParent();
//            }
//        }
//
//        return new Point(x, y);
//    }

    private void moveComponentTo(Box box, int x, int y) {
        Element componentRootElement = box.getComponentRootElement();
        // We should already have a locked buffer with a user visible
        // undo event when this methhod is called
        // XXX Not here.
//        assert webform.getModel().isWriteLocked();
//        if (!webform.isWriteLocked()) {
        if (!jsfForm.isWriteLocked()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("This method has to be called under write lock! It is not.")); // NOI18N
        }

        // prevent multiple updates for the same element - only need a single refresh
        try {
//            webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//            webform.setUpdatesSuspended(componentRootElement, true);
            jsfForm.setUpdatesSuspended(componentRootElement, true);

//            CssBox parentBox = box.getParent();
            Box parentBox = box.getParent();
            
            List<StyleData> set = new ArrayList<StyleData>(3);
//            set.add(new StyleData(XhtmlCss.POSITION_INDEX, CssConstants.CSS_ABSOLUTE_VALUE));
            set.add(new StyleData(XhtmlCss.POSITION_INDEX, CssProvider.getValueService().getAbsoluteValue()));
            set.add(getHorizontalCssSetting(x, box.getWidth(), box, parentBox, componentRootElement));
            set.add(getVerticalCssSetting(y, box.getHeight(), box, parentBox, componentRootElement));

//            XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
// <removing design bean manipulation in engine>
//            engine.updateLocalStyleValues((RaveElement)e, set, null);
// ====
//            Util.updateLocalStyleValuesForElement(e,
//                    (StyleData[])set.toArray(new StyleData[set.size()]), null);
//            WebForm.getDomProviderService().updateLocalStyleValuesForElement(componentRootElement,
//                    set.toArray(new StyleData[set.size()]), null);
            JsfSupportUtilities.updateLocalStyleValuesForElement(componentRootElement,
                    set.toArray(new StyleData[set.size()]), null);
// </removing design bean manipulation in engine>
        } finally {
//            webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//            webform.setUpdatesSuspended(componentRootElement, false);
            jsfForm.setUpdatesSuspended(componentRootElement, false);
        }
        
        fireComponentMovedTo(new DefaultDomDocumentEvent(this, null));
    }

    // XXX Moved from designer/../GridHandler.
    public void frontComponents(Box[] boxes) {
//        Document doc = webform.getDocument();

//        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(GridHandler.class, "BringToFront")); // NOI18N
//        DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(GridHandler.class, "BringToFront")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_BringToFront")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_BringToFront")); // NOI18N
        try {
//            doc.writeLock(NbBundle.getMessage(GridHandler.class, "BringToFront")); // NOI18N

//            int num = boxes.size();
            int num = boxes.length;

//            for (int i = 0; i < num; i++) {
//                CssBox box = boxes.get(i);
//            for (CssBox box : boxes) {
            for (Box box : boxes) {
//                MarkupDesignBean bean = box.getDesignBean();
//                MarkupDesignBean bean = CssBox.getMarkupDesignBeanForCssBox(box);
//                Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
                Element componentRootElement = box.getComponentRootElement();
                
//                assert bean != null;

//                Element e = box.getElement();

//                if (e == null) {
//                    e = bean.getElement();
//                }

//                assert e != null;
                if (componentRootElement == null) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new NullPointerException("There is no component root element for box=" + box));
                    continue;
                }

                // Locate the highest z index in box' parent
//                int highest = CssBox.AUTO;
                int highest = CssValue.AUTO;
//                CssBox parent = box.getParent();
                Box parent = box.getParent();

                // #6358276 NPE.
                if(parent != null) {
//                    for (int j = 0, m = parent.getBoxCount(); j < m; j++) {
//                        CssBox sibling = parent.getBox(j);
                    for (Box sibling : parent.getChildren()) {

                        if (sibling == box) {
                            continue;
                        }

//                        if ((highest == CssBox.AUTO) ||
//                                ((sibling.getZ() != CssBox.AUTO) && (sibling.getZ() > highest))) {
                        if ((highest == CssValue.AUTO)
                        || ((sibling.getZ() != CssValue.AUTO) && (sibling.getZ() > highest))) {
                            highest = sibling.getZ();
                        }
                    }
                }

//                if (highest == CssBox.AUTO) {
                if (highest == CssValue.AUTO) {
                    highest = 500;
                } else {
                    highest++;
                }

                try {
//                    doc.getWebForm().getDomSynchronizer().setUpdatesSuspended(bean, true);
//                    doc.getWebForm().setUpdatesSuspended(componentRootElement, true);
//                    webform.setUpdatesSuspended(componentRootElement, true);
                    jsfForm.setUpdatesSuspended(componentRootElement, true);

//                    XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
                    
                    List<StyleData> set = new ArrayList<StyleData>(1);
                    set.add(new StyleData(XhtmlCss.Z_INDEX, Integer.toString(highest)));
// <removing design bean manipulation in engine>
//                    engine.updateLocalStyleValues((RaveElement)e, set, null);
// ====
//                    Util.updateLocalStyleValuesForElement(e,
//                            (StyleData[])set.toArray(new StyleData[set.size()]), null);
//                    WebForm.getDomProviderService().updateLocalStyleValuesForElement(componentRootElement,
//                            set.toArray(new StyleData[set.size()]), null);
                    JsfSupportUtilities.updateLocalStyleValuesForElement(componentRootElement,
                            set.toArray(new StyleData[set.size()]), null);
// </removing design bean manipulation in engine>
                } finally {
//                    doc.getWebForm().getDomSynchronizer().setUpdatesSuspended(bean, false);
//                    doc.getWebForm().setUpdatesSuspended(componentRootElement, false);
//                    webform.setUpdatesSuspended(componentRootElement, false);
                    jsfForm.setUpdatesSuspended(componentRootElement, false);
                }
            }
        } finally {
//            doc.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//            webform.writeUnlock(writeLock);
            jsfForm.writeUnlock(writeLock);
        }
    }

    // XXX Moved from designer/../GridHandler.
//    public void back(WebForm webform, List<CssBox> boxes) {
    public void backComponents(Box[] boxes) {
//        Document doc = webform.getDocument();

//        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(GridHandler.class, "SendToBack")); // NOI18N
//        DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(GridHandler.class, "SendToBack")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_SendToBack")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_SendToBack")); // NOI18N
        try {
//            doc.writeLock(NbBundle.getMessage(GridHandler.class, "SendToBack")); // NOI18N

//            int num = boxes.size();
            int num = boxes.length;

//            for (int i = 0; i < num; i++) {
//                CssBox box = boxes.get(i);
//            for (CssBox box : boxes) {
            for (Box box : boxes) {
//                MarkupDesignBean bean = box.getDesignBean();
//                MarkupDesignBean bean = CssBox.getMarkupDesignBeanForCssBox(box);
//                Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
                Element componentRootElement = box.getComponentRootElement();
//                assert bean != null;

//                Element e = box.getElement();
//                Element e = componentRootElement;

//                if (e == null) {
//                    e = bean.getElement();
//                }

//                assert e != null;
                if (componentRootElement == null) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new NullPointerException("There is no component root element for box=" + box));
                    continue;
                }

                // Locate the lowest z index in box' parent
                // XXX is auto less than 0?
//                int lowest = CssBox.AUTO;
                int lowest = CssValue.AUTO;
//                CssBox parent = box.getParent();
                Box parent = box.getParent();

                // #6358276 NPE.
                if(parent != null) {
//                    for (int j = 0, m = parent.getBoxCount(); j < m; j++) {
//                        CssBox sibling = parent.getBox(j);
                    for (Box sibling : parent.getChildren()) {
                        if (sibling == box) {
                            continue;
                        }

//                        if ((lowest == CssBox.AUTO) ||
//                                ((sibling.getZ() != CssBox.AUTO) && (sibling.getZ() < lowest))) {
//                            lowest = sibling.getZ();
//                        }
                        if ((lowest == CssValue.AUTO)
                        || ((sibling.getZ() != CssValue.AUTO) && (sibling.getZ() < lowest))) {
                            lowest = sibling.getZ();
                        }
                    }
                }

//                if (lowest == CssBox.AUTO) {
                if (lowest == CssValue.AUTO) {
                    lowest = 500;
                } else {
                    lowest--;
                }

                try {
//                    webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//                    webform.setUpdatesSuspended(componentRootElement, true);
                    jsfForm.setUpdatesSuspended(componentRootElement, true);

//                    XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
                    
                    List<StyleData> set = new ArrayList<StyleData>(1);
                    set.add(new StyleData(XhtmlCss.Z_INDEX, Integer.toString(lowest)));
// <removing design bean manipulation in engine>
//                    engine.updateLocalStyleValues((RaveElement)e, set, null);
// ====
//                    Util.updateLocalStyleValuesForElement(e,
//                            (StyleData[])set.toArray(new StyleData[set.size()]), null);
//                    WebForm.getDomProviderService().updateLocalStyleValuesForElement(componentRootElement,
//                            set.toArray(new StyleData[set.size()]), null);
                    JsfSupportUtilities.updateLocalStyleValuesForElement(componentRootElement,
                            set.toArray(new StyleData[set.size()]), null);
// </removing design bean manipulation in engine>
                } finally {
//                    webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//                    webform.setUpdatesSuspended(componentRootElement, false);
                    jsfForm.setUpdatesSuspended(componentRootElement, false);
                }
            }
        } finally {
//            doc.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//            webform.writeUnlock(writeLock);
            jsfForm.writeUnlock(writeLock);
        }
    }

    // XXX Moved from designer/../GridHandler.
    /** Resize the given component to new dimensions.
     * Note that the x,y position might change too, for example, when
     * you resize the component by dragging a selection handle on the
     * top or left edges of the component.
     *
     * <p>
     * @param editor The editor containing the resized component
     * @param component Component being resized
     * @param element The DOM element for the component
     * @param newX The left edge of the component after resize
     * @param xMoved True iff the left edge position changed during the resize
     * @param newY The top edge of the component after resize
     * @param yMoved True iff the top edge position moved during the resize
     * @param newWidth The new width after resize
     * @param newHeight The new height after resize
     * @param box Box being resized
     * @param snapDisabled If true, skip snapping
     * @todo Should I use floating point coordinates instead?
     */
//    public void resize(DesignerPane editor, Element componentRootElement, /*MarkupDesignBean bean,*/ int newX, boolean xMoved,
//        int newY, boolean yMoved, int newWidth, boolean widthChanged, int newHeight,
//        boolean heightChanged, CssBox box, boolean snapDisabled) {
    public void resizeComponent(Designer designer, Element componentRootElement, /*MarkupDesignBean bean,*/ int newX, boolean xMoved,
        int newY, boolean yMoved, int newWidth, boolean widthChanged, int newHeight,
        boolean heightChanged, Box box, boolean snapEnabled) {
        // Locate a grid layout parent
//        Document doc = editor.getDocument();
//        WebForm webform = doc.getWebForm();
//        WebForm webform = editor.getWebForm();

        int x = newX;
        int y = newY;

        if (snapEnabled) {
//            x = snapX(newX, box.getPositionedBy());
//            y = snapY(newY, box.getPositionedBy());
            x = designer.snapX(newX, box.getPositionedBy());
            y = designer.snapY(newY, box.getPositionedBy());
        }

        Element element = box.getElement();

        if (element == null) {
//            element = bean.getElement();
            element = componentRootElement;
        }

        boolean absolute = isAbsolutelyPositioned(element);

//        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(GridHandler.class, "ResizeComponent")); // NOI18N
//        DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(GridHandler.class, "ResizeComponent")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_ResizeComponent")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_ResizeComponent")); // NOI18N
        // Gotta set width and height attributes!
        try {
//            doc.writeLock(NbBundle.getMessage(GridHandler.class, "ResizeComponent")); // NOI18N

            // prevent multiple updates for the same element - only need a single refresh
//            webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//            webform.setUpdatesSuspended(componentRootElement, true);
            jsfForm.setUpdatesSuspended(componentRootElement, true);

            List<StyleData> set = new ArrayList<StyleData>(5);
            List<StyleData> remove = new ArrayList<StyleData>(3);

            if (absolute && (xMoved || yMoved)) {
//                set.add(new StyleData(XhtmlCss.POSITION_INDEX, CssConstants.CSS_ABSOLUTE_VALUE));
                set.add(new StyleData(XhtmlCss.POSITION_INDEX, CssProvider.getValueService().getAbsoluteValue()));

//                CssBox parentBox = box.getParent();
                Box parentBox = box.getParent();

                if (xMoved) {
                    set.add(getHorizontalCssSetting(x, newWidth, box, parentBox, element));
                }

                if (yMoved) {
                    set.add(getVerticalCssSetting(y, newHeight, box, parentBox, element));
                }
            }

            if (widthChanged) {
//                if (!DndHandler.setDesignProperty(bean, HtmlAttribute.WIDTH, newWidth, webform)) {
//                if (!WebForm.getDomProviderService().setDesignProperty(bean, HtmlAttribute.WIDTH, newWidth)) {
//                if (!WebForm.getDomProviderService().setStyleAttribute(componentRootElement, HtmlAttribute.WIDTH, newWidth)) {
                if (!JsfSupportUtilities.setStyleAttribute(componentRootElement, HtmlAttribute.WIDTH, newWidth)) {
                    set.add(new StyleData(XhtmlCss.WIDTH_INDEX, Integer.toString(newWidth) + "px")); // NOI18N
                } else {
                    // Ensure that we don't have a conflict
                    remove.add(new StyleData(XhtmlCss.WIDTH_INDEX));
                }
            }

            if (heightChanged) {
//                if (!DndHandler.setDesignProperty(bean, HtmlAttribute.HEIGHT, newHeight, webform)) {
//                if (!WebForm.getDomProviderService().setDesignProperty(bean, HtmlAttribute.HEIGHT, newHeight)) {
                if (!JsfSupportUtilities.setStyleAttribute(componentRootElement, HtmlAttribute.HEIGHT, newHeight)) {
                    set.add(new StyleData(XhtmlCss.HEIGHT_INDEX, Integer.toString(newHeight) + "px")); // NOI18N
                } else {
                    // Ensure that we don't have a conflict
                    remove.add(new StyleData(XhtmlCss.HEIGHT_INDEX));
                }
            }

//            XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
            
// <removing design bean manipulation in engine>
//            engine.updateLocalStyleValues((RaveElement)element, set, remove);
// ====
//            Util.updateLocalStyleValuesForElement(element,
//                    (StyleData[])set.toArray(new StyleData[set.size()]),
//                    (StyleData[])remove.toArray(new StyleData[remove.size()]));
//            WebForm.getDomProviderService().updateLocalStyleValuesForElement(element,
//                    set.toArray(new StyleData[set.size()]),
//                    remove.toArray(new StyleData[remove.size()]));
            JsfSupportUtilities.updateLocalStyleValuesForElement(element,
                    set.toArray(new StyleData[set.size()]),
                    remove.toArray(new StyleData[remove.size()]));
// </removing design bean manipulation in engine>
        } finally {
//            webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//            webform.setUpdatesSuspended(componentRootElement, false);
            jsfForm.setUpdatesSuspended(componentRootElement, false);
//            doc.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//            webform.writeUnlock(writeLock);
            jsfForm.writeUnlock(writeLock);
        }
    }

    // XXX Moved from designer/../GridHandler.
    public void snapToGrid(Designer designer) {
////        GridHandler handler = GridHandler.getInstance();
////        DesignerPane editor = webForm.getPane();
//        SelectionManager sm = webForm.getSelection();
////        Iterator it = sm.iterator();
//        Element[] componentRootElements = sm.getSelectedComponentRootElements();
////        ModelViewMapper mapper = webform.getMapper();
        Element[] componentRootElements = designer.getSelectedComponents();
        
        boolean haveMoved = false;
//        Document doc = webform.getDocument();

//        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(AlignAction.class, "LBL_SnapToGrid")); // NOI18N
//        DomProvider.WriteLock writeLock = webForm.writeLock(NbBundle.getMessage(GridHandler.class, "LBL_SnapToGrid")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_SnapToGrid")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_SnapToGrid")); // NOI18N
        try {
//            doc.writeLock(NbBundle.getMessage(AlignAction.class, "LBL_SnapToGrid")); // NOI18N

//            while (it.hasNext()) {
//                MarkupDesignBean bean = (MarkupDesignBean)it.next();
            for (Element componentRootElement : componentRootElements) {
//                MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//                CssBox box = mapper.findBox(bean);
//                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webForm.getPane().getPageBox(), componentRootElement);
                Box box = designer.findBoxForComponentRootElement(componentRootElement);

                if (box == null) {
                    continue;
                }

//                boolean canAlign = box.getBoxType().isAbsolutelyPositioned();
                boolean canAlign = box.isAbsolutelyPositioned();

                if (!canAlign) {
                    continue;
                }

                int x = box.getAbsoluteX();
                int y = box.getAbsoluteY();

                // Snap to grid.
//                x = snapX(x, box.getPositionedBy());
//                y = snapY(y, box.getPositionedBy());
                x = designer.snapX(x, box.getPositionedBy());
                y = designer.snapY(y, box.getPositionedBy());
                
//                moveTo(editor, /*bean,*/ box, x, y /*, false*/);
//                webForm.getDomDocument().moveComponentTo(box, x, y);
                moveComponentTo(box, x, y);
                
                haveMoved = true;
            }
        } finally {
//            doc.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//            webForm.writeUnlock(writeLock);
            jsfForm.writeUnlock(writeLock);
        }

//        if (!haveMoved) {
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GridHandler.class, "MSG_AlignAbsolute"));
//            UIManager.getLookAndFeel().provideErrorFeedback(webForm.getPane());
//        }
        
//        fireComponentsMoved(new DefaultDomDocumentEvent(this, null));
    }

    // XXX Moved from designer/../GridHandler.
    public void align(Designer designer, JsfForm.Alignment alignment) {
        // Primary
//        SelectionManager sm = webForm.getSelection();
//
//        if (sm.isSelectionEmpty()) {
//            return;
//        }
//
//        sm.pickPrimary();
        Element primaryComponnetRootElement = designer.getPrimarySelectedComponent();

//        ModelViewMapper mapper = webform.getMapper();
//        CssBox primaryBox = mapper.findBox(sm.getPrimary());
//        CssBox primaryBox = ModelViewMapper.findBox(webForm.getPane().getPageBox(), sm.getPrimary());
        Box primaryBox = designer.findBoxForComponentRootElement(primaryComponnetRootElement);

        if (primaryBox == null) {
            return;
        }

        boolean haveMoved = false;
//        Document doc = webform.getDocument();

//        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(SelectionManager.class, "Align")); // NOI18N
//        DomProvider.WriteLock writeLock = webForm.writeLock(NbBundle.getMessage(SelectionManager.class, "Align")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_Align")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_Align")); // NOI18N
        try {
//            doc.writeLock(NbBundle.getMessage(SelectionManager.class, "Align")); // NOI18N

//            GridHandler handler = GridHandler.getInstance();
//            DesignerPane editor = webForm.getPane();
//            boolean canAlign = primaryBox.getBoxType().isAbsolutelyPositioned();
            boolean canAlign = primaryBox.isAbsolutelyPositioned();
            
            int x = primaryBox.getAbsoluteX();
            int y = primaryBox.getAbsoluteY();
            int w = primaryBox.getWidth();
            int h = primaryBox.getHeight();
//            Iterator it = sm.iterator();
//
//            while (canAlign && it.hasNext()) {
//                MarkupDesignBean bean = (MarkupDesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
            for (Element componentRootElement : designer.getSelectedComponents()) {
//                MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//                CssBox box = mapper.findBox(bean);
//                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webForm.getPane().getPageBox(), componentRootElement);
                Box box = designer.findBoxForComponentRootElement(componentRootElement);

                if (box == null) {
                    continue;
                }

                // XXX Should I use isPositioned() instead? (e.g. are relative
                // positioned boxes alignable?
//                if (!box.getBoxType().isAbsolutelyPositioned()) {
                if (!box.isAbsolutelyPositioned()) {
                    continue;
                }

                haveMoved = true;

                /*
                 Element element = FacesSupport.getElement(fob.component);
                 if (element == null) {
                 continue;
                 }
                 */
                switch (alignment) {
                case TOP:
//                    moveTo(editor, /*bean,*/ box, box.getAbsoluteX(), y/*, true*/);
//                    webForm.getDomDocument().moveComponentTo(box, box.getAbsoluteX(), y);
                    moveComponentTo(box, box.getAbsoluteX(), y);

                    break;

                case MIDDLE:
//                    moveTo(editor, /*bean,*/ box, box.getAbsoluteX(),
//                        (y + (h / 2)) - (box.getHeight() / 2)/*, true*/);
//                    webForm.getDomDocument().moveComponentTo(box, box.getAbsoluteX(), (y + (h / 2)) - (box.getHeight() / 2));
                    moveComponentTo(box, box.getAbsoluteX(), (y + (h / 2)) - (box.getHeight() / 2));

                    break;

                case BOTTOM:
//                    moveTo(editor, /*bean,*/ box, box.getAbsoluteX(),
//                        (y + h) - box.getHeight()/*, true*/);
//                    webForm.getDomDocument().moveComponentTo(box, box.getAbsoluteX(), (y + h) - box.getHeight());
                    moveComponentTo(box, box.getAbsoluteX(), (y + h) - box.getHeight());

                    break;

                case LEFT:
//                    moveTo(editor, /*bean,*/ box, x, box.getAbsoluteY()/*, true*/);
//                    webForm.getDomDocument().moveComponentTo(box, x, box.getAbsoluteY());
                    moveComponentTo(box, x, box.getAbsoluteY());

                    break;

                case CENTER:
//                    moveTo(editor, /*bean,*/ box, (x + (w / 2)) - (box.getWidth() / 2),
//                        box.getAbsoluteY()/*, true*/);
//                    webForm.getDomDocument().moveComponentTo(box, (x + (w / 2)) - (box.getWidth() / 2), box.getAbsoluteY());
                    moveComponentTo(box, (x + (w / 2)) - (box.getWidth() / 2), box.getAbsoluteY());

                    break;

                case RIGHT:
//                    moveTo(editor, /*bean,*/ box, (x + w) - box.getWidth(), box.getAbsoluteY()/*, true*/);
//                    webForm.getDomDocument().moveComponentTo(box, (x + w) - box.getWidth(), box.getAbsoluteY());
                    moveComponentTo(box, (x + w) - box.getWidth(), box.getAbsoluteY());

                    break;
                }
            }
        } finally {
//            doc.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//            webForm.writeUnlock(writeLock);
            jsfForm.writeUnlock(writeLock);
        }

//        if (!haveMoved) {
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GridHandler.class,"MSG_AlignAbsolute"));
//            UIManager.getLookAndFeel().provideErrorFeedback(webForm.getPane());
//        }
        
//        fireComponentsMoved(new DefaultDomDocumentEvent(this, null));
    }

    // XXX Moved from designer/../DesignerCaret
    /**
     * @todo Check deletion back to first char in <body> !
     * @todo Check read-only state etc
     */
    public boolean deleteNextChar(Designer designer, DomRange range) {
        if (range == null) {
            return false;
        }

//            UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_DeleteText")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_DeleteText")); // NOI18N
        try {
            // TODO - compute previous visual position, decide if it's
            //    isWithinEditableRegion(Position pos) 
            // and if so, set the range to it and delete the range.
    //        if (hasSelection()) {
    //            removeSelection();
            if (!range.isEmpty()) {
                deleteRangeContents(range);
                return true;
            }

    //        Document doc = component.getDocument();
    //        Position mark = range.getMark();
            DomPosition mark = range.getMark();
    //        Position dot = ModelViewMapper.computeArrowRight(doc.getWebForm(), mark);
    //        Position dot = ModelViewMapper.computeArrowRight(component.getWebForm(), mark);
    //        DomPosition dot = ModelViewMapper.computeArrowRight(component.getWebForm(), mark);
            DomPosition dot = designer.computeNextPosition(mark);

    //        if ((dot == Position.NONE) || !isWithinEditableRegion(dot)) {
    //        if ((dot == DomPosition.NONE) || !isWithinEditableRegion(dot)) {
            if ((dot == DomPosition.NONE) || !designer.isInsideEditableRegion(dot)) {
    //            UIManager.getLookAndFeel().provideErrorFeedback(component); // beep

                return false;
            }

            range.setRange(mark.getNode(), mark.getOffset(), dot.getNode(), dot.getOffset());
    //        range.deleteContents();
    //        removeSelection();
            deleteRangeContents(range);

            return true;
        } finally {
//                doc.writeUnlock();
            jsfForm.writeUnlock(writeLock);
        }
    }

    // XXX Moved from designer/../DesignerCaret.
    /**
     * @todo Check deletion back to first char in <body> !
     * @todo Check read-only state etc
     */
    public boolean deletePreviousChar(Designer designer, DomRange range) {
        if (range == null) {
            return false;
        }
        
//            UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_DeleteText")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_DeleteText")); // NOI18N
        try {
            // TODO - compute previous visual position, decide if it's
            //    isWithinEditableRegion(Position pos) 
            // and if so, set the range to it and delete the range.
    //        if (hasSelection()) {
    //            removeSelection();
            if (!range.isEmpty()) {
                deleteRangeContents(range);

                return true;
            }

    //        Document doc = component.getDocument();
    //        Position mark = range.getMark();
            DomPosition mark = range.getMark();
    //        Position dot = ModelViewMapper.computeArrowLeft(doc.getWebForm(), mark);
    //        Position dot = ModelViewMapper.computeArrowLeft(component.getWebForm(), mark);
    //        DomPosition dot = ModelViewMapper.computeArrowLeft(component.getWebForm(), mark);
            DomPosition dot = designer.computePreviousPosition(mark);

    //        if ((dot == Position.NONE) || !isWithinEditableRegion(dot)) {
    //        if ((dot == DomPosition.NONE) || !isWithinEditableRegion(dot)) {
            if ((dot == DomPosition.NONE) || !designer.isInsideEditableRegion(dot)) {
    //            UIManager.getLookAndFeel().provideErrorFeedback(component); // beep

                return false;
            }

            range.setRange(dot.getNode(), dot.getOffset(), mark.getNode(), mark.getOffset());

            // XXX DEBUGGING ONLY
            /*
            Element element = doc.getBody();
            if (element != null) {
                System.out.println("BEFORE DELETION: " + org.netbeans.modules.visualweb.css2.FacesSupport.getHtmlStream(element));
            }
            */
    //        range.deleteContents();
    //        removeSelection();
            deleteRangeContents(range);

            // XXX DEBUGGING ONLY

            /*
            if (element != null) {
                System.out.println("BEFORE DELETION: " + org.netbeans.modules.visualweb.css2.FacesSupport.getHtmlStream(element));
            }
            */
            return true;
        } finally {
//                doc.writeUnlock();
//                webform.getModel().writeUnlock(undoEvent);
            jsfForm.writeUnlock(writeLock);
        }

    }

    public void deleteComponents(Element[] componentRootElements) {
//        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(SelectionTopComp.class, "DeleteSelection")); // NOI18N
//        DomProvider.WriteLock writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_DeleteComponents")); // NOI18N
        UndoEvent writeLock = jsfForm.writeLock(NbBundle.getMessage(DomDocumentImpl.class, "LBL_DeleteComponents")); // NOI18N
        try {
            for (Element componentRootElement : componentRootElements) {
                if (JsfSupportUtilities.isSpecialComponent(componentRootElement)) {
                    continue;
                }

                DesignBean designBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
                if (designBean == null) {
                    return;
                }
                jsfForm.deleteDesignBean(designBean);
            }
        } finally {
//            doc.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
            jsfForm.writeUnlock(writeLock);

        }
    }
    
    // XXX
    boolean isRenderedNode(Node node) {
        return jsfForm.isRenderedNode(node);
    }

    JsfForm getJsfForm() {
        return jsfForm;
    }
}
