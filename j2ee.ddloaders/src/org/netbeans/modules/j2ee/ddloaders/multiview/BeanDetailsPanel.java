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

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbDetailForm;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
class BeanDetailsPanel extends EjbDetailForm {
    private final Ejb ejb;
    private EjbJarMultiViewDataObject dataObject;

    private abstract class EditorModel extends ItemEditorHelper.ItemEditorModel {

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

    private class LargeIconEditorModel extends EditorModel {

        protected String getValue() {
            return ejb.getLargeIcon();
        }

        protected void setValue(String value) {
            ejb.setLargeIcon(value);
        }
    }

    private class SmallIconEditorModel extends EditorModel {

        protected boolean validate(String value) {
            return value.length() > 0;
        }

        protected String getValue() {
            return ejb.getSmallIcon();
        }

        protected void setValue(String value) {
            ejb.setSmallIcon(value);
        }
    }

    private class DescriptionEditorModel extends EditorModel {

        protected boolean validate(String value) {
            return value.length() > 0;
        }

        protected String getValue() {
            return ejb.getDefaultDescription();
        }

        protected void setValue(String value) {
            ejb.setDescription(value);
        }
    }

    private class DisplayNameEditorModel extends EditorModel {

        protected boolean validate(String value) {
            if (value.length() > 0) {
                return true;
            } else {
                reloadEditorText();
                return false;
            }
        }

        protected String getValue() {
            return ejb.getDefaultDisplayName();
        }

        protected void setValue(String value) {
            ejb.setDisplayName(value);
        }
    }

    public BeanDetailsPanel(SectionNodeView sectionView, EjbJarMultiViewDataObject dataObject, final Ejb ejb) {
        super(sectionView);
        this.dataObject = dataObject;
        this.ejb = ejb;
        new ItemEditorHelper(getDisplayNameTextField(), new DisplayNameEditorModel());
        new ItemEditorHelper(getDescriptionTextArea(), new DescriptionEditorModel());
        new ItemEditorHelper(getSmallIconTextField(), new SmallIconEditorModel());
        new ItemEditorHelper(getLargeIconTextField(), new LargeIconEditorModel());
        getBrowseLargeIconButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO: open browser for the icon
            }
        });
        getBrowseSmallIconButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO: open browser for the icon
            }
        });
    }
}
