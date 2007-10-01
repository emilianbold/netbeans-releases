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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.options;

import java.beans.*;
import java.awt.Image;
import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka, Ales Novak
*/
public class BasePrintOptionsBeanInfo extends SimpleBeanInfo {

    /** Prefix of the icon location. */
    private String iconPrefix;

    /** Icons for compiler settings objects. */
    private Image icon;
    private Image icon32;

    public BasePrintOptionsBeanInfo() {
        this("/org/netbeans/modules/editor/resources/baseOptions"); // NOI18N
    }

    public BasePrintOptionsBeanInfo(String iconPrefix) {
        this.iconPrefix = iconPrefix;
    }

    /*
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] descriptors;
        String[] propNames = getPropNames();
        try {
            descriptors = new PropertyDescriptor[propNames.length];

            for (int i = 0; i < propNames.length; i++) {
                descriptors[i] = new PropertyDescriptor(propNames[i], getBeanClass());
                descriptors[i].setDisplayName(getString("PROP_" + propNames[i])); // NOI18N
                descriptors[i].setShortDescription(getString("HINT_" + propNames[i])); // NOI18N
                if (BasePrintOptions.PRINT_COLORING_MAP_PROP.equals(propNames[i])) {
                    descriptors[i].setPropertyEditorClass(ColoringArrayEditor.class);
                }
            }
        } catch (IntrospectionException e) {
            descriptors = new PropertyDescriptor[0];
        }
        return descriptors;
    }

    protected String getString(String s) {
        return NbBundle.getMessage(BasePrintOptionsBeanInfo.class, s);
    }

    protected Class getBeanClass() {
        return BasePrintOptions.class;
    }

    protected String[] getPropNames() {
        return BasePrintOptions.BASE_PROP_NAMES;
    }

    /* @param type Desired type of the icon
    * @return returns the Java loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage(iconPrefix + ".gif"); // NOI18N
            return icon;
        }
        else {
            if (icon32 == null)
                icon32 = loadImage(iconPrefix + "32.gif"); // NOI18N
            return icon32;
        }
    }
}
