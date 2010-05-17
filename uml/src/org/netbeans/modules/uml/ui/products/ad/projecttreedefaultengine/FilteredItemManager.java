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
 *
 * Created on Jun 12, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.controls.filter.FilterItem;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.filter.IFilterItem;
import org.netbeans.modules.uml.ui.controls.filter.IFilterNode;
import org.netbeans.modules.uml.ui.controls.filter.ProjectTreeFilterDialogEventsAdapter;
import org.netbeans.modules.uml.ui.support.UserSettings;


/**
 * Manages the project tree filter items.  Uses the
 * IProjectTreeFilterDialogEventsSink interface to add items to the filter
 * dialog.
 * 
 * @author Trey Spiva
 * @see IProjectTreeFilterDialogEventsSink
 * @see IFilterDialog
 */
public class FilteredItemManager extends ProjectTreeFilterDialogEventsAdapter
{
   private TreeMap     m_FilterItems     = new TreeMap();
   private TreeMap     m_DiagramItems    = new TreeMap();
   private IFilterItem m_AllModelItems   = new FilterItem(DefaultEngineResource.getString("IDS_MODEL_ELEMENTS"));
   private IFilterItem m_AllDiagramItems = new FilterItem(DefaultEngineResource.getString("IDS_DIAGRAMS"));
   
   /**
    * Initializes the filter items using a hash map.
    * 
    * @param displayedItems The display items.  The key is the element type and
    *                       the value is either "y" or "n".
    */
	public void initialize(HashMap displayedItems)
	{
      boolean addedImportPackages = false;
      boolean addedImportElements = false;
      
      Set keySet = displayedItems.keySet();
      for (Iterator iter = keySet.iterator(); iter.hasNext();)
      {
         String curKey = (String)iter.next();
         if((addedImportPackages == false) && 
            (curKey.equals("ImportedPackages") == true))
         {
            addedImportPackages = true;
            m_FilterItems.put("ImportedPackages", 
                              new FilterItem("ImportedPackages", 
                                             IFilterItem.FILTER_STATE_OFF));
         }
         else if((addedImportPackages == false) && 
                 (curKey.equals("ImportedElements") == true))
         {
            addedImportPackages = true;
            m_FilterItems.put("ImportedElements",
                              new FilterItem("ImportedElements", 
                                             IFilterItem.FILTER_STATE_OFF));
         }
         else if(curKey.length() > 0)
         {
            String value = (String)displayedItems.get(curKey);
            
            int fValue = (value.equalsIgnoreCase("y") == true ? IFilterItem.FILTER_STATE_ON : IFilterItem.FILTER_STATE_OFF);
            FilterItem item = new FilterItem(curKey, fValue);
            m_FilterItems.put(curKey, item);
         }
      }
      
      m_DiagramItems.put("ActivityDiagram", new FilterItem("ActivityDiagram", IFilterItem.FILTER_STATE_ON));
      m_DiagramItems.put("ClassDiagram", new FilterItem("ClassDiagram", IFilterItem.FILTER_STATE_ON));
      m_DiagramItems.put("CollaborationDiagram", new FilterItem("CollaborationDiagram", IFilterItem.FILTER_STATE_ON));
      m_DiagramItems.put("ComponentDiagram", new FilterItem("ComponentDiagram", IFilterItem.FILTER_STATE_ON));
      m_DiagramItems.put("DeploymentDiagram", new FilterItem("DeploymentDiagram", IFilterItem.FILTER_STATE_ON));
      m_DiagramItems.put("SequenceDiagram", new FilterItem("SequenceDiagram", IFilterItem.FILTER_STATE_ON));
      m_DiagramItems.put("StateDiagram", new FilterItem("StateDiagram", IFilterItem.FILTER_STATE_ON));
      m_DiagramItems.put("UseCaseDiagram", new FilterItem("UseCaseDiagram", IFilterItem.FILTER_STATE_ON));
      
      getFromRegistry();
	}
	
