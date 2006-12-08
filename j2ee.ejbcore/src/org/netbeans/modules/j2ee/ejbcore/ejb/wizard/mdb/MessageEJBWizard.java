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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.io.IOException;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.MessageGenerator;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class MessageEJBWizard implements WizardDescriptor.InstantiatingIterator{

    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private MessageEJBWizardPanel ejbPanel;
    private WizardDescriptor wiz;

    private static final String [] SESSION_STEPS = new String [] {
        NbBundle.getMessage(MessageEJBWizard.class, "LBL_SpecifyEJBInfo")
    };

    public String name () {
        return NbBundle.getMessage (MessageEJBWizard.class, "LBL_MessageEJBWizardTitle");
    }

    public void uninitialize(WizardDescriptor wiz) {
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        ejbPanel = new MessageEJBWizardPanel(wiz);
        WizardDescriptor.Panel p = new ValidatingPanel(JavaTemplates.createPackageChooser(project,sourceGroups, ejbPanel, true));
        JComponent c = (JComponent) p.getComponent ();
        Util.changeLabelInComponent(c, NbBundle.getMessage(Util.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), NbBundle.getMessage(MessageEJBWizard.class, "LBL_EJB_Name") );
        Util.hideLabelAndLabelFor(c, NbBundle.getMessage(Util.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));
        panels = new WizardDescriptor.Panel[] {p};
        Utils.mergeSteps(wiz, panels, SESSION_STEPS);
    }

    public Set instantiate() throws IOException {
        FileObject pkg = Templates.getTargetFolder(wiz);
        String ejbName = Templates.getTargetName(wiz);
        Project project = Templates.getProject(wiz);
        FileObject result = null;
        
        MessageGenerator generator = new MessageGenerator();
        try {
            result = generator.generate(ejbName, pkg, ejbPanel, project);
        } catch (VersionNotSupportedException vnse) {
            IOException ioe = new IOException();
            ioe.initCause(vnse);
            throw ioe;
        }
        return result == null ? Collections.EMPTY_SET : Collections.singleton(result);
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }

    public boolean hasPrevious () {
        return index > 0;
    }

    public boolean hasNext () {
    return index < panels.length - 1;
    }

    public void nextPanel () {
        if (! hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }
    public void previousPanel () {
        if (! hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    /**
     * A panel which checks whether the target project has a valid server set,
     * otherwise it delegates to another panel.
     */
    private static final class ValidatingPanel extends DelegatingWizardDescriptorPanel {

        public ValidatingPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }

        public boolean isValid() {
            if (!org.netbeans.modules.j2ee.common.Util.isValidServerInstance(getProject())) {
                getWizardDescriptor().putProperty("WizardPanel_errorMessage",
                        NbBundle.getMessage(MessageEJBWizard.class, "ERR_MissingServer")); // NOI18N
                return false;
            }
            return super.isValid();
        }
    }
}
