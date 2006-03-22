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


import java.util.Vector;
import java.util.Properties;

import com.installshield.event.ui.*;
import com.installshield.event.*;
import com.installshield.wizard.*;
import com.installshield.product.wizardbeans.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.product.service.product.*;
import com.installshield.product.*;
import com.installshield.database.designtime.*;
import com.installshield.database.*;
import com.installshield.util.*;
import com.installshield.wizardx.panels.*;

public class PanelProductConsoleImpl {

    private static final String DESCRIPTION =
        "$L(com.installshield.product.i18n.ProductResources, ProductPanel.selectProduct)";

    private static final String SELECTED_PRODUCTS_VAR = "IS_SELECTED_PRODUCTS";
    private static final String REF_ID_VAR = "IS_REF_ID";
    private static final String INSTALLTYPE_INTERNAL_NAME = "InstallType";

    private double col0Width = 0.67;
    private double col1Width = 0.33;

    private DynamicProductReference[] products = new DynamicProductReference[0];
    private Object[] installationTypePanels = new Object[0];
    private boolean dataInitialized = false;

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

        try {
            products = ProductServiceUtils.readProductRefs(this,context.getServices());
            setProductRefSelectionStates(products, context.getServices());
            installationTypePanels = readInstallationTypes(context.getWizard());

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

            updateProductActiveStates(context.getServices());
            updateProductFeatureStates(context.getServices());
            createSetupTypeRefSequence(
                context.getWizard(),
                context.getServices());
        } catch (ServiceException e) {
            context.getServices().logEvent(this, Log.ERROR, e);
        }
        context.setReturnValue(true);
    }

    public void consoleInteractionProduct(ISDialogContext context) {

        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        WizardServices wServices = context.getServices();

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
                    "ProductPanel.consoleProductOptions"));
            tty.printLine();

            // table header
            table.addRow(
                new String[] {
                    LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "ProductPanel.product"),
                    LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "ProductPanel.setupType")});
            table.addRow(
                new String[] {
                    StringUtils.createString(
                        '-',
                        (int) (tableWidth * col0Width)),
                    StringUtils.createString(
                        '-',
                        (int) (tableWidth * col1Width))});

            verifyData(context.getWizard());

            for (int i = 0; i < products.length; i++) {
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
                // col1: "<setup type display name>" - only display for active products
                StringBuffer col1 = new StringBuffer();
                if (products[i].isActive()) {
                    ISInstallationTypeDef setupType =
                        getInstallationType(
                            context.getWizard(),
                            products[i].getBeanId());
                    if (setupType != null) {
                        col1.append(
                            " "
                                + MnemonicString.stripMn(
                                    context.resolveString(
                                        setupType.getDisplayName())));
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
                        "FeaturePanel.continueInstalling"));

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

                // display selected feature -- assert response - 1 is in range
                DynamicProductReference selected = products[response - 1];

                boolean toggle = false;
                boolean changeSetupType = false;

                if (selected.isActive()
                    && getInstallationType(
                        context.getWizard(),
                        selected.getBeanId())
                        != null) {
                    // if selected is active and has setup type, prompt as to what to do
                    tty.printLine();
                    String displayName =
                        MnemonicString.stripMn(
                            context.resolveString(selected.getDisplayName()));
                    tty.printLine(
                        " 1. "
                            + LocalizedStringResolver.resolve(
                                "com.installshield.product.i18n.ProductResources",
                                "ProductPanel.consoleDeselectProduct",
                                new String[] { displayName }));
                    tty.printLine(
                        " 2. "
                            + LocalizedStringResolver.resolve(
                                "com.installshield.product.i18n.ProductResources",
                                "ProductPanel.consoleChangeSetupType",
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
                        // user wants to change setup type
                        changeSetupType = true;
                    }
                } else {
                    // otherwise just toggle the selection without prompting
                    toggle = true;
                }

                if (toggle) {
                    // toggle bean state
                    selected.setActive(!selected.isActive());
                }

                if (changeSetupType) {
                    tty.printLine();
                    tty.printLine(
                        LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "ProductPanel.consoleProductSetupTypes",
                            new String[] {
                                MnemonicString.stripMn(
                                    context.resolveString(
                                        selected.getDisplayName()))}));
                    tty.printLine();
                    ISInstallationTypeDef[] types =
                        getInstallationTypes(
                            context.getWizard(),
                            selected.getBeanId());
                    // assert types != null
                    int curType = -1;
                    String selectedType =
                        getSelectedInstallationTypeId(
                            context.getWizard(),
                            selected.getBeanId());
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
                                    types[i].getDisplayName())));
                        tty.printLine(buf.toString());
                        if (types[i].getName().equals(selectedType)) {
                            curType = i;
                        }
                    }
                    tty.printLine();
                    response =
                        tty.promptInt(
                            LocalizedStringResolver.resolve(
                                "com.installshield.product.i18n.ProductResources",
                                "ProductPanel.consoleSelectSetupType"),
                            curType + 1,
                            0,
                            types.length);
                    if (response >= 1) {
                        setSelectedInstallationTypeId(
                            context.getWizard(),
                            selected.getBeanId(),
                            types[response - 1].getName());
                    }
                }

                tty.printLine();

            } else {
                // assert response is 0 - continue installation
                //set selected products in database
                setSelectedProducts(context.getServices());
                tty.setBaseIndent(0);
                break;
            }
        }
    }

    private void setSelectedProducts(WizardServices wServices) {
        String selectedProductsStr = "";

        //build comma delimited list
        for (int i = 0; i < products.length; i++) {
            if (products[i].isActive()) {
                selectedProductsStr += "," + products[i].getBeanId();
            }
        }

        selectedProductsStr = selectedProductsStr.substring(1);
        try {
            wServices.getISDatabase().setVariableValue(
                SELECTED_PRODUCTS_VAR,
                selectedProductsStr);
        } catch (ISDatabaseException dbe) {
            wServices.logEvent(this, Log.ERROR, dbe);
        } catch (ServiceException e) {
            wServices.logEvent(this, Log.ERROR, e);
        }

    }

    private ISInstallationTypeDef getInstallationType(
        Wizard wizard,
        String productBeanId) {
        ISInstallationTypeDef[] types =
            getInstallationTypes(wizard, productBeanId);
        String selectedId =
            getSelectedInstallationTypeId(wizard, productBeanId);

        if (types != null) {
            if ((selectedId == null) || (selectedId.trim().length() == 0)) {
                return types[0];
            } else {
                for (int i = 0; types != null && i < types.length; i++) {
                    if (types[i].getName().equals(selectedId)) {
                        return types[i];
                    }
                }
                //no match, return first
                return types[0];
            }
        } else
            return null;
    }

    private Object getInstallationTypePanel(String productBeanId) {
        // assert setupTypePanels length is same as products length

        for (int i = 0; i < products.length; i++) {
            if (products[i].getBeanId().equals(productBeanId)) {
                return installationTypePanels[i];
            }
        }
        return null;
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

    private DynamicProductReference getProduct(String beanId) {
        for (int i = 0; i < products.length; i++) {
            if (products[i].getBeanId().equals(beanId)) {
                return products[i];
            }
        }
        return null;
    }

    public ISInstallationTypeDef[] getInstallationTypes(
        Wizard wizard,
        String productBeanId) {

        verifyData(wizard);

        ISInstallationTypeDef[] installTypes = null;
        DynamicProductReference product = getProduct(productBeanId);
        if (product != null) {
            try {
                String installer = product.getInstaller();
                Wizard w = wizard.getExternalWizard(installer);
                installTypes =
                    w
                        .getServices()
                        .getISDatabase()
                        .getDatabaseDef()
                        .getInstallationTypes();
            } catch (Exception e) {
                wizard.getServices().logEvent(this, Log.ERROR, e);
            }
        }
        return installTypes;
    }

    private void setSelectedInstallationTypeId(
        Wizard wizard,
        String productBeanId,
        String setupTypeId) {

        verifyData(wizard);

        DynamicProductReference product = getProduct(productBeanId);
        try {
            String installer = product.getInstaller();
            Wizard w = wizard.getExternalWizard(installer);
            w
                .getServices()
                .getISDatabase()
                .getDatabaseDef()
                .setSelectedInstallationType(
                setupTypeId);
        } catch (Exception e) {
            wizard.getServices().logEvent(this, Log.ERROR, e);
        }

    }

    private String getSelectedInstallationTypeId(
        Wizard wizard,
        String productBeanId) {

        verifyData(wizard);

        String selectedInstallType = null;

        DynamicProductReference product = getProduct(productBeanId);
        if (product != null) {
            try {
                String installer = product.getInstaller();
                Wizard w = wizard.getExternalWizard(installer);
                selectedInstallType =
                    w
                        .getServices()
                        .getISDatabase()
                        .getDatabaseDef()
                        .getSelectedInstallationType();
            } catch (Exception e) {
                wizard.getServices().logEvent(this, Log.ERROR, e);
            }
        }
        return selectedInstallType;
    }

    private Object[] readInstallationTypes(Wizard wizard) {
        Object[] installTypePanels = new Object[products.length];

        for (int i = 0; i < products.length; i++) {
            String installer = products[i].getInstaller();

            try {
                Wizard childWizard = wizard.getExternalWizard(installer);

                //Use wizard iterator to find either a CustomDialog bean whose
                //internal name is "InstallType" or a legacy 5.x SetupTypePanel
                WizardTreeIterator iter =
                    WizardTreeIteratorFactory.createStandardIterator(
                        childWizard.getWizardTree());
                WizardBean cur = iter.getNext(iter.begin());
                while (cur != iter.end()) {
                    if (cur instanceof CustomDialog) {
                        String panelId = ((CustomDialog)cur).getPanelId();
                        if (panelId.equals(INSTALLTYPE_INTERNAL_NAME))
                            installTypePanels[i] = (CustomDialog)cur;
                    } else if (cur instanceof SetupTypePanel) {
                        installTypePanels[i] = (SetupTypePanel)cur;
                    }
                    cur = iter.getNext(cur);
                }
            } catch (WizardException e) {
                wizard.getServices().logEvent(
                    this,
                    Log.WARNING,
                    "cannot open " + installer);
                wizard.getServices().logEvent(this, Log.WARNING, e);
            }
        }

        return installTypePanels;
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
     * Updates the active state of all Feature associated with selected products
     * and selected installation types
     * @param wServices
     */
    public void updateProductFeatureStates(WizardServices wServices) {

        try {
            for (int i = 0; i < products.length; i++) {
                String installer = products[i].getInstaller();
                WizardServices subServices =
                    wServices.getWizardServices(installer);
                ISDatabaseDef db = subServices.getISDatabase().getDatabaseDef();
                String selectedInstallType =
                    subServices.resolveString(db.getSelectedInstallationType());
                ISInstallationTypeDef installType = null;

                ISInstallationTypeDef[] installTypes =
                    db.getInstallationTypes();
                for (int j = 0; j < installTypes.length; j++) {
                    if (installTypes[j]
                        .getName()
                        .equals(selectedInstallType)) {
                        installType = installTypes[j];
                        break;
                    }
                }

                String[] installTypeFeatures = installType.getFeatures();
                ProductService pService =
                    (ProductService)subServices.getService(ProductService.NAME);
                Properties[] features =
                    collectFeatureProperties(null, pService);
                if (installTypeFeatures.length > 0) {
                    for (int j = 0; j < features.length; j++) {
                        boolean active = false;
                        for (int k = 0;
                            !active && k < installTypeFeatures.length;
                            k++) {
                            if (features[j]
                                .getProperty("beanId")
                                .equals(installTypeFeatures[k])) {
                                active = true;
                            }
                        }
                        pService.setProductBeanProperty(
                            ProductService.DEFAULT_PRODUCT_SOURCE,
                            features[j].getProperty("beanId"),
                            "active",
                            new Boolean(active));
                    }
                }

                if (installationTypePanels[i] != null) {
                    //set the panel as inactive so it doesn't get visited -- this
                    // panel takes the place of the setup type panel
                     ((WizardBean)installationTypePanels[i]).setActive(false);
                }
            }
        } catch (ServiceException e) {
            wServices.logEvent(this, Log.ERROR, e);
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

    /**
     * Enables linking to the appropriate dialogs based on a product's selected
     * installation type.  
     *
     */
    public void createSetupTypeRefSequence(
        Wizard wizard,
        WizardServices wServices) {

        // to enable linking into the appropriate sub-panels according to
        // a product's selected setup type, use a sequence that contains
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

            WizardSequence setupTypeRefSequence = new WizardSequence();
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
                setupTypeRefSequence);

            setCurrentSequenceId(setupTypeRefSequence.getBeanId(), wServices);

            // for each selected product that has a setup type panel
            for (int i = 0; i < products.length; i++) {
                if (products[i].isActive()
                    && installationTypePanels[i] != null) {
                    // search its wizard tree for a SetupTypeSequence whose 
                    // setupTypePanel property equals the bean id if the product's 
                    // setup type panel and whose setupType property equals the 
                    // currently select setup type

                    try {
                        Wizard w =
                            wizard.getExternalWizard(
                                products[i].getInstaller());
                        WizardTreeIterator iter =
                            WizardTreeIteratorFactory.createClassTypeIterator(
                                w.getWizardTree(),
                                SetupTypeSequence.class);
                        WizardBean cur = iter.getNext(iter.begin());
                        while (cur != iter.end()) {
                            // ASSERT - safe cast
                            SetupTypeSequence seq = (SetupTypeSequence)cur;
                            if (seq
                                .getSetupTypePanel()
                                .equals(
                                    ((WizardBean)installationTypePanels[i])
                                        .getBeanId())
                                && seq.isActive()) {

                                // add refernce to setupTypeRefSequence
                                WizardBeanReference seqRef =
                                    new WizardBeanReference();
                                wizard.getWizardTree().add(
                                    setupTypeRefSequence,
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

    private Properties getFeatureFilter() {
        Properties featureFilter = new Properties();
        featureFilter.put("filter.class", ProductFeature.class.getName());
        return featureFilter;
    }

    private Properties getProductFilter() {
        Properties productFilter = new Properties();
        productFilter.put("filter.class", Product.class.getName());
        return productFilter;
    }

    private void verifyData(Wizard wizard) {

        if (!dataInitialized) {
            try {
                products = ProductServiceUtils.readProductRefs(this,wizard.getServices());
            } catch (Exception e) {
                products = new DynamicProductReference[0];
                wizard.getServices().logEvent(this, Log.ERROR, e);
            }
            //installationTypePanels = readInstallationTypes(wizard);
            dataInitialized = true;
        }
    }
}
