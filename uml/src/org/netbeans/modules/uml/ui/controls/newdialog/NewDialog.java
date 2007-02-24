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
 * Created on Jul 15, 2003
 *
 */
package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Frame;

import org.netbeans.modules.uml.ui.support.NewDialogTabKind;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;

/**
 * @author sumitabhk
 *
 */
public class NewDialog implements INewDialog
{
	/// Should we run silent
	protected boolean m_bRunSilent;

	/// What tabs should we display
	protected boolean m_bWorkspaceTab;
	protected boolean m_bProjectTab;
	protected boolean m_bDiagramTab;
	protected boolean m_bPackageTab;
	protected boolean m_bElementTab;

	/// The various defaults
	INewDialogWorkspaceDetails m_WorkspaceDetails = null;
	INewDialogProjectDetails  m_ProjectDetails = null;
	INewDialogDiagramDetails  m_DiagramDetails = null;
	INewDialogPackageDetails  m_PackageDetails = null;
	INewDialogElementDetails   m_ElementDetails = null;

	/// The default tab
	int /*NewDialogTabKind*/ m_DefaultTab;

	/// The results of this dialog
	INewDialogTabDetails  m_Results = null;

	/**
	 * 
	 */
	public NewDialog()
	{
		super();

		m_bRunSilent    = false;

		m_bWorkspaceTab = true;
		m_bProjectTab   = true;
		m_bDiagramTab   = true;
		m_bPackageTab   = true;
		m_bElementTab   = true;

		m_DefaultTab    = NewDialogTabKind.NDTK_ALL;
		
//		IMessenger pMsg = ProductHelper.getMessenger();
//		if (pMsg != null)
//		{
//			m_bRunSilent = pMsg.getDisablingMessageing();
//		}
	}

	/**
	 * Display the dialog.
	 *
	 * @param parent[in] The HWND parent for this dialog
	 *
	 * @return HRESULT
	 */
	public INewDialogTabDetails display(Frame parent)
	{
		return display2(null, parent);
	}

