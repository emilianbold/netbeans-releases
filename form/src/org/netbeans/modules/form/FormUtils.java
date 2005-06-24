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
import javax.swing.border.TitledBorder;
import java.text.MessageFormat;
import java.security.*;

import org.openide.ErrorManager;
import org.openide.util.*;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.netbeans.api.java.classpath.*;

import org.netbeans.modules.form.editors2.IconEditor;
import org.netbeans.modules.form.editors2.BorderDesignSupport;
import org.netbeans.modules.form.project.ClassPathUtils;

/**
 * A class that contains utility methods for the formeditor.
 * @author Ian Formanek
 */

public class FormUtils
{
    // constants for CopyProperties method
    public static final int CHANGED_ONLY = 1;
    public static final int DISABLE_CHANGE_FIRING = 2;
    public static final int PASS_DESIGN_VALUES = 4;

    private static final Object CLASS_EXACTLY = new Object();
    private static final Object CLASS_AND_SUBCLASSES = new Object();
    private static final Object CLASS_AND_SWING_SUBCLASSES = new Object();

    static final Object PROP_PREFERRED = new Object();
    static final Object PROP_NORMAL = new Object();
    static final Object PROP_EXPERT = new Object();
    static final Object PROP_HIDDEN = new Object();

    /** Table defining categories of properties. It overrides original Swing
     * definition from beaninfo (which is often inadequate). */
    private static Object[][] propertyCategories = {
        { java.awt.Component.class, CLASS_AND_SUBCLASSES,
                "locale", PROP_HIDDEN,
                "locationOnScreen", PROP_HIDDEN,
                "showing", PROP_HIDDEN },
        { java.awt.Component.class, CLASS_AND_SWING_SUBCLASSES,
                "accessibleContext", PROP_HIDDEN,
                "components", PROP_HIDDEN,
                "containerListeners", PROP_HIDDEN,
                "focusTraversalPolicySet", PROP_HIDDEN,
                "focusCycleRootAncestor", PROP_HIDDEN,
                "focusOwner", PROP_HIDDEN },
        { java.awt.Container.class, CLASS_AND_SUBCLASSES,
                "componentCount", PROP_HIDDEN,
                "layout", PROP_HIDDEN },
        { javax.swing.JComponent.class, CLASS_AND_SUBCLASSES,
                "debugGraphicsOptions", PROP_EXPERT,
                "preferredSize", PROP_NORMAL,
                "actionMap", PROP_HIDDEN },
        { javax.swing.JComponent.class, CLASS_AND_SWING_SUBCLASSES,
                "graphics", PROP_HIDDEN,
                "height", PROP_HIDDEN,
                "inputMap", PROP_HIDDEN,
                "maximumSizeSet", PROP_HIDDEN,
                "minimumSizeSet", PROP_HIDDEN,
                "preferredSizeSet", PROP_HIDDEN,
                "registeredKeyStrokes", PROP_HIDDEN,
                "rootPane", PROP_HIDDEN,
                "topLevelAncestor", PROP_HIDDEN,
                "validateRoot", PROP_HIDDEN,
                "visibleRect", PROP_HIDDEN,
                "width", PROP_HIDDEN,
                "x", PROP_HIDDEN,
                "y", PROP_HIDDEN,
                "ancestorListeners", PROP_HIDDEN,
                "propertyChangeListeners", PROP_HIDDEN,
                "vetoableChangeListeners", PROP_HIDDEN,
                "actionListeners", PROP_HIDDEN,
                "changeListeners", PROP_HIDDEN,
                "itemListeners", PROP_HIDDEN,
                "managingFocus", PROP_HIDDEN,
                "optimizedDrawingEnabled", PROP_HIDDEN,
                "paintingTile", PROP_HIDDEN },
        { java.awt.Window.class, CLASS_AND_SWING_SUBCLASSES,
                "focusCycleRootAncestor", PROP_HIDDEN,
                "focusOwner", PROP_HIDDEN,
                "active", PROP_HIDDEN,
                "alignmentX", PROP_HIDDEN,
                "alignmentY", PROP_HIDDEN,
                "bufferStrategy", PROP_HIDDEN,
                "focused", PROP_HIDDEN,
                "graphicsConfiguration", PROP_HIDDEN,
                "mostRecentFocusOwner", PROP_HIDDEN,
                "inputContext", PROP_HIDDEN,
                "ownedWindows", PROP_HIDDEN,
                "owner", PROP_HIDDEN,
                "windowFocusListeners", PROP_HIDDEN,
                "windowListeners", PROP_HIDDEN,
                "windowStateListeners", PROP_HIDDEN,
                "warningString", PROP_HIDDEN,
                "toolkit", PROP_HIDDEN,
                "focusableWindow", PROP_HIDDEN,
                "locationRelativeTo", PROP_HIDDEN },
        { javax.swing.text.JTextComponent.class, CLASS_AND_SUBCLASSES,
                "document", PROP_PREFERRED,
                "text", PROP_PREFERRED,
                "editable", PROP_PREFERRED,
                "disabledTextColor", PROP_NORMAL,
                "selectedTextColor", PROP_NORMAL,
                "selectionColor", PROP_NORMAL,
                "caretColor", PROP_NORMAL },
        { javax.swing.text.JTextComponent.class, CLASS_AND_SWING_SUBCLASSES,
                "actions", PROP_HIDDEN,
                "caretListeners", PROP_HIDDEN,
                "inputMethodRequests", PROP_HIDDEN },
        { javax.swing.JTextField.class, CLASS_AND_SUBCLASSES,
                "columns", PROP_PREFERRED },
        { javax.swing.JTextField.class, CLASS_AND_SWING_SUBCLASSES,
                "horizontalVisibility", PROP_HIDDEN },
        { javax.swing.JFormattedTextField.class, CLASS_EXACTLY,
                "formatter", PROP_HIDDEN },
        { javax.swing.JPasswordField.class, CLASS_EXACTLY,
                "password", PROP_HIDDEN },
        { javax.swing.JTextArea.class, CLASS_AND_SUBCLASSES,
                "columns", PROP_PREFERRED,
                "rows", PROP_PREFERRED,
                "lineWrap", PROP_PREFERRED,
                "wrapStyleWord", PROP_PREFERRED },
        { javax.swing.JEditorPane.class, CLASS_AND_SUBCLASSES,
                "border", PROP_PREFERRED,
                "font", PROP_PREFERRED },
        { javax.swing.JEditorPane.class, CLASS_AND_SWING_SUBCLASSES,
                "hyperlinkListeners", PROP_HIDDEN },
        { javax.swing.JTextPane.class, CLASS_EXACTLY,
                "characterAttributes", PROP_HIDDEN,
                "paragraphAttributes", PROP_HIDDEN },
        { javax.swing.JTree.class, CLASS_AND_SUBCLASSES,
                "border", PROP_PREFERRED },
        { javax.swing.JTree.class, CLASS_EXACTLY,
                "editing", PROP_HIDDEN,
                "editingPath", PROP_HIDDEN,
                "selectionCount", PROP_HIDDEN,
                "selectionEmpty", PROP_HIDDEN,
                "lastSelectedPathComponent", PROP_HIDDEN,
                "leadSelectionRow", PROP_HIDDEN,
                "maxSelectionRow", PROP_HIDDEN,
                "minSelectionRow", PROP_HIDDEN,
                "treeExpansionListeners", PROP_HIDDEN,
                "treeSelectionListeners", PROP_HIDDEN,
                "treeWillExpandListeners", PROP_HIDDEN },
        { javax.swing.AbstractButton.class, CLASS_AND_SUBCLASSES,
                "mnemonic", PROP_PREFERRED,
                "action", PROP_PREFERRED },
        { javax.swing.AbstractButton.class, CLASS_AND_SWING_SUBCLASSES,
                "selectedObjects", PROP_HIDDEN },
        { javax.swing.JToggleButton.class, CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED },
        { javax.swing.JButton.class, CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED,
                "defaultButton", PROP_HIDDEN },
        { javax.swing.JCheckBox.class, CLASS_EXACTLY,
                "icon", PROP_NORMAL },
        { javax.swing.JRadioButton.class, CLASS_EXACTLY,
                "icon", PROP_NORMAL },
        { javax.swing.JMenuItem.class, CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED },
        { javax.swing.JMenuItem.class, CLASS_AND_SWING_SUBCLASSES,
                "menuDragMouseListeners", PROP_HIDDEN,
                "menuKeyListeners", PROP_HIDDEN },
        { javax.swing.JCheckBoxMenuItem.class, CLASS_AND_SUBCLASSES,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED,
                "icon", PROP_NORMAL },
        { javax.swing.JRadioButtonMenuItem.class, CLASS_AND_SUBCLASSES,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED,
                "icon", PROP_NORMAL },
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
                "verticalAlignment", PROP_PREFERRED,
                "displayedMnemonic", PROP_PREFERRED,
                "labelFor", PROP_PREFERRED },
        { javax.swing.JList.class, CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED,
                "border", PROP_PREFERRED,
                "selectionMode", PROP_PREFERRED },
        { javax.swing.JComboBox.class, CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED },
        { javax.swing.JComboBox.class, CLASS_EXACTLY,
                "popupVisible", PROP_HIDDEN,
                "popupMenuListeners", PROP_HIDDEN,
                "selectedObjects", PROP_HIDDEN },
        { javax.swing.Scrollable.class, CLASS_AND_SWING_SUBCLASSES,
                "preferredScrollableViewportSize", PROP_HIDDEN,
                "scrollableTracksViewportWidth", PROP_HIDDEN,
                "scrollableTracksViewportHeight", PROP_HIDDEN },
        { javax.swing.JScrollBar.class, CLASS_EXACTLY,
                "adjustmentListeners", PROP_HIDDEN },
        { javax.swing.JTable.class, CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED,
                "border", PROP_PREFERRED },
        { javax.swing.JTable.class, CLASS_EXACTLY,
                "editing", PROP_HIDDEN,
                "editorComponent", PROP_HIDDEN,
                "selectedColumn", PROP_HIDDEN,
                "selectedColumnCount", PROP_HIDDEN,
                "selectedColumns", PROP_HIDDEN,
                "selectedRow", PROP_HIDDEN,
                "selectedRowCount", PROP_HIDDEN,
                "selectedRows", PROP_HIDDEN },
        { javax.swing.JSeparator.class, CLASS_EXACTLY,
                "font", PROP_NORMAL },
        { javax.swing.JInternalFrame.class, CLASS_AND_SUBCLASSES,
                "defaultCloseOperation", PROP_PREFERRED,
                "visible", PROP_NORMAL },
        { javax.swing.JInternalFrame.class, CLASS_EXACTLY,
                "menuBar", PROP_HIDDEN,
                "JMenuBar", PROP_HIDDEN,
                "desktopPane", PROP_HIDDEN,
                "internalFrameListeners", PROP_HIDDEN,
                "mostRecentFocusOwner", PROP_HIDDEN,
                "warningString", PROP_HIDDEN,
                "closed", PROP_HIDDEN },
        { javax.swing.JMenu.class, CLASS_EXACTLY,
                "accelerator", PROP_HIDDEN,
                "tearOff", PROP_HIDDEN,
                "menuComponents", PROP_HIDDEN,
                "menuListeners", PROP_HIDDEN,
                "popupMenu", PROP_HIDDEN,
                "topLevelMenu", PROP_HIDDEN },
        { javax.swing.JPopupMenu.class, CLASS_AND_SWING_SUBCLASSES,
                "popupMenuListeners", PROP_HIDDEN },
        { java.awt.Frame.class, CLASS_AND_SWING_SUBCLASSES,
                "cursorType", PROP_HIDDEN,
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
        { javax.swing.MenuElement.class, CLASS_AND_SWING_SUBCLASSES,
                "component", PROP_HIDDEN,
                "subElements", PROP_HIDDEN },
        { javax.swing.JMenuBar.class, CLASS_EXACTLY,
                "helpMenu", PROP_HIDDEN,
                "menuCount", PROP_HIDDEN,
                "selected", PROP_HIDDEN },
        { javax.swing.JSpinner.class, CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED },
        { java.applet.Applet.class, CLASS_AND_SUBCLASSES,
                "appletContext", PROP_HIDDEN,
                "codeBase", PROP_HIDDEN,
                "documentBase", PROP_HIDDEN },
        { javax.swing.JFileChooser.class, CLASS_EXACTLY,
                "acceptAllFileFilter", PROP_HIDDEN,
                "choosableFileFilters", PROP_HIDDEN }
    };

