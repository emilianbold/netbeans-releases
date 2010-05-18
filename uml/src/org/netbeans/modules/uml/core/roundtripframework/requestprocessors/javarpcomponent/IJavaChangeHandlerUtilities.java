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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import java.util.List;

/**
 * @author Aztec
 */
public interface IJavaChangeHandlerUtilities extends IRequestProcessorUtilities
{
	public RequestDetails getRequestDetails(IChangeRequest pRequest);

	public ETPairT<IAttribute, IClassifier> getAttributeAndClass(IChangeRequest pRequest,
			boolean fromBefore);
	
	public ETPairT<IOperation, IClassifier> getOperationAndClass(IChangeRequest pRequest,
			boolean fromBefore);
		
	public ETPairT<IAttribute, IClassifier> getAttributeAndClass(IChangeRequest pRequest, 
			INavigableEnd pNotThisEnd, 
			boolean fromBefore);
	public boolean isElementUnnamed(INamedElement pElement);

	public boolean autoNameNavigableEndPreference();

	public String attributePrefix();

	public boolean createReadAccessor(IAttribute pAttr, IClassifier pClass, boolean force);

	public boolean createWriteAccessor(IAttribute pAttr, IClassifier pClass, boolean force);

	public ETPairT<ETList<IOperation>, ETList<IDependency>> getReadAccessorsOfAttribute
	(IAttribute pAttr, IClassifier pFromClass);
	
	public ETPairT<ETList<IOperation>, ETList<IDependency>> getWriteAccessorsOfAttribute
	(IAttribute pAttr, IClassifier pFromClass);

	public void breakReadAccessorFromAttribute(IOperation pOp);

	public void breakWriteAccessorFromAttribute(IOperation pOp);

	public void setOperationReturnType(IOperation pOperation, String retType);

	public IParameter getPositionParameter(IOperation pParamsOp, int position);

	public String readAccessorPrefix();

	public String writeAccessorPrefix ();

	public String removePrefixFromAttributeName(String attrName);

	public void moveToClass2(ETList<IOperation> list, IClassifier pClass);

	public IChangeRequest createChangeRequest(Class requestClass, int cType, int cDetail, IElement pBefore, IElement pAfter, IElement pElementForFiles);

	public IDependency createRealization(INamedElement pSupplier, INamedElement pClient, INamespace pNamespace);

	public IClassifier getType(IElement pAttr);

	public IClassifier getClass(IChangeRequest pRequest);

	public IClassifier getClass(IChangeRequest pRequest, boolean fromBefore);

	public ETList<IOperation> getConstructors(IClassifier pClass);

	public void createConstructor(IClassifier pClass);

	public void createConstructor(IClassifier pClass, boolean force);

    public void createDestructor(IClassifier pClass);

    public void createDestructor(IClassifier pClass, boolean force);


	public IElement getElement(IChangeRequest pRequest, boolean fromBefore);

	public void breakReadAccessorsOfAttribute(IAttribute pAttr);

	public void breakWriteAccessorsOfAttribute(IAttribute pAttr);

	public ETList<IAssociationEnd> getReferencingNavEnds(IClassifier pClass);

	public ETList<INavigableEnd> getParticipatingNavEnds(IClassifier pClass);

	public ETList<IClassifier> getSpecializations(IClassifier pBaseClass);

	public ETList<IClassifier> getGeneralizations(IClassifier pSubClass);

	public ETList<IClassifier> getImplementedInterfaces(IClassifier pClass);

	public ETList<IClassifier> getImplementingClassifiers(IClassifier pClass, ETList<IClassifier> classList);

	public ETList<IElement> getDependents(IClassifier pIndependentElement);

	public ETList<IElement> getDependencies(IClassifier pDependentElement);

	public ETList<IClassifier> getNavigableClasses(IClassifier pClass);

	public ETList<IClassifier> getNavigatingClasses(IClassifier pClass);

	public ETList<IOperation> collectRedefinedOps(IOperation pOp);

	public ETList<IOperation> collectRedefiningOps(IOperation pOp);
	
	//	   Notice that since this routine returns op pairs, we don't have to have the corresponding "CollectRedefiningOps"
	public ETList<IOperation> collectRedefinedOps(IClassifier pBaseClass, IClassifier pDerivedClass);

	public ETTripleT<ETList<IOperation>, ETList<IOperation>, ETList<IOperation>> 
						collectRedefinedOps(IClassifier pClass, boolean bExclusive);

