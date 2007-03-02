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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import com.sun.rave.designtime.CustomizerResult;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;



public class DataBindingHelper {

    //------------------------------------------------------------------------ Public Static Methods

    public static Class BIND_VALUE_TO_OBJECT = BindValueToObjectPanel.class;
    public static Class BIND_VALUE_TO_DATAPROVIDER = BindValueToDataProviderPanel.class;
    public static Class BIND_OPTIONS_TO_DATAPROVIDER = BindOptionsToDataProviderPanel.class;
    public static Class BIND_SELECTITEMS_TO_DATAPROVIDER = BindSelectItemsToDataProviderPanel.class;

    public static DisplayAction getDataBindingAction(DesignBean bean, String propName, Class[] panelClasses, boolean showExpr, String menuText, String dialogTitle) {
        return new DataBindingCustomizerAction(bean, propName, panelClasses, showExpr, menuText, dialogTitle);
    }
    public static DisplayAction getDataBindingAction(DesignBean bean, String propName, Class[] panelClasses, boolean showExpr, String menuText) {
        return new DataBindingCustomizerAction(bean, propName, panelClasses, showExpr, menuText);
    }
    public static DisplayAction getDataBindingAction(DesignBean bean, String propName, Class[] panelClasses, boolean showExpr) {
        return new DataBindingCustomizerAction(bean, propName, panelClasses, showExpr);
    }
    public static DisplayAction getDataBindingAction(DesignBean bean, String propName, Class[] panelClasses) {
        return new DataBindingCustomizerAction(bean, propName, panelClasses);
    }
    public static DisplayAction getDataBindingAction(DesignBean bean, String propName) {
        return new DataBindingCustomizerAction(bean, propName);
    }

    //------------------------------------------------------------------------------- Action Classes

    private static class DataBindingCustomizerAction extends BasicDisplayAction {

        public DataBindingCustomizerAction(DesignBean bean, String propName, Class[] panelClasses, boolean showExpr, String menuText, String dialogTitle) {
            super(menuText, null, "projrave_ui_elements_webform_dataref_binding_db"); //NOI18N
            this.bean = bean;
            this.dialogTitle = dialogTitle;
            this.propName = propName;
            this.panelClasses = panelClasses;
            this.showExpr = showExpr;
        }

        public DataBindingCustomizerAction(DesignBean bean, String propName, Class[] panelClasses, boolean showExpr, String menuText) {
            super(menuText, null, "projrave_ui_elements_webform_dataref_binding_db"); //NOI18N
            this.bean = bean;
            this.propName = propName;
            this.panelClasses = panelClasses;
            this.showExpr = showExpr;
        }

        public DataBindingCustomizerAction(DesignBean bean, String propName, Class[] panelClasses, boolean showExpr) {
            super(bundle.getMessage("bindToDataEllipse"), null,
                "projrave_ui_elements_webform_dataref_binding_db"); //NOI18N
            this.bean = bean;
            this.propName = propName;
            this.panelClasses = panelClasses;
            this.showExpr = showExpr;
        }

        public DataBindingCustomizerAction(DesignBean bean, String propName, Class[] panelClasses) {
            super(bundle.getMessage("bindToDataEllipse"), null,
                "projrave_ui_elements_webform_dataref_binding_db"); //NOI18N
            this.bean = bean;
            this.propName = propName;
            this.panelClasses = panelClasses;
        }

        public DataBindingCustomizerAction(DesignBean bean, String propName) {
            super(bundle.getMessage("bindToDataEllipse"), null,
                "projrave_ui_elements_webform_dataref_binding_db"); //NOI18N
            this.bean = bean;
            this.propName = propName;
        }

        protected DesignBean bean;
        protected String propName;
        protected Class[] panelClasses;
        protected boolean showExpr = true;

        protected String dialogTitle;
        public void setDialogTitle(String dialogTitle) {
            this.dialogTitle = dialogTitle;
        }

        public String getDialogTitle() {
            return dialogTitle;
        }

        public Result invoke() {
            DataBindingCustomizer sdbc = dialogTitle != null
                ? new DataBindingCustomizer(propName, panelClasses, showExpr, dialogTitle)
                : new DataBindingCustomizer(propName, panelClasses, showExpr);
            return new CustomizerResult(bean, sdbc);
        }
    }

    public static final Bundle bundle = Bundle.getBundle(DataBindingHelper.class);
}
