/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.io.*;
import java.util.*;
import java.net.URL;

import org.openide.xml.XMLUtil;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.UserCancelException;

import org.netbeans.tax.TreeUtilities;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.core.lib.GuiUtil;
import org.netbeans.api.xml.services.UserCatalog;
import org.xml.sax.*;

/**
 * GenerateDTDSupport class generate a DTD by guessing it from
 * XML document.
 * <p>
 * It's alreafy prepared for plugging in XMLSchema generator.
 *
 * @author   Libor Kramolis
 * @author   Petr Kuzel, rewritten to SAX
 */
public final class GenerateDTDSupport implements XMLGenerateCookie  {
    static final String DTD_EXT = "dtd"; // NOI18N

    /**
     * XML document data object actiang as a "template".
     */
    private final DataObject template;
    private ElementInfo current;
    private final Stack elementStack = new Stack();

    private final Map elementInfos;
    private String warning;
    private String rootQName;


    /**
     * @param template data object that is a "template" for created DTD
     */
    public GenerateDTDSupport(XMLDataObject template) {
        this.template = template;
        rootQName = null;
        warning = null;
        current = null;
        elementInfos = new LinkedHashMap(101);
    }

    /**
     * Performs a dialog with a user and generates the DTD
     */
    public void generate() {

        try {

            FileObject primFile = template.getPrimaryFile();
            String name = primFile.getName();
            FileObject folder = primFile.getParent();

            FileObject generFile = (new SelectFileDialog(folder, name, DTD_EXT, Util.NONEMPTY_CHECK)).getFileObject();
            name = generFile.getName();

            // IANA encoding name
            String encoding = "UTF-8";
            String dtd = xml2dtd(name, encoding);
            if (dtd == null) {
                String msg = Util.THIS.getString("BK0009");
                GuiUtil.notifyWarning(msg + "\n" + warning); // NOI18N
                return;
            }

            // write to file
            FileLock lock = null;
            Writer writer = null;
            try {
                lock = generFile.lock();
                encoding = TreeUtilities.iana2java(encoding == null ? "UTF-8" : encoding); // NOI18N
                OutputStream output = generFile.getOutputStream(lock);
                try {
                    writer = new OutputStreamWriter(output, encoding);
                } catch (UnsupportedEncodingException e) {
                    writer = new OutputStreamWriter(output);
                }
                writer = new PrintWriter(writer);
                writer.write(dtd.toString());
                lock.releaseLock();
            } finally {
                if (writer != null)
                    writer.close();
                if (lock != null)
                    lock.releaseLock();
            }

// disabled until in-memory XML model is not performance problem
//            trySetDocumentType(name);

            GuiUtil.performDefaultAction(generFile);

        } catch (UserCancelException e) {
//          } catch (FileStateInvalidException e) {
//          } catch (TreeException e) {
//          } catch (IOException e) {
        } catch (Exception exc) {
            GuiUtil.notifyException(exc);
        }
    }


//    /**
//     * Update template's DOCTYPE to point to generated DTD.
//     */
//    private void trySetDocumentType(String fileName) {
//        if (templateRoot.getParentNode() instanceof TreeDocument) { // try to set only when element is root document element
//            TreeDocument document = (TreeDocument) templateRoot.getParentNode();
//            if (GuiUtil.confirmAction(Util.THIS.getString("MSG_use_dtd_as_document_type?"))) {
//                try {
//                    TreeDocumentType newDoctype = new TreeDocumentType(templateRoot.getQName(), null, fileName + "." + DTD_EXT); // NOI18N
//                    document.setDocumentType(newDoctype);
//                } catch (TreeException exc) {
//                    GuiUtil.notifyWarning(exc.getLocalizedMessage());
//                }
//            }
//        }
//    }


