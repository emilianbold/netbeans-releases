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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.binding;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import javax.faces.component.UIComponent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;

public class BindingSourcePanel extends JPanel {

    private static final Bundle bundle = Bundle.getBundle(BindingSourcePanel.class);

    JComboBox compCombo = new JComboBox();
    JLabel compLabel = new JLabel();
    DefaultComboBoxModel compComboModel = new DefaultComboBoxModel();
    DefaultListModel propListModel = new DefaultListModel();
    JLabel propLabel = new JLabel();
    JScrollPane propScroll = new JScrollPane();
    JList propList = new JList();
    JRadioButton showDefault = new JRadioButton();
    JRadioButton showAdvanced = new JRadioButton();
    JRadioButton showAll = new JRadioButton();
    JPanel radioPanel = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    ButtonGroup showGroup = new ButtonGroup();
    JTextPane noneText = new JTextPane();

    public BindingSourcePanel() {
        try {
            jbInit();
        }
        catch (Exception ex) {
//            ex.printStackTrace();
        }
        propList.getAccessibleContext().setAccessibleName(bundle.getMessage("SOURCE_PROP_LIST_ACCESS_NAME"));
        propList.getAccessibleContext().setAccessibleDescription(bundle.getMessage("SOURCE_PROP_LIST_ACCESS_DESC"));
        propLabel.setLabelFor(propList);
        showDefault.getAccessibleContext().setAccessibleName(bundle.getMessage("SHOW_DEFAULT_ACCESS_NAME"));
        showDefault.getAccessibleContext().setAccessibleDescription(bundle.getMessage("SHOW_DEFAULT_ACCESS_DESC"));
        showAdvanced.getAccessibleContext().setAccessibleName(bundle.getMessage("SHOW_ADVANCED_ACCESS_NAME"));
        showAdvanced.getAccessibleContext().setAccessibleDescription(bundle.getMessage("SHOW_ADVANCED_ACCESS_DESC"));
        showAll.getAccessibleContext().setAccessibleName(bundle.getMessage("SHOW_ALL_ACCESS_NAME"));
        showAll.getAccessibleContext().setAccessibleDescription(bundle.getMessage("SHOW_ALL_ACCESS_DESC"));
        showDefault.setMnemonic(bundle.getMessage("SHOW_DEFAULT_MNEMONIC").charAt(0));
        showAdvanced.setMnemonic(bundle.getMessage("SHOW_ADVANCED_MNEMONIC").charAt(0));
        showAll.setMnemonic(bundle.getMessage("SHOW_ALL_MNEMONIC").charAt(0));
    }

    protected PropertyBindingPanel bindingPanel;
    public BindingSourcePanel(PropertyBindingPanel bindingPanel) {
        this();
        this.bindingPanel = bindingPanel;
    }

    protected DesignContext showingContext = null;
    public void sourceContextChanged(DesignContext context) {
        if (showingContext != null && showingContext == context) return;
        showingContext = context;
        compComboModel.removeAllElements();
        if (context != null) {
            DesignBean root = context.getRootContainer();
            fillCombo(root.getChildBeans());
        }
    }

    protected DesignBean showingBean = null;
    public void sourceBeanChanged(DesignBean bean) {
        if (showingBean == bean) return;
        showingBean = bean;
        if (bean != null) {
            compCombo.setSelectedItem(bean);
            enumProps();
        }
        else {
            propListModel.removeAllElements();
        }
    }

    protected DesignProperty showingProp = null;
    public void sourcePropertyChanged(DesignProperty prop) {
        if (showingProp == prop) return;
        showingProp = prop;
        if (prop != null) {
            propList.setSelectedValue(prop, true);
        }
        else {
            propList.clearSelection();
        }
    }

    protected void fillCombo(DesignBean[] beans) {
        for (int i = 0; i < beans.length; i++) {
            if (beans[i].getInstance() instanceof UIComponent) {
                compComboModel.addElement(beans[i]);
            }
            if (beans[i].isContainer()) {
                fillCombo(beans[i].getChildBeans());
            }
        }
    }

