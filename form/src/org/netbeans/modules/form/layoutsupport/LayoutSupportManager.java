/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.beans.*;
import java.util.*;

import org.openide.nodes.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.delegates.NullLayoutSupport;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * @author Tomas Pavek
 */

public final class LayoutSupportManager implements LayoutSupportContext {

    private LayoutSupportDelegate layoutDelegate;

    private RADVisualContainer metaContainer;

    private Container primaryContainer; // bean instance from metaContainer
    private Container primaryContainerDelegate; // container delegate for it

    private CodeStructure codeStructure;

    private CodeExpression containerCodeExpression;
    private CodeExpression containerDelegateCodeExpression;

    // ----------
    // initialization

    // initialization for a new container
    public void initialize(RADVisualContainer container,
                           CodeStructure codeStructure)
    {
        this.metaContainer = container;
        this.codeStructure = codeStructure;

        layoutDelegate = null;

        containerCodeExpression = metaContainer.getCodeExpression();
        containerDelegateCodeExpression = null;
    }

    // further initialization for a container restored from XML (or code)
    // initialize(...) method must be called first
    public boolean initializeFromCode() {
        // first try to find a dedicated layout delegate (for a container)
        Class layoutDelegateClass =
            LayoutSupportRegistry.getLayoutDelegateForContainer(
                                      metaContainer.getBeanClass());

        if (layoutDelegateClass == null) {
            // find a general layout delegate (for given LayoutManager)
            Iterator it = CodeStructure.getDefinedStatementsIterator(
                                          getContainerDelegateCodeExpression());
            CodeStatement[] statements =
                CodeStructure.filterStatements(
                                it, AbstractLayoutSupport.getSetLayoutMethod());

            if (statements.length > 0) { // LayoutManager from code
                CodeExpressionOrigin layoutOrigin =
                    statements[0].getStatementParameters()[0].getOrigin();
                layoutDelegateClass =
                    LayoutSupportRegistry.getLayoutDelegateForLayout(
                                              layoutOrigin.getType());

                if (layoutDelegateClass == null) {
                    // handle special case of null layout
                    if (layoutOrigin.getType() == LayoutManager.class
                        && layoutOrigin.getCreationParameters().length == 0
                        && layoutOrigin.getParentExpression() == null
                        && "null".equals(layoutOrigin.getJavaCodeString(null, null))) // NOI18N
                    {
                        layoutDelegateClass = NullLayoutSupport.class;
                    }
                }
            }
            else { // default LayoutManager
                LayoutManager defaultLM =
                    getPrimaryContainerDelegate().getLayout();
                layoutDelegateClass = defaultLM != null ?
                    LayoutSupportRegistry.getLayoutDelegateForLayout(
                                              defaultLM.getClass()) :
                    NullLayoutSupport.class;
            }

            if (layoutDelegateClass == null)
                return false;
        }

        try {
            LayoutSupportDelegate delegate = LayoutSupportRegistry
                                    .createLayoutDelegate(layoutDelegateClass);
            setLayoutDelegate(delegate, true);
            return true;
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return false;
        }
    }

