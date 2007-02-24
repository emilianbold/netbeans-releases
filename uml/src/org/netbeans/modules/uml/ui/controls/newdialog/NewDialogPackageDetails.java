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

import java.util.Vector;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.ui.support.NewPackageKind;

/**
 * @author sumitabhk
 *
 */
public class NewDialogPackageDetails implements INewDialogPackageDetails
{
	private String m_Name;
	private INamespace m_Namespace;
	private Vector < IElement > m_AdditionalNamespaces;
	private boolean m_CreateScopedDiagram;
	private String m_ScopedDiagramName;
	private int /*NewPackageKind*/ m_PackageKind;
	private boolean m_AllowFromRESelection;
	private int /*DiagramKind*/ m_DiagramKind;

	/**
	 * 
	 */
	public NewDialogPackageDetails()
	{
		super();
		m_CreateScopedDiagram = false;
		m_PackageKind = NewPackageKind.NPKGK_PACKAGE;
		m_AllowFromRESelection = true;
		m_DiagramKind = IDiagramKind.DK_CLASS_DIAGRAM;
	}

	/**
	 * Name of the package.
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
	 * Name of the package.
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
	 * The namespace this package will occupy.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public INamespace getNamespace()
	{
		return m_Namespace;
	}

	/**
	 * The namespace this package will occupy.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setNamespace(INamespace value)
	{
		m_Namespace = value;
	}

	/**
	 * Add an additional namespace to our list of possible namespaces.
	 *
	 * @param pNamespace[in]
	 * 
	 * @return HRESULT
	 */
	public long addNamespace(INamespace pNamespace)
	{
		if (pNamespace instanceof IElement)
		{
			if (m_AdditionalNamespaces == null)
			{
				m_AdditionalNamespaces = new Vector<IElement>();
			}
			if (m_AdditionalNamespaces != null)
			{
				IElement pElement = (IElement)pNamespace;
				m_AdditionalNamespaces.add(pElement);
			}
		}
		return 0;
	}

	/**
	 * Should we create a scoped diagram?
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public boolean getCreateScopedDiagram()
	{
		return m_CreateScopedDiagram;
	}

	/**
	 * Should we create a scoped diagram?
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setCreateScopedDiagram(boolean value)
	{
		m_CreateScopedDiagram = value;
	}

	/**
	 * The name of the scoped diagram.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public String getScopedDiagramName()
	{
		return m_ScopedDiagramName;
	}

	/**
	 * The name of the scoped diagram.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setScopedDiagramName(String value)
	{
		m_ScopedDiagramName = value;
	}

	/**
	 * The kind of scoped diagram.
	 *
	 * @param pKind[out]
	 * 
	 * @return HRESULT
	 */
	public int getScopedDiagramKind()
	{
		return m_DiagramKind;
	}

	/**
	 * The kind of scoped diagram.
	 *
	 * @param pKind[out]
	 * 
	 * @return HRESULT
	 */
	public void setScopedDiagramKind(int value)
	{
		m_DiagramKind = value;
	}

	/**
	 * The kind of package to create.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public int getPackageKind()
	{
		return m_PackageKind;
	}

	/**
	 * The kind of package to create.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public void setPackageKind(int value)
	{
		m_PackageKind = value;
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

}



