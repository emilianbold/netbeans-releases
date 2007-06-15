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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.*;
import java.text.MessageFormat;
import java.util.*;
import java.beans.*;
import java.security.*;
import javax.swing.Action;

import org.openide.ErrorManager;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.explorer.propertysheet.editors.NodeCustomizer;

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.editors.TableCustomizer;
import org.netbeans.modules.form.menu.AddSubItemAction;
import org.netbeans.modules.form.menu.MenuEditLayer;

public class RADComponentNode extends FormNode
        implements RADComponentCookie, FormPropertyCookie {
    private final static MessageFormat nodeNameFormat =
            new MessageFormat(
            FormUtils.getBundleString("FMT_ComponentNodeName")); // NOI18N
    private final static MessageFormat nodeNoNameFormat =
            new MessageFormat(
            FormUtils.getBundleString("FMT_UnnamedComponentNodeName")); // NOI18N
    
    private RADComponent component;
    private Action[] actions;
    private boolean highlightDisplayName;
    
    public RADComponentNode(RADComponent component) {
        this(component instanceof ComponentContainer ?
            new RADChildren((ComponentContainer)component) : Children.LEAF,
            component);
    }
    
    public RADComponentNode(Children children, RADComponent component) {
        super(children, component.getFormModel());
        this.component = component;
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
    
    public Action getPreferredAction() {
        if (component instanceof RADVisualContainer)
            return SystemAction.get(EditContainerAction.class);
        //        if (component.getEventHandlers().getDefaultEvent() != null)
        return SystemAction.get(DefaultRADAction.class);
        
        //        return null;
    }
    
    public Action[] getActions(boolean context) {
        if (actions == null) { // from AbstractNode
            ArrayList actions = new ArrayList(20);
            RADComponent topComp = component.getFormModel().getTopRADComponent();
            
            if (component.isReadOnly()) {
                if (component == topComp) {
                    actions.add(SystemAction.get(TestAction.class));
                    actions.add(null);
                }
                Event[] events = component.getKnownEvents();
                for (int i=0; i < events.length; i++) {
                    if (events[i].hasEventHandlers()) {
                        actions.add(SystemAction.get(EventsAction.class));
                        actions.add(null);
                        break;
                    }
                }
                
                actions.add(SystemAction.get(CopyAction.class));
            } else {
                java.util.List actionProps = component.getActionProperties();
                Iterator iter = actionProps.iterator();
                while (iter.hasNext()) {
                    final RADProperty prop = (RADProperty)iter.next();
                    Action action = new PropertyAction(prop);
                    actions.add(action);
                }
                if (InPlaceEditLayer.supportsEditingFor(component.getBeanClass(), false)) {
                    actions.add(SystemAction.get(InPlaceEditAction.class));
                }
                if (component != topComp) {
                    actions.add(SystemAction.get(ChangeVariableNameAction.class));
                }
                if (component == topComp) {
                    actions.add(SystemAction.get(TestAction.class));
                }
                actions.add(SystemAction.get(BindAction.class));
                actions.add(SystemAction.get(EventsAction.class));
                actions.add(null);
                
                actions.add(SystemAction.get(AlignAction.class));
                actions.add(SystemAction.get(SetAnchoringAction.class));
                actions.add(SystemAction.get(SetResizabilityAction.class));
                actions.add(SystemAction.get(ChooseSameSizeAction.class));
                actions.add(SystemAction.get(DefaultSizeAction.class));
                actions.add(SystemAction.get(CustomizeEmptySpaceAction.class));
                actions.add(SystemAction.get(EncloseAction.class));
                actions.add(null);
                
                if(MenuEditLayer.isMenuRelatedRADComponent(component)) {
                    if(MenuEditLayer.isMenuRelatedContainer(component)) {
                        actions.add(SystemAction.get(AddSubItemAction.class));
                    }
                }
                
                if (component instanceof RADVisualContainer) {
                    if (!((RADVisualContainer)component).hasDedicatedLayoutSupport()) {
                        actions.add(SystemAction.get(EditContainerAction.class));
                        if (topComp != null && component != topComp) {
                            actions.add(SystemAction.get(EditFormAction.class));
                        }
                        actions.add(SystemAction.get(SelectLayoutAction.class));
                        actions.add(SystemAction.get(CustomizeLayoutAction.class));
                    }
                    // only use the AddAction for non-menu containers
                    if(!MenuEditLayer.isMenuRelatedContainer(component)) {
                        actions.add(SystemAction.get(AddAction.class));
                    }
                    
                }
                if (getNewTypes().length != 0) {
                    actions.add(null);
                    actions.add(SystemAction.get(NewAction.class));
                }
                if (component != topComp) {
                    actions.add(SystemAction.get(MoveUpAction.class));
                    actions.add(SystemAction.get(MoveDownAction.class));
                }
                if (component instanceof ComponentContainer) {
                    actions.add(SystemAction.get(ReorderAction.class));
                }
                actions.add(null);
                
                if (component != topComp) {
                    actions.add(SystemAction.get(CutAction.class));
                }
                actions.add(SystemAction.get(CopyAction.class));
                if (component instanceof ComponentContainer) {
                    actions.add(SystemAction.get(PasteAction.class));
                }
                if (component != topComp) {
                    actions.add(SystemAction.get(DuplicateAction.class));
                    actions.add(SystemAction.get(DeleteAction.class));
                }
                
                actions.add(null);
                actions.add(SystemAction.get(CustomCodeAction.class));
            }
            actions.add(null);
            
            javax.swing.Action[] superActions = super.getActions(context);
            for (int i=0; i < superActions.length; i++)
                actions.add(superActions[i]);
            
            this.actions = new Action[actions.size()];
            actions.toArray(this.actions);
        }
        
        return actions;
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
        component.rename(s);
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
        if (MetaComponentCreator.isTransparentLayoutComponent(component.getParentComponent())) {
            component = component.getParentComponent();
        }
        if (EventQueue.isDispatchThread()) {
            component.getFormModel().removeComponent(component, true);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    component.getFormModel().removeComponent(component, true);
                }
            });
        }
        component.setNodeReference(null);
        super.destroy();
    }
    
    /** Test whether there is a customizer for this node. If true,
     * the customizer can be obtained via {@link #getCustomizer}.
     *
     * @return <CODE>true</CODE> if there is a customizer
     */
    public boolean hasCustomizer() {
        return !component.isReadOnly()
                && ((component.getBeanInfo().getBeanDescriptor().getCustomizerClass() != null)
                    || javax.swing.JTable.class.isAssignableFrom(component.getBeanClass()));
    }
    
    /** Creates the customizer component for the node.
     * @return the component, or null if there is no customizer
     */
    protected Component createCustomizer() {
        Class customizerClass = component.getBeanInfo().getBeanDescriptor().getCustomizerClass();
        if (customizerClass == null) {
            if (javax.swing.JTable.class.isAssignableFrom(component.getBeanClass())) {
                customizerClass = TableCustomizer.class;
            } else {
                return null;
            }
        }
        
        Object customizerObject;
        try {
            customizerObject = customizerClass.newInstance();
        }
        catch (InstantiationException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            return null;
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            return null;
        }
        
        if (!(customizerObject instanceof Component)
                || !(customizerObject instanceof Customizer))
            return null;
        
        if (customizerObject instanceof NodeCustomizer)
            ((NodeCustomizer)customizerObject)
                    .attach(component.getNodeReference());
        
        Customizer customizer = (Customizer) customizerObject;
        
        customizer.setObject(component.getBeanInstance());

        if (customizerObject instanceof FormAwareEditor) {
            // Hack - returns some property
            Node.Property prop = component.getProperties()[0].getProperties()[0];
            ((FormAwareEditor)customizerObject).setContext(component.getFormModel(), (FormProperty)prop);
        }

        customizer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                FormProperty[] properties;
                if (evt.getPropertyName() != null) {
                    FormProperty changedProperty =
                            component.getBeanProperty(evt.getPropertyName());
                    if (changedProperty != null)
                        properties = new FormProperty[] { changedProperty };
                    else return; // non-existing property?
                }
                else {
                    properties = component.getAllBeanProperties();
                    evt = null;
                }
                updatePropertiesFromCustomizer(properties, evt);
            }
        });
        // [undo/redo for customizer probably does not work...]
        
        return (Component) customizerObject;
    }
    
    private void updatePropertiesFromCustomizer(
            final FormProperty[] properties,
            final PropertyChangeEvent evt)
    {
        // we run this as privileged to avoid security problems - because
        // the property change is fired from untrusted bean customizer code
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Object oldValue = evt != null ? evt.getOldValue() : null;
                Object newValue = evt != null ? evt.getNewValue() : null;
                
                for (int i=0; i < properties.length; i++) {
                    FormProperty prop = properties[i];
                    try {
                        prop.reinstateProperty();
                        //                        if (prop.isChanged()) // [what if changed to default value?]
                        prop.propertyValueChanged(oldValue, newValue);
                    }
                    catch (Exception ex) { // unlikely to happen
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
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
                CopySupport.getComponentCopyFlavor(),
                component);
    }
    
    /** Cut this node to the clipboard.
     *
     * @return {@link Transferable} with one flavor, {@link RAD_COMPONENT_COPY_FLAVOR }
     * @throws IOException if it could not cut
     */
    public Transferable clipboardCut() throws java.io.IOException {
        return new CopySupport.RADTransferable(
                CopySupport.getComponentCutFlavor(),
                component);
    }
    
    /** Accumulate the paste types that this node can handle
     * for a given transferable.
     * @param t a transferable containing clipboard data
     * @param s a list of {@link PasteType}s that will have added to it all
     *          types valid for this node
     */
    protected void createPasteTypes(Transferable t, java.util.List s) {
        CopySupport.createPasteTypes(t, s, component.getFormModel(), component);
    }

    // -----------------------------------------------------------------------------
    // RADComponentCookie implementation
    
    public RADComponent getRADComponent() {
        return component;
    }
    
    // -----------------------------------
    // FormPropertyCookie implementation
    
    public FormProperty getProperty(String name) {
        return (FormProperty)
                component.getPropertyByName(name, FormProperty.class, true);
        //        Node.Property prop = component.getPropertyByName(name, true);
        //        return (FormProperty) (prop instanceof FormProperty ? prop : null);
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
                
                if (visualCont.shouldHaveLayoutNode()) {
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
                component.getFormModel().fireComponentsReordered(cont, perm);
            }
        }
    }
    
    private static final class ChangeVariableNameAction extends RenameAction {
        public String getName() {
            return NbBundle.getMessage(ChangeVariableNameAction.class, "ChangeVariableNameAction"); // NOI18N
        }
    }
    
    public String getHtmlDisplayName() {
        if(highlightDisplayName) {
            return  "<html><b>" + getDisplayName() + "</b></html>"; // NOI18N
        } else {
            return "<html>" + getDisplayName() + "</html>"; // NOI18N
        }
    }
    
    void highlightDisplayName(boolean highlight) {
        if(highlight != highlightDisplayName) {
            highlightDisplayName = highlight;
            fireDisplayNameChange(null, getDisplayName());
        }
    }
}
