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


package org.netbeans.modules.uml.core.metamodel.structure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IArtifact extends IClassifier
{
	/**
	 * method AddImplementedElement
	*/
	public void addImplementedElement( INamedElement comp );

	/**
	 * method RemoveImplementedElement
	*/
	public void removeImplementedElement( INamedElement comp );

	/**
	 * property ImplementedElements
	*/
	public ETList<INamedElement> getImplementedElements();

	/**
	 * method AddDeployment
	*/
	public void addDeployment( IDeployment dep );

	/**
	 * method RemoveDeployment
	*/
	public void removeDeployment( IDeployment dep );

	/**
	 * property Deployments
	*/
	public ETList<IDeployment> getDeployments();

	/**
	 * property Content
	*/
	public IDeploymentSpecification getContent();

	/**
	 * property Content
	*/
	public void setContent( IDeploymentSpecification value );

	/**
	 * The absolute path to the source file.
	*/
	public String getFileName();

	/**
	 * The absolute path to the source file.
	*/
	public void setFileName( String value );

}
