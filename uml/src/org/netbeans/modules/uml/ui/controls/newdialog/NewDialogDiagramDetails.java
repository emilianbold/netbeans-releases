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


/*
 * Created on Jul 15, 2003
 *
 */
package org.netbeans.modules.uml.ui.controls.newdialog;

import java.util.Vector;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;

/**
 * @author sumitabhk
 *
 */
public class NewDialogDiagramDetails implements INewDialogDiagramDetails
{
	private String m_Name;
	private INamespace m_Namespace;
	private Vector<IElement> m_AdditionalNamespaces;
	private int /*DiagramKind*/ m_DiagramKind;
	private int m_lAvailableDiagramKinds;

	/**
	 * 
	 */
	public NewDialogDiagramDetails()
	{
		super();
		m_DiagramKind = IDiagramKind.DK_UNKNOWN;
		m_lAvailableDiagramKinds = IDiagramKind.DK_ALL;
	}

	/**
	 * Name of the diagram.
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
	 * Name of the diagram.
	 *
	 * @param newVal[in]
	 *
	 * @return HRESULT
	 */
	public void setName(String value)
	{
		m_Name = value;
	}

	/**
	 * The selected namespace.
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
	 * The selected namespace.
	 *
	 * @param pVal[in]
	 *
	 * @return HRESULT
	 */
	public void setNamespace(INamespace value)
	{
		m_Namespace = value;
	}

	/**
	 * Adds an additional namespace to our list of possible namespaces.
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
	 * The selected diagram kind.
	 *
	 * @param pVal[out]
	 *
	 * @return HRESULT
	 */
	public int getDiagramKind()
	{
		return m_DiagramKind;
	}

	/**
	 * The selected diagram kind.
	 *
	 * @param pVal[in]
	 *
	 * @return HRESULT
	 */
	public void setDiagramKind(int value)
	{
		m_DiagramKind = value;
	}

	/**
	 * The diagram kinds that are available for selection in the dialog.
	 *
	 * @param pVal[out]
	 *
	 * @return HRESULT
	 */
	public int getAvailableDiagramKinds()
	{
		return m_lAvailableDiagramKinds;
	}

	/**
	 * The diagram kinds that are available for selection in the dialog.
	 *
	 * @param pVal[in]
	 *
	 * @return HRESULT
	 */
	public void setAvailableDiagramKinds(int value)
	{
		m_lAvailableDiagramKinds = value;
	}

}




