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
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import java.text.MessageFormat;

import org.openide.util.Utilities;
import org.openide.util.SharedClassObject;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.TopManager;

import org.netbeans.modules.form.compat2.border.BorderDesignSupport;

/**
 * A class that contains utility methods for the formeditor.
 * @author Ian Formanek
 */

public class FormUtils
{
    // Static variables

    // constants for CopyProperties method
    public static final int CHANGED_ONLY = 1;
    public static final int DISABLE_CHANGE_FIRING = 2;
    public static final int PASS_DESIGN_VALUES = 4;

    private static String PROP_NAME = "PropertyName"; // NOI18N
    private static final boolean debug = System.getProperty("netbeans.debug.form") != null;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = (FormLoaderSettings)
                   SharedClassObject.findObject(FormLoaderSettings.class, true);

    /** The list of all well-known heavyweight components */
    private static Class[] heavyweightComponents;
    private static HashMap jComponentIgnored;
    private static HashMap valuesCache = new HashMap();

    private static final Object CLASS_EXACTLY = new Object();
    private static final Object CLASS_AND_SUBCLASSES = new Object();
    private static final Object CLASS_AND_SWING_SUBCLASSES = new Object();

    static final Object PROP_PREFERRED = new Object();
    static final Object PROP_NORMAL = new Object();
    static final Object PROP_EXPERT = new Object();
    static final Object PROP_HIDDEN = new Object();

    private static Object[][] propsClassifications = {
        { java.awt.Container.class, CLASS_AND_SUBCLASSES,
                "layout", PROP_HIDDEN },
        { javax.swing.JComponent.class, CLASS_AND_SUBCLASSES,
                "debugGraphicsOptions", PROP_EXPERT,
                "preferredSize", PROP_NORMAL },
        { javax.swing.text.JTextComponent.class, CLASS_AND_SUBCLASSES,
                "document", PROP_PREFERRED,
                "text", PROP_PREFERRED,
                "editable", PROP_PREFERRED,
                "disabledTextColor", PROP_NORMAL,
                "selectedTextColor", PROP_NORMAL,
                "selectionColor", PROP_NORMAL,
                "caretColor", PROP_NORMAL },
        { javax.swing.JTextField.class, CLASS_AND_SUBCLASSES,
                "columns", PROP_PREFERRED },
        { javax.swing.JTextArea.class, CLASS_AND_SUBCLASSES,
                "columns", PROP_PREFERRED,
                "rows", PROP_PREFERRED,
                "lineWrap", PROP_PREFERRED,
                "wrapStyleWord", PROP_PREFERRED },
        { javax.swing.JEditorPane.class, CLASS_AND_SUBCLASSES,
                "border", PROP_PREFERRED,
                "font", PROP_PREFERRED },
        { javax.swing.JTree.class, CLASS_AND_SUBCLASSES,
                "border", PROP_PREFERRED },
        { javax.swing.AbstractButton.class, CLASS_AND_SUBCLASSES,
                "mnemonic", PROP_PREFERRED },
        { javax.swing.JToggleButton.class, CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED },
        { javax.swing.JButton.class, CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED },
        { javax.swing.JCheckBox.class, CLASS_EXACTLY,
                "icon", PROP_NORMAL },
        { javax.swing.JRadioButton.class, CLASS_EXACTLY,
                "icon", PROP_NORMAL },
        { javax.swing.JCheckBoxMenuItem.class, CLASS_AND_SUBCLASSES,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED },
        { javax.swing.JRadioButtonMenuItem.class, CLASS_AND_SUBCLASSES,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED },
        { javax.swing.JTabbedPane.class, CLASS_EXACTLY,
                "selectedComponent", PROP_EXPERT },
        { javax.swing.JSplitPane.class, CLASS_AND_SUBCLASSES,
                "dividerLocation", PROP_PREFERRED,
                "dividerSize", PROP_PREFERRED,
                "orientation", PROP_PREFERRED,
                "resizeWeight", PROP_PREFERRED },
        { javax.swing.JSplitPane.class, CLASS_EXACTLY,
                "leftComponent", PROP_HIDDEN,
                "rightComponent", PROP_HIDDEN,
                "topComponent", PROP_HIDDEN,
                "bottomComponent", PROP_HIDDEN },
        { javax.swing.JSlider.class, CLASS_AND_SUBCLASSES,
                "majorTickSpacing", PROP_PREFERRED,
                "minorTickSpacing", PROP_PREFERRED,
                "paintLabels", PROP_PREFERRED,
                "paintTicks", PROP_PREFERRED,
                "paintTrack", PROP_PREFERRED,
                "snapToTicks", PROP_PREFERRED },
        { javax.swing.JLabel.class, CLASS_AND_SUBCLASSES,
                "horizontalAlignment", PROP_PREFERRED,
                "verticalAlignment", PROP_PREFERRED },
        { javax.swing.JList.class, CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED,
                "border", PROP_PREFERRED },
        { javax.swing.JComboBox.class, CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED },
        { javax.swing.JTable.class, CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED,
                "border", PROP_PREFERRED },
        { javax.swing.JSeparator.class, CLASS_EXACTLY,
                "font", PROP_NORMAL },
        { javax.swing.JInternalFrame.class, CLASS_AND_SUBCLASSES,
                "visible", PROP_NORMAL },
        { javax.swing.JInternalFrame.class, CLASS_EXACTLY,
                "menuBar", PROP_HIDDEN,
                "JMenuBar", PROP_HIDDEN,
                "layout", PROP_HIDDEN },
        { javax.swing.JMenu.class, CLASS_EXACTLY,
                "accelerator", PROP_HIDDEN,
                "tearOff", PROP_HIDDEN },
        { java.awt.Frame.class, CLASS_EXACTLY,
                "menuBar", PROP_HIDDEN },
        { javax.swing.JFrame.class, CLASS_AND_SUBCLASSES,
                "title", PROP_PREFERRED },
        { javax.swing.JFrame.class, CLASS_EXACTLY,
                "menuBar", PROP_HIDDEN,
                "layout", PROP_HIDDEN },
        { javax.swing.JDialog.class, CLASS_AND_SUBCLASSES,
                "title", PROP_PREFERRED },
        { javax.swing.JDialog.class, CLASS_EXACTLY,
                "layout", PROP_HIDDEN },
        { javax.swing.JMenuBar.class, CLASS_EXACTLY,
                "helpMenu", PROP_HIDDEN },
        { java.applet.Applet.class, CLASS_AND_SUBCLASSES,
                "appletContext", PROP_HIDDEN,
                "codeBase", PROP_HIDDEN,
                "documentBase", PROP_HIDDEN,
                "locale", PROP_HIDDEN }
    };