	//	   This routine collects all operations on a class that are redefinitions, redefined, or both. 
	//	   Mostly, this would be used during a delete of a class.
	public void breakRedefinitions(ETList<IOperation> baseOps, ETList<IOperation> derivedOps);

	public void breakRedefinitions(IClassifier pBaseClass, IClassifier pDerivedClass);
	
	public void breakRedefinitions(IOperation pOperation);
	
	public void breakRedefinitionsPropagated(IClassifier pBaseClass, IClassifier pDerivedClass);

	public void applyInheritedOperations(IRequestValidator request, 
										 IElement pBaseElement, 
										 IElement pDerivedElement, 
										 IOperationCollectionBehavior behaviorControl);

	public void applyInheritedOperations(IRequestValidator request, 
										 ETList<IClassifier> pBaseClasses, 
										 IClassifier pDerivedClass, 
										 IOperationCollectionBehavior behaviorControl);

	public void buildRedefinition(IOperation pBaseOp, IOperation pRedefiningOp);

	public void buildRedefinitions(ETList<IOperation> oppairs);

	//	   This routine is used when a generalization or implementation is created.
	//	   It looks in the two classes for operations that match signature. Because 
	//	   we want to keep the functions as generic as possible, we DON'T really
	//	   want to build the redefinition here. But we cannot just return a single
	//	   operation, since we want to keep the pairs together. So, I really need
	//	   to create a typedef pair and a vector of those pair. Until then, I just
	//	   return a list whose length should always be even, where the first operation
	//	   is the base op, and the second is the redefining op.

	public ETList<IOperation> discoverRedefinitions(IClassifier pBaseClass, IClassifier pRedefiningClass);

	public ETList<IOperation> appendOperationsToList(ETList<IOperation> partList, ETList<IOperation> fullList);

	public ETList<IClassifier> add(IClassifier pItem, ETList<IClassifier> pList);

	public IOperation copyOperation(IOperation pOrig, IClassifier pOwnerOfNew);

	public IParameter copyParameter(IParameter pOrig, IBehavioralFeature pOwnerOfNew);

	public void addRedefiningOperation(IRequestValidator request, IOperation pOrigOp, IOperation pNewOp, IClassifier pToClass);

	public ETList<IOperation> collectInheritedAbstractOperations(IClassifier pClass, IOperationCollectionBehavior behaviorControl);

	public ETList<IOperation> collectAbstractOperations(IClassifier pClass, IOperationCollectionBehavior behaviorControl);

	public void breakRedefinition(IOperation  pBaseOp, IOperation  pDerivedOp);

	public String getOperationReturnType(IOperation pOperation);

	public IParameter getCorrespondingParameter(IOperation pOperation, IParameter pParameter);
	
	public IParameter getCorrespondingParameter(IOperation pOperation, IOperation pParamsOp, IParameter pParameter);
	
	public int getParameterPosition(IOperation pParamsOp, IParameter pParameter);

	public String formatOperation(IOperation pOp);
	
	public String formatMultiplicity(IMultiplicity pMult);

	public ETList<IClassifier> elementsToClasses(ETList<IElement> inList);

	public void deleteDependencies(ETList<IDependency> deps);
	
	public boolean removePrefixFromAccessor();
	
	public boolean isSame(IElement pItem1, IElement pItem2);
	
	public boolean compareSignatures(IOperation pOp1, IOperation pOp2);
    
	public boolean compareNames(INamedElement pItem1, INamedElement pItem2);
	
	public IOperation addOperationToClass(IClassifier pClass, 
											String opName, 
											IClassifier opType, 
											ETList<IJRPParameter> parameters, 
											boolean addToClass);
											
	public IOperation addOperationToClass(IClassifier pClass, 
											String opName, 
											IClassifier opType,											 
											ETList<IJRPParameter> parameters,
											int visibility, 
											boolean addToClass);
											
	public IOperation addOperationToClass(IClassifier pClass, 
											String opName, 
											String opType, 
											ETList<IJRPParameter> parameters, 
											boolean addToClass);
																						
	public IOperation addOperationToClass(IClassifier pClass, 
											String opName, 
											String opType, 
											ETList<IJRPParameter> parameters,
											int visibility,
											boolean addToClass);																						
																					
	public IOperation addParametersToOperation(IOperation pOperation, 
											   ETList<IJRPParameter> parameters);		
											   
	public IOperation addParameterToOperation(IOperation pOperation, IParameter pParameter);
    
	public IOperation addParameterToOperation(IOperation pOperation, IJRPParameter pParameter);
																				
