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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

/**
 * @author sumitabhk
 *
 */
public class WorkspaceImpl extends WSProjectImpl implements IWorkspace
{

   private Document m_Document = null;   
   private ArrayList < IWSProject > m_Projects = new ArrayList < IWSProject >();
	//private ArrayList m_Projects = new ArrayList();

   /**
    * 
    */
   public WorkspaceImpl()
   {
      super();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#createWSProject(java.lang.String, java.lang.String)
    */
   public IWSProject createWSProject(String baseDirectory, String name) 
   throws WorkspaceManagementException
   {
		IWSProject retVal = null;
      EventState state = new EventState(getEventDispatcher(), "WSProjectEvents");
      
      validateWSProjectCreation(baseDirectory, name);
      
      if((dispatchWSProjectPreCreate(name) == true) && (getDocument() != null))
      {
      	Element element = XMLManip.createElement(getRootElement(), "EMBT:WSProject");
		   //Element element = XMLManip.createElement(getRootElement(), "EMBT:WSProject", "www.sun.com");
      	if(element != null)
      	{	
      		//getRootElement().add(element);
      		retVal = createProject(element, name);
      		if (retVal != null)
      		{
				retVal.setOpen(true);
				retVal.setBaseDirectory(baseDirectory);
      		
				dispatchWSProjectCreate(retVal);
				m_IsDirty = true;
      		}
      	}
      }
      
      state.existState();
      
      return retVal;
   }
   
   public String getBaseDirectory()
   {
		try
		{
			String fileName = getLocation();
			if (fileName != null && fileName.length() > 0)
			{
				int index = fileName.lastIndexOf(File.separator);
				if (index >= 0)
				{
					return fileName.substring(0, index);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
   		return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#getWSProjects()
    */
   public ETList<IWSProject> getWSProjects() throws WorkspaceManagementException
   {
		ETList<IWSProject> retVal = new ETArrayList<IWSProject>();
		
		validateDocument();
		
		Document doc = getDocument();
		List nodes = doc.getRootElement().selectNodes("//EMBT:WSProject");
		boolean isDirty = isDirty();
		
		// Prevent rename events from occurring
		boolean origFlag = EventBlocker.startBlocking();
		try
		{
			for(int index = 0; index < nodes.size(); index++)
			{
				if(nodes.get(index) instanceof Element)
				{
					Element curElement = (Element)nodes.get(index);
					String name = XMLManip.getAttributeValue(curElement, "name");
				
					// We want the isDirty flag to be set to false when the project 
					// is created.
					retVal.add(createProject(curElement, name, true, false));
				}
			}
		
			if (!isDirty)
			{
				//	The call to CreateProject will result in the setting of the dirty
				// flag to true. If we weren't dirty coming into get_WSProjects(), we
				// shouldn't be dirty going out either...
				setIsDirty(isDirty);		
			}
		}
		finally
		{
			EventBlocker.stopBlocking(origFlag);
		}
		
      return retVal;
   }

   /**
    * Retrieves the WSProject that matches the name passed in.
	 *
	 * @param projName	The name to match against.
	 * @return	The found WSProject.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#getWSProjectByName(java.lang.String)
    */
   public IWSProject getWSProjectByName(String projName)
   {
   	return getWSProjectByName(projName, true);
   }
	
	
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#getDocument()
    */
   public Document getDocument()
   {      
      return m_Document;
   }

   /**
    *  Sets the document that this Workspace represents.
    * 
    * @param value The new document.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#setDocument(org.dom4j.Document)
    */
   public void setDocument(Document value)
		throws WorkspaceManagementException
   {
      m_Document = value;
      Element element = getRootElement();
      
      setElement(element);
      resetState();
   }   

   /**
	*
	* Sets the filename of this workspace.
	*
	* @param newVal[in]	The absolute path to the workspace
	*
	* @return HRESULT
	* 
	*/
   public void setLocation(String newVal)
   {
		try
		{
			validatePath(newVal);
			Element root = getRootElement();
			setAttributeValue("fileName", newVal);
			m_IsDirty = true;
		}
		catch (WorkspaceManagementException e)
		{
			e.printStackTrace();
		}
   }

   /**
	*
	* Retrieves the filename of this Workspace.
	*
	* @param pVal[out]	The current location of this Workspace
	*
	* @return HRESULT
	* 
	*/
   public String getLocation()
   {
   		String retVal = "";
		try
		{
			Element root = getRootElement();
			if (root != null)
			{
				retVal = XMLManip.getAttributeValue(root, "fileName");
			}
		}
		catch (WorkspaceManagementException e)
		{
			e.printStackTrace();
		}
   		return retVal;
   }

   /**
    * Opens the WSProject that matches the passed in name.
	 *
	 * @param projName The name of the project to open.
	 * @return The opened WSProject, else 0.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#openWSProjectByName(java.lang.String)
    */
   public IWSProject openWSProjectByName(String projName)
     throws WorkspaceManagementException
   {
      IWSProject retVal = getWSProjectByName(projName, true);
      
      if(retVal != null)
      {
      	ensureWSProjectIsOpen(retVal);
      }
      
      return retVal;
   }

   /**
	*
	* Opens a WSProject by looking for a WSElement that contains the location of the Project.
	*
	* @param locationOfProj[in]  The location
	* @param wsProject[out]      The wsProject
	*
	* @return HRESULT
	*
	*/
   public IWSProject openWSProjectByLocation( String locationStr )
	   throws WorkspaceManagementException
	{
		IWSProject retProj = null;
		if (locationStr != null && locationStr.length() > 0)
		{
			retProj = getWSProjectByLocation(locationStr);
			if (retProj != null)
			{
				ensureWSProjectIsOpen(retProj);
			}
		}
		return retProj;
	}

	/**
	 *
	 * Retrieves a WSProject by location. Maybe open may not be.
	 *
	 * @param locationOfProj[in]  The location of the project
	 * @param wsProject[out]      The found ws project
	 *
	 * @return HRESULT
	 *
	 */
	private IWSProject getWSProjectByLocation(String locationOfProj)
	{
		IWSProject retProj = null;
		try
		{
			if (m_Element != null && locationOfProj.length() > 0)
			{
				String fileLoc = locationOfProj.toLowerCase();
				String elementPath = retrievePathRelativeToWorkspace(locationOfProj);
				if (elementPath.length() > 0)
				{
					XMLManip.checkForIllegals(elementPath);
					String relative = StringUtilities.splice(elementPath, "\\", "/");
				
					String query = ".//EMBT:WSElement[@href='";
					query += relative;
					query += "']/parent::*";
				
					Node node = m_Element.selectSingleNode(query);
					if (node != null)
					{
						String name = XMLManip.getAttributeValue(node, "name");
						if (name != null && name.length() > 0)
						{
							retProj = getWSProjectByName(name, true);
						}
					}
				}
			}
		}
		catch (WorkspaceManagementException e)
		{}
		return retProj;
	}

   /**
    *  Closes the WSProject that matches the passed-in name.
	 *
	 * @param projName[in]	The name of the project to close.
	 * @param saveFirst[in] - true to save the WSProject first before closing, else
	 *                      - false to just close it.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#closeWSProjectByName(java.lang.String, boolean)
    */
   public void closeWSProjectByName(String projName, boolean saveFirst)
     throws WorkspaceManagementException
   {
      if(projName.length() > 0)
      {
      	IWSProject wsProject = getWSProjectByName(projName, false);
      	if(wsProject != null)
      	{
      		wsProject.close(saveFirst);
      	}
      }
   }

   /**
    * Closes all open projects.
	 *
	 * @param saveFirst[in] - true to save the WSProjects first before closing, else
	 *                      - false to just close them.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#closeAllProjects(boolean)
    */
   public void closeAllProjects(boolean saveFirst)
   {
		if(m_Projects.isEmpty() == false)
		{
			for (Iterator iter = m_Projects.iterator(); iter.hasNext();)
         {
            IWSProject project = (IWSProject)iter.next();
            try
            {
               project.close(saveFirst);
            }
            catch (WorkspaceManagementException e)
            {
					// HAVE TODO: Figure out how to respond to an event canceled
            }
         }
         
         m_Projects.clear();
		}
   }

   /**
    * Removes the WSProject with the matching name from this Workspace.
	 *
	 * @param projName he name of the WSProject to remove
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#removeWSProjectByName(java.lang.String)
    */
   public void removeWSProjectByName(String projName)
     throws WorkspaceManagementException
   {
   	if(projName.length() > 0)
   	{
			validateDocument();
			
			// The true must be there otherwise removing ws projects from the workspace
			// fails when the project is open.  To test, open a WS and a project in the main
			// gui.  Then right click on the project and select remove project from workspace.
			IWSProject wsProject = getWSProjectByName(projName, true);
			
			EventState state = new EventState(getEventDispatcher(), "WSProjectEvents");
			
			IWSProjectEventDispatcher dispactcher = prepareWSProjectDispatcher();
			try
         {
            if(dispactcher.dispatchWSProjectPreRemove(wsProject) == true)
            {
            	// Retrieve the DOM element from the project and remove it from our tree.
            	Element projElement = wsProject.getElement();
            	
            	if(projElement != null)
            	{
            		Element root = getRootElement();
            		root.remove(projElement);
            		removeProject(wsProject);
            		
						dispactcher.dispatchWSProjectRemoved(wsProject);
            	}
            }
         }
         catch (InvalidArguments e)
         {
            throw new WorkspaceManagementException(e);
         }
			
			state.existState();
   	}		
   }   

   /**
	 * Removes the WSProject from this Workspace.
	 *
	 * @param wsProject[in]	The WSProject to remove
	 * @see RemoveWSProjectByName()
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#removeWSProject(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
    */
   public void removeWSProject(IWSProject wsProject) 
   	throws WorkspaceManagementException
   {
		if(wsProject != null)
		{
			removeWSProjectByName(wsProject.getName());
		}
   }

   /**
    * Makes sure that the absolute path to a particular file is unique across this 
	 * Workspace.
	 *
	 * @param location	The path to check against
	 *
	 * @return true if everything is ok, false if the location
	 *         already exists in one of the WSProjects.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#verifyUniqueElementLocation(java.lang.String)
    */
   public boolean verifyUniqueElementLocation(String location)
     throws WorkspaceManagementException
   {
		boolean retVal = location.length() > 0;
		
		ETList<IWSProject> wsProjects = getWSProjects();
		
		if((location.length() > 0) && wsProjects != null)
		{
			for (int index = 0; (index < wsProjects.size()) && (retVal == true); index++)
         {
            retVal = wsProjects.get(index).verifyUniqueLocation(location);            
         }
		}
		
		return retVal;
   }

   /**
    * Removes the WSElement found at the specified location.
	 *
	 * @param location[in]	The absolute path to the WSElement
	 *
	 * @return <b>true> if the element was successfully removed. <false> if it
	 *         was not found.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#removeWSElementByLocation(java.lang.String)
    */
   public boolean removeWSElementByLocation(String location)
	  throws WorkspaceManagementException
   {
		boolean retVal = false;
		
		if(location.length() > 0)
		{
			for (Iterator iter = m_Projects.iterator(); (iter.hasNext()) && (retVal == false);)
         {
            IWSProject proj = (IWSProject)iter.next();
            if(proj != null)
            {
            	retVal = proj.removeElementByLocation(location);
            }
         }
		}
		
		return retVal;
   }

   /**
    * Attempts to open a WSProject by matching the data string passed in.
	 *
	 * @param dataStr   The Data string to match against.
	 * @return The found WSProject
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspace#openWSProjectByData(java.lang.String)
    */
   public IWSProject openWSProjectByData(String dataStr) 
   	throws WorkspaceManagementException
   {
		IWSProject retVal = null;
		
		if(dataStr.length() > 0)
		{
			StringBuffer query = new StringBuffer("//EMBT:WSElement[@data='" );
			query.append(dataStr);
			query.append("']/parent::*");
			//query.append("']");
			
			//**************************************************
         // TEST TEST TEST
         //**************************************************
         ETSystem.out.println(getDocument().asXML());
         ETSystem.out.println(query.toString());
			//**************************************************
			// TEST TEST TEST
			//**************************************************
			Node node = XMLManip.selectSingleNode(m_Document, query.toString());
			String name = XMLManip.getAttributeValue(node, "name");
			retVal = openWSProjectByName(name);
		}
      
      return retVal;
   }
   
   //**************************************************
   // WSElement Overridden Methods
   //**************************************************
	
	/**
	 * Saves this workspace and all its contents.
	 * 
	 * @param location The absolute path to the location
 	 *                 to save the Workspace.
 	 * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSElement#save(java.lang.String)
	 */
	public void save(String location)
		throws WorkspaceManagementException
	{
		validateDocument();
		
		if(isDirty() == true)
		{
			String fileName = location;
			if(fileName == null || fileName.length() <= 0)
			{
				fileName = getLocation();
			}
			
			if(validatePath(fileName) == true)
			{
				Element root = getRootElement();
				
				IWorkspaceEventDispatcher dispatcher = getEventDispatcher();
				EventState state = new EventState(dispatcher, "WorkspaceEvents");
				
            try
            {
               boolean proceed = dispatchWorkspacePreSave(fileName);			
               if(proceed != false)
               {
                  // Save any toplevel elements that this Workspace owns
                  boolean origFlag = EventBlocker.startBlocking(dispatcher);
                  try
                  {
                     super.save(fileName);
                  }
                  finally
                  {
                     EventBlocker.stopBlocking(origFlag, dispatcher);
                  }

                  // Loop through all of the WSProject Elements and save
                  // them.
                  //for (Iterator < IWSProject > iter = m_Projects.iterator(); iter.hasNext();)
                  for (Iterator iter = m_Projects.iterator(); iter.hasNext();)
                  {
                     IWSProject project = ( IWSProject )iter.next();
                     project.save();               

                  }

                  //	If the Workspace and all of it's elements are actually not
                  // dirty themselves, that is, if only their TwoPhaseCommit objects
                  // are dirty, then the Workspace file does not need to get physically
                  // saved.
                  Attribute attr = root.attribute("fileName");
                  if(attr != null)
                  {
                     root.remove(attr);
                  }

                  boolean successful = XMLManip.save(getDocument(), fileName);
                  XMLManip.setAttributeValue(root, "fileName", fileName);

                  if (!successful)
                  {
                     throw new WorkspaceManagementException( );
                  }

                  dispatchWorkspaceSaved(this);
               }
            }
            finally
            {
               state.existState();
            }
            m_IsDirty = false;
			}
		}
	}
	
	//**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Sends the WorkspaceSaved event to all registered workspace listeners.
    * 
    * @param space The workspace that is being saved.
    */
   protected void dispatchWorkspaceSaved(IWorkspace space)
   {
   	try
      {
         IWorkspaceEventDispatcher dispatcher = getEventDispatcher();
         IEventPayload payload = dispatcher.createPayload("WorkspaceSaved");
         dispatcher.fireWorkspaceSaved(space, payload);
      }
      catch (NullPointerException e)
      {
         // Do nothing.  The event dispatcher is null.
      }      
   }

   /**
    * @param fileName
    * @return
    */
   private boolean dispatchWorkspacePreSave(String fileName)
   {
   	boolean retVal = true;
   	
		IWorkspaceEventDispatcher dispatcher = (IWorkspaceEventDispatcher)getEventDispatcher();	   	
      if(dispatcher != null)
      {
      	IEventPayload payload = dispatcher.createPayload("WorkspacePreSave");
      	retVal = dispatcher.fireWorkspacePreSave(fileName, payload);
      }
      
      return retVal;
   }

   /**
	 *
	 * Makes sure that the passed in path contains a valid directory
	 * spec.
	 *
	 * @param path An absolute path. If there is a filename, it is handled.
	 *
	 * @return true if the path is valid, false otherwise.
	 *
	 */
   private boolean validatePath(String path)
   {
   	boolean retVal = true;
   	
      try
      {
         WorkspaceManager.validatePath(path);
      }
      catch (WorkspaceManagementException e)
      {
         retVal = false;
      }
      
      return retVal;
   }

   /**
	 * Retrieves the root element of the workspace.
	 *
	 * @return The root element.
	 * @throws WorkspaceManagementException if the document has not been set
	 *         or the document does not have a root element.
 	 */
   protected Element getRootElement()
   	throws WorkspaceManagementException
   {
		Element retVal = null;
		
		validateDocument();
		
		retVal = m_Document.getRootElement();		
		if(retVal == null)
		{
			throw new WorkspaceManagementException(WorkspaceMessages.getString("WORKSPACE_CORRUPTED"));
		}
		
		return retVal;       
   }

   /**
    * Makes sure that the XML document this Workspace represents is valid.
	 *
	 * @throws WorkspaceManagementException exception if the document has not
	 *         been set.
    */
   private void validateDocument() throws WorkspaceManagementException
   {
      if(m_Document == null)
      {
			throw new WorkspaceManagementException(WorkspaceMessages.getString("NO_DATASOURCE"));
      }
   }
	
	/**
	 * Takes this Workspace back to a just-after-creation-state.
	 */
	private void resetState()
	{
		m_Projects.clear();
		setIsDirty(false);
	}

	/**
	 *
	 * Dipatches the Project precreate event.
	 *
	 * @param projName The name of the new project.
	 * @return True if project creation should continue, else false.
	 */
	protected boolean dispatchWSProjectPreCreate(String name)
	{
		boolean retVal = true;
		
  	   try
      {
         IWSProjectEventDispatcher dispatcher = prepareWSProjectDispatcher();
         IResultCell cell = dispatcher.dispatchWSProjectPreCreate(this, name);
         if (cell != null)
         {
            retVal = cell.canContinue();
         }
      }
      catch (InvalidArguments e)
      {
         e.printStackTrace();
      }
		catch(NullPointerException e)
		{
			// Do not worry about the exception.  For some reason
			// the dispatcher was null.  The event will not be sent.
			retVal = true;
		}
		
		return retVal;
	}

   /**
    * Dispatches the WSProject created event.
	 * @param project The WSProject that was just created.
	 */
	protected void dispatchWSProjectCreate(IWSProject project) 
	{
		try
      {
         IWSProjectEventDispatcher dispatcher = prepareWSProjectDispatcher();
         dispatcher.dispatchWSProjectCreated(project);
      }
      catch (InvalidArguments e)
      {
         e.printStackTrace();
      }
      catch(NullPointerException e)
      {
      	// Do not worry about the exception.  For some reason
      	// the dispatcher was null.  The event will not be sent.
      }
   
	}
	
	/**
	 * Creates a WSProjectEventDispatcher object ready to fire events.
	 * @return The prepared dispatcher.
	 */
	private IWSProjectEventDispatcher prepareWSProjectDispatcher()
	{
		IWSProjectEventDispatcher retVal = new WSProjectEventDispatcher();
		retVal.setEventDispatcher(getEventDispatcher());		
		
		return retVal;
	}
	
	/**
	 * @param baseDirectory
	 * @param name
	 */
	protected void validateWSProjectCreation(String baseDirectory, String name) 
		throws WorkspaceManagementException
	{
		validateDocument();
		WorkspaceManager.validateName(name);
		WorkspaceManager.validatePath(baseDirectory);
		verifyUniqueProject(name);
      
	}
	
	/**
	 * Makes sure that no other WSProject exists by the
 	 * passed in name.
 	 * 
    * @param name The name to match against.
    */
   protected void verifyUniqueProject(String name) 
   	throws WorkspaceManagementException
   {
   	// I am using a StringBuffer because it is suppose to be
   	// faster than adding two string together.
      StringBuffer query = new StringBuffer( "//WSProject[@name=\"");
      query.append(name);
		query.append("\"]");
		
		Document doc = getDocument();
		if(doc != null)
		{
			List nodes = doc.selectNodes(query.toString());
			if(nodes.size() > 0)
			{
				throw new WorkspaceManagementException(WorkspaceMessages.getString("PROJECT_EXISTS"));
			}
		}
		
   }

   /**
	 * Creates a new project. The project returned may not actually have been created. Rather,
	 * it was discovered on the internal collection of WSProjects.
	 *
	 * @param storage[in] The IStorage to put on the new WSProject.
	 * @param name[in] The name of the project to create. Default is 0. If 0, the name
	 *                 found on element will be used.
	 * @return The new object.
	 */
	protected IWSProject createProject(Element element, String name)
	{
		return createProject(element, name, true);
	}
	
	protected IWSProject createProject(Element element, String name, boolean add)
	{
		return createProject(element, name, add, true);
	}
	
	/**
	 * Creates a new project. The project returned may not actually have been created. Rather,
	 * it was discovered on the internal collection of WSProjects.  When the projects name is 
	 * set the isDirty flag will be set to true.  If the createDirty is set to false then 
	 * the is dirty flag will be set to false.
	 * 
	 * <I><B>NOTE:</B> This is different than the C++ version.  In C++ the created project is
	 * an out parameter and the method returns an HRESULT.  The HRESULT is used to indicate 
	 * if the project was actually created of found in the collection of projects.  In Java 
	 * objects are passed by value and thus an object can not be an out parameter.  So, we
	 * are returning the project instead.  Since we are returning the object we can to use 
	 * the return value to indicate that the project was already created.  So, the createDirty
	 * flag is needed to know if the dirty flag should be set or not.</I>
	 *
	 * @param storage[in] The IStorage to put on the new WSProject.
	 * @param name[in] The name of the project to create. Default is 0. If 0, the name
	 *                 found on element will be used.
	 * @param add[in] True to automatically add the newly created WSProject to our internal collection ( the
	 *                default ). False to not.
	 * @param createDirty If true and the WSProject is created the dirty flag will remain set.
	 *                    if false and the WSProject is created the dirty flag will be set to false.
	 * @return The new object.
	 */
	protected IWSProject createProject(Element element, 
	                                   String  name, 
	                                   boolean add, 
	                                   boolean createDirty)
	{
		IWSProject retVal = findProjectByName(m_Projects, name);
		
		if(retVal == null)
		{
			
			IWSProject proj = new WSProjectImpl();
			proj.setElement(element);
			proj.setOwner(this);
			
			if(name != null && name.length() > 0)
			{
				// Prevent rename events from occurring
				boolean origFlag = EventBlocker.startBlocking();
				try
				{
					proj.setName(name);
				}
				finally
				{
					EventBlocker.stopBlocking(origFlag);
				}
				
				if(createDirty == false)
				{
					proj.setIsDirty(false);
				}
			}
			
			if(add == true)
			{
				retVal = addProject(proj);
			}
			else
			{
				retVal = proj;
			}
		}
		
		return retVal;
	}

   /**
    * Adds the passed in WSProject to the internal collection.
	 *
	 * @param project[in] The project to add.
	 * @return It is possible for the WSProject to already be on
	 *         the list of projects. In any case, the project that
	 *         matches the name of the passed-in project is returned
	 *         in actualProject. Callers should always use actualProject
	 *         upon return to make sure that the WSProject object is the
	 *         object actually on the Workspace's list of projects.
    */
   private IWSProject addProject(IWSProject project)
   {
		IWSProject projectToAdd = project;
		if(m_Projects.size() > 0)
		{
			String name = project.getName();
			for (Iterator iter = m_Projects.iterator(); iter.hasNext();)
         {
            IWSProject curProj = (IWSProject)iter.next();
            if(curProj != null)
            {
            	if(name != null && name.equals(curProj.getName()))
            	{
            		projectToAdd = curProj;
            		break;
            	}
            }
         }
		}
		
		if(projectToAdd == project)
		{
			m_Projects.add(projectToAdd);
		} 
      return projectToAdd;
   }

   /**
    * Locates a WSProject in the passed-in collection by the name of the project.
	 *
	 * @param projects[in] The collection to look through.
	 * @param projName[in] The name of the Project to match against.
	 * @return The found WSProject, if any.
    */
   private IWSProject findProjectByName(ArrayList projects, String name)
   {
		IWSProject retVal = null;
		
		for (Iterator iter = projects.iterator(); iter.hasNext() && (retVal == null);)
      {
         IWSProject curProject = (IWSProject)iter.next();
         if(name.equals(curProject.getName()) == true)
         {
         	retVal = curProject;
         }
      }
		
      return retVal;
   }
   
	/**
	 *
	 * Retrieves a WSProject owned by this Workspace.
	 *
	 * @param projName The name to match against.
	 * @param createIfNotFound True to create an IWSProject if not found in the internal collection, else
	 *                         false to not create it.
	 * @return The found WSProject.
	 */
	public IWSProject getWSProjectByName(String projName, boolean createIfNotFound)
	{
		IWSProject retVal = null;
	
		if(projName.length() > 0)
		{
			if(m_Projects.size() > 0)
			{
				retVal = findProjectByName(m_Projects, projName);
			}
			
			if((retVal == null) && (createIfNotFound == true))
			{
				StringBuffer query = new StringBuffer("//EMBT:WSProject[@name=\"");
				query.append(projName);
				query.append("\"]");
				
				Node node = XMLManip.selectSingleNode(getDocument(), query.toString());
				if(node instanceof Element)
				{
					retVal = createProject((Element)node, projName);
				}
			}
		}
	
		return retVal;
	}

	/**
	 * Makes sure the passed in IWSProject is open
	 *
	 * @param wsProj The WSProject to ensure is open
	 */
	private void ensureWSProjectIsOpen(IWSProject wsProj)
	  throws WorkspaceManagementException
	{
		if((wsProj != null) && (wsProj.isOpen() == false))
		{
			wsProj.open();
		}      
	}
	
	/**
	 * Checks if this object is an instance of IWorkspace.
	 * 
	 * @return true if the object is a workspace
	 */
	protected boolean isWorkspace()
	{
		return true;
	}
	
	/**
	 *
	 * Determines whether or not this Workspace needs to be saved or not.
	 *
	 * @param pVal[out]	true if the Workspace is dirty, else false.
	 *
	 * @return HRESULT
	 * 
	 */
	public boolean isDirty()
	{
		boolean dirty = false;
		if (!m_IsDirty)
		{
			// Loop through all the projects to see if they are dirty
			if (m_Projects != null)
			{
				int count = m_Projects.size();
				for (int i=0; i<count; i++)
				{
					IWSProject proj = m_Projects.get(i);
					dirty = proj.isDirty();
					if (dirty)
					{
						break;
					}
				}
			}
		}
		else
		{
			dirty = true;
		}
		return dirty;
	}
	
	/**
	 * Makes sure that the WSProject that has the same name as
	 * wsProject is no longer present on the internal collection of
	 * WSProjects.
	 *
	 * @param wsProject[in] The WSProject to remove.
	 */
	private void removeProject(IWSProject wsProject)
	{
		if(m_Projects.isEmpty() == false)
		{
			m_Projects.remove(wsProject);
			m_IsDirty = true;
		}
   
	}
}


