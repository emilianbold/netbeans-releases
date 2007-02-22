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
 * PropertyViewFactory.java
 *
 * Created on January 29, 2007, 6:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.property.view;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.property.model.PropertyModelException;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public abstract class PropertyViewFactory {
    
    private static PropertyViewFactory mInstance;
    
    /** Creates a new instance of PropertyViewFactory */
    public PropertyViewFactory() {
    }
    
    public static synchronized PropertyViewFactory getInstance() throws PropertyModelException {
        if (null == mInstance) {
            String fac = System.getProperty(PropertyViewFactory.class.getName(),
                    "org.netbeans.modules.xml.wsdl.ui.property.view.impl.PropertyViewFactoryImpl");//NOI18N
            try {
                mInstance = (PropertyViewFactory) Class.forName(fac).newInstance();
            } catch (Exception e) {
                throw new PropertyModelException(
                        NbBundle.getMessage(PropertyViewFactory.class, "ERR_MSG_PropertyViewFactory_CLASS_NOT_FOUND", fac), e);
            }
        }
        return mInstance;
    }
    
    /**
     * Get all the property set for a given extensibility element
     */
    public abstract Sheet.Set[] getPropertySets(ExtensibilityElement exElement, QName elementQName, Element schemaElement);
}
