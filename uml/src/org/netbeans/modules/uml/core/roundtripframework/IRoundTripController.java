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
