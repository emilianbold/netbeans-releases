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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.xtest.util;

import java.io.*;
import java.util.*;
import java.text.*;
import org.w3c.dom.*;

/**
 * Serializes DOM trees.
 * @author  vs124454
 */
public class XMLWriter {


    private PrettyPrinter printer;
    private String encoding;


    /** Creates new XMLWriter */
    public XMLWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
        printer = new PrettyPrinter(new OutputStreamWriter(out,encoding), 3, 80);
        this.encoding = encoding;
    }


    

    public void write(Document doc) throws IOException {
        write(doc, null);
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void write(Document doc, LinkedList entities) throws IOException {
        printer.print("<?xml version=\"1.0\" encoding=\""+getEncoding()+"\"?>");
        printer.newLine();
        
        if (null != entities && 0 != entities.size()) {
            writeEntities(doc.getDocumentElement().getNodeName(), entities);
        }

        writeElement(doc.getDocumentElement());
    }
    
    /**
     * Translates <, & , " and > to corresponding entities.
     */
    private String xmlEscape(String orig) {
        if (orig == null) return "";
        StringBuffer temp = new StringBuffer();
        StringCharacterIterator sci = new StringCharacterIterator(orig);
        for (char c = sci.first(); c != CharacterIterator.DONE;c = sci.next()) {
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

    /** Replaces invalid xml characters by its hex value. Valid xml characters
     * are only these: #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * (http://www.w3.org/TR/2004/REC-xml-20040204/#charsets).
     */
    private String invalidCharactersEscape(String orig) {
        if (orig == null) return "";
        StringBuffer temp = new StringBuffer();
        StringCharacterIterator sci = new StringCharacterIterator(orig);
        for (char c = sci.first(); c != CharacterIterator.DONE;c = sci.next()) {
            if((c == 9) || (c == 10) || (c == 13) ||
                    ((c >= 0x0020) && (c <= 0xD7FF)) ||
                    ((c >= 0xE000) && (c <= 0xFFFD)) ||
                    ((c >= 0x10000) && (c <= 0x10FFFF))) {
                // regular char
                temp.append(c);
            } else {
                // invalid char (replace by hex value)
                temp.append('0');
                temp.append('x');
                temp.append(Integer.toHexString(c).toCharArray());
            }
        }
        return temp.toString();
    }
    
    /**
     *  Writes a DOM element to a stream.
     */
    private void writeElement(Element element) throws IOException {        
        
        printer.print("<"+element.getTagName());        
        printer.indentUp();

        // Write attributes
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            // #50889 - invalid xml characters causes that results are not
            // properly generated. We have to replace invalid characters by
            // its hex value.
            String attribute = " "+attr.getName()+"=\""+invalidCharactersEscape(xmlEscape(attr.getValue()))+"\"";
            printer.print(attribute);
        }
                
        
        boolean hasChildren = element.hasChildNodes();
        if (hasChildren) {
            printer.print(">");
            printer.newLine();
        } else {            
            printer.indentDown();
            printer.print("/>");
            printer.newLine();
            return;
        }

        // Write child attributes and text
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                writeElement((Element)child);                
            }

            if (child.getNodeType() == Node.TEXT_NODE) {
                String textNode = child.getNodeValue();
                if (!justWhiteSpaces(textNode)) {
                    printer.print(child.getNodeValue());                
                }
            }
            
            if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                printer.print("<![CDATA[");
                String s = child.getNodeValue();
                // Caution - the string might contain "]]>". Can happen if you do e.g.:
                // assertEquals(Arrays.asList(new Object[] {Arrays.asList(new Object[0])}), null);
                // which would print the bogus XML:
                // <![CDATA[junit.framework.AssertionFailedError: expected:<[[]]> but was:<null>
                //         at Whatever.java:123
                // ]]>
                // Cf.: http://www.w3.org/TR/REC-xml#sec-cdata-sect
                // Of course using a real XML serializer (as e.g. XMLUtil.write) does this for you.
                s = s.replaceAll("]]>", "]]]]><![CDATA[>");
                // #50889 - invalid xml characters causes that results are not
                // properly generated. We have to replace invalid characters by
                // its hex value.
                s = invalidCharactersEscape(s);
                printer.printUnformatted(s);
                printer.printUnformatted("]]>");
                printer.newLine();
            }
            
            if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                printer.print("&"+child.getNodeName()+";");
            }
        }
        
        // Write element close        
        printer.indentDown();        
        printer.print("</"+element.getTagName()+">");
        printer.newLine();
    }
    
    // check whether string contains just whitespaces
    private static boolean justWhiteSpaces(String string) {
        char[] str = string.toCharArray();
        for (int i=0; i < str.length; i++) {
            if (!Character.isWhitespace(str[i])) {
                return false;
            }
        }
        return true;
    }
    
    
    private void writeEntities(String name, LinkedList ents) throws IOException {
/*
<!DOCTYPE Company [
  <!ENTITY bas_d SYSTEM "bas\%id%_d.xml">
  <!ENTITY bas_c SYSTEM "bas\%id%_c.xml">
  <!ENTITY fin SYSTEM "fin\%id%.xml">
  <!ENTITY lang SYSTEM "disc://<feclipath>/configuration/lang.xml">
  <!ENTITY dummy SYSTEM "dummy.xml">
]>
*/
        printer.newLine();
        printer.print("<!DOCTYPE "+name+" [");
        printer.indentUp();
        Iterator it = ents.iterator();
        while (it.hasNext()) {
            DOMEntityDecl e = (DOMEntityDecl)it.next();
            printer.newLine();
            printer.print("<!ENTITY "+e.getName()+" SYSTEM \""+e.getSystemId()+"\">");
            printer.newLine();
        }
        printer.indentDown();
        printer.newLine();
        printer.print("]>");
    }
    
    
    
    
    
    // pretty printer class for formatting xml output with indentation and word wrapping
    public static class PrettyPrinter {
        
        private int maxColumns = 0;
        private int indentSize = 0;
        private Writer writer;
        
        private int currentColumn = 0;
        private int currentIndent = 0;
        private String indentString;
        
        PrettyPrinter(Writer writer, int indentSize, int maxColumns) {
            this.writer = writer;
            this.indentSize = indentSize;
            this.maxColumns = maxColumns;
            indentString = getIndentString(indentSize);
        }
        
        public void indentUp() {
            currentIndent++;
            
        }
        
        public void indentDown() {
            if (currentIndent > 0) {
                currentIndent--;
            }
        }
        
        public void print(String string) throws IOException {
            int oldIndex = 0 ;
            int newIndex = 0;
            while ((newIndex = string.indexOf('\n',oldIndex)) >= 0) {
                String subString = string.substring(oldIndex,newIndex);
                print(subString);
                if (subString.length() < maxColumns) {
                    newLine();
                }
                oldIndex = newIndex + 1;
            }
            
            if (oldIndex > 0) {
                // printout the rest of the string;
                String subString = string.substring(oldIndex);                              
                print(subString);
                return;
            }
                        
            // check whether the string will be over our limit
            if ((currentColumn + string.length()) >= maxColumns) {
                newLine();
            }
            
            // indent before printing the string (if at the beiginning of line)
            if (currentColumn == 0) {                
                printIndent();
            }
            
            // print out the string
            writer.write(string);
            currentColumn += string.length();
            writer.flush();
        }
        
        public void printUnformatted(String string) throws IOException {
            writer.write(string);
            writer.flush();
        }
    
        
        public void newLine() throws IOException {
            writer.write('\n');
            currentColumn = 0;
            writer.flush();
        }
        
        private void printIndent() throws IOException {
            for (int i=0; i < currentIndent; i++) {
                writer.write(indentString);
                currentColumn += indentSize;
            }
        }
        
        public void reset() {
            currentColumn = 0;
            currentIndent = 0;
        }
        
        private static String getIndentString(int indentSize) {
            char[] indentChars = new char[indentSize];
            for (int i=0; i < indentSize; i++) {
                indentChars[i] = ' ';
            }
            return new String(indentChars);
        }      
    }
}
