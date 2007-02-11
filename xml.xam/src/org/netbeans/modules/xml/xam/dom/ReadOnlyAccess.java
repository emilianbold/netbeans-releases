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
package org.netbeans.modules.xml.xam.dom;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Default document model access with limited support for read-only operations.
 * 
 * @author Nam Nguyen
 */
public class ReadOnlyAccess extends DocumentModelAccess {
    private AbstractDocumentModel model;
    private Document rootDoc;
    
    /** Creates a new instance of PlainDOMAccess */
    public ReadOnlyAccess(AbstractDocumentModel model) {
        this.model = model;
    }
    
    public AbstractDocumentModel getModel() {
        return model;
    }
    
    public void setPrefix(Element node, String prefix) {
        throw new UnsupportedOperationException("setPrefix access not supported.");
    }
    
    public List<Element> getPathFromRoot(Document root, Element node) {
        // mainly for merge sync, not needed
        throw new UnsupportedOperationException("getPathFromRoot access not supported.");
    }
    
    public String getXPath(Document root, Element node) {
        throw new UnsupportedOperationException("getXPath access not supported.");
    }
    
    public Map<QName, String> getAttributeMap(Element element) {
        Map<QName,String> qValues = new HashMap<QName,String>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i=0; i<attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            if (isXmlnsAttribute(attr)) {
                continue;
            }
            QName q = AbstractDocumentComponent.getQName(attr);
            qValues.put(q, attr.getValue());
        }
        return qValues;
    }
    
    public static boolean isXmlnsAttribute(Attr attr) {
        return XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getPrefix()) ||
                XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getName());
    }
    
    public Element duplicate(Element element) {
        throw new UnsupportedOperationException("getXPath access not supported.");
    }
    
    public String getXmlFragment(Element element) {
        String fragment = getXmlFragmentInclusive(element);
        if (fragment.endsWith("/>")) {
            return null;
        }
        int start = fragment.indexOf(">");
        start++;
        int end = fragment.lastIndexOf("<");
        return fragment.substring(start, end);
    }
    
    public void setXmlFragment(Element element, String text, DocumentModelAccess.NodeUpdater updater) throws IOException {
        throw new UnsupportedOperationException("setXmlFragment access not supported.");
    }
    
    public void setText(Element element, String val, DocumentModelAccess.NodeUpdater updater) {
        throw new UnsupportedOperationException("setText access not supported.");
    }
    
    public void removeAttribute(Element element, String name, DocumentModelAccess.NodeUpdater updater) {
        throw new UnsupportedOperationException("removeAttribute access not supported.");
    }
    
    public void setAttribute(Element element, String name, String value, DocumentModelAccess.NodeUpdater updater) {
        throw new UnsupportedOperationException("setAttribute access not supported.");
    }
    
    public int findPosition(Node node) {
        Element root = ((DocumentComponent) model.getRootComponent()).getPeer();
        javax.swing.text.Document doc = model.getBaseDocument();
        try {
            String buf = doc.getText(0, doc.getLength());
            if (node instanceof Element) {
                int pos = getRootElementPosition(buf, root);
                StringScanner scanner = new StringScanner(buf, pos);
                return findPosition((Element)node, root, scanner);
            }
        } catch(BadLocationException e) {
            // just return -1
        }
        return -1;
    }
    
    private int getRootElementPosition(String buf, Element root) {
        NodeList children = root.getOwnerDocument().getChildNodes();
        int pos = 0;
        for (int i=0; i<children.getLength(); i++) {
            Node n = children.item(i);
            if (n != root) {
                String s = n.getNodeValue();
                assert (s != null) : "Invalid document";
                pos += s.length();
            } else {
                break;
            }
        }
        pos = buf.indexOf(root.getTagName(), pos)-1;
        assert pos > 0 : "Root element position should be greater than 0";
        return pos;
    }
    
    private static class StringScanner {
        String buf;
        int pos = -1;
        public StringScanner(String buf, int pos) {
            this.buf = buf;
            this.pos = pos;
        }
        public void scanTo(String token) {
            pos = buf.indexOf(token, pos);
            if (pos == -1) {
                throw new IllegalArgumentException("Scan failed: position -1");
            }
        }
        public void skip(String token) {
            scanTo(token);
            skip(token.length());
        }
        public void skip(int count) {
            pos += count;
        }
    }
    
    private int findPosition(Element target, Element base, StringScanner scanner) {
        if (target == base) {
            return scanner.pos;
        }
        scanner.skip(">");
        NodeList children = base.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node node = children.item(i);
            // condition is we always have argument fromPos be the index of the
            // previous element plus sum length of any text/comment/attribute
            if (! (node instanceof Element)) {
                String s = node.getNodeValue();
                if (s == null) {
                    s = node.getTextContent();
                }
                if (s != null) {
                    scanner.skip(s.length());
                }
                continue;
            }
            Element current = (Element)children.item(i);
            String tag = "<" + current.getTagName(); 
            scanner.scanTo(tag);
            if (current == target) {
                return scanner.pos;
            }
            
            int found = findPosition(target, current, scanner);
            if (found > -1) {
                return found;
            }
        }
        if (children.getLength() > 0) {
            scanner.skip(">");
        }
        return -1;
    }
    
    public int getElementIndexOf(Node parent, Element child) {
        // only needed for sync merge
        throw new UnsupportedOperationException("getElementIndexOf access not supported.");
    }
    
    public List<Node> findNodes(Document root, String xpath) {
        throw new UnsupportedOperationException("findNodes access not supported.");
    }
    
    public Node findNode(Document root, String xpath) {
        throw new UnsupportedOperationException("findNode access not supported.");
    }
    
    public Element getContainingElement(int position) {
        try {
            javax.swing.text.Document swingDoc = model.getBaseDocument();
            String buf = swingDoc.getText(0, swingDoc.getLength());
            Element root = model.getDocument().getDocumentElement();
            if (position < 0) return null;
            return findElement(position, buf, root, getRootElementPosition(buf, root));
        } catch(Exception e) {
            return null;
        }
    }
    
    private String getNonElementString(String buf, int basePos, Node node) {
        assert ! (node instanceof Element) : "Element is not expected";
        String s = node.getNodeValue();
        if (s == null) {
            s = node.getTextContent();
        }
        assert (s != null) : "Expected node has string value";
        return s;
    }
    
    protected Element findElement(int position, String buf, Element base, int basePos) {
        if (basePos == position) {
            return base;
        }
        
        NodeList children = base.getChildNodes();
        
        for (int i=0; i<children.getLength(); i++) {
            Node node = children.item(i);
            // check if the position is more than boundary of next element
            // i.e. basePos plus sum length of any text/comment
            if (! (node instanceof Element)) {
                String s = getNonElementString(buf, basePos, node);
                basePos = buf.indexOf(s, basePos);
                if (position < basePos) {
                    return base;
                } else {
                    basePos += s.length();
                }
                continue;
            }
            Element current = (Element)children.item(i);
            String tag = "<" + current.getTagName(); //NOI18N
            basePos = buf.indexOf(tag, basePos);
            if (basePos > position) {
                return base;
            } else {
                Element found = findElement(position, buf, current, basePos);
                if (found != null) {
                    return found;
                }
                if (i+1 < children.getLength() && ! (children.item(i+1) instanceof Element)) {
                    String s = getNonElementString(buf, basePos, children.item(i+1));
                    int endCurrent = buf.indexOf(s, basePos);
                    if (endCurrent > position) {
                        return current;
                    }
                }
            }
            
        }
        return null;
    }
    
    public void addUndoableEditListener(UndoableEditListener listener) {
        throw new UnsupportedOperationException("addUndoableEditListener access not supported.");
    }
    
    public void removeUndoableEditListener(UndoableEditListener listener) {
        throw new UnsupportedOperationException("removeUndoableEditListener access not supported.");
    }
    
    public void appendChild(Node node, Node newChild, DocumentModelAccess.NodeUpdater updater) {
        throw new UnsupportedOperationException("appendChild access not supported.");
    }
    
    public void insertBefore(Node node, Node newChild, Node refChild, DocumentModelAccess.NodeUpdater updater) {
        throw new UnsupportedOperationException("insertBefore access not supported.");
    }
    
    public void removeChild(Node node, Node child, DocumentModelAccess.NodeUpdater updater) {
        throw new UnsupportedOperationException("removeChild access not supported.");
    }
    
    public void replaceChild(Node node, Node child, Node newChild, DocumentModelAccess.NodeUpdater updater) {
        throw new UnsupportedOperationException("replaceChild access not supported.");
    }
    
    public Model.State sync() throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            javax.swing.text.Document buffer = model.getBaseDocument();
            String xmlText = buffer.getText(0, buffer.getLength());
            BufferedReader reader = new BufferedReader(new StringReader(xmlText));
            rootDoc = db.parse(new InputSource(reader));
            Element rootElement = rootDoc.getDocumentElement();
            if (model.createRootComponent(rootElement) == null) {
                throw new IOException("Cannot create model with "+
                        new QName(rootElement.getNamespaceURI(), rootElement.getLocalName()));
            }
            return Model.State.VALID;
        } catch (ParserConfigurationException pce) {
            IOException ioe = new IOException();
            ioe.initCause(pce);
            throw ioe;
        } catch (BadLocationException ble) {
            IOException ioe = new IOException();
            ioe.initCause(ble);
            throw ioe;
        } catch (SAXException saxe) {
            IOException ioe = new IOException();
            ioe.initCause(saxe);
            throw ioe;
        }
    }
    
    public ElementIdentity getElementIdentity() {
        throw new UnsupportedOperationException("getElementIdentity access not supported.");
    }
    
    public Document getDocumentRoot() {
        return rootDoc;
    }
    
    public void flush() {
        //NOOP
    }
    
    public void finishUndoRedo() {
        throw new UnsupportedOperationException("finishUndoRedo access not supported.");
    }
    
    public boolean areSameNodes(Node n1, Node n2) {
        return n1.equals(n2);
    }
    
    public void prepareForUndoRedo() {
        throw new UnsupportedOperationException("prepareForUndoRedo access not supported.");
    }
    
    public void addMergeEventHandler(PropertyChangeListener l) {
        //NOT SUPPORTED
    }
    
    public void removeMergeEventHandler(PropertyChangeListener l) {
        //NOT SUPPORTED
    }
    
    public Node getOldEventParentNode(PropertyChangeEvent evt) {
        //NOT SUPPORTED
        return null;
    }
    
    public Node getOldEventNode(PropertyChangeEvent evt) {
        //NOT SUPPORTED
        return null;
    }
    
    public Node getNewEventParentNode(PropertyChangeEvent evt) {
        //NOT SUPPORTED
        return null;
    }
    
    public Node getNewEventNode(PropertyChangeEvent evt) {
        //NOT SUPPORTED
        return null;
    }
    
    public String normalizeUndefinedAttributeValue(String value) {
        return "".equals(value) ? null : value; //NOI18N
    }
    
    public static class Provider implements DocumentModelAccessProvider {
        private static Provider instance;
        protected Provider() {
        }
        
        public static Provider getInstance() {
            if (instance == null) {
                instance = new Provider();
            }
            return instance;
        }
        
        public DocumentModelAccess createModelAccess(AbstractDocumentModel model) {
            return new ReadOnlyAccess(model);
        }
        
        public javax.swing.text.Document loadSwingDocument(InputStream in)
        throws IOException, BadLocationException {
            
            javax.swing.text.Document sd = new PlainDocument();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            try {
                String line = null;
                while ((line = br.readLine()) != null) {
                    sd.insertString(sd.getLength(), line+System.getProperty("line.separator"), null); // NOI18N
                }
            } finally {
                br.close();
            }
            return sd;
        }
        
        public Object getModelSourceKey(ModelSource source) {
            return source.getLookup().lookup(File.class);
        }
    }
    
}
