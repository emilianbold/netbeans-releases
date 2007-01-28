/*
 * EjbGroupTreeNodes.java
 *
 * Created on February 24, 2005, 10:23 AM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import java.util.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;


/**
 * The tree hierarchy for a given EJB group
 *
 * @author  cao
 */
public class EjbGroupTreeNodes {
    
    private EjbGroup ejbGroup;
    private DefaultMutableTreeNode root;
    private MethodNode firstToBeSelectedNode;
    
    public EjbGroupTreeNodes( EjbGroup ejbGrp ) {
        this.ejbGroup = ejbGrp;
        buildTree();
    }
    
    public TreeNode getRoot()
    {
        return this.root;
    }
    
    public MethodNode geFirstNodeToBeSelected()
    {
        return firstToBeSelectedNode;
    }
    
    
    // Build the tree for the given ejb group
    private void buildTree()
    {
        // The root of the tree is the group name
        root = new DefaultMutableTreeNode( ejbGroup.getName() );
        
        // Add all the session ejbs as the children of the root
        boolean firstMethod = true;
        boolean found1stConfigurableMethod = false;
        for( Iterator iter = ejbGroup.getSessionBeans().iterator(); iter.hasNext(); )
        {
            EjbInfo ejb = (EjbInfo)iter.next();
            DefaultMutableTreeNode ejbNode = new DefaultMutableTreeNode( ejb.getJNDIName() );
            root.add( ejbNode );
            
            // Add the method nodes as the children of this session ejb node
            for( Iterator mIter = ejb.getMethods().iterator(); mIter.hasNext(); )
            {
                MethodInfo method = (MethodInfo)mIter.next();
                
                // Do not show create() methods
                if( !method.isBusinessMethod() )
                    continue;
                
                MethodNode methodNode = new MethodNode( method );
                ejbNode.add( methodNode );
                
                // By default, the first method node in the tree is to be selected
                if( firstMethod ) 
                {
                    firstToBeSelectedNode = methodNode;
                    firstMethod = false;
                    
                    // Found the first configurable method
                    if( method.isMethodConfigurable() )
                        found1stConfigurableMethod = true;
                }
                else
                {
                    if( !found1stConfigurableMethod && method.isMethodConfigurable() )
                    {
                        firstToBeSelectedNode = methodNode;
                        found1stConfigurableMethod = true;
                    }
                }
            }
        }
        
        
    }
    
    public static class MethodNode extends DefaultMutableTreeNode
    {
        private MethodInfo methodInfo;
        
        public MethodNode( MethodInfo method )
        {
            super( method.getName() );
            this.methodInfo = method;
        }
        
        public MethodInfo getMethod()
        {
            return this.methodInfo;
        }
    }
    
}
