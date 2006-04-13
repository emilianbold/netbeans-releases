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

package org.netbeans.installer;

import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.WizardBeanCondition;

import java.io.File;

public class ValidInstallLocationCondition extends WizardBeanCondition
{
    public String describe() {
        return "the specified installation location must be valid";
    }
    
    public String defaultName() {
        return "valid installation location";
    }
    
    protected boolean evaluateTrueCondition() {
        try {
            ProductService service = (ProductService)
            getWizardBean().getWizard().getServices().getService(ProductService.NAME);
            String installDir = (String) service.getProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    null,
                    "absoluteInstallLocation");
            
            File dir = new File(installDir);
            if (!dir.exists()) {
                return true;
            }
            if (!dir.isDirectory()) {
                return false;
            }
            
            String[] list = dir.list();
            return list.length == 0;
        } catch (Exception e) {
            logEvent(this, Log.ERROR, e);
        }
        return true;
    }
}
