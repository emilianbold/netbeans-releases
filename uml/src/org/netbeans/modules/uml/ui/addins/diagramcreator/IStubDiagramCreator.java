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

package org.netbeans.modules.uml.ui.addins.diagramcreator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;

/**
 * @author sumitabhk
 *
 */
public interface IStubDiagramCreator
{
	public IProxyDiagram createDiagram( int diagramKind, INamespace pNamespace,
						   				String sDiagramName, ETList<IElement> pElements);

	public IProxyDiagram createDiagram( String sDiagramKind, INamespace pNamespace,
						   				String sDiagramName, String sProjectXMIID,
						   				IStrings pXMIIDsToCDFS, IStrings pXMIIDsForNavigationOnly);

	public void createStubETLDFile(String sDiagramFullFilename);

	public void createStubETLPFile(int diagramKind, INamespace pNamespace,
							   	   String sDiagramName, String sDiagramFullFilename,
							   	   ETList<IElement> pElements);

	public IProductArchive createEmptyStubETLPFile(String sDiagramKind, INamespace pNamespace,
									 			String sDiagramName, String bsDiagramFullFilename);

	public void insert( IProductArchive pArchive, String bsTopLevelXMIID,
						IStrings pXMIIDs, boolean bIgnoreForCDFS );

	public void insert( IProductArchive pArchive, String bsTopLevelXMIID,
						String bsXMIID, boolean bIgnoreForCDFS );

}


