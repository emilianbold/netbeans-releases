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

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.NewType;

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.layoutsupport.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.text.MessageFormat;
import java.util.*;


public class RADComponentNode extends AbstractNode
    implements RADComponentCookie, FormCookie
{
    public static DataFlavor RAD_COMPONENT_COPY_FLAVOR = new RADDataFlavor(
        RADComponentNode.class,
        "RAD_COMPONENT_COPY_FLAVOR" // NOI18N
        );
    public static DataFlavor RAD_COMPONENT_CUT_FLAVOR = new RADDataFlavor(
        RADComponentNode.class,
        "RAD_COMPONENT_CUT_FLAVOR" // NOI18N
        );


    private final static MessageFormat nameFormat =
        new MessageFormat(NbBundle.getBundle(RADComponentNode.class).getString("FMT_ComponentName"));
    private final static MessageFormat formNameFormat =
        new MessageFormat(NbBundle.getBundle(RADComponentNode.class).getString("FMT_FormName"));

    private RADComponent component;
    private RADComponentInstance radComponentInstance;

    public RADComponentNode(RADComponent component) {
        super((component instanceof ComponentContainer)
              ? new RADChildren((ComponentContainer)component) : Children.LEAF);
        this.component = component;
        radComponentInstance = new RADComponentInstance(component);
        component.setNodeReference(this);
        getCookieSet().add(this);
        if (component instanceof ComponentContainer) {
            getCookieSet().add(new ComponentsIndex());
        }
        updateName();
    }

    void updateName() {
        Class compClass = component.getBeanClass();
        if (component instanceof FormContainer) {
            Class formClass =((FormContainer)component).getFormInfo().getFormInstance().getClass();
            setDisplayName(formNameFormat.format(
                new Object[] {
                    component.getFormModel().getFormDataObject().getName() ,
                    formClass.getName(),
                    Utilities.getShortClassName(formClass) }));
        } else {
            setDisplayName(nameFormat.format(
                new Object[] {
                    getName(),
                    compClass.getName(),
                    Utilities.getShortClassName(compClass) }));
        }
    }

    void updateChildren() {
        ((RADChildren)getChildren()).updateKeys();
    }

    public void fireComponentPropertiesChange() {
        firePropertyChange(null, null, null);
    }

    public void fireComponentPropertySetsChange() {
        firePropertySetsChange(null, null);
    }

    /** Provides package-private access for firing property changes */
    void firePropertyChangeHelper(String name, Object oldValue, Object newValue) {
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
            return new HelpCtx(RADComponentNode.class);
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
    protected SystemAction [] createActions() {
        ArrayList actions = new ArrayList(20);

        if (component.isReadOnly()) {
            if (component.getEventHandlers().getHandlersCount() > 0) {
                actions.add(SystemAction.get(EventsAction.class));
                actions.add(null);
            }
            actions.add(SystemAction.get(GotoFormAction.class));
            actions.add(SystemAction.get(GotoEditorAction.class));
            actions.add(SystemAction.get(GotoInspectorAction.class));
            actions.add(null);
            actions.add(SystemAction.get(CopyAction.class));
            actions.add(null);
        }
        else {
            if (component instanceof RADVisualContainer) {
                actions.add(SystemAction.get(EditContainerAction.class));
                actions.add(null);
                actions.add(SystemAction.get(SelectLayoutAction.class));
                actions.add(SystemAction.get(CustomizeLayoutAction.class));
                actions.add(null);
            }
            actions.add(SystemAction.get(EventsAction.class));
            actions.add(null);

            if (component instanceof ComponentContainer) {
                actions.add(SystemAction.get(ReorderAction.class));
                if (!(component instanceof FormContainer)) {
                    actions.add(SystemAction.get(MoveUpAction.class));
                    actions.add(SystemAction.get(MoveDownAction.class));
                }
                actions.add(null);
                actions.add(SystemAction.get(GotoFormAction.class));
                actions.add(SystemAction.get(GotoEditorAction.class));
                actions.add(SystemAction.get(GotoInspectorAction.class));
                actions.add(null);
                actions.add(SystemAction.get(CutAction.class));
                actions.add(SystemAction.get(CopyAction.class));
                actions.add(SystemAction.get(PasteAction.class));
            } else {
                actions.add(SystemAction.get(InPlaceEditAction.class));
                actions.add(null);
                actions.add(SystemAction.get(GotoFormAction.class));
                actions.add(SystemAction.get(GotoEditorAction.class));
                actions.add(SystemAction.get(GotoInspectorAction.class));
                actions.add(null);
                actions.add(SystemAction.get(MoveUpAction.class));
                actions.add(SystemAction.get(MoveDownAction.class));
                actions.add(null);
                actions.add(SystemAction.get(CutAction.class));
                actions.add(SystemAction.get(CopyAction.class));
            }

            actions.add(null);
            if (!(component instanceof FormContainer)) {
                actions.add(SystemAction.get(RenameAction.class));
                actions.add(SystemAction.get(DeleteAction.class));
                actions.add(null);
            }
            if (getNewTypes().length != 0) {
                actions.add(SystemAction.get(NewAction.class));
                actions.add(null);
            }
        }

//        actions.add(SystemAction.get(ToolsAction.class));
        actions.add(SystemAction.get(PropertiesAction.class));

        SystemAction[] array = new SystemAction [actions.size()];
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
        return !component.isReadOnly() && !(component instanceof FormContainer);
    }

    /** Can this node be destroyed?
     * @return <CODE>false</CODE>
     */
    public boolean canDestroy() {
        return !component.isReadOnly() && !(component instanceof FormContainer);
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
        component.getFormModel().deleteComponent(component);
        super.destroy();
    }

    /** Get a cookie from the node.
     * Uses the cookie set as determined by {@link #getCookieSet}.
     *
     * @param type the representation class
     * @return the cookie or <code>null</code>
     */
    public Node.Cookie getCookie(Class type) {
        if (InstanceCookie.class.equals(type)) {
            return radComponentInstance;
        }
        Node.Cookie inh = super.getCookie(type);
        if (inh == null) {
            if (CompilerCookie.class.isAssignableFrom(type) ||
                SaveCookie.class.isAssignableFrom(type) ||
                DataObject.class.isAssignableFrom(type) ||
                ExecCookie.class.isAssignableFrom(type) ||
                DebuggerCookie.class.isAssignableFrom(type) ||
                CloseCookie.class.isAssignableFrom(type) ||
                ArgumentsCookie.class.isAssignableFrom(type) ||
                PrintCookie.class.isAssignableFrom(type)) {
                return component.getFormModel().getFormDataObject().getCookie(type);
            }
        }
        return inh;
    }

    /** Test whether there is a customizer for this node. If true,
     * the customizer can be obtained via {@link #getCustomizer}.
     *
     * @return <CODE>true</CODE> if there is a customizer
     */
    public boolean hasCustomizer() {
        return !component.isReadOnly() && component.getBeanInfo().getBeanDescriptor()
                                                 .getCustomizerClass() != null;
    }

    /** Get the customizer component.
     * @return the component, or <CODE>null</CODE> if there is no customizer
     */
    public java.awt.Component getCustomizer() {
        Class customizerClass = component.getBeanInfo().getBeanDescriptor().getCustomizerClass();
        if (customizerClass == null) return null;
        Object customizer;
        try {
            customizer = customizerClass.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
        if (!(customizer instanceof java.awt.Component) ||
            !(customizer instanceof java.beans.Customizer)) return null;

        if (customizer instanceof FormAwareEditor) {
            ((FormAwareEditor)customizer).setFormModel(component.getFormModel());
        }
        if (customizer instanceof org.openide.explorer.propertysheet.editors.NodeCustomizer) {
            ((org.openide.explorer.propertysheet.editors.NodeCustomizer)customizer).attach(component.getNodeReference());
        }

        ((java.beans.Customizer)customizer).setObject(component.getBeanInstance());
        // [PENDING - in X2 there is some strange addPropertyChangeListener code here...]
        return(java.awt.Component)customizer;
    }

    // -----------------------------------------------------------------------------------------
    // Clipboard operations

    /** Test whether this node can be copied.
     * The default implementation returns <code>true</code>.
     * @return <code>true</code> if it can
     */
    public boolean canCopy() {
        return !(component instanceof FormContainer);
    }

    /** Test whether this node can be cut.
     * The default implementation assumes it can if this node is writeable.
     * @return <code>true</code> if it can
     */
    public boolean canCut() {
        return !component.isReadOnly() && !(component instanceof FormContainer);
    }

    /** Copy this node to the clipboard.
     *
     * @return The transferable for RACComponentNode
     * @throws IOException if it could not copy
     */
    public Transferable clipboardCopy() throws java.io.IOException {
        return new RADTransferable(RAD_COMPONENT_COPY_FLAVOR, component);
    }

    /** Store original name of component and subcomponents. */
    private void storeNames(RADComponent comp) {
        comp.storeName();
        if (comp instanceof ComponentContainer) {
            RADComponent comps[] =((ComponentContainer) comp).getSubBeans();
            for (int i=0, n=comps.length; i<n; i++) {
                storeNames(comps[i]);
            }
        }
    }

    /** Cut this node to the clipboard.
     *
     * @return {@link Transferable} with one flavor, {@link RAD_COMPONENT_COPY_FLAVOR }
     * @throws IOException if it could not cut
     */
    public Transferable clipboardCut() throws java.io.IOException {
        return new RADTransferable(RAD_COMPONENT_CUT_FLAVOR, component);
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
        if (component.isReadOnly()) return;

        boolean copy = t.isDataFlavorSupported(RAD_COMPONENT_COPY_FLAVOR);
        boolean cut = t.isDataFlavorSupported(RAD_COMPONENT_CUT_FLAVOR);

        if (copy || cut) {
            try {
                RADComponent transComp = (RADComponent)
                        t.getTransferData(t.getTransferDataFlavors()[0]);

                if (transComp instanceof RADMenuItemComponent) {
                    // pasting menu component, check if it is possible
                    if (canPasteMenuComponent((RADMenuItemComponent)transComp, cut))
                        s.add(new RADPaste(t));
                }
                else if (component instanceof RADVisualContainer // including the form
                         && (!cut || canPasteCut(transComp)))
                    // pasting to visual container (not only visual components)
                    s.add(new RADPaste(t));
            }
            catch (UnsupportedFlavorException e) {} // should not happen
            catch (java.io.IOException e) {} // should not happen
        }
        else { // if there is not a RADComponent in the clipboard,
               // try if it is not InstanceCookie
            InstanceCookie ic = (InstanceCookie)NodeTransfer.cookie(t,
                                      NodeTransfer.COPY, InstanceCookie.class);
            if (ic != null)
                s.add(new InstancePaste(t));
        }
    }

    // Checks whether the source menu component can be pasted to this component.
    // (There are some non-trivial restrictions when copying menu components.)
    private boolean canPasteMenuComponent(RADMenuItemComponent sourceMenuComp,
                                          boolean cut) {
        boolean canPaste = false;

        if (!(component instanceof RADMenuComponent)) {
            // target component is not a menu component
            if (component instanceof FormContainer) {
                // target component is the form
                int sourceMenuType = sourceMenuComp.getMenuItemType();
                canPaste = sourceMenuType == RADMenuItemComponent.T_MENUBAR
                        || sourceMenuType == RADMenuItemComponent.T_JMENUBAR
                        || sourceMenuType == RADMenuItemComponent.T_POPUPMENU
                        || sourceMenuType == RADMenuItemComponent.T_JPOPUPMENU;
            }
        }
        else { // target component is some menu container
            int menuType = ((RADMenuComponent)component).getMenuItemType();
            int sourceMenuType = sourceMenuComp.getMenuItemType();

            if (menuType == RADMenuItemComponent.T_MENUBAR
                    || menuType == RADMenuItemComponent.T_JMENUBAR) {
                // target component is a menu bar - only menus are allowed
                canPaste = sourceMenuType == RADMenuItemComponent.T_MENU
                        || sourceMenuType == RADMenuItemComponent.T_JMENU;
            }
            else { // target component is a menu - menus and menu items allowed
                canPaste = sourceMenuType != RADMenuItemComponent.T_MENUBAR
                        && sourceMenuType != RADMenuItemComponent.T_JMENUBAR
                        && sourceMenuType != RADMenuItemComponent.T_POPUPMENU
                        && sourceMenuType != RADMenuItemComponent.T_JPOPUPMENU;
            }
        }

        return canPaste && cut ?
                 canPasteCut(sourceMenuComp) : canPaste;
    }

    // Checks whether source component is not to be pasted to its own
    // container or even to itself.
    private boolean canPasteCut(RADComponent sourceComp) {
        if (sourceComp.getFormModel() != component.getFormModel())
            return true; // source component is from another form

        if (sourceComp instanceof RADVisualComponent) {
            if (!(component instanceof RADVisualContainer)) return false;
            RADVisualContainer targetContainer = (RADVisualContainer)component;
            RADVisualContainer sourceContainer =
                ((RADVisualComponent)sourceComp).getParentContainer();
            if (targetContainer == sourceContainer) return false;

            // target container also cannot be in source container tree
            do {
                if (targetContainer == sourceComp) return false;
                targetContainer = targetContainer.getParentContainer();
            }
            while (targetContainer != null);
        }
        else if (sourceComp instanceof RADMenuItemComponent) {
            if (!(component instanceof RADMenuComponent)) return false;
            RADMenuComponent targetContainer = (RADMenuComponent)component;
            RADMenuComponent sourceContainer =
                ((RADMenuItemComponent)sourceComp).getParentMenu();
            if (targetContainer == sourceContainer) return false;

            // target container also cannot be in source container tree
            do {
                if (targetContainer == sourceComp) return false;
                targetContainer = targetContainer.getParentMenu();
            }
            while (targetContainer != null);
        }
        else return false;

        return true;
    }

    // -----------------------------------------------------------------------------
    // RADComponentCookie implementation

    public RADComponent getRADComponent() {
        return component;
    }

    // -------------------------------------------------------------------------------
    // FormCookie implementation

    /** Focuses the source editor */
    public void gotoEditor() {
        component.getFormModel().getFormEditorSupport().gotoEditor();
    }

    /** Focuses the form */
    public void gotoForm() {
        component.getFormModel().getFormEditorSupport().gotoForm();
    }

    // -----------------------------------------------------------------------------
    // Innerclasses

    /** Index support for reordering of file system pool.
     */
    private final class ComponentsIndex extends org.openide.nodes.Index.Support {

        /** Get the nodes; should be overridden if needed.
         * @return the nodes
         * @throws NotImplementedException always
         */
        public Node[] getNodes() {
            RADComponent[] comps =((ComponentContainer)getRADComponent()).getSubBeans();
            Node[] nodes = new Node[comps.length];
            for (int i = 0; i < comps.length; i++) {
                nodes[i] = comps[i].getNodeReference();
            }
            return nodes;
        }

        /** Get the node count. Subclasses must provide this.
         * @return the count
         */
        public int getNodesCount() {
            return getNodes().length;
        }

        /** Reorder by permutation. Subclasses must provide this.
         * @param perm the permutation
         */
        public void reorder(int[] perm) {
            ((ComponentContainer)getRADComponent()).reorderSubComponents(perm);
            ((RADChildren)getChildren()).updateKeys();
        }
    }

    static class RADDataFlavor extends DataFlavor {
        static final long serialVersionUID =3851021533468196849L;
        RADDataFlavor(Class representationClass, String name) {
            super(representationClass, name);
        }
    }

    public static class RADTransferable implements Transferable {
        private RADComponent radComponent;
        private DataFlavor[] flavors;

        RADTransferable(DataFlavor flavor, RADComponent radComponent) {
            this(new DataFlavor[] { flavor }, radComponent);
        }

        RADTransferable(DataFlavor[] flavors, RADComponent radComponent) {
            this.flavors = flavors;
            this.radComponent = radComponent;
        }

        /** Returns an array of DataFlavor objects indicating the flavors the data
         * can be provided in.  The array should be ordered according to preference
         * for providing the data(from most richly descriptive to least descriptive).
         * @return an array of data flavors in which this data can be transferred
         */
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        /** Returns whether or not the specified data flavor is supported for
         * this object.
         * @param flavor the requested flavor for the data
         * @return boolean indicating wjether or not the data flavor is supported
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i] == flavor) { // comparison based on exact instances, as these are static in this node
                    return true;
                }
            }
            return false;
        }

        /** Returns an object which represents the data to be transferred.  The class
         * of the object returned is defined by the representation class of the flavor.
         *
         * @param flavor the requested flavor for the data
         * @see DataFlavor#getRepresentationClass
         * @exception IOException                if the data is no longer available
         *              in the requested flavor.
         * @exception UnsupportedFlavorException if the requested data flavor is
         *              not supported.
         */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException {
            if (flavor instanceof RADDataFlavor) {
                return radComponent;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    // -----------------------------------------------------------------------------
    // Paste types

    /** Paste type for meta components.
     */
    private final class RADPaste extends PasteType {
        private Transferable transferable;

        public RADPaste(Transferable t) {
            this.transferable = t;
        }

        public Transferable paste() throws java.io.IOException {
            boolean fromCut =
                transferable.isDataFlavorSupported(RAD_COMPONENT_CUT_FLAVOR);

            RADComponent sourceComponent = null;
            try {
                sourceComponent = (RADComponent) transferable.getTransferData(
                    fromCut ? RAD_COMPONENT_CUT_FLAVOR : RAD_COMPONENT_COPY_FLAVOR);
            }
            catch (java.io.IOException e) { } // ignore - should not happen
            catch (UnsupportedFlavorException e) { } // ignore - should not happen

            if (sourceComponent == null) return null;

            FormModel targetForm = component.getFormModel();

            if (!fromCut) { // pasting copy of RADComponent
                RADComponent newCopy = makeCopy(sourceComponent); //, false);
                if (newCopy instanceof RADVisualComponent 
                        && component instanceof RADVisualContainer) {
                    addVisualComponentCopy((RADVisualComponent)newCopy,
                                           (RADVisualContainer)component,
                                           targetForm);
                }
                else {
                    if (sourceComponent instanceof RADMenuItemComponent
                            && component instanceof RADMenuComponent) {
                        targetForm.addNonVisualComponent(newCopy, 
                                     (ComponentContainer)component);
                    }
                    else targetForm.addNonVisualComponent(newCopy, null);
                }
                return null;
            }
            else { // pasting cut RADComponent (same instance)
                FormModel sourceForm = sourceComponent.getFormModel();
                if (sourceForm != targetForm) { // taken from another form
                    Node sourceNode = sourceComponent.getNodeReference();
                    // delete component in the source
                    if (sourceNode != null) sourceNode.destroy();
                    else sourceForm.deleteComponent(sourceComponent);
                    sourceComponent.initialize(targetForm);
                }
                else { // same form
                    if (!canPasteCut(sourceComponent))
                        return transferable; // ignore paste to itself

                    // remove source component from its parent
                    sourceForm.removeComponent(sourceComponent);
                }

                if (sourceComponent instanceof RADVisualComponent) {
                    addVisualComponentCopy((RADVisualComponent)sourceComponent,
                                           (RADVisualContainer)component,
                                           targetForm);
                }
                else {
                    if (sourceComponent instanceof RADMenuItemComponent
                            && component instanceof RADMenuComponent) {
                        targetForm.addNonVisualComponent(sourceComponent, 
                                                 (ComponentContainer)component);
                    }
                    else targetForm.addNonVisualComponent(sourceComponent, null);
                }

                // put copy flavor as the new one, as the first instance was used already
                return new RADTransferable(RAD_COMPONENT_COPY_FLAVOR, sourceComponent);
            }
        }
    }

    private RADComponent makeCopy(RADComponent original) {//, boolean assignName) {
        RADComponent copy;
        if (original instanceof RADVisualContainer)
            copy = new RADVisualContainer();
        else if (original instanceof RADVisualComponent)
            copy = new RADVisualComponent();
        else if (original instanceof RADMenuComponent)
            copy = new RADMenuComponent();
        else if (original instanceof RADMenuItemComponent)
            copy = new RADMenuItemComponent();
        else
            copy = new RADComponent();

        copy.initialize(component.getFormModel());
        copy.setComponent(original.getBeanClass());
        //if (assignName) copyComponent.setName(component.getFormModel().getVariablePool().getNewName(original.getBeanClass()));

        // 1. copy subcomponents
        if (original instanceof ComponentContainer) {
            RADComponent[] originalSubs = ((ComponentContainer)original).getSubBeans();
            RADComponent[] newSubs = new RADComponent[originalSubs.length];
            for (int i = 0; i < originalSubs.length; i++) {
                newSubs[i] = makeCopy(originalSubs [i]); //, true);
            }
            ((ComponentContainer)copy).initSubComponents(newSubs);
        }

        // 2. clone LayoutSupport on container
        if (original instanceof RADVisualContainer) {
            RADVisualContainer newCont = (RADVisualContainer)copy;
            LayoutSupport origLS = ((RADVisualContainer)original).getLayoutSupport();
            if (origLS != null) {
                try {
                    LayoutSupport newLS = LayoutSupportRegistry
                                          .copyLayoutSupport(origLS, newCont);
                    newCont.setLayoutSupport(newLS);
                }
                catch (Exception ex) { // ignore
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        ex.printStackTrace();
                }
            }
            else newCont.initLayoutSupport();
        }

        // 3. copy changed properties
        RADProperty[] originalProps = original.getAllBeanProperties();
        RADProperty[] newProps = copy.getAllBeanProperties();
        FormUtils.copyProperties(originalProps, newProps, true, false);

        // 4. copy aux values
        java.util.Map auxVals = original.getAuxValues();
        for (Iterator it = auxVals.keySet().iterator(); it.hasNext(); ) {
            String auxName = (String)it.next();
            Object auxValue = auxVals.get(auxName);
            try {
                copy.setAuxValue(auxName, FormUtils.cloneObject(auxValue));
            }
            catch (Exception e) {
                // ignore aux value with problem
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    e.printStackTrace();
            }
        }

        // 5. copy constraints
        if (original instanceof RADVisualComponent) {
            Map constraints = ((RADVisualComponent)original).getConstraintsMap();
            Map newConstraints = new HashMap();

            for (Iterator it = constraints.keySet().iterator(); it.hasNext(); ) {
                try { // clone constraints description (should be serializable)
                    Object layoutClassName = it.next();
                    Object clonedConstraints = FormUtils.cloneBeanInstance(
                                       constraints.get(layoutClassName), null);
                    newConstraints.put(layoutClassName, clonedConstraints);
                }
                catch (Exception e) { // ignore
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace();
                }
            }
            ((RADVisualComponent)copy).setConstraintsMap(newConstraints);
        }

        // 6. copy events
        // only if copied between forms
        // [PENDING - Events]

        return copy;
    }

    private void addVisualComponentCopy(RADVisualComponent comp,
                                        RADVisualContainer cont,
                                        FormModel form) {
        LayoutSupport laysup = cont.getLayoutSupport();
        if (laysup != null) {
            LayoutSupport.ConstraintsDesc cd = laysup.getConstraints(comp);
            form.addVisualComponent(comp, cont, laysup.fixConstraints(cd));
        }
    }

    /** Paste type for InstanceCookie.
     */
    private final class InstancePaste extends PasteType {
        private Transferable transferable;
        
        /**
         * @param obj object to work with
         */
        public InstancePaste(Transferable t) {
            transferable = t;
        }
    
        /** Paste.
         */
        public final Transferable paste() throws java.io.IOException {
            // This paste operation is very similar to adding a new bean.
            // Should be rewritten to share most code with HandleLayer
            // (to behave same way, without code duplicating here).

            InstanceCookie ic = (InstanceCookie)NodeTransfer.cookie(transferable,
                                      NodeTransfer.COPY, InstanceCookie.class);
            FormModel formModel = component.getFormModel();

            try {
                Class instanceClass = ic.instanceClass();

                if (Component.class.isAssignableFrom(instanceClass)) {
                    // visual component
                    Object isContainer =
                        Container.class.isAssignableFrom(instanceClass) ?
                            BeanSupport.createBeanInfo(instanceClass)
                                .getBeanDescriptor().getValue("isContainer") : // NOI18N
                            Boolean.FALSE;

                    RADVisualComponent newComp = null;
                    RADVisualContainer newCont = null;
                    if (isContainer == null || Boolean.TRUE.equals(isContainer))
                        newCont = new RADVisualContainer();

                    while (newComp == null) {
                        // initialize meta-component and its bean instance
                        newComp = newCont == null ?
                            new RADVisualComponent() : newCont;

                        newComp.initialize(formModel);
                        newComp.initInstance(ic);

                        if (newCont != null) {
                            // initialize LayoutSupport
                            newCont.initLayoutSupport();
                            if (newCont.getLayoutSupport() == null) {
                                // no LayoutSupport found for the container,
                                // create RADVisualComponent only
                                newCont = null;
                                newComp = null;
                            }
                        }
                    }

                    RADVisualContainer parentCont = (RADVisualContainer)component;
                    formModel.addVisualComponent(newComp, parentCont, null);

                    // for some components, we initialize their properties with some non-default values
                    // e.g. a label on buttons, checkboxes
                    FormEditor.defaultComponentInit(newComp);
                }
                else { // non-visual component
                    RADComponent newComp = new RADComponent();
                    newComp.initialize(formModel);
                    newComp.initInstance(ic);
                    formModel.addNonVisualComponent(newComp, null);
                }
            }
            catch (Throwable th) {
                if (th instanceof ThreadDeath)
                    throw (ThreadDeath)th;
                else {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        th.printStackTrace();
                    TopManager.getDefault().notifyException(th);
                    // [PENDING - better notification]
                }
            }
            return transferable;
        }
    }
}
