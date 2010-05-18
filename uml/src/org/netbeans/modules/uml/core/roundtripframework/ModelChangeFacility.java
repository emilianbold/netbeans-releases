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
 * File       : ModelChangeFacility.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ModelChangeFacility
    extends RequestFacility
    implements IModelChangeFacility
{
    IFacilityManager m_pFacilityManager;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#addAttribute(java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, boolean)
     */
    public IAttribute addAttribute(
        String language,
        String sName,
        String sType,
        IClassifier pClassifier,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        IAttributeChangeFacility pFacility = null;
        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        
        if  (pFacility != null)
        {
            return pFacility.addAttribute2(sName,
                                  sType,
                                  pClassifier,
                                  rtOffCreate,
                                  rtOffPostProcessing);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#addOperation(java.lang.String, java.lang.String, java.lang.String)
     */
    public IOperation addOperation(
        String language,
        String sName,
        String sReturnType,
        ETList<IParameter> pParameters, 
        IClassifier pClassifier)
    {
        if(language == null || sName == null || sReturnType == null || pClassifier == null) return null;
   
        
        IMethodChangeFacility pFacility = null;
        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            return pFacility.addOperation(sName,
                                    sReturnType,
                                    pParameters,
                                    pClassifier);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#addOperationToClassifier(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void addOperationToClassifier(
        String language,
        IOperation pOperation,
        IClassifier pClassifier)
    {
        if(language == null || pOperation == null || pClassifier == null) return ;
   
        
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.addOperationToClassifier(pOperation, pClassifier);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#attributeAdded(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void attributeAdded(String language, IAttribute pAttr)
    {
        if(language == null || pAttr == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.added(pAttr);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#attributeDeleted(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void attributeDeleted(
        String language,
        IAttribute pAttr,
        IClassifier pClassifier)
    {
        if(language == null || pAttr == null || pClassifier == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.deleted(pAttr, pClassifier);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#attributeNameChanged(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void attributeNameChanged(String language, IAttribute pAttr)
    {
        if(language == null || pAttr == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.nameChanged(pAttr);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#attributeTypeChanged(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void attributeTypeChanged(String language, IAttribute pAttr)
    {
        if(language == null || pAttr == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.typeChanged(pAttr);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeAttributeFinal(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, boolean)
     */
    public void changeAttributeFinal(
        String language,
        IAttribute pAttribute,
        boolean isFinal,
        boolean rtOff)
    {
        if(language == null || pAttribute == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.changeFinal(pAttribute, isFinal, rtOff);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeAttributeMultiplicity(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, boolean)
     */
    public void changeAttributeMultiplicity(
        String language,
        IAttribute pAttribute,
        IMultiplicity pMultiplicity,
        boolean rtOffWhileChanging,
        boolean rtOffPostProcessing)
    {
        if(language == null || pAttribute == null || pMultiplicity == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.changeMultiplicity(pAttribute, 
                                            pMultiplicity, 
                                            rtOffWhileChanging, 
                                            rtOffPostProcessing);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeAttributeName(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, boolean, boolean)
     */
    public void changeAttributeName(
        String language,
        IAttribute pAttr,
        String sNewName,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        if(language == null || pAttr == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.changeName(pAttr, 
                                    sNewName, 
                                    rtOffCreate, 
                                    rtOffPostProcessing);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeAttributeStatic(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, boolean)
     */
    public void changeAttributeStatic(
        String language,
        IAttribute pAttribute,
        boolean isStatic,
        boolean rtOff)
    {
        if(language == null || pAttribute == null) return ;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.changeStatic(pAttribute, isStatic, rtOff);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeAttributeType(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, java.lang.String)
     */
    public IAttribute changeAttributeType(
        String language,
        IAttribute pAttr,
        IClassifier pClassifier,
        String sName,
        String sNewType)
    {
        if(language == null || 
            pAttr == null || 
            pClassifier == null ||
            sName == null ||
            sNewType == null) return null;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            return pFacility.changeAttributeType(pAttr,
                                            pClassifier,
                                            sName,
                                            sNewType);
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeAttributeTypeName(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, boolean, boolean)
     */
    public void changeAttributeTypeName(
        String language,
        IAttribute pAttr,
        String sNewType,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        if(language == null || 
            pAttr == null ||
            sNewType == null) return;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.changeType(pAttr,
                        sNewType,
                        rtOffCreate,
                        rtOffPostProcessing);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeAttributeVisibility(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, int, boolean, boolean)
     */
    public void changeAttributeVisibility(
        String language,
        IAttribute pAttr,
        int visibility,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        if(language == null || 
            pAttr == null) return;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.changeVisibility(pAttr,
                        visibility,
                        rtOffCreate,
                        rtOffPostProcessing);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeClassifierName(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String)
     */
    public void changeClassifierName(
        String language,
        IClassifier pClass,
        String sNewName)
    {
        if(language == null || 
        pClass == null || sNewName == null) return;
   
        
        IClassChangeFacility pFacility = null;        
        try
        {
            pFacility = (IClassChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "class");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.changeName(pClass, sNewName);
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeInitializer(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, boolean)
     */
    public void changeInitializer(
        String language,
        IAttribute pAttribute,
        String initializer,
        boolean rtOff)
    {
        if(language == null || 
        pAttribute == null) return;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.changeInitializer(pAttribute, initializer, rtOff);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeOperationName(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, java.lang.String)
     */
    public void changeOperationName(
        String language,
        IOperation pOp,
        String sNewName)
    {
        if(language == null || 
            pOp == null || sNewName == null) return;
           
                
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.changeName(pOp, sNewName, false, true);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeOperationReturnTypeMultiplicity(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, boolean)
     */
    public void changeOperationReturnTypeMultiplicity(
        String language,
        IOperation pOperation,
        IMultiplicity pMultiplicity,
        boolean rtOffWhileChanging,
        boolean rtOffPostProcessing)
    {
        if(language == null || 
        pOperation == null || pMultiplicity == null) return;
           
                
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.changeReturnTypeMultiplicity( pOperation,
                                                    pMultiplicity,
                                                    rtOffWhileChanging,
                                                    rtOffPostProcessing );
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeOperationType(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, java.lang.String)
     */
    public void changeOperationType(
        String language,
        IOperation pOp,
        String sNewType)
    {
        if(language == null || 
        pOp == null) return;
           
                
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.changeType(pOp, sNewType, false, true);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#changeParameterMultiplicity(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, boolean)
     */
    public void changeParameterMultiplicity(
        String language,
        IParameter pParameter,
        IMultiplicity pMultiplicity,
        boolean rtOffWhileChanging,
        boolean rtOffPostProcessing)
    {
        if(language == null || 
        pParameter == null || pMultiplicity == null) return;
           
                
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.changeParameterMultiplicity( pParameter,
                                                    pMultiplicity,
                                                    rtOffWhileChanging,
                                                    rtOffPostProcessing );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#classifierNameChanged(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void classifierNameChanged(String language, IClassifier pClass)
    {
        if(language == null || pClass == null) return;
           
                
        IClassChangeFacility pFacility = null;        
        try
        {
            pFacility = (IClassChangeFacility)getChangeFacility(this, 
                                                               language, 
                                                               "class");
        }
        catch (ClassCastException ex){}                        
                
        if(pFacility != null)
        {
            pFacility.nameChanged( pClass );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#createAttribute(java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public IAttribute createAttribute(
        String language,
        String sName,
        String sType,
        IClassifier pClassifier)
    {
        if(language == null || 
            pClassifier == null ||
            sName == null || sType == null) return null;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            return pFacility.createAttribute(sName,
                                                sType,
                                                pClassifier);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#deleteAttribute(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, boolean)
     */
    public void deleteAttribute(
        String language,
        IAttribute pAttr,
        boolean rtOffDelete,
        boolean rtOffPostDelete)
    {
        if(language == null || 
            pAttr == null) return;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.delete( pAttr, rtOffDelete, rtOffPostDelete);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#deleteOperation(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void deleteOperation(String language, IOperation pOp)
    {
        if(language == null || pOp == null) return;   
        
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.delete(pOp);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#dependencyAdded(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void dependencyAdded(
        String language,
        IClassifier pDependent,
        IClassifier pIndependent)
    {
        if(language == null || 
        pDependent == null || pIndependent == null) return;
           
                
        IDependencyChangeFacility pFacility = null;        
        try
        {
            pFacility = (IDependencyChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "dependency");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.added(pDependent, pIndependent);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#dependencyDeleted(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void dependencyDeleted(
        String language,
        IClassifier pDependent,
        IClassifier pIndependent)
    {
        if(language == null || 
        pDependent == null || pIndependent == null) return;
           
                
        IDependencyChangeFacility pFacility = null;        
        try
        {
            pFacility = (IDependencyChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "dependency");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.deleted(pDependent, pIndependent);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#findAttributeAndChangeName(java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void findAttributeAndChangeName(
        String language,
        String sOldName,
        String sNewName,
        IClassifier pClassifier)
    {
        if(language == null || 
            sOldName == null || 
            pClassifier == null ||
            sNewName == null ||
            pClassifier == null) return;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.findAndChangeName(sOldName, sNewName, pClassifier);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#findAttributeAndChangeType(java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public IAttribute findAttributeAndChangeType(
        String language,
        String sName,
        String sNewType,
        IClassifier pClassifier)
    {
        if(language == null || 
            sName == null || 
            pClassifier == null ||
            sNewType == null) return null;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            return pFacility.findAndChangeType(sName, sNewType, pClassifier);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#findAttributeAndDelete(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void findAttributeAndDelete(
        String language,
        String sName,
        IClassifier pClassifier)
    {
        if(language == null || 
            sName == null || 
            pClassifier == null) return;
   
        
        IAttributeChangeFacility pFacility = null;        
        try
        {
            pFacility = (IAttributeChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "attribute");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.findAndDelete(sName, pClassifier);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#generalizationAdded(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void generalizationAdded(
        String language,
        IClassifier pGeneral,
        IClassifier pSpecific)
    {
        if(language == null || 
            pGeneral == null || 
            pSpecific == null) return;
   
        
        IGeneralizationChangeFacility pFacility = null;        
        try
        {
            pFacility = (IGeneralizationChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "generalization");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.added(pGeneral, pSpecific);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#generalizationDeleted(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void generalizationDeleted(
        String language,
        IClassifier pGeneral,
        IClassifier pSpecific)
    {
        if(language == null || 
            pGeneral == null || 
            pSpecific == null) return;
   
        
        IGeneralizationChangeFacility pFacility = null;        
        try
        {
            pFacility = (IGeneralizationChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "generalization");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.deleted(pGeneral, pSpecific);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#getFacilityManager(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager)
     */
    public IFacilityManager getFacilityManager()
    {
        return ProductRetriever.retrieveProduct().getFacilityManager();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#implementationAdded(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void implementationAdded(
        String language,
        IClassifier pSupplier,
        IClassifier pContract)
    {
        if(language == null || 
        pSupplier == null || 
        pContract == null) return;
   
        
        IImplementationChangeFacility pFacility = null;        
        try
        {
            pFacility = (IImplementationChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "generalization");
        }
        catch (ClassCastException ex){}
                
        
        if(pFacility != null)
        {
            pFacility.added(pSupplier, pContract);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#implementationDeleted(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void implementationDeleted(
        String language,
        IClassifier pSupplier,
        IClassifier pContract)
    {
        IImplementationChangeFacility pFacility = null;        
        try
        {
            pFacility = (IImplementationChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "generalization");
        }
        catch (ClassCastException ex){}
                        
                
        if(pFacility != null)
        {
            pFacility.deleted(pSupplier, pContract);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#operationAdded(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void operationAdded(String language, IOperation pOp)
    {
        if(language == null || pOp == null) return ;
   
        
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.added(pOp);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#operationDeleted(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void operationDeleted(
        String language,
        IOperation pOp,
        IClassifier pClassifier)
    {
        if(language == null || pOp == null || pClassifier == null) return ;
   
        
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.deleted(pOp, pClassifier);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#operationNameChanged(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void operationNameChanged(String language, IOperation pOp)
    {
        if(language == null || pOp == null) return ;
   
        
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.nameChanged(pOp);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IModelChangeFacility#operationTypeChanged(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void operationTypeChanged(String language, IOperation pOp)
    {
        if(language == null || pOp == null) return ;
   
        
        IMethodChangeFacility pFacility = null;        
        try
        {
            pFacility = (IMethodChangeFacility)getChangeFacility(this, 
                                                                language, 
                                                                "operation");
        }
        catch (ClassCastException ex){}
        

        if(pFacility != null)
        {
            pFacility.typeChanged(pOp);
        }
    }
    
    public IFacility getChangeFacility(IModelChangeFacility pFacility,
                                        String  language,
                                        String  facilityType)
    {
        if(pFacility == null) return null;
        
        String facilityName;
        String  propertyName = language + "." + facilityType;
        facilityName = pFacility.getPropertyValue(propertyName);

        if( facilityName != null)
        {
            String facilityFullName = "RoundTrip." + facilityName;
      
            IFacilityManager pManager = pFacility.getFacilityManager();

            if(pManager != null)
            {
                return pManager.retrieveFacility(facilityFullName);
            }
       }
       return null;
    }


}
