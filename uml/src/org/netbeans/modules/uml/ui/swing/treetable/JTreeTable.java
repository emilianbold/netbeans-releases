/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.ui.swing.treetable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import javax.swing.table.*;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditorResources;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;


/**
 * This example shows how to create a simple JTreeTable component, 
 * by using a JTree as a renderer (and editor) for the cells in a 
 * particular column in the JTable.  
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 */
public class JTreeTable extends JTable implements ActionListener{
    /** A subclass of JTree. */
    protected TreeTableCellRenderer tree;

    public JTreeTable(TreeTableModel treeTableModel) 
    {
		// Create the tree. It will be used as a renderer and editor. 
		this(treeTableModel, null);
    }

	public JTreeTable(TreeTableModel treeTableModel, TreeTableCellRenderer renderer) 
	{
		super();

		// Create the tree. It will be used as a renderer and editor.
		if (renderer != null)
		{ 
			tree = renderer;
		}
		else
		{
			tree = new TreeTableCellRenderer(treeTableModel);
		}

		tree.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		

		// Install a tableModel representing the visible rows in the tree. 
		super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

		// Force the JTable and JTree to share their row selection models. 
		ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel()); 

		// Install the tree editor renderer and editor. 
		setDefaultRenderer(TreeTableModel.class, tree); 
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor(tree));

		// No grid.
		setShowGrid(false);
		
		//setCellSelectionEnabled(true);
		// No intercell spacing
		setIntercellSpacing(new Dimension(0, 0));	

		// And update the height of the trees row to match that of
		// the table.
//		if (tree.getRowHeight() < 1) {
			// Metal looks better like this.
//			setRowHeight(18);
			setRowHeight(getFont().getSize()+5);
//		}

		this.setShowVerticalLines(true);
		this.doLayout();
		
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		String srcText = source.getText();
		if (srcText.equals(PropertyEditorResources.getString("PropertyEditor.Create_Menu")))
		{
			//PropertyEditor.instance().onPopupCreate();
		}
	}

	/**
	 * Overridden to invoke repaint for the particular location if
	 * the column contains the tree. This is done as the tree editor does
	 * not fill the bounds of the cell, we need the renderer to paint
	 * the tree in the background, and then draw the editor over it.
	 */
	public boolean editCellAt(int row, int column, EventObject e){
	boolean retValue = super.editCellAt(row, column, e);
	if (retValue && getColumnClass(column) == TreeTableModel.class) {
		repaint(getCellRect(row, column, false));
	}
	if (column == 1)
	{
		retValue = false;
	}
	return retValue;
	}

    /**
     * Overridden to message super and forward the method to the tree.
     * Since the tree is not actually in the component hieachy it will
     * never receive this unless we forward it in this manner.
     */
    public void updateUI() {
		super.updateUI();
		if(tree != null) 
		{
		    tree.updateUI();
		}
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor(tree));
		// Use the tree's default foreground and background colors in the
		// table. 
	        LookAndFeel.installColorsAndFont(this, "Tree.background",
	                                         "Tree.foreground", "Tree.font");
    }

    /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
     * paint the editor. The UI currently uses different techniques to 
     * paint the renderers and editors and overriding setBounds() below 
     * is not the right thing to do for an editor. Returning -1 for the 
     * editing row in this case, ensures the editor is never painted. 
     */
    public int getEditingRow() {
        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 :
	        editingRow;  
    }

	/**
	 * Returns the actual row that is editing as <code>getEditingRow</code>
	 * will always return -1.
	 */
	private int realEditingRow() {
	return editingRow;
	}

	/**
	 * This is overridden to invoke super's implementation, and then,
	 * if the receiver is editing a Tree column, the editor's bounds is
	 * reset. The reason we have to do this is because JTable doesn't
	 * think the table is being edited, as <code>getEditingRow</code> returns
	 * -1, and therefore doesn't automatically resize the editor for us.
	 */
	public void sizeColumnsToFit(int resizingColumn) 
	{ 
		super.sizeColumnsToFit(resizingColumn);
		if (getEditingColumn() != -1 && getColumnClass(editingColumn) ==
			TreeTableModel.class) {
			Rectangle cellRect = getCellRect(realEditingRow(),
							 getEditingColumn(), false);
				Component component = getEditorComponent();
			component.setBounds(cellRect);
				component.validate();
		}
	}

    /**
     * Overridden to pass the new rowHeight to the tree.
     */
    public void setRowHeight(int rowHeight) { 
        super.setRowHeight(rowHeight); 
	if (tree != null && tree.getRowHeight() != rowHeight) {
            tree.setRowHeight(getRowHeight()); 
	}
    }

    /**
     * Returns the tree that is being shared between the model.
     */
    public TreeTableCellRenderer getTree() {
		return tree;
    }

