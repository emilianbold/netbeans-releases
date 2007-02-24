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
 * Created on Jun 9, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.treetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

/**
 * @author sumitabhk
 *
 */
/**
 * A TreeCellRenderer that displays a JTree.
 */
public class TreeTableCellRenderer extends JTree implements TableCellRenderer{

	/**
	 * 
	 */
	public TreeTableCellRenderer()
	{
		super();
	}

	/** Last table/tree row asked to renderer. */
	protected int visibleRow;

	public TreeTableCellRenderer(TreeModel model) {
		super(model);
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.putClientProperty("JTree.lineStyle", "Angled");
		setShowsRootHandles(true);
		
	
		/*this.addTreeSelectionListener( new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e)
			{
				// TODO Auto-generated method stub
				ETSystem.out.println("Value changed for tree selection");
			}
		}
		);*/
		
//		this.addTreeExpansionListener( new TreeExpansionListener() {
//
//			public void treeExpanded(TreeExpansionEvent event)
//			{
//				// TODO Auto-generated method stub
//				ETSystem.out.println("Tree expanded");
//				
//			}
//
//			public void treeCollapsed(TreeExpansionEvent event)
//			{
//				// TODO Auto-generated method stub
//				ETSystem.out.println("Tree collapsed");
//				
//			}
//		}
//		);
	}

	/**
	 * updateUI is overridden to set the colors of the Tree's renderer
	 * to match that of the table.
	 */
	public void updateUI() {
		super.updateUI();
		// Make the tree's cell renderer use the table's cell selection
		// colors. 
		TreeCellRenderer tcr = getCellRenderer();
		if (tcr instanceof DefaultTreeCellRenderer) {
		DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr); 
		// For 1.1 uncomment this, 1.2 has a bug that will cause an
		// exception to be thrown if the border selection color is
		// null.
		//dtcr.setBorderSelectionColor(null);
		dtcr.setTextSelectionColor(UIManager.getColor
					   ("Table.selectionForeground"));
		dtcr.setBackgroundSelectionColor(UIManager.getColor
						("Table.selectionBackground"));
		}
	}

	/**
	 * Sets the row height of the tree, and forwards the row height to
	 * the table.
	 */
	public void setRowHeight(int rowHeight) { 
		if (rowHeight > 0) {
		super.setRowHeight(rowHeight); 
		if (this != null &&
			this.getRowHeight() != rowHeight) {
			this.setRowHeight(getRowHeight()); 
		}
		}
	}

	/**
	 * This is overridden to set the height to match that of the JTable.
	 */
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, 0, w, this.getHeight());
	}

	/**
	 * Sublcassed to translate the graphics such that the last visible
	 * row will be drawn at 0,0.
	 */
	public void paint(Graphics g) {
		g.translate(0, -visibleRow * getRowHeight());
	    
		super.paint(g);
	}

	/**
	 * TreeCellRenderer method. Overridden to update the visible row.
	 */
	public Component getTableCellRendererComponent(JTable table,
							   Object value,
							   boolean isSelected,
							   boolean hasFocus,
							   int row, int column) {
		this.putClientProperty("JTree.lineStyle", "Angled");
		setShowsRootHandles(true);
		if(isSelected)
		{
			setBackground(table.getSelectionBackground());
			
		}
		else
			setBackground(table.getBackground());

		visibleRow = row;
		return this;
	}

}




