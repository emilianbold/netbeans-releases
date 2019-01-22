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
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.ImageEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementEvent;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementListener;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class PropertyEditorImage extends PropertyEditorUserCode implements PropertyEditorElement, PropertyEditorResourceElementListener {

    private JRadioButton radioButton;
    private ImageEditorElement element;
    private String resourcePath = ""; // NOI18N
    private WeakReference<DesignComponent> component;

    private PropertyEditorImage() {
        super(NbBundle.getMessage(PropertyEditorImage.class, "LBL_IMAGE_UCLABEL")); // NOI18N
    }

    public static PropertyEditorImage createInstance() {
        return new PropertyEditorImage();
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (element != null) {
            element.clean(component);
            element = null;
        }
        radioButton = null;
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorImage.class, "LBL_IMAGE_STR")); // NOI18N;
        element = new ImageEditorElement();
        element.addPropertyEditorResourceElementListener(this);
    }

    @Override
    public Component getCustomEditor() {
        DesignComponent component_ = null;
        if (component != null && component.get() != null) {
            component_ = component.get();
        }
        if (element == null) {
            initComponents();
            if (component_ != null) {
                element.init(component_.getDocument());
            }
            initElements(Collections.<PropertyEditorElement>singleton(this));
        }
        element.setDesignComponentWrapper(new DesignComponentWrapper(component_));
        return super.getCustomEditor();
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(resourcePath);
        }
    }

    public void updateState(PropertyValue value) {
        if (value == null) {
            element.setDesignComponentWrapper(null);
        } else if (component != null && component.get() != null) {
            element.setDesignComponentWrapper(new DesignComponentWrapper(component.get()));
        }
        element.setAllEnabled(true);
    }

    @Override
    public void setAsText(String text) {
        saveValue(text);
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }

        PropertyValue value = (PropertyValue) super.getValue();
        return MidpTypes.getString(value);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public JComponent getCustomEditorComponent() {
        return element;
    }

    public JRadioButton getRadioButton() {
        return radioButton;
    }

    public boolean isInitiallySelected() {
        return true;
    }

    public boolean isVerticallyResizable() {
        return true;
    }

    public void elementChanged(PropertyEditorResourceElementEvent event) {
        PropertyValue propertyValue = event.getPropertyValue();
        resourcePath = MidpTypes.getString(propertyValue);
        if (resourcePath == null) {
            resourcePath = ""; // NOI18N
        }
        radioButton.setSelected(true);
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    private void saveValue(String text) {
        super.setValue(MidpTypes.createStringValue(text));
    }

    @Override
    public void init(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
        super.init(component);
    }
}
