/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            String sbDestination = productDestination + File.separator + "_uninst" + File.separator + "storagebuilder"; 
            logEvent(this, Log.DBG, "exited productDestination: " + productDestination);
            logEvent(this, Log.DBG, "exited sbDestination: " + sbDestination);
            service.setRetainedProductBeanProperty(ProductService.DEFAULT_PRODUCT_SOURCE,
            Names.STORAGE_BUILDER_ID, "installLocation", sbDestination);
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
            String sbDestination = productDestination + File.separator + "_uninst" + File.separator + "storagebuilder"; 
            logEvent(this, Log.DBG, "execute productDestination: " + productDestination);
            logEvent(this, Log.DBG, "execute sbDestination: " + sbDestination);
            service.setRetainedProductBeanProperty(ProductService.DEFAULT_PRODUCT_SOURCE,
            Names.STORAGE_BUILDER_ID, "installLocation", sbDestination);
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
    }
    
}
