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



package org.netbeans.modules.uml.ui.support.relationshipVerification;

import org.netbeans.modules.uml.core.metamodel.common.commonactivities.ActivityRelationFactory;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityRelationFactory;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IMultiFlow;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateRelationFactory;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.StateRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.constructs.ConstructsRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IConstructsRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidator;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.DynamicsRelationFactory;
import org.netbeans.modules.uml.core.metamodel.dynamics.IDynamicsRelationFactory;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/*
 *
 * @author KevinM
 *
 */
public class EdgeVerificationImpl implements IEdgeVerification
{
    public EdgeVerificationImpl()
    {
        super();
    }
    
        /*
         * Verifies that the starting node is valid for this relation type.
         */
    public boolean verifyStartNode(IElement pStartNode, String sEdgeMetaTypeString)
    {
        if (pStartNode == null || sEdgeMetaTypeString == null)
            return false;
        
        boolean bValid = true;
        
        try
        {
            String sStartingNodeType = pStartNode.getElementType();
            
            if (isGeneralization(sEdgeMetaTypeString) ||
                    isAssociation(sEdgeMetaTypeString) ||
                    isBinaryAssociation(sEdgeMetaTypeString) ||
                    isAggregation(sEdgeMetaTypeString))
            {
                IClassifier pStartingClassifier = getClassifier(pStartNode);
                
                // These are valid between classifiers (and interfaces for implementations)
                if (pStartingClassifier == null)
                {
                    if (isGeneralization(sEdgeMetaTypeString))
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_GENERALIZATION_NO_CLASSIFIER"));
                    }
                    if (isAssociation(sEdgeMetaTypeString))
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_ASSOCIATION_NO_CLASSIFIER"));
                    }
                    if (isImplementation(sEdgeMetaTypeString))
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_IMPLEMENTATION_NO_CLASSIFIER"));
                    }
                    if (isAggregation(sEdgeMetaTypeString))
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_AGGREGATION_NO_CLASSIFIER"));
                    }
                    bValid = false;
                }
                
                boolean isComponentOrClass = isClass(sStartingNodeType) ||
                        isComponent(sStartingNodeType) ? true : false;
                
                
                // Re-check implementation.  The starting node must be a class
                if (isImplementation(sEdgeMetaTypeString) &&
                        isComponentOrClass == false)
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_IMPLEMENTATION_NO_CLASSIFIER"));
                    bValid = false;
                }
            }
            else if (isDependency(sEdgeMetaTypeString) ||
                    isRealization(sEdgeMetaTypeString))
            {
                INamedElement pStartingNamedElement = getNamedElement(pStartNode);
                
                // These are valid between named elements
                if (pStartingNamedElement == null)
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_DEPENDENCY_NO_NAMEDELEMENT"));
                    bValid = false;
                }
            }
            else if (isMessage(sEdgeMetaTypeString))
            {
                ILifeline pStartingLifeline = getLifeline( pStartNode );
                IInteractionFragment  pStartingFragment = getInteractionFragment( pStartNode );
                
                // These are valid between named ILifelines
                if( (pStartingFragment == null) &&
                        (pStartingLifeline == null) )
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_MESSAGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isRelationActivity(sEdgeMetaTypeString))
            {
                // Valid between activity nodes
                IActivityNode pStartingActivityNode =  getActivityNode(pStartNode);
                if ( pStartingActivityNode == null)
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_ACTIVITYEDGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isTransition(sEdgeMetaTypeString))
            {
                // Valid between activity nodes
                IStateVertex pStartingStateVertex = getStateVertex( pStartNode );
                if ( pStartingStateVertex == null)
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_TRANSITIONEDGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isConnector(sEdgeMetaTypeString))
            {
                // Valid between lifelines
                ILifeline pLifeline = getLifeline(pStartNode);
                if ( pLifeline == null || pLifeline.getRepresentingClassifier() == null)
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_CONNECTOREDGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isExtend(sEdgeMetaTypeString))
            {
                if (pStartNode instanceof IUseCase)
                {
                    bValid = true;
                }
                else
                {
                    bValid = false;
                }
            }
            else if (isInclude(sEdgeMetaTypeString))
            {
                if (pStartNode instanceof IUseCase)
                {
                    bValid = true;
                }
                else
                {
                    bValid = false;
                }
            }
            else if (isDerivation(sEdgeMetaTypeString))
            {
            }
            
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            
            //hr = COMErrorManager::ReportError( e );
            bValid = false;
        }
        return bValid;
    }
    
    
        /*
         * Verifies that the finishing node is valid for this relation type.
         */
    public boolean verifyFinishNode(IElement pStartNode, IElement pFinishNode, String sEdgeMetaTypeString)
    {
        if (pStartNode == null ||
                pFinishNode == null || sEdgeMetaTypeString == null)
            return false;
        
        boolean bValid = true;
        
        try
        {
            // The bValid value input may have meaning, so we must modifiy it if needed
            // For example, the sequence diagram listener sets this value when the mouse is moving.
            
            if (isGeneralization(sEdgeMetaTypeString) ||
                    isAssociation(sEdgeMetaTypeString) ||
                    isAggregation(sEdgeMetaTypeString))
            {
                IClassifier  pStartingClassifier = getClassifier(pStartNode);
                IClassifier  pFinishClassifier = getClassifier(pFinishNode);
                
                // These are valid between classifiers
                if (pStartingClassifier != null && pFinishClassifier != null)
                {
                    if (isGeneralization(sEdgeMetaTypeString))
                    {
                        // Make sure the generalization isn't reflexive.
                        if (!pStartingClassifier.isSame(pFinishClassifier))
                        {
                            // interface can only be subclassed by interface, 79835 & 80551
                            if ((getInterface(pFinishNode)!=null &&
                                    getInterface(pStartNode) == null) ||
                                    (getInterface(pFinishNode)==null &&
                                    getInterface(pStartNode) != null))
                                bValid = false;
                        }
                        else
                            bValid = false;
                    }
                    else
                        bValid = true;
                }
                else
                {
                    if (isGeneralization(sEdgeMetaTypeString))
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_GENERALIZATION_NO_CLASSIFIER"));
                    }
                    else
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_ASSOCIATION_NO_CLASSIFIER"));
                    }
                    bValid = false;
                }
            }
            else if (isImplementation(sEdgeMetaTypeString))
            {
                IClassifier pStartingClassifier = getClassifier(pStartNode);
                IInterface pFinishInterface = getInterface(pFinishNode);
                
                // valid between a classifier and implementation
                bValid = (pStartingClassifier != null && pFinishInterface != null) ? true : false;
                
                //if sEdgeMetaTypeString equal to "implementation" then
                //pStartingClassifier should be type of class or <<class role>>
                // 87500, Java language is a special case, it allows enum to implement
                // an interface
                boolean isJava = false;
                ETList<ILanguage> list = pStartNode.getLanguages();
                if (list.size() > 0 && list.get(0).getName().equals("Java")) // NOI18N
                {
                    isJava = true;
                }
                if(!(pStartingClassifier instanceof IClass) ||
                        (pStartingClassifier instanceof IPartFacade &&
                        !isClass(pStartingClassifier.getTypeConstraint())))
                {
                    if (isJava)
                        bValid = pStartingClassifier instanceof IEnumeration;
                    else
                        bValid = false;
                }
                
                //if sEdgeMetaTypeString equal to "implementation" then
                //pFinishInterface should be type of interface or <<interface role>>
                if(pFinishInterface instanceof IPartFacade &&
                        !pFinishInterface.getTypeConstraint().equals("Interface"))
                {
                    bValid = false;
                }
                if(pFinishNode instanceof IDerivationClassifier)
                {
                    bValid = true;
                }
            }
            else if (isDependency(sEdgeMetaTypeString) ||
                    isRealization(sEdgeMetaTypeString))
            {
                INamedElement  pStartingNamedElement = getNamedElement(pStartNode);
                INamedElement  pFinishNamedElement = getNamedElement(pFinishNode);
                IAssociationEnd  pStartingAssociationEnd = getAssociationEnd(pStartNode);
                IAssociationEnd pFinishAssociationEnd = getAssociationEnd(pFinishNode);
                
                // Valid between named elements, that are not qualifiers (IAssociationEnds)
                if (pStartingNamedElement != null &&
                        pFinishNamedElement != null &&
                        pStartingAssociationEnd == null &&
                        pFinishAssociationEnd == null)
                {
                    // Make sure the generalization isn't reflexive
                    bValid = pStartingNamedElement.isSame(pFinishNamedElement) ? false : true;
                }
                else
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_DEPENDENCY_NO_CLASSIFIER"));
                    bValid = false;
                }
            }
            else if (isMessage(sEdgeMetaTypeString))
            {
                ILifeline pStartingLifeline = getLifeline(pFinishNode);
                ILifeline cpFinishLifeline = getLifeline(pStartNode);
                IInteractionFragment pStartingFragment = getInteractionFragment(pFinishNode);
                IInteractionFragment cpFinishFragment = getInteractionFragment(pStartNode);
                
                // Valid between lifelines elements
                if( (pStartingLifeline != null || pStartingFragment != null) &&
                        (cpFinishLifeline != null || cpFinishFragment != null) )
                {
                    // bValid is set in CSequenceDiagramAddEdgeListener::OnDrawingAreaEdgeMouseMove()
                    bValid = true;
                }
                else
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_MESSAGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isComment(sEdgeMetaTypeString))
            {
                IComment pStartingComment = getComment(pStartNode);
                IComment pFinishComment = getComment(pFinishNode);
                
                if ( ( pStartingComment != null && pFinishComment == null ) ||
                        ( pStartingComment == null && pFinishComment != null ) )
                {
                    bValid = true;
                }
                else
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_COMMENT_LINK_INVALID"));
                    bValid = false;
                }
            }
            else if (sEdgeMetaTypeString.equals("PartFacade"))
            {
                IPartFacade pStartingPartFacade = getPartFacade(pStartNode);
                IPartFacade pFinishPartFacade = getPartFacade(pFinishNode);
                
                ICollaboration pStartingCollaboration = getCollaboration(pStartNode);
                ICollaboration pFinishCollaboration = getCollaboration(pFinishNode);
                
                if ( ( pStartingPartFacade != null && pFinishCollaboration != null ) ||
                        ( pFinishPartFacade != null && pStartingCollaboration != null ) )
                {
                    bValid = true;
                }
                else
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_PARTFACADE_LINK_INVALID"));
                    bValid = false;
                }
            }
            else if (isRelationActivity(sEdgeMetaTypeString))
            {
                IActivityNode pStartingActiviyNode = getActivityNode(pStartNode);
                IActivityNode pFinishActiviyNode = getActivityNode(pFinishNode);
                
                // These are valid between activity nodes
                if (pStartingActiviyNode != null && pFinishActiviyNode != null)
                {
                    bValid = true;
                }
                else
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_ACTIVITYEDGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isTransition(sEdgeMetaTypeString))
            {
                IStateVertex pStartingStateVertex = getStateVertex(pStartNode);
                IStateVertex pFinishStateVertex = getStateVertex(pFinishNode);
                // These are valid between activity nodes
                if (pStartingStateVertex != null && pFinishStateVertex != null)
                {
                    bValid = true;
                }
                else
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_ACTIVITYEDGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isConnector(sEdgeMetaTypeString))
            {
                ILifeline pStartingLifeline = getLifeline(pStartNode);
                ILifeline pFinishLifeline = getLifeline(pFinishNode);
                
                // These are valid between lifelines
                if (pStartingLifeline != null && pFinishLifeline != null)
                {
                    if (pStartingLifeline.getRepresentingClassifier() != null && pFinishLifeline.getRepresentingClassifier() != null)
                    {
                        bValid = true;
                    }
                    else
                    {
                        bValid = false;
                    }
                }
                else
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_CONNECTOREDGE_INVALID"));
                    bValid = false;
                }
            }
            else if (isExtend(sEdgeMetaTypeString))
            {
                if (pStartNode instanceof IUseCase)
                {
                    bValid = true;
                }
                else
                {
                    bValid = false;
                }
            }
            else if (isInclude(sEdgeMetaTypeString))
            {
                if (pStartNode instanceof IUseCase)
                {
                    bValid = true;
                }
                else
                {
                    bValid = false;
                }
            }
            else if (isDerivation(sEdgeMetaTypeString))
            {
                // Cann't derive from your self.
                return pStartNode.isSame(pFinishNode) ? false : true;
            }
            else if (isNestedLink(sEdgeMetaTypeString))
            {
                //	Don't allow reflex nesting, it crashes the DOM 4j.
                bValid = !pFinishNode.isSame(pStartNode);
            }
            else if (sEdgeMetaTypeString.length() == 0)
            {
                // No metatype is created, so we allow this between any objects.
                bValid = true;
            }
        }
        catch ( Exception e )
        {
            //hr = COMErrorManager::ReportError( err );
            e.printStackTrace();
            bValid = false;
        }
        return bValid;
    }
    
        /*
         * Verifies that the edge is valid, if so then the appropriate edge IElement is created and returned
         */
    public IElement verifyAndCreateEdgeRelation(IETEdge pEdge, INamespace pNamespace,
            String sEdgeMetaTypeString, String sInitializationString)
    {
        if (pEdge == null || pNamespace == null || sEdgeMetaTypeString == null)
            return null;
        
        IElement pModelElement = null;
        
        try
        {
            IETNode pFromNode = pEdge.getFromNode();
            IETNode pToNode = pEdge.getToNode();
            
            boolean createdElement = false;
            
            if (pFromNode == null || pToNode == null)
                return null;	// We can not continue with out nodes.
            
            IElement pFromElement = getElement(pFromNode);
            IElement pToElement = getElement(pToNode);
            
            // Get the from and to IElements from the from and to nodes
            if (pFromElement == null || pToElement == null)
                return null;
            
            // Verify that this relationship is ok
            IRelationProxy pRelationProxy = createRelationProxy();
            RelationValidator pRelationValidator = createRelationValidator();
            boolean bRelationshipOK = false;
            
            try
            {
                if (pRelationProxy != null && pRelationValidator != null)
                {
                    pRelationProxy.setFrom(pFromElement);
                    pRelationProxy.setTo(pToElement);
                    pRelationProxy.setConnectionElementType(sEdgeMetaTypeString);
                    
                    // Verify the relation
                    pRelationValidator.validateRelation(pRelationProxy);
                    
                    bRelationshipOK = pRelationProxy.getRelationValidated();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                bRelationshipOK = true;
            }
            
            if (bRelationshipOK == true)
            {
                String actualMetaType =  pRelationProxy.getConnectionElementType();
                
                // Create the correct model element
                IRelationFactory pFactory = createRelationFactory();
                
                if (pFactory == null)
                    return null;	// We can not continue without a factory!
                
                if (isAssociation(actualMetaType) && isBinaryAssociation(actualMetaType) == false)
                {
                    IClassifier pFromClassifier = getClassifier(pFromElement);
                    IClassifier pToClassifier = getClassifier(pToElement);
                    
                    if (pFromClassifier != null && pToClassifier != null)
                    {
                        IAssociation pAssociation = this.createAssociation(pFactory, pNamespace,
                                pFromClassifier,
                                pToClassifier,
                                sInitializationString);
                        
                        if (pAssociation != null)
                            pModelElement = (IElement)pAssociation;
                    }
                    else
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_ASSOCIATION_NO_CLASSIFIER"));
                    }
                }
                else if (isAggregation(actualMetaType))
                {
                    IClassifier pFromClassifier = getClassifier(pFromElement);
                    IClassifier pToClassifier = getClassifier(pToElement);
                    
                    if (pFromClassifier != null && pToClassifier != null)
                    {
                        IAssociation pAggregation = this.createAggregation(pFactory,
                                pNamespace,
                                pFromClassifier,
                                pToClassifier,
                                sInitializationString);
                        
                        if (pAggregation != null)
                            pModelElement = (IElement)pAggregation;
                    }
                    else
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_ASSOCIATION_NO_CLASSIFIER"));
                    }
                }
                else if (isGeneralization(actualMetaType))
                {
                    IClassifier pFromClassifier = getClassifier(pFromElement);
                    IClassifier pToClassifier = getClassifier(pToElement);
                    
                    if (pFromClassifier != null && pToClassifier != null)
                    {
                        IGeneralization pGeneralization = pFactory.createGeneralization(
                                pToClassifier, pFromClassifier);
                        
                        if (pGeneralization != null)
                            pModelElement = (IElement)pGeneralization;
                    }
                    else
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_GENERALIZATION_NO_CLASSIFIER"));
                    }
                }
                else if (isMessage(actualMetaType))
                {
                    
                    pModelElement = processMessage( pEdge,
                            sInitializationString,
                            pFromNode,
                            pToNode,
                            pFromElement,
                            pToElement);
                }
                else if (isDependency(actualMetaType))
                {
                    INamedElement pFromClassifier = getNamedElement(pFromElement);
                    INamedElement pToClassifier = getNamedElement(pToElement);
                    
                    if (pFromClassifier != null && pToClassifier != null)
                    {
                        //IDependency createDependency2(INamedElement client, INamedElement supplier, String depType, INamespace space);
                        // public IDependency createDependency(INamedElement client, INamedElement supplier, INamespace space);
                        IDependency pDependency = pFactory.createDependency(
                                pFromClassifier, pToClassifier, pNamespace);
                        
                        if (pDependency != null)
                            pModelElement = (IElement)pDependency;
                    }
                    else
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_DEPENDENCY_NO_CLASSIFIER"));
                    }
                }
                else if (isDependency2(actualMetaType))
                {
                    INamedElement pFromClassifier = getNamedElement(pFromElement);
                    INamedElement pToClassifier = getNamedElement(pToElement);
                    
                    if (pFromClassifier != null && pToClassifier != null)
                    {
                        IDependency pDependency = pFactory.createDependency2( pFromClassifier,
                                pToClassifier,
                                actualMetaType ,
                                pNamespace);
                        
                        if (pDependency != null)
                            pModelElement = (IElement)pDependency;
                    }
                    else
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_DEPENDENCY_NO_CLASSIFIER"));
                    }
                }
                else if (isBinaryAssociation(actualMetaType))
                {
                    String message = new String("Can't create BinaryAssociations yet");
                    sendErrorMessage(message);
                }
                else if (isRealization(actualMetaType))
                {
                    INamedElement pFromClassifier = getNamedElement(pFromElement);
                    INamedElement pToClassifier = getNamedElement(pToElement);
                    IInterface pFromSupplier = getInterface(pFromElement); // In case this should be an implementation
                    IInterface pToSupplier = getInterface(pToElement);     // In case this should be an implementation
                    
                    // Check to see if we have an implementation
                    if ( pFromSupplier == null &&
                            pFromClassifier != null &&
                            pToSupplier != null)
                    {
                        // We have an implementation
                        IDependency pImplementation =pFactory.createDependency2(
                                pFromClassifier, pToSupplier, new String("Implementation"), pNamespace);
                        
                        if (pImplementation != null)
                            pModelElement = (IElement)pImplementation;
                    }
                    else if (pFromClassifier != null && pToClassifier != null)
                    {
                        IDependency pDependency = pFactory.createDependency2(
                                pFromClassifier, pToClassifier,  new String("Realization"), pNamespace);
                        
                        if (pDependency != null)
                            pModelElement = (IElement)pDependency;
                    }
                    else
                    {
                        sendWarningMessage(RelationshipVerificationResources.getString("IDS_DEPENDENCY_NO_CLASSIFIER"));
                    }
                }
                else if (isImplementation(actualMetaType))
                {
                    // Use the CreateDependency2 to create the implementation
                    IClassifier pClient = getClassifier(pFromElement);
                    IInterface pSupplier = getInterface(pToElement); // The interface
                    
                    if (pClient != null && pSupplier != null )
                    {
                        IDependency pImplementation = pFactory.createDependency2( pClient,
                                pSupplier,	actualMetaType, pNamespace);
                        
                        if (pImplementation != null)
                            pModelElement = (IElement)pImplementation;
                    }
                    else if(pToElement instanceof IDerivationClassifier)
                    {
                        IDerivationClassifier classifier = (IDerivationClassifier)pToElement;
                        IDependency pImplementation = pFactory.createDependency2( pClient,
                                classifier, actualMetaType, pNamespace);
                        
                        if (pImplementation != null)
                            pModelElement = (IElement)pImplementation;
                    }
                }
                else if (isRelationActivity(actualMetaType))
                {
                    IActivityRelationFactory pActivityRelationFactory = createActivityRelationFactory();
                    IActivityNode pActivityFromNode = getActivityNode(pFromElement);
                    IActivityNode pActivityToNode = getActivityNode(pToElement);
                    
                    if (pActivityRelationFactory != null && pActivityFromNode != null && pActivityToNode!= null)
                    {
                        IMultiFlow pActivityEdge = pActivityRelationFactory.createEdge(pActivityFromNode,
                                pActivityToNode,
                                null); // Use the namespace of the pFromNode (as an IActivity)
                        
                        pModelElement = pActivityEdge;
                                                /*
                                                 if (pActivityEdge)
                                                 {
                                                 _VH(pActivityEdge.QueryInterface(&pModelElement));
                                                 }
                                                 */
                    }
                }
                else if (isTransition(actualMetaType))
                {
                    IStateRelationFactory pStateRelationFactory = new StateRelationFactory();
                    
                    IStateVertex pFromVertex = null;
                    if (pFromElement instanceof IStateVertex)
                    {
                        pFromVertex = (IStateVertex)pFromElement;
                    }
                    
                    IStateVertex pToVertex = null;
                    if (pToElement instanceof IStateVertex)
                    {
                        pToVertex = (IStateVertex)pToElement;
                    }
                    
                    if (pFromVertex != null && pToVertex != null)
                    {
                        // Use the container of the pFromNode (as an IActivity)
                        ITransition pNewTrans = pStateRelationFactory.createTransition(pFromVertex, pToVertex, null);
                        
                        if (pNewTrans != null)
                        {
                            pModelElement = pNewTrans;
                        }
                    }
                }
                else if (isConnector(actualMetaType))
                {
                    IDynamicsRelationFactory pDynamicsRelationFactory = new DynamicsRelationFactory();
                    
                    ILifeline pFromLine = null;
                    if (pFromElement instanceof ILifeline)
                    {
                        pFromLine = (ILifeline)pFromElement;
                    }
                    
                    ILifeline pToLine = null;
                    if (pToElement instanceof ILifeline)
                    {
                        pToLine = (ILifeline)pToElement;
                    }
                    
                    if (pFromLine != null && pToLine != null)
                    {
                        IMessageConnector pNewTrans = pDynamicsRelationFactory.createMessageConnector(pFromLine, pToLine);
                        
                        if (pNewTrans != null)
                        {
                            pModelElement = pNewTrans;
                        }
                        else
                        {
                            sendWarningMessage(RelationshipVerificationResources.getString("IDS_LIFELINE_NO_REPRESENTS"));
                        }
                    }
                }
                else if (isExtend(actualMetaType))
                {
                    IConstructsRelationFactory pConstructsRelationFactory = new ConstructsRelationFactory();
                    
                    IUseCase pFromCase = null;
                    if (pFromElement != null)
                    {
                        pFromCase = (IUseCase)pFromElement;
                    }
                    
                    IUseCase pToCase = null;
                    if (pToElement != null)
                    {
                        pToCase = (IUseCase)pToElement;
                    }
                    
                    if (pFromCase != null && pToCase != null)
                    {
                        IExtend pNewTrans = pConstructsRelationFactory.createExtend(pFromCase, pToCase);
                        
                        if (pNewTrans != null)
                        {
                            pModelElement = pNewTrans;
                        }
                    }
                }
                else if (isInclude(actualMetaType))
                {
                    IConstructsRelationFactory pConstructsRelationFactory = new ConstructsRelationFactory();
                    
                    IUseCase pFromCase = null;
                    if (pFromElement != null)
                    {
                        pFromCase = (IUseCase)pFromElement;
                    }
                    
                    IUseCase pToCase = null;
                    if (pToElement != null)
                    {
                        pToCase = (IUseCase)pToElement;
                    }
                    
                    if (pFromCase != null && pToCase != null)
                    {
                        IInclude pNewTrans = pConstructsRelationFactory.createInclude(pFromCase, pToCase);
                        
                        if (pNewTrans != null)
                        {
                            pModelElement = pNewTrans;
                        }
                    }
                }
                else if (isDerivation(actualMetaType))
                {
                    // Use the CreateDerivation to create the derivation relationship
                    IDerivationClassifier pInstanciation =  pFromElement instanceof IDerivationClassifier ? (IDerivationClassifier)pFromElement : null;
                    IClassifier pTemplate = pToElement instanceof IClassifier ? (IClassifier) pToElement : null; // The template
                    
                    if (pInstanciation != null && pTemplate != null)
                    {
                        IDerivation  pDerivation = pFactory.createDerivation(pInstanciation, pTemplate);
                        if (pDerivation != null)
                        {
                            pModelElement = pDerivation;
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            pModelElement = null;
        }
        
        return pModelElement;
    }
    
    /**
     * Create the model element for the message.  This implementation does not
     * do anything with messages.
     *
     * @param pEdge TS edge representing the IMessage to be created
     * @param sInitializationString Original string used to create the Tom Sawyer edge
     * @param pFromNode TS node at the start of the edge
     * @param pToNode TS node at the finish of the edge
     * @param pFromElement  Element associated with the TS node at the start of the edge
     * @param pToElement  Element associated with the TS node at the finish of the edge
     * @param pModelElement
     */
    protected IElement processMessage(IETEdge pEdge,
            String sInitializationString,
            IETNode pFromNode,
            IETNode pToNode,
            IElement pFromElement,
            IElement pToElement)
    {
        // The basic edge verification does nothing with messages.
        return null;
    }
    
    protected IAssociation createAssociation(IRelationFactory pFactory,
            INamespace pNamespace,
            IClassifier pFromClassifier,
            IClassifier pToClassifier,
            String sInitString)
    {
        if (pFactory == null || pFromClassifier == null || pToClassifier == null)
            return null;
        
        IAssociation pAssociation = null;
        try
        {
            if (sInitString != null && sInitString.length() > 0)
            {
                if (sInitString.endsWith("Association NN NA"))
                {
                    pAssociation = pFactory.createAssociation2( pFromClassifier,
                            pToClassifier,
                            AssociationKindEnum.AK_ASSOCIATION,
                            false, //startNavigable
                            true, //endNavigable
                            pNamespace);
                }
                else if (sInitString.endsWith("Association NN NN"))
                {
                    pAssociation = pFactory.createAssociation2( pFromClassifier,
                            pToClassifier,
                            AssociationKindEnum.AK_ASSOCIATION,
                            false, //startNavigable
                            false, //endNavigable
                            pNamespace);
                }
            }
            
            if (pAssociation == null)
            {
                pAssociation = pFactory.createAssociation( pFromClassifier,
                        pToClassifier,
                        pNamespace);
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            pAssociation = null;
            //hr = COMErrorManager::ReportError( err );
        }
        return pAssociation;
    }
    
    IAssociation createAggregation(IRelationFactory pFactory,
            INamespace pNamespace,
            IClassifier pFromClassifier,
            IClassifier pToClassifier,
            String sInitString)
    {
        if (pFactory == null || pFromClassifier == null || pToClassifier == null)
            return null;
        
        IAssociation pAggregation = null;
        try
        {
            if (sInitString != null && sInitString.length() > 0)
            {
                if (sInitString.endsWith("Aggregation CO NN"))
                {
                    pAggregation = pFactory.createAssociation2( pFromClassifier,
                            pToClassifier,
                            AssociationKindEnum.AK_COMPOSITION,
                            false, //startNavigable
                            false, //endNavigable
                            pNamespace);
                }
                else if (sInitString.endsWith("Aggregation AG NN"))
                {
                    pAggregation = pFactory.createAssociation2( pFromClassifier,
                            pToClassifier,
                            AssociationKindEnum.AK_AGGREGATION,
                            false, //startNavigable
                            false, //endNavigable
                            pNamespace);
                }
                else if (sInitString.endsWith("Aggregation CO NA"))
                {
                    pAggregation = pFactory.createAssociation2( pFromClassifier,
                            pToClassifier,
                            AssociationKindEnum.AK_COMPOSITION,
                            false, //startNavigable
                            true, //endNavigable
                            pNamespace);
                }
                else if (sInitString.endsWith("Aggregation AG NA"))
                {
                    pAggregation = pFactory.createAssociation2( pFromClassifier,
                            pToClassifier,
                            AssociationKindEnum.AK_AGGREGATION,
                            false, //startNavigable
                            true, //endNavigable
                            pNamespace);
                }
            }
            
            if (pAggregation == null)
            {
                pAggregation = pFactory.createAssociation2( pFromClassifier,
                        pToClassifier,
                        AssociationKindEnum.AK_AGGREGATION,
                        false, //startNavigable
                        false, //endNavigable
                        pNamespace);
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            pAggregation = null;
            // hr = COMErrorManager::ReportError( err );
        }
        return pAggregation;
    }
    
        /*
         * Create the AssociationClassifier link.
         */
    public IAssociationClass createAssociationClassifierRelation(IETNode pSourceNode, IETNode pTarget)
    {
        if (pSourceNode == null || pTarget == null)
            return null;
        
        IAssociationClass pAssociationClass = null;
        try
        {
            IElement pSourceElement = getElement(pSourceNode);
            IElement pTargetElement = getElement(pTarget);
            
            IClassifier pSourceClassifier = getClassifier(pSourceElement);
            IClassifier pTargetClassifier = getClassifier(pTargetElement);
            
            if (pSourceClassifier != null && pTargetClassifier != null)
            {
                IRelationFactory pFactory = this.createRelationFactory();
                
                if (pFactory != null)
                {
                    IAssociationClass pTempAssociation = pFactory.createAssociationClass(pSourceClassifier,
                            pTargetClassifier,
                            AssociationKindEnum.AK_ASSOCIATION_CLASS,
                            false,
                            false,
                            null);
                    
                    if (pTempAssociation != null)
                        pAssociationClass = (IAssociationClass)pTempAssociation;
                }
                
                if (pAssociationClass == null)
                {
                    sendWarningMessage(RelationshipVerificationResources.getString("IDS_ASSOCIATIONCLASS_COULDNOTCREATE"));
                }
            }
            else
            {
                sendWarningMessage(RelationshipVerificationResources.getString("IDS_ASSOCIATIONCLASSIFIER_PROBLEM"));
            }
        }
        catch (Exception e )
        {
            e.printStackTrace();
            pAssociationClass = null;
        }
        return pAssociationClass;
    }
    
        /*
         * Verifies that this edge can be reconnected.
         */
    public boolean verifyReconnectStart(IElement pReconnectingSideElement, String sEdgeMetaTypeString)
    {
        return true;
    }
    
    // Helpers
    protected IClassifier getClassifier(IElement element)
    {
        if (element instanceof IClassifier)
        {
            return (IClassifier)element;
        }
        return null;
    }
    
    protected INamedElement getNamedElement(IElement element)
    {
        if (element instanceof INamedElement)
        {
            return (INamedElement)element;
        }
        return null;
    }
    
    protected IInteractionFragment getInteractionFragment(IElement element)
    {
        if (element instanceof IInteractionFragment)
        {
            return (IInteractionFragment)element;
        }
        return null;
    }
    
    protected ILifeline getLifeline(IElement element)
    {
        if (element instanceof ILifeline)
        {
            return (ILifeline)element;
        }
        return null;
    }
    
    protected IStateVertex getStateVertex(IElement element)
    {
        if (element instanceof IStateVertex)
        {
            return (IStateVertex)element;
        }
        return null;
    }
    
    protected IActivityNode getActivityNode(IElement element)
    {
        if (element instanceof IActivityNode)
        {
            return (IActivityNode)element;
        }
        return null;
    }
    
    protected IInterface getInterface(IElement element)
    {
        if (element instanceof IInterface)
        {
            return (IInterface)element;
        }
        return null;
    }
    
    protected IAssociationEnd getAssociationEnd(IElement element)
    {
        if (element instanceof IAssociationEnd)
        {
            return (IAssociationEnd)element;
        }
        return null;
    }
    
    protected IComment getComment(IElement element)
    {
        if (element instanceof IComment)
        {
            return (IComment)element;
        }
        return null;
    }
    
    protected IPartFacade getPartFacade(IElement element)
    {
        if (element instanceof IPartFacade)
        {
            return (IPartFacade)element;
        }
        return null;
    }
    
    protected ICollaboration getCollaboration(IElement element)
    {
        if (element instanceof ICollaboration)
        {
            return (ICollaboration)element;
        }
        return null;
    }
    
    protected IElement getElement(IETNode nodeElement)
    {
        return TypeConversions.getElement(nodeElement);
    }
    
    protected IRelationProxy createRelationProxy()
    {
        return new RelationProxy();
    }
    
    protected RelationValidator createRelationValidator()
    {
        return new RelationValidator();
    }
    
    protected IRelationFactory createRelationFactory()
    {
        return new RelationFactory();
    }
    
    protected IActivityRelationFactory createActivityRelationFactory()
    {
        // TODO: Implement IActivityRelationFactory
        return new ActivityRelationFactory();
    }
    
    protected boolean sendWarningMessage(String msg)
    {
        UMLMessagingHelper msgHelper = new UMLMessagingHelper();
        msgHelper.sendWarningMessage(msg);
        return false;
    }
    
    protected boolean sendErrorMessage(String msg)
    {
        UMLMessagingHelper msgHelper = new UMLMessagingHelper();
        msgHelper.sendErrorMessage(msg);
        return false;
    }
    
    // Meta type crackers.
    public boolean isRelationActivity(String actualMetaType)
    {
        return actualMetaType.equals("ActivityEdge") ||
                actualMetaType.equals("ControlFlow")  ||
                actualMetaType.equals("ObjectFlow")  ||
                actualMetaType.equals("MultiFlow") ? true : false;
    }
    
    public boolean isDependency2(String actualMetaType)
    {
        return actualMetaType.equals("Abstraction") ||
                actualMetaType.equals("Delegate") ||
                actualMetaType.equals("Usage") ||
                actualMetaType.equals("Permission") ? true : false;
    }
    
    public boolean isDependency(String actualMetaType)
    {
        return actualMetaType.equals("Dependency");
    }
    
    public boolean isTransition(String actualMetaType)
    {
        return actualMetaType.equals("Transition");
    }
    
    public boolean isMessage(String actualMetaType)
    {
        return actualMetaType.equals("Message");
    }
    
        /*
         * Returns true if the meta string is an Association or BinaryAssociation
         */
    public boolean isAssociation(String actualMetaType)
    {
        return actualMetaType.equals("Association") ||
                isBinaryAssociation(actualMetaType) ? true : false;
    }
    
    public boolean isBinaryAssociation(String actualMetaType)
    {
        return actualMetaType.equals("BinaryAssociation");
    }
    
    public boolean isRealization(String actualMetaType)
    {
        return actualMetaType.equals("Realization");
    }
    
    public boolean isAggregation(String actualMetaType)
    {
        return actualMetaType.equals("Aggregation");
    }
    
    public boolean isGeneralization(String actualMetaType)
    {
        return actualMetaType.equals("Generalization");
    }
    
    public boolean isConnector(String actualMetaType)
    {
        return actualMetaType.equals("Connector");
    }
    
    public boolean isImplementation(String actualMetaType)
    {
        return actualMetaType.equals("Implementation");
    }
    
    public boolean isComment(String actualMetaType)
    {
        return actualMetaType.equals("Comment");
    }
    
    public boolean isExtend(String actualMetaType)
    {
        return actualMetaType.equals("Extend");
    }
    
    public boolean isInclude(String actualMetaType)
    {
        return actualMetaType.equals("Include");
    }
    
    public boolean isDerivation(String actualMetaType)
    {
        return actualMetaType.equals("Derivation");
    }
    
    public boolean isComponent(String actualMetaType)
    {
        return actualMetaType.equals("Component");
    }
    
    public boolean isClass(String actualMetaType)
    {
        return actualMetaType.equals("Class");
    }
    
    public boolean isNestedLink(String actualMetaType)
    {
        return  actualMetaType.equals("NestedLink");
    }
    
    /**
     *
     * From the initialization string determine the message kind.
     *
     * @param sInitializationString Original string used to create the Tom
     *                              Sawyer edge
     *
     * @return The enumerated message kind determined from the initialization
     *         string.  The value will be one of the IMessageKind values.
     * @see org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind
     *
     */
    protected int determineMessageTypeFromInitializationString(String initStr)
    {
        int retVal = IMessageKind.MK_SYNCHRONOUS;
        
        int pos = initStr.lastIndexOf(' ');
        if(pos > 0)
        {
            String modifier = initStr.substring(pos).trim();
            if(modifier.equals("asynchronous") == true)
            {
                retVal = IMessageKind.MK_ASYNCHRONOUS;
            }
            else if(modifier.equals("create") == true)
            {
                retVal = IMessageKind.MK_CREATE;
            }
            else if(modifier.equals("result") == true)
            {
                retVal = IMessageKind.MK_RESULT;
            }
            else if(modifier.equals("Message") == false)
            {
                assert false : "Do we have another message type " + initStr;
            }
        }
        
        return retVal;
    }
}

