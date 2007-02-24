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
 * File       : RoundTripUtils.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class RoundTripUtils
{
    public static void setOperationReturnTypeMultiplicity(IOperation pOperation, 
                                                    IMultiplicity pMultiplicity)
    {
        if(pOperation == null || pMultiplicity == null) return;
        
        IParameter pParameter = pOperation.getReturnType();
        if( pParameter != null)
        {
            setParameterMultiplicity( pParameter, pMultiplicity );
        }

    }
    
    public static void setAttributeMultiplicity(IAttribute pAttribute, 
                                            IMultiplicity pMultiplicity)
    {
        if(pAttribute == null || pMultiplicity == null) return;
        
        IMultiplicity pCurrentMultiplicity = pAttribute.getMultiplicity();
        if( pCurrentMultiplicity != null)
        {
            setMultiplicity(pCurrentMultiplicity, pMultiplicity);
        }
    }
    
    public static void setParameterMultiplicity(IParameter pParameter, 
                                            IMultiplicity pMultiplicity)
    {
        if(pParameter == null || pMultiplicity == null) return;
        
        IMultiplicity pCurrentMultiplicity = pParameter.getMultiplicity();
        if( pCurrentMultiplicity != null)
        {
            setMultiplicity(pCurrentMultiplicity, pMultiplicity);
        }
    }
    
    public static void setMultiplicity(IMultiplicity pMultiplicity, 
                                IMultiplicity pNewMultiplicity)
    {
        if (pMultiplicity != null)
        {
            // Remove all existing multiplicity ranges
            pMultiplicity.removeAllRanges();

            ETList<IMultiplicityRange> pRanges = pNewMultiplicity.getRanges();

            if(pRanges != null)
            {
                // For each range in @a pNewMultiplicity, add a new range
                // with same upper and lower bounds to
                // pMultiplicity
                int numRanges = pRanges.size();
                IMultiplicityRange pNewMultiplicityRange = null;
                for( int index = 0; index < numRanges; ++index )
                {
                    pNewMultiplicityRange = pRanges.get(index);
                    if(pNewMultiplicityRange != null)
                    {
                        IMultiplicityRange pNewRange = pNewMultiplicity
                                                            .createRange();                  
  
                        if(pNewRange != null)
                        {
                            // Initialize the New Range from the current range's data
                            pNewRange.setUpper(pNewMultiplicityRange.getUpper());
                            pNewRange.setLower(pNewMultiplicityRange.getLower());

                            // Add the range
                            pMultiplicity.addRange(pNewRange);
                        }
                    }
                }
            }
        }
    }                                
    
    public static int getRTModeFromTurnOffFlag(boolean flag)
    {
        return flag ? RTMode.RTM_OFF : RTMode.RTM_LIVE;
    }
}
