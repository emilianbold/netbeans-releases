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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.propertyeditors.eventhandler;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.categories.DisplayablesCategoryCD;
import org.netbeans.modules.vmd.midp.components.categories.PointsCategoryCD;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.points.CallPointCD;
import org.netbeans.modules.vmd.midp.components.points.MethodPointCD;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public final class PropertyEditorEventHandler extends DesignPropertyEditor {
    
    private static final String DO_NOTHING = NbBundle.getMessage(PropertyEditorEventHandler.class, "LBL_NOTHING_ACTION"); // NOI18N
    
    private final CustomEditor customEditor;
    private DesignDocument document;
    private DesignComponent component;
    
    private PropertyEditorEventHandler() {
        Collection<PropertyEditorElementFactory> factories = Lookup.getDefault().lookup(new Lookup.Template(PropertyEditorElementFactory.class)).allInstances();
        Collection<PropertyEditorEventHandlerElement> elements = new ArrayList<PropertyEditorEventHandlerElement>(factories.size());
        for (PropertyEditorElementFactory factory : factories) {
            elements.add(factory.createElement());
        }
        
        customEditor = new CustomEditor(elements);
    }
    
    public static final PropertyEditorEventHandler createInstance() {
        return new PropertyEditorEventHandler();
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                PropertyValue value = null;
                Iterator<DesignComponent> iterator = component.getComponents().iterator();
                if (iterator.hasNext()) {
                    DesignComponent eventHandler = iterator.next();
                    value = PropertyValue.createComponentReference(eventHandler);
                }
                
                DesignComponent displayableCategory = MidpDocumentSupport.getCategoryComponent(document, DisplayablesCategoryCD.TYPEID);
                DesignComponent pointsCategory = MidpDocumentSupport.getCategoryComponent(document, PointsCategoryCD.TYPEID);
                List<DesignComponent> displayables = DocumentSupport.gatherAllComponentsOfTypeID(displayableCategory, DisplayableCD.TYPEID);
                
                customEditor.updateModels(displayables, PropertyEditorEventHandlerElement.MODEL_TYPE_DISPLAYABLES, value);
                List<DesignComponent> alerts = DocumentSupport.gatherAllComponentsOfTypeID(displayableCategory, AlertCD.TYPEID);
                
                customEditor.updateModels(alerts, PropertyEditorEventHandlerElement.MODEL_TYPE_ALERTS, value);
                List<DesignComponent> points = DocumentSupport.gatherAllComponentsOfTypeID(pointsCategory, CallPointCD.TYPEID);
                List<DesignComponent> methods = DocumentSupport.gatherAllComponentsOfTypeID(pointsCategory, MethodPointCD.TYPEID);
                List<DesignComponent> pointsAndMethods = new ArrayList<DesignComponent>(points.size() + methods.size());
                
                pointsAndMethods.addAll(points);
                pointsAndMethods.addAll(methods);
                customEditor.updateModels(pointsAndMethods, PropertyEditorEventHandlerElement.MODEL_TYPE_POINTS, value);
                
                List<DesignComponent> mobileDevices = DocumentSupport.gatherSubComponentsOfType(pointsCategory, MobileDeviceCD.TYPEID);
                customEditor.setExitMidletEnabled(mobileDevices.size() == 1);
            }
        });
        
        return customEditor;
    }
    
    public boolean canEditAsText() {
        return false;
    }
    
    public String getAsText() {
        final String[] string = new String[1];
        if (component != null) {
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    Iterator<DesignComponent> iterator = component.getComponents().iterator();
                    if (!iterator.hasNext()) {
                        string[0] = DO_NOTHING;
                    } else {
                        DesignComponent eventHandler = iterator.next();
                        InfoPresenter presenter = eventHandler.getPresenter(InfoPresenter.class);
                        if (presenter != null) {
                            string[0] = presenter.getDisplayName(InfoPresenter.NameType.PRIMARY);
                        } else {
                            throw new IllegalStateException("No infoPresenter for " + eventHandler); // NOI18N
                        }
                    }
                }
            });
        }
        return string[0];
    }
    
    public void init(DesignComponent component) {
        document = component.getDocument();
        this.component = component;
    }
    
    public boolean executeInsideWriteTransaction() {
        customEditor.createEventHandler(component);
        return false;
    }
    
    public boolean isDefaultValue() {
        return true;
    }
    
    private static class CustomEditor extends JPanel {
        private Collection<PropertyEditorEventHandlerElement> elements;
        private JRadioButton doNothingRadioButton;
        
        public CustomEditor(Collection<PropertyEditorEventHandlerElement> elements) {
            this.elements = elements;
            initComponents(elements);
        }
        
        private void initComponents(Collection<PropertyEditorEventHandlerElement> elements) {
            setLayout(new GridBagLayout());
            ButtonGroup buttonGroup = new ButtonGroup();
            GridBagConstraints constraints = new GridBagConstraints();
            int gridy = 0;
            boolean wasSelected = false;
            for (PropertyEditorEventHandlerElement element : elements) {
                JRadioButton rb = element.getRadioButton();
                if (element.isInitiallySelected()) {
                    rb.setSelected(true);
                    wasSelected = true;
                }
                buttonGroup.add(rb);
                
                constraints.insets = new Insets(12, 12, 6, 12);
                constraints.anchor = GridBagConstraints.NORTHWEST;
                constraints.gridx = 0;
                constraints.gridy = gridy++;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.fill = GridBagConstraints.BOTH;
                add(rb, constraints);
                
                Component component = element.getComponent();
                if (component != null) {
                    constraints.insets = new Insets(0, 12, 12, 12);
                    constraints.anchor = GridBagConstraints.NORTHWEST;
                    constraints.gridx = 0;
                    constraints.gridy = gridy++;
                    constraints.weightx = 1.0;
                    constraints.weighty = 1.0;
                    constraints.fill = GridBagConstraints.BOTH;
                    add(component, constraints);
                }
            }
            
            doNothingRadioButton = new JRadioButton();
            Mnemonics.setLocalizedText(doNothingRadioButton, NbBundle.getMessage(PropertyEditorEventHandler.class, "LBL_NOTHING")); // NOI18N
            doNothingRadioButton.setSelected(!wasSelected);
            buttonGroup.add(doNothingRadioButton);
            
            constraints.insets = new Insets(0, 12, 6, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = gridy++;
            constraints.weightx = 0.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(doNothingRadioButton, constraints);
        }
        
        public void updateModels(List<DesignComponent> components, int modelType, PropertyValue value) {
            for (PropertyEditorEventHandlerElement element : elements) {
                element.updateModel(components, modelType);
                element.setPropertyValue(value);
                checkDoNothing(value);
            }
        }
        
        public void setExitMidletEnabled(boolean enabled) {
            for (PropertyEditorEventHandlerElement element : elements) {
                if (element instanceof ExitMidletElement) {
                    element.setEnabled(enabled);
                    break;
                }
            }
        }
        
        private void checkDoNothing(PropertyValue value) {
            if (value == null || value.getComponent() == null) {
                doNothingRadioButton.setSelected(true);
            }
        }
        
        private void resetEventHandler(DesignComponent eventSource) {
            MidpDocumentSupport.updateEventHandlerWithNew(eventSource, null);
        }
        
        public void createEventHandler(DesignComponent eventSource) {
            if (doNothingRadioButton.isSelected()) {
                resetEventHandler(eventSource);
            } else {
                for (PropertyEditorEventHandlerElement element : elements) {
                    if (eventSource != null) {
                        element.createEventHandler(eventSource);
                    }
                }
            }
        }
    }
}
