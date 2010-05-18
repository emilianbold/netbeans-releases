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
package org.netbeans.modules.visualweb.insync.markup;

import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.Util;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.events.MutationEventImpl;
import org.apache.xerces.util.EncodingMap;
import org.apache.xml.serialize.OutputFormat;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.SourceUnit;
import org.netbeans.modules.visualweb.insync.UndoManager;
import org.netbeans.modules.visualweb.insync.markup.JspxSerializer;
import org.openide.windows.OutputListener;
import org.w3c.dom.UserDataHandler;

/**
 *
 */
public class MarkupUnit extends SourceUnit implements org.w3c.dom.events.EventListener {

    public static final int ALLOW_XML  = 0x01;
    public static final int ALLOW_HTML = 0x02;

    int flags;
    /** Original parsed DOM document. */
    private Document sourceDocument;
    /** For JSP/JSF page -> rendered DOM document. */
    private Document renderedDocument;
    HashMap namespaces = new HashMap();  // namespace URI => prefix mapping
    Map<String, String> namespaceUriMap = new HashMap<String, String>();  // prefix ==> namespace URI mapping
    URI baseURI;
    URL base;
    private boolean supportCss;
    protected EventTarget registeredAsEventListenerOn;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct an MarkupUnit with a source doc
     */
    public MarkupUnit(FileObject fobj, int flags, boolean supportCss, UndoManager undoManager) {
        super(fobj, undoManager);
        this.flags = flags;
        this.supportCss = supportCss;
        //Trace.enableTraceCategory("insync.markup");
    }

    public void destroy() {
        namespaces.clear();
        namespaceUriMap.clear();
        base = null;
        baseURI = null;
//        XhtmlCssEngine engine = CssEngineServiceProvider.getDefault().getCssEngine(sourceDocument);
//        if (engine != null) {
//            engine.dispose();
//            engine = null;
//        }
//        CssProvider.getEngineService().removeCssEngineForDocument(sourceDocument);
        CssProvider.getEngineService().removeCssEngineForDocument(renderedDocument);
        synchronized (LOCK_RENDERED_DOCUMENT) {
            renderedDocument = null;
        }

        sourceDocument = null;
        unregisterDomListeners();
        super.destroy();
    }

    //------------------------------------------------------------------------ Document Node Helpers

    /**
     * Ensures that there is an element of a given type at the root of the document.
     * @return the found or created element
     */
    public static Element ensureRoot(Document document, String tag) {
        Element root = document.getDocumentElement();
        if (root == null) {
            root = document.createElement(tag);
            document.appendChild(root);
            Trace.trace("insync.markup", "MU ensure created root " + root);
        }
        else
            Trace.trace("insync.markup", "MU ensure found root " + root);
        return root;
    }

    /**
     *
     */
    public static Element ensureElement(Element parent, String tag, Element after) {
        Element elem = getFirstDescendantElement(parent, tag);
        if (elem == null) {
            Document document = parent.getOwnerDocument();
            elem = document.createElement(tag);

            Element before = after != null ? getNextSiblingElement(after)
                                           : getFirstChildElement(parent);
            if (before != null)
                parent.insertBefore(elem, before);
            else
                parent.appendChild(elem);
            Trace.trace("insync.markup", "MU scan created " + elem + " under " + parent +
                        " before " + before);
        }
        else
            Trace.trace("insync.markup", "MU scan found " + elem + " under " + elem.getParentNode());
        return elem;
    }

    /**
     *
     */
    public Element addElement(Element parent, Node before, String taglibUri, String tagPrefix,
                              String tag) {

        String prefix = taglibUri != null && taglibUri.length() > 0
                            ? getNamespacePrefix(taglibUri, tagPrefix) + ":"
                            : "";
        Element element = sourceDocument.createElementNS(taglibUri, prefix + tag);

        if (parent != null) {
            if (before != null)
                parent.insertBefore(element, before);
            else
                parent.appendChild(element);
        }
        else {
            sourceDocument.appendChild(element);
        }

        return element;
    }

    /**
     * Ensure that a given attribute of a given element exists. Create it with the default value if
     * it does not, leave it alone if it is. Handles special case of xmlns definition attributes.
     */
    public void ensureAttributeExists(Element element, String attr, String defValue) {
        if (element.getAttributeNode(attr) == null)
            ensureAttributeValue(element, attr, defValue);
    }

    /**
     * Ensure that a given attribute of a given element is a specific value. Update it if it is not,
     * leave it alone if it is. Handles special case of xmlns definition attributes.
     */
    public void ensureAttributeValue(Element element, String attr, String value) {
        if (!element.getAttribute(attr).equals(value)) {
            element.setAttribute(attr, value);
            if (element == sourceDocument.getDocumentElement() && attr.startsWith("xmlns:")) {
                String prefix = attr.substring(6);
                namespaces.put(value, prefix);
                namespaceUriMap.put(prefix, value);
            }
        }
    }

    /**
     * Get the first descendant element with a given tag
     */
    public static Element getFirstDescendantElement(Element parent, String tag) {
        NodeList childs = parent.getElementsByTagName(tag);
        if (childs.getLength() >= 1)
            return (Element)childs.item(0);
        return null;
    }

    /**
     * Return the child element with a given value for a given attribute
     */
    public static Element getDescendantElementByAttr(Element parent, String tag, String attrName,
                                                     String attrValue) {
        NodeList elems = parent.getElementsByTagName(tag);
        int elemcount = elems.getLength();
        for (int i = 0; i < elemcount; i++) {
            Element e = (Element)elems.item(i);
            if (e.getAttribute(attrName).equals(attrValue))
                return e;
        }
        return null;
    }

    /**
     * Return the child element with a given value for a given attribute
     */
    public static boolean isDescendent(Node parent, Node descendent) {
        while (descendent != null) {
            if (descendent == parent)
                return true;
            else
                descendent = descendent.getParentNode();
        }
        return false;
    }

