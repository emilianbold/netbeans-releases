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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbImplementationAndInterfacesForm;
import org.netbeans.modules.xml.multiview.ItemCheckBoxHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import javax.swing.*;
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
    private static final String LINK_BEAN = "linkBean";
    private static final String LINK_LOCAL = "linkLocal";
    private static final String LINK_LOCAL_HOME = "linkLocalHome";
    private static final String LINK_REMOTE = "linkRemote";
    private static final String LINK_REMOTE_HOME = "linkRemoteHome";

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView,
            final EntityAndSessionHelper helper) {
        super(sectionNodeView);
        this.helper = helper;
        getBeanClassTextField().setDocument(beanClassDocument);
        getLocalComponentTextField().setDocument(localComponentDocument);
        getLocalHomeTextField().setDocument(localHomeDocument);
        getRemoteComponentTextField().setDocument(remoteComponentDocument);
        getRemoteHomeTextField().setDocument(remoteHomeDocument);
        final JButton moveClassButton = getMoveClassButton();
        final JButton renameClassButton = getRenameClassButton();

        scheduleRefreshView();

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
//                Utils.activateMoveClassUI(className);
                moveClassButton.setEnabled(false);
                renameClassButton.setEnabled(false);
            }
        });
        renameClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                Utils.activateRenameClassUI(className);
                moveClassButton.setEnabled(false);
                renameClassButton.setEnabled(false);
            }
        });

        XmlMultiViewDataSynchronizer synchronizer =
                ((EjbJarMultiViewDataObject) sectionNodeView.getDataObject()).getModelSynchronizer();
        addRefreshable(new ItemCheckBoxHelper(synchronizer, getLocalInterfaceCheckBox()) {
            public boolean getItemValue() {
                boolean value = helper.getLocal() != null;
                getLocalComponentLinkButton().setVisible(value);
                getLocalHomeLinkButton().setVisible(value);
                return value;
            }

            public void setItemValue(boolean value) {
                if (value != getItemValue()) {
                    if (value) {
                        addInterfaces(true);
                    } else {
                        removeInterfaces(true);
                    }
                    refresh();
                }
            }
        });

        addRefreshable(new ItemCheckBoxHelper(synchronizer, getRemoteInterfaceCheckBox()) {
            public boolean getItemValue() {
                boolean value = helper.getRemote() != null;
                getRemoteComponentLinkButton().setVisible(value);
                getRemoteHomeLinkButton().setVisible(value);
                return value;
            }

            public void setItemValue(boolean value) {
                if (value != getItemValue()) {
                    if (value) {
                        addInterfaces(false);
                    } else {
                        removeInterfaces(false);
                    }
                    refresh();
                }
            }
        });

        initLinkButton(getBeanClassLinkButton(), LINK_BEAN);
        initLinkButton(getLocalComponentLinkButton(), LINK_LOCAL);
        initLinkButton(getLocalHomeLinkButton(), LINK_LOCAL_HOME);
        initLinkButton(getRemoteComponentLinkButton(), LINK_REMOTE);
        initLinkButton(getRemoteHomeLinkButton(), LINK_REMOTE_HOME);
    }

    private void initLinkButton(AbstractButton button, String key) {
        LinkButton.initLinkButton(button, this, null, key);
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
                scheduleRefreshView();
            }
        }
    }

    private void removeInterfaces(boolean local) {
        String componentInterface = local ? helper.getLocal() : helper.getRemote();
        String homeInterface = local ? helper.getLocalHome() : helper.getHome();
//        String businessInterfaceName = helper.getBusinessInterfaceName(local);
//        String msg;
//        if (businessInterfaceName == null) {
//            msg = Utils.getBundleMessage("MSG_RemoveInterfaces", homeInterface, componentInterface);
//        } else {
//            msg = Utils.getBundleMessage("MSG_RemoveInterfaces2", homeInterface, componentInterface,
//                    businessInterfaceName);
//        }
        String interfaceType = Utils.getBundleMessage(local ? "TXT_Local" : "TXT_Remote");
        String title = Utils.getBundleMessage("LBL_RemoveInterfaces", interfaceType);
//        NotifyDescriptor descriptor = new NotifyDescriptor(msg, title, NotifyDescriptor.YES_NO_OPTION,
//                NotifyDescriptor.WARNING_MESSAGE, null, null);
//        DialogDisplayer.getDefault().notify(descriptor);
//        if (NotifyDescriptor.YES_OPTION == descriptor.getValue()) {
//            try {
//                helper.removeInterfaces(local);
//            } finally {
//                scheduleRefreshView();
//            }
//        }
    }

    public void refreshView() {
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

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof EntityAndSession) {
            scheduleRefreshView();
        }
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
//        JavaClass javaClass;
//        if(ddProperty == LINK_BEAN) {
//            javaClass = helper.getBeanClass();
//        } else if(ddProperty == LINK_LOCAL) {
//            javaClass = helper.getLocalBusinessInterfaceClass();
//        } else if(ddProperty == LINK_LOCAL_HOME) {
//            javaClass = helper.getLocalHomeInterfaceClass();
//        } else if(ddProperty == LINK_REMOTE) {
//            javaClass = helper.getRemoteBusinessInterfaceClass();
//        } else if (ddProperty == LINK_REMOTE_HOME) {
//            javaClass = helper.getHomeInterfaceClass();
//        } else {
//            javaClass = null;
//        }
//        if (javaClass != null) {
//            Utils.openEditorFor(helper.ejbJarFile, javaClass);
//        }
    }

}
