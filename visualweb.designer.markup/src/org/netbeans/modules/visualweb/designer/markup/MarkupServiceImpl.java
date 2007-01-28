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


package org.netbeans.modules.visualweb.designer.markup;


import org.netbeans.modules.visualweb.api.insync.InSyncService;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * Impl of the <code>MarkupService</code>.
 * <p>
 * Note: Do not use this from other modules, use <code>MarkupService</code>
 * This won't be public.
 * </p>
 *
 * @author Peter Zavadsky
 */
public final class  MarkupServiceImpl {


    private MarkupServiceImpl() {
    }


//    public static String expandHtmlEntities(String html, boolean warn, Node node) {
//        FileObject fileObject = null;
//        int lineNumber = -1;
//        if (node != null) {
//            if (node.getNodeType() == Node.TEXT_NODE) {
//                node = node.getParentNode();
//            }
//
//            Element element = InSyncService.getProvider().getCorrespondingSourceElement((Element)node);
//
//            if (element != null) {
//                Document doc = element.getOwnerDocument();
//                // <markup_separation>
////                    MarkupUnit unit = doc.getMarkup();
////                    if (unit != null) {
////                        fileObject = unit.getFileObject();
////                        lineNumber = unit.computeLine(element);
////                    }
//                // ====
//                fileObject = InSyncService.getProvider().getFileObject(doc);
//                lineNumber = InSyncService.getProvider().computeLine(doc, element);
//                // </markup_seaparation>
//            }
//        }
//
//        return Entities.getExpandedString(html, warn, fileObject, lineNumber);
//    }
//
//    public static int getUnexpandedOffset(String unexpanded, int expandedOffset) {
//        return Entities.getUnexpandedOffset(unexpanded, expandedOffset);
//    }
//
//    public static int getExpandedOffset(String unexpanded, int unexpandedOffset) {
//        return Entities.getExpandedOffset(unexpanded, unexpandedOffset);
//    }

//    public static XhtmlElement getCorrespondingSourceElement(XhtmlElement element) {
//        return MarkupUtilities.getCorrespondingSourceElement(element);
//    }


//    // <utilities methods>
//    public static URL getCascadedXMLBase(Element elt) {
//        return MarkupUtilities.getCascadedXMLBase(elt);
//    }

    // XXX From org.netbeans.modules.visualweb.insync.Util.
//    /**
//     * Given an element which may be in a rendered DocumentFragment, return the corresponding JSF
//     * element in the source.
//     */
//    public static Element getCorrespondingSourceElement(Element element) {
//        return MarkupUtilities.getCorrespondingSourceElement(element);
//    }
//
//
//    // <markup_separation> copied from insync/Util
//    // XXX This should be separate utility api, openide extension or what.
//    /**
//     * Show the given line in a particular file.
//     *
//     * @param filename The full path to the file
//     * @param lineno The line number
//     * @param openFirst Usually you'll want to pass false. When set to true, this will first open
//     *            the file, then request the given line number; this works around certain bugs for
//     *            some editor types like CSS files.
//     */
//    public static void show(String filename, int lineno, int column, boolean openFirst) {
//        MarkupUtilities.show(filename, lineno, column, openFirst);
//    }
//
//    /**
//     * Show the given line in a particular file.
//     *
//     * @param fileObject The FileObject for the file
//     * @param lineno The line number
//     * @param openFirst Usually you'll want to pass false. When set to true, this will first open
//     *            the file, then request the given line number; this works around certain bugs for
//     *            some editor types like CSS files.
//     */
//    public static void show(FileObject fileObject, int lineno, int column,
//                            boolean openFirst) {
//        MarkupUtilities.show(fileObject, lineno, column, openFirst);
//    }
    
//    // <markup_separation> moved from insync/MarkupUnit
//    /** Convert the given URL to a path: decode spaces from %20's, etc.
//     * If the url does not begin with "file:" it will not do anything.
//     * @todo Find a better home for this method
//     */
//    public static String fromURL(String url) {
//        return MarkupUtilities.fromURL(url);
//    }
//    // </markup_separation>


// <error_handling> Moved from RaveDocument.
// XXX These methods are suspicoius, they deal with openide output window.
//    // and there may not be any knowing about it from this impls.
//    /** Clear document related errors. 
//     * @param delayed When set, don't actually clear the errors right now;
//     * it clears the errors next time another error is added. */
//    public static void clearErrors(boolean delayed) {
//        MarkupUtilities.clearErrors(delayed);
//    }
    
//    /** 
//     * Display the given error message to the user. The optional listener argument
//     * (pass in null if not applicable) will make the line hyperlinked and the
//     * listener is invoked to process any user clicks.
//     * @param message The string to be displayed to the user
//     * @param listener null, or a listener to be notified when the user clicks
//     *   the linked message
//     */
//    public static void displayError(String message, OutputListener listener) {
//        MarkupUtilities.displayError(message, listener);
//    }

//    /**
//     * Cause the panel/window within which errors are displayed to come to the front if possible.
//     *
//     */
//    public static void selectErrors() {
//        MarkupUtilities.selectErrors();
//    }
//    
//    public static void displayError(String message) {
//        MarkupUtilities.displayError(message);
//    }
//    
//    public static void displayErrorForLocation(String message, Object location, int line, int column) {
//        MarkupUtilities.displayErrorForLocation(message, location, line, column);
//    }
//    
//    public static void displayErrorForFileObject(String message, FileObject fileObject, int line, int column) {
//        MarkupUtilities.displayErrorForFileObject(message, fileObject, line, column);
//    }
//    
//    /** Given a general location object provided from the CSS parser,
//     * compute the correct file name to use.
//     */
//    public static String computeFilename(Object location) {
//        return MarkupUtilities.computeFilename(location);
//    }
//    /** Given a general location object provided from the CSS parser,
//     * compute the correct line number to use.
//     */
//    public static int computeLineNumber(Object location, int line) {
//        return MarkupUtilities.computeLineNumber(location, line);
//    }
// </error_handling>

//    // XXX Moved from DesignerService.
//    /**
//     * Return an InputStream for the given CSS URI, if the corresponding CSS
//     * file is open and edited. Otherwise return null.
//     *
//     * @param uri The URI to the CSS file. <b>MUST</b> be an absolute file url!
//     * @return An InputStream for the live edited CSS
//     */
//    public static InputStream getOpenCssStream(String uriString) {
//        return MarkupUtilities.getOpenCssStream(uriString);
//    }
//    // </utilities methods>
    
