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
     * Create new TransformableSupport for given data object in DOCUMENT_MODE.
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

        Transformer transformer = newTransformer (transformSource);
//        transformer.setErrorListener (ErrorListener listener); //!!! notitier ???
        transformer.transform (xmlSource, outputResult);
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
    
}
