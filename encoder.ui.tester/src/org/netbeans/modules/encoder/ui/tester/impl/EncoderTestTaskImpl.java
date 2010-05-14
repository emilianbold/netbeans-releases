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

import com.sun.encoder.Encoder;
import com.sun.encoder.EncoderConfigurationException;
import com.sun.encoder.EncoderException;
import com.sun.encoder.EncoderFactory;
import com.sun.encoder.EncoderProperties;
import com.sun.encoder.EncoderType;
import com.sun.encoder.util.UnicodeFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ResourceBundle;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.encoder.ui.tester.EncoderTestTask;
import org.xml.sax.SAXException;

/**
 * An implementation of the EncoderTestTask interface
 *
 * @author Cannis Meng, Jun Xu
 */
public class EncoderTestTaskImpl implements EncoderTestTask {

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/tester/impl/Bundle");
    /**
     * Returns an Encoder instance based on specified encoder type, xsd meta
     * file and root element.
     *
     * @param type encoder type.
     * @param xsdFile the xsd meta file.
     * @param rootElement root element.
     * @return an Encoder instance.
     *
     * @throws java.io.FileNotFoundException
     * @throws com.sun.encoder.EncoderConfigurationException
     */
    public Encoder getEncoder(EncoderType type, File xsdFile, QName rootElement)
            throws FileNotFoundException, EncoderConfigurationException {
        if (xsdFile == null || !xsdFile.exists()) {
            throw new java.io.FileNotFoundException(_bundle.getString("test_task.lbl.no_xsd_file"));
        }
        String metaPath = xsdFile.getAbsolutePath();
        EncoderFactory factory = null;
        Encoder encoder = null;
        factory = EncoderFactory.newInstance();
        encoder = factory.newEncoder(type, factory.makeMeta(metaPath, rootElement));
        return encoder;
    }

    /**
     * Decodes the input file according the xsd meta file and
     * generate a output xml file.
     *
     * @param type encoder type.
     * @param metaFile the xsd meta file.
     * @param rootElement root element for decoding.
     * @param inputFile input file.
     * @param outputFile output file.
     * @param predecodeCoding pre-decode coding.
     * @param charBased whether or not character based.
     * @return output file.
     *
     * @throws com.sun.encoder.EncoderException
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerConfigurationException
     * @throws javax.xml.transform.TransformerException
     * @throws com.sun.encoder.EncoderConfigurationException
     */
    public File decode(EncoderType type, File metaFile, QName rootElement,
            File inputFile, File outputFile, String predecodeCoding,
            boolean charBased) throws EncoderException, IOException,
                    TransformerConfigurationException, TransformerException,
                    EncoderConfigurationException {
        Encoder encoder = null;
        Source decodedXML = null;
        Writer writer = null;

        encoder = getEncoder(type, metaFile, rootElement);
        EncoderProperties properties = new EncoderProperties();
        if (predecodeCoding.length() > 0) {
            properties.setPreDecodeCharCoding(predecodeCoding);
        }
        if (charBased) {
            decodedXML = encoder.decodeFromString(UnicodeFile.getText(inputFile), properties);
        } else {
            decodedXML = encoder.decodeFromBytes(TesterUtil.loadBytes(inputFile), properties);
        }
        writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"); //NOI18N

        StreamResult sResult = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.newTransformer().transform(decodedXML, sResult);


        if (!encoder.dispose()) {
            throw new EncoderException(_bundle.getString("test_task.exp.disposal_of_encoder_failed"));
        }

        return outputFile;
    }

    /**
     * Encodes the xml file with the xsd meta file and generate the encoded
     * output file.
     *
     * @param type encoder type.
     * @param metaFile the xsd meta file.
     * @param rootElement root element for encoding.
     * @param xmlFile xml input file.
     * @param outputFile output file.
     * @param postencodeCoding post-encode coding.
     * @param charBased whether or not character based.
     * @return null.
     *
     * @throws com.sun.encoder.EncoderException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws com.sun.encoder.EncoderConfigurationException
     */
    public File encode(EncoderType type, File metaFile, QName rootElement,
            File xmlFile, File outputFile, String postencodeCoding,
            boolean charBased) throws EncoderException, IOException,
                    ParserConfigurationException, SAXException,
                    EncoderConfigurationException {

        Encoder encoder = getEncoder(type, metaFile, rootElement);
        EncoderProperties properties = new EncoderProperties();
        if (postencodeCoding.length() > 0) {
            properties.setPostEncodeCharCoding(postencodeCoding);
        }
        Source decodedXML;
        decodedXML = new DOMSource(TesterUtil.loadDocument(xmlFile), xmlFile.toString());

        //Does the encoding
        if (charBased) {
            String encodedResult =
                    encoder.encodeToString(decodedXML, properties);
            UnicodeFile.setText(outputFile, encodedResult);
        } else {
            byte[] encodedResult =
                    encoder.encodeToBytes(decodedXML, properties);
            //Writes the encoded result to a file
            TesterUtil.writeBytes(encodedResult, outputFile);
        }

        return null;
    }
}
