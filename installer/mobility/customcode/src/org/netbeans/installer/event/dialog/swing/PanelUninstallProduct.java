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


import java.util.Vector;
import java.util.Properties;

import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.product.*;
import com.installshield.product.service.product.*;
import com.installshield.util.*;
import com.installshield.ui.controls.*;
import com.installshield.ui.controls.swing.*;
import com.installshield.wizard.service.*;

public class PanelUninstallProduct {

    private static final String PRODUCT_CTRL_ID = "PRODUCT_SELECTION_CONTROL";

    public void queryEnterUninstallProduct(ISQueryContext context) {

        DynamicProductReference[] products;

        try {
            products = readProductRefs(context.getServices());
        } catch (Exception e) {
            products = new DynamicProductReference[0];
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

        //if no products are referenced then skip dialog
        if (products.length <= 0) {
            context.setReturnValue(false);
        }
    }

    public void queryExitUninstallProduct(ISDialogQueryContext context) {

        DynamicProductReference[] products;

        ISControl ctrl = context.getISPanel().getControl(PRODUCT_CTRL_ID);

        //ASSERT safe cast
        SwingUninstallProductControl productControl =
            (SwingUninstallProductControl)ctrl;

        products = productControl.getProducts();

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
                    "UninstallProductPanel.selectionRequired"),
                UserInputRequest.ERROR);
            context.setReturnValue(false);
            return;
        }

        productControl.updateProductActiveStates();
        productControl.createUninstallTypeRefSequence();

        context.setReturnValue(true);
    }

    private DynamicProductReference[] readProductRefs(WizardServices services)
        throws ServiceException {
        Vector v = new Vector();

        // get the list of children of the root
        ProductService rootService =
            (ProductService)services.getService(ProductService.NAME);
        Properties[] refs =
            rootService.getProductBeanChildren(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                null,
                new String[] {
                    "beanId",
                    "installer",
                    "active",
                    "uUID",
                    "version" },
                null);

        for (int i = 0; i < refs.length; i++) {
            String installer = (String)refs[i].get("installer");
            if (installer != null) {
                try {
                    WizardServices childServices =
                        services.getWizardServices(installer);
                    ProductService childService =
                        (ProductService)childServices.getService(
                            ProductService.NAME);
                    String displayName =
                        (String)childService.getProductBeanProperty(
                            ProductService.DEFAULT_PRODUCT_SOURCE,
                            null,
                            "displayName");
                    refs[i].put("displayName", displayName);
                    v.addElement(refs[i]);
                } catch (ServiceException e) {
                    LogUtils.getLog().logEvent(this, Log.ERROR, e);
                }
            } else {
                LogUtils.getLog().logEvent(
                    this,
                    Log.WARNING,
                    "Could not find property \"installer\" in bean "
                        + refs[i].get("beanId")
                        + " -- unable to display product reference");
            }
        }

        DynamicProductReference[] ret = new DynamicProductReference[v.size()];
        for (int i = 0; i < ret.length; i++) {
            Properties p = (Properties)v.elementAt(i);
            ret[i] = new DynamicProductReference();
            ret[i].setBeanId(p.getProperty("beanId", ""));
            ret[i].setDisplayName(p.getProperty("displayName", ""));
            ret[i].setInstaller(p.getProperty("installer", ""));
            ret[i].setUUID(p.getProperty("uUID", ""));
            ret[i].setVersion(p.getProperty("version", ""));
            try {
                ret[i].setActive(((Boolean)p.get("active")).booleanValue());
            } catch (Exception e) {
                ret[i].setActive(false);
                LogUtils.getLog().logEvent(this, Log.ERROR, e);
            }
        }
        return ret;
    }

}
