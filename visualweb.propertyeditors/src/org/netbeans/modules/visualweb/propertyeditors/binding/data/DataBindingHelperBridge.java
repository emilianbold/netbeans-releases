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

import com.sun.rave.propertyeditors.binding.DataBindingHelper.Panel;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayAction;
import java.util.EnumMap;

/**
 * A bridge between the exported DataBindingHelper interface and the internal implementation.
 *
 * @author gjmurphy
 */
public class DataBindingHelperBridge implements com.sun.rave.propertyeditors.binding.DataBindingHelper {
    
    public DisplayAction getDataBindingAction(DesignBean designBean, String propertyName, Panel[] panels, boolean showExpr, String menuText, String dialogTitle) {
        return DataBindingHelper.getDataBindingAction(designBean, propertyName, getPanelClasses(panels), showExpr, menuText, dialogTitle);
    }

    public DisplayAction getDataBindingAction(DesignBean designBean, String propertyName, Panel[] panels, boolean showExpr, String menuText) {
        return DataBindingHelper.getDataBindingAction(designBean, propertyName, getPanelClasses(panels), showExpr, menuText);
    }

    public DisplayAction getDataBindingAction(DesignBean designBean, String propertyName, Panel[] panels, boolean showExpr) {
        return DataBindingHelper.getDataBindingAction(designBean, propertyName, getPanelClasses(panels), showExpr);
    }

    public DisplayAction getDataBindingAction(DesignBean designBean, String propertyName, Panel[] panels) {
        return DataBindingHelper.getDataBindingAction(designBean, propertyName, getPanelClasses(panels));
    }

    public DisplayAction getDataBindingAction(DesignBean designBean, String propertyName) {
        return DataBindingHelper.getDataBindingAction(designBean, propertyName);
    }
    
    private static EnumMap<Panel,Class> panelClassMap = new EnumMap<Panel,Class>(Panel.class);
    
    static {
        panelClassMap.put(Panel.BIND_VALUE_TO_OBJECT, DataBindingHelper.BIND_VALUE_TO_OBJECT);
        panelClassMap.put(Panel.BIND_VALUE_TO_DATAPROVIDER, DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER);
        panelClassMap.put(Panel.BIND_OPTIONS_TO_DATAPROVIDER, DataBindingHelper.BIND_OPTIONS_TO_DATAPROVIDER);
        panelClassMap.put(Panel.BIND_SELECTITEMS_TO_DATAPROVIDER, DataBindingHelper.BIND_SELECTITEMS_TO_DATAPROVIDER);
    }
    
    private Class[] getPanelClasses(Panel[] panels) {
        Class[] classes = new Class[panels.length];
        for (int i = 0; i < panels.length; i++) {
            classes[i] = panelClassMap.get(panels[i]);
        }
        return classes;
    }
    
}
