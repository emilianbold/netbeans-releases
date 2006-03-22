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


import java.io.*;
import java.util.Stack;
import java.util.Vector;
import com.installshield.event.ui.*;
import com.installshield.event.*;
import com.installshield.ui.controls.*;
import com.installshield.util.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.service.file.*;
import com.installshield.product.service.product.*;
import com.installshield.database.*;

public class PanelDestination {

    private static final String ABSOLUTE_INSTALL_LOCATION = "absoluteInstallLocation";
    private static final String INSTALL_LOCATION = "installLocation";
    private static final String BROWSE_CONTROL_ID = "browseControl";
    private static final String DESTINATION_VAR_NAME = "IS_DESTINATION";
    private static final String RETURN_VAL = "RETURN_VAL";

    public void queryEnterDestination(ISQueryContext context) {
        String installLocation = null;
        /*
         * Get default installLocation from browse control var. If null, then
         * get from absoluteInstallLocation property on product.
         */
        try {
            installLocation = context.getServices().getISDatabase()
                    .getVariableValue(DESTINATION_VAR_NAME);
            if ((installLocation == null)
                    || (installLocation.trim().length() == 0)) {
                installLocation = getInstallLocation((ProductService) context
                        .getService(ProductService.NAME));
                context.getServices().getISDatabase().setVariableValue(
                        DESTINATION_VAR_NAME, installLocation);
                context.getServices().getISDatabase().setVariableValue(
                        RETURN_VAL, "true");
            }
        } catch (ISDatabaseException dbe) {
        } catch (ServiceException e) {
        }
    }

    public void queryExitDestination(ISDialogQueryContext context) {
        /*
         * Set value entered in browse control as install location on Product
         * Service
         */
        try {
            ISBrowseControl browseControl = context.getISPanel()
                    .getBrowseControl(BROWSE_CONTROL_ID);
            if (browseControl != null) {
                String installPath = browseControl.getFile();
                //validate
                if ((installPath != null)
                        && (validateDestination(context.getWizard(),
                                installPath))) {
                    updateProductTree((ProductService) context
                            .getService(ProductService.NAME), installPath);
                    context.setReturnValue(true);
                    return;
                } else {
                    context.getServices().getISDatabase().setVariableValue(
                            RETURN_VAL, "false");
                    browseControl.setFile(installPath);
                }
            }
        } catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        } catch (ISDatabaseException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
        context.setReturnValue(false);
    }

    /**
     * Called when panel is displayed in console mode when "options-record" or
     * "options-template" command line option is used.
     */
    public void generateOptionsEntriesDestination(ISOptionsContext context) {
        String value = null;
        String option = null;
        String panelId = context.getPanel().getName();
        Vector optionEntries = context.getOptionEntries();
        try {
            ProductService pService = (ProductService) context
                    .getService(ProductService.NAME);
            if (context.getValueType() == WizardBean.TEMPLATE_VALUE) {
                value = LocalizedStringResolver.resolve(
                        "com.installshield.wizard.i18n.WizardResources",
                        "WizardBean.valueStr");
            } else {
                value = getInstallLocation(pService);
            }
            option = "-P installLocation=\"" + value + "\"";
            String productName = context.resolveString("$P(displayName)");
            String title = LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "DestinationPanel.ote1Title", new String[]{productName});
            String doc = LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "DestinationPanel.ote1Doc");
            optionEntries.addElement(new OptionsTemplateEntry(title, doc,
                    option));
        } catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

    private void showLocalizedErrorMsg(Wizard wizard, String bundle,
            String titleKey, String msgKey, String[] params) {
        try {
            String msg;
            String title = LocalizedStringResolver.resolve(bundle, titleKey);
            if (params.length != 0)
                msg = LocalizedStringResolver.resolve(bundle, msgKey, params);
            else
                msg = LocalizedStringResolver.resolve(bundle, msgKey);
            wizard.getServices().displayUserMessage(title, msg,
                    WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }

    private boolean validateDestination(Wizard wizard, String destination) {
        // There are three ways a directory can be invalid:
        //
        //  1 - If it is blank or is all white-space
        //  2 - If the file service rejects it as valid
        //  3 - If it cannot be created
        // blank/all white-space check
        if (StringUtils.isWhitespace(destination)) {
            showLocalizedErrorMsg(wizard,
                    "com.installshield.product.i18n.ProductResources",
                    "DestinationPanel.destinationDirectory",
                    "DestinationPanel.specifyDirectory", new String[0]);
            return false;
        }
        try {
            FileService fileService = (FileService) wizard.getServices()
                    .getService(FileService.NAME);
            // validate file name
            fileService.validateFileName(destination);
            // writable check
            if (!isDirectoryWritable(fileService, destination)) {
                showLocalizedErrorMsg(wizard,
                        "com.installshield.product.i18n.ProductResources",
                        "DestinationPanel.destinationDirectory", "notWritable",
                        new String[]{destination});
                return false;
            }
        } catch (ServiceException e) {
            showErrorMsg(wizard, LocalizedStringResolver.resolve(
                    "com.installshield.product.i18n.ProductResources",
                    "DestinationPanel.destinationDirectory"), e.getMessage());
            return false;
        }
        // all checks pass, directory is ok
        return true;
    }

    private boolean isDirectoryWritable(FileService fileService,
            String destination) {
        try {
            // check if the directory already existed and has write permissions
            if (fileService.fileExists(destination)
                    && !fileService.isDirectoryWritable(destination)) {
                throw new IOException("Directory exists and its read-only");
            }
            // create a stack of directory parents for destination -- we'll
            // create and delete
            // any that don't exist to ensure that we can write to destination
            Stack parents = new Stack();
            String cur = destination;
            while (cur != null && !fileService.fileExists(cur)) {
                parents.push(cur);
                cur = fileService.getParent(cur);
            }
            // create any parents that don't exist, keeping track of the dirs we
            // create
            Vector created = new Vector();
            while (!parents.isEmpty()) {
                String parent = (String) parents.pop();
                fileService.createDirectory(parent);
                created.addElement(parent);
            }
            // delete any directories we created
            for (int i = created.size() - 1; i >= 0; i--) {
                fileService.deleteDirectory((String) created.elementAt(i));
            }
            // we were able to write to create destination
            return true;
        } catch (Exception e) {
            LogUtils.getLog().logEvent(this, Log.WARNING, e.getMessage());
            return false;
        }
    }

    private void showErrorMsg(Wizard wizard, String title, String msg) {
        try {
            wizard.getServices().displayUserMessage(title, msg,
                    WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }

    private String getInstallLocation(ProductService service) {
        try {
            if (service == null) {
                return null;
            }
            return (String) service.getProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE, null,
                    ABSOLUTE_INSTALL_LOCATION);
        } catch (ServiceException e) {
            return null;
        }
    }

    private void updateProductTree(ProductService service, String destination) {
        try {
            service.setProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE, null,
                    INSTALL_LOCATION, destination);
        } catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }
}