	public boolean capAttributeNameInAccessor ();
	
	public String capAttributeName(String attrName);
	
	public ETList<IClassifier> getAllDerivedClasses(IClassifier pBaseClass,
													ETList<IClassifier> derivedClasses,	 
													boolean addBaseToList);
													
	public ETList<IOperation> collectOpsFromGeneralizations(IClassifier pBaseClass,
										ETList<IClassifier> pBaseClasses, 
										ETList<IOperation> opList, 
										IOperationCollectionBehavior behaviorControl);
    
	public ETList<IOperation> collectOpsFromImplementations(IClassifier pBaseClass,
										ETList<IClassifier> pBaseClasses, 
										ETList<IOperation> opList, 
										IOperationCollectionBehavior behaviorControl);
											
	public boolean createConstructorPreference();
	
    public boolean createDestructorPreference();
	
	public boolean createAccessorPreference();
	
	public boolean isSameClass(IOperation pItem1, IClassifier pItem2);
    
	public boolean isSameClass(IOperation pItem1, IOperation pItem2);
	
	/***/
	
	public String getOldOperationSig(IChangeRequest pRequest);
	
	public String getParameterTypeName(IProject pProject, IParameter pParameter);
	
	public IElement copyElement(IElement pOrig, IElement pOwnerOfNew);
	
	public IAttribute copyAttribute(IAttribute pOrig, IClassifier pOwnerOfNew);
	
	public void copyMultiplicity(IAttribute pOrig, IAttribute pNew); // copies the multiplicity OF the typed element
	
	public void copyMultiplicity(ITypedElement pOrig, ITypedElement pNew); // copies the multiplicity OF the typed element
	
	public void changeAttributeOfAccessors(IAttribute pOldAttr, IAttribute pNewAttr);
	
	public void addParameterToOperation(IOperation pOperation, 
										String parmType, 
										String parmName, 
										int direction);
										
	public String getRelationType(IRelationProxy pRelation);
	
	/// automatically discover and build redefinitions between existing operations that match signatures.
 	public ETTripleT<ETList<IClassifier>, ETList<IOperation>, ETList<IOperation>> 
				 buildExistingRedefinitions(IClassifier pBaseClass,
										IClassifier pDerivedClass);

	/// automatically discover and build redefinitions between existing operations that match signatures.
 	public ETPairT<ETList<IClassifier>, ETList<IOperation>> 
				 buildExistingRedefinitions2(IClassifier pBaseClass,
									 IClassifier pDerivedClass);
									 											
	public ETList<IOperation> getOperationsByName(IClassifier pClass, String opName);
										 		
	public ETTripleT<ETList<IClassifier>, ETList<IOperation>, ETList<IOperation>> 
				collectRedefiningOps(IClassifier pClass, boolean bExclusive);
					
	public ETList<IClassifier> collectBaseClasses(IClassifier pClass);
	
	public ETList<IClassifier> getImplementingClassifiers(IClassifier pClass);
	
	public ETList<IAssociationEnd> getParticipatingEnds(IClassifier pClass);
	
	public ETList<IClassifier> addClassifier(IClassifier pItem, ETList<IClassifier> classes);
	
	public ETList<IOperation> addOperation(IOperation pItem, ETList<IOperation> ops);
    
	public ETList<IElement> addElement(IElement pItem, ETList<IElement> elems);
    
	public ETList<IAssociationEnd> addAssociationEnd(IAssociationEnd pItem, ETList<IAssociationEnd> ends);
	
	// These four functions check against EXISTING redefinition.

	public boolean isOperationRedefinedBy(IOperation pCandidate, ETList<IOperation> opsToCheckAgainst);
    
	public boolean isOperationRedefinedBy(IOperation pCandidate, IOperation pOpToCheckAgainst);
    
	public boolean isOperationRedefining(IOperation pCandidate, ETList<IOperation> opsToCheckAgainst);
    
	public boolean isOperationRedefining(IOperation pCandidate, IOperation pOpToCheckAgainst);
    
	public boolean compareTypes(ITypedElement pItem1, ITypedElement pItem2);
	
	public boolean isSameClass(IAttribute pItem1, IClassifier pItem2);
    
	public boolean isSameClass(IAttribute pItem1, IAttribute pItem2);
	
	//	retval = lh < rh
	public boolean isVisibilityLess(int lh, int rh);
	
	// MOVE and DUPE fuctions

    public void moveToClass(IOperation pItem, IClassifier pClass);
    
