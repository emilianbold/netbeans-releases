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
package org.netbeans.modules.visualweb.designer.markup;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.lang.reflect.Field;
import org.apache.xerces.dom.AttrImpl;
import org.apache.xerces.dom.AttrNSImpl;
import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.dom.DOMMessageFormatter;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.xni.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


// CVS note: This file used to be called XhtmlDocument (same directory)
// if you need to look at older CVS history

/**
 * This class provides a DocumentImpl for use with xerces, but which instead
 * of ElementImpl produces XhtmlElementImpl objects.
 *
 * @author Tor Norbye
 */
public abstract class AbstractRaveDocument extends DocumentImpl /*implements ParsingDocument*/ {
    /**
     *
     */
    private static final long serialVersionUID = 3979265845992305975L;

    /**
     * Non-NS elements should not be created; we don't want to mix
     * these with NS-elements since some document Edwin pointed me to
     * describes that this is a risky thing to do.
     * So we force the created element to be a NS element.
     *
     * @param tagName The name of the element type to instantiate. For
     * XML, this is case-sensitive. For HTML, the tagName parameter may
     * be provided in any case, but it must be mapped to the canonical
     * uppercase form by the DOM implementation.
     *
     * @throws DOMException(INVALID_NAME_ERR) if the tag name is not
     * acceptable.
     */
    public Element createElement(String tagName) throws DOMException {
        // TODO - should I look for a colon and if so, split out the end
        // as a local name?
        //return createElementNS(null, tagName);
        // XXX What namespace should we put here? Arguably, xhtml!
        // Since namespaceless tags are probably html tag elements
        // See http://www.w3.org/TR/REC-xml-names/#defaulting for more.
        return createElementNS(NamespaceContext.XML_URI, tagName);
    }

    /**
     * Introduced in DOM Level 2. <p>
     * Creates an element of the given qualified name and namespace URI.
     * If the given namespaceURI is null or an empty string and the
     * qualifiedName has a prefix that is "xml", the created element
     * is bound to the predefined namespace
     * "http://www.w3.org/XML/1998/namespace" [Namespaces].
     * @param namespaceURI The namespace URI of the element to
     *                     create.
     * @param qualifiedName The qualified name of the element type to
     *                      instantiate.
     * @return Element A new Element object with the following attributes:
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified
     * name contains an invalid character.
     * @throws DOMException NAMESPACE_ERR: Raised if the qualifiedName has a
     *                      prefix that is "xml" and the namespaceURI is
     *                      neither null nor an empty string nor
     *                      "http://www.w3.org/XML/1998/namespace", or
     *                      if the qualifiedName has a prefix different
     *                      from "xml" and the namespaceURI is null or an
     *                      empty string.
     * @since WD-DOM-Level-2-19990923
     */
    public Element createElementNS(String namespaceURI, String qualifiedName)
        throws DOMException {
        Element element = createSpecialElements(namespaceURI, qualifiedName);
        if (element != null) {
            return element;
        }
        if (namespaceURI == null && qualifiedName.indexOf(':') != -1) {
            // Happens for for example jsp:include.directive
            namespaceURI = NamespaceContext.XML_URI;
        }
//        return new RaveElementImpl(this, namespaceURI, qualifiedName);
        return createDefaultElement(this, namespaceURI, qualifiedName);
    }

    /**
     * NON-DOM: a factory method used by the Xerces DOM parser
     * to create an element.
     *
     * @param namespaceURI The namespace URI of the element to
     *                     create.
     * @param qualifiedName The qualified name of the element type to
     *                      instantiate.
     * @param localpart  The local name of the attribute to instantiate.
     *
     * @return Element A new Element object with the following attributes:
     * @exception DOMException INVALID_CHARACTER_ERR: Raised if the specified
     *                   name contains an invalid character.
     */
    public Element createElementNS(
        String namespaceURI,
        String qualifiedName,
        String localpart)
        throws DOMException {

        Element element = createSpecialElements(namespaceURI, qualifiedName);
        if (element != null) {
            return element;
        }
//        return new RaveElementImpl(this, namespaceURI, qualifiedName, localpart);
        return createDefaultElement(this, namespaceURI, qualifiedName, localpart);
    }