//    /**
//     * A TreeCellRenderer that displays a JTree.
//     */
//    public class TreeTableCellRenderer extends JTree implements
//	         TableCellRenderer {
//	/** Last table/tree row asked to renderer. */
//	protected int visibleRow;
//
//	public TreeTableCellRenderer(TreeModel model) {
//	    super(model);
//		//this.putClientProperty("JTree.lineStyle", "Angled");
//		//this.putClientProperty("JTree.lineStyle", "Horizontal");
//		setBorder(BorderFactory.createLineBorder(Color.BLACK));
//		setShowsRootHandles(true);
//		
//	
//		/*this.addTreeSelectionListener( new TreeSelectionListener() {
//
//			public void valueChanged(TreeSelectionEvent e)
//			{
//				// TODO Auto-generated method stub
//				ETSystem.out.println("Value changed for tree selection");
//			}
//		}
//		);*/
//		
////		this.addTreeExpansionListener( new TreeExpansionListener() {
////
////			public void treeExpanded(TreeExpansionEvent event)
////			{
////				// TODO Auto-generated method stub
////				ETSystem.out.println("Tree expanded");
////				
////			}
////
////			public void treeCollapsed(TreeExpansionEvent event)
////			{
////				// TODO Auto-generated method stub
////				ETSystem.out.println("Tree collapsed");
////				
////			}
////		}
////		);
//	}
//
//	/**
//	 * updateUI is overridden to set the colors of the Tree's renderer
//	 * to match that of the table.
//	 */
//	public void updateUI() {
//	    super.updateUI();
//	    // Make the tree's cell renderer use the table's cell selection
//	    // colors. 
//	    TreeCellRenderer tcr = getCellRenderer();
//	    if (tcr instanceof DefaultTreeCellRenderer) {
//		DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr); 
//		// For 1.1 uncomment this, 1.2 has a bug that will cause an
//		// exception to be thrown if the border selection color is
//		// null.
//		//dtcr.setBorderSelectionColor(null);
//		dtcr.setTextSelectionColor(UIManager.getColor
//					   ("Table.selectionForeground"));
//		dtcr.setBackgroundSelectionColor(UIManager.getColor
//						("Table.selectionBackground"));
//	    }
//	}
//
//	/**
//	 * Sets the row height of the tree, and forwards the row height to
//	 * the table.
//	 */
//	public void setRowHeight(int rowHeight) { 
//	    if (rowHeight > 0) {
//		super.setRowHeight(rowHeight); 
//		if (JTreeTable.this != null &&
//		    JTreeTable.this.getRowHeight() != rowHeight) {
//		    JTreeTable.this.setRowHeight(getRowHeight()); 
//		}
//	    }
//	}
//
//	/**
//	 * This is overridden to set the height to match that of the JTable.
//	 */
//	public void setBounds(int x, int y, int w, int h) {
//	    super.setBounds(x, 0, w, JTreeTable.this.getHeight());
//	}
//
//	/**
//	 * Sublcassed to translate the graphics such that the last visible
//	 * row will be drawn at 0,0.
//	 */
//	public void paint(Graphics g) {
//	    g.translate(0, -visibleRow * getRowHeight());
//	    
//	    super.paint(g);
//	}
//
//	/**
//	 * TreeCellRenderer method. Overridden to update the visible row.
//	 */
//	public Component getTableCellRendererComponent(JTable table,
//						       Object value,
//						       boolean isSelected,
//						       boolean hasFocus,
//						       int row, int column) {
//		if (value != null && column == 1)
//		{
//			ETSystem.out.println(value.getClass() + " " + value);
//		}
//		
//		
//	    if(isSelected)
//	    {
//			setBackground(table.getSelectionBackground());
//			
//	    }
//	    else
//			setBackground(table.getBackground());
//
//	    visibleRow = row;
//	    return this;
//	}
//    }


