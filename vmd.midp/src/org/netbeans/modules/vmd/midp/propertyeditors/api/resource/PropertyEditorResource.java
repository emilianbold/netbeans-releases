/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.propertyeditors.api.resource;

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
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
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

    private PropertyEditorResource(PropertyEditorResourceElement perElement, String newComponentAsText, String noneComponentAsText, String userCodeLabel) {
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

        createdComponents = new HashMap<String, DesignComponent>();

        // TODO lazy init
        radioButton = new JRadioButton();
        rePanel = new ResourceEditorPanel(perElement, noneComponentAsText, radioButton);
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorResource.class, "LBL_RB_RESOURCE")); // NOI18N

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static final PropertyEditorResource createInstance(PropertyEditorResourceElement perElement,
            String newComponentAsText, String noneComponentAsText, String userCodeLabel) {
        return new PropertyEditorResource(perElement, newComponentAsText, noneComponentAsText, userCodeLabel);
    }

    public static final DesignPropertyEditor createFontPropertyEditor() {
        return new PropertyEditorResource(new FontEditorElement(),
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_FONTRESOURCEPE_NEW"), // NOI18N
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_FONTRESOURCEPE_NONE"), // NOI18N
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_FONTRESOURCEPE_UCLABEL")); //NOI18N
    }

    public static final DesignPropertyEditor createTickerPropertyEditor() {
        return new PropertyEditorResource(new TickerEditorElement(),
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_TICKERRESOURCEPE_NEW"), // NOI18N
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_TICKERRESOURCEPE_NONE"), //NOI18N
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_TICKERRESOURCEPE_UCLABEL")); //NOI18N
    }

    public static final DesignPropertyEditor createImagePropertyEditor() {
        return new PropertyEditorResource(new ImageEditorElement(),
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_NEW"), // NOI18N
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_NONE"), //NOI18N
                NbBundle.getMessage(PropertyEditorResource.class, "LBL_IMAGERESOURCEPE_UCLABEL")); //NOI18N
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
            super.setValue(PropertyValue.createComponentReference(componentsMap.get(text)));
        } else if (text.equals(noneComponentAsText)) {
            super.setValue(NULL_VALUE);
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
                        PropertyEditorResource.super.setValue(PropertyValue.createComponentReference(createdComponent));
                    }
                }
            });
        } else {
            Map<String, DesignComponentWrapper> wrappersMap = rePanel.getWrappersMap();
            for (String key : wrappersMap.keySet()) {
                if (key.equals(text)) {
                    DesignComponent createdComponent = createdComponents.get(text);
                    super.setValue(PropertyValue.createComponentReference(createdComponent));
                    createdComponents.clear();
                    break;
                }
            }
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
    public String[] getTags() {
        if (isCurrentValueAUserCodeType()) {
            return null;
        }

        Set<String> components = getComponentsMap().keySet();
        List<String> tags = new ArrayList<String>(components.size() + 2);
        tags.add(noneComponentAsText);
        tags.addAll(components);
        tags.add(newComponentAsText);
        return tags.toArray(new String[tags.size()]);
    }

    @Override
    public Boolean canEditAsText() {
        if (isCurrentValueAUserCodeType()) {
            return super.canEditAsText();
        }
        return null;
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (getRadioButton().isSelected()) {
            saveChanges();
            saveValue(getTextForPropertyValue());
        }
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
        if (rePanel.needsUpdate()) {
            radioButton.setSelected(!isCurrentValueAUserCodeType());
            rePanel.update(getComponentsMap(), getDecodeValue(value));
        }
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }
}
