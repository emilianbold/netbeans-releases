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
