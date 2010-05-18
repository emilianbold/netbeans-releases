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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Branch;
import org.dom4j.Node;
import org.dom4j.QName;

import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

/**
 *
 * @author sumitabhk
 */
public class WSProjectImpl extends WSElementImpl implements IWSProject
{
   /** The event dispatcher for the project. */
   private IWorkspaceEventDispatcher m_Dispatcher = null;
   
   private ETList<IWSElement> m_Elements = null;
   
   /** Specifies the project is opened of closed. */
   private boolean m_IsOpen = false;
   
   /**
    *
    */
   public WSProjectImpl()
   {
      super();
   }
   
   /**
    * Retrieves the set of elements in this WSProject.
    *
    * @return The WSElements collection.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#getElements()
    * @throws WorkspaceManagementException to specify workspace errors.
    */
   public ETList<IWSElement> getElements()
   throws WorkspaceManagementException
   {
      return getElements(false);
   }
   
   /**
    * Retrieves the set of elements in this WSProject.  If the force flag
    * is set to true then the WSProjects elements will always be returned.
    *
    * @param force true - Do a quick open if nessecary, false throw an
    *              exception if the project is not open.
    * @return The WSElements collection.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#getElements()
    * @throws WorkspaceManagementException to specify workspace errors.
    */
   public ETList<IWSElement> getElements(boolean force)
   throws WorkspaceManagementException
   {
      ETList<IWSElement> retVal = null;
      
      if(force == false)
      {
         // If the WSProject is not open then a WorkspaceManagementException
         // is thrown.  Since I am not going to do anything with the exception
         // I am letting it pass out of the method.  It is safe to assume that
         // everything is OK if the execution continues.
         verifyOpenState();
      }
      
      Element element = getElement();
      if(element != null)
      {
         XMLManip.DebugXML(element, false);
         
         //			//QName qName = QName.get("EMBT:WSElement", "http://www.sun.com");
         //			QName qName = QName.get("EMBT:WSElement", "www.sun.com");
         //			List nodes = element.elements(qName);
         //
         //			List nodes2 = element.selectNodes("./EMBT:WSElement");
         
         List nodes = element.elements();
         
         if(nodes != null)
         {
            //retVal = new IWSElement[nodes.size()];
            retVal = new ETArrayList<IWSElement>();
            for (int index = 0; index < nodes.size(); index++)
            {
               Element curElement = (Element)nodes.get(index);
               
               String name = curElement.getName();
               if(curElement.getQualifiedName().equals("EMBT:WSElement") == true)
               {
                  IWSElement newWSElement = createElement(curElement);
                  
                  if(newWSElement != null)
                  {
                     //retVal[index] = newWSElement;
                     retVal.add(newWSElement);
                  }
               }
            }
         }
      }
      return retVal;
   }
   
