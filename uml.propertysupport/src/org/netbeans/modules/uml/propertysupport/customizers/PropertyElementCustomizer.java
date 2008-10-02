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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * StereotypeCustomizer.java
 *
 * Created on March 15, 2005, 9:20 AM
 */

package org.netbeans.modules.uml.propertysupport.customizers;

import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import org.netbeans.modules.uml.propertysupport.customizers.PropertyDataFormatter;
import org.netbeans.modules.uml.propertysupport.nodes.CustomPropertyEditor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.Box;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.DefaultCellEditor;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.uml.core.support.Debug;

/**
 *
 * @author  Administrator
 */
public class PropertyElementCustomizer extends JPanel 
        implements Customizer, EnhancedCustomPropertyEditor
{
   //private ResourceBundle mBundle = ResourceBundle.getBundle("org/netbeans/modules/uml/propertysupport/customizers/Bundle");
   private ResourceBundle mBundle = NbBundle.getBundle(PropertyElementCustomizer.class);

   private PropertyElementTabelModel mDataModel = new PropertyElementTabelModel();
   // These members are not use anywhere in this class; hence, comment them out
   //private ArrayList < String > mAddedStereotypes = new ArrayList < String >();
   //private ArrayList < String > mRemovedStereotypes = new ArrayList < String >();
   private Vector < IPropertyElement > localElement = new Vector < IPropertyElement > ();
   
   private IPropertyElement mElement = null;
   private IPropertyDefinition mDefinition = null;
   private CustomPropertyEditor mEditor = null;
   
   /** Creates new form StereotypeCustomizer */
   public PropertyElementCustomizer()
   {
      this(null);
   }
   
   public PropertyElementCustomizer(IPropertyElement element)
   {
      initComponents();
      mDataTable.getSelectionModel().addListSelectionListener(new SelectionListener());
      setElement(element, null);
      
   }
   
   public void setElement(IPropertyElement element,
                          IPropertyDefinition def)
   {      
      mElement = element;       
      mDefinition = def;
      if(element != null)
      {         
         mDataModel = null;
         initializeModel();         
      }
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
    
   protected void initializeModel()
   {
      if((mElement != null) && (mDefinition != null))
      {
         mDataModel = new PropertyElementTabelModel();
         
         mDataModel.addTableModelListener(new javax.swing.event.TableModelListener() {
            public void tableChanged(javax.swing.event.TableModelEvent evt) {
                dataChangedPerformed(evt);
            }
         });
         
         TableCellRenderer renders[] = mDataModel.initializeModel();
         mDataTable.setModel(mDataModel);
         mDataTable.setDoubleBuffered(true);
         
         for(int index = 0; index < renders.length; index++)
         {
            TableColumn col = mDataTable.getColumnModel().getColumn(index);
            if(col != null)
            {
               col.setCellRenderer(renders[index]);
               //col.setCellEditor(new ListCellEditor());
               if(renders[index] instanceof ListCellRender)
               {
                  //col.setCellEditor(new javax.swing.DefaultCellEditor((JComboBox)renders[index]));
                  col.setCellEditor(new ListCellEditor());
               }
               else if(renders[index] instanceof TextCellRender)
               {
                  col.setCellEditor(new TextCellEditor());
               }
            }
         }
         
         //ETList < String > initialStereotypes = mElement.getAppliedStereotypesAsString();
         //ETList < Object > initialStereotypes = mElement.getAppliedStereotypes();
         DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
         Vector < IPropertyElement > initialData = mElement.getSubElements();
         if(initialData != null)
         {
            for(IPropertyElement curType : initialData)
            {
               IPropertyDefinition def = curType.getPropertyDefinition();
               if(def.isOnDemand() == true)
               {                  
                  builder.loadOnDemandProperties(curType, true);
               }
               // mDataModel.addPropertyElement(curType);
            }
            for (IPropertyElement curType : ((localElement.size () > 0) ? localElement : initialData)) {
                mDataModel.addPropertyElement (curType);
            }
         }
      }
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

        mPropertyData = new javax.swing.JScrollPane();
        mDataTable = new javax.swing.JTable();
        mControlPanel = new javax.swing.JPanel();
        mAddBtn = new javax.swing.JButton();
        mRemoveBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        mDataTable.setModel(new javax.swing.table.DefaultTableModel(
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
        mPropertyData.setViewportView(mDataTable);
        mDataTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "ACSD_PropertyTable"));
        mDataTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "ACSD_PropertyTable"));
        mDataTable.getAccessibleContext().setAccessibleParent(this);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(mPropertyData, gridBagConstraints);

        mControlPanel.setLayout(new java.awt.GridBagLayout());

        mControlPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 1.0;
        add(mControlPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mAddBtn, org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "BTN_ADD"));
        mAddBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mAddBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(mAddBtn, gridBagConstraints);
        mAddBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "BTN_ADD"));
        mAddBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "ASD_NA"));

        mControlPanel.add(Box.createVerticalStrut(5));
        org.openide.awt.Mnemonics.setLocalizedText(mRemoveBtn, org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "BTN_REMOVE"));
        mRemoveBtn.setEnabled(false);
        mRemoveBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mRemoveBtnActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(mRemoveBtn, gridBagConstraints);
        mRemoveBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "BTN_REMOVE"));
        mRemoveBtn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "ASD_NA"));

        jLabel1.setLabelFor(mDataTable);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PropertyElementCustomizer.class, "LBL_PropertyValues"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jLabel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

   private void mRemoveBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mRemoveBtnActionPerformed
   {//GEN-HEADEREND:event_mRemoveBtnActionPerformed
      int[] selectedRows = mDataTable.getSelectedRows();
      if (selectedRows.length == 0) { // no thing is selected
          return;
      }  
      //Process last edited cell for subsequence row "add" (if any) works correctly
      setLastEditedCell(true);
      mDataModel.removeRows(selectedRows);
   }//GEN-LAST:event_mRemoveBtnActionPerformed

   private void mAddBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mAddBtnActionPerformed
   {//GEN-HEADEREND:event_mAddBtnActionPerformed
       mDataModel.createNewProperty();
       int rowIndex = mDataModel.getRowCount() - 1;
       // higlight the newly added row
       // mDataTable.setRowSelectionInterval(rowIndex, rowIndex);
       
   }//GEN-LAST:event_mAddBtnActionPerformed

   public void dataChangedPerformed(TableModelEvent evt) {
        int type = evt.getType();
        
        // This is a hack to fix a problem described in bug ## 6274130
        // That is... in Stereotypes, Remove tends to delete a wrong line.
        // This block of code provide a fix for a table with 1 column whose cell editor
        // is a combobox, like the Stereotypes customizer.
        if (type == evt.DELETE && mDataTable.getModel().getColumnCount() == 1) {     
            TableColumn column = mDataTable.getColumnModel().getColumn(0);
            DefaultCellEditor cellEditor = (DefaultCellEditor) column.getCellEditor();
            java.awt.Component comp = cellEditor.getComponent();
            javax.swing.JComboBox box = null;
            if (comp instanceof javax.swing.JComboBox) {
                box = (javax.swing.JComboBox) comp;
                box.getModel().setSelectedItem("");
            }
        }
    }
   ////////////////////////////////////////////////////////////////////////////
   // EnhancedCustomPropertyEditor Implementation
   
   /** 
    * Get the customized property value.  This implementation will 
    * return an array of property elements.  Basically when this method
    * gets called the user has pressed the OK button.
    *
    * @return the property value
    * @exception IllegalStateException when the custom property editor does not contain a valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue() throws IllegalStateException
    {
       Object retVal = null;
       // This method is added to fix CR 6291552
       setLastEditedCell(true);
       mDataModel.saveModel();
       mDataModel = null;
       
       IPropertyElementManager manager = mElement.getPropertyElementManager();
       
       IPropertyElement parent = mElement.getParent();
       manager.reloadElement(mElement.getElement(), mElement.getPropertyDefinition(), mElement);
       notifyChanged();
       return retVal;
    }
    
    public void setVisible(boolean aFlag)
    {
       if((aFlag == true) && (mDataModel == null))
       {
          initializeModel();
       }
       
       super.setVisible(aFlag);
    }
    
    // This method processes the value of the last cell beeing edited.
    // The value of the cell is either saved to the data model or reset to empty string 
    // based on the "saved" flag.
    private void setLastEditedCell(Boolean saved) 
    {
        int editingRow = mDataTable.getEditingRow();
        int editingCol = mDataTable.getEditingColumn();
        // if no cell is currently being edited, simply return.
        if (editingRow < 0 || editingCol < 0)
            return;
        DefaultCellEditor cellEditor = (DefaultCellEditor) mDataTable.getCellEditor(
                editingRow, editingCol);
        Component editorComponent = cellEditor.getComponent();
        if (editorComponent instanceof JTextField)
        {
            String lastEditedText = ((JTextField)editorComponent).getText();
            Debug.out.println("getLastUpdatedCell: lasteditedText="+lastEditedText);
            if (saved)   // update the data model with the value
            {
                mDataTable.getModel().setValueAt(lastEditedText, editingRow, editingCol);
                cellEditor.stopCellEditing();
            }
            else  // reset the value to empty string.
            {
                ((JTextField) editorComponent).setText("");
                cellEditor.cancelCellEditing();
            }
        }
        editorComponent.transferFocus();
        return;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Helper Classes
    
    /**
     * Property editor tabel model.
     */
    public class PropertyElementTabelModel extends AbstractTableModel 
    {
        private ArrayList < PropertyData > mData = new ArrayList < PropertyData >();
        private ArrayList < PropertyData > mRemovedData = new ArrayList < PropertyData >();
        private ArrayList < IPropertyDefinition > mColumnDefintions = new ArrayList < IPropertyDefinition >();
        
        //private IPropertyDefinition mDefinition = null;
        
        public PropertyElementTabelModel() 
        {
            
        }
        
        public PropertyElementTabelModel(IElement element, ArrayList < IStereotype > list) 
        {
        }
        
        public void saveModel() 
        {
            localElement.removeAllElements();
            //printData(mData);
            for(PropertyData data : mData) 
            {
                data.save();
                localElement.add(data.getPropertyElement());
            }
            
            for(PropertyData data : mRemovedData) 
            {
                data.remove();
            }
        }
        
        public void removeRow(int row) 
        {
            PropertyData data = mData.get(row);
            mData.remove(row);
            //fireTableRowsDeleted(row, row);
            
            mRemovedData.add(data);
        }
        
        // This method removes multi selected row intervals.
        // The indices of the selected rows are specified
        // in the array rowIndices in an increasing order.
        public void removeRows(int[] rowIndex) 
        {
            if (rowIndex == null || rowIndex.length == 0)
                return;
            
            // extract the removed object by theis indices
            PropertyData [] removedRows = new PropertyData [rowIndex.length];
            for (int i = 0; i < rowIndex.length; i++) 
            {
                removedRows[i] =  mData.get(rowIndex[i]);
            }
            // search for each removedRow in mData;
            // if found, remove the row from mData
            PropertyData target = null;
            PropertyData aRow = null;
            for (int i = 0; i < removedRows.length; i++) 
            {
                target = removedRows[i];
                for (int j = 0; j < getRowCount(); j++) 
                {
                    aRow = (PropertyData) mData.get(j);
                    if (aRow == target)  // intentionally compare the objects' addresses
                    {
                        fireTableRowsDeleted(j, j);
                        mData.remove(aRow);
                        mRemovedData.add(aRow);
                        //fireTableRowsDeleted(j, j);
                        break;
                    }
                }
            }
        }
        
        
        public TableCellRenderer[] initializeModel() 
        {
            ArrayList < TableCellRenderer > renders = new ArrayList < TableCellRenderer >();
            if(mDefinition != null) 
            {
                Vector < IPropertyDefinition > columns = mDefinition.getSubDefinitions();
                for(IPropertyDefinition def : columns) 
                {
                    if(def.getMultiplicity() == 1) 
                    {
                        mColumnDefintions.add(def);
                        switch(DefinitionPropertyBuilder.instance().getControlType(def)) 
                        {
                            case DefinitionPropertyBuilder.CONTROL_MULTIEDIT:
                            case DefinitionPropertyBuilder.CONTROL_EDIT:
                                renders.add(new TextCellRender());
                                break;
                            case DefinitionPropertyBuilder.CONTROL_BOOLEAN:
                                //renders.add(new TextCellRender());
                                break;
                            case DefinitionPropertyBuilder.CONTROL_COMBO:
                                renders.add(new ListCellRender());
                                break;
                            case DefinitionPropertyBuilder.CONTROL_LIST:
                                renders.add(new ListCellRender());
                                break;
                            case DefinitionPropertyBuilder.CONTROL_FONT:
                                break;
                            case DefinitionPropertyBuilder.CONTROL_COLOR:
                                break;
                            case DefinitionPropertyBuilder.CONTROL_FONTLIST:
                                break;
                            case DefinitionPropertyBuilder.CONTROL_COLORLIST:
                                break;
                            case DefinitionPropertyBuilder.CONTROL_CUSTOM:
                                break;
                        }
                    }
                }
            }
            
            TableCellRenderer[] retVal = new TableCellRenderer[renders.size()];
            renders.toArray(retVal);
            return retVal;
        }
        
        protected IPropertyDefinition getColumnDefinition(int column) 
        {
            IPropertyDefinition retVal = null;
            
            if(column < getColumnCount()) 
            {
                retVal = mColumnDefintions.get(column);
            }
            
            return retVal;
        }
        
        protected void printData(ArrayList <PropertyData> data) 
        {
            if (data == null)
                Debug.out.println("Data is null");
            else if (data.size() == 0)
                Debug.out.println("Data is emty");
            else 
            {
                IPropertyElement cellVal = null;
                Debug.out.println("*****");
                for (int i = 0; i < data.size(); i++) 
                {
                    for (int j = 0; j < getColumnCount(); j++) 
                    {
                        cellVal = getPropertyElement(i, j);
                        Debug.out.println("["+i+","+j+"]: " + cellVal.getValue());
                    }
                }
                Debug.out.println("*****");
            }
        }
        /////////////////////////////////////////////////////////////////////////
        // TableModel implementation
        
        public boolean isCellEditable(int rowIndex, int columnIndex) 
        {
            return true;
        }
        
        public int getColumnCount() 
        {
            return mColumnDefintions.size();
        }
        
        public String getColumnName(int column) 
        {
            String retVal = "< UNKNOWN >";
            
            IPropertyDefinition def = getColumnDefinition(column);
            if(def != null) 
            {
                //Fix CR 6274580: use the displayed name instead of 
                // the programmed name as the column header
                //retVal = def.getName();
                retVal = def.getDisplayName();
            }
            
            return retVal;
        }
        
        public int getRowCount() 
        {
            return mData.size();
        }
        
        public void setValueAt(Object aValue,
                int row,
                int col) 
        {
            //System.out.println("setValueAt(" + row + ", " + col + ") : " + aValue);
            
            IPropertyElement element = getPropertyElement(row, col);
            if((element != null) && (aValue instanceof String)) 
            {
                if(col == 2)
                {
                    String value = PropertyDataFormatter.translateToFullyQualifiedName((String)aValue);
                    element.setValue(value);
                }
                else 
                {  
                   // Fixed IZ=102600
                   element.setValue((String)aValue);
                }
            }          
        }
        
        public Object getValueAt(int row, int col) 
        {
            IPropertyElement retVal = getPropertyElement(row, col);
            
            //System.out.println("getValueAt(" + row + ", " + col + ") : " + retVal.getValue());
            //return retVal.getValue();
            return retVal;
        }
        
        protected IPropertyElement getPropertyElement(int row, int col) 
        {
            IPropertyElement retVal = null;
            
            if(row < getRowCount()) 
            {
                PropertyData data = mData.get(row);
                if(col < getColumnCount()) 
                {
                    IPropertyElement element = data.getPropertyElement();
                    if(element != null) 
                    {
                        Vector < IPropertyElement > sub = element.getSubElements();
                        if(sub != null) 
                        {
                            retVal = sub.get(col);
                        }
                    }
                }
            }
            
            return retVal;
        }
        
        public void addPropertyElement(IPropertyElement value) 
        {
            mData.add(new PropertyData(value, null));           
            fireTableRowsInserted(getRowCount(), getRowCount());
        }
        
        public void createNewProperty() 
        {
            DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
            
            // Steps
            // 1) Given the property definitions build property elements - Done
            // 2) Given property elements build Node.Property objects - Done
            // 3) Make the property objects so they do not automatically persist - Done
            // 4) Add properties to our data structure
            IPropertyElement data = builder.retrievePropertyElement(mDefinition, mElement);
            //Node.Property[] properties = builder.buildProperties(data, false);
            addPropertyElement(data);
        }
        
        class PropertyData 
        {
            private IPropertyElement mPropertyElement = null;
            private Node.Property[] mData = null;
            
            public PropertyData(IPropertyElement umlData, Node.Property[] nbData) 
            {
                mPropertyElement= umlData;
                mData = nbData;
            }
            
            public IPropertyElement getPropertyElement() 
            {
                return mPropertyElement;
            }
            
            public void save() 
            {
                IPropertyElement element = getPropertyElement();
                if(element != null) 
                {
                    save(element);
                }
            }
            
            protected void save(IPropertyElement element) 
            {
                if(element != null) 
                {
                    String value = element.getValue();
                    String transValue = PropertyDataFormatter.translateToFullyQualifiedName(value);
                    element.setValue(transValue);
                    
                    element.save();                 
                    Vector < IPropertyElement > children = element.getSubElements();
                    for(IPropertyElement child : children) 
                    {
                        save(child);
                    }
                }
            }
            
            public void remove() 
            {
                IPropertyElement element = getPropertyElement();
                if(element != null) 
                {
                    element.remove();
                }
            }
            
            public Node.Property getData(int column) 
            {
                Node.Property retVal = null;
                
                if(column < mData.length) 
                {
                    retVal = mData[column];
                }
                
                return retVal;
            }
            
            public int getNumberOfColumns() 
            {
                return mData.length;
            }
        }
    }
    
    // Change the ListCellRender to the DefaultTableCellRenderer to
    // inherit all the default behavors, like selection hilighting...
    // This is to fix bug # 6274130.
    // This method display the value as a string on text field.
    public class ListCellRender extends DefaultTableCellRenderer 
    {
        protected void setValue(Object value) 
        {
            if(value instanceof IPropertyElement) 
            {
                IPropertyElement element = (IPropertyElement) value;
                String cellValue = element.getValue();
                
                String transValue = cellValue == null ? "" : cellValue;
                setText(PropertyDataFormatter.translateFullyQualifiedName(transValue));
            }
        }
        
        
    }
    
    // Change the TextCellRender to the DefaultTableCellRenderer to
    // inherit all the default behavors, like selection hilighting...
    // This is to fix bug # 6274130.
    // This method display the value as a string on text field.
    public class TextCellRender extends DefaultTableCellRenderer 
    {
        protected void setValue(Object value) 
        {
            if(value instanceof IPropertyElement) 
            {
                IPropertyElement element = (IPropertyElement) value;
                String cellValue = element.getValue();
                setText(cellValue == null ? "" : cellValue);
            }
        }
    }

    private class SelectionListener implements ListSelectionListener
	{
        public void valueChanged(ListSelectionEvent e) 
		{
           mRemoveBtn.setEnabled(mDataTable.getSelectedRows().length>0);
        }
	}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton mAddBtn;
    private javax.swing.JPanel mControlPanel;
    private javax.swing.JTable mDataTable;
    private javax.swing.JScrollPane mPropertyData;
    private javax.swing.JButton mRemoveBtn;
    // End of variables declaration//GEN-END:variables
   
}
