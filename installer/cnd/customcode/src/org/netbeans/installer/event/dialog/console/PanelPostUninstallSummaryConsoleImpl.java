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

package org.netbeans.installer.event.dialog.console;


import java.util.Properties;

import com.installshield.event.ui.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.product.service.product.*;
import com.installshield.util.*;

public class PanelPostUninstallSummaryConsoleImpl {

    private int type = ProductService.POST_UNINSTALL;
    private final String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;

    public void consoleInteractionPostUninstallSummary(ISDialogContext context) {
		
		TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();

        try {

            ProductService service =
                (ProductService)context.getService(ProductService.NAME);

            Properties summary =
                service.getProductSummary(
                    productURL,
                    type,
                    ProductService.HTML);

            String sumMessage =
                context.getServices().resolveString(
                    summary.getProperty(ProductService.SUMMARY_MSG));

            String summaryText =
                new HtmlToTextConverter().convertText(sumMessage);
            summaryText = summaryText.trim();
            tty.printPage(summaryText);
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

    }

}
