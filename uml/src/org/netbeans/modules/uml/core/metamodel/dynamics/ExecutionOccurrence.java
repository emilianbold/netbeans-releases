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



package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;

public class ExecutionOccurrence extends InteractionFragment
    implements IExecutionOccurrence
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence#getStart()
     */
    public IEventOccurrence getStart()
    {
        return new ElementCollector<IEventOccurrence>()
            .retrieveSingleElementWithAttrID( this, "start", IEventOccurrence.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence#setStart(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setStart(final IEventOccurrence occ)
    {
        new ElementConnector<IExecutionOccurrence>()
            .addChildAndConnect( this, true, "start", "start", occ, 
                new IBackPointer<IExecutionOccurrence>()
                {
                    public void execute(IExecutionOccurrence execOcc)
                    {
                        occ.setStartExec(execOcc);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence#getFinish()
     */
    public IEventOccurrence getFinish()
    {
        return new ElementCollector<IEventOccurrence>()
            .retrieveSingleElementWithAttrID( this, "finish", IEventOccurrence.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence#setFinish(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setFinish(final IEventOccurrence occ)
    {
        new ElementConnector<IExecutionOccurrence>()
            .addChildAndConnect( this, true, "finish", "finish", occ, 
                new IBackPointer<IExecutionOccurrence>()
                {
                    public void execute(IExecutionOccurrence execOcc)
                    {
                        occ.setFinishExec(execOcc);
                    }
                } );
    }
}