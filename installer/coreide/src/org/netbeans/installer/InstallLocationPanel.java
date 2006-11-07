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

package org.netbeans.installer;

import com.installshield.product.service.product.ProductService;
import com.installshield.product.wizardbeans.DestinationPanel;
import com.installshield.util.Log;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.service.ServiceException;

import java.io.File;

public class InstallLocationPanel extends DestinationPanel {
    
    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";
    
    public boolean queryEnter (WizardBeanEvent event) {
        logEvent(this, Log.DBG, "queryEnter ENTER");
        try {
            ProductService service = (ProductService) getService(ProductService.NAME);
            String destination = (String) service.getProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE, null, "installLocation");
            
            if (!Util.isWindowsOS()) {
                File root = new File("/");
                if (!root.canWrite()) {
                    service.setProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    null,
                    "installLocation",
                    resolveString(BUNDLE + "Product.installLocationForNonRoot)"));
                }
            } else {
                service.setProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                null,
                "installLocation",
                resolveString(BUNDLE + "Product.installLocationWindows)"));
            }
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        
        return super.queryEnter(event);
    }

    public void exited (WizardBeanEvent event) {
        logEvent(this, Log.DBG, "exited ENTER");
        super.exited(event);
        try {
            //Set install location for storage builder
            ProductService service = (ProductService)getService(ProductService.NAME);
            String productDestination = (String) service.getProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE, null, "installLocation");
            logEvent(this, Log.DBG, "exited productDestination: " + productDestination);
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
    }
    
    public void execute (WizardBeanEvent event) {
        logEvent(this, Log.DBG, "execute ENTER");
        super.execute(event);
        try {
            //Set install location for storage builder
            ProductService service = (ProductService)getService(ProductService.NAME);
            String productDestination = (String) service.getProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE, null, "installLocation");
            logEvent(this, Log.DBG, "execute productDestination: " + productDestination);
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
    }
    
}
