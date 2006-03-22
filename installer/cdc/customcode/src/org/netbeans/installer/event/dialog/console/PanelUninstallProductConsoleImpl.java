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


import java.util.Hashtable;
import java.util.Vector;
import java.util.Properties;

import com.installshield.event.ui.*;
import com.installshield.event.*;
import com.installshield.wizard.*;
import com.installshield.product.wizardbeans.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.product.i18n.ProductResourcesConst;
import com.installshield.product.service.product.*;
import com.installshield.product.*;
import com.installshield.database.designtime.*;
import com.installshield.database.*;
import com.installshield.util.*;

public class PanelUninstallProductConsoleImpl {

    private static final String DESCRIPTION =
        "$L(com.installshield.product.i18n.ProductResources, UninstallProductPanel.selectProduct)";

    private static final String SELECTED_PRODUCTS_VAR =
        "IS_SELECTED_UNINST_PRODUCTS";
    private static final String SELECTED_TYPES_VAR = "IS_SELECTED_UNINST_TYPES";
    private static final String REF_ID_VAR = "IS_UNINST_REF_ID";
    private static final String TYPE_DELIMITER = ";";

    private static final int COMPLETE_UNINSTALL_INDEX = 0;
    private static final int PARTIAL_UNINSTALL_INDEX = 1;

    private static final String COMPLETE_UNINSTALL = "Complete";
    private static final String PARTIAL_UNINSTALL = "Partial";

    private double col0Width = 0.67;
    private double col1Width = 0.33;

    private DynamicProductReference[] products = new DynamicProductReference[0];
    private Object[] installationTypePanels = new Object[0];
    private boolean dataInitialized = false;

    private Hashtable uninstTypes = new Hashtable();

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

