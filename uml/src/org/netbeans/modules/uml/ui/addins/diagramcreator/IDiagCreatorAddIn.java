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

//	 Date:: Oct 27, 2003 5:19:49 PM
package org.netbeans.modules.uml.ui.addins.diagramcreator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;

public interface IDiagCreatorAddIn
{
   public static class CRB // ChildRetrievalBehavior
   {
      /// Do not get any child model elements and add them to the diagram.
      public final static int NONE = 0;
      /// Add child elements to the diagram.
      public final static int GET_ALL = 1;
      /// Add child elements for States to the diagram.
      public final static int GET_STATE_CHILDREN = 2;
   }
   
	/**
	 * Creates a diagram from the selected elements in the project tree.
	*/
	public long guiCreateDiagramFromProjectTreeElements( IProjectTreeControl pProjectTree );

	/**
	 * Creates a diagram from the input elements
	*/
	public long guiCreateDiagramFromElements( ETList<IElement> pElements, IElement pParentElement, IProjectTreeControl pProjectTree );

	/**
	 * Creates the specified diagram, and adds the input elements to the diagram.
	*/
	public IDiagram createDiagramForElements( /* DiagramKind */ int diagramKind, INamespace pNamespace, String sDiagramName, ETList<IElement> pElements, IDiagram pCreatedDiagram );

	/**
	 * Creates the specified diagram, and adds the input elements to the diagram when the diagram is next opened.
	*/
	public IProxyDiagram createStubDiagramForElements( /* DiagramKind */ int diagramKind, INamespace pNamespace, String sDiagramName, ETList<IElement> pElements );

	/**
	 * Creates the specified diagram, and adds the input XMIIDs as elements to the diagram when the diagram is next opened.
	*/
	public IProxyDiagram createStubDiagramForXMIIDs( String sDiagramKind, INamespace pNamespace, String sDiagramName, String sProjectXMIID, IStrings pXMIIDsToCDFS, IStrings pXMIIDsForNavigationOnly );

	/**
	 * Adds the input elements to the input diagram.
	*/
	public long addElementsToDiagram( IDiagram pDiagram, ETList<IElement> pElements, IElement pParentElement, /* ChildRetrievalBehavior */ int nChildRetrievalBehavior );

}