    /** Table with explicit changes to propeties accessibility. E.g. some
     * properties needs to be restricted to "detached write". */
    private static Object[][] propertiesAccess = {
        { javax.swing.JFrame.class, CLASS_AND_SUBCLASSES,
              "defaultCloseOperation", new Integer(FormProperty.DETACHED_WRITE) }
    };
    
    /** Table defining order of dependent properties. */
    private static Object[][] propertyOrder = {
        { javax.swing.text.JTextComponent.class,
            "document", "text" },
        { javax.swing.JSpinner.class,
            "model", "editor" },
        { javax.swing.AbstractButton.class,
            "action", "actionCommand",
            "action", "enabled",
            "action", "mnemonic",
            "action", "icon",
            "action", "text",
            "action", "toolTipText" },
        { javax.swing.JMenuItem.class,
            "action", "accelerator" },
        { javax.swing.JList.class,
            "model", "selectedIndex",
            "model", "selectedValues" },
        { javax.swing.JComboBox.class,
            "model", "selectedIndex",
            "model", "selectedItem" },
        { java.awt.TextComponent.class,
            "text", "selectionStart",
            "text", "selectionEnd" }
    };

    /** List of components that should never be containers; some of them are
     * not specified in original Swing beaninfos. */
    private static String[] forbiddenContainers = {
        "javax.swing.JLabel", // NOI18N
        "javax.swing.JButton", // NOI18N
        "javax.swing.JToggleButton", // NOI18N
        "javax.swing.JCheckBox", // NOI18N
        "javax.swing.JRadioButton", // NOI18N
        "javax.swing.JComboBox", // NOI18N
        "javax.swing.JList", // NOI18N
        "javax.swing.JTextField", // NOI18N
        "javax.swing.JTextArea", // NOI18N
        "javax.swing.JScrollBar", // NOI18N
        "javax.swing.JSlider", // NOI18N
        "javax.swing.JProgressBar", // NOI18N
        "javax.swing.JFormattedTextField", // NOI18N
        "javax.swing.JPasswordField", // NOI18N
        "javax.swing.JSpinner", // NOI18N
        "javax.swing.JSeparator", // NOI18N
        "javax.swing.JTextPane", // NOI18N
        "javax.swing.JEditorPane", // NOI18N
        "javax.swing.JTree", // NOI18N
        "javax.swing.JTable", // NOI18N
        "javax.swing.JOptionPane", // NOI18N
        "javax.swing.JColorChooser", // NOI18N
        "javax.swing.JFileChooser", // NOI18N
    };
    