    // set and initialize new layout delegate
    public void setLayoutDelegate(LayoutSupportDelegate newDelegate,
                                  boolean initFromCode)
    {
        LayoutSupportDelegate oldDelegate = layoutDelegate;
        int componentCount = oldDelegate != null ?
                             oldDelegate.getComponentCount() : 0;

        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();

        RADVisualComponent[] metacomps = null;
        LayoutConstraints[] oldConstraints = null;

        if (oldDelegate != null
            && (newDelegate != oldDelegate || !initFromCode))
        { // clean the old layout delegate
            CodeStructure.removeStatements(
                oldDelegate.getLayoutCode().getStatementsIterator());

            if (componentCount > 0) {
                metacomps = metaContainer.getSubComponents();
                oldConstraints = new LayoutConstraints[componentCount];
            }

            for (int i=0; i < componentCount; i++) {
                LayoutConstraints constr = oldDelegate.getConstraints(i);
                oldConstraints[i] = constr;
                if (constr != null)
                    metacomps[i].setLayoutConstraints(oldDelegate.getClass(),
                                                      constr);

                CodeStructure.removeStatements(
                    oldDelegate.getComponentCode(i).getStatementsIterator());
            }

            oldDelegate.removeAll();
            oldDelegate.clearContainer(cont, contDel);
        }

        layoutDelegate = newDelegate;

        if (newDelegate == null)
            return;

        newDelegate.initialize(this, initFromCode);

        if (initFromCode)
            return; // return now, do not setup primary container

        newDelegate.setLayoutToContainer(cont, contDel);

        if (componentCount == 0)
            return; // no components in container

        CodeExpression[] compExps = new CodeExpression[componentCount];
        Component[] designComps = new Component[componentCount];
        Component[] primaryComps = new Component[componentCount];
        LayoutConstraints[] newConstraints = new LayoutConstraints[componentCount];

        if (metacomps == null)
            metacomps = metaContainer.getSubComponents();

        FormDesigner designer =
            getMetaContainer().getFormModel().getFormDesigner();

        for (int i=0; i < componentCount; i++) {
            RADVisualComponent metacomp = metacomps[i];
            compExps[i] = metacomp.getCodeExpression();
            primaryComps[i] = metacomp.getComponent();
            ensureFakePeerAttached(primaryComps[i]);
            newConstraints[i] = newDelegate == null ? null :
                metacomp.getLayoutConstraints(newDelegate.getClass());

            metacomp.resetConstraintsProperties();

            Component comp = (Component) designer.getComponent(metacomp);
            designComps[i] = comp != null ? comp : metacomp.getComponent();
        }

        if (oldConstraints != null)
            newDelegate.convertConstraints(
                            oldConstraints, newConstraints, designComps);

        newDelegate.addComponents(compExps, newConstraints);

        newDelegate.addComponentsToContainer(cont, contDel, primaryComps, 0);
    }

    public LayoutSupportDelegate getLayoutDelegate() {
        return layoutDelegate;
    }

    // copy layout delegate from another container
    public void copyLayoutDelegateFrom(
                    LayoutSupportManager sourceLayoutSupport)
    {
        LayoutSupportDelegate oldDelegate = layoutDelegate;
        LayoutSupportDelegate sourceDelegate =
            sourceLayoutSupport.getLayoutDelegate();

        int componentCount = sourceDelegate.getComponentCount();

        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();

        RADVisualComponent[] metacomps = null;

        if (oldDelegate != null) { // clean the old layout delegate
            CodeStructure.removeStatements(
                oldDelegate.getLayoutCode().getStatementsIterator());

            if (componentCount > 0)
                metacomps = metaContainer.getSubComponents();

            for (int i=0; i < componentCount; i++) {
                LayoutConstraints constr = oldDelegate.getConstraints(i);
                if (constr != null)
                    metacomps[i].setLayoutConstraints(oldDelegate.getClass(),
                                                      constr);

                CodeStructure.removeStatements(
                    oldDelegate.getComponentCode(i).getStatementsIterator());
            }

            oldDelegate.removeAll();
            oldDelegate.clearContainer(cont, contDel);
        }

        CodeExpression[] compExps = new CodeExpression[componentCount];
        Component[] primaryComps = new Component[componentCount];

        if (metacomps == null)
            metacomps = metaContainer.getSubComponents();

        for (int i=0; i < componentCount; i++) {
            RADVisualComponent metacomp = metacomps[i];
            compExps[i] = metacomp.getCodeExpression();
            primaryComps[i] = metacomp.getComponent();
            ensureFakePeerAttached(primaryComps[i]);
            metacomp.resetConstraintsProperties();
        }

        LayoutSupportDelegate newDelegate =
            sourceDelegate.cloneLayout(this, compExps);
        newDelegate.setLayoutToContainer(cont, contDel);
        newDelegate.addComponentsToContainer(cont, contDel, primaryComps, 0);

        layoutDelegate = newDelegate;
    }

    // setup primary container according to current layout and constraints
    // parameters
    public void setupPrimaryContainer() {
        Container cont = getPrimaryContainer();
        Container contDel = getPrimaryContainerDelegate();

        layoutDelegate.clearContainer(cont, contDel);
        layoutDelegate.setLayoutToContainer(cont, contDel);

        RADVisualComponent[] components = metaContainer.getSubComponents();
        if (components.length > 0) {
            Component[] comps = new Component[components.length];
            for (int i=0; i < components.length; i++) {
                comps[i] = components[i].getComponent();
                ensureFakePeerAttached(comps[i]);
            }

            layoutDelegate.addComponentsToContainer(cont, contDel, comps, 0);
        }
    }