    public void moveToClass3(IAttribute pItem, IClassifier pClass);
    
    public void moveToClass4(ETList<IAttribute> list, IClassifier pClass); 												


    // Here is the function that looks for an operation with the same signature.
    // In other words, discovers the redefinition. Notice that this routine does 
    // not assume which is the redefined and which is the redefining. That is 
    // up to the caller of this routine.

    public IOperation discoverRedefinition(IOperation pOperation, ETList<IOperation> opList);

    // This routine is used when a generalization or implementation is created.
    // It looks in the two classes for operations that match signature. Because 
    // we want to keep the functions as generic as possible, we DON'T really
    // want to build the redefinition here. But we cannot just return a single
    // operation, since we want to keep the pairs together. So, I really need
    // to create a typedef pair and a vector of those pair. Until then, I just
    // return a list whose length should always be even, where the first operation
    // is the base op, and the second is the redefining op.

    

    // Here is what the following routines mean.
    // Given two sets:
    // Set1 = {A,B,C}
    // Set2 = {B,C,D}
    // SetU = Union(Set1,Set2) = {A,B,C,D}
    // SetI = Intersect(Set1,Set2) = {B,C}
    // SetS1 = Subtract(Set1,Set2) = {A}
    // SetS2 = Subtract(Set2,Set1) = {D}
    // SetD  = Difference(Set1,Set2) = {A,D} 
    // notice that D(S1,S2) = U(SetS1,SetS2) = Sub(SetU,SetI)

    public ETList<IElement> elementListUnion(ETList<IElement> list1, ETList<IElement> list2);
    public ETList<IElement> elementListIntersect(ETList<IElement> list1, ETList<IElement> list2);
    public ETList<IElement> elementListSubtract(ETList<IElement> list1, ETList<IElement> list2);
    public ETList<IElement> elementListDifference(ETList<IElement> list1, ETList<IElement> list2);

    public ETList<IClassifier> elementListUnion2(ETList<IClassifier> list1, ETList<IClassifier> list2);
    public ETList<IClassifier> elementListIntersect2(ETList<IClassifier> list1, ETList<IClassifier> list2);
    public ETList<IClassifier> elementListSubtract2(ETList<IClassifier> list1, ETList<IClassifier> list2);
    public ETList<IClassifier> elementListDifference2(ETList<IClassifier> list1, ETList<IClassifier> list2);

    public ETList<IOperation> elementListUnion3(ETList<IOperation> list1, ETList<IOperation> list2);
    public ETList<IOperation> elementListIntersect3(ETList<IOperation> list1, ETList<IOperation> list2);
    public ETList<IOperation> elementListSubtract3(ETList<IOperation> list1, ETList<IOperation> list2);
    public ETList<IOperation> elementListDifference3(ETList<IOperation> list1, ETList<IOperation> list2);
    
    public ETList<IElement> classesToElements(ETList<IClassifier> inList);
    

    public ETList<IElement> operationsToElements(ETList<IOperation> inList);
    public ETList<IOperation> elementsToOperations(ETList<IElement> inList);

    // List membership functions

    public boolean isMember(IOperation pItem, ETList<IOperation> pList);
    
    public boolean isMember2(IOperation pItem, ETList<IRedefinableElement> pList);
    
    public boolean isMember3(IClassifier pItem, ETList<IClassifier> pList);
        
    public String attributePrefix (String prefix);
    
    public String getPreferenceKey();
        
    public String getPreferencePath ();

    public String getLanguage();
    
    public String stringFixer(String toFix);
       
    public ETPairT<ETList<IElement>, ETList<IElement>> getAssociatedArtifacts(IElement pElement);

    public String getJavaSourceFile(IArtifact pArtifact, String fileName);

       // Look for existing dependencies (of all types)

    public boolean isDependent(IElement pDependent, IElement pIndependent);
    
    public IPackage getPackage (IElement pElement);
    
    public boolean isTemplateClass(IChangeRequest  pRequest);
    
    public ETList<IClassifier> add(IClassifier pItem);
        
    public List findAttrsForReadAccessor(IOperation oper, IClassifier pClass);
    
    public List findAttrsForWriteAccessor(IOperation oper, IClassifier pClass);
    
    //Jyothi: Added the following two methods to Fix Bug#6327840 
    public boolean doesGetterExist(IAttribute pAttr, IClassifier pClass);
    
    public boolean doesSetterExist(IAttribute pAttr, IClassifier pClass);
        
}
