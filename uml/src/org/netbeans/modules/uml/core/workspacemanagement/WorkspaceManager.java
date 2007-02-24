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

package org.netbeans.modules.uml.core.workspacemanagement;

import org.dom4j.Document;

import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.support.umlsupport.Validator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
/**
 * @author sumitabhk
 *
 */
public class WorkspaceManager implements IWorkspaceManager
{

   private IWorkspaceEventDispatcher m_Dispatcher = null;

   /**
    *
    */
   public WorkspaceManager()
   {
      super();
   }

   /**
    *
    * Creates a new Workspace object.
    *
    * @param name[in] The name of the new workspace.
    * @param space[out] The new Workspace.
    *
    * @return OK if all is well. WSM_E_INVALID_NAME if the name passed in is
    *         invalid. WSM_E_INVALID_LOCATION  if the fileName passed in doesn't
    *         refer to a valid directory.
    * 
    */
   public IWorkspace createWorkspace(String fileName, String name) 
   throws InvalidArguments, WorkspaceManagementException
   {
      IWorkspace space = null;

      validatePath(fileName); 
      validateName(name);
      

      String header = WorkspaceMessages.getString("WORKSPACE_HEADER");
      Document doc = XMLManip.loadXML(header);

      if (doc.getRootElement() != null)
      {
         // Establish the context that the next couple of events will fire in.
         IWorkspaceEventDispatchHelper dispatcher = new WorkspaceEventDispatchHelper();
         dispatcher.setEventDispatcher(m_Dispatcher);
         boolean proceed = dispatcher.dispatchWorkspacePreCreate(fileName, name);
         if (proceed)
         {
            space = establishNewWorkspace(doc, name, fileName, false);
            if (space != null)
            {
               // Save the Workspace initially, assuring that the file has
               // been created on the file system.
               space.setIsDirty(true);
               space.save(fileName);
               dispatcher.dispatchWorkspaceCreated(space);
            }
         }
      }
      
      return space;
   }   

   /**
    *
    * Attempts to open and retrieve a Workspace located at the passed in location.
    *
    * @param fileName[in] The absolute path to the workspace file to open.
    * @param space[out] The Workspace Object.
    *
    * @return OK, else WSM_E_INVALID_LOCATION  if the fileName passed in doesn't
    *         refer to a valid directory.
    * 
    */
   public IWorkspace openWorkspace(String fileName) 
   	throws InvalidArguments, WorkspaceManagementException
   {
      IWorkspace space = null;
      Document doc = verifyFileFormat(fileName);
      IWorkspaceEventDispatchHelper dispatcher =
         new WorkspaceEventDispatchHelper();
      dispatcher.setEventDispatcher(m_Dispatcher);
      boolean proceed = dispatcher.dispatchWorkspacePreOpen(fileName);
      if (proceed)
      {
         space = establishNewWorkspace(doc, null, fileName, true);
         if (space != null)
         {
            dispatcher.dispatchWorkspaceOpened(space);
         }
      }
      return space;
   }

   /**
    *
    * Collapses the passed in Workspace, readying the workspace for transport
    * via email, etc.
    *
    * @param space[in] The workspace to collapse.
    *
    * @return HRESULT
    * @todo Implement
    *
    */
   public void collapseWorkspace(IWorkspace space)
   {
   	// The current implementation of WorkspaceManager does not implement
   	// this method.
   }

   /**
    *
    * Expands a Workspace that was previously collapsed.
    *
    * @param fileName[in] File that contains the collapsed workspace.
    * @param space[out] The resulting IWorkspace.
    *
    * @return HRESULT
    * @todo Implement
    *
    */
   public IWorkspace expandWorkspace(String fileName)
   {
		// The current implementation of WorkspaceManager does not implement
		// this method.
		
      return null;
   }

   /**
    *
    * Retrieves the EventDispatcher associated with this Mangager.
    *
    * @param pVal[out] The actual dispatcher.
    *
    * @return HRESULT
    * 
    */
   public IWorkspaceEventDispatcher getEventDispatcher()
   {
      return m_Dispatcher;
   }

