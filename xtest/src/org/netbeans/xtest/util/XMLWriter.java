/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * XMLWriter.java
 *
 * Created on March 29, 2001, 7:44 PM
 */

package org.netbeans.xtest.util;

import java.io.*;
import java.util.*;
import java.text.*;
import org.w3c.dom.*;

/**
 *
 * @author  vs124454
 * @version 
 */
public class XMLWriter {

    /** Creates new XMLWriter */
    public XMLWriter() {
    }

    public void write(Document doc, Writer wri) throws IOException {
        write(doc, null, wri);
    }
    
    public void write(Document doc, LinkedList entities, Writer wri) throws IOException {
        wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        if (null != entities && 0 != entities.size())
            writeEntities(doc.getDocumentElement().getNodeName(), entities, wri);

        writeElement(doc.getDocumentElement(), wri, 0);
        wri.flush();
    }
    
    /**
     * Translates <, & , " and > to corresponding entities.
     */
    private String xmlEscape(String orig) {
        if (orig == null) return "";
        StringBuffer temp = new StringBuffer();
        StringCharacterIterator sci = new StringCharacterIterator(orig);
        for (char c = sci.first(); c != CharacterIterator.DONE;
             c = sci.next()) {

            switch (c) {
            case '<':
                temp.append("&lt;");
                break;
            case '>':
                temp.append("&gt;");
                break;
            case '\"':
                temp.append("&quot;");
                break;
            case '&':
                temp.append("&amp;");
                break;
            default:
                temp.append(c);
                break;
            }
        }
        return temp.toString();
    }

    /**
     *  Writes a DOM element to a stream.
     */
    private void writeElement(Element element, Writer out, int indent) throws IOException {
        // Write indent characters
        writeIndent(out, indent);

        // Write element
        out.write("<");
        out.write(element.getTagName());

        // Write attributes
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            out.write(" ");
            out.write(attr.getName());
            out.write("=\"");
            out.write(xmlEscape(attr.getValue()));
            out.write("\"");
        }
        out.write(">");

        // Write child attributes and text
        boolean hasChildren = false;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (!hasChildren) {
                    out.write("\n");
                    hasChildren = true;
                }
                writeElement((Element)child, out, indent + 1);
            }

            if (child.getNodeType() == Node.TEXT_NODE) {
                out.write("<![CDATA[");
                out.write(((Text)child).getData());
                out.write("]]>");
            }

            if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                out.write("&");
                out.write(child.getNodeName());
                out.write(";");
            }
        }

        // If we had child elements, we need to indent before we close
        // the element, otherwise we're on the same line and don't need
        // to indent
        if (hasChildren) {
            writeIndent(out, indent);
        }

        // Write element close
        out.write("</");
        out.write(element.getTagName());
        out.write(">\n");
    }
    
    private void writeEntities(String name, LinkedList ents, Writer out) throws IOException {
/*
<!DOCTYPE Company [
  <!ENTITY bas_d SYSTEM "bas\%id%_d.xml">
  <!ENTITY bas_c SYSTEM "bas\%id%_c.xml">
  <!ENTITY fin SYSTEM "fin\%id%.xml">
  <!ENTITY lang SYSTEM "disc://<feclipath>/configuration/lang.xml">
  <!ENTITY dummy SYSTEM "dummy.xml">
]>
*/
        
        out.write("<!DOCTYPE ");
        out.write(name);
        out.write(" [\n");
        Iterator it = ents.iterator();
        while (it.hasNext()) {
            DOMEntityDecl e = (DOMEntityDecl)it.next();

            writeIndent(out, 1);
            out.write("<!ENTITY ");
            out.write(e.getName());
            out.write(" SYSTEM \"");
            out.write(e.getSystemId());
            out.write("\">\n");
        }
        out.write("]>\n");
    }
    
    private void writeIndent(Writer out, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            out.write("    ");
        }
    }
}
