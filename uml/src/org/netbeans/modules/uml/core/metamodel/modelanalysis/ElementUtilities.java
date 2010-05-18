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
 * File       : ElementUtilities.java
 * Created on : Oct 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.modelanalysis;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ElementUtilities
{

 /**
  *
  * Tests whether the given operation is redefined by any operation on the 
  * given list.
  *
  * @param pOp The operation to test
  * @param opList The list of operations to test pOp against
  *
  * @return True if the operation is redefined by an operation on the list.
  *
  */

    protected boolean isOperationRedefinedBy( IOperation pOp, ETList<IOperation> opList )
    {
        boolean retval = false;
    
        if ( pOp != null && opList != null )
        {    
            int count = opList.size();
            for (int idx = 0 ; idx < count ; idx++)
            {
                if(isOperationRedefinedBy(pOp, opList.get(idx)))
                    return true;
            }
        }
        return retval;
     }
 

    /**
     *
     * Checks to see if the candidate operation is already a redefinition of the other.
     * This means not only to check direct redefinitions, but goes all the way
     * down the redefinition tree.
     *
     * Because an operation can be redefined many times, but will usually redefine
     * only 1, we invert the question: Is pRedefOp redefining pOp?
     * This should make the search much more efficient. In other words, we assume 
     * that all backpointers are consistent.
     *
     * @param pOp[in] The Operation to test
     * @param pRedefOp[in] The operation that may or may not redefine pOp
     *
     * @return True if pOp is redefined by pRedefOp
     *
     */
    
    protected boolean isOperationRedefinedBy(IOperation pOp, IOperation pRedefOp)
    {
        boolean retval = false;
        if ( pOp != null && pRedefOp != null )
        {
            return isOperationRedefining (pRedefOp, pOp);
        }
       return retval;
    }


    /**
     *
     * Checks to see if the candidate operation is already redefining the other.
     * This means not only to check direct redefinitions, but goes all the way
     * up the redefinition tree.
     *
     * @param pRedefOp[in] The operation that redefines others.
     * @param pOp[in] The operation that to be looked for on the candidates list of redefinitions
     *
     * @return True if pRedefOp is a redefinition of pOp
     *
     */
    
    protected boolean isOperationRedefining(IOperation pRedefOp, 
                                                        IOperation pOp)
    {
        boolean retval = false;
     
        if ( pRedefOp != null && pOp != null )
        {
            ETList <IRedefinableElement> opList = pRedefOp.getRedefinedElements();
    
            if ( opList != null )
            {
                // for efficiency's sake first check ALL of these before recursing,
                // instead of putting an isSame call inside the while loop
    
                retval = isMember (pOp, opList);
    
                if (!retval)
                {
                    // now recurse.
    
                    int count = opList.size();
                    IRedefinableElement pItem = null;               
                    for (int idx = 0;  idx < count ; idx++)
                    {
                        pItem = opList.get(idx);
    
                        if (pItem != null)
                        {
                            if(pItem instanceof IOperation) 
                            {
                                IOperation pItemOP = (IOperation)pItem;
                                if ( pItemOP != null )
                                {
                                    if(isOperationRedefining ( pItemOP, pOp ))
                                        return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return retval;
    }

    /**
     *
     * This is a convenience function that calls the IsSame method on IElement
     * and returns the result as a boolean instead of a VARIANT.
     *
     * @param pItem1[in] The first element
     * @param pItem2[in] The second element
     *
     * @return True if the elements are the same
     *
     */
    
    protected boolean isSame (IElement pItem1, IElement pItem2 )
    {
        boolean isSame = false;    
    
        if ( pItem1 != null && pItem2 != null )
        {         
            if (pItem1.isSame(pItem2))
            {
                isSame = true;
            }
        }
    
       return isSame;
    }

    /**
     *
     * Check to see if the given operation is a member of the given list
     *
     * @param pItem[in] The operation to search for
     * @param pList[in] The list to search in
     *
     * @return True if the item is a member of the list
     *
     */

    protected boolean isMember(IOperation pItem, ETList<IOperation> pList)
    {
       boolean retval = false;
    
        if ( pItem != null && pList != null )
        {    
            int count = pList.size();
            for (int idx = 0 ; idx < count ; idx++)
            {
                if(isSame(pItem, pList.get(idx)))
                    return true;
            }
        }
        return retval;
    }
    
    /**
     *
     * Check to see if the given operation is a member of the given list
     *
     * @param pItem[in] The operation to search for
     * @param pList[in] The list to search in
     *
     * @return True if the item is a member of the list
     *
     */

    protected boolean isMember(IRedefinableElement pItem, ETList<IRedefinableElement> pList)
    {
       boolean retval = false;
    
        if ( pItem != null && pList != null )
        {    
            int count = pList.size();
            for (int idx = 0 ; idx < count ; idx++)
            {
                if(isSame(pItem, pList.get(idx)))
                    return true;
            }
        }
        return retval;
    }    

}
