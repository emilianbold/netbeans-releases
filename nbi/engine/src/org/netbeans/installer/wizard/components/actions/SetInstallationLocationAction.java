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
 *  
 * $Id$
 */
package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;

/**
 *
 * @author Kirill Sorokin
 */
public class SetInstallationLocationAction extends DefaultWizardAction {
    public void execute() {
        String uid = getProperty(SOURCE_COMPONENT_UID_PROPERTY);
        String relativeLocation = getProperty(RELATIVE_LOCATION_PROPERTY);
        
        if (uid == null) {
            ErrorManager.notify(ErrorLevel.ERROR, "Required property not set");
            return;
        }
        
        ProductComponent targetComponent = getWizard().getProductComponent();
        ProductComponent sourceComponent = targetComponent.getRequirementByUid(uid);
        
        if (sourceComponent == null) {
            ErrorManager.notify(ErrorLevel.ERROR, "Component with the given uid does not exist");
            return;
        }
        
        File newLocation;
        if (relativeLocation != null) {
            newLocation = new File(sourceComponent.getInstallationLocation(), relativeLocation);
        } else {
            newLocation = sourceComponent.getInstallationLocation();
        }
        
        targetComponent.setInstallationLocation(newLocation.getAbsoluteFile());
    }
    
    public void cancel() {
        // does nothing
    }
    
    private static final String SOURCE_COMPONENT_UID_PROPERTY = "source.component";
    private static final String RELATIVE_LOCATION_PROPERTY = "relative.location";
}
