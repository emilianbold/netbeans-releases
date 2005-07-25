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


import java.util.Properties;
import com.installshield.event.ISSilentContext;
import com.installshield.product.service.product.ProductService;
import com.installshield.product.*;
import com.installshield.wizard.service.*;
import com.installshield.util.*;

public class PanelUpdateCheckSilentImpl {

    private static final String PRODUCT_RESOURCES_NAME =
        "com.installshield.product.i18n.ProductResources";
    public static final int EXECUTE_ONLY_CHECK_NAMES = 1;

    public void silentExecuteUpdateCheck(ISSilentContext context) {

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

    private Properties executeChecks(
        ISSilentContext context,
        String summaryStyle)
        throws ServiceException {

        boolean include = true;
        InstallChecker checker =
            context.getServices().getInstallChecker();
        checker.check(InstallChecker.CHECK_NAMES, include, summaryStyle);
        return checker.getReport();
    }

}