    /**
     * Generate the DTD into temporary string.
     * @return null if problems leaved in <code>warning</code> field occured
     *         otherwise the DTD.
     */
    String xml2dtd(String name, String encoding) {
        StringBuffer sb = new StringBuffer();

        // fill table of dtd declarations
        if (false == scanTemplate()) {
            return null;
        }

        if (encoding != null) {
            sb.append("<?xml version='1.0' encoding='").append(encoding).append("'?>\n\n"); // NOI18N
        }

        String todo = Util.THIS.getString("TODO", name + "." + DTD_EXT);
        sb.append("<!--\n    ").append(todo).append("\n\n-->");

        String usage = Util.THIS.getString("BK0010");
        sb.append("<!--\n").append("    " + usage + "\n\n").append("    <?xml version=\"1.0\"?>\n\n").// NOI18N
        append("    <!DOCTYPE ").append(rootQName).append(" SYSTEM \"").// NOI18N
        append(name).append(".").append(DTD_EXT).append("\">\n\n").// NOI18N
        append("    <").append(rootQName).append(">\n    ...\n").append("    </").append(rootQName).append(">\n").// NOI18N
        append("-->\n"); // NOI18N

        // generate DTD contaent by the table
        //??? we could easily plug here XML Schema generator

        Iterator it = elementInfos.values().iterator();
        ElementInfo elem;
        while (it.hasNext()) {
            sb.append("\n"); // NOI18N
            elem = (ElementInfo) it.next();
            // <!ELEMENT ...
            sb.append("<!--- " + Util.THIS.getString("FMT_DTDDoc") + " -->\n");
            //!!! there may by clash if the doccument happens to map several
            // URI into one prefix
            sb.append("<!ELEMENT ").append(elem.name.qName).append(" "); // NOI18N

            if (elem.empty) {
                sb.append("EMPTY"); // NOI18N
            } else {
                Collection collect = elem.children;
                if ((elem.pcdata == true) ||
                        (collect.size() == 0)) {
                    Vector vect = new Vector(collect);
                    vect.insertElementAt(new XName("","","#PCDATA"), 0); // NOI18N
                    collect = vect;
                }
                Iterator itc = collect.iterator();
                XName elemName;
                elemName = (XName) itc.next();
                sb.append("(").append(elemName.qName); // NOI18N
                while (itc.hasNext()) {
                    elemName = (XName) itc.next();
                    sb.append("|").append(elemName.qName); // NOI18N
                }

                //!!!HACK #6928
                if (false == sb.toString().endsWith("#PCDATA")) { // NOI18N
                    sb.append(")*"); // NOI18N
                } else {
                    sb.append(")"); // NOI18N
                }
            }
            sb.append(">\n"); // NOI18N

            // <!ATTLIST ...
            if (elem.attributes.size() != 0) {
                sb.append("<!ATTLIST ").append(elem.name.qName).append("\n"); // NOI18N
                Iterator ita = elem.attributes.iterator();
                while (ita.hasNext()) {
                    XName attName = (XName) ita.next();
                    sb.append("    ").append(attName.qName).append(" CDATA #IMPLIED\n"); // NOI18N
                }
                sb.append("  >\n"); // NOI18N
            }
        }

        return sb.toString();
    }

    /**
     * Using SAX events fill elementsInfo map.
     * @return false in parsing errors have occured.
     */
    private boolean scanTemplate() {
        URL url = null;
        XMLReader parser = null;
        try {
            url = template.getPrimaryFile().getURL();
        } catch (FileStateInvalidException e) {
            warning = e.getLocalizedMessage();
            return false;
        }

        String system = url.toExternalForm();
        try {
            parser = XMLUtil.createXMLReader(false, true);
            Impl impl = new Impl();
            parser.setContentHandler(impl);
            parser.setErrorHandler(impl);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);  // NOI18N
            UserCatalog catalog = UserCatalog.getDefault();
            if (catalog != null) {
                EntityResolver resolver = catalog.getEntityResolver();
                if (resolver != null) {
                    parser.setEntityResolver(resolver);
                }
            }
        } catch (SAXException e) {
            warning = e.getLocalizedMessage();
            return false;
        }

