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


package org.netbeans.modules.properties;


import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.SystemColor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.NotifyDescriptor;
import org.openide.options.SystemOption;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.windows.Mode;
import org.openide.windows.Workspace;


/**
 * @author  Petr Jiricka
 */
public class BundleEditPanel extends javax.swing.JPanel {

    private DataObject dobj;
    private PropertiesTableModel ptm;

    private ListSelectionModel rowSelections;
    private ListSelectionModel columnSelections;

    static final long serialVersionUID =-843810329041244483L;

    /** Default implementation of PropertiesColors inetrface. */
    public static final PropertiesColors DEFAULTCOLORS = new PropertiesColors() {
        public Color getKeyColor() {return Color.blue;}
        public Color getValueColor() {return Color.magenta;}
        public Color getShadowColor() {return new Color(SystemColor.controlHighlight.getRGB());}
        public Color getKeyBackground() {return Color.white;}
        public Color getValueBackground() {return Color.white;}

        public void colorsUpdated() {}
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        public void removePropertyChangeListener(PropertyChangeListener listener) {}
    };
    
    /** Class representing colors in table view. */
    private static PropertiesColors colors;
    
    /** Listener on color changes. */    
    private PropertyChangeListener colorsListener;

    
    /** Creates new form BundleEditPanel */
    public BundleEditPanel(final DataObject obj, PropertiesTableModel ptm) {
        this.dobj = obj;
        this.ptm = ptm;

        initComponents ();
        
        initColors();
        
        // header renderer
        final javax.swing.table.DefaultTableCellRenderer headerRenderer = new javax.swing.table.DefaultTableCellRenderer() {
	    public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                         boolean isSelected, boolean hasFocus, int row, int column) {
	        if (table != null) {
	            javax.swing.table.JTableHeader header = table.getTableHeader();
	            if (header != null) {
	                setForeground(header.getForeground());
	                setBackground(header.getBackground());
	                setFont(header.getFont());
	            }
                }

                setText((value == null) ? "" : value.toString());
		setBorder(javax.swing.UIManager.getBorder("TableHeader.cellBorder"));
	        return this;
            }
        };

        // this subclass of Default column model is provided due correct set of column widths 
        // see the JTable and horizontal scrolling problem in Java Discussion Forum
        theTable.setColumnModel(new javax.swing.table.DefaultTableColumnModel() {
            public void addColumn(TableColumn aColumn) {
                if (aColumn == null) {
                    throw new IllegalArgumentException("Object is null");
                }

                tableColumns.addElement(aColumn);
                aColumn.addPropertyChangeListener(this);
                recalcWidthCache();
                // this method call is only difference with overriden superclass method
                setColumnWidths();
                
                // set header renderer this 'ugly' way (for each column),
                // in jdk1.2 is not possible to set default renderer
                // for JTableHeader like in jdk1.3
                aColumn.setHeaderRenderer(headerRenderer);
                
                // Post columnAdded event notification
                fireColumnAdded(new javax.swing.event.TableColumnModelEvent(this, 0,
                                                          getColumnCount() - 1));
            }
        });
        
        theTable.setModel(ptm);

        // table cell editor
        JTextField textField = new JTextField();
        textField.setBorder(new LineBorder(Color.black));
        theTable.setDefaultEditor(PropertiesTableModel.StringPair.class,
                                  new PropertiesTableCellEditor(textField, textComment, textValue));