    protected void enumProps() {
        DesignProperty[] props = bindingPanel.getSourceBean().getProperties();
        propListModel.removeAllElements();
        ArrayList pa = new ArrayList();
        for (int i = 0; i < props.length; i++) {
            // no read-only properties
            if (props[i].getPropertyDescriptor().getWriteMethod() == null) {
                continue;
            }
            // remove non-bindable properties
            AttributeDescriptor ad = (AttributeDescriptor)props[i].getPropertyDescriptor().getValue(
                Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR);
            if (ad == null || !ad.isBindable()) {
                continue;
            }
            // remove hidden/expert props
            if (showDefault.isSelected()) {
                CategoryDescriptor pcd = (CategoryDescriptor)
                    props[i].getPropertyDescriptor().getValue(Constants.PropertyDescriptor.CATEGORY);
                if (props[i].getPropertyDescriptor().isHidden() ||
                    props[i].getPropertyDescriptor().isExpert() ||
                    (pcd != null && pcd.getName().equals("Advanced"))) { // JOE! HACK!
                    continue;
                }
            }
            // remove hidden props
            else if (showAdvanced.isSelected()) {
                if (props[i].getPropertyDescriptor().isHidden()) {
                    continue;
                }
            }
            pa.add(props[i]);
        }
        ArrayList mods = new ArrayList();
        ArrayList rest = new ArrayList();
        for (int i = 0; i < pa.size(); i++) {
            DesignProperty p = (DesignProperty)pa.get(i);
            if (p.isModified()) {
                String vx = p.getValueSource();
                if (vx != null && vx.startsWith("#{") && vx.endsWith("}")) {  //NOI18N
                    mods.add(p);
                    continue;
                }
            }
            rest.add(p);
        }
        for (int i = 0; i < mods.size(); i++) {
            propListModel.addElement(mods.get(i));
        }
        for (int i = 0; i < rest.size(); i++) {
            propListModel.addElement(rest.get(i));
        }
        if (propListModel.getSize() > 0) {
            this.remove(noneText);
            this.add(propScroll, propScrollConstraints);
            if (mods.size() > 0) {
                propList.setSelectedIndex(0);
            }
            else {
                boolean foundValue = false;
                for (int i = 0; i < rest.size(); i++) {
                    if (((DesignProperty)rest.get(i)).getPropertyDescriptor().getName() == "value") { //NOI18N
                        propList.setSelectedValue(rest.get(i), true);
                        foundValue = true;
                        break;
                    }
                }
                if (!foundValue) {
                    for (int i = 0; i < rest.size(); i++) {
                        if (((DesignProperty)rest.get(i)).getPropertyDescriptor().getName() == "text") { //NOI18N
                            propList.setSelectedValue(rest.get(i), true);
                            break;
                        }
                    }
                }
            }
            if (propList.getSelectedValue() == null) {
                propList.setSelectedIndex(0);
            }
        }
        else {
            this.remove(propScroll);
            this.add(noneText, propScrollConstraints);
        }
        this.validate();
        this.doLayout();
        this.repaint(100);
    }

    GridBagConstraints propScrollConstraints = new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 8, 0, 0), 0, 0);

    void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);

        noneText.setEditable(false);
        noneText.setFont(propLabel.getFont());
        noneText.setBorder(UIManager.getBorder("TextField.border"));    //NOI18N
        noneText.setText(bundle.getMessage("noBindableProps")); //NOI18N

        compLabel.setText(bundle.getMessage("selectComponent")); //NOI18N
        compCombo.setModel(compComboModel);
        compCombo.setRenderer(new CompComboRenderer());
        compCombo.setEditable(false);
        compCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                compCombo_actionPerformed(e);
            }
        });
        propLabel.setText(bundle.getMessage("selectBindableProp")); //NOI18N
        propLabel.setDisplayedMnemonic(bundle.getMessage("selectBindablePropDisplayedMnemonic").charAt(0)); //NOI18N
        
        propList.setModel(propListModel);
        propList.setCellRenderer(new PropListRenderer());
        propList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                propList_valueChanged(e);
            }
        });
        showDefault.setText(bundle.getMessage("default")); //NOI18N
        showDefault.setSelected(true);
        showDefault.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                show_itemStateChanged(e);
            }
        });
        showAdvanced.setText(bundle.getMessage("advanced")); //NOI18N
        showAdvanced.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                show_itemStateChanged(e);
            }
        });
        showAll.setText(bundle.getMessage("all")); //NOI18N
        showAll.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                show_itemStateChanged(e);
            }
        });
        radioPanel.setLayout(flowLayout1);
        flowLayout1.setAlignment(FlowLayout.LEFT);
        flowLayout1.setHgap(5);
        flowLayout1.setVgap(0);