    /**
     * Get the first child node that is an element
     */
    public static Element getFirstChildElement(Node parent) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element)
                return (Element)child;
        }
        return null;
    }

    /**
     * Get the next sibling node that is an element
     */
    public static Element getNextSiblingElement(Node elem) {
        for (Node sib = elem.getNextSibling(); sib != null; sib = sib.getNextSibling()) {
            if (sib instanceof Element)
                return (Element)sib;

        }
        return null;
    }

    /**
     * Get the text body of an element. Any markup is ignored.
     */
    public static String getElementText(Element elem) {
        if (elem == null)
            return null;
        StringBuffer sb = new StringBuffer();
        for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Text)
                sb.append(((Text)child).getData());
        }
        return sb.toString();
    }

    /**
     * Set the text body of an element. Any markup is lost.
     */
    public static void setElementText(Element elem, String text) {
        for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Text)
                elem.removeChild(child);
        }
        if (text != null)
            elem.appendChild(elem.getOwnerDocument().createTextNode(text));
    }

    public String findTaglibUri(String prefix) {
       return namespaceUriMap.get(prefix);   
    }


    //---------------------------------------------------------------------------------------- Input

    /**
     * Parse the input source into a DOM Document using the configured JAXP DOM implementation
     */
    private Document parseDom(org.xml.sax.InputSource is) {
        try {
            DocumentBuilder parser;
            parser = MarkupService.createRaveSourceDocumentBuilder(supportCss);

            parser.setErrorHandler(new ErrorHandler() {

                public void error(SAXParseException exception) {
                    addError(exception, true);
                }

                public void fatalError(SAXParseException exception) {
                    addError(exception, true);
                }

                public void warning(SAXParseException exception) {
                    addError(exception, false);
                }
            });

            // TODO: only set this to empty if a first parse fails?
            parser.setEntityResolver(new XhtmlEntityResolver());

            Trace.trace("insync.markup", "DOM Parsing: " + getName());
            Trace.flush();

            // Catch empty file induced parse exceptions ahead of time & just gen a new DOM.
//            if (!is.getCharacterStream().ready())
//                return parser.newDocument();

            // Invoke the DOM parser
            Document doc = parser.parse(is);
//            if (doc instanceof RaveDocument) {
                Document xdoc = doc;
                // <markup_separation>
//                xdoc.setMarkupUnit(this);
                // ====
                setMarkupUnitForDocument(xdoc, this);
                // </markup_separation>
                
                // Figure out the best source encoding & use it. Either the .nbattr or the ?xml
                String encoding = getEncoding();
//                String xencoding = xdoc.getEncoding();
                String xencoding = xdoc.getXmlEncoding();
                if (encoding != null && encoding.length() > 0) {
                    if (xencoding == null || xencoding.length() == 0) {
//                        ((RaveDocument)xdoc).setInputEncoding(encoding);
                        MarkupService.setInputEncodingForDocument(xdoc, encoding);
                    }
                }
                else {
                    if (xencoding != null && xencoding.length() > 0) {
                        encoding = xencoding;
                    }
                }
//            }

            return doc;
        }
        catch (ParserConfigurationException e) {
            Trace.trace("insync.markup", "DOM Parsing: " + getName());
            Trace.trace("insync.markup", e);
            setBusted();
        }
        catch (SAXException e) {
            Trace.trace("insync.markup", "DOM Parsing: " + getName());
            Trace.trace("insync.markup", e);
            setBusted();
        }
        catch (IOException e) {
            Trace.trace("insync.markup", "DOM Parsing: " + getName());
            Trace.trace("insync.markup", e);
            setBusted();
        }

        return null;
    }

    // TODO: only set this to empty if a first parse fails?
    public static class XhtmlEntityResolver implements EntityResolver {
        public org.xml.sax.InputSource resolveEntity(String pubid, String sysid) {
            return new org.xml.sax.InputSource(new ByteArrayInputStream(new byte[0]));
        }
    }

    /**
     * The parser always bails after the first error, so we don't have to keep a "list" of errors
     * (and chain them when they're on the same line) - we can just point directly to our one error.
     * Null when there is no error.
     *
     * @todo Is this true? Will it bail if it sees a warning (as opposed to an error) ? What
     * constitutes a warning from Xerces)
     */
    private ParserAnnotation error;

    /**
     * A new parse is about to begin - clear out existing errors
     */
    private void resetErrors() {
        error = null;
    }

    /**
     * Add a parser error to the hashmap for the given sax error
     */
    private void addError(SAXParseException exception, boolean isError) {
        String message = exception.getMessage();
        int line = exception.getLineNumber();
        // If file is empty then saved, we get -1
        if (line < 0)
            line = 1;
        int column = exception.getColumnNumber();
        if (column < 0)
            column = 0;
        error = new ParserAnnotation(message, fobj, line, column);
        if (isError)
            setBusted();
    }

    /**
     * Return the list of errors if this unit does not compile. If there are no errors it returns an
     * empty array - never null.
     *
     * @return An array of ParserAnnotations.
     */
    public ParserAnnotation[] getErrors() {
        if (error != null)
            return new ParserAnnotation[] { error };
        else
            return ParserAnnotation.EMPTY_ARRAY;
    }

    /**
     * Read the actual characters from the source document's content and into our DOM source document
     * object. Try an XML and/or an HTML parser depending on our flags setting.
     */
    protected void read(char[] buf, int len) {
        Trace.trace("insync.markup", "MU.read"); // \"" + new String(buf, 0, len) + "\"");
        //long start = System.currentTimeMillis();

        // cleanup listeners on old document
        Document oldDocument = sourceDocument;
        Document newDocument = null;

        InSyncServiceProvider.get().getRaveErrorHandler().clearErrors(true);
        resetErrors();

        // force reinitialization!!!
        base = null;
        baseURI = null;

        // Input structure for JAXP SAX and DOM
        org.xml.sax.InputSource is = new org.xml.sax.InputSource(new CharArrayReader(buf, 0, len));
        is.setSystemId(getName());

        // Try an XML parse first if it is allowed
        if ((flags & ALLOW_XML) != 0) {
            newDocument = parseDom(is);
            if (newDocument == null) {  // reset input source if parse aborted
                is = new org.xml.sax.InputSource(new CharArrayReader(buf, 0, len));
                is.setSystemId(getName());
            }
        }

        if (getState().isBusted())
            return;

        // Force pre-init of style sheets, such that errors in the <style> tag section etc. will 
        // get triggered. This doesn't initialize per-tag/local styles, only the <head> section 
        // style sheets and the head <style> tag.
        sourceDocument = newDocument;  // for initializeStyleSheet
        
        if (sourceDocument.getDocumentElement() != null) {
            MarkupService.markJspxSource(sourceDocument.getDocumentElement());
        }

        // Attempt to initialize stylesheets via Batik' CSS parser
        if (supportCss)
            syncEngine();// Initialize default stylesheet

        // style sheet parsing can cause errors too
        if (getState().isBusted()) {
            sourceDocument = oldDocument;
            return;
        }

        // register change listener & notify listeners regarding replaced doc
        unregisterDomListeners();

        sourceDocument = newDocument;

//        // XXX Reinit also the rendered doc?
//        synchronized (LOCK_RENDERED_DOCUMENT) {
//            renderedDocument = null;
//        }
        // XXX Is this the correct place (and thing to do)?
        setMarkupUnitForDocument(getRenderedDom(), this);

        if (sourceDocument != null) {
            registerDomListeners(sourceDocument);
            Element root = sourceDocument.getDocumentElement();
            NamedNodeMap attrs = root.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr)attrs.item(i);
                if (attr.getName().startsWith("xmlns:")) {
                    String prefix = attr.getName().substring(6);
                    namespaces.put(attr.getValue(), prefix);
                    namespaceUriMap.put(prefix, attr.getValue());
                }
            }
        }

        
        //long duration = System.currentTimeMillis() - start;
        //System.err.println("MU.read: XML parse time: " + duration + "ms for " + getName());

        if (oldDocument != null)
            fireDocumentReplaced(oldDocument);
    }

