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
 * File       : MethodChangeFacility.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class MethodChangeFacility
    extends RequestFacility
    implements IMethodChangeFacility
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#added(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void added(IOperation pOp)
    {
        // Stubbed in C++ code.

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#addOperation(java.lang.String, java.lang.String)
     */
    public IOperation addOperation(String sName, 
                                    String sReturnType, 
                                    ETList<IParameter> pParameters, 
                                    IClassifier pClassifier)
    {
        if(sName == null || pClassifier == null) return null;
        // Create the new Operation
        IOperation op = pClassifier.createOperation(sReturnType, sName);
        
        if (op != null)
        {
            if (pParameters != null)
            {
                // Add the parameters to the operation
                op.setParameters(pParameters);
            }
                 
            // Add the Operation to the classifier
            addOperationToClassifier(op, pClassifier);
            added(op);
        }
        return op;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#addOperationToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void addOperationToClassifier(
        IOperation pOperation,
        IClassifier pClassifier)
    {
        if (pOperation == null || pClassifier == null) return;
        
        new RoundTripModeRestorer(RTMode.RTM_OFF);
        pClassifier.addOperation(pOperation );

        added(pOperation);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#changeName(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, java.lang.String)
     */
    public void changeName(IOperation pOp, 
                           String sNewName,
                           boolean rtOffCreate,
                           boolean rtOffPostProcessing)
    {
        if(pOp == null) return;
        
        RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils
                                        .getRTModeFromTurnOffFlag( rtOffCreate ));
        
        pOp.setName(sNewName);

        restorer.setMode(RoundTripUtils.
                            getRTModeFromTurnOffFlag(rtOffPostProcessing));
        
        nameChanged(pOp);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#changeParameterMultiplicity(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, boolean)
     */
    public void changeParameterMultiplicity(
        IParameter pParameter,
        IMultiplicity pMultiplicity,
        boolean rtOffWhileChanging,
        boolean rtOffPostProcessing)
    {
        if (pParameter == null || pMultiplicity == null) return;
        
         RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils.getRTModeFromTurnOffFlag(rtOffWhileChanging));
         
        RoundTripUtils.setParameterMultiplicity(pParameter, pMultiplicity);
        
        restorer.restoreOriginalMode();


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#changeReturnTypeMultiplicity(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, boolean)
     */
    public void changeReturnTypeMultiplicity(
        IOperation pOperation,
        IMultiplicity pMultiplicity,
        boolean rtOffWhileChanging,
        boolean rtOffPostProcessing)
    {
        if (pOperation == null || pMultiplicity == null) return;
        
         RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils.getRTModeFromTurnOffFlag(rtOffWhileChanging));
         
        RoundTripUtils.setOperationReturnTypeMultiplicity(pOperation, pMultiplicity);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#changeType(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, java.lang.String)
     */
    public void changeType(IOperation pOp, 
            String sNewType,
            boolean rtOffWhileChanging,
            boolean rtOffPostProcessing)
    {
        RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils.getRTModeFromTurnOffFlag(rtOffWhileChanging));
         
        pOp.setReturnType2(sNewType);
        
        restorer.setMode(RoundTripUtils.getRTModeFromTurnOffFlag(rtOffPostProcessing));
        typeChanged(pOp);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#delete(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void delete(IOperation pOp)
    {
         RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RTMode.RTM_OFF);
         
        IClassifier pClassifier = pOp.getFeaturingClassifier();

        if(pClassifier != null)
        {
            pClassifier.removeFeature(pOp);
        }
        pOp.delete();
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#deleted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void deleted(IOperation pOp, IClassifier pClassifier)
    {
        // Stubbed in C++ code.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#nameChanged(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void nameChanged(IOperation pOp)
    {
        // Stubbed in C++ code.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IMethodChangeFacility#typeChanged(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void typeChanged(IOperation pOp)
    {
        // Stubbed in C++ code.
    }

}
