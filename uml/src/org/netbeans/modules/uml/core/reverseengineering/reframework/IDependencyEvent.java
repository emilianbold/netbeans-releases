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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

public interface IDependencyEvent extends IParserData
{
	/**
	 * Retrieves the supplier of the dependency.  The supplier will be specified using the UML fully scoped name of the supplier model element.
	*/
	public String getSupplier();

	/**
	 * Retrieves the client of the dependency.  The client will be specified using the UML fully scoped name of the supplier model element.
	*/
	public String getClient();

	/**
	 * Determines if the dependency is a package dependency or a class dependency.
	*/
	public boolean getIsClassDependency();
        
        public boolean isStaticDependency();

	/**
	 * Retrieves the package name that is the reciever of the dependency.  If the dependency is a class dependency then the package name is the package that contains the class.
	*/
	public String getSupplierPackage();

	/**
	 * Retrieves the name of the class that is the reciever of the dependency.  The class name property is only valid when the dependeny is a class dependency.
	*/
	public String getSupplierClassName();

	/**
	 * Test if the specified class name is the same as the supplier of the dependency.  This method is only valid if the dependency is a class dependency.
	*/
	public boolean isSameClass( String className );

}