        // set renderer
        theTable.setDefaultRenderer(PropertiesTableModel.StringPair.class, new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int column) {
  
                java.awt.Component c = super.getTableCellRendererComponent(table,
                    UtilConvert.unicodesToChars(((PropertiesTableModel.StringPair)value).getValue()),
                    isSelected, hasFocus, row, column);
         
                PropertiesTableModel.StringPair sp = (PropertiesTableModel.StringPair)value;
                
                // set backgound
                if(sp.isKeyType())
                    c.setBackground(colors.getKeyBackground());
                else {
                    if( sp.getValue() != null)
                        c.setBackground(colors.getValueBackground());
                    else
                        c.setBackground(colors.getShadowColor());
                }

                // set foregound
                if(sp.isKeyType())
                    c.setForeground(colors.getKeyColor());
                else
                    c.setForeground(colors.getValueColor());
                
                return c;
            }
        });

        // selection listeners
        rowSelections = theTable.getSelectionModel();
        rowSelections.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    rowSelections = (ListSelectionModel)e.getSource();
                    selectionChanged();
                }
            });
        columnSelections = theTable.getColumnModel().getSelectionModel();
        columnSelections.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    columnSelections = (ListSelectionModel)e.getSource();
                    selectionChanged();
                }
            });

        // property change listener - listens to editing state of the table
        theTable.addPropertyChangeListener(new PropertyChangeListener() {
                                               public void propertyChange(PropertyChangeEvent evt) {
                                                   if (evt.getPropertyName().equals("tableCellEditor")) {
                                                       updateEnabled();
                                                   }
                                               }
                                           });

        // listens on clikcs on table header, detects column and sort accordingly to chosen one
        theTable.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel colModel = theTable.getColumnModel();
                int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
                int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();
                // not detected column
                if (modelIndex < 0)
                    return;
                ((PropertiesDataObject)dobj).getBundleStructure().sort(modelIndex);
            }
        });

    }

    /** Calculates width of columns from the table component. 
    */
    void setColumnWidths() {
        int totalWidth = theTable.getParent().getWidth();
        if(totalWidth == 0) {
            try {
                Workspace currWS = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
                Mode editorMode = currWS.findMode(org.openide.text.CloneableEditorSupport.EDITOR_MODE);
                totalWidth = editorMode.getBounds().width;
            } catch (NullPointerException npe) {
                // just catch exception
                // means the editor mode is was not yet reconstructed
            }
        }
        int columnCount = theTable.getColumnModel().getColumnCount();
        int columnWidth = totalWidth/columnCount;
        
        if(columnWidth < totalWidth/5)
            columnWidth = totalWidth/5;
        
        // set the column widths
        for (int i = 0; i < theTable.getColumnModel().getColumnCount(); i++) {
            TableColumn column = theTable.getColumnModel().getColumn(i);

            column.setPreferredWidth(columnWidth);
        }
    }
    
    void stopEditing() {
        if (!theTable.isEditing()) return;
        TableCellEditor cellEdit = theTable.getCellEditor();
        if (cellEdit != null)
            cellEdit.stopCellEditing();
    }

    private void selectionChanged() {
        // label for the key/value
        if (columnSelections.isSelectionEmpty() || (columnSelections.getMaxSelectionIndex() > 0))
            valueLabel.setText(NbBundle.getBundle(PropertiesOpen.class).getString("LBL_ValueLabel"));
        else
            valueLabel.setText(NbBundle.getBundle(PropertiesOpen.class).getString("LBL_KeyLabel"));

        // remove button
        if (rowSelections.isSelectionEmpty() ||
                rowSelections.getMinSelectionIndex()    != rowSelections.getMaxSelectionIndex()) {
            removeButton.setEnabled(false);
        }
        else {
            removeButton.setEnabled(true);
        }

        // fields at the bottom
        if (rowSelections.isSelectionEmpty() || columnSelections.isSelectionEmpty() ||
                rowSelections.getMinSelectionIndex()    != rowSelections.getMaxSelectionIndex() ||
                columnSelections.getMinSelectionIndex() != columnSelections.getMaxSelectionIndex()) {
            if (!theTable.isEditing()) {
                textComment.setText("");
                textValue.setText("");
            }
        }
        else {
            if (!theTable.isEditing()) {
                PropertiesTableModel.StringPair sp =
                    (PropertiesTableModel.StringPair)theTable.getModel().getValueAt(rowSelections.getMinSelectionIndex(),
                            columnSelections.getMinSelectionIndex());
                textComment.setText(sp.getComment());
                textValue.setText(sp.getValue());

                /*          boolean edit = theTable.editCellAt(rowSelections.getMinSelectionIndex(),
                                                           columnSelections.getMinSelectionIndex());*/
            }

            // the selection is ok - edit, if not already editing this field
            if (theTable.getEditingRow()    != rowSelections.getMinSelectionIndex() ||
                    theTable.getEditingColumn() != columnSelections.getMinSelectionIndex()) {
                SwingUtilities.invokeLater(new Runnable() {
                                               public void run() {
                                                   theTable.editCellAt(rowSelections.getMinSelectionIndex(),
                                                                       columnSelections.getMinSelectionIndex());
                                               }
                                           });
            }
        }
    }

    /** Updates the enabled status of the fields */
    private void updateEnabled() {
        // always edit value
        textValue.setEditable(theTable.isEditing());
        textValue.setEnabled(theTable.isEditing());
        // sometimes edit the comment
        if (theTable.isEditing()) {
            PropertiesTableModel.StringPair sp =
                (PropertiesTableModel.StringPair)theTable.getCellEditor().getCellEditorValue();
            textComment.setEditable(sp.isCommentEditable());
            textComment.setEnabled(sp.isCommentEditable());
        }
        else {
            textComment.setEditable(false);
            textComment.setEnabled(false);
        }
    }

    /** Returns the main table with all values */
    public JTable getTable() {
        return theTable;
    }

    /** Initializes colors variable. */
    private void initColors() {
        try {
            Class options = Class.forName
                            ("org.netbeans.modules.properties.syntax.PropertiesOptions",
                             false, this.getClass().getClassLoader());
            Method colorsMethod = options.getMethod ("getColors", null);
            colors = (PropertiesColors)colorsMethod.invoke (options.newInstance(), null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }

        // colors were not gained (editor module is probably not installed), use our defaults
        if(colors == null)
            colors = DEFAULTCOLORS;        

        // listen on changes of color settings
        colors.addPropertyChangeListener(WeakListener.propertyChange(colorsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // colors changed repaint table
                BundleEditPanel.this.repaint();
            }
        }, colors));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        theTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        commentLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textComment = new javax.swing.JTextArea();
        valueLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textValue = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        jPanel1.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        
          theTable.setModel(new javax.swing.table.DefaultTableModel (
            new Object [][] {
                
            },
            new String [] {
                
            }
            ));
            theTable.setCellSelectionEnabled(true);
            theTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            scrollPane.setViewportView(theTable);
            
            gridBagConstraints2 = new java.awt.GridBagConstraints();
          gridBagConstraints2.gridwidth = 0;
          gridBagConstraints2.gridheight = 0;
          gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints2.insets = new java.awt.Insets(8, 8, 8, 8);
          gridBagConstraints2.weightx = 1.0;
          gridBagConstraints2.weighty = 1.0;
          jPanel1.add(scrollPane, gridBagConstraints2);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.gridheight = -1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(jPanel1, gridBagConstraints1);
        
        
        jPanel2.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        
        commentLabel.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_CommentLabel"));
          gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.insets = new java.awt.Insets(0, 8, 0, 8);
          gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
          jPanel2.add(commentLabel, gridBagConstraints3);
          
          
        
          textComment.setLineWrap(true);
            textComment.setRows(2);
            textComment.setEditable(false);
            textComment.setEnabled(false);
            jScrollPane2.setViewportView(textComment);
            
            gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.gridwidth = 0;
          gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints3.insets = new java.awt.Insets(0, 0, 8, 0);
          gridBagConstraints3.weightx = 1.0;
          gridBagConstraints3.weighty = 1.0;
          jPanel2.add(jScrollPane2, gridBagConstraints3);
          
          
        valueLabel.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_ValueLabel"));
          gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints3.insets = new java.awt.Insets(0, 8, 0, 8);
          gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
          jPanel2.add(valueLabel, gridBagConstraints3);
          
          
        
          textValue.setLineWrap(true);
            textValue.setRows(2);
            textValue.setEditable(false);
            textValue.setEnabled(false);
            jScrollPane3.setViewportView(textValue);
            
            gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.gridwidth = 0;
          gridBagConstraints3.gridheight = 0;
          gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints3.insets = new java.awt.Insets(0, 0, 8, 0);
          gridBagConstraints3.weightx = 1.0;
          gridBagConstraints3.weighty = 1.0;
          jPanel2.add(jScrollPane3, gridBagConstraints3);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = -1;
        gridBagConstraints1.gridheight = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.3;
        add(jPanel2, gridBagConstraints1);
        
        
        jPanel3.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints4;
        
        addButton.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_AddPropertyButton"));
          addButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  addButtonActionPerformed(evt);
              }
          }
          );
          gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.gridwidth = 0;
          gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints4.insets = new java.awt.Insets(0, 8, 0, 8);
          jPanel3.add(addButton, gridBagConstraints4);
          
          
        removeButton.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_RemovePropertyButton"));
          removeButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  removeButtonActionPerformed(evt);
              }
          }
          );
          gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.gridwidth = 0;
          gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints4.insets = new java.awt.Insets(8, 8, 8, 8);
          jPanel3.add(removeButton, gridBagConstraints4);
          
          
        gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.gridwidth = 0;
          gridBagConstraints4.gridheight = 0;
          gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints4.weightx = 1.0;
          gridBagConstraints4.weighty = 1.0;
          jPanel3.add(jPanel4, gridBagConstraints4);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(8, 0, 0, 0);
        add(jPanel3, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        stopEditing();
        String key = ((PropertiesTableModel.StringPair)theTable.getModel().getValueAt(rowSelections.getMinSelectionIndex(), 0)).getValue();
        
        // dont't remove elemnt with key == null ( this is only case -> when there is an empty file with comment only)
        if(key == null) return; 
        
        NotifyDescriptor.Confirmation msg = new NotifyDescriptor.Confirmation(
                                                java.text.MessageFormat.format(
                                                    NbBundle.getBundle(BundleEditPanel.class).getString("MSG_DeleteKeyQuestion"),
                                                    new Object[] { key }),
                                                NotifyDescriptor.OK_CANCEL_OPTION);
                                                    
        if (TopManager.getDefault().notify(msg).equals(NotifyDescriptor.OK_OPTION)) {
            try {
                // starts "atomic" acion for special undo redo manager of opend support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = new Object();

                for (int i=0; i < ((PropertiesDataObject)dobj).getBundleStructure().getEntryCount(); i++) {
                    PropertiesFileEntry entry = ((PropertiesDataObject)dobj).getBundleStructure().getNthEntry(i);
                    if (entry != null) {
                        PropertiesStructure ps = entry.getHandler().getStructure();
                        if (ps != null) {
                            ps.deleteItem(key);
                        }
                    }
                }
            } finally {
                // finishes "atomic" undo redo action for special undo redo manager of open support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = null;
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        stopEditing();
        DialogDescriptor.InputLine descr = new DialogDescriptor.InputLine(
                                               NbBundle.getBundle(BundleEditPanel.class).getString ("CTL_PropertyKey"),
                                               NbBundle.getBundle(BundleEditPanel.class).getString("CTL_NewPropertyTitle"));

        boolean okPressed = TopManager.getDefault ().notify (descr).equals (NotifyDescriptor.OK_OPTION);

        if (okPressed) {
            try {
                // starts "atomic" acion for special undo redo manager of opend support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = new Object();

                String key = UtilConvert.charsToUnicodes(UtilConvert.escapePropertiesSpecialChars(descr.getInputText()));
                // add key to all entries
                for (int i=0; i < ((PropertiesDataObject)dobj).getBundleStructure().getEntryCount(); i++) {            
                    PropertiesFileEntry entry = ((PropertiesDataObject)dobj).getBundleStructure().getNthEntry(i);
                    if (!entry.getHandler().getStructure().addItem(key, "", "")) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                                           java.text.MessageFormat.format(
                                                               NbBundle.getBundle(BundleEditPanel.class).getString("MSG_KeyExists"),
                                                               new Object[] {UtilConvert.charsToUnicodes(UtilConvert.escapePropertiesSpecialChars(descr.getInputText()))}),
                                                           NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(msg);
                    }
                }
            } finally {
                // finishes "atomic" undo redo action for special undo redo manager of open support
                ((PropertiesDataObject)dobj).getOpenSupport().atomicUndoRedoFlag = null;
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable theTable;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea textComment;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea textValue;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables


    /** Inner interface used for gainng colors for table view. There are two implemenations.
     * The default one in this class, containing default colors, and implementaion 
     * in syntax/PropertiesOptions class which passes colors from editor settings. That 
     * implementaiton is available only when Editor module is installed. */
    public interface PropertiesColors {
        public Color getKeyColor();
        public Color getValueColor();
        public Color getShadowColor();
        public Color getKeyBackground();
        public Color getValueBackground();

        public void colorsUpdated();
        public void addPropertyChangeListener(PropertyChangeListener listener);
        public void removePropertyChangeListener(PropertyChangeListener listener);
   } // end of inner inaterface PropertiesColors
   
}
