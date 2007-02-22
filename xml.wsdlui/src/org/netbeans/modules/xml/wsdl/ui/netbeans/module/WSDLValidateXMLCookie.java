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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
import org.netbeans.modules.xml.validation.ValidateXMLCookieImpl;
import org.netbeans.modules.xml.xam.Model;
import org.openide.ErrorManager;

/**
 * Implements the ValidateXMLCookie cookie for WSDL models.
 *
 * @author  Nathan Fiedler
 */
public class WSDLValidateXMLCookie extends ValidateXMLCookieImpl {
    private WSDLDataObject dataObject;

    /**
     * Creates a new instance of WSDLValidateXMLCookie.
     */
    public WSDLValidateXMLCookie(WSDLDataObject dobj) {
        dataObject = dobj;
    }

    protected Model getModel() {
        try {
            return dataObject.getWSDLEditorSupport().getModel();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return null;
    }
}
