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

import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbImplementationAndInterfacesForm;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.src.ClassElement;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesPanel extends EjbImplementationAndInterfacesForm {
    private EntityAndSession ejb;
    private NonEditableDocument beanClassDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return ejb == null ? null : ejb.getEjbClass();
        }
    };
    private NonEditableDocument localComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return ejb == null ? null : ejb.getLocal();
        }
    };
    private NonEditableDocument localHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return ejb == null ? null : ejb.getLocalHome();
        }
    };
    private NonEditableDocument remoteComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return ejb == null ? null : ejb.getRemote();
        }
    };
    private NonEditableDocument remoteHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return ejb == null ? null : ejb.getHome();
        }
    };

    private String className = null;

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView, final EntityAndSession ejb) {
        super(sectionNodeView);
        this.ejb = ejb;
        getBeanClassTextField().setDocument(beanClassDocument);
        getLocalComponentTextField().setDocument(localComponentDocument);
        getLocalHomeTextField().setDocument(localHomeDocument);
        getRemoteComponentTextField().setDocument(remoteComponentDocument);
        getRemoteHomeTextField().setDocument(remoteHomeDocument);
        final JButton moveClassButton = getMoveClassButton();
        final JButton renameClassButton = getRenameClassButton();

        populateFields();

        FocusListener focusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                Component component = e.getComponent();
                if (component instanceof JTextField) {
                    className = ((JTextField) component).getText();
                    moveClassButton.setEnabled(true);
                    renameClassButton.setEnabled(true);
                } else {
                    boolean isRefactorButton = component == moveClassButton || component == renameClassButton;
                    if (moveClassButton.isEnabled()) {
                        moveClassButton.setEnabled(isRefactorButton);
                    }
                    if (renameClassButton.isEnabled()) {
                        renameClassButton.setEnabled(isRefactorButton);
                    }
                }
            }
        };
        addFocusListener(focusListener);

        //todo:remove following rows
        moveClassButton.setVisible(false);
        renameClassButton.setVisible(false);
        moveClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EjbJarMultiViewDataObject ejbJarDataObject = (EjbJarMultiViewDataObject) ((SectionNodeView) getSectionView()).getDataObject();
                SourceGroup[] sourceGroups = ejbJarDataObject.getSourceGroups();
                Utils.activateMoveClassUI(className, sourceGroups[0]);
            }
        });
        renameClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.activateRenameClassUI(className);
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
            try {
                Utils.addInterfaces(ejbJarFile, ejb, local);
            } finally {
                populateFields();
            }
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
            try {
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
            } finally {
                populateFields();
            }
        }
    }

    public void dataFileChanged() {
        populateFields();
    }

    public void populateFields() {
        //getBeanClassTextField().setText(ejb.getEjbClass());
        beanClassDocument.init();
        localComponentDocument.init();
        localHomeDocument.init();
        remoteComponentDocument.init();
        remoteHomeDocument.init();

        String localComponent = ejb.getLocal();
        boolean isLocal = localComponent != null;
        getLocalInterfaceCheckBox().setSelected(isLocal);
        getLocalHomeTextField().setText(isLocal ? ejb.getLocalHome() : null);

        String remoteComponent = ejb.getRemote();
        boolean isRemote = remoteComponent != null;
        getRemoteInterfaceCheckBox().setSelected(isRemote);
        getRemoteComponentTextField().setText(isRemote ? remoteComponent : null);
        getRemoteHomeTextField().setText(isRemote ? ejb.getHome() : null);
    }

    private abstract class NonEditableDocument extends PlainDocument {

        String text = null;

        protected abstract String retrieveText();

        protected NonEditableDocument() {
            init();
        }

        public void init() {
            String s = retrieveText();
            if (s == null) {
                s = "";
            }
            if (!s.equals(text)) {
                text = s;
                try {
                    super.remove(0, super.getLength());
                    super.insertString(0, s, null);
                } catch (BadLocationException e) {

                }
            }
        }

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {

        }

        public void remove(int offs, int len) throws BadLocationException {

        }
    }

}
