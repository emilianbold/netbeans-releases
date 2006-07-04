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
