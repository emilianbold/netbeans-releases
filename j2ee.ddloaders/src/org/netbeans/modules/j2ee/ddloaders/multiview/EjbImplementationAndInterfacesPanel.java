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

    private EntityAndSessionHelper helper;
    private NonEditableDocument beanClassDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getEjbClass();
        }
    };
    private NonEditableDocument localComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocal();
        }
    };
    private NonEditableDocument localHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocalHome();
        }
    };
    private NonEditableDocument remoteComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getRemote();
        }
    };
    private NonEditableDocument remoteHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getHome();
        }
    };

    private String className = null;

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView, final EntityAndSession ejb,
            EntityAndSessionHelper helper) {
        super(sectionNodeView);
        this.helper = helper;
        getBeanClassTextField().setDocument(beanClassDocument);
        getLocalComponentTextField().setDocument(localComponentDocument);
        getLocalHomeTextField().setDocument(localHomeDocument);
        getRemoteComponentTextField().setDocument(remoteComponentDocument);
        getRemoteHomeTextField().setDocument(remoteHomeDocument);
        final JButton moveClassButton = getMoveClassButton();
        final JButton renameClassButton = getRenameClassButton();

        refreshView();

        FocusListener focusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                Component component = e.getComponent();
                if (component instanceof JTextField) {
                    className = ((JTextField) component).getText().trim();
                    boolean enabled = className.length() > 0;
                    moveClassButton.setEnabled(enabled);
                    renameClassButton.setEnabled(enabled);
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

        moveClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.activateMoveClassUI(className);
                moveClassButton.setEnabled(false);
                renameClassButton.setEnabled(false);
            }
        });
        renameClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.activateRenameClassUI(className);
                moveClassButton.setEnabled(false);
                renameClassButton.setEnabled(false);
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
            try {
                helper.addInterfaces(local);
            } finally {
                refreshView();
            }
        }
    }

    private void removeInterfaces(boolean local) {
        String componentInterface = local ? helper.getLocal() : helper.getRemote();
        String homeInterface = local ? helper.getLocalHome() : helper.getHome();
        String businessInterfaceName = helper.getBusinessInterfaceName(local);
        String msg;
        if (businessInterfaceName == null) {
            msg = Utils.getBundleMessage("MSG_RemoveInterfaces", homeInterface, componentInterface);
        } else {
            msg = Utils.getBundleMessage("MSG_RemoveInterfaces2", homeInterface, componentInterface,
                    businessInterfaceName);
        }
        String interfaceType = Utils.getBundleMessage(local ? "TXT_Local" : "TXT_Remote");
        String title = Utils.getBundleMessage("LBL_RemoveInterfaces", interfaceType);
        NotifyDescriptor descriptor = new NotifyDescriptor(msg, title, NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(descriptor);
        if (NotifyDescriptor.YES_OPTION == descriptor.getValue()) {
            try {
                helper.removeInterfaces(local);
            } finally {
                refreshView();
            }
        }
    }

    public void dataFileChanged() {
        refreshView();
    }

    protected void refreshView() {
        beanClassDocument.init();
        localComponentDocument.init();
        localHomeDocument.init();
        remoteComponentDocument.init();
        remoteHomeDocument.init();

        String localComponent = helper.getLocal();
        boolean isLocal = localComponent != null;
        getLocalInterfaceCheckBox().setSelected(isLocal);
        String remoteComponent = helper.getRemote();
        boolean isRemote = remoteComponent != null;
        getRemoteInterfaceCheckBox().setSelected(isRemote);
    }

    protected void propertyChanged(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof EntityAndSession) {
            refreshView();
        } else {
            super.propertyChanged(source, propertyName, oldValue, newValue);
        }
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
