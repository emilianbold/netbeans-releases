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



package org.netbeans.modules.uml.ui.swing.projecttree;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeDiagramNode;
import org.netbeans.modules.uml.ui.controls.projecttree.TreeElementNode;

/**
 * @author sumitabhk
 *
 */
public class ProjectTreeCellEditor implements TreeCellEditor
{
    JTree tree =  null;
    EditControlImpl m_EditControl = null;
    /**
     *
     */
    public ProjectTreeCellEditor(JTree tree)
    {
        this.tree = tree;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.tree.TreeCellEditor#getTreeCellEditorComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int)
         */
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
    {
        //TreePath path = tree.getPathForRow(row);
        //Object sel = path.getLastPathComponent();
        
        if (value instanceof TreeElementNode)
        {
            TreeElementNode node = (TreeElementNode)value;
            String str = node.getDisplayedName();
            m_EditControl = new EditControlImpl(this);
            m_EditControl.setBorder(null);
            m_EditControl.setOpaque(false);
            
            m_EditControl.setVisible(true);
            m_EditControl.setText(str);
            
            IProjectTreeItem item = node.getDataItem();
            IElement elem = item.getModelElement();
            m_EditControl.setElement(elem);
            
            return m_EditControl;
        }
        else if (value instanceof ProjectTreeDiagramNode)
        {
            //we want to allow editing of diagrams too.
            ProjectTreeDiagramNode node = (ProjectTreeDiagramNode)value;
            String str = node.getDisplayedName();
            m_EditControl = new EditControlImpl(this);
            m_EditControl.setBorder(null);
            m_EditControl.setOpaque(false);
            
            m_EditControl.setVisible(true);
            m_EditControl.setText(str);
            
            IProjectTreeItem item = node.getDataItem();
            IProxyDiagram pDia = item.getDiagram();
            if (pDia != null && pDia.isOpen())
            {
                IDiagram dia = pDia.getDiagram();
                m_EditControl.setElement(dia);
            }
            
            return m_EditControl;
        }
        return new EditControlImpl(this);
    }
    
        /* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
    public Object getCellEditorValue()
    {
        // TODO Auto-generated method stub
        return m_EditControl;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
         */
    public boolean isCellEditable(EventObject anEvent)
    {
        // temp fix for 78872, the entire design center tree view needs to be re-written
        if (anEvent instanceof MouseEvent)
            return false;
        
        Object sel = tree.getLastSelectedPathComponent();
        if (sel instanceof TreeElementNode)
        {
            return ((TreeElementNode)sel).getElement() instanceof INamedElement;
        }
        else if (sel instanceof ProjectTreeDiagramNode)
        {
            return true;
        }
        return false;
        
//        if (anEvent instanceof MouseEvent)
//        {
//            if (anEvent.getSource() instanceof JTree)
//            {
//                JTree tree = (JTree)anEvent.getSource();
//                Object selObject = tree.getLastSelectedPathComponent();
//                
//                if (selObject instanceof TreeElementNode)
//                    
//                {
//                    TreeElementNode node = (TreeElementNode)selObject;
//                    
//                    MouseEvent e = (MouseEvent)anEvent;
//                    if (e.getClickCount() == 3)
//                    {
//                        return true;
//                    }
//                    if (e.getClickCount() == 1)
//                    {
//                    }
//                }
//            }
//            
//        }
//        
//        if (anEvent == null)
//        {
//            //we are programmatically going in edit mode, so return true.
//            return true;
//        }
//        TODO Auto-generated method stub
//                return false;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
         */
    public boolean shouldSelectCell(EventObject anEvent)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.CellEditor#stopCellEditing()
         */
    public boolean stopCellEditing()
    {
        // TODO Auto-generated method stub
        ETSystem.out.println("Calling stopCellEditing");
        if (m_EditControl != null)
        {
            String sText = m_EditControl.getText();
            if( (sText == null) || (sText.equals("") == true) )
            {
                return false;
            }
            m_EditControl.handleSave();
        }
        return true;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.CellEditor#cancelCellEditing()
         */
    public void cancelCellEditing()
    {
        // TODO Auto-generated method stub
        ETSystem.out.println("Calling cancelCellEditing");
        if (m_EditControl != null)
        {
            m_EditControl.handleRollback();
        }
    }
    
        /* (non-Javadoc)
         * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
         */
    public void addCellEditorListener(CellEditorListener l)
    {
        // TODO Auto-generated method stub
        
    }
    
        /* (non-Javadoc)
         * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
         */
    public void removeCellEditorListener(CellEditorListener l)
    {
        // TODO Auto-generated method stub
        
    }
    
}



