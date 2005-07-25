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

import com.installshield.event.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.product.service.product.*;
import com.installshield.product.*;
import com.installshield.util.*;

public class PanelUninstallFeature {

    /**
    	 * Called when panel is displayed in console mode when "options-record" or 
    	 * "options-template" command line option is used.
    	 */
    public void generateOptionsEntriesUninstallFeature(ISOptionsContext context) {

        ProductTree tree = null;
        try {
            ProductService service =
                (ProductService)context.getService(ProductService.NAME);
            tree =
                service.getSoftwareObjectTree(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    new String[] { "active", "displayName", "visible" });
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
            return;
        }

        // assert tree != null
        Vector entries = context.getOptionEntries();
        ProductTreeIterator iter =
            ProductTreeIteratorFactory.createFeatureIterator(tree.getRoot());

        for (ProductBean cur = iter.getNext(iter.begin());
            cur != iter.end();
            cur = iter.getNext(cur)) {
            // assert safe-cast
            ProductFeature feature = (ProductFeature)cur;
            String displayName =
                context.resolveString(
                    "$P(" + feature.getBeanId() + ".displayName)");
            // feature entry title
            String title =
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "FeaturePanel.oteTitle",
                    new String[] { displayName });
            // setup type entry doc
            String doc =
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "FeaturePanel.oteDoc",
                    new String[] { displayName, feature.getBeanId()});
            // setup type entry option
            String option = "-P " + feature.getBeanId() + ".active=";
            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                option
                    += LocalizedStringResolver.resolve(
                        "com.installshield.wizard.i18n.WizardResources",
                        "WizardBean.valueStr");
            }
            else {
                option += feature.isActive();
            }
            entries.addElement(new OptionsTemplateEntry(title, doc, option));
        }

    }

}
