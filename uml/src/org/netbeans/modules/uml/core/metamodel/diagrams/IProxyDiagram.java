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


package org.netbeans.modules.uml.core.metamodel.diagrams;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public interface IProxyDiagram
{
   public DiagramDetails getDiagramDetails();

	/**
	 * Get/Set the name of this drawing.
	*/
	public String getName();

	/**
	 * Get/Set the name of this drawing.
	*/
	public void setName( String value );

	/**
	 * Get/Set the alias of this drawing.
	*/
	public String getAlias();

	/**
	 * Get/Set the alias of this drawing.
	*/
	public void setAlias( String value );

	/**
	 * Sets / Gets the name or alias of this element.
	*/
	public String getNameWithAlias();

	/**
	 * Sets / Gets the name or alias of this element.
	*/
	public void setNameWithAlias( String value );

	/**
	 * Retrieves the fully qualified name of the element. This will be in the form 'A::B::C'.
	*/
	public String getQualifiedName();

	/**
	 * Get/Set the documentation for this drawing.
	*/
	public String getDocumentation();

	/**
	 * Get/Set the documentation for this drawing.
	*/
	public void setDocumentation( String value );

	/**
	 * Gets the drawing area namespace
	*/
	public INamespace getNamespace();

	/**
	 * Gets the drawing area namespace
	*/
	public void setNamespace( INamespace value );

	/**
	 * Gets the drawing area namespace XMIID
	*/
	public String getNamespaceXMIID();

	/**
	 * Gets the diagram XMIID
	*/
	public String getXMIID();

	/**
	 * Get/Set the filename that this proxy diagram is looking at
	*/
	public String getFilename();

	/**
	 * Get/Set the filename that this proxy diagram is looking at
	*/
	public void setFilename( String value );

	/**
	 * Get/Set the type of this drawing.
	*/
	public int getDiagramKind();

	/**
	 * Get/Set the type of this drawing as a string.
	*/
	public String getDiagramKindName();

	/**
	 * Gets the drawing area toplevel project
	*/
	public IProject getProject();

	/**
	 * Returns the IDiagram that represents this proxy.  If the diagram is close NULL is returned.
	*/
	public IDiagram getDiagram();

	/**
	 * Is this diagram open?
	*/
	public boolean isOpen();

	/**
	 * Returns true if bDiagramFilename represents a valid filename.  It looks for both .etp and .etl files.
	*/
	public boolean isValidDiagram();

	/**
	 * Are these proxy diagrams the same?
	*/
	public boolean isSame( IProxyDiagram pProxy );

	/**
	 * Is this diagram readonly?
	*/
	public boolean getReadOnly();

	/**
	 * Adds an associated diagram
	*/
	public void addAssociatedDiagram( String sDiagramXMIID );

	/**
	 * Adds an associated diagram
	*/
	public void addAssociatedDiagram( IProxyDiagram pDiagram );

	/**
	 * Adds an association between diagram 1 and 2 and 2 and 1
	*/
	public void addDualAssociatedDiagrams( IProxyDiagram pDiagram1, IProxyDiagram pDiagram2 );

	/**
	 * Removes an associated diagram
	*/
	public void removeAssociatedDiagram( String sDiagramXMIID );

	/**
	 * Removes an associated diagram
	*/
	public void removeAssociatedDiagram( IProxyDiagram pDiagram );

	/**
	 * Removes an association between diagram 1 and 2 and 2 and 1
	*/
	public void removeDualAssociatedDiagrams( IProxyDiagram pDiagram1, IProxyDiagram pDiagram2 );

	/**
	 * Returns the associated diagrams
	*/
	public ETList<IProxyDiagram> getAssociatedDiagrams();

	/**
	 * Is this an associated diagram?
	*/
	public boolean isAssociatedDiagram( String sDiagramXMIID );

	/**
	 * Is this an associated diagram?
	*/
	public boolean isAssociatedDiagram( IProxyDiagram pDiagram );

	/**
	 * Adds an associated model element
	*/
	public void addAssociatedElement( String sTopLevelElementXMIID, String sModelElementXMIID );

	/**
	 * Adds an associated model element
	*/
	public void addAssociatedElement( IElement pElement );

	/**
	 * Removes an associated model element
	*/
	public void removeAssociatedElement( String sTopLevelElementXMIID, String sModelElementXMIID );

	/**
	 * Removes an associated model element
	*/
	public void removeAssociatedElement( IElement pElement );

	/**
	 * Returns the associated model elements
	*/
	public ETList<IElement> getAssociatedElements();

	/**
	 * Is this an associated element?
	*/
	public boolean isAssociatedElement( String sModelElementXMIID );

	/**
	 * Is this an associated element?
	*/
	public boolean isAssociatedElement( IElement pElement );

	//needed for showing the element properly in navigation dialog
	public String toString();
}
