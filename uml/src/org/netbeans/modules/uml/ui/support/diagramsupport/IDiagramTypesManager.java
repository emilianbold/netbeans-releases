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
