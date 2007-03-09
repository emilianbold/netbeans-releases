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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.extension.bpel;


import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

public class PropertyConfigurator extends
        ExtensibilityElementConfigurator {

    private static QName myQName = BPELQName.PROPERTY.getQName();
    
    private static QName[] supportedQNames = {myQName};

    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Property getProperty(ExtensibilityElement extensibilityElement,
            QName qname, String attributeName) {
        return null;
    }

    @Override
    public String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname) {
        return "name"; //NOI18N
    }

    @Override
    public String getAttributeUniqueValuePrefix(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        if (attributeName.equals("name"))//NOI18N
            return NbBundle.getMessage(PropertyConfigurator.class, "PROPERTY_NAME_PREFIX");
        return null;
    }

    @Override
    public String getDefaultValue(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname) {
        return NbBundle.getMessage(PropertyConfigurator.class, "LBL_Property_TypeDisplayName");
    }

}
