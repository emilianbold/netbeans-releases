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

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider;
import org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.AbstractLookup.Content;

public class WSDLExtLookupProvider implements WSDLLookupProvider, Lookup.Provider {

    private Lookup lookup;
    private InstanceContent content;
    
    public WSDLExtLookupProvider() {
        content = new InstanceContent();
        lookup = new AbstractLookup(content);
        content.add(new NewCustomizerProvider() {
        
            public Property getProperty(ExtensibilityElement element,
                    QName elementQName, QName attributeQName, boolean isOptional) {
                if (elementQName.equals(BPELQName.PROPERTY_ALIAS.getQName())) {
                    if (attributeQName.getLocalPart().equals("propertyName")) {
                        try {
                            return new PropertyNameProperty(new ExtensibilityElementPropertyAdapter(element, "propertyName", isOptional), String.class, "getValue", "setValue");
                        } catch (NoSuchMethodException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        
        });
    }

    protected Content getContent() {
        return content;
    }
    
    public Provider getProvider(String namespace) {
        if (namespace.equals(BPELQName.VARPROP_NS)) {
            return this;
        }
        return null;
    }

    

    public Lookup getLookup() {
        return lookup;
    }

}
