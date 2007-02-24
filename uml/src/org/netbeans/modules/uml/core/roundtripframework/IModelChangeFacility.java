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
 * File       : IModelChangeFacility.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public interface IModelChangeFacility extends IRequestFacility
{
    public IAttribute addAttribute(String language, 
                                    String sName, 
                                    String sType, 
                                    IClassifier pClassifier, 
                                    boolean rtOffCreate,
                                    boolean rtOffPostProcessing);
                                    
    public void findAttributeAndDelete(String language, 
                                        String sName, 
                                        IClassifier pClassifier);
                                        
    public void findAttributeAndChangeName(String language, 
                                            String sOldName, 
                                            String sNewName, 
                                            IClassifier pClassifier);
    
    public IAttribute findAttributeAndChangeType(String language, 
                                            String sName, 
                                            String sNewType, 
                                            IClassifier pClassifier);
                                            
    public void deleteAttribute(String language, 
                                IAttribute pAttr, 
                                boolean rtOffDelete, 
                                boolean rtOffPostDelete);
    
    public void changeAttributeName(String language, 
                                    IAttribute pAttr, 
                                    String sNewName, 
                                    boolean rtOffCreate, 
                                    boolean rtOffPostProcessing);
                                    
    public void changeAttributeTypeName(String language, 
                                        IAttribute pAttr, 
                                        String sNewType, 
                                        boolean rtOffCreate, 
                                        boolean rtOffPostProcessing);
                                        
    public void changeAttributeVisibility(String language, 
                                            IAttribute pAttr, 
                                            /*VisibilityKind*/int visibility, 
                                            boolean rtOffCreate,
                                            boolean rtOffPostProcessing);
                                            
    public void attributeAdded(String language, IAttribute pAttr);
    
    public void attributeDeleted(String language, 
                                    IAttribute pAttr, 
                                    IClassifier pClassifier);
                                    
    public void attributeNameChanged(String language, IAttribute pAttr);
    
    public void attributeTypeChanged(String language, IAttribute pAttr);
    
    public IAttribute createAttribute(String language, 
                                        String sName, 
                                        String sType, 
                                        IClassifier pClassifier);
                                        
    public IAttribute changeAttributeType(String language, 
                                            IAttribute pAttr, 
                                            IClassifier pClassifier, 
                                            String sName, 
                                            String sNewType);
                                            
    public IOperation addOperation(String language, 
                                    String sName, 
                                    String sReturnType, 
                                    ETList<IParameter> pParameters, 
                                    IClassifier pClassifier);
                                    
    public void addOperationToClassifier(String language, 
                                            IOperation pOperation, 
                                            IClassifier pClassifier);
                                            
    public void deleteOperation(String language, IOperation pOp);
    
    public void changeOperationName(String language, 
                                    IOperation pOp, 
                                    String sNewName);
                                    
    public void changeOperationType(String language, 
                                    IOperation pOp, 
                                    String sNewType);
                                    
    public void operationAdded(String language, IOperation pOp);
    
    public void operationDeleted(String language, 
                                    IOperation pOp, 
                                    IClassifier pClassifier);
                                    
    public void operationNameChanged(String language, IOperation pOp);
    
    public void operationTypeChanged(String language, IOperation pOp);
    
    public void changeClassifierName(String language, 
                                        IClassifier pClass, 
                                        String sNewName);
                                        
    public void classifierNameChanged(String language, 
                                        IClassifier pClass);
                                        
    public void generalizationAdded(String language, 
                                    IClassifier pGeneral, 
                                    IClassifier pSpecific);
                                    
    public void generalizationDeleted(String language, 
                                        IClassifier pGeneral, 
                                        IClassifier pSpecific);
                                        
    public void implementationAdded(String language, 
                                    IClassifier pSupplier, 
                                    IClassifier pContract);
                                    
    public void implementationDeleted(String language, 
                                        IClassifier pSupplier, 
                                        IClassifier pContract);
                                        
    public void dependencyAdded(String language, 
                                IClassifier pDependent, 
                                IClassifier pIndependent);
                                
    public void dependencyDeleted(String language, 
                                    IClassifier pDependent, 
                                    IClassifier pIndependent);
                                    
    public void changeInitializer(String language, 
                                    IAttribute pAttribute, 
                                    String initializer, 
                                    boolean rtOff);
                                    
    public void changeAttributeFinal(String language, 
                                    IAttribute pAttribute, 
                                    boolean isFinal, 
                                    boolean rtOff);
                                    
    public void changeAttributeStatic(String language, 
                                        IAttribute pAttribute, 
                                        boolean isStatic, 
                                        boolean rtOff);
                                        
    public void changeAttributeMultiplicity(String language, 
                                            IAttribute pAttribute, 
                                            IMultiplicity pMultiplicity, 
                                            boolean rtOffWhileChanging, 
                                            boolean rtOffPostProcessing);
                                            
    public void changeOperationReturnTypeMultiplicity(String language, 
                                                        IOperation pOperation, 
                                                        IMultiplicity pMultiplicity,
                                                        boolean rtOffWhileChanging, 
                                                        boolean rtOffPostProcessing);

    public void changeParameterMultiplicity(String language, 
                                            IParameter pParameter, 
                                            IMultiplicity pMultiplicity, 
                                            boolean rtOffWhileChanging, 
                                            boolean rtOffPostProcessing);

    public IFacilityManager getFacilityManager();
}
