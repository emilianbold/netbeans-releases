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
 * SerializeDOM.java
 *
 * Created on October 30, 2001, 5:58 PM
 */

package org.netbeans.xtest.util;

import org.w3c.dom.*;
import java.io.*;
import javax.xml.parsers.*;
import org.netbeans.xtest.util.XMLWriter;
import org.netbeans.xtest.util.XMLFactoryUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author  mb115822
 * @version 
 */
public class SerializeDOM {

    /** Creates new SerializeDOM */
    public SerializeDOM() {
    }
    

    public static DocumentBuilder getDocumentBuilder() {
        try {
            return XMLFactoryUtil.newDocumentBuilder();
        }
        catch(Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }    
    
    public static void serializeToStream(Document doc, OutputStream out) throws IOException {
        XMLWriter xmlWriter = new XMLWriter(out, "UTF-8");
        xmlWriter.write(doc);
        //out.close();
    }
    
    public static void serializeToFile(Document doc, File outFile) throws IOException {
        OutputStream out = new FileOutputStream(outFile);
        serializeToStream(doc,out);
        out.close();
    }
    
        public static Document parseStream(InputStream input) throws IOException {
        try {
            Document parsedDoc = getDocumentBuilder().parse(input);
            return parsedDoc;
        } catch (SAXException saxe) {
            throw new IOException(saxe.getMessage());
        }
    }
    
     public static Document parseFile(File inputFile) throws IOException {
        try {
            Document parsedDoc = getDocumentBuilder().parse(inputFile);
            return parsedDoc;
        } catch (SAXException saxe) {
            throw new IOException(saxe.getMessage());
        }
    }


}
