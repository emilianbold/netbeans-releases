/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import org.openide.nodes.Node;
import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.modules.xml.core.cookies.CookieFactory;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class DTDGenerateSupportFactory extends CookieFactory {
    /** */
    private final DTDDataObject dataObject;
    /** */
    private static final Class[] SUPPORTED_COOKIES = new Class[] {
        SAXGeneratorSupport.class,
        GenerateDOMScannerSupport.class,
    };


    /** Create new DTDGenerateSupportFactory. */
    public DTDGenerateSupportFactory (DTDDataObject dataObject) {
        this.dataObject = dataObject;
    }


    /**
     */
    protected Class[] supportedCookies () {
        return SUPPORTED_COOKIES;
    }

    /**
     */
    public Node.Cookie createCookie (Class clazz) {
        if ( SAXGeneratorSupport.class.isAssignableFrom (clazz) ) {
            return new SAXGeneratorSupport (this.dataObject);
        } else if ( GenerateDOMScannerSupport.class.isAssignableFrom (clazz) ) {
            return new GenerateDOMScannerSupport (this.dataObject);
        }

        return null;
    }


    //
    // class Creator
    //

    public static final class Creator implements DTDDataObject.DTDCookieFactoryCreator {

        /**
         */
        public CookieFactory createCookieFactory (DataObject obj) {
            return new DTDGenerateSupportFactory ((DTDDataObject) obj);
        }

    } // end: class Creator

}
