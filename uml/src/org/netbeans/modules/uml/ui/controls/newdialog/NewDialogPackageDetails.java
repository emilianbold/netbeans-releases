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



