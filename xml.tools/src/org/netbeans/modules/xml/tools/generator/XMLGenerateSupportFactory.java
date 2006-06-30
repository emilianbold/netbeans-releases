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
        if ( clazz.isAssignableFrom (GenerateDTDSupport.class) ) {
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
