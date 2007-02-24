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
 * File       : IMethodChangeFacility.java
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
public interface IMethodChangeFacility extends IRequestFacility
{
    public IOperation addOperation(String sName, 
                                    String sReturnType, 
                                    ETList<IParameter> pParameters, 
                                    IClassifier pClassifier);
    
    public void addOperationToClassifier(IOperation pOperation, IClassifier pClassifier);
    
    public void delete(IOperation pOp);
    
    public void changeName(IOperation pOp, String sNewName,
                           boolean rtOffCreate,
                           boolean rtOffPostProcessing);
    
    public void changeType(IOperation pOp, String sNewType,
            boolean rtOffWhileChanging,
            boolean rtOffPostProcessing);
    
    public void added(IOperation pOp);
    
    public void deleted(IOperation pOp, IClassifier pClassifier);
    
    public void nameChanged(IOperation pOp);
    
    public void typeChanged(IOperation pOp);
    
    public void changeReturnTypeMultiplicity(IOperation pOperation, 
                                                IMultiplicity pMultiplicity, 
                                                boolean rtOffWhileChanging, 
                                                boolean rtOffPostProcessing);
                                                
    public void changeParameterMultiplicity(IParameter pParameter, 
                                            IMultiplicity pMultiplicity, 
                                            boolean rtOffWhileChanging, 
                                            boolean rtOffPostProcessing);
}
