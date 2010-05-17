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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

public interface ILanguageDataType
{
	/**
	 * The name of the data type.
	*/
	public String getName();

	/**
	 * The name of the data type.
	*/
	public void setName( String value );

	/**
	 * The data type's kind.  The data kinds will usally be "primitive" or "user-defined".
	*/
	public int getKind();

	/**
	 * The data type's kind.  The data kinds will usally be "primitive" or "user-defined".
	*/
	public void setKind( /* DataTypeKind */ int value );

	/**
	 * The scope of the data type.  The value of scope will be "global" when the data type applies to all Describe projects, or a list of projects that applies to the data type.
	*/
	public String getScope();

	/**
	 * The scope of the data type.  The value of scope will be "global" when the data type applies to all Describe projects, or a list of projects that applies to the data type.
	*/
	public void setScope( String value );

	/**
	 * Specifies that the Data Type is the default value for attributes.
	*/
	public boolean getIsDefaultAttributeType();

	/**
	 * Specifies that the Data Type is the default value for attributes.
	*/
	public void setIsDefaultAttributeType( boolean value );

	/**
	 * Specifies that the Data Type is the default value for operations.
	*/
	public boolean getIsOperationDefaultType();

	/**
	 * Specifies that the Data Type is the default value for operations.
	*/
	public void setIsOperationDefaultType( boolean value );

	/**
	 * The default value of data type.
	*/
	public String getDefaultValue();

	/**
	 * The default value of data type.
	*/
	public void setDefaultValue( String value );

	/**
	 * The language independent name of the data type.  Only primitives have a UML name.
	*/
	public String getUMLName();

	/**
	 * The language independent name of the data type.  Only primitives have a UML name.
	*/
	public void setUMLName( String value );

}
