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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;

public interface IFeature extends IRedefinableElement
{
	/**
	 * Retrieves the fully qualified name of the element. Project name is never included.  This will be in the form 'A::B::C'.
	*/
	public String getQualifiedName2();

	/**
	 * Specifies if the Feature is defined for the Classifier (true) or for the Instances of the Classifier (false). The default value is false.
	*/
	public boolean getIsStatic();

	/**
	 * Specifies if the Feature is defined for the Classifier (true) or for the Instances of the Classifier (false). The default value is false.
	*/
	public void setIsStatic( boolean value );

	/**
	 * The Classifier declaring the Feature.
	*/
	public IClassifier getFeaturingClassifier();

	/**
	 * The Classifier declaring the Feature.
	*/
	public void setFeaturingClassifier( IClassifier value );

	/**
	 * Moves this Feature to the passed in Classifier.
	*/
	public void moveToClassifier( IClassifier destination );

	/**
	 * Duplicates this Feature, then adds it to the passed in Classifier.
	*/
	public IFeature duplicateToClassifier( IClassifier destination );

}
