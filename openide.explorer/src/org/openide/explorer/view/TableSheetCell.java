/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.view;

import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.*;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;

import java.beans.*;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import java.text.MessageFormat;

import java.util.EventObject;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import org.netbeans.modules.openide.explorer.TTVEnvBridge;


/**
 * TableCellEditor/Renderer implementation. Component returned is the PropertyPanel
 *
 * @author Jan Rojcek
 */
class TableSheetCell extends AbstractCellEditor implements TableModelListener, PropertyChangeListener, TableCellEditor,
    TableCellRenderer {
    /* Table sheet cell works only with NodeTableModel */
    private NodeTableModel tableModel;

    /* Determines how to paint renderer */
    private Boolean flat;

    //
    // Editor
    //

    /** Actually edited node (its property) */
    private Node node;

    /** Edited property */
    private Property prop;

    //
    // Renderer
    //

    /** Default header renderer */
    private TableCellRenderer headerRenderer = (new JTableHeader()).getDefaultRenderer();

    /** Null panel is used if cell value is null */
    private NullPanel nullPanel;

    /** Two-tier cache for property panels
     * Map<TreeNode, WeakHashMap<Node.Property, Reference<FocusedPropertyPanel>> */
    private Map panelCache = new WeakHashMap(); // weak! #31275
    private FocusedPropertyPanel renderer = null;
    private PropertyPanel editor = null;

    public TableSheetCell(NodeTableModel tableModel) {
        this.tableModel = tableModel;
        setFlat(false);
    }

    /**
     * Set how to paint renderer.
     * @param f <code>true</code> means flat, <code>false</code> means with button border
     */
    public void setFlat(boolean f) {
        Color controlDkShadow = Color.lightGray;

        if (UIManager.getColor("controlDkShadow") != null) {
            controlDkShadow = UIManager.getColor("controlDkShadow"); // NOI18N
        }

        Color controlLtHighlight = Color.black;

        if (UIManager.getColor("controlLtHighlight") != null) {
            controlLtHighlight = UIManager.getColor("controlLtHighlight"); // NOI18N
        }

        Color buttonFocusColor = Color.blue;

        if (UIManager.getColor("Button.focus") != null) {
            buttonFocusColor = UIManager.getColor("Button.focus"); // NOI18N
        }

        flat = f ? Boolean.TRUE : Boolean.FALSE;
    }

    /** Returns <code>null<code>.
     * @return <code>null</code>
     */
    public Object getCellEditorValue() {
        return null;
    }

    /** Returns editor of property.
     * @param table
     * @param value
     * @param isSelected
     * @param r row
     * @param c column
     * @return <code>PropertyPanel</code>
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
        prop = (Property) value;
        node = tableModel.nodeForRow(r);
        node.addPropertyChangeListener(this);
        tableModel.addTableModelListener(this);

        // create property panel
        PropertyPanel propPanel = getEditor(prop, node);

        propPanel.setBackground(table.getSelectionBackground());
        propPanel.setForeground(table.getSelectionForeground());

        //Fix for 35534, text shifts when editing.  Maybe better fix possible
        //in EditablePropertyDisplayer or InplaceEditorFactory.
        propPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, table.getSelectionBackground()));

        return propPanel;
    }

    /** Cell should not be selected
     * @param ev event
     * @return <code>false</code>
     */
    public boolean shouldSelectCell(EventObject ev) {
        return true;
    }

    /** Return true.
     * @param e event
     * @return <code>true</code>
     */
    public boolean isCellEditable(EventObject e) {
        return true;
    }

    /** Forwards node property change to property model
     * @param evt event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        //        stopCellEditing(); //XXX ?
        ((NodeTableModel) tableModel).fireTableDataChanged();
    }

    /**
     * Detaches listeners.
     * Calls <code>fireEditingStopped</code> and returns true.
     * @return true
     */
    public boolean stopCellEditing() {
        if (prop != null) {
            detachEditor();
        }

        return super.stopCellEditing();
    }

    /**
     * Detaches listeners.
     * Calls <code>fireEditingCanceled</code>.
     */
    public void cancelCellEditing() {
        if (prop != null) {
            detachEditor();
        }

        super.cancelCellEditing();
    }

    /** Table has changed. If underlied property was switched then cancel editing.
     * @param e event
     */
    public void tableChanged(TableModelEvent e) {
        cancelCellEditing();
    }

    /** Removes listeners and frees resources.
     */
    private void detachEditor() {
        node.removePropertyChangeListener(this);
        tableModel.removeTableModelListener(this);
        node = null;
        prop = null;
    }

    private FocusedPropertyPanel getRenderer(Property p, Node n) {
        TTVEnvBridge bridge = TTVEnvBridge.getInstance(this);
        bridge.setCurrentBeans(new Node[] { n });

        if (renderer == null) {
            renderer = new FocusedPropertyPanel(p, PropertyPanel.PREF_READ_ONLY | PropertyPanel.PREF_TABLEUI);
            renderer.putClientProperty("beanBridgeIdentifier", this); //NOI18N
        }

        renderer.setProperty(p);
        renderer.putClientProperty("flat", Boolean.TRUE);

        return renderer;
    }

    /** Getter for actual cell renderer.
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return <code>PropertyPanel</code>
     */
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
    ) {
        // Header renderer
        if (row == -1) {
            Component comp = headerRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                );

            if (comp instanceof JComponent) {
                String tip = (column > 0) ? tableModel.propertyForColumn(column).getShortDescription()
                                          : table.getColumnName(0);
                ((JComponent) comp).setToolTipText(tip);
            }

            return comp;
        }

        Property prop = (Property) value;
        Node node = tableModel.nodeForRow(row);

        if (prop != null) {
            FocusedPropertyPanel propPanel = getRenderer(prop, node);
            propPanel.setFocused(hasFocus);

            String tooltipText = null;

            try {
                Object tooltipValue = prop.getValue();

                if (null != tooltipValue) {
                    tooltipText = tooltipValue.toString();
                }
            } catch (IllegalAccessException eaE) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, eaE);
            } catch (InvocationTargetException itE) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, itE);
            }

            propPanel.setToolTipText(createHtmlTooltip(tooltipText, propPanel.getFont()));
            propPanel.setOpaque(true);

            if (isSelected) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                boolean tableHasFocus = (table == focusOwner) || table.isAncestorOf(focusOwner) ||
                    (focusOwner instanceof Container && ((Container) focusOwner).isAncestorOf(table));

                if ((table == focusOwner) && table.isEditing()) {
                    //XXX really need to check if the editor has focus
                    tableHasFocus = true;
                }

                propPanel.setBackground(
                    tableHasFocus ? table.getSelectionBackground() : TreeTable.getUnfocusedSelectedBackground()
                );

                propPanel.setForeground(
                    tableHasFocus ? table.getSelectionForeground() : TreeTable.getUnfocusedSelectedForeground()
                );
            } else {
                propPanel.setBackground(table.getBackground());
                propPanel.setForeground(table.getForeground());
            }

            return propPanel;
        }

        if (nullPanel == null) {
            nullPanel = new NullPanel(node);
            nullPanel.setOpaque(true);
        } else {
            nullPanel.setNode(node);
        }

        if (isSelected) {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

            boolean tableHasFocus = hasFocus || (table == focusOwner) || table.isAncestorOf(focusOwner) ||
                (focusOwner instanceof Container && ((Container) focusOwner).isAncestorOf(table));

            nullPanel.setBackground(
                tableHasFocus ? table.getSelectionBackground() : TreeTable.getUnfocusedSelectedBackground()
            );

            //XXX may want to handle inverse theme here and use brighter if
            //below a threshold.  Deferred to centralized color management
            //being implemented.
            nullPanel.setForeground(table.getSelectionForeground().darker());
        } else {
            nullPanel.setBackground(table.getBackground());
            nullPanel.setForeground(table.getForeground());
        }

        nullPanel.setFocused(hasFocus);

        return nullPanel;
    }

    private PropertyPanel getEditor(Property p, Node n) {
        int prefs = PropertyPanel.PREF_TABLEUI;

        TTVEnvBridge bridge = TTVEnvBridge.getInstance(this);

        //workaround for issue 38132 - use env bridge to pass the 
        //node to propertypanel so it can call PropertyEnv.setBeans()
        //with it.  The sad thing is almost nobody uses PropertyEnv.getBeans(),
        //but we have to do it for all cases.
        bridge.setCurrentBeans(new Node[] { n });

        if (editor == null) {
            editor = new PropertyPanel(p, prefs);

            editor.putClientProperty("flat", Boolean.TRUE); //NOI18N
            editor.putClientProperty("beanBridgeIdentifier", this); //NOI18N

            editor.setProperty(p);

            return editor;
        }

        editor.setProperty(p);

        //Okay, the property panel has already grabbed the beans, clear
        //them so no references are held.
        return editor;
    }

    private PropertyPanel obtainPanel(Node node, Property prop) {
        return getEditor(prop, node);
    }

    private static String getString(String key) {
        return NbBundle.getBundle(TableSheetCell.class).getString(key);
    }

    /**
     * HTML-ize a tooltip, splitting long lines. It's package private for unit
     * testing.
     */
    static String createHtmlTooltip(String value, Font font) {
        if (value == null) {
            return "null"; // NOI18N
        }

        // break up massive tooltips
        String token = null;

        if (value.indexOf(" ") != -1) { //NOI18N
            token = " "; //NOI18N
        } else if (value.indexOf(",") != -1) { //NOI18N
            token = ","; //NOI18N
        } else if (value.indexOf(";") != -1) { //NOI18N
            token = ";"; //NOI18N
        } else if (value.indexOf("/") != -1) { //NOI18N
            token = "/"; //NOI18N
        } else if (value.indexOf(">") != -1) { //NOI18N
            token = ">"; //NOI18N
        } else if (value.indexOf("\\") != -1) { //NOI18N
            token = "\\"; //NOI18N
        } else {
            //give up
            return makeDisplayble(value, font);
        }

        StringTokenizer tk = new StringTokenizer(value, token, true);

        StringBuffer sb = new StringBuffer(value.length() + 20);
        sb.append("<html>"); //NOI18N

        int charCount = 0;
        int lineCount = 0;

        while (tk.hasMoreTokens()) {
            String a = tk.nextToken();
            a = makeDisplayble(a, font);
            charCount += a.length();
            sb.append(a);

            if (tk.hasMoreTokens()) {
                charCount++;
            }

            if (charCount > 80) {
                sb.append("<br>"); //NOI18N
                charCount = 0;
                lineCount++;

                if (lineCount > 10) {
                    //Don't let things like VCS variables create
                    //a tooltip bigger than the screen. 99% of the
                    //time this is not a problem.
                    sb.append(NbBundle.getMessage(TableSheetCell.class, "MSG_ELLIPSIS")); //NOI18N

                    return sb.toString();
                }
            }
        }

        sb.append("</html>"); //NOI18N

        return sb.toString();
    }

    /**
     * Makes the given String displayble. Probably there doesn't exists
     * perfect solution for all situation. (someone prefer display those
     * squares for undisplayable chars, someone unicode placeholders). So lets
     * try do the best compromise.
     */
    private static String makeDisplayble(String str, Font f) {
        if (null == str) {
            return str;
        }

        if (null == f) {
            f = new JLabel().getFont();
        }

        StringBuffer buf = new StringBuffer((int) (str.length() * 1.3)); // x -> \u1234
        char[] chars = str.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
            case '\t':
                buf.append("&nbsp;&nbsp;&nbsp;&nbsp;" + // NOI18N
                    "&nbsp;&nbsp;&nbsp;&nbsp;"
                ); // NOI18N

            case '\n':
                break;

            case '\r':
                break;

            case '\b':
                buf.append("\\b");

                break; // NOI18N

            case '\f':
                buf.append("\\f");

                break; // NOI18N

            default:

                if (!processHtmlEntity(buf, c)) {
                    if ((null == f) || f.canDisplay(c)) {
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N

                        String hex = Integer.toHexString(c);

                        for (int j = 0; j < (4 - hex.length()); j++)
                            buf.append('0');

                        buf.append(hex);
                    }
                }
            }
        }

        return buf.toString();
    }

    private static boolean processHtmlEntity(StringBuffer buf, char c) {
        switch (c) {
        case '>':
            buf.append("&gt;");

            break; // NOI18N

        case '<':
            buf.append("&lt;");

            break; // NOI18N

        case '&':
            buf.append("&amp;");

            break; // NOI18N

        default:
            return false;
        }

        return true;
    }

    private static class NullPanel extends JPanel {
        private WeakReference weakNode;
        private boolean focused = false;

        NullPanel(Node node) {
            this.weakNode = new WeakReference(node);
        }

        void setNode(Node node) {
            this.weakNode = new WeakReference(node);
        }

        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleNullPanel();
            }

            return accessibleContext;
        }

        public void setFocused(boolean val) {
            focused = val;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (focused) {
                Color bdr = UIManager.getColor("Tree.selectionBorderColor"); //NOI18N

                if (bdr == null) {
                    //Button focus color doesn't work on win classic - better to
                    //get the color from a value we know will work - Tim
                    if (getForeground().equals(Color.BLACK)) { //typical
                        bdr = getBackground().darker();
                    } else {
                        bdr = getForeground().darker();
                    }
                }

                g.setColor(bdr);
                g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                g.setColor(bdr);
            }
        }

        public void addComponentListener(java.awt.event.ComponentListener l) {
            //do nothing
        }

        public void addHierarchyListener(java.awt.event.HierarchyListener l) {
            //do nothing
        }

        public void repaint() {
            //do nothing
        }

        public void repaint(int x, int y, int width, int height) {
            //do nothing
        }

        public void invalidate() {
            //do nothing
        }

        public void revalidate() {
            //do nothing
        }

        public void validate() {
            //do nothing
        }

        public void firePropertyChange(String s, Object a, Object b) {
            //do nothing
        }

        private class AccessibleNullPanel extends AccessibleJPanel {
            AccessibleNullPanel() {
            }

            public String getAccessibleName() {
                String name = super.getAccessibleName();

                if (name == null) {
                    name = getString("ACS_NullPanel");
                }

                return name;
            }

            public String getAccessibleDescription() {
                String description = super.getAccessibleDescription();

                if (description == null) {
                    Node node = (Node) weakNode.get();

                    if (node != null) {
                        description = MessageFormat.format(
                                getString("ACSD_NullPanel"), new Object[] { node.getDisplayName() }
                            );
                    }
                }

                return description;
            }
        }
    }

    /** Table cell renderer component. Paints focus border on property panel. */
    private static class FocusedPropertyPanel extends PropertyPanel {
        //XXX delete this class when new property panel is committed
        boolean focused;

        public FocusedPropertyPanel(Property p, int preferences) {
            super(p, preferences);
        }

        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        public void addComponentListener(java.awt.event.ComponentListener l) {
            //do nothing
        }

        public void addHierarchyListener(java.awt.event.HierarchyListener l) {
            //do nothing
        }

        public void repaint(long tm, int x, int y, int width, int height) {
            //do nothing
        }

        public void revalidate() {
            //do nothing
        }

        public void firePropertyChange(String s, Object a, Object b) {
            //do nothing
            if ("flat".equals(s)) {
                super.firePropertyChange(s, a, b);
            }
        }

        public boolean isValid() {
            return true;
        }

        public boolean isShowing() {
            return true;
        }

        public void update(Graphics g) {
            //do nothing
        }

        public void paint(Graphics g) {
            //do this for self-painting editors in Options window - because
            //we've turned off most property changes, the background won't be
            //painted correctly otherwise
            Color c = getBackground();
            Color old = g.getColor();
            g.setColor(c);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(old);

            super.paint(g);

            if (focused) {
                Color bdr = UIManager.getColor("Tree.selectionBorderColor"); //NOI18N

                if (bdr == null) {
                    //Button focus color doesn't work on win classic - better to
                    //get the color from a value we know will work - Tim
                    if (getForeground().equals(Color.BLACK)) { //typical
                        bdr = getBackground().darker();
                    } else {
                        bdr = getForeground().darker();
                    }
                }

                g.setColor(bdr);
                g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            }

            g.setColor(old);
        }

        ////////////////// Accessibility support ///////////////////////////////
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleFocusedPropertyPanel();
            }

            return accessibleContext;
        }

        private class AccessibleFocusedPropertyPanel extends AccessibleJComponent {
            AccessibleFocusedPropertyPanel() {
            }

            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.PANEL;
            }

            public String getAccessibleName() {
                FeatureDescriptor fd = ((ExPropertyModel) getModel()).getFeatureDescriptor();
                PropertyEditor editor = getPropertyEditor();

                return MessageFormat.format(
                    getString("ACS_PropertyPanelRenderer"),
                    new Object[] { fd.getDisplayName(), (editor == null) ? getString("CTL_No_value") : editor.getAsText() }
                );
            }

            public String getAccessibleDescription() {
                FeatureDescriptor fd = ((ExPropertyModel) getModel()).getFeatureDescriptor();
                Node node = (Node) ((ExPropertyModel) getModel()).getBeans()[0];
                Class clazz = getModel().getPropertyType();

                return MessageFormat.format(
                    getString("ACSD_PropertyPanelRenderer"),
                    new Object[] {
                        fd.getShortDescription(), (clazz == null) ? getString("CTL_No_type") : clazz.getName(),
                        node.getDisplayName()
                    }
                );
            }
        }
    }
}
