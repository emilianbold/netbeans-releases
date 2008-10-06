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
package org.netbeans.modules.vmd.midp.propertyeditors.api.resource;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteSupport;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.CleanUp;
import org.netbeans.modules.vmd.midp.propertyeditors.DatabindingElement;
import org.netbeans.modules.vmd.midp.propertyeditors.DatabindingElementUI;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.FontEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.ImageEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.TickerEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorResource extends PropertyEditorUserCode implements PropertyEditorElement {

    private Map<String, DesignComponent> createdComponents;
    private final TypeID componentTypeID;
    private String noneComponentAsText;
    private String newComponentAsText;
    private ResourceEditorPanel rePanel;
    private JRadioButton radioButton;
    private PropertyEditorResourceElement perElement;
    private DatabindingElement databindingElement;

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (createdComponents != null) {
            createdComponents.clear();
            createdComponents = null;
        }
        if (rePanel != null) {
            rePanel.clean(component);
            rePanel = null;
        }
        radioButton = null;
        if (perElement instanceof CleanUp) {
            ((CleanUp) perElement).clean(component);
        }
        perElement = null;
        if (databindingElement != null) {
            databindingElement.clean(component);
        }
        databindingElement = null;
    }

    private PropertyEditorResource(PropertyEditorResourceElement perElement,
            String newComponentAsText,
            String noneComponentAsText,
            String userCodeLabel,
            boolean databinding) {
        super(userCodeLabel);

        if (newComponentAsText == null || noneComponentAsText == null) {
            throw Debug.illegalArgument("Argument can not be null"); //NOI18N
        }

        if (newComponentAsText.equals(noneComponentAsText)) {
            throw Debug.illegalArgument("Arguments can not be equal"); //NOI18N
        }

        this.componentTypeID = perElement.getTypeID();
        this.newComponentAsText = newComponentAsText;
        this.noneComponentAsText = noneComponentAsText;
        this.perElement = perElement;
        perElement.setPropertyEditorMessageAwareness(this);

        createdComponents = new HashMap<String, DesignComponent>();

        // TODO lazy init
        radioButton = new JRadioButton();
        rePanel = new ResourceEditorPanel(perElement, noneComponentAsText, radioButton);
        Mnemonics.setLocalizedText(radioButton,
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_RB_RESOURCE")); // NOI18N
        radioButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PropertyEditorResource.class, "ACSN_RB_RESOURCE"));
        radioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PropertyEditorResource.class, "ACSD_RB_RESOURCE"));
        perElement.addPropertyEditorResourceElementListener(rePanel);
        if (databinding) {
            Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(2);
            databindingElement = new DatabindingElement(this);
            elements.add(this);
            elements.add(databindingElement);
            initElements(elements);
        } else {
            initElements(Collections.<PropertyEditorElement>singleton(this));
        }
    }

    public static final PropertyEditorResource createInstance(PropertyEditorResourceElement perElement, String newComponentAsText, String noneComponentAsText, String userCodeLabel) {
        return new PropertyEditorResource(perElement, newComponentAsText, noneComponentAsText, userCodeLabel, false);
    }

    public static final DesignPropertyEditor createFontPropertyEditor() {
        return new PropertyEditorResource(new FontEditorElement(), NbBundle.getMessage(PropertyEditorResource.class, "LBL_FONTRESOURCEPE_NEW"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_FONTRESOURCEPE_NONE"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_FONTRESOURCEPE_UCLABEL"), false); //NOI18N
    }

    public static final DesignPropertyEditor createTickerPropertyEditor() {
        return new PropertyEditorResource(new TickerEditorElement(), NbBundle.getMessage(PropertyEditorResource.class, "LBL_TICKERRESOURCEPE_NEW"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_TICKERRESOURCEPE_NONE"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_TICKERRESOURCEPE_UCLABEL"), false); //NOI18N
    }

    public static final DesignPropertyEditor createImagePropertyEditor() {
        return new PropertyEditorResource(new ImageEditorElement(), NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_NEW"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_NONE"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_UCLABEL"), false); //NOI18N
    }

    public static final DesignPropertyEditor createImagePropertyEditorWithDatabinding() {
        return new PropertyEditorResource(new ImageEditorElement(), NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_NEW"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_NONE"), NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_UCLABEL"), true); //NOI18N
    }

    @Override
    public final Component getCustomEditor() {
        perElement.getCustomEdiotrNotification();
        return super.getCustomEditor();
    }

    private Map<String, DesignComponent> getComponentsMap() {
        final Map<String, DesignComponent> componentsMap = new TreeMap<String, DesignComponent>();
        if (component == null || component.get() == null) {
            return componentsMap;
        }

        final DesignDocument document = component.get().getDocument();
        document.getTransactionManager().readAccess(new Runnable() {

            public void run() {
                Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(document, ResourcesCategoryCD.TYPEID).getComponents();
                for (DesignComponent comp : components) {
                    if (comp.getType().equals(componentTypeID)) {
                        componentsMap.put(getComponentDisplayName(comp), comp);
                    }
                }
            }
        });
        return componentsMap;
    }

    private String getComponentDisplayName(DesignComponent component) {
        if (component == null) {
            return noneComponentAsText;
        }
        // issue 104721 fix
        // dirty hack to check whether component was detached from document or not
        if (component.getParentComponent() == null && component.getDocument().getRootComponent() != component) {
            return noneComponentAsText;
        }

        return MidpValueSupport.getHumanReadableString(component);
    }

    @Override
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        }
        if (component == null || getPropertyNames() == null) {
            return null;
        }
        String databinding = MidpDatabindingSupport.getDatabaindingAsText(component.get(), getPropertyNames().get(0));
        if (databinding != null) {
            return databinding;
        }
        PropertyValue value = (PropertyValue) super.getValue();
        return getDecodeValue(value);
    }

    private void saveValue(String text) {
        if (text == null || text.length() <= 0) {
            return;
        }
        if (component == null || component.get() == null) {
            return;
        }

        final DesignDocument document = component.get().getDocument();
        Map<String, DesignComponent> componentsMap = getComponentsMap();
        if (componentsMap.get(text) != null) {
            setValue(PropertyValue.createComponentReference(componentsMap.get(text)));
        } else if (text.equals(noneComponentAsText)) {
            setValue(NULL_VALUE);
        } else if (text.equals(newComponentAsText)) {
            document.getTransactionManager().writeAccess(new Runnable() {

                public void run() {
                    ComponentProducer producer = DocumentSupport.getComponentProducer(document, componentTypeID.toString());
                    if (producer == null) {
                        throw new IllegalStateException("No producer for TypeID : " + componentTypeID.toString()); // NOI18N
                    }
                    DesignComponent category = MidpDocumentSupport.getCategoryComponent(document, ResourcesCategoryCD.TYPEID);
                    ComponentProducer.Result result = AcceptSupport.accept(category, producer, null);
                    DesignComponent createdComponent = result != null ? result.getMainComponent() : null;

                    if (createdComponent != null) {
                        initInstanceNameForComponent(createdComponent);
                        PropertyEditorResource.this.setValue(PropertyValue.createComponentReference(createdComponent));
                    }
                }
            });
        } else {
            Map<String, DesignComponentWrapper> wrappersMap = rePanel.getWrappersMap();
            for (String key : wrappersMap.keySet()) {
                if (key.equals(text)) {
                    DesignComponent createdComponent = createdComponents.get(text);
                    setValue(PropertyValue.createComponentReference(createdComponent));
                    createdComponents.clear();
                    break;
                }
            }
        }
    }

    private void setValue(PropertyValue value) {
        super.setValue(value);
        final DesignComponent component_ = component.get();
        if (!NULL_VALUE.equals(value) && perElement.isPostSetValueSupported(component_)) {
            perElement.postSetValue(component_, value.getComponent());
        } else if (NULL_VALUE.equals(value)) {
            perElement.nullValueSet(component_);
        }
    }

    // invoke in the write transaction
    private void initInstanceNameForComponent(DesignComponent component) {
        String nameToBeCreated = perElement.getResourceNameSuggestion();
        PropertyValue instanceName = InstanceNameResolver.createFromSuggested(component, nameToBeCreated);
        component.writeProperty(ClassCD.PROP_INSTANCE_NAME, instanceName);
    }

    private String getDecodeValue(final PropertyValue value) {
        if (value == null || value.getKind() == PropertyValue.Kind.NULL) {
            return noneComponentAsText;
        }
        if (component == null || component.get() == null) {
            return noneComponentAsText;
        }

        final String[] decodeValue = new String[1];
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                DesignComponent valueComponent = value.getComponent();
                decodeValue[0] = getComponentDisplayName(valueComponent);
            }
        });

        return decodeValue[0] != null ? decodeValue[0] : "n/a"; //NOI18N
    }

    @Override
    public void init(DesignComponent component) {
        perElement.setDesignComponent(component);
        super.init(component);
    }

    @Override
    public String[] getTags() {
        Set<String> components = getComponentsMap().keySet();
        List<String> tags = new ArrayList<String>(components.size() + 2);
        if (isCurrentValueAUserCodeType()) {
            tags.add(PropertyEditorUserCode.USER_CODE_TEXT);
        } else {
            tags.add(noneComponentAsText);
            tags.addAll(components);
            tags.add(newComponentAsText);
        }
        return tags.toArray(new String[tags.size()]);
    }

    @Override
    public Boolean canEditAsText() {
        return null;
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (getRadioButton().isSelected()) {
            saveChanges();
            saveValue(getTextForPropertyValue());
        }
        final DesignComponent _component = component.get();
        if (databindingElement != null && databindingElement.getRadioButton().isSelected()) {
            ((DatabindingElementUI) databindingElement.getCustomEditorComponent()).saveToModel(_component);
        } else if (databindingElement != null) {
            ((DatabindingElementUI) databindingElement.getCustomEditorComponent()).resetValuesInModel(_component);
        }
    }

    @Override
    public boolean executeInsideWriteTransaction() {
        if (databindingElement != null && databindingElement.getRadioButton().isSelected()) {
            return false;
        }
        return super.executeInsideWriteTransaction();
    }

    @Override
    public boolean isExecuteInsideWriteTransactionUsed() {
        if (databindingElement != null && databindingElement.getRadioButton().isSelected()) {
            return true;
        }
        return super.isExecuteInsideWriteTransactionUsed();
    }

    public String getTextForPropertyValue() {
        return rePanel.getTextForPropertyValue();
    }

    private void saveChanges() {
        if (rePanel.wasAnyDesignComponentChanged()) {
            Map<String, DesignComponent> componentsMap = getComponentsMap();
            Map<String, DesignComponentWrapper> wrappersMap = rePanel.getWrappersMap();

            final Collection<DesignComponent> toBeDeleted = new ArrayList<DesignComponent>();
            for (final String key : wrappersMap.keySet()) {
                final DesignComponentWrapper wrapper = wrappersMap.get(key);

                if (wrapper.hasChanges()) {
                    final DesignComponent _component = componentsMap.get(key);
                    if (_component != null) {
                        _component.getDocument().getTransactionManager().writeAccess(new Runnable() {

                            public void run() {
                                if (wrapper.getComponent() != null) {
                                    // component need to be changed
                                    Map<String, PropertyValue> changes = wrapper.getChanges();
                                    for (String propertyName : changes.keySet()) {
                                        final PropertyValue propertyValue = changes.get(propertyName);
                                        _component.writeProperty(propertyName, propertyValue);
                                    }
                                } else {
                                    // component need to be deleted
                                    toBeDeleted.add(_component);
                                }
                            }
                        });
                    } else {
                        // component need to be created
                        if (wrapper.isDeleted()) {
                            // do not create
                            continue;
                        }

                        if (component != null && component.get() != null) {
                            final DesignDocument document = component.get().getDocument();
                            document.getTransactionManager().writeAccess(new Runnable() {

                                public void run() {
                                    ComponentProducer producer = DocumentSupport.getComponentProducer(document, componentTypeID.toString());
                                    if (producer == null) {
                                        throw new IllegalStateException("No producer for TypeID : " + componentTypeID.toString()); // NOI18N
                                    }
                                    DesignComponent category = MidpDocumentSupport.getCategoryComponent(document, ResourcesCategoryCD.TYPEID);
                                    ComponentProducer.Result result = AcceptSupport.accept(category, producer, null);
                                    DesignComponent createdComponent = result != null ? result.getMainComponent() : null;
                                    if (createdComponent != null) {
                                        createdComponent.writeProperty(ClassCD.PROP_INSTANCE_NAME, MidpTypes.createStringValue(key));

                                        Map<String, PropertyValue> changes = wrapper.getChanges();
                                        for (String propertyName : changes.keySet()) {
                                            final PropertyValue propertyValue = changes.get(propertyName);
                                            createdComponent.writeProperty(propertyName, propertyValue);
                                        }
                                        createdComponents.put(key, createdComponent);
                                    }
                                }
                            });
                        }
                    }
                }

                if (!toBeDeleted.isEmpty() && component != null && component.get() != null) {
                    final DesignDocument document = component.get().getDocument();
                    document.getTransactionManager().writeAccess(new Runnable() {

                        public void run() {
                            DeleteSupport.invokeDirectUserDeletion(document, toBeDeleted, false);
                        }
                    });
                }

            }
            perElement.postSaveValue(component.get());

        }
    }

    public JComponent getCustomEditorComponent() {
        return rePanel;
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

    public void updateState(PropertyValue value) {
        final DesignComponent c = component.get();
        if (databindingElement != null) {
            databindingElement.updateDesignComponent(c);
        }
        if (MidpDatabindingSupport.getDatabaindingAsText(component.get(), getPropertyNames().get(0)) != null) {
            ((DatabindingElementUI) databindingElement.getCustomEditorComponent()).updateComponent(c);
        } else if (rePanel.needsUpdate()) {
            radioButton.setSelected(!isCurrentValueAUserCodeType());
            rePanel.update(getComponentsMap(), getDecodeValue(value));
        }
    }

    @Override
    public boolean isResetToDefaultAutomatically() {
        if (component == null) {
            super.isResetToDefaultAutomatically();
        }
        return perElement.isResetToDefaultAutomatically(component.get());
    }

    @Override
    public void customEditorResetToDefaultButtonPressed() {
        if (component != null && component.get() != null) {
            perElement.preResetToDefaultValue(component.get());
        }
        super.customEditorResetToDefaultButtonPressed();
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }
}