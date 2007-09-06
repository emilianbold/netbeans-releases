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

import java.util.*;
import java.beans.*;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.MenuElement;

import org.openide.nodes.*;

import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.layoutsupport.*;

/**
 *
 * @author Ian Formanek
 */

public class RADVisualComponent extends RADComponent {
    private static final String PROP_LAYOUT_COMPONENT_HORIZONTAL_SIZE = "layoutComponentHorizontalSize"; // NOI18N
    private static final String PROP_LAYOUT_COMPONENT_VERTICAL_SIZE = "layoutComponentVerticalSize"; // NOI18N
    private static final String PROP_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE = "layoutComponentHorizontalResizable"; // NOI18N
    private static final String PROP_LAYOUT_COMPONENT_VERTICAL_RESIZABLE = "layoutComponentVerticalResizable"; // NOI18N

    // -----------------------------------------------------------------------------
    // Private properties

    // [??]
    private HashMap constraints = new HashMap();
//    transient private RADVisualContainer parent;

    private Node.Property[] constraintsProperties;
    private ConstraintsListenerConvertor constraintsListener;

    private MetaAccessibleContext accessibilityData;
    private FormProperty[] accessibilityProperties;

    enum MenuType { JMenuItem, JCheckBoxMenuItem, JRadioButtonMenuItem,
                    JMenu, JMenuBar, JPopupMenu, JSeparator }

    // -----------------------------------------------------------------------------
    // Initialization

//    public void setParentComponent(RADComponent parentComp) {
//        super.setParentComponent(parentComp);
//        if (parentComp != null)
//            getConstraintsProperties();
//    }

//    void initParent(RADVisualContainer parent) {
//        this.parent = parent;
//    }

/*    protected void setBeanInstance(Object beanInstance) {
        if (beanInstance instanceof java.awt.Component) {
            boolean attached = FakePeerSupport.attachFakePeer(
                                            (java.awt.Component)beanInstance);
            if (attached && beanInstance instanceof java.awt.Container)
                FakePeerSupport.attachFakePeerRecursively(
                                            (java.awt.Container)beanInstance);
        }

        super.setBeanInstance(beanInstance);
    } */

    // -----------------------------------------------------------------------------
    // Public interface

    /** @return The JavaBean visual component represented by this RADVisualComponent */
//    public java.awt.Component getComponent() { // [is it needed ???]
//        return (java.awt.Component) getBeanInstance();
//    }

    public final RADVisualContainer getParentContainer() {
        return (RADVisualContainer) getParentComponent();
    }

