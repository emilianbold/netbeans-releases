/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.gizmo.options;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.List;
import org.netbeans.modules.dlight.toolsui.api.ToolsCustomizerPanelFactory;
import org.netbeans.modules.dlight.toolsui.api.PanelWithApply;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class GizmoStringNodeProp extends Node.Property<String> {

    private GizmoStringConfiguration stringConfiguration;
    private String def = null;
    private boolean canWrite = true;
    private final String name;
    private final String description;
    IntEditor intEditor = null;

    public GizmoStringNodeProp(GizmoStringConfiguration stringConfiguration, String txt1, String name, String description) {
        super(String.class);
        this.name = name;
        this.description = description;
        this.stringConfiguration = stringConfiguration;
        setValue("title", NbBundle.getMessage(GizmoStringNodeProp.class, "DLG_TITLE_ConfigurationManager")); // NOI18N
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortDescription() {
        return description;
    }
    
//    @Override
//    public String getHtmlDisplayName() {
//        if (getStringConfiguration().getModified()) {
//            return "<b>" + getDisplayName(); // NOI18N
//        }
//        else {
//            return null;
//        }
//    }

    public String getValue() {
        return getStringConfiguration().getValueDef(def);
    }

    public void setValue(String v) {
        getStringConfiguration().setValue(v);
    }

    public boolean canWrite() {
        return canWrite;
    }


    public boolean canRead() {
        return true;
    }

    @Override
    public void restoreDefaultValue() {
        getStringConfiguration().reset();
    }

    @Override
    public boolean supportsDefaultValue() {
//        return true;
        return false;
    }

    @Override
    public boolean isDefaultValue() {
        return !stringConfiguration.getModified();
    }
    
    public void setDefaultValue(String def) {
        this.def = def;
        getStringConfiguration().setDefaultValue(def);
    }

    public void setCanWrite(boolean v) {
        canWrite = v;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (intEditor == null) {
            intEditor = new IntEditor();
        }
        return intEditor;
    }

    /**
     * @return the stringConfiguration
     */
    public GizmoStringConfiguration getStringConfiguration() {
        return stringConfiguration;
    }

    /**
     * @param stringConfiguration the stringConfiguration to set
     */
    public void setStringConfiguration(GizmoStringConfiguration stringConfiguration) {
        this.stringConfiguration = stringConfiguration;
    }

    private class IntEditor extends PropertyEditorSupport implements ExPropertyEditor, VetoableChangeListener {
        private PropertyEnv env;
        private PanelWithApply toolsManagerPanel = null;

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            return getStringConfiguration().getValue();
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public String[] getTags() {
            List<String> list = getStringConfiguration().getGizmoOptionsImpl().getValidConfigurationDisplayNames();
            return list.toArray(new String[list.size()]);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(this);
            toolsManagerPanel = ToolsCustomizerPanelFactory.getCustomizerByName(getAsText());
            return toolsManagerPanel;
        }

        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        /**
         * Once the user presses OK, we attempt to validate the remote host. We never veto the action
         * because a failure should still close the property editor, but with the host still offline.
         * Set the PropertyEnv state to valid so the dialog is removed.
         *
         * @param evt A PropertyEnv where we can control the custom property editor
         * @throws java.beans.PropertyVetoException
         */
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            toolsManagerPanel.apply();
        }
    }
}
