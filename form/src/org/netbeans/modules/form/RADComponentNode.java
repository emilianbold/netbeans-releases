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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.datatransfer.*;
import java.text.MessageFormat;
import java.util.*;
import java.beans.*;

import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.explorer.propertysheet.editors.NodeCustomizer;

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.layoutsupport.*;


public class RADComponentNode extends FormNode
    implements RADComponentCookie//, FormCookie
{
   private final static MessageFormat nodeNameFormat =
        new MessageFormat(
            FormEditor.getFormBundle().getString("FMT_ComponentNodeName")); // NOI18N
    private final static MessageFormat nodeNoNameFormat =
        new MessageFormat(
            FormEditor.getFormBundle().getString("FMT_UnnamedComponentNodeName")); // NOI18N

    private RADComponent component;
    private RADComponentInstance radComponentInstance;


    public RADComponentNode(RADComponent component) {
        this(component instanceof ComponentContainer ?
                new RADChildren((ComponentContainer)component) : Children.LEAF,
             component);
    }

    public RADComponentNode(Children children, RADComponent component) {
        super(children, component.getFormModel());
        this.component = component;
        radComponentInstance = new RADComponentInstance(component);
        component.setNodeReference(this);
//        getCookieSet().add(this);
        if (component instanceof ComponentContainer)
            getCookieSet().add(new ComponentsIndex());
        updateName();
    }

    void updateName() {
        String compClassName = Utilities.getShortClassName(
                                             component.getBeanClass());
        if (component == component.getFormModel().getTopRADComponent())
            setDisplayName(nodeNoNameFormat.format(
                new Object[] { compClassName }));
        else
            setDisplayName(nodeNameFormat.format(
                new Object[] { getName(), compClassName }));
    }

    public void fireComponentPropertiesChange() {
        firePropertyChange(null, null, null);
    }

    public void fireComponentPropertySetsChange() {
        firePropertySetsChange(null, null);
    }

    /** Provides access for firing property changes */
    public void firePropertyChangeHelper(String name,
                                         Object oldValue, Object newValue) {
        super.firePropertyChange(name, oldValue, newValue);
    }

    public Image getIcon(int iconType) {
        // try to get a special icon
        Image icon = BeanSupport.getBeanIcon(component.getBeanClass(), iconType);
        if (icon != null) return icon;

        // get icon from BeanInfo
        java.beans.BeanInfo bi = component.getBeanInfo();
        if (bi != null) {
            icon = bi.getIcon(iconType);
            if (icon != null) return icon;
        }

        // use default icon
        return super.getIcon(iconType);
    }

    public Image getOpenedIcon(int iconType) {
        return getIcon(iconType);
    }

    public HelpCtx getHelpCtx() {
        HelpCtx help = InstanceSupport.findHelp(new InstanceCookie() {
            public Object instanceCreate() {
                return component.getBeanInstance();
            }
            public String instanceName() {
                return component.getName();
            }
            public Class instanceClass() {
                return component.getBeanClass();
            }
        });
        if (help != null)
            return help;
        else
            return new HelpCtx("gui.component-inspector"); // NOI18N
    }

    public Node.PropertySet[] getPropertySets() {
        return component.getProperties();
    }

    /* List new types that can be created in this node.
     * @return new types
     */
    public NewType[] getNewTypes() {
        return component.getNewTypes();
    }

    /** Get the default action for this node.
     * This action can but need not be one from the list returned
     * from {@link #getActions}. If so, the popup menu returned from {@link #getContextMenu}
     * is encouraged to highlight the action.
     *
     * @return default action, or <code>null</code> if there should be none
     */
    public SystemAction getDefaultAction() {
        if (component instanceof RADVisualContainer)
            return SystemAction.get(EditContainerAction.class);
        if (component.getEventHandlers().getDefaultEvent() != null)
            return SystemAction.get(DefaultRADAction.class);

        return null;
    }

    /** Lazily initialize set of node's actions(overridable).
     * The default implementation returns <code>null</code>.
     * <p><em>Warning:</em> do not call {@link #getActions} within this method.
     * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
     * @return array of actions for this node, or <code>null</code> to use the default node actions
     */
    protected SystemAction[] createActions() {
        ArrayList actions = new ArrayList(20);

        if (component.isReadOnly()) {
            if (component.getEventHandlers().getHandlersCount() > 0) {
                actions.add(SystemAction.get(EventsAction.class));
                actions.add(null);
            }
            actions.add(SystemAction.get(CopyAction.class));
        }
        else {
            if (component instanceof RADVisualContainer) {
                if (!((RADVisualContainer)component).getLayoutSupport().isDedicated()) {
                    actions.add(SystemAction.get(SelectLayoutAction.class));
                    actions.add(SystemAction.get(CustomizeLayoutAction.class));
                    actions.add(null);
                }

                actions.add(SystemAction.get(EditContainerAction.class));
                RADComponent topComp =
                    component.getFormModel().getTopRADComponent();
                if (topComp != null && component != topComp)
                    actions.add(SystemAction.get(EditFormAction.class));
                actions.add(null);
            }

            actions.add(SystemAction.get(EventsAction.class));
            actions.add(null);

            if (component instanceof ComponentContainer) {
                if (component != component.getFormModel().getTopRADComponent())
                    actions.add(SystemAction.get(CutAction.class));
                actions.add(SystemAction.get(CopyAction.class));
                actions.add(SystemAction.get(PasteAction.class));
                actions.add(null);
                if (component != component.getFormModel().getTopRADComponent()) {
                    actions.add(SystemAction.get(RenameAction.class));
                    actions.add(SystemAction.get(DeleteAction.class));
                    actions.add(null);
                }
                actions.add(SystemAction.get(ReorderAction.class));
            } else {
                actions.add(SystemAction.get(InPlaceEditAction.class));
                actions.add(null);
                actions.add(SystemAction.get(CutAction.class));
                actions.add(SystemAction.get(CopyAction.class));
                actions.add(null);
                actions.add(SystemAction.get(RenameAction.class));
                actions.add(SystemAction.get(DeleteAction.class));
                actions.add(null);
            }

            if (component != component.getFormModel().getTopRADComponent()) {
                actions.add(SystemAction.get(MoveUpAction.class));
                actions.add(SystemAction.get(MoveDownAction.class));
            }

            if (getNewTypes().length != 0) {
                actions.add(SystemAction.get(NewAction.class));
            }
        }

        actions.add(null);

        SystemAction[] superActions = super.createActions();
        for (int i=0; i < superActions.length; i++)
            actions.add(superActions[i]);

        SystemAction[] array = new SystemAction[actions.size()];
        actions.toArray(array);
        return array;
    }

    /** Set the system name. Fires a property change event.
     * Also may change the display name according to {@link #displayFormat}.
     *
     * @param s the new name
     */
    public String getName() {
        return component.getName();
    }

    /** Set the system name. Fires a property change event.
     * Also may change the display name according to {@link #displayFormat}.
     *
     * @param s the new name
     */
    public void setName(String s) {
        component.setName(s);
    }

    /** Can this node be renamed?
     * @return <code>false</code>
     */
    public boolean canRename() {
        return !component.isReadOnly()
               && component != component.getFormModel().getTopRADComponent();
    }

    /** Can this node be destroyed?
     * @return <CODE>false</CODE>
     */
    public boolean canDestroy() {
        return !component.isReadOnly()
               && component != component.getFormModel().getTopRADComponent();
    }

    /** Remove the node from its parent and deletes it.
     * The default
     * implementation obtains write access to
     * the {@link Children#MUTEX children's lock}, and removes
     * the node from its parent(if any). Also fires a property change.
     * <P>
     * This may be overridden by subclasses to do any additional
     * cleanup.
     *
     * @exception IOException if something fails
     */
    public void destroy() throws java.io.IOException {
        // turn on notifying the user about deleting event handlers
        FormEventHandlers.setNotifyUser(true);
        component.getFormModel().removeComponent(component);
        FormEventHandlers.restorePreviousNotifySettings();

        super.destroy();
    }

    /** Get a cookie from the node.
     * Uses the cookie set as determined by {@link #getCookieSet}.
     *
     * @param type the representation class
     * @return the cookie or <code>null</code>
     */
    public Node.Cookie getCookie(Class type) {
        if (InstanceCookie.class.equals(type))
            return radComponentInstance;

        return super.getCookie(type);
    }

    /** Test whether there is a customizer for this node. If true,
     * the customizer can be obtained via {@link #getCustomizer}.
     *
     * @return <CODE>true</CODE> if there is a customizer
     */
    public boolean hasCustomizer() {
        return !component.isReadOnly()
               && component.getBeanInfo().getBeanDescriptor()
                                                 .getCustomizerClass() != null;
    }

    /** Get the customizer component.
     * @return the component, or <CODE>null</CODE> if there is no customizer
     */
    public java.awt.Component getCustomizer() {
        Class customizerClass =
            component.getBeanInfo().getBeanDescriptor().getCustomizerClass();
        if (customizerClass == null) return null;

        Object customizerObject;
        try {
            customizerObject = customizerClass.newInstance();
        }
        catch (InstantiationException e1) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e1.printStackTrace();
            return null;
        }
        catch (IllegalAccessException e2) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e2.printStackTrace();
            return null;
        }

        if (!(customizerObject instanceof Component) ||
            !(customizerObject instanceof Customizer)) return null;

        if (customizerObject instanceof FormAwareEditor)
            ((FormAwareEditor)customizerObject)
                .setFormModel(component.getFormModel());

        if (customizerObject instanceof NodeCustomizer)
            ((NodeCustomizer)customizerObject)
                .attach(component.getNodeReference());

        Customizer customizer = (Customizer) customizerObject;

        customizer.setObject(component.getBeanInstance());

        customizer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                RADProperty[] properties;
                if (evt.getPropertyName() != null) {
                    RADProperty changedProperty = component.getPropertyByName(
                                                         evt.getPropertyName());
                    if (changedProperty != null)
                        properties = new RADProperty[] { changedProperty };
                    else return;
                }
                else properties = component.getAllBeanProperties();

                for (int i=0; i < properties.length; i++) {
                    RADProperty prop = properties[i];
                    if (FormUtils.isIgnoredProperty(component.getBeanClass(),
                                                    prop.getName()))
                        continue;

                    try {
                        prop.reinstateProperty();
                        if (prop.isChanged())
                            prop.propertyValueChanged(evt.getOldValue(),
                                                      evt.getNewValue());
                    }
                    catch (Exception ex) { // unlikely to happen
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                            ex.printStackTrace();
                    }
                }
            }
        });

        return (Component) customizerObject;
    }

    // -----------------------------------------------------------------------------------------
    // Clipboard operations

    /** Test whether this node can be copied.
     * The default implementation returns <code>true</code>.
     * @return <code>true</code> if it can
     */
    public boolean canCopy() {
        return true;
    }

    /** Test whether this node can be cut.
     * The default implementation assumes it can if this node is writeable.
     * @return <code>true</code> if it can
     */
    public boolean canCut() {
        return !component.isReadOnly()
               && component != component.getFormModel().getTopRADComponent();
    }

    /** Copy this node to the clipboard.
     *
     * @return The transferable for RACComponentNode
     * @throws IOException if it could not copy
     */
    public Transferable clipboardCopy() throws java.io.IOException {
        return new CopySupport.RADTransferable(
                                 CopySupport.COMPONENT_COPY_FLAVOR, component);
    }

    /** Store original name of component and subcomponents. */
