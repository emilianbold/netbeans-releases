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
            PropertiesNodesManager.this.document = document;
            document.getListenerManager().addDesignListener(this, new DesignEventFilter().setGlobal(true));
            ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
            ActiveViewSupport.getDefault().addActiveViewListener(this);
        } else if (this.document != null) {
            ActiveDocumentSupport.getDefault().removeActiveDocumentListener(this);
            ActiveViewSupport.getDefault().removeActiveViewListener(this);
            this.document.getListenerManager().removeDesignListener(this);
            this.document.getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    DesignComponent root = PropertiesNodesManager.this.document.getRootComponent();
                    for (DesignComponent c : root.getComponents()) {
                        cleanUpAll(c);
                    }
                }
            });

            this.document = null;
            view = null;
            propertySupportMap = null;
            sheetMap = null;
            nodesMap = null;
        }
    }

    private void cleanUpAll(DesignComponent parent) {
        for (final DesignComponent c : parent.getComponents()) {
            Collection<? extends PropertiesPresenter> presenters = c.getPresenters(PropertiesPresenter.class);
            for (PropertiesPresenter p : presenters) {
                for (final DesignPropertyDescriptor pd : p.getDesignPropertyDescriptors()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            pd.getPropertyEditor().cleanUp(c);
                            System.out.println(c.toString());
                        }
                    });
                }
                
            }
            cleanUpAll(c);
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
