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

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
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
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.openide.util.NbBundle;

/**
 *
 * @author  khu
 */
public class ReturnTypeCustomizer extends javax.swing.JPanel implements EnhancedCustomPropertyEditor
{
    
    private IPropertyElement mElement = null;
    private IPropertyDefinition mDefinition = null;
    private CustomPropertyEditor mEditor = null;
    
    MultiplicityTableModel model = null;
    
    //    boolean isMultiple = false;
    
    /** Creates new form ReturnTypeCustomizer */
    public ReturnTypeCustomizer()
    {
        initComponents();
    }
    public void setElement(IPropertyElement element, IPropertyDefinition def)
    {
        mElement = element;
        mDefinition = def;
        
//        resetTables();
        initializeType();
        initializeMulti();
        
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
        }
        
        //        IParameter param = (IParameter)mElement.getElement();
        
    }
    
    protected void initializeMulti()
    {
        IParameter param = (IParameter)mElement.getElement();
        IMultiplicity mult = param.getMultiplicity();
        
        model = new MultiplicityTableModel(mult);
        multiplicityTable.setModel(model);
        
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
        removeMulButton = new javax.swing.JButton();
        addMulButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        multiplicityTable = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(350, 200));

        returnTypeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle").getString("RETURN_TYPE_Mnemonic").charAt(0));
        returnTypeLabel.setLabelFor(returnTypeCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle"); // NOI18N
        returnTypeLabel.setText(bundle.getString("RETURN_TYPE")); // NOI18N

        returnTypeCombo.setEditable(true);

        multiplicityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Multiplicity"));

        org.openide.awt.Mnemonics.setLocalizedText(removeMulButton, bundle.getString("BTN_REMOVERANGE")); // NOI18N
        removeMulButton.setActionCommand("REMOVE_MULTI");
        removeMulButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeMulButtonactionHandler(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addMulButton, bundle.getString("BTN_ADDRANGE")); // NOI18N
        addMulButton.setActionCommand("ADD_MULTI");
        addMulButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addMulButtonactionHandler(evt);
            }
        });

        multiplicityTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(multiplicityTable);

        org.jdesktop.layout.GroupLayout multiplicityPanelLayout = new org.jdesktop.layout.GroupLayout(multiplicityPanel);
        multiplicityPanel.setLayout(multiplicityPanelLayout);
        multiplicityPanelLayout.setHorizontalGroup(
            multiplicityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, multiplicityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(multiplicityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addMulButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(removeMulButton))
                .addContainerGap())
        );

        multiplicityPanelLayout.linkSize(new java.awt.Component[] {addMulButton, removeMulButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        multiplicityPanelLayout.setVerticalGroup(
            multiplicityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(multiplicityPanelLayout.createSequentialGroup()
                .add(multiplicityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(multiplicityPanelLayout.createSequentialGroup()
                        .add(addMulButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(removeMulButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                .addContainerGap())
        );

        removeMulButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReturnTypeCustomizer.class, "ACSN_REMOVERANGE")); // NOI18N
        removeMulButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReturnTypeCustomizer.class, "ACSN_REMOVERANGE")); // NOI18N
        addMulButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReturnTypeCustomizer.class, "ACSN_ADDRANGE")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(returnTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(returnTypeCombo, 0, 562, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(multiplicityPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(returnTypeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(returnTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(multiplicityPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        returnTypeCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReturnTypeCustomizer.class, "ACSN_TYPE")); // NOI18N
        returnTypeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReturnTypeCustomizer.class, "ACSN_TYPE")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
private void removeMulButtonactionHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMulButtonactionHandler
    model.removeRange(multiplicityTable.getSelectedRow());
}//GEN-LAST:event_removeMulButtonactionHandler

private void addMulButtonactionHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMulButtonactionHandler
    model.addRange();
}//GEN-LAST:event_addMulButtonactionHandler

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
        
        param.setType2((String)returnTypeCombo.getSelectedItem());
        model.saveRanges();
        
        notifyChanged();
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
            ranges.remove(row);
            fireRowRemoved(row);
        }
        
        public void saveRanges()
        {
            multiplicity.removeAllRanges();
            
            for(RangeData data : ranges)
            {
                IMultiplicityRange range = multiplicity.createRange();
                
                range.setRange(data.getLower(), data.getUpper());
                range.setCollectionType(data.getCollection());
                
                multiplicity.addRange(range);
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
