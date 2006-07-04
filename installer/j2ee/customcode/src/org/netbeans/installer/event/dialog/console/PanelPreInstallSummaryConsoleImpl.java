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

package org.netbeans.installer.event.dialog.console;


import java.util.Properties;

import com.installshield.event.ui.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.product.service.product.*;
import com.installshield.util.*;

public class PanelPreInstallSummaryConsoleImpl {

    private int type = ProductService.PRE_INSTALL;
    private final String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;

    public void consoleInteractionPreInstallSummary(ISDialogContext context) {

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