   /**
    *
    * Sets the EventDispatcher on this manager.
    *
    * @param newVal[in] The new Dispatcher.
    *
    * @return HRESULT
    * 
    */
   public void setEventDispatcher(IWorkspaceEventDispatcher value)
   {
      m_Dispatcher = value;
   }

   /**
    *
    * Closes the passed-in Workspace. All open WSProjects will also be closed.
    *
    * @param space[in] The Workspace to close.
    * @param fileName[in] The absolute location to save the Workspace if the save flag
    *                     is true
    * @param save[in] 
    *                - true to save the contents of the Workspace, else
    *                - false to discard changes since the last close and save
    *                  of the workspace.
    *
    * @return HREUSLT
    */
   public void closeWorkspace(IWorkspace space, String fileName, boolean save) 
   	throws InvalidArguments, WorkspaceManagementException
   {
      IWorkspaceEventDispatcher eventDisp = getEventDispatcher();
      IWorkspaceEventDispatchHelper dispatcher = 
         new WorkspaceEventDispatchHelper();
         
      dispatcher.setEventDispatcher(eventDisp);
      boolean proceed = dispatcher.dispatchWorkspacePreClose(space);
      if (proceed)
      {
         space.closeAllProjects(save);
         if (save)
         {
            space.save(fileName);
         }
         space.setOpen(false);
         dispatcher.dispatchWorkspaceClosed(space);
      }
   }

   /**
    * Creates the actual Workspace object and sets all pertinent data.
    *
    * @param doc[in] The XML document to associate with the Workspace.
    * @param name[in] The name of the Workspace.
    * @param fileName[in] The location of the new Workspace.
    * @param space[out] The actual Workspace that was created.
    * @param opening[in] True if the workspace is being opened, else false ( the default )
    *                    if this call is being made as a result of a CreateWorkspace call.
    *
    * @return HRESULT
    */
   private IWorkspace establishNewWorkspace(Document doc, 
                                            String name,
                                            String fileName,
                                            boolean opening) 
   	throws WorkspaceManagementException
   {
      IWorkspace space = null;
      
      // Prevent any and all events from firing. The destructor of
      // the EventBlocker will release the hold. This will prevent 
      // the Workspace PreRename and Renamed events from firing.
      boolean origBlock = EventBlocker.startBlocking();
      try
      {
		space = new WorkspaceImpl();
		space.setDocument(doc);
		space.setLocation(fileName);
		if (!opening)
		{
		   space.setName(name);
		}
		space.setEventDispatcher(m_Dispatcher);
		space.setOpen(true);

		// Clear the dirty flag. No need to cause the Workspace to think
		// it needs to be saved.
		space.setIsDirty(false);
      }
      finally
      {
      	EventBlocker.stopBlocking(origBlock);
      }

      return space;
   }

   /**
    *
    * Makes sure that the file passed ins is an XML file that contains an EMBT:Workspace
    * root element.
    *
    * @param fileName[in] The absolute path to the file to test.
    * @param doc[out] The XML document, if all went well.
    *
    * @return S_OK, else USR_E_INVALID_FORMAT if the is an XML using wrong tags, else a file
    *         that is not even an XML file.
    */
   protected Document verifyFileFormat(String fileName)
   {
      return Validator.verifyXMLFileFormat(fileName, "EMBT:Workspace");
   }

	/**
	 * Makes sure that the passed-in path contains a valid directory
	 * spec.
	 *
	 * @param path An absolute path. If there is a filename, it is handled.
	 * @return true if the path is a valid path, Otherwise false.
	 */
	public static void validatePath(String fileName)
		throws WorkspaceManagementException
	{
		if(Validator.validatePath( fileName ) == false)
		{
			throw new WorkspaceManagementException(WorkspaceMessages.getString("BAD_LOCATION"));
		}
	}
	
	/**
	 * @param name
	 */
	protected static void validateName(String name)
		throws WorkspaceManagementException
	{
		if(name.length() <= 0)
		{
			throw new WorkspaceManagementException(WorkspaceMessages.getString("BAD_NAME"));
		}   
	}
}


