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

package org.netbeans.modules.vmd.api.inspector.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak
 */
public class ArrayPropertyOrderingController implements InspectorOrderingController {
    
    private String propertyName;
    private Integer order;
    private TypeID supportedTypeID;
    
    public ArrayPropertyOrderingController(String propertyName, Integer order, TypeID supportedTypeID) {
        this.propertyName = propertyName;
        this.order = order;
        this.supportedTypeID = supportedTypeID;
    }
    
    public List<InspectorFolder> getOrdered(DesignComponent component,Collection<InspectorFolder> folders) { 
         List<InspectorFolder> orderedList = new ArrayList<InspectorFolder>(folders.size());
         List<PropertyValue> array = component.readProperty(propertyName).getArray();
            for (PropertyValue value : array) {
                DesignComponent itemsComponent = value.getComponent();
                for (InspectorFolder f : folders)
                    if (f.getComponentID().equals(itemsComponent.getComponentID())) {
                        orderedList.add(f);
                        break;
                    }
            }
            
            return orderedList ;
    }
    
    public Integer getOrder() {
        return order;
    }
    
    public boolean isTypeIDSupported(DesignDocument document, TypeID typeID) {        
        if (document.getDescriptorRegistry().isInHierarchy(supportedTypeID, typeID))
            return true;
        
        return false;
    }
    
}
