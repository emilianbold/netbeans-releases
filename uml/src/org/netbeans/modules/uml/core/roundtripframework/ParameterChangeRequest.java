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
 * File       : ParameterChangeRequest.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ParameterChangeRequest
    extends ChangeRequest
    implements IParameterChangeRequest
{
    private IOperation m_BeforeOp;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IParameterChangeRequest#getAfterOperation()
     */
    public IOperation getAfterOperation()
    {
        int cDetail = getRequestDetailType();
        
        if (cDetail != RequestDetailKind.RDT_PARAMETER_REMOVED)
        {
            // just get the op off the after parameter
            IElement pReqEl = getAfter();

            if (pReqEl != null)
            {
                IParameter pParm = (pReqEl instanceof IParameter)
                                        ?(IParameter)pReqEl:null;
                if (pParm != null)
                {
                    IBehavioralFeature pFeat = pParm.getBehavioralFeature();
                    
                    return (pFeat instanceof IOperation)
                                    ? (IOperation)pFeat : null;
                }
            }
        }
        else
        {
            // The removed parameter may or may not have a backpointer
            // to the operation. We don't care. Just get the class off
            // the old operation and look for that operation on that 
            // classifier. Because old operation is a clone, we cannot
            // just use that one.

            if (m_BeforeOp != null)
            {
                IClassifier pClass = m_BeforeOp.getFeaturingClassifier();
                if (pClass != null)
                {
                    ETList <IOperation> oplist = pClass.getOperations();
                    if (oplist != null)
                    {
                        int count = oplist.size();
                        int idx = 0;
                        boolean isSame = false;
                        IOperation pItem = null;
                        while (idx < count && isSame == false)
                        {
                            pItem = oplist.get(idx);
                            idx++;
                            if(isSame = m_BeforeOp.isSame( pItem))
                                return pItem;
                        }
                    }
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IParameterChangeRequest#getBeforeOperation()
     */
    public IOperation getBeforeOperation()
    {
        return m_BeforeOp;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IParameterChangeRequest#setBeforeOperation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void setBeforeOperation(IOperation newVal)
    {
        m_BeforeOp = newVal;
    }

}