    // <separation of models> moved from designer/FacesSupport
//    /**
//     * Generate the html string from the given node. This will return
//     * an empty string unless the Node is an Element or a DocumentFragment
//     * or a Document.
//     */
//    public static String getHtmlStream(Node node) {
//        if (node.getNodeType() == Node.ELEMENT_NODE) {
//            return getHtmlStream((Element)node);
//        } else if (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
//            return getHtmlStream((DocumentFragment)node);
//        } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
//            return getHtmlStream((org.w3c.dom.Document)node);
//        } else if ((node.getNodeType() == Node.TEXT_NODE) ||
//                (node.getNodeType() == Node.CDATA_SECTION_NODE)) {
//            return node.getNodeValue();
//        } else {
//            return "";
//        }
//    }
//
//    /** Generate the html string from the given element */
//    public static String getHtmlStream(Element element) {
//        StringWriter w = new StringWriter(); // XXX initial size?
//        OutputFormat format = new OutputFormat(element.getOwnerDocument(), null, true); // default enc, do-indent
//        format.setLineWidth(160);
//        format.setIndent(4);
//
//        JspxSerializer serializer = new JspxSerializer(w, format);
//
//        try {
//            serializer.serialize(element);
//        } catch (java.io.IOException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        return w.getBuffer().toString();
//    }
//
//    /** Generate the html string from the given element. Does formatting. */
//    public static String getHtmlStream(org.w3c.dom.Document document) {
//        StringWriter w = new StringWriter(); // XXX initial size?
//        OutputFormat format = new OutputFormat(document, null, true); // default enc, do-indent
//        format.setLineWidth(160);
//        format.setIndent(4);
//
//        JspxSerializer serializer = new JspxSerializer(w, format);
//
//        try {
//            serializer.serialize(document);
//        } catch (java.io.IOException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        return w.getBuffer().toString();
//    }
//
//    /** Generate the html string from the given document fragment */
//    public static String getHtmlStream(DocumentFragment df) {
//        OutputFormat format = new OutputFormat(df.getOwnerDocument()); // default enc, do-indent
//        format.setLineWidth(160);
//        format.setIndent(4);
//
//        StringWriter w = new StringWriter(); // XXX initial size?
//        JspxSerializer serializer = new JspxSerializer(w, format);
//
//        try {
//            serializer.serialize(df);
//        } catch (java.io.IOException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        return w.getBuffer().toString();
//    }
    // </separation of models> moved from designer/FacesSupport
    
    public static DocumentBuilder createRaveSourceDocumentBuilder(boolean useCss) throws ParserConfigurationException {
        return RaveDocumentBuilderFactory.newDocumentBuilder(useCss, true);
    }
    