    /** Construct special elements for some tags */
    private Element createSpecialElements(String namespaceURI,
                                          String qualifiedName) {
        char firstChar = qualifiedName.charAt(0);
        switch (firstChar) {
        case 's':
            if (qualifiedName.equals(HtmlTag.STYLE.name)) {
//                StyleElement e = new StyleElement(this, namespaceURI, qualifiedName);
                Element e = createStyleElement(this, namespaceURI, qualifiedName);
//                XhtmlCssEngine engine = ((RaveDocument)e.getOwnerDocument()).getCssEngine();
//                if (engine != null) {
//                    engine.addTransientStyleSheetNode(e);
//                }
                CssProvider.getEngineService().addTransientStyleSheetNodeForDocument(this, e);
                return e;
            }
            break;
        case 't':
            if (qualifiedName.equals(HtmlTag.TABLE.name)) {
//                return new RaveTableElementImpl(this, namespaceURI, qualifiedName);
                return createTableElement(this, namespaceURI, qualifiedName);
            }
            break;
        case 'l':
            if (qualifiedName.equals(HtmlTag.LINK.name)) {
                // TODO - should we enforce that "rel" is "stylesheet"
                // (case insensitively) and "type" is "text/css"
                // and "href" is set?
//                return new StylesheetLinkElement(this, namespaceURI, qualifiedName);
                return createStylesheetLinkElement(this, namespaceURI, qualifiedName);
            }
        }
        return null;
    }    

    /**
     * Factory method; creates a Text node having this Document as its
     * OwnerDoc.
     *
     * @param data The initial contents of the Text.
     */
    public Text createTextNode(String data) {
//        return new RaveTextImpl(this, data);
        return createTextNode(this, data);
    }

//    public Text createJspxTextNode(String data) {
//        RaveText text = new RaveText(this, data);
//        text.setJspx(true);
//        return text;
//    }

    public Node importNode(Node source, boolean deep)
        throws DOMException {
        Node copied = super.importNode(source, deep);
        duplicateXhtmlInfo(source, copied, deep);
        return copied;
    }

