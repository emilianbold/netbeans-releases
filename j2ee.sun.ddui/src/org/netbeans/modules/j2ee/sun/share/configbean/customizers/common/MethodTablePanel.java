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
/*
 * MethodTablePanel.java
 *
 * Created on February 8, 2005, 8:47 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultCellEditor;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


/**
 *
 * @author Rajeshwar Patil
 */
public abstract class MethodTablePanel extends JPanel {

    private MethodTableModel model ;

    protected TableWithToolTips methodTable;
    protected javax.swing.JScrollPane tablePane;
    protected javax.swing.JPanel tablePanel;


    /** Creates new form MethodTablePanel */
    public MethodTablePanel(MethodTableModel model){
        this.model = model;
        initComponents();
    }


    /** Creates a new instance of MethodTablePanel */
    public MethodTablePanel() {
        this.model = getMethodTableModel();
        initComponents();
    }


    protected void setData(){
        methodTable.setModel(model);
    }


    protected abstract MethodTableModel  getMethodTableModel();
    protected abstract String getTablePaneAcsblName();
    protected abstract String getTablePaneAcsblDesc();
    protected abstract String getTablePaneToolTip();


    //Get the tooltip for the table cell
    protected String getToolTip(int row, int col){
        return null;
    }


    //Get the tooltip for the table column header
    protected String getToolTip(int col){
        return null;
    }


    protected void initComponents() {
        //assert(model != null);
        if(model == null) return;
        setLayout(new GridBagLayout());

        methodTable = new TableWithToolTips();

        JTableHeader header = methodTable.getTableHeader();
        ColumnHeaderToolTips tips = new ColumnHeaderToolTips();

        // Assign a tooltip for each of the columns
        header.addMouseMotionListener(tips);

        methodTable.setModel(model);
        methodTable.setRowSelectionAllowed(false);
        methodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        javax.swing.JPanel panel = getPanel();
        if(panel != null){
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
            add(panel, gridBagConstraints);
        }

        //adjustColumnWidth(1, false);
        //adjustColumnWidth(methodTable, 2, "description field template", false);   //NOI18N
        tablePane = new javax.swing.JScrollPane(methodTable);
        tablePane.setOpaque(true);
        tablePane.setToolTipText(getTablePaneToolTip());

        gridBagConstraints = getTableGridBagConstraints();
        add(tablePane, gridBagConstraints); 
        tablePane.getAccessibleContext().setAccessibleName(getTablePaneAcsblName());
        tablePane.getAccessibleContext().setAccessibleDescription(getTablePaneAcsblDesc());
     }    

     protected javax.swing.JPanel getPanel() {
        return null;
    }

    protected GridBagConstraints getTableGridBagConstraints(){
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        return gridBagConstraints;
    }


    protected void setColumnRenderer(TableCellRenderer renderer, int col){
        TableColumnModel columnModel = methodTable.getColumnModel();
        TableColumn column = columnModel.getColumn(col);
        column.setCellRenderer(renderer);
    }


    protected void setColumnEditor(TableCellEditor editor, int col){
        TableColumnModel columnModel = methodTable.getColumnModel();
        TableColumn column = columnModel.getColumn(col);
        column.setCellEditor(editor);
    }


    //get the column at the given index in the given table
    protected TableColumn getColumn(int n, JTable table)
    {
	return ((n >= 0) && (n < table.getColumnCount()))?
	    table.getColumnModel().getColumn(n) :
	    null;
    }


    /* set column to resonable preferred width */
    protected void adjustColumnWidth(int c, boolean includeEditor)
    {
	adjustColumnWidth(methodTable, c, null, includeEditor);
    }


