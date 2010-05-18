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
