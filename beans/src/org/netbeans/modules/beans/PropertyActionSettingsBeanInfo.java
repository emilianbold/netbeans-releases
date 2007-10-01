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

package org.netbeans.modules.beans;

import java.awt.Image;
import java.beans.*;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Description of {@link PropertyActionSettings}.
 *
 * @author Petr Suchomel
 */
public class PropertyActionSettingsBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor(org.netbeans.modules.beans.PropertyActionSettings.class);
        descr.setDisplayName(NbBundle.getBundle(PropertyActionSettingsBeanInfo.class).getString("PROP_Option_Menu"));
        descr.setValue ("version", "1.1"); // NOI18N            
        descr.setValue("global", Boolean.FALSE); // NOI18N
        return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {                
            PropertyDescriptor propstyle = new PropertyDescriptor("propStyle", PropertyActionSettings.class);
            propstyle.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Prop_Style"));
            propstyle.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Prop_Style"));
            propstyle.setPropertyEditorClass (PropertyStyleEditor.class);

    	    return new PropertyDescriptor[] {propstyle};
        } catch (IntrospectionException ie) {
	        ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    public Image getIcon (int type) {
        return Utilities.loadImage("org/netbeans/modules/beans/resources/beansSetting.gif");
    }
    
    public static class PropertyStyleEditor extends PropertyEditorSupport {

        private static final String[] tags = {
            NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "MSG_Option_Gen_Undescored"),
            NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "MSG_Option_Gen_This"),
        };

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            String type = (String) getValue ();
            
            if(type.equals(PropertyActionSettings.GENERATE_UNDERSCORED)){
                return tags[0];
            }
            else if(type.equals(PropertyActionSettings.GENERATE_WITH_THIS)){
                return tags[1];
            }
            return tags[0];
        }

        public void setAsText (String text) throws IllegalArgumentException {
            if (tags[0].equals (text))
                setValue (PropertyActionSettings.GENERATE_UNDERSCORED);
            else if (tags[1].equals (text))
                setValue (PropertyActionSettings.GENERATE_WITH_THIS);
            else
                throw new IllegalArgumentException ();
        }
    }
}
