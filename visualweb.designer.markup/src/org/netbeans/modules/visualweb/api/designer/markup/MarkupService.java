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


package org.netbeans.modules.visualweb.api.designer.markup;


import org.netbeans.modules.visualweb.designer.markup.MarkupServiceImpl;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


/**
 * <code>MarkupService</code> implementation.
 *
 * @author Peter Zavadsky
 */
public final class  MarkupService {


    private MarkupService() {
    }


//    public static String expandHtmlEntities(String html) {
//        return expandHtmlEntities(html, true, null);
//    }
//
//    public static String expandHtmlEntities(String html, boolean warn) {
//        return expandHtmlEntities(html, warn, null);
//    }
//
//    public static String expandHtmlEntities(String html, boolean warn, Node node) {
//        return MarkupServiceImpl.expandHtmlEntities(html, warn, node);
//    }
//
//    public static int getUnexpandedOffset(String unexpanded, int expandedOffset) {
//        return MarkupServiceImpl.getUnexpandedOffset(unexpanded, expandedOffset);
//    }
//
//    public static int getExpandedOffset(String unexpanded, int unexpandedOffset) {
//        return MarkupServiceImpl.getExpandedOffset(unexpanded, unexpandedOffset);
//    }


//    // <utilities methods>
//    public static URL getCascadedXMLBase(Element elt) {
//        return MarkupServiceImpl.getCascadedXMLBase(elt);
//    }

//    // XXX From org.netbeans.modules.visualweb.insync.Util.
//    /**
//     * Given an element which may be in a rendered DocumentFragment, return the corresponding JSF
//     * element in the source.
//     */
//    public static Element getCorrespondingSourceElement(Element element) {
//        return MarkupServiceImpl.getCorrespondingSourceElement(element);
//    }


    // <markup_separation> copied from insync/Util
    // XXX This should be separate utility api, openide extension or what.
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
//        MarkupServiceImpl.show(filename, lineno, column, openFirst);
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
//    public static void show(FileObject fileObject, int lineno, int column, boolean openFirst) {
//        MarkupServiceImpl.show(fileObject, lineno, column, openFirst);
//    }

//    // <markup_separation> moved from insync/MarkupUnit
//    /** Convert the given URL to a path: decode spaces from %20's, etc.
//     * If the url does not begin with "file:" it will not do anything.
//     * @todo Find a better home for this method
//     */
//    public static String fromURL(String url) {
//        return MarkupServiceImpl.fromURL(url);
//    }
//    // </markup_separation>


//// <error_handling> Moved from RaveDocument.
//// XXX These methods are suspicoius, they deal with openide output window.
//    // and there may not be any knowing about it from this impls.
//    /** Clear document related errors. 
//     * @param delayed When set, don't actually clear the errors right now;
//     * it clears the errors next time another error is added. */
//    public static void clearErrors(boolean delayed) {
//        MarkupServiceImpl.clearErrors(delayed);
//    }
//    
////    /** 
////     * Display the given error message to the user. The optional listener argument
////     * (pass in null if not applicable) will make the line hyperlinked and the
////     * listener is invoked to process any user clicks.
////     * @param message The string to be displayed to the user
////     * @param listener null, or a listener to be notified when the user clicks
////     *   the linked message
////     */
////    public static void displayError(String message, OutputListener listener) {
////        MarkupServiceImpl.displayError(message, listener);
////    }
//
//    /**
//     * Cause the panel/window within which errors are displayed to come to the front if possible.
//     *
//     */
//    public static void selectErrors() {
//        MarkupServiceImpl.selectErrors();
//    }
//    
//    public static void displayError(String message) {
//        MarkupServiceImpl.displayError(message);
//    }
//    
//    public static void displayErrorForLocation(String message, Object location, int line, int column) {
//        MarkupServiceImpl.displayErrorForLocation(message, location, line, column);
//    }
//    
//    public static void displayErrorForFileObject(String message, FileObject fileObject, int line, int column) {
//        MarkupServiceImpl.displayErrorForFileObject(message, fileObject, line, column);
//    }
//    
//    /** Given a general location object provided from the CSS parser,
//     * compute the correct file name to use. */
//    public static String computeFilename(Object location) {
//        return MarkupServiceImpl.computeFilename(location);
//    }
//    /** Given a general location object provided from the CSS parser,
//     * compute the correct line number to use. */
//    public static int computeLineNumber(Object location, int line) {
//        return MarkupServiceImpl.computeLineNumber(location, line);
//    }
//// </error_handling>

//    // XXX Moved from DesignerService.
//    /**
//     * Return an InputStream for the given CSS URI, if the corresponding CSS
//     * file is open and edited. Otherwise return null.
//     *
//     * @param uri The URI to the CSS file. <b>MUST</b> be an absolute file url!
//     * @return An InputStream for the live edited CSS
//     */
//    public static InputStream getOpenCssStream(String uriString) {
//        return MarkupServiceImpl.getOpenCssStream(uriString);
//    }
//    // </utilities methods>
    
//    /**
//     * Generate the html string from the given node. This will return
//     * an empty string unless the Node is an Element or a DocumentFragment
//     * or a Document.
//     */
//    public static String getHtmlStream(Node node) {
//        return MarkupServiceImpl.getHtmlStream(node);
//    }
//
//    /** Generate the html string from the given element */
//    public static String getHtmlStream(Element element) {
//        return MarkupServiceImpl.getHtmlStream(element);
//    }
//
//    /** Generate the html string from the given document. Does formatting. */
//    public static String getHtmlStream(Document document) {
//        return MarkupServiceImpl.getHtmlStream(document);
//    }
//
//    /** Generate the html string from the given document fragment */
//    public static String getHtmlStream(DocumentFragment df) {
//        return MarkupServiceImpl.getHtmlStream(df);
//    }
    
