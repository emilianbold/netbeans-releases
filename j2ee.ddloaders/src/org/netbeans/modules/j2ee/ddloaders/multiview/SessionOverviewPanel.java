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
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class SessionOverviewPanel extends SessionOverviewForm {

    private static abstract class SectionItemEditorModel extends ItemEditorHelper.ItemEditorModel {
        XmlMultiViewDataObject dataObject;

        protected SectionItemEditorModel(XmlMultiViewDataObject dataObject) {
            this.dataObject = dataObject;
        }

        protected boolean validate(String value) {
            return true;
        }

        protected abstract void setValue(String value);

        protected abstract String getValue();

        public final boolean setItemValue(String value) {
            if (validate(value)) {
                setValue(value);
                dataObject.modelUpdatedFromUI();
                return true;
            } else {
                return false;
            }
        }

        public final String getItemValue() {
            return getValue();
        }

        public void documentUpdated() {
        }
    }

    EjbJarMultiViewDataObject dataObject;
    Session session;
    SectionNodeView sectionView;

    public SessionOverviewPanel(final SectionNodeView sectionView, XmlMultiViewDataObject dataObject, Session session) {
        super(sectionView);
        this.dataObject = (EjbJarMultiViewDataObject) dataObject;
        this.session = session;
        this.sectionView = sectionView;
        init();
    }

    private void init() {
        new ItemEditorHelper(getEjbNameTextField(), new SectionItemEditorModel(dataObject) {
            protected boolean validate(String value) {
                return value.length() > 0;
            }

            protected String getValue() {
                return session.getEjbName();
            }

            protected void setValue(String value) {
                session.setEjbName(value);
            }
        });

        new ItemOptionHelper(getSessionTypeButtonGroup()) {

            public String getItemValue() {
                return session.getSessionType();
            }

            public void setItemValue(String value) {
                session.setSessionType(value);
                dataObject.modelUpdatedFromUI();
            }
        };

        new ItemOptionHelper(getTransactionTypeButtonGroup()) {

            public String getItemValue() {
                return session.getTransactionType();
            }

            public void setItemValue(String value) {
                session.setTransactionType(value);
                dataObject.modelUpdatedFromUI();
            }
        };
    }

}
