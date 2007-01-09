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
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class SetInstallationLocationAction extends WizardAction {
    public static final String SOURCE_UID_PROPERTY = "source.component";
    public static final String RELATIVE_LOCATION_PROPERTY = "relative.location";
    
    public void execute() {
        String uid              = getProperty(SOURCE_UID_PROPERTY);
        String relativeLocation = getProperty(RELATIVE_LOCATION_PROPERTY);
        
        if (uid == null) {
            ErrorManager.notifyError("Required property not set");
            return;
        }
        
        // we do expect the property container of the wizard to be a product, if 
        // it's not we should fail
        Product target = (Product) getWizard().getProduct();
        Product source = target.getRequirementByUid(uid);
        
        if (source == null) {
            ErrorManager.notifyError("Component with the given uid does not exist");
            return;
        }
        
        File location;
        if (relativeLocation != null) {
            location = new File(source.getInstallationLocation(), relativeLocation);
        } else {
            location = source.getInstallationLocation();
        }
        
        target.setInstallationLocation(location.getAbsoluteFile());
    }
    
    public WizardUi getWizardUi() {
        return null; // we do not have any ui for this action
    }
}
