/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.ArrayIterator;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Alexander Simon
 */
public final class DiscoveryWizardAction extends CallableSystemAction {
    
    private WizardDescriptor.Panel[] panels;
    
    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(getString("WIZARD_TITLE_TXT")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // do something
        }
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private InstantiatingIterator getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new SelectObjectFilesWizard(),
                new ConsolidationStrategyWizard(),
                new SelectConfigurationWizard(),
                new RemoveUnusedWizard()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
            
        }
        return new DiscoveryWizardIterator(panels);
    }
    
    public String getName() {
        return getString("ACTION_TITLE_TXT");
    }
    
    public String iconResource() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private String getString(String key) {
        return NbBundle.getBundle(DiscoveryWizardAction.class).getString(key);
    }
    
    public class DiscoveryWizardIterator extends ArrayIterator implements InstantiatingIterator {
        private WizardDescriptor wizard;
        /** Creates a new instance of DiscoveryWizardIterator */
        public DiscoveryWizardIterator(Panel[] panels ) {
            super(panels);
        }
        
        public Set instantiate() throws IOException {
            return DiscoveryProjectGenerator.makeProject(wizard);
        }
        
        public void initialize(WizardDescriptor wizard) {
            this.wizard = wizard;
        }
        
        public void uninitialize(WizardDescriptor wizard) {
            wizard.putProperty("rootFolder", null); // NOI18N
            wizard.putProperty("consolidationLevel", null); // NOI18N
            wizard.putProperty("configurations", null); // NOI18N
            wizard.putProperty("included", null); // NOI18N
            panels = null;
        }
    }
}

