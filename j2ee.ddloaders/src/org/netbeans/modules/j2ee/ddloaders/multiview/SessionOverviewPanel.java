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

import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.SessionOverviewForm;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ItemOptionHelper;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class SessionOverviewPanel extends SessionOverviewForm {

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof Session) {
            scheduleRefreshView();
        }
    }

    public SessionOverviewPanel(final SectionNodeView sectionNodeView, final Session session) {
        super(sectionNodeView);

        final EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();

        addRefreshable(new ItemEditorHelper(getEjbNameTextField(), new TextItemEditorModel(dataObject, false) {
            protected String getValue() {
                return session.getEjbName();
            }

            protected void setValue(String value) {
                session.setEjbName(value);
            }
        }));

        addRefreshable(new ItemOptionHelper(dataObject, getSessionTypeButtonGroup()) {
            public String getItemValue() {
                return session.getSessionType();
            }

            public void setItemValue(String value) {
                session.setSessionType(value);
            }
        });

        addRefreshable(new ItemOptionHelper(dataObject, getTransactionTypeButtonGroup()) {
            public String getItemValue() {
                return session.getTransactionType();
            }

            public void setItemValue(String value) {
                session.setTransactionType(value);
            }
        });
    }

}
