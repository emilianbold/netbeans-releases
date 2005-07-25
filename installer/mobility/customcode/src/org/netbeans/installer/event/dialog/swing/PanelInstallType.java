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
import com.installshield.event.*;
import com.installshield.util.*;
import com.installshield.wizard.*;
import com.installshield.product.*;
import com.installshield.product.service.product.ProductService;
import com.installshield.ui.controls.*;
import com.installshield.database.designtime.*;

public class PanelInstallType {
    private Properties featureFilter = null;
    private Properties productFilter = null;

    public void queryExitInstallType(ISDialogQueryContext context) {

        ISPanel panel = context.getISPanel();
        ISControl ctrl = panel.getControl("installationTypes");
        ISDatabaseDef db = ctrl.getISDatabaseDef();
        String selected = db.getSelectedInstallationType();

        ISInstallationTypeDef setupType = null;
        ISInstallationTypeDef[] setupTypes = db.getInstallationTypes();
        for (int i = 0; i < setupTypes.length; i++) {
            if (setupTypes[i].getName().equals(selected)) {
                setupType = setupTypes[i];
                break;
            }
        }

        String[] setupTypeFeatures = setupType.getFeatures();
        ProductService pservice = null;
        try {
            pservice = (ProductService)context.getService(ProductService.NAME);
            Properties[] features = collectFeatureProperties(null, pservice);
            //Feature's "active" state is set if it is part of the selected SetupType.
            boolean active = false;
            for (int i = 0; i < features.length; i++) {
                active = false;
                if (setupTypeFeatures.length > 0) {
                    for (int j = 0;
                        !active && j < setupTypeFeatures.length;
                        j++) {
                        if (features[i]
                            .getProperty("beanId")
                            .equals(setupTypeFeatures[j])) {
                            active = true;
                        }
                    }
                }
                else {
                    active = true;
                    db.updatFeaturesForInstallType(
                        features[i].getProperty("beanId"),
                        true);
                }
                pservice.setProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    features[i].getProperty("beanId"),
                    "active",
                    new Boolean(active));
            }

        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public void generateOptionsEntriesInstallType(ISOptionsContext context) {

        try {
            ISDatabaseDef isDb =
                context
                    .getWizard()
                    .getServices()
                    .getISDatabase()
                    .getDatabaseDef();

            //if there are no setup types, don't bother with documenting this panel
            ISInstallationTypeDef[] types = isDb.getInstallationTypes();
            if (types.length == 0) {
                return;
            }

            String doc =
                "The Installation Type to be used when installing the product.  Stored as a Variable and must be set with -V.";

            ISVariableDef var = isDb.getSelectedInstallationTypeVariable();
            String varName =
                isDb.getSelectedInstallationTypeVariable().getName();
            String option = "-V " + varName + "=";

            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                option
                    += LocalizedStringResolver.resolve(
                        "com.installshield.wizard.i18n.WizardResources",
                        "WizardBean.valueStr");
            }
            else {
                option += isDb.getSelectedInstallationType();
            }

            context.getOptionEntries().addElement(
                new OptionsTemplateEntry(
                    "Custom Dialog: " + context.getPanel().getName(),
                    doc,
                    option));
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
            return;
        }
    }

    private Properties[] collectFeatureProperties(
        String parentBeanId,
        ProductService pService)
        throws ServiceException {
        Vector features = new Vector();
        Properties[] products =
            pService.getProductBeanChildren(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                parentBeanId,
                new String[] { "beanId" },
                getProductFilter());
        for (int i = 0; i < products.length; i++) {
            Properties[] collectedFeatures =
                collectFeatureProperties(
                    products[i].getProperty("beanId"),
                    pService);
            for (int j = 0; j < collectedFeatures.length; j++) {
                features.addElement(collectedFeatures[j]);
            }
        }

        Properties[] childFeatures =
            pService.getProductBeanChildren(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                parentBeanId,
                new String[] { "beanId" },
                getFeatureFilter());

        for (int i = 0; i < childFeatures.length; i++) {
            features.addElement(childFeatures[i]);
            Properties[] collectedFeatures =
                collectFeatureProperties(
                    childFeatures[i].getProperty("beanId"),
                    pService);
            for (int j = 0; j < collectedFeatures.length; j++) {
                features.addElement(collectedFeatures[j]);
            }
        }
        Properties[] returnProperties = new Properties[features.size()];
        for (int i = 0; i < features.size(); i++) {
            returnProperties[i] = (Properties)features.elementAt(i);
        }
        return returnProperties;
    }

    private Properties getProductFilter() {
        if (productFilter == null) {
            productFilter = new Properties();
            productFilter.put("filter.class", Product.class.getName());
        }
        return productFilter;
    }
    private Properties getFeatureFilter() {
        if (featureFilter == null) {
            featureFilter = new Properties();
            featureFilter.put("filter.class", ProductFeature.class.getName());
        }
        return featureFilter;
    }

}