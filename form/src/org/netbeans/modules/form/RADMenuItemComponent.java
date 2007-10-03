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

package org.netbeans.modules.form;

import java.awt.*;

/**
 * RADMenuItemComponent represents one menu item component in the Form.
 *
 * @author Petr Hamernik, Ian Formanek
 */

public class RADMenuItemComponent extends RADComponent {

//    private MetaAccessibleContext accessibilityData;
//    private FormProperty[] accessibilityProperties;

    /** Type of menu */
    private int type;

    /** Possible constants for type variable */
    static final int T_MENUBAR              = 0x01110;
    static final int T_MENUITEM             = 0x00011;
    static final int T_CHECKBOXMENUITEM     = 0x00012;
    static final int T_MENU                 = 0x00113;
    static final int T_POPUPMENU            = 0x01114;

//    static final int T_JPOPUPMENU           = 0x01125;
//    static final int T_JMENUBAR             = 0x01126;
//    static final int T_JMENUITEM            = 0x00027;
//    static final int T_JCHECKBOXMENUITEM    = 0x00028;
//    static final int T_JMENU                = 0x00129;
//    static final int T_JRADIOBUTTONMENUITEM = 0x0002A;

    static final int T_SEPARATOR            = 0x1001B;
//    static final int T_JSEPARATOR           = 0x1002C;

    /** Masks for the T_XXX constants */
    static final int MASK_AWT               = 0x00010;
//    static final int MASK_SWING             = 0x00020;
    static final int MASK_CONTAINER         = 0x00100;
    static final int MASK_ROOT              = 0x01000;
    static final int MASK_SEPARATOR         = 0x10000;


    public Object initInstance(Class beanClass) throws Exception {
        type = recognizeType(beanClass);
        return super.initInstance(beanClass);
    }

    protected org.openide.nodes.Node.Property[] createSyntheticProperties() {
        // no synthetic properties for AWT Separator
        if (type == T_SEPARATOR)
            return RADComponent.NO_PROPERTIES;
        else
            return super.createSyntheticProperties();
    }

    int getMenuItemType() {
        return type;
    }

    /** Recognizes type of the menu from its class.
     * @return adequate T_XXX constant
     */
    static int recognizeType(Class cl) {
//        if (JSeparator.class.isAssignableFrom(cl))
//            return T_JSEPARATOR;
        if (org.netbeans.modules.form.Separator.class.isAssignableFrom(cl))
            return T_SEPARATOR;
        if (PopupMenu.class.isAssignableFrom(cl))
            return T_POPUPMENU;
        if (Menu.class.isAssignableFrom(cl))
            return  T_MENU;
        if (CheckboxMenuItem.class.isAssignableFrom(cl))
            return T_CHECKBOXMENUITEM;
        if (MenuItem.class.isAssignableFrom(cl))
            return  T_MENUITEM;
        if (MenuBar.class.isAssignableFrom(cl))
            return T_MENUBAR;
//        if (JRadioButtonMenuItem.class.isAssignableFrom(cl))
//            return T_JRADIOBUTTONMENUITEM;
//        if (JMenu.class.isAssignableFrom(cl))
//            return T_JMENU;
//        if (JCheckBoxMenuItem.class.isAssignableFrom(cl))
//            return T_JCHECKBOXMENUITEM;
//        if (JMenuItem.class.isAssignableFrom(cl))
//            return T_JMENUITEM;
//        if (JMenuBar.class.isAssignableFrom(cl))
//            return T_JMENUBAR;
//        if (JPopupMenu.class.isAssignableFrom(cl))
//            return T_JPOPUPMENU;

        throw new IllegalArgumentException("Cannot create RADMenuItemComponent for class: "+cl.getName()); // NOI18N
    }

    public Object cloneBeanInstance(java.util.Collection relativeProperties) {
        if (type == T_SEPARATOR)
            return null; // don't clone artificial org.netbeans.modules.form.Separator
        return super.cloneBeanInstance(relativeProperties);
    }

    /*
    // ----------
    // accessibility properties

    // [The following code duplicates lots of code from RADVisualComponent.
    // It appears not only here that the metacomponents need to be reworked...]

    protected void createPropertySets(java.util.List propSets) {
        super.createPropertySets(propSets);

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

    public Node.Property getPropertyByName(String name,
                                           Class propertyType,
                                           boolean fromAll)
    {
        if (fromAll && accessibilityProperties == null)
            createAccessibilityProperties();
        return super.getPropertyByName(name, propertyType, fromAll);
    }

    protected void clearProperties() {
        super.clearProperties();
        accessibilityData = null;
        accessibilityProperties = null;
    }

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
                            return getAccessibleContext().getAccessibleDescription();
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
                                RADComponent metacont = getParentComponent();
                                if (metacont != null
                                        && metacont.getBeanInstance() == acP)
                                    return metacont;
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
    } */
}
