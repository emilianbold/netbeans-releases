/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.xml.cookies;

import java.io.*;
import java.net.*;
import java.util.*;

import org.xml.sax.*;

import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.util.*;

import org.netbeans.api.xml.parsers.DocumentInputSource;
import org.netbeans.api.xml.services.UserCatalog;

/**
 * Adapt <code>DataObject</code> to other common XML interfaces.
 *
 * @author      Petr Kuzel
 * @deprecated  XML tools SPI candidate
 * @since       0.9
 */
public final class DataObjectAdapters {
    
    /** SAX feature: Perform namespace processing. */
    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces"; // NOI18N
    
    /** cached SAXParserFactory instance. */
    private static SAXParserFactory saxParserFactory;
    
    private  DataObjectAdapters() {
    }
    
    /**
     * Create InputSource from DataObject. Default implementation prefers opened
     * Swing <code>Document</code> over primary file URL.
     * @throws IOException if I/O error occurs.
     * @return InputSource never <code>null</code>
     */           
    public static InputSource inputSource(DataObject dataObject) throws IOException {
        return new DataObjectInputSource(dataObject);
    }

    /**
     *
     */
    private static class DataObjectInputSource extends InputSource {
        
        private final DataObject dataObject;
        private final String systemId;
        
        public DataObjectInputSource(DataObject dataObject) throws IOException {
            this.dataObject = dataObject;
            this.systemId = dataObject.getPrimaryFile().getURL().toExternalForm();
        }
                
        public String getSystemId() {
            return systemId;
        }
        
        public Reader getCharacterStream() {

            EditorCookie editor = (EditorCookie) dataObject.getCookie(EditorCookie.class);

            if (editor != null) {
                Document doc = editor.getDocument();
                if (doc != null) {
                    return  new DocumentInputSource(doc).getCharacterStream();
                }
            }             
            
            return null;
        }
        
    }
    
    /**
     * Create Source from DataObject. Default implementation prefers opened
     * Swing <code>Document</code> over primary file URL.
     * @throws IOException if I/O error occurs.
     * @return InputSource never <code>null</code>
     */               
    public static Source source(DataObject dataObject) throws IOException {
        try {
            URL url = dataObject.getPrimaryFile().getURL();
            String systemId = url.toExternalForm();

            XMLReader reader = newXMLReader();
            reader.setEntityResolver (getEntityResolver());
            Source source = new SAXSource (reader, inputSource (dataObject));

            return source;        
        } catch (ParserConfigurationException ex) {
            throw new IOException();
        } catch (SAXNotRecognizedException ex) {
            throw new IOException();
        } catch (SAXNotSupportedException ex) {
            throw new IOException();
        } catch (SAXException ex) {
            throw new IOException();
        }
    }
    
    private static SAXParserFactory getSAXParserFactory () throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if ( saxParserFactory == null ) {
            saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature (SAX_FEATURES_NAMESPACES, true);
        }
        return saxParserFactory;
    }

    /**
     *
     * @throws ParserConfigurationException if a parser cannot
     *         be created which satisfies the requested configuration.
     * @throws SAXException if a parser cannot be created which satisfies the requested configuration.
     */
    private static XMLReader newXMLReader () throws ParserConfigurationException, SAXException {
        SAXParser parser = getSAXParserFactory().newSAXParser();
        return parser.getXMLReader();
    }
    
    private static EntityResolver getEntityResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
        return res;
    }        
}
