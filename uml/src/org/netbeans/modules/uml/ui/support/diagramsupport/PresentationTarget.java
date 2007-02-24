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


