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

/*
 * XMLFactoryUtil.java
 *
 * Created on October 12, 2001, 5:08 PM
 */

package org.netbeans.xtest.util;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;

/**
 *
 * @author  mb115822
 */
public class XMLFactoryUtil {
    
    private static final String[] names = new String[] {};
        //{"javax.xml.parsers.DocumentBuilderFactory",
         //"javax.xml.parsers.SAXParserFactory", 
         //"org.apache.xerces.xni.parser.XMLParserConfiguration",
         //"org.xml.sax.driver",
         //"javax.xml.transform.TransformerFactory"};
    private static final String[] values = new String[] {};
        //{"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl",
         //"org.apache.xerces.jaxp.SAXParserFactoryImpl",
         //"org.apache.xerces.parsers.StandardParserConfiguration",
         //"org.apache.xerces.parsers.SAXParser",
         //"org.apache.xalan.processor.TransformerFactoryImpl"};
    
    private static String[] setNewProperties() {
        
        String oldValues[]=new String[names.length];
        for (int i=0; i<names.length; i++) 
            oldValues[i]=System.setProperty(names[i], values[i]);
         
        return oldValues;
         
    }
    
    private static void setOriginalProperties(String[] oldValues) {
        for (int i=0; i<names.length; i++)
            if (oldValues[i]==null)
                System.getProperties().remove(names[i]);
            else
                System.setProperty(names[i], oldValues[i]);
        
    }

    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        String[] oldProperties = setNewProperties();
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } finally {
            setOriginalProperties(oldProperties);
        }
    }
        
    public static Transformer newTransformer() throws TransformerConfigurationException {
        String[] oldProperties = setNewProperties();
        try {
            return  (TransformerFactory.newInstance()).newTransformer();
        } finally {
            setOriginalProperties(oldProperties);
        }
    }

    public static Transformer newTransformer(StreamSource xsltSource) throws TransformerConfigurationException {
        String[] oldProperties = setNewProperties();
        try {
            return  (TransformerFactory.newInstance()).newTransformer(xsltSource);
        } finally {
            setOriginalProperties(oldProperties);
        }
    }
    
}
