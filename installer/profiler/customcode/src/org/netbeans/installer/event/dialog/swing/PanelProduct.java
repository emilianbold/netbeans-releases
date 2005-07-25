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


import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.product.*;
import com.installshield.product.service.product.*;
import com.installshield.util.*;
import com.installshield.ui.controls.*;
import com.installshield.ui.controls.swing.*;

public class PanelProduct {

    private static final String PRODUCT_CTRL_ID = "PRODUCT_SELECTION_CONTROL";

    public void queryEnterProduct(ISQueryContext context) {

        DynamicProductReference[] products;

        try {
            products = ProductServiceUtils.readProductRefs(this,context.getServices());
        } catch (Exception e) {
            products = new DynamicProductReference[0];
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

        //if no products are referenced then skip dialog
        if (products.length <= 0) {
            context.setReturnValue(false);
        }
    }

    public void queryExitProduct(ISDialogQueryContext context) {

        DynamicProductReference[] products;

        ISControl ctrl = context.getISPanel().getControl(PRODUCT_CTRL_ID);

        //ASSERT safe cast
        SwingProductControl productControl = (SwingProductControl)ctrl;

        products = productControl.getProducts();
        Object[] installTypePanels = productControl.getInstallationTypePanels();

        //user is required to select at least one product
        boolean selected = false;
        for (int i = 0; !selected && i < products.length; i++) {
            selected = products[i].isActive();
        }

        //must select at least one product
        if (!selected) {
            context.getWizardUI().displayUserMessage(
                context.resolveString(context.getWizard().getTitle()),
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "ProductPanel.selectionRequired"),
                UserInputRequest.ERROR);
            context.setReturnValue(false);
            return;
        }

        productControl.updateProductActiveStates();
        productControl.updateProductFeatureStates();
        productControl.createSetupTypeRefSequence();

        context.setReturnValue(true);
    }

}