    public RADVisualContainer getMetaContainer() {
        return metaContainer;
    }

    public boolean supportsArranging() {
        return layoutDelegate instanceof LayoutSupportArranging;
    }

    // ---------
    // public API delegated to LayoutSupportDelegate

    public boolean isDedicated() {
        return layoutDelegate.isDedicated();
    }

    // node presentation
    public boolean shouldHaveNode() {
        return layoutDelegate.shouldHaveNode();
    }

    public String getDisplayName() {
        return layoutDelegate.getDisplayName();
    }

    public Image getIcon(int type) {
        return layoutDelegate.getIcon(type);
    }

    // properties and customizer
    public Node.PropertySet[] getPropertySets() {
        return layoutDelegate.getPropertySets();
    }

    public Node.Property[] getAllProperties() {
        if (layoutDelegate instanceof AbstractLayoutSupport)
            return ((AbstractLayoutSupport)layoutDelegate).getAllProperties();

        ArrayList allPropsList = new ArrayList();
        Node.PropertySet[] propertySets = layoutDelegate.getPropertySets();
        for (int i=0; i < propertySets.length; i++) {
            Node.Property[] props = propertySets[i].getProperties();
            for (int j=0; j < props.length; j++)
                allPropsList.add(props[j]);
        }

        Node.Property[] allProperties = new Node.Property[allPropsList.size()];
        allPropsList.toArray(allProperties);
        return allProperties;
    }

    public Class getCustomizerClass() {
        return layoutDelegate.getCustomizerClass();
    }

    // code meta data
    public CodeGroup getLayoutCode() {
        return layoutDelegate.getLayoutCode();
    }

    public CodeGroup getComponentCode(int index) {
        return layoutDelegate.getComponentCode(index);
    }

    public CodeGroup getComponentCode(RADVisualComponent metacomp) {
        int index = metaContainer.getIndexOf(metacomp);
        return index >= 0 && index < layoutDelegate.getComponentCount() ?
               layoutDelegate.getComponentCode(index) : null;
    }

    public int getComponentCount() {
        return layoutDelegate.getComponentCount();
    }

    // components adding/removing
    public void addComponents(RADVisualComponent[] components,
                              LayoutConstraints[] constraints)
    {
        CodeExpression[] compExps = new CodeExpression[components.length];
        Component[] comps = new Component[components.length];

        for (int i=0; i < components.length; i++) {
            RADVisualComponent metacomp = components[i];
            metacomp.resetConstraintsProperties();

            compExps[i] = metacomp.getCodeExpression();
            comps[i] = metacomp.getComponent();
            ensureFakePeerAttached(comps[i]);
        }

        int oldCount = layoutDelegate.getComponentCount();

        layoutDelegate.addComponents(compExps, constraints);

//        for (int i=0; i < components.length; i++) {
//            // store the constraints object in the meta component
//            LayoutConstraints constr =
//                layoutDelegate.getConstraints(oldCount + i);
//            if (constr != null)
//                components[i].setLayoutConstraints(layoutDelegate.getClass(),
//                                                   constr);
//        }

        layoutDelegate.addComponentsToContainer(getPrimaryContainer(),
                                                getPrimaryContainerDelegate(),
                                                comps, oldCount);
    }

    public void addComponent(RADVisualComponent metacomp,
                             LayoutConstraints constraints)
    {
        metacomp.resetConstraintsProperties();

        int oldCount = layoutDelegate.getComponentCount();

        layoutDelegate.addComponents(
                         new CodeExpression[] { metacomp.getCodeExpression() },
                         new LayoutConstraints[] { constraints });

        Component primaryComponent = metacomp.getComponent();
        ensureFakePeerAttached(primaryComponent);
        layoutDelegate.addComponentsToContainer(
                           getPrimaryContainer(),
                           getPrimaryContainerDelegate(),
                           new Component[] { primaryComponent },
                           oldCount);
    }

