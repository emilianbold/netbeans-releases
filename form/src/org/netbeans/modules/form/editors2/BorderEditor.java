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


package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.*;
import java.text.MessageFormat;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.openide.awt.SplittedPanel;
import org.openide.nodes.*;
import org.openide.explorer.view.ListView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.explorer.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.compat2.border.*;
import org.netbeans.modules.form.palette.*;

/**
 * A property editor for swing border class.
 *
 * This editor should be in some subpackage under developerx package,
 * but it is not possible now, because this package is only package where are
 * property editors searched.
 *
 * @author Petr Hamernik
 */
public final class BorderEditor extends PropertyEditorSupport
                                implements FormAwareEditor, XMLPropertyEditor,
                                           org.netbeans.modules.form.NamedPropertyEditor {

    /** Icon bases for unknown border node. */
    private static final String UNKNOWN_BORDER_BASE = "org/netbeans/modules/form/editors2/unknownBorder"; // NOI18N
    /** Icon bases for no border node. */
    private static final String NO_BORDER_BASE = "org/netbeans/modules/form/editors2/nullBorder"; // NOI18N

    private static final ResourceBundle bundle = NbBundle.getBundle(BorderEditor.class);

    private static final String NO_BORDER = bundle.getString("LAB_NoBorder");
    private static final MessageFormat UNKNOWN_BORDER = new MessageFormat(bundle.getString("LAB_FMT_UnknownBorder"));

    // --------------
    // variables

    private Object current;

    private FormModel formModel;
    private FormPropertyContext propertyContext;
    private boolean needsUpdate;
    private BorderDesignSupport borderSupport;

    // customizer
    private BorderPanel bPanel;

    // --------------
    // init

    public BorderEditor() {
        bPanel = null;
        current = null;
        needsUpdate = false;
    }

    // FormAwareEditor implementation
    public void setFormModel(FormModel model) {
        formModel = model;
        propertyContext = new FormPropertyContext.DefaultImpl(model);
    }

    // ------------------
    // main methods

    public Object getValue() {
        if (needsUpdate) {
            current = borderSupport;
            needsUpdate = false;
        }

        return current;
    }

    public void setValue(Object value) {
        current = value;
        borderSupport = null;

        if (value instanceof BorderDesignSupport) {
            try {
                borderSupport = new BorderDesignSupport((BorderDesignSupport)value);
            } catch (InstantiationException ex) {
                ex.printStackTrace(); // it shouldn't happen
            } catch (IllegalAccessException ex) {
                ex.printStackTrace(); // it shouldn't happen
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace(); // it shouldn't happen
            } catch (InvocationTargetException ex) {
                ex.printStackTrace(); // it shouldn't happen
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace(); // it shouldn't happen
            }
        } else if (value instanceof Border) {
            if (!(value instanceof javax.swing.plaf.UIResource))
                borderSupport = new BorderDesignSupport((Border)value);
        }
        else if (value instanceof BorderInfo)
            borderSupport = new BorderDesignSupport((BorderInfo)value);

        if (borderSupport != null) {
            borderSupport.setPropertyContext(propertyContext);
            needsUpdate = false;

            if (bPanel != null)
                bPanel.setValue(value);
        }
    }

    public String getAsText() {
        return null; 
        // should not return any text, because border is not editable as a text
    }

    public void setAsText(String string) {
    }

    public boolean isPaintable() {
        return true;
    }

    public void paintValue(Graphics g, Rectangle rectangle) {
        String valueText;
        Object value = getValue();

        if (value == null)
            valueText = NO_BORDER;
        else if (borderSupport != null) //value instanceof BorderDesignSupport) // || value instanceof Border)
            valueText = "[" + borderSupport.getDisplayName() + "]";
        else
            valueText = "[" + org.openide.util.Utilities.getShortClassName(
                            value.getClass()) + "]";

        FontMetrics fm = g.getFontMetrics();
        g.drawString(valueText, rectangle.x,
                       rectangle.y + (rectangle.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    public String getJavaInitializationString() {
        Object value = getValue();
        if (value == null)
            return "null";
        if (borderSupport != null)
            return borderSupport.getJavaInitializationString();

        // nothing to generate otherwise
        return null;
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public Component getCustomEditor() {
        if (bPanel == null) {
            bPanel = new BorderPanel();
        }
        return bPanel;
    }
    
    
    // ------------------------------------------
    // NamedPropertyEditor implementation

    /** @return display name of the property editor */
    public String getDisplayName() {
        return bundle.getString("CTL_BorderEditor_DisplayName");
    }
    

    // --------------------------
    // innerclasses

    final class BorderPanel extends ExplorerPanel
                            implements PropertyChangeListener,
                                       VetoableChangeListener {
        NoBorderNode noBorder;
        UnknownBorderNode unknownBorder;
        Node root;

        static final long serialVersionUID =-2613206277499334010L;

        BorderPanel() {
            ArrayList borders = new ArrayList(10);

            // track changes in properties of selected border
            PropertyChangeListener pListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    updateBorder(getExplorerManager().getSelectedNodes()[0]);
                }
            };

            PaletteItem[] items = CPManager.getDefault().getAllItems();
            for (int i = 0; i < items.length; i++) {
                if (items[i].isBorder()) {
                    try {
                        BorderListNode listNode = new BorderListNode(
                                                        items[i], formModel);
                        listNode.addPropertyChangeListener(pListener);
                        borders.add(listNode);
                    } 
                    catch (Exception e) { // ignore, do not add to the list
                        e.printStackTrace();
                    } 
                }
            }

            root = new AbstractNode(new Children.Array());
            noBorder = new NoBorderNode();
            root.getChildren().add(new Node[] { noBorder });

            Node[] bordersArray = new Node[borders.size()];
            borders.toArray(bordersArray);
            Arrays.sort(bordersArray, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((Node)o1).getDisplayName().compareTo(
                             ((Node)o2).getDisplayName());
                }
            });
            root.getChildren().add(bordersArray);

            getExplorerManager().setRootContext(root);
            getExplorerManager().addPropertyChangeListener(this);
            getExplorerManager().addVetoableChangeListener(this);

            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(5, 5, 5, 5));

            SplittedPanel split = new SplittedPanel();
            split.setSplitType(SplittedPanel.VERTICAL);
            split.setSplitAbsolute(false);
            split.setSplitPosition(45);

            ListView listView = new ListView();
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new EtchedBorder(),
                                             bundle.getString("LAB_AvailableBorders")));
            panel.setLayout(new BorderLayout());
            panel.add(BorderLayout.CENTER, listView);
            split.add(panel, SplittedPanel.ADD_TOP);

            PropertySheetView sheetView = new PropertySheetView();
            split.add(sheetView, SplittedPanel.ADD_BOTTOM);

            add(BorderLayout.CENTER, split);
        }

        void setValue(Object border) {
            if (border == null) {
                try {
                    getExplorerManager().setSelectedNodes(new Node[] { noBorder });
                } 
                catch (PropertyVetoException e) {
                }
                return;
            }

//            if (realBorder || border instanceof BorderDesignSupport
//                              || border instanceof BorderInfo)
            if (borderSupport != null) {
                boolean realBorder = border instanceof Border;
                Class borderClass = borderSupport.getBorderClass();
                Node[] nodes = root.getChildren().getNodes();

                for (int i = 0; i < nodes.length; i++) {
                    if (nodes[i] instanceof BorderListNode) {
                        BorderListNode borderNode = (BorderListNode)nodes[i];
                        if (borderNode.getBorderSupport().getBorderClass()
                                                              == borderClass) {
                            if (!realBorder)
                                borderNode.setBorderSupport(borderSupport);
                            else
                                borderNode.getBorderSupport().setBorder((Border)border);

                            if (unknownBorder != null) {
                                root.getChildren().remove(new Node[] { unknownBorder });
                                unknownBorder = null;
                            }
                            try {
                                getExplorerManager().setSelectedNodes(new Node[] { borderNode });
                            } 
                            catch (PropertyVetoException e) { // should not happen
                            }
                            return;
                        }
                    }
                }
            }

            if (unknownBorder != null)
                unknownBorder.setBorder(border);
            else {
                unknownBorder = new UnknownBorderNode(border);
                root.getChildren().add(new Node[] { unknownBorder });
                try {
                    getExplorerManager().setSelectedNodes(new Node[] { unknownBorder });
                } 
                catch (PropertyVetoException e) {
                }
            }
        }

        /** Prepares update (re-creation) of BorderDesignSupport object according to
         * changes. This is needed when another border was selected or some
         * property of currently selected border was changed.
         */
        void updateBorder(Node node) {
            if (node instanceof NoBorderNode) {
                BorderEditor.this.borderSupport = null;
                needsUpdate = true;
            }
            else if (node instanceof UnknownBorderNode) {
                BorderEditor.this.current = ((UnknownBorderNode)node).getBorder();
                needsUpdate = false;
            }
            else {
                BorderEditor.this.borderSupport = ((BorderListNode)node).getBorderSupport();
                needsUpdate = true;
            }
            BorderEditor.this.firePropertyChange(); // why??
        }

        // track changes in nodes selection
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes = (Node[]) evt.getNewValue();
                if (nodes.length == 1)
                    updateBorder(nodes[0]);
                else if (nodes.length == 0)
                    try {
                        getExplorerManager().setSelectedNodes(new Node[] { noBorder });
                    } 
                    catch (PropertyVetoException e) {
                    }
            }
        }

        // only one border can be selected
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes =(Node[]) evt.getNewValue();
                if (nodes.length != 1)
                    throw new PropertyVetoException("", evt); // NOI18N
            }
        }

        public org.openide.util.HelpCtx getHelpCtx() {
            return new HelpCtx(BorderPanel.class);
        }

        public Dimension getPreferredSize() {
            return new Dimension(360, 440);
        }
    }

    static final class BorderListNode extends AbstractNode implements PropertyChangeListener {
        private BorderDesignSupport borderSupport;
        private FormPropertyContext propertyContext;
        private Node palItemNode;

        BorderListNode(PaletteItem palItem, FormModel model)
            throws InstantiationException,
                   IllegalAccessException {
//                   IllegalArgumentException,
//                   java.lang.reflect.InvocationTargetException {

            super(Children.LEAF);

            Object border = palItem.createInstance();
            if (border instanceof Border)
                borderSupport = new BorderDesignSupport((Border)border);
            else if (border instanceof BorderInfo)
                borderSupport = new BorderDesignSupport((BorderInfo)border);
            else // should not happen
                throw new IllegalArgumentException();

            propertyContext = new FormPropertyContext.DefaultImpl(model);
            borderSupport.setPropertyContext(propertyContext);

            setName(borderSupport.getDisplayName());
            palItemNode = palItem.getItemNode();
        }

        /** Find an icon for this node (in the closed state).
         * @param type constant from {@link java.beans.BeanInfo}
         * @return icon to use to represent the node
         */
        public Image getIcon(int type) {
            return palItemNode.getIcon(type);
        }

        /** Find an icon for this node (in the open state).
         * This icon is used when the node may have children and is expanded.
         * @param type constant from {@link java.beans.BeanInfo}
         * @return icon to use to represent the node when open
         */
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        /** Creates property set for this node. */
        protected Sheet createSheet() {
            Node.Property[] props = borderSupport.getProperties();
            Sheet.Set propsSet = Sheet.createPropertiesSet();
            propsSet.put(props);
            Sheet sheet = new Sheet();
            sheet.put(propsSet);

            for (int i=0; i < props.length; i++) {
                if (props[i] instanceof FormProperty)
                    ((FormProperty)props[i]).addPropertyChangeListener(this);
                else // compatibility with BorderInfo borders etc.
                    if (props[i] instanceof BorderInfoSupport.BorderProp)
                        ((BorderInfoSupport.BorderProp)props[i]).setPropertyChangeListener(this);
            }

            return sheet;
        }

        public BorderDesignSupport getBorderSupport() {
            return borderSupport;
        }

        public void setBorderSupport(BorderDesignSupport borderSupport) {
            borderSupport.setPropertyContext(propertyContext);
            this.borderSupport = borderSupport;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // update the border
            firePropertyChange("", null, null); // NOI18N
        }
    }

    static final class NoBorderNode extends AbstractNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 3454994916520236035L;

        NoBorderNode() {
            super(Children.LEAF);
            setDisplayName(NO_BORDER);
            setIconBase(NO_BORDER_BASE);
        }

    }

    static final class UnknownBorderNode extends AbstractNode {
        static final long serialVersionUID = 3063018048992659100L;

        private Object border;

        UnknownBorderNode(Object border) {
            super(Children.LEAF);
            setBorder(border);
            setIconBase(UNKNOWN_BORDER_BASE);
        }

        void setBorder(Object border) {
            this.border = border;
            String longName = border.getClass().getName();
            int dot = longName.lastIndexOf('.');
            String shortName =(dot < 0) ? longName : longName.substring(dot + 1);
            setDisplayName(UNKNOWN_BORDER.format(new Object[] { longName, shortName }));
        }

        Object getBorder() {
            return border;
        }

    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    private static final String XML_BORDER = "Border"; // NOI18N
    private static final String ATTR_INFO = "info"; // NOI18N
    private static final String PROP_NAME = "PropertyName"; // NOI18N

    /** Called to store current property value into XML subtree.
     * @param doc The XML document to store the XML in - should be used for
     *            creating nodes only
     * @return the XML DOM element representing a subtree of XML from which
               the value should be loaded
     */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        Object value = getValue();
        if ((value instanceof BorderDesignSupport || value instanceof Border)
                 && borderSupport != null) {
            org.w3c.dom.Node storedNode = null;
            BorderInfo bInfo = borderSupport.getBorderInfo();

            if (bInfo != null) {
                org.w3c.dom.Node mainNode = createBorderInfoNode(doc,
                                              bInfo.getClass().getName());
                org.w3c.dom.Node borderNode = bInfo.storeToXML(doc);
                if (borderNode != null) {
                    mainNode.appendChild(borderNode);
                    storedNode = mainNode;
                }
            }
            else {
                // we must preserve backward compatibility of storing standard
                // swing borders (for which BorderInfo classes were used sooner)
                Class borderClass = borderSupport.getBorderClass();

                if (borderClass.isAssignableFrom(TitledBorder.class))
                    storedNode = storeTitledBorder(doc);
                else if (borderClass.isAssignableFrom(EtchedBorder.class))
                    storedNode = storeEtchedBorder(doc);
                else if (borderClass.isAssignableFrom(LineBorder.class))
                    storedNode = storeLineBorder(doc);
                else if (borderClass.isAssignableFrom(EmptyBorder.class))
                    storedNode = storeEmptyBorder(doc);
                else if (borderClass.isAssignableFrom(CompoundBorder.class))
                    storedNode = storeCompoundBorder(doc);
                else if (SoftBevelBorder.class.isAssignableFrom(borderClass))
                    storedNode = storeBevelBorder(doc, ID_BI_SOFTBEVEL);
                else if (BevelBorder.class.isAssignableFrom(borderClass))
                    storedNode = storeBevelBorder(doc, ID_BI_BEVEL);
                else if (borderClass.isAssignableFrom(MatteBorder.class))
                    storedNode = storeMatteBorder(doc);

                // no other way of storing to XML ...
            }

            return storedNode;
        }

        else if (value == null) {
            return storeNullBorder(doc);
        }

        return null; // cannot be saved
    }

    /** Called to load property value from specified XML subtree.
     * If succesfully loaded, the value should be available via getValue().
     * An IOException should be thrown when the value cannot be restored from
     * the specified XML element
     * @param element the XML DOM element representing a subtree of XML from
     *                which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from
                  the specified XML element
     */
    public void readFromXML(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        try {
            org.w3c.dom.Node readNode = null;
            org.w3c.dom.NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
                if (children.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    readNode = children.item(i);
                    break;
                }
            if (readNode == null) return;
            
            String infoName = attributes.getNamedItem(ATTR_INFO).getNodeValue();

            if (ID_BI_TITLED.equals(infoName))
                readTitledBorder(readNode);
            else if (ID_BI_ETCHED.equals(infoName))
                readEtchedBorder(readNode);
            else if (ID_BI_LINE.equals(infoName))
                readLineBorder(readNode);
            else if (ID_BI_EMPTY.equals(infoName))
                readEmptyBorder(readNode);
            else if (ID_BI_COMPOUND.equals(infoName))
                readCompoundBorder(readNode);
            else if (ID_BI_SOFTBEVEL.equals(infoName))
                readBevelBorder(readNode, SoftBevelBorder.class);
            else if (ID_BI_BEVEL.equals(infoName))
                readBevelBorder(readNode, BevelBorder.class);
            else if (ID_BI_MATTECOLOR.equals(infoName)
                     || ID_BI_MATTEICON.equals(infoName))
                readMatteBorder(readNode);
            else if (ID_BI_NULL_BORDER.equals(infoName)) { // no border
                borderSupport = null;
                needsUpdate = true;
            }
            else { // read as BorderInfo
                BorderInfo bInfo = (BorderInfo)PersistenceObjectRegistry
                                                      .createInstance(infoName);
                bInfo.readFromXML(readNode);
                borderSupport = new BorderDesignSupport(bInfo);
                needsUpdate = true;
            }
            // no other way of reading from XML
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }

    // ------------------------------
    // helper storing/reading methods

    private org.w3c.dom.Element createBorderInfoNode(org.w3c.dom.Document doc,
                                                     String name) {
        org.w3c.dom.Element el = doc.createElement(XML_BORDER);
        el.setAttribute(ATTR_INFO, name);
        return el;
    }

    private static void writeProperty(String propName, FormProperty prop,
                                      org.w3c.dom.Element el,
                                      org.w3c.dom.Document doc) {
        boolean written = false;
        org.w3c.dom.Node valueNode = null;

        PropertyEditor propEd = prop.getCurrentEditor();
        if (propEd instanceof XMLPropertyEditor) {
            Object value;
            try {
                value = prop.getValue();
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace(); // problem getting value => ignore
                return;
            }
            propEd.setValue(value);
            valueNode = ((XMLPropertyEditor)propEd).storeToXML(doc);
            if (valueNode != null) {
                el.appendChild(valueNode);
                if (valueNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                    ((org.w3c.dom.Element)valueNode).setAttribute(PROP_NAME, propName);
                return;
            }
        }

        // writing property to XML didn't succeed (should not happen
        // for standard swing borders)
        try {
            String encodedSerializeValue = GandalfPersistenceManager
                                                 .encodeValue(prop.getValue());
            if (encodedSerializeValue != null)
                el.setAttribute(propName, encodedSerializeValue);
        }
        catch (Exception ex) { // should not happen
        }
    }

    private static Object readProperty(String xmlPropName,
                                       String borderPropName,
                                       BorderDesignSupport bSupport,
                                       org.w3c.dom.Node element)
                                                throws java.io.IOException {
        boolean valueRead = false;
        Object value = null;
        org.w3c.dom.Node propNode = null;
        org.w3c.dom.NodeList items = element.getChildNodes();

        for (int i = 0; i < items.getLength(); i++)
            if (items.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                && ((org.w3c.dom.Element)items.item(i)).getAttribute(PROP_NAME)
                                                         .equals(xmlPropName)) {
                propNode = items.item(i);
                break;
            }

        if (propNode != null) { // node found
            FormProperty prop = (FormProperty)bSupport.getPropertyOfName(borderPropName);
            if (prop == null) return null;

            java.io.IOException lastEx = null;
            PropertyEditor editors[] = FormPropertyEditorManager.getAllEditors(
                                                           prop.getValueType());

            for (int i=0; i < editors.length && !valueRead; i++) {
                PropertyEditor prEd = editors[i];
                if (prEd instanceof XMLPropertyEditor) {
                    try {
                        prop.getPropertyContext().initPropertyEditor(prEd);
                        ((XMLPropertyEditor)prEd).readFromXML(propNode);
                        value = prEd.getValue();
                        prop.setValue(value);
                        prop.setCurrentEditor(prEd);
                        valueRead = true;
                    }
                    catch (java.io.IOException e1) {
                        lastEx = e1;
                    }
                    catch (Exception e2) { // should not happen
                        e2.printStackTrace();
                    }
                }
            }
            if (!valueRead && lastEx != null) throw lastEx;
        }
        else { // node not found, try attribute (in the element) with encoded
               // serialized value (should not happen for standard swing borders)
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node attr = attributes == null ? null :
                                    attributes.getNamedItem(xmlPropName);
            if (attr != null) { // ... but it happened :)
                String valueText = attr.getNodeValue();
                if (valueText != null) {
                    FormProperty prop = (FormProperty)bSupport
                                            .getPropertyOfName(borderPropName);
                    if (prop != null && (value = GandalfPersistenceManager
                                            .decodeValue(valueText)) != null)
                        try {
                            prop.setValue(value);
                        }
                        catch (Exception ex) { // should not happen
                            ex.printStackTrace();
                        }
                }
            }
        }

        return value;
    }

    // -----------------
    // No border (null)

    private static final String ID_BI_NULL_BORDER = "null"; // NOI18N

    private org.w3c.dom.Node storeNullBorder(org.w3c.dom.Document doc) {
        try {
            return createBorderInfoNode(doc, ID_BI_NULL_BORDER);
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // TitledBorder XML persistence - compatible with former TitledBorderInfo

    private static final String XML_TITLED_BORDER = "TitledBorder"; // NOI18N
    private static final String ID_BI_TITLED // "ID" of former TitledBorderInfo
        = "org.netbeans.modules.form.compat2.border.TitledBorderInfo"; // NOI18N

    private static final String ATTR_TITLE = "title"; // NOI18N
    private static final String ATTR_TITLE_X = "titleX"; // NOI18N
    private static final String ATTR_BORDER = "innerBorder"; // NOI18N
    private static final String ATTR_JUSTIFICATION = "justification"; // NOI18N
    private static final String ATTR_POSITION = "position"; // NOI18N
    private static final String ATTR_FONT = "font"; // NOI18N
    private static final String ATTR_TITLE_COLOR = "color"; // NOI18N

    private org.w3c.dom.Node storeTitledBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_TITLED_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("border");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_BORDER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("title");
            if (prop != null && prop.isChanged()) {
                Object realValue = prop.getRealValue();
                el.setAttribute(ATTR_TITLE, realValue instanceof String ?
                                            (String)realValue : "");

                Object value = prop.getValue();
                if (value instanceof FormDesignValue)
                    // store also FormDesignValue (for title only)
                    writeProperty(ATTR_TITLE_X, prop, el, doc);
            }

            prop = (FormProperty)borderSupport.getPropertyOfName("titleJustification");
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_JUSTIFICATION, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("titlePosition");
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_POSITION, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("titleFont");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_FONT, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("titleColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_TITLE_COLOR, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_TITLED);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readTitledBorder(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_TITLED_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(TitledBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            readProperty(ATTR_BORDER, "border", borderSupport, element);

            // for title, first try to read FormDesignValue
            Object title = readProperty(ATTR_TITLE_X, "title", borderSupport, element);
            if (title == null // no design value, get simple String attribute
                  && (node = attributes.getNamedItem(ATTR_TITLE)) != null
                  && (prop = (FormProperty)borderSupport
                                          .getPropertyOfName("title")) != null)
                prop.setValue(node.getNodeValue());

            node = attributes.getNamedItem(ATTR_JUSTIFICATION);
            if (node != null && (prop = (FormProperty)borderSupport
                             .getPropertyOfName("titleJustification")) != null)
                prop.setValue(new Integer(node.getNodeValue()));

            node = attributes.getNamedItem(ATTR_POSITION);
            if (node != null && (prop = (FormProperty)borderSupport
                                  .getPropertyOfName("titlePosition")) != null)
                prop.setValue(new Integer(node.getNodeValue()));

            readProperty(ATTR_FONT, "titleFont", borderSupport, element);

            readProperty(ATTR_TITLE_COLOR, "titleColor", borderSupport, element);

            needsUpdate = true;
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }

    // ------------------------------------------------------------------------
    // EtchedBorder XML persistence - compatible with former EtchedBorderInfo

    private static final String XML_ETCHED_BORDER = "EtchetBorder"; // NOI18N
    private static final String ID_BI_ETCHED // "ID" of former EtchedBorderInfo
        = "org.netbeans.modules.form.compat2.border.EtchedBorderInfo"; // NOI18N

    private static final String ATTR_ETCH_TYPE = "bevelType"; // NOI18N
    private static final String ATTR_HIGHLIGHT = "highlight"; // NOI18N
    private static final String ATTR_SHADOW = "shadow"; // NOI18N

    public org.w3c.dom.Node storeEtchedBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_ETCHED_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("etchType");
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_ETCH_TYPE, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("highlightColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_HIGHLIGHT, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("shadowColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_SHADOW, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_ETCHED);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readEtchedBorder(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_ETCHED_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(EtchedBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            node = attributes.getNamedItem(ATTR_ETCH_TYPE);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("etchType")) != null)
                prop.setValue(new Integer(node.getNodeValue()));

            readProperty(ATTR_HIGHLIGHT, "highlightColor", borderSupport, element);

            readProperty(ATTR_SHADOW, "shadowColor", borderSupport, element);

            needsUpdate = true;
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }

    // ------------------------------------------------------------------------
    // LineBorder XML persistence - compatible with former LineBorderInfo

    private static final String XML_LINE_BORDER = "LineBorder"; // NOI18N
    private static final String ID_BI_LINE // "ID" of former LineBorderInfo
        = "org.netbeans.modules.form.compat2.border.LineBorderInfo"; // NOI18N

    private static final String ATTR_THICKNESS = "thickness"; // NOI18N
    private static final String ATTR_LINE_COLOR = "color"; // NOI18N
    private static final String ATTR_CORNERS = "roundedCorners"; // NOI18N

    public org.w3c.dom.Node storeLineBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_LINE_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("lineColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_LINE_COLOR, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("thickness");
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_THICKNESS, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("roundedCorners");
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_CORNERS, prop.getRealValue().toString());

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_LINE);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readLineBorder(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_LINE_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(LineBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            readProperty(ATTR_LINE_COLOR, "lineColor", borderSupport, element);

            node = attributes.getNamedItem(ATTR_THICKNESS);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("thickness")) != null)
                prop.setValue(new Integer(node.getNodeValue()));

            node = attributes.getNamedItem(ATTR_CORNERS);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("roundedCorners")) != null)
                prop.setValue(new Boolean(node.getNodeValue()));

            needsUpdate = true;
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }

    // ------------------------------------------------------------------------
    // EmptyBorder XML persistence - compatible with former EmptyBorderInfo

    private static final String XML_EMPTY_BORDER = "EmptyBorder"; // NOI18N
    private static final String ID_BI_EMPTY // "ID" of former EmptyBorderInfo
        = "org.netbeans.modules.form.compat2.border.EmptyBorderInfo"; // NOI18N

    private static final String ATTR_TOP = "top"; // NOI18N
    private static final String ATTR_LEFT = "left"; // NOI18N
    private static final String ATTR_RIGHT = "right"; // NOI18N
    private static final String ATTR_BOTTOM = "bottom"; // NOI18N

    public org.w3c.dom.Node storeEmptyBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_EMPTY_BORDER);
            FormProperty prop = (FormProperty)borderSupport.getPropertyOfName(
                                                               "borderInsets");
            Object value;
            if (prop != null && prop.isChanged()
                  && (value = prop.getRealValue()) instanceof Insets) {
                Insets insets = (Insets)value;
                el.setAttribute(ATTR_TOP, Integer.toString(insets.top));
                el.setAttribute(ATTR_LEFT, Integer.toString(insets.left));
                el.setAttribute(ATTR_BOTTOM, Integer.toString(insets.bottom));
                el.setAttribute(ATTR_RIGHT, Integer.toString(insets.right));
            }

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_EMPTY);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readEmptyBorder(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_EMPTY_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            int top=1, left=1, bottom=1, right=1;

            if ((node = attributes.getNamedItem(ATTR_TOP)) != null)
                top = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_LEFT)) != null)
                left = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_BOTTOM)) != null)
                bottom = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_RIGHT)) != null)
                right = Integer.parseInt(node.getNodeValue());
            
            borderSupport = new BorderDesignSupport(EmptyBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            if ((top != 1 || left != 1 || bottom != 1 || right != 1)
                  && (prop = (FormProperty)borderSupport
                                   .getPropertyOfName("borderInsets")) != null)
                prop.setValue(new Insets(top,left,bottom,right));

            needsUpdate = true;
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }

    // ------------------------------------------------------------------------
    // CompoundBorder XML persistence - compatible with former CompoundBorderInfo

    private static final String XML_COMPOUND_BORDER = "CompundBorder"; // NOI18N
    private static final String ID_BI_COMPOUND // "ID" of former CompoundBorderInfo
        = "org.netbeans.modules.form.compat2.border.CompoundBorderInfo"; // NOI18N

    private static final String ATTR_OUTSIDE = "outside"; // NOI18N
    private static final String ATTR_INSIDE = "inside"; // NOI18N

    private org.w3c.dom.Node storeCompoundBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_COMPOUND_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("outsideBorder");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_OUTSIDE, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("insideBorder");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_INSIDE, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_COMPOUND);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readCompoundBorder(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_COMPOUND_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            borderSupport = new BorderDesignSupport(CompoundBorder.class);
            borderSupport.setPropertyContext(propertyContext);

            readProperty(ATTR_OUTSIDE, "outsideBorder", borderSupport, element);
            readProperty(ATTR_INSIDE, "insideBorder", borderSupport, element);

            needsUpdate = true;
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }

    // ------------------------------------------------------------------------
    // BevelBorder & SoftBevelBorder XML persistence - compatible with former
    // BevelAbstractBorderInfo

    private static final String XML_BEVEL_BORDER = "BevelBorder"; // NOI18N
    private static final String ID_BI_BEVEL // "ID" of former BevelBorderInfo
        = "org.netbeans.modules.form.compat2.border.BevelBorderInfo"; // NOI18N
    private static final String ID_BI_SOFTBEVEL // "ID" of former SoftBevelBorderInfo
        = "org.netbeans.modules.form.compat2.border.SoftBevelBorderInfo"; // NOI18N

    private static final String ATTR_BEVEL_TYPE = "bevelType"; // NOI18N
    private static final String ATTR_HIGHLIGHT_OUTER = "highlightOuter"; // NOI18N
    private static final String ATTR_HIGHLIGHT_INNER = "highlightInner"; // NOI18N
    private static final String ATTR_SHADOW_OUTER = "shadowOuter"; // NOI18N
    private static final String ATTR_SHADOW_INNER = "shadowInner"; // NOI18N

    public org.w3c.dom.Node storeBevelBorder(org.w3c.dom.Document doc,
                                             String infoId) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_BEVEL_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("bevelType");
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_BEVEL_TYPE, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("highlightOuterColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_HIGHLIGHT_OUTER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("highlightInnerColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_HIGHLIGHT_INNER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("shadowOuterColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_SHADOW_OUTER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("shadowInnerColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_SHADOW_INNER, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, infoId);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readBevelBorder(org.w3c.dom.Node element, Class borderClass)
    throws java.io.IOException {
        if (!XML_BEVEL_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(borderClass);
            FormProperty prop;

            node = attributes.getNamedItem(ATTR_BEVEL_TYPE);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("bevelType")) != null)
                prop.setValue(new Integer(node.getNodeValue()));

            readProperty(ATTR_HIGHLIGHT_OUTER, "highlightOuterColor", borderSupport, element);
            readProperty(ATTR_HIGHLIGHT_INNER, "highlightInnerColor", borderSupport, element);
            readProperty(ATTR_SHADOW_OUTER, "shadowOuterColor", borderSupport, element);
            readProperty(ATTR_SHADOW_INNER, "shadowInnerColor", borderSupport, element);

            needsUpdate = true;
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }

    // ------------------------------------------------------------------------
    // MatteBorder XML persistence - compatible with former
    // MatteColorBorderInfo and MatteIconBorderInfo

    private static final String XML_MATTE_COLOR_BORDER = "MatteColorBorder"; // NOI18N
    private static final String XML_MATTE_ICON_BORDER = "MatteIconBorder"; // NOI18N
    private static final String ID_BI_MATTECOLOR // "ID" of former MatteColorBorderInfo
        = "org.netbeans.modules.form.compat2.border.MatteColorBorderInfo"; // NOI18N
    private static final String ID_BI_MATTEICON // "ID" of former MatteIconBorderInfo
        = "org.netbeans.modules.form.compat2.border.MatteIconBorderInfo"; // NOI18N

    private static final String ATTR_MATTE_COLOR = "color"; // NOI18N
    private static final String ATTR_MATTE_ICON = "icon"; // NOI18N

    public org.w3c.dom.Node storeMatteBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el;
            String infoId;
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("tileIcon");
            if (prop.isChanged()) {
                el = doc.createElement(XML_MATTE_ICON_BORDER);
                infoId = ID_BI_MATTEICON;
                writeProperty(ATTR_MATTE_ICON, prop, el, doc);
            }
            else {
                el = doc.createElement(XML_MATTE_COLOR_BORDER);
                infoId = ID_BI_MATTECOLOR;
            }

            prop = (FormProperty)borderSupport.getPropertyOfName("matteColor");
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_MATTE_COLOR, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("borderInsets");
            Object value;
            if (prop != null && prop.isChanged()
                  && (value = prop.getRealValue()) instanceof Insets) {
                Insets insets = (Insets)value;
                el.setAttribute(ATTR_TOP, Integer.toString(insets.top));
                el.setAttribute(ATTR_LEFT, Integer.toString(insets.left));
                el.setAttribute(ATTR_BOTTOM, Integer.toString(insets.bottom));
                el.setAttribute(ATTR_RIGHT, Integer.toString(insets.right));
            }

            org.w3c.dom.Node nod = createBorderInfoNode(doc, infoId);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readMatteBorder(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_MATTE_COLOR_BORDER.equals(element.getNodeName())
              && !XML_MATTE_ICON_BORDER.equals(element.getNodeName()))
            throw new java.io.IOException();

        try {
            borderSupport = new BorderDesignSupport(MatteBorder.class);
            borderSupport.setPropertyContext(propertyContext);

            readProperty(ATTR_MATTE_ICON, "tileIcon", borderSupport, element);
            readProperty(ATTR_MATTE_COLOR, "matteColor", borderSupport, element);

            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;
            FormProperty prop;

            int top=1, left=1, bottom=1, right=1;

            if ((node = attributes.getNamedItem(ATTR_TOP)) != null)
                top = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_LEFT)) != null)
                left = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_BOTTOM)) != null)
                bottom = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_RIGHT)) != null)
                right = Integer.parseInt(node.getNodeValue());
            
            if ((top != 1 || left != 1 || bottom != 1 || right != 1)
                  && (prop = (FormProperty)borderSupport
                                   .getPropertyOfName("borderInsets")) != null)
                prop.setValue(new Insets(top,left,bottom,right));

            needsUpdate = true;
        } 
        catch (Exception e) {
            throw new java.io.IOException(e.toString());
        }
    }
}
