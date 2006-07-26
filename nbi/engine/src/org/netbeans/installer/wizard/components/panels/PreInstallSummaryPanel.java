/*
 * PreInstallSummaryPanel.java
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

import java.util.List;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;

/**
 *
 * @author Kirill Sorokin
 */
public class PreInstallSummaryPanel extends TextPanel {
    public void initialize() {
        String text = "";
        
        List<ProductComponent> componentsToInstall = ProductRegistry.getInstance().getComponentsToInstall();
        if (componentsToInstall.size() > 0) {
            text += "You have chosen to install the following components:\n\n";
            for (ProductComponent component: componentsToInstall) {
                text += "    " + component.getDisplayName() + "\n";
            }
            
            text += "\n\n";
        }
        
        List<ProductComponent> componentsToUninstall = ProductRegistry.getInstance().getComponentsToUninstall();
        if (componentsToUninstall.size() > 0) {
            text += "You have chosen to uninstall the following components:\n\n";
            for (ProductComponent component: componentsToUninstall) {
                text += "    " + component.getDisplayName() + "\n";
            }
        }
        
        setProperty(TEXT_PROPERTY, text);
        
        super.initialize();
    }
}
