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

import java.io.IOException;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.api.xml.services.UserCatalog;

/**
 * Perform Transform action on XML document.
 * Default implementation of {@link TransformableCookie} cookie.
 *
 * @author     Libor Kramolis
 * @deprecated XML tools SPI candidate
 */
public final class TransformableSupport implements TransformableCookie {
    /** SAX feature: Perform namespace processing. */
    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces"; // NOI18N

    // associated data object
    private final DataObject dataObject;
    /** cached TransformerFactory instance. */
    private static TransformerFactory transformerFactory;
    /** cached SAXParserFactory instance. */
    private static SAXParserFactory saxParserFactory;
    
    
    /** 
     * Create new TransformableSupport for given data object.
     * @param dataObject supported data object
     */    
    public TransformableSupport (DataObject dataObject) {
        this.dataObject = dataObject;
    }

    /**
     * Transform this object by XSL Transformation.
     *
     * @param transformSource source of transformation.
     * @param outputResult result of transformation.
     * @param listener optional listener (<code>null</code> allowed)
     *                 giving judgement details.
     * @throws TransformerException if an unrecoverable error occurs during the course of the transformation
     */
    public void transform (Source transformSource, Result outputResult, CookieObserver notifier) throws TransformerException {
        try {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TransformableSupport.transform");
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("   transformSource = " + transformSource.getSystemId());
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("   outputResult = " + outputResult.getSystemId());

            URL url = dataObject.getPrimaryFile().getURL();
            Source xmlSource = createSource (url.toExternalForm());

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("   xmlSource = " + xmlSource.getSystemId());

            // prepare transformer == parse stylesheet, errors may occur
            Transformer transformer = newTransformer (transformSource);
            
            // transform
            if (notifier != null) {
                Proxy proxy = new Proxy (notifier);
                transformer.setErrorListener (proxy);
            }
            transformer.transform (xmlSource, outputResult);
            
        } catch (Exception exc) { // TransformerException, ParserConfigurationException, SAXException, FileStateInvalidException
            TransformerException transExcept = null;
            CookieObserver.Message message = null;

            if ( notifier != null ) {
                message = new CookieObserver.Message
                    (exc.getLocalizedMessage(), 
                     CookieObserver.Message.FATAL_ERROR_LEVEL);
            }

            if ( exc instanceof TransformerException ) {
                transExcept = (TransformerException)exc;

                if ( message != null ) {
                    message.addDetail (transExcept);
                }
            } else if ( exc instanceof SAXParseException ) {
                transExcept = new TransformerException (exc);

                if ( message != null ) {
                    message.addDetail ((SAXParseException)exc);
                }
            } else {
                transExcept = new TransformerException (exc);

                if ( message != null ) {
                    message.addDetail (transExcept);
                }
            }

            if (notifier != null) {
                notifier.receive (message);
            }
            
            throw transExcept;
        }                
    }

    //
    // utils
    //

    private static URIResolver getURIResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        URIResolver res = (catalog == null ? null : catalog.getURIResolver());
        return res;
    }

    private static EntityResolver getEntityResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
        return res;
    }
    
    private static TransformerFactory getTransformerFactory () {
        if ( transformerFactory == null ) {
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver (getURIResolver()); //!!! maybe that it should be set every call if UsersCatalog instances are dynamic
        }
        return transformerFactory;
    }


    private static Transformer newTransformer (Source xsl) throws TransformerConfigurationException {
        return getTransformerFactory().newTransformer (xsl);
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


    /**
     *
     * @throws ParserConfigurationException if a parser cannot
     *         be created which satisfies the requested configuration.
     * @throws SAXException if a parser cannot be created which satisfies the requested configuration.
     */
    private static Source createSource (String systemId) throws ParserConfigurationException, SAXException {
        XMLReader reader = newXMLReader();
        reader.setEntityResolver (getEntityResolver());
        Source source = new SAXSource (reader, new InputSource (systemId));

        return source;
    }


    //
    // class Proxy
    //

    private static class Proxy implements ErrorListener {
        
        private final CookieObserver peer;
        
        public Proxy (CookieObserver peer) {
            if (peer == null) {
                throw new NullPointerException();
            }
            this.peer = peer;
        }
        
        public void error (TransformerException tex) throws javax.xml.transform.TransformerException {
            CookieObserver.Message message = new CookieObserver.Message(
                tex.getLocalizedMessage(), 
                CookieObserver.Message.ERROR_LEVEL
            );            
            message.addDetail(new DefaultXMLProcessorDetail(tex));
            peer.receive (message);
        }
        
        public void fatalError(TransformerException tex) throws javax.xml.transform.TransformerException {
            CookieObserver.Message message = new CookieObserver.Message(
                tex.getLocalizedMessage(), 
                CookieObserver.Message.FATAL_ERROR_LEVEL
            );            
            message.addDetail(new DefaultXMLProcessorDetail(tex));
            peer.receive (message);            
        }
        
        public void warning(TransformerException tex) throws javax.xml.transform.TransformerException {
            CookieObserver.Message message = new CookieObserver.Message(
                tex.getLocalizedMessage(), 
                CookieObserver.Message.WARNING_LEVEL
            );            
            message.addDetail(new DefaultXMLProcessorDetail(tex));
            peer.receive (message);            
        }
        
    } // class Proxy
    
}
