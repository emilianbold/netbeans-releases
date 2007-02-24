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



package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;

/**
 * @author KevinM
 *
 */
public class ActivityRelationFactory extends RelationFactory implements IActivityRelationFactory {

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityRelationFactory#createEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode, org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode, org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity)
	 */
	public IMultiFlow createEdge(IActivityNode from, IActivityNode to, IActivity activity) {
		IMultiFlow flow = null;
		try {
			IActivity pParentActivity = activity instanceof IActivity ? (IActivity) activity : null;

			if (pParentActivity == null && from != null) {
				
				// The user past NULL for the parent so use the from node as the parent activity
				pParentActivity = (IActivity) OwnerRetriever.getOwnerByType(from,IActivity.class);
			}

			TypedFactoryRetriever < IMultiFlow > factory = new TypedFactoryRetriever < IMultiFlow > ();

			flow = factory.createType("MultiFlow");

			if (flow != null && pParentActivity != null) {
				// _VH( flow.CopyTo( newEdge ));

				RelationshipEventsHelper helper = new RelationshipEventsHelper(flow);

				if (helper.firePreRelationCreated(from, to)) {
					flow.setSource(from);
					flow.setTarget(to);

					// Now add the edge to the activity

					pParentActivity.addEdge(flow);

					helper.fireRelationCreated();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return flow;
	}

}
