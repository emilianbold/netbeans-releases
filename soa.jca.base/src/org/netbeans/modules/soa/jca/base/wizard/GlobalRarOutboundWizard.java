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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base.wizard;

import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import java.awt.Component;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.soa.jca.base.generator.api.JavacTreeModel;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 * entry point for Global Rar Outbound wizard
 *
 * @author echou
 */
public class GlobalRarOutboundWizard implements WizardDescriptor.Iterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private JavacTreeModel javacTreeModel;
    private Project project;
    private String rarName;

    public GlobalRarOutboundWizard(JavacTreeModel javacTreeModel, Project project, String rarName) {
        this.javacTreeModel = javacTreeModel;
        this.project = project;
        this.rarName = rarName;
    }

    public Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length; // NOI18N
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void addChangeListener(ChangeListener arg0) {}

    public void removeChangeListener(ChangeListener arg0) {}

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            List<WizardDescriptor.Panel> panelList = new ArrayList<WizardDescriptor.Panel> ();

            GlobalRarProvider provider = GlobalRarRegistry.getInstance().getRar(rarName);

            if (provider.getOTDTypes() != null && provider.getOTDTypes().size() == 1) {
                // skip OTD selection step
            } else {
                panelList.add(new GlobalRarWizardPanelSelectOtd(project));
            }
            panelList.add(new GlobalRarWizardPanelResource(javacTreeModel, project, rarName));

            if (provider.getAdditionalConfig() == null || provider.getAdditionalConfig().size() == 0) {
                // skip Additional Config step
            } else {
                panelList.add(new GlobalRarWizardPanelAdditionalConfig());
            }

            panels = panelList.toArray(new WizardDescriptor.Panel[0] );

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
        return panels;
    }


}
