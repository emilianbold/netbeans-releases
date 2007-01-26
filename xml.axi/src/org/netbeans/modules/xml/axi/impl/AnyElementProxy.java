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
package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;

/**
 * Proxy for an AnyElement, acts on behalf of an AnyElement.
 * Delegates all calls to the original AnyElement.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AnyElementProxy extends AnyElement implements AXIComponentProxy {
    
    /**
     * Creates an AnyElementProxy.
     */
    public AnyElementProxy(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
        
    public ComponentType getComponentType() {
        return ComponentType.PROXY;
    }
    
    private AnyElement getShared() {
        return (AnyElement)getSharedComponent();
    }
    
    public String getTargetNamespace() {
        return getShared().getTargetNamespace();
    }

    public void setTargetNamespace(String value) {
        getShared().setTargetNamespace(value);
    }
    
    public ProcessContents getProcessContents() {
        return getShared().getProcessContents();
    }
    
    public void setProcessContents(ProcessContents value) {
        getShared().setProcessContents(value);
    }
       
    public String toString() {
        return getShared().toString();
    }    
}
