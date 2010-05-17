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



package org.netbeans.modules.uml.ui.swing.preferencedialog.testbed;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventDispatcher;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManager;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManagerEventDispatcher;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManagerEventsAdapter;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.swing.preferencedialog.PreferenceDialogUI;

/**
 * @author sumitabhk
 *
 */
public class TestPreferenceDialog
{
	private String m_ImageDir = null;
	private PreferenceAccessor m_PreferenceAccessor = null;
	private IPreferenceManagerEventsSink m_EventsSink = null;
	private IPreferenceManagerEventDispatcher m_PrefDispatcher = null;
	private boolean m_Advanced = false;
	private static ICoreProduct m_Product = null;

	/**
	 * 
	 */
	public TestPreferenceDialog()
	{
		super();
	}

	public static void main(String[] args)
	{
		try
		{
		   // Initialize the COM to Java bridge
		   //BridgeKeeper.initialize();
		}
		catch (Exception E)
		{
		   // Ignore already initialized
		}
		
		//m_Product = CoreProductManager.instance().getCoreProduct();
		//m_Product.initialize();
		CoreProductManager manager = (CoreProductManager)CoreProductManager.instance();
		ICoreProduct product = manager.getCoreProduct();
		IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
		if (conMan != null)
		{
			//m_ImageDir = conMan.getDefaultConfigLocation();
		}
		PreferenceAccessor.instance();
		//ProductRetriever.retrievePreferenceManager();
		PreferenceDialogUI ui = new PreferenceDialogUI();
		ui.doLayout();
		ui.setVisible(true);
		ui.show();
	}
	
	public void initialize()
	{
		IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
		if (conMan != null)
		{
			m_ImageDir = conMan.getDefaultConfigLocation();
		}
		
		if (m_PreferenceAccessor == null)
		{
			m_PreferenceAccessor = PreferenceAccessor.instance();
		}
		
		initializeTree();
		initializeButtons();
		initializeEventsSink();
		
	}

	/**
	 * 
	 */
	private void initializeEventsSink()
	{
		if (m_EventsSink == null)
		{
			m_EventsSink = new PreferenceManagerEventsAdapter();
			//m_eventsSink.setPreferenceControl(this);
		}
		
		if (m_EventsSink != null)
		{
			//DispatchHelper helper = new DispatchHelper();
			IPreferenceManagerEventDispatcher helper = 
					getPreferenceManagerDispatcher();
			helper.registerPreferenceManagerEvents(m_EventsSink);
		}
	}

	/**
	 * Gets the drawing area dispatcher.
	 */
	public IPreferenceManagerEventDispatcher getPreferenceManagerDispatcher()
	{
	   //return (IDrawingAreaEventDispatcher)getJavaDispatcher(EventDispatchNameKeeper.drawingAreaName());
   
	   if(m_PrefDispatcher == null)
	   {
		  m_PrefDispatcher = (IPreferenceManagerEventDispatcher)getJavaDispatcher(EventDispatchNameKeeper.preferenceManager());
	   }
	   // Since we are not using the Java code 100% (Basically we are not using core at
	   // all.  I will have to create the Dispatcher and add it to core if it has not 
	   // already been added.
   
	   if(m_PrefDispatcher == null)
	   {
		  m_PrefDispatcher = new PreferenceManagerEventDispatcher();       
		  addJavaDispatcher(EventDispatchNameKeeper.drawingAreaName(), m_PrefDispatcher);
	   }

	   return m_PrefDispatcher; 

	}

	protected org.netbeans.modules.uml.core.eventframework.IEventDispatcher getJavaDispatcher(String name)
	{
		 org.netbeans.modules.uml.core.eventframework.IEventDispatcher retVal = null;		
		
		 if(name.length() > 0)
		 {
			 try
			 {
				 //org.netbeans.modules.uml.core.eventframework.IEventDispatchController controller = m_Product.getEventDispatchController();				
				 //retVal = controller.retrieveDispatcher(name);
			 }
			 catch(NullPointerException e)
			 {
				 // HAVE TODO: Determine what to do about excpetions.
			 }
		 }
		 else
		 {
			 // HAVE TODO: notify that the name is invalid.
		 }
		
		 return retVal; 
	}

	protected void addJavaDispatcher(String name, org.netbeans.modules.uml.core.eventframework.IEventDispatcher newVal)
	{
		try
		{
			//org.netbeans.modules.uml.core.eventframework.IEventDispatchController controller = m_Product.getEventDispatchController();				
			//controller.addDispatcher(name, newVal);
		}
		catch(NullPointerException e)
		{
			// HAVE TODO: Determine what to do about excpetions.
		}
	}

	/**
	 * 
	 */
	private void initializeButtons()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	private void initializeTree()
	{
	}
	
