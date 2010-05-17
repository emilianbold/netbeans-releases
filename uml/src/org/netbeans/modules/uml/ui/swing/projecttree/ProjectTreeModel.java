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
 * Created on May 22, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.projecttree;

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * @author treys
 *
 */
public class ProjectTreeModel implements TreeModel
{
	
   Element m_Project        = null;
	XPath   m_AttOpQuery     = null;
	XPath   m_NamespaceQuery = null;
	
	public ProjectTreeModel(Document doc)
	{
		setProject(doc);
		initializeXPaths(doc);
	}
	
   /**
    * 
    */
   private void initializeXPaths(Document doc)
   {
		String cQuery = "UML:Element.ownedElement/*[name(.) = 'UML:Attribute' or name(.) = 'UML:Operation']";
		m_AttOpQuery = doc.createXPath(cQuery);
		
		String nQuery = "UML:Element.ownedElement/*[name(.) = 'UML:Class' or name(.) = 'UML:Interface' or name(.) = 'UML:Package']";
		//String nQuery = "UML:Element.ownedElement/UML:Package";
		m_NamespaceQuery = doc.createXPath(nQuery);
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getRoot()
    */
   public Object getRoot()
   {
      return m_Project;
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
    */
   public Object getChild(Object parent, int index)
   {
   	Object retVal = null;
   	
		List children = getChildren(parent);
		if((children != null) && (index < children.size()))
		{
			retVal = children.get(index);
		}
   	
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
    */
   public int getChildCount(Object parent)
   {
      int retVal = 0;
      
		List children = getChildren(parent);
		if(children != null)
   	{
			retVal = children.size();
   	}
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
    */
   public boolean isLeaf(Object node)
   {
      boolean retVal = true;
      
		List children = getChildren(node);
		if((children != null) && (children.size() > 0))
		{
			retVal = false;
		}
      
      return retVal;
   }
   
	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child)
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener l)
	{
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener l)
	{
		
	}
		
   //**************************************************************
   // Data Getter and Setters
   //**************************************************************
   
   /**
    * @return
    */
   public Element getProject()
   {
      return m_Project;
   }

   /**
    * @param document
    */
   public void setProject(Document document)
   {
      //m_Project = document;
		setProject((Element)document.selectSingleNode("//UML:Project"));
   }
   
	/**
	 * @param document
	 */
	public void setProject(Element projectNode)
	{
		m_Project = projectNode;
	}

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
    */
   public void valueForPathChanged(TreePath path, Object newValue)
   {
      // TODO Auto-generated method stub
      
   }
	
	protected List getChildren(Object parent)
	{
		List retVal = null;
		
		if(parent instanceof Node)
		{   	
			Node parentNode = (Node)parent;	

			String name = parentNode.getName();
			if((name.equals("Class") == true) ||
				(name.equals("Interface") == true))
			{				
				retVal = m_AttOpQuery.selectNodes(parentNode);
				//retVal = parentNode.selectNodes("UML:Element.ownedElement/*[name(.) = 'UML:Attribute' or name(.) = 'UML:Operation']");
				   	
			}
			else
			{
				retVal = m_NamespaceQuery.selectNodes(parentNode);
				//retVal = parentNode.selectNodes("UML:Element.ownedElement/*[name(.) = 'UML:Class' or name(.) = 'UML:Interface' or name(.) = 'UML:Package']");   
			}
		}
		
		return retVal;
	}
}



