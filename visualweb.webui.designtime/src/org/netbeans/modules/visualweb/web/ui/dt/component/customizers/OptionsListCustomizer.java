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
package org.netbeans.modules.visualweb.web.ui.dt.component.customizers;

import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import com.sun.rave.designtime.faces.ResolveResult;
import com.sun.rave.designtime.impl.BasicCustomizer2;
import com.sun.rave.web.ui.component.AddRemove;
import com.sun.rave.web.ui.component.Selector;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.model.DefaultOptionsList;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.model.OptionsList;
import com.sun.rave.web.ui.model.MultipleSelectOptionsList;
import com.sun.rave.web.ui.model.SingleSelectOptionsList;
import java.awt.Component;
import java.beans.PropertyChangeSupport;


public class OptionsListCustomizer extends BasicCustomizer2 {

    public static String ITEMS_PROP = "items"; //NOI18N
    public static String SELECTED_PROP = "selected"; //NOI18N
    public static String MULTIPLE_PROP = "multiple"; //NOI18N
    public static String OPTIONS_PROP = "options"; //NOI18N
    public static String SELECTEDVALUE_PROP = "selectedValue"; //NOI18N

    public OptionsListCustomizer() {
        super(OptionsListPanel.class, DesignMessageUtil.getMessage(OptionsListCustomizer.class,
                "OptionsListCustomizer.title"), null, "the-help-key"); // NOI18N
        setApplyCapable(true);
    }

    private OptionsListPanel optionsListPanel;
    private DesignBean designBean;
    private DesignBean optionsListBean;

    public Component getCustomizerPanel(DesignBean designBean) {
        this.setDisplayName(DesignMessageUtil.getMessage(OptionsListCustomizer.class,
                "OptionsListCustomizer.title") + " - " + designBean.getInstanceName());
        // If component does not have "items" or "selected" properties, or if
        // "items" is not bound to an instance of DefaultItemsList, return null.
        DesignProperty itemsProperty = designBean.getProperty(ITEMS_PROP);
        if (itemsProperty == null || !(itemsProperty instanceof FacesDesignProperty) || !((FacesDesignProperty) itemsProperty).isBound())
            return null;
        DesignProperty selectedProperty = designBean.getProperty(SELECTED_PROP);
        if (selectedProperty == null || !(selectedProperty instanceof FacesDesignProperty))
            return null;
        String expression = ((FacesDesignProperty) itemsProperty).getValueBinding().getExpressionString();
        ResolveResult resolveResult = ((FacesDesignContext)designBean.getDesignContext()).resolveBindingExprToBean(expression);
        if (resolveResult == null || resolveResult.getDesignBean() == null ||
                        !(OptionsList.class.isAssignableFrom(resolveResult.getDesignBean().getInstance().getClass())))
            return null;
        // Configure a new options editing panel. If compoment has a multiple property
        // make the panel an instance that accepts multiple choice. If compoment's selected
        // property is bound to the DefaultOptionList's selectedValues property, set
        // the panel's valueSelecting property to true. Set the panel's options and
        // selected values to the DefaultOptionsLists's current values.
        this.designBean = designBean;
        this.optionsListBean = resolveResult.getDesignBean();
        DesignProperty multipleProperty = designBean.getProperty(MULTIPLE_PROP);
        if (multipleProperty != null && optionsListBean.getInstance() instanceof DefaultOptionsList) {
            optionsListPanel = new OptionsListPanel(true);
            optionsListPanel.setMultipleChoice(((Boolean) multipleProperty.getValue()).booleanValue());
        } else if (optionsListBean.getInstance() instanceof MultipleSelectOptionsList) {
            optionsListPanel = new OptionsListPanel(true);
            optionsListPanel.setMultipleChoice(true);
        } else if (optionsListBean.getInstance() instanceof SingleSelectOptionsList){
            optionsListPanel = new OptionsListPanel(false);
        }
        optionsListPanel.setOptions((Option []) optionsListBean.getProperty(OPTIONS_PROP).getValue());
        optionsListPanel.setSelectedValues(optionsListBean.getProperty(SELECTEDVALUE_PROP).getValue());
        if (((FacesDesignProperty) selectedProperty).isBound())
            optionsListPanel.setValueSelecting(true);
        return optionsListPanel;
    }
    
    public boolean isModified() {
        return false;
    }
    
    public Result applyChanges() {
        if (optionsListBean == null)
            return Result.FAILURE;
        DesignProperty multipleProperty = designBean.getProperty(MULTIPLE_PROP);
        if (multipleProperty != null && optionsListBean.getInstance() instanceof DefaultOptionsList) {
            Boolean b = optionsListPanel.isMultipleChoice() ? Boolean.TRUE : Boolean.FALSE;
            multipleProperty.setValue(b);
            optionsListBean.getProperty(MULTIPLE_PROP).setValue(b);
        }
        optionsListBean.getProperty(OPTIONS_PROP).setValue(optionsListPanel.getOptions());
        optionsListBean.getProperty(SELECTEDVALUE_PROP).setValue(optionsListPanel.getSelectedValues());
        FacesDesignProperty selectedProperty = (FacesDesignProperty) designBean.getProperty(SELECTED_PROP);
        if (optionsListPanel.isValueSelecting()) {
            FacesDesignContext context = (FacesDesignContext) designBean.getDesignContext();
            designBean.getProperty(SELECTED_PROP).setValueSource(
                    context.getBindingExpr(optionsListBean, "." + SELECTEDVALUE_PROP));
        } else {
            designBean.getProperty(SELECTED_PROP).setValueSource(null);
        }
        return Result.SUCCESS;
    }
    
}