    /** The properties whose changes are ignored in JComponent subclasses */

    private static String[] jComponentIgnoredList = new String [] {
        "UI", // NOI18N
        "layout", // NOI18N
        "actionMap", // NOI18N
        "border", // NOI18N
        "model" // NOI18N
    };

    static {
        try {
            heavyweightComponents = new Class[] {
                java.awt.Button.class,
                java.awt.Canvas.class,
                java.awt.List.class,
                java.awt.Button.class,
                java.awt.Label.class,
                java.awt.TextField.class,
                java.awt.TextArea.class,
                java.awt.Checkbox.class,
                java.awt.Choice.class,
                java.awt.List.class,
                java.awt.Scrollbar.class,
                java.awt.ScrollPane.class,
                java.awt.Panel.class,
            };
        } catch (Exception e) {
            throw new InternalError("Cannot initialize AWT classes"); // NOI18N
        }

        jComponentIgnored = new HashMap(15);
        for (int i = 0; i < jComponentIgnoredList.length; i++)
            jComponentIgnored.put(jComponentIgnoredList[i], jComponentIgnoredList[i]);
    }

    // -----------------------------------------------------------------------------
    // Utility methods

    // !! not called from anywhere
    public static void notifyPropertyException(Class beanClass,
                                               String propertyName,
                                               String displayName,
                                               Throwable t,
                                               boolean reading) {
        boolean dontPrint = false;
        // if it is a subclass of Applet, we ignore InvocationTargetException on
        // codeBase, documentBase and appletContext properties

        if (java.applet.Applet.class.isAssignableFrom(beanClass))
            if ("codeBase".equals(propertyName) || // NOI18N
                "documentBase".equals(propertyName) || // NOI18N
                "appletContext".equals(propertyName)) // NOI18N
                dontPrint = true;
        if ("tearOff".equals(propertyName) || "helpMenu".equals(propertyName)) // NOI18N
            dontPrint = true;
        if (!dontPrint) {
            String fmt;
            if (reading)
                fmt = NbBundle.getBundle(FormUtils.class).getString("FMT_ERR_ReadingProperty");
            else
                fmt = NbBundle.getBundle(FormUtils.class).getString("FMT_ERR_WritingProperty");

            TopManager.getDefault().getStdOut().println(
                MessageFormat.format(fmt,
                                     new Object[] { t.getClass().getName(),
                                                    propertyName,
                                                    displayName }));
        }
    }

