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

package org.netbeans.core;

import java.awt.Image;
import java.beans.*;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** A BeanInfo for global IDE settings.
 *
 * @author Ian Formanek
 */
public class IDESettingsBeanInfo extends SimpleBeanInfo {

    /** Provides an explicit property info. */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (IDESettings.PROP_CONFIRM_DELETE, IDESettings.class,
                                               "getConfirmDelete", "setConfirmDelete"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_HOME_PAGE, IDESettings.class,
                                               "getHomePage", "setHomePage"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_SHOW_FILE_EXTENSIONS, IDESettings.class,
                                               "getShowFileExtensions", "setShowFileExtensions"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_SHOW_TOOLTIPS_IN_IDE, IDESettings.class,
                                               "getShowToolTipsInIDE", "setShowToolTipsInIDE"), // NOI18N
                       new PropertyDescriptor (IDESettings.PROP_IGNORED_FILES, IDESettings.class,
                                               "getIgnoredFiles", "setIgnoredFiles"), // NOI18N
                   };

            desc[0].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_CONFIRM_DELETE"));
            desc[0].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_CONFIRM_DELETE"));
            desc[0].setHidden (true);
            
            desc[1].setHidden (true);
            
            desc[2].setHidden (true);

            desc[3].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_SHOW_TOOLTIPS_IN_IDE"));
            desc[3].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class, "HINT_SHOW_TOOLTIPS_IN_IDE"));
            
            desc[4].setDisplayName (NbBundle.getMessage (IDESettingsBeanInfo.class, "PROP_ignoredFiles"));
            desc[4].setShortDescription (NbBundle.getMessage (IDESettingsBeanInfo.class,"HINT_ignoredFiles"));            
            
            return desc;
        } catch (IntrospectionException ex) {
	    Exceptions.printStackTrace(ex);
	    return null;
        }

    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor retval = super.getBeanDescriptor();
        if (retval == null) {
            retval = new BeanDescriptor(IDESettings.class);
        }
        retval.setDisplayName(NbBundle.getMessage (IDESettingsBeanInfo.class, "CTL_IDESettings"));
        return retval;
    }

    /** Returns the IDESettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
	    return Utilities.loadImage("org/netbeans/core/resources/ideSettings.gif"); // NOI18N
        else
            return Utilities.loadImage ("org/netbeans/core/resources/ideSettings32.gif"); // NOI18N
    }

}
