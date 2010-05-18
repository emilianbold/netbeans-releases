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
