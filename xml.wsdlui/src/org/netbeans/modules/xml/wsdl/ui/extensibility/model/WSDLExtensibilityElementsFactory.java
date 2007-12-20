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

/*
 * Created on May 25, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.WSDLExtensibilityElementsFactoryImpl;
import org.openide.util.NbBundle;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class WSDLExtensibilityElementsFactory {
    
    private static WSDLExtensibilityElementsFactory mInstance;
    
    public static synchronized WSDLExtensibilityElementsFactory getInstance() throws Exception {
        if (null == mInstance) {
            String fac = System.getProperty(WSDLExtensibilityElementsFactory.class.getName(),
                    "org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.WSDLExtensibilityElementsFactoryImpl");//NOI18N
            try {
                mInstance = (WSDLExtensibilityElementsFactory) Class.forName(fac).newInstance();
            } catch (Exception e) {
                throw new Exception(
                        NbBundle.getMessage(WSDLExtensibilityElementsFactory.class, "ERR_MSG_WSDLExtensibilityElementsFactory_CLASS_NOT_FOUND", fac), e);
            }
        }
        return mInstance;
        
        
    }
    
    public abstract WSDLExtensibilityElements getWSDLExtensibilityElements();
}