    public void removeComponent(RADVisualComponent metacomp, int index) {
        // first store constraints in the meta component
        LayoutConstraints constr = layoutDelegate.getConstraints(index);
        if (constr != null)
            metacomp.setLayoutConstraints(layoutDelegate.getClass(), constr);

        // remove code
        CodeStructure.removeStatements(
            layoutDelegate.getComponentCode(index).getStatementsIterator());

        // remove the component from layout
        layoutDelegate.removeComponent(index);

        // remove the component instance from the primary container instance
        layoutDelegate.removeComponentFromContainer(
                           getPrimaryContainer(),
                           getPrimaryContainerDelegate(),
                           metacomp.getComponent(),
                           index);
    }

    public void removeAll() {
        // first store constraints in meta components
        RADVisualComponent[] components = metaContainer.getSubComponents();
        for (int i=0; i < components.length; i++) {
            LayoutConstraints constr =
                layoutDelegate.getConstraints(i);
            if (constr != null)
                components[i].setLayoutConstraints(layoutDelegate.getClass(),
                                                   constr);
        }

        // remove code of all components
        for (int i=0, n=layoutDelegate.getComponentCount(); i < n; i++)
            CodeStructure.removeStatements(
                layoutDelegate.getComponentCode(i).getStatementsIterator());

        // remove components from layout
        layoutDelegate.removeAll();

        // clear the primary container instance
        layoutDelegate.clearContainer(getPrimaryContainer(),
                                      getPrimaryContainerDelegate());
    }

    public boolean isLayoutChanged() {
        Container defaultContainer = (Container)
                BeanSupport.getDefaultInstance(metaContainer.getBeanClass());
        Container defaultContDelegate =
                metaContainer.getContainerDelegate(defaultContainer);

        return layoutDelegate.isLayoutChanged(defaultContainer,
                                              defaultContDelegate);
    }

    // managing constraints
    public LayoutConstraints getConstraints(int index) {
        return layoutDelegate.getConstraints(index);
    }

    public LayoutConstraints getConstraints(RADVisualComponent metacomp) {
        int index = metaContainer.getIndexOf(metacomp);
        return index >= 0 && index < layoutDelegate.getComponentCount() ?
               layoutDelegate.getConstraints(index) : null;
    }

    public static LayoutConstraints storeConstraints(
                                        RADVisualComponent metacomp)
    {
        RADVisualContainer parent = metacomp.getParentContainer();
        if (parent == null)
            return null;

        LayoutSupportManager layoutSupport = parent.getLayoutSupport();
        LayoutConstraints constr = layoutSupport.getConstraints(metacomp);
        if (constr != null)
            metacomp.setLayoutConstraints(
                         layoutSupport.getLayoutDelegate().getClass(),
                         constr);
        return constr;
    }

    public LayoutConstraints getStoredConstraints(RADVisualComponent metacomp) {
        return metacomp.getLayoutConstraints(layoutDelegate.getClass());
    }

