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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

public interface ICodeGenerationScript
{
	/**
	 * The name of the script.  The name of the script can be thought of as the script type.
	*/
	public String getName();

	/**
	 * The name of the script.  The name of the script can be thought of as the script type.
	*/
	public void setName( String name );

	/**
	 * The name of the script file that is to used by the script to determine the code to generate.
	*/
	public String getFile();

	/**
	 * The name of the script file that is to used by the script to determine the code to generate.
	*/
	public void setFile( String fileName );

	/**
	 * Executes the script and returns the code that represents the specified data.
	*/
	public String execute( IElement pElement );

	/**
	 * The Language object this script is owned by.
	*/
	public ILanguage getLanguage();

	/**
	 * The Language object this script is owned by.
	*/
	public void setLanguage( ILanguage language );

}
