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
