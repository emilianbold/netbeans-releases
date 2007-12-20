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

package org.netbeans.modules.xml.wsdl.ui.spi;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class ExtensibilityElementConfiguratorFactory {
    private static Map<QName, ExtensibilityElementConfigurator> configurators;

    public ExtensibilityElementConfiguratorFactory() {
        initialise();
    }

    private void initialise() {
        lookupFactories();
    }
    
    public ExtensibilityElementConfigurator getExtensibilityElementConfigurator(QName qname) {
        if (configurators == null || qname == null) return null;
        return configurators.get(qname);
    }
    
    public Node.Property getNodeProperty(ExtensibilityElement element, QName qname, String attributeName) {
        if (configurators == null) return null;
        
        ExtensibilityElementConfigurator configurator = configurators.get(qname);
        if (configurator != null) {
            return configurator.getProperty(element, qname, attributeName);
        }
        return null;
    }
    
    private synchronized void lookupFactories() {
        if(configurators != null)
            return;
        
        configurators = new HashMap<QName, ExtensibilityElementConfigurator>();
        
        Lookup.Result result = Lookup.getDefault().lookup(
                new Lookup.Template(ExtensibilityElementConfigurator.class));
        
        for(Object obj: result.allInstances()) {
            ExtensibilityElementConfigurator factory = (ExtensibilityElementConfigurator) obj;
            
            for (QName qname : factory.getSupportedQNames()) {
                if (configurators.containsKey(qname)) {
                    ErrorManager.getDefault().notify(new Exception("There is a ExtensibilityConfigurator already present for this "  + qname.toString()));
                }
                configurators.put(qname, factory);
            }
        }
        
    }
}
