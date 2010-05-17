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


package org.netbeans.modules.uml.ui.support.diagramsupport;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;

/**
 * @author sumitabhk
 *
 *
 */
public class PresentationTarget implements IPresentationTarget
{
	/// The XML id of the presentation element
	private String m_PresentationId = "";
	
	/// The full filename of the diagram that contains this presentation element
	private String m_DiagramFilename = "";
	
	/// IF the diagram is open, then here's the open diagram.
	private IDiagram m_Diagram = null;
	
	/// The XML id of the model element (usually "", only used if a diagram has been upgraded and not yet opened)
	private String m_MEID = "";
	
	/// The XML id of the model element (usually "", only used if a diagram has been upgraded and not yet opened)
	private String m_TopLevelID = "";
	
	/**
	 * 
	 */
	public PresentationTarget()
	{
		super();
	}

	/**
	 * Gets the diagram filename that has a presentation element that represents the queried model element.
	 *
	 * @param pVal[out,retval] The full path filename of the diagram (etld or etlp)
	 */
	public String getDiagramFilename()
	{
		return m_DiagramFilename;
	}

	/**
	 * Puts the diagram filename that has a presentation element that represents the queried model element.
	 *
	 * @param newVal[in] The full path filename of the diagram (etld or etlp)
	 */
	public void setDiagramFilename(String value)
	{
		m_DiagramFilename = value;
	}

	/**
	 * Gets the xml id of the presentation element that was found matching the model element queried.
	 *
	 * @param pVal[out,retval] The xmiid of the presentation element inside this diagram
	 */
	public String getPresentationID()
	{
		return m_PresentationId;
	}

	/**
	 * Sets the xml id of the presentation element that was found matching the model element queried.
	 *
	 * @param pVal[in] The xmiid of the presentation element inside this diagram
	 */
	public void setPresentationID(String value)
	{
		m_PresentationId = value;
	}

	/**
	 * Gets the open diagram, if there is one, that the presentation element resides on.
	 *
	 * @param pVal[out,retval] If the diagram is open, this is the open diagram, otherwise NULL
	 */
	public IDiagram getOpenDiagram()
	{
		return m_Diagram;
	}

	/**
	 * Sets the open diagram, if there is one, that the presentation element resides on.
	 *
	 * @param newVal[in] If the diagram is open, this is the open diagram
	 */
	public void setOpenDiagram(IDiagram value)
	{
		m_Diagram = value;
	}

	/**
	 * Returns the proxy diagram.
	 *
	 * @param pVal[out,retval] The proxy diagram.  This could be a closed or opened diagram.
	 */
	public IProxyDiagram getProxyDiagram()
	{
		IProxyDiagram retDia = null;
		if (m_DiagramFilename != null && m_DiagramFilename.length() > 0)
		{
			IProxyDiagramManager mgr = ProxyDiagramManager.instance();
			retDia = mgr.getDiagram(m_DiagramFilename);
		}
		return retDia;
	}

	/**
	 * The XML id of the model element (usually "", only used if a diagram has been upgraded and not yet opened)
	 *
	 * @param pVal[out,retval] The xmiid of the model element inside this diagram
	 */
	public String getModelElementID()
	{
		return m_MEID;
	}

	/**
	 * The XML id of the model element (usually "", only used if a diagram has been upgraded and not yet opened)
	 *
	 * @param pVal[in] The xmiid of the model element inside this diagram
	 */
	public void setModelElementID(String newVal)
	{
		m_MEID = newVal;
	}

	/**
	 * The TopLevel XML id of the model element (usually "", only used if a diagram has been upgraded and not yet opened)
	 *
	 * @param pVal[in] The xmiid of the model element inside this diagram
	 */
	public void setTopLevelID(String newVal)
	{
		m_TopLevelID = newVal;
	}

	/**
	 * The TopLevel XML id of the model element (usually "", only used if a diagram has been upgraded and not yet opened)
	 *
	 * @param pVal[out,retval] The xmiid of the model element inside this diagram
	 */
	public String getTopLevelID()
	{
		return m_TopLevelID;
	}
	
	public String toString()
	{
		String retStr = "";
		IProxyDiagram dia = getProxyDiagram();
		if (dia != null)
		{
			retStr = dia.getName();
		}
		return retStr;
	}

}


