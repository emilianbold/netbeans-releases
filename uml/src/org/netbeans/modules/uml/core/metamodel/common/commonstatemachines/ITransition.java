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


package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ITransition extends INamedElement
{
	/**
	 * property IsInternal
	*/
	public boolean getIsInternal();

	/**
	 * property IsInternal
	*/
	public void setIsInternal( boolean value );

	/**
	 * property Source
	*/
	public IStateVertex getSource();

	/**
	 * property Source
	*/
	public void setSource( IStateVertex value );

	/**
	 * property Target
	*/
	public IStateVertex getTarget();

	/**
	 * property Target
	*/
	public void setTarget( IStateVertex value );

	/**
	 * property Guard
	*/
	public IConstraint getGuard();

	/**
	 * property Guard
	*/
	public void setGuard( IConstraint value );

	/**
	 * property Effect
	*/
	public IProcedure getEffect();

	/**
	 * property Effect
	*/
	public void setEffect( IProcedure value );

	/**
	 * property Trigger
	*/
	public IEvent getTrigger();

	/**
	 * property Trigger
	*/
	public void setTrigger( IEvent value );

	/**
	 * property PreCondition
	*/
	public IConstraint getPreCondition();

	/**
	 * property PreCondition
	*/
	public void setPreCondition( IConstraint value );

	/**
	 * property PostCondition
	*/
	public IConstraint getPostCondition();

	/**
	 * property PostCondition
	*/
	public void setPostCondition( IConstraint value );

	/**
	 * method AddReferredOperation
	*/
	public void addReferredOperation( IOperation pOper );

	/**
	 * method RemoveReferredOperation
	*/
	public void removeReferredOperation( IOperation pOper );

	/**
	 * property ReferredOperations
	*/
	public ETList<IOperation> getReferredOperations();

	/**
	 * property Container
	*/
	public IRegion getContainer();

	/**
	 * property Container
	*/
	public void setContainer( IRegion value );

	/**
	 * method CreatePreCondition
	*/
	public IConstraint createPreCondition( String condition );

	/**
	 * method CreatePostCondition
	*/
	public IConstraint createPostCondition( String condition );
}
