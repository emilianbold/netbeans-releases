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
 */
public class ValidateXMLSupport extends SharedXMLSupport implements ValidateXMLCookie {
            
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

