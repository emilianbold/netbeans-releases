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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;

public interface IParameter extends INamedElement, ITypedElement
{
	/**
	 * Sets / Gets the direction flag on the parameter, indicating the semantics of how that data the parameter represents is entering the behavior.
	*/
	public int getDirection();

	/**
	 * Sets / Gets the direction flag on the parameter, indicating the semantics of how that data the parameter represents is entering the behavior.
	*/
	public void setDirection( /* ParameterDirectionKind */ int value );

	/**
	 * Sets / Gets the expression that holds the default initialization for the parameter.
	*/
	public IExpression getDefault();

	/**
	 * Sets / Gets the expression that holds the default initialization for the parameter.
	*/
	public void setDefault( IExpression exp );

	/**
	 * Sets / Gets the name of the Parameter.
	*/
	public String getName();

	/**
	 * Sets / Gets the name of the Parameter.
	*/
	public void setName( String value );

	/**
	 * Set the type via a name that will be resolved into a Classifier.
	*/
	public void setType2( String value );

	/**
	 * Retrieves the BehavioralFeature this parameter is a part of.
	*/
	public IBehavioralFeature getBehavioralFeature();

	/**
	 * Retrieves the Behavior this parameter is a part of.
	*/
	public IBehavior getBehavior();

	/**
	 * The name of the Classifier who specifies this Parameter's type.
	*/
	public String getTypeName();

	/**
	 * The name of the Classifier who specifies this Parameter's type.
	*/
	public void setTypeName( String value );

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public String getDefault2();

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault2( String value );

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public String getDefault3();

	/**
	 * The default parameter initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault3( String lang, String body );

	/**
	 * Specifies extra semantics associated with the Parameter.
	*/
	public int getParameterKind();

	/**
	 * Specifies extra semantics associated with the Parameter.
	*/
	public void setParameterKind( /* ParameterSemanticsKind */ int value );
	
	public IVersionableElement performDuplication();
	
}