//    /**
//     * TreeTableCellEditor implementation. Component returned is the
//     * JTree.
//     */
//    public class TreeTableCellEditor extends AbstractCellEditor implements
//	         TableCellEditor {
//	public Component getTableCellEditorComponent(JTable table,
//						     Object value,
//						     boolean isSelected,
//						     int r, int c)
//	{
//		Component retObj = null;//tree;
//		TreePath path = getTree().getSelectionPath();
//		if (path != null)
//		{
//			Object obj = path.getLastPathComponent();
//			if (obj instanceof DefaultMutableTreeNode)
//			{
//				DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
//				IPropertyElement ele = (IPropertyElement)node.getUserObject();
//				IPropertyDefinition def = ele.getPropertyDefinition();
//				long mult = def.getMultiplicity();
//				String values = def.getValidValues();
//				if (values != null)
//				{
//					retObj = new JComboBox();
//				}
//				ETSystem.out.println("in getTableCellRen... = " + mult + values);
//			}
//		}
//	    return retObj;
//	}
//
//	/**
//	 * Overridden to return false, and if the event is a mouse event
//	 * it is forwarded to the tree.<p>
//	 * The behavior for this is debatable, and should really be offered
//	 * as a property. By returning false, all keyboard actions are
//	 * implemented in terms of the table. By returning true, the
//	 * tree would get a chance to do something with the keyboard
//	 * events. For the most part this is ok. But for certain keys,
//	 * such as left/right, the tree will expand/collapse where as
//	 * the table focus should really move to a different column. Page
//	 * up/down should also be implemented in terms of the table.
//	 * By returning false this also has the added benefit that clicking
//	 * outside of the bounds of the tree node, but still in the tree
//	 * column will select the row, whereas if this returned true
//	 * that wouldn't be the case.
//	 * <p>By returning false we are also enforcing the policy that
//	 * the tree will never be editable (at least by a key sequence).
//	 */
//	public boolean isCellEditable(EventObject e) {
//	    if (e instanceof MouseEvent) {
//		for (int counter = getColumnCount() - 1; counter >= 0;
//		     counter--) {
//		    if (getColumnClass(counter) == TreeTableModel.class) {
//			MouseEvent me = (MouseEvent)e;
//			MouseEvent newME = new MouseEvent(tree, me.getID(),
//				   me.getWhen(), me.getModifiers(),
//				   me.getX() - getCellRect(0, counter, true).x,
//				   me.getY(), me.getClickCount(),
//                                   me.isPopupTrigger());
//			tree.dispatchEvent(newME);
//			break;
//		    }
//		}
//	    }
//	    return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see javax.swing.CellEditor#getCellEditorValue()
//	 */
//	public Object getCellEditorValue()
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//    }


