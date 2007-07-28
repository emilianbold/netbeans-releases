/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

/*
 * ReturnTypeCustomizer.java
 *
 * Created on April 14, 2005, 10:57 AM
 */

package org.netbeans.modules.uml.propertysupport.customizers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.propertysupport.nodes.CustomPropertyEditor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  khu
 */
public class ReturnTypeCustomizer extends javax.swing.JPanel 
        implements Customizer, EnhancedCustomPropertyEditor
{
    private ResourceBundle bundle = NbBundle.getBundle(ReturnTypeCustomizer.class);
    private IPropertyElement mElement = null;
    private IPropertyDefinition mDefinition = null;
    private CustomPropertyEditor mEditor = null;
   
    /** Creates new form ReturnTypeCustomizer */
    public ReturnTypeCustomizer()
    {
        initComponents();
    }
    
    public void setElement(IPropertyElement element, IPropertyDefinition def)
    {
        mElement = element;
        mDefinition = def;
        initializeMulti();   // initializedMulti() must be called before initializeType()
        initializeType();
        setPreferredSize(new Dimension(680, 330));
    }
    
    public void setPropertySupport(CustomPropertyEditor editor)
    {
        mEditor = editor;
    }
    
    protected void notifyChanged()
    {
        if(mEditor != null)
        {
            mEditor.firePropertyChange();
        }
    }
    
    protected void initializeType()
    {
        Vector < IPropertyElement > elems = mElement.getSubElements();
        
        IStrings typeNames;
        IPropertyElement typeEl = null;
        IPropertyDefinition typeDef = null;
        if (elems != null && elems.size() > 0)
        {
            typeEl = elems.get(0);
            typeDef = typeEl.getPropertyDefinition();
        }
        if (typeDef != null && typeEl != null)
        {
            typeNames = typeDef.getValidValue(typeEl);
        }
        else
        {
            typeNames = searchAllTypes();
        }
        
        if (typeNames != null)
        {
            ComboBoxModel model = new DefaultComboBoxModel(typeNames.toArray());
            returnTypeCombo.setModel(model);
        }
        
        if (typeEl != null)
        {
            returnTypeCombo.setSelectedItem(typeEl.getValue());
            returnTypeChangedHandler(null);
        }
    }
    
    protected void initializeMulti()
    {
        IParameter param = (IParameter)mElement.getElement();
        IMultiplicity mult = param.getMultiplicity();
        
        MultiplicityTableModel tableModel = new MultiplicityTableModel(mult);
        multiplicityTable.setModel(tableModel);
        multiplicityTable.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                tableChangeHandler(e);
            }
        });
        
        // The collection type column needs a custom column render and
        // editor.
        TableColumn column = multiplicityTable.getColumnModel().getColumn(2);
        column.setCellEditor(new CollectionTypeEditor());
        column.setCellRenderer(new CollectionTypeRender());
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        returnTypeLabel = new javax.swing.JLabel();
        returnTypeCombo = new javax.swing.JComboBox();
        multiplicityPanel = new javax.swing.JPanel();
        addMulButton = new javax.swing.JButton();
        removeMulButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        multiplicityTable = new javax.swing.JTable();
        messagePanel = new javax.swing.JPanel();
        messageIcon = new javax.swing.JLabel();
        messageArea = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(480, 250));
        setLayout(new java.awt.GridBagLayout());

        returnTypeLabel.setLabelFor(returnTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(returnTypeLabel, bundle.getString("RETURN_TYPE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 5);
        add(returnTypeLabel, gridBagConstraints);

        returnTypeCombo.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                returnTypeChangedHandler(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        add(returnTypeCombo, gridBagConstraints);
        returnTypeCombo.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TYPE")); // NOI18N
        returnTypeCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_TYPE")); // NOI18N

        multiplicityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("MULTIPLICITY"))); // NOI18N
        multiplicityPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addMulButton, bundle.getString("BTN_ADDRANGE")); // NOI18N
        addMulButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addMulButtonactionHandler(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        multiplicityPanel.add(addMulButton, gridBagConstraints);
        addMulButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_ADDRANGE")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeMulButton, bundle.getString("BTN_REMOVERANGE")); // NOI18N
        removeMulButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeMulButtonactionHandler(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 5);
        multiplicityPanel.add(removeMulButton, gridBagConstraints);
        removeMulButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSN_REMOVERANGE")); // NOI18N

        multiplicityTable.setModel(new MultiplicityTableModel());
        jScrollPane1.setViewportView(multiplicityTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        multiplicityPanel.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(multiplicityPanel, gridBagConstraints);

        messagePanel.setLayout(new java.awt.GridBagLayout());

        messageIcon.setLabelFor(messageArea);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        messagePanel.add(messageIcon, gridBagConstraints);

        messageArea.setBackground(messagePanel.getBackground());
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setRows(1);
        messageArea.setWrapStyleWord(true);
        messageArea.setAutoscrolls(false);

        // set foreground color & font for messageArea
        Color c = javax.swing.UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null)
        {
            c = new Color(89,79,191);
        }
        messageArea.setForeground(c);
        messageArea.setFont(this.getFont());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        messagePanel.add(messageArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(messagePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void removeMulButtonactionHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMulButtonactionHandler
    MultiplicityTableModel tableModel = (MultiplicityTableModel) multiplicityTable.getModel();
    if (tableModel != null)
    {
        tableModel.removeRange(multiplicityTable.getSelectedRow());
    }
}//GEN-LAST:event_removeMulButtonactionHandler

private void addMulButtonactionHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMulButtonactionHandler
    MultiplicityTableModel tableModel = (MultiplicityTableModel) multiplicityTable.getModel();
    if (tableModel != null)
    {
        tableModel.addRange();
    }
}//GEN-LAST:event_addMulButtonactionHandler

private void returnTypeChangedHandler(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_returnTypeChangedHandler
    String selectedType = (String)returnTypeCombo.getSelectedItem();
        MultiplicityTableModel tableModel = (MultiplicityTableModel) multiplicityTable.getModel();
        // if selected return type is 'void', remove all multiplicity ranges and
        // disable the multiplicity buttons
        if ("void".equals(selectedType)) 
        {
            if (tableModel != null ) 
            {
                tableModel.removeAllRanges();
            }
            enableButtons(false);
        } 
        else
        {   
            enableButtons(true);
        }
        this.setMessage("");
}//GEN-LAST:event_returnTypeChangedHandler
    
    private void enableButtons(boolean enabled) 
    {
        this.removeMulButton.setEnabled(enabled);
        this.addMulButton.setEnabled(enabled);
    }
    
    public void tableChangeHandler(TableModelEvent e)
    {
        MultiplictyRangeHandler mrHandler = new MultiplictyRangeHandler();
        ETPairT<Boolean, String> retVal = mrHandler.tableValueChanged(e);
        if (retVal != null)
        {
            boolean valid = retVal.getParamOne().booleanValue();
            String message = retVal.getParamTwo();
            setMessage(message);
            addMulButton.setEnabled(valid); 
        }
    }
    
    private void setMessage (String text) {
        Icon icon = null;
        if (text != null) {
            this.messageArea.setText(text);
            if (text.trim().length() > 0) {
                icon = new ImageIcon(
                  Utilities.loadImage("org/netbeans/modules/uml/resources/error.gif")); // NOI18N
            }
            this.messageIcon.setIcon(icon); 
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // EnhancedCustomPropertyEditor Implementation

    /**
    // * Get the customized property value.  This implementation will
     * return an array of property elements.  Basically when this method
     * gets called the user has pressed the OK button.
     *
     * @return the property value
     * @exception IllegalStateException when the custom property editor does not contain a valid property value
     *            (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException
    {   
        IParameter param = (IParameter)mElement.getElement();
        String selectedType = (String)returnTypeCombo.getSelectedItem();
        if (selectedType != null && selectedType.trim().length() > 0) 
        {
            param.setType2(selectedType);
            MultiplicityTableModel tableModel = (MultiplicityTableModel) multiplicityTable.getModel();
            tableModel.saveRanges();
            notifyChanged();
        }
        return null;
    }
    
    protected void save(IPropertyElement element)
    {
        if (element != null)
        {
            element.save();
            Vector < IPropertyElement > children = element.getSubElements();
            for (IPropertyElement child : children)
            {
                save(child);
            }
        }
    }

    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMulButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JLabel messageIcon;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JPanel multiplicityPanel;
    private javax.swing.JTable multiplicityTable;
    private javax.swing.JButton removeMulButton;
    private javax.swing.JComboBox returnTypeCombo;
    private javax.swing.JLabel returnTypeLabel;
    // End of variables declaration//GEN-END:variables
    
    
    private IStrings searchAllTypes()
    {
        IStrings list = new Strings();
        IProduct prod = ProductHelper.getProduct();
        if (prod != null)
        {
            IProductProjectManager pMan = prod.getProjectManager();
            if (pMan != null)
            {
                IProject proj = pMan.getCurrentProject();
                if (proj != null)
                {
                    ITypeManager typeMan = proj.getTypeManager();
                    if (typeMan != null)
                    {
                        IPickListManager pickMan = typeMan.getPickListManager();
                        if (pickMan != null)
                        {
                            String filter = "DataType Class Interface";
                            list = pickMan.getTypeNamesWithStringFilter(filter);
                        }
                    }
                }
            }
        }
        return list;
    }
    
    private class MultiplicityTableModel implements TableModel
    {
        private IMultiplicity multiplicity = null;
        private ArrayList < RangeData > ranges = new ArrayList <RangeData>();
        private ArrayList < TableModelListener > listeners = 
                new ArrayList < TableModelListener >();
        
        public MultiplicityTableModel() {
        }
        
        public MultiplicityTableModel(IMultiplicity mult)
        {
            for(IMultiplicityRange range : mult.getRanges())
            {
                RangeData data = new RangeData(range.getLower(),
                                               range.getUpper(),
                                               range.getCollectionType());
                ranges.add(data);
            }
            
            multiplicity = mult;
        }
        
        public void addRange()
        {
            RangeData data = new RangeData("0", "*", "");
            ranges.add(data);
            fireRowAdded();
        }
        
        public void removeRange(int row)
        {
            // fix issue 108135.
            if (row == -1)  // no selected row, remove nothing
                return;
            
            ranges.remove(row);
            fireRowRemoved(row);
        }
        
        public void removeAllRanges()
        {
            if ( ranges != null)
            {
                for (int row = 0; row < ranges.size(); row++)
                {
                    ranges.remove(row);
                    fireRowRemoved(row);
                }
            }
        }
        
        public void saveRanges()
        {
            multiplicity.removeAllRanges();
            if (ranges != null) 
            {
                for(RangeData data : ranges) 
                {
                    IMultiplicityRange range = multiplicity.createRange();
                    
                    range.setRange(data.getLower(), data.getUpper());
                    range.setCollectionType(data.getCollection());
                    
                    multiplicity.addRange(range);
                }
            }
        }
        
        public int getRowCount()
        {
            return ranges.size();
        }
        
        public int getColumnCount()
        {
            return 3;
        }
        
        public String getColumnName(int col)
        {
            String retVal = "";
            if(col == 0)
            {
                retVal = NbBundle.getMessage(ReturnTypeCustomizer.class, "LOWER");
            }
            else if(col == 1)
            {
                retVal = NbBundle.getMessage(ReturnTypeCustomizer.class, "UPPER");
            }
            else
            {
                IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
                retVal = translator.translateWord("PSK_COLLECTION_OVERRIDE_DATA_TYPE");
            }
            
            return retVal;
        }
        
        public Class<?> getColumnClass(int col)
        {
            return String.class;
        }
        
        public boolean isCellEditable(int row, int col)
        {
            return true;
        }
        
        public Object getValueAt(int row, int col)
        {
            RangeData data = ranges.get(row);
            
            Object retVal = null;
            switch(col)
            {
                case 0:
                    retVal = data.getLower();
                    break;
                case 1:
                    retVal = data.getUpper();
                    break;
                default:
                    retVal = data.getCollection();
                    break;
            }
            
            return retVal;
        }
        
        public void setValueAt(Object value, int row, int col)
        {
            RangeData data = ranges.get(row);
            
            switch(col)
            {
                case 0:
                    data.setLower((String)value);
                    break;
                case 1:
                    data.setUpper((String)value);
                    break;
                default:
                    data.setCollection((String)value);
                    break;
            }
            
            TableModelEvent event = new TableModelEvent(this, row, row, col);
            for(TableModelListener listener : listeners)
            {
                listener.tableChanged(event);
            }
        }
        
        public void addTableModelListener(TableModelListener listener)
        {
            listeners.add(listener);
        }
        
        public void removeTableModelListener(TableModelListener listener)
        {
            listeners.remove(listener);
        }
        
        public void fireRowAdded()
        {
            TableModelEvent event = new TableModelEvent(this, 
                                                        ranges.size(), 
                                                        ranges.size(), 
                                                        TableModelEvent.ALL_COLUMNS, 
                                                        TableModelEvent.INSERT);
            for(TableModelListener listener : listeners)
            {
                listener.tableChanged(event);
            }
        }
        
        public void fireRowRemoved(int row)
        {
            TableModelEvent event = new TableModelEvent(this, 
                                                        row, 
                                                        row, 
                                                        TableModelEvent.ALL_COLUMNS, 
                                                        TableModelEvent.DELETE);
            for(TableModelListener listener : listeners)
            {
                listener.tableChanged(event);
            }
        }
        
        private class RangeData 
        {
            private String lower = "";
            private String upper = "";
            private String collection = "";

            public RangeData(String lower, String upper, String collection)
            {
                setLower(lower);
                setUpper(upper);
                setCollection(collection);
            }
            
            public String getLower()
            {
                return lower;
            }
            
            public void setLower(String lower)
            {
                this.lower = lower;
            }
            
            public String getUpper()
            {
                return upper;
            }
            
            public void setUpper(String upper)
            {
                this.upper = upper;
            }
            
            public String getCollection()
            {
                return collection;
            }
            
            public void setCollection(String collection)
            {
                this.collection = collection;
            }
        }
    }
    
    /**
     * The table cell editor used to render the collection type property.
     */
    public class CollectionTypeEditor extends DefaultCellEditor
    {
        public CollectionTypeEditor()
        {
            super(new JComboBox());
        }
        
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column)
        {
            JComboBox retVal = (JComboBox) super.getTableCellEditorComponent(table, 
                                                                             value,
                                                                             isSelected, 
                                                                             row,
                                                                             column);
            
            
            IParameter param = (IParameter)mElement.getElement();
            for(String type : param.getPossibleCollectionTypes())
            {
                String s = PropertyDataFormatter.translateFullyQualifiedName(type);
                retVal.addItem(s);
            }
            
            String t = PropertyDataFormatter.translateFullyQualifiedName((String)value);
            retVal.setSelectedItem(t);
            
            return retVal;
        }
    }
    
    /**
     * The cell render used to correctly render the collection type information.
     */
    public class CollectionTypeRender extends DefaultTableCellRenderer
    {
        protected void setValue(Object value)
        {
            Object realValue = PropertyDataFormatter.translateFullyQualifiedName((String)value);;
            super.setValue(realValue);
        }
    }
}