    protected void adjustColumnWidth(JTable table, int c, Object templateValue, boolean includeEditor)
    {
	/* table column */
	TableColumn tc = getColumn(c, table);
	if (tc == null) { 
	    System.out.println("Invalid column index: " + c);           //NOI18N
	    return; 
	}

	/* actual width */
	int actualWidth = tc.getWidth();

	/* initial preferred column width */
	//int prefWidth = tc.getPreferredWidth(); // default is 75
	TableCellRenderer hr = tc.getHeaderRenderer();
	if (hr == null) { hr = table.getTableHeader().getDefaultRenderer(); }
	Component hc = 
            hr.getTableCellRendererComponent(table, tc.getHeaderValue(), false, false, 0, c);
	int prefWidth = hc.getPreferredSize().width;

	/* template width */
	if (templateValue != null) {
	    TableCellRenderer rend = table.getCellRenderer(0, c);
            Component comp = 
                getTableCellRendererComponent(rend, table, templateValue, false, false, 0, c, false);
	    Dimension ps = comp.getPreferredSize();
	    if (ps.width > prefWidth) { prefWidth = ps.width; }
	}

	/* adjust preferred column width */
	for (int r = 0; r < methodTable.getRowCount(); r++) {

	    /* cell editor */
	    if (includeEditor && table.getModel().isCellEditable(r, c)) {
	        TableCellEditor tce = table.getCellEditor(r, c);
	    	if (tce instanceof DefaultCellEditor) {
	    	    Component comp = ((DefaultCellEditor)tce).getComponent();
	    	    if (comp instanceof JComboBox) {
	    	    	Dimension ps = comp.getPreferredSize();
	    	    	if (ps.width > prefWidth) { prefWidth = ps.width; }
	    	    }
		}
	    }

	    /* cell value */
	    Object v = table.getValueAt(r, c);
	    if (v != null) {
	        TableCellRenderer rend = table.getCellRenderer(r, c);
                Component comp = 
                    getTableCellRendererComponent(rend, table, v, false, false, r, c, false);
	        Dimension ps = comp.getPreferredSize();
	        if (ps.width > prefWidth) { prefWidth = ps.width; }
	    }
	}

	/* set column width */
	prefWidth += 4; // right margin offset
        //prefWidth = prefWidth + 25; // right margin offset
        tc.setMinWidth(prefWidth);
        tc.setMaxWidth(prefWidth);
        tc.setPreferredWidth(prefWidth);
	//this.sizeColumnsToFit(-1); // <== reset to preferred widths
    }


    public Component getTableCellRendererComponent(TableCellRenderer rend, 
                JTable tbl, Object val, boolean isSel, boolean focus, int r, 
                    int c, boolean rightAlign) {
	    Component renderer = 
                rend.getTableCellRendererComponent(tbl, val, isSel, focus, r, c);

	    if (renderer instanceof JComponent) {
		JComponent comp = (JComponent)renderer;
                if (comp instanceof JLabel) {
                    String str = (val == null) ? "" : val.toString();
                    FontMetrics fm = getFontMetrics(((JLabel)comp).getFont());
                    String displayedVal = null;
                    if (rightAlign) {
                        displayedVal = rightAlignLongText(
                                            str,
                                            fm,
                                            cellWidth(tbl,(JLabel)comp, r, c));
                    }
                    else {
                        displayedVal = leftAlignLongText(
                                            str,
                                            fm,
                                            cellWidth(tbl,(JLabel)comp, r, c));
                    }
                    ((JLabel)comp).setText(displayedVal);
                    if (!displayedVal.equals(str)) {
                        ((JLabel)comp).setToolTipText(str);
                    }
                    else {
                        ((JLabel)comp).setToolTipText(null);
                    }
                }
		if (isSel) {
		    comp.setBackground(tbl.getSelectionBackground());
		    comp.setOpaque(true);
		} else {
		    TableModel model = tbl.getModel();
		    Color bg = model.isCellEditable(r, c)? 
		   	EnabledBackgroundColor : DisabledBackgroundColor;
		    ///if (((r & 1) == 1) && (model.getColumnCount() > 1)) { bg = brighter(bg, -0.03); }
		    comp.setBackground(bg);
		    comp.setOpaque(true);
		}
	    } else {
		System.out.println("Not a JComponent: " + renderer);
	    }
	    return renderer;
    	}

    
    public int cellWidth(JTable tbl, JLabel label, int r, int c) {
        int cellWidth = tbl.getCellRect(r, c, false).width;
        Insets insets = label.getInsets();
        cellWidth -= insets.left + insets.right;
        return cellWidth;
    }
            
    
    
