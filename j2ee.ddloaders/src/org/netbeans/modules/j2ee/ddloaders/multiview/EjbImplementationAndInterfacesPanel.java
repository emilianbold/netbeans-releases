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
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesPanel extends EjbImplementationAndInterfacesForm {
    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(SectionNodeView sectionNodeView, final EntityAndSession ejb) {
        super(sectionNodeView);
        final EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();
        new ItemEditorHelper(getBeanClassTextField(), new TextItemEditorModel(dataObject) {
            protected boolean validate(String value) {
                return value.length() > 0;
            }

            protected String getValue() {
                return ejb.getEjbClass();
            }

            protected void setValue(String value) {
                ejb.setEjbClass(value);
            }
        });

        JCheckBox localInterfaceCheckBox = getLocalInterfaceCheckBox();
        JTextField localComponentTextField = getLocalComponentTextField();
        JTextField localHomeTextField = getLocalHomeTextField();
        boolean isLocal = ejb.getLocal() != null;
        localInterfaceCheckBox.setSelected(isLocal);
        localComponentTextField.setEnabled(isLocal);
        localHomeTextField.setEnabled(isLocal);

        new ItemEditorHelper(getLocalComponentTextField(), new TextItemEditorModel(dataObject) {
            protected void setValue(String value) {
                ejb.setLocal(value);
            }

            protected String getValue() {
                return ejb.getLocal();
            }
        });

        new ItemEditorHelper(getLocalHomeTextField(), new TextItemEditorModel(dataObject) {
            protected void setValue(String value) {
                ejb.setLocalHome(value);
            }

            protected String getValue() {
                return ejb.getLocalHome();
            }
        });

        JCheckBox remoteInterfaceCheckBox = getRemoteInterfaceCheckBox();
        JTextField remoteComponentTextField = getRemoteComponentTextField();
        JTextField remoteHomeTextField = getRemoteHomeTextField();
        boolean isRemote = ejb.getRemote() != null;
        remoteInterfaceCheckBox.setSelected(isRemote);
        remoteComponentTextField.setEnabled(isRemote);
        remoteHomeTextField.setEnabled(isRemote);

        new ItemEditorHelper(getRemoteComponentTextField(), new TextItemEditorModel(dataObject) {
            protected void setValue(String value) {
                ejb.setRemote(value);
            }

            protected String getValue() {
                return ejb.getRemote();
            }
        });

        new ItemEditorHelper(getRemoteHomeTextField(), new TextItemEditorModel(dataObject) {
            protected void setValue(String value) {
                ejb.setHome(value);
            }

            protected String getValue() {
                return ejb.getHome();
            }
        });
    }
}
