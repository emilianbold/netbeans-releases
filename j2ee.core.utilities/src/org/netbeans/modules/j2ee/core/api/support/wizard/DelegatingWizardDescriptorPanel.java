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

package org.netbeans.modules.j2ee.core.api.support.wizard;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * A <code>WizardDescriptor.Panel</code> which delegates to another panel.
 * It can be used to add further validation to e.g. a panel returned by
 * <code>JavaTemplates.createPackageChooser()</code>.
 *
 * <p>This class currently only implements <code>WizardDescriptor.Panel</code>
 * and <code>WizardDescriptor.FinishablePanel</code>. It will not delegate
 * methods in other subinterfaces of <code>WizardDescriptor.Panel</code>.</p>
 *
 * @author Andrei Badea
 */
public class DelegatingWizardDescriptorPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private final WizardDescriptor.Panel delegate;

    private WizardDescriptor wizardDescriptor;
    private Project project;

    public DelegatingWizardDescriptorPanel(WizardDescriptor.Panel delegate) {
        this.delegate = delegate;
    }

    public Component getComponent() {
        return delegate.getComponent();
    }

    public HelpCtx getHelp() {
        return delegate.getHelp();
    }

    public void readSettings(Object settings) {
        if (wizardDescriptor == null) {
            wizardDescriptor = (WizardDescriptor)settings;
            project = Templates.getProject((WizardDescriptor)settings);
        }
        delegate.readSettings(settings);
    }

    public void storeSettings(Object settings) {
        delegate.storeSettings(settings);
    }

    public boolean isValid() {
        return delegate.isValid();
    }

    public void addChangeListener(ChangeListener l) {
        delegate.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        delegate.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        if (delegate instanceof WizardDescriptor.FinishablePanel) {
            return ((WizardDescriptor.FinishablePanel)delegate).isFinishPanel();
        }
        return false;
    }

    protected WizardDescriptor getWizardDescriptor() {
        return wizardDescriptor;
    }

    protected Project getProject() {
        return project;
    }
}
