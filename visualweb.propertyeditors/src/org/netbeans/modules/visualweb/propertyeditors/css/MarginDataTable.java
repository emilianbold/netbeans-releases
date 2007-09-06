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
package org.netbeans.modules.visualweb.propertyeditors.css;

import org.netbeans.modules.visualweb.propertyeditors.css.model.BorderData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import java.awt.*;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;

import java.awt.Component;
import java.awt.event.*;
import java.awt.AWTEvent;
import java.lang.Boolean;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.EventObject;
import javax.swing.tree.*;
import java.io.Serializable;
import javax.swing.*;
import org.openide.util.NbBundle;


/**
 * Margin Data Table that holds the margin style info
 * @author  Winston Prakash
 */
public class MarginDataTable extends JTable{
    CssStyleData cssStyleData ;
    /**
     * Holds value of property value.
     */
    private String value;
    
    MarginWidthField allMarginField;
    PaddingWidthField allPaddingField;
    MarginWidthField topMarginField;
    PaddingWidthField topPaddingField;
    MarginWidthField bottomMarginField;
    PaddingWidthField leftPaddingField;
    MarginWidthField rightMarginField;
    PaddingWidthField rightPaddingField;
    PaddingWidthField bottomPaddingField;
    MarginWidthField leftMarginField;
    
    private MarginPropertyChangeListener marginPropertyChangeListener = new MarginPropertyChangeListener();
    private PaddingPropertyChangeListener paddingPropertyChangeListener = new PaddingPropertyChangeListener();
    