    private static ClassPattern[] designTimeClasses = new ClassPattern[] {
        new ClassPattern("org.netbeans.modules.form.", ClassPattern.PACKAGE_AND_SUBPACKAGES), // NOI18N
        new ClassPattern("org.netbeans.beaninfo.editors.", ClassPattern.PACKAGE), // NOI18N
        new ClassPattern("org.netbeans.modules.i18n.form.", ClassPattern.PACKAGE) // NOI18N
    };

    // -----------------------------------------------------------------------------
    // Utility methods

    public static ResourceBundle getBundle() {
        return NbBundle.getBundle(FormUtils.class);
    }

    public static String getBundleString(String key) {
        return NbBundle.getBundle(FormUtils.class).getString(key);
    }

    public static String getFormattedBundleString(String key,
                                                  Object[] arguments)
    {
        ResourceBundle bundle = NbBundle.getBundle(FormUtils.class);
        return MessageFormat.format(bundle.getString(key), arguments);
    }

    /** Utility method that tries to clone an object. Objects of explicitly
     * specified types are constructed directly, other are serialized and
     * deserialized (if not serializable exception is thrown).
     */
    public static Object cloneObject(Object o, FormModel formModel) throws CloneNotSupportedException {
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

        if (o.getClass() == Font.class) {
            // Issue 49973
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                Font font = (Font)o;
                return new Font(font.getName(), font.getStyle(), font.getSize());
            } else {
                return Font.getFont(((Font)o).getAttributes());
            }
        }
        // Issue 49973
        if ((o.getClass() == TitledBorder.class)
            && (Utilities.getOperatingSystem() == Utilities.OS_MAC)) {
            TitledBorder border = (TitledBorder)o;
            return new TitledBorder(
                border.getBorder(),
                border.getTitle(),
                border.getTitleJustification(),
                border.getTitlePosition(),
                border.getTitleFont(),
                border.getTitleColor());
        }
        if (o.getClass() == Color.class)
            return new Color(((Color)o).getRGB());
        if (o instanceof Dimension)
            return new Dimension((Dimension)o);
        if (o instanceof Point)
            return new Point((Point)o);
        if (o instanceof Rectangle)
            return new Rectangle((Rectangle)o);
        if (o instanceof Insets)
            return ((Insets)o).clone();
        if (o instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint)o;
            return new GradientPaint(gp.getPoint1(), gp.getColor1(), gp.getPoint2(), gp.getColor2(), gp.isCyclic());
        }
        if (o instanceof Serializable)
            return cloneBeanInstance(o, null, formModel);

        throw new CloneNotSupportedException();
    }

    /** Utility method that tries to clone an object as a bean.
     * First - if it is serializable, then it is copied using serialization.
     * If not serializable, then all properties (taken from BeanInfo) are
     * copied (property values cloned recursively).
     */
    public static Object cloneBeanInstance(Object bean, BeanInfo bInfo, FormModel formModel)
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
                return new OIS(bais, bean.getClass().getClassLoader(), formModel).readObject();
            }
            catch (Exception ex) {
                ErrorManager em = ErrorManager.getDefault();
                em.annotate(ex, "Cannot clone "+bean.getClass().getName()); // NOI18N
                em.notify(ErrorManager.INFORMATIONAL, ex);
                throw new CloneNotSupportedException(ex.getMessage());
            }
        }

        // object is not Serializable
        Object clone;
        try {
            clone = CreationFactory.createDefaultInstance(bean.getClass());
            if (clone == null)
                throw new CloneNotSupportedException();

            if (bInfo == null)
                bInfo = Utilities.getBeanInfo(bean.getClass());
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
                    propertyValue = cloneObject(propertyValue, formModel);
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
                        FormModel formModel = (sfProp == null) ? null : sfProp.getPropertyContext().getFormModel();
                        propertyValue = FormUtils.cloneObject(propertyValue, formModel);
                    }
                    catch (CloneNotSupportedException ex) {} // ignore, don't report
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    public static void copyPropertiesToBean(RADProperty[] props,
                                            Object targetBean,
                                            Collection relativeProperties)
    {
        for (int i = 0; i < props.length; i++) {
            RADProperty prop = props[i];
            if (!prop.isChanged())
                continue;

            Method writeMethod = prop.getPropertyDescriptor().getWriteMethod();
            if (writeMethod == null
                || !prop.canWriteToTarget()
                || !writeMethod.getDeclaringClass().isAssignableFrom(
                                                         targetBean.getClass()))
                continue;

            try {
                if (relativeProperties != null) {
                    Object value = prop.getValue();
                    if (value instanceof RADComponent
                        || value instanceof RADComponent.ComponentReference
                        || (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue
                            && (((RADConnectionPropertyEditor.RADConnectionDesignValue)value).type
                                == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_BEAN)))
                    {
                        relativeProperties.add(prop);
                        continue;
                    }
                }

                Object realValue = prop.getRealValue();
                if (realValue == FormDesignValue.IGNORED_VALUE)
                    continue; // ignore this value, as it is not a real value

                realValue = FormUtils.cloneObject(realValue, props[i].getPropertyContext().getFormModel());
                writeMethod.invoke(targetBean, new Object[] { realValue });
            }
            catch (CloneNotSupportedException ex) { // ignore, don't report
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
                             radValue.requiredTypeName, radValue.value);
        }
        else if (value instanceof BorderDesignSupport) {
            try {
                return new BorderDesignSupport((BorderDesignSupport)value);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        } else if (value instanceof IconEditor.NbImageIcon) {
            return new IconEditor.NbImageIcon((IconEditor.NbImageIcon)value);
        }

        return null;
    }

    // ---------

    public static boolean isContainer(Class beanClass) {
//        Map containerBeans = formSettings.getContainerBeans();
//        Object registered = containerBeans != null ?
//                              containerBeans.get(beanClass.getName()) : null;
//        if (registered instanceof Boolean)
//            return ((Boolean)registered).booleanValue();

        // not registered
        int containerStatus = canBeContainer(beanClass);
        if (containerStatus == -1) { // "isContainer" attribute not specified
            containerStatus = 1;
            Class cls = beanClass.getSuperclass();
            while (cls != null
                   && !cls.equals(java.awt.Container.class))
            {
                String beanClassName = cls.getName();
                int i;
                for (i=0; i < forbiddenContainers.length; i++)
                    if (beanClassName.equals(forbiddenContainers[i]))
                        break; // superclass cannot be container

                if (i < forbiddenContainers.length) {
                    containerStatus = 0;
                    break;
                }

                cls = cls.getSuperclass();
            }
        }

        return containerStatus == 1;
//        boolean isContainer = containerStatus == 1;
//
//        if (beanClass.getName().startsWith("javax.swing.")) // NOI18N
//            setIsContainer(beanClass, isContainer);
//
//        return isContainer;
    }

    /** @return 1 if the class is explicitly specified as container in BeanInfo;
     *          0 if the class is explicitly enumerated in forbiddenContainers
     *          or specified as non-container in its BeanInfo;
     *          -1 if the class is not forbidden nor specified in BeanInfo at all
     */
    public static int canBeContainer(Class beanClass) {
        if (beanClass == null
                || !java.awt.Container.class.isAssignableFrom(beanClass))
            return 0;

        String beanClassName = beanClass.getName();
        for (int i=0; i < forbiddenContainers.length; i++)
            if (beanClassName.equals(forbiddenContainers[i]))
                return 0; // cannot be container

        Object isContainerValue = null;
        try {
            BeanDescriptor desc = Utilities.getBeanInfo(beanClass)
                                                    .getBeanDescriptor();
            if (desc != null)
               isContainerValue = desc.getValue("isContainer"); // NOI18N
        }
        catch (Exception ex) { // ignore failure
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        catch (LinkageError ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        if (isContainerValue instanceof Boolean)
            return ((Boolean)isContainerValue).booleanValue() ? 1 : 0;
        return -1; // "isContainer" attribute not specified
    }

//    public static void setIsContainer(Class beanClass, boolean isContainer) {
//        if (beanClass == null)
//            return;
//
//        Map containerBeans = formSettings.getContainerBeans();
//        if (containerBeans == null)
//            containerBeans = new HashMap();
//
//        containerBeans.put(beanClass.getName(), isContainer ? Boolean.TRUE : Boolean.FALSE);
//        formSettings.setContainerBeans(containerBeans);
//    }
//
//    public static void removeIsContainerRegistration(Class beanClass) {
//        Map containerBeans = formSettings.getContainerBeans();
//        if (containerBeans == null || beanClass == null)
//            return;
//
//        containerBeans.remove(beanClass.getName());
//        formSettings.setContainerBeans(containerBeans);
//    }

    // ---------

    /** Returns explicit property category classification (defined in
     * propertyCategories table)for properties of given class.
     * The returned array can be used in getPropertyCategory method to get
     * category for individual property. Used for SWING components to
     * correct their default (insufficient) classification.
     */
    static Object[] getPropertiesCategoryClsf(Class beanClass,
                                              BeanDescriptor beanDescriptor)
    {
        ArrayList reClsf = null;
//        Class beanClass = beanInfo.getBeanDescriptor().getBeanClass();

        // some magic with JComponents first...
        if (javax.swing.JComponent.class.isAssignableFrom(beanClass)) {
            reClsf = new ArrayList(8);
            Object isContainerValue = beanDescriptor.getValue("isContainer"); // NOI18N
            if (isContainerValue == null || Boolean.TRUE.equals(isContainerValue)) {
                reClsf.add("font"); // NOI18N
                reClsf.add(PROP_NORMAL);
            }
            else {
                reClsf.add("border"); // NOI18N
                reClsf.add(PROP_NORMAL); // NOI18N
            }
        }

        return collectPropertiesClsf(beanClass, propertyCategories, reClsf);
/*        for (int i=0; i < propertyCategories.length; i++) {
            Object[] clsf = propertyCategories[i];
            Class refClass = (Class)clsf[0];
            Object subclasses = clsf[1];

            if (refClass.equals(beanClass)
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
        return null; */
    }

    /** Returns type of property (PROP_PREFERRED, PROP_NORMAL, PROP_EXPERT or
     * PROP_HIDDEN) based on PropertyDescriptor and definitions in
     * properties classification for given bean class (returned from
     * getPropertiesCategoryClsf method).
     */
    static Object getPropertyCategory(PropertyDescriptor pd,
                                      Object[] propsClsf)
    {
        Object cat = findPropertyClsf(pd.getName(), propsClsf);
        if (cat != null)
            return cat;

        if (pd.isHidden())
            return PROP_HIDDEN;
        if (pd.isExpert())
            return PROP_EXPERT;
        if (pd.isPreferred() || Boolean.TRUE.equals(pd.getValue("preferred"))) // NOI18N
            return PROP_PREFERRED;
        return PROP_NORMAL;
    }

    /** Returns explicit property access type classification for properties of
     * given class (defined in propertiesAccess table). The returned array can
     * be used in getPropertyAccess method to get the access type for
     * individual property.
     */
    static Object[] getPropertiesAccessClsf(Class beanClass) {
        return collectPropertiesClsf(beanClass, propertiesAccess, null);
    }

    /** Returns access type for given property (as FormProperty constant).
     * 0 if no restriction is explicitly defined.
     */
    static int getPropertyAccess(PropertyDescriptor pd,
                                 Object[] propsClsf)
    {
        Object access = findPropertyClsf(pd.getName(), propsClsf);
        return access == null ? 0 : ((Integer)access).intValue();
    }

    private static Object[] collectPropertiesClsf(Class beanClass,
                                                  Object[][] table,
                                                  java.util.List list)
    {
        for (int i=0; i < table.length; i++) {
            Object[] clsf = table[i];
            Class refClass = (Class)clsf[0];
            Object subclasses = clsf[1];

            if (refClass.equals(beanClass)
                ||
                (subclasses == CLASS_AND_SUBCLASSES
                         && refClass.isAssignableFrom(beanClass))
                ||
                (subclasses == CLASS_AND_SWING_SUBCLASSES
                         && refClass.isAssignableFrom(beanClass)
                         && beanClass.getName().startsWith("javax.swing."))) { // NOI18N
                if (list == null)
                    list = new ArrayList(8);
                for (int j=2; j < clsf.length; j++)
                    list.add(clsf[j]);
            }
        }

        if (list != null) {
            Object[] array = new Object[list.size()];
            list.toArray(array);
            return array;
        }
        return null;
    }

    private static Object findPropertyClsf(String name, Object[] clsf) {
        if (clsf != null) {
            int i = clsf.length;
            while (i > 0) {
                if (clsf[i-2].equals(name))
                    return clsf[i-1];
                i -= 2;
            }
        }
        return null;
    }

    static boolean isContainerContentDependentProperty(Class beanClass,
                                                       String propName) {
        return ("selectedIndex".equals(propName) || "selectedComponent".equals(propName)) // NOI18N
               && javax.swing.JTabbedPane.class.isAssignableFrom(beanClass);
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
    
    static void sortProperties(Node.Property[] properties) {
        Arrays.sort(properties, new Comparator() {
            public int compare(Object o1, Object o2) {
                String n1 =((Node.Property)o1).getDisplayName();
                String n2 =((Node.Property)o2).getDisplayName();
                return n1.compareTo(n2);
            }
        });
    }
    
    static void reorderProperties(Class beanClass, RADProperty[] properties) {
        sortProperties(properties);
        Object[] order = collectPropertiesOrder(beanClass, propertyOrder);
        for (int i=0; i<order.length/2; i++) {
            updatePropertiesOrder(properties, (String)order[2*i], (String)order[2*i+1]);
        }
    }
    
    private static void updatePropertiesOrder(RADProperty[] properties,
        String firstProp, String secondProp) {
        int firstIndex = findPropertyIndex(properties, firstProp);
        int secondIndex = findPropertyIndex(properties, secondProp);
        if ((firstIndex != -1) && (secondIndex != -1) && (firstIndex > secondIndex)) {
            // Move the first one before the second
            RADProperty first = properties[firstIndex];
            for (int i=firstIndex; i>secondIndex; i--) {
                properties[i] = properties[i-1];
            }
            properties[secondIndex] = first;
        }
    }
    
    private static int findPropertyIndex(RADProperty[] properties, String property) {
        int index = -1;
        for (int i=0; i<properties.length; i++) {
            if (property.equals(properties[i].getName())) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    private static Object[] collectPropertiesOrder(Class beanClass, Object[][] table) {
        java.util.List list = new LinkedList();
        for (int i=0; i < table.length; i++) {
            Object[] order = table[i];
            Class refClass = (Class)order[0];

            if (refClass.isAssignableFrom(beanClass)) {
                for (int j=1; j<order.length; j++) {
                    list.add(order[j]);
                }
            }
        }
        return list.toArray();
    }

    // ---------

    /** Loads a class of a component to be used (instantiated) in the form
     * editor. The class might be either a support class being part of the IDE,
     * or a user class defined externally (by a project classpath). This methods
     * tries both ways. There are also separate loadSystemClass and
     * loadUserClass methods available.
     * @param name String name of the class
     * @param formFile FileObject representing the form file as part of a project
     */
    public static Class loadClass(String name, FileObject formFile)
        throws ClassNotFoundException
    {
        if (isDesignTimeClass(name)) {
            // first try the system classloader (for IDE and module stuff
            // like property editors, form support classes, etc)
            try {
                return loadSystemClass(name);
            }
            catch (ClassNotFoundException ex) {
            // ignore, likely this is not the right classloader
            }
            catch (LinkageError ex) {
                // some problem during loading [should not be left uncaught here?]
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            if (formFile != null) { // fall back to project class loader
                return loadUserClass(name, formFile);
            }
        }
        else { // first try the project class loader (for user classes)
            if (formFile != null) {
                try {
                    return loadUserClass(name, formFile);
                }
                catch (ClassNotFoundException ex) {
                // ignore, likely this is not the right classloader
                }
                catch (LinkageError ex) {
                    // some problem during loading [should not be left uncaught here?]
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            // fall back to system class loader (for IDE classes)
            return loadSystemClass(name);
        }

        throw new ClassNotFoundException(); // classpath unknown
    }

    public static Class loadClass(String name, FormModel form)
        throws ClassNotFoundException
    {
        return loadClass(name, FormEditor.getFormDataObject(form).getFormFile());
    }

    /** Loads a class using IDE system class loader. Usable for form module
     * support classes, property editors, etc.
     */
    public static Class loadSystemClass(String name) throws ClassNotFoundException {
        ClassLoader loader = (ClassLoader)
                                 Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null)
            throw new ClassNotFoundException();

        return loader.loadClass(name);
    }

    /** Loads a class from a class path of a user project specified by given
     * form file placed somewhere in the project.
     */
    public static Class loadUserClass(String name, FileObject formFile)
        throws ClassNotFoundException
    {
        return ClassPathUtils.loadClass(name, formFile);
    }

//    public static Class loadUserClass(String name, FormModel form)
//        throws ClassNotFoundException
//    {
//        return loadUserClass(name, FormEditorSupport.getFormDataObject(form).getFormFile());
//    }
    
    public static synchronized void registerDesignTimeClass(String pattern) {
        ClassPattern[] patterns = new ClassPattern[designTimeClasses.length + 1];
        System.arraycopy(designTimeClasses, 0, patterns, 0, designTimeClasses.length);
        ClassPattern classPattern;
        if (pattern.endsWith("**")) { // NOI18N
            classPattern = new ClassPattern(
                pattern.substring(0, pattern.length()-2),
                ClassPattern.PACKAGE_AND_SUBPACKAGES);
        } else if (pattern.endsWith("*")) { // NOI18N
            classPattern = new ClassPattern(
                pattern.substring(0, pattern.length()-1),
                ClassPattern.PACKAGE);
        } else {
            classPattern = new ClassPattern(pattern, ClassPattern.CLASS);
        }
        patterns[designTimeClasses.length] = classPattern;
        designTimeClasses = patterns;
    }

    static boolean isDesignTimeClass(String className) {
        for (int i=0; i<designTimeClasses.length; i++) {
            ClassPattern pattern = designTimeClasses[i];
            switch (pattern.type) {
                case (ClassPattern.CLASS):
                    if (className.equals(pattern.name)) return true;
                    break;
                case (ClassPattern.PACKAGE):
                    if (className.startsWith(pattern.name)
                        && (className.lastIndexOf('.') <= pattern.name.length())) return true;
                    break;
                case (ClassPattern.PACKAGE_AND_SUBPACKAGES):
                    if (className.startsWith(pattern.name)) return true;
                    break;
            }
        }
        return false;
    }
    
    private static class ClassPattern {
        static final int CLASS = 0;
        static final int PACKAGE = 1;
        static final int PACKAGE_AND_SUBPACKAGES = 2;
        String name;
        int type;
        
        ClassPattern(String name, int type) {
            this.name = name;
            this.type = type;
        }
    }

    // ---------

    private static class OIS extends ObjectInputStream {
        private ClassLoader classLoader;
        private FormModel formModel;

        public OIS(InputStream is, ClassLoader loader, FormModel formModel) throws IOException {
            super(is);
            this.formModel = formModel;
            classLoader = loader;
        }

        protected Class resolveClass(ObjectStreamClass streamCls)
            throws IOException, ClassNotFoundException
        {
            String name = streamCls.getName();
            if (name.startsWith("[")) { // NOI18N
                // load array element class first to avoid failure
                for (int i=1, n=name.length(); i < n; i++) {
                    char c = name.charAt(i);
                    if (c == 'L' && name.endsWith(";")) { // NOI18N
                        String clsName = name.substring(i+1, n-1);
                        loadClass(clsName);
                        break;
                    }
                    else if (c != '[')
                        return super.resolveClass(streamCls);
                }
            }
            return loadClass(name);
        }
        
        private Class loadClass(String name) throws ClassNotFoundException {
            if (classLoader != null) {
                try {
                    return classLoader.loadClass(name);
                } catch (ClassNotFoundException ex) {}
            }
            return FormUtils.loadClass(name, formModel);
        }
        
    }
}
