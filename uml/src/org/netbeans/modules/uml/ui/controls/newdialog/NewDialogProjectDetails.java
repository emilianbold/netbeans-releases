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



package org.netbeans.modules.uml.ui.controls.newdialog;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.NewProjectKind;

/**
 * @author sumitabhk
 *
 */
public class NewDialogProjectDetails implements INewDialogProjectDetails
{
	private String m_Name;
	private String m_Location;
	private String m_Mode;
	private String m_Language;
	private boolean m_AddToSourceControl;
	private boolean m_AllowFromRESelection;
	private int /*NewProjectKind*/ m_ProjectKind;
	private boolean  m_IsLanguageReadOnly;
	private boolean  m_CreateDiagram;
   private IProject m_Project;

	/**
	 * 
	 */
	public NewDialogProjectDetails()
	{
		super();
		m_AddToSourceControl = false;
		m_AllowFromRESelection = false;
		m_ProjectKind = NewProjectKind.NPK_PROJECT;
		m_CreateDiagram = true;
		m_IsLanguageReadOnly = false;
	}

	/**
	 * Name of the project.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public String getName()
	{
		return m_Name;
	}

	/**
	 * Name of the project.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setName(String value)
	{
		m_Name = value;
	}

	/**
	 * Location of the project.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public String getLocation()
	{
		return m_Location;
	}

	/**
	 * Location of the project.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setLocation(String value)
	{
		m_Location = value;
	}

	/**
	 * Should this project be added to source control?
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public boolean getAddToSourceControl()
	{
		return m_AddToSourceControl;
	}

	/**
	 * Should this project be added to source control?
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setAddToSourceControl(boolean value)
	{
		m_AddToSourceControl = value;
	}

	/**
	 * The kind of project to create.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public int getProjectKind()
	{
		return m_ProjectKind;
	}

	/**
	 * The kind of project to create.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setProjectKind(int value)
	{
		m_ProjectKind = value;
	}

	/**
	 * The default mode of the project.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public String getMode()
	{
		return m_Mode;
	}

	/**
	 * The default mode of the project.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setMode(String value)
	{
		m_Mode = value;
	}

	/**
	 * The default language of the project.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public String getLanguage()
	{
		return m_Language;
	}

	/**
	 * The default language of the project.
	 *
	 * @param pVal[in]
	 * 
	 * @return HRESULT
	 */
	public void setLanguage(String value)
	{
		m_Language = value;
	}

	/**
	 * Should the dialog show the From Reverse Engineering selection?
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public boolean getAllowFromRESelection()
	{
		return m_AllowFromRESelection;
	}

	/**
	 * Should the dialog show the From Reverse Engineering selection?
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setAllowFromRESelection(boolean value)
	{
		m_AllowFromRESelection = value;
	}

	/**
	 * Specifies if the language control is to be read.
	 *
	 * @param *pVal [out] True if readonly, false otherwise.
	 */
	public boolean getIsLanguageReadOnly()
	{
		return m_IsLanguageReadOnly;
	}

	/**
	 * Specifies if the language control is to be read.
	 *
	 * @param *pVal [out] True if readonly, false otherwise.
	 */
	public void setIsLanguageReadOnly(boolean value)
	{
		m_IsLanguageReadOnly = value;
	}

	/**
	 * Specifies whether or not to prompt the user to create a new diagram.
	 *
	 * @param *pVal [out] True if the user is to be prompted
	 */
	public boolean getPromptToCreateDiagram()
	{
		return m_CreateDiagram;
	}

	/**
	 * Specifies whether or not to prompt the user to create a new diagram.
	 *
	 * @param *pVal [out] True if the user is to be prompted
	 */
	public void setPromptToCreateDiagram(boolean value)
	{
		m_CreateDiagram = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogProjectDetails#getCreatedProject()
	 */
	public IProject getCreatedProject()
	{
		return m_Project;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogProjectDetails#setCreatedProject(com.embarcadero.describe.structure.IProject)
	 */
	public void setCreatedProject(IProject value)
	{
      m_Project = value;
	}

}



