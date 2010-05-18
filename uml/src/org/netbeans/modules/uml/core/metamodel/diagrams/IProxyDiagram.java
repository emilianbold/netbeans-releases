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