    /** For a cloned tree, update the xhtml info. We couldn't just 
     * override clone because xerces' importNode implementation doesn't
     * use it.
     */
    private void duplicateXhtmlInfo(Node src, Node dst, boolean deep) {
//        if (src instanceof RaveElement) {
//            assert dst instanceof RaveElement;            
//            RaveElement srcElement = (RaveElement)src;
//            RaveElement dstElement = (RaveElement)dst;
//            ((RaveElementImpl)dstElement).copyFrom((RaveElementImpl)srcElement);
//            //dstElement.source = srcElement.getSourceNode();
//            dstElement.setSource(srcElement);
//        } else if (src instanceof RaveText) {
//            assert dst instanceof RaveText;            
//            RaveText srcText = (RaveText)src;
//            RaveText dstText = (RaveText)dst;
//            ((RaveTextImpl)dstText).copyFrom((RaveTextImpl)srcText);
//            dstText.setSource(srcText);
//        }
//        if (src instanceof RaveSourceElement) {
        if (src instanceof Element && src.getOwnerDocument() instanceof RaveSourceDocument) {
//            assert dst instanceof RaveRenderedElement; 
//            RaveSourceElement srcElement = (RaveSourceElement)src;
            Element srcElement = (Element)src;
            
            if (dst instanceof RaveElement) {
                ((RaveElement)dst).copyFrom((RaveElement)srcElement);
            }
            
//            if (dst instanceof RaveRenderedElement) {
            if (dst instanceof Element && dst.getOwnerDocument() instanceof RaveRenderedDocument) {
                //dstElement.source = srcElement.getSourceNode();
//                ((RaveRenderedElement)dst).linkToSourceElement(srcElement);
                MarkupServiceImpl.linkToSourceElement((Element)dst, srcElement);
            }
//        } else if (src instanceof RaveSourceText) {
        } else if (src instanceof Text && src.getOwnerDocument() instanceof RaveSourceDocument) {
//            assert dst instanceof RaveRenderedText;
//            RaveSourceText srcText = (RaveSourceText)src;
            Text srcText = (Text)src;
            
            if (dst instanceof RaveText) {
                ((RaveText)dst).copyFrom((RaveText)srcText);
            }
//            if (dst instanceof RaveRenderedText) {
            if (dst instanceof Text && dst.getOwnerDocument() instanceof RaveRenderedDocument) {
//                ((RaveRenderedText)dst).linkToSourceText(srcText);
                MarkupServiceImpl.linkToSourceText((Text)dst, srcText);
            }
//        } else if (src instanceof RaveRenderedElement) {
        } else if (src instanceof Element && src.getOwnerDocument() instanceof RaveRenderedDocument) {
            // XXX Prerendered elements - see doc jsp writer, and inline editing.
//            RaveSourceElement srcElement = ((RaveRenderedElement)src).getSourceElement();
            Element srcElement = MarkupServiceImpl.getSourceElement((Element)src);
            
//            if (srcElement != null) {
            if (srcElement instanceof RaveElement) {
                if (dst instanceof RaveElement) {
                    ((RaveElement)dst).copyFrom((RaveElement)srcElement);
                }

//                if (dst instanceof RaveRenderedElement) {
                if (dst instanceof Element && dst.getOwnerDocument() instanceof RaveRenderedDocument) {
                    //dstElement.source = srcElement.getSourceNode();
//                    ((RaveRenderedElement)dst).linkToSourceElement(srcElement);
                    MarkupServiceImpl.linkToSourceElement((Element)dst, srcElement);
                }
            }
        }
//        else if (src instanceof RaveRenderedText) {
//            RaveSourceText srcText = ((RaveRenderedTextImpl)src).sourceText;
//
//            if (srcText != null) {
//                if (dst instanceof AbstractRaveText) {
//                    ((AbstractRaveText)dst).copyFrom((AbstractRaveText)srcText);
//                }
//                if (dst instanceof RaveRenderedText) {
//                    ((RaveRenderedText)dst).setSource(srcText);
//                }
//            }
//        }
        
        if (deep) {
            NodeList srcChildren = src.getChildNodes();
            NodeList dstChildren = dst.getChildNodes();
            int len = srcChildren.getLength();
            assert dstChildren.getLength() == len;
            
            for (int i = 0; i < len; i++) {
                duplicateXhtmlInfo(srcChildren.item(i), dstChildren.item(i), deep);
            }
        }
    }

    // Moved to MarkupServiceImpl
//    /**
//     * Given two matching node trees where one represents a tree of
//     * nodes rendered from the other, update the source and render references
//     * in the nodes such that the "src" tree is marked as the source nodes
//     * for "dst".
//     */
//    public static void markRendered(Node src, Node dst) {
//        if (src instanceof RaveElement) {
//            assert dst instanceof RaveElement;            
//            RaveElement srcElement = (RaveElement)src;
//            RaveElement dstElement = (RaveElement)dst;
//            srcElement.source = null;
//            dstElement.source = srcElement;
//            dstElement.setSource(srcElement);
//        } else if (src instanceof RaveText) {
//            assert dst instanceof RaveText;            
//            RaveText srcText = (RaveText)src;
//            RaveText dstText = (RaveText)dst;
//            srcText.source = null;
//            dstText.source = srcText;
//            dstText.setSource(srcText);
//        }
//        NodeList srcChildren = src.getChildNodes();
//        NodeList dstChildren = dst.getChildNodes();
//        int len = srcChildren.getLength();
//        assert dstChildren.getLength() == len;
//        
//        for (int i = 0; i < len; i++) {
//            markRendered(srcChildren.item(i), dstChildren.item(i));
//        }
//    }

