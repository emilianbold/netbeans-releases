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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

/**
 * @author sumitabhk
 *
 */
public class ProductEventHandler implements IElementDisposalEventsSink,
											IPreferenceManagerEventsSink,
											IProjectEventsSink
{
	private IADProduct m_ProductToAdvise = null;
	/**
	 * 
	 */
	public ProductEventHandler()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink#onPreDisposeElements()
	 */
	public void onPreDisposeElements( ETList<IVersionableElement> pElements, IResultCell cell )
	{
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink#onDisposedElements()
	 */
	public void onDisposedElements( ETList<IVersionableElement> pElements, IResultCell cell )
	{
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceChange(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceChange(String Name, IPropertyElement pElement, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceAdd(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceAdd(String Name, IPropertyElement pElement, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceRemove(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferenceRemove(String Name, IPropertyElement pElement, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferencesChange(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreferencesChange(IPropertyElement[] pElements, IResultCell cell)
	{
		ETList<IPropertyElement> pElementList = new ETArrayList<IPropertyElement>();
		for (int i = 0; i < pElements.length; i++)
		{
			pElementList.add(pElements[i]);
		}
		((ADProduct)m_ProductToAdvise).onPreferencesChange(pElementList);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreModeModified(IProject pProject, String newValue, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onModeModified(IProject pProject, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultLanguageModified(IProject pProject, String newValue, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreCreate(IWorkspace space, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectCreated(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectCreated(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectOpened(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectOpened(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreRename(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreRename(IProject Project, String newName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectRenamed(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectRenamed(IProject Project, String oldName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreClose(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreClose(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectClosed(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectClosed(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreSave(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreSave(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectSaved(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectSaved(IProject Project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	public void setProductToAdvise(IADProduct prod)
	{
		m_ProductToAdvise = prod;
	}
}



