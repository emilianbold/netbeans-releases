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

/*
 * File       : StateRelationFactory.java
 * Created on : Sep 19, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;

/**
 * @author Aztec
 */
public class StateRelationFactory implements IStateRelationFactory
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateRelationFactory#createTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex, org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex, org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion)
     */
    public ITransition createTransition(IStateVertex source,
                                        IStateVertex target,
                                        IRegion container)
    {
        ITransition trans = new TypedFactoryRetriever<ITransition>()
                                    .createType("Transition");
        if(trans != null)                                    
        {
            RelationshipEventsHelper helper 
                = new RelationshipEventsHelper(trans);
                
            if(helper.firePreRelationCreated(source, target))
            {
                if(container == null)
                {
                    container = source.getContainer();
                }
                
                if (container != null)
                {
                    trans.setSource(source);
                    trans.setTarget(target);

                    // Now add the edge to the activity
                    container.addTransition(trans);
                    
                    helper.fireRelationCreated();
                }
            }
        }
        
        return trans;
    }
}