//    /**
//     * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
//     * to listen for changes in the ListSelectionModel it maintains. Once
//     * a change in the ListSelectionModel happens, the paths are updated
//     * in the DefaultTreeSelectionModel.
//     */
//    class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel { 
//	/** Set to true when we are updating the ListSelectionModel. */
//	protected boolean         updatingListSelectionModel;
//
//	public ListToTreeSelectionModelWrapper() {
//	    super();
//	    getListSelectionModel().addListSelectionListener
//	                            (createListSelectionListener());
//	}
//
//	/**
//	 * Returns the list selection model. ListToTreeSelectionModelWrapper
//	 * listens for changes to this model and updates the selected paths
//	 * accordingly.
//	 */
//	ListSelectionModel getListSelectionModel() {
//	    return listSelectionModel; 
//	}
//
//	/**
//	 * This is overridden to set <code>updatingListSelectionModel</code>
//	 * and message super. This is the only place DefaultTreeSelectionModel
//	 * alters the ListSelectionModel.
//	 */
//	public void resetRowSelection() {
//	    if(!updatingListSelectionModel) {
//		updatingListSelectionModel = true;
//		try {
//		    super.resetRowSelection();
//		}
//		finally {
//		    updatingListSelectionModel = false;
//		}
//	    }
//	    // Notice how we don't message super if
//	    // updatingListSelectionModel is true. If
//	    // updatingListSelectionModel is true, it implies the
//	    // ListSelectionModel has already been updated and the
//	    // paths are the only thing that needs to be updated.
//	}
//
//	/**
//	 * Creates and returns an instance of ListSelectionHandler.
//	 */
//	protected ListSelectionListener createListSelectionListener() {
//	    return new ListSelectionHandler();
//	}
//
//	/**
//	 * If <code>updatingListSelectionModel</code> is false, this will
//	 * reset the selected paths from the selected rows in the list
//	 * selection model.
//	 */
//	protected void updateSelectedPathsFromSelectedRows() {
//	    if(!updatingListSelectionModel) {
//		updatingListSelectionModel = true;
//		try {
//		    // This is way expensive, ListSelectionModel needs an
//		    // enumerator for iterating.
//		    int        min = listSelectionModel.getMinSelectionIndex();
//		    int        max = listSelectionModel.getMaxSelectionIndex();
//
//		    clearSelection();
//		    if(min != -1 && max != -1) {
//			for(int counter = min; counter <= max; counter++) {
//			    if(listSelectionModel.isSelectedIndex(counter)) {
//				TreePath     selPath = tree.getPathForRow
//				                            (counter);
//
//				if(selPath != null) {
//				    addSelectionPath(selPath);
//				}
//			    }
//			}
//		    }
//		}
//		finally {
//		    updatingListSelectionModel = false;
//		}
//	    }
//	}
//
//	/**
//	 * Class responsible for calling updateSelectedPathsFromSelectedRows
//	 * when the selection of the list changse.
//	 */
//	class ListSelectionHandler implements ListSelectionListener {
//	    public void valueChanged(ListSelectionEvent e) {
//		updateSelectedPathsFromSelectedRows();
//	    }
//	}
//    }

//	class PopupListener extends MouseAdapter {
//	public void mousePressed(MouseEvent e) {
//		maybeShowPopup(e);
//	}
//
//	public void mouseReleased(MouseEvent e) {
//		maybeShowPopup(e);
//	}
//
//	private void maybeShowPopup(MouseEvent e) {
//		//getSelectionModel()
//		
//		TreePath path = getTree().getPathForLocation(e.getX(), e.getY());
//		if (path != null)
//		{
//			Object obj = path.getLastPathComponent();
//			ETSystem.out.println(obj.getClass());
//			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
//			IPropertyElement ele = (IPropertyElement)node.getUserObject();
//			ETSystem.out.println("Mouse Clicked on " + ele.getName());
//		}
//		if (e.isPopupTrigger()) {
//			popup.show(e.getComponent(),
//					   e.getX(), e.getY());
//		}
//	}
//	}

