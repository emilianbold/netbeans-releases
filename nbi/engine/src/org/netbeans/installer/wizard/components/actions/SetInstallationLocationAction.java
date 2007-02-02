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
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class SetInstallationLocationAction extends WizardAction {
    public static final String SOURCE_UID_PROPERTY = "source.component";
    public static final String RELATIVE_LOCATION_PROPERTY = "relative.location";
    
    public void execute() {
        final String uid              = getProperty(SOURCE_UID_PROPERTY);
        final String relativeLocation = getProperty(RELATIVE_LOCATION_PROPERTY);
        
        if (uid == null) {
            ErrorManager.notifyError("Required property not set");
            return;
        }
        
        // we do expect the property container of the wizard to be a product, if
        // it's not we should fail
        final Product target = (Product) getWizard().getContext().get(Product.class);
        
        final List<Dependency> dependencies = target.getDependencyByUid(uid);
        final Product source =
                Registry.getInstance().getProducts(dependencies.get(0)).get(0);
        
        if (source == null) {
            ErrorManager.notifyError("Component with the given uid does not exist");
            return;
        }
        
        File sourceLocation = null;
        try {
            if (SystemUtils.isMacOS() && source.getLogic().wrapForMacOs()) {
                sourceLocation = new File(
                        source.getInstallationLocation(), 
                        "Contents/Resources/" + 
                        source.getInstallationLocation().getName().replaceAll("\\.app$",""));
            } else {
                sourceLocation = source.getInstallationLocation();
            }
        } catch (InitializationException e) {
            ErrorManager.notifyError("Could not access configuration logic", e);
        }
        
        final File location;
        if (relativeLocation != null) {
            location = new File(sourceLocation, relativeLocation);
        } else {
            location = sourceLocation;
        }
        
        target.setInstallationLocation(location.getAbsoluteFile());
    }
    
    public WizardActionUi getWizardUi() {
        return null; // we do not have any ui for this action
    }
}