	/**
	 * Load the preferences into the preference tree by asking the preference manager for
	 * the information.  The preference manager has built its information upon starting the
	 * application from the preferences files.  This control just takes that information and
	 * loads it into the appropriate grids.
	 *
	 * @return HRESULT
	 */
	public Vector<DefaultMutableTreeNode> loadTree()
	{
		Vector<DefaultMutableTreeNode> children = new Vector<DefaultMutableTreeNode>();
		IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
		if (prefMan != null)
		{
			IPropertyElement[] elems = prefMan.getPropertyElements();
			if (elems != null)
			{
				// loop through them and add them to the tree
				int count = elems.length;
				for (int i=0; i<count; i++)
				{
					IPropertyElement pEle = elems[i];
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					if (pDef != null)
					{
						// Now add it to the tree
						String name = pDef.getDisplayName();
						boolean load = true;
						String advancedStr = pDef.getFromAttrMap("advanced");
						if (advancedStr.equals("PSK_TRUE") && !m_Advanced)
						{
							load = false;
						}
						
						if (load)
						{
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(pEle);
							children.add(node);
							loadTreeWithSubElements(node, pDef, pEle);
							//node.setExpanded(true);
							
						}
					}
				}
			}
		}
		return children;
	}

	/**
	 * Take the passed-in information and build child nodes in the preference tree.
	 *
	 * @param pNode[in]		The parent grid node
	 * @param pDef[in]		The property definition associated with the passed in property element
	 * @param pEle[in]		The property element in which to process its child elements
	 *
	 * @return HRESULT
	 */
	private void loadTreeWithSubElements(DefaultMutableTreeNode node, 
										IPropertyDefinition pDef, 
										IPropertyElement pEle)
	{
		// The structure of the preference tree is to only display things in the tree that have of a certain
		// level of child nodes, so this is checking to see if the current element should have a node created
		// in the preference tree or not
		if (hasGrandChildren(pEle))
		{
			// loop through its sub "child" elements
			Vector<IPropertyElement> subElems = pEle.getSubElements();
			if (subElems != null)
			{
				int count = subElems.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement subEle = subElems.elementAt(i);

					// if this element has child elements, then we want to add it to the preference tree
					if (hasChildren(subEle))
					{
						IPropertyDefinition subDef = subEle.getPropertyDefinition();
						if (subDef != null)
						{
							// definitions that have no display name or control type are for information only
							// not for display
							String name = subDef.getDisplayName();
							String controlType = subDef.getControlType();
							if ( (name == null || name.length() == 0) &&
								 (controlType == null || controlType.length() == 0) )
							{
								//do nothing
							}
							else
							{
								boolean load = true;
								String advancedStr = subDef.getFromAttrMap("advanced");
								if (advancedStr.equals("PSK_TRUE") && !m_Advanced)
								{
									load = false;
								}
								
								if (load)
								{
									DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(subEle);
									node.add(childNode);
									//childNode.setExpanded(false);
									loadTreeWithSubElements(childNode, subDef, subEle);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Does the passed-in property element have at least one level of children.
	 *
	 * @param pEle[in] The property element in question
	 *
	 * @return BOOL	Whether or not is has at least one level of children
	 */
	private boolean hasChildren(IPropertyElement pEle)
	{
		boolean isParent = false;
		
		// the simple fact that it has children is not good enough
		// in some cases, it could have children, but the children may only be information
		// holders, not actually displayed to the user (Fonts/Colors)
		if (pEle != null)
		{
			Vector<IPropertyElement> subElems = pEle.getSubElements();
			if (subElems != null)
			{
				int count = subElems.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement subEle = subElems.elementAt(i);
					IPropertyDefinition subDef = subEle.getPropertyDefinition();
					if (subDef != null)
					{
						// check if the child is displayed to the user
						String name = subDef.getDisplayName();
						String controlType = subDef.getControlType();
						
						if ( (name == null || name.length() == 0) &&
							 (controlType == null || controlType.length() == 0) )
						{
							//not a valid child
						}
						else
						{
							isParent = true;
							break;
						}
					}
				}
			}
		}
		
		return isParent;
	}

	/**
	 * Does the passed-in property element have at least two levels of children.
	 *
	 * @param pEle[in]	The property element in question
	 *
	 * @return BOOL	Whether or not is has at least two levels of children
	 */
	private boolean hasGrandChildren(IPropertyElement pEle)
	{
		boolean isGrandParent = false;
		if (pEle != null)
		{
			Vector<IPropertyElement> subEles = pEle.getSubElements();
			if (subEles != null)
			{
				int count = subEles.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement ele = subEles.elementAt(i);
					Vector<IPropertyElement> subElems2 = ele.getSubElements();
					if (subElems2 != null)
					{
						int count2 = subElems2.size();
						if (count2 > 0)
						{
							isGrandParent = true;
							break;
						}
					}
				}
			}
		}
		return isGrandParent;
	}
}



