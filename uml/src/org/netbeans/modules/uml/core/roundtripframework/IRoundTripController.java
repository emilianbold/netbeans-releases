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

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;

public interface IRoundTripController extends IDocumentationModifiedEventsSink,
                                                IElementLifeTimeEventsSink,
                                                INamedElementEventsSink,
                                                INamespaceModifiedEventsSink,
                                                IRelationValidatorEventsSink,
                                                IRelationEventsSink,
                                                IAttributeEventsSink,
                                                IBehavioralFeatureEventsSink,
                                                IClassifierFeatureEventsSink,
                                                IFeatureEventsSink,
                                                IOperationEventsSink,
                                                IParameterEventsSink,
                                                IStructuralFeatureEventsSink,
                                                ITypedElementEventsSink,
                                                IClassifierTransformEventsSink,
                                                IAssociationEndTransformEventsSink,
                                                IRedefinableElementModifiedEventsSink,
                                                IAffectedElementEventsSink,
                                                IEventFrameworkEventsSink,
                                                IWSProjectEventsSink,
                                                IPackageEventsSink                                                        
{
	/**
	 * Sets / Gets the mode of this controller.
	*/
	public int getMode();

	/**
	 * Sets / Gets the mode of this controller.
	*/
	public void setMode( /* RTMode */ int mode );

	/**
	 * Sets / Gets the dispatch controller resident on the RoundTripController.
	*/
	public IEventDispatchController getEventDispatchController();

	/**
	 * Sets / Gets the dispatch controller resident on the RoundTripController.
	*/
	public void setEventDispatchController( IEventDispatchController controller );

	/**
	 * Initializes the controller, setting its initial mode and establishing sink registration.
	*/
	public void initialize( ICoreProduct prod, /* RTMode */ int mode );

	/**
	 * Retrieves the dispatcher responsible for the round trip events.
	*/
	public IRoundTripEventDispatcher getRoundTripDispatcher();

	/**
	 * Retrieves the dispatcher responsible for the classifier change events.
	*/
	public IClassifierEventDispatcher getClassifierDispatcher();

	/**
	 * Retrieves the dispatcher responsible for the element lifetime events.
	*/
	public IElementLifeTimeEventDispatcher getElementLifeTimeDispatcher();

	/**
	 * Retrieves the dispatcher responsible for the element change events.
	*/
	public IElementChangeEventDispatcher getElementChangeDispatcher();

	/**
	 * Retrieves the dispatcher responsible for the relation validation events.
	*/
	public IRelationValidatorEventDispatcher getRelationValidatorDispatcher();

	/**
	 * DeInitializes the controller.
	*/
	public void deInitialize();

}
