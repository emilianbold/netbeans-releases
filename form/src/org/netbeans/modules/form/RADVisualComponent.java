/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
    private Map<String,LayoutConstraints> constraints = new HashMap<String,LayoutConstraints>();
//    transient private RADVisualContainer parent;

    private Node.Property[] constraintsProperties;
    private ConstraintsListenerConvertor constraintsListener;

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

    /**
     * Sets component's constraints description for given layout-support class. 
     * 
     * @param layoutDelegateClass class of the layout delegate these constraints belong to.
     * @param constr layout constraints.
     */
    public void setLayoutConstraints(Class layoutDelegateClass,
                                     LayoutConstraints constr)
    {
        if (constr != null) {
            constraints.put(layoutDelegateClass.getName(), constr);
        }
    }

    /**
     * Gets component's constraints description for given layout-support class.
     * 
     * @param layoutDelegateClass class of the layout delegate.
     * @return layout constraints for the given layout delegate.
     */
    public LayoutConstraints getLayoutConstraints(Class layoutDelegateClass) {
        return constraints.get(layoutDelegateClass.getName());
    }

    Map<String,LayoutConstraints> getConstraintsMap() {
        return constraints;
    }

    void setConstraintsMap(Map<String,LayoutConstraints> map) {
        constraints.putAll(map);
    }

    // ---------------
    // Properties

    @Override
    protected void createPropertySets(List<Node.PropertySet> propSets) {
        super.createPropertySets(propSets);
        if (SUPPRESS_PROPERTY_TABS) {
            return;
        }

        if (constraintsProperties == null)
            createConstraintsProperties();

        if (constraintsProperties != null && constraintsProperties.length > 0)
            propSets.add(propSets.size() - 1,
                         new Node.PropertySet("layout", // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTab"), // NOI18N
                    FormUtils.getBundleString("CTL_LayoutTabHint")) // NOI18N
            {
                @Override
                public Node.Property[] getProperties() {
                    Node.Property[] props = getConstraintsProperties();
                    return (props == null) ? NO_PROPERTIES : props;
                }
            });

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

    @Override
    protected void clearProperties() {
        super.clearProperties();
        constraintsProperties = null;
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
            // Issue 154824 - do not create layout properties for menu-bar
            if (getParentContainer().isLayoutSubcomponent(this)) {
                LayoutConstraints constr = layoutSupport.getConstraints(this);
                if (constr != null) {
                    constraintsProperties = constr.getProperties();
                }
            }
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
                @Override
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
        @Override
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

        @Override
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

        @Override
        public Object convert(Object value, FormProperty property) {
            return resourcePropertyConvert(value, property);
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
            
        @Override
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
        
        @Override
        public Object getValue() {
            int size = component.getLayoutInterval(dimension).getPreferredSize(false);
            return new Integer(size);
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }
        
        @Override
        public void restoreDefaultValue() {
            setValue(new Integer(LayoutConstants.NOT_EXPLICITLY_DEFINED));
        }
        
        @Override
        public boolean isDefaultValue() {
            return ((Integer)getValue()).intValue() == LayoutConstants.NOT_EXPLICITLY_DEFINED;
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                private String notExplicitelyDefined = FormUtils.getBundleString("VALUE_SizeNotExplicitelyDefined"); // NOI18N
                
                @Override
                public String[] getTags() {
                    return new String[] {notExplicitelyDefined};
                }

                @Override
                public String getAsText() {
                    Integer value = (Integer)getValue();
                    if (value.intValue() == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                        return notExplicitelyDefined;
                    } else {
                        return value.toString();
                    }
                }

                @Override
                public void setAsText(String str) {
                    if (notExplicitelyDefined.equals(str)) {
                        setValue(new Integer(LayoutConstants.NOT_EXPLICITLY_DEFINED));
                    } else {
                        try {
                            int size = Integer.parseInt(str);
                            if (size < 0) {
                                throw new IllegalArgumentException();
                            }
                            setValue(size);
                        }  catch (NumberFormatException e) {
                            throw new IllegalArgumentException();
                        }
                    }
                }
            };
        }

        @Override
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
            
        @Override
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
        
        @Override
        public Object getValue() {
            int pref = component.getLayoutInterval(dimension).getPreferredSize(false);
            int max = component.getLayoutInterval(dimension).getMaximumSize(false);
            return Boolean.valueOf((max != pref) && (max != LayoutConstants.USE_PREFERRED_SIZE));
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }
        
        @Override
        public void restoreDefaultValue() {
            setValue(Boolean.FALSE);
        }
        
        @Override
        public boolean isDefaultValue() {
            return getValue().equals(Boolean.FALSE);
        }

        @Override
        public boolean canWrite() {
            return !isReadOnly();
        }

    }

}
