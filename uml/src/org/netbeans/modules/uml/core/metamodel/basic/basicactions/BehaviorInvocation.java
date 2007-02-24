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


package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;



public class BehaviorInvocation extends Element implements IBehaviorInvocation
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#addArgument(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPin)
     */
    public void addBehaviorArgument(IPin pArg)
    {
        addElementByID(pArg, "argument");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#addResult(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPin)
     */
    public void addResult(IPin pArg)
    {
        addElementByID(pArg, "result");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#getArguments()
     */
    public ETList <IPin> getBehaviorArguments()
    {
        ElementCollector<IPin> collector = new ElementCollector<IPin>();
        return collector.retrieveElementCollection((IElement)this,"argument", IPin.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#getBehavior()
     */
    public IBehavior getBehavior()
    {
        ElementCollector<IBehavior> col = new ElementCollector<IBehavior>();
        return col.retrieveSingleElementWithAttrID( this, "behavior", IBehavior.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#getResult()
     */
    public ETList <IPin> getResults()
    {
        ElementCollector< IPin > col = new ElementCollector<IPin>();
        return col.retrieveElementCollectionWithAttrIDs( this, "result", IPin.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#removeArgument(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPin)
     */
    public void removeBehaviorArgument(IPin pArg)
    {
        removeElementByID( pArg, "argument");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#removeResult(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPin)
     */
    public void removeResult(IPin pArg)
    {
        removeElementByID( pArg, "result");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation#setBehavior(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
     */
    public void setBehavior(IBehavior newVal)
    {
        addElementByID(newVal, "behavior");
    }

}