    // Moving the func into MarkupUnit#getUrlForDocument.
//    public void setUrl(URL url) {
//        this.url = url;
//    }
//
//    public URL getUrl() {
//        return url;
//    }

    // There is already a getXmlEncoding method see xerces/CoreDocumentImpl.
//    public String getEncoding() {
//        return encoding;
//    }


//    public XhtmlCssEngine getCssEngine() {
//        return engine;
//    }
//
//    public void setCssEngine(XhtmlCssEngine engine) {
//        this.engine = engine;
//    }
    
//    /** Clear document related errors. 
//     * @param delayed When set, don't actually clear the errors right now;
//     * it clears the errors next time another error is added. */
//    public static void clearErrors(boolean delayed) {
//        if (delayed) {
//            clearErrors = true;
//        } else {
//            OutputWriter out = getOutputWriter();
//            try {
//                out.reset();
//            }
//            catch (IOException ioe) {
//                // This is lame - our own output window shouldn't
//                // throw IO exceptions!
//                ErrorManager.getDefault().notify(ioe);
//            }
//        }
//    }
//    
//    private static boolean clearErrors = false;
//
//    private static OutputWriter getOutputWriter() {
//        InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(RaveDocument.class, "WindowTitle"), false);
//        OutputWriter out = io.getOut();
//        return out;
//    }
//    
//    /** 
//     * Display the given error message to the user. The optional listener argument
//     * (pass in null if not applicable) will make the line hyperlinked and the
//     * listener is invoked to process any user clicks.
//     * @param message The string to be displayed to the user
//     * @param listener null, or a listener to be notified when the user clicks
//     *   the linked message
//     */
//    public static void displayError(String message, OutputListener listener) {
//        OutputWriter out = getOutputWriter();
//        try {
//            if (clearErrors) {
//                out.reset();
//                clearErrors = false;
//            }
//            // Write the error message to the output tab:
//            out.println(message, listener);
//        }
//        catch (IOException ioe) {
//            // This is lame - our own output window shouldn't throw IO exceptions!
//            ErrorManager.getDefault().notify(ioe);
//        }
//    }
//
//    /**
//     * Cause the panel/window within which errors are displayed to come to the front if possible.
//     *
//     */
//    public static void selectErrors() {
//        InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(RaveDocument.class, "WindowTitle"), false);
//        io.select();
//    }
//    
//    /** Display an error message for the given source element. The error
//     * will be clickable.
//     */
//    public static void displayError(final org.openide.filesystems.FileObject fileObject,
//                                    final int lineNumber, 
//                                    String message) {
////        final XhtmlElement e = Util.getSource(element);
//        OutputListener listener = new OutputListener() {
//                public void outputLineSelected(OutputEvent ev) {
//                }
//                public void outputLineAction(OutputEvent ev) {
////                    Util.show(null, unit.getFileObject(), unit.getLine(e),
////                              0, true);
//                    // <markup_separation>
////                    Util.show(null, fileObject, lineNumber, 0, true);
//                    // ====
//                    MarkupUtilities.show(null, fileObject, lineNumber, 0, true);
//                    // </markup_separation>
//                }
//                public void outputLineCleared (OutputEvent ev) {
//                }
//            };
//        displayError(message, listener);
//    }
        
//    public void setRoot(RaveElement root) {
//        this.root = root;
//    }
//    
//    /** 
//     * Return the root element for the document, which may be rendered
//     * @todo Rename to getEffectiveRoot() ?
//     */
//    public RaveElement getRoot() {
//        if (root != null) {
//            return root;
//        } else {
//            return (RaveElement)getDocumentElement();
//        }
//    }
    
//    // ----------- Implements ParsingDocument ------------------------
//
//    public void appendParsedString(Node parent, String xhtml, MarkupDesignBean bean) {
//        // <markup_separation>
////        markup.appendParsedString(parent, xhtml, bean);
//        // ====
//        InSyncService.getProvider().appendParsedString(this, parent, xhtml, bean);
//        // </markup_separation>
//    }