//    private XhtmlCssEngine engine;

// XXX Not used, removing.
//    private static boolean reuseEngine = System.getProperty("designer.reuseCssEngine") != null;
    private void syncEngine() {
//        Document doc = sourceDocument;
        // XXX It seems we need to parse styles for both documents (source and rendered).
        Document sourceDom = sourceDocument;
//        Document renderedDom = getRenderedDom();
        
//        doc.setUrl(getBase()); // needed by engine
//        setUrlForDocument(doc, getBase());
        setUrlForDocument(sourceDom, getBase());
//        setUrlForDocument(renderedDom, getBase());
        
//        if (reuseEngine && engine != null) {
//            engine.setDocument(doc);
//// <moving RaveDoc refs outside> engine is not interested in registering itself somewhere.
//            doc.setCssEngine(engine);
//// </moving RaveDoc refs outside>
//            return;
//        }
        // <markup_separation>
//        engine = XhtmlCssEngine.create(doc, this, doc.getUrl());
        // ====
//        engine = XhtmlCssEngine.create(doc, doc.getUrl());
//        XhtmlCssEngine engine = XhtmlCssEngine.create(doc, getUrlForDocument(doc));
//// <moved from engine impl> it doesn't (shoudn't) know about RaveDocument.
//        if (doc != null) {
////            doc.setCssEngine(engine);
//            CssEngineServiceProvider.getDefault().setCssEngine(doc, engine);
//        }
//// </moved from engine impl>
//        CssProvider.getEngineService().createCssEngineForDocument(doc, getUrlForDocument(doc));
        CssProvider.getEngineService().createCssEngineForDocument(sourceDom, getUrlForDocument(sourceDom));
//        CssProvider.getEngineService().createCssEngineForDocument(renderedDom, getUrlForDocument(renderedDom));
        // </markup_separation>
    }

