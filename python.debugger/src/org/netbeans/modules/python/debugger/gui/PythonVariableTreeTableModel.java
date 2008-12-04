/**
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package org.netbeans.modules.python.debugger.gui;

import org.netbeans.modules.python.debugger.utils.AbstractTreeTableModel;
import org.netbeans.modules.python.debugger.utils.TreeTableModel;







public class PythonVariableTreeTableModel
extends AbstractTreeTableModel    
{

    // Types of the columns.
    static protected Class[]  cTypes = { TreeTableModel.class, Object.class };

    private String[] _columnNames = null ;
    private PythonContainer _parent = null ; 
    /** true when table conatins global variables references */
    private boolean _global = false ;


    public PythonVariableTreeTableModel( Object root ,
                                         String columnNames[] ,
                                         boolean global
                                       ) 
    { 
      // initiate root Node 
      super( root ) ;
       _columnNames = columnNames       ; 
       _global = global ; 
    }
    

    public void set_parent( PythonContainer parent )
    { _parent = parent ; }
    
    //
    // Some convenience methods. 
    //

    protected PythonVariableTreeDataNode getDataNode(Object node) 
    {
     return ((PythonVariableTreeDataNode)node); 
    }

    protected Object[] getChildren(Object node) 
    {
      PythonVariableTreeDataNode datanode = ((PythonVariableTreeDataNode)node); 
       return datanode.get_children(); 
    }

    //
    // The TreeModel interface
    //

    public int getChildCount(Object node) 
    { 
      Object[] children = getChildren(node); 
      return (children == null) ? 0 : children.length;
    }

    
    public boolean isCellEditable(Object node, int column) 
    {
      if (  getColumnClass(column) == TreeTableModel.class )
        return true ; 
      if ( node instanceof PythonVariableTreeDataNode ) 
      {
      PythonVariableTreeDataNode cur =(PythonVariableTreeDataNode)node ; 
        if ( cur.isLeaf() )
          return true ; 
      }  
      return false ; 
    }

    
    public Object getChild(Object node, int i) 
    {
      
   return getChildren(node)[i]; 
    }

    // The superclass's implementation would work, but this is more efficient. 
    public boolean isLeaf(Object node) 
    { 
    PythonVariableTreeDataNode cur = (PythonVariableTreeDataNode) node ;  
      return cur.isLeaf() ;
    }

    //
    //  The TreeTableNode interface. 
    //

    public int getColumnCount() 
    {    
      return _columnNames.length; 
    }

    public String getColumnName(int column) 
    { return _columnNames[column]; }

    public Class getColumnClass(int column) 
    {return cTypes[column]; }

    /**
     * Can be invoked when a node has changed, will create the
     * appropriate event.
     */
    protected void nodeChanged( PythonVariableTreeDataNode candidate ) 
    {
      PythonVariableTreeDataNode parent = candidate.get_parent();
      if (parent != null) 
      {
        PythonVariableTreeDataNode[]   path = parent.getPath();
        int[]     index = { getIndexOfChild(parent, candidate) };
        Object[]     children = { candidate };

        fireTreeNodesChanged( PythonVariableTreeTableModel.this, 
                              path ,  
                              index,
                              children);
      }
    }
    
    protected void nodeStructureChange( PythonVariableTreeDataNode candidate )
   {
     fireTreeStructureChanged( this, candidate.getPath(), null, null);
   }
    
   public Object getValueAt(Object node, int column) 
   {
   PythonVariableTreeDataNode dataNode = getDataNode(node) ; 
 
     switch(column) 
     {
       case 0:
         return dataNode ;
       case 1:
         // populate Variable content value
         return dataNode.get_varContent() ;
     }
     return null; 
   }
   
   
   public void setValueAt( Object newValue , Object node , int column )
   {
     if ( column == 0 ) // tree
       super.setValueAt(newValue , node , column ) ;
     else 
     {
     PythonVariableTreeDataNode dataNode = getDataNode(node) ; 
       // Complex names may need to get built from tree path  
       PythonVariableTreeDataNode path[] = dataNode.getPath() ; 
       String varName = PythonVariableTreeDataNode.buildPythonName(path) ;
       dataNode.set_varContent((String)newValue) ; 
       // populate newDataValue to python side
       if ( _parent != null )
         _parent.dbgVariableChanged(varName , (String)newValue ,_global ) ;  
       
     }
   }  
   
}
