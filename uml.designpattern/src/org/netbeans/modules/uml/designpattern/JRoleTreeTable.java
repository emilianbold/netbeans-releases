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
 * Created on Jun 18, 2003
 *
 */
package org.netbeans.modules.uml.designpattern;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.swing.treetable.JDefaultMutableTreeNode;
import org.netbeans.modules.uml.ui.swing.treetable.JTreeTable;
import org.netbeans.modules.uml.ui.swing.treetable.TreeTableModel;

/**
 * @author sumitabhk
 *
 */
public class JRoleTreeTable extends JTreeTable
{
	private JPopupMenu m_popup = null;
	private WizardRoles m_Class = null;

	private int m_CurRow = 0;
	private WizardRoleObject m_CurRole = null;

	/**
	 *
	 */
	public JRoleTreeTable(TreeTableModel treeTableModel, WizardRoles clazz)
	{
		super(treeTableModel);
		m_Class = clazz;

		m_popup = new JPopupMenu();

		GridBagLayout gbl = new GridBagLayout();
		double[] vals = {0.0, 0.5, 0.5};
		gbl.columnWeights = vals;
		setLayout(gbl);
		gbl.invalidateLayout(this);
		doLayout();

		TableColumnModel colMod = getColumnModel();
		RoleTreeTableCellEditor cellEditor = new RoleTreeTableCellEditor(tree, clazz);
		colMod.getColumn(0).setCellEditor(cellEditor);
		colMod.getColumn(1).setCellEditor(cellEditor);
		colMod.getColumn(2).setCellEditor(cellEditor);

		RoleCellRenderer valueRenderer = new RoleCellRenderer();
		colMod.getColumn(0).setCellRenderer(valueRenderer);
		colMod.getColumn(1).setCellRenderer(tree);
		colMod.getColumn(2).setCellRenderer(valueRenderer);

		MouseListener popupListener = new RoleTreeTablePopupListener();

		this.addMouseListener(popupListener);
		getTree().addMouseListener(new TreeMouseHandler());

		// add key event listener
		java.awt.event.KeyListener keyListener = new RoleTreeKeyboardHandler();
		this.addKeyListener(keyListener);

		ToolTipManager.sharedInstance().registerComponent(this);
		setShowVerticalLines(true);
		setShowHorizontalLines(true);

		if (treeTableModel.getColumnName(0).equals(""))
		{
			getColumnModel().getColumn(0).setMinWidth(20);
			getColumnModel().getColumn(0).setMaxWidth(20);
		}

	}

        public void handlePopupDisplay(Component c) {
            
            
            
            int row = getTree().getSelectionRows()[0];
            Rectangle r = getTree().getRowBounds(row) ;
            
            int y = r.y ;
            int x = r.x;
            
            Point p = new Point (x,y) ; //c.getLocationOnScreen() ;
            handlePopupDisplay (c, row, p) ;
        }
        
        public void handlePopupDisplay(MouseEvent me) {
            Point p = me.getPoint() ;
            
            int row = rowAtPoint(p) ;
            
            handlePopupDisplay (me.getComponent(), row, p) ;
        }
        
	public void handlePopupDisplay(Component component, int row, Point p) {
            m_popup.removeAll();
            //pass this on to the PropertyEditor, passing in the location of
            // mouse click.
//            int row = rowAtPoint(p);
            //TreePath path = getTree().getPathForLocation(e.getX(), e.getY());
            TreePath path = getTree().getPathForRow(row);
            
            if (path != null) {
                Object obj = path.getLastPathComponent();
                JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
                WizardRoleObject wro = (WizardRoleObject)node.getUserObject();
                if (wro != null) {
                    //set the current row and selected element to get popup menu to work.
                    m_CurRow = row;
                    m_CurRole = wro;
                    ETList<String> strs = m_Class.showMenu(row);
                    m_Class.setRightClickRow(row);
                    if (strs != null) {
                        int count = strs.size();
                        for (int i=0; i<count; i++) {
                            String str = strs.get(i);
                            JMenuItem menuItem = new JMenuItem(str);
                            menuItem.addActionListener(this);
                            m_popup.add(menuItem);
                        }
                    }
                }
            }
            
            if (m_popup != null) {
                m_popup.show(component,p.x,p.y);
            }
            
        }

        
	public WizardRoles getClazz()
	{
		return m_Class;
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		String srcText = source.getText();
		String createStr = DefaultDesignPatternResource.getString("IDS_CREATE");
		String delStr = DefaultDesignPatternResource.getString("IDS_DELETE");
		String renStr = DefaultDesignPatternResource.getString("IDS_RENAME");
		if (srcText.equals(createStr))
		{
			m_Class.onPopupCreate(m_CurRow);
		}
		else if (srcText.equals(delStr))
		{
			m_Class.onPopupDelete(m_CurRow);
		}
		else if (srcText.equals(renStr))
		{
			m_Class.onPopupRename(m_CurRow);
		}
	}

