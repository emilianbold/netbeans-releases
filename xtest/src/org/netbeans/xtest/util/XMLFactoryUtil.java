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
 * XMLFactoryUtil.java
 *
 * Created on October 12, 2001, 5:08 PM
 */

package org.netbeans.xtest.util;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.apache.xalan.processor.TransformerFactoryImpl;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;

/**
 *
 * @author  mb115822
 */
public class XMLFactoryUtil {
    
    private static final String[] names = new String[]
        {"javax.xml.parsers.DocumentBuilderFactory",
         "javax.xml.parsers.SAXParserFactory", 
         "org.apache.xerces.xni.parser.XMLParserConfiguration",
         "org.xml.sax.driver",
         "javax.xml.transform.TransformerFactory"};
    private static final String[] values = new String[]
        {"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl",
         "org.apache.xerces.jaxp.SAXParserFactoryImpl",
         "org.apache.xerces.parsers.StandardParserConfiguration",
         "org.apache.xerces.parsers.SAXParser",
         "org.apache.xalan.processor.TransformerFactoryImpl"};
    
    private static String[] setNewProperties() {
        
        String oldValues[]=new String[names.length];
        /*for (int i=0; i<names.length; i++) 
            oldValues[i]=System.setProperty(names[i], values[i]);
         */
        return oldValues;
    }
    
    private static void setOriginalProperties(String[] oldValues) {
        /*for (int i=0; i<names.length; i++)
            if (oldValues[i]==null)
                System.getProperties().remove(names[i]);
            else
                System.setProperty(names[i], oldValues[i]);
         */
    }

    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        String[] oldProperties = setNewProperties();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(DocumentBuilderFactoryImpl.class.getClassLoader());
            return new DocumentBuilderFactoryImpl().newDocumentBuilder();
        } finally {
            setOriginalProperties(oldProperties);
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }
        
    public static Transformer newTransformer() throws TransformerConfigurationException {
        String[] oldProperties = setNewProperties();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(TransformerFactoryImpl.class.getClassLoader());
            return new NestedTransformerImpl(new TransformerFactoryImpl().newTransformer());
        } finally {
            setOriginalProperties(oldProperties);
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }

    public static Transformer newTransformer(StreamSource xsltSource) throws TransformerConfigurationException {
        String[] oldProperties = setNewProperties();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(TransformerFactoryImpl.class.getClassLoader());
            return new NestedTransformerImpl(new TransformerFactoryImpl().newTransformer(xsltSource));
        } finally {
            setOriginalProperties(oldProperties);
            Thread.currentThread().setContextClassLoader(contextLoader);
        }
    }
    
    public static class NestedTransformerImpl extends Transformer {
        
        private Transformer trans;
        
        public NestedTransformerImpl(Transformer trans) {
            this.trans=trans;
        }
        
        public void clearParameters() {
            trans.clearParameters();
        }
        
        public ErrorListener getErrorListener() {
            return trans.getErrorListener();
        }
        
        public Properties getOutputProperties() {
            return trans.getOutputProperties();
        }
        
        public String getOutputProperty(String str) throws IllegalArgumentException {
            return trans.getOutputProperty(str);
        }
        
        public Object getParameter(String str) {
            return trans.getParameter(str);
        }
        
        public URIResolver getURIResolver() {
            return trans.getURIResolver();
        }
        
        public void setErrorListener(ErrorListener errorListener) throws IllegalArgumentException {
            trans.setErrorListener(errorListener);
        }
        
        public void setOutputProperties(Properties properties) throws IllegalArgumentException {
            trans.setOutputProperties(properties);
        }
        
        public void setOutputProperty(String str, String str1) throws IllegalArgumentException {
            trans.setOutputProperty(str, str1);
        }
        
        public void setParameter(String str, Object obj) {
            trans.setParameter(str, obj);
        }
        
        public void setURIResolver(URIResolver uRIResolver) {
            trans.setURIResolver(uRIResolver);
        }

        public void transform(Source source, Result result) throws TransformerException {
            String[] oldProperties = setNewProperties();
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            try {
                
                // totally ugly hack of transformation to get work on JDK 1.4.2
                String val=trans.getOutputProperty("{http://xml.apache.org/xalan}content-handler");
                if (val!=null) trans.setOutputProperty("{http://xml.apache.org/xslt}content-handler", val);
                val=trans.getOutputProperty("{http://xml.apache.org/xalan}entities");
                if (val!=null) trans.setOutputProperty("{http://xml.apache.org/xslt}entities", val);
                val=trans.getOutputProperty("{http://xml.apache.org/xalan}indent-amount");
                if (val!=null) trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", val);
                                
                Thread.currentThread().setContextClassLoader(DocumentBuilderFactoryImpl.class.getClassLoader());
                trans.transform(source, result);
            } finally {
                setOriginalProperties(oldProperties);
                Thread.currentThread().setContextClassLoader(contextLoader);
            }
        }
        
    }
}
