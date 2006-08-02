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
 *  
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

/**
 *
 * @author Dmitry Lipin
 */
public class XMLUtils {
    private static final XMLUtils xmlUtils = new XMLUtils();
    private static final String ERROR_SAVING_FILE = "Could not save XML to file ";
    
    /** Creates a new instance of XMLUtils */
    private XMLUtils() {
    }
    
    public static XMLUtils getInstance() {
        return xmlUtils;
    }
    
    public void saveXMLDocument(Document doc, File file,File xsltTransformFile)
    throws TransformerConfigurationException, TransformerException, IOException {
        FileOutputStream outputStream = null;
        try {
            Source xsltSource = new StreamSource(xsltTransformFile);
            Source domSource = new DOMSource(doc);
            outputStream = new FileOutputStream(file);
            Result streamResult = new StreamResult(outputStream);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(xsltSource);
            
            transformer.transform(domSource, streamResult);
        } finally {
            if(outputStream!=null) {
                outputStream.close();
            }
        }
    }
}
