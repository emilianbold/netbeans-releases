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

package org.netbeans.modules.uml.core.roundtripframework.codegeneration;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;

public interface IInsertionPointLocator
{
	/**
	 * Gets / Sets Language
	*/
	public String getLanguage();

	/**
	 * Gets / Sets Language
	*/
	public void setLanguage( String value );

	/**
	 * returns a file location where an class' attribute code should be inserted
	*/
	public int getAttributeInsertionPoint( IClassifier pClassifier, IAttribute pAttribute, ISourceFileArtifact pArtifact );

	/**
	 * returns a file location where an class' operation code should be inserted
	*/
	public int getOperationInsertionPoint( IClassifier pClassifier, IOperation pOperation, ISourceFileArtifact pArtifact );

	/**
	 * returns a file location where an class' nested classifier code should be inserted
	*/
	public int getNestedClassInsertionPoint( IClassifier pOuterClassifier, IClassifier pNestedClassifier, ISourceFileArtifact pArtifact );

	/**
	 * returns a file location where a package statement should be inserted
	*/
	public int getPackageStatementInsertionPoint( IClassifier pClassifer, ISourceFileArtifact pArtifact );

	/**
	 * returns a file location where a dependency statement should be inserted
	*/
	public int getDependencyStatementInsertionPoint( IClassifier pClassifer, ISourceFileArtifact pArtifact );

}