//        this.add(compCombo, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
//            GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 0), 0, 0));
//        this.add(compLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
//            GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 0), 0, 0));
        this.add(propLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 0), 0, 0));
        this.add(propScroll, propScrollConstraints);
        this.add(radioPanel, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 0), 0, 0));
        radioPanel.add(showDefault, null);
        radioPanel.add(showAdvanced, null);
        radioPanel.add(showAll, null);
        propScroll.getViewport().add(propList, null);
        showGroup.add(showDefault);
        showGroup.add(showAdvanced);
        showGroup.add(showAll);
    }

    class CompComboRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof DesignBean) {
                DesignBean b = (DesignBean)value;

                String prefix = ""; //NOI18N
                DesignBean p = b.getBeanParent();
                while (p != null && p != b.getDesignContext().getRootContainer()) {
                    prefix += "   "; //NOI18N
                    p = p.getBeanParent();
                }

                this.setText(prefix + b.getInstanceName());
                BeanInfo bi = b.getBeanInfo();
                Image img = bi.getIcon(BeanInfo.ICON_COLOR_16x16);
                if (img != null) {
                    this.setIcon(new ImageIcon(img));
                }
                else {
                    this.setIcon(BEAN_ICON);
                }
            }

            return this;
        }
    }

    protected static Icon BLANK_ICON = new Icon() {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(c.getBackground());
            g.fillRect(x, y, 16, 16);
        }
        public int getIconWidth() { return 16; }
        public int getIconHeight() { return 16; }
    };

    public static Icon BEAN_ICON = new ImageIcon(BindingSourcePanel.class.getResource("img/bean.gif")); //NOI18N

    class PropListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof DesignProperty) {
                DesignProperty p = (DesignProperty)value;

                PropertyDescriptor pd = p.getPropertyDescriptor();
                String cn = pd.getPropertyType().getName();
                if (cn.startsWith("[")) { //NOI18N
                    cn = decodeTypeName(cn);
                }
                if (cn.indexOf(".") > -1) { //NOI18N
                    cn = cn.substring(cn.lastIndexOf(".") + 1); //NOI18N
                }

                boolean bold = false;
                if (p.isModified()) {
                    String vx = p.getValueSource();
                    if (vx==null) {
                        vx="";
                    }
                    bold = vx.startsWith("#{") && vx.endsWith("}"); //NOI18N
                }

                StringBuffer sb = new StringBuffer();
                sb.append("<html>"); //NOI18N
                if (bold) {
                    sb.append("<b>"); //NOI18N
                }
                sb.append(pd.getName());
                if (bold) {
                    sb.append("</b>"); //NOI18N
                }                
                sb.append(" &nbsp; <font><i>"); //NOI18N
                sb.append(cn);
                sb.append("</i></font></html>"); //NOI18N
                this.setText(sb.toString());
                this.setIcon(UIManager.getIcon("Tree.leafIcon")); //NOI18N
            }

            return this;
        }
    }

    void show_itemStateChanged(ItemEvent e) {
        Object o = propList.getSelectedValue();
        enumProps();
        propList.setSelectedValue(o, false);
    }

    void compCombo_actionPerformed(ActionEvent e) {
        DesignBean b = (DesignBean)compCombo.getSelectedItem();
        bindingPanel.setSourceBean(b);
    }

    void propList_valueChanged(ListSelectionEvent e) {
        DesignProperty p = (DesignProperty)propList.getSelectedValue();
        bindingPanel.setSourceProperty(p);
    }

    static HashMap arrayTypeKeyHash = new HashMap();
    static {
        arrayTypeKeyHash.put("B", "byte"); //NOI18N
        arrayTypeKeyHash.put("C", "char"); //NOI18N
        arrayTypeKeyHash.put("D", "double"); //NOI18N
        arrayTypeKeyHash.put("F", "float"); //NOI18N
        arrayTypeKeyHash.put("I", "int"); //NOI18N
        arrayTypeKeyHash.put("J", "long"); //NOI18N
        arrayTypeKeyHash.put("S", "short"); //NOI18N
        arrayTypeKeyHash.put("Z", "boolean"); //NOI18N
        arrayTypeKeyHash.put("V", "void"); //NOI18N
    }

    String decodeTypeName(String tn) {
        if (tn.startsWith("[")) { //NOI18N
            int depth = 0;
            while (tn.startsWith("[")) { //NOI18N
                tn = tn.substring(1);
                depth++;
            }
            if (tn.startsWith("L")) { //NOI18N
                tn = tn.substring(1);
                tn = tn.substring(0, tn.length() - 1);
            }
            else {
                char typeKey = tn.charAt(0);
                tn = (String)arrayTypeKeyHash.get("" + typeKey); //NOI18N
            }
            for (int i = 0; i < depth; i++) {
                tn += "[]"; //NOI18N
            }
        }
        return tn;
    }
}
