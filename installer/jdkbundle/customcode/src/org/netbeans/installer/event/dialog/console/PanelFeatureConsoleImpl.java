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


import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import com.installshield.event.ui.*;
import com.installshield.event.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.console.*;
import com.installshield.product.service.product.*;
import com.installshield.product.*;
import com.installshield.util.*;

public class PanelFeatureConsoleImpl {

    private static final String DESCRIPTION =
        "$L(com.installshield.product.i18n.ProductResources,FeaturePanel.description,$P(displayName))";

    public void consoleInteractionFeature(ISDialogContext context) {
        TTYDisplay tty = ((ConsoleWizardUI)context.getWizardUI()).getTTY();
        WizardServices wServices = context.getServices();

        ProductTree productTree = getProductTree(wServices);
        ProductBean curBean = productTree.getRoot();
        ProductService service;
        try {
            service = (ProductService)context.getService(ProductService.NAME);
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
            return;
        }

        while (true) {

            // header/instructions
            tty.setBaseIndent(0);
            tty.printLine(wServices.resolveString(DESCRIPTION));
            tty.printLine();

            // context
            if (curBean != productTree.getRoot() || !isRootSelectable()) {
                printContext(tty, productTree, curBean, wServices);
            }
            else {
                printSelectableRoot(tty, productTree, curBean, wServices);
            }

            // list of features
            tty.printLine();
            ProductBean[] features =
                printFeatures(tty, productTree, curBean, wServices);

            // other options
            tty.printLine();
            printOtherOptions(tty, productTree, curBean, wServices);

            // prompt
            tty.printLine();
            int response =
                tty.promptInt(
                    LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "FeaturePanel.consoleChooseAction"),
                    0,
                    productTree.getParent(curBean) != null
                        || isRootSelectable() ? -1 : 0,
                    features.length);
            tty.printLine();

            boolean toggle = false;
            ProductBean selected = null;

            if (response >= 1) {

                // display selected feature -- assert response - 1 is in range
                selected = features[response - 1];

                if (beanHasChildren(productTree, selected)) {
                    // if selected has children, prompt as to what to do
                    tty.printLine();
                    printFeatureSelectOptions(
                        tty,
                        selected,
                        productTree,
                        wServices);
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
                    }
                    else if (response == 2) {
                        // user wants to view selected
                        curBean = selected;
                    }
                }
                else {
                    // otherwise just toggle the selection without prompting
                    toggle = true;
                }

            }
            else if (response == -1) {

                if (curBean != productTree.getRoot()) {
                    // display curBean's parent -- assert parent is not null
                    curBean = productTree.getParent(curBean);
                }
                else {
                    // user wants to toggle the product root
                    // assert isRootSelectable() is true
                    selected = curBean;
                    toggle = true;
                }

            }
            else {

                // assert response is 0 - continue installation
                tty.setBaseIndent(0);
                break;
            }

            if (toggle) {
                // toggle bean state and refresh data
                // assert selected is not null
                setBeanActive(
                    service,
                    selected.getBeanId(),
                    !isBeanActive(productTree, selected.getBeanId()));
                productTree = getProductTree(wServices);
                curBean = productTree.getBean(curBean.getBeanId());
            }

            tty.printLine();
        }

    }
    
    
    /**
     * Called when panel is displayed in console mode when "options-record" or 
     * "options-template" command line option is used.
     */
	public void generateOptionsEntriesFeature(ISOptionsContext context) {
		
		ProductTree tree = null;
		try {
			ProductService service = (ProductService)context.getService(ProductService.NAME);
			tree = getProductTree(context.getServices());
		} catch (ServiceException e) {
			LogUtils.getLog().logEvent(this, Log.ERROR, e);
			return;
		}
		
		// assert tree != null
		Vector entries = context.getOptionEntries();
		ProductTreeIterator iter = ProductTreeIteratorFactory.createFeatureIterator(tree.getRoot());
					
		for (ProductBean cur = iter.getNext(iter.begin()); cur != iter.end(); cur = iter.getNext(cur)) {
			// assert safe-cast
			ProductFeature feature = (ProductFeature)cur;
			String displayName = context.resolveString("$P(" + feature.getBeanId() + ".displayName)");
			// feature entry title
			String title = LocalizedStringResolver.resolve(
				"com.installshield.product.i18n.ProductResources", "FeaturePanel.oteTitle",
				new String[] { displayName });
			// setup type entry doc
			String doc = LocalizedStringResolver.resolve(
				"com.installshield.product.i18n.ProductResources", "FeaturePanel.oteDoc",
				new String[] { displayName, feature.getBeanId() });
			// setup type entry option
			String option ="-P " + feature.getBeanId() + ".active=";
			if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
				option += LocalizedStringResolver.resolve("com.installshield.wizard.i18n.WizardResources", "WizardBean.valueStr");
			} else {
				option += feature.isActive();
			}
			entries.addElement(new OptionsTemplateEntry(title, doc, option));
		}
		
	}

    private ProductTree getProductTree(WizardServices wizardServices) {
        ProductService service;
        ProductTree productTree = null;
        try {
            service =
                (ProductService)wizardServices.getService(ProductService.NAME);
            Properties filter = new Properties();
            filter.put("filter.condition", "true");
            filter.put("filter.selectedLocales", "true");
    		filter.put("filter.platform", "true");
            productTree =
                service.getSoftwareObjectTree(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    getRequiredFeatureProperties(), filter);
        }
        catch (ServiceException e) {
            wizardServices.logEvent(this, Log.ERROR, e);
        }
        return productTree;
    }

    private String[] getRequiredFeatureProperties() {
        return new String[] { "active", "displayName", "visible", "enabled" };
    }

    private boolean isRootSelectable() {
        return false;
    }

    private void printSelectableRoot(
        TTYDisplay tty,
        ProductTree tree,
        ProductBean root,
        WizardServices wServices) {

        // assert root == tree.getRoot()
        tty.printLine(
            "   ["
                + (isBeanActive(tree, root.getBeanId()) ? "x" : " ")
                + "] "
                + wServices.resolveString(root.getDisplayName()));
        tty.setBaseIndent(tty.getBaseIndent() + TTYDisplay.DEFAULT_INDENT);
    }

    private void printContext(
        TTYDisplay tty,
        ProductTree tree,
        ProductBean context,
        WizardServices wServices) {

        Stack stack = new Stack();
        for (ProductBean bean = context;
            bean != null;
            bean = tree.getParent(bean)) {
            stack.push(bean);
        }

        while (!stack.empty()) {
            ProductBean cur = (ProductBean)stack.pop();
            String displayName = wServices.resolveString(cur.getDisplayName());
            if (tree.getParent(cur) == null) {
                tty.printLine("   " + displayName);
            }
            else {
                tty.printLine(" - " + displayName);
            }
            tty.setBaseIndent(tty.getBaseIndent() + TTYDisplay.DEFAULT_INDENT);
        }
    }

    private ProductBean[] printFeatures(
        TTYDisplay tty,
        ProductTree tree,
        ProductBean parent,
        WizardServices wServices) {

        int index = 1;
        ProductTreeIterator iter =
            ProductTreeIteratorFactory.createChildIterator(parent);

        boolean featureOptionsPrinted = false;
        Vector displayed = new Vector();
        ProductService service;

        try {
            service = (ProductService)wServices.getService(ProductService.NAME);
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
            return null;
        }

        for (ProductBean child = iter.getNext(iter.begin());
            child != iter.end();
            child = iter.getNext(child)) {
            if (canDisplayBean(tree, child)) {

                // instructions
                if (!featureOptionsPrinted) {
                    tty.printLine(
                        LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "FeaturePanel.consoleFeatureOptions"));
                    tty.printLine();
                    featureOptionsPrinted = true;
                }

                // line format: n._+_[x]_Display Name				

                StringBuffer line = new StringBuffer();
                line.append(formatOption(index++));
                line.append(".");

                if (beanHasChildren(tree, child)) {
                    line.append(" +");
                }
                else {
                    line.append("  ");
                }

                boolean required =
                    isBeanRequired(service, tree, child.getBeanId());
                if (required || isBeanActive(tree, child.getBeanId())) {
                    line.append("[x] ");
                }
                else {
                    line.append("[ ] ");
                }
                line.append(
                    getNodeCaption(
                        wServices.resolveString(child.getDisplayName()),
                        required,
                        isBeanInstalled(service, child.getBeanId()),
                        !isBeanEnabled(tree, child.getBeanId())));

                tty.printLine(line.toString());

                displayed.addElement(child);
            }
        }

        ProductBean[] ret = new ProductBean[displayed.size()];
        displayed.copyInto(ret);
        return ret;
    }

    private void printOtherOptions(
        TTYDisplay tty,
        ProductTree tree,
        ProductBean parent,
        WizardServices wServices) {

        boolean hasParent = tree.getParent(parent) != null;

        tty.printLine(
            LocalizedStringResolver.resolve(
                "com.installshield.product.i18n.ProductResources",
                "FeaturePanel.consoleOtherOptions"));
        tty.printLine();

        // view parent option
        if (hasParent) {
            tty.printLine(
                formatOption(-1)
                    + ". "
                    + LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "FeaturePanel.viewFeaturesParent"));

            // toggle root selection option
        }
        else if (parent == tree.getRoot() && isRootSelectable()) {
            String displayName =
                wServices.resolveString(parent.getDisplayName());
            if (isBeanActive(tree, parent.getBeanId())) {
                tty.printLine(
                    formatOption(-1)
                        + ". "
                        + LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "FeaturePanel.deselectFeature",
                            new String[] { displayName }));
            }
            else {
                tty.printLine(
                    formatOption(-1) + ". " + getSelectOption(displayName));
            }
        }

        // continue option
        tty.printLine(formatOption(0) + ". " + getContinueOption());
    }

    private String formatOption(int option) {
        if (option >= 10) {
            return " " + option;
        }
        else if (option >= 0) {
            return "  " + option;
        }
        else {
            return " " + option;
        }
    }

    private String getContinueOption() {
        return LocalizedStringResolver.resolve(
            "com.installshield.product.i18n.ProductResources",
            "FeaturePanel.continueInstalling");
    }

    private String getSelectOption(String beanDisplayName) {
        return LocalizedStringResolver.resolve(
            "com.installshield.product.i18n.ProductResources",
            "FeaturePanel.selectFeature",
            new String[] { beanDisplayName });
    }

    private boolean beanHasChildren(ProductTree tree, ProductBean bean) {

        ProductTreeIterator iter =
            ProductTreeIteratorFactory.createChildIterator(bean);
        for (ProductBean child = iter.getNext(iter.begin());
            child != iter.end();
            child = iter.getNext(child)) {
            if (canDisplayBean(tree, child)) {
                return true;
            }
        }
        return false;
    }

    private boolean canDisplayBean(ProductTree tree, ProductBean bean) {
        return (bean instanceof ProductFeature || bean instanceof Product)
            && isBeanVisible(tree, bean.getBeanId());
    }

    private void printFeatureSelectOptions(
        TTYDisplay tty,
        ProductBean bean,
        ProductTree tree,
        WizardServices wServices) {

        String displayName = wServices.resolveString(bean.getDisplayName());

        if (isBeanActive(tree, bean.getBeanId())) {
            tty.printLine(
                " 1. "
                    + LocalizedStringResolver.resolve(
                        "com.installshield.product.i18n.ProductResources",
                        "FeaturePanel.deselectFeature",
                        new String[] { displayName }));
        }
        else {
            tty.printLine(" 1. " + getSelectOption(displayName));
        }
        tty.printLine(
            " 2. "
                + LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "FeaturePanel.viewSubfeatures",
                    new String[] { displayName }));

    }

    private boolean isBeanActive(ProductTree tree, String beanId) {
        ProductBean bean = tree.getBean(beanId);
        return bean != null ? bean.isActive() : false;
    }
    
    private boolean isBeanEnabled(ProductTree tree, String beanId) {
        boolean enabled = true;
        ProductBean bean = tree.getBean(beanId);
        if (bean instanceof ProductFeature) {
            enabled = ((ProductFeature) bean).isEnabled();
        }
        return enabled;
    }


    private boolean isBeanVisible(ProductTree tree, String beanId) {

        ProductBean bean = tree.getBean(beanId);
        if (bean instanceof Product) {
            return bean != tree.getRoot() && ((Product)bean).isVisible();
        }
        else if (bean instanceof ProductFeature) {
            return ((ProductFeature)bean).isVisible();
        }
        else {
            return false;
        }
    }

    private boolean isBeanInstalled(ProductService service, String beanId) {

        Boolean installed;

        try {
            installed =
                (Boolean)service.getProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    beanId,
                    "installed");
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
            LogUtils.getLog().logEvent(
                this,
                Log.WARNING,
                "could not determine installed state for bean "
                    + beanId
                    + " - assuming not installed");
            installed = Boolean.FALSE;
        }
        return installed.booleanValue();
    }

    private boolean isBeanRequired(
        ProductService service,
        ProductTree tree,
        String beanId) {
        try {
            // if the bean isn't selected for installation AND it appears in the list of objects being
            // installed, then we know implicitly that it's required by something
            ProductBean bean = tree.getBean(beanId);
            if (bean != null && !bean.isActive()) {
                SoftwareObject[] installed = getInstallSequence(service);
                for (int i = 0; i < installed.length; i++) {
                    // assert safe cast
                    if (((ProductBean)installed[i])
                        .getBeanId()
                        .equals(beanId)) {
                        return true;
                    }
                }
            }
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }

        return false;

    }

    private SoftwareObject[] getInstallSequence(ProductService service)
        throws ServiceException {

        return service.getCurrentSoftwareObjectInstallSequence(
            ProductService.DEFAULT_PRODUCT_SOURCE);
    }

    private void setBeanActive(
        ProductService service,
        String beanId,
        boolean active) {
        try {
            service.setProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                beanId,
                "active",
                new Boolean(active));
        }
        catch (ServiceException e) {
            // TODO: notify user
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    private String getNodeCaption(
        String displayName,
        boolean required,
        boolean installed,
        boolean disabled) {

        StringBuffer buf = new StringBuffer();
        buf.append(displayName);
        if (required) {
            buf.append(" ");
            buf.append(
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "FeaturePanel.requiredLabel"));
        }
        if (installed) {
            buf.append(" ");
            buf.append(
                LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "FeaturePanel.installedLabel"));
        }
        if (disabled) {
            buf.append(" ");
            buf.append(
                    LocalizedStringResolver.resolve(
                            "com.installshield.product.i18n.ProductResources",
                            "FeaturePanel.disabledLabel"));
        }
        return buf.toString();
    }

}