	/**
	 * Display the dialog.
	 *
	 * @param pValidateProcessor [in] The validator object used to make sure the tabs are valid
	 * for the new dialog.
	 * @param parent[in] The HWND parent for this dialog
	 *
	 * @return HRESULT
	 */
	public INewDialogTabDetails display2(INewDialogValidateProcessor pValidateProcessor, Frame parent)
	{
		INewDialogTabDetails retObj = null;
		try 
		{
			if (!m_bRunSilent)
			{
				// Set the default tab to be the last tab used, unless the user has specifically
				// said to set a default tab
				if (m_DefaultTab == NewDialogTabKind.NDTK_ALL)
				{
					// The default is the last tab used.
					m_DefaultTab = NewDialogTabKind.NWIK_NEW_PACKAGE;
					
					//need to find a way to get last opened tab.
				}
				
				JDefaultNewDialog dialog = null;
				if (parent != null)
				{
					String title = NewDialogResources.getString("IDS_NEWDIALOG");
					dialog = new JDefaultNewDialog(parent, title, true);
				}
				else
				{
					dialog = new JDefaultNewDialog();
				}
				dialog.init(pValidateProcessor);
				dialog.init(null, null, null);
				if (m_DiagramDetails != null)
				{
					dialog.initWithDefaults(m_DiagramDetails);
					retObj = m_DiagramDetails;
				}
				else if (m_ElementDetails != null)
				{
					dialog.initWithDefaults(m_ElementDetails);
					retObj = m_ElementDetails;
				}
				else if (m_PackageDetails != null)
				{
					dialog.initWithDefaults(m_PackageDetails);
					retObj = m_PackageDetails;
				}
				else if (m_ProjectDetails != null)
				{
					dialog.initWithDefaults(m_ProjectDetails);
					retObj = m_ProjectDetails;
				}
				else if (m_WorkspaceDetails != null)
				{
					dialog.initWithDefaults(m_WorkspaceDetails);
					retObj = m_WorkspaceDetails;
				}
				else
				{
					dialog.initWithDefaults(null);
				}

				if (dialog.doModal() == IWizardSheet.PSWIZB_FINISH) 
				{
					retObj = dialog.getResult();
				}
				else
				{
					retObj = null;
				}
			}
		}
		catch (Exception e)
		{
			//Log.stackTrace(e);
		}
		m_Results = retObj;
		return retObj;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialog#putDefaultTab(int)
	 */
	public long putDefaultTab(int nDefaultTab)
	{
		m_DefaultTab = nDefaultTab;
		return 0;
	}

	/**
	 * Adds a tab to the dialog.  If the user doesn't specify then by default
	 * all are on. Once the user adds a specific tab all others are turned off.
	 *
	 * @param nTabKind[in]
	 */
	public long addTab(int nTabKind)
	{
		boolean bThisTab = false;
		if (nTabKind == NewDialogTabKind.NDTK_NONE)
		{
			m_bWorkspaceTab = false;
			m_bProjectTab   = false;
			m_bDiagramTab   = false;
			m_bPackageTab   = false;
			m_bElementTab   = false;
		}
		else if (nTabKind == NewDialogTabKind.NDTK_ALL)
		{
			m_bWorkspaceTab = true;
			m_bProjectTab   = true;
			m_bDiagramTab   = true;
			m_bPackageTab   = true;
			m_bElementTab   = true;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_WORKSPACE)
		{
			bThisTab = m_bWorkspaceTab;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_PROJECT)
		{
			bThisTab = m_bProjectTab;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_DIAGRAM)
		{
			bThisTab = m_bDiagramTab;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_PACKAGE)
		{
			bThisTab = m_bPackageTab;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_ELEMENT)
		{
			bThisTab = m_bElementTab;
		}

		if (bThisTab)
		{
			if (m_bWorkspaceTab &&
				m_bProjectTab &&
				m_bDiagramTab &&
				m_bPackageTab &&
				m_bElementTab)
			{
			   // All are set.  Turn them all off and add just this guy
			   m_bWorkspaceTab = false;
			   m_bProjectTab   = false;
			   m_bDiagramTab   = false;
			   m_bPackageTab   = false;
			   m_bElementTab   = false;
			}
			bThisTab = true;
		}

		return 0;
	}

	/**
	 * Is this a tab that is gonna get displayed?
	 *
	 * @param nTabKind [in] The tab kind to see if it's gonna display.  Don't use
	 * tab kinds of none or all.
	 * @param bIsTab [out,retval] TRUE if this tab is displayed.
	 */
	public boolean isTab(int nTabKind)
	{
		boolean result = false;
		if (nTabKind == NewDialogTabKind.NWIK_NEW_WORKSPACE)
		{
			result = (m_bWorkspaceTab) ? true : false;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_PROJECT)
		{
			result = (m_bProjectTab) ? true : false;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_DIAGRAM)
		{
			result = (m_bDiagramTab) ? true : false;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_PACKAGE)
		{
			result = (m_bPackageTab) ? true : false;
		}
		else if (nTabKind == NewDialogTabKind.NWIK_NEW_ELEMENT)
		{
			result = (m_bElementTab) ? true : false;
		}
		return result;
	}

	/**
	 * Provides defaults to one of the tabs.
	 *
	 * @param pDetails[in]
	 *
	 * @return HRESULT
	 */
	public long specifyDefaults(INewDialogTabDetails pDetails)
	{
		if (pDetails instanceof INewDialogWorkspaceDetails)
		{
			INewDialogWorkspaceDetails pWorkspace = (INewDialogWorkspaceDetails)pDetails;

			// Make sure this tab is on
			m_bWorkspaceTab = true;
			m_WorkspaceDetails = pWorkspace;
		}
		else if (pDetails instanceof INewDialogProjectDetails)
		{
			INewDialogProjectDetails pProject = (INewDialogProjectDetails)pDetails;

			// Make sure this tab is on
			m_bProjectTab = true;
			m_ProjectDetails = pProject;
		}
		else if (pDetails instanceof INewDialogDiagramDetails)
		{
			INewDialogDiagramDetails pDiagram = (INewDialogDiagramDetails)pDetails;

			// Make sure this tab is on
			m_bDiagramTab = true;
			m_DiagramDetails = pDiagram;
		}
		else if (pDetails instanceof INewDialogPackageDetails)
		{
			INewDialogPackageDetails pPackage = (INewDialogPackageDetails)pDetails;

			// Make sure this tab is on
			m_bPackageTab = true;
			m_PackageDetails = pPackage;
		}
		else if (pDetails instanceof INewDialogElementDetails)
		{
			INewDialogElementDetails pElement = (INewDialogElementDetails)pDetails;

			// Make sure this tab is on
			m_bElementTab = true;
			m_ElementDetails = pElement;
		}

		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialog#getResult()
	 */
	public INewDialogTabDetails getResult()
	{
		return m_Results;
	}

	/**
	 * Returns the silent flag for this dialog.  If silent then any Display calls will
	 * not display a dialog, but rather immediately return S_OK;
	 *
	 * @param pVal Has this dialog been silenced
	 *
	 * @return HRESULT
	 */
	public boolean isRunSilent()
	{
		return m_bRunSilent;
	}

	/**
	 * Sets the silent flag for this dialog.  If silent then any Display calls will
	 * not display a dialog, but rather immediately return S_OK;
	 *
	 * @param newVal Whether or not this dialog should be silent
	 *
	 * @return HRESULT
	 */
	public void setIsRunSilent(boolean value)
	{
		m_bRunSilent = value;
	}

}



