/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xsd;

import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;

/**
 * ContentHandler impl for building XSD grammar
 * @author  anovak
 */
class XSDContentHandler implements ContentHandler {
    
    /** Stack for parsed elements */
    private List /*<Element>*/ elementsStack;
    /** All elements */
    private Map elements;
    /** All types */
    private Map types;
    
    private PrintStream ps;
    
    /** Creates a new instance of XSDContentHandler */
    public XSDContentHandler(PrintStream ps) {
        this.ps = ps;
        this.elements = new HashMap();
        this.types = new HashMap();
        this.elementsStack = new ArrayList();
    }

    public org.netbeans.modules.xml.api.model.GrammarQuery getGrammar() {
            return new XSDGrammar(elements, types);
    }
    
    private void println(String s) {
        ps.println(s);
    }
    
    private void printlnAttributes(Attributes atts) {
        println("START Attributes");
        for (int i = 0; i < atts.getLength(); i++) {
            println("Attr[" + i + "] localname: " + atts.getLocalName(i) + " qname: " + atts.getQName(i) + " value: " + atts.getValue(i) + " URI: " + atts.getURI(i) + " type: " + atts.getType(i));
        }
        println("END Attributes");
    }
    
    
    private static void printlnElement(SchemaElement e, String prefix) {
        System.out.println(prefix + e.toString());
        Iterator it = e.getSubelements();
        while (it.hasNext()) {
            printlnElement((SchemaElement) it.next(), prefix + "    ");
        }
        
    }
    
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
    //    println("characters: " + new String(ch, start, length));
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
        System.out.println("Stack size: " + elementsStack.size());
        
        SchemaElement e = (SchemaElement) elementsStack.get(0);
        printlnElement(e, "    ");
        
        // println("END Document");
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
        // println("endELement: " + namespaceURI + " name: " + localName + " qname: " + qName);
        // pop element
        elementsStack.remove(elementsStack.size() - 1);
    }
    
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
        // println("endPrefixMapping: " + prefix);
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(String target, String data) throws org.xml.sax.SAXException {
        // println("Processing instruction: " + target + " data: " + data);
    }
    
    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }
    
    public void skippedEntity(String name) throws org.xml.sax.SAXException {
        // println("skippedEntity: " + name);
    }
    
    public void startDocument() throws org.xml.sax.SAXException {
        elementsStack.add(SchemaElement.createSchemaElement(null, "TOP_LEVEL", null));
        // println("START Doc");
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
        SchemaElement e = SchemaElement.createSchemaElement(namespaceURI, qName, atts);
        System.err.println("ELEMENTS ADDING: " + atts.getValue("name") + " qname: " + qName);
        SchemaElement parent = (SchemaElement) elementsStack.get(elementsStack.size() - 1);
        if (parent != null && parent.getSAXAttributes() != null) {
            System.err.println("INTO: " + parent.getSAXAttributes().getValue("name"));
        }
        elements.put(atts.getValue("name"),  e);
        parent.addSubelement(e);
        // push
        elementsStack.add(e);
       // println("startElements: " + namespaceURI + "locName: " + localName + " qName: " + qName);
       // printlnAttributes(atts);
    }
    
    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
        // println("startPrefixMapping: prefix:  " + prefix + " URI: " + uri);
    }    
}