   public void turnOffModelElements()
   {
      m_AllModelItems.setState(IFilterItem.FILTER_STATE_OFF);
   }
   
   public void turnOnModelElements()
   {
      m_AllModelItems.setState(IFilterItem.FILTER_STATE_ON);
   }
   
   public void turnOffDiagram()
   {
      m_AllDiagramItems.setState(IFilterItem.FILTER_STATE_OFF);
   }
   
   public void turnOnDiagrams()
   {
      m_AllDiagramItems.setState(IFilterItem.FILTER_STATE_ON);
   }
   
   /**
    * Checks if all model elements are to be filtered out.
    * 
    * @return <code>true</code> if all model elements are to be filtered out.
    *         <code>false</code> if not all model elements are to bed filtered
    *         out.
    */
	public boolean areAllModelElementsHidden()
	{
		return m_AllModelItems.getState() == IFilterItem.FILTER_STATE_OFF ? true : false;
	}
	
   /**
    * Checks if a item is to be displayed.
    * 
    * @param elementType The name of the item in the filter manager.
    * @return <code>true</code> if all model elements are to be filtered out.
    *         <code>false</code> if not all model elements are to bed filtered
    *         out.
    */
	public boolean isDisplayed(String elementType)
	{
		boolean retVal = false;
	
		IFilterItem item = getFilterItem(elementType);
	   if(item != null)
      {
         retVal = (item.getState() == IFilterItem.FILTER_STATE_ON ? true : false);
      }
      
		return retVal;
	}
   
   /**
    * Set the display state of the specified item.
    * 
    * @param elementType The name of the item in the filter manager.
    * @param show <code>true</code> if the item is to be displayed and 
    *             <code>false</code> if the item is not to be displayed.
    */
   public void setIsDisplayed(String elementType, boolean show)
	{
      IFilterItem item = getFilterItem(elementType);
      if(item != null)
      {
         item.setState( (show == true ? IFilterItem.FILTER_STATE_ON : IFilterItem.FILTER_STATE_OFF) );
      }
	}

