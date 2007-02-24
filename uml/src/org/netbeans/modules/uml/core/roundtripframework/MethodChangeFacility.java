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
