/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

public interface IRelationProxy
{
	/**
	 * Sets / Gets the from element in this proxy.
	*/
	public IElement getFrom();

	/**
	 * Sets / Gets the from element in this proxy.
	*/
	public void setFrom( IElement value );

	/**
	 * Sets / Gets the to element in this proxy.
	*/
	public IElement getTo();

	/**
	 * Sets / Gets the to element in this proxy.
	*/
	public void setTo( IElement value );

	/**
	 * Sets gets the element that performs the connection between the two elements.
	*/
	public IElement getConnection();

	/**
	 * If the connection is 0 then this is the type of connection that should be verified.
	*/
	public String getConnectionElementType();

	/**
	 * If the connection is 0 then this is the type of connection that should be verified.
	*/
	public void setConnectionElementType( String value );

	/**
	 * If used for validation this returns true if the relation has been validated.
	*/
	public boolean getRelationValidated();

	/**
	 * If used for validation this returns true if the relation has been validated.
	*/
	public void setRelationValidated( boolean value );

	/**
	 * Sets gets the element that performs the connection between the two elements.
	*/
	public void setConnection( IElement value );

	/**
	 * Determines whether or not this proxy contains the elements passed in.
	*/
	public boolean matches( IElement From, IElement To, IElement Connection );

	/**
	 * Retrieves the from element dictated by the Connection type. If Connection returns 0, so will this method.
	*/
	public IElement getRelationFrom();

	/**
	 * Retrieves the from element dictated by the Connection type. If Connection returns 0, so will this method.
	*/
	public IElement getRelationTo();

	/**
	 * Retrieves the element that physically owns the Connection type. If Connection returns 0, so will this method.
	*/
	public IElement getRelationOwner();
        
        /**
         * This flag indicates whether this relationship is a reconnected one. 
         * This flag is useful when used with validation. It relaxes the validation process 
         * if  it's a reconnected relationship.
         * @param val
         */
        public void setReconnectionFlag (boolean val);
        
        /**
         * Checks if this relationship is a connected one.
         * @return true if this relationship is a reconnected one; false otherwise.
         */
        public boolean isReconnected ();

}
