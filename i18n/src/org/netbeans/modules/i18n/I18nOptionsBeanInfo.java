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


package org.netbeans.modules.i18n;


import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Bean info for <code>I18nOptions</code> class.
 *
 * @author  Peter Zavadsky
 */
public class I18nOptionsBeanInfo extends SimpleBeanInfo {
    
    /**
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor(I18nOptions.class);
        descr.setDisplayName(
                NbBundle.getMessage(I18nOptions.class,
                                    "LBL_Internationalization"));       //NOI18N
        return descr;
    }

    /** Overrides superclass method. */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor replaceValuePD = new PropertyDescriptor(I18nOptions.PROP_REPLACE_RESOURCE_VALUE, I18nOptions.class);

            ResourceBundle bundle = NbBundle.getBundle(I18nOptionsBeanInfo.class);
            
            // Set display names.
            replaceValuePD.setDisplayName(bundle.getString("TXT_ReplaceResourceValue"));

            // Set short descriptions.
            replaceValuePD.setShortDescription(bundle.getString("TXT_ReplaceResourceValueDesc"));
            
            return new PropertyDescriptor[] {
                replaceValuePD,
            };
        } catch(IntrospectionException ie) {
            return null;
        }
    }

    /** Overrides superclass method. */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
	    return ImageUtilities.loadImage("org/netbeans/modules/i18n/i18nAction.gif"); // NOI18N
        } else { // 32
            return ImageUtilities.loadImage("org/netbeans/modules/properties/propertiesKey32.gif"); // NOI18N
        }
    }
    
}
