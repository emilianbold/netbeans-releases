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

package org.netbeans.installer.event.dialog.console;


import java.util.Locale;
import java.util.Vector;
import java.util.StringTokenizer;

import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.wizard.*;
import com.installshield.product.service.product.*;
import com.installshield.product.*;
import com.installshield.util.*;
import com.installshield.util.sort.*;

public class PanelLocaleConsoleImpl {

    private static final String DESCRIPTION =
        "$L(com.installshield.product.i18n.ProductResources, LocalePanel.description, $P(displayName))";

    private static final boolean autoSelect = false;
    private static final boolean multiSelect = true;

    private Locale[] available = new Locale[0];
    private Locale[] selected = new Locale[0];

    public void queryEnterLocale(ISQueryContext context) {

        //if not autoselect always show
        if (!autoSelect) {
            context.setReturnValue(true);
            return;
        }

        try {
            ProductService pService =
                (ProductService)context.getService(ProductService.NAME);

            Locale[] available = this.getAvailableLocales(pService);
            Locale[] selected = new Locale[0];

            int localeOption = getLocaleOption(pService);

            //autoselect system locale
            if (localeOption == ProductTree.LOCALE_OPTION_SYSTEM
                || localeOption == ProductTree.LOCALE_OPTION_SYSTEM_THEN_PROMPT
                || localeOption
                    == ProductTree.LOCALE_OPTION_SYSTEM_THEN_DEFAULT) {

                Locale match =
                    LocaleUtils.findBestMatch(available, Locale.getDefault());
                if (match != null) {
                    selected = new Locale[] { match };
                    LogUtils.getLog().logEvent(
                        this,
                        Log.DBG,
                        "autoselected locales based on product tree locale option");
                    updateSelected(pService, selected);
                    context.setReturnValue(false);
                    return;
                }
            }

            //autoselect only one available locale
            if (available.length == 0) {
                selected = available;
                LogUtils.getLog().logEvent(
                    this,
                    Log.DBG,
                    "autoselected locales because only one is available");
                updateSelected(pService, selected);
                context.setReturnValue(false);
                return;
            }

            //going to show -- update selected locales
            try {
                String selectedStr =
                    (String)pService.getProductTreeProperty(
                        ProductService.DEFAULT_PRODUCT_SOURCE,
                        "selectedLocales");
                StringTokenizer tokens =
                    new StringTokenizer(
                        selectedStr != null ? selectedStr : "",
                        ",");
                selected = new Locale[tokens.countTokens()];
                int i = 0;
                while (tokens.hasMoreTokens()) {
                    selected[i++] =
                        PropertyUtils.createLocale(tokens.nextToken());
                }
            }
            catch (ServiceException e) {
                LogUtils.getLog().logEvent(this, Log.ERROR, e);
            }

        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

        context.setReturnValue(true);

    }

    public void consoleInteractionLocale(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        try {
            ProductService pService =
                (ProductService)context.getService(ProductService.NAME);

            tty.printLine(context.getServices().resolveString(DESCRIPTION));
            tty.printLine();

            Locale[] available = getAvailableLocales(pService);
            Locale[] selected = getDefaultSelectedLocales(context.getServices());
            String[] options = new String[available.length];
            Vector indexVector = new Vector();

            for (int i = 0; i < options.length; i++) {
                options[i] = LocaleUtils.getLocaleDisplayName(available[i]);
                if (isSelected(selected, available[i])) {
                    indexVector.addElement(new Integer(i));
                }
            }
            int[] indexes = new int[indexVector.size()];
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = ((Integer)indexVector.elementAt(i)).intValue();
            }

            ConsoleChoice choice = new ConsoleChoice();
            choice.setOptions(options);
            choice.setMultiSelect(multiSelect);
            if (multiSelect) {
                choice.setSelected(indexes);
            }
            else if (indexes.length > 0) {
                choice.setSelected(indexes[0]);
            }

            // interact with user
            choice.consoleInteraction(tty);

            // update selected
            indexes = choice.getSelected();
            Locale[] newSelected = new Locale[indexes.length];
            for (int i = 0; i < newSelected.length; i++) {
                newSelected[i] = available[indexes[i]];
            }

            updateSelected(pService, newSelected);
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

        //panel.setSelectedLocales(newSelected);
    }

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

        Locale[] available;

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

    private int getLocaleOption(ProductService pService) {
        try {
            return (
                (Integer)pService.getProductTreeProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    "localeOption"))
                .intValue();
        }
        catch (Exception e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
            return ProductTree.LOCALE_OPTION_PROMPT;
        }
    }

    private void updateSelected(ProductService pService, Locale[] selected) {
        updateProductTree(pService, encodeSelected(selected));
    }

    private void updateProductTree(ProductService pService, String selected) {
        try {
            LogUtils.getLog().logEvent(
                this,
                Log.DBG,
                "setting selected locales to \"" + selected + "\"");
            pService.setProductTreeProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                "selectedLocales",
                selected);
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    private String encodeSelected(Locale[] selected) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < selected.length; i++) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append(selected[i].toString());
        }
        return buf.toString();
    }

    private Locale[] getDefaultSelectedLocales(WizardServices wServices) {
        String selectedLocales = null;
        Locale[] defaultLocale = null;

        try {
            ProductService service =
                (ProductService)wServices.getService(ProductService.NAME);

            selectedLocales =
                (String)service.getProductTreeProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    "selectedLocales");

            if (selectedLocales == null) {
                selectedLocales = "";
            }

            StringTokenizer tokenizer =
                new StringTokenizer(selectedLocales, ",");
            defaultLocale = new Locale[tokenizer.countTokens()];
            int i = 0;
            while (tokenizer.hasMoreTokens()) {
                defaultLocale[i++] = new Locale(tokenizer.nextToken(), "");
            }

        }
        catch (ServiceException e) {
            //ignore this
            available = new Locale[0];
        }
        return defaultLocale;

    }

    private boolean isSelected(Locale[] selected, Locale l) {
        for (int i = 0; i < selected.length; i++) {
            if (selected[i].equals(l)) {
                return true;
            }
        }
        return false;
    }

}
