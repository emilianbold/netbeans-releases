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


package org.netbeans.modules.uml.ui.products.ad.applicationcore;

import org.netbeans.modules.uml.core.Application;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.IQueryManager;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

/**
 * @author sumitabhk
 *
 */
public class ADApplication implements IADApplication //, IProjectUpgradeEventsSink
{
	private IApplication m_Application = null;

	/**
	 * 
	 */
	public ADApplication()
	{
		//super();
		createApplication();
	}

	/**
	 * CoCreates and application and stores it as a member variable.  Where necessary CADApplication
	 * routines will simply dispatch to the wrapped IApplication.
	 */
	private void createApplication()
	{
		if (m_Application == null)
		{
			m_Application = new Application();
			registerToDispatchers();
		}
	}

	private void registerToDispatchers()
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.structure());
		if (obj != null && obj instanceof IStructureEventDispatcher)
		{
			//((IStructureEventDispatcher)obj).registerForProjectUpgradeEvents(this);
		}
	}

	/**
	 * Returns the IApplication that this CADApplication wraps.  You'll need this function
	 * when you desire to retrieve events from the IApplication interface.  You need to advise
	 * to what is returned.
	 *
	 * @param pVal The IApplication that this CADApplication is wrapping
	 */
	public IApplication getApplication()
	{
		return m_Application;
	}

	/**
	 * Replaces the currently wrapped IApplication with the argument one.
	 *
	 * @param newVal The IApplication should now be wrapped
	 */
	public void setApplication(IApplication value)
	{
		m_Application = value;
	}

	/**
	 * This is a passthrough to the IApplication that is kept as a member variable.
	 *
	 * @param newProject[out]
	 */
	public IProject createProject()
	{
		IProject retProj = null;
		if (m_Application != null)
		{
			retProj = m_Application.createProject();
		}
		return retProj;
	}
	
	public IWorkspace createWorkspace(String location, String name)
	{
		IWorkspace retProj = null;
		if (m_Application != null)
		{
			retProj = m_Application.createWorkspace(location, name);
		}
		return retProj;
	}
	
	public IWorkspace openWorkspace(String location)
	{
		IWorkspace retProj = null;
		if (m_Application != null)
		{
			retProj = m_Application.openWorkspace(location);
		}
		return retProj;
	}
	
	public void closeWorkspace(IWorkspace space, String location, boolean save)
	{
		if (m_Application != null)
		{
			m_Application.closeWorkspace(space, location, save);
		}
	}
	
	public IWSProject importProject(IWorkspace space, IProject proj)
	{
		IWSProject retProj = null;
		if (m_Application != null)
		{
			retProj = m_Application.importProject(space, proj);
		}
		return retProj;
	}
	
	/**
	 * This is a passthrough to the IApplication that is kept as a member variable.
	 *
	 * @param fileName[in]
	 * @param project[out]
	 */
	public IProject openProject(String filename)
	{
		IProject retProj = null;
		if (m_Application != null)
		{
			retProj = m_Application.openProject(filename);
		}
		return retProj;
	}
	
	public IProject openProject(IWorkspace space, String projname)
	{
		IProject retProj = null;
		if (m_Application != null)
		{
			retProj = m_Application.openProject(space, projname);
		}
		return retProj;
	}
	
	public IProject openProject(IWorkspace space, IWSProject wsProj)
	{
		IProject retProj = null;
		if (m_Application != null)
		{
			retProj = m_Application.openProject(space, wsProj);
		}
		return retProj;
	}
	
	/**
	 * This is a passthrough to the IApplication that is kept as a member variable.
	 *
	 * @param project[in]
	 * @param save[in]
	 */
	public void closeProject(IProject proj, boolean save)
	{
		if (m_Application != null)
		{
			m_Application.closeProject(proj, save);
		}
	}

	/**
	 * This is a passthrough to the IApplication that is kept as a member variable.
	 *
	 * save[in]
	 */
	public void closeAllProjects( boolean save )
	{
		if (m_Application != null)
		{
			m_Application.closeAllProjects(save);
		}
	}
	
	/**
	 * This is a passthrough to the IApplication that is kept as a member variable.
	 *
	 * @param pVal[out]
	 */
	public ETList<IProject> getProjects()
	{
		ETList<IProject> retVal = null;
		if (m_Application != null)
		{
			retVal = m_Application.getProjects();
		}
		return retVal;
	}
	
	public ETList<IProject> getProjects(String filename)
	{
		ETList<IProject> retVal = null;
		if (m_Application != null)
		{
			retVal = m_Application.getProjects(filename);
		}
		return retVal;
	}
	
	/**
	 * This is a passthrough to the IApplication that is kept as a member variable.
	 *
	 * @param pWorkspace [in] The workspace the project should be in
	 * @param projectName[in] The project we're looking for
	 * @param project[out] The found project
	 */
	public IProject getProjectByName(IWorkspace pWorkspace, String projectName)
	{
		 IProject retProj = null;
		 if (m_Application != null)
		 {
		 	retProj = m_Application.getProjectByName(pWorkspace, projectName);
		 }
		 return retProj;
	}
	
	public IProject getProjectByName(String projectName)
	{
		 IProject retProj = null;
		 if (m_Application != null)
		 {
			retProj = m_Application.getProjectByName(projectName);
		 }
		 return retProj;
	}
	
	public IProject getProjectByID(String projID)
	{
		 IProject retProj = null;
		 if (m_Application != null)
		 {
			retProj = m_Application.getProjectByID(projID);
		 }
		 return retProj;
	}
	
	public IProject getProjectByFileName(String fileName)
	{
		 IProject retProj = null;
		 if (m_Application != null)
		 {
			retProj = m_Application.getProjectByFileName(fileName);
		 }
		 return retProj;
	}
	
	public String getExeLocation()
	{
		String loc = "";
		if (m_Application != null)
		{
			loc = m_Application.getInstallLocation();
		}
		return loc;
	}
	
	public String getInstallLocation() 
	{
		String loc = "";
		if (m_Application != null)
		{
			loc = m_Application.getInstallLocation();
		}
		return loc;
	}
	
	public void destroy() 
	{
		if (m_Application != null)
		{
			revokeDispatchers();
			m_Application.destroy();
		}
	}
	
	/**
	 * 
	 */
	private void revokeDispatchers()
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.structure());
		if (obj != null && obj instanceof IStructureEventDispatcher)
		{
			//((IStructureEventDispatcher)obj).revokeProjectUpgradeSink(this);
		}
	}

	/**
	 *
	 * Returns the number of closed projects.
	 *
	 * @param nNumClosed The number of closed projects
	 *
	 * @return 
	 *
	 */
	public int getNumClosedProjects()
	{
		int num = 0;
		if (m_Application != null)
		{
			num = m_Application.getNumClosedProjects();
		}
		return num;
	}
	
	/**
	 *
	 * Are all the WSProjects owned by this element opened.
	 *
	 * @param bAllOpened true if all the WSProjects are opened
	 *
	 * @return 
	 *
	 */
	public int getNumOpenedProjects()
	{
		int num = 0;
		if (m_Application != null)
		{
			num = m_Application.getNumOpenedProjects();
		}
		return num;
	}
	
	public IQueryManager getQueryManager()
	{
		IQueryManager retObj = null;
		if (m_Application != null)
		{
			retObj = m_Application.getQueryManager();
		}
		return retObj;
	}
	
	public void setQueryManager(IQueryManager newVal)
	{
		if (m_Application != null)
		{
			m_Application.setQueryManager(newVal);
		}
	}
	
	public String getApplicationVersion()
	{
		String retObj = null;
		if (m_Application != null)
		{
			retObj = m_Application.getApplicationVersion();
		}
		return retObj;
	}
        
        
         /**
	 * This is a passthrough to the IApplication.save 
	 *
	 * @param project[in]
	 */
	public void saveProject(IProject proj)
	{
		if (m_Application != null)
		{
			m_Application.saveProject(proj);
		}
	}
	
}