    /** @return The index of this component within visual components of its parent */
    public final int getComponentIndex() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getIndexOf(this) : -1;
//        return ((ComponentContainer)getParentComponent()).getIndexOf(this);
    }

    final LayoutSupportManager getParentLayoutSupport() {
        RADVisualContainer parent = (RADVisualContainer) getParentComponent();
        return parent != null ? parent.getLayoutSupport() : null;
    }

    boolean isMenuTypeComponent() {
        return MenuElement.class.isAssignableFrom(getBeanClass());
    }

    /**
     * Returns whether this component is treated specially as a menu component.
     * Not only it must be of particluar Swing menu class, but must also be used
     * as a menu, not as normal visual component. Technically it must be either
     * contained in another menu, or be a menu bar of a window.
     * @return whether the component is a menu used in another menu or as menu
     *         bar in a window
     */
    public boolean isMenuComponent() {
        if (isMenuTypeComponent()) {
            RADVisualContainer parent = getParentContainer();
            if ((parent == null && !isInModel())
                || (parent != null
                    && (parent.isMenuTypeComponent() || this == parent.getContainerMenu()))) {
                return true;
            }
        }
        return false;
    }

    static MenuType getMenuType(Class cl) {
        if (MenuElement.class.isAssignableFrom(cl)) {
            if (JMenu.class.isAssignableFrom(cl)) {
                return MenuType.JMenu;
            }
            if (JMenuBar.class.isAssignableFrom(cl)) {
                return MenuType.JMenuBar;
            }
            if (JCheckBoxMenuItem.class.isAssignableFrom(cl)) {
                return MenuType.JCheckBoxMenuItem;
            }
            if (JRadioButtonMenuItem.class.isAssignableFrom(cl)) {
                return MenuType.JRadioButtonMenuItem;
            }
            if (JMenuItem.class.isAssignableFrom(cl)) {
                return MenuType.JMenuItem;
            }
            if (JPopupMenu.class.isAssignableFrom(cl)) {
                return MenuType.JPopupMenu;
            }
        } else if (JSeparator.class.isAssignableFrom(cl)) {
            return MenuType.JSeparator;
        }
        return null;
    }

    // -----------------------------------------------------------------------------
    // Layout constraints management

    /** Sets component's constraints description for given layout-support class. 
     */
    public void setLayoutConstraints(Class layoutDelegateClass,
                                     LayoutConstraints constr)
    {
        if (constr != null)
            constraints.put(layoutDelegateClass.getName(), constr);
    }

    /** Gets component's constraints description for given layout-support class.
     */
    public LayoutConstraints getLayoutConstraints(Class layoutDelegateClass) {
        return (LayoutConstraints)
               constraints.get(layoutDelegateClass.getName());
    }

    HashMap getConstraintsMap() {
        return constraints;
    }

    void setConstraintsMap(Map map) {
        constraints.putAll(map);
    }

    // ---------------
    // Properties

    protected void createPropertySets(List propSets) {
        super.createPropertySets(propSets);

        if (constraintsProperties == null)
            createConstraintsProperties();

        if (constraintsProperties != null && constraintsProperties.length > 0)
            propSets.add(propSets.size() - 1,
                         new Node.PropertySet("layout", // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTab"), // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTabHint")) // NOI18N
            {
                public Node.Property[] getProperties() {
                    return getConstraintsProperties();
                }
            });

        if (accessibilityProperties == null)
            createAccessibilityProperties();

        if (accessibilityProperties.length > 0)
            propSets.add(new Node.PropertySet(
                "accessibility", // NOI18N
                FormUtils.getBundleString("CTL_AccessibilityTab"), // NOI18N
                FormUtils.getBundleString("CTL_AccessibilityTabHint")) // NOI18N
            {
                public Node.Property[] getProperties() {
                    return getAccessibilityProperties();
                }
            });
    }

    @Override
    public <T> T getPropertyByName(String name, Class<? extends T> propertyType, boolean fromAll) {
        if (fromAll && accessibilityProperties == null)
            createAccessibilityProperties();
        return super.getPropertyByName(name, propertyType, fromAll);
    }

    /** Called to modify original properties obtained from BeanInfo.
     * Properties may be added, removed etc. - due to specific needs
     * of subclasses. Here used for adding ButtonGroupProperty.
     */
/*    protected void changePropertiesExplicitly(List prefProps,
                                              List normalProps,
                                              List expertProps) {

        super.changePropertiesExplicitly(prefProps, normalProps, expertProps);

        if (getBeanInstance() instanceof java.awt.TextComponent) {
            // hack for AWT text components - "text" property should be first
            for (int i=0, n=normalProps.size(); i < n; i++) {
                RADProperty prop = (RADProperty) normalProps.get(i);
                if ("text".equals(prop.getName())) { // NOI18N
                    normalProps.remove(i);
                    normalProps.add(0, prop);
                    break;
                }
            }
        }

        // hack for buttons - add a fake property for ButtonGroup
//        if (getBeanInstance() instanceof javax.swing.AbstractButton)
//            try {
//                Node.Property prop = new ButtonGroupProperty(this);
//                nameToProperty.put(prop.getName(), prop);
//                if (getBeanInstance() instanceof javax.swing.JToggleButton)
//                    prefProps.add(prop);
//                else
//                    normalProps.add(prop);
//            }
//            catch (IntrospectionException ex) {} // should not happen

//        if (getBeanInstance() instanceof javax.swing.JLabel)
//            try {
//                PropertyDescriptor pd = new PropertyDescriptor("displayedMnemonic",
//                    javax.swing.JLabel.class, "getDisplayedMnemonic", "setDisplayedMnemonic");
//                normalProps.add(createProperty(pd));
//            }
//            catch (IntrospectionException ex) {} // should not happen
    } */

    protected void clearProperties() {
        super.clearProperties();
        constraintsProperties = null;
        accessibilityData = null;
        accessibilityProperties = null;
    }

    // ---------
    // constraints properties

    public Node.Property[] getConstraintsProperties() {
        if (constraintsProperties == null)
            createConstraintsProperties();
        return constraintsProperties;
    }

    public void resetConstraintsProperties() {
        if (constraintsProperties != null) {
            for (int i=0; i < constraintsProperties.length; i++)
                nameToProperty.remove(constraintsProperties[i].getName());

            constraintsProperties = null;
            propertySets = null;

            RADComponentNode node = getNodeReference();
            if (node != null)
                node.fireComponentPropertySetsChange();
        }
    }

    private void createConstraintsProperties() {
        constraintsProperties = null;

        LayoutSupportManager layoutSupport = getParentLayoutSupport();
        if (layoutSupport != null) {
            LayoutConstraints constr = layoutSupport.getConstraints(this);
            if (constr != null)
                constraintsProperties = constr.getProperties();
        } else if (getParentContainer() != null) {
            LayoutComponent component = getFormModel().getLayoutModel().getLayoutComponent(getId());
            if (component == null) return; // Will be called again later
            constraintsProperties = new Node.Property[] {
                new LayoutComponentSizeProperty(component, LayoutConstants.HORIZONTAL),
                new LayoutComponentSizeProperty(component, LayoutConstants.VERTICAL),
                new LayoutComponentResizableProperty(component, LayoutConstants.HORIZONTAL),
                new LayoutComponentResizableProperty(component, LayoutConstants.VERTICAL)
            };
            component.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    RADComponentNode node = getNodeReference();
                    if (node != null) {
                        String propName = evt.getPropertyName();
                        if (LayoutConstants.PROP_HORIZONTAL_PREF_SIZE.equals(propName)) {
                            node.firePropertyChangeHelper(PROP_LAYOUT_COMPONENT_HORIZONTAL_SIZE, null, null);
                        } else if (LayoutConstants.PROP_VERTICAL_PREF_SIZE.equals(propName)) {
                            node.firePropertyChangeHelper(PROP_LAYOUT_COMPONENT_VERTICAL_SIZE, null, null);
                        } else if (LayoutConstants.PROP_HORIZONTAL_MAX_SIZE.equals(propName)) {
                            node.firePropertyChangeHelper(PROP_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE, null, null);
                        } else if (LayoutConstants.PROP_VERTICAL_MAX_SIZE.equals(propName)) {
                            node.firePropertyChangeHelper(PROP_LAYOUT_COMPONENT_VERTICAL_RESIZABLE, null, null);
                        }
                    }
                }
            });
        }

        if (constraintsProperties == null) {
            constraintsProperties = NO_PROPERTIES;
            return;
        }

        for (int i=0; i < constraintsProperties.length; i++) {
            if (constraintsProperties[i] instanceof FormProperty) {
                FormProperty prop = (FormProperty)constraintsProperties[i];

                // we suppose the constraint property is not a RADProperty...
                prop.addVetoableChangeListener(getConstraintsListener());
                prop.addPropertyChangeListener(getConstraintsListener());
                prop.addValueConvertor(getConstraintsListener());

                prop.setPropertyContext(new FormPropertyContext.Component(this));

                if (isReadOnly() || !isValid()) {
                    int type = prop.getAccessType() | FormProperty.NO_WRITE;
                    prop.setAccessType(type);
                }
                nameToProperty.put(prop.getName(), prop);
            }
        }
    }

    private ConstraintsListenerConvertor getConstraintsListener() {
        if (constraintsListener == null)
            constraintsListener = new ConstraintsListenerConvertor();
        return constraintsListener;
    }

    private class ConstraintsListenerConvertor implements VetoableChangeListener,
                             PropertyChangeListener, FormProperty.ValueConvertor
    {
        public void vetoableChange(PropertyChangeEvent ev)
            throws PropertyVetoException
        {
            Object source = ev.getSource();
            String eventName = ev.getPropertyName();
            if (source instanceof FormProperty
                && (FormProperty.PROP_VALUE.equals(eventName)
                    || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName)))
            {
                resourcePropertyChanged(ev);

                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints,
                                             ((FormProperty)source).getName(),
                                             ev.getOldValue(),
                                             ev.getNewValue());

                layoutSupport.componentLayoutChanged(index, ev);
            }
        }

        public void propertyChange(PropertyChangeEvent ev) {
            Object source = ev.getSource();
            if (source instanceof FormProperty
                && FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
            {
                LayoutSupportManager layoutSupport = getParentLayoutSupport();
                int index = getComponentIndex();
                LayoutConstraints constraints =
                    layoutSupport.getConstraints(index);

                ev = new PropertyChangeEvent(constraints, null, null, null);

                try {
                    layoutSupport.componentLayoutChanged(index, ev);
                }
                catch (PropertyVetoException ex) {} // should not happen
            }
        }

        public Object convert(Object value, FormProperty property) {
            return resourcePropertyConvert(value, property);
        }
    }

    // ----------
    // accessibility properties

    public FormProperty[] getAccessibilityProperties() {
        if (accessibilityProperties == null)
            createAccessibilityProperties();
        return accessibilityProperties;
    }

    private void createAccessibilityProperties() {
        Object comp = getBeanInstance();
        if (comp instanceof Accessible
            && ((Accessible)comp).getAccessibleContext() != null)
        {
            if (accessibilityData == null)
                accessibilityData = new MetaAccessibleContext();
            accessibilityProperties = accessibilityData.getProperties();

            for (int i=0; i < accessibilityProperties.length; i++) {
                FormProperty prop = accessibilityProperties[i];
                setPropertyListener(prop);
                prop.setPropertyContext(new FormPropertyContext.Component(this));
                nameToProperty.put(prop.getName(), prop);
            }
        }
        else {
            accessibilityData = null;
            accessibilityProperties = NO_PROPERTIES;
        }
    }

    private class MetaAccessibleContext {
        private Object accName = BeanSupport.NO_VALUE;
        private Object accDescription = BeanSupport.NO_VALUE;
        private Object accParent = BeanSupport.NO_VALUE;

        private FormProperty[] properties;

        FormProperty[] getProperties() {
            if (properties == null) {
                properties = new FormProperty[] {
                    new FormProperty(
                        "AccessibleContext.accessibleName", // NOI18N
                        String.class,
                        FormUtils.getBundleString("PROP_AccessibleName"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleName")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return accName != BeanSupport.NO_VALUE ?
                                       accName : getDefaultValue();
                        }
                        public void setTargetValue(Object value) {
                            accName = (String) value;
                        }
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        public Object getDefaultValue() {
                            return getAccessibleContext().getAccessibleName();
                        }
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accName = BeanSupport.NO_VALUE;
                        }
                        String getPartialSetterCode(String javaInitStr) {
                            return "getAccessibleContext().setAccessibleName(" // NOI18N
                                   + javaInitStr + ")"; // NOI18N
                        }
                    },

                    new FormProperty(
                        "AccessibleContext.accessibleDescription", // NOI18N
                        String.class,
                        FormUtils.getBundleString("PROP_AccessibleDescription"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleDescription")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return accDescription != BeanSupport.NO_VALUE ?
                                       accDescription : getDefaultValue();
                        }
                        public void setTargetValue(Object value) {
                            accDescription = (String) value;
                        }
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        public Object getDefaultValue() {
                            AccessibleContext context = getAccessibleContext();
                            Object bean = getBeanInstance();
                            if (bean instanceof JComponent) {
                                Object o = ((JComponent)bean).getClientProperty("labeledBy"); // NOI18N
                                if (o instanceof Accessible) {
                                    AccessibleContext ac = ((Accessible) o).getAccessibleContext();
                                    if (ac == context) {
                                        return FormUtils.getBundleString("MSG_CyclicAccessibleContext"); // NOI18N
                                    }
                                }
                            }
                            return context.getAccessibleDescription();
                        }
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accDescription = BeanSupport.NO_VALUE;
                        }
                        String getPartialSetterCode(String javaInitStr) {
                            return
                              "getAccessibleContext().setAccessibleDescription(" // NOI18N
                              + javaInitStr + ")"; // NOI18N
                        }
                    },

                    new FormProperty(
                        "AccessibleContext.accessibleParent", // NOI18N
                        Accessible.class,
                        FormUtils.getBundleString("PROP_AccessibleParent"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleParent")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return accParent != BeanSupport.NO_VALUE ?
                                       accParent : getDefaultValue();
                        }
                        public void setTargetValue(Object value) {
                            accParent = value;
                        }
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        public Object getDefaultValue() {
                            Object acP = getAccessibleContext()
                                             .getAccessibleParent();
                            if (acP != null) {
                                RADVisualContainer metacont = getParentContainer();
                                if (metacont != null) {
                                    Object cont = metacont.getContainerDelegate(
                                                    metacont.getBeanInstance());
                                    if (cont == acP)
                                        return metacont;
                                }
                            }
                            return acP;
                        }
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accParent = BeanSupport.NO_VALUE;
                        }
                        public PropertyEditor getExpliciteEditor() {
                            return new AccessibleParentEditor();
                        }
                        String getPartialSetterCode(String javaInitStr) {
                            return javaInitStr == null ? null :
                                "getAccessibleContext().setAccessibleParent(" // NOI18N
                                + javaInitStr + ")"; // NOI18N
                        }
                    }
                };
            }
            return properties;
        }

        private AccessibleContext getAccessibleContext() {
            return ((Accessible)getBeanInstance()).getAccessibleContext();
        }
    }

    public static class AccessibleParentEditor extends ComponentChooserEditor {
        public AccessibleParentEditor() {
            super();
            setBeanTypes(new Class[] { Accessible.class });
        }
    }
    
    /**
     * Preferred size of the component in the layout.
     */
    private class LayoutComponentSizeProperty extends PropertySupport.ReadWrite {
        private LayoutComponent component;
        private int dimension;
        
        private LayoutComponentSizeProperty(LayoutComponent component, int dimension) {
            super(dimension == LayoutConstants.HORIZONTAL ? PROP_LAYOUT_COMPONENT_HORIZONTAL_SIZE
                : PROP_LAYOUT_COMPONENT_VERTICAL_SIZE, Integer.class, null, null);
            boolean horizontal = dimension == LayoutConstants.HORIZONTAL;
            setDisplayName(FormUtils.getBundleString(horizontal ?
                "PROP_LAYOUT_COMPONENT_HORIZONTAL_SIZE" : "PROP_LAYOUT_COMPONENT_VERTICAL_SIZE")); // NOI18N
            setShortDescription(FormUtils.getBundleString(horizontal ?
                "HINT_LAYOUT_COMPONENT_HORIZONTAL_SIZE" : "HINT_LAYOUT_COMPONENT_VERTICAL_SIZE")); // NOI18N
            this.component = component;
            this.dimension = dimension;
            setValue("canEditAsText", Boolean.TRUE); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            LayoutModel layoutModel = getFormModel().getLayoutModel();
            LayoutInterval interval = component.getLayoutInterval(dimension);
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            try {
                layoutModel.setIntervalSize(interval, interval.getMinimumSize(false), newValue.intValue(), interval.getMaximumSize(false));
                getNodeReference().firePropertyChangeHelper(
                    getName(), oldValue, newValue);
                autoUndo = false;
            } finally {
                getFormModel().fireContainerLayoutChanged(getParentContainer(), null, null, null);
                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    getFormModel().addUndoableEdit(ue);
                }
                if (autoUndo) {
                    getFormModel().forceUndoOfCompoundEdit();
                }
            }
        }
        
        public Object getValue() {
            int size = component.getLayoutInterval(dimension).getPreferredSize(false);
            return new Integer(size);
        }

        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(new Integer(LayoutConstants.NOT_EXPLICITLY_DEFINED));
        }
        
        public boolean isDefaultValue() {
            return ((Integer)getValue()).intValue() == LayoutConstants.NOT_EXPLICITLY_DEFINED;
        }
        
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                private String notExplicitelyDefined = FormUtils.getBundleString("VALUE_SizeNotExplicitelyDefined"); // NOI18N
                
                public String[] getTags() {
                    return new String[] {notExplicitelyDefined};
                }

                public String getAsText() {
                    Integer value = (Integer)getValue();
                    if (value.intValue() == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                        return notExplicitelyDefined;
                    } else {
                        return value.toString();
                    }
                }

                public void setAsText(String str) {
                    if (notExplicitelyDefined.equals(str)) {
                        setValue(new Integer(LayoutConstants.NOT_EXPLICITLY_DEFINED));
                    } else {
                        try {
                            setValue(new Integer(Integer.parseInt(str)));
                        } 
                        catch (NumberFormatException e) {} // ignore                        
                    }
                }
            };
        }

        public boolean canWrite() {
            return !isReadOnly();
        }
    
    }
    
    /**
     * Property that determines whether the component should be resizable.
     */
    private class LayoutComponentResizableProperty extends PropertySupport.ReadWrite {
        private LayoutComponent component;
        private int dimension;
        
        private LayoutComponentResizableProperty(LayoutComponent component, int dimension) {
            super(dimension == LayoutConstants.HORIZONTAL ? PROP_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE
                : PROP_LAYOUT_COMPONENT_VERTICAL_RESIZABLE, Boolean.class, null, null);
            boolean horizontal = dimension == LayoutConstants.HORIZONTAL;
            setDisplayName(FormUtils.getBundleString(horizontal ?
                "PROP_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE" : "PROP_LAYOUT_COMPONENT_VERTICAL_RESIZABLE")); // NOI18N
            setShortDescription(FormUtils.getBundleString(horizontal ?
                "HINT_LAYOUT_COMPONENT_HORIZONTAL_RESIZABLE" : "HINT_LAYOUT_COMPONENT_VERTICAL_RESIZABLE")); // NOI18N
            this.component = component;
            this.dimension = dimension;
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            boolean resizable = newValue.booleanValue();
            LayoutModel layoutModel = getFormModel().getLayoutModel();
            LayoutInterval interval = component.getLayoutInterval(dimension);
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;
            try {
                layoutModel.setIntervalSize(interval,
                    resizable ? LayoutConstants.NOT_EXPLICITLY_DEFINED : LayoutConstants.USE_PREFERRED_SIZE,
                    interval.getPreferredSize(false),
                    resizable ? Short.MAX_VALUE : LayoutConstants.USE_PREFERRED_SIZE);
                getNodeReference().firePropertyChangeHelper(
                    getName(), oldValue, newValue);                
                autoUndo = false;
            } finally {
                getFormModel().fireContainerLayoutChanged(getParentContainer(), null, null, null);
                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    getFormModel().addUndoableEdit(ue);
                }
                if (autoUndo) {
                    getFormModel().forceUndoOfCompoundEdit();
                }
            }
        }
        
        public Object getValue() {
            int pref = component.getLayoutInterval(dimension).getPreferredSize(false);
            int max = component.getLayoutInterval(dimension).getMaximumSize(false);
            return Boolean.valueOf((max != pref) && (max != LayoutConstants.USE_PREFERRED_SIZE));
        }

        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(Boolean.FALSE);
        }
        
        public boolean isDefaultValue() {
            return getValue().equals(Boolean.FALSE);
        }

        public boolean canWrite() {
            return !isReadOnly();
        }

    }

}
