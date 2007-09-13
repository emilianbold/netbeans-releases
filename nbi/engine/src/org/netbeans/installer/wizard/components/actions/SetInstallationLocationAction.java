/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class SetInstallationLocationAction extends WizardAction {
    public static final String SOURCE_UID_PROPERTY = 
            "source.component";//NOI18N
    public static final String RELATIVE_LOCATION_PROPERTY = 
            "relative.location";//NOI18N
    
    public void execute() {
        final String uid              = getProperty(SOURCE_UID_PROPERTY);
        final String relativeLocation = getProperty(RELATIVE_LOCATION_PROPERTY);
        
        if (uid == null) {
            ErrorManager.notifyError(ResourceUtils.getString(
                    SetInstallationLocationAction.class,
                    ERROR_SOURCE_UID_NOT_SET_KEY));
            return;
        }
        
        // we do expect the property container of the wizard to be a product, if
        // it's not we should fail
        final Product target = (Product) getWizard().getContext().get(Product.class);
        
        final List<Dependency> dependencies = target.getDependencyByUid(uid);
        final Product source =
                Registry.getInstance().getProducts(dependencies.get(0)).get(0);
        
        if (source == null) {
            ErrorManager.notifyError(ResourceUtils.getString(
                    SetInstallationLocationAction.class,
                    ERROR_CANNOT_FIND_COMPONENT_KEY, uid));
            return;
        }
        
        File sourceLocation = null;
        try {
            final File location = source.getInstallationLocation();
            if (SystemUtils.isMacOS() && source.getLogic().wrapForMacOs() &&
                    location.getName().endsWith(".app")) {
                sourceLocation = new File(
                        location,
                        "Contents/Resources/" +
                        location.getName().replaceAll("\\.app$",""));
            } else {
                sourceLocation = location;
            }
        } catch (InitializationException e) {
            ErrorManager.notifyError(ResourceUtils.getString(
                    SetInstallationLocationAction.class,
                    ERROR_CANNOT_GET_LOGIC_KEY, 
                    target.getDisplayName()), e);
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
    
    public boolean isCancellable() {
        return false;
    }
    
    private static final String ERROR_SOURCE_UID_NOT_SET_KEY =
            "SILA.error.source.uid.not.set";//NOI18N
    private static final String ERROR_CANNOT_FIND_COMPONENT_KEY =
            "SILA.error.cannot.find.component";//NOI18N
    private static final String ERROR_CANNOT_GET_LOGIC_KEY = 
            "SILA.error.cannot.get.logic";//NOI18N
}
