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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.MessageGenerator;
import org.openide.*;
import org.openide.util.*;

import java.io.*;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.TransactionHelper;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class MessageEJBWizard implements WizardDescriptor.InstantiatingIterator{
    private TransactionHelper transactionHelper = new TransactionHelper();
    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private MessageEJBWizardDescriptor ejbPanel;
    private WizardDescriptor wiz;

    private static final String [] SESSION_STEPS =
                                   new String [] {
                                       NbBundle.getMessage (MessageEJBWizard.class,
                         "LBL_SpecifyEJBInfo")
                                   };

    public String name () {
    return NbBundle.getMessage (MessageEJBWizard.class,
                     "LBL_MessageEJBWizardTitle");
    }

    public void uninitialize(WizardDescriptor wiz) {
        transactionHelper.uninitialize();
    }

    public void initialize(WizardDescriptor wizardDescriptor) {
        transactionHelper.initialize(wizardDescriptor);
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        ejbPanel = new MessageEJBWizardDescriptor(wiz);
        WizardDescriptor.Panel p = JavaTemplates.createPackageChooser(project,sourceGroups, ejbPanel, true);
        JComponent c = (JComponent) p.getComponent ();
        Util.changeLabelInComponent(c, NbBundle.getMessage(Util.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), NbBundle.getMessage(MessageEJBWizard.class, "LBL_EJB_Name") );
        Util.hideLabelAndLabelFor(c, NbBundle.getMessage(Util.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));
        panels = new WizardDescriptor.Panel[] {p};
        Utils.mergeSteps(wiz, panels, SESSION_STEPS);
    }

    public Set instantiate () throws IOException {
        FileObject pkg = Templates.getTargetFolder(wiz);
        String ejbName = Templates.getTargetName(wiz);
        Project project = Templates.getProject(wiz);
        MessageGenerator sg = new MessageGenerator();
        try {
            sg.generate(ejbName, pkg, ejbPanel.isQueue(), project);
        } catch (VersionNotSupportedException vnse) {
            IOException ioe = new IOException();
            ioe.initCause(vnse);
            throw ioe;
        }
        transactionHelper.write();
        return Collections.EMPTY_SET; // change to return generated files
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

}