        // key event listener class
        public class RoleTreeKeyboardHandler extends java.awt.event.KeyAdapter {
            
            public void keyPressed(java.awt.event.KeyEvent e) {
                int key = e.getKeyCode();
                int SHFT_F10 = KeyStroke.getKeyStroke("shift F10").getKeyCode() ;
                
                int[] selRows = getSelectedRows();
                if(selRows == null || selRows.length<1) return;
                
                if (key == SHFT_F10) {
                    handlePopupDisplay(e.getComponent()) ;
                }
                
                ETList<Integer> keys;
                switch (key) {
                    // handle INSERT key
                    case java.awt.event.KeyEvent.VK_INSERT:
                        //consider first selected row only
                        int selRow = selRows[0];
                        keys = m_Class.handleKeys(selRow);
                        if(keys.contains(new Integer(key))) {
                            m_Class.onPopupCreate(selRow);
                        }
                        break;
                    // handle DELETE key
                    case java.awt.event.KeyEvent.VK_DELETE:
                        for (int rCtr=0; rCtr<selRows.length;rCtr++) {
                            keys = m_Class.handleKeys(selRows[rCtr]);
                            if(keys.contains(new Integer(key))) {
                                m_Class.onPopupDelete(selRows[rCtr]);
                                for(int jrCtr=rCtr+1;jrCtr<selRows.length;jrCtr++) {
                                    selRows[jrCtr] = selRows[jrCtr]-1;
                                }
                            }
                        }
                        break;
                }
//                updateUI();
            }
        }

	public class TreeMouseHandler extends MouseInputAdapter
	{
	   public void mousePressed(MouseEvent e)
	   {
			int selRow = getTree().getRowForLocation(e.getX(), e.getY());
			TreePath selPath = getTree().getPathForLocation(e.getX(), e.getY());
			if(selRow != -1) {
				if(e.getClickCount() == 2)
				{
				  // TODO m_editor.handleDoubleClick(selRow, selPath);
				  //m_editor.refresh();
				}
				else if (e.getClickCount() == 1)
				{
		  			Object obj = selPath.getLastPathComponent();
		  			if (obj instanceof JDefaultMutableTreeNode)
		  			{
		  				//getTree().getUI().
//						if(selPath != null && !getModel().isLeaf(selPath.getLastPathComponent())){
//							int                     boxWidth;
//							java.awt.Insets                  i = tree.getInsets();
//
//							if(getExpandedIcon() != null)
//							boxWidth = getExpandedIcon().getIconWidth();
//							else
//							boxWidth = 8;
//
//							int                     boxLeftX = (i != null) ? i.left : 0;
//
//							if (leftToRight) {
//								boxLeftX += (((path.getPathCount() + depthOffset - 2) *
//									  totalChildIndent) + getLeftChildIndent()) -
//										  boxWidth / 2;
//							}
//							else {
//								boxLeftX += lastWidth - 1 -
//										((path.getPathCount() - 2 + depthOffset) *
//									 totalChildIndent) - getLeftChildIndent() -
//										boxWidth / 2;
//							}
//							int boxRightX = boxLeftX + boxWidth;
//
//							return mouseX >= boxLeftX && mouseX <= boxRightX;
//						}
//						return false;
					}
				}
			}
	   }

	}


}



