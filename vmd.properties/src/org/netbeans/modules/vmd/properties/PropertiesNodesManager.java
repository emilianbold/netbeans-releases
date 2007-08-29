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

package org.netbeans.modules.vmd.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DesignListener;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.GroupPropertyEditor;
import org.netbeans.modules.vmd.api.properties.PropertiesPresenter;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Sheet;
import org.openide.util.WeakSet;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Karol Harezlak
 */
//TODO rename it to PropeertiesViewNodesManager or PropeertiesViewNodesController
public final class PropertiesNodesManager implements DesignDocumentAwareness,
                                                     DesignListener,
                                                     ActiveDocumentSupport.Listener,
                                                     ActiveViewSupport.Listener {

    private static final WeakHashMap<DataEditorView, PropertiesNodesManager> INSTANCES = new WeakHashMap<DataEditorView, PropertiesNodesManager>();
    private static Comparator<DesignPropertyDescriptor> compareByDisplayName = new Comparator<DesignPropertyDescriptor>() {
        public int compare(DesignPropertyDescriptor descriptor1, DesignPropertyDescriptor descriptor2) {
            return descriptor1.getPropertyDisplayName().compareTo(descriptor2.getPropertyDisplayName());
        }
    };

    public static synchronized PropertiesNodesManager getInstance(DataEditorView view) {
        if (INSTANCES.get(view) == null) {
            PropertiesNodesManager manager = new PropertiesNodesManager(view);
            INSTANCES.put(view, manager);
        }
        return INSTANCES.get(view);
    }

    private WeakHashMap<DataEditorView, InstanceContent> icMap;
    private Collection<InstanceContent> ics;
    private WeakHashMap<InstanceContent, WeakSet<Node>> nodesToRemoveMap;
    private DataEditorView view;
    private DesignDocument document;
    private WeakHashMap<DesignComponent, WeakSet<DefaultPropertySupport>> propertySupportMap;
    private WeakHashMap<DesignComponent, Sheet> sheetMap;
    private WeakHashMap<DesignComponent, PropertiesNode> nodesMap;

    private PropertiesNodesManager(DataEditorView view) {
        view.getContext().addDesignDocumentAwareness(this);
        nodesToRemoveMap = new WeakHashMap<InstanceContent, WeakSet<Node>>();
        this.view = view;
        icMap = new WeakHashMap<DataEditorView, InstanceContent>();
        ics = new HashSet<InstanceContent>();
        propertySupportMap = new WeakHashMap<DesignComponent, WeakSet<DefaultPropertySupport>>();
        sheetMap = new WeakHashMap<DesignComponent, Sheet>();
        nodesMap = new WeakHashMap<DesignComponent, PropertiesNode>();
    }

    public void setDesignDocument(DesignDocument document) {
        if (document != null) {
            this.document = document;
            document.getListenerManager().addDesignListener(this, new DesignEventFilter().setGlobal(true));
            ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
            ActiveViewSupport.getDefault().addActiveViewListener(this);
        } else if (this.document != null) {
            ActiveDocumentSupport.getDefault().removeActiveDocumentListener(this);
            ActiveViewSupport.getDefault().removeActiveViewListener(this);
            this.document.getListenerManager().removeDesignListener(this);
            this.document = null;
            view = null;
            propertySupportMap = null;
            sheetMap = null;
            nodesMap = null;
        }
    }

    public void designChanged(DesignEvent event) {
        final Collection<DesignComponent> selectedComponents = new WeakSet<DesignComponent>(document.getSelectedComponents());
        if (event.isSelectionChanged()) {
            repaintPropertiesWindow(selectedComponents);
        }
        if (event.isStructureChanged()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    PropertiesNodesManager.this.updatePropertyEditorsValues(selectedComponents);
                    PropertiesNodesManager.this.updateSheet(selectedComponents);
                }
            });
        }
    }

    public void activeDocumentChanged(DesignDocument deactivatedDocument, DesignDocument activatedDocument) {
        if (document == null) {
            return;
        }
        if (activatedDocument == null) {
            document.getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    repaintPropertiesWindow(Collections.EMPTY_LIST);
                }
            });
        }
        if (document != activatedDocument) {
            return;
        }
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                repaintPropertiesWindow(document.getSelectedComponents());
            }
        });
    }

    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }

    public void activeViewChanged(final DataEditorView deactivatedView, final DataEditorView activatedView) {
        if (document == null) {
            return;
        }
        if (activatedView != null && view.getContext() == activatedView.getContext() && activatedView.getKind() == DataEditorView.Kind.MODEL) {
            document.getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    repaintPropertiesWindow(document.getSelectedComponents());
                }
            });
        } else if (deactivatedView != null && deactivatedView.getKind() == DataEditorView.Kind.MODEL) {
            document.getTransactionManager().readAccess(new Runnable() {

                @SuppressWarnings(value = "unchecked")
                public void run() {
                    repaintPropertiesWindow(Collections.EMPTY_SET);
                }
            });
        }
    }

    public void add(InstanceContent ic) {
        assert (ic != null);
        this.ics.add(ic);
    }

    public void add(DataEditorView view, InstanceContent ic) {
        assert (ic != null);
        icMap.put(view, ic);
    }

    private void changeLookup(Collection<DesignComponent> components) {
        if (components == null) {
            return;
        }
        Collection<InstanceContent> tempIcs = new HashSet<InstanceContent>();
        tempIcs.addAll(ics);
        if (icMap.get(view) != null) {
            tempIcs.add(icMap.get(view));
        }
        for (InstanceContent ic : tempIcs) {
            WeakSet<Node> nodesToRemove = nodesToRemoveMap.get(ic);
            if (nodesToRemove == null) {
                nodesToRemove = new WeakSet<Node>();
                nodesToRemoveMap.put(ic, nodesToRemove);
            }
            ic.set(Collections.EMPTY_SET, new PropertiesNodeConverter());
            nodesToRemove.clear();
        }
        if (components.isEmpty()) {
            for (InstanceContent ic : tempIcs) {
                ic.set(Collections.singleton(Node.EMPTY), new PropertiesNodeConverter());
            }
        }
        for (InstanceContent ic : tempIcs) {
            Set<Node> nodesToRemove = nodesToRemoveMap.get(ic);
            for (DesignComponent component : components) {
                PropertiesNode node = nodesMap.get(component);
                if (node == null) {
                    node = new PropertiesNode(view, component);
                    nodesMap.put(component, node);
                }
                nodesToRemove.add(node);
            }
            if (nodesToRemove != null && !nodesToRemove.isEmpty()) {
                ic.set(nodesToRemove, new PropertiesNodeConverter());
            }
        }
    }

    public synchronized Sheet getSheet(DesignComponent component) {
        assert (component != null);
        if (sheetMap == null) {
            return null;
        }
        if (sheetMap.get(component) == null) {
            sheetMap.put(component, createSheet(component));
        }
        return sheetMap.get(component);
    }

    public Sheet createSheet(final DesignComponent component) {
        final Sheet sheet = new Sheet();
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                List<DesignPropertyDescriptor> designerPropertyDescriptors;
                List<String> categories;
                designerPropertyDescriptors = new ArrayList<DesignPropertyDescriptor>();
                categories = new ArrayList<String>();
                for (PropertiesPresenter propertiesPresenter : component.getPresenters(PropertiesPresenter.class)) {
                    designerPropertyDescriptors.addAll(propertiesPresenter.getDesignPropertyDescriptors());
                    categories.addAll(propertiesPresenter.getPropertiesCategories());
                }
                if (designerPropertyDescriptors != null) {
                    Collections.sort(designerPropertyDescriptors, compareByDisplayName);
                }
                createCategoriesSet(sheet, categories);
                for (DesignPropertyDescriptor designerPropertyDescriptor : designerPropertyDescriptors) {
                    DefaultPropertySupport property;
                    DesignPropertyEditor propertyEditor = designerPropertyDescriptor.getPropertyEditor();

                    if (propertyEditor instanceof GroupPropertyEditor && designerPropertyDescriptor.getPropertyNames().size() == 0) {
                        throw new IllegalStateException("To use AdvancedPropertyEditorSupport you need to specific at least one propertyName"); //NOI18N
                    }
                    if (propertyEditor instanceof GroupPropertyEditor) {
                        property = new AdvancedPropertySupport(designerPropertyDescriptor, designerPropertyDescriptor.getPropertyEditorType());
                        addPropertySupport(component, property);
                    } else if (designerPropertyDescriptor.getPropertyNames().size() <= 1) {
                        property = new PrimitivePropertySupport(designerPropertyDescriptor, designerPropertyDescriptor.getPropertyEditorType());
                        addPropertySupport(component, property);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    if (propertyEditor != null && propertyEditor.canEditAsText() != null) {
                        property.setValue("canEditAsText", propertyEditor.canEditAsText()); //NOI18N
                    }
                    property.setValue("changeImmediate", false); // NOI18
                    sheet.get(designerPropertyDescriptor.getPropertyCategory()).put(property);
                }
            }
        });
        return sheet;
    }

    public void updateSheet(Collection<DesignComponent> components) {
        if (components == null || components.isEmpty()) {
            return;
        }
        for (DesignComponent component : components) {
            Sheet sheet = sheetMap.get(component);
            if (sheet == null) {
                continue;
            }
            for (Node.PropertySet set : sheet.toArray()) {
                for (Property property : set.getProperties()) {
                    if (!(property instanceof DefaultPropertySupport)) {
                        continue;
                    }
                    ((DefaultPropertySupport) property).update();
                }
            }
        }
    }

    private void addPropertySupport(DesignComponent component, DefaultPropertySupport propertySupport) {
        WeakSet<DefaultPropertySupport> propertySupports = propertySupportMap.get(component);
        if (propertySupports == null) {
            propertySupports = new WeakSet<DefaultPropertySupport>();
            propertySupportMap.put(component, propertySupports);
        }
        propertySupports.add(propertySupport);
    }

    private void updatePropertyEditorsValues(Collection<DesignComponent> components) {
        if (components == null) {
            return;
        }
        Collection<InstanceContent> tempIcs = new HashSet<InstanceContent>();
        tempIcs.addAll(ics);
        if (icMap.get(view) != null) {
            tempIcs.add(icMap.get(view));
        }
        for (InstanceContent ic : tempIcs) {
            WeakSet<Node> nodesToRemove = nodesToRemoveMap.get(ic);
            if (nodesToRemove == null) {
                continue;
            }
            for (Node node : nodesToRemove) {
                PropertiesNode pn = (PropertiesNode) node;
                pn.updateNode(getSheet(pn.getComponent()));
            }
        }
    }

    private void repaintPropertiesWindow(final Collection<DesignComponent> selectedComponents) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (view != null) {
                    PropertiesNodesManager.this.changeLookup(selectedComponents);
                    PropertiesNodesManager.this.updateSheet(selectedComponents);
                }
            }
        });
    }

    private synchronized void createCategoriesSet(Sheet sheet, List<String> categories) {
        for (String propertyCategory : categories) {
            sheet.put(createPropertiesSet(propertyCategory));
        }
    }

    private synchronized Sheet.Set createPropertiesSet(String categoryName) {
        Sheet.Set setSheet = new Sheet.Set();
        setSheet.setName(categoryName);
        setSheet.setDisplayName(categoryName);
        return setSheet;
    }

    private class PropertiesNodeConverter implements InstanceContent.Convertor {

        public Object convert(Object object) {
            return object;
        }

        public Class type(Object object) {
            return object.getClass();
        }

        public String id(Object object) {
            return object.toString();
        }

        public String displayName(Object object) {
            return object.toString();
        }
    }
}
