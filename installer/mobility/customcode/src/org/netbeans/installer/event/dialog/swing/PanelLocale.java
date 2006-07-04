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


import java.util.Locale;

import com.installshield.event.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.*;
import com.installshield.product.service.product.*;
import com.installshield.util.*;
import com.installshield.util.sort.*;

public class PanelLocale {

    public void generateOptionsEntriesLocale(ISOptionsContext context) {

        try {
            ProductService pService =
                (ProductService)context.getService(ProductService.NAME);

            Locale[] locales = getAvailableLocales(pService);

            String title =
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "LocalePanel.ote1Title");
            StringBuffer enumStr = new StringBuffer();
            enumStr.append("<enum>");
            for (int i = 0; i < locales.length; i++) {
                enumStr.append("<value>");
                enumStr.append(
                    locales[i].toString()
                        + ":"
                        + LocaleUtils.getLocaleDisplayName(locales[i]));
                enumStr.append("</value>");
            }

            enumStr.append("</enum>");

            // assert locales.length > 0
            // Otherwise set no options entries for context
            if ((locales != null) && (locales.length > 0)) {
                String sampleLocale =
                    LocaleUtils.getLocaleDisplayName(locales[0]);
                String sampleOption =
                    "-P selectedLocales=" + locales[0].toString();
                String multMsg = "";

                if (locales.length > 1) {
                    multMsg =
                        LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "LocalePanel.ote1MultLocales",
                            new String[] {
                                locales[0].toString(),
                                locales[1].toString(),
                                LocaleUtils.getLocaleDisplayName(locales[0]),
                                LocaleUtils.getLocaleDisplayName(locales[1])});
                }

                String doc =
                    LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "LocalePanel.ote1Doc",
                        new String[] {
                            enumStr.toString(),
                            sampleLocale,
                            sampleOption,
                            multMsg });
                String option = "-P selectedLocales=";
                if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                    option
                        += LocalizedStringResolver.resolve(
                            "com.installshield.wizard.i18n.WizardResources",
                            "WizardBean.valueStr");
                }
                else {
                    try {
                        String selectedLocales =
                            (String)pService.getProductTreeProperty(
                                ProductService.DEFAULT_PRODUCT_SOURCE,
                                "selectedLocales");
                        if (selectedLocales == null) {
                            selectedLocales = "";
                        }
                        option += selectedLocales;
                    }
                    catch (Exception e) {
                        LogUtils.getLog().logEvent(this, Log.ERROR, e);
                    }
                }

                context.getOptionEntries().addElement(
                    new OptionsTemplateEntry(title, doc, option));
            }
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

    }

    private Locale[] getAvailableLocales(ProductService pService) {

        Locale[] available = new Locale[0];

        try {
            String[] availableStrs =
                pService.getProductLocales(
                    ProductService.DEFAULT_PRODUCT_SOURCE);
            available = new Locale[availableStrs.length];
            for (int i = 0; i < availableStrs.length; i++) {
                available[i] = PropertyUtils.createLocale(availableStrs[i]);
            }
            SortUtils.qsort(available, new LocaleCompare());
        }
        catch (ServiceException e) {
            available = new Locale[0];
        }
        return available;
    }

}
