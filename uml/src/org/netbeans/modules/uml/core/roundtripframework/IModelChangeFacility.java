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
