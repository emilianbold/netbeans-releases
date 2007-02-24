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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public interface IJavaRequestProcessor
        extends
            IRequestProcessor,
            INamedElementEventsSink,
            IFeatureEventsSink,
            IBehavioralFeatureEventsSink,
            IClassifierFeatureEventsSink,
            IRelationValidatorEventsSink,
            IRedefinableElementModifiedEventsSink,
            IClassifierTransformEventsSink,
            IElementLifeTimeEventsSink,
            IAffectedElementEventsSink,
            IEventFrameworkEventsSink,
            INamespaceModifiedEventsSink,
            IAttributeEventsSink,
            ITypedElementEventsSink
{
    public ETList<IChangeRequest> processRequests(ETList<IChangeRequest> inputRequests);
    public void initialize(IRoundTripController controller);
    public void deInitialize(IRoundTripController controller);
    public String getLanguage();
    public ILanguage getLanguage2();
}