    // <markup_separation> See MarkupUnit#raveDoc2markupUnit in insync
//    void setMarkupUnit(MarkupUnit markup) {
//        this.markup = markup;
//    }
//    
//    /** Return the associated markup unit - IF ANY */
//    public MarkupUnit getMarkup() {
//        return markup;
//    }
    // </markup_separation>

//    private URL url;
//    private XhtmlCssEngine engine;
    // <markup_separation>
//    private MarkupUnit markup;
    // </markup_separation>
//    private RaveElement root;

    protected abstract Element createDefaultElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName);

    protected abstract Element createDefaultElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName, String localpart);

    protected abstract Element createStyleElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName);

    protected abstract Element createTableElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName);

    protected abstract Element createStylesheetLinkElement(CoreDocumentImpl document, String namespaceURI, String qualifiedName);

    protected abstract Text createTextNode(CoreDocumentImpl document, String data);
    
    @Override
    public Attr createAttribute(String name) throws DOMException {
        if (errorChecking && !isXMLName(name,/*xml11Version*/isXml11Version())) {
            String msg =
                DOMMessageFormatter.formatMessage(
                    DOMMessageFormatter.DOM_DOMAIN,
                    "INVALID_CHARACTER_ERR",
                    null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
        return new Fix105085AttrImpl(this, name);
    }

    @Override
    public Attr createAttributeNS(String namespaceUri, String qualifiedName) throws DOMException {
        return new Fix105085AttrNSImpl(this, namespaceUri, qualifiedName);
    }

    @Override
    public Attr createAttributeNS(String namespaceUri, String qualifiedName, String localPart) throws DOMException {
        return new Fix105085AttrNSImpl(this, namespaceUri, qualifiedName, localPart);
    }

    private boolean isXml11Version() {
        try {
            Field field = CoreDocumentImpl.class.getDeclaredField("xml11Version"); // NOI18N
            field.setAccessible(true);
            return field.getBoolean(this);
        } catch (IllegalArgumentException ex) {
            log(ex);
        } catch (IllegalAccessException ex) {
            log(ex);
        } catch (NoSuchFieldException ex) {
            log(ex);
        } catch (SecurityException ex) {
            log(ex);
        }
        return false;
    }
    
    private static class Fix105085AttrImpl extends AttrImpl {
        public Fix105085AttrImpl(CoreDocumentImpl doc, String name) {
            super(doc, name);
        }
        
        @Override
        public void setValue(String value) {
            super.setValue(value);
            // XXX #105085 Not storing the cached node, and forcing it to recreate each time.
            textNode = null;
        }
    } // End of FixAttrImpl.
    
    private static class Fix105085AttrNSImpl extends AttrNSImpl {
        public Fix105085AttrNSImpl(CoreDocumentImpl doc, String namespaceUri, String qualifiedName) {
            super(doc, namespaceUri, qualifiedName);
        }
        
        public Fix105085AttrNSImpl(CoreDocumentImpl doc, String namespaceUri, String qualifiedName, String localPart) {
            super(doc, namespaceUri, qualifiedName, localPart);
        }
        
        @Override
        public void setValue(String value) {
            super.setValue(value);
            // XXX #105085 Not storing the cached node, and forcing it to recreate each time.
            textNode = null;
        }
    } // End of FixAttrNSImpl.
    
    private static void log(Exception ex) {
        Logger logger = getLogger();
        logger.log(Level.INFO, null, ex);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(CoreDocumentImpl.class.getName());
    }
}
