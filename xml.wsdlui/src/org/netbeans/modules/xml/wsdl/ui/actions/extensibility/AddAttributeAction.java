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
 * AddAttributeAction.java
 *
 * Created on April 25, 2006, 11:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.actions.extensibility;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.actions.CommonAddExtensibilityAttributeAction;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author radval
 */
public class AddAttributeAction extends CommonAddExtensibilityAttributeAction {
    
    /** Creates a new instance of AddAttributeAction */
    public AddAttributeAction() {
    }
    
        
    @Override
    protected Vector<String> getNamespaces(WSDLComponent wsdlComponent) {
        Map<String, String> prefixToNameSpaceMap = Utility.getPrefixes(wsdlComponent);
        Set<String> namespaceSet = new HashSet<String>();
        namespaceSet.addAll(prefixToNameSpaceMap.values());
        
        namespaceSet.remove(Constants.WSDL_DEFAUL_NAMESPACE);
        QName qName = ((AbstractDocumentComponent) wsdlComponent).getQName();
        namespaceSet.remove(qName.getNamespaceURI());
        
        return new Vector<String>(namespaceSet);
    }
}