    public MarginDataTable(CssStyleData cssStyleData ){
        super(6,3);
        this.cssStyleData = cssStyleData;
        setDefaultRenderer( JComponent.class, new JComponentCellRenderer() );
        setDefaultEditor( JComponent.class, new JComponentCellEditor() );
        FontMetrics fontMetrics = getFontMetrics(getFont());
        setRowHeight((fontMetrics.getHeight() + 10) > 25 ? (fontMetrics.getHeight() + 10) : 25);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setIntercellSpacing(new Dimension(5,5));
        //setShowGrid(false);
        setTableHeader(null);
        setBackground(new JPanel().getBackground());
        initCells();
        getColumnModel().getColumn(0).setPreferredWidth(30);
        getColumnModel().getColumn(1).setPreferredWidth(150);
        getColumnModel().getColumn(2).setPreferredWidth(150);
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "MARGIN_STYLE_TABLE_ACCESS_NAME"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "MARGIN_STYLE_TABLE_ACCESS_DESC"));
    }
    
    public void initCells(){
        JPanel colHeader1 = new JPanel();
        //colHeader1.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_SIDE")));
        setValueAt(colHeader1, 0, 0 );
        JPanel colHeader2 = new JPanel();
        colHeader2.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_ALL")));
        setValueAt(colHeader2, 0, 1 );
        JPanel colHeader3 = new JPanel();
        colHeader3.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "PADDING")));
        setValueAt(colHeader3, 0, 2 );
        
        JPanel rowHeader1 = new JPanel();
        rowHeader1.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_ALL")));
        setValueAt(rowHeader1, 1, 0 );
        JPanel rowHeader2 = new JPanel();
        rowHeader2.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_TOP")));
        setValueAt(rowHeader2, 2, 0 );
        JPanel rowHeader3 = new JPanel();
        rowHeader3.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_BOTTOM")));
        setValueAt(rowHeader3, 3, 0 );
        JPanel rowHeader4 = new JPanel();
        rowHeader4.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_LEFT")));
        setValueAt(rowHeader4, 4, 0 );
        JPanel rowHeader5 = new JPanel();
        rowHeader5.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_RIGHT")));
        setValueAt(rowHeader5, 5, 0 );
        
        // All Side Margin
        allMarginField = new MarginWidthField();
        allMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
        allMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
        allMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
        setValueAt(allMarginField, 1, 1);
        
        // All Side Margin
        allPaddingField = new PaddingWidthField();
        allPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
        allPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
        allPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
        setValueAt(allPaddingField, 1, 2);
        
        // Top Side Margin
        topMarginField = new MarginWidthField();
        topMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
        topMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
        topMarginField.setMarginString(cssStyleData.getProperty(CssStyleData.MARGIN_TOP));
        topMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
        setValueAt(topMarginField, 2, 1);
        
        // Top Side Margin
        topPaddingField = new PaddingWidthField();
        topPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
        topPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
        topPaddingField.setPaddingString(cssStyleData.getProperty(CssStyleData.PADDING_TOP));
        topPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
        setValueAt(topPaddingField, 2, 2);
        
        // Bottom Side Margin
        bottomMarginField = new MarginWidthField();
        bottomMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
        bottomMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
        bottomMarginField.setMarginString(cssStyleData.getProperty(CssStyleData.MARGIN_BOTTOM));
        bottomMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
        setValueAt(bottomMarginField, 3, 1);
        
        // Bottom Side Margin
        bottomPaddingField = new PaddingWidthField();
        bottomPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
        bottomPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
        bottomPaddingField.setPaddingString(cssStyleData.getProperty(CssStyleData.PADDING_BOTTOM));
        bottomPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
        setValueAt(bottomPaddingField, 3, 2);
        
        // Left Side Margin
        leftMarginField = new MarginWidthField();
        leftMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
        leftMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
        leftMarginField.setMarginString(cssStyleData.getProperty(CssStyleData.MARGIN_LEFT));
        leftMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
        setValueAt(leftMarginField, 4, 1);
        
        // Left Side Margin
        leftPaddingField = new PaddingWidthField();
        leftPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
        leftPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
        leftPaddingField.setPaddingString(cssStyleData.getProperty(CssStyleData.PADDING_LEFT));
        leftPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
        setValueAt(leftPaddingField, 4, 2);
        
        // Left Side Margin
        rightMarginField = new MarginWidthField();
        rightMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
        rightMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
        rightMarginField.setMarginString(cssStyleData.getProperty(CssStyleData.MARGIN_RIGHT));
        rightMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
        setValueAt(rightMarginField, 5, 1);
        
        // Left Side Margin
        rightPaddingField = new PaddingWidthField();
        rightPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
        rightPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
        rightPaddingField.setPaddingString(cssStyleData.getProperty(CssStyleData.PADDING_RIGHT));
        rightPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
        setValueAt(rightPaddingField, 5, 2);
    }
    
    private void checkMarginAll(){
        allMarginField.removeCssPropertyChangeListener(marginPropertyChangeListener);
        String topMargin = cssStyleData.getProperty(CssStyleData.MARGIN_TOP);
        String bottomMargin = cssStyleData.getProperty(CssStyleData.MARGIN_BOTTOM);
        String leftMargin = cssStyleData.getProperty(CssStyleData.MARGIN_LEFT);
        String rightMargin = cssStyleData.getProperty(CssStyleData.MARGIN_RIGHT);
        
        if ((topMargin != null) && (bottomMargin != null) && (leftMargin != null) && (rightMargin != null) &&
                (topMargin.equals(bottomMargin)) && (topMargin.equals(leftMargin)) &&
                (topMargin.equals(rightMargin))){
            allMarginField.setMarginString(topMargin);
        }else{
            allMarginField.setMarginString(null);
        }
        allMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
    }
    
    private void checkPaddingAll(){
        allPaddingField.removeCssPropertyChangeListener(paddingPropertyChangeListener);
        String topPadding = cssStyleData.getProperty(CssStyleData.PADDING_TOP);
        String bottomPadding = cssStyleData.getProperty(CssStyleData.PADDING_BOTTOM);
        String leftPadding = cssStyleData.getProperty(CssStyleData.PADDING_LEFT);
        String rightPadding = cssStyleData.getProperty(CssStyleData.PADDING_RIGHT);
        
        if ((topPadding != null) && (bottomPadding != null) && (leftPadding != null) && (rightPadding != null) &&
                (topPadding.equals(bottomPadding)) && (topPadding.equals(leftPadding)) &&
                (topPadding.equals(rightPadding))){
            allPaddingField.setPaddingString(topPadding);
        }else{
            allPaddingField.setPaddingString(null);
        }
        
        allPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
    }
    
    private class MarginPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() == allMarginField){
                
                topMarginField.removeCssPropertyChangeListener(marginPropertyChangeListener);
                bottomMarginField.removeCssPropertyChangeListener(marginPropertyChangeListener);
                leftMarginField.removeCssPropertyChangeListener(marginPropertyChangeListener);
                rightMarginField.removeCssPropertyChangeListener(marginPropertyChangeListener);
                
                cssStyleData.modifyProperty(CssStyleData.MARGIN_TOP, evt.getNewValue().toString());
                topMarginField.setMarginString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.MARGIN_BOTTOM, evt.getNewValue().toString());
                bottomMarginField.setMarginString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.MARGIN_LEFT, evt.getNewValue().toString());
                leftMarginField.setMarginString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.MARGIN_RIGHT, evt.getNewValue().toString());
                rightMarginField.setMarginString(evt.getNewValue().toString());
                
                topMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
                bottomMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
                leftMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
                rightMarginField.addCssPropertyChangeListener(marginPropertyChangeListener);
            }else if(evt.getSource() == topMarginField){
                cssStyleData.modifyProperty(CssStyleData.MARGIN_TOP, evt.getNewValue().toString());
                checkMarginAll();
            }else if(evt.getSource() == bottomMarginField){
                cssStyleData.modifyProperty(CssStyleData.MARGIN_BOTTOM, evt.getNewValue().toString());
                checkMarginAll();
            }if(evt.getSource() == leftMarginField){
                cssStyleData.modifyProperty(CssStyleData.MARGIN_LEFT, evt.getNewValue().toString());
                checkMarginAll();
            }if(evt.getSource() == rightMarginField){
                cssStyleData.modifyProperty(CssStyleData.MARGIN_RIGHT, evt.getNewValue().toString());
                checkMarginAll();
            }
        }
    }
    
    private class PaddingPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() == allPaddingField){
                topPaddingField.removeCssPropertyChangeListener(paddingPropertyChangeListener);
                bottomPaddingField.removeCssPropertyChangeListener(paddingPropertyChangeListener);
                leftPaddingField.removeCssPropertyChangeListener(paddingPropertyChangeListener);
                rightPaddingField.removeCssPropertyChangeListener(paddingPropertyChangeListener);
                
                cssStyleData.modifyProperty(CssStyleData.PADDING_TOP, evt.getNewValue().toString());
                topPaddingField.setPaddingString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.PADDING_BOTTOM, evt.getNewValue().toString());
                bottomPaddingField.setPaddingString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.PADDING_LEFT, evt.getNewValue().toString());
                leftPaddingField.setPaddingString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.PADDING_RIGHT, evt.getNewValue().toString());
                rightPaddingField.setPaddingString(evt.getNewValue().toString());
                
                topPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
                bottomPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
                leftPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
                rightPaddingField.addCssPropertyChangeListener(paddingPropertyChangeListener);
            }else if(evt.getSource() == topPaddingField){
                cssStyleData.modifyProperty(CssStyleData.PADDING_TOP, evt.getNewValue().toString());
                checkPaddingAll();
            }else if(evt.getSource() == bottomPaddingField){
                cssStyleData.modifyProperty(CssStyleData.PADDING_BOTTOM, evt.getNewValue().toString());
                checkPaddingAll();
            }else if(evt.getSource() == leftPaddingField){
                cssStyleData.modifyProperty(CssStyleData.PADDING_LEFT, evt.getNewValue().toString());
                checkPaddingAll();
            }else if(evt.getSource() == rightPaddingField){
                cssStyleData.modifyProperty(CssStyleData.PADDING_RIGHT, evt.getNewValue().toString());
                checkPaddingAll();
            }
        }
    }
    
    class PropertyChangeListenerImpl implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt) {
        }
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellRenderer renderer = tableColumn.getCellRenderer();
        if (renderer == null) {
            Class c = getColumnClass(column);
            if( c.equals(Object.class) ) {
                Object o = getValueAt(row,column);
                if( o != null )
                    c = getValueAt(row,column).getClass();
            }
            renderer = getDefaultRenderer(c);
        }
        return renderer;
    }
    
    public TableCellEditor getCellEditor(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellEditor editor = tableColumn.getCellEditor();
        if (editor == null) {
            Class c = getColumnClass(column);
            if( c.equals(Object.class) ) {
                Object o = getValueAt(row,column);
                if( o != null )
                    c = getValueAt(row,column).getClass();
            }
            editor = getDefaultEditor(c);
        }
        return editor;
    }
    
    
    
    class JComponentCellRenderer implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent comp = (JComponent)value;
            comp.setMinimumSize(new Dimension(100,25));
            return comp;
        }
    }
    
    class JComponentCellEditor implements TableCellEditor, TreeCellEditor,
            Serializable {
        
        protected EventListenerList listenerList = new EventListenerList();
        transient protected ChangeEvent changeEvent = null;
        
        protected JComponent editorComponent = null;
        protected JComponent container = null;		// Can be tree or table
        
        
        public Component getComponent() {
            return editorComponent;
        }
        
        
        public Object getCellEditorValue() {
            return editorComponent;
        }
        
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }
        
        public boolean shouldSelectCell(EventObject anEvent) {
            if( editorComponent != null && anEvent instanceof MouseEvent
                    && ((MouseEvent)anEvent).getID() == MouseEvent.MOUSE_PRESSED ) {
                Component dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, 3, 3 );
                MouseEvent e = (MouseEvent)anEvent;
                MouseEvent e2 = new MouseEvent( dispatchComponent, MouseEvent.MOUSE_RELEASED,
                        e.getWhen() + 100000, e.getModifiers(), 3, 3, e.getClickCount(),
                        e.isPopupTrigger() );
                dispatchComponent.dispatchEvent(e2);
                e2 = new MouseEvent( dispatchComponent, MouseEvent.MOUSE_CLICKED,
                        e.getWhen() + 100001, e.getModifiers(), 3, 3, 1,
                        e.isPopupTrigger() );
                dispatchComponent.dispatchEvent(e2);
            }
            return false;
        }
        
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
        
        public void cancelCellEditing() {
            fireEditingCanceled();
        }
        
        public void addCellEditorListener(CellEditorListener l) {
            listenerList.add(CellEditorListener.class, l);
        }
        
        public void removeCellEditorListener(CellEditorListener l) {
            listenerList.remove(CellEditorListener.class, l);
        }
        
        protected void fireEditingStopped() {
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==CellEditorListener.class) {
                    // Lazily create the event:
                    if (changeEvent == null)
                        changeEvent = new ChangeEvent(this);
                    ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
                }
            }
        }
        
        protected void fireEditingCanceled() {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==CellEditorListener.class) {
                    // Lazily create the event:
                    if (changeEvent == null)
                        changeEvent = new ChangeEvent(this);
                    ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
                }
            }
        }
        
        // implements javax.swing.tree.TreeCellEditor
        public Component getTreeCellEditorComponent(JTree tree, Object value,
                boolean isSelected, boolean expanded, boolean leaf, int row) {
            String         stringValue = tree.convertValueToText(value, isSelected,
                    expanded, leaf, row, false);
            
            editorComponent = (JComponent)value;
            container = tree;
            return editorComponent;
        }
        
        // implements javax.swing.table.TableCellEditor
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            editorComponent = (JComponent)value;
            container = table;
            return editorComponent;
        }
        
    } // End of class JComponentCellEditor
    
}