/*    private void storeNames(RADComponent comp) {
        comp.storeName();
        if (comp instanceof ComponentContainer) {
            RADComponent comps[] =((ComponentContainer) comp).getSubBeans();
            for (int i=0, n=comps.length; i<n; i++) {
                storeNames(comps[i]);
            }
        }
    } */

    /** Cut this node to the clipboard.
     *
     * @return {@link Transferable} with one flavor, {@link RAD_COMPONENT_COPY_FLAVOR }
     * @throws IOException if it could not cut
     */
    public Transferable clipboardCut() throws java.io.IOException {
        return new CopySupport.RADTransferable(
                                 CopySupport.COMPONENT_CUT_FLAVOR, component);
    }

    /** Accumulate the paste types that this node can handle
     * for a given transferable.
     * <P>
     * The default implementation simply tests whether the transferable supports
     * {@link NodeTransfer#nodePasteFlavor}, and if so, it obtains the paste types
     * from the {@link NodeTransfer.Paste transfer data} and inserts them into the set.
     *
     * @param t a transferable containing clipboard data
     * @param s a list of {@link PasteType}s that will have added to it all types
     *    valid for this node
     */
    protected void createPasteTypes(Transferable t, java.util.List s) {
        if (component.isReadOnly())
            return;

        boolean copy = t.isDataFlavorSupported(CopySupport.COMPONENT_COPY_FLAVOR);
        boolean cut = t.isDataFlavorSupported(CopySupport.COMPONENT_CUT_FLAVOR);

        if (copy || cut) { // copy or cut some RADComponent
            if (!(component instanceof ComponentContainer))
                return; // component can be pasted only to a container

            RADComponent transComp = null;
            try {
                transComp = (RADComponent) t.getTransferData(
                                                t.getTransferDataFlavors()[0]);
            }
            catch (UnsupportedFlavorException e) {} // should not happen
            catch (java.io.IOException e) {} // should not happen

            if (transComp == null)
                return;

            if (transComp instanceof RADMenuItemComponent) {
                // pasting menu component, check if it is possible
                if (!canPasteMenuComponent((RADMenuItemComponent)transComp, cut))
                    return;
            }
            else if (component instanceof RADVisualContainer
                     && transComp instanceof RADVisualComponent)
            {   // pasting a visual component
                // check general cut conditions
                if (cut && !canPasteCut(transComp))
                    return;

                Class transClass = transComp.getBeanClass();
                if (java.awt.Window.class.isAssignableFrom(transClass)
                        || java.applet.Applet.class.isAssignableFrom(transClass))
                    return; // cannot copy Window or Applet to another component
            }
            else return; // not allowed combination of source/target

            s.add(new CopySupport.RADPaste(t,
                                           (ComponentContainer) component,
                                           component.getFormModel()));
        }
        else { // if there is not a RADComponent in the clipboard,
               // try if it is not InstanceCookie
            InstanceCookie ic =
                (InstanceCookie) NodeTransfer.cookie(t,
                                                     NodeTransfer.COPY,
                                                     InstanceCookie.class);
            if (ic != null)
                s.add(new CopySupport.InstancePaste(t,
                                                    component,
                                                    component.getFormModel()));
        }
    }

    // Checks whether the source menu component can be pasted to this component.
    private boolean canPasteMenuComponent(RADMenuItemComponent sourceMenuComp,
                                          boolean cut) {
        boolean canPaste = false;

        if (!(component instanceof RADMenuComponent)) {
            // target component is not a menu component
            if (component instanceof RADVisualContainer
                && sourceMenuComp instanceof RADMenuComponent)
            {
                RADVisualContainer cont = (RADVisualContainer) component;
                canPaste = cont.getContainerMenu() == null
                           && cont.canHaveMenu(sourceMenuComp.getBeanClass());
            }
        }
        else { // target component is some menu container
            canPaste = ((RADMenuComponent)component).canAddItem(
                                                sourceMenuComp.getBeanClass());
        }

        return canPaste && cut ?
                 canPasteCut(sourceMenuComp) : canPaste;
    }

    // Checks whether source component is not to be pasted to its own
    // container or even to itself.
    private boolean canPasteCut(RADComponent sourceComp) {
        if (sourceComp.getFormModel() != component.getFormModel())
            return true; // source component is from another form

        return sourceComp != component
               && sourceComp.getParentComponent() != component
               && !sourceComp.isParentComponent(component);
    }

    // -----------------------------------------------------------------------------
    // RADComponentCookie implementation

    public RADComponent getRADComponent() {
        return component;
    }

    // -----------------------------------------------------------------------------
    // Innerclasses

    public static class RADChildren extends FormNodeChildren {
        private ComponentContainer container;
        private Object keyLayout;

        public RADChildren(ComponentContainer container) {
            super();
            this.container = container;
            updateKeys();
        }

        // FormNodeChildren implementation
        protected void updateKeys() {
            RADComponent[] subComps = container.getSubBeans();
            ArrayList keys = new ArrayList(subComps.length + 2);

            if (container instanceof RADVisualContainer) {
                RADVisualContainer visualCont = (RADVisualContainer) container;

                RADComponent menuComp = visualCont.getContainerMenu();
                if (menuComp != null)
                    keys.add(menuComp);

                if (visualCont.getLayoutSupport().shouldHaveNode()) {
                    keyLayout = visualCont.getLayoutSupport().getLayoutDelegate(); //new Object(); // [need not be recreated every time]
                    keys.add(keyLayout);
                }

                for (int i=0; i < subComps.length; i++)
                    if (subComps[i] != menuComp)
                        keys.add(subComps[i]);
            }
            else {
                for (int i=0; i < subComps.length; i++)
                    keys.add(subComps[i]);
            }

            setKeys(keys);
        }

        protected Node[] createNodes(Object key) {
            Node node;
            if (key == keyLayout)
                node = new LayoutNode((RADVisualContainer)container);
            else {
                node = new RADComponentNode((RADComponent)key);
                node.getChildren().getNodes(); // enforce subnodes creation
            }
            return new Node[] { node };
        }
    }

    private final class ComponentsIndex extends org.openide.nodes.Index.Support {

        public Node[] getNodes() {
            RADComponent[] comps;
            if (component instanceof RADVisualContainer)
                comps = ((RADVisualContainer)component).getSubComponents();
            else if (component instanceof ComponentContainer)
                comps = ((ComponentContainer)component).getSubBeans();
            else
                comps = null;

            Node[] nodes = new Node[comps != null ? comps.length : 0];
            for (int i = 0; i < comps.length; i++)
                nodes[i] = comps[i].getNodeReference();

            return nodes;
        }

        public int getNodesCount() {
            return getNodes().length;
        }

        public void reorder(int[] perm) {
            if (component instanceof ComponentContainer) {
                ComponentContainer cont = (ComponentContainer) component;
                cont.reorderSubComponents(perm);
                component.getFormModel().fireComponentsReordered(cont);
            }
        }
    }
}
