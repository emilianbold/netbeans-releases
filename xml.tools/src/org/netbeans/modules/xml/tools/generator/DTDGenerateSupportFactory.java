/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
        
        //TODO: Retouche
//        if ( clazz.isAssignableFrom (SAXGeneratorSupport.class) ) {
//            return new SAXGeneratorSupport (this.dataObject);
//        } else if ( clazz.isAssignableFrom (GenerateDOMScannerSupport.class) ) {
//            return new GenerateDOMScannerSupport (this.dataObject);
//        }
//        
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
