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

import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IFileInformation
{
	/**
	 * Retrieves the number of dependencies found in a source file.
	*/
	public int getTotalDependencies();

	/**
	 * Adds a new dependency to the source file information.
	*/
	public long addDependency( IDependencyEvent newVal );

	/**
	 * Retrieves the information about one of the dependencies in the soruce file.
	*/
	public IDependencyEvent getDependency( int index );

	/**
	 * Retrieves the number of top level classes found in a source file.
	*/
	public int getTotalClasses();

	/**
	 * Adds a new class to the source file information.
	*/
	public long addClass( IREClass newVal );

	/**
	 * Retrieves the information about one of the top level classes in the soruce file.
	*/
	public IREClass getClass( int index );

	/**
	 * Adds an error event to the file information.
	*/
	public long addError( IErrorEvent e );

	/**
	 * Retrieves the number of errors that occur in the file that was parsed.
	*/
	public int getTotalErrors();

	/**
	 * Retrieves the information about a specific error.
	*/
	public IErrorEvent getError( int index );

	/**
	 * Retrieves all the error that where found in the file.
	*/
	public ETList<IErrorEvent> getErrors();

	/**
	 * Retrieves the dependencies found when parsing the file.
	*/
	public ETList<IDependencyEvent> getDependencies();

	/**
	 * Retrieves the number of packages found in a source file.
	*/
	public int getTotalPackages();

	/**
	 * Adds a new package to the source file information.
	*/
	public long addPackage( IPackageEvent newVal );

	/**
	 * Retrieves the information about one of the packages in the soruce file.
	*/
	public IPackageEvent getPackage( int index );

}
