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
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;

public interface IAttribute extends IStructuralFeature, IParameterableElement
{
	/**
	 * Specifies whether the Attribute is derived, i.e. its value or values can be computed from other information. The default value is false.
	*/
	public boolean getIsDerived();

	/**
	 * Specifies whether the Attribute is derived, i.e. its value or values can be computed from other information. The default value is false.
	*/
	public void setIsDerived( boolean value );

	/**
	 * References an optional expression specifying how to set the attribute when creating an instance in the absence of a specific setting for the attribute.
	*/
	public IExpression getDefault();

	/**
	 * References an optional expression specifying how to set the attribute when creating an instance in the absence of a specific setting for the attribute.
	*/
	public void setDefault( IExpression value );

	/**
	 * property DerivationRule
	*/
	public IExpression getDerivationRule();

	/**
	 * property DerivationRule
	*/
	public void setDerivationRule( IExpression value );

	/**
	 * property AssociationEnd
	*/
	public IAssociationEnd getAssociationEnd();

	/**
	 * property AssociationEnd
	*/
	public void setAssociationEnd( IAssociationEnd value );

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public String getDefault2();

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault2( String value );

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public ETPairT<String,String> getDefault3();

	/**
	 * The default attribute initializer. Easy access to the body property of the Expression.
	*/
	public void setDefault3( String lang, String body );

	/**
	 * Determines whether or not this attribute has the WithEvents modifier associated with it. This is specific to the VB programming language.
	*/
	public boolean getIsWithEvents();

	/**
	 * Determines whether or not this attribute has the WithEvents modifier associated with it. This is specific to the VB programming language.
	*/
	public void setIsWithEvents( boolean value );

	/**
	 * Indicates whether or not the attribute instance is created on the heap or not upon the instanciation of the featuring classifier.
	*/
	public boolean getHeapBased();

	/**
	 * Indicates whether or not the attribute instance is created on the heap or not upon the instanciation of the featuring classifier.
	*/
	public void setHeapBased( boolean value );

	/**
	 * Indicates whether or not this attribute maps to a primary key column in a database.
	*/
	public boolean getIsPrimaryKey();

	/**
	 * Indicates whether or not this attribute maps to a primary key column in a database.
	*/
	public void setIsPrimaryKey( boolean value );

}
