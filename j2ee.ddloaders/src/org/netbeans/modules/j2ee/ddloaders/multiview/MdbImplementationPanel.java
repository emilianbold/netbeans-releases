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

import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.MdbImplementationForm;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.src.ClassElement;
import org.openide.filesystems.FileObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
public class MdbImplementationPanel extends MdbImplementationForm {
    private XmlMultiViewDataObject dataObject;
    private static final String LINK_BEAN = "linkBean";
    private MessageDriven messageDriven;
    private NonEditableDocument beanClassDocument;

    /**
     * Creates new form MdbImplementationForm
     *
     * @param sectionNodeView enclosing SectionNodeView object
     */
    public MdbImplementationPanel(final SectionNodeView sectionNodeView, final MessageDriven messageDriven) {
        super(sectionNodeView);
        this.messageDriven = messageDriven;
        dataObject = sectionNodeView.getDataObject();
        beanClassDocument = new NonEditableDocument() {
            protected String retrieveText() {
                return messageDriven.getEjbClass();
            }
        };
        getBeanClassTextField().setDocument(beanClassDocument);
        JButton moveClassButton = getMoveClassButton();
        moveClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.activateMoveClassUI(messageDriven.getEjbClass());
                signalUIChange();
            }
        });
        JButton renameClassButton = getRenameClassButton();
        renameClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.activateRenameClassUI(messageDriven.getEjbClass());
                signalUIChange();
            }
        });
        LinkButton.initLinkButton(getBeanClassLinkButton(), this, null, LINK_BEAN);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        scheduleRefreshView();
    }

    public void refreshView() {
         beanClassDocument.init();
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
        if(ddProperty == LINK_BEAN) {
            final FileObject ejbJarFile = dataObject.getPrimaryFile();
            final ClassPath sourceClassPath = Utils.getSourceClassPath(ejbJarFile);
            ClassElement beanClass = Utils.getClassElement(sourceClassPath, messageDriven.getEjbClass());
            Utils.openEditorFor(ejbJarFile, beanClass);
        }
    }
}
