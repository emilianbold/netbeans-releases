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

import org.xml.sax.InputSource;

import org.netbeans.api.xml.cookies.*;

/**
 * <code>ValidateXMLCookie</code> implementation support simplifing cookie 
 * providers based on <code>InputSource</code>s representing XML documents
 * and entities.
 * <p>
 * <b>Primary use case</b> in a DataObject subclass (which primary file is XML):
 * <pre>
 *   CookieSet cookies = getCookieSet();
 *   InputSource in = DataObjectAdapters.inputSource(this);
 *   ValidateXMLSupport cookieImpl = new ValidateXMLSupport(in);
 *   cookies.add(cookieImpl);
 * </pre>
 * <p>
 * <b>Secondary use case:</b> Subclasses can customize the class by customization
 * protected methods. The customized subclass can be used according to
 * primary use case.
 *
 * @author Petr Kuzel
 * @deprecated XML tools SPI candidate
 */
public class ValidateXMLSupport extends TestXMLSupport implements ValidateXMLCookie {
            
    /** 
     * Create new ValidateXMLSupport for given data object. 
     * @param inputSource Supported InputSource.
     */    
    public ValidateXMLSupport(InputSource inputSource) {
        super(inputSource);
    }
    
    // inherit JavaDoc
    public boolean validateXML(CookieObserver l) {
        return super.validateXML(l);
    }
}

