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

package org.netbeans.modules.encoder.ui.tester;

import com.sun.encoder.EncoderConfigurationException;
import com.sun.encoder.EncoderException;
import com.sun.encoder.EncoderType;
import java.io.File;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;



/**
 * An interface that represents encoding test task.
 *
 * @author Cannis Meng
 */
public interface EncoderTestTask {

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
    File decode(EncoderType type, File metaFile, QName rootElement,
            File inputFile, File outputFile, String predecodeCoding,
            boolean charBased) throws EncoderException, IOException,
                    TransformerConfigurationException, TransformerException,
                    EncoderConfigurationException;

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
    File encode(EncoderType type, File metaFile, QName rootElement,
            File xmlFile, File outputFile, String postencodeCoding,
            boolean charBased) throws EncoderException, IOException,
                    ParserConfigurationException, SAXException,
                    EncoderConfigurationException;

}
