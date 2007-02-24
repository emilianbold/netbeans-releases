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

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ILanguageCodeGenerator
{
	/**
	 * Called when an operation is created
	*/
	public long onOperationCreated( ICGOperationChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when operation is modified
	*/
	public long onOperationModified( ICGOperationChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when an operation is deleted
	*/
	public long onOperationDeleted( ICGOperationChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when an attribute is created
	*/
	public long onAttributeCreated( ICGAttributeChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when an attribute is modified
	*/
	public long onAttributeModified( ICGAttributeChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when an attribute is deleted
	*/
	public long onAttributeDeleted( ICGAttributeChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when an attribute is moved from one classifier to another
	*/
	public long onAttributeMoved( ICGAttributeMovementChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when an operation is moved from one classifier to another
	*/
	public long onOperationMoved( ICGOperationMovementChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a package is modified
	*/
	public long onPackageModified( ICGPackageChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a package is created
	*/
	public long onPackageCreated( ICGPackageChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a package is deleted
	*/
	public long onPackageDeleted( ICGPackageChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a dependency is created
	*/
	public long onDependencyCreated( ICGDependencyChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a dependency is deleted
	*/
	public long onDependencyDeleted( ICGDependencyChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a classifier is created
	*/
	public long onClassifierCreated( ICGClassChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a classifier is modified
	*/
	public long onClassifierModified( ICGClassChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a classifier is deleted
	*/
	public long onClassifierDeleted( ICGClassChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a classifier's namespace is changed
	*/
	public long onClassifierNamespaceChanged( ICGPackageChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when an element is added to a namespace 
	*/
	public long onElementAddedToNamespace( ICGChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

	/**
	 * Called when a classifier's nesting changes 
	*/
	public long onClassifierNestingChange( ICGClassNestingChangeNotification pChangeNotification, ETList<ICodeGenerationChangeRequest> pCGRequests );

}
