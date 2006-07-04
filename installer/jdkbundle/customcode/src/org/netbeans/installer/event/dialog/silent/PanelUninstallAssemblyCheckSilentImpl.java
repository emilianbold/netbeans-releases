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
