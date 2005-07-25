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


import com.installshield.event.ISSilentContext;
import com.installshield.product.service.product.ProductService;
import com.installshield.wizard.service.file.FileService;
import com.installshield.wizard.service.ServiceException;
import com.installshield.util.*;

public class PanelDestinationSilentImpl {
	
	private static final String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
	private static final String CREATE_DIR = "createDirectoryResponse";
	private static final String YES_RESPONSE = "yes";

    public void silentExecuteDestination(ISSilentContext context) {

        try {
            String productInstallLocation = "";
            ProductService pservice =
                (ProductService)context.getService(ProductService.NAME);
            productInstallLocation =
                (String)pservice.getProductBeanProperty(
                    productURL,
                    null,
                    "absoluteInstallLocation");
            FileService fileService = (FileService)context.getService(FileService.NAME);

            String resolvedDestination = "";
            if (productInstallLocation != null) {
                resolvedDestination = context.resolveString(productInstallLocation);
            }

            boolean dirExists = false;
            if (resolvedDestination.trim().length() > 0
                && !resolvedDestination.equals("")) {
                dirExists =
                    fileService.fileExists(resolvedDestination) ? true : false;
                if (!dirExists) {
                    if (context.getServices().getValue(CREATE_DIR)
                        != null) {
                        String response =
                            (String) (context.getWizard()
                                .getServices()
                                .getValue(CREATE_DIR));
                        if (response.equals(YES_RESPONSE)) {
                            fileService.createDirectory(resolvedDestination);
                        }
                    }
                }
            }
        }
        catch (ServiceException e) {
            LogUtils.getLog().logEvent(this, Log.ERROR, e);
        }
    }

}
