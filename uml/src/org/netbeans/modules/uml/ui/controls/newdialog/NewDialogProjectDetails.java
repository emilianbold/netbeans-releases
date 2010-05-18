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
	 */
	public void setCreatedProject(IProject value)
	{
      m_Project = value;
	}

}



