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