   /**
    * Creates a new WorkspaceProjectElement in this WorkspaceProject
    * from data in an external file.
    *
    * @param fileName The absolute path to the external file. This can be "", but not 0.
    * @param name The name to be applied to the new WorkspaceProjectElement.
    * @param data The data for the new WorkspaceProjectElement.
    * @return The new element.
    * @throws WorkspaceManagementException to specify workspace errors.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#addElement(java.lang.String, java.lang.String, java.lang.String)
    */
   public IWSElement addElement(String fileName, String name, String data)
   throws WorkspaceManagementException
   {
      IWSElement retVal = null;
      
      if(name.length() > 0)
      {
         verifyOpenState();
         
         // We will allow project elements that don't have a file associated
         // with them.
         if (fileName.length() > 0)
         {
            verifyUniqueLocation(fileName);
            
            if(getElement() != null)
            {
               String elementPath = retrievePathRelativeToWorkspace(fileName);
               if(elementPath.length() > 0)
               {
                  retVal = createElement(elementPath, name, data);
               }
            }
         }
         else
         {
            
         }
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#addElementFromDoc(org.dom4j.Document, java.lang.String)
    */
   public IWSElement addElementFromDoc(Document doc, String name)
   {
      IWSElement retVal = null;
      
      if((doc != null) && (name.length() > 0))
      {
         //verifyOpenState();
         // TODO Auto-generated method stub
      }
      
      return retVal;
   }
   
   /**
    *
    * Retrieves a WorkspaceProjectElement by name.
    *
    * @param name[in] The name to match against.
    * @param element[out] The found element.
    *
    * @return HRESULT
    *
    */
   public IWSElement getElementByName(String name)
   {
      IWSElement retEle = null;
      try
      {
         verifyOpenState();
         
         // First attempt to locate the element in our internal collection first
         if (m_Elements != null)
         {
            retEle = findElementByName(m_Elements, name);
         }
         if (retEle == null)
         {
            retEle = getElementByAttributeQuery("name", name);
         }
      }
      catch (WorkspaceManagementException e)
      {}
      return retEle;
   }
   
   /**
    *
    * Retrieves a WorkspaceProjectElement from the passed-in collection based on its name.
    *
    * @param elements[in] The collection to scan.
    * @param elementName[in] The name to match against.
    * @param element[out] The found element, if found.
    *
    * @return HRESULT
    *
    */
   private IWSElement findElementByName(ETList<IWSElement> elements, String elementName)
   {
      IWSElement retEle = null;
      try
      {
         verifyOpenState();
         int count = elements.size();
         for (int i=0; i<count; i++)
         {
            IWSElement curEle = elements.get(i);
            String name = curEle.getName();
            if (name != null && name.equals(elementName))
            {
               retEle = curEle;
            }
         }
      }
      catch (WorkspaceManagementException e)
      {}
      return retEle;
   }
   
   /**
    * Retrieves a WorkspaceProjectElement by its file location setting.
    *
    * @param location The file location to match against.
    * @return The found element.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#getElementByLocation(java.lang.String)
    */
   public IWSElement getElementByLocation(String location)
   throws WorkspaceManagementException
   {
      return getElementByLocation(location, false);
   }
   
   public IWSElement getElementByLocation(String location, boolean force)
   throws WorkspaceManagementException
   {
      IWSElement retVal = null;
      
      if(force == false)
      {
         //	If the WSProject is not open then a WorkspaceManagementException
         // is thrown.  Since I am not going to do anything with the exception
         // I am letting it pass out of the method.  It is safe to assume that
         // everything is OK if the execution continues.
         verifyOpenState();
      }
      
      //String lCase = location.toLowerCase();
      
      if(getElements() != null)
      {
         retVal = findElementByLocation(getElements(), location);
      }
      
      if(retVal == null)
      {
         String elementPath = retrievePathRelativeToWorkspace(location);
         if(elementPath.length() > 0)
         {
            String relative = StringUtilities.splice(elementPath, "\\", "/");
            retVal = getElementByAttributeQuery("href", relative);
         }
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#open()
    */
   public void open()
   throws WorkspaceManagementException
   {
      if((isOpen() == false) && validateWorkspace() == true )
      {
         IWorkspace space = getWorkspace();
         IWorkspaceEventDispatcher dispatcher = space.getEventDispatcher();
         try
         {
            boolean proceed = true;
            IWSProjectEventDispatcher projDispatcher = null;
            EventState state = null;
            if(dispatcher != null)
            {
               state = new EventState(dispatcher, "WorkspaceProjectEvents");
               
               projDispatcher = new WSProjectEventDispatcher(dispatcher);
               proceed = projDispatcher.dispatchWSProjectPreOpen(space, getName());
            }
            
            if( proceed)
            {
               setOpen(true);
               if (projDispatcher != null)
               {
                  projDispatcher.dispatchWSProjectOpened(this);
               }
            }
            if (state != null)
            {
               state.existState();
            }
         }
         catch (InvalidArguments e)
         {
            throw new WorkspaceManagementException(e);
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#close(boolean)
    */
   public void close(boolean saveFirst)
   throws WorkspaceManagementException
   {
      if((isOpen() == true) && (validateWorkspace() == true))
      {
         if(saveFirst == true)
         {
            save("");
         }
         
         try
         {
            // The NullPointerException will be caught below.
            
            IWorkspace space = getWorkspace();
            IWorkspaceEventDispatcher disp = space.getEventDispatcher();
            
            EventState state = new EventState(disp, "WorkspaceProjectEvents");
            
            IWSProjectEventDispatcher dispatcher = new WSProjectEventDispatcher(disp);
            try
            {
               if(dispatcher.dispatchWSProjectPreClose(this) == true)
               {
                  dispatcher.dispatchWSProjectClosed(this);
                  
                  // Null out the two phase commit that was established in Application::EstablishTwoPhaseConnection
                  // if you don't then the IProject remains open!
                  //
                  // NOTE: This is all badness. We need to NOT have the WSElement own the ITwoPhaseCommit object,
                  // as it pins it in memory.
                  
                  setTwoPhaseCommit( null );
                  
                  IWSElement element = getElementByName("_MetaData__");
                  if(element != null)
                  {
                     element.setTwoPhaseCommit(null);
                  }
                  
                  IWSElement vbElement = getElementByName("_VBAProject__");
                  if(vbElement != null)
                  {
                     vbElement.setTwoPhaseCommit(null);
                  }
                  setOpen(false);
               }
            }
            catch (InvalidArguments e1)
            {
               throw new WorkspaceManagementException(e1);
            }
            
            state.existState();
         }
         catch (NullPointerException e)
         {
            // Ignore this exception.  Either getWorkspace or getEventDispather
            // returned a null.
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#isOpen()
    */
   public boolean isOpen()
   {
      return m_IsOpen;
   }
   
   /**
    *
    * Determines whether or not this WorkspaceProject needs to be saved or not.
    *
    * @param pVal[out] true if the WorkspaceProject is dirty, else false.
    *
    * @return HRESULT
    *
    */
   public boolean isDirty()
   {
      boolean dirty = false;
      if (!m_IsDirty)
      {
         if (m_Elements != null)
         {
            int count = m_Elements.size();
            for (int i=0; i<count; i++)
            {
               IWSElement ele = m_Elements.get(i);
               dirty = ele.isDirty();
               if (dirty)
               {
                  // Only want to set to dirty if the element
                  // was actually dirty. get_IsDirty() will return
                  // S_FALSE if the element itself wasn't dirty,
                  // but its twophase commit object was
                  m_IsDirty = true;
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
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#setOpen(boolean)
    */
   public void setOpen(boolean value)
   {
      m_IsOpen = value;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#getBaseDirectory()
    */
   public String getBaseDirectory()
   {
      String retVal = "";
      
      Element curElement = getElement();
      if(curElement != null)
      {
         String baseDir = XMLManip.getAttributeValue(curElement, "baseDirectory");
         retVal = retrieveAbsolutePath(getWorkspaceDir(), baseDir);
      }
      
      return retVal;
   }
   
   /**
    * Sets the base directory on the WorkspaceProject.
    *
    * @param newVal The absolute path.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#setBaseDirectory(java.lang.String)
    */
   public void setBaseDirectory(String newVal)
   throws WorkspaceManagementException
   {
      verifyOpenState();
      String dir = validateDirectory(newVal);
      
      String wsLoc = getWorkspaceDir();
      
      // Need to get the relative path from the Workspace's directory to the
      // base directory of the WorkspaceProject
      String relative = retrieveRelativePath(dir, wsLoc);
      if(relative.length() > 0)
      {
         setAttributeValue("baseDirectory", relative);
      }
      m_IsDirty = true;
   }
   
   /**
    *
    * Sets the name of this WorkspaceProject.
    *
    * @param newVal[in] The new name.
    *
    * @return S_OK, else WSM_E_INVALID_NAME if the name is blank.
    *
    */
   public void setName(String newVal)
   {
      try
      {
         if (m_Element != null)
         {
            WorkspaceManager.validateName(newVal);
            String oldName = getName();
            if (oldName == null || oldName.length() == 0)
            {
               oldName = "";
            }
            
            IWSProjectEventDispatcher dispatcher = prepareWSProjectEventDispatcher();
            boolean proceed = true;
            if (dispatcher != null)
            {
               proceed = dispatcher.dispatchWSProjectPreRename(this, newVal);
            }
            
            if (proceed)
            {
               setAttributeValue("name", newVal);
               m_IsDirty = true;
               if (dispatcher != null)
               {
                  dispatcher.dispatchWSProjectRenamed(this, oldName);
               }
            }
         }
      }
      catch (WorkspaceManagementException e)
      {
         e.printStackTrace();
      }
      catch (InvalidArguments e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Makes sure that the location that is being used to add a new
    * WorkspaceProjectElement is unique within this WorkspaceProject.
    *
    * @param fileName[in] The location to check.
    *
    * @return true if everything is ok, false if the location
    *         already exists in one of the WSProjects.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#verifyUniqueLocation(java.lang.String)
    */
   public boolean verifyUniqueLocation(String fileName)
   throws WorkspaceManagementException
   {
      if (fileName == null)
         return false;
      boolean retVal = fileName.length() > 0;
      
      if(retVal == true)
      {
         // By passing true into the getElements we basically do a
         // quick open.  The C++ version has an inner classed called
         // QuickOpen that when it is removed from the stack closes
         // the project.  Since Java does not behave the same and
         // all QuickOpen does is set the m_isOpen to true so getElements
         // will be successful, I have overloaded getElements to ignore
         // the m_IsOpen flag.
         
         //going to make project open temporarily
         boolean isProjOpen = isOpen();
         try
         {
            setOpen(true);
            ETList<IWSElement> elements = getElements(true);
            
            if(elements != null)
            {
               File testFile = new File(fileName);
               for (int index = 0; (index < elements.size()) && (retVal == true); index++)
               {
                  String curLoc = elements.get(index).getLocation();
                  if (curLoc != null)
                  {
                     File curLocFile = new File(curLoc);
                     if(curLocFile.equals(testFile) == true)
                     {
                        retVal = false;
                     }
                  }
               }
            }
         }
         finally
         {
            //restore the project open state
            setOpen(isProjOpen);
         }
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#removeElement(org.netbeans.modules.uml.core.workspacemanagement.IWSElement)
    */
   public void removeElement(IWSElement wsElement)
   throws InvalidArguments
   {
      if((wsElement != null) && (getElement() != null))
      {
         IWorkspaceEventDispatcher dispatcher = getEventDispatcher();
         
         boolean proceed = false;
         if(dispatcher != null)
         {
            IEventPayload payload = dispatcher.createPayload("WSElementPreRemove");
            proceed = dispatcher.fireWSElementPreRemove(wsElement, payload);
         }
         
         if(proceed == true)
         {
            Element xmlElement = wsElement.getElement();
            if(xmlElement != null)
            {
               Node parent = xmlElement.getParent();
               if(parent instanceof Branch)
               {
                  ((Branch)parent).remove(xmlElement);
               }
               
               // Now remove the WSProject from out internal collection.
               removeProjectElement(wsElement);
               setIsDirty(true);
               
               if(dispatcher != null)
               {
                  IEventPayload cPayload = dispatcher.createPayload("WSElementRemoved");
                  dispatcher.fireWSElementRemoved(wsElement, cPayload);
               }
            }
         }
      }
      else
      {
         throw new InvalidArguments();
      }
   }
   
   /**
    * Removes a WorkspaceProjectElement by matching the location property
    * with the one passed in.
    *
    * @param location The location to match against.
    *
    * @return <b>true> if the element was successfully removed. <false> if it
    *         was not found.
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#removeElementByLocation(java.lang.String)
    */
   public boolean removeElementByLocation(String location)
   throws WorkspaceManagementException
   {
      boolean retVal = false;
      
      if(location.length() > 0)
      {
         String wsLoc = getWorkspaceDir();
         String relative = retrieveRelativePath(location, wsLoc);
         relative = StringUtilities.splice(relative, "\\", "/");
         IWSElement element = getElementByLocation(relative);
         
         if(element != null)
         {
            try
            {
               removeElement(element);
               retVal = true;
            }
            catch (InvalidArguments e)
            {
               throw new WorkspaceManagementException(e);
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * Retrieves the event dispatcher assocatiated with this project's Workspace.
    *
    * @return The dispatcher.
    */
   public IWorkspaceEventDispatcher getEventDispatcher()
   {
      IWorkspaceEventDispatcher retVal = null;
      
      IWorkspace space = getWorkspace();
      
      // Basically if we are a workspace then just return the m_Dispatcher
      // instance.  Otherwise find the workspace and return turn
      // the m_Disptcher object.
      if((space != null) && (isWorkspace() == false))
      {
         retVal = space.getEventDispatcher();
      }
      else
      {
         if(m_Dispatcher != null)
         {
            retVal = m_Dispatcher;
         }
      }
      
      return retVal;
   }
   
   /**
    * Sets the EventDispatcher on this Workspace.
    *
    * @param The new dispatcher
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#setEventDispatcher(org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher)
    */
   public void setEventDispatcher(IWorkspaceEventDispatcher value)
   {
      m_Dispatcher = value;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProject#getElementsByDataValue(java.lang.String)
    */
   public ETList < IWSElement > getElementsByDataValue(String dataToMatch)
   {
      
      ETList < IWSElement > matchedElements = null;
      
      if (m_Element != null)
      {
         String query = ".//*[@data=\"";
         query += dataToMatch;
         query += "\"]";
         
         List elements = m_Element.selectNodes(query);
         
         if (elements != null)
         {
            
            ETList < IWSElement > wsElements = new ETArrayList < IWSElement > ();
            
            for (Iterator < IWSElement > iter = elements.iterator(); iter.hasNext();)
            {
               Node node = (Node)iter.next();
               
               if (node != null)
               {
                  
                  Element nodeElement = (Element) node;
                  
                  IWSElement element = createElement(nodeElement);
                  
                  if (element != null)
                  {
                     wsElements.add(element);
                  }
               }
            }
            
            matchedElements = wsElements;
         }
      }
      
      return matchedElements;
   }
   
   /**
    *
    * Retrieves a string used to identify this WSProject in the users Source
    * Control Management tool.
    *
    * @param pVal[out] The id.
    *
    * @return HRESULT
    *
    */
   public String getSourceControlID()
   {
      String retStr = "";
      retStr = XMLManip.getAttributeValue(m_Element, "scmID");
      return retStr;
   }
   
   /**
    *
    * Sets the id that identifies this WSProject in the user's SCM tool.
    *
    * @param newVal[in] The new ID.
    *
    * @return HRESULT
    *
    */
   public void setSourceControlID(String value)
   {
      setAttributeValue("scmID", value);
      m_IsDirty = true;
   }
   
   //**************************************************
   // WSElement Override Methods
   //**************************************************
   
   /**
    * Saves this project and all its members.
    *
    * @param location Where this project should be saved to.
    */
   public void save(String location)
   throws WorkspaceManagementException
   {
      if (m_Elements != null && isDirty() && validateWorkspace())
      {
         markWorkspaceDirty();
         
         IWorkspaceEventDispatcher disp = getEventDispatcher();
         if (disp != null)
         {
            EventState state =
            new EventState(disp, "WorkspaceProjectEvents");
            IWSProjectEventDispatcher dispatcher =
            prepareWSProjectEventDispatcher();
            
            try
            {
               boolean proceed = dispatcher.dispatchWSProjectPreSave(this);
               if (proceed)
               {
                  ETList < IWSElement > elements =
                  new ETArrayList < IWSElement > ();
                  elements = preCommitElements(elements);
                  verifyPreCommit(elements);
                  commitElements(elements);
                  
                  // TODO: Send info message
                  //                        {
                  //                           CComBSTR name;
                  //
                  //                           _VH( get_Name( &name ));
                  //
                  //                           if (name.Length ())
                  //                           {
                  //                              INFO_MESSAGE_ID_REPL( IDS_MESSAGINGFACILITY, IDS_WSPROJECT_SAVED, name );
                  //                           }
                  //                        }
                  
                  setIsDirty(false);
                  dispatcher.dispatchWSProjectSaved(this);
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }
   
   /**
    * This method should only be called during the Save operation of this
    * WSProjectImpl. It makes sure that if someone is programmatically calling
    * a Save() on this WSProject, that the dirty flag of the Workspace is also
    * set to true, ensuring that the Workspace is saved at a later date.
    * <br/>
    * This method was formerly called <code>EnsureWorkspaceState</code>.
    *
    * @author aztec
    */
   protected void markWorkspaceDirty()
   {
      IWorkspace workspace = getWorkspace();
      if (workspace != null)
         workspace.setIsDirty(true);
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   protected final void verifyOpenState()
   throws WorkspaceManagementException
   {
      if(isOpen() == false)
      {
         throw new WorkspaceManagementException(WorkspaceMessages.getString("WSPROJECT_NOT_OPEN"));
      }
   }
   
   /**
    *
    * Creates a new WorkspaceProjectElement.  The location of the
    * new WSElement will be an empty string.
    *
    * @param curElement The DOM element used to create the IWSElement.
    * @return The new element.
    *
    * @return HRESULT
    *
    */
   protected IWSElement createElement(Element curElement)
   throws WorkspaceManagementException
   {
      return createElement(curElement, "");
   }
   
   /**
    *
    * Creates a new WorkspaceProjectElement.
    *
    * @param curElement The DOM element used to create the IWSElement.
    * @param location The location of the element, Can be an empty string.
    * @return The new element.
    *
    * @return HRESULT
    *
    */
   protected IWSElement createElement(Element curElement, String location)
   throws WorkspaceManagementException
   {
      IWSElement retVal = null;
      
      if (curElement != null)
      {
         if (m_Elements == null)
         {
            m_Elements = new ETArrayList < IWSElement >();
         }
         
         // This version does not have a location specified
         retVal = findElementByLocation(m_Elements, location);
         
         if (retVal == null)
         {
            retVal = new WSElementImpl();
            retVal.setElement(curElement);
            
            retVal.setOwner(this);
            
            if (location.length() > 0)
            {
               retVal.setLocation(location);
            }
            
            try
            {
               addElement(retVal, m_Elements);
            }
            catch(InvalidArguments e)
            {
               throw new WorkspaceManagementException(e);
            }
            retVal.setIsDirty(false);
         }
         
      }
      
      
      return retVal;
   }
   
   /**
    * Creates a new WorkspaceProjectElement.
    *
    * @param elementPath The path to the element.
    * @param name The name of the element.
    * @param data The elements data.
    * @return The new workspace element.
    * @throws WorkspaceManagementException to specify workspace errors.
    */
   protected IWSElement createElement(String fileName, String name, String data)
   throws WorkspaceManagementException
   {
      IWSElement retVal = null;
      
      verifyOpenState();
      
      if((name.length() > 0) && (getElement() != null))
      {
         // Do not dispatch any more Workspace events until we're
         // ready to dispatch the create event.
         IWorkspaceEventDispatcher disp = getEventDispatcher();
         boolean origFlag = disp.getPreventAllEvents();
         
         try
         {
            EventState state = new EventState(disp, "WorkspaceProjectEvents");
            
            if(dispatchWSProjectElementPreCreate(fileName, name, data) == true)
            {
               disp.setPreventAllEvents(true);
               
               Document doc = getElement().getDocument();
               //            	Element nodeElement = XMLManip.createElement(doc,
               //                                                            "EMBT:WSElement" ,
               //                                                            "www.sun.com");
               //					appendChild(getElement(), nodeElement);
               
               Element nodeElement = XMLManip.createElement(getElement(), "EMBT:WSElement");
               
               
               retVal = createElement(nodeElement, fileName);
               if (retVal != null)
               {
                  retVal.setName(name);
                  retVal.setData(data);
                  
                  disp.setPreventAllEvents(origFlag);
                  dispatchWSProjectElementCreated(retVal);
                  m_IsDirty = true;
               }
               
            }
            
            state.existState();
         }
         catch (NullPointerException e)
         {
         }
         catch(InvalidArguments ie)
         {
         }
         finally
         {
            disp.setPreventAllEvents(origFlag);
         }
         
      }
      
      return retVal;
   }
   
   /**
    * @param element
    */
   private void dispatchWSProjectElementCreated(IWSElement element)
   throws InvalidArguments
   {
      if(element != null)
      {
         IWorkspaceEventDispatcher dispatcher = getEventDispatcher();
         if(dispatcher != null)
         {
            IEventPayload payload = dispatcher.createPayload("WorkspaceProjectElementCreated");
            dispatcher.fireWSElementCreated(element, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
   }
   
   /**
    * @param fileName
    * @param name
    * @param data
    * @return
    */
   private boolean dispatchWSProjectElementPreCreate(String fileName, String name, String data)
   {
      boolean retVal = true;
      
      IWorkspaceEventDispatcher disp = getEventDispatcher();
      
      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("WorkspaceProjectElementPreCreate");
         retVal = disp.fireWSElementPreCreate(this, fileName, name, data, payload);
      }
      
      return retVal;
   }
   
   /**
    *
    * Performs a full commit on all WSElements contained in elements.
    *
    * @param elements The collection of elements to commit.
    */
   private void commitElements(ETList < IWSElement > elements)
   throws WorkspaceManagementException, InvalidArguments
   {
      verifyOpenState();
      
      if (elements != null)
      {
         for (Iterator < IWSElement > iter = elements.iterator(); iter.hasNext();)
         {
            IWSElement element = iter.next();
            if(element != null)
            {
               ITwoPhaseCommit commit = element.getTwoPhaseCommit();
               if(commit != null)
               {
                  commit.commit();
               }
            }
         }
      }
      else
      {
         throw new InvalidArguments();
      }
   }
   
   /**
    *
    * Makes sure that all dirty project elements that were previously
    * PreCommitted did not cause another dependent project element to become
    * dirty again.
    *
    * @param elements The collection of element to verify.
    */
   private void verifyPreCommit(ETList < IWSElement > elements)
   throws WorkspaceManagementException, InvalidArguments
   {
      final int BAIL_OUT = 10;
      
      boolean isDirty      = false;
      int     checkBailOut = 0;
      
      do
      {
         for (Iterator < IWSElement > iter = elements.iterator(); iter.hasNext();)
         {
            IWSElement element = iter.next();
            if(element != null)
            {
               if(element.isDirty() == true)
               {
                  isDirty = true;
                  break;
               }
            }
         }
         
         if(isDirty)
         {
            preCommitElements(elements);
         }
         checkBailOut++;
      }
      while ((isDirty == true) && (checkBailOut < BAIL_OUT));
      
   }
   
   /**
    * @param elements
    */
   private ETList < IWSElement > preCommitElements(ETList < IWSElement > elements)
   throws WorkspaceManagementException, InvalidArguments
   {
      verifyOpenState();
      
      if (m_Elements != null)
      {
         int count = m_Elements.size();
         //for (Iterator < IWSElement > iter = m_Elements.iterator(); iter.hasNext();)
         for (int i=count-1; i>=0; i--)
         {
            IWSElement element = m_Elements.get(i);
            if(element != null)
            {
               if(element.isDirty() == true)
               {
                  ITwoPhaseCommit commit = element.getTwoPhaseCommit();
                  if(commit != null)
                  {
                     commit.preCommit();
                  }
                  
                  //	Clear the dirty bit on the element.
                  // At this point, the only thing left to do
                  // should be to commit the changes back to the
                  // original file ( which is the job of the Commit()
                  // method ).
                  
                  element.save("");
                  
                  // Copy onto the collection of elements that were
                  // PreCommitted.
                  
                  elements = addElement( element, elements );
               }
            }
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      return elements;
   }
   
   /**
    * @param element
    * @param elements
    */
   private ETList < IWSElement > addElement(IWSElement element, ETList < IWSElement > elements)
   throws WorkspaceManagementException, InvalidArguments
   {
      verifyOpenState();
      
      if(element != null && elements != null)
      {
         boolean addElement = true;
         String  loc        = element.getLocation();
         
         int count = elements.size();
         //for (Iterator < IWSElement > iter = elements.iterator(); iter.hasNext();)
         for (int i=0; i<count; i++)
         {
            IWSElement curEelement = elements.get(i);
            if(curEelement != null)
            {
               String projLoc = curEelement.getLocation();
               String curFileName = getWorkspaceFileName();
               String test        = retrieveAbsolutePath(curFileName, projLoc);
               
               if(loc != null && loc.equals(test))
               {
                  addElement = false;
                  break;
               }
            }
         }
         
         if(addElement == true)
         {
            elements.add(element);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      return elements;
   }
   
   /**
    * @param curFileName
    * @param projLoc
    * @return
    */
   private String retrieveAbsolutePath(String curFileName, String projLoc)
   {
      // HAVE TODO: Figure out how to handle relative paths.
      String path = projLoc;
      File file = new File(path);
      if (!file.isAbsolute())
      {
         // If the path is relative, we need to get an absolute
         // path in order to make the match comparison.
         File absFile = new File(curFileName, projLoc).getAbsoluteFile();
         try
         {
            path = absFile.getCanonicalPath();
         }
         catch (IOException e)
         {
            path = absFile.toString();
         }
      }
      //return curFileName;
      return path;
   }
   
   /**
    * @return
    */
   private String getWorkspaceFileName()
   throws WorkspaceManagementException
   {
      String retVal = "";
      
      IWorkspace workspace = getWorkspace();
      if(workspace != null)
      {
         retVal = workspace.getLocation();
      }
      else
      {
         retVal = getLocation();
      }
      
      return retVal;
   }
   
   /**
    * @return
    */
   private IWorkspace getWorkspace()
   {
      IWorkspace retVal = null;
      
      IWSProject owner = getOwner();
      if (owner instanceof IWorkspace)
      {
         retVal = (IWorkspace)owner;
      }
      else
      {
         if (this instanceof IWorkspace)
         {
            retVal = (IWorkspace)this;
         }
      }
      
      return retVal;
   }
   
   /**
    *
    */
   protected IWSProjectEventDispatcher prepareWSProjectEventDispatcher()
   {
      IWSProjectEventDispatcher retVal = null;
      
      IWorkspaceEventDispatcher actualDisp = getEventDispatcher();
      if(actualDisp != null)
      {
         retVal = new WSProjectEventDispatcher(actualDisp);
      }
      
      return retVal;
   }
   
   /**
    * @param m_Elements
    * @param location
    * @return
    */
   private IWSElement findElementByLocation(ETList< IWSElement > collection,
   String                   location)
   throws WorkspaceManagementException
   {
      IWSElement retVal = null;
      
      //	If the WSProject is not open then a WorkspaceManagementException
      // is thrown.  Since I are not going to do anything with the exception
      // I am letting it pass out of the method.  It is safe to assume that
      // everything is OK if the execution continues.
      verifyOpenState();
      
      for (Iterator iter = collection.iterator();
      (iter.hasNext() == true) && (retVal == null); )
      {
         IWSElement curElement = (IWSElement)iter.next();
         try
         {
            String curLocation = curElement.getLocation();
            if(curLocation.equalsIgnoreCase(location) == true)
            {
               retVal = curElement;
            }
         }
         catch (NullPointerException e)
         {
            // Basically we should never get in this situation.
            // However, if we do I want to continue on with the
            // interation.
         }
      }
      
      return retVal;
   }
   
   /**
    * @param fileName
    */
   protected String retrievePathRelativeToWorkspace(String fileName)
   throws WorkspaceManagementException
   {
      String retVal = "";
      
      String wsFileName = getWorkspaceFileName();
      retVal = retrieveRelativePath(fileName, wsFileName);
      
      return retVal;
      
   }
   
   /**
    * Retrieves the relative path between newFile and curFile.
    *
    * @param newFile The new file we are trying to get a relative path to.
    * @param curFile The root path where we are relative to.
    *
    * @return The relative path, else "" on error.
    */
   private String retrieveRelativePath(String file, String parent)
   {
      return PathManip.retrieveRelativePath(file, parent);
   }
   
   /**
    * @return
    */
   private String getWorkspaceDir()
   {
      String retVal = "";
      
      IWorkspace space = getWorkspace();
      if((space != null) && (space != this))
      {
         retVal = space.getBaseDirectory();
      }
      
      return retVal;
   }
   
   /**
    * Determines whether or not the passed-in string contains a valid directory.
    *
    * @param dir The path to check. If dir contains an absolute path to
    *            a file, the filename and extension will be stripped off.
    */
   protected String validateDirectory(String dir)
   throws WorkspaceManagementException
   {
      String retVal = dir;
      
      File dirFile = new File(dir);
      if(dirFile.isDirectory() == false)
      {
         File parentFile = dirFile.getParentFile();
         if(parentFile.isDirectory() == false)
         {
            throw new WorkspaceManagementException(WorkspaceMessages.getString("BAD_LOCATION"));
         }
         else
         {
            retVal = parentFile.getAbsolutePath();
         }
      }
      
      return retVal;
   }
   
   /**
    * Makes sure that this WorkspaceProject is properly owned
    * by an encapsulating Workspace.
    *
    * @return true if owned, else false.
    */
   protected boolean validateWorkspace()
   throws WorkspaceManagementException
   {
      boolean retVal = false;
      
      if(getOwner() != null)
      {
         retVal = true;
      }
      else if(isWorkspace() == true)
      {
         retVal = true;
      }
      else
      {
         throw new WorkspaceManagementException(WorkspaceMessages.getString("NO_WORKSPACE"));
      }
      
      return retVal;
   }
   
   /**
    *
    * Adds a new element to the internal collection.
    *
    * @param element The WorkspaceProjectElement to add.
    * @param elements The collection of elements that we are appending.
    *
    */
   protected void AddElement(IWSElement element, ArrayList < IWSElement > elements)
   throws WorkspaceManagementException
   {
      if(elements.size() > 0)
      {
         boolean addElement = true;
         String  loc        = element.getLocation();
         
         for (int index = 0; (index < elements.size()) && (addElement == true); index++)
         {
            String projLocation = elements.get(index).getLocation();
            String curFileName  = getWorkspaceFileName();
            
            String test = retrieveAbsolutePath(curFileName, projLocation);
            if(loc.equals(test) == true)
            {
               addElement = false;
            }
         }
         
         if(addElement == true)
         {
            elements.add(element);
         }
      }
   }
   
   /**
    * Retrieves a WorkspaceProjectElement by a query matching against a specific attribute
    * and that attribute's value. The attribute is an XML attribute.
    *
    * @param attrName The name of the XML attribute to use in the query.
    * @param attrValue The value of the attribute to match against.
    * @return The found element, if found.
    */
   protected IWSElement getElementByAttributeQuery(String attrName, String attrValue)
   throws WorkspaceManagementException
   {
      IWSElement retVal = null;
      
      if(getElement() != null)
      {
         StringBuffer query = new StringBuffer("./EMBT:WSElement[@");
         query.append(attrName);
         query.append("=\"");
         query.append(attrValue);
         query.append("\"]");
         
         Node node = XMLManip.selectSingleNode(getElement(), query.toString());
         if(node instanceof Element)
         {
            retVal = createElement(((Element)node));
         }
      }
      
      return retVal;
   }
   
   /**
    * Retrieves an element out of the passed-in collection by the location property.
    *
    * @param elements[in] The collection to scan.
    * @param location[in] The location to match against.
    * @return The found element, if found.
    */
   //	protected IWSElement findElementByLocation(ETList<IWSElement> elements,
   //	                                           String location)
   //	  throws WorkspaceManagementException
   //	{
   //		return findElementByLocation(elements, location, false);
   //	}
   
   /**
    * Retrieves an element out of the passed-in collection by the location property.
    * If the force flag is set to true then the WSProjects elements will always
    * be returned.
    *
    * @param elements[in] The collection to scan.
    * @param location[in] The location to match against.
    * @param force true - Do a quick open if nessecary, false throw an
    *              exception if the project is not open.
    * @return The found element, if found.
    */
   protected IWSElement findElementByLocation(ETList<IWSElement> elements,
   String location,
   boolean force)
   throws WorkspaceManagementException
   {
      IWSElement retVal = null;
      
      if(force == false)
      {
         // If the WSProject is not open then a WorkspaceManagementException
         // is thrown.  Since I am not going to do anything with the exception
         // I am letting it pass out of the method.  It is safe to assume that
         // everything is OK if the execution continues.
         verifyOpenState();
      }
      
      File locationFile = new File(location);
      
      for (int index = 0; (index < elements.size()) && (retVal == null); index++)
      {
         String curLoc = elements.get(index).getLocation();
         if(curLoc.length() > 0)
         {
            File curLocFile = new File(curLoc);
            if(locationFile.equals(curLocFile) == true)
            {
               retVal = elements.get(index);
            }
         }
      }
      
      return retVal;
   }
   
   /**
    *  Makes sure that the WorkspaceProjectElement that has the same location
    * property as the wsElement is no longer present on the internal collection of
    * WSElements.
    *
    * @param wsElement The WorkspaceProjectElement to remove.
    */
   protected void removeProjectElement(IWSElement wsElement)
   throws InvalidArguments
   {
      if((wsElement != null) && (m_Elements != null))
      {
         m_Elements.remove(wsElement);
      }
      else
      {
         throw new InvalidArguments();
      }
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
      //according to C++ code, we need to do a quickOpen here
      boolean open = isOpen();
      try
      {
         setOpen(true);
         IWSElement element = getElementByName("_MetaData__");
         if (element != null)
         {
            loc = element.getLocation();
         }
      }
      finally
      {
         setOpen(open);
      }
      return loc;
   }
   
}
