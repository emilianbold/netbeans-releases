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

package org.netbeans.modules.uml.core.workspacemanagement;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.io.IOException;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.openide.util.NbPreferences;

/**
 * @author sumitabhk
 *
 */
public class WSElementImpl implements IWSElement
{

   //its it XML element representing this WSElement
   protected Element m_Element = null;
   private IWSProject m_Owner = null;
   protected ITwoPhaseCommit m_TwoPhase = null;
   protected boolean m_IsDirty = false;

   /**
    * 
    */
   public WSElementImpl()
   {
      super();
   }

   /**
    *
    * Retrieves the name of this element.
    *
    * @param pVal[out] The name.
    *
    * @return HRESULT
    * 
    */
   public String getName()
   {
      String name = null;
      if (m_Element != null)
      {
         name = m_Element.attributeValue("name");
      }
      return name;
   }

   /**
	* Sets / Gets the name or alias of this element.
	*/
   public String getNameWithAlias()
   {
   	  String retName = null;
	  try
	  {
		 if (showAliasedNames())
		 {
			retName = getAlias();
		 }
		 else
		 {
			retName = getName();
		 }
	  }
	  catch (Exception err )
	  {
	  }
	  return retName;
   }

   /**
	*
	* Retrieves the alias of this element.
	*
	* @param pVal[out] The alias.
	*
	* @return HRESULT
	* 
	*/
   public String getAlias()
   {
   		String retVal = null;
	  try
	  {
		 if( m_Element != null)
		 {
		 	try
		 	{
				retVal = XMLManip.getAttributeValue( m_Element, "alias");
		 	}
		 	catch (Exception e)
		 	{
				retVal = null;
		 	}

			if (retVal == null || retVal.length() == 0)
			{
			   retVal = getName();
			}
		 }
	  }
	  catch( Exception err )
	  {
	  }
	  return retVal;
   }

   /**
	*
	* Renames this element.
	*
	* @param newVal[in] The new alias.
	*
	* @return HRESULT
	* 
	*/
   public void setAlias(String newVal)
   {
   		if (m_Element != null)
   		{
   			IWorkspaceEventDispatcher disp = getDispAndElement();
   			IWSElement ele = this;
   			boolean proceed = false;
   			
   			if (disp != null)
   			{
   				IEventPayload payload = disp.createPayload("WSElementPreAliasChanged");
   				proceed = disp.fireWSElementPreAliasChanged(ele, newVal, payload);
   			}
   			
   			if (proceed)
   			{
   				setAttributeValue("alias", newVal);
   				m_IsDirty = true;
   				if (disp != null)
   				{
   					IEventPayload payload = disp.createPayload("WSElementAliasChanged");
   					disp.fireWSElementAliasChanged(ele, payload);
   				}
   			}
   		}
   }

   /**
	* Sets / Gets the name or alias of this element.
	*/
   public void setNameWithAlias(String newVal)
   {
	  try
	  {
		 if (showAliasedNames())
		 {
			setAlias(newVal);
		 }
		 else
		 {
			setName(newVal);
		 }
	  }
	  catch (Exception err )
	  {
	  }
   }

   /**
	* Returns true if we should show the aliased name
	*/
   private boolean showAliasedNames()
   {
       //kris richards - changing to NbPrefs
       return NbPreferences.forModule (WSElementImpl.class).getBoolean ("UML_Show_Aliases", false) ;
   }

   /**
    *
    * Renames this element.
    *
    * @param newVal[in] The new name.
    *
    * @return HRESULT
    * 
    */
   public void setName(String value)
   {
      if (m_Element != null)
      {
         IWorkspaceEventDispatcher disp = getDispAndElement();
         IWSElement elem = this;
         boolean proceed = true;
         if (disp != null)
         {
            IEventPayload payload =
               disp.createPayload("WSElementPreNameChanged");
            proceed = disp.fireWSElementPreNameChanged(elem, value, payload);
         }
         if (proceed)
         {
            setAttributeValue("name", value);
            m_IsDirty = true;
            if (disp != null)
            {
               IEventPayload payload =
                  disp.createPayload("WSElementNameChanged");
               disp.fireWSElementNameChanged(elem, payload);
            }
         }
      }
   }

   /**
    *
    * Retrieves the WSProject owned by this element.
    *
    * @param pVal[out] The WSProject.
    *
    * @return HRESULT
    * 
    */
   public IWSProject getOwner()
   {
      return m_Owner;
   }

