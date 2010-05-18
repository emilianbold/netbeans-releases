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

public interface IDiagramTypesManager
{
	/**
	 * Returns the diagram kind for this display name (ie Class Diagram to DK_CLASS_DIAGRAM)
	*/
	public int getDiagramKind( String sDiagramTypeName );

	/**
	 * Returns the diagram kind (ie Class Diagram) for the argument diagram
	*/
	public String getDiagramTypeName( IDiagram pDiagram );

	/**
	 * Returns the diagram kind (ie Class Diagram) for the argument enumeration
	*/
	public String getDiagramTypeName( /* DiagramKind */ int nDiagramKind );

	/**
	 * Returns the diagram kind without spaces (ie ClassDiagram) for the argument diagram
	*/
	public String getDiagramTypeNameNoSpaces( IDiagram pDiagram );

	/**
	 * Returns the diagram kind without spaces (ie ClassDiagram) for the argument diagram kind (ie Class Diagram)
	*/
	public String getDiagramTypeNameNoSpaces( String sDiagramTypeName );

	/**
	 * Returns the diagram kind without spaces (ie Class Diagram) for the argument enumeration
	*/
	public String getDiagramTypeNameNoSpaces( /* DiagramKind */ int nDiagramKind );

	/**
	 * Returns short diagram type for this long diagram type (ie Class Diagram to CLD)
	*/
	public String getShortDiagramTypeName( String sDiagramTypeName );

	/**
	 * The UML Type for this diagram (ie StructuralDiagram)
	*/
	public String getUMLType( IDiagram pDiagram );

	/**
	 * The UML Type for this diagram (ie StructuralDiagram)
	*/
	public String getUMLType( String sDiagramTypeName );

	/**
	 * The UML Type for this diagram (ie StructuralDiagram)
	*/
	public String getUMLType( /* DiagramKind */ int nDiagramKind );

	/**
	 * The diagram engine controlling this behavior (ClassDiagram)
	*/
	public String getDiagramEngine( IDiagram pDiagram );

	/**
	 * The diagram engine controlling this behavior (ClassDiagram)
	*/
	public String getDiagramEngine( String sDiagramTypeName );

	/**
	 * The diagram engine controlling this behavior (ClassDiagram)
	*/
	public String getDiagramEngine( /* DiagramKind */ int nDiagramKind );

	/**
	 * The open diagram icon
	*/
	public String getOpenIcon( IDiagram pDiagram );

	/**
	 * The open diagram icon
	*/
	public String getOpenIcon( String sDiagramTypeName );

	/**
	 * The open diagram icon
	*/
	public String getOpenIcon( /* DiagramKind */ int nDiagramKind );

	/**
	 * The closed diagram icon
	*/
	public String getClosedIcon( IDiagram pDiagram );

	/**
	 * The closed diagram icon
	*/
	public String getClosedIcon( String sDiagramTypeName );

	/**
	 * The closed diagram icon
	*/
	public String getClosedIcon( /* DiagramKind */ int nDiagramKind );

	/**
	 * The broken diagram icon
	*/
	public String getBrokenIcon( IDiagram pDiagram );

	/**
	 * The broken diagram icon
	*/
	public String getBrokenIcon( String sDiagramTypeName );

	/**
	 * The broken diagram icon
	*/
	public String getBrokenIcon( /* DiagramKind */ int nDiagramKind );

}
