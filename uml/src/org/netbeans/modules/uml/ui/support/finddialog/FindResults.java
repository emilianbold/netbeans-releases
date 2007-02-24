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


package org.netbeans.modules.uml.ui.support.finddialog;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;

public class FindResults implements IFindResults
{
	private ETList<IElement> m_Elements = new ETArrayList<IElement>();
	private ETList<IProxyDiagram> m_Diagrams = new ETArrayList<IProxyDiagram>();	
	private ETList<IWSProject> m_Projects = new ETArrayList<IWSProject>();	

	public FindResults()
	{
		super();
	}

	public ETList<IElement> getElements() {
		return m_Elements;
	}

	public void setElements(ETList<IElement> value) 
	{
		if (value != null)
		{
			m_Elements = null;
			m_Elements = value;
		}
	}	
	public ETList<IProxyDiagram> getDiagrams() {
		return m_Diagrams;
	}
	public void setDiagrams(ETList<IProxyDiagram> value) 
	{
		if (value != null)
		{
			m_Diagrams = null;
			m_Diagrams = value;
		}
	}	
	public ETList<IWSProject> getProjects() {
		return m_Projects;
	}
	
	public void setProjects(ETList<IWSProject> value) 
	{
		if (value != null)
		{
			m_Projects = null;
			m_Projects = value;
		}
	}	
}
