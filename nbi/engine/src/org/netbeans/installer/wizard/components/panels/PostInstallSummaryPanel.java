/*
 * PostInstallSummaryPanel.java
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

/**
 *
 * @author Kirill Sorokin
 */
public class PostInstallSummaryPanel extends TextPanel {
    public void initialize() {
        getBackButton().setEnabled(false);
        getCancelButton().setEnabled(false);
        
        String text = "We honestly hope that everything completed " +
                "successfully as no actual checks are performed for the " +
                "prototype. Sorry for any inconvenience caused.";
        
        setProperty(TEXT_PROPERTY, text);
        
        super.initialize();
    }    
}
