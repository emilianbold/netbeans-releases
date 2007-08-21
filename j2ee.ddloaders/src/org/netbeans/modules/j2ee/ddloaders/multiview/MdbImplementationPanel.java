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

import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.MdbImplementationForm;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
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
//            final ClassPath sourceClassPath = Utils.getSourceClassPath(ejbJarFile);
//            JavaClass beanClass = (JavaClass) JMIUtils.resolveType(messageDriven.getEjbClass());
//            Utils.openEditorFor(ejbJarFile, beanClass);
        }
    }
}
