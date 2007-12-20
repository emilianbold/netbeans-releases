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
 * PropertyModelFactory.java
 *
 * Created on January 23, 2007, 5:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.openide.util.NbBundle;


/**
 *
 * 
 */
public abstract class PropertyModelFactory {
    
    public static final String PROP_NAMESPACE = "http://xml.netbeans.org/schema/wsdlui/property";
    
    private static PropertyModelFactory mInstance;
    
    /** Creates a new instance of PropertyModelFactory */
    public PropertyModelFactory() {
    }
    
    public static synchronized PropertyModelFactory getInstance() throws PropertyModelException {
        if (null == mInstance) {
            String fac = System.getProperty(PropertyModelFactory.class.getName(),
                    "org.netbeans.modules.xml.wsdl.ui.property.model.impl.PropertyModelFactoryImpl");//NOI18N
            try {
                mInstance = (PropertyModelFactory) Class.forName(fac).newInstance();
            } catch (Exception e) {
                throw new PropertyModelException(
                        NbBundle.getMessage(PropertyModelFactory.class, "ERR_MSG_PropertyModelFactory_CLASS_NOT_FOUND", fac), e);
            }
        }
        return mInstance;
    }
    
    public abstract ElementProperties getElementProperties(QName elementQName) throws PropertyModelException;
}
