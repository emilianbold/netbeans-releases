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
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.MessageDrivenOverviewForm;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ItemOptionHelper;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class MessageDrivenOverviewPanel extends MessageDrivenOverviewForm {
    /**
     * @param sectionNodeView enclosing SectionNodeView object
     */
    public MessageDrivenOverviewPanel(SectionNodeView sectionNodeView, final MessageDriven messageDriven) {
        super(sectionNodeView);

        final EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();

        new ItemEditorHelper(getNameTextField(), new TextItemEditorModel(dataObject) {
            protected boolean validate(String value) {
                return value.length() > 0;
            }

            protected String getValue() {
                return messageDriven.getEjbName();
            }

            protected void setValue(String value) {
                messageDriven.setEjbName(value);
            }
        });

        new ItemOptionHelper(getTransactionTypeButtonGroup()) {

            public String getItemValue() {
                return messageDriven.getTransactionType();
            }

            public void setItemValue(String value) {
                messageDriven.setTransactionType(value);
                dataObject.modelUpdatedFromUI();
            }
        };

    }
}
