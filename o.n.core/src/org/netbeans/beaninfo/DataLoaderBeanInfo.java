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

package org.netbeans.beaninfo;

import java.awt.Component;
import java.awt.Image;
import java.beans.*;

import org.openide.awt.Actions;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.loaders.DataLoader;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;

/** BeanInfo for {@link DataLoader}. */
public class DataLoaderBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor representationClass = new PropertyDescriptor ("representationClassName", DataLoader.class, "getRepresentationClassName", null); // NOI18N
            representationClass.setDisplayName (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("PROP_representationClass"));
            representationClass.setShortDescription (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("HINT_representationClass"));
            representationClass.setExpert (true);
            PropertyDescriptor actions = new PropertyDescriptor ("actions", DataLoader.class); // NOI18N
            actions.setDisplayName (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("PROP_actions"));
            actions.setShortDescription (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("HINT_actions"));
            actions.setPropertyEditorClass (ActionsEditor.class);
            actions.setValue ("canEditAsText", Boolean.FALSE); // NOI18N
            return new PropertyDescriptor[] { actions, representationClass };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }

    /**
    * Return the icon
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return ImageUtilities.loadImage("org/netbeans/core/resources/objectTypes.gif"); // NOI18N
        else
            return ImageUtilities.loadImage("org/netbeans/core/resources/objectTypes32.gif"); // NOI18N
    }

    public static class ActionsEditor extends PropertyEditorSupport
    implements ExPropertyEditor {

        private PropertyEnv env;

        public boolean supportsCustomEditor () {
            return true;
        }

        public Component getCustomEditor () {
            return new LoaderActionsPanel (this, env);
        }
        
        public String getAsText () {
            SystemAction[] actions = (SystemAction[]) getValue ();
            if (actions == null) return ""; // NOI18N
            StringBuffer buf = new StringBuffer(actions.length * 15 + 1);
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] == null) continue;
                if (i > 0) buf.append (", "); // I18N?
                buf.append (Actions.cutAmpersand (actions[i].getName ()));
            }
            return buf.toString ();
        }
        
        public void setAsText (String text) throws IllegalArgumentException {
            throw new IllegalArgumentException ();
        }

        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }

    }

}
