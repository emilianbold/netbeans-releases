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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.validation;

import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.xam.Model;

/**
 * Default implementation of ValidateXmlCookie.
 * @author Praveen Savur
 */
public abstract class ValidateXMLCookieImpl implements ValidateXMLCookie {
    
    private Model model;
    
    /**
     * Creates a new instance of ValidateXMLCookieImpl.
     */
    public ValidateXMLCookieImpl() {
    }

    /**
     * Implement Validate XML action.
     * @param cookieObserver Optional listener. This is unused in this implementation.
     * @return This implementation always returns true.
     */
    public boolean validateXML(CookieObserver cookieObserver) {
        ValidationOutputWindowController validationController =
                new ValidationOutputWindowController();
        validationController.validate(getModel());
        return true;
    }

    
    /**
     * Retrieve the model that will be validated.
     *
     * @return  model to validate.
     */
    protected abstract Model getModel();
    
}