//    /** Return CSS engine associated with this unit */
//    public XhtmlCssEngine getCssEngine() {
////        return engine;
//        return CssEngineServiceProvider.getDefault().getCssEngine(sourceDocument);
//    }

    //--------------------------------------------------------------------------------------- Output

    /**
     * Return a Xerces-compatible IANA encoding given a Java or IANA encoding
     * @param encoding Java or IANA encoding
     * @return Xerces-compatible IANA encoding
     */
    public static String getIanaEncoding(String encoding) {
        if (encoding != null) {
            // need to convert it to upper case:
            String upEncoding = encoding.toUpperCase(Locale.ENGLISH);
 
            // if it's a known IANA encoding, that's good enough for us
            if (EncodingMap.getIANA2JavaMapping(upEncoding) != null)
                return encoding;  

            // if it is a java encoding try mapping it to IANA
            String ianaEncoding = EncodingMap.getJava2IANAMapping(upEncoding);
            if (ianaEncoding != null)
                return ianaEncoding;

            // if it's a known java encoding alias, that needs to be normalized...
            /*
            if (Charset.isSupported(encoding)) {
                Charset charset = Charset.forName(encoding);
                if (charset != null) {
                    ianaEncoding = charset.name().toUpperCase(Locale.ENGLISH);
                    EncodingMap.putIANA2JavaMapping(ianaEncoding, upEncoding);
                    EncodingMap.putJava2IANAMapping(upEncoding, ianaEncoding);
                    
                    OutputFormat format = new OutputFormat(Method.XML, charset.name(), true);  // do-indent==true
                    format.setAllowJavaNames(true);              
                    EncodingInfo ei = null;
                    try {
                        ei = format.getEncodingInfo();
                    }
                    catch (UnsupportedEncodingException e) {
                        System.err.println(e);
                        return "UTF-8";
                    }

                    return charset.name();
                }
            }*/
        }
        return "UTF-8";  // Not an IANA or Xerces encoding, or was null for UTF8 default
    }

    /**
     * @return the current encoding in IANA form, or null for default (UTF8)
     */
    public String getEncoding() {
        Charset encodingCharset = FileEncodingQuery.getEncoding(fobj);
        return (encodingCharset == null ? null : encodingCharset.name());        
    }

    static final String BLANK = "                                                                                                                        ";

    public void indent(PrintWriter w, int level) {
        w.print(BLANK.substring(0, level * 4));
    }

    public void dump(org.w3c.dom.Node node, PrintWriter w, int level) {
        indent(w, level);
        if (node instanceof Element)
            w.println("E lname:" + node.getLocalName());
        else
            w.println(node.getClass().getName() + " lname:" + node.getLocalName());
        org.w3c.dom.Node child = node.getFirstChild();
        while (child != null) {
            dump(child, w, level + 1);
            child = child.getNextSibling();
        }
    }

    public void dumpTo(PrintWriter w) {
        if (sourceDocument != null) {
            dump(sourceDocument, w, 0);
        }
    }

    /** Get the output format to be used when serializing this buffer */
    public OutputFormat getOutputFormat() {
        String xencoding = getIanaEncoding(getEncoding());
        OutputFormat format = new OutputFormat(sourceDocument, xencoding, true);  // do-indent==true
        format.setLineWidth(160);
        format.setIndent(4);
        format.setAllowJavaNames(true);
        return format;
    }

    public void writeTo(Writer w) throws java.io.IOException {
        OutputFormat format = getOutputFormat();
        JspxSerializer serializer = new JspxSerializer(w, format);
        serializer.serialize(sourceDocument);
    }

    //------------------------------------------------------------------------------------ Accessors

    public Document getSourceDom() {
        return sourceDocument;
    }

    /** Lock for sync creating of rendered document. */
    private final Object LOCK_RENDERED_DOCUMENT = new Object();
            
    public Document getRenderedDom() {
        boolean created = false;
        synchronized (LOCK_RENDERED_DOCUMENT) {
            if (renderedDocument == null) {
                renderedDocument = createEmptyRenderedDocument();
                created = true;
            }
        }
        if (created) {
            initRenderedDocument();
        }
        
        return renderedDocument;
    }
    
    private void initRenderedDocument() {
        Document renderedDom;
        synchronized (LOCK_RENDERED_DOCUMENT) {
            renderedDom = renderedDocument;
        }
        setUrlForDocument(renderedDom, getBase());
        CssProvider.getEngineService().createCssEngineForDocument(renderedDom, getUrlForDocument(renderedDom));
    }
    
    private static Document createEmptyRenderedDocument() {
        try {
            org.xml.sax.InputSource is = new org.xml.sax.InputSource(new StringReader("<html></html>")); // TEMP
            DocumentBuilder parser = MarkupService.createRaveRenderedDocumentBuilder(true);
            Document doc = parser.parse(is);
            return doc;
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        } catch (org.xml.sax.SAXException ex) {
            ex.printStackTrace();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the actual prefix in this document for a given namespace. Registers the default if it
     * does not yet exist.
     *
     * @param namespaceUri
     * @param suggPrefix
     * @return the existing or newly registered prefix
     */
    public String getNamespacePrefix(String namespaceUri, String suggPrefix) {
        String prefix = (String)namespaces.get(namespaceUri);
        if (prefix != null)
            return prefix;

        Collection prefixes = namespaces.values();

        // try out the suggested prefix first if one was given
        if (suggPrefix != null && !prefixes.contains(suggPrefix)) {
            prefix = suggPrefix;
        }
        else {
            // first, try to make up a prefix from the last part of the URI
            int slash = namespaceUri.lastIndexOf('/');
            if (slash >= 0) {
                for (int l = 1; slash+1+l < namespaceUri.length(); l++) {
                    String p = namespaceUri.substring(slash+1, slash+1+l);
                    if (!prefixes.contains(p)) {
                        prefix = p;
                        break;
                    }
                }
            }
//            if (prefix == null) {
//                // try something else...
//            }
        }

        if (prefix != null) {
            Element root = sourceDocument.getDocumentElement();
            ensureAttributeValue(root, "xmlns:" + prefix, namespaceUri);
        }

        return prefix;
    }

    public URI getBaseURI() {
        if (baseURI == null) {
            if (base == null) {
                base = getBase();
            } else {
                try {
                    baseURI = new URI(base.toExternalForm());
                } catch (URISyntaxException use) {
                    baseURI = null;
                }
            }
        }
        return baseURI;
    }

    /**
     * Returns the location to resolve relative URLs against. By default this will be the document's
     * URL if the document was loaded from a URL. If a base tag is found and can be parsed, it will
     * be used as the base location.
     *
     * @return the base location
     */
    public URL getBase() {
        if (base == null) {
            // First see if we have a <base> tag within the <head>

            // TODO - gather ALL <base> elements within the head
            // and process them
            Element root = sourceDocument.getDocumentElement();
            Element html = findHtmlTag(root);
            if (html != null) {
                Element head = Util.findChild("head", html, false);
                if (head != null) {
                    Element baseElement = Util.findChild("base", head, false);
                    if (baseElement != null) {
                        String href = baseElement.getAttribute("href");
                        if (href != null && href.length() > 0) {
                            try {
                                try {
                                    baseURI = new URI(href);
                                    //base = new URL(href);
                                    base = baseURI.toURL();
                                } catch (URISyntaxException ex) {
                                    base = new URL(href);
                                }
                            } catch (MalformedURLException mue) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
                            }
                            if (base != null) {
                                return base;
                            }
                        }
                    }
                }
            }

            // No <base>, so get the URL of the document file itself
            // and use that to resolve relative URLs.

            // Compute base
            /* These URLs don't seem to work - they become
               nbfs://<whatever> which Swing is not handling. So we've
               gotta do the file:// thing. This means for now, opening
               a web form inside a jar file with relative URLs won't work.
            try {
               base = dobj.getPrimaryFile().getParent().getURL();
            }
            catch (org.openide.filesystems.FileStateInvalidException e) {
               ErrorManager.getDefault().notify(e);
            }
            */
            if (fobj == null) // Testsuite
                return null;

            try {
                FileObject fp = fobj.getParent();
                baseURI = FileUtil.toFile(fp).toURI();
                base = baseURI.toURL();
            }
            catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return base;
    }

    /** Convert the given path to a URL: encode spaces to %20's, use
     * only forward slashes, etc.
     * @todo Find a better home for this method
     */
    public static String toURL(String path) {
        // The URL encoder doesn't seem to do this - surprising
        path = path.replace('\\','/');
        // This was also surprising - it makes spaces into +'es instead
        path = path.replaceAll(" ", "%20");
        StringWriter sw = new StringWriter();
        try {
            // WriteURL signature changed in JSF1.2-02-b04
            com.sun.faces.util.HtmlUtils.writeURL(sw, path, null, null);
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return path;
        }
        return sw.toString();
    }

    // <markup_separation> moved to designer/markup/XhtmlCssEngine
//    /** Convert the given URL to a path: decode spaces from %20's, etc.
//     * If the url does not begin with "file:" it will not do anything.
//     * @todo Find a better home for this method
//     */
//    public static String fromURL(String url) {
//        if (url.startsWith("file:")) { // NOI18N
//            int n = url.length();
//            StringBuffer sb = new StringBuffer(n);
//            for (int i = 5; i < n; i++) {
//                char c = url.charAt(i);
//                // TODO -- any File.separatorChar manipulation perhaps?
//                if (c == '%' && i < n-3) {
//                    char d1 = url.charAt(i+1);
//                    char d2 = url.charAt(i+2);
//                    if (Character.isDigit(d1) && Character.isDigit(d2)) {
//                        String numString = ""+d1+d2;
//                        try {
//                            int num = Integer.parseInt(numString, 16);
//                            if (num >= 0 && num <= 255) {
//                                sb.append((char)num);
//                                i += 2;
//                                continue;
//                            }
//                        } catch (NumberFormatException nex) {
//                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nex);
//                        }
//                    }
//                    sb.append(c);
//                } else {
//                    sb.append(c);
//                }
//            }
//            return sb.toString();
//        }
//        return url;
//    }
    // </markup_separation>


    /**
     * Return true iff this web page should not be converted to a web form if it's missing a backing
     * file. Web forms can be marked in this way if the user answers no to the conversion dialog.
     */
    public boolean isHtmlOnly() {
        if (sourceDocument == null) {
            return false;
        }
        Element root = sourceDocument.getDocumentElement();
        Element html = findHtmlTag(root);
        if (html == null) { // We're hosed!!! This shouldn't happen
            Thread.dumpStack();
            return false;
        }
        Element head = Util.findChild("head", html, true);
        if (head == null) {
            return false;
        }
        NodeList list = head.getChildNodes();
        int len = list.getLength();
        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = list.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element meta = (Element)child;
                if ("meta".equals(meta.getTagName())) {
                    if (!"creator.webform".equalsIgnoreCase(meta.getAttribute("name"))) {
                        continue;
                    }
                    String isForm = meta.getAttribute("content");
                    return isForm != null && isForm.equals("no");
                }
            }
        }
        return false;
    }

    /**
     * Mark this web form in such a way that in the future, isHtmlOnly will return false. In other
     * words, if this web page should not be converted to a web form, call this method such that the
     * conversion dialog is not shown next time this markup document is opened. This method should
     * NOT be called on a file which already returns isHtmlOnly (it may add additional meta tags,
     * not look for existing ones first).
     */
    public void markHtmlOnly() { // not called setHtmlOnly since we only
                                 // support setting the property to
                                 // true, not false
        assert !isHtmlOnly();

        Element root = sourceDocument.getDocumentElement();
        Element html = findHtmlTag(root);
        if (html == null) { // We're hosed!!! This shouldn't happen
            Thread.dumpStack();
            return;
        }
        Element head = ensureElement(html, "head", null);
        Element meta = ensureElement(head, "meta", null);
        meta.setAttribute("content", "no");
        meta.setAttribute("name", "creator.webform");
    }

    /**
     * Locate the &lt;html&gt; tag. In a normal xhtml/html document, it's the same as the root tag
     * for the DOM, but in our JSF files, it might be nested within &lt;jsp:root&gt;,
     * &lt;f:view&gt;, etc.
     *
     * @param root  The root tag
     * @todo Just pass in the Document node instead?
     * @return The html tag Element
     */
    public Element findHtmlTag(Node root) {
        if (root.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element)root;
            if ("html".equals(element.getTagName()))  // Don't allow "HTML"
                return element;
        }
        NodeList list = root.getChildNodes();
        int len = list.getLength();
        for (int i = 0; i < len; i++) {
            Node child = list.item(i);
            Element match = findHtmlTag(child);
            if (match != null)
                return match;
        }
        return null;
    }

    //---------------------------------------------------------------------------------- Public CSS2

    /** Add a parser error to the hashmap for the given CSS/SAC error */
    /*
    private void addError(CSSParseException exception, boolean isError) {
        String message = exception.getMessage();
        int line = exception.getLineNumber();
        int column = exception.getColumnNumber();
        //String uri = exception.getURI();

        // XXX What is the file here? Gotta track that somehow, so
        // I can either point to a particular stylesheet file, or
        // a local style attribute, or a <style> tag section...

        error = new ParserAnnotation(message, fobj, line, column);

        if (isError)
            setInvalid();
    }
    */

    //----------------------------------------------------------------------------------- Public DOM

    /**
     * Create a new document, from an xhtml fragment source string
     */
    private Document createDocument(Document ownerDom, final String source) {
        try {
            // just a plain ol' XML DOM Document, use the regular XML DOM parser
            // wrap source with a fake root since it may contain more than one element
            // XXX Note: designer knows about the fake-root element
            // name. Don't change arbitrarily!
            String fullSource = "<!DOCTYPE html \nPUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"\"><fake-root>" + source + "</fake-root>";
            org.xml.sax.InputSource is =
                new org.xml.sax.InputSource(new StringReader(fullSource));
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();            
            try {
            	Thread.currentThread().setContextClassLoader((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class));
                DocumentBuilder parser = MarkupService.createRaveSourceDocumentBuilder(supportCss);
                parser.setEntityResolver(new XhtmlEntityResolver());
    
                parser.setErrorHandler(new ErrorHandler() {
                        // ---- Implements org.xml.sax.ErrorHandler ----------
    
                        public void  error(SAXParseException exception) {
                            fragmentError(exception, true, source);
                        }
    
                        public void fatalError(SAXParseException exception) {
                            fragmentError(exception, true, source);
                        }
    
                        public void warning(SAXParseException exception) {
                            fragmentError(exception, false, source);
                        }
                    });
    
                Document fragdoc = parser.parse(is);
//            if (fragdoc instanceof RaveDocument) {
                    // <markup_separation>
//                ((RaveDocument)fragdoc).setMarkupUnit(this);
                    // ====
                    setMarkupUnitForDocument(fragdoc, this);
                    // </markup_separation>
//                ((RaveDocument)fragdoc).setCssEngine(((RaveDocument)sourceDocument).getCssEngine());
//                CssProvider.getEngineService().reuseCssEngineForDocument(fragdoc, sourceDocument);
                    // XXX Not fragdoc but its rendered doc?!
                    CssProvider.getEngineService().reuseCssEngineForDocument(fragdoc, ownerDom);
//            }
    
                return fragdoc;
            } finally {
            	Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
        catch (java.io.IOException e) {
            // should not happen reading from a string!
//            Trace.trace("insync.markup", "Error in createDocumentFragment");
//            Trace.trace("insync.markup", e);
            e.printStackTrace();
        }
        catch (org.xml.sax.SAXException e) {
//            Trace.trace("insync.markup", "Error in createDocumentFragment");
//            Trace.trace("insync.markup", e);
            e.printStackTrace();
        }
        catch (javax.xml.parsers.ParserConfigurationException e) {
//            Trace.trace("insync.markup", "Error in createDocumentFragment");
//            Trace.trace("insync.markup", e);
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * Create a new source document fragment, from a source string, that will live in the current
//     * source document. The fragment needs to be added as a child to a specific node before it will really
//     * exist in the source document tree.
//     */
//    private DocumentFragment createDocumentFragment(Document ownerDom, String source) {
//        Document fragdoc = createDocument(ownerDom, source);
//        if (fragdoc != null) {
////            DocumentFragment fragment = sourceDocument.createDocumentFragment();
//            DocumentFragment fragment = ownerDom.createDocumentFragment();
//                    
//            NodeList elems = fragdoc.getDocumentElement().getChildNodes();
//            // get the elems from our fake root
//            // import them into this sourceDocument, and add the copies to the fragment
//            int elemCount = elems.getLength();
//            for (int i = 0; i < elemCount; i++) {
////                Node e = sourceDocument.importNode(elems.item(i), true);
//                Node e = ownerDom.importNode(elems.item(i), true);
//                
//                fragment.appendChild(e);
//            }
//            return fragment;
//        }
//        return null;
//    }


    private void fragmentError(SAXParseException exception, boolean isError, String source) {
        String message = exception.getMessage();
        int line = exception.getLineNumber();
        int column = exception.getColumnNumber();
        //String publicId = exception.getPublicId();
        //String systemId = exception.getSystemId();
        if (source.length() > 80) {
            source = source.substring(0, 80) + "...";
        }

        // TODO: i18n.
        String error = line + ":" + column + ": " + message + ": " + source;

        InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(MarkupUnit.class, "WindowTitle"), false);
        OutputWriter out = io.getOut();
        try {
            out.reset();
            // XXX #102988 There can't be null listener now.
            out.println(error,new OutputListener() {
                public void outputLineSelected(OutputEvent evt) {
                    // No op.
                }
                public void outputLineAction(OutputEvent evt) {
                    // No op.
                }
                public void outputLineCleared(OutputEvent evt) {
                    // No op.
                }
            });
        }
        catch (IOException ex) {
            // This is lame - our own output window shouldn't throw
            // IO exceptions!
            ErrorManager.getDefault().notify(ex);
        }
    }

//    private Document createEmptyDocument() {
//        Document doc = createEmptyDocument(supportCss);
////        if (doc instanceof RaveDocument)
////            // <markup_separation>
//////            ((RaveDocument)doc).setMarkupUnit(this);
////            // ====
////        {
//            setMarkupUnitForDocument(doc, this);
////        }
//            // </markup_separation>
//        return doc;
//    }
//
//    private static Document createEmptyDocument(boolean supportCss) {
//        try {
//            org.xml.sax.InputSource is =
//                new org.xml.sax.InputSource(new StringReader("<html><body><p/></body></html>"));
//            DocumentBuilder parser = MarkupService.createRaveSourceDocumentBuilder(supportCss);
//            Document doc = parser.parse(is);
//            return doc;
//        }
//        catch (java.io.IOException e) {
//            // should not happen reading from a string!
//            Trace.trace("insync.markup", "Error in createEmptyDocument");
//            Trace.trace("insync.markup", e);
//        }
//        catch (org.xml.sax.SAXException e) {
//            Trace.trace("insync.markup", "Error in createEmptyDocument");
//            Trace.trace("insync.markup", e);
//        }
//        catch (javax.xml.parsers.ParserConfigurationException e) {
//            Trace.trace("insync.markup", "Error in createEmptyDocument");
//            Trace.trace("insync.markup", e);
//        }
//        return null;
//    }

    public void appendParsedString(Node parent, String xhtml, MarkupDesignBean bean) {
//        assert parent.getOwnerDocument() == sourceDocument;
        Document ownerDom = parent.getOwnerDocument();

        if (xhtml.startsWith("<?") || xhtml.startsWith("<!DOCTYPE")) {
            // Skip it -- we can't parse this.
            // Braveheart likes to throw in (literally)
            // <?xml version="1.0" encoding="UTF-8"?>
            // for example
            return;
        }
        Document fragdoc = createDocument(ownerDom, xhtml);
        // NOTE - we don't mark this source jspx as is done in read();
        // this source is part of rendered html from a component, and
        // has already had entities expanded.
        if (fragdoc == null) {
            // Parse error - for now just insert the xhtml directly as
            // text - e.g. the user may see something like "<b>Hello</b>"
            // instead of Hello in a bold font
//            Node textNode = sourceDocument.createTextNode(xhtml);
            Node textNode = ownerDom.createTextNode(xhtml);
            parent.appendChild(textNode);
        } else {
            NodeList elems = fragdoc.getDocumentElement().getChildNodes();
            // get the elems from our fake root
            // import them into this source document, and add the copies to the fragment

            // Find Element containing the import location
            Element parentElement = null;
            Node curr = parent;
            while (curr != null) {
                if (curr instanceof Element) {
                    parentElement = (Element)curr;
                    break;
                }
                curr = curr.getParentNode();
            }

            int elemCount = elems.getLength();
            for (int i = 0; i < elemCount; i++) {
                // XXX Can't I just bang them into my other documenet fragment?
//                Node e = sourceDocument.importNode(elems.item(i), true);
                Node e = ownerDom.importNode(elems.item(i), true);

                // The xhtml fragment imported should participate in
                // the CSS context at the imported location
                if (parentElement != null && e instanceof Element) {
//                    RaveElement.setStyleParent((Element)e, parentElement);
                    CssProvider.getEngineService().setStyleParentForElement((Element)e, parentElement);
                    // I have to set it on the Document too, because import node
                    // will set up source pointers back to the derived document even
                    // though it's not technically the jspx source (and the source
                    // pointers are followed by the designer when computing text)
                    // Consider fixing this later.
//                    RaveElement.setStyleParent((Element)elems.item(i), parentElement);
                    CssProvider.getEngineService().setStyleParentForElement((Element)elems.item(i), parentElement);
                }
                parent.appendChild(e);
                setBean(e, bean);
            }
        }
    }
    
    /** Set the DesignBean references recursively on the given node tree */
    private void setBean(Node node, MarkupDesignBean bean) {
//        if (node instanceof RaveElement) {
//            ((RaveElement)node).setDesignBean(bean);
//        }
        if (node instanceof Element) {
            setMarkupDesignBeanForElement((Element)node, bean);
        }
        
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            setBean(nl.item(i), bean);
        }
    }
    

    // XXX #123995 The apache impl of Document userData leaks, so we
    // need to avoid using it for now. Returning to previous, but also
    // using weak refs (because there is a link from MarkupDesignBean to Element).
    /** Map <code>Element</code> to <code>MarkupDesignBean</code>. */
    private static final Map element2markupDesignBean = new WeakHashMap(200);
//    private static final String KEY_MARKUP_DESIGN_BEAN = "vwpMarkupDesignBean"; // NOI18N
    
    public static void setMarkupDesignBeanForElement(Element element, MarkupDesignBean markupDesignBean) {
        synchronized (element2markupDesignBean) {
            element2markupDesignBean.put(element, new WeakReference(markupDesignBean));
        }
//        if (element == null) {
//            // XXX Log problem?
//            return;
//        }
//        element.setUserData(KEY_MARKUP_DESIGN_BEAN, markupDesignBean, MarkupDesignBeanDataHandler.getDefault());
    }
    
    public static MarkupDesignBean getMarkupDesignBeanForElement(Element element) {
        synchronized (element2markupDesignBean) {
            WeakReference ret = (WeakReference)element2markupDesignBean.get(element);
            return ret == null ? null : (MarkupDesignBean)ret.get();
        }
//        if (element == null) {
//            // XXX Log problem?
//            return null;
//        }
//        return (MarkupDesignBean)element.getUserData(KEY_MARKUP_DESIGN_BEAN);
    }
    
//    private static class MarkupDesignBeanDataHandler implements UserDataHandler {
//        private static final MarkupDesignBeanDataHandler INSTANCE = new MarkupDesignBeanDataHandler();
//        public static MarkupDesignBeanDataHandler getDefault() {
//            return INSTANCE;
//        }
//        public void handle(short operation, String key, Object data, Node src, Node dst) {
//            // No op.
//            // TODO Provide the copying (after remove the copying in the AbstractRaveElement).
//        }
//    } // End of MarkupDesignBeanUserData.

    //------------------------------------------------------------------------ Change Event Handling

// <refactoring> why copy when one depends on it directly
//    // This section of constants copied from Xerces 2.5.0's MutationEventImpl class
//    // NON-DOM CONSTANTS: Storage efficiency, avoid risk of typos.
//    public static final String DOM_SUBTREE_MODIFIED = "DOMSubtreeModified";
//    public static final String DOM_NODE_INSERTED = "DOMNodeInserted";
//    public static final String DOM_NODE_REMOVED = "DOMNodeRemoved";
//    public static final String DOM_NODE_REMOVED_FROM_DOCUMENT = "DOMNodeRemovedFromDocument";
//    public static final String DOM_NODE_INSERTED_INTO_DOCUMENT = "DOMNodeInsertedIntoDocument";
//    public static final String DOM_ATTR_MODIFIED = "DOMAttrModified";
//    public static final String DOM_CHARACTER_DATA_MODIFIED = "DOMCharacterDataModified";
//
//    public static final String DOM_DOCUMENT_REPLACED = "DOMDocumentReplaced";
// ====
    public static final String DOM_SUBTREE_MODIFIED = MutationEventImpl.DOM_SUBTREE_MODIFIED;
    public static final String DOM_NODE_INSERTED = MutationEventImpl.DOM_NODE_INSERTED;
    public static final String DOM_NODE_REMOVED = MutationEventImpl.DOM_NODE_REMOVED;
    public static final String DOM_NODE_REMOVED_FROM_DOCUMENT = MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT;
    public static final String DOM_NODE_INSERTED_INTO_DOCUMENT = MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT;
    public static final String DOM_ATTR_MODIFIED = MutationEventImpl.DOM_ATTR_MODIFIED;
    public static final String DOM_CHARACTER_DATA_MODIFIED = MutationEventImpl.DOM_CHARACTER_DATA_MODIFIED;

    /** Insync extention of MutationEventImpl type (see insyc/MarkupUnit and xerces/MuationEventImpl). */
    public static final String DOM_DOCUMENT_REPLACED = "DOMDocumentReplaced"; // NOI18N
// </refactoring>

    private void registerDomListeners(Document document) {
        Trace.trace("insync.markup", "MU.registerDomListeners " + document);
        EventTarget target = (EventTarget)document;
        target.addEventListener(DOM_ATTR_MODIFIED, this, false);
        target.addEventListener(DOM_SUBTREE_MODIFIED, this, false);
        registeredAsEventListenerOn = target;
        /*
        target.addEventListener(DOM_NODE_INSERTED, this, false);
        target.addEventListener(DOM_NODE_INSERTED_INTO_DOCUMENT, this, false);
        target.addEventListener(DOM_NODE_REMOVED, this, false);
        target.addEventListener(DOM_NODE_REMOVED_FROM_DOCUMENT, this, false);
        target.addEventListener(DOM_CHARACTER_DATA_MODIFIED, this, false);
        */
    }

    private void fireDocumentReplaced(Document document) {
        Trace.trace("insync.markup", "MU.fireDomListeners " + document);
        DocumentEvent doc = (DocumentEvent)document;
        MutationEvent me = (MutationEvent)doc.createEvent("MutationEvents");
        me.initMutationEvent(DOM_DOCUMENT_REPLACED, false, false,
                             document, null, null, null,
                             MutationEvent.REMOVAL);

        EventTarget target = (EventTarget)document;
        target.dispatchEvent(me);
    }

    protected void unregisterDomListeners() {
        if (registeredAsEventListenerOn == null)
            return;
        registeredAsEventListenerOn.removeEventListener(DOM_ATTR_MODIFIED, this, false);
        registeredAsEventListenerOn.removeEventListener(DOM_SUBTREE_MODIFIED, this, false);
        registeredAsEventListenerOn = null;
    }
    
    public void handleEvent(org.w3c.dom.events.Event e) {
        setModelDirty();
    }


    //------------------------------------------------------------------------ Compute Buffer Positions

    /**
     * Return the buffer start position of a given node. Not currently very efficient or accurate
     * @return the char offset of the node, -1 if not found
     */
    public int computeLine(Element element) {
        LineCountingWriter w = new LineCountingWriter();
        OutputFormat format = getOutputFormat();
        TargetXMLSerializer serializer = new TargetXMLSerializer(w, format, element);
        int pos = 0;
        try {
            serializer.serialize(sourceDocument);
        }
        catch (java.io.EOFException e) {
            // normal stop...
            pos = w.pos;
            if (!serializer.isAdjusted()) {
                pos++;
            }
        }
        catch (java.io.IOException e) {
            assert Trace.trace("insync.java", "Error scanning for node position: " + e);
         }

        // It always includes an extra newline
        return pos-1;
    }

    /**
     * Return the buffer start offset of a given node. Not currently very efficient or accurate
     * @return the char offset of the node, -1 if not found
     */
    public int getOffset(Element element) {
        LineCountingWriter w = new LineCountingWriter();
        OutputFormat format = getOutputFormat();
        TargetXMLSerializer serializer = new TargetXMLSerializer(w, format, element);
        int offset = -1;
        try {
            serializer.serialize(sourceDocument);
        }
        catch (java.io.EOFException e) {
            // normal stop...
            offset = w.offset;
        }
        catch (java.io.IOException e) {
            assert Trace.trace("insync.java", "Error scanning for node position: " + e);
         }

        return offset;
    }

    private class TargetXMLSerializer extends JspxSerializer {
        private Element target;
        private boolean adjusted = true;

        private TargetXMLSerializer(java.io.Writer writer, 
                                    OutputFormat format, Element target) {
            super(writer, format);
            this.target = target;
        }
        
        boolean isAdjusted() {
            return adjusted;
        }

	public void serializeElement(Element elem) throws IOException, EOFException{
            if (elem == target) {
                if (getElementState().empty) {
                    adjusted = false;
                }
                //_printer.breakLine();
                _printer.flush();
                throw new EOFException();
            }
            super.serializeElement(elem);
        }
    }
    
    public class LineCountingWriter extends Writer {
        public int pos;
        public int offset;
        
        public void close() {}
        public void flush() {
        }
        public void write(char[] buf) {
            offset += buf.length;
            for (int i = 0; i < buf.length; i++) {
                if (buf[i] == '\n') {
                    pos++;
                }
            }
        }
        public void write(char[] buf, int off, int len) {
            offset += len;
            for (int i = 0; i < len; i++) {
                if (buf[off+i] == '\n') {
                    pos++;
                }
            }
        }
        public void write(int c) {
            offset++;
            if (c == '\n') {
                pos++;
            }
        }
        public void write(String str) {
            offset += str.length();
            for (int i = 0, n = str.length(); i < n; i++) {
                if (str.charAt(i) == '\n') {
                    pos++;
                }
            }
        }
        public void write(String str, int off, int len) {
            offset += len;
            for (int i = 0; i < len; i++) {
                if (str.charAt(off+i) == '\n') {
                    pos++;
                }
            }
        }
    }    
    
    
//    // <markup_separation> Maintain the map between doc and unit, 
//    // and css engine and unit.
//    // Do not pass it directly into the doc or engine, it is not needed there.
//    /** Map between <code>org.w3c.dom.Document</code> and <code>MarkupUnit</code> */
//    private final static Map doc2markupUnit = new WeakHashMap();
//    private static final String KEY_MARKUP_UNIT = "vwpMarkupUnit"; // NOI18N
  
    private static final Map<Document, MarkupUnit> doc2markupUnit = new WeakHashMap<Document, MarkupUnit>();
    
    private static void setMarkupUnitForDocument(Document doc, MarkupUnit markupUnit) {
//        synchronized (doc2markupUnit) {
//            doc2markupUnit.put(doc, markupUnit);
//        }
        if (doc == null) {
            return;
        }
//        doc.setUserData(KEY_MARKUP_UNIT, markupUnit, MarkupUnitDataHandler.getDefault());
        doc2markupUnit.put(doc, markupUnit);
    }
    
    public static MarkupUnit getMarkupUnitForDocument(Document doc) {
//        synchronized (doc2markupUnit) {
//            return (MarkupUnit)doc2markupUnit.get(doc);
//        }
        if (doc == null) {
            return null;
        }
//        return (MarkupUnit)doc.getUserData(KEY_MARKUP_UNIT);
        return doc2markupUnit.get(doc);
    }
    
//    private static class MarkupUnitDataHandler implements UserDataHandler {
//        private static final MarkupUnitDataHandler INSTANCE = new MarkupUnitDataHandler();
//        
//        public static MarkupUnitDataHandler getDefault() {
//            return INSTANCE;
//        }
//        
//        public void handle(short operation, String key, Object data, Node src, Node dst) {
//            // No op.
//        }
//    } // End of MarkupUnitDataHandler.
    
//    /** Map between <code>XhtmlCssEngine</code> and <code>MarkupUnit</code>. */
//    private final static Map cssEngine2markupUnit = new WeakHashMap();
//    
//    private void setMarkupUnitForCssEngine(XhtmlCssEngine cssEngine, MarkupUnit markupUnit) {
//        synchronized (cssEngine2markupUnit) {
//            cssEngine2markupUnit.put(cssEngine, markupUnit);
//        }
//    }
//    
//    public MarkupUnit getMarkupUnitForCssEngine(XhtmlCssEngine cssEngine) {
//        synchronized (cssEngine2MarkupUnit) {
//            return (MarkupUnit)cssEngine2markupUnit;
//        }
//    }
    // </markup_separation>
    
//    /** Map between <code>org.w3c.dom.Document</code> and <code>URL</code> */
//    private final static Map doc2url = new WeakHashMap();
//    private static final String KEY_URL = "vwpUrl"; // NOI18N
    
    private static final Map<Document, URL> doc2url = new WeakHashMap<Document, URL>();
    
    public /*private*/ static void setUrlForDocument(Document doc, URL url) {
//        synchronized (doc2url) {
//            doc2url.put(doc, url);
//        }
        if (doc == null) {
            return;
        }
//        doc.setUserData(KEY_URL, url, UrlDataHandler.getDefault());
        doc2url.put(doc, url);
    }
    
    public static URL getUrlForDocument(Document doc) {
//        synchronized (doc2url) {
//            return (URL)doc2url.get(doc);
//        }
        if (doc == null) {
            return null;
        }
//        return (URL)doc.getUserData(KEY_URL);
        return doc2url.get(doc);
    }
    
//    private static class UrlDataHandler implements UserDataHandler {
//        private static final UrlDataHandler INSTANCE = new UrlDataHandler();
//        
//        public static UrlDataHandler getDefault() {
//            return INSTANCE;
//        }
//        
//        public void handle(short operation, String key, Object data, Node src, Node dst) {
//            // No op.
//        }
//    } // End of UrlDataHandler.
}
