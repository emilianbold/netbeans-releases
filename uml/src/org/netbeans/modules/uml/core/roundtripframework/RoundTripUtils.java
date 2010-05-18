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
