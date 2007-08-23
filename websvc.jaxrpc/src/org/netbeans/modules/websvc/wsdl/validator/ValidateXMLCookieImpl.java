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

package org.netbeans.modules.websvc.wsdl.validator;

import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.loaders.DataObject;

/**
 *
 * @author rico
 */
public class ValidateXMLCookieImpl implements ValidateXMLCookie{
    private DataObject dobj;
    
    /** Creates a new instance of ValidateXMLCookieImpl */
    public ValidateXMLCookieImpl(DataObject dobj) {
        this.dobj = dobj;
    }

    public boolean validateXML(CookieObserver observer) {
        ValidationOutputWindowController validationController =
                new ValidationOutputWindowController();
        validationController.validate(getModel());
        return true;
    }
    
    private Model getModel(){
	ModelSource modelSource = Utilities.getModelSource(dobj.getPrimaryFile(), 
			true);
	return WSDLModelFactory.getDefault().getModel(modelSource);
    }
    
}
