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

package org.netbeans.installer.event.dialog.swing;


import java.util.*;
import com.installshield.wizard.service.*;
import com.installshield.event.ui.*;
import com.installshield.product.service.product.ProductService;
import com.installshield.ui.controls.*;

public class PanelPreUninstallSummary {
	
    private int type = ProductService.PRE_UNINSTALL;
    private final String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
	private static final String HTML_CONTROL_ID = "summary";

    public void initializeUIPreUninstallSummary(ISDialogContext context) {
        	
        try {
            ProductService service =
                (ProductService)context.getService(ProductService.NAME);
            Properties summary =
                service.getProductSummary(
                    productURL,
                    type,
                    ProductService.HTML);
            String sumMessage = summary.getProperty(ProductService.SUMMARY_MSG);
            ISPanel panel = context.getISPanel();
            ISHtmlControl html = panel.getISHtmlControl(HTML_CONTROL_ID);
            html.setContentType(ISHtmlControl.HTML_CONTENT_TYPE);
            html.setText(sumMessage);
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }

    }

}