     /**
     * Determines if a string is longer than the width of the component that
     * contains it.  If the string is longer, a substring of the original string 
     * will be returned and will appear right aligned in the component 
     * with leading '...'.
     *
     * @param  str   the original text
     *         fm    the FontMetrics to use for calculating stringWidth()
     *         width the width of the component that contains original text
     * @return       the substring prepended with '...' that will fit inside 
     *               the component
     * @see          Image
     */   
    public static String rightAlignLongText(String str, FontMetrics fm, int width) {
        if (str.length() > 0) {
            int swidth = fm.stringWidth(str);
            if (width > 0 && swidth > width) {
                int i = 0;
                while (swidth > width && i < str.length()) {
                    i += 1;
                    String test = "..." + str.substring(i);             //NOI18N
                    swidth = fm.stringWidth(test);
                }
                str = "..." + str.substring(i);
            }
        }
        return str;
    }


    /**
     * Determines if a string is longer than the width of the component that
     * contains it.  If the string is longer, a substring of the original string 
     * will be returned and will appear left aligned in the component 
     * with trailing '...'.
     *
     * @param  str   the original text
     *         fm    the FontMetrics to use for calculating stringWidth()
     *         width the width of the component that contains original text
     * @return       the substring with '...' appendage that will fit inside 
     *               the component
     * @see          Image
     */   
    public static String leftAlignLongText(String str, FontMetrics fm, int width) {
        if (str.length() > 0) {
            int swidth = fm.stringWidth(str);
            if (width > 0 && swidth > width) {
                int i = 0;
                while (swidth > width && i < str.length()) {
                    i += 1;
                    String test = str.substring(0, str.length() - i) + "...";   //NOI18N
                    swidth = fm.stringWidth(test);
                }
                str = str.substring(0, str.length() - i) + "...";               //NOI18N
            }
        }
        return str;
    }
    

    public static Color brighter(Color color, double factor) {
	int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
	if ((factor > 0.0) && (factor < 1.0)) { // brighter
	    factor = 1.0 - factor;
	    int f = (int)(1.0 / (1.0 - factor));
	    if ((r == 0) && (g == 0) && (b == 0)) { 
		return new Color(f, f, f); 
	    } else {
	    	if ((r > 0) && (r < f)) { r = f; }
	    	if ((g > 0) && (g < f)) { g = f; }
	    	if ((b > 0) && (b < f)) { b = f; }
	    	return new Color(
		    Math.min((int)(r / factor), 255),
		    Math.min((int)(g / factor), 255), 
		    Math.min((int)(b / factor), 255));
	    }
	} else
	if ((factor < 0.0) && (factor > -1.0)) { // darker
	    factor = 1.0 + factor;
	    return new Color(
		Math.max((int)(r * factor),0), 
		Math.max((int)(g * factor),0), 
		Math.max((int)(b * factor),0));
	}
	return color;
    }


    public void setTablePanePreferredSize(Dimension dimension){
        tablePane.setMinimumSize(dimension);
        tablePane.setPreferredSize(dimension);
    }
    
    
    public static final Color  DisabledBackgroundColor	= Color.lightGray;
    public static final Color  EnabledBackgroundColor	= Color.white;
    public static final Color  DisabledTextColor	= Color.darkGray;

   
    protected class ColumnHeaderToolTips extends MouseMotionAdapter {
        // Current column whose tooltip is being displayed.
        // This variable is used to minimize the calls to setToolTipText().
        int curCol = -1;

        // Maps TableColumn objects to tooltips
        Map tips = new HashMap();

        // If tooltip is null, removes any tooltip text.
        public void setToolTip(TableColumn col, String tooltip) {
            if (tooltip == null) {
                tips.remove(col);
            } else {
                tips.put(col, tooltip);
            }
        }


        public void mouseMoved(MouseEvent evt) {
            int col = -1;
            JTableHeader header = (JTableHeader)evt.getSource();
            JTable table = header.getTable();
            TableColumnModel colModel = table.getColumnModel();
            col = colModel.getColumnIndexAtX(evt.getX());

            // Return if not clicked on any column header
            if (col >= 0) {
                if (col != curCol) {
                    header.setToolTipText(getToolTip(col));
                    curCol = col;
                }
            }
        }
    }


    protected class TableWithToolTips extends JTable {
       public Component prepareRenderer(TableCellRenderer renderer,
                                         int rowIndex, int vColIndex) {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (c instanceof JComponent) {
                JComponent jc = (JComponent)c;
                jc.setToolTipText(getToolTip(rowIndex, vColIndex));
            }
            return c;
        }
    }

}
