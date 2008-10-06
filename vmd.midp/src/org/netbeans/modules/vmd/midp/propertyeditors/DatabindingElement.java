/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author karolharezlak
 */
public final class DatabindingElement implements PropertyEditorElement, CleanUp {

    private static final String DATABINDING_LABEL = "DatabindingElement.radioButton"; // NOI18N
    private static final String ASCN_DATABINDING = "ASCN_Databinding";
    private static final String ASCD_DATABINDING = "ASCD_Databinding";
    private JRadioButton radioButton;
    private DatabindingElementUI customEditor;
    private WeakReference<DesignComponent> component;
    private DesignPropertyEditor propertyEditor;

    public void clean(DesignComponent component) {
        if (customEditor != null) {
            customEditor.clean(component);
            customEditor = null;
        }
        radioButton = null;
        this.component = null;
        propertyEditor = null;
    }

    public DatabindingElement(DesignPropertyEditor propertyEditor) {
        assert propertyEditor != null;
        this.propertyEditor = propertyEditor;
    }

    public void updateState(PropertyValue value) {
        if (component == null) {
            return;
        }
        final DesignComponent c = component.get();
        if (c == null) {
            return;
        }
        customEditor.updateComponent(c);
    }

    public void updateDesignComponent(DesignComponent component) {
        this.component = new WeakReference(component);
    }

    public void setTextForPropertyValue(String text) {
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public JComponent getCustomEditorComponent() {
        if (customEditor == null) {
            customEditor = new DatabindingElementUI(propertyEditor, radioButton);
            radioButton.setSelected(false);
            if (component != null) {
                customEditor.updateComponent(component.get());
            }
        }
        return customEditor;
    }

    public JRadioButton getRadioButton() {
        if (radioButton == null) {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(
                    DatabindingElement.class, DATABINDING_LABEL));
            radioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                    DatabindingElement.class, ASCN_DATABINDING));
            radioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                    DatabindingElement.class, ASCD_DATABINDING));
        }
        return radioButton;
    }

    public boolean isInitiallySelected() {
        return true;
    }

    public boolean isVerticallyResizable() {
        return true;
    }
}
