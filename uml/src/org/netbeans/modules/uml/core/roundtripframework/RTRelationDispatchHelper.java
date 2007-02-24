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
 * File       : RTRelationDispatchHelper.java
 * Created on : Nov 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * @author Aztec
 */
public class RTRelationDispatchHelper extends RTDispatchHelper
{
    public RTRelationDispatchHelper()
    {
        super();
    }
    
    public RTRelationDispatchHelper(IProcessorManager procMan, 
                            IEventDispatchController controller)
    {
        super(procMan, controller, EventDispatchNameKeeper.EDT_RELATION_KIND);
    }
    
    public void establish(IRelationProxy proxy, IResultCell cell)
    {
        RTStateTester rt = new RTStateTester();
        if (rt.isRelationInRoundTripState(proxy))
        {
            createRTContextPayload(cell, null);
        }
        else
        {
            m_Dispatcher = null;
        }
    }

}
