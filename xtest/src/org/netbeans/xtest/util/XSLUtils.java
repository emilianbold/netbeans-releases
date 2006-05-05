/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * XSLUtils.java
 *
 * Created on June 4, 2002, 4:18 PM
 */

package org.netbeans.xtest.util;

// 
import java.io.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
// xsl 
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

/**
 *
 * @author  mb115822
 */
public class XSLUtils {
    
   // only static utils
   private XSLUtils() {
   }
   
   
   
   
   public static File getXSLFile(File xtestHome, String xslFilename) throws IOException {
       if (!xtestHome.isDirectory()) {
           throw new IOException("xtest home is not set");
       }
       File xslFile = new File(xtestHome,"lib"+File.separator+"xsl"+File.separator+xslFilename);
       if (!xslFile.isFile()) {
           throw new IOException("cannot find xsl file "+xslFile);
       }
       return xslFile;
   }
   
   
   public static Transformer getTransformer(File xsl) throws TransformerConfigurationException {
       StreamSource xslSource = new StreamSource(xsl);
       Transformer transformer = XMLFactoryUtil.newTransformer(xslSource);
       return transformer;
   }
   
   public static Document transform(Transformer transformer, Document xml) throws TransformerException, ParserConfigurationException {
       DOMSource domSource = new DOMSource(xml);
       DOMResult domResult = new DOMResult();
       transform(transformer,domSource,domResult);
       Node aNode = domResult.getNode();       
       Document resultDoc = XMLFactoryUtil.newDocumentBuilder().newDocument();
       resultDoc.appendChild(aNode);
       return resultDoc;
   }
   
   public static void transform(Transformer transformer, Document xml, File outputXML) throws TransformerException, IOException {
       DOMSource domSource = new DOMSource(xml);
       FileOutputStream outputFileStream = new FileOutputStream(outputXML);
       StreamResult streamResult = new StreamResult(outputFileStream);
       transform(transformer,domSource,streamResult);
       outputFileStream.close();
    }
    
    
    public static void transform(Transformer transformer, Source xmlSource, Result outputTarget) throws TransformerException {
        transformer.transform(xmlSource, outputTarget);
    }
    
    
    public static void transform(Transformer transformer, File inputXML, File outputXML) throws TransformerException, IOException {
        StreamSource xmlSource = new StreamSource(inputXML);
        FileOutputStream outputFileStream = new FileOutputStream(outputXML);
        StreamResult xmlResult = new StreamResult(outputFileStream);
        transform(transformer,xmlSource,xmlResult);
        outputFileStream.close();
    }
    
    public static void transform(File xsl, File inputXML, File outputXML) throws TransformerException, IOException {
        Transformer transformer = getTransformer(xsl);
        transform(transformer, inputXML, outputXML);
    }
    
    // here are also some extension to Xalan transformer --- these are used by the XTest's XSL scripts
    
    // replace 'original' string by 'replacement' string in 'input' string
    public static String replaceString(String input, String original, String replacement) {
        int index=0;
        int newIndex;
        StringBuffer resultBuffer = new StringBuffer(input.length());
        while (((newIndex = input.indexOf(original,index)) > -1)&(index < input.length())) {
            if (index < newIndex) {
                resultBuffer.append(input.substring(index,newIndex));
            }
            resultBuffer.append(replacement);
            index = newIndex + original.length();
        }
        
        if (index < input.length()) {
            resultBuffer.append(input.substring(index));
        }
        
        return resultBuffer.toString();
    }

}
