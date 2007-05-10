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
package org.netbeans.modules.visualweb.designer.jsf;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** A BeanInfo for designer settings.
 *
 * @author Po-Ting Wu
 * @author Peter Zavadsky (added BeanDescriptor).
 */
public class JsfDesignerPreferencesBeanInfo extends SimpleBeanInfo {
    
    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor(JsfDesignerPreferences.class);
        beanDescriptor.setDisplayName(NbBundle.getBundle(JsfDesignerPreferences.class).getString("CTL_DesignerSettings"));
        beanDescriptor.setValue("helpID", "projrave_ui_elements_options_visual_editor"); // NOI18N
        return beanDescriptor;
    }
    
    /** Provides an explicit property info. */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] desc =
                new PropertyDescriptor[] {
                    new PropertyDescriptor(JsfDesignerPreferences.PROP_GRID_SHOW,JsfDesignerPreferences.class,"getGridShow","setGridShow"),new java.beans.PropertyDescriptor(JsfDesignerPreferences.PROP_GRID_SNAP, JsfDesignerPreferences.class, "getGridSnap", "setGridSnap"),new java.beans.PropertyDescriptor(JsfDesignerPreferences.PROP_GRID_WIDTH, JsfDesignerPreferences.class, "getGridWidth", "setGridWidth"),new java.beans.PropertyDescriptor(JsfDesignerPreferences.PROP_GRID_HEIGHT, JsfDesignerPreferences.class, "getGridHeight", "setGridHeight"),new java.beans.PropertyDescriptor(JsfDesignerPreferences.PROP_PAGE_SIZE, JsfDesignerPreferences.class, "getPageSize", "setPageSize"),new java.beans.PropertyDescriptor(JsfDesignerPreferences.PROP_SHOW_DECORATIONS, JsfDesignerPreferences.class, "isShowDecorations", "setShowDecorations"),new java.beans.PropertyDescriptor(JsfDesignerPreferences.PROP_DEFAULT_FONT_SIZE, JsfDesignerPreferences.class, "getDefaultFontSize", "setDefaultFontSize") // NOI18N
                };

            desc[0].setDisplayName(NbBundle.getMessage(JsfDesignerPreferences.class, "PROP_GRID_SHOW"));
            desc[0].setShortDescription(NbBundle.getMessage(JsfDesignerPreferences.class, "HINT_GRID_SHOW"));

            desc[1].setDisplayName(NbBundle.getMessage(JsfDesignerPreferences.class, "PROP_GRID_SNAP"));
            desc[1].setShortDescription(NbBundle.getMessage(JsfDesignerPreferences.class, "HINT_GRID_SNAP"));

            desc[2].setDisplayName(NbBundle.getMessage(JsfDesignerPreferences.class, "PROP_GRID_WIDTH"));
            desc[2].setShortDescription(NbBundle.getMessage(JsfDesignerPreferences.class,
                    "HINT_GRID_WIDTH"));

            desc[3].setDisplayName(NbBundle.getMessage(JsfDesignerPreferences.class, "PROP_GRID_HEIGHT"));
            desc[3].setShortDescription(NbBundle.getMessage(JsfDesignerPreferences.class,
                    "HINT_GRID_HEIGHT"));

            desc[4].setDisplayName(NbBundle.getMessage(JsfDesignerPreferences.class, "PROP_PAGE_SIZE"));
            desc[4].setShortDescription(NbBundle.getMessage(JsfDesignerPreferences.class, "HINT_PAGE_SIZE"));
            desc[4].setPropertyEditorClass(ResolutionEditor.class);

            desc[5].setDisplayName(NbBundle.getMessage(JsfDesignerPreferences.class, "PROP_SHOW_DECORATIONS"));
            desc[5].setShortDescription(NbBundle.getMessage(JsfDesignerPreferences.class, "HINT_SHOW_DECORATIONS"));
            // #6470521 Do not show this to user, toolbar button is enough.
            desc[5].setHidden(true);

            desc[6].setDisplayName(NbBundle.getMessage(JsfDesignerPreferences.class, "PROP_DEFAULT_FONT_SIZE"));
            desc[6].setShortDescription(NbBundle.getMessage(JsfDesignerPreferences.class, "HINT_DEFAULT_FONT_SIZE"));

            return desc;
        } catch (IntrospectionException ex) {
            log(ex);

            return null;
        }
    }

    /** Returns the designer icon */
    @Override
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/visualweb/designer/jsf/resources/preferences.gif"); // NOI18N
    }
    
    private static void log(Exception exception) {
        Logger logger = Logger.getLogger(JsfDesignerPreferencesBeanInfo.class.getName());
        logger.log(Level.INFO, null, exception);
    }
}