//	public class RightAlignRenderer extends Component implements TableCellRenderer {
//		/** Last table/tree row asked to renderer. */
//		protected int visibleRow;
//		
//		public RightAlignRenderer() {
//			super();
//			setAlignmentX(JLabel.RIGHT_ALIGNMENT);
//		}
//		
//		/**
//		 * TreeCellRenderer method. Overridden to update the visible row.
//		 */
//		public Component getTableCellRendererComponent(JTable table,
//								   Object value,
//								   boolean isSelected,
//								   boolean hasFocus,
//								   int row, int column) {
//			if(isSelected)
//				setBackground(table.getSelectionBackground());
//			else
//				setBackground(table.getBackground());
//		
//			//visibleRow = row;
//			return this;
//		}
//	}
//
//	private class IndicatorRenderer extends DefaultTableCellRenderer {
//
//		public IndicatorRenderer() {
//			super();
//			//setHorizontalAlignment(JLabel.RIGHT);
//		
//			this.putClientProperty("JTree.lineStyle", "Horizontal");
//		}
//
//
//		/**
//	 	* Returns this.
//	 	*/
//		public Component getTableCellRendererComponent(JTable table,
//				Object value, boolean isSelected, boolean hasFocus,
//				int row, int column) {
//			super.getTableCellRendererComponent(table, value, isSelected,
//						hasFocus, row, column);
//						
//			TreePath path = getTree().getSelectionPath();
//			if (path != null)
//			{
//				Object obj = path.getLastPathComponent();
//				if (obj instanceof DefaultMutableTreeNode)
//				{
//					DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
//					IPropertyElement ele = (IPropertyElement)node.getUserObject();
//					IPropertyDefinition def = ele.getPropertyDefinition();
//					long mult = def.getMultiplicity();
//					String values = def.getValidValues();
//					if (values != null)
//					{
//						//getColumnModel().
//					}
//					ETSystem.out.println("in getTableCellRen... = " + mult + values);
//				}
//			}
//			if(isSelected)
//			{
//				setBackground(table.getSelectionBackground());
//			}
//			else
//				setBackground(table.getBackground());
//			
//			return this;
//		}
//
//	}

	public void expandNode(JDefaultMutableTreeNode node, boolean expand)
	{
		if (node != null)
		{
			int row = node.getRow();
			tree.expandNode(row, expand);
		}
	}


	public class TreeTableCellRenderer extends JTree implements TableCellRenderer {

		/**
		 * 
		 */
		public TreeTableCellRenderer()
		{
			super();
		}

		/** Last table/tree row asked to renderer. */
		protected int visibleRow;

		/** Border to draw around the tree, if this is non-null, it will
		 * be painted. */
		protected Border highlightBorder;

		public TreeTableCellRenderer(TreeModel model) {
			super(model);
			//setBorder(BorderFactory.createLineBorder(Color.BLACK));
			this.putClientProperty("JTree.lineStyle", "Angled");
			//setShowsRootHandles(true);
			setExpandsSelectedPaths(true);
			setScrollsOnExpand(true);
	
		}

		public void addMouseListener(MouseListener l)
		{
			super.addMouseListener(l);
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
			if (JTreeTable.this != null &&
				JTreeTable.this.getRowHeight() != rowHeight) {
				JTreeTable.this.setRowHeight(getRowHeight()); 
			}
			}
		}

		/**
		 * This is overridden to set the height to match that of the JTable.
		 */
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, JTreeTable.this.getHeight());
		}

		/**
		 * Sublcassed to translate the graphics such that the last visible
		 * row will be drawn at 0,0.
		 */
		public void paint(Graphics g) {
			g.translate(0, -visibleRow * getRowHeight());
	    
			super.paint(g);
			// Draw the Table border if we have focus.
			if (highlightBorder != null) {
			highlightBorder.paintBorder(this, g, 0, visibleRow *
							getRowHeight(), getWidth(),
							getRowHeight());
			}
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
			//setShowsRootHandles(true);
			//setSelectionRow(row);
			
			Color background;
			Color foreground;

			if(isSelected) 
			{
				background = table.getSelectionBackground();
				foreground = table.getSelectionForeground();
			}
			else 
			{
				background = table.getBackground();
				foreground = table.getForeground();
			}
			highlightBorder = null;
			if (realEditingRow() == row && getEditingColumn() == column) 
			{
				background = UIManager.getColor("Table.focusCellBackground");
				foreground = UIManager.getColor("Table.focusCellForeground");
			}
			else if (hasFocus) 
			{
				highlightBorder = UIManager.getBorder
							  ("Table.focusCellHighlightBorder");
				if (isCellEditable(row, column)) 
				{
					background = UIManager.getColor
						 ("Table.focusCellBackground");
					foreground = UIManager.getColor
						 ("Table.focusCellForeground");
				}
			}
			
			TreeCellRenderer tcr = getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer) 
			{
				DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr);
				
				//we do not want to show icons for the nodes in property editor.
				dtcr.setLeafIcon(null);
				dtcr.setOpenIcon(null);
				dtcr.setClosedIcon(null);
				 
				if (isSelected) 
				{
					dtcr.setTextSelectionColor(foreground);
					dtcr.setBackgroundSelectionColor(background);
				}
				else 
				{
					dtcr.setTextNonSelectionColor(foreground);
					dtcr.setBackgroundNonSelectionColor(background);
				}
				
				if (column == 1)
				{
					TreePath path = getPathForRow(row);
					Object tempNode = path.getLastPathComponent();
					if (tempNode != null && tempNode instanceof JDefaultMutableTreeNode)
					{
						JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)tempNode;
						Object obj = node.getUserObject();
						if (obj != null && obj instanceof IPropertyElement)
						{
							IPropertyElement ele = (IPropertyElement)obj;
							if (ele != null)
							{
								IPropertyDefinition pDef = ele.getPropertyDefinition();
								long mult = pDef.getMultiplicity();
								if (mult > 1)
								{
									//ETSystem.out.println("Setting bold for " + pDef.getName());
									java.awt.Font f = dtcr.getFont();
									setFont(f.deriveFont(java.awt.Font.BOLD));
								
								}
								else
								{
									//ETSystem.out.println("Setting plain for " + pDef.getName());
									java.awt.Font f = dtcr.getFont();
									setFont(f.deriveFont(java.awt.Font.PLAIN));
								}
							}
						}
						
						boolean isRootNode = node.isRoot();
						if (isRootNode)
						{
							Color color = new Color(192, 192, 192);
							setBackground(color);
							dtcr.setBackgroundNonSelectionColor(color);
							dtcr.setBackgroundSelectionColor(color);
						}
						else
						{
							setBackground(Color.WHITE);
						}
					}
				}
			}

			visibleRow = row;
			return this;
		}
		
		public void expandNode(int row, boolean val)
		{
			TreePath path = getPathForRow(row);
			setExpandedState(path, val);
			
		}

	}

	public class TreeTableDefaultCellEditor extends DefaultCellEditor {
		public TreeTableDefaultCellEditor() {
			super(new TreeTableTextField());
		}
	
	/**
	 * Overridden to determine an offset that tree would place the
	 * editor at. The offset is determined from the
	 * <code>getRowBounds</code> JTree method, and additionally
	 * from the icon DefaultTreeCellRenderer will use.
	 * <p>The offset is then set on the TreeTableTextField component
	 * created in the constructor, and returned.
	 */
	public Component getTableCellEditorComponent(JTable table,
							 Object value,
							 boolean isSelected,
							 int r, int c) {
		Component component = super.getTableCellEditorComponent
		(table, value, isSelected, r, c);
		JTree t = getTree();
		boolean rv = t.isRootVisible();
		int offsetRow = rv ? r : r - 1;
		Rectangle bounds = t.getRowBounds(offsetRow);
		int offset = bounds.x;
		TreeCellRenderer tcr = t.getCellRenderer();
		if (tcr instanceof DefaultTreeCellRenderer) {
		Object node = t.getPathForRow(offsetRow).
						getLastPathComponent();
		Icon icon = null;
//		if (t.getModel().isLeaf(node))
//			icon = ((DefaultTreeCellRenderer)tcr).getLeafIcon();
//		else if (tree.isExpanded(offsetRow))
//			icon = ((DefaultTreeCellRenderer)tcr).getOpenIcon();
//		else
//			icon = ((DefaultTreeCellRenderer)tcr).getClosedIcon();
		if (icon != null) {
			offset += ((DefaultTreeCellRenderer)tcr).getIconTextGap() +
				  icon.getIconWidth();
		}
		}
		((TreeTableTextField)getComponent()).offset = offset;
		return component;
	}
	
	/**
	 * This is overridden to forward the event to the tree. This will
	 * return true if the click count >= 3, or the event is null.
	 */
	public boolean isCellEditable(EventObject e) {
		if (e instanceof MouseEvent) {
				MouseEvent me = (MouseEvent)e;
				// If the modifiers are not 0 (or the left mouse button),
				// tree may try and toggle the selection, and table
				// will then try and toggle, resulting in the
				// selection remaining the same. To avoid this, we
				// only dispatch when the modifiers are 0 (or the left mouse
				// button).
				if (me.getModifiers() == 0 ||
							me.getModifiers() == InputEvent.BUTTON1_MASK) {
					for (int counter = getColumnCount() - 1; counter >= 0;
					 counter--) {
					if (getColumnClass(counter) == TreeTableModel.class) {
						MouseEvent newME = new MouseEvent
							  (JTreeTable.this.tree, me.getID(),
						   me.getWhen(), me.getModifiers(),
						   me.getX() - getCellRect(0, counter, true).x,
						   me.getY(), me.getClickCount(),
										   me.isPopupTrigger());
						JTreeTable.this.tree.dispatchEvent(newME);
						break;
					}
					}
				}
				return false;
			}
			return false;
		}
	}

	static class TreeTableTextField extends JTextField {
		public int offset;
		
		public void reshape(int x, int y, int w, int h) {
			int newX = Math.max(x, offset);
			super.reshape(newX, y, w - (newX - x), h);
		}
	}

	/**
	 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	 * to listen for changes in the ListSelectionModel it maintains. Once
	 * a change in the ListSelectionModel happens, the paths are updated
	 * in the DefaultTreeSelectionModel.
	 */
	class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel { 
		/** Set to true when we are updating the ListSelectionModel. */
		protected boolean         updatingListSelectionModel;
		
		public ListToTreeSelectionModelWrapper() {
			super();
			getListSelectionModel().addListSelectionListener(createListSelectionListener());
		}
		
		/**
		 * Returns the list selection model. ListToTreeSelectionModelWrapper
		 * listens for changes to this model and updates the selected paths
		 * accordingly.
		 */
		ListSelectionModel getListSelectionModel() {
			return listSelectionModel; 
		}
		
		/**
		 * This is overridden to set <code>updatingListSelectionModel</code>
		 * and message super. This is the only place DefaultTreeSelectionModel
		 * alters the ListSelectionModel.
		 */
		public void resetRowSelection() {
			if(!updatingListSelectionModel) {
			updatingListSelectionModel = true;
			try {
				super.resetRowSelection();
			}
			finally {
				updatingListSelectionModel = false;
			}
			}
			// Notice how we don't message super if
			// updatingListSelectionModel is true. If
			// updatingListSelectionModel is true, it implies the
			// ListSelectionModel has already been updated and the
			// paths are the only thing that needs to be updated.
		}
		
		/**
		 * Creates and returns an instance of ListSelectionHandler.
		 */
		protected ListSelectionListener createListSelectionListener() {
			return new ListSelectionHandler();
		}
	
		/**
		 * If <code>updatingListSelectionModel</code> is false, this will
		 * reset the selected paths from the selected rows in the list
		 * selection model.
		 */
		protected void updateSelectedPathsFromSelectedRows() 
		{
			if(!updatingListSelectionModel) 
			{
				updatingListSelectionModel = true;
				try {
					// This is way expensive, ListSelectionModel needs an
					// enumerator for iterating.
					int        min = listSelectionModel.getMinSelectionIndex();
					int        max = listSelectionModel.getMaxSelectionIndex();
			
					clearSelection();
					if(min != -1 && max != -1) {
					for(int counter = min; counter <= max; counter++) 
					{
						if(listSelectionModel.isSelectedIndex(counter)) 
						{
							TreePath     selPath = tree.getPathForRow
													(counter);
			
							if(selPath != null) 
							{
								addSelectionPath(selPath);
							}
						}
					}
					}
				}
				finally {
					updatingListSelectionModel = false;
				}
			}
		}
	
		/**
		 * Class responsible for calling updateSelectedPathsFromSelectedRows
		 * when the selection of the list changse.
		 */
		class ListSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) 
			{
				updateSelectedPathsFromSelectedRows();
			}
		}
	}


}

