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

import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.core.cookies.CookieFactory;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class XMLGenerateSupportFactory extends CookieFactory {
    /** */
    private final XMLDataObject dataObject;
    /** */
    private static final Class[] SUPPORTED_COOKIES = new Class[] {
        GenerateDTDSupport.class,        
    };


    /** Create new XMLGenerateSupportFactory. */
    public XMLGenerateSupportFactory (XMLDataObject dataObject) {
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
        if ( GenerateDTDSupport.class.isAssignableFrom (clazz) ) {
            return new GenerateDTDSupport (this.dataObject);
        }

        return null;
    }


    //
    // class Creator
    //

    public static final class Creator implements XMLDataObject.XMLCookieFactoryCreator {

        /**
         */
        public CookieFactory createCookieFactory (DataObject obj) {
            return new XMLGenerateSupportFactory ((XMLDataObject) obj);
        }

    } // end: class Creator

}
