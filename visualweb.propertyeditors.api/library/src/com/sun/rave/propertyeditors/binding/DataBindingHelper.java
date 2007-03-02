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
package com.sun.rave.propertyeditors.binding;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayAction;


/**
 * A helper class that generates design-time IDE actions for binding components
 * to data providers and other data sources. An implementation is obtained via
 * the class {@link DataBindingHelperRegistry}.
 */
public interface DataBindingHelper {
    
    public static enum Panel {
        BIND_VALUE_TO_OBJECT, BIND_VALUE_TO_DATAPROVIDER, BIND_OPTIONS_TO_DATAPROVIDER,
        BIND_SELECTITEMS_TO_DATAPROVIDER
    };

    public DisplayAction getDataBindingAction(DesignBean bean, String propName, Panel[] panelClasses, boolean showExpr, String menuText, String dialogTitle);

    public DisplayAction getDataBindingAction(DesignBean bean, String propName, Panel[] panelClasses, boolean showExpr, String menuText);

    public DisplayAction getDataBindingAction(DesignBean bean, String propName, Panel[] panelClasses, boolean showExpr);

    public DisplayAction getDataBindingAction(DesignBean bean, String propName, Panel[] panelClasses);

    public DisplayAction getDataBindingAction(DesignBean bean, String propName);
    
}
