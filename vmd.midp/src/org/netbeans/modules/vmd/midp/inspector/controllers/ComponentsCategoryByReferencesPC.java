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

package org.netbeans.modules.vmd.midp.inspector.controllers;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.Debug;

/**
 *
 * @author Karol Harezlak
 */

public final class ComponentsCategoryByReferencesPC implements InspectorPositionController {

    private TypeID typeID;
    private String propertyName;
    
    public ComponentsCategoryByReferencesPC(TypeID typeID, String propertyName) {
        if (typeID == null)
            throw Debug.error ("Argument typID cant be null"); //NOI18N
        
        this.typeID = typeID;
        this.propertyName = propertyName;
    }
    
    public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
        if (path.getPath().size() < 2 || path.getLastElement().getTypeID() != typeID)
            return false;
        
        int referencedIndex = path.getPath().size() - 2;
        long referencedComponentID = path.getPath().get(referencedIndex).getComponentID();
        
        DesignComponent referencedComponent = component.getDocument().getComponentByUID(referencedComponentID).readProperty(propertyName).getComponent();
        if (referencedComponent != null && component  == referencedComponent)
            return true;

        return false;
    }
    
}
