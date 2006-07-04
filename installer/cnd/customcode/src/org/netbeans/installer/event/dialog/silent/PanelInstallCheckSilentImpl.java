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


import java.util.Properties;
import com.installshield.event.ISSilentContext;
import com.installshield.product.service.product.ProductService;
import com.installshield.product.*;
import com.installshield.wizard.service.*;
import com.installshield.util.*;

public class PanelInstallCheckSilentImpl {

    private static final String PRODUCT_RESOURCES_NAME =
        "com.installshield.product.i18n.ProductResources";
	public static final int EXECUTE_ONLY_CHECK_NAMES = 1;

    public void silentExecuteInstallCheck(ISSilentContext context) {
    	
        try {
            Properties result = executeChecks(context, ProductService.TEXT);

            String errorCount =
                (String)result.get(ProductService.SUMMARY_ERROR_COUNT);
            String warningCount =
                (String)result.get(ProductService.SUMMARY_WARNING_COUNT);
            boolean cancelWizard =
                errorCount != null && !errorCount.equals("0");
            boolean cancelWizardWarn =
                warningCount != null && !warningCount.equals("0");
            if (cancelWizard) {
                LogUtils.getLog().logEvent(
                    this,
                    Log.ERROR,
                    LocalizedStringResolver.resolve(
                        PRODUCT_RESOURCES_NAME,
                        "ProductCheckPanel.defaultCancelWizardMessage"));
                context.getWizard().exit(ProductExitCodes.FAILED_PRODUCT_CHECK);
            }
            if (cancelWizardWarn) {
                LogUtils.getLog().logEvent(
                    this,
                    Log.WARNING,
                    LocalizedStringResolver.resolve(
                        PRODUCT_RESOURCES_NAME,
                        "ProductCheckPanel.defaultCancelWizardMessage"));
                context.getWizard().exit(ProductExitCodes.FAILED_PRODUCT_CHECK);
            }
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

    }

    private Properties executeChecks(ISSilentContext context, String summaryStyle)
        throws ServiceException {
       	
		boolean include = true;
        InstallChecker checker = context.getServices().getInstallChecker();
        checker.check(InstallChecker.CHECK_NAMES, include, summaryStyle);
        return checker.getReport();
    }

}