    //
    //
    //

    /** Utility method that tries to clone an object. Objects of explicitly
     * specified types are constructed directly, other are serialized and
     * deserialized (if not serializable exception is thrown).
     */
    public static Object cloneObject(Object o) throws CloneNotSupportedException {
        if (o == null) return null;

        if ((o instanceof Byte) ||
                 (o instanceof Short) ||
                 (o instanceof Integer) ||
                 (o instanceof Long) ||
                 (o instanceof Float) ||
                 (o instanceof Double) ||
                 (o instanceof Boolean) ||
                 (o instanceof Character) ||
                 (o instanceof String)) {
            return o; // no need to change reference
        }

        if (o instanceof Font)
            return Font.getFont(((Font)o).getAttributes());
        if (o instanceof Color)
            return new Color(((Color)o).getRGB());
        if (o instanceof Dimension)
            return new Dimension((Dimension)o);
        if (o instanceof Point)
            return new Point((Point)o);
        if (o instanceof Rectangle)
            return new Rectangle((Rectangle)o);
        if (o instanceof Insets)
            return ((Insets)o).clone();
        if (o instanceof Serializable)
            return cloneBeanInstance(o, null);

        throw new CloneNotSupportedException();
    }

    /** Utility method that tries to clone an object as a bean.
     * First - if it is serializable, then it is copied using serialization.
     * If not serializable, then all properties (taken from BeanInfo) are
     * copied (property values cloned recursively).
     */
    public static Object cloneBeanInstance(Object bean, BeanInfo bInfo)
        throws CloneNotSupportedException
    {
        if (bean == null)
            return null;

        if (bean instanceof Serializable) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(bean);
                oos.close();

                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//                ObjectInputStream ois = new ObjectInputStream(bais);
                return new OIS(bais).readObject();
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    System.err.println("[WARNING] Cannot clone "+bean.getClass().getName()); // NOI18N
                    ex.printStackTrace();
                }
                throw new CloneNotSupportedException();
            }
        }

        // object is not Serializable
        Object clone;
        try {
            clone = CreationFactory.createDefaultInstance(bean.getClass());
            if (clone == null)
                throw new CloneNotSupportedException();

            if (bInfo == null) {
                try {
                    bInfo = Utilities.getBeanInfo(bean.getClass());
                } 
                catch (IntrospectionException e) {
                    throw new CloneNotSupportedException(e.getMessage());
                }
            }
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            throw new CloneNotSupportedException(ex.getMessage());
        }

        // default instance successfully created, now copy properties
        PropertyDescriptor[] pds = bInfo.getPropertyDescriptors();
        for (int i=0; i < pds.length; i++) {
            Method getter = pds[i].getReadMethod();
            Method setter = pds[i].getWriteMethod();
            if (getter != null && setter != null) {
                Object propertyValue;
                try {
                    propertyValue = getter.invoke(bean, new Object[0]);
                }
                catch (Exception e1) { // ignore - do not copy this property
                    continue;
                }
                try {
                    propertyValue = cloneObject(propertyValue);
                }
                catch (Exception e2) { // ignore - do not clone property value
                }
                try {
                    setter.invoke(clone, new Object[] {  });
                }
                catch (Exception e3) { // ignore - do not copy this property
                }
            }
        }

        return clone;
    }

    /** This method provides copying of property values from one array of
     * properties to another. The arrays need not be equally sorted. It is
     * recommended to use arrays of FormProperty, for which the mode parameter
     * can be used to specify some options (using bit flags):
     * CHANGED_ONLY (to copy only values of changed properties),
     * DISABLE_CHANGE_FIRING (to disable firing of changes in target properties),
     * PASS_DESIGN_VALUES (to pass the same FormDesignValue instances if they
     *                     cannot or should not be copied),
     */
    public static void copyProperties(Node.Property[] sourceProperties,
                                      Node.Property[] targetProperties,
                                      int mode)
    {
        for (int i=0; i < sourceProperties.length; i++) {
            Node.Property snProp = sourceProperties[i];
            FormProperty sfProp = snProp instanceof FormProperty ?
                                    (FormProperty)snProp : null;

            if (sfProp != null
                    && (mode & CHANGED_ONLY) != 0
                    && !sfProp.isChanged())
                continue; // copy only changed properties

            // find target property
            Node.Property tnProp = targetProperties[i];
            if (!tnProp.getName().equals(snProp.getName())) {
                int j;
                for (j=0; j < targetProperties.length; j++) {
                    tnProp = targetProperties[i];
                    if (tnProp.getName().equals(snProp.getName()))
                        break;
                }
                if (j == targetProperties.length)
                    continue; // not found
            }
            FormProperty tfProp = tnProp instanceof FormProperty ?
                                    (FormProperty)tnProp : null;

            try {
                // get and clone property value
                Object propertyValue = snProp.getValue();
                if (!(propertyValue instanceof FormDesignValue)) {
                    try { // clone common property value
                        propertyValue = FormUtils.cloneObject(propertyValue);
                    }
                    catch (CloneNotSupportedException ex) { } // ignore
                }
                else { // handle FormDesignValue
                    Object val = copyFormDesignValue((FormDesignValue)
                                                     propertyValue);
                    if (val != null)
                        propertyValue = val;
                    else if ((mode & PASS_DESIGN_VALUES) == 0)
                        continue; // cannot just pass the same design value
                }

                // set property value
                if (tfProp != null) {
                    boolean firing = tfProp.isChangeFiring();
                    tfProp.setChangeFiring((mode & DISABLE_CHANGE_FIRING) == 0);
                    tfProp.setValue(propertyValue);
                    tfProp.setChangeFiring(firing);
                }
                else tnProp.setValue(propertyValue);

                if (sfProp != null && tfProp != null) {
                    // also clone current PropertyEditor
                    PropertyEditor sPrEd = sfProp.getCurrentEditor();
                    PropertyEditor tPrEd = tfProp.getCurrentEditor();
                    if (sPrEd != null
                        && (tPrEd == null 
                            || sPrEd.getClass() != tPrEd.getClass()))
                    {
                        tPrEd = sPrEd instanceof RADConnectionPropertyEditor ?
                            new RADConnectionPropertyEditor(tfProp.getValueType()) :
                            (PropertyEditor)CreationFactory.createDefaultInstance(
                                                             sPrEd.getClass());
                        tfProp.setCurrentEditor(tPrEd);
                    }
                }
            }
            catch (Exception ex) { // ignore
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
            }
        }
    }

    public static void copyPropertiesToBean(RADProperty[] props,
                                            Object targetBean,
                                            Collection relativeProperties) {
        for (int i = 0; i < props.length; i++) {
            RADProperty prop = props[i];
            if (!prop.isChanged())
                continue;

            Method writeMethod = prop.getPropertyDescriptor().getWriteMethod();
            if (writeMethod == null || !writeMethod.getDeclaringClass()
                                   .isAssignableFrom(targetBean.getClass()))
                continue;

            try {
                Object value = prop.getRealValue();
                if (value == FormDesignValue.IGNORED_VALUE)
                    continue; // ignore this value, as it is not a real value

                if (value instanceof RADComponent
                        && relativeProperties != null)
                    relativeProperties.add(prop);

                value = FormUtils.cloneObject(value);
                writeMethod.invoke(targetBean, new Object[] { value });
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
            }
        }
    }

    static Object copyFormDesignValue(FormDesignValue value) {
        if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
            RADConnectionPropertyEditor.RADConnectionDesignValue radValue =
                (RADConnectionPropertyEditor.RADConnectionDesignValue) value;
            if (radValue.userCode != null)
                return new RADConnectionPropertyEditor.RADConnectionDesignValue(
                             radValue.userCode);
            else if (radValue.value != null)
                return new RADConnectionPropertyEditor.RADConnectionDesignValue(
                             radValue.requiredTypeName, radValue.userCode);
        }
        else if (value instanceof BorderDesignSupport) {
            try {
                return new BorderDesignSupport((BorderDesignSupport)value);
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * A utility method for checking whether specified component is heavyweight
     * or lightweight.
     * @param comp The component to check
     */

    public static boolean isHeavyweight(Component comp) {
        for (int i=0; i < heavyweightComponents.length; i++)
            if (heavyweightComponents[i].isAssignableFrom(comp.getClass()))
                return true;
        return false;
    }

    public static boolean isIgnoredProperty(Class beanClass, String propertyName) {
        if (JComponent.class.isAssignableFrom(beanClass)) {
            if (jComponentIgnored.get(propertyName) != null)
                return true;
        }
        if (javax.swing.JDesktopPane.class.isAssignableFrom(beanClass) && "desktopManager".equals(propertyName)) // NOI18N
            return true;
        if (javax.swing.JInternalFrame.class.isAssignableFrom(beanClass) && "menuBar".equals(propertyName)) // NOI18N
            return true;
        return false;
    }

    // ---------

    public static boolean isContainer(Class beanClass) {
        if (beanClass == null || !Container.class.isAssignableFrom(beanClass))
            return false;

        Map containerBeans = formSettings.getContainerBeans();
        Boolean isContReg = containerBeans == null ? null :
                            getRegisteredIsContainer(beanClass, containerBeans);
        boolean isCont;

        if (isContReg != null)
            isCont = isContReg.booleanValue();
        else { // not registered, find "isContainer" attribute in BeanDescriptor
            Object isContainerValue = null;
            try {
                BeanDescriptor desc = Utilities.getBeanInfo(beanClass)
                                                    .getBeanDescriptor();
                if (desc != null)
                   isContainerValue = desc.getValue("isContainer"); // NOI18N
            }
            catch (IntrospectionException ex) { // ignore
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
            }

            // can be false only if the attribute is explicitly set to false
            isCont = isContainerValue instanceof Boolean ?
                         ((Boolean)isContainerValue).booleanValue() : true;

            setIsContainer(beanClass, isCont);
        }

        return isCont;
    }

    public static void setIsContainer(Class beanClass, boolean isContainer) {
        Map containerBeans = formSettings.getContainerBeans();
        if (beanClass == null)
            return;

        if (containerBeans == null) {
            containerBeans = new HashMap();
            formSettings.setContainerBeans(containerBeans);
        }

        containerBeans.put(beanClass.getName(), new Boolean(isContainer));
    }

    public static void removeIsContainerRegistration(Class beanClass) {
        Map containerBeans = formSettings.getContainerBeans();
        if (containerBeans == null || beanClass == null)
            return;

        containerBeans.remove(beanClass.getName());

        if (containerBeans.isEmpty())
            formSettings.setContainerBeans(null);
    }

    private static Boolean getRegisteredIsContainer(Class beanClass, Map map) {
        Object val = map.get(beanClass.getName());
        if (val instanceof Boolean)
            return (Boolean) val;

        if (beanClass.isAssignableFrom(Container.class))
            return null;

        beanClass = beanClass.getSuperclass();
        return getRegisteredIsContainer(beanClass, map);
    }

    // ---------

    /** Returns explicit changes in properties classification (preferred, normal,
     * expert). Used for SWING components to correct default (insufficient)
     * classification taken from BeanInfo.
     */
    static Object[] getPropertiesClassification(BeanInfo beanInfo) {
        ArrayList reClsf = null;
        Class beanClass = beanInfo.getBeanDescriptor().getBeanClass();

        if (javax.swing.JComponent.class.isAssignableFrom(beanClass)) {
            reClsf = new ArrayList(8);
            Object isContainerValue = beanInfo.getBeanDescriptor().getValue("isContainer"); // NOI18N
            if (isContainerValue == null || Boolean.TRUE.equals(isContainerValue)) {
                reClsf.add("font"); // NOI18N
                reClsf.add(PROP_NORMAL);
            }
            else {
                reClsf.add("border"); // NOI18N
                reClsf.add(PROP_NORMAL); // NOI18N
            }
        }

        for (int i=0; i < propsClassifications.length; i++) {
            Object[] clsf = propsClassifications[i];
            Class refClass = (Class)clsf[0];
            Object subclasses = clsf[1];

            if ((subclasses == CLASS_EXACTLY && refClass == beanClass)
                ||
                (subclasses == CLASS_AND_SUBCLASSES
                         && refClass.isAssignableFrom(beanClass))
                ||
                (subclasses == CLASS_AND_SWING_SUBCLASSES
                         && refClass.isAssignableFrom(beanClass)
                         && beanClass.getName().startsWith("javax.swing."))) {
                if (reClsf == null)
                    reClsf = new ArrayList(8);
                for (int j=2; j < clsf.length; j++)
                    reClsf.add(clsf[j]);
            }
        }

        if (reClsf != null) {
            Object[] clsfArray = new Object[reClsf.size()];
            reClsf.toArray(clsfArray);
            return clsfArray;
        }
        return null;
    }

    /** Returns type of property (PROP_PREFERRED, PROP_NORMAL, PROP_EXPERT or
     * PROP_HIDDEN) based on PropertyDescriptor and explicit changes in
     * properties classification for given bean class (returned from
     * getPropertiesClassification(BeanInfo beanInfo) method).
     */
    static Object getPropertyType(PropertyDescriptor pd,
                                  Object[] propsClsf) {
        if (propsClsf != null) {
            String propName = pd.getName();

            int i = propsClsf.length;
            while (i > 0) {
                if (propsClsf[i-2].equals(propName))
                    return propsClsf[i-1];
                i -= 2;
            }
        }

        if (pd.isHidden())
            return PROP_HIDDEN;
        if (pd.isExpert())
            return PROP_EXPERT;
        if (pd.isPreferred() || Boolean.TRUE.equals(pd.getValue("preferred"))) // NOI18N
            return PROP_PREFERRED;
        return PROP_NORMAL;
    }

    static boolean isContainerContentDependentProperty(Class beanClass,
                                                       String propName) {
        return "selectedIndex".equals(propName) // NOI18N
               && javax.swing.JTabbedPane.class.isAssignableFrom(beanClass);
    }

    /** @return a default name for event handling method - it is a concatenation of
     * the component name and the name of the listener method(with first letter capital)
     *(e.g. button1MouseReleased).
     */
    public static String getDefaultEventName(RADComponent component, Method listenerMethod) {
        String componentName = component.getName();
        if (component == component.getFormModel().getTopRADComponent()) {
            componentName = "form"; // NOI18N
        }
        return getDefaultEventName(componentName, listenerMethod);
    }

    static String getDefaultEventName(String name, Method listenerMethod) {
        StringBuffer sb = new StringBuffer(name);
        String lm = listenerMethod.getName();
        sb.append(lm.substring(0, 1).toUpperCase());
        sb.append(lm.substring(1));
        return sb.toString();
    }

    /** @return a formatted name of specified method
     */
    public static String getMethodName(MethodDescriptor desc) {
        StringBuffer sb = new StringBuffer(desc.getName());
        Class[] params = desc.getMethod().getParameterTypes();
        if ((params == null) ||(params.length == 0)) {
            sb.append("()"); // NOI18N
        } else {
            for (int i = 0; i < params.length; i++) {
                if (i == 0) sb.append("("); // NOI18N
                else sb.append(", "); // NOI18N
                sb.append(Utilities.getShortClassName(params[i]));
            }
            sb.append(")"); // NOI18N
        }

        return sb.toString();
    }

    // -----------------------------------------------------------------------------
    // Visual utility methods

    /**
     * This method converts array of Rectangles(with compoment bounds) to
     * GridBagConstraints.
     *
     * Some bug(properly in GridBagLayout) appeares:
     *
     *  here will be bad size!!!!
     *               |
     *               V
     * mmmm  mmmm  mmmm  mmmm
     *         mmmmm
     */
    public static GridBagConstraints[] convertToConstraints(Rectangle[] r, Component[] com) {
        int i, k = r.length;
        GridBagConstraints[] c = new GridBagConstraints[k];
        for (i = 0; i < k; i++) {
            c [i] = new GridBagConstraints();
            int gx = 0, x1 = r [i].x;
            int gy = 0, y1 = r [i].y;
            int gw = 1, x2 = x1 + r [i].width;
            int gh = 1, y2 = y1 + r [i].height;
            int fromX = 0, fromY = 0;
            int j, l = r.length;
            for (j = 0; j < l; j++) {
                int xe = r [j].x + r [j].width;
                int ye = r [j].y + r [j].height;
                if (xe <= x1) {
                    gx++;
                    fromX = Math.max(fromX, xe);
                }
                if (ye <= y1) {
                    gy++;
                    fromY = Math.max(fromY, ye);
                }
                if ((xe > x1) &&(xe < x2)) gw++;
                if ((ye > y1) &&(ye < y2)) gh++;
            }
            c [i].gridx = gx;
            c [i].gridy = gy;
            c [i].gridwidth = gw;
            c [i].gridheight = gh;
            c [i].insets = new Insets(y1 - fromY, x1 - fromX, 0, 0);
            c [i].fill = GridBagConstraints.BOTH;
            c [i].ipadx =(r [i].width - com [i].getPreferredSize().width);
            c [i].ipady =(r [i].height - com [i].getPreferredSize().height);
        }
        return c;
    }

    /**
     * Translates specified event so that the source component of the new event
     * is the original event's source component's parent and the coordinates of
     * the event are translated appropriately to the parent's coordinate space.
     *
     * @param evt the MouseEvent to be translated
     * @param parent the parent component
     * @exception IllegalArgumentException is thrown if the source component
     *    of the MouseEvent is not a subcomponent of given parent component
     */
    static MouseEvent translateMouseEventToParent(MouseEvent evt, Component parent) {
        if (parent == null)
            return evt;

        Component comp = evt.getComponent();
        while (!parent.equals(comp)) {
            if (comp instanceof JComponent)
                evt.translatePoint(((JComponent)comp).getX(),
                                   ((JComponent)comp).getY());
            else {
                Rectangle bounds = comp.getBounds();
                evt.translatePoint(bounds.x, bounds.y);
            }
            comp = comp.getParent();
            if (comp == null) {
                System.err.println("Component: "+evt.getSource()+" is not under its parent's container: "+parent); // NOI18N
                break;
            }
        }

        return new MouseEvent(parent,
                              evt.getID(),
                              evt.getWhen(),
                              evt.getModifiers(),
                              evt.getX(),
                              evt.getY(),
                              evt.getClickCount(),
                              evt.isPopupTrigger()
                              );
    }

    // -----------------------------------------------------------------------------
    // DEBUG utilities

    public static void DEBUG() {
        if (debug) {
            Thread.dumpStack();
        }
    }

    public static void DEBUG(String s) {
        if (debug) {
            System.out.println(s);
        }
    }

    /** A utility method which looks for the first common ancestor of the
     * classes specified. The ancestor is either one of the two classes, if one of them extends the other
     * or their first superclass which is common to both.
     * @param cl1 the first class
     * @param cl2 the first class
     * @return class which is superclass of both classes provided, or null if the first common superclass is java.lang.Object]
     */
    public Class findCommonAncestor(Class cl1, Class cl2) {
        // handle direct inheritance
        if (cl1.isAssignableFrom(cl2)) return cl1; // cl2 is subclass of cl1
        if (cl2.isAssignableFrom(cl1)) return cl2; // cl1 is subclass of cl2

        ArrayList cl1Ancestors = new ArrayList(8);
        ArrayList cl2Ancestors = new ArrayList(8);
        Class cl1An = cl1.getSuperclass();
        Class cl2An = cl2.getSuperclass();
        while (cl1An != null) {
            cl1Ancestors.add(cl1An);
            cl1An = cl1An.getSuperclass();
        }
        while (cl2An != null) {
            cl2Ancestors.add(cl2An);
            cl2An = cl2An.getSuperclass();
        }
        if (cl2Ancestors.size() > cl1Ancestors.size()) {
            ArrayList temp = cl1Ancestors;
            cl1Ancestors = cl2Ancestors;
            cl2Ancestors = temp;
        }
        // cl1Ancestors is now the longer stack of classes,
        // i.e. it must contain the first common superclass
        Class foundClass = null;
        for (Iterator it = cl1Ancestors.iterator(); it.hasNext();) {
            Object o = it.next();
            if (cl2Ancestors.contains(o)) {
                foundClass =(Class)o;
                break;
            }
        }
        if (foundClass.equals(Object.class)) {
            foundClass = null; // if Object is the first common superclass, null is returned
        }
        return foundClass;
    }

    /** A utility method which looks for the first common ancestor of the
     * classes specified. The ancestor is either one of the two classes, if one of them extends the other
     * or their first superclass which is common to both.
     * The stopClass parameter can be used to limit the superclass to be found to be instance
     * @param cl1 the first class
     * @param cl2 the first class
     * @param stopClass a class to limit the results to, i.e. a result returned is either subclass of the stopClass or null. 
     *                  If the stopClass is null, the result is not limited to any particular class
     * @return class which is superclass of both classes provided, or null if the first common superclass is not inmstance of stopClass]
     */
    public Class findCommonAncestor(Class cl1, Class cl2, Class stopClass) {
        Class cl = findCommonAncestor(cl1, cl2);
        if (stopClass == null) return cl;
        if ((cl == null) ||(!(stopClass.isAssignableFrom(cl)))) return null;
        return cl;
    }

    // -----------------------------------------------------------------------------
    // XML utilities

    /** Read property from XML node.
     * @param propName name of property
     * @param propClass class of property(to find editor)
     * @param element XML element representing the property
     * @return value of property created from XML element
     */
    public static Object readProperty(String propName, Class propClass, org.w3c.dom.Node element) throws java.io.IOException {
        org.w3c.dom.NodeList items = element.getChildNodes();
        Object result = null;
        if (items.getLength() >0) {
            PropertyEditor propEdit = FormPropertyEditorManager.findEditor(propClass);
            for (int i=0, n=items.getLength();i<n; i++){
                if (items.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
                    ((org.w3c.dom.Element) items.item(i)).getAttribute(PROP_NAME).equals(propName)) {
                    ((XMLPropertyEditor) propEdit).readFromXML(items.item(i));
                    result = propEdit.getValue();
                }
            }
        }
        if (result == null) {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            if (attributes != null) {
                org.w3c.dom.Node attr = attributes.getNamedItem(propName);
                if (attr!=null) {
                    String valueText = attr.getNodeValue();
                    if (valueText != null){
                        result = GandalfPersistenceManager.decodeValue(valueText);
                    }
                }
            }
        }
        return result;
    }

    /** Write information about Color into XML element.
     * @param propName name of property
     * @param value value of property
     * @param propClass class of property(to find editor)
     * @param element XML element to write to
     * @param doc the whole XML document
     */
    public static void writeProperty(String propName, Object value, Class propClass, org.w3c.dom.Element el,org.w3c.dom.Document doc) {
        boolean written = false;
        PropertyEditor propEdit = FormPropertyEditorManager.findEditor(propClass);
        org.w3c.dom.Node valueNode = null;
        if (propEdit instanceof XMLPropertyEditor) {
            propEdit.setValue(value);
            valueNode =((XMLPropertyEditor) propEdit).storeToXML(doc);
            if (valueNode != null) {
                el.appendChild(valueNode);
                if (valueNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    ((org.w3c.dom.Element) valueNode).setAttribute(PROP_NAME, propName);
                }
                written = true;
            }
        }
        if (!written) {
            String encodedSerializeValue = GandalfPersistenceManager.encodeValue(value);
            if (encodedSerializeValue != null) {
                el.setAttribute(propName, encodedSerializeValue);
            } else {
                // [PENDING - notify problem?]
            }
        }
    }

    // ---------

    private static class OIS extends ObjectInputStream {
        public OIS(InputStream is) throws IOException {
            super(is);
        }

        protected Class resolveClass(ObjectStreamClass streamCls)
            throws IOException, ClassNotFoundException
        {
            return TopManager.getDefault().currentClassLoader().loadClass(
                                                       streamCls.getName());
        }
    }
}