   /**
    * Check the filter items and hte diagram items to find the specified 
    * filter item.
    * 
    * @param elementType The name of the item to locate.
    * @return The filter item if found or <code>null</code> if the item is
    *         not found.
    */
   protected IFilterItem getFilterItem(String elementType)
   {
      IFilterItem retVal = (IFilterItem)m_FilterItems.get(elementType);
      if(retVal == null)
      {
         retVal = (IFilterItem)m_DiagramItems.get(elementType);
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink#OnProjectTreeFilterDialogInit(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onProjectTreeFilterDialogInit(IFilterDialog dialog, IResultCell cell)
   {
		// Restore the latest stuff from the registry
   	getFromRegistry();
   	
      IFilterNode modelElementsNode = dialog.createRootNode(m_AllModelItems);
      for (Iterator iter = m_FilterItems.values().iterator(); iter.hasNext();)
      {
         IFilterItem curItem = (IFilterItem)iter.next();
         dialog.addFilterItem(modelElementsNode, curItem);
      }
      
      IFilterNode diagramElementsNode = dialog.createRootNode(m_AllDiagramItems);
      for (Iterator iter = m_DiagramItems.values().iterator(); iter.hasNext();)
      {
         IFilterItem curItem = (IFilterItem)iter.next();
         dialog.addFilterItem(diagramElementsNode, curItem);
      }
   }
	/**
	 * Get the current settings from the registry.  In jUML, we are not using registry settings, so instead
	 * we are using the UserSettings files.  These entries will be stored in the .Settings file so that they
	 * are application wide.  The entries that will be written to the file will be those that are to be
	 * excluded (or unchecked).
	 */   
   public void getFromRegistry()
   {
   	UserSettings settings = new UserSettings();
   	if (settings != null)
   	{
			// set the check of the top model elements/diagrams to off
			// they will be set to on if any of its children are checked
			m_AllModelItems.setState(IFilterItem.FILTER_STATE_OFF);
			m_AllDiagramItems.setState(IFilterItem.FILTER_STATE_OFF);
			for (Iterator iter = m_FilterItems.values().iterator(); iter.hasNext();)
			{
				IFilterItem curItem = (IFilterItem)iter.next();
				if (curItem != null)
				{
					String name = curItem.getName();
					String value = settings.getFilterDialogModelElement(name);
					if (value == null)
					{
						// if there is no value in the file for this element type, it will be "on" or "checked"
						curItem.setState(IFilterItem.FILTER_STATE_ON);
						// if any one of the children are on, we will check the top "model elements"
						m_AllModelItems.setState(IFilterItem.FILTER_STATE_ON);
					}
					else
					{
						// if there is an entry in the file for this element type, it will "unchecked"
						curItem.setState(IFilterItem.FILTER_STATE_OFF);
					}
				}
			}
			for (Iterator iter = m_DiagramItems.values().iterator(); iter.hasNext();)
			{
				IFilterItem curItem = (IFilterItem)iter.next();
				if (curItem != null)
				{
					String name = curItem.getName();
					String value = settings.getFilterDialogDiagram(name);
					if (value == null)
					{
						curItem.setState(IFilterItem.FILTER_STATE_ON);
						// if any one of the children are on, we will check the top "node"
						m_AllDiagramItems.setState(IFilterItem.FILTER_STATE_ON);
					}
					else
					{
						curItem.setState(IFilterItem.FILTER_STATE_OFF);
					}
				}
			}
   	}
   }
	/**
	 * Saves the current settings to the registry.  In jUML, we are not using registry settings, so instead
	 * we are using the UserSettings files.  These entries will be stored in the .Settings file so that they
	 * are application wide.  The entries that will be written to the file will be those that are to be
	 * excluded (or unchecked).
	 */   
   public void saveToRegistry()
   {
		UserSettings settings = new UserSettings();
		if (settings != null)
		{
			for (Iterator iter = m_FilterItems.values().iterator(); iter.hasNext();)
			{
				IFilterItem curItem = (IFilterItem)iter.next();
				if (curItem != null)
				{
					if (curItem.getState() == IFilterItem.FILTER_STATE_OFF)
					{
						// if it is unchecked, then write it to the file
						String name = curItem.getName();
						settings.addFilterDialogModelElement(name);
					}
					else
					{
						// if it is checked, make sure it isn't in the file
						String name = curItem.getName();
						String value = settings.getFilterDialogModelElement(name);
						if (value != null)
						{
							settings.removeFilterDialogModelElement(name);
						}
						// if one of the children are checked, need to make sure that the parent is checked
						// because the user could have unchecked the parent, and then manually checked a child
						// which would cause the parent to be out of synch
						m_AllModelItems.setState(IFilterItem.FILTER_STATE_ON);
					}
				}
			}
			for (Iterator iter = m_DiagramItems.values().iterator(); iter.hasNext();)
			{
				IFilterItem curItem = (IFilterItem)iter.next();
				if (curItem != null)
				{
					if (curItem.getState() == IFilterItem.FILTER_STATE_OFF)
					{
						// if it is unchecked, then write it to the file
						String name = curItem.getName();
						settings.addFilterDialogDiagram(name);
					}
					else
					{
						// if it is checked, make sure it isn't in the file
						String name = curItem.getName();
						String value = settings.getFilterDialogDiagram(name);
						if (value != null)
						{
							settings.removeFilterDialogDiagram(name);
						}
						// if one of the children are checked, need to make sure that the parent is checked
						// because the user could have unchecked the parent, and then manually checked a child
						// which would cause the parent to be out of synch
						m_AllDiagramItems.setState(IFilterItem.FILTER_STATE_ON);
					}
				}
			}
		}
   }
   
}
