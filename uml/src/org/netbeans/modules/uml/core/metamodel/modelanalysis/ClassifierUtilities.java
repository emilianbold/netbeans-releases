/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * File       : ClassifierUtilities.java
 * Created on : Oct 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.modelanalysis;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ClassifierUtilities extends ElementUtilities
{
    /**
     *
     * Collect all classes that are direct base classes of the specified classifier.
     * The return list is created if needed, and is not cleared, so the caller can 
     * repeatedly call this routine to build a collection of classifiers.
     *
     * @param pClass[in] The classifier
     * @param pRetVal[in, out] The list of classifiers
     *
     * @return 
     *
     */

    public ETList<IClassifier> collectGeneralizingClassifiers(IClassifier pClass)
    {
        ETList<IClassifier> pRetVal = new ETArrayList<IClassifier>();

        if ( pClass != null )
        {
            ETList <IGeneralization> genRels = pClass.getGeneralizations();

            if (genRels != null)
            {
                int count = genRels.size();
                for (int idx = 0; idx < count ; idx++)
                {
                    IGeneralization pRel = genRels.get(idx);
                    if ( pRel != null )
                    {
                        IClassifier pBaseClass = pRel.getGeneral();
                        if ( pBaseClass != null )
                        {
                            pRetVal.add(pBaseClass);
                        }
                    }
                }
            }
        }
        return pRetVal;
    }
    
    /**
     *
     * Collect all interfaces that are directly implemented by the specified classifier.
     * The return list is created if needed, and is not cleared, so the caller can
     * repeatedly call this routine to build a collection of classifiers.
     *
     * @param pClass[in] The classifier
     * @param pRetVal[in, out] The list of interfaces
     *
     * @return 
     *
     */
    
    public ETList<IClassifier> collectImplementedInterfaces(IClassifier pClass)
    {
        ETList<IClassifier> pRetVal = new ETArrayList<IClassifier>();

        if ( pClass != null )
        {
            ETList <IImplementation> impRels = pClass.getImplementations();

            if (impRels != null)
            {
                int count = impRels.size();
                for (int idx = 0; idx < count ; idx++)
                {
                    IImplementation pRel = impRels.get(idx);
                    if ( pRel != null )
                    {
                        IClassifier pInterface = pRel.getContract();
                        if ( pInterface != null )
                        {
                            pRetVal.add(pInterface);
                        }
                    }
                }
            }
        }
        return pRetVal;
    }
    
    /**
     *
     * Collect all operations that members of this classifier or any of its
     * generalizations or implemented interfaces. 
     * Redefined operations are only listed once.
     * The return list is created if needed, and is not cleared, so the caller can
     * repeatedly call this routine to build a collection of operations.
     * Constructors and destructors are not collected.
     *
     * @param pClass[in] The classifier 
     * @param pRetVal[in, out] The list of all operations that make up this classifier
     *
     * @return 
     *
     */

    public ETList<IOperation> collectAllOperations(IClassifier pClass)
    {
        if(pClass == null) return null;
        
        ETList<IOperation> pRetVal = new ETArrayList<IOperation>();

        ETList<IOperation> fullList = pClass.getOperations();        
        

          // UPDATE:  We need to have an operation that gets the operations from the class
          //          as a sorted list by the name of the operation.

        if ( fullList != null )
        {
            TreeMap<String, IOperation> mapString2Operation 
                        = new TreeMap<String, IOperation>();
          
            int count = fullList.size();
            IOperation pOp = null;
            for (int idx = 0 ; idx < count ; idx++)
            {
                pOp = fullList.get(idx);
                if ( pOp != null )
                {
                   // If this operation is virtual or abstract,
                   // we want to add it to the list.

                   // If it is a constructor or a finalize function, we
                   // do not want to add it.

 
                    if (pOp.getIsConstructor() == false)
                    {
                        // the operation is not a constructor or destructor so we
                        // may add this to the list
    
                        // Don't add items onto the list that are already redefined by an item
                        // that is already on the list.

                        boolean isRedefined = isOperationRedefinedBy (pOp, pRetVal);

                        if (!isRedefined )
                        {                                
                            String bsName = pOp.getName();
                            mapString2Operation.put(bsName, pOp);
                        }
                        else
                        {
                            pRetVal.add(pOp);
                        }
                    }
                }
            }
            pRetVal.addAll(mapString2Operation.values());
        }        
        // Now, we have to go up the generalization links and realization link
        // from the base class.

        if(pRetVal == null)
            pRetVal = collectOpsFromGeneralizations (pClass);
        else
        {
            ETList<IOperation> opList = collectOpsFromGeneralizations(pClass);
            if(opList != null)
                pRetVal.addAll(opList);
        }
            
        if(pRetVal == null)
            pRetVal = collectOpsFromImplementations (pClass);
        else
        {
            ETList<IOperation> opList = collectOpsFromImplementations(pClass);
            if(opList != null)
                pRetVal.addAll(opList);
        }

        return pRetVal;
    }

    /**
     *
     * Collects operations from all base classes of the given class.
     * Only goes one level up.
     * The return list is created if needed, and is not cleared, so the caller can
     * repeatedly call this routine to build a collection of operations.
     * Constructors and destructors are not collected.
     *
     * @param pClass[in] The class from which to gather generalizations.
     * @param opList[in, out] The list of operations.
     *
     * @return 
     *
     */

    protected ETList<IOperation> collectOpsFromGeneralizations (IClassifier pClass)
    {
        ETList<IOperation> opList = null;
        if ( pClass != null )
        {
            ETList < IClassifier > baseClasses 
                = collectGeneralizingClassifiers(pClass);
            opList = collectAllOperations (baseClasses);
        }
        return opList;
    }
    
    /**
     *
     * Collects operations from all interfaces implemented by the given class.
     * Only goes one level up.
     * The return list is created if needed, and is not cleared, so the caller can
     * repeatedly call this routine to build a collection of operations.
     * Constructors and destructors are not collected.
     *
     * @param pClass[in] The class from which to implemented interfaces.
     * @param opList[in, out] The list of operations.
     *
     * @return 
     *
     */

    protected ETList<IOperation> collectOpsFromImplementations (IClassifier  pClass)
    {
        ETList<IOperation> opList = null;
        if ( pClass != null )
        {
            ETList < IClassifier > implementedInterfaces 
                = collectImplementedInterfaces(pClass);
            opList = collectAllOperations (implementedInterfaces);
        }
        return opList;
    }    

    /**
     *
     * Collects operations from all of the given classes.
     * The return list is created if needed, and is not cleared, so the caller can
     * repeatedly call this routine to build a collection of operations.
     * Constructors and destructors are not collected.
     *
     * @param classList[in] The classes from which to gather operations
     * @param opList[in, out] The list of operations.
     *
     * @return 
     *
     */

    protected ETList<IOperation> collectAllOperations (ETList<IClassifier> classList)
    {
        ETList<IOperation> opList = null;
        if (classList != null)
        {
            int count = classList.size();
            IClassifier pItem = null;
            for (int idx = 0 ; idx < count ; ++idx )
            {
                pItem = classList.get(idx);
                if ( pItem != null )
                {
                    if(opList == null)
                        opList = collectAllOperations (pItem);
                    else
                        opList.addAll(collectAllOperations (pItem));
                }
             }
        }
        return opList;
    }
}
