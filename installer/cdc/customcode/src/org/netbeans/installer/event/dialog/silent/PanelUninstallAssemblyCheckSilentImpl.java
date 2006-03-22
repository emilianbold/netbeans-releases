/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer.event.dialog.silent;


import com.installshield.event.ISSilentContext;
import com.installshield.product.ProductExitCodes;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.LocalizedStringResolver;
import com.installshield.util.Log;
import com.installshield.util.LogUtils;
import com.installshield.wizard.service.ServiceException;

public class PanelUninstallAssemblyCheckSilentImpl {

    public void silentUninstallAssemblyCheck(ISSilentContext context) {
    	
        try {
            ProductService productService = (ProductService)context.getService(ProductService.NAME);
            boolean cancelWizard = !productService.runUninstallAssemblyCheck();
            if (cancelWizard) {
                String errorMsg = LocalizedStringResolver.resolve(
                        "com.installshield.wizardx.i18n.WizardXResources",
                		"UninstallAssemblyCheckPanel.errorMessage",
                		new String[]{"$P(displayName)","$P(displayName)"});

                LogUtils.getLog().logEvent(
                    this,
                    Log.ERROR,
                    errorMsg);
                context.getWizard().exit(ProductExitCodes.FAILED_UNINSTALL_ASSEMBLY_CHECK);
            }
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }
}