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

package org.netbeans.modules.soa.jca.base.inbound.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import org.netbeans.modules.soa.jca.base.Util;
import org.netbeans.modules.soa.jca.base.inbound.InboundGenerator;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * entry point for Global Rar Inbound Wizard
 *
 * @author echou
 */
public class GlobalRarInboundWizard implements WizardDescriptor.ProgressInstantiatingIterator {

    public static final String RAR_NAME_PROP = "RAR_NAME"; // NOI18N
    public static final String OTD_TYPE_PROP = "OTD_TYPE"; // NOI18N
    public static final String LISTENER_NAME_PROP = "LISTENER_NAME"; // NOI18N
    public static final String INBOUND_CONFIG_DATA_PROP = "INBOUND_CONFIG_DATA"; // NOI18N
    public static final String TX_PROP = "TX"; // NOI18N

    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private int index;

    public Set instantiate() throws IOException {
        return null;
    }
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            String rarName = (String) wizard.getProperty(RAR_NAME_PROP);
            List<String> rarLibs = GlobalRarRegistry.getInstance().getRar(rarName).getLibraryNames();
            String otdType = (String) wizard.getProperty(OTD_TYPE_PROP);
            String listenerName = (String) wizard.getProperty(LISTENER_NAME_PROP);
            InboundConfigDataImpl inboundConfigData =
                    (InboundConfigDataImpl) wizard.getProperty(INBOUND_CONFIG_DATA_PROP);
            String tx = (String) wizard.getProperty(TX_PROP);

            Project project = Templates.getProject(wizard);
            String className = Templates.getTargetName(wizard);
            FileObject folder = Templates.getTargetFolder(wizard);
            String pkgName = Util.getSelectedPackageName(folder);

            FileObject template = GlobalRarRegistry.getInstance().getRar(rarName).getInboundMDBTemplate();

            InboundGenerator generator = new InboundGenerator(rarName, rarLibs, otdType, listenerName,
                    inboundConfigData, tx, project, className, folder, template, pkgName);
            generator.addLibraryDependency();
            FileObject result = generator.generate();
            return Collections.singleton(result);
        } finally {
            handle.finish();
        }
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        this.panels = getPanels();
        this.index = 0;
    }

    public void uninitialize(WizardDescriptor wizard) {
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/wizard/Bundle").getString("Inbound_Global_Rar_Wizard");
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        index++;
    }

    public void previousPanel() {
        index--;
    }

    public void addChangeListener(ChangeListener arg0) {
    }

    public void removeChangeListener(ChangeListener arg0) {
    }

    private WizardDescriptor.Panel[] getPanels() {
        Project project = Templates.getProject(wizard);
        Sources sources = (Sources) project.getLookup().lookup(Sources.class);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        WizardDescriptor.Panel packageChooserPanel = new DelegatingWizardPanel(
                JavaTemplates.createPackageChooser(project, sourceGroups, null, true));

        WizardDescriptor.Panel[] wizardPanels = new WizardDescriptor.Panel[] {
            packageChooserPanel,
            new GlobalRarWizardPanelSelectInbound(project),
            new GlobalRarWizardPanelEditActivation(project, wizard)
        };
        String[] steps = new String[wizardPanels.length + 1];
        steps[0] = java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/wizard/Bundle").getString("Choose_...");
        for (int i = 0; i < wizardPanels.length; i++) {
            Component c = wizardPanels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i + 1] = c.getName();
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

        return wizardPanels;
    }

    private static class DelegatingWizardPanel implements WizardDescriptor.Panel {

        private WizardDescriptor.Panel delegate;

        DelegatingWizardPanel(WizardDescriptor.Panel delegate) {
            this.delegate = delegate;
        }

        public Component getComponent() {
            return delegate.getComponent();
        }

        public HelpCtx getHelp() {
            return new HelpCtx("org.netbeans.modules.soa.jca.base.about");
        }

        public void readSettings(Object arg0) {
            delegate.readSettings(arg0);
        }

        public void storeSettings(Object arg0) {
            delegate.storeSettings(arg0);
        }

        public boolean isValid() {
            return delegate.isValid();
        }

        public void addChangeListener(ChangeListener arg0) {
            delegate.addChangeListener(arg0);
        }

        public void removeChangeListener(ChangeListener arg0) {
            delegate.removeChangeListener(arg0);
        }

    }
}
