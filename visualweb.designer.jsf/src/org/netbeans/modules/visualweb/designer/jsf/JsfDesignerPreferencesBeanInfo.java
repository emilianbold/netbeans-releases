/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.designer.jsf;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.ImageUtilities;
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
        return ImageUtilities.loadImage("org/netbeans/modules/visualweb/designer/jsf/resources/preferences.gif"); // NOI18N
    }
    
    private static void log(Exception exception) {
        Logger logger = Logger.getLogger(JsfDesignerPreferencesBeanInfo.class.getName());
        logger.log(Level.INFO, null, exception);
    }
}
