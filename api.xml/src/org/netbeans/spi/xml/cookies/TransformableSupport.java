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

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

import org.netbeans.api.xml.cookies.TransformableCookie;
import org.netbeans.api.xml.parsers.ProcessorNotifier;

/**
 * Perform Transform action on XML document.
 * Default implementation of TransformableCookie cookie.
 *
 * @author  Libor Kramolis
 * @deprecated XML tools SPI candidate
 */
public final class TransformableSupport implements TransformableCookie {
    // associated data object
    private final DataObject dataObject;
    /** cached TransformerFactory instance. */
    private static TransformerFactory factory;
    
    
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
     */
    public void transform (Source transformSource, Result outputResult, ProcessorNotifier notifier)
            throws IOException, TransformerConfigurationException, TransformerException {
        URL url = dataObject.getPrimaryFile().getURL();
        Source xmlSource = new StreamSource (url.toExternalForm());

        // prepare transformer == parse stylesheet, errors may occure
        
        try {
            Transformer transformer = newTransformer (transformSource);
            
            // transform

            if (notifier != null) {
                Proxy proxy = new Proxy(notifier);
                transformer.setErrorListener(proxy);
            }
            transformer.transform (xmlSource, outputResult);
            
        } catch (TransformerConfigurationException ex) {
            // thrown if error in style sheet, unwrap first TransformerException
            TransformerException tException = ex;
            do {                
                Throwable wrapped = tException.getException();
                if (wrapped instanceof TransformerException) {
                    tException = (TransformerException) wrapped;
                } else {
                    tException = null;
                }
            } while (tException instanceof TransformerConfigurationException);
            
            if (tException instanceof TransformerException) {
                if (notifier != null) {
                    ProcessorNotifier.Message message = new ProcessorNotifier.Message(tException);
                    notifier.fatalError(message);
                }
            } else {
                if (notifier != null) {
                    notifier.message(ex.getLocalizedMessage());
                }
                
            }
        }                
    }

    
    private static TransformerFactory getTransformerFactory () {
        if ( factory == null ) {
            factory = TransformerFactory.newInstance();
        }
        return factory;
    }


    private static Transformer newTransformer (Source xsl) throws TransformerConfigurationException {
        return getTransformerFactory().newTransformer (xsl);
    }

    private static class Proxy implements ErrorListener {
        
        private final ProcessorNotifier peer;
        
        public Proxy (ProcessorNotifier peer) {
            if (peer == null) throw new NullPointerException();
            this.peer = peer;
        }
        
        public void error(TransformerException tex) throws javax.xml.transform.TransformerException {
            ProcessorNotifier.Message message = new ProcessorNotifier.Message(tex);
            peer.error(message);
        }
        
        public void fatalError(TransformerException tex) throws javax.xml.transform.TransformerException {
            ProcessorNotifier.Message message = new ProcessorNotifier.Message(tex);
            peer.fatalError(message);            
        }
        
        public void warning(TransformerException tex) throws javax.xml.transform.TransformerException {
            ProcessorNotifier.Message message = new ProcessorNotifier.Message(tex);
            peer.warning(message);            
        }
        
    }
    
}
