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
import org.netbeans.modules.visualweb.propertyeditors.css.model.BorderModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import java.awt.*;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
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
 * Border Data Table that holds the border style info
 * @author  Winston Prakash
 */
public class BorderDataTable extends JTable{
    CssStyleData cssStyleData ;
    /**
     * Holds value of property value.
     */
    private String value;
    
    JComboBox allStyleCombo;
    BorderWidthField allWidthField;
    ColorSelectionField allColorField;
    
    JComboBox topStyleCombo;
    BorderWidthField topWidthField;
    ColorSelectionField topColorField;
    
    JComboBox bottomStyleCombo;
    BorderWidthField bottomWidthField;
    ColorSelectionField bottomColorField;
    
    JComboBox leftStyleCombo;
    BorderWidthField leftWidthField;
    ColorSelectionField leftColorField;
    
    JComboBox rightStyleCombo;
    BorderWidthField rightWidthField;
    ColorSelectionField rightColorField;
    
    private StyleItemListener styleItemListener = new StyleItemListener();
    private WidthPropertyChangeListener widthPropertyChangeListener = new WidthPropertyChangeListener();
    private ColorPropertyChangeListener colorPropertyChangeListener = new ColorPropertyChangeListener();
    
    public BorderDataTable(CssStyleData cssStyleData ){
        super(6,4);
        this.cssStyleData = cssStyleData;
        setDefaultRenderer( JComponent.class, new JComponentCellRenderer() );
        setDefaultEditor( JComponent.class, new JComponentCellEditor() );
        FontMetrics fontMetrics = getFontMetrics(getFont());
        setRowHeight((fontMetrics.getHeight() + 10) > 25 ? (fontMetrics.getHeight() + 10) : 25);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setIntercellSpacing(new Dimension(5,5));
        setTableHeader(null);
        setBackground(new JPanel().getBackground());
        initCells();
        getColumnModel().getColumn(0).setPreferredWidth(50);
        getColumnModel().getColumn(1).setPreferredWidth(75);
        getColumnModel().getColumn(2).setPreferredWidth(125);
        getColumnModel().getColumn(3).setPreferredWidth(125);
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "BORDER_STYLE_TABLE_ACCESS_NAME"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "BORDER_STYLE_TABLE_ACCESS_DESC"));
    }
    
    public void initCells(){
        JPanel colHeader1 = new JPanel();
        //colHeader1.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_SIDE")));
        setValueAt(colHeader1, 0, 0 );
        JPanel colHeader2 = new JPanel();
        colHeader2.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_STYLE")));
        setValueAt(colHeader2, 0, 1 );
        JPanel colHeader3 = new JPanel();
        colHeader3.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_WIDTH")));
        setValueAt(colHeader3, 0, 2 );
        JPanel colHeader6 = new JPanel();
        colHeader6.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_COLOR")));
        setValueAt(colHeader6, 0, 3);
        
        JPanel rowHeader1 = new JPanel();
        rowHeader1.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_ALL")));
        setValueAt(rowHeader1, 1, 0 );
        JPanel rowHeader2 = new JPanel();
        rowHeader2.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_TOP")));
        setValueAt(rowHeader2, 2, 0 );
        JPanel rowHeader3 = new JPanel();
        rowHeader3.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_BOTTOM")));
        setValueAt(rowHeader3, 3, 0 );
        JPanel rowHeader4 = new JPanel();
        rowHeader4.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_LEFT")));
        setValueAt(rowHeader4, 4, 0 );
        JPanel rowHeader5 = new JPanel();
        rowHeader5.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_RIGHT")));
        setValueAt(rowHeader5, 5, 0 );
        
        BorderModel borderModel = new BorderModel();
        
        // All Side Style
        allStyleCombo = new JComboBox();
        allStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_STYLE_ACCESS_NAME"));
        allStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_STYLE_ACCESS_DESC"));
        allStyleCombo.setModel(borderModel.getStyleList());
        allStyleCombo.addItemListener(styleItemListener);
        setValueAt(allStyleCombo, 1, 1);
        
        // All Side Width
        allWidthField = new BorderWidthField();
        allWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
        allWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
        allWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
        setValueAt(allWidthField, 1, 2);
        
        // All Side Width
        allColorField = new ColorSelectionField();
        allColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
        setValueAt(allColorField, 1, 3);
        
        // Set the value for the top side border
        
        // Top Side Style
        topStyleCombo = new JComboBox();
        topStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_STYLE_ACCESS_NAME"));
        topStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_STYLE_ACCESS_DESC"));
        topStyleCombo.setModel(borderModel.getStyleList());
        if(cssStyleData.getProperty(CssStyleData.BORDER_TOP_STYLE) != null){
            topStyleCombo.setSelectedItem(cssStyleData.getProperty(CssStyleData.BORDER_TOP_STYLE));
        }
        topStyleCombo.addItemListener(styleItemListener);
        setValueAt(topStyleCombo, 2, 1);
        
        // Top Side Width
        topWidthField = new BorderWidthField();
        topWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
        topWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
        topWidthField.setWidthString(cssStyleData.getProperty(CssStyleData.BORDER_TOP_WIDTH));
        topWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
        setValueAt(topWidthField, 2, 2);
        
        // Top Side Color
        topColorField = new ColorSelectionField();
        topColorField.setColorString(cssStyleData.getProperty(CssStyleData.BORDER_TOP_COLOR));
        topColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
        setValueAt(topColorField, 2, 3);
        
        // Set the Bottom Side data
        
        // Bottom Side Style
        bottomStyleCombo = new JComboBox();
        bottomStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_STYLE_ACCESS_NAME"));
        bottomStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_STYLE_ACCESS_DESC"));
        bottomStyleCombo.setModel(borderModel.getStyleList());
        if(cssStyleData.getProperty(CssStyleData.BORDER_BOTTOM_STYLE) != null){
            bottomStyleCombo.setSelectedItem(cssStyleData.getProperty(CssStyleData.BORDER_BOTTOM_STYLE));
        }
        bottomStyleCombo.addItemListener(styleItemListener);
        setValueAt(bottomStyleCombo, 3, 1);
        
        // Bottom Side Width
        bottomWidthField = new BorderWidthField();
        bottomWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
        bottomWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
        bottomWidthField.setWidthString(cssStyleData.getProperty(CssStyleData.BORDER_BOTTOM_WIDTH));
        bottomWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
        setValueAt(bottomWidthField, 3, 2);
        
        // Bottom Side Width
        bottomColorField = new ColorSelectionField();
        bottomColorField.setColorString(cssStyleData.getProperty(CssStyleData.BORDER_BOTTOM_COLOR));
        bottomColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
        setValueAt(bottomColorField, 3, 3);
        
        
        // Set the Left Side data
        
        // Left Side Style
        leftStyleCombo = new JComboBox( new String[] {});
        leftStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_STYLE_ACCESS_NAME"));
        leftStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_STYLE_ACCESS_DESC"));
        leftStyleCombo.setModel(borderModel.getStyleList());
        if(cssStyleData.getProperty(CssStyleData.BORDER_LEFT_STYLE) != null){
            leftStyleCombo.setSelectedItem(cssStyleData.getProperty(CssStyleData.BORDER_LEFT_STYLE));
        }
        leftStyleCombo.addItemListener(styleItemListener);
        setValueAt(leftStyleCombo, 4, 1);
        
        // Left Side Width
        leftWidthField = new BorderWidthField();
        leftWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
        leftWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
        leftWidthField.setWidthString(cssStyleData.getProperty(CssStyleData.BORDER_LEFT_WIDTH));
        leftWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
        setValueAt(leftWidthField, 4, 2);
        
        // Left Side Width
        leftColorField = new ColorSelectionField();
        leftColorField.setColorString(cssStyleData.getProperty(CssStyleData.BORDER_LEFT_COLOR));
        leftColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
        setValueAt(leftColorField, 4, 3);
        
        // Set the Right Side data
        
        // Right Side Style
        rightStyleCombo = new JComboBox();
        rightStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_STYLE_ACCESS_NAME"));
        rightStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_STYLE_ACCESS_DESC"));
        rightStyleCombo.setModel(borderModel.getStyleList());
        if(cssStyleData.getProperty(CssStyleData.BORDER_RIGHT_STYLE) != null){
            rightStyleCombo.setSelectedItem(cssStyleData.getProperty(CssStyleData.BORDER_RIGHT_STYLE));
        }
        rightStyleCombo.addItemListener(styleItemListener);
        setValueAt(rightStyleCombo, 5, 1);
        
        
        // Right Side Width
        rightWidthField = new BorderWidthField();
        rightWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_ACCESS_NAME"),
                NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
        rightWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_ACCESS_DESC"),
                NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
        rightWidthField.setWidthString(cssStyleData.getProperty(CssStyleData.BORDER_RIGHT_WIDTH));
        rightWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
        setValueAt(rightWidthField, 5, 2);
        
        // Right Side Color
        rightColorField = new ColorSelectionField();
        rightColorField.setColorString(cssStyleData.getProperty(CssStyleData.BORDER_RIGHT_COLOR));
        rightColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
        setValueAt(rightColorField, 5, 3);
        
        checkBorderStyleAll();
        checkBorderWidthAll();
        checkBorderColorAll();
    }
    
    private void checkBorderStyleAll(){
        allStyleCombo.removeItemListener(styleItemListener);
        
        String topStyle = cssStyleData.getProperty(CssStyleData.BORDER_TOP_STYLE);
        String bottomStyle = cssStyleData.getProperty(CssStyleData.BORDER_BOTTOM_STYLE);
        String leftStyle = cssStyleData.getProperty(CssStyleData.BORDER_LEFT_STYLE);
        String rightStyle = cssStyleData.getProperty(CssStyleData.BORDER_RIGHT_STYLE);
        
        if ((topStyle != null) && (bottomStyle != null) && (leftStyle != null) && (rightStyle != null) &&
                (topStyle.equals(bottomStyle)) && (topStyle.equals(leftStyle)) &&
                (topStyle.equals(rightStyle))){
            allStyleCombo.setSelectedItem(topStyle);
        }else{
            allStyleCombo.setSelectedIndex(0);
        }
        allStyleCombo.addItemListener(styleItemListener);
    }
    
    private void checkBorderWidthAll(){
        allWidthField.removeCssPropertyChangeListener(widthPropertyChangeListener);
        
        String topWidth = cssStyleData.getProperty(CssStyleData.BORDER_TOP_WIDTH);
        String bottomWidth = cssStyleData.getProperty(CssStyleData.BORDER_BOTTOM_WIDTH);
        String leftWidth = cssStyleData.getProperty(CssStyleData.BORDER_LEFT_WIDTH);
        String rightWidth = cssStyleData.getProperty(CssStyleData.BORDER_RIGHT_WIDTH);
        
        if ((topWidth != null) && (bottomWidth != null) && (leftWidth != null) && (rightWidth != null) &&
                (topWidth.equals(bottomWidth)) && (topWidth.equals(leftWidth)) &&
                (topWidth.equals(rightWidth))){
            allWidthField.setWidthString(topWidth);
        }else{
            allWidthField.setWidthString(null);
        }
        allWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
    }
    
    private void checkBorderColorAll(){
        allColorField.removeCssPropertyChangeListener(colorPropertyChangeListener);
        String topColor = cssStyleData.getProperty(CssStyleData.BORDER_TOP_COLOR);
        String bottomColor = cssStyleData.getProperty(CssStyleData.BORDER_BOTTOM_COLOR);
        String leftColor = cssStyleData.getProperty(CssStyleData.BORDER_LEFT_COLOR);
        String rightColor = cssStyleData.getProperty(CssStyleData.BORDER_RIGHT_COLOR);
        
        if ((topColor != null) && (bottomColor != null) && (leftColor != null) && (rightColor != null) &&
                (topColor.equals(bottomColor)) && (topColor.equals(leftColor)) &&
                (topColor.equals(rightColor))){
            allColorField.setColorString(topColor);
        }else{
            allColorField.setColorString(null);
        }
        allColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
    }
    
    private class StyleItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent evt) {
            if(evt.getSource() == allStyleCombo){
                topStyleCombo.removeItemListener(styleItemListener);
                bottomStyleCombo.removeItemListener(styleItemListener);
                leftStyleCombo.removeItemListener(styleItemListener);
                rightStyleCombo.removeItemListener(styleItemListener);
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_TOP_STYLE, allStyleCombo.getSelectedItem().toString());
                topStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_BOTTOM_STYLE, allStyleCombo.getSelectedItem().toString());
                bottomStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_LEFT_STYLE, allStyleCombo.getSelectedItem().toString());
                leftStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_RIGHT_STYLE, allStyleCombo.getSelectedItem().toString());
                rightStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                
                topStyleCombo.addItemListener(styleItemListener);
                bottomStyleCombo.addItemListener(styleItemListener);
                leftStyleCombo.addItemListener(styleItemListener);
                rightStyleCombo.addItemListener(styleItemListener);
            }else if(evt.getSource() == topStyleCombo){
                cssStyleData.modifyProperty(CssStyleData.BORDER_TOP_STYLE, topStyleCombo.getSelectedItem().toString());
                checkBorderStyleAll();
            }else if(evt.getSource() == bottomStyleCombo){
                cssStyleData.modifyProperty(CssStyleData.BORDER_BOTTOM_STYLE, bottomStyleCombo.getSelectedItem().toString());
                checkBorderStyleAll();
                
            }else if(evt.getSource() == leftStyleCombo){
                cssStyleData.modifyProperty(CssStyleData.BORDER_LEFT_STYLE, leftStyleCombo.getSelectedItem().toString());
                checkBorderStyleAll();
                
            }else if(evt.getSource() == rightStyleCombo){
                cssStyleData.modifyProperty(CssStyleData.BORDER_RIGHT_STYLE, rightStyleCombo.getSelectedItem().toString());
                checkBorderStyleAll();
                
            }
            
        }
    }
    
    private class WidthPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() == allWidthField){
                topWidthField.removeCssPropertyChangeListener(widthPropertyChangeListener);
                bottomWidthField.removeCssPropertyChangeListener(widthPropertyChangeListener);
                leftWidthField.removeCssPropertyChangeListener(widthPropertyChangeListener);
                rightWidthField.removeCssPropertyChangeListener(widthPropertyChangeListener);
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_TOP_WIDTH, evt.getNewValue().toString());
                topWidthField.setWidthString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_BOTTOM_WIDTH, evt.getNewValue().toString());
                bottomWidthField.setWidthString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_LEFT_WIDTH, evt.getNewValue().toString());
                leftWidthField.setWidthString(evt.getNewValue().toString());
                
                cssStyleData.modifyProperty(CssStyleData.BORDER_RIGHT_WIDTH, evt.getNewValue().toString());
                rightWidthField.setWidthString(evt.getNewValue().toString());
                
                topWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
                bottomWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
                leftWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
                rightWidthField.addCssPropertyChangeListener(widthPropertyChangeListener);
            }else if(evt.getSource() == topWidthField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_TOP_WIDTH, evt.getNewValue().toString());
                checkBorderWidthAll();
            }else if(evt.getSource() == bottomWidthField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_BOTTOM_WIDTH, evt.getNewValue().toString());
                checkBorderWidthAll();
            }else if(evt.getSource() == leftWidthField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_LEFT_WIDTH, evt.getNewValue().toString());
                checkBorderWidthAll();
            }else if(evt.getSource() == rightWidthField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_RIGHT_WIDTH, evt.getNewValue().toString());
                checkBorderWidthAll();
            }
        }
    }
    
    private class ColorPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() == allColorField){
                topColorField.removeCssPropertyChangeListener(colorPropertyChangeListener);
                bottomColorField.removeCssPropertyChangeListener(colorPropertyChangeListener);
                leftColorField.removeCssPropertyChangeListener(colorPropertyChangeListener);
                rightColorField.removeCssPropertyChangeListener(colorPropertyChangeListener);
                
                topColorField.setColorString(allColorField.getColorString());
                cssStyleData.modifyProperty(CssStyleData.BORDER_TOP_COLOR, allColorField.getColorString());
                
                bottomColorField.setColorString(allColorField.getColorString());
                cssStyleData.modifyProperty(CssStyleData.BORDER_BOTTOM_COLOR, allColorField.getColorString());
                
                leftColorField.setColorString(allColorField.getColorString());
                cssStyleData.modifyProperty(CssStyleData.BORDER_LEFT_COLOR, allColorField.getColorString());
                
                rightColorField.setColorString(allColorField.getColorString());
                cssStyleData.modifyProperty(CssStyleData.BORDER_RIGHT_COLOR, allColorField.getColorString());
                
                topColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
                bottomColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
                leftColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
                rightColorField.addCssPropertyChangeListener(colorPropertyChangeListener);
            }if(evt.getSource() == topColorField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_TOP_COLOR, topColorField.getColorString());
                checkBorderColorAll();
            }if(evt.getSource() == bottomColorField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_BOTTOM_COLOR, bottomColorField.getColorString());
                checkBorderColorAll();
            }if(evt.getSource() == leftColorField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_LEFT_COLOR, leftColorField.getColorString());
                checkBorderColorAll();
            }if(evt.getSource() == rightColorField){
                cssStyleData.modifyProperty(CssStyleData.BORDER_RIGHT_COLOR, rightColorField.getColorString());
                checkBorderColorAll();
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
    
    class JComponentCellEditor implements TableCellEditor, TreeCellEditor,Serializable {
        
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