        InputSource input = new InputSource(system);
        try {
            parser.parse(input);
            return true;
        } catch (IOException e) {
            warning = e.getLocalizedMessage();
            return false;
        } catch (SAXException e) {
            warning = e.getLocalizedMessage();
            return false;
        }
    }



    /**
     * Return true if parameter contains just white spaces.
     */
    private boolean wsOnly(String s) {
        if (s == null) return true;

        char[] data = s.toCharArray();
        for (int i = 0; i < data.length; i++) {
            if (Character.isWhitespace(data[i]) == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return true if parameter contains just white spaces.
     */
    private boolean wsOnly(char[] data, int from, int length) {
        for (int i = from; i < from + length; i++) {
            if (Character.isWhitespace(data[i]) == false) {
                return false;
            }
        }

        return true;
    }

    // SAX2 Content handler methods

    private class Impl implements ContentHandler, ErrorHandler {
        public void characters(char[] chars, int i, int i1) throws SAXException {
            if (false == wsOnly(chars, i, i1)) {
                if (current != null) {
                    current.hasPCDATA();
                }
            }
        }

        public void endDocument() throws SAXException {
        }

        public void endElement(String s, String s1, String s2) throws SAXException {
            current = (ElementInfo) elementStack.pop();
        }

        public void endPrefixMapping(String s) throws SAXException {
        }

        public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        }

        public void processingInstruction(String s, String s1) throws SAXException {
        }

        public void setDocumentLocator(Locator locator) {
        }

        public void skippedEntity(String s) throws SAXException {
        }

        public void startDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (rootQName == null) {
                rootQName = qName;
            }

            XName xName = new XName(uri, localName, qName);
            ElementInfo info = (ElementInfo) elementInfos.get(xName);
            if (info == null) {
                info = new ElementInfo(uri, localName, qName);
                elementInfos.put(xName, info);
            }
            for (int i = 0; i<attributes.getLength(); i++) {
                // what is URI of the "xmlns:" prefix?
                info.addAttribute(attributes.getURI(i), attributes.getLocalName(i), attributes.getQName(i));
            }

            if (current != null) {
                current.addChild(info);
            }
            elementStack.push(current);
            current = info;
        }

        public void startPrefixMapping(String s, String s1) throws SAXException {
        }

        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public void warning(SAXParseException e) throws SAXException {
        }
    }


    /**
     * Holds all info gathered while scanning template document.
     */
    private class ElementInfo {
        XName name;
        Set children;
        Set attributes;
        boolean pcdata;
        boolean empty;

        public ElementInfo(String uri, String localName, String qName) {
            name = new XName(uri, localName, qName);
            children = new HashSet();
            attributes = new HashSet();
            pcdata = false;
            empty = true;
        }

        public void hasPCDATA() {
            pcdata = true;
            empty = false;
        }

        public boolean isTextAllowed() {
            return pcdata;
        }

        public void addChild(ElementInfo info) {
            empty = false;
            children.add(info.name);
        }

        public void addAttribute(String uri, String localName, String qName) {
            attributes.add(new XName(uri, localName, qName));
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof ElementInfo) {
                ElementInfo info = (ElementInfo) obj;
                return name.equals(info.name);
            }
            return false;
        }

        public int hashCode() {
            return name.hashCode();
        }
    } // end of inner class ElementInfo

    /**
     * Structured XML name with value based identity.
     * The identity onors all URI, localName, qName. It's usefull
     * for DTD generators. XML Schema generators must remove
     * {URI}localName duplicities caused by qNames.
     */
    private static class XName {
        private String uri, localName, qName;
        public XName(String uri, String localName, String qName) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
        }

        public boolean equals(Object peer) {
            if (peer == this) return true;
            if (peer instanceof XName) {
                XName id = (XName) peer;
                return uri.equals(id.uri)
                    && localName.equals(id.localName)
                    && qName.equals(id.qName);
            }
            return false;
        }

        public int hashCode() {
            return uri.hashCode() ^ localName.hashCode() ^ qName.hashCode();
        }
    }
}
