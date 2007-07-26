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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.text.Keymap;
import javax.swing.undo.UndoManager;
import org.netbeans.api.java.source.ui.DialogBinding;
import org.netbeans.editor.ActionFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ExtCaret;

import org.openide.ErrorManager;
import org.openide.util.*;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.text.CloneableEditorSupport;

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
    public static final int DONT_CLONE_VALUES = 8;

    private static final Object CLASS_EXACTLY = new Object();
    private static final Object CLASS_AND_SUBCLASSES = new Object();
    private static final Object CLASS_AND_SWING_SUBCLASSES = new Object();

    static final Object PROP_PREFERRED = new Object();
    static final Object PROP_NORMAL = new Object();
    static final Object PROP_EXPERT = new Object();
    static final Object PROP_HIDDEN = new Object();

    static final String PROP_REQUIRES_PARENT = "thisPropertyRequiresParent"; // NOI18N
    static final String PROP_REQUIRES_CHILDREN = "thisPropertyRequiresChildren"; // NOI18N

    /** Table defining categories of properties. It overrides original Swing
     * definition from beaninfo (which is often inadequate). */
    private static Object[][] propertyCategories = {
        { "java.awt.Component", CLASS_AND_SUBCLASSES,
                "locale", PROP_HIDDEN,
                "locationOnScreen", PROP_HIDDEN,
                "showing", PROP_HIDDEN },
        { "java.awt.Component", CLASS_AND_SWING_SUBCLASSES,
                "accessibleContext", PROP_HIDDEN,
                "components", PROP_HIDDEN,
                "containerListeners", PROP_HIDDEN,
                "focusTraversalPolicySet", PROP_HIDDEN,
                "focusCycleRootAncestor", PROP_HIDDEN,
                "focusOwner", PROP_HIDDEN },
        { "java.awt.Container", CLASS_AND_SUBCLASSES,
                "componentCount", PROP_HIDDEN,
                "layout", PROP_HIDDEN },
        { "javax.swing.JComponent", CLASS_AND_SUBCLASSES,
                "debugGraphicsOptions", PROP_EXPERT,
                "preferredSize", PROP_NORMAL,
                "actionMap", PROP_HIDDEN },
        { "javax.swing.JComponent", CLASS_AND_SWING_SUBCLASSES,
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
        { "java.awt.Window", CLASS_AND_SWING_SUBCLASSES,
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
        { "javax.swing.text.JTextComponent", CLASS_AND_SUBCLASSES,
                "document", PROP_PREFERRED,
                "text", PROP_PREFERRED,
                "editable", PROP_PREFERRED,
                "disabledTextColor", PROP_NORMAL,
                "selectedTextColor", PROP_NORMAL,
                "selectionColor", PROP_NORMAL,
                "caretColor", PROP_NORMAL },
        { "javax.swing.text.JTextComponent", CLASS_AND_SWING_SUBCLASSES,
                "actions", PROP_HIDDEN,
                "caretListeners", PROP_HIDDEN,
                "inputMethodRequests", PROP_HIDDEN },
        { "javax.swing.JTextField", CLASS_AND_SUBCLASSES,
                "columns", PROP_PREFERRED },
        { "javax.swing.JTextField", CLASS_AND_SWING_SUBCLASSES,
                "horizontalVisibility", PROP_HIDDEN },
        { "javax.swing.JFormattedTextField", CLASS_EXACTLY,
                "formatterFactory", PROP_PREFERRED,
                "formatter", PROP_HIDDEN },
        { "javax.swing.JPasswordField", CLASS_EXACTLY,
                "password", PROP_HIDDEN },
        { "javax.swing.JTextArea", CLASS_AND_SUBCLASSES,
                "columns", PROP_PREFERRED,
                "rows", PROP_PREFERRED,
                "lineWrap", PROP_PREFERRED,
                "wrapStyleWord", PROP_PREFERRED },
        { "javax.swing.JEditorPane", CLASS_AND_SUBCLASSES,
                "border", PROP_PREFERRED,
                "font", PROP_PREFERRED,
                "contentType", PROP_PREFERRED,
                "editorKit", PROP_PREFERRED },
        { "javax.swing.JEditorPane", CLASS_AND_SWING_SUBCLASSES,
                "hyperlinkListeners", PROP_HIDDEN },
        { "javax.swing.JTextPane", CLASS_EXACTLY,
                "characterAttributes", PROP_HIDDEN,
                "paragraphAttributes", PROP_HIDDEN },
        { "javax.swing.JTree", CLASS_AND_SUBCLASSES,
                "border", PROP_PREFERRED },
        { "javax.swing.JTree", CLASS_EXACTLY,
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
        { "javax.swing.AbstractButton", CLASS_AND_SUBCLASSES,
                "mnemonic", PROP_PREFERRED,
                "action", PROP_PREFERRED },
        { "javax.swing.AbstractButton", CLASS_AND_SWING_SUBCLASSES,
                "selectedObjects", PROP_HIDDEN },
        { "javax.swing.JToggleButton", CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED },
        { "javax.swing.JButton", CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED,
                "defaultButton", PROP_HIDDEN },
        { "javax.swing.JCheckBox", CLASS_EXACTLY,
                "icon", PROP_NORMAL },
        { "javax.swing.JRadioButton", CLASS_EXACTLY,
                "icon", PROP_NORMAL },
        { "javax.swing.JMenuItem", CLASS_AND_SUBCLASSES,
                "icon", PROP_PREFERRED },
        { "javax.swing.JMenuItem", CLASS_AND_SWING_SUBCLASSES,
                "menuDragMouseListeners", PROP_HIDDEN,
                "menuKeyListeners", PROP_HIDDEN },
        { "javax.swing.JCheckBoxMenuItem", CLASS_AND_SUBCLASSES,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED,
                "icon", PROP_NORMAL },
        { "javax.swing.JRadioButtonMenuItem", CLASS_AND_SUBCLASSES,
                "selected", PROP_PREFERRED,
                "buttonGroup", PROP_PREFERRED,
                "icon", PROP_NORMAL },
        { "javax.swing.JTabbedPane", CLASS_EXACTLY,
                "selectedComponent", PROP_EXPERT },
        { "javax.swing.JSplitPane", CLASS_AND_SUBCLASSES,
                "dividerLocation", PROP_PREFERRED,
                "dividerSize", PROP_PREFERRED,
                "orientation", PROP_PREFERRED,
                "resizeWeight", PROP_PREFERRED },
        { "javax.swing.JSplitPane", CLASS_EXACTLY,
                "leftComponent", PROP_HIDDEN,
                "rightComponent", PROP_HIDDEN,
                "topComponent", PROP_HIDDEN,
                "bottomComponent", PROP_HIDDEN },
        { "javax.swing.JSlider", CLASS_AND_SUBCLASSES,
                "majorTickSpacing", PROP_PREFERRED,
                "minorTickSpacing", PROP_PREFERRED,
                "paintLabels", PROP_PREFERRED,
                "paintTicks", PROP_PREFERRED,
                "paintTrack", PROP_PREFERRED,
                "snapToTicks", PROP_PREFERRED },
        { "javax.swing.JLabel", CLASS_AND_SUBCLASSES,
                "horizontalAlignment", PROP_PREFERRED,
                "verticalAlignment", PROP_PREFERRED,
                "displayedMnemonic", PROP_PREFERRED,
                "labelFor", PROP_PREFERRED },
        { "javax.swing.JList", CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED,
                "border", PROP_PREFERRED,
                "selectionMode", PROP_PREFERRED },
        { "javax.swing.JComboBox", CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED },
        { "javax.swing.JComboBox", CLASS_EXACTLY,
                "popupVisible", PROP_HIDDEN,
                "popupMenuListeners", PROP_HIDDEN,
                "selectedObjects", PROP_HIDDEN },
        { "javax.swing.Scrollable", CLASS_AND_SWING_SUBCLASSES,
                "preferredScrollableViewportSize", PROP_HIDDEN,
                "scrollableTracksViewportWidth", PROP_HIDDEN,
                "scrollableTracksViewportHeight", PROP_HIDDEN },
        { "javax.swing.JScrollBar", CLASS_EXACTLY,
                "adjustmentListeners", PROP_HIDDEN },
        { "javax.swing.JTable", CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED,
                "border", PROP_PREFERRED },
        { "javax.swing.JTable", CLASS_EXACTLY,
                "editing", PROP_HIDDEN,
                "editorComponent", PROP_HIDDEN,
                "selectedColumn", PROP_HIDDEN,
                "selectedColumnCount", PROP_HIDDEN,
                "selectedColumns", PROP_HIDDEN,
                "selectedRow", PROP_HIDDEN,
                "selectedRowCount", PROP_HIDDEN,
                "selectedRows", PROP_HIDDEN },
        { "javax.swing.JSeparator", CLASS_EXACTLY,
                "font", PROP_NORMAL },
        { "javax.swing.JInternalFrame", CLASS_AND_SUBCLASSES,
                "defaultCloseOperation", PROP_PREFERRED,
                "visible", PROP_NORMAL },
        { "javax.swing.JInternalFrame", CLASS_EXACTLY,
                "menuBar", PROP_HIDDEN,
                "JMenuBar", PROP_HIDDEN,
                "desktopPane", PROP_HIDDEN,
                "internalFrameListeners", PROP_HIDDEN,
                "mostRecentFocusOwner", PROP_HIDDEN,
                "warningString", PROP_HIDDEN,
                "closed", PROP_HIDDEN },
        { "javax.swing.JMenu", CLASS_EXACTLY,
                "accelerator", PROP_HIDDEN,
                "tearOff", PROP_HIDDEN,
                "menuComponents", PROP_HIDDEN,
                "menuListeners", PROP_HIDDEN,
                "popupMenu", PROP_HIDDEN,
                "topLevelMenu", PROP_HIDDEN },
        { "javax.swing.JPopupMenu", CLASS_AND_SWING_SUBCLASSES,
                "popupMenuListeners", PROP_HIDDEN },
        { "java.awt.Frame", CLASS_AND_SWING_SUBCLASSES,
                "cursorType", PROP_HIDDEN,
                "menuBar", PROP_HIDDEN },
        { "javax.swing.JFrame", CLASS_AND_SUBCLASSES,
                "title", PROP_PREFERRED },
        { "javax.swing.JFrame", CLASS_EXACTLY,
                "menuBar", PROP_HIDDEN,
                "layout", PROP_HIDDEN },
        { "javax.swing.JDialog", CLASS_AND_SUBCLASSES,
                "title", PROP_PREFERRED },
        { "javax.swing.JDialog", CLASS_EXACTLY,
                "layout", PROP_HIDDEN },
        { "javax.swing.MenuElement", CLASS_AND_SWING_SUBCLASSES,
                "component", PROP_HIDDEN,
                "subElements", PROP_HIDDEN },
        { "javax.swing.JMenuBar", CLASS_EXACTLY,
                "helpMenu", PROP_HIDDEN,
                "menuCount", PROP_HIDDEN,
                "selected", PROP_HIDDEN },
        { "javax.swing.JSpinner", CLASS_AND_SUBCLASSES,
                "model", PROP_PREFERRED },
        { "java.applet.Applet", CLASS_AND_SUBCLASSES,
                "appletContext", PROP_HIDDEN,
                "codeBase", PROP_HIDDEN,
                "documentBase", PROP_HIDDEN },
        { "javax.swing.JFileChooser", CLASS_EXACTLY,
                "acceptAllFileFilter", PROP_HIDDEN,
                "choosableFileFilters", PROP_HIDDEN }
    };

    /** Table with explicit changes to propeties accessibility. E.g. some
     * properties needs to be restricted to "detached write". */
    private static Object[][] propertiesAccess = {
        { "javax.swing.JFrame", CLASS_AND_SUBCLASSES,
              "defaultCloseOperation", new Integer(FormProperty.DETACHED_WRITE) }
    };

    /** Table of properties that need the component to be added in the parent,
     * or child components in the container before the property can be set.
     * [We use one table though these are two distinct categories - it's fine
     * until there is a property requiring both conditions.] */
    private static Object[][] propertyContainerDeps = {
        { "javax.swing.JTabbedPane", CLASS_AND_SUBCLASSES,
                  "selectedIndex", PROP_REQUIRES_CHILDREN,
                  "selectedComponent", PROP_REQUIRES_CHILDREN },
        { "javax.swing.JInternalFrame", CLASS_AND_SUBCLASSES,
                  "maximum", PROP_REQUIRES_PARENT,
                  "icon", PROP_REQUIRES_PARENT },
        // columnModel can be set by binding property
        { "javax.swing.JTable", CLASS_AND_SUBCLASSES,
                  "columnModel", PROP_REQUIRES_PARENT }
    };

    /** Table defining order of dependent properties. */
    private static Object[][] propertyOrder = {
        { "javax.swing.text.JTextComponent",
            "document", "text" },
        { "javax.swing.JSpinner",
            "model", "editor" },
        { "javax.swing.AbstractButton",
            "action", "actionCommand",
            "action", "enabled",
            "action", "mnemonic",
            "action", "icon",
            "action", "text",
            "action", "toolTipText" },
        { "javax.swing.JMenuItem",
            "action", "accelerator" },
        { "javax.swing.JList",
            "model", "selectedIndex",
            "model", "selectedValues" },
        { "javax.swing.JComboBox",
            "model", "selectedIndex",
            "model", "selectedItem" },
        { "java.awt.TextComponent",
            "text", "selectionStart",
            "text", "selectionEnd" },
        { "javax.swing.JEditorPane",
            "contentType", "text",
            "editorKit", "text" }
    };

    /** Table enumerating properties that can hold HTML text. */
    private static Object[][] swingTextProperties = {
        { "javax.swing.JComponent", FormUtils.CLASS_AND_SUBCLASSES,
            "text", Boolean.TRUE,
            "toolTipText", Boolean.TRUE }
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
        return NbBundle.getMessage(FormUtils.class, key, arguments);
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
            return o;
        }
        // Issue 49973
        if ((o.getClass() == TitledBorder.class) && Utilities.isMac()) {
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
                    setter.invoke(clone, new Object[] { propertyValue });
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
                Object copiedValue = propertyValue;
                if ((mode & DONT_CLONE_VALUES) == 0) {
                    if (!(propertyValue instanceof FormDesignValue)) {
                        try { // clone common property value                        
                            FormModel formModel = (sfProp == null) ? null : sfProp.getPropertyContext().getFormModel();                        
                            copiedValue = FormUtils.cloneObject(propertyValue, formModel);
                        }
                        catch (CloneNotSupportedException ex) {} // ignore, don't report
                    }
                    else { // handle FormDesignValue                    
                        Object val = ((FormDesignValue)propertyValue).copy(tfProp);
                        if (val != null)
                            copiedValue = val;
                        else if ((mode & PASS_DESIGN_VALUES) == 0)
                            continue; // cannot just pass the same design value
                    }
                }

                // set property value
                if (tfProp != null) {
                    boolean firing = tfProp.isChangeFiring();
                    tfProp.setChangeFiring((mode & DISABLE_CHANGE_FIRING) == 0);
                    tfProp.setValue(copiedValue);
                    tfProp.setChangeFiring(firing);
                }
                else tnProp.setValue(copiedValue);

                if (sfProp != null && tfProp != null) {
                    // also clone current PropertyEditor
                    PropertyEditor sPrEd = sfProp.getCurrentEditor();
                    PropertyEditor tPrEd = tfProp.getCurrentEditor();
                    if (sPrEd != null
                        && (tPrEd == null
                            || sPrEd.getClass() != tPrEd.getClass())
                        && (propertyValue == copiedValue
                            || (propertyValue != null && copiedValue != null
                                && propertyValue.getClass() == copiedValue.getClass())))
                    {
                        tPrEd = sPrEd instanceof RADConnectionPropertyEditor ?
                            new RADConnectionPropertyEditor(tfProp.getValueType()) :
                            (PropertyEditor)CreationFactory.createDefaultInstance(
                                                             sPrEd.getClass());
                        tfProp.setCurrentEditor(tPrEd);
                    }
                }

                // also copy the resource/i18n attributes set on the property
                copyPropertyAttrs(snProp, tnProp, ResourceSupport.getPropertyAttrNames());
            }
            catch (Exception ex) { // ignore
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    private static void copyPropertyAttrs(Node.Property sourceProp, Node.Property targetProp, String[] attrs) {
        for (String attr : attrs) {
            Object value = sourceProp.getValue(attr);
            if (value != null) {
                targetProp.setValue(attr, value);
            }
        }
    }

    public static void copyPropertiesToBean(RADProperty[] props,
                                            Object targetBean,
                                            Collection relativeProperties)
    {
        for (int i = 0; i < props.length; i++) {
            RADProperty prop = props[i];
            if (!prop.isChanged() || !prop.canWriteToTarget())
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

                Method writeMethod = getPropertyWriteMethod(prop, targetBean.getClass()); //prop.getPropertyDescriptor().getWriteMethod();
                if (writeMethod == null) {
                    continue;
                }

                Object value = prop.getValue();
                Object newValue = null;
                if (value instanceof FormDesignValue) {
                    newValue = ((FormDesignValue)value).getDesignValue(targetBean);
                }
                if (newValue == null) {
                    Object realValue = prop.getRealValue();
                    if (realValue == FormDesignValue.IGNORED_VALUE)
                        continue; // ignore this value, as it is not a real value

                    newValue = FormUtils.cloneObject(realValue, props[i].getPropertyContext().getFormModel());
                }
                writeMethod.invoke(targetBean, new Object[] { newValue });
            }
            catch (CloneNotSupportedException ex) { // ignore, don't report
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    public static Method getPropertyWriteMethod(RADProperty property, Class targetClass) {
        Method method = property.getPropertyDescriptor().getWriteMethod();
        if (method != null
            && targetClass != null
            && !method.getDeclaringClass().isAssignableFrom(targetClass)) {
            // try to use find the same method in the target class
            try {
                method = targetClass.getMethod(method.getName(), 
                                               method.getParameterTypes());
            } catch (Exception ex) { // ignore
                method = null;
            }
        }
        return method;
    }

    public static void setupEditorPane(javax.swing.JEditorPane editor, FileObject srcFile, int ccPosition) {
        editor.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-java")); // NOI18N
        DialogBinding.bindComponentToFile(srcFile, ccPosition, 0, editor);

        // don't use global undo/redo actions, register basic ones
        KeyStroke[] undoKeys = new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0),
                                                 KeyStroke.getKeyStroke(KeyEvent.VK_Z, 130) };
        KeyStroke[] redoKeys = new KeyStroke[] { KeyStroke.getKeyStroke(KeyEvent.VK_AGAIN, 0),
                                                 KeyStroke.getKeyStroke(KeyEvent.VK_Y, 130) };
        Keymap keymap = editor.getKeymap();
        Action undoAction = new ActionFactory.UndoAction();
        for (KeyStroke k : undoKeys) {
            keymap.removeKeyStrokeBinding(k);
            keymap.addActionForKeyStroke(k, undoAction);
        }
        Action redoAction = new ActionFactory.RedoAction();
        for (KeyStroke k : redoKeys) {
            keymap.removeKeyStrokeBinding(k);
            keymap.addActionForKeyStroke(k, redoAction);
        }
        UndoManager um = new UndoManager();
        editor.getDocument().addUndoableEditListener(um);
        editor.getDocument().putProperty(BaseDocument.UNDO_MANAGER_PROP, um);

        // do not highlight current row
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editor);
        eui.removeLayer(ExtCaret.HIGHLIGHT_ROW_LAYER_NAME);
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
        if ("javax.swing.JPopupMenu".equals(beanClassName)) { // NOI18N
            return 1;
        }
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
        catch (Error ex) { // Issue 74002
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

    public static boolean isVisualizableClass(Class cls) {
        if (java.awt.Component.class.isAssignableFrom(cls)) {
            return true;
        }
        for (ViewConverter c : getViewConverters()) {
            if (c.canVisualize(cls)) {
                return true;
            }
        }
        return false;
    }

    static ViewConverter[] getViewConverters() {
        Lookup.Result<ViewConverter> result = Lookup.getDefault().lookupResult(ViewConverter.class);
        Collection<? extends ViewConverter> all = result.allInstances();
        ViewConverter[] converters = new ViewConverter[all.size()];
        int i = all.size();
        for (ViewConverter c : all) {
            converters[--i] = c;
        }
        return converters;
    }

    static ComponentConverter[] getClassConverters() {
        Lookup.Result<ComponentConverter> result = Lookup.getDefault().lookupResult(ComponentConverter.class);
        Collection<? extends ComponentConverter> all = result.allInstances();
        ComponentConverter[] converters = new ComponentConverter[all.size()];
        int i = all.size();
        for (ComponentConverter c : all) {
            converters[--i] = c;
        }
        return converters;
    }

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
    }

    /** Returns type of property (PROP_PREFERRED, PROP_NORMAL, PROP_EXPERT or
     * PROP_HIDDEN) based on PropertyDescriptor and definitions in
     * properties classification for given bean class (returned from
     * getPropertiesCategoryClsf method).
     */
    static Object getPropertyCategory(FeatureDescriptor pd,
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

    static Object[] getPropertiesParentChildDepsClsf(Class beanClass) {
        return collectPropertiesClsf(beanClass, propertyContainerDeps, null);
    }

    static String getPropertyParentChildDependency(PropertyDescriptor pd,
                                                 Object[] propClsf)
    {
        return (String) findPropertyClsf(pd.getName(), propClsf);
    }

    /**
     * Finds out if given property can hold text with <html> prefix. Basically
     * it must be a text property of a Swing component. Used by String property
     * editor.
     * @return true if the property can hold <html> text
     */
    public static boolean isHTMLTextProperty(Node.Property property) {
        if (property.getValueType() == String.class) {
            if (property instanceof RADProperty) {
                Class beanClass = ((RADProperty)property).getRADComponent().getBeanClass();
                Object[] clsf = collectPropertiesClsf(beanClass, swingTextProperties, null);
                return findPropertyClsf(property.getName(), clsf) != null;
            } else if (property.getName().equals("TabConstraints.tabTitle")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    private static Object[] collectPropertiesClsf(Class beanClass,
                                                  Object[][] table,
                                                  java.util.List list)
    {
        // Set of names of super classes of the bean and interfaces implemented by the bean.
        Set superClasses = superClasses(beanClass);

        for (int i=0; i < table.length; i++) {
            Object[] clsf = table[i];
            String refClass = (String)clsf[0];
            Object subclasses = clsf[1];

            if (refClass.equals(beanClass.getName())
                ||
                (subclasses == CLASS_AND_SUBCLASSES
                         && superClasses.contains(refClass))
                ||
                (subclasses == CLASS_AND_SWING_SUBCLASSES
                         && superClasses.contains(refClass)
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

    static boolean isMarkedParentDependentProperty(Node.Property prop) {
        return Boolean.TRUE.equals(prop.getValue(PROP_REQUIRES_PARENT));
    }

    static boolean isMarkedChildrenDependentProperty(Node.Property prop) {
        return Boolean.TRUE.equals(prop.getValue(PROP_REQUIRES_CHILDREN));
    }

    /** @return a formatted name of specified method
     */
    public static String getMethodName(MethodDescriptor desc) {                
        return getMethodName(desc.getName(), desc.getMethod().getParameterTypes());
    }
    
    public static String getMethodName(String name, Class[] params) {        
	StringBuffer sb = new StringBuffer(name);
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
        // Set of names of super classes of the bean and interfaces implemented by the bean.
        Set superClasses = superClasses(beanClass);

        java.util.List list = new LinkedList();
        for (int i=0; i < table.length; i++) {
            Object[] order = table[i];
            String refClass = (String)order[0];

            if (superClasses.contains(refClass)) {
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
     * or a user class defined externally (by a project classpath).
     * There are also separate loadSystemClass for loading a module class only.
     * @param name String name of the class
     * @param formFile FileObject representing the form file as part of a project
     */
    public static Class loadClass(String name, FileObject formFile)
        throws ClassNotFoundException
    {
        return ClassPathUtils.loadClass(name, formFile);
    }

    public static Class loadClass(String name, FormModel form)
        throws ClassNotFoundException
    {
        FormDataObject dobj = FormEditor.getFormDataObject(form);
        return dobj != null ? loadClass(name, dobj.getFormFile()) :
                              loadSystemClass(name); // layout test may have no data object
    }

    /** Loads a class using IDE system class loader. Usable for form module
     * support classes, property editors, etc.
     */
    public static Class loadSystemClass(String name) throws ClassNotFoundException {
        ClassLoader loader = (ClassLoader)
                                 Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null)
            throw new ClassNotFoundException();

        return Class.forName(name, true, loader);
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
            return loadClass(name);
        }
        
        private Class loadClass(String name) throws ClassNotFoundException {
            if (classLoader != null) {
                try {
                    return Class.forName(name, true, classLoader);
                } catch (ClassNotFoundException ex) {}
            }
            return FormUtils.loadClass(name, formModel);
        }
        
    }

    public static List<RADComponent> getSelectedLayoutComponents(Node[] nodes) {
        if ((nodes == null) || (nodes.length < 1))
            return null;

        List<RADComponent> components = new ArrayList<RADComponent>();
        for (int i=0; i<nodes.length; i++) {
            RADComponentCookie radCookie =
                (RADComponentCookie) nodes[i].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if ((metacomp instanceof RADVisualComponent)) {
                    RADVisualComponent visComp = (RADVisualComponent)metacomp;
                    RADVisualContainer visCont = visComp.getParentContainer();
                    if ((visCont != null) && javax.swing.JScrollPane.class.isAssignableFrom(visCont.getBeanInstance().getClass())) {
                        visComp = visCont;
                        visCont = visCont.getParentContainer();
                    }

                    if (isVisualInDesigner(visComp) && (visCont!= null)
                            && (visCont.getLayoutSupport() == null)
                            && !visComp.isMenuComponent()) {
                        components.add(visComp);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        return components;
    }

    public static boolean isVisualInDesigner(RADComponent comp) {
        if (comp instanceof RADVisualComponent) {
            FormDesigner designer = FormEditor.getFormDesigner(comp.getFormModel());
            if (designer != null) {
                return designer.isInDesigner((RADVisualComponent)comp);
            }
        }
        return false;
    }
    
    private static Set superClasses(Class beanClass) {
        Set superClasses = new HashSet();
        Class[] infaces = beanClass.getInterfaces();
        for (int i=0; i<infaces.length; i++) {
            superClasses.add(infaces[i].getName());
        }
        Class superClass = beanClass;
        do {
            superClasses.add(superClass.getName());
        } while ((superClass = superClass.getSuperclass()) != null);
        return superClasses;
    }

    /**
     * "Un-generifies" the given type.
     *
     * @param type type to "un-generify".
     * @return "un-generified" type.
     */
    public static Class typeToClass(TypeHelper type) {
        Class clazz = Object.class;
        if (type == null) return clazz;
        Type t = type.getType();
        if (t instanceof Class) {
            clazz = (Class)t;
        } else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            clazz = (Class)pt.getRawType();
        } else if (t instanceof WildcardType) {
            WildcardType wt = (WildcardType)t;
            for (Type bound : wt.getUpperBounds()) {
                clazz = typeToClass(new TypeHelper(bound, type.getActualTypeArgs()));
                if (!Object.class.equals(clazz) && !clazz.isInterface()) break;
            }
        } else if (t instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable)t;
            Map<String,TypeHelper> actualTypeArgs = type.getActualTypeArgs();
            if (actualTypeArgs != null) {
                TypeHelper tt = actualTypeArgs.get(tv.getName());
                if (tt != null) {
                    Type typ = typeToClass(tt);
                    clazz = typeToClass(new TypeHelper(typ, actualTypeArgs));
                }
            }
        }
        return clazz;
    }

    /**
     * Represents generified type with (possibly) some type parameters set.
     */
    public static class TypeHelper {
        /** The type. */
        private Type type;
        /** Fully qualified name of the type. */
        private String name;
        /** Type parameters that has been set. */
        private Map<String,TypeHelper> actualTypeArgs;

        /**
         * Creates <code>TypeHelper</code> that represents <code>Object</code>.
         */
        public TypeHelper() {
            this(Object.class, null);
        }

        public TypeHelper(String name) {
            this(name, null);
        }

        public TypeHelper(String name, Map<String,TypeHelper> actualTypeArgs) {
            this.name = name;
            this.actualTypeArgs = actualTypeArgs;
        }

        /**
         * Creates <code>TypeHelper</code> that represents given type with
         * no type arguments set.
         *
         * @param type type.
         */
        public TypeHelper(Type type) {
            this(type, null);
        }

        /**
         * Creates <code>TypeHelper</code> that represents given type with
         * some type arguments set.
         *
         * @param type type.
         * @param actualTypeArgs type parameters that has been set.
         */
        public TypeHelper(Type type, Map<String,TypeHelper> actualTypeArgs) {
            this.type = type;
            this.actualTypeArgs = actualTypeArgs;
        }

        /**
         * Returns generified type represented by this instance.
         *
         * @return generified type represented by this instance.
         */
        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        /**
         * Returns map of type parameters that has been set.
         *
         * @return map or <code>null</code> if the type is not generified
         * or none of its type parameters has been set.
         */
        public Map<String,TypeHelper> getActualTypeArgs() {
            return actualTypeArgs;
        }

        /**
         * Returns (undefined ;-)) normalized form of this type.
         */
        TypeHelper normalize() {
            TypeHelper t = this;
            if (type instanceof TypeVariable) {
                if (actualTypeArgs != null) {
                    TypeVariable tv = (TypeVariable)type;
                    t = actualTypeArgs.get(tv.getName());
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType)type;
                Class clazz = (Class)pt.getRawType();
                Map<String,TypeHelper> newMap = new HashMap<String,TypeHelper>();
                Type[] args = pt.getActualTypeArguments();
                TypeVariable[] tvar = clazz.getTypeParameters();
                for (int i=0; i<tvar.length; i++) {
                    Type arg = args[i];
                    TypeHelper sub = new TypeHelper(arg, actualTypeArgs).normalize();
                    newMap.put(tvar[i].getName(), new TypeHelper(sub.getType()));
                }
                t = new TypeHelper(clazz, newMap);
            } else if (type instanceof WildcardType) {
                WildcardType wt = (WildcardType)type;
                // PENDING more upper bounds
                TypeHelper sub = new TypeHelper(wt.getUpperBounds()[0], actualTypeArgs).normalize();
                t = new TypeHelper(sub.getType());
            }
            return t;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TypeHelper) {
                TypeHelper t = (TypeHelper)o;
                return ((name == null) ? (t.name == null) : t.name.equals(name))
                        && ((type == null) ? (t.type == null) : t.type.equals(type));
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }

    public static String autobox(String className) {
        if (className.equals("byte") || className.equals("short") || className.equals("long") // NOI18N
                || className.equals("float") || className.equals("double") || className.equals("boolean")) { // NOI18N
            className = "java.lang." + Character.toUpperCase(className.charAt(0)) + className.substring(1); // NOI18N
        } else if (className.equals("int")) { // NOI18N
            className = "java.lang.Integer"; // NOI18N
        } else if (className.equals("char")) { // NOI18N
            className = "java.lang.Character"; // NOI18N
        }
        return className;
    }
 
    /*
     * Calls Introspector.getBeanInfo() more safely to handle 3rd party BeanInfos
     * that may be broken or malformed. This is a replacement for Introspector.getBeanInfo().
     * @see java.beans.Introspector.getBeanInfo(Class)
     */
    public static java.beans.BeanInfo getBeanInfo(Class clazz) throws java.beans.IntrospectionException {
        try {
            return Utilities.getBeanInfo(clazz);//, java.beans.Introspector.USE_ALL_BEANINFO);
        } catch (Error ex1) { // why is Error thrown instead of IntrospectionException?
            try {
                return Introspector.getBeanInfo(clazz, java.beans.Introspector.IGNORE_IMMEDIATE_BEANINFO);
            } catch (Error ex2) {
                return Introspector.getBeanInfo(clazz, java.beans.Introspector.IGNORE_ALL_BEANINFO);
            }
        }
    }
}
