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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JRadioButton;

import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGListCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGListElementEventSourceCD;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

/**
 * @author ads
 *
 */
public final class PropertyEditorListModel extends PropertyEditorUserCode
        implements PropertyEditorElement {

    private PropertyEditorListModel(String userCodeLabel, String modelText) {
        super(userCodeLabel);
        myModelText = modelText;
    }

    public static PropertyEditorListModel createInstance(String label,
            String modelText) {
        return new PropertyEditorListModel(label, modelText);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode#cleanUp(org.netbeans.modules.vmd.api.model.DesignComponent)
     */
    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (myCustomEditor != null) {
            myCustomEditor.cleanUp();
            myCustomEditor = null;
        }
        myRadioButton = null;
    }

    @Override
    public boolean executeInsideWriteTransaction() {
        if (component.get().getType() == SVGListCD.TYPEID) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isExecuteInsideWriteTransactionUsed() {
        PropertyValue.Kind kind = null;
        if (getValue() instanceof PropertyValue) {
            kind = ((PropertyValue) getValue()).getKind();
        }
        if (component.get().getType() == SVGListCD.TYPEID && kind != PropertyValue.Kind.USERCODE) {
            return true;
        } else if (component.get().getType() == SVGListCD.TYPEID && kind == PropertyValue.Kind.USERCODE) {
            component.get().getDocument().getTransactionManager().writeAccess(new Runnable() {

                public void run() {
                    component.get().writeProperty(SVGListCD.PROP_ELEMENTS, PropertyValue.createNull());
                    Collection<DesignComponent> children = new HashSet<DesignComponent>(component.get().getComponents());
                    for (DesignComponent child : children) {
                        component.get().getDocument().deleteComponent(child);
                    }
                }
            });
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (myCustomEditor != null) {
                        myCustomEditor.removeElemnts();
                    }
                }
            });
        }
        return false;
    }

    @Override
    public Boolean canEditAsText() {
        return false;
    }

    @Override
    public String getAsText() {
        PropertyValue.Kind kind = null;
        if (getValue() instanceof PropertyValue) {
            kind = ((PropertyValue) getValue()).getKind();
        }
        if (kind == PropertyValue.Kind.USERCODE) {
            return super.getAsText();
        }
        return myModelText;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#getCustomEditorComponent()
     */
    public JComponent getCustomEditorComponent() {
        return myCustomEditor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#getRadioButton()
     */
    public JRadioButton getRadioButton() {
        return myRadioButton;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode#getCustomEditor()
     */
    @Override
    public Component getCustomEditor() {
        if (myCustomEditor == null) {
            initComponents();
            initElements(Collections.<PropertyEditorElement>singleton(this));
        }
        if (component.get().getType() == SVGListCD.TYPEID) {
            component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    myCustomEditor.setValue(component.get().readProperty(SVGListCD.PROP_ELEMENTS));
                }
            });
        }
        return super.getCustomEditor();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#getTextForPropertyValue()
     */
    public String getTextForPropertyValue() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#isInitiallySelected()
     */
    public boolean isInitiallySelected() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#isVerticallyResizable()
     */
    public boolean isVerticallyResizable() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#setTextForPropertyValue(java.lang.String)
     */
    public void setTextForPropertyValue(String arg0) {
        // should not be called becuase it is not editable as text
        assert false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement#updateState(org.netbeans.modules.vmd.api.model.PropertyValue)
     */
    public void updateState(final PropertyValue value) {
        if (value != null) {
            component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    myCustomEditor.setValue(value);
                }
            });

        }
        myRadioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode#customEditorOKButtonPressed()
     */
    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();

        if (myRadioButton.isSelected()) {
            if (SVGListCD.TYPEID != component.get().getType()) {
                saveValue(myCustomEditor.getValue());
            } else {
                component.get().getDocument().getTransactionManager().writeAccess(new Runnable() {

                    public void run() {
                        Integer index = -1;
                        List<PropertyValue> array = component.get().readProperty(SVGListCD.PROP_ELEMENTS).getArray();
                        if (array != null) {
                            index = array.size() - 1;
                        }
                        if (index == myCustomEditor.getValue().size() - 1) {
                            return;
                        }
                        if (index < myCustomEditor.getValue().size() - 1) {
                            if (index == -1) {
                                index = 0;
                            } else {
                                index++;
                            }
                            for (int i = index; i <= myCustomEditor.getValue().size() - 1; i++) {
                                DesignComponent element = component.get().getDocument().createComponent(SVGListElementEventSourceCD.TYPEID);
                                component.get().addComponent(element);
                                //element.writeProperty(SVGListElementEventSourceCD.PROP_INDEX, MidpTypes.createIntegerValue(i));
                                //array = component.get().readProperty(SVGListCD.PROP_ELEMENTS);
                                if (array == null) {
                                    component.get().writeProperty(SVGListCD.PROP_ELEMENTS, PropertyValue.createArray(SVGListElementEventSourceCD.TYPEID, new ArrayList<PropertyValue>()));
                                }
                                ArraySupport.append(component.get(), SVGListCD.PROP_ELEMENTS, element);
                                array = component.get().readProperty(SVGListCD.PROP_ELEMENTS).getArray();
                            }

                        } else if (index > myCustomEditor.getValue().size() - 1) {
                            for (PropertyValue value : array) {
                                DesignComponent child = value.getComponent();
                                if (child.getType() != SVGListElementEventSourceCD.TYPEID) {
                                    continue;
                                }
                                Integer currentIndex = array.indexOf(value);
                                if (currentIndex == null) {
                                    throw new IllegalArgumentException();
                                }
                                if (currentIndex > myCustomEditor.getValue().size() - 1) {
                                    PropertyValue array_ = component.get().readProperty(SVGListCD.PROP_ELEMENTS);
                                    if (array != null) {
                                        ArraySupport.remove(component.get(), SVGListCD.PROP_ELEMENTS, child);
                                    }
                                    component.get().getDocument().deleteComponent(child);
                                }
                            }
                        }

                    }
                });
                component.get().getDocument().getTransactionManager().writeAccess(new Runnable() {

                    public void run() {
                        PropertyValue arrayValue = component.get().readProperty(SVGListCD.PROP_ELEMENTS);
                        if (arrayValue == PropertyValue.createNull()) {
                            setValue(arrayValue);
                            return;
                        }
                        List<PropertyValue> array = arrayValue.getArray();
                        if (array != null) {
                            for (PropertyValue value : array) {
                                DesignComponent child = value.getComponent();
                                if (child.getType() != SVGListElementEventSourceCD.TYPEID) {
                                    continue;
                                }
                                String string = (String) child.readProperty(SVGListElementEventSourceCD.PROP_STRING).getPrimitiveValue();
                                Integer childIndex = array.indexOf(value);
                                if (string == null || !string.equals(myCustomEditor.getValue().get(childIndex))) {
                                    child.writeProperty(SVGListElementEventSourceCD.PROP_STRING, MidpTypes.createStringValue(myCustomEditor.getValue().get(childIndex)));
                                }
                            }
                        } else {
                            PropertyValue model = PropertyValue.createEmptyArray(MidpTypes.TYPEID_JAVA_LANG_STRING);
                            if (component != null) {
                                DesignComponent c = component.get();
                                c.writeProperty(PropertyEditorListModel.this.getPropertyNames().iterator().next(),  PropertyValue.createNull());
                            }
                        }
                    }
                });
                component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        PropertyValue value = component.get().readProperty(SVGListCD.PROP_ELEMENTS);
                        PropertyEditorListModel.super.setValue(value);
                    }
                });
            }
        }
    }

    private void saveValue(List<String> modelItems) {
        List<PropertyValue> list = new ArrayList<PropertyValue>(modelItems.size());
        for (String string : modelItems) {
            PropertyValue value = MidpTypes.createStringValue(string);
            list.add(value);
        }
        PropertyValue model = PropertyValue.createArray(
                MidpTypes.TYPEID_JAVA_LANG_STRING, list);
        super.setValue(model);
    }

    private void initComponents() {
        myRadioButton = new JRadioButton();
        Mnemonics.setLocalizedText(myRadioButton, NbBundle.getMessage(
                PropertyEditorButtonGroup.class, "LBL_DefaultModel")); // NOI18N

        myRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                PropertyEditorButtonGroup.class, "ACSN_DefaultModel")); // NOI18N
        myRadioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(
                PropertyEditorButtonGroup.class, "ACSD_DefaultModel"));

        myCustomEditor = new SVGListPropertyCustomEditor();
    }
    private SVGListPropertyCustomEditor myCustomEditor;
    private JRadioButton myRadioButton;
    private String myModelText;
}