    public static DocumentBuilder createRaveRenderedDocumentBuilder(boolean useCss) throws ParserConfigurationException {
        return RaveDocumentBuilderFactory.newDocumentBuilder(useCss, false);
    }
    
    // Moved from RaveDocument.
    /**
     * Given two matching node trees where one represents a tree of
     * nodes rendered from the other, update the source and render references
     * in the nodes such that the "src" tree is marked as the source nodes
     * for "dst".
     */
    public static void markRendered(Node src, Node dst) {
//        if (src instanceof RaveElement) {
//            assert dst instanceof RaveElement;            
//            RaveElement srcElement = (RaveElement)src;
//            RaveElement dstElement = (RaveElement)dst;
////            srcElement.source = null;
////            dstElement.source = srcElement;
//            dstElement.setSource(srcElement);
//        } else if (src instanceof RaveText) {
//            assert dst instanceof RaveText;      
//            RaveText srcText = (RaveText)src;
//            RaveText dstText = (RaveText)dst;
////            srcText.source = null;
////            dstText.source = srcText;
//            dstText.setSource(srcText);
//        }
        if (src instanceof RaveSourceElement) {
            assert dst instanceof RaveRenderedElement;            
            RaveSourceElement srcElement = (RaveSourceElement)src;
            RaveRenderedElement dstElement = (RaveRenderedElement)dst;
//            srcElement.source = null;
//            dstElement.source = srcElement;
            dstElement.setSource(srcElement);
        } else if (src instanceof RaveSourceText) {
            assert dst instanceof RaveRenderedText;      
            RaveSourceText srcText = (RaveSourceText)src;
            RaveRenderedText dstText = (RaveRenderedText)dst;
//            srcText.source = null;
//            dstText.source = srcText;
            dstText.setSource(srcText);
        }
        
        NodeList srcChildren = src.getChildNodes();
        NodeList dstChildren = dst.getChildNodes();
        int len = srcChildren.getLength();
        assert dstChildren.getLength() == len;
        
        for (int i = 0; i < len; i++) {
            markRendered(srcChildren.item(i), dstChildren.item(i));
        }
    }
    
    public static void markRenderedNodes(Element parent, Node node) {
//        RaveElement element;
//        if (node instanceof RaveElement) {
//            element = (RaveElement)node;
        RaveRenderedElement element;
        if (node instanceof RaveRenderedElement) {
            element = (RaveRenderedElement)node;
        } else {
            element = null;
        }

        // We work our way right to left, bottom to top, to ensure that
        // the last setJsp call made for a particular jsp node will be the
        // leftmost, topmost rendered node for that jsp element.
        NodeList nl = node.getChildNodes();

        for (int n = nl.getLength(), i = n - 1; i >= 0; i--) {
            markRenderedNodes(element, nl.item(i));
        }

//        if (node instanceof RaveRenderNode) {
//            RaveRenderNode rn = (RaveRenderNode)node;
        
//        if (node instanceof RaveElementImpl || node instanceof RaveTextImpl) {
        if (node instanceof RaveRenderedElementImpl || node instanceof RaveRenderedTextImpl) {

            if (element != null) {
//                if ((parent != null) && (parent.getDesignBean() == element.getDesignBean())) {
                MarkupDesignBean markupDesignBean = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
                MarkupDesignBean parentMarkupDesignBean = parent == null ? null : InSyncService.getProvider().getMarkupDesignBeanForElement(parent);
                if (markupDesignBean == parentMarkupDesignBean) {
                    element.setSource(null);
//                } else if (element.getDesignBean() != null) {
//                    element.setSource((RaveElement)element.getDesignBean().getElement());
                } else if (markupDesignBean != null) {
//                    element.setSource((RaveElement)InSyncService.getProvider().getMarkupDesignBeanForElement(element).getElement());
                    // #6455709 Some strange class cast exception, couldn't reproduce, needs to be tested more carefully.
                    Element elem = markupDesignBean.getElement();
                    if (elem instanceof RaveSourceElement) {
                        element.setSource((RaveSourceElement)elem);
                    } else {
                        // XXX Log a problem?
                        element.setSource(null);
                    }
                } else {
//                    rn.markRendered();
//                    if (node instanceof RaveElementImpl) {
//                        ((RaveElementImpl)node).markRendered();
//                    } else if (node instanceof RaveTextImpl) {
//                        ((RaveTextImpl)node).markRendered();
//                    }
                }
            } else {
//                rn.markRendered();
//                if (node instanceof RaveElementImpl) {
//                    ((RaveElementImpl)node).markRendered();
//                } else if (node instanceof RaveTextImpl) {
//                    ((RaveTextImpl)node).markRendered();
//                }
            }
        }
    }
    
    public static void markJspxSource(Node n) {
//        if (n instanceof RaveRenderNode) {
//            ((RaveRenderNode)n).setJspx(true);
//        }
//        if (n instanceof RaveElementImpl) {
//            ((RaveElementImpl)n).setJspx(true);
//        } else if (n instanceof RaveTextImpl) {
//            ((RaveTextImpl)n).setJspx(true);
//        }
        if (n instanceof AbstractRaveElement) {
            ((AbstractRaveElement)n).setJspx(true);
        } else if (n instanceof AbstractRaveText) {
            ((AbstractRaveText)n).setJspx(true);
        }

        NodeList list = n.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            markJspxSource(list.item(i));
        }
    }

    public static void setInputEncodingForDocument(Document document, String inputEncoding) {
        if (document instanceof DocumentImpl) {
            ((DocumentImpl)document).setInputEncoding(inputEncoding);
        }
    }
    
    public static String getStyleText(Element e) {
        String text = "";
        Node n = e.getFirstChild();
        if (n != null) {
            StringBuffer sb = new StringBuffer();
            while (n != null) {
                if (n.getNodeType() == Node.CDATA_SECTION_NODE
                    // Unlike javascript, where the first line in a comment should be treated
                    // as a comment, the browsers seem to treat all comment text as style rules
                    || n.getNodeType() == Node.COMMENT_NODE
                    
                    || n.getNodeType() == Node.TEXT_NODE)
                    // XXX should pick up comments contents too!!
                    sb.append(n.getNodeValue());
                n = n.getNextSibling();
            }
            text = sb.toString();
            // Strip out comments?
        }
        return text;
    }
    
    // XXX From org.netbeans.modules.visualweb.insync.Util.
    public static Element getCorrespondingSourceElement(Element elem) {
//        if (!(elem instanceof RaveElement)) {
//        if (!(elem instanceof RaveElementImpl)) {
        if (!(elem instanceof RaveRenderedElementImpl)) {
            return elem;
        }
        
//        RaveElement element = (RaveElement)elem;
//        RaveElementImpl element = (RaveElementImpl)elem;
        RaveRenderedElementImpl element = (RaveRenderedElementImpl)elem;
        
        if (!element.isRendered()) {
            return element;
        }
        
        org.w3c.dom.Node node = element;
        while (node != null) {
//            if (node instanceof RaveElement) {
//                RaveElement xel = (RaveElement)node;
//            if (node instanceof RaveElementImpl) {
//                RaveElementImpl xel = (RaveElementImpl)node;
//                if (xel.isRendered()) {
//                    RaveElement src = xel.getSource();
//                    if (src != null) {
            if (node instanceof RaveRenderedElementImpl) {
                RaveRenderedElementImpl xel = (RaveRenderedElementImpl)node;
                if (xel.isRendered()) {
                    RaveSourceElement src = xel.getSource();
                    if (src != null) {
                        return src;
                    }
                }
            }
            node = node.getParentNode();
        }
        
//        return element.getSourceElement();
        return element.getSource();
    }
    
    /** Returns true if the node is rendered.
     * By "is rendered" I mean that the position points to a node
     * in a renderer-hierarchy DOM (such as HTML rendered from JSF components).
     */
    public static boolean isRenderedNode(Node node) {
//        if (node instanceof RaveRenderNode) {
//            return ((RaveRenderNode)node).isRendered();
//        }
//        if (node instanceof RaveElementImpl) {
//            return ((RaveElementImpl)node).isRendered();
//        } else if (node instanceof RaveTextImpl) {
//            return ((RaveTextImpl)node).isRendered();
//        }
        if (node instanceof AbstractRaveElement) {
            return ((AbstractRaveElement)node).isRendered();
        } else if (node instanceof AbstractRaveText) {
            return ((AbstractRaveText)node).isRendered();
        }
        return false;
    }
    
    public static boolean isJspxNode(Node node) {
//        if (node instanceof RaveRenderNode) {
//            return ((RaveRenderNode)node).isJspx();
//        }
//        if (node instanceof RaveElementImpl) {
//            return ((RaveElementImpl)node).isJspx();
//        } else if (node instanceof RaveTextImpl) {
//            return ((RaveTextImpl)node).isJspx();
//        }
        if (node instanceof AbstractRaveElement) {
            return ((AbstractRaveElement)node).isJspx();
        } else if (node instanceof AbstractRaveText) {
            return ((AbstractRaveText)node).isJspx();
        }
        return false;
    }
    
    public static void setJspxNode(Node node, boolean jspx) {
//        if (node instanceof RaveRenderNode) {
//            ((RaveRenderNode)node).setJspx(jspx);
//        }
//        if (node instanceof RaveElementImpl) {
//            ((RaveElementImpl)node).setJspx(jspx);
//        } else if (node instanceof RaveTextImpl) {
//            ((RaveTextImpl)node).setJspx(jspx);
//        }
        if (node instanceof AbstractRaveElement) {
            ((AbstractRaveElement)node).setJspx(jspx);
        } else if (node instanceof AbstractRaveText) {
            ((AbstractRaveText)node).setJspx(jspx);
        }
    }
    
    public static Node getRenderedNodeForNode(Node node) {
//        if (node instanceof RaveRenderNode) {
//            return ((RaveRenderNode)node).getRenderedNode();
//        }
//        if (node instanceof RaveElementImpl) {
//            return ((RaveElementImpl)node).getRenderedNode();
//        } else if (node instanceof RaveTextImpl) {
//            return ((RaveTextImpl)node).getRenderedNode();
//        }
        if (node instanceof RaveSourceElement) {
            return ((RaveSourceElement)node).getRendered();
        } else if (node instanceof RaveSourceText) {
            return ((RaveSourceText)node).getRendered();
        } else if (node instanceof RaveRenderedElement
        || node instanceof RaveRenderedText) { // XXX
            return node;
        }
        
        return null;
    }
    
    public static Node getSourceNodeForNode(Node node) {
//        if (node instanceof RaveRenderNode) {
//            return ((RaveRenderNode)node).getSourceNode();
//        }
//        if (node instanceof RaveElementImpl) {
//            return ((RaveElementImpl)node).getSourceNode();
//        } else if (node instanceof RaveTextImpl) {
//            return ((RaveTextImpl)node).getSourceNode();
//        }
        if (node instanceof RaveRenderedElement) {
            return ((RaveRenderedElement)node).getSource();
        } else if (node instanceof RaveRenderedText) {
            return ((RaveRenderedText)node).getSource();
        } else if (node instanceof RaveSourceElement
        || node instanceof RaveSourceText) { // XXX
            return node;
        }
        return null;
    }
    
    public static Element getRenderedElementForElement(Element element) {
//        if (element instanceof RaveElement) {
//            return ((RaveElement)element).getRendered();
//        }
        if (element instanceof RaveSourceElement) {
            return ((RaveSourceElement)element).getRendered();
        } else if (element instanceof RaveRenderedElement) { // XXX
            return element;
        }
        return null;
    }
    
    public static void setRenderedElementForElement(Element element, Element renderedElement) {
//        if (element instanceof RaveElement) {
//            ((RaveElement)element).setRendered((RaveElement)renderedElement);
//        }
        if (element instanceof RaveSourceElement) {
            ((RaveSourceElement)element).setRendered((RaveRenderedElement)renderedElement);
        }
    }
    
    public static Element getSourceElementForElement(Element element) {
//        if (element instanceof RaveElement) {
//            return ((RaveElement)element).getSource();
//        }
        if (element instanceof RaveRenderedElement) {
            return ((RaveRenderedElement)element).getSource();
        } else if (element instanceof RaveSourceElement) { // XXX
            return element;
        }
        return null;
    }
    
    public static Text getRenderedTextForText(Text text) {
//        if (text instanceof RaveText) {
//            return ((RaveText)text).getRendered();
//        }
        if (text instanceof RaveSourceText) {
            return ((RaveSourceText)text).getRendered();
        } else if (text instanceof RaveRenderedText) { // XXX
            return text;
        }
        return null;
    }
    
    public static Text getSourceTextForText(Text text) {
//        if (text instanceof RaveText) {
//            return ((RaveText)text).getSource();
//        }
        if (text instanceof RaveRenderedText) {
            return ((RaveRenderedText)text).getSource();
        } else if (text instanceof RaveSourceText) { // XXX
            return text;
        }
        return null;
    }
    
    public static void setSourceTextForText(Text text, Text sourceText) {
//        if (text instanceof RaveText) {
//            ((RaveText)text).setSource((RaveText)sourceText);
//        }
        if (text instanceof RaveRenderedText) {
            ((RaveRenderedText)text).setSource((RaveSourceText)sourceText);
        }
    }
    
    public static Element getTBodyElementForTableElement(Element tableElement) {
        if (tableElement instanceof RaveRenderedTableElement) {
            ((RaveRenderedTableElement)tableElement).getTbody();
        } else if (tableElement instanceof RaveSourceTableElement) {
            return ((RaveSourceTableElement)tableElement).getTbody();
        }
        return null;
    }
    
}
