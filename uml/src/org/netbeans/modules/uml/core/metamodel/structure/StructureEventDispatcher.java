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


package org.netbeans.modules.uml.core.metamodel.structure;

import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

public class StructureEventDispatcher extends EventDispatcher implements IStructureEventDispatcher
{

	private EventManager< IProjectEventsSink > m_ProjectSink =
	 							new EventManager< IProjectEventsSink >();
	private EventManager< IArtifactEventsSink > m_ArtifactSink = 
				    			new EventManager< IArtifactEventsSink >();
											
	/**
	 * 
	 */
	public StructureEventDispatcher() 
	{
		super();
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param handler[in] The actual sink that will recieve notifications	 
	 */
   public void registerForProjectEvents(IProjectEventsSink handler)
   {
		m_ProjectSink.addListener(handler,null);      
   }

  /**
   *
   * Removes a listener from the current list.
   */
   public void revokeProjectSink(IProjectEventsSink handler)
   {
		m_ProjectSink.removeListener(handler);      
   }

   /* 
    * 
    */
   public void registerForArtifactEvents(IArtifactEventsSink handler)
   {
		m_ArtifactSink.addListener(handler,null);      
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#revokeArtifactSink(org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink)
    */
   public void revokeArtifactSink(IArtifactEventsSink handler)
   {      
		m_ArtifactSink.removeListener(handler);  
   }

   /**
	*
	* Fired right before the Mode property on the passed in Project is changed.
	*
	* @param pProject[in] The project in question
	* @param newValue[in] The value of the Mode
	* @return proceed[out] true if the event was fully dispatched, else
	*                     false if a listener cancelled full dispatch
	*/
   public boolean firePreModeModified(IProject pProject, String newValue, IEventPayload payload)
   {
		boolean proceed = true;
	
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(newValue);
	    Object var = prepareVariant(vect);	    
		if (validateEvent("PreModeModified", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor preModeModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onPreModeModified");
			
			Object[] parms = new Object[3];
			parms[0] = pProject;
			parms[1] = newValue;
			parms[2] = cell;
			preModeModified.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(preModeModified);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /**
	* Fired whenever the passed in Project's Mode property has changed
	*
	* @param pProject[in]  The modified Project
	* @param payload[in]   The payload
	*/
   public void fireModeModified(IProject pProject, IEventPayload payload)
   {
		if (validateEvent("ModeModified", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor modeModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onModeModified");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			modeModified.setParameters(parms);
			m_ProjectSink.notifyListeners(modeModified);
		}	      
   }

   /**
	*
	* Fired right before the DefaultLanguage property on the passed in Project is changed.
	*
	* @param pProject[in] The project in question
	* @param newValue[in] The value of the language
	* @return proceed[out] true if the event was fully dispatched, else
	*                     false if a listener cancelled full dispatch
	*/
   public boolean firePreDefaultLanguageModified(IProject pProject, String newValue, IEventPayload payload)
   {
		boolean proceed = true;
		
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(newValue);
		Object var = prepareVariant(vect);
		if (validateEvent("PreDefaultLanguageModified", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor preDefaultLanguageModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onPreDefaultLanguageModified");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			preDefaultLanguageModified.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(preDefaultLanguageModified);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
      
   }

   /**
	*
	* Fired whenever the default language on the passed in Project has been changed
	*
	* @param pProject[in]        The modified Project
	* @param payload[in]         The payload
	*/
   public void fireDefaultLanguageModified(IProject pProject, IEventPayload payload)
   {
		if (validateEvent("DefaultLanguageModified", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor defaultLanguageModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onDefaultLanguageModified");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			defaultLanguageModified.setParameters(parms);
			m_ProjectSink.notifyListeners(defaultLanguageModified);
		}	
      
   }

   public boolean fireProjectPreCreate(IWorkspace space, IEventPayload payload)
   {
		boolean proceed = true;
		
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,space);		
		Object var = prepareVariant(vect);
		if (validateEvent("ProjectPreCreate", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectPreCreate = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectPreCreate");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			projectPreCreate.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(projectPreCreate);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }


   public void fireProjectCreated(IProject pProject, IEventPayload payload)
   {
		if (validateEvent("ProjectCreated", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectCreated = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectCreated");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			projectCreated.setParameters(parms);
			m_ProjectSink.notifyListeners(projectCreated);
		}	
      
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireProjectPreOpen(IWorkspace space, String fileName, IEventPayload payload)
   {
		boolean proceed = true;
		
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,space);
		vect.add(fileName);
		Object var = prepareVariant(vect);
		if (validateEvent("ProjectPreOpen", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectPreOpen = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectPreOpen");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			projectPreOpen.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(projectPreOpen);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectOpened(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectOpened(IProject pProject, IEventPayload payload)
   {
		if (validateEvent("ProjectOpened", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectOpened = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectOpened");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			projectOpened.setParameters(parms);
			m_ProjectSink.notifyListeners(projectOpened);
		}      
      
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectPreRename(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireProjectPreRename(IProject pProject, String newName, IEventPayload payload)
   {
		boolean proceed = true;
		
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(newName);
		Object var = prepareVariant(vect);
		if (validateEvent("ProjectPreRename", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectPreRename = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectPreRename");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			projectPreRename.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(projectPreRename);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectRenamed(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectRenamed(IProject pProject, String oldName, IEventPayload payload)
   {
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(oldName);
		Object var = prepareVariant(vect);
		if (validateEvent("ProjectRenamed", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectRenamed = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectRenamed");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			projectRenamed.setParameters(parms);
			m_ProjectSink.notifyListeners(projectRenamed);
		}	
      
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectPreClose(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireProjectPreClose(IProject pProject, IEventPayload payload)
   {
		boolean proceed = true;
	
		if (validateEvent("ProjectPreClose", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectPreClose = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectPreClose");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			projectPreClose.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(projectPreClose);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectClosed(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectClosed(IProject pProject, IEventPayload payload)
   {
		if (validateEvent("ProjectClosed", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectClosed = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectClosed");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			projectClosed.setParameters(parms);
			m_ProjectSink.notifyListeners(projectClosed);
		}	
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectPreSave(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireProjectPreSave(IProject pProject, IEventPayload payload)
   {
		boolean proceed = true;
		
		if (validateEvent("ProjectPreSave", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectPreSave = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectPreSave");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			projectPreSave.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(projectPreSave);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireProjectSaved(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectSaved(IProject pProject, IEventPayload payload)
   {
		if (validateEvent("ProjectSaved", pProject))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor projectSaved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onProjectSaved");
			
			Object[] parms = new Object[2];
			parms[0] = pProject;
			parms[1] = cell;
			projectSaved.setParameters(parms);
			m_ProjectSink.notifyListeners(projectSaved);
		}	      
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireArtifactFileNamePreModified(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireArtifactFileNamePreModified(IArtifact artifact, String newName, IEventPayload payload)
   {
		boolean proceed = true;
			
		if (validateEvent("ArtifactFileNamePreModified", artifact))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor artifactFileNamePreModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink", 
						"onPreFileNameModified");
			
			Object[] parms = new Object[3];
			parms[0] = artifact;
			parms[1] = newName;
			parms[2] = cell;
			artifactFileNamePreModified.setParameters(parms);
			m_ArtifactSink.notifyListenersWithQualifiedProceed(artifactFileNamePreModified);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;      
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireArtifactFileNameModified(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireArtifactFileNameModified(IArtifact artifact, String oldName, IEventPayload payload)
   {	
		if (validateEvent("ArtifactFileNameModified", artifact))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor artifactFileNameModified = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink", 
						"onFileNameModified");
			
			Object[] parms = new Object[3];
			parms[0] = artifact;
			parms[1] = oldName;
			parms[2] = cell;
			artifactFileNameModified.setParameters(parms);
			m_ArtifactSink.notifyListeners(artifactFileNameModified);
		}	            
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireArtifactPreDirty(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireArtifactPreDirty(IArtifact artifact, IEventPayload payload)
   {		
		boolean proceed = true;
				
		if (validateEvent("ArtifactPreDirty", artifact))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor artifactPreDirty = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink", 
						"onPreDirty");
			
			Object[] parms = new Object[2];
			parms[0] = artifact;			
			parms[1] = cell;
			artifactPreDirty.setParameters(parms);
			m_ArtifactSink.notifyListenersWithQualifiedProceed(artifactPreDirty);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireArtifactDirty(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireArtifactDirty(IArtifact artifact, IEventPayload payload)
   {   	
		if (validateEvent("ArtifactDirtied", artifact))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor artifactDirtied = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IArtifactEventsSink", 
						"onDirty");
			
			Object[] parms = new Object[2];
			parms[0] = artifact;			
			parms[1] = cell;
			artifactDirtied.setParameters(parms);
			m_ArtifactSink.notifyListeners(artifactDirtied);
		}	
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireArtifactPreSave(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireArtifactPreSave(IArtifact artifact, String fileName, IEventPayload payload)
   {
		boolean proceed = true;
				
		if (validateEvent("ArtifactPreSaved", artifact))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor artifactPreSaved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onArtifactPreSaved");
			
			Object[] parms = new Object[3];
			parms[0] = artifact;
			parms[1] = fileName;
			parms[2] = cell;
			artifactPreSaved.setParameters(parms);
			m_ArtifactSink.notifyListenersWithQualifiedProceed(artifactPreSaved);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;   
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireArtifactSave(org.netbeans.modules.uml.core.metamodel.structure.IArtifact, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireArtifactSave(IArtifact artifact, String fileName, IEventPayload payload)
   {
		if (validateEvent("ArtifactSaved", artifact))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor artifactSaved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onArtifactSaved");
			
			Object[] parms = new Object[3];
			parms[0] = artifact;
			parms[1] = fileName;			
			parms[2] = cell;
			artifactSaved.setParameters(parms);
			m_ProjectSink.notifyListeners(artifactSaved);
		}      
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#firePreReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean firePreReferencedLibraryAdded(IProject pProject, String refLibLoc, IEventPayload payload)
   {  
		boolean proceed = true;
			
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(refLibLoc);
		Object var = prepareVariant(vect);
		if (validateEvent("PreReferencedLibraryAdded", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor preReferencedLibraryAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onPreReferencedLibraryAdded");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			preReferencedLibraryAdded.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(preReferencedLibraryAdded);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /* 
    * @see org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher#fireReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireReferencedLibraryAdded(IProject pProject, String refLibLoc, IEventPayload payload)
   {
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(refLibLoc);
		Object var = prepareVariant(vect);
		if (validateEvent("ReferencedLibraryAdded", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor referencedLibraryAdded = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onReferencedLibraryAdded");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			referencedLibraryAdded.setParameters(parms);
			m_ProjectSink.notifyListeners(referencedLibraryAdded);
		}
   }

   /* 
    * 
    */
   public boolean firePreReferencedLibraryRemoved(IProject pProject, String refLibLoc, IEventPayload payload)
   {
		boolean proceed = true;
				
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(refLibLoc);
		Object var = prepareVariant(vect);
		if (validateEvent("PreReferencedLibraryRemoved", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor preReferencedLibraryRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onPreReferencedLibraryRemoved");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			preReferencedLibraryRemoved.setParameters(parms);
			m_ProjectSink.notifyListenersWithQualifiedProceed(preReferencedLibraryRemoved);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;
   }

   /* 
    *
    */
   public void fireReferencedLibraryRemoved(IProject pProject, String refLibLoc, IEventPayload payload)
   {
		Vector<Object> vect = new Vector<Object>();
		vect.add(0,pProject);
		vect.add(refLibLoc);
		Object var = prepareVariant(vect);
		if (validateEvent("ReferencedLibraryRemoved", var))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor referencedLibraryRemoved = new EventFunctor("org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink", 
						"onReferencedLibraryRemoved");
			
			Object[] parms = new Object[2];
			parms[0] = var;
			parms[1] = cell;
			referencedLibraryRemoved.setParameters(parms);
			m_ProjectSink.notifyListeners(referencedLibraryRemoved);
		}      
   }	


   public int getNumRegisteredSinks()
   {
		return m_ProjectSink.getNumListeners() +
			   m_ArtifactSink.getNumListeners();
   }
}


