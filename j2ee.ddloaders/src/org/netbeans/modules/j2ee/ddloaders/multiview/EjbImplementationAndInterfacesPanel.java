/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbImplementationAndInterfacesForm;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesPanel extends EjbImplementationAndInterfacesForm {
    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView, final EntityAndSession ejb) {
        super(sectionNodeView);
        getBeanClassTextField().setEnabled(false);
        getLocalInterfaceCheckBox().setEnabled(false);
        getLocalComponentTextField().setEnabled(false);
        getLocalHomeTextField().setEnabled(false);
        getRemoteInterfaceCheckBox().setEnabled(false);
        getRemoteComponentTextField().setEnabled(false);
        getRemoteHomeTextField().setEnabled(false);

        populateFields(this, ejb);

        // TODO: remove following statement after implementation completion
        getChangeClassesButton().setVisible(false);

        getChangeClassesButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editValues(sectionNodeView, ejb);
            }

        });

    }

    private void populateFields(EjbImplementationAndInterfacesForm form, final EntityAndSession ejb) {
        form.getBeanClassTextField().setText(ejb.getEjbClass());

        String localComponent = ejb.getLocal();
        boolean isLocal = localComponent != null;
        form.getLocalInterfaceCheckBox().setSelected(isLocal);
        form.getLocalComponentTextField().setText(isLocal ? localComponent : null);
        form.getLocalHomeTextField().setText(isLocal ? ejb.getLocalHome() : null);

        String remoteComponent = ejb.getRemote();
        boolean isRemote = remoteComponent != null;
        form.getRemoteInterfaceCheckBox().setSelected(isRemote);
        form.getRemoteComponentTextField().setText(isRemote ? remoteComponent : null);
        form.getRemoteHomeTextField().setText(isRemote ? ejb.getHome() : null);
    }

    private void editValues(final SectionNodeView sectionNodeView, final EntityAndSession ejb) {

        final EjbImplementationAndInterfacesForm form = new EjbImplementationAndInterfacesForm(sectionNodeView);

        String ejbClass = ejb.getEjbClass();
        final String localComponent = ejb.getLocal();
        final String localHome = ejb.getLocalHome();
        final String remoteComponent = ejb.getRemote();
        final String remoteHome = ejb.getHome();

        form.getChangeClassesButton().setVisible(false);

        final JCheckBox localInterfaceCheckBox = form.getLocalInterfaceCheckBox();
        localInterfaceCheckBox.addActionListener(new ActionListener() {

            {
                localInterfaceCheckBox.setSelected(localComponent != null);
                actionPerformed(null);
            }

            public void actionPerformed(ActionEvent e) {
                final JTextField localComponentTextField = form.getLocalComponentTextField();
                final JTextField localHomeTextField = form.getLocalHomeTextField();
                boolean isLocal = localInterfaceCheckBox.isSelected();
                localInterfaceCheckBox.setSelected(isLocal);
                localComponentTextField.setEnabled(isLocal);
                localComponentTextField.setText(isLocal ? localComponent : null);
                localHomeTextField.setEnabled(isLocal);
                localHomeTextField.setText(isLocal ? localHome : null);
            }
        });

        final JCheckBox remoteInterfaceCheckBox = form.getRemoteInterfaceCheckBox();
        remoteInterfaceCheckBox.addActionListener(new ActionListener() {

            {
                remoteInterfaceCheckBox.setSelected(remoteComponent != null);
                actionPerformed(null);
            }

            public void actionPerformed(ActionEvent e) {
                final JTextField remoteComponentTextField = form.getRemoteComponentTextField();
                final JTextField remoteHomeTextField = form.getRemoteHomeTextField();
                boolean isRemote = remoteInterfaceCheckBox.isSelected();
                remoteInterfaceCheckBox.setSelected(isRemote);
                remoteComponentTextField.setEnabled(isRemote);
                remoteComponentTextField.setText(isRemote ? remoteComponent : null);
                remoteHomeTextField.setEnabled(isRemote);
                remoteHomeTextField.setText(isRemote ? remoteHome : null);
            }
        });

        final JTextField beanClassTextField = form.getBeanClassTextField();
        beanClassTextField.setText(ejbClass);

        final DialogDescriptor descriptor = new DialogDescriptor(form, "");
        descriptor.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);

        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);

        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            System.out.println("validate");
            System.out.println("refactor");
            System.out.println("update");
        } else {
            System.out.println("cancel");
        }

    }
}
