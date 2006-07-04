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


import java.util.Vector;

import com.installshield.event.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.product.service.product.*;
import com.installshield.product.*;
import com.installshield.util.*;

public class PanelFeature {

    /**
     * Called when panel is displayed in console mode when "options-record" or 
     * "options-template" command line option is used.
     */
    public void generateOptionsEntriesFeature(ISOptionsContext context) {

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
