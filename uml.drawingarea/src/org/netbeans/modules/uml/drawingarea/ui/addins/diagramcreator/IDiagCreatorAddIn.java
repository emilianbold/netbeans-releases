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

//	 Date:: Oct 27, 2003 5:19:49 PM
package org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator; 

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;

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
	 * Creates a diagram from the input elements
	*/
	public long guiCreateDiagramFromElements( ETList<IElement> pElements, IElement pParentElement, IProjectTreeModel pProjectTreeModel );

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
