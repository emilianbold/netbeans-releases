/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.ui.tester.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Encoder tester utility class
 *
 * @author Cannis Meng
 */
public class TesterUtil {

    /**
     * Loads a byte array from a file
     */
    public static byte[] loadBytes(File binFile) throws IOException {
        byte[] bytes = new byte[(int) binFile.length()];
        InputStream is = null;
        try {
            is = new FileInputStream(binFile);
            is.read(bytes);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return bytes;
    }

    /**
     * Writes a byte array to a file
     */
    public static void writeBytes(byte[] bytes, File binFile) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(binFile);
            os.write(bytes);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * Loads a DOM document from a file
     */
    public static Document loadDocument(File docFile)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory domFactory
            = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        return builder.parse(docFile);
    }
}
