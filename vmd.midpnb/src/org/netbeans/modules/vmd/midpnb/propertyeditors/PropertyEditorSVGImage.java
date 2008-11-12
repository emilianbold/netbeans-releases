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
package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.DisplayablesCategoryCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementEvent;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementListener;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormSupport;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorSVGImage extends PropertyEditorUserCode implements PropertyEditorElement, PropertyEditorResourceElementListener {

    private JRadioButton radioButton;
    private SVGImageEditorElement customEditor;
    private String resourcePath = ""; // NOI18N

    private PropertyEditorSVGImage() {
        super(NbBundle.getMessage(PropertyEditorSVGImage.class, "LBL_SVGIMAGE_UCLABEL")); // NOI18N;
        initComponents();
        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorSVGImage createInstance() {
        return new PropertyEditorSVGImage();
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.clean(component);
            customEditor = null;
        }
        radioButton = null;
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorSVGImage.class, "LBL_SVGIMAGE_STR")); // NOI18N;

        radioButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PropertyEditorSVGImage.class, "ACSN_SVGIMAGE_STR")); // NOI18N;
        radioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PropertyEditorSVGImage.class, "ACSD_SVGIMAGE_STR")); // NOI18N;

        customEditor = new SVGImageEditorElement();
        customEditor.addPropertyEditorResourceElementListener(this);
        customEditor.setPropertyEditorMessageAwareness(this);
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
            customEditor.setDesignComponentWrapper(null);
        } else if (component != null && component.get() != null) {
            customEditor.setDesignComponentWrapper(new DesignComponentWrapper(component.get()));
        }
        customEditor.setAllEnabled(true);
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
        return customEditor;
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

    private void saveValue(final String text) {
        if (component == null || component.get() == null) {
            return;
        }
        
        final DesignComponent component_ = component.get();
        final PropertyValue oldValue[] = new PropertyValue[1];
        component_.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                  oldValue[0] = component_.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
            }
        });
        component_.getDocument().getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                DesignComponent category = MidpDocumentSupport.getCategoryComponent(component_.getDocument(), DisplayablesCategoryCD.TYPEID);
                Collection<DesignComponent> svgForms = new HashSet<DesignComponent>();
                for (DesignComponent child : category.getComponents()) {
                    if (component_.getDocument().getDescriptorRegistry().isInHierarchy(SVGFormCD.TYPEID, child.getType())) {
                        if (child.readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent() == component_) {
                            svgForms.add(child);
                        }
                    }
                }
               
                if (!svgForms.isEmpty() && oldValue[0].getKind() == PropertyValue.Kind.VALUE) {
                    if (oldValue[0].getPrimitiveValue().equals(text)) {
                        return;
                    }
                    for (DesignComponent svgForm : svgForms) {
                        SVGFormSupport.removeAllSVGFormComponents(svgForm);
                        FileObject svgImageFile = SVGFormSupport.getSVGFile(component_.getDocument(), text);
                        if (svgImageFile != null) {
                            SVGFormSupport.parseSVGImageItems(svgImageFile, svgForm);
                        }
                    }
                    PropertyEditorSVGImage.super.setValue(MidpTypes.createStringValue(text));
                } else {
                    PropertyEditorSVGImage.super.setValue(MidpTypes.createStringValue(text));
                }
            }
        });

    }
}