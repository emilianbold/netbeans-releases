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

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak
 */
public final class FolderPositionControllerFactory {
    //TODO This Controllers could be singeltons !!
    public static InspectorPositionController createHierarchical(){
        return new InspectorPositionController() {
            public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
                if (path.getPath().isEmpty())
                    return false;
                
                int parentIndex = path.getPath().size() - 1;
                
                if (path.getPath().get(parentIndex).getComponentID() != null && folder.getComponentID() != null ) {

                    long parentComponentID = path.getPath().get(parentIndex).getComponentID();
                    DesignComponent parentComponent = component.getDocument().getComponentByUID(parentComponentID);
                    
                    if (parentComponent.getType() != path.getLastElement().getTypeID())
                        return false;
                    
                    for (DesignComponent childComponent : parentComponent.getComponents()) {
                        if (childComponent.getComponentID() == folder.getComponentID())
                            return true;
                    }
                }
                
                
                return false;
            }
        };
    }
    
    public static InspectorPositionController createReference(final String propertyName){
        return new InspectorPositionController() {
            
            public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
                if (path.getPath().isEmpty())
                    return false;
                
                int parentIndex = path.getPath().size() - 1;
                
                if (path.getPath().get(parentIndex).getComponentID() != null && folder.getComponentID() != null ) {
                    long parentComponentID = path.getPath().get(parentIndex).getComponentID().longValue();
                    DesignComponent parentComponent = component.getDocument().getComponentByUID(parentComponentID);
                    
                    if (propertyName == null){
                        for (PropertyDescriptor pd : parentComponent.getComponentDescriptor().getPropertyDescriptors()){
                            if (pd.getType().getKind() == TypeID.Kind.COMPONENT) {
                                DesignComponent referedComponent = parentComponent.readProperty(pd.getName()).getComponent();
                                if (referedComponent != null && referedComponent.getComponentID() == folder.getComponentID())
                                    return true;
                            }
                        }
                    } else if (parentComponent.getComponentDescriptor().getPropertyDescriptor(propertyName) != null) {
                        DesignComponent referedComponent  = parentComponent.readProperty(propertyName).getComponent();
                        if (referedComponent  != null && referedComponent.getComponentID() == folder.getComponentID())
                            return true;
                    }
                }
                
                return false;
            }
        };
    }
    
}