    public static DocumentBuilder createRaveSourceDocumentBuilder(boolean useCss) throws ParserConfigurationException {
        return MarkupServiceImpl.createRaveSourceDocumentBuilder(useCss);
    }

    public static DocumentBuilder createRaveRenderedDocumentBuilder(boolean useCss) throws ParserConfigurationException {
        return MarkupServiceImpl.createRaveRenderedDocumentBuilder(useCss);
    }
    
    public static void markRendered(Node src, Node dst) {
        MarkupServiceImpl.markRendered(src, dst);
    }
    
    /** Mark all nodes in a node tree as rendered HTML nodes, and point back to the
     * source nodes in the JSP DOM.  For nodes that all point to the same source
     * node I want only the topmost nodes to point to the source.
     */
    public static void markRenderedNodes(Node node) {
        MarkupServiceImpl.markRenderedNodes(null, node);
    }
    
    /** Recursively mark all text nodes in the given node subtree as
     * being jspx nodes
     */
    public static void markJspxSource(Node node) {
        MarkupServiceImpl.markJspxSource(node);
    }
    
    public static void setInputEncodingForDocument(Document document, String inputEncoding) {
        MarkupServiceImpl.setInputEncodingForDocument(document, inputEncoding);
    }
    
    /** Get the text or comment text children of this element, which
     * should correspond to style rules.
     */
    public static String getStyleText(Element element) {
        return MarkupServiceImpl.getStyleText(element);
    }
    
    /**
     * Given an element which may be in a rendered DocumentFragment, return the corresponding JSF
     * element in the source.
     */
    public static Element getCorrespondingSourceElement(Element element) {
        return MarkupServiceImpl.getCorrespondingSourceElement(element);
    }
    
//    public static boolean isRenderedNode(Node node) {
//        return MarkupServiceImpl.isRenderedNode(node);
//    }
    
    public static Node getRenderedNodeForNode(Node node) {
        return MarkupServiceImpl.getRenderedNodeForNode(node);
    }
    
    public static Node getSourceNodeForNode(Node node) {
        return MarkupServiceImpl.getSourceNodeForNode(node);
    }
    
    /** XXX Get rid of this, it seems only RaveText uses it. */
    public static boolean isJspxNode(Node node) {
        return MarkupServiceImpl.isJspxNode(node);
    }
    
    public static void setJspxNode(Node node, boolean jspx) {
        MarkupServiceImpl.setJspxNode(node, jspx);
    }
    
    public static Element getRenderedElementForElement(Element element) {
        return MarkupServiceImpl.getRenderedElementForElement(element);
    }
    
    public static void setRenderedElementForElement(Element element, Element renderedElement) {
        MarkupServiceImpl.setRenderedElementForElement(element, renderedElement);
    }
    
    public static Element getSourceElementForElement(Element element) {
        return MarkupServiceImpl.getSourceElementForElement(element);
    }
    
    public static Text getRenderedTextForText(Text text) {
        return MarkupServiceImpl.getRenderedTextForText(text);
    }
    
    public static Text getSourceTextForText(Text text) {
        return MarkupServiceImpl.getSourceTextForText(text);
    }
    
    public static void setSourceTextForText(Text text, Text sourceText) {
        MarkupServiceImpl.setSourceTextForText(text, sourceText);
    }
    
    public static Element getTBodyElementForTableElement(Element tableElement) {
        return MarkupServiceImpl.getTBodyElementForTableElement(tableElement);
    }
}