    // managing live components
    public void setLayoutToContainer(Container container,
                                     Container containerDelegate)
    {
        layoutDelegate.setLayoutToContainer(container, containerDelegate);
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        layoutDelegate.addComponentsToContainer(container, containerDelegate,
                                                components, index);
    }

    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component,
                                                int index)
    {
        return layoutDelegate.removeComponentFromContainer(
                            container, containerDelegate, component, index);
    }

    public boolean clearContainer(Container container,
                                  Container containerDelegate)
    {
        return layoutDelegate.clearContainer(container, containerDelegate);
    }

    // drag and drop support
    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        return layoutDelegate.getNewConstraints(container, containerDelegate,
                                                component, index,
                                                posInCont, posInComp);
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        return layoutDelegate.getNewIndex(container, containerDelegate,
                                          component, index,
                                          posInCont, posInComp);
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        return layoutDelegate.paintDragFeedback(container, containerDelegate,
                                                component,
                                                newConstraints, newIndex,
                                                g);
    }

    // resizing support
    public int getResizableDirections(Component component, int index) {
        return layoutDelegate.getResizableDirections(component, index);
    }

    public LayoutConstraints getResizedConstraints(Component component,
                                                   int index,
                                                   Insets sizeChanges)
    {
        return layoutDelegate.getResizedConstraints(component, index,
                                                    sizeChanges);
    }

    // arranging support - use only if supportsArranging() returns true
    public void processMouseClick(Point p, Container cont) {
        ((LayoutSupportArranging)layoutDelegate).processMouseClick(p, cont);
    }

    // arranging support - use only if supportsArranging() returns true
    public void selectComponent(int index) { //RADVisualComponent metacomp
        ((LayoutSupportArranging)layoutDelegate).selectComponent(index);
    }

    // arranging support - use only if supportsArranging() returns true
    public void arrangeContainer(Container container,
                                 Container containerDelegate)
    {
        ((LayoutSupportArranging)layoutDelegate)
            .arrangeContainer(container, containerDelegate);
    }

    // -----------
    // API for layout delegates (LayoutSupportContext implementation)

    public CodeStructure getCodeStructure() {
        return codeStructure;
    }

    public CodeExpression getContainerCodeExpression() {
        return containerCodeExpression;
    }

    public CodeExpression getContainerDelegateCodeExpression() {
        if (containerDelegateCodeExpression == null) {
            java.lang.reflect.Method delegateGetter =
                metaContainer.getContainerDelegateMethod();

            if (delegateGetter != null) { // there should be a container delegate
                Iterator it = CodeStructure.getDefinedExpressionsIterator(
                                                  containerCodeExpression);
                CodeExpression[] expressions = CodeStructure.filterExpressions(
                                                            it, delegateGetter);
                if (expressions.length > 0) {
                    // the expresion for the container delegate already exists
                    containerDelegateCodeExpression = expressions[0];
                }
                else { // create a new expresion for the container delegate
                    CodeExpressionOrigin origin = CodeStructure.createOrigin(
                                                    containerCodeExpression,
                                                    delegateGetter,
                                                    null);
                    containerDelegateCodeExpression =
                        codeStructure.createExpression(origin);
                }
            }
            else // no special container delegate
                containerDelegateCodeExpression = containerCodeExpression;
        }

        return containerDelegateCodeExpression;
    }

    // return container instance of meta container
    public Container getPrimaryContainer() {
        return (Container) metaContainer.getBeanInstance();
    }

    // return container delegate of container instance of meta container
    public Container getPrimaryContainerDelegate() {
        Container defCont = (Container) metaContainer.getBeanInstance();
        if (primaryContainerDelegate == null || primaryContainer != defCont) {
            primaryContainer = defCont;
            primaryContainerDelegate =
                metaContainer.getContainerDelegate(defCont);
        }
        return primaryContainerDelegate;
    }

    // return component instance of meta component
    public Component getPrimaryComponent(int index) {
        return metaContainer.getSubComponent(index).getComponent();
    }

    public void containerLayoutChanged(PropertyChangeEvent evt) {
        // [the firing method should be changed in FormModel...]
        metaContainer.getFormModel().fireContainerLayoutChanged(metaContainer,
                                                                null, null);

        LayoutNode node = metaContainer.getLayoutNodeReference();
        if (node == null)
            return;

        // propagate the change to node
        if (evt.getPropertyName() != null)
            node.fireLayoutPropertiesChange();
        else
            node.fireLayoutPropertySetsChange();
    }

    public void componentLayoutChanged(int index, PropertyChangeEvent evt) {
        RADVisualComponent metacomp = metaContainer.getSubComponent(index);

        if (evt.getPropertyName() != null) {
            metaContainer.getFormModel().fireComponentLayoutChanged(
                                         metacomp,
                                         evt.getPropertyName(),
                                         evt.getOldValue(), evt.getNewValue());

            if (metacomp.getNodeReference() != null) // propagate the change to node
                metacomp.getNodeReference().firePropertyChangeHelper(
                                          evt.getPropertyName(),
                                          evt.getOldValue(), evt.getNewValue());
        }
        else {
            if (metacomp.getNodeReference() != null) // propagate the change to node
                metacomp.getNodeReference().fireComponentPropertySetsChange();
        }
    }

    // ---------

    private static void ensureFakePeerAttached(Component comp) {
        boolean attached = FakePeerSupport.attachFakePeer(comp);
        if (attached && comp instanceof Container)
            FakePeerSupport.attachFakePeerRecursively((Container)comp);
    }
}
