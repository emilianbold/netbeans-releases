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

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IREOperation extends IREClassFeature, IREHasMultiplicity
{
	/**
	 * Specifies whether the operation must be defined by a descendent. True indicates that the operation must be defined by a descendent. False indicates that a descendent is not required to define the operation.
	*/
	public boolean getIsAbstract();

	/**
	 * Retrieves the operations parameters
	*/
	public ETList<IREParameter> getParameters();

	/**
	 * Specifies if the attribute is a primitive attribute or an object instance.
	*/
	public boolean getIsPrimitive();

	/**
	 * Specifies if the operation is a constructor of the owner class.
	*/
	public boolean getIsConstructor();

	/**
	 * Creates a data-wise clone of this IREOperation and returns it in pOperation (an IOperation object).  Does not add pOperation to pClassifier (an IClassifier object).
	*/
	public IOperation clone( IClassifier pClassifier );

	/**
	 * Returns a list of names of Exceptions that this operation may raise.
	*/
	public IStrings getRaisedExceptions();

	/**
	 * Specific to the Java language.
	*/
	public boolean getIsStrictFP();

	/**
	 * Specific to the Java language.
	*/
	public boolean getIsNative();

	/**
	 * property Concurrency
	*/
	public int getConcurrency();
    
    public ETList<IREMultiplicityRange> getMultiplicity();
 
    public void setMultiplicity(ETList<IREMultiplicityRange> mul);
}