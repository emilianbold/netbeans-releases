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
 * Created on May 22, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.preferencedialog;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
public class PreferenceDialogTreeModel implements ISwingPreferenceDialogModel
{
	private PreferenceDialogUI m_PreferenceControl = null;	
	private Vector<DefaultMutableTreeNode> m_Children = null;
   Element m_Project        = null;
	XPath   m_AttOpQuery     = null;
	XPath   m_NamespaceQuery = null;
	
	public PreferenceDialogTreeModel(Document doc)
	{
		//super(doc);
		setProject(doc);
		initializeXPaths(doc);
	}
	
	public PreferenceDialogTreeModel(PreferenceDialogUI control)
	{
		//super();
		m_PreferenceControl = control;
		initialize();
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

   private void initialize()
   {
   		m_Children = m_PreferenceControl.loadTree();
   }

	public void reinitialize()
	{
		m_Children = null;
		m_Children = m_PreferenceControl.loadTree();
	}

   /* (non-Javadoc)
    * @see javax.swing.tree.TreeModel#getRoot()
    */
   public Object getRoot()
   {
   		Object retObj = null;
   		if (m_Children == null)
   		{
   			initialize();
   		}
   		
   		if (m_Children != null && m_Children.size() > 0)
   		{
   			retObj = m_Children.elementAt(0);
   		}
      return retObj;
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
		
		if(parent instanceof DefaultMutableTreeNode)
		{   	
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)parent;	

			if (m_Children == null)
			{
				initialize();
			}
			
			if (m_Children != null)
			{
				//try to find the passed in node in m_Children
				Enumeration childNodes = parentNode.children();
				if (childNodes != null)
				{
					retVal = new Vector();
					while (childNodes.hasMoreElements())
					{
						retVal.add(childNodes.nextElement());
					}
				}
				//retVal = parentNode.children();
			}
		}
		
		return retVal;
	}
}



