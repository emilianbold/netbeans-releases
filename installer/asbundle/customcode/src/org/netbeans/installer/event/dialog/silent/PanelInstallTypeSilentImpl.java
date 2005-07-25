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
import java.util.Vector;

import com.installshield.event.ISSilentContext;
import com.installshield.database.designtime.*;
import com.installshield.database.runtime.impl.*;
import com.installshield.wizard.service.*;
import com.installshield.product.service.product.*;
import com.installshield.product.Product;
import com.installshield.product.ProductFeature;
import com.installshield.util.*;

public class PanelInstallTypeSilentImpl {

    public void silentExecuteInstallType(ISSilentContext context) {
        updateProductTree(context.getServices());
    }

    private ISInstallationTypeDef getSelectedInstallationType(WizardServices services) {
        try {

            ISDatabaseImpl isDatabase =
                (ISDatabaseImpl)services.getISDatabase();
            ISDatabaseDef dbDef = isDatabase.getDatabaseDef();
            ISInstallationTypeDef[] setupTypeDefs = dbDef.getInstallationTypes();
            for (int i = 0;
                setupTypeDefs != null && i < setupTypeDefs.length;
                i++) {
                if (setupTypeDefs[i]
                    .getName()
                    .equals(
                        services.resolveString(
                            dbDef.getSelectedInstallationType()))) {
                    return setupTypeDefs[i];
                }
            }
            if (setupTypeDefs != null && setupTypeDefs.length > 0) {
                return setupTypeDefs[0];
            }

        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
        return null;
    }

    private void updateProductTree(WizardServices wServices) {

        // if nothing selected, log message
        ISInstallationTypeDef selected = getSelectedInstallationType(wServices);
        ISDatabaseDef isDb;
        if (selected == null) {
            try {
                isDb = wServices.getISDatabase().getDatabaseDef();
                if (isDb.getSelectedInstallationType().equals("")) {
                    LogUtils.getLog().logEvent(
                        this,
                        Log.MSG1,
                        LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "SetupTypePanel.noSetupType"));
                }
                else {
                    LogUtils.getLog().logEvent(
                        this,
                        Log.ERROR,
                        LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "SetupTypePanel.badSetupType",
                            new String[] {
                                 isDb.getSelectedInstallationType()}));
                }
            }
            catch (ServiceException e) {
                //ignore
            }
            return;
        }

        // update setup type via service (changes feature states accordingly)		
        try {
            ProductService pservice =
                (ProductService)wServices.getService(ProductService.NAME);
            Properties[] features = collectFeatureProperties(null, pservice);
            String[] setupTypeFeatures = selected.getFeatures();
            //Feature's "active" state is set if it is part of the selected SetupType.
            if (setupTypeFeatures.length > 0) {
                for (int i = 0; i < features.length; i++) {
                    boolean active = false;
                    for (int j = 0;
                        !active && j < setupTypeFeatures.length;
                        j++) {
                        if (features[i]
                            .getProperty("beanId")
                            .equals(setupTypeFeatures[j])) {
                            active = true;
                        }
                    }
                    pservice.setProductBeanProperty(
                        ProductService.DEFAULT_PRODUCT_SOURCE,
                        features[i].getProperty("beanId"),
                        "active",
                        new Boolean(active));
                }
            }

        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    private Properties[] collectFeatureProperties(
        String parentBeanId,
        ProductService pService)
        throws ServiceException {

        Vector features = new Vector();

        Properties productFilter = new Properties();
        productFilter.put("filter.class", Product.class.getName());

        Properties[] products =
            pService.getProductBeanChildren(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                parentBeanId,
                new String[] { "beanId" },
                productFilter);

        for (int i = 0; i < products.length; i++) {
            Properties[] collectedFeatures =
                collectFeatureProperties(
                    products[i].getProperty("beanId"),
                    pService);
            for (int j = 0; j < collectedFeatures.length; j++) {
                features.addElement(collectedFeatures[j]);
            }
        }

        Properties featureFilter = new Properties();
        featureFilter.put("filter.class", ProductFeature.class.getName());

        Properties[] childFeatures =
            pService.getProductBeanChildren(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                parentBeanId,
                new String[] { "beanId" },
                featureFilter);

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

}
