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

package org.netbeans.modules.uml.integration.finddialog;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;

public interface IFindResults
{
	/**
	 * Array of elements matching the find criteria
	*/
	public ETList<IElement> getElements();

	/**
	 * Array of elements matching the find criteria
	*/
	public void setElements( ETList<IElement> value );

	/**
	 * Array of diagrams matching the find criteria
	*/
	public ETList<IProxyDiagram> getDiagrams();

	/**
	 * Array of diagrams matching the find criteria
	*/
	public void setDiagrams( ETList<IProxyDiagram> value );

	/**
	 * Array of projects matching the find criteria - future use
	*/
	public ETList<IWSProject> getProjects();

	/**
	 * Array of projects matching the find criteria - future use
	*/
	public void setProjects( ETList<IWSProject> value );

}
