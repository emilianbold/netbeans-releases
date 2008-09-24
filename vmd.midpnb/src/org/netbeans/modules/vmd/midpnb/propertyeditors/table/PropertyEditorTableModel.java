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

package org.netbeans.modules.vmd.midpnb.propertyeditors.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.ref.WeakReference;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementEvent;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementListener;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorTableModel extends DesignPropertyEditor implements PropertyEditorResourceElementListener {

    private WeakReference<DesignComponent> component;
    private JPanel customEditorPanel;
    private TableModelEditorElement customEditor;
    private PropertyValue values;
    private PropertyValue headers;

    private PropertyEditorTableModel() {
        initComponents();
    }

    public static PropertyEditorTableModel createInstance() {
        return new PropertyEditorTableModel();
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.clean(component);
            customEditor = null;
        }
        values = null;
        headers = null;
        if (customEditorPanel == null) {
            customEditorPanel.removeAll();
        }
        this.component = null;
    }

    private void initComponents() {
        customEditor = new TableModelEditorElement();
        customEditor.addPropertyEditorResourceElementListener(this);
        customEditorPanel = new JPanel(new BorderLayout());
        customEditorPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        customEditorPanel.add(customEditor, BorderLayout.CENTER);
    }

    @Override
    public void init(DesignComponent component) {
        if (component != null) {
            this.component = new WeakReference<DesignComponent>(component);
        }
    }

    @Override
    public Component getCustomEditor() {
        //if (customEditorPanel.isShowing()) {
        if (component != null && component.get() != null) {
            customEditor.setDesignComponentWrapper(new DesignComponentWrapper(component.get()));
        }
        customEditor.setAllEnabled(true);
        //}
        return customEditorPanel;
    }

    @Override
    public Boolean canEditAsText() {
        return Boolean.FALSE;
    }

    @Override
    public String getAsText() {
        return NbBundle.getMessage(PropertyEditorTableModel.class, "DISP_PE_TableModel_GetAsText"); //NOI18N
    }

    @Override
    public boolean executeInsideWriteTransaction() {
        if (component == null || component.get() == null) {
            return false;
        }

        DesignComponent _component = component.get();
        if (headers != null) {
            _component.writeProperty(SimpleTableModelCD.PROP_COLUMN_NAMES, headers);
        }
        if (values != null) {
            _component.writeProperty(SimpleTableModelCD.PROP_VALUES, values);
        }
        return false;
    }

    @Override
    public boolean isExecuteInsideWriteTransactionUsed() {
        return true;
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    public void elementChanged(PropertyEditorResourceElementEvent event) {
        PropertyValue propertyValue = event.getPropertyValue();
        String propertyName = event.getPropertyName();
        if (SimpleTableModelCD.PROP_COLUMN_NAMES.equals(propertyName)) {
            headers = propertyValue;
        } else if (SimpleTableModelCD.PROP_VALUES.equals(propertyName)) {
            values = propertyValue;
        } else {
            throw Debug.illegalArgument("Illegal property value has been passed"); // NOI18N
        }
    }

    @Override
    public boolean isDefaultValue() {
        if (component == null || component.get() == null) {
            return true;
        }

        final boolean[] isDefaultValue = new boolean[]{true};
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                DesignComponent _component = component.get();
                boolean names = PropertyValue.createNull().equals(_component.readProperty(SimpleTableModelCD.PROP_COLUMN_NAMES));
                boolean values = PropertyValue.createNull().equals(_component.readProperty(SimpleTableModelCD.PROP_VALUES));
                isDefaultValue[0] = names && values;
            }
        });

        return isDefaultValue[0];
    }

    @Override
    public boolean isResetToDefaultAutomatically() {
        return false;
    }

    @Override
    public void customEditorResetToDefaultButtonPressed() {
        if (component != null && component.get() != null) {
            DesignComponent _component = component.get();
            _component.writeProperty(SimpleTableModelCD.PROP_COLUMN_NAMES, PropertyValue.createNull());
            _component.writeProperty(SimpleTableModelCD.PROP_VALUES, PropertyValue.createNull());
        }
    }
}