        try {
            products = readProductRefs(context.getServices());
            setProductRefSelectionStates(products, context.getServices());
            refreshProductRefSelectionTypes(context.getServices());

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

            updateProductActiveStates(context.getServices());
            createUninstallTypeRefSequence(
                context.getWizard(),
                context.getServices());
        } catch (ServiceException e) {
            context.getServices().logEvent(this, Log.ERROR, e);
        }
        context.setReturnValue(true);
    }

    private void setUninstallType(String refBeanId, String uninstType) {
        uninstTypes.put(refBeanId, uninstType);
    }

    private String getUninstallType(String refBeanId) {
        return (String)uninstTypes.get(refBeanId);
    }

    public void consoleInteractionUninstallProduct(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        WizardServices wServices = context.getServices();

        boolean dataRefreshed = false;
        while (true) {

            tty.setBaseIndent(0);
            tty.printLine(context.resolveString(DESCRIPTION));
            tty.printLine();
            tty.setBaseIndent(tty.getBaseIndent() + TTYDisplay.DEFAULT_INDENT);

            TTYTable table = new TTYTable();
            int tableWidth = tty.getWidth() - 2 * tty.getBaseIndent();

            // print instructions
            tty.printLine(
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "UninstallProductPanel.consoleProductOptions"));
            tty.printLine();

            // table header
            table.addRow(
                new String[] {
                    LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "UninstallProductPanel.product"),
                    LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "UninstallProductPanel.setupType")});
            table.addRow(
                new String[] {
                    StringUtils.createString(
                        '-',
                        (int) (tableWidth * col0Width)),
                    StringUtils.createString(
                        '-',
                        (int) (tableWidth * col1Width))});

            verifyData(context.getWizard());
            if (!dataRefreshed) {
                dataRefreshed = true;
                refreshProductRefSelectionTypes(context.getServices());
            }

            for (int i = 0; i < products.length; i++) {
                String uninstType = getUninstallType(products[i].getBeanId());
                if (uninstType == null) {
                    uninstType = COMPLETE_UNINSTALL;
                    setUninstallType(products[i].getBeanId(), uninstType);
                }

                // col0: "nn. [x] <product display name>"
                StringBuffer col0 = new StringBuffer();
                if (i <= 9) {
                    col0.append(' ');
                }
                col0.append(i + 1);
                col0.append(". [");
                if (products[i].isActive()) {
                    col0.append('x');
                } else {
                    col0.append(' ');
                }
                col0.append("] ");
                col0.append(
                    context.resolveString(products[i].getDisplayName()));
                // col1: "<uninstall type display name>" - only display for active products
                StringBuffer col1 = new StringBuffer();
                if (products[i].isActive()) {
                    String typeName = getUninstallTypeDisplayName(uninstType);
                    if (typeName != null) {
                        col1.append(" " + MnemonicString.stripMn(typeName));
                    }
                }
                table.addRow(new String[] { col0.toString(), col1.toString()});
            }

            tty.printTable(table);

            // other options
            tty.printLine();
            tty.printLine(
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "FeaturePanel.consoleOtherOptions"));
            tty.printLine();
            tty.printLine(
                " 0. "
                    + LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "UninstallFeaturePanel.continueUninstalling"));

            // prompt
            tty.printLine();
            int response =
                tty.promptInt(
                    LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "FeaturePanel.consoleChooseAction"),
                    0,
                    0,
                    products.length);
            tty.printLine();

            if (response >= 1) {

                // display selected product -- assert response - 1 is in range
                DynamicProductReference selected = products[response - 1];

                boolean toggle = false;
                boolean changeUninstType = false;

                if (selected.isActive()) {
                    // if selected is active, prompt as to what to do
                    tty.printLine();
                    String displayName =
                        MnemonicString.stripMn(
                            context.resolveString(selected.getDisplayName()));
                    tty.printLine(
                        " 1. "
                            + LocalizedStringResolver.resolve(
                                "com.installshield.product.i18n.ProductResources",
                                "UninstallProductPanel.consoleDeselectProduct",
                                new String[] { displayName }));
                    tty.printLine(
                        " 2. "
                            + LocalizedStringResolver.resolve(
                                "com.installshield.product.i18n.ProductResources",
                                "UninstallProductPanel.consoleChangeUninstallType",
                                new String[] { displayName }));
                    tty.printLine();
                    response =
                        tty.promptInt(
                            LocalizedStringResolver.resolve(
                                "com.installshield.product.i18n.ProductResources",
                                "FeaturePanel.consoleChooseAction"),
                            1,
                            0,
                            2);
                    tty.printLine();
                    if (response == 1) {
                        // user wants to toggle active state
                        toggle = true;
                    } else if (response == 2) {
                        // user wants to change uninstall type
                        changeUninstType = true;
                    }
                } else {
                    // otherwise just toggle the selection without prompting
                    toggle = true;
                }

                if (toggle) {
                    // toggle bean state
                    selected.setActive(!selected.isActive());
                }

                if (changeUninstType) {
                    tty.printLine();
                    tty.printLine(
                        LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "UninstallProductPanel.consoleProductUninstallTypes",
                            new String[] {
                                MnemonicString.stripMn(
                                    context.resolveString(
                                        selected.getDisplayName()))}));
                    tty.printLine();

                    String[] types =
                        new String[] { COMPLETE_UNINSTALL, PARTIAL_UNINSTALL };

                    int curType = -1;
                    String selectedType =
                        getUninstallType(selected.getBeanId());
                    for (int i = 0; i < types.length; i++) {
                        StringBuffer buf = new StringBuffer();
                        if (i <= 9) {
                            buf.append(' ');
                        }
                        buf.append(i + 1);
                        buf.append(". ");
                        buf.append(
                            MnemonicString.stripMn(
                                context.resolveString(
                                    getUninstallTypeDisplayName(types[i]))));
                        tty.printLine(buf.toString());
                        if (types[i].equals(selectedType)) {
                            curType = i;
                        }
                    }
                    tty.printLine();
                    response =
                        tty.promptInt(
                            LocalizedStringResolver.resolve(
                                "com.installshield.product.i18n.ProductResources",
                                "ProductPanel.consoleSelectUninstallType"),
                            curType + 1,
                            0,
                            types.length);
                    if (response >= 1) {
                        setUninstallType(
                            selected.getBeanId(),
                            types[response - 1]);
                    }
                }

                tty.printLine();

            } else {
                // assert response is 0 - continue installation
                //set selected products in database
                setSelectedProducts(context.getServices());
                setSelectedTypes(context.getServices());
                tty.setBaseIndent(0);
                break;
            }
        }
    }

    private String getUninstallTypeDisplayName(String type) {
        if (type.equals(PARTIAL_UNINSTALL)) {
            return LocalizedStringResolver.resolve(
                ProductResourcesConst.NAME,
                "UninstallTypePanel.partialDisplayName");
        } else {
            return LocalizedStringResolver.resolve(
                ProductResourcesConst.NAME,
                "UninstallTypePanel.completeDisplayName");
        }
    }

    private void setSelectedProducts(WizardServices wServices) {

        Vector v = new Vector();

        //build comma delimited list
        for (int i = 0; i < products.length; i++) {
            if (products[i].isActive()) {
                v.addElement(products[i].getBeanId());
            }
        }

        String[] selectedIds = new String[v.size()];
        v.copyInto(selectedIds);

        try {
            wServices.getISDatabase().setVariableValue(
                SELECTED_PRODUCTS_VAR,
                StringUtils.createCommaDelimitedString(selectedIds));
        } catch (ISDatabaseException dbe) {
            wServices.logEvent(this, Log.ERROR, dbe);
        } catch (ServiceException e) {
            wServices.logEvent(this, Log.ERROR, e);
        }

    }

    private DynamicProductReference[] readProductRefs(WizardServices wServices)
        throws ServiceException {
        Vector v = new Vector();

        // get the list of children of the root
        ProductService rootService =
            (ProductService)wServices.getService(ProductService.NAME);
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
                        wServices.getWizardServices(installer);
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
                    wServices.logEvent(this, Log.ERROR, e);
                }
            } else {
                wServices.logEvent(
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
                wServices.logEvent(this, Log.ERROR, e);
            }
        }
        return ret;
    }

    private void setProductRefSelectionStates(
        DynamicProductReference[] productRefs,
        WizardServices wServices)
        throws ServiceException {

        ISDatabaseDef isDb = wServices.getISDatabase().getDatabaseDef();

        String selectedProdStr = isDb.getVariableValue(SELECTED_PRODUCTS_VAR);
        String[] selProducts = new String[0];

        if ((selectedProdStr != null)
            && (selectedProdStr.trim().length() != 0)) {
            selProducts =
                StringUtils.parseCommaDelimitedString(selectedProdStr);

            for (int i = 0; i < productRefs.length; i++) {
                boolean selected = false;
                for (int j = 0; !selected && j < selProducts.length; j++) {
                    if (productRefs[i].getBeanId().equals(selProducts[j])) {
                        selected = true;
                    }
                }
                productRefs[i].setActive(selected);
            }
        }
    }

    private void setSelectedTypes(WizardServices wServices) {

        Vector v = new Vector();

        //build comma delimited list
        // NOTE: Type info is "<beanId>;<type>" within the array
        for (int i = 0; i < products.length; i++) {
            String uninstallType = getUninstallType(products[i].getBeanId());
            if (uninstallType == null) {
                uninstallType = COMPLETE_UNINSTALL;
            }
            v.addElement(
                products[i].getBeanId() + TYPE_DELIMITER + uninstallType);
        }

        String[] types = new String[v.size()];
        v.copyInto(types);

        try {
            wServices.getISDatabase().setVariableValue(
                SELECTED_TYPES_VAR,
                StringUtils.createCommaDelimitedString(types));
        } catch (ISDatabaseException dbe) {
            wServices.logEvent(this, Log.ERROR, dbe);
        } catch (ServiceException e) {
            wServices.logEvent(this, Log.ERROR, e);
        }

    }

    private void refreshProductRefSelectionTypes(WizardServices wServices) {

        try {
            ISDatabaseDef isDb = wServices.getISDatabase().getDatabaseDef();

            String selectedTypeStr = isDb.getVariableValue(SELECTED_TYPES_VAR);

            // NOTE: Type info is "<beanId>;<type>" within the array
            String[] typeInfo = new String[0];

            if ((selectedTypeStr != null)
                && (selectedTypeStr.trim().length() != 0)) {
                typeInfo =
                    StringUtils.parseCommaDelimitedString(selectedTypeStr);
                for (int i = 0; typeInfo != null && i < typeInfo.length; i++) {
                    String info = typeInfo[i];
                    String beanId;
                    String uninstType;
                    int index = info.indexOf(TYPE_DELIMITER);
                    if (index >= 0) {
                        beanId = info.substring(0, index);
                        uninstType = info.substring(index + 1);
                    } else {
                        // NOTE: This really should never happen
                        beanId = info;
                        uninstType = COMPLETE_UNINSTALL;
                    }
                    setUninstallType(beanId, uninstType);
                }
            }
        } catch (ServiceException e) {
            wServices.logEvent(this, Log.ERROR, e);
            wServices.logEvent(
                this,
                Log.ERROR,
                "cannot update product uninstall selection types");
        }
    }

    private DynamicProductReference getProduct(String beanId) {
        for (int i = 0; i < products.length; i++) {
            if (products[i].getBeanId().equals(beanId)) {
                return products[i];
            }
        }
        return null;
    }

    /**
     * Updates the active status for all selected Products
     * @param wServices
     */
    public void updateProductActiveStates(WizardServices wServices) {

        try {
            ProductService service =
                (ProductService)wServices.getService(ProductService.NAME);
            for (int i = 0; i < products.length; i++) {
                // update product ref state
                try {
                    service.setProductBeanProperty(
                        ProductService.DEFAULT_PRODUCT_SOURCE,
                        products[i].getBeanId(),
                        "active",
                        new Boolean(products[i].isActive()));
                } catch (ServiceException e) {
                    wServices.logEvent(this, Log.ERROR, e);
                    wServices.logEvent(
                        this,
                        Log.ERROR,
                        "cannot update active state for product reference "
                            + products[i].getBeanId());
                }

                // update the product itself
                try {
                    WizardServices subServices =
                        wServices.getWizardServices(products[i].getInstaller());
                    ProductService subService =
                        (ProductService)subServices.getService(
                            ProductService.NAME);
                    subService.setProductBeanProperty(
                        ProductService.DEFAULT_PRODUCT_SOURCE,
                        null,
                        "activeForUninstall",
                        new Boolean(products[i].isActive()));
                } catch (ServiceException e) {
                    wServices.logEvent(this, Log.ERROR, e);
                    wServices.logEvent(
                        this,
                        Log.ERROR,
                        "cannot update active state for product reference "
                            + products[i].getBeanId());
                }
            }
        } catch (ServiceException e) {
            wServices.logEvent(this, Log.ERROR, e);
            wServices.logEvent(
                this,
                Log.ERROR,
                "cannot update product reference states");
        }
    }

    /**
     * Enables linking to the appropriate dialogs based on a product's selected
     * uninstallation type.  
     */
    public void createUninstallTypeRefSequence(
        Wizard wizard,
        WizardServices wServices) {

        // to enable linking into the appropriate sub-panels according to
        // a product's selected uninstall type, use a sequence that contains
        // wizard references to the sub-panels
        try {
            String seqId = getCurrentSequenceId(wServices);
            if (seqId != null && seqId.trim().length() > 0) {
                WizardTree tree = wizard.getWizardTree();
                WizardBean bean = tree.getBean(seqId);
                if (bean != null) {
                    tree.remove(bean);
                }
            }

            WizardSequence uninstTypeRefSequence = new WizardSequence();
            WizardBean selectionRef =
                findProductPanelSelectionReference(wizard);
            if (selectionRef == null) {
                System.out.println("Selectionref is null");
            }
            WizardSequence refParent =
                wizard.getWizardTree().getParent(selectionRef);
            int refIndex = wizard.getWizardTree().getChildIndex(selectionRef);
            wizard.getWizardTree().insert(
                refParent,
                refIndex + 1,
                uninstTypeRefSequence);
                
            setCurrentSequenceId(uninstTypeRefSequence.getBeanId(), wServices);

            // for each selected product that has a setup type panel
            for (int i = 0; i < products.length; i++) {
                String uninstallType =
                    getUninstallType(products[i].getBeanId());
                if (products[i].isActive()
                    && uninstallType != null
                    && uninstallType.equals(PARTIAL_UNINSTALL)) {
                    // search its wizard tree for a UninstallTypeSequence
                    try {
                        Wizard w =
                            wizard.getExternalWizard(
                                products[i].getInstaller());
                        WizardTreeIterator iter =
                            WizardTreeIteratorFactory.createClassTypeIterator(
                                w.getWizardTree(),
                                UninstallTypeSequence.class);
                        WizardBean cur = iter.getNext(iter.begin());
                        while (cur != iter.end()) {
                            // ASSERT - safe cast
                            UninstallTypeSequence seq =
                                (UninstallTypeSequence)cur;
                            if (seq.isActive()) {
                                // add reference to uninstTypeRefSequence
                                WizardBeanReference seqRef =
                                    new WizardBeanReference();
                                wizard.getWizardTree().add(
                                    uninstTypeRefSequence,
                                    seqRef);
                                seqRef.setBeanId(
                                    "__ref_to_"
                                        + w.getId()
                                        + "_"
                                        + seq.getBeanId());
                                seqRef.setWizardReference(
                                    products[i].getInstaller());
                                seqRef.setBeanIdReference(seq.getBeanId());
                            }
                            cur = iter.getNext(cur);
                        }
                    } catch (WizardException e) {
                        // this should not happen -- we've already opened the wizard
                        // by this time so any error would likely have been caught
                        // earlier
                        wizard.getServices().logEvent(this, Log.ERROR, e);
                    }
                }
            }
        } catch (OperationRejectedException e) {
            wizard.getServices().logEvent(this, Log.ERROR, e);
        } catch (ServiceException e) {
            wizard.getServices().logEvent(this, Log.ERROR, e);
        } catch (ISDatabaseException e) {
            wizard.getServices().logEvent(this, Log.ERROR, e);
        }
    }

    private void setCurrentSequenceId(String seqId, WizardServices wServices)
        throws ServiceException, ISDatabaseException {

        wServices.getISDatabase().setVariableValue(REF_ID_VAR, seqId);
    }

    private String getCurrentSequenceId(WizardServices wServices)
        throws ServiceException, ISDatabaseException {

        return wServices.getISDatabase().getVariableValue(REF_ID_VAR);
    }

    /**
     * Searches the current product tree for a wizard bean of type
     * ProductSelectionReference that has a productPanel property
     * equals to this panel's beanId. If cannot find this bean, returns this.
     */
    private WizardBean findProductPanelSelectionReference(Wizard wizard) {
        WizardTreeIterator iter =
            WizardTreeIteratorFactory.createClassTypeIterator(
                wizard.getWizardTree(),
                ProductPanelSelectionReference.class);
        WizardBean cur = iter.getNext(iter.begin());

        while (cur != iter.end()) {
            // ASSERT - safe cast
            ProductPanelSelectionReference ref =
                (ProductPanelSelectionReference)cur;
            if ((ref.getProductDialog().trim().length() == 0)
                || (ref
                    .getProductDialog()
                    .equals(wizard.getCurrentBean().getBeanId()))) {
                return cur;
            }
            cur = iter.getNext(cur);
        }
        return null;
    }

    private void verifyData(Wizard wizard) {

        if (!dataInitialized) {
            try {
                products = readProductRefs(wizard.getServices());
            } catch (Exception e) {
                products = new DynamicProductReference[0];
                wizard.getServices().logEvent(this, Log.ERROR, e);
            }
            //installationTypePanels = readInstallationTypes(wizard);
            dataInitialized = true;
        }
    }
}