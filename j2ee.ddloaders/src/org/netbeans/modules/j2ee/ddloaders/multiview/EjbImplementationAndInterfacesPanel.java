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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.src.ClassElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
        final JTextField beanClassTextField = getBeanClassTextField();
        final JTextField localComponentTextField = getLocalComponentTextField();
        final JTextField localHomeTextField = getLocalHomeTextField();
        final JTextField remoteComponentTextField = getRemoteComponentTextField();
        final JTextField remoteHomeTextField = getRemoteHomeTextField();
        final JButton moveClassButton = getMoveClassButton();
        final JButton renameClassButton = getRenameClassButton();
        beanClassTextField.setEditable(false);
        localComponentTextField.setEditable(false);
        localHomeTextField.setEditable(false);
        remoteComponentTextField.setEditable(false);
        remoteHomeTextField.setEditable(false);

        populateFields();

        FocusListener focusListener = new FocusListener() {
            public void focusGained(FocusEvent e) {
                Component component = e.getComponent();
                if (component instanceof JTextField) {
                    JTextField textField = ((JTextField) component);
                    textField.setSelectionStart(0);
                    textField.setSelectionEnd(textField.getText().length());
                    moveClassButton.setEnabled(true);
                    renameClassButton.setEnabled(true);
                } else {
                    moveClassButton.setEnabled(false);
                    renameClassButton.setEnabled(false);
                }
            }

            public void focusLost(FocusEvent e) {
                Component component = e.getComponent();
                if (component instanceof JTextField) {
                }
            }
        };
        addFocusListener(focusListener);

        //todo:remove following rows
        moveClassButton.setVisible(false);
        renameClassButton.setVisible(false);
        moveClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        renameClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
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

    private void addInterfaces(boolean local) {
        String interfaceType = Utils.getBundleMessage(local ? "TXT_Local" : "TXT_Remote");
        String msg = Utils.getBundleMessage("MSG_AddInterfaces", interfaceType);
        String title = Utils.getBundleMessage("LBL_AddInterfaces");
        NotifyDescriptor descriptor = new NotifyDescriptor(msg, title, NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(descriptor);
        if (NotifyDescriptor.YES_OPTION == descriptor.getValue()) {
            EntityAndSession ejb = this.ejb;
            SectionNodeView sectionNodeView = (SectionNodeView) getSectionView();
            FileObject ejbJarFile = sectionNodeView.getDataObject().getPrimaryFile();
            Utils.addInterfaces(ejbJarFile, ejb, local);
            populateFields();
        }
    }

    private void removeInterfaces(boolean local) {
        String componentInterface = local ? ejb.getLocal() : ejb.getRemote();
        String homeInterface = local ? ejb.getLocalHome() : ejb.getHome();
        String msg = Utils.getBundleMessage("MSG_RemoveInterfaces", componentInterface, homeInterface);
        String interfaceType = Utils.getBundleMessage(local ? "TXT_Local" : "TXT_Remote");
        String title = Utils.getBundleMessage("LBL_RemoveInterfaces", interfaceType);
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
        populateFields();
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

}
