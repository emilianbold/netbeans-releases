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
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbInterfaceForm;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.entity.EntityGenerator;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.src.ClassElement;
import org.openide.filesystems.FileObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesPanel extends EjbImplementationAndInterfacesForm {
    private EntityAndSession ejb;

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView, final EntityAndSession ejb) {
        super(sectionNodeView);
        this.ejb = ejb;
        getBeanClassTextField().setEditable(false);
        getLocalComponentTextField().setEditable(false);
        getLocalHomeTextField().setEditable(false);
        getRemoteComponentTextField().setEditable(false);
        getRemoteHomeTextField().setEditable(false);

        populateFields();

        getChangeClassesButton().setVisible(false);
        getChangeClassesButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editValues(sectionNodeView, ejb);
            }

        });

        getLocalInterfaceCheckBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ejb.getLocal() != null) {
                    removeInterfaces(true);
                } else {
                    addInterfaces(true);
                }
            }
        });

        getRemoteInterfaceCheckBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ejb.getRemote() != null) {
                    removeInterfaces(false);
                } else {
                    addInterfaces(false);
                }
            }
        });
    }

    private static String getPackage(String fullClassName) {
        return fullClassName.substring(0, fullClassName.lastIndexOf('.'));
    }

    private void addInterfaces(boolean local) {
        EjbInterfaceForm form = new EjbInterfaceForm();
        if (openAddDialog(local, form)) {
            String componentInterfaceName = form.getComponentTextField().getText().trim();
            String homeInterfaceName = form.getHomeTextField().getText().trim();
//            new EntityGenerator().
        }
    }

    private boolean openAddDialog(boolean local, final EjbInterfaceForm form) {
        String ejbName = ejb.getEjbName();
        String pkg = getPackage(ejb.getEjbClass());
        String defaultComponentInterface = local ?
                EjbGenerationUtil.getLocalName(pkg, ejbName) : EjbGenerationUtil.getRemoteName(pkg, ejbName);
        String defaultHomeInterface = local ?
                EjbGenerationUtil.getLocalHomeName(pkg, ejbName) : EjbGenerationUtil.getHomeName(pkg, ejbName);
        final DialogDescriptor descriptor = new DialogDescriptor(form, "Add Interfaces");
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        final JTextField componentTextField = form.getComponentTextField();
        final JTextField homeTextField = form.getHomeTextField();
        componentTextField.setText(defaultComponentInterface);
        homeTextField.setText(defaultHomeInterface);
        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                validate();
            }

            public void insertUpdate(DocumentEvent e) {
                validate();
            }

            public void removeUpdate(DocumentEvent e) {
                validate();
            }

            private void validate() {
                String componentInterface = componentTextField.getText().trim();
                String homeInterface = homeTextField.getText().trim();
                boolean isValid;
                String msg;
                final String messageId = "MSG_InvalidInterface";
                if (!Utils.isValidPackageName(componentInterface)) {
                    isValid = false;
                    msg = Utils.getBundleMessage(messageId, componentInterface);
                } else if (!Utils.isValidPackageName(homeInterface)) {
                    isValid = false;
                    msg = Utils.getBundleMessage(messageId, homeInterface);
                } else {
                    isValid = true;
                    msg = " ";
                }
                form.getErrorLabel().setText(msg);
                descriptor.setValid(isValid);
            }
        };
        componentTextField.getDocument().addDocumentListener(documentListener);
        homeTextField.getDocument().addDocumentListener(documentListener);
        dialog.setVisible(true);
        boolean b = DialogDescriptor.OK_OPTION == descriptor.getValue();
        return b;
    }

    private void removeInterfaces(boolean local) {
        String componentInterface = local ? ejb.getLocal() : ejb.getRemote();
        String homeInterface = local ? ejb.getLocalHome() : ejb.getHome();
        String msg = Utils.getBundleMessage("MSG_RemoveInterfaces", componentInterface, homeInterface);
        String interfaceType = Utils.getBundleMessage(local ? "TXT_Local" : "TXT_Remote");
        String title = Utils.getBundleMessage("LBL_InterfacesRemoval", interfaceType);
        NotifyDescriptor descriptor = new NotifyDescriptor(msg, title, NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(descriptor);
        if (NotifyDescriptor.YES_OPTION == descriptor.getValue()) {
            SectionNodeView sectionNodeView = (SectionNodeView) getSectionView();
            FileObject ejbJarFile = sectionNodeView.getDataObject().getPrimaryFile();
            ClassElement beanClass = Utils.getBeanClass(ejbJarFile, ejb);
            Utils.removeInterface(beanClass, componentInterface);
            Utils.removeClassFile(ejbJarFile, componentInterface);
            Utils.removeInterface(beanClass, homeInterface);
            Utils.removeClassFile(ejbJarFile, homeInterface);
            if (local) {
                ejb.setLocal(null);
                ejb.setLocalHome(null);
            } else {
                ejb.setRemote(null);
                ejb.setHome(null);
            }
        }
    }

    public void dataFileChanged() {
        populateFields();
    }

    public void populateFields() {
        getBeanClassTextField().setText(ejb.getEjbClass());

        String localComponent = ejb.getLocal();
        boolean isLocal = localComponent != null;
        getLocalInterfaceCheckBox().setSelected(isLocal);
        getLocalComponentTextField().setText(isLocal ? localComponent : null);
        getLocalHomeTextField().setText(isLocal ? ejb.getLocalHome() : null);

        String remoteComponent = ejb.getRemote();
        boolean isRemote = remoteComponent != null;
        getRemoteInterfaceCheckBox().setSelected(isRemote);
        getRemoteComponentTextField().setText(isRemote ? remoteComponent : null);
        getRemoteHomeTextField().setText(isRemote ? ejb.getHome() : null);
    }

    private void editValues(final SectionNodeView sectionNodeView, final EntityAndSession ejb) {

        final EjbImplementationAndInterfacesForm form = new EjbImplementationAndInterfacesForm(sectionNodeView);

        String ejbClass = ejb.getEjbClass();
        final String localComponent = ejb.getLocal();
        final String localHome = ejb.getLocalHome();
        final String remoteComponent = ejb.getRemote();
        final String remoteHome = ejb.getHome();

        form.getChangeClassesButton().setVisible(false);
        form.setOpaque(false);

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

        final DialogDescriptor descriptor = new DialogDescriptor(form, ""); //NOI18N
        descriptor.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);

        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);

        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            if (remoteInterfaceCheckBox.isSelected() != (remoteComponent != null)) {
                if (remoteComponent == null) {
                    //todo: remove
                } else {
                    //todo:add
                }
            } else {
                //TODO: check names

            }
            if (localInterfaceCheckBox.isSelected() != (localComponent != null)) {
                if (localComponent == null) {
                    //todo: remove
                } else {
                    //todo:add
                }

            } else {
                //TODO: check names

            }
        }

    }
}
