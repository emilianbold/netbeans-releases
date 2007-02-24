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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;

public interface ICGPackageChangeNotification extends ICGSingleClassChangeNotification
{
	/**
	 * Gets / Sets Old Package Name
	*/
	public String getOldPackageName();

	/**
	 * Gets / Sets Old Package Name
	*/
	public void setOldPackageName( String value );

	/**
	 * Gets / Sets New Package Name
	*/
	public String getNewPackageName();

	/**
	 * Gets / Sets New Package Name
	*/
	public void setNewPackageName( String value );

	/**
	 * Gets / Sets Modified Package
	*/
	public String getModifiedPackage();

	/**
	 * Gets / Sets Modified Package
	*/
	public void setModifiedPackage( String value );

	/**
	 * Gets / Sets the Old Package
	*/
	public void setOldPackage( IPackage value );

	/**
	 * Gets / Sets the Old Package
	*/
	public IPackage getOldPackage();

	/**
	 * Gets / Sets the New Package
	*/
	public void setNewPackage( IPackage value );

	/**
	 * Gets / Sets the New Package
	*/
	public IPackage getNewPackage();

	/**
	 * Gets / Sets Old SourceDir property
	*/
	public String getOldSourceDir();

	/**
	 * Gets / Sets Old SourceDir property
	*/
	public void setOldSourceDir( String value );

}
