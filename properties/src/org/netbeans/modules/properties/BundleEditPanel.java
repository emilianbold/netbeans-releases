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


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.BorderUIResource.BevelBorderUIResource;
import javax.swing.table.*;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.windows.TopComponent;


/**
 * @author  Petr Jiricka
 */
public class BundleEditPanel extends JPanel {
    
    /** PropertiesDataObject this panel presents. */
    private PropertiesDataObject obj;

    /** Reference to row selection model for managing editing selected cells, together with #columnSelections. */
    private ListSelectionModel rowSelections;
    
    /** Reference to column selection model for managing editing cells, together with #rowSelections.*/
    private ListSelectionModel columnSelections;
    
    /** Generated serialized version UID. */
    static final long serialVersionUID =-843810329041244483L;

    /** Default implementation of PropertiesSettings interface. */
    public static final PropertiesSettings DEFAULT_SETTINGS = new PropertiesSettings() {
        public Color getKeyColor() {return Color.blue;}
        public Color getKeyBackground() {return Color.white;}
        public Color getValueColor() {return Color.magenta;}
        public Color getValueBackground() {return Color.white;}
        public Color getHighlightColor() {return Color.black;}
        public Color getHighlightBackground() {return Color.yellow;}
        public Color getShadowColor() {return new Color(SystemColor.controlHighlight.getRGB());}

        public KeyStroke[] getKeyStrokesFindNext() {return new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)};}
        public KeyStroke[] getKeyStrokesFindPrevious() {return new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_F3, Event.SHIFT_MASK)};}
        public KeyStroke[] getKeyStrokesToggleHighlight() {return new KeyStroke[] {KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.SHIFT_MASK | Event.ALT_MASK)};}
       
        public void settingsUpdated() {}
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        public void removePropertyChangeListener(PropertyChangeListener listener) {}
    };
    
    /** Class representing colors in table view. */
    static PropertiesSettings settings;
    
    /** Listener on color changes. */    
    private PropertyChangeListener settingsListener;

    
    /** Creates new form BundleEditPanel */
    public BundleEditPanel(final PropertiesDataObject obj, PropertiesTableModel ptm) {
        this.obj = obj;

        initComponents ();
        
        initSettings();
        
        // Set header renderer.
        final DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            // Sorted column.
            private int column;
            
	    public Component getTableCellRendererComponent(JTable table, Object value,
                         boolean isSelected, boolean hasFocus, int row, int column) {
                
                this.column = column;             
                             
	        if (table != null) {
	            JTableHeader header = table.getTableHeader();
	            if (header != null) {
	                this.setForeground(header.getForeground());
	                this.setBackground(header.getBackground());
	                this.setFont(header.getFont());
	            }
                }

                setText((value == null) ? "" : value.toString()); // NOI18N
		this.setBorder(UIManager.getBorder("TableHeader.cellBorder")); // NOI18N
	        return this;
            }
            
            // Overrides superclass for painting ascending/descending marks for sorted column header.
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // If the column is the sorted one draw mark on it header.
                if(column == obj.getBundleStructure().getSortIndex() ) {
                    
                    Color oldColor = g.getColor();

                    FontMetrics fm = g.getFontMetrics();
                    Rectangle space = fm.getStringBounds(" ", g).getBounds(); // NOI18N
                    Rectangle mark = fm.getStringBounds("\u25B2", g).getBounds(); // NOI18N
                    Rectangle bounds = this.getBounds();
                    
                    Insets insets = this.getInsets();

                    BevelBorderUIResource bevelUI = (BevelBorderUIResource)BorderUIResource.getLoweredBevelBorderUIResource();
                    
                    boolean ascending = obj.getBundleStructure().getSortOrder();

                    int x1, x2, x3, y1, y2, y3; 
                    
                    if(ascending) {
                        // Ascending order.
                        x1 = space.width + mark.width/2;
                        x2 = space.width;
                        x3 = space.width + mark.width;

                        y1 = bounds.y + insets.top+2;
                        y2 = bounds.y + bounds.height - insets.bottom-2;
                        y3 = y2;
                    } else {
                        // Descending order.
                        x1 = space.width;
                        x2 = space.width + mark.width;
                        x3 = space.width + mark.width/2;

                        y1 = bounds.y + insets.top + 2;
                        y2 = y1;
                        y3 = bounds.y + bounds.height - insets.bottom - 2;
                    }

                    // Draw bevel border.
                    // Draw shadow outer color.
                    g.setColor(bevelUI.getShadowOuterColor(this));
                    if(ascending)
                        g.drawLine(x1, y1, x2, y2);
                    else
                        g.drawPolyline(new int[] {x2, x1, x3}, new int[] {y2, y1, y3}, 3);
                        
                    // Draw shadow inner color.
                    g.setColor(bevelUI.getShadowInnerColor(this));
                    if(ascending)
                        g.drawLine(x1, y1+1, x2+1, y2-1);
                    else
                        g.drawPolyline(new int[] {x2-1, x1+1, x3}, new int[] {y2+1, y1+1, y3-1}, 3);
                        
                    // Draw highlihght outer color.
                    g.setColor(bevelUI.getHighlightOuterColor(this));
                    if(ascending)
                        g.drawPolyline(new int[] {x1, x3, x2}, new int[] {y1, y3, y2}, 3);
                    else
                        g.drawLine(x2, y2, x3, y3);
                        
                    // Draw highlight inner color.
                    g.setColor(bevelUI.getHighlightInnerColor(this));
                    if(ascending)
                        g.drawPolyline(new int[] {x1, x3-1, x2+1}, new int[] {y1+1, y3-1, y2-1}, 3);
                    else
                        g.drawLine(x2-1, y2+1, x3, y3-1);
                    
                    g.setColor(oldColor);
                }
            }

        };

        // This subclass of Default column model is provided due correct set of column widths,
        // see the JTable and horizontal scrolling problem in Java Discussion Forum.
        table.setColumnModel(new DefaultTableColumnModel() {
            
            /** Helper listener. */
            private AncestorListener ancestorListener;

            /** Overrides superclass method. */
            public void addColumn(TableColumn aColumn) {
                if (aColumn == null) {
                    throw new IllegalArgumentException("Object is null"); // NOI18N
                }

                tableColumns.addElement(aColumn);
                aColumn.addPropertyChangeListener(this);

                // this method call is only difference with overriden superclass method
                adjustColumnWidths();
                
                // set header renderer this 'ugly' way (for each column),
                // in jdk1.2 is not possible to set default renderer
                // for JTableHeader like in jdk1.3
                aColumn.setHeaderRenderer(headerRenderer);
                
                // Post columnAdded event notification
                    fireColumnAdded(new TableColumnModelEvent(this, 0,
                    getColumnCount() - 1));
            }

            /** Helper method adjusting the table according top component or mode which contains it, the
             * minimal width of column is 1/10 of screen width. */
            private void adjustColumnWidths() {
                // The least initial width of column (1/10 of screen witdh).
                int columnWidth = Toolkit.getDefaultToolkit().getScreenSize().width/10;

                // Try to set widths according parent (viewport) width.
                int totalWidth = 0;
                TopComponent tc = (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, table);
                if(tc != null) {
                    totalWidth = tc.getBounds().width;
                } else {
                    if(ancestorListener == null) {
                        table.addAncestorListener(ancestorListener = new AncestorListener() {
                            /** If the ancestor is TopComponent adjustColumnWidths. */
                            public void ancestorAdded(AncestorEvent evt) {
                                if(evt.getAncestor() instanceof TopComponent) {
                                    adjustColumnWidths();
                                    table.removeAncestorListener(ancestorListener);
                                    ancestorListener = null;
                                }
                            }

                            /** Does nothing. */
                            public void ancestorMoved(AncestorEvent evt) {
                            }

                            /** Does nothing. */
                            public void ancestorRemoved(AncestorEvent evt) {
                            }
                        });
                    }
                }

                // Decrease of insets of scrollpane and insets set in layout manager.
                // Note: Layout constraints hardcoded instead of getting via method call -> 
                // keep consistent with numbers in initComponents method.
                totalWidth -= scrollPane.getInsets().left + scrollPane.getInsets().right + 12 + 11;

                // Helper variable for keeping additional pixels which remains after division.
                int remainder = 0;
                
                // If calculations were succesful try to set the widths in case calculated width
                // for one column is not less than 1/10 of screen width.
                if(totalWidth > 0) {
                    int computedColumnWidth = totalWidth / table.getColumnCount();
                    if(computedColumnWidth > columnWidth) {
                        columnWidth = computedColumnWidth - table.getColumnModel().getColumnMargin();
                        remainder = totalWidth % table.getColumnCount();
                    }
                }
                
                // Set the column widths.
                for (int i = 0; i < table.getColumnCount(); i++) {
                    TableColumn column = table.getColumnModel().getColumn(i);
                    
                    // Add remainder to first column.
                    if(i==0) {
                        // It is necessary to set both 'widths', see javax.swing.TableColumn.
                        column.setPreferredWidth(columnWidth + remainder);
                        column.setWidth(columnWidth + remainder); 
                    } else {                    
                        // It is necessary to set both 'widths', see javax.swing.TableColumn.
                        column.setPreferredWidth(columnWidth);
                        column.setWidth(columnWidth);
                    }
                }
                
                // Recalculate total column width.
                recalcWidthCache();

                // Revalidate table so the widths will fit properly.
                table.revalidate();

                // Repaint header afterwards. Seems stupid but necessary.
                table.getTableHeader().repaint();
            }
        });
        
        // Sets table model.
        table.setModel(ptm);

        // table cell editor
        JTextField textField = new JTextField();
        textField.setBorder(new LineBorder(Color.black));
        table.setDefaultEditor(PropertiesTableModel.StringPair.class,
            new PropertiesTableCellEditor(textField, textComment, textValue));

        // set renderer
        table.setDefaultRenderer(PropertiesTableModel.StringPair.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                PropertiesTableModel.StringPair sp = (PropertiesTableModel.StringPair)value;
                        
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, sp, isSelected, hasFocus, row, column);
                label.setText(sp.getValue() == null ? "" : UtilConvert.unicodesToChars(sp.toString())); // NOI18N
         
                // Set background color.
                if(sp.isKeyType())
                    label.setBackground(settings.getKeyBackground());
                else {
                    if( sp.getValue() != null)
                        label.setBackground(settings.getValueBackground());
                    else
                        label.setBackground(settings.getShadowColor());
                }

                // Set foregound color.
                if(sp.isKeyType())
                    label.setForeground(settings.getKeyColor());
                else
                    label.setForeground(settings.getValueColor());
                
                return label;
            }

            // Overrides superclass method. It adds the highlighting of search occurences in it.
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // If there is a highlihgt flag set do additional drawings.
                if(FindPerformer.getFindPerformer(BundleEditPanel.this.table).isHighlightSearch()) {
                    String text = getText();
                    String findString = FindPerformer.getFindPerformer(BundleEditPanel.this.table).getFindString();

                    // If there is a findString and the cell could contain it go ahead.
                    if(text != null && text.length()>0 && findString != null && findString.length()>0) {
                        int index = 0;
                        int width = (int)g.getFontMetrics().getStringBounds(findString, g).getWidth();

                        Color oldColor = g.getColor();                    
                        // In each iteration highlight one occurence of findString in this cell.
                        while((index = text.indexOf(findString, index)) >= 0) {

                            int x = (int)g.getFontMetrics().getStringBounds(text.substring(0, index), g).getWidth()+this.getInsets().left;

                            g.setColor(settings.getHighlightBackground());
                            g.fillRect(x, 0, width, g.getClipBounds().height);

                            g.setColor(settings.getHighlightColor());
                            g.drawString(findString, x, -(int)g.getFontMetrics().getStringBounds(findString, g).getY());

                            index += findString.length();
                        }
                        // Reset original color.
                        g.setColor(oldColor);
                    }
                }
            }
        });

        // selection listeners
        rowSelections = table.getSelectionModel();

        rowSelections.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    rowSelections = (ListSelectionModel)e.getSource();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            selectionChanged();
                        }
                    });
                }
            });
        
        columnSelections = table.getColumnModel().getSelectionModel();
        columnSelections.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    columnSelections = (ListSelectionModel)e.getSource();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            selectionChanged();
                        }
                    });
                }
            });
            
        // property change listener - listens to editing state of the table
        table.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("tableCellEditor")) { // NOI18N
                    updateEnabled();
                }
            }
        });

        // listens on clikcs on table header, detects column and sort accordingly to chosen one
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel colModel = table.getColumnModel();
                int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
                // No column was clicked.
                if(columnModelIndex < 0)
                    return;
                int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();
                // not detected column
                if (modelIndex < 0)
                    return;
                obj.getBundleStructure().sort(modelIndex);
            }
        });
    } // End of constructor.

    
    /** Stops editing if editing is in run. */
    private void stopEditing() {
        if (!table.isEditing()) return;
        TableCellEditor cellEdit = table.getCellEditor();
        if (cellEdit != null)
            cellEdit.stopCellEditing();
    }

    /** Selection changed event handler. */
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
        } else {
            removeButton.setEnabled(true);
        }

        // fields at the bottom
        if (rowSelections.isSelectionEmpty() || columnSelections.isSelectionEmpty() ||
                rowSelections.getMinSelectionIndex()    != rowSelections.getMaxSelectionIndex() ||
                columnSelections.getMinSelectionIndex() != columnSelections.getMaxSelectionIndex()) {
            if (!table.isEditing()) {
                textComment.setText("");
                textValue.setText("");
            }
        } else {
            if (!table.isEditing()) {
                PropertiesTableModel.StringPair sp =
                    (PropertiesTableModel.StringPair)table.getModel().getValueAt(rowSelections.getMinSelectionIndex(),
                            columnSelections.getMinSelectionIndex());
                textComment.setText(sp.getComment());
                textValue.setText(sp.getValue());

                //          boolean edit = table.editCellAt(rowSelections.getMinSelectionIndex(),
                //                                           columnSelections.getMinSelectionIndex());
            }

            // the selection is ok - set cell editable if:
            // 1) it is not going to be edited as a search result (client property TABLE_SEARCH_RESULT)
            // 2) and if it is not already editing this field
            if (table.getClientProperty(FindPerformer.TABLE_SEARCH_RESULT) == null 
                && (table.getEditingRow() != rowSelections.getMinSelectionIndex()
                || table.getEditingColumn() != columnSelections.getMinSelectionIndex()) ) {
                    table.editCellAt(rowSelections.getMinSelectionIndex(), columnSelections.getMinSelectionIndex());
            }
        }
    }

    /** Updates the enabled status of the fields */
    private void updateEnabled() {
        // always edit value
        textValue.setEditable(table.isEditing());
        textValue.setEnabled(table.isEditing());
        // sometimes edit the comment
        if (table.isEditing()) {
            PropertiesTableModel.StringPair sp = (PropertiesTableModel.StringPair)table.getCellEditor().getCellEditorValue();
            textComment.setEditable(sp.isCommentEditable());
            textComment.setEnabled(sp.isCommentEditable());
        } else {
            textComment.setEditable(false);
            textComment.setEnabled(false);
        }
    }

    /** Returns the main table with all values */
    public JTable getTable() {
        return table;
    }

    /** Initializes #settings variable. */
    private void initSettings() {
        try {
            // Test for editor module only.
            Class editorModule = Class.forName("org.netbeans.modules.editor.EditorModule", // NOI18N
                false, this.getClass().getClassLoader());
            
            Class options = Class.forName("org.netbeans.modules.properties.syntax.PropertiesOptions", // NOI18N
                false, this.getClass().getClassLoader());
            Method settingsMethod = options.getMethod ("getSettings", null); // NOI18N
            settings = (PropertiesSettings)settingsMethod.invoke (options.newInstance(), null);
        } catch (NoClassDefFoundError err) {
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }

        // settings were not gained (editor module is probably not installed), use our defaults
        if(settings == null)
            settings = DEFAULT_SETTINGS;        

        // listen on changes of setting settings
        settings.addPropertyChangeListener(WeakListener.propertyChange(settingsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // settings changed repaint table
                BundleEditPanel.this.repaint();
            }
        }, settings));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        tablePanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        valuePanel = new javax.swing.JPanel();
        commentLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textComment = new javax.swing.JTextArea();
        valueLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textValue = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        tablePanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        
          table.setCellSelectionEnabled(true);
            table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
            scrollPane.setViewportView(table);
            
            gridBagConstraints2 = new java.awt.GridBagConstraints();
          gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints2.insets = new java.awt.Insets(12, 12, 0, 11);
          gridBagConstraints2.weightx = 1.0;
          gridBagConstraints2.weighty = 1.0;
          tablePanel.add(scrollPane, gridBagConstraints2);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(tablePanel, gridBagConstraints1);
        
        
        valuePanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        
        commentLabel.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_CommentLabel"));
          gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints3.insets = new java.awt.Insets(11, 11, 0, 0);
          gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
          valuePanel.add(commentLabel, gridBagConstraints3);
          
          
        
          textComment.setLineWrap(true);
            textComment.setRows(2);
            textComment.setEditable(false);
            textComment.setEnabled(false);
            jScrollPane2.setViewportView(textComment);
            
            gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints3.insets = new java.awt.Insets(11, 11, 0, 0);
          gridBagConstraints3.weightx = 1.0;
          gridBagConstraints3.weighty = 1.0;
          valuePanel.add(jScrollPane2, gridBagConstraints3);
          
          
        valueLabel.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_ValueLabel"));
          gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.gridx = 0;
          gridBagConstraints3.gridy = 1;
          gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints3.insets = new java.awt.Insets(11, 11, 11, 0);
          gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
          valuePanel.add(valueLabel, gridBagConstraints3);
          
          
        
          textValue.setLineWrap(true);
            textValue.setRows(2);
            textValue.setEditable(false);
            textValue.setEnabled(false);
            jScrollPane3.setViewportView(textValue);
            
            gridBagConstraints3 = new java.awt.GridBagConstraints();
          gridBagConstraints3.gridx = 1;
          gridBagConstraints3.gridy = 1;
          gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
          gridBagConstraints3.insets = new java.awt.Insets(7, 11, 11, 0);
          gridBagConstraints3.weightx = 1.0;
          gridBagConstraints3.weighty = 1.0;
          valuePanel.add(jScrollPane3, gridBagConstraints3);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(valuePanel, gridBagConstraints1);
        
        
        buttonPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints4;
        
        addButton.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_AddPropertyButton"));
          addButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  addButtonActionPerformed(evt);
              }
          }
          );
          gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints4.insets = new java.awt.Insets(11, 11, 0, 11);
          gridBagConstraints4.anchor = java.awt.GridBagConstraints.SOUTH;
          gridBagConstraints4.weighty = 1.0;
          buttonPanel.add(addButton, gridBagConstraints4);
          
          
        removeButton.setText(NbBundle.getBundle(BundleEditPanel.class).getString("LBL_RemovePropertyButton"));
          removeButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  removeButtonActionPerformed(evt);
              }
          }
          );
          gridBagConstraints4 = new java.awt.GridBagConstraints();
          gridBagConstraints4.gridx = 0;
          gridBagConstraints4.gridy = 1;
          gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
          gridBagConstraints4.insets = new java.awt.Insets(5, 11, 11, 11);
          gridBagConstraints4.anchor = java.awt.GridBagConstraints.SOUTH;
          buttonPanel.add(removeButton, gridBagConstraints4);
          
          
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        add(buttonPanel, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        stopEditing();
        String key = ((PropertiesTableModel.StringPair)table.getModel().getValueAt(rowSelections.getMinSelectionIndex(), 0)).getValue();
        
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
                obj.getOpenSupport().atomicUndoRedoFlag = new Object();

                for (int i=0; i < obj.getBundleStructure().getEntryCount(); i++) {
                    PropertiesFileEntry entry = obj.getBundleStructure().getNthEntry(i);
                    if (entry != null) {
                        PropertiesStructure ps = entry.getHandler().getStructure();
                        if (ps != null) {
                            ps.deleteItem(key);
                        }
                    }
                }
            } finally {
                // finishes "atomic" undo redo action for special undo redo manager of open support
                obj.getOpenSupport().atomicUndoRedoFlag = null;
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
                obj.getOpenSupport().atomicUndoRedoFlag = new Object();

                String key = UtilConvert.charsToUnicodes(UtilConvert.escapePropertiesSpecialChars(descr.getInputText()));
                // add key to all entries
                for (int i=0; i < obj.getBundleStructure().getEntryCount(); i++) {            
                    PropertiesFileEntry entry = obj.getBundleStructure().getNthEntry(i);
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
                obj.getOpenSupport().atomicUndoRedoFlag = null;
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    private javax.swing.JPanel valuePanel;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea textComment;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea textValue;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables


    /** Interface used for gaining colors and keystrokes for table view, from
     * editor module via soft dependence. There are two implemenations.
     * The default one in this class, containing default colors and key strokes, and implementaion 
     * in syntax/PropertiesOptions class which passes colors and keystrokes from editor settings. That 
     * implementaiton is available only when Editor module is installed. */
    public interface PropertiesSettings {
        public Color getKeyColor();
        public Color getKeyBackground();
        public Color getValueColor();
        public Color getValueBackground();
        public Color getHighlightColor();
        public Color getHighlightBackground();
        public Color getShadowColor();

        public KeyStroke[] getKeyStrokesFindNext();
        public KeyStroke[] getKeyStrokesFindPrevious();
        public KeyStroke[] getKeyStrokesToggleHighlight();
        
        public void settingsUpdated();
        public void addPropertyChangeListener(PropertyChangeListener listener);
        public void removePropertyChangeListener(PropertyChangeListener listener);
   } // End of interface PropertiesSettings.
   
}