   /**
    *
    * Sets the owner on this element.
    *
    * @param newVal[in] The new owner.
    *
    * @return HRESULT
    * 
    */
   public void setOwner(IWSProject value)
   {
      IWorkspaceEventDispatcher disp = getDispAndElement();
      IWSElement elem = this;
      boolean proceed = true;
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("WSElementPreOwnerChanged");
         proceed = disp.fireWSElementPreOwnerChange(elem, value, payload);
      }
      if (proceed)
      {
         m_Owner = value;
         m_IsDirty = true;
         if (disp != null)
         {
            IEventPayload payload = disp.createPayload("WSElementOwnerChanged");
            disp.fireWSElementOwnerChanged(elem, payload);
         }
      }
   }

   /**
    *
    * Retrieves the DOM element that this WSElement represents.
    *
    * @param pVal[out] The DOM element.
    *
    * @return HRESULT
    * 
    */
   public Element getElement()
   {
      return m_Element;
   }

   /**
    *
    * Sets the DOM element behind the element.
    *
    * @param newVal[in] The DOM element.
    *
    * @return HRESULT
    * 
    */
   public void setElement(Element value)
   {
      m_Element = value;
      m_IsDirty = true;
   }

   /**
    *
    * Retrieves the location of the external file that this element represents.
    *
    * @param pVal[out] The location of the file.
    *
    * @return HRESULT
    * 
    */
   public String getLocation() 
   	throws WorkspaceManagementException
   {
      String loc = null;
      if (m_Element != null)
      {
         String fileName = m_Element.attributeValue("href");
         if (fileName != null && fileName.length() > 0)
         {
         	Log.out("WSElement getLocation - " + fileName);
            loc = fileName;
//            loc = StringUtilities.splice(loc, "/", "\\");
			loc = StringUtilities.splice(loc, "/", File.separator);

			Log.out("WSElement getLocation after splice - " + loc);
            // Make sure to handle any relative path situation
            try
            {
               loc = resolveRelativePath(loc);
			   Log.out("WSElement getLocation after resolveRelativePath - " + loc);
            }
            catch (IOException e)
            {
               throw new WorkspaceManagementException(e);
            }
         }
      }
      return loc;
   }

   /**
    *
    * Sets the location of the external file that this element represents.
    *
    * @param newVal[in] The new location
    *
    * @return HRESULT
    * 
    */
   public void setLocation(String value)
   {
      // We have to turn all backslashes to forward slashes as a query against
      // a string with backslashes will fail otherwise

      String loc = StringUtilities.splice(value, "\\", "/");

      // Lower the case here so that we don't have any discrepancies when retrieving
      // later. All path manipulation should be done case-insensitive. However,
      // performing XPath queries is case sensitive. I am unaware of a mechanism that
      // allows us to perform an XPath query case insensitive.
      //
      // 2/6/03 We don't need to persist this, as checks are being made case-insensitive
      // where they are needed. By making this lowercase, the Clear-case SCM integration
      // was hosed.

      //StringUtilities::LCase( loc );
      IWorkspaceEventDispatcher disp = getDispAndElement();
      IWSElement elem = this;
      boolean proceed = true;

      if (disp != null)
      {
         IEventPayload payload =
            disp.createPayload("WSElementPreLocationChanged");
         proceed = disp.fireWSElementPreLocationChanged(elem, value, payload);
      }
      
      if (proceed)
      {
         setAttributeValue("href", loc);
         m_IsDirty = true;
         
         if(disp != null)
         {
	         IEventPayload payload = disp.createPayload("WSElementLocationChanged");
	         disp.fireWSElementLocationChanged(elem, payload);
         }
      }
   }

   /**
    *
    * Retrieves the object that will handle the two phase commit process for this
    * element.
    *
    * @param pVal[out] The current object.
    *
    * @return HREUSLT
    * 
    */
   public ITwoPhaseCommit getTwoPhaseCommit()
   {
      return m_TwoPhase;
   }

   /**
    *
    * Sets the object that will handle the two phase commit process for this
    * element.
    *
    * @param newVal[in] The interface.
    *
    * @return HRESULT
    * 
    */
   public void setTwoPhaseCommit(ITwoPhaseCommit value)
   {
      m_TwoPhase = value;
   }

   /**
    *
    * Determines whether or not this element needs to be saved or not.
    *
    * @param pVal[out] true if this element is dirty, else false
    *
    * @return S_OK, else S_FALSE if this element is not dirty itself, but rather,
    *         the TwoPhaseCommit object attached to it.
    * 
    */
   public boolean isDirty()
   {
      boolean retVal = m_IsDirty ? true : false;
      if (!m_IsDirty && m_TwoPhase != null)
      {
         boolean bTwoPhaseDirty = false;

         // See if the two phase commit registered item says it is dirty.
         // This allows the object that is controlling the actual saving
         // of this element to control state.

         bTwoPhaseDirty = m_TwoPhase.isDirty();
         retVal = bTwoPhaseDirty;

         if (bTwoPhaseDirty)
         {
            // We need to indicate that the WSElement itself is
            // not dirty, but rather, the dependent TwoPhaseCommit
            // object

            //hr = S_FALSE;
         }
      }
      return retVal;
   }

   /**
    *
    * Manually sets the dirty flag on this WSElement.
    *
    * @param newVal[in] true will cause this element to be saved next
    *                   time Save() is called, else false will cause the 
    *                   Save() method to do nothing.
    *
    * @return S_OK
    */
   public void setIsDirty(boolean value)
   {
      m_IsDirty = value;
   }

   /**
    * Saves the element.
    *
    * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element.
    */
   public void save(String location)
   	throws WorkspaceManagementException
   {
      m_IsDirty = false;
   }
   
	/**
	 * Saves the element.
	 *
	 * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element.
	 */
	public void save()
		throws WorkspaceManagementException
	{
		save("");
	}

   /**
    *
    * Retrieves the data for this element.
    *
    * @param pVal[out] The data.
    *
    * @return HRESULT
    * 
    */
   public String getData()
   {
      String data = null;
      if (m_Element != null)
      {
         data = m_Element.attributeValue("data");
      }
      return data;
   }

   /**
    *
    * sets the data for this ProjectElement.
    *
    * @param newVal[in] The new data.
    *
    * @return HRESULT
    * 
    */
   public void setData(String value)
   {
      if (m_Element != null)
      {
         IWorkspaceEventDispatcher disp = getDispAndElement();
         IWSElement elem = this;
         boolean proceed = true;
         if (disp != null)
         {
            IEventPayload payload =
               disp.createPayload("WSElementPreDataChanged");
            proceed = disp.fireWSElementPreDataChanged(elem, value, payload);
         }
         if (proceed)
         {
            setAttributeValue("data", value);
            m_IsDirty = true;
            if (disp != null)
            {
               IEventPayload payload =
                  disp.createPayload("WSElementDataChanged");
               disp.fireWSElementDataChanged(elem, payload);
            }
         }
      }
   }

   /**
    *
    * Retrieves the documentation text associated with this element.
    *
    * @param pVal[out] The current value.
    *
    * @return HRESULT
    *
    */
   public String getDocumentation()
   {
      String doc = null;
      if (m_Element != null)
      {
         doc = XMLManip.retrieveNodeTextValue(m_Element, "EMBT:Documentation");
      }
      return doc;
   }

   /**
    *
    * Sets the documentation field on this WSElement.
    *
    * @param newVal[in] The new documentation value.
    *
    * @return HRESULT
    *
    */
   public void setDocumentation(String value)
   {
      if (m_Element != null)
      {
         String curDoc = getDocumentation();
         if (!curDoc.equals(value))
         {
            IWorkspaceEventDispatcher disp = getDispAndElement();
            IWSElement elem = this;
            boolean proceed = true;
            if (disp != null)
            {
               IEventPayload payload =
                  disp.createPayload("WSElementPreDocChanged");
               proceed = disp.fireWSElementPreDocChanged(elem, value, payload);
            }
            if (proceed)
            {
               //List list = m_Element.selectSingleNode("EMBT:Documentation");
               Node docNode = m_Element.selectSingleNode("EMBT:Documentation");
//               if (list != null && list.size() > 0)
//               {
//                  docNode = (Node)list.get(0);
//               }
               if (docNode == null)
               {
                  Document doc = m_Element.getDocument();
                  docNode = doc.addElement("EMBT:Documentation");//, "www.sun.com");
                  //m_Element.appendChild(docNode);
                  docNode.setParent(m_Element);
               }
               if (docNode != null)
               {
                  docNode.setText(value);
                  m_IsDirty = true;

                  if (disp != null)
                  {
                     IEventPayload payload =
                        disp.createPayload("WSElementDocChanged");
                     disp.fireWSElementDocChanged(elem, payload);
                  }
               }
            }
         }
      }
   }

   /**
    *
    * Retrieves the dispatching object for WSElement events.
    *
    * @param disp[out] The dispatcher.
    *
    * @return HRESULT
    *
    */
   protected IWorkspaceEventDispatcher getEventDispatcher()
   {
      IWorkspaceEventDispatcher disp = null;
      // Retrieve the outermost owner. That is the element that holds the 
      // event dispatcher, if there is one
      IWSProject outerOwner = null;
      IWSProject owner = getOwner();
      while (owner != null)
      {
         outerOwner = owner;
         IWSProject temp = owner.getOwner();
         if (temp == null)
         {
            owner = null;
         }
         else
         {
            owner = temp;
         }
      }
      if (outerOwner != null)
      {
         disp = outerOwner.getEventDispatcher();
      }
      return disp;
   }

   /**
    *
    * Retrieves the dispatcher and the COM interface that this impl class represents.
    *
    * @param disp[out] The event dispatcher.
    * @param element[out] The wrapping interface.
    *
    * @return HRESULT
    *
    */
   protected IWorkspaceEventDispatcher getDispAndElement()
   {
	  IWorkspaceEventDispatcher disp = getEventDispatcher();
      if (disp == null)
      {
         // disp will be null if it is a Workspace
         // actually making this call
         if (this instanceof IWorkspace)
         {
            IWorkspace space = (IWorkspace)this;
            disp = space.getEventDispatcher();
         }
      }
      return disp;
   }

   /**
    *
    * Sets an xml attribute value on the element, firing the WSElementModified events
    *
    * @param attrName[in]  The name of the attribute to set.
    * @param value[in]     The value of the attribute
    *
    * @return S_OK if all is well. S_FALSE if the set didn't occur because a listener
    *         to the element modified event cancelled.
    *
    */
   protected void setAttributeValue(String name, String value)
   {
      IWorkspaceEventDispatcher disp = getDispAndElement();
      IWSElement elem = this;
      boolean proceed = true;
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("WSElementPreModify");
         proceed = disp.fireWSElementPreModify(elem, payload);
      }
      if (proceed)
      {
         m_Element.addAttribute(name, value);

         if (disp != null)
         {
            IEventPayload payload = disp.createPayload("WSElementModified");
            disp.fireWSElementModified(elem, payload);
         }
      }
   }

   /**
    *
    * Appends a child element to the element passed in, firing the low level WSElementModified
    * events
    *
    * @param element[in]   The element to append child to.
    * @param child[in]     The child to append.
    */
   protected void appendChild(Element element, Node child)
   {
      IWorkspaceEventDispatcher disp = getDispAndElement();
      IWSElement elem = this;
      boolean proceed = true;
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("WSElementPreModify");
         proceed = disp.fireWSElementPreModify(elem, payload);
      }
      if (proceed)
      {
         element.add(child);

         if (disp != null)
         {
            IEventPayload payload = disp.createPayload("WSElementModified");
            disp.fireWSElementModified(elem, payload);
         }
      }
   }

   /**
    *
    * Checks to see if fileName is relative to the current Workspace. If it is, an absolute
    * path is built.
    *
    * @param fileName[in] The path to check against.
    * @param pVal[out] The converted path.
    *
    * @return HRESULT
    * 
    */
   protected String resolveRelativePath(String filename) 
   	throws IOException, WorkspaceManagementException
   {
      String retPath = filename;
      
      File file = new File(filename);      
      if(file.isAbsolute() == false)
      {
//          return file.getAbsolutePath();
      	String ownerPath = getOwnerAbsolutePath();
      	if(ownerPath.length() > 0)
      	{
      		String parent = StringUtilities.getPath(ownerPath);
			StringBuffer path = new StringBuffer(parent);
			path.append(File.separator);
			path.append(filename);
			
			File completeFile = new File(path.toString());
			retPath = completeFile.getCanonicalPath();
      	}
      }
      
      return retPath;
   }

   /**
    * Retrieves the file location of the outer owner of this element. This
	 * should be a Workspace element.
	 *
	 * @return The absolute path.
    */
   protected String getOwnerAbsolutePath() throws WorkspaceManagementException
   {
		String retVal = "";
		
		IWSProject project = getOwner();

        if (project != null)
        {
    		while(project.getOwner() != null)
    		{
    			project = project.getOwner();
    		}
    		
    		if(project != null)
    		{
    			retVal = project.getLocation();
    		}
        }
		
      return retVal;
   }
   
	/**
	 * Checks if this object is an instance of IWorkspace.
	 * 
	 * @return true if the object is a workspace
	 */
	protected boolean isWorkspace()
	{
		return false;
	}
}



