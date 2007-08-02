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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import java.util.HashMap;
import java.util.Vector;
import java.util.Collection;
import org.dom4j.Document;
import org.dom4j.Node;
import java.util.List;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.eventframework.IOriginalAndNewEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.AutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementChangeDispatchHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.SourceFileArtifact;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageDataType;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.support.umlsupport.FileManip;
import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.NamedCollection;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramEngine;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;



/**
 * @author sumitabhk
 *
 */
public class Classifier extends Namespace implements IClassifier, 
                                                     IRedefinableElement,
                                                     IParameterableElement,
                                                     IAutonomousElement
{

	public static int NED_INVALID  = 0;
	public static int NED_INBOUND  = 1; // pointing toward this IClassifier
	public static int NED_OUTBOUND = 2; // pointing away from this IClassifier

	IRedefinableElement m_RedefineAggregate = new RedefinableElement();
	IParameterableElement m_ParameterableAggregate = new ParameterableElement();
	IAutonomousElement m_AutonomousAggregate = new AutonomousElement();

	public void setNode(Node n)
	{
		super.setNode(n);
		m_RedefineAggregate.setNode(n);
		m_ParameterableAggregate.setNode(n);
		m_AutonomousAggregate.setNode(n);
	}

	/**
	 *
	 * Gets the abstract flag on this classifier.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsAbstract() 
	{
		return getBooleanAttributeValue( "isAbstract", false );
	}

	/**
	 *
	 * Sets the abstract flag on this classifier.
	 *
	 * @param newVal[in] The new value
	 *
	 * @return HRESULT
	 *
	 */
	public void setIsAbstract(boolean value) 
	{
		boolean isAbstract = getIsAbstract();
		// No need to set if the values are same
		if (isAbstract != value)
		{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = (IClassifierEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.classifier());
			boolean proceed = true;
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("ClassifierPreAbstractModified");
				proceed = disp.fireClassifierPreAbstractModified(this, value, payload);
			}
			
			if (proceed)
			{
				setBooleanAttributeValue("isAbstract", value);
				if (disp != null)
				{
					IEventPayload payload = disp.createPayload("ClassifierAbstractModified");
					disp.fireClassifierAbstractModified(this, payload);
				}
			}
			else
			{
				//cancel the event
			}
		}
	}

	/**
	 *
	 * Gets the leaf property on this Classifier
	 *
	 * @param pVal [out] The value
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsLeaf() 
	{
		return getBooleanAttributeValue("isLeaf", false);
	}

	/**
	 *
	 * Sets the leaf property on this Classifier
	 *
	 * @param newVal[in] The new value
	 *
	 * @return HRESULT
	 *
	 */
	public void setIsLeaf(boolean value) 
	{
		boolean isLeaf = getIsLeaf();
		// No need to set if the values are same
		if (isLeaf != value) {
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp = (IClassifierEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.classifier());
			boolean proceed = true;
			if (disp != null) {
				IEventPayload payload = disp.createPayload("PreLeafModified");
				proceed = disp.firePreLeafModified(this, value, payload);
			}

			if (proceed) {
				setBooleanAttributeValue("isLeaf", value);
				if (disp != null) {
					IEventPayload payload = disp.createPayload("LeafModified");
					disp.fireLeafModified(this, payload);
				}
			} else {
				//cancel the event
			}
		}
	}

	/**
	 *
	 * Adds a generalization relationship to this classifier where this
	 * classifier plays the specific/sub class role.
	 *
	 * @param gen [in] 
	 *
	 * @return HRESULT
	 *
	 */
	public void addGeneralization(IGeneralization generalization) 
	{	
		final IGeneralization gen = generalization;
		new ElementConnector<IClassifier>().addChildAndConnect(this, false, 
							"UML:Classifier.generalization", 
							"UML:Classifier.generalization", gen,
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									gen.setSpecific(obj);
								 }
							 }										
							);
	}

	/**
	 *
	 * Removes a generalization from this classifier where this classifier plays 
	 * the specific/sub class role.
	 *
	 * @param gen [in] 
	 *
	 * @return 
	 *
	 */
	public void removeGeneralization(IGeneralization generalization) 
	{
		final IGeneralization gen = generalization;
		new ElementConnector<IClassifier>().removeElement(this, gen, 
                                           "UML:Classifier.generalization/*",
										   new IBackPointer<IClassifier>() 
										   {
											  public void execute(IClassifier obj) 
											  {
												  gen.setSpecific(obj);
											  }
										  }
										);
	}

	/**
	 *
	 * Retrieves the collection of generalization relationships this Classifier
	 * plays the specific /sub class role.
	 *
	 * @param pVal [out] 
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IGeneralization> getGeneralizations() 
	{
		ElementCollector<IGeneralization> collector = new ElementCollector<IGeneralization>();
		
		return collector.retrieveElementCollection(m_Node, "UML:Classifier.generalization/*", IGeneralization.class);
	}

	/**
	 *
	 * Adds a generalization relationship to this classifier where this classifier
	 * plays the general / super class role.
	 *
	 * @param gen [in] 
	 *
	 * @return
	 *
	 */
	public void addSpecialization(IGeneralization generalization) 
	{
		final IGeneralization gen = generalization;
		new ElementConnector<IClassifier>().addChildAndConnect(this, true, "specialization", "specialization", 
							   gen,
								new IBackPointer<IClassifier>() 
								{
									public void execute(IClassifier obj) 
									{
										gen.setGeneral(obj);
									}
								}	
							  );		 
	}

	/**
	 *
	 * Removes a generalization from this classifier where this classifier plays
	 * the general / super class role.
	 *
	 * @param gen [in] 
	 *
	 * @return
	 *
	 */
	public void removeSpecialization(IGeneralization generalization) 
	{
		final IGeneralization gen = generalization;
		new ElementConnector<IClassifier>().removeByID(this, gen, "specialization",
										   new IBackPointer<IClassifier>() 
										   {
											  public void execute(IClassifier obj) 
											  {
												gen.setGeneral(obj);
											  }
										  }
										); 
	}

	/**
	 *
	 * Retrieves the collection of generalization relationships this Classifier
	 * plays the general / super class role.
	 *
	 * @param newVal[in] The collection of generalization relationships
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IGeneralization> getSpecializations() 
	{
		ElementCollector<IGeneralization> collector = new ElementCollector<IGeneralization>();
		return collector.retrieveElementCollectionWithAttrIDs(this, "specialization", IGeneralization.class);
	}

	/**
	 *
	 * Description
	 *
	 * @param imp [in]
	 *
	 * @return 
	 *
	 */
	public void addImplementation(IImplementation imp) 
	{
		addClientDependency(imp);
	}

	/**
	 *
	 * Description
	 *
	 * @param imp [in] 
	 *
	 * @return 
	 *
	 */
	public void removeImplementation(IImplementation imp) 
	{
		removeClientDependency(imp);
	}

	/**
	 *
	 * Retrieves the Implementations that this Classifier is playing the client role
	 * in.
	 *
	 * @param pVal[out] The collection of Implementations
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IImplementation> getImplementations() 
	{
		ETList<IDependency> depsList = getClientDependenciesByType("Implementation");
		
		CollectionTranslator<IDependency, IImplementation> trans = 
            new CollectionTranslator<IDependency, IImplementation>();
		return trans.copyCollection(depsList);
	}

	/**
	 *
	 * Adds collaboration for the classifier. 
	 *
	 * @param col [in]
	 *
	 * @return
	 *
	 */
	public void addCollaboration(ICollaborationOccurrence col)
	{
		addChild("UML:Classifier.collaboration", "UML:Classifier.collaboration", col );
	}

	/**
	 *
	 * Removes the collaboration for the classifier. 
	 *
	 * @param col [in]
	 *
	 * @return
	 *
	 */
	public void removeCollaboration(ICollaborationOccurrence col) 
	{
		UMLXMLManip.removeChild(getNode(), col);
	}

	/* 
	 * Gets the list of collaborations for the classifier.
	 */
	public ETList<ICollaborationOccurrence> getCollaborations() 
	{
		ElementCollector<ICollaborationOccurrence> collector = 
													new ElementCollector<ICollaborationOccurrence>();
		return collector.retrieveElementCollection(m_Node, "UML:Classifier.collaboration/*", ICollaborationOccurrence.class);
	}

	/**
	 *
	 * Gets the representation object for this classifier	 
	 *
	 * @return ICollaborationOccurrence.
	 *
	 */
	public ICollaborationOccurrence getRepresentation() 
	{
		ElementCollector<ICollaborationOccurrence> collector = 
							new ElementCollector<ICollaborationOccurrence>();
 		return collector.retrieveSingleElement(m_Node, "UML:Classifier.representation/*", ICollaborationOccurrence.class);
	}

	/**
	 *
	 * Sets the representation node for this classifier.
	 *
	 * @param col [in] 
	 *
	 * @return 
	 *
	 */
	public void setRepresentation(ICollaborationOccurrence value) 
	{
		addChild("UML:Classifier.representation", "UML:Classifier.representation", value );
	}

	/**
	 *
	 * Adds a behaviour to this classifier.
	 *
	 * @param behavior [in] 
	 *
	 * @return 
	 *
	 */
	public void addBehavior(IBehavior behavior) 
	{
		final IBehavior behav = behavior;
		new ElementConnector<IClassifier>().addChildAndConnect(this, false, 
							"UML:Classifier.ownedBehavior", 
							"UML:Classifier.ownedBehavior", behav,
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									behav.setContext(obj);
								 }
							 }										
							);
	}

	/**
	 *
	 * removes a behavior from the classifier
	 *
	 * @param behavior [in] 
	 *
	 * @return 
	 *
	 */
	public void removeBehavior(IBehavior behavior)
	{
		final IBehavior behav = behavior;
		new ElementConnector<IClassifier>().removeElement(this, behav, "UML:Classifier.ownedBehavior/*",
										   new IBackPointer<IClassifier>() 
										   {
											  public void execute(IClassifier obj) 
											  {
												behav.setContext(obj);
											  }
										  }
										);		 
	}

	/**
	 *
	 * Gets all the behaviors for this classifier	 
	 *
	 * @return ETList<IBehavior> - the generic ETList of IBehaviors.
	 *
	 */
	public ETList<IBehavior> getBehaviors() 
	{
		ElementCollector<IBehavior> collector =	new ElementCollector<IBehavior>();		
		return collector.retrieveElementCollection(m_Node, "UML:Classifier.ownedBehavior/*", IBehavior.class);
	}

	/**
	 *
	 * Gets the classifierBehavior for this classifier.
	 *
	 * @param behavior [out] 
	 *
	 * @return 
	 *
	 */
	public IBehavior getClassifierBehavior() 
	{
		ElementCollector<IBehavior> collector = new ElementCollector<IBehavior>();
		return collector.retrieveSingleElement(m_Node, "UML:Classifier.classifierBehavior/*", IBehavior.class);
	}

	/**
	 *
	 * Sets the classifier behavior
	 *
	 * @param behavior [in] 
	 *
	 * @return 
	 *
	 */
	public void setClassifierBehavior(IBehavior value) 
	{
		addChild("UML:Classifier.classifierBehavior", "UML:Classifier.classifierBehavior", value);
	}

	/**
	 *
	 * adds the increment object for this classifier.
	 *
	 * @param inc [in] 
	 *
	 * @return 
	 *
	 */
	public void addIncrement(IIncrement inc) 
	{
		final IIncrement increment = inc;
		new ElementConnector<IClassifier>().addChildAndConnect(this, false, 
							"UML:Classifier.increment", 
							"UML:Classifier.increment", increment,
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									increment.setPartialClassifier(obj);
								 }
							 }										
							);		 
	}

	/**
	 *
	 * removes the specified increment from this classifier.
	 *
	 * @param inc [in] 
	 *
	 * @return 
	 *
	 */
	public void removeIncrement(IIncrement inc) 
	{
		final IIncrement increment = inc;
		new ElementConnector<IClassifier>().removeElement(this, increment, "UML:Classifier.increment/*",
										   new IBackPointer<IClassifier>() 
										   {
											  public void execute(IClassifier obj) 
											  {
												increment.setPartialClassifier(obj);
											  }
										  }
										);
	}

	/**
	 *
	 * gets all the increment objects associated with this Classifier
	 *
	 * @param incs [in] 
	 *
	 * @return the generic ETList of IIncrement.
	 *
	 */
	public ETList<IIncrement> getIncrements() 
	{
		ElementCollector<IIncrement> collector = new ElementCollector<IIncrement>();
		return collector.retrieveElementCollection(m_Node, "UML:Classifier.increment/*", IIncrement.class);
	}

	/**
	 *
	 * Retrieve all the Features of this Classifier. This includes Attributes,
	 * Operations, Methods, etc.	  
	 *
	 * @return ETList<IFeature> the generic ETList of features.
	 *
	 */
	public ETList<IFeature> getFeatures() 
	{
		Node node = getNode();
		ETList<IAttribute> attrs = getAttributes();
		ETList<IOperation> opers = getOperations();

        ETList<IFeature> features = new ETArrayList<IFeature>(); 
		
		CollectionTranslator<IAttribute,IFeature> transAttr = new 
							CollectionTranslator<IAttribute,IFeature>();
		transAttr.addToCollection(attrs,features);       
		
		CollectionTranslator<IOperation,IFeature> transOper = new 
							CollectionTranslator<IOperation,IFeature>();
		transOper.addToCollection(opers,features);
		
		return features.size() > 0? features : null;
	}

	/*
	 * Does not do anything right now
	 */
	public void setFeatures(ETList<IFeature> value)
	{
		// TODO: Implement
	}

	/**
	 *
	 * Adds the feature to this Classifier.
	 *
	 * @param feature[in] The feature to add
	 *
	 * @return HRESULTs
	 *
	 */
        public void addFeature(IFeature feat)
        {
            if (feat != null)
            {
                // Pop the context that has been plugging events
                // for feature.
                revokeEventContext(feat);
                
                IElementChangeDispatchHelper helper = new ElementChangeDispatchHelper();

//                if (Util.hasNameCollision(
//                    this, feat.getName(), feat.getElementType(), feat))
//                {
//                    return;
//                }
                
                EventDispatchRetriever ret = EventDispatchRetriever.instance();
                IClassifierEventDispatcher disp = (IClassifierEventDispatcher) ret.getDispatcher(
                    EventDispatchNameKeeper.classifier());
                boolean proceed = true;
                if (disp != null)
                {
                    IEventPayload payload = disp.createPayload("FeaturePreAdded");
                    proceed = disp.fireFeaturePreAdded(this, feat, payload);
                }
                
                if (proceed)
                {
                    addElement(feat);
                    if (disp != null)
                    {
                        IEventPayload payload = disp.createPayload("FeatureAdded");
                        disp.fireFeatureAdded(this, feat, payload);
                    }
                    else
                    {
                        proceed =false;
                    }
                }
            }
        }

	/**
	 *
	 * Inserts a new feature into this classifier's list of features immediately before the 
	 * existing feature passed in. If existingFeature is null, then the new feature is appended 
	 * to the end of the features list
	 *
	 * @param existingFeature[in] The existing feature to insert before. Can be 0.
	 * @param newFeature[in]      The new feature to insert.
	 *
	 * @return 
	 *
	 */
	public void insertFeature(IFeature existingFeature, IFeature newFeature) 
	{
		insertNode((org.dom4j.Element)null, existingFeature, newFeature);
		 
	}

	/**
	 *
	 * Removes the feature from this Classifier.
	 *
	 * @param feat[in] The Feature to remove
	 *
	 * @return HRESULT
	 *
	 */
	public void removeFeature(IFeature feat) 
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
				(IClassifierEventDispatcher) ret.getDispatcher(
					EventDispatchNameKeeper.classifier());
		boolean proceed = true;
		if (disp != null) {
			IEventPayload payload = disp.createPayload("FeaturePreRemoved");
			proceed = disp.fireFeaturePreRemoved(this, feat, payload);
		}

		if (proceed) 
		{	
			final IFeature feature = feat;
			new ElementConnector<IClassifier>().removeElement(this, feature, 
                                               "UML:Classifier.feature/*",
											   new IBackPointer<IClassifier>() 
											   {
												  public void execute(IClassifier obj) 
												  {
													feature.setFeaturingClassifier(obj);
												  }
											  }
											);
			
			if (disp != null) {
				IEventPayload payload = disp.createPayload("FeatureRemoved");
				disp.fireFeatureRemoved(this, feat, payload);
			}
		} else {
			//cancel the event
		}
		 
	}

	/**
	 * Retrieves the collection Attribute elements that this Classifier owns.
	 *
	 * @param pVal[out] The collection of Attributes
	 *
	 * @return HRESULTs
	 */
	public ETList<IAttribute> getAttributes() 
	{
		ElementCollector<IAttribute> collector = new ElementCollector<IAttribute>();
		return collector.retrieveElementCollection(m_Node, 
												   "./UML:Element.ownedElement/UML:Attribute", IAttribute.class);
	}

	/**
	 * Retrieves the collection of Operation elements that this Classifier owns.
	 *
	 * @param pVal[out] The operation collection
	 *
	 * @return HRESULTs
	 */
	public ETList<IOperation> getOperations()
	{
		ElementCollector<IOperation> collector = new ElementCollector<IOperation>();
		return collector.retrieveElementCollection(m_Node,
													"./UML:Element.ownedElement/UML:Operation", IOperation.class);
	}

	/**
	 * Adds a new Attribute to this Classifier.
	 *
	 * @param newVal[in] The new Attribute
	 *
	 * @return HRESULTs
	 */
	public void addAttribute(IAttribute newVal) 
	{
		addFeature(newVal);
	}

	/**
	 * Adds a new Operation to this Classifier.
	 *
	 * @param newVal[in] The new operation
	 *
	 * @return HRESULTs
	 */
	public void addOperation(IOperation newVal) 
	{
		addFeature(newVal);
	}

	/**
	* Creates a new attribute with the passed-in information. The new attribute
	* is returned. NOTE: the attribute is NOT added to this Classifier.
	*
	* @param newType[in] The type of this attribute. If the type is not found
	*             in the model, a dummy DataType will be created with
	*             that type as the name. If 0 or "" is passed, a default type is
	*             used.
	* @param newName[in] The name of the attribute. If 0 or "" is passed, a default
	*                    name is used.	
	*
	* @return IAttribute - the newly created attribute.
	* 
	* @todo Log a warning if more than one type is actually found
	*
	*/
	public IAttribute createAttribute(String type, String name) 
	{
            boolean makeSureToCreateType = false;
            
            IAttribute retAttr = null;
            if (type == null || type.length() == 0)
            {
                // Set the type to a default setting. Hardcoded for now
                //type = "int";
                ICoreProduct prod = ProductRetriever.retrieveProduct();
                if (prod != null)
                {
                    ILanguageManager lMan = prod.getLanguageManager();
                    if (lMan != null)
                    {
                        ILanguageDataType pDataType =
                                lMan.getAttributeDefaultType(this);
                        if (pDataType != null)
                        {
                            type = pDataType.getName();
                            
                            makeSureToCreateType = true;
                        }
                    }
                }
            }
            else
            {
                ETList < ILanguage > languages = getLanguages();
                for(ILanguage language : languages)
                {
                    makeSureToCreateType = language.isDataType(type);
                }
            }
            
            if (name == null || name.length() == 0)
            {
                name = retrieveDefaultName();
            }
            
            // IZ 80953 - When creating a attribute with the default type, we
            // need to make sure that the UnknowClassifierCreate is not set to
            // "NO".  If the preference is set to "No" then the operation will 
            // not be created.  Since we get the return type from the language
            // datatypes, we should assume that it should be present in the 
            // system.  After we retrieve the type, we should make sure that 
            // the preference is set to the original state.

            IClassifier clazz = resolveSingleClassifierFromString(type);
            
            if (clazz != null)
                retAttr = createAttribute2(clazz, name);
            return retAttr;
        }

	/**
	 *
	 * Creates a new operation with the passed-in information.
	 *
	 * @param newRetType[in] The name of the type that will be the return
	 *							 type of the new operation. If null or "" is passed, a
	 *                    default type will be used.
	 * @param newName[in] The name of the operation. If 0 or "" is passed, a
	 *                    default name will be used. 
	 * @return IOperation - the newly created operation.	 
	 */
	public IOperation createOperation(String retType, String name)
        {
            IOperation retOper = null;
            
            boolean makeSureToCreateType = false;
            if (retType == null || retType.length() == 0)
            {
                // Set the type to a default setting. Hardcoded for now
                //type = "int";
                ICoreProduct prod = ProductRetriever.retrieveProduct();
                if (prod != null)
                {
                    ILanguageManager lMan = prod.getLanguageManager();
                    if (lMan != null)
                    {
                        ILanguageDataType pDataType = lMan.getOperationDefaultType(this);
                        if (pDataType != null)
                        {
                            retType = pDataType.getName();
                            makeSureToCreateType = true;
                        }
                    }
                }
            }
            else
            {
                ETList < ILanguage > languages = getLanguages();
                for(ILanguage language : languages)
                {
                    makeSureToCreateType = language.isDataType(retType);
                }
            }
            
            if (name == null || name.length() == 0)
            {
                name = retrieveDefaultName();
            }
            
            // IZ 80953 - When creating a attribute with the default type, we
            // need to make sure that the UnknowClassifierCreate is not set to
            // "NO".  If the preference is set to "No" then the operation will 
            // not be created.  Since we get the return type from the language
            // datatypes, we should assume that it should be present in the 
            // system.  After we retrieve the type, we should make sure that 
            // the preference is set to the original state.
            PreferenceAccessor pref = PreferenceAccessor.instance();
            boolean curCreateValue = pref.getUnknownClassifierCreate();
            if((makeSureToCreateType == true) && (curCreateValue == false))
            {
                pref.setPreferenceValue("", 
                                        "NewProject|UnknownClassifier", 
                                        "UnknownClassifierCreate",
                                        "PSK_YES");
            }
            
            IClassifier clazz = resolveSingleClassifierFromString(retType);
            
            if((makeSureToCreateType == true) && (curCreateValue == false))
            {
                pref.setPreferenceValue("", 
                                        "NewProject|UnknownClassifier", 
                                        "UnknownClassifierCreate",
                                        "PSK_NO");
            }
            
            if (clazz != null)
            {
                retOper = createOperation2(clazz, name);
            }
            
            return retOper;
        }

	/**
	 *
	 * Attempts to find a single classifier in this classifier's namespace or 
	 * above namespaces. If more than one classifier is found with the same name, 
	 * only the first one is used.
	 *
	 * @param typeName[in] The name to match against	 
	 *
	 * @return IClassifier
	 */
	private IClassifier resolveSingleClassifierFromString(String retType) 
	{
		IClassifier retClass = null;
		INamedElement element = resolveSingleTypeFromString(retType);
		if (element != null && element instanceof IClassifier)
		{
			retClass = (IClassifier)element;
		}
		return retClass;
	}

	/**
	* Creates a new attribute with the passed-in information. The new attribute
	* is returned. NOTE: the attribute is NOT added to this Classifier.
	*
	* @param type[in] The type of this attribute
	* @param name[in] The name of the attribute
	*
	* @return IAttribute - the newly created attribute.
	*/
	public IAttribute createAttribute2(IClassifier type, String name) 
	{
		IAttribute retAttr = null;
		FactoryRetriever ret = FactoryRetriever.instance();
		Object obj = ret.createType("Attribute", null);
		if (obj != null && obj instanceof IAttribute)
		{
			retAttr = (IAttribute)obj;
			establishEventContext(retAttr);
			if (retAttr instanceof ITypedElement)
			{
				ITypedElement element = (ITypedElement)retAttr;
				if (element != null)
				{
					element.setType(type);
				}			
			}
			retAttr.setName(name);
		}
		return retAttr;
	}

	/**
	 *
	 * Creates a new Operation with the passed-in information.
	 *
	 * @param retType[in] The Classifier that represents the return type
	 * @param name[in] The name of the Operation	 
	 *
	 * @return IOperation. the newly created operation.
	 */
	public IOperation createOperation2(IClassifier retType, String name) 
	{
		IOperation retOper = null;
		FactoryRetriever ret = FactoryRetriever.instance();
		Object obj = ret.createType("Operation", null);
		if (obj != null && obj instanceof IOperation)
		{
			retOper = (IOperation)obj;
			establishEventContext(retOper);
			if (retType != null)
			{
				IParameter parm = retOper.createParameter2(retType, "");
				retOper.setReturnType(parm);
			}
			retOper.setName(name);
		}
		return retOper;
	}

	/**
	 *
	 * Creates a new Attribute, giving it a default type and name.	  
	 *
	 * @return IAttribute - the newly created attribute.
	 *
	 */
	public IAttribute createAttribute3() 
	{
		return createAttribute(null, null);
	}

	/**
	 *
	 * Creates a new operation, giving it a default return type and name.
	 *
	 * @param newOper[out] The new operation
	 *
	 * @return HRESULT
	 *
	 */
	public IOperation createOperation3() 
	{
		return createOperation(null, null);
	}

	/**
	 *
	 * Adds the association end to this classifier's list of ends.
	 *
	 * @param end [in] 
	 *
	 * @return 
	 *
	 */
	public void addAssociationEnd(IAssociationEnd assocEnd) 
	{
		final IAssociationEnd end = assocEnd;
		new ElementConnector<IClassifier>().addChildAndConnect(this, true, 
							"associationEnd", 
							"associationEnd", end,
							 new IBackPointer<IClassifier>() 
							 {
								 public void execute(IClassifier obj) 
								 {
									end.setParticipant(obj);
								 }
							 }										
							);
	}

	/**
	 *
	 * Removes the passed-in end from this classifier's list.
	 *
	 * @param end [in] 
	 *
	 * @return 
	 *
	 */
	public void removeAssociationEnd(IAssociationEnd assocEnd) 
	{
		final IAssociationEnd end = assocEnd;
		new ElementConnector<IClassifier>().removeByID
							(this,end, "associationEnd",							 
							 new IBackPointer<IClassifier>() 
							 {
								public void execute(IClassifier obj) 
								{
									end.setParticipant(obj);
								}
							}
						   );		 
	}

	/**
	 *
	 * Retrieves the collection of IAssociationEnd objects this Classifier
	 * is a participant on.
	 *
	 * @param ends [out] 
	 *
	 * @return 
	 *
	 */
	public ETList<IAssociationEnd> getAssociationEnds() 
	{
		ElementCollector<IAssociationEnd> collector = new ElementCollector<IAssociationEnd>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"associationEnd", IAssociationEnd.class);
	}

	/**
	 *
	 * Transforms this classifier into another.
	 *
	 * @param typeName[in] The name of another Classifier-derived type
	 * @param newForm[out] The new object
	 *
	 * @return HRESULT
	 *
	 */
	public IClassifier transform(String typeName) 
	{
		IClassifier newForm = null;

		if (Util.hasNameCollision(this.getOwningPackage(), this.getName(), typeName, this))
		{
			DialogDisplayer.getDefault().notify(
							new NotifyDescriptor.Message(NbBundle.getMessage(
								DiagramEngine.class, "IDS_NAMESPACECOLLISION")));
							
			return null;
		}
		
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
				(IClassifierEventDispatcher) ret.getDispatcher(
					EventDispatchNameKeeper.classifier());
			boolean proceed = true;
			if (disp != null) {
				IEventPayload payload = disp.createPayload("PreTransform");
				proceed = disp.firePreTransform(this, typeName, payload);
			}

			if (proceed) {
				if (isKnownClassifier(typeName))
				{
					// Notice that if the type name coming in is not known,
					// we won't fire the post method. The idea is that we
					// will fire the pre so that if any addin wants to handle the
					// transform they can, and then they are responsible for firing
					// the post

					// Need to tell the FactoryRetriever to remove the COM object
					// that this impl class represents
					newForm = transformNode(typeName);

					if (disp != null) {
						IEventPayload payload = disp.createPayload("Transformed");
						disp.fireTransformed(newForm, payload);
					}
				}
			} else {
				//cancel the event
			}
		return newForm;
	}


	
	/**
	 *
	 * Determines whether or not the name coming in is the name of a Classifier
	 * derived type.
	 *
	 * @param typeName[in] The name to check against
	 *
	 * @return HRESULT
	 *
	 */

	protected boolean isKnownClassifier( String typeName )
	{
		boolean known = false;
		if( typeName != null &&  typeName.length() > 0 )
		{
		   if( typeName.equals("Actor") ||
			   typeName.equals("Class") ||
			   typeName.equals("Association") ||
			   typeName.equals("Aggregation") ||
			   typeName.equals("Component") ||
			   typeName.equals("DataType") ||
			   typeName.equals("UseCase") ||
			   typeName.equals("Collaboration") ||
			   typeName.equals("Interface") ||
			   typeName.equals("Signal") ||
			   typeName.equals("Artifact") ||
			   typeName.equals("Enumeration") ||
			   typeName.equals("Node"))
		   {
			  known = true;
		   }
		}
		return known;
	}

	/**
	 *
	 * Performs the actual transformation of the node.
	 *
	 * @param typeName[in] The name of the Classifier type to transform the node to
	 * @param newForm[out] The new object
	 *
	 * @return HRESULT
	 *
	 */
	protected IClassifier transformNode(String typeName)
	{
		IClassifier retClass = null;
		preTransformNode(typeName);
		Object obj = UMLXMLManip.transformElement(this, typeName);
		if (obj != null)
		{
			retClass = (IClassifier)obj;

			// If the new form is not an interface then remove the interface stereotype
			if (retClass instanceof IInterface)
			{}
			else
			{
				retClass.removeStereotype2("interface");
			}
		}
		return retClass;
	}

	/**
	 *
	 * This routine is overloaded here so that certain stereotypes
	 * are removed before a transform is done.
	 *
	 * @param typeName[in] The type to be transformed to
	 *
	 * @return HRESULT
	 *
	 */
	protected void preTransformNode(String typeName )
	{
		// If we are being transformed to an interface, we have to
		// have the stereotype now.
		if (typeName.equals("Interface"))
		{
			ensureStereotype( "interface" );
		}
                // 86370, attributes and operations do not make sense to actor type
                else if (typeName.equals("Actor"))
                {
                    deleteAttributesAndOperations();
                }
	}
        
        
        private void deleteAttributesAndOperations()
        {
            for (IOperation op : getOperations())
            {
                op.delete();
            }
            for (IAttribute attr : getAttributes())
            {
                attr.delete();
            }
        }

	/**
	 *
	 * Retrieves all the collection of associations this Classifier participates in.
	 *
	 * @param assocs[out] The collection of associations.
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IAssociation> getAssociations() 
	{
		ETList<IAssociationEnd> ends = getAssociationEnds();
      ETList<IAssociation> assocs = new ETArrayList<IAssociation>();
		if (ends != null)
		{
			int count = ends.size();
			for (int i=0; i<count; i++)
			{
				IAssociationEnd end = ends.get(i);
				if (end != null)
            {
               assocs.addIfNotInList( end.getAssociation() );
            }
			}
		}
		return assocs;
	}

	/**
	 *
	 * Creates a new operation that has the same name as this Classifier and whose
	 * Constructor property is set to True.
	 *
	 * @param newOper[out] The new operation
	 *
	 * @return HRESULT
	 *
	 */
	public IOperation createConstructor() 
	{
		return createLifeTimeOperation( true );
	}

    
    /**
     *
     * Creates an Operation with the same name as this Classifier, whose
     * Destructor property is true..
     *
     * @param newOper [out] 
     *
     * @return 
     *
     */
    public IOperation createDestructor()
    {
       return createLifeTimeOperation(false);   
    }

	/**
	 *
	 * Retrieves all the Navigable ends on the other side of the association that this
	 * Classifier is referencing. These ends are significant in that they result in the
	 * modification of this Classifier's feature list. In other words, to fully comprehend
	 * all the attributes that make up this Classifier's attribute list, you must ask
	 * for the NavigableEnds collection as well, as NavigableEnd is derived off of
	 * Attribute.
	 *
	 * @param pEnds[out] The collection of NavigableEnds
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INavigableEnd> getNavigableEnds()
    {
		ETList<INavigableEnd> navigableEnds = new ETArrayList<INavigableEnd>();
		ETList<IAssociationEnd> ends = getAssociationEnds();
		if (ends != null)
		{
			int count = ends.size();
			for (int i=0; i<count; i++)
			{
				IAssociationEnd end = ends.get(i);
				// Now get the other side of the association
				// and check for NavigableEnds
				if (end != null)
				{
					ETList<IAssociationEnd> otherEnds = end.getOtherEnd();
					if (otherEnds != null)
					{
						int otherEndCount = otherEnds.size();
						for (int j=0; j<otherEndCount; j++)
						{
							IAssociationEnd otherEnd = otherEnds.get(j);
							if (otherEnd instanceof INavigableEnd)
							{
								navigableEnds.add( (INavigableEnd)otherEnd );
							}
						}
					}
				}
			}
		}
		return navigableEnds;
	}

	/**
	 *
	 * Determines whether or not this Classifier is persisted or not.
	 *
	 * @param pVal [out] 
	 *
	 * @return 
	 *
	 */
	public boolean getIsTransient() 
	{
		return getBooleanAttributeValue("isTransient", true);
	}

	/**
	 *
	 * Determines whether or not this Classifier is persisted or not.
	 *
	 * @param newVal [in] 
	 *
	 * @return 
	 *
	 */
	public void setIsTransient(boolean value) 
	{
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
			IClassifierEventDispatcher disp =
				(IClassifierEventDispatcher) ret.getDispatcher(
					EventDispatchNameKeeper.classifier());
			boolean proceed = true;
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("PreTransientModified");
				proceed = disp.fireClassifierPreTransientModified(this, value, payload);
			}

			if (proceed)
			{
				setBooleanAttributeValue("isTransient", value);
				if (disp != null)
				{
					IEventPayload payload = disp.createPayload("TransientModified");
					disp.fireClassifierTransientModified(this, payload);
				}
			}
			else
			{
				//cancel the event
			}
	}

	/**
	 *
	 * Retrieves all the features that are redefining other features. 
	 *
	 * @param pVal[out] The NamedCollections object that contains NamedCollections of Features 
	 *                  redefining other features. Each NamedCollection is named according to 
	 *                  the name of the Classifier that containes the redefined feature. The data
	 *                  property of the NamedCollection is an IFeature collection.
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamedCollection> getRedefiningFeatures() 
	{
		String query = "./UML:Element.ownedElement/*[ string-length(@redefinedElement) > 0 and ( ( name() = 'UML:Operation' ) or ( name() = 'UML:Attribute' )) ]";
        ElementCollector<IFeature> collector = new ElementCollector<IFeature>();
        ETList<IFeature> elems = collector.retrieveElementCollection(getNode(), query, IFeature.class);		    

		if (elems != null)
		{
			HashMap< String, INamedCollection >  collection = new HashMap < String, INamedCollection >(); 
			int count = elems.size();
			for (int i=0; i<count; i++)
			{
				IFeature feature = elems.get(i);
				long refCount = feature.getRedefinedElementCount();
				if (refCount > 0)
				{
					ETList<IRedefinableElement> refElems = feature.getRedefinedElements();
					gatherRedefinedElements(feature, refElems, collection);
				}
			}
            
            return new ETArrayList<INamedCollection>(collection.values());
		}
		return null;
	}

	/**
	 *
	 * Retrieves all the IAttribute elements this Classifier owns that are redefining other
	 * IAttributes in super classes or implemented interfaces.
	 *
	 * @param pVal[out] A NamedCollections object. The Data property of each NamedCollection object
	 *                  contains an IAttributes collection.
	 *                  
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamedCollection> getRedefiningAttributes() 
    {
		RedefinableElementFilter filter = new RedefinableElementFilter(this, IAttribute.class);
		return filter.filter();
	}

	/**
	 *
	 * Retrieves all the IOperation elements this Classifier owns that are redefining other
	 * IOperations in super classes or implemented interfaces.
	 *
	 * @param pVal[out] A NamedCollections object. The Data property of each NamedCollection object
	 *                  contains an IOperations collection.
	 *                  
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamedCollection> getRedefiningOperations() 
    {
		RedefinableElementFilter filter = new RedefinableElementFilter(this, IOperation.class);
		return filter.filter();
	}

	/**
	 *
	 * Retrieves all the features on this Classifier that DO NOT override / redefine other
	 * features.
	 *
	 * @param pVal[out] The collection holding the features
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IFeature> getNonRedefiningFeatures() 
    {
		ETList<IAttribute> attrs = getNonRedefiningAttributes();
		ETList<IOperation> opers = getNonRedefiningOperations();
        
        ETList<IFeature> features = new ETArrayList<IFeature>();
		CollectionTranslator<IAttribute,IFeature> transAttr = new 
							CollectionTranslator<IAttribute,IFeature>();
		transAttr.addToCollection(attrs,features);       
		
		CollectionTranslator<IOperation,IFeature> transOper = new 
							CollectionTranslator<IOperation,IFeature>();
		transOper.addToCollection(opers,features);

		return features;
	}
	
	/**
	 *
	 * Retrieves all the attributes on this classifier that are not redefining other attributes,
	 *
	 * @param pVal[out] The collection holding the attributes, else 0 if none found
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IAttribute> getNonRedefiningAttributes() 
	{
		String query = "./UML:Element.ownedElement/UML:Attribute[ not(@redefinedElement) or @redefinedElement = '']";
		ElementCollector<IAttribute> collector = new ElementCollector<IAttribute>();
		return collector.retrieveElementCollection(m_Node, query, IAttribute.class);
	}

	/**
	 *
	 * Retrieves all the operations on this classifier that are not redefining other operations.
	 *
	 * @param pVal[out] The collection holding the operation, else 0 if none found
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IOperation> getNonRedefiningOperations() 
	{
		String query = "./UML:Element.ownedElement/UML:Operation[ not(@redefinedElement) or @redefinedElement = '']";
		ElementCollector<IOperation> collector = new ElementCollector<IOperation>();
		return collector.retrieveElementCollection(m_Node, query, IOperation.class);
	}

	/**
	 *
	 * @see NamespaceImpl::EstablishNodeAttributes()
	 *
	 */
	public void establishNodeAttributes( org.dom4j.Element node )
	{
	   super.establishNodeAttributes( node );
	}

	/**
	 *
	 * Adds a template parameter to this Classifier, thus making this classifier
	 * a templated classifier
	 *
	 * @param pParm[in] The template parameter
	 *
	 * @return HRESULT
	 * @note Since the Classifier takes ownership of the parameter, we are also
	 *       keeping track of the xmi ids of the parameters in an xml attribute 
	 *       called "templateParameter" for easy retrieval of the parameters. This
	 *       is needed as there are a number of types that derive off of IParameterableElement.
	 *       If we just put these type into the ownedElement element, we would not
	 *       be able to differentiate them from other owned elements.
	 *
	 */
	public void addTemplateParameter(IParameterableElement pParm)
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
				(IClassifierEventDispatcher) ret.getDispatcher(
					EventDispatchNameKeeper.classifier());
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreTemplateParameterAdded");
			proceed = disp.firePreTemplateParameterAdded(this, pParm, payload);
		}

		if (proceed)
		{
			addOwnedElement(pParm);
			// Now need to keep track of the ID so that we can easily
			// retrieve the templates later
			addElementByID(pParm, "templateParameter");
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("TemplateParameterAdded");
				disp.fireTemplateParameterAdded(this, pParm, payload);
			}
		}
		else
		{
			//cancel the event
		}
		 
	}

	/**
	 *
	 * Removes a template parameter from this Classifier 
	 *
	 * @param pParm[in] The parameter to remove
	 *
	 * @return HRESULT
	 *
	 */
	public void removeTemplateParameter(IParameterableElement pParm) 
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
			(IClassifierEventDispatcher) ret.getDispatcher(
				EventDispatchNameKeeper.classifier());
		boolean proceed = true;
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreTemplateParameterRemoved");
			proceed = disp.firePreTemplateParameterRemoved(this, pParm, payload);
		}

		if (proceed)
		{
			removeElement(pParm);
			removeElementByID(pParm, "templateParameter");
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("TemplateParameterRemoved");
				disp.fireTemplateParameterRemoved(this, pParm, payload);
			}
		}
		else
		{
			//cancel the event
		}
		 
	}

	/**
	 *
	 * Is the argument a template parameter of this classifier?
	 *
	 * @param pParm[in] The parameter to query to see if it's a template of this classifier
	 * @param bIsTemplateParameter[out,retval] true if this guy is a template parameter
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getIsTemplateParameter(IParameterableElement pParm) 
	{
		boolean isTemp = false;
		ETList<IParameterableElement> elems = getTemplateParameters();
		if (elems != null)
		{
			int count = elems.size();
			for (int i=0; i<count; i++)
			{
				IParameterableElement elem = elems.get(i);
				boolean isSame = elem.isSame(pParm);
				if (isSame)
				{
					isTemp = true;
					break;
				}
			}
		}
		return isTemp;
	}

	/**
	 *
	 * Retrieves the formal template parameters owned by this Classifier 
	 *
	 * @param pParms[out] The parameters
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IParameterableElement> getTemplateParameters() 
	{		
		ElementCollector<IParameterableElement> collector = 
											new ElementCollector<IParameterableElement>();
		ETList<IParameterableElement> parametableElements = 
						collector.retrieveElementCollectionWithAttrIDs(this, "templateParameter", IParameterableElement.class);
		
		return parametableElements;
	}
    
    public String getTemplateParametersAsString()
    {
        ETList<IParameterableElement> paramsList = getTemplateParameters();
        if (paramsList == null || paramsList.size() == 0)
            return "";
        
        StringBuffer paramsBuff = new StringBuffer();
        
        for (IParameterableElement param: paramsList)
        {
            paramsBuff.append(param.getName()).append(", ");
        }
        
        // return all but the last ", "
        return paramsBuff.substring(0, paramsBuff.length()-2);
    }

	/**
	 *
	 * Retrieves the Derivation relationship this Classifier is participating in. The existence of
	 * this relationship means that this Classifier is a template instance of an existing templated
	 * classifier.
	 *
	 * @param pVal[out] The relationship
	 *
	 * @return HRESULT
	 *
	 */
	public IDerivation getDerivation() 
	{
		ElementCollector<IDerivation> collector = new ElementCollector<IDerivation>();
		return collector.retrieveSingleElement(m_Node, "UML:Element.ownedElement/UML:Derivation", IDerivation.class);
	}

	/**
	 *
	 * Sets this Classifier into a Derivation relationship
	 *
	 * @param newVal[in] The relationship
	 *
	 * @return HRESULT
	 * @see get_Derivation()
	 *
	 */
	public void setDerivation(IDerivation value) 
	{
		addElement(value);
		value.setClient(this);
	}

	/** 
	 * Returns a list of all NavigableEnds that aim away from this Classifier.
	 * 
	 * @param pNavigableEnds[out] the list of NavigableEnds.
	 */
	public ETList<INavigableEnd> getOutboundNavigableEnds() 
    {
		return getNavigableEndsByDirection(NED_OUTBOUND);
	}

	/** 
	 * Returns a list of all NavigableEnds that aim towards this Classifier.
	 * 
	 * @param pNavigableEnds[out] the list of NavigableEnds.
	 */
	public ETList<INavigableEnd> getInboundNavigableEnds() 
    {
		return getNavigableEndsByDirection(NED_INBOUND);
	}

	/** 
	 * Returns a list of NavigableEnds that are connected to this IClassifer and that are
	 * pointing in the direction specified by @a direction
	 * 
	 * @param direction[in] the direction that the navigable ends should be pointing
	 * @param ppNavigableEnds[out] the navigable ends
	 */
	private ETList<INavigableEnd> getNavigableEndsByDirection(int direction)
	{
		// Create the list of Navigable Ends.  Even if there aren't any outbound
		// navigable ends we'll still return a (empty) list.
		ETList<INavigableEnd> navigableEnds = new ETArrayList<INavigableEnd>();

		// Get a list of all association ends that touch this
		// classifier.
		ETList<IAssociationEnd> assocEnds = getAssociationEnds();
		
		// If there are any association ends in the list...
		if (assocEnds != null && assocEnds.size() > 0)
		{
			int count = assocEnds.size();			
			for (int i=0; i < count; i++)
			{
				IAssociationEnd end = assocEnds.get(i);

                ETPairT<INavigableEnd, Integer> navInf =
                        getNavigableEndAndDirection(end);
				INavigableEnd navEnd = navInf.getParamOne();
                int endDirection = navInf.getParamTwo().intValue();
				if (navEnd != null && endDirection == direction)
				{
					navigableEnds.add(navEnd);
				}
			}
		}
		return navigableEnds;
	}

	/** 
	 * Given an IAssociationEnd that is connected to this IClassifier, this 
     * operation determines what direction the associated INavigableEnd is 
     * pointing and returns that INavigableEnd.
	 * 
	 * @param pAssociationEnd an IAssociationEnd that is connected to this 
     *                        IClassifier
	 * 
     * @return An <code>ETPairT</code> with the INavigableEnd and navigability
     *         direction. The navigability direction <code>Integer</code> is 
     *         guaranteed to be non-null.
	 */
	protected ETPairT<INavigableEnd, Integer> 
        getNavigableEndAndDirection(IAssociationEnd assocEnd)
	{
        int dir = NED_INVALID;

		INavigableEnd retEnd = null;
		// First see if it's inbound
		if (assocEnd instanceof INavigableEnd)
		{
			retEnd = (INavigableEnd)assocEnd;
			dir = NED_INBOUND;
		}
		else
		{
			// Maybe it's outbound.      
			// Get the other ends.
			ETList<IAssociationEnd> otherEnds = null;
			otherEnds = assocEnd.getOtherEnd();
			if (otherEnds != null)
			{
				// If there is one or more "other ends"...
				int count = otherEnds.size();
				if (count > 0)
				{
					// Get the first "other end".  NOTE: Not
					// handling cases where there are more than 2
					// other ends.
					IAssociationEnd otherEnd = otherEnds.get(0);

					// If the other end is a NavigableEnd, add it to our list
					// of outbound navigable ends.
					if (otherEnd instanceof INavigableEnd)
					{
						retEnd = (INavigableEnd)otherEnd;
						dir = NED_OUTBOUND;
					}
				}
			}
		}
		return new ETPairT<INavigableEnd, Integer>(retEnd, new Integer(dir));
	}

	/**
	 *
	 * Retrieves the redefined attributes that this Classifier owns
	 *
	 * @param pVal[out] The collection
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IAttribute> getRedefiningAttributes2() 
    {
		ETList<IAttribute> retAttrs = new ETArrayList<IAttribute>();
		ETList<INamedCollection> cols = getRedefiningAttributes();
		if (cols != null)
		{
			int count = cols.size();
			CollectionTranslator<IFeature, IAttribute> tr = new CollectionTranslator<IFeature, IAttribute>();
			for (int i=0; i<count; i++)
			{
				INamedCollection col = cols.get(i);
				Object obj = col.getData();
				if (obj != null)
				{
					ETList<IFeature> attrs = new ETArrayList<IFeature>((Collection)obj);
					tr.addToCollection(attrs, retAttrs);
				}
			}
		}
		return retAttrs;
	}

	/**
	 *
	 * Retrieves the redefined operations that this Classifier owns
	 *
	 * @param pVal[out] The collection
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IOperation> getRedefiningOperations2() 
    {
		ETList<IOperation> retOpers = new ETArrayList<IOperation>();
		ETList<INamedCollection> cols = getRedefiningOperations();
        
		if (cols != null)
		{
			int count = cols.size();
			for (int i=0; i<count; i++)
			{
				INamedCollection col = cols.get(i);
				Object obj = col.getData();
				if (obj != null)
				{
					CollectionTranslator<IOperation,IOperation> transOper = new 
										CollectionTranslator<IOperation,IOperation>();
					transOper.addToCollection(new ETArrayList<IOperation>((Collection)obj),retOpers);					
				}			
			}
		}
		return retOpers;
	}

	/**
	 *
	 * Retrieves the default value that can be used to initialize this type.
	 *
	 * @param pVal[out]  The initialization value
	 *
	 * @return HRESULT
	 * 
	 */
	public String getDefaultTypeValue() 
	{
		String defaultValue = "0";
		ETList<ILanguage> langs = getLanguages();
		if (langs != null)
		{
			int count = langs.size();
			if (count > 0)
			{
				String name = getName();
				boolean foundDataType = false;
				for (int i=0; i<count; i++)
				{
					ILanguage lang = langs.get(i);
					ILanguageDataType langType = lang.getDataType(name);
					if (langType != null)
					{
						defaultValue = langType.getDefaultValue();
						foundDataType = true;
						break;
					}
					else
					{
						ILanguageDataType operType = lang.getOperationDefaultType();
						if (operType != null)
						{
							defaultValue = operType.getDefaultValue();
						}
					}
				}
				
				if ((defaultValue == null || defaultValue.length() <= 0) 
                    && !foundDataType)
				{
					ILanguage lang = langs.get(0);
					defaultValue = lang.getDefault("UnknownDataType Initialization");
				}
			}
		}
		return defaultValue;
	}

	/**
	 *
	 * Retrieves the first attribute found that matches the passed in name
	 *
	 * @param attrName[in]  The name of the attribute to retrieve
	 * @param pAttr[out]    The found attribute, if any
	 *
	 * @return HRESULT
	 * @note If there are more than one Attribute, the first one is retrieved
	 *
	 */
	public IAttribute getAttributeByName(String attrName) 
	{
		IAttribute retAttr = null;
		ETList<IAttribute> attrs = getAttributesByName(attrName);
		if (attrs != null && attrs.size() > 0)
		{
			retAttr = attrs.get(0);
		}
		return retAttr;
	}

	/**
	 *
	 * Retrieves all the Attributes by the passed in name
	 *
	 * @param attrName[in]  The name to match against
	 * @param pAttrs[out]   The collection
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IAttribute> getAttributesByName(String attrName) 
	{
        ETList<IAttribute> attributes = null;
		if (attrName.length() > 0)
		{
			String query = "UML:Element.ownedElement/UML:Attribute[@name=\""
                + attrName + "\"]";
            ElementCollector<IAttribute> collector = new ElementCollector<IAttribute>();
			attributes = collector.retrieveElementCollection(
                getNode(), query, IAttribute.class);
		}
		return attributes;
	}

	/**
	 *
	 * Retrieves all Attributes and out bound NavigableEnds by matching their names against the passed in name
	 *
	 * @param attrName[in]  The name to match against
	 * @param pAttrs[out]   The found attributes and ends
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IAttribute> getAttributesAndNavEndsByName(String attrName) 
	{
		ETList<IAttribute> retAttrsList = new ETArrayList<IAttribute>();

        ETList<IAttribute> curAttrs = getAttributesByName(attrName);       
		ETList<INavigableEnd> curEnds = getOutboundNavigableEnds();
		if (curAttrs != null || curEnds != null)
		{
			 if (curAttrs != null)
			 {
			 	retAttrsList = curAttrs;
			 }
			 if (curEnds != null)
			 {
			 	int count = curEnds.size();
			 	for (int i=0; i<count; i++)
			 	{
			 		INavigableEnd end = curEnds.get(i);
			 		String name = end.getName();
			 		if (name != null && name.equals(attrName))
			 		{
			 			retAttrsList.add(end);		 			
					}
				}
			}
		}
		return retAttrsList;
	}

	/**
	 *
	 * Retrieves all the operations by the passed in name
	 *
	 * @param operName[in]  The operation name to match against
	 * @param pOpers[out]   The collection
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IOperation> getOperationsByName(String operName) 
    {
        ETList<IOperation> operations = null;
    	if (operName.length() > 0)
		{
			String query = "UML:Element.ownedElement/UML:Operation[@name=\"";
			query += operName;
			query += "\"]";
	
            ElementCollector<IOperation> collector = new ElementCollector<IOperation>();
    		operations = collector.retrieveElementCollection(m_Node, query, IOperation.class);
			
        }
	    return operations;       
	}

	/**
	 *
	 * Creates a new EventContext that will be propogated to all 
	 * EventDispatchers on the Product's EventDispatchController.
	 * This Context will prevent events from firing when initiated 
	 * from the passed-in element.
	 *
	 * @param feature[in] The feature to create the context and 
	 *                    EventFilter with
	 *
	 * @return HRESULT
	 *
	 */
	protected void establishEventContext( IFeature feature )
	{
		EventContextManager man = new EventContextManager();
		man.establishVersionableElementContext(this, feature, null);
	}

	/**
	 *
	 * Pops any event context that has a filter on it with an
	 * ID that matches the XMI ID of the element passed into this
	 * method.
	 *
	 * @param feature[in] The element to match against
	 *
	 * @return HRESULT
	 *
	 */
	protected void revokeEventContext(IFeature feature)
	{
		EventContextManager man = new EventContextManager();
		man.revokeEventContext(feature, null);
	}

	/**
	 *
	 * Retrieves the controller on the current product.
	 *
	 * @param cont[out] The dispatch controller
	 *
	 * @return HRESULT
	 *
	 */
	protected IEventDispatchController getController()
	{
		IEventDispatchController cont = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			cont = prod.getEventDispatchController();
		}
		return cont;
	}
	
	/**
	 *
	 * Handles the creation of either a constructor operation or destructor operation,
	 * dependent on isConstructor.
	 *
	 * @param isConstructor[in] - true to create a constructor, else
	 *                          - false to create a destructor
	 * @param newOper[out] The new operation
	 *
	 * @return HRESULT
	 *
	 */
	protected IOperation createLifeTimeOperation(boolean isConstructor)
	{
		String name = getName();
		IOperation oper = createOperation2(null, name);
		if (oper != null)
		{
			if (isConstructor)
			{
				oper.setIsConstructor(true);
			}
			else
			{
				oper.setIsConstructor(false);
			}
		}
		return oper;
	}
	
	/**
	 *
	 * Removes all elements that should also be deleted or at least modified
	 * when this element is deleted.
	 *
	 * @param thisElement[in] The COM object representing this element
	 *
	 * @return HRESULT
	 *
	 */
	protected void performDependentElementCleanUp( IVersionableElement thisElement )
	{

	}
	
	public IVersionableElement performDuplication()
	{
		 ETList<IParameterableElement> pElements = getTemplateParameters();		 
		 if( pElements != null)
		 {
			int numParms = 0;
			numParms = pElements.size();

			for( int i = 0; i < numParms; i++ )
			{
			   IParameterableElement pParm = pElements.get(i);
			   if( pParm != null)
			   {
				  removeTemplateParameter( pParm );
			   }
			}
		  }

		 IClassifier dup = (IClassifier)performDuplication();
		 if( dup != null)
		 {
			performDuplicationProcess( dup);
			duplicateTemplateParms( dup, pElements );
		 }
		 return dup;	  
	}
	
	protected void duplicateGeneralizations(IClassifier dup)
	{
	  ETList<IGeneralization> gens = dup.getGeneralizations();
	  establishDuplicatedGeneralizations( true, gens, dup );
        	
	  gens = dup.getSpecializations() ;
	  establishDuplicatedGeneralizations( false, gens, dup );
	}
	
	/**
	 *
	 * Duplicates every Generalization passed in via the gens collection. This method
	 * then makes sure that each end of the newly Duplicated Generalization knows of
	 * the new Generalization.
	 *
	 * @param isGeneral True if the collection of generalizations was a result
	 *                          of the get_Generalization call performed on
	 *                          dupClassifier, else false if the collection was a result
	 *                          of the get_Specializations
	 * @param gens The collection of Generalizations
	 * @param dup The duplicated Classifier
	 *	 
	 *
	 */
	protected void establishDuplicatedGeneralizations(boolean isGeneral,
													  ETList<IGeneralization> gens,
													  IClassifier dup)
    {
        int num = gens.size();
		if( num > 0)
		{		   
		   for( int i = 0; i < num; i++ )
		   {
			  IGeneralization gen = gens.get(i);
			  if( gen != null)
			  {
				 IVersionableElement ver = gen.duplicate();
				 IGeneralization dupGen = (IGeneralization) ver;
				 if( dupGen != null)
				 {
					Node scoping = dup.getNode();
				    RedefinableElement elemm = new RedefinableElement();
				    elemm.replaceIds(dup, dupGen);
				    
					// If isGeneral is true, that means that the
					// generalization collection coming in was retrieved
					// via a get_Generalizations off the duplicated
					// named element passed in ( dupNamed ). That means
					// that we need to make sure we connect with the 
					// CLIENT end, as the supplier end was taken care
					// of with the ReplaceIDs call

					if( isGeneral )
					{
					   IClassifier general = dupGen.getGeneral();
					   if( general != null)
					   {
						  general.addSpecialization( dupGen );
					   }
					}
					else
					{
					   IClassifier specific = dupGen.getSpecific();					   
					   if( specific != null)
					   {
						  specific.addGeneralization( dupGen );
					   }
					}

					// Now, the duplicated classifier has a reference to 
					// the original generalization, but we want it to have a reference
					// to the duplicated generalization. We cannot use Remove and Add
					// because those routines are "smart" in that they try to 
					// keep everything synchronized instead of just removing a node.

					String depID = gen.getXMIID();
					String dupeID = dupGen.getXMIID();					
					Node dupeNode = dup.getNode(); 
					
					RedefinableElement elem = new RedefinableElement();
					elem.replaceIds(depID, dupeID, dupeNode,".//");					
				 }
			  }
		   }
		}
    }

	/**
	 *
	 * Duplicates all the CollaborationOccurrences belonging to dup.
	 *
	 * @param dup IClassifier - The duplicated Classifier
	 *	 
	 *
	 */												  
	protected void duplicateCollaborations(IClassifier dup)
	{
		ETList<ICollaborationOccurrence> collabs = dup.getCollaborations();
		if( collabs != null)
		{
		   int num = collabs.size();	            
		   for( int i = 0; i < num; i++ )
		   {
			  ICollaborationOccurrence collab = collabs.get(i);			  
			  if( collab != null)
			  {
				 IVersionableElement ver = collab.duplicate();				 
				 ICollaborationOccurrence dupCollab =(ICollaborationOccurrence)ver;
				 if( dupCollab != null )
				 {					
					replaceIds( dup, dupCollab );
				 }
			  }
		   }
		}
	}
	
	/**
	 *
	 * Duplicates all the Behaviors belonging to dupClassifier.
	 *
	 * @param dup The duplicated Classifier
	 *	 
	 *
	 */
	protected void duplicateBehaviors(IClassifier dup)
	{
		ETList<IBehavior> behavior = dup.getBehaviors();   		

	    if( behavior != null)
	    {
		  int num = behavior.size(); 
 		  for( int i = 0; i < num; i++ )
   		  {
			IBehavior behav = behavior.get(i);			
			if( behavior != null)
			{
			   IVersionableElement ver = behav.duplicate();
			   IBehavior dupBehavior = (IBehavior) ver;
			   if( dupBehavior != null)
			   {				
			   	  replaceIds(dup,dupBehavior);				  
			   }
			}
		 }
	  }		
	}
	
	protected void duplicateIncrements(IClassifier dup)
	{
		ETList<IIncrement> increments = dup.getIncrements();   		

		if( increments != null)
		{
		  int num = increments.size(); 
		  for( int i = 0; i < num; i++ )
		  {
			IIncrement increment = increments.get(i);			
			if( increment != null)
			{
			   IVersionableElement ver = increment.duplicate();
			   IIncrement dupIncrement = (IIncrement) ver;
			   if( dupIncrement != null)
			   {				
				replaceIds(dup,dupIncrement);				  
			   }
			}
		 }
	  }		
	}
	/**
	 *
	 * Performs the actions specific to Classifier during a Duplate procedure.
	 *
	 * @param dup The duplicated Classifier
	 *
	 */
	protected void performDuplicationProcess(IClassifier dup)
	{
		duplicateGeneralizations( dup );
		duplicateCollaborations( dup );
		duplicateBehaviors( dup );
		duplicateIncrements( dup );
		replaceIds( dup, dup );
	}

	/**
	 *
	 * Gathers all the redefined elements in the passed-in collection, grouping them
	 * by the name of the Classifier in which they belong.
	 *
	 * @param redefiningFeature[in] The feature that is redefining the features in redElems
	 * @param redElems[in] The collection of redefined features
	 * @param collections[out] The modified CollectionsMap
	 *
	 * @return HRESULT
	 *
	 */
	protected void gatherRedefinedElements( IFeature redefiningFeature, 
	                                        ETList<IRedefinableElement> redElems, 
	                                        HashMap < String, INamedCollection > collection )
	{
		if (redElems != null)
		{
			int count = redElems.size();
			for (int i=0; i<count; i++)
			{
				IRedefinableElement elem = redElems.get(i);
				if (elem instanceof IFeature)
				{
					IFeature redFeature = (IFeature)elem;
					IClassifier classifier = redFeature.getFeaturingClassifier();
					if (classifier != null)
					{
						String name = classifier.getName();
						if (name.length() > 0)
						{
							addFeatureToRedefiningCollection( name, redefiningFeature, collection ); 
						}
					}
				}
			}
		}
	}
	
	/**
	 *
	 * Adds the passed-in redefining feature to a NamedCollection that matches the passed in
	 * classifier name. That NamedCollection is added to or modified on the collections map.
	 *
	 * @param classifierName[in] The name of the Classifier that holds the feature that is
	 *                           being redefined by redefingFeature
	 * @param redefiningFeature[in] The feature doing the redefinition work
	 * @param collections[out] The collections map to be modified
	 *
	 * @return HRESULT
	 *
	 */
	protected void addFeatureToRedefiningCollection( String classifierName, IFeature redefiningFeature, HashMap < String, INamedCollection > collection)
	{
		Object obj = collection.get(classifierName);
		if (obj == null)
		{
			ETList<IFeature> features = new ETArrayList<IFeature>();
			features.add(redefiningFeature);
			INamedCollection col = new NamedCollection();
			col.setName(classifierName);
			col.setData(features);
			collection.put(classifierName, col);
		}
		else
		{
			INamedCollection col = (INamedCollection)obj;
			Object data = col.getData();
			if (data != null)
			{
				Collection features = (Collection)data;
				features.add(redefiningFeature);
			}
		}
	}
	
	/**
	 *
	 * Overrides the NamedElement method of the same signature in order to handle 
	 * the impact that changing the name of a Classifier can entail. Since a Classifier
	 * can be used as a type wherever an element makes use of an ITypedElement,
	 * we have to be extremely sensitive to the name of a Classifier changing.
	 * If we determine that a change in the Classifier name is about to occur,
	 * we must discover any and every type that is referencing this Classifier.
	 * This is largely due to the fact that source code must be updated and GUI
	 * must be refreshed. If the model is in Analysis or Design mode, or we are
	 * not using a product that has a GUI component, there will be no impact search.
	 *
	 * This method should only be called if it is already safe to set the name
	 * of this element and fire the post name change event. This means that
	 * the pre-name modified event has already been dispatched.
	 * 
	 * This method is called from the NamedElementImpl::SetNewNameValue() 
	 * operation.
	 *
	 * @param newName[in]         The new name 
	 * @param fireModEvent[in]    true if events should fire, else false
	 * @param helper[in]          The dispatcher to fire the events with
	 * @param element[in]         The object representing this NamedElement
	 *
	 * @return HRESULT
	 *
	 */
	public void setNewNameValue( String newName, boolean fireModEvent, 
								IElementChangeDispatchHelper helper, 
								INamedElement element )
	{
		ETList<IVersionableElement> impactedElems = null;

		// Determine whether or not we are in a state that necesitates
		// an impact analysis
		if (fireModEvent && performImpactAnalysis(newName))
		{
			impactedElems = retrieveImpactedElements();
		}
		
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IClassifierEventDispatcher disp =
				(IClassifierEventDispatcher) ret.getDispatcher(
					EventDispatchNameKeeper.classifier());
		boolean proceed = true;
		if (impactedElems != null)
		{
			// Dispatch the pre impacted event
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("PreImpacted");
				if (payload instanceof IOriginalAndNewEventPayload)
				{
					IOriginalAndNewEventPayload pChangePayload = (IOriginalAndNewEventPayload)payload;
					String curName = getName();
					pChangePayload.setOriginalValue(curName);
					pChangePayload.setNewValue(newName);
				}
				proceed = disp.firePreImpacted(this, impactedElems, payload);
			}
		}

		// Make the actual name change
		if (proceed)
		{
			//NamedElement.setNewNAmeValue
			super.setNewNameValue(newName, fireModEvent, helper, element);

			if (impactedElems != null && disp != null)
			{
				// Dispatch the post impact event
				IEventPayload payload = disp.createPayload("Impacted");
				disp.fireImpacted(this, impactedElems, payload);
			}
		}
		else
		{
			//cancel the event
		}
	}
	
	/**
	 *
	 * Determines whether or not an impact analysis
	 * query should be performed.
	 * 
	 * @param newName[in] The proposed new name for this Classifier
	 *
	 * @return true if the query should be performed, else
	 *         false if not needed
	 *
	 */
	protected boolean performImpactAnalysis( String str )
	{
		boolean perform = false;
		String defaultName = retrieveDefaultName();
		String curName = getName();
		
		// No need to perform an impact test if the user is naming
		// the element for the first time
		if (curName != null && !curName.equals(defaultName))
		{
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			if (prod != null)
			{
				boolean isGui = false;
				isGui = prod.isGUIProduct();
				IProject proj = getProject();
				if (proj != null)
				{
					int rtMode = 0; //RT_OFF
					String mode = proj.getMode();
					IRoundTripController rtController = prod.getRoundTripController();
					if (rtController != null)
					{
						rtMode = rtController.getMode(); 
					}

					// Impact analysis will be performed if:
					//
					// 1. 
					// We must assume that a GUI will
					// need to refresh various components, so an impact analysis
					// is needed.
					//
					// 2.
					// The project this Classifier is a part of is in Design or
					// implementation mode AND Rountrip is NOT OFF
					//
					// 3. Make sure the current classifer is not an IAssociation
					//    ( see issue W2161 )

					String nodeName = getElementType();
					if (nodeName.length() > 0)
					{
						if (!nodeName.equals("Association") && !nodeName.equals("Aggregation"))
						{
							if (isGui || !mode.equals("Analysis") && (rtMode != 0))
							{
								perform = true;
							}
						}
					}
				}
			}
		}
		return perform;
	}

	/**
	 *
	 * Retrieves all elements that are referencing this element in a typed
	 * element fashion. 
	 *
	 * @param impacted[out] The collection of elements impacted
	 *
	 * @return HRESULT
	 *
	 */
	protected ETList<IVersionableElement> retrieveImpactedElements()
	{
		ETList<IVersionableElement> impacted = null;
		IProject proj = getProject();
		if (proj != null)
		{
			ITypeManager typeMan = proj.getTypeManager();
			if (typeMan != null)
			{
				IStrings files = typeMan.gatherExternalFileLocations();
				if (files != null)
				{
					// Now, Determine whether or not the files pointed to by
					// these file names actually have any reference to this Classifier
					Vector < String > fileCol = new Vector < String >();
					if (determineImpactedFiles(files, fileCol) > 0)
					{
						if(fileCol.size() > 0)
						{
							loadImpactedFiles(fileCol, typeMan);
						}
					}
				}
			}

			// Now perform the query, as we know everything we need is in the DOM
			String xmiid = super.getXMIID();
			impacted = performImpactQuery(proj, xmiid);
		}
		return impacted;
	}
	
	/**
	 *
	 * Given a collection of files, this method determines whether or not any of 
	 * those files contains the XMI id of this classifier.
	 *
	 * @param files[in]           The collection of absolute path names that will be searched
	 * @param impactedFiles[out]  The names of the files that contained the xmi ID
	 *
	 * @return Number of impacted files
	 *
	 */
	private long determineImpactedFiles( IStrings files, Vector < String > impactedFiles )
	{
		long impacted = 0;
		if (files != null)
		{
			long count = files.getCount();
			if (count > 0)
			{
				String xmiid = super.getXMIID();
				for (int i=0; i<count; i++)
				{
					String fileLoc = files.item(i);
					if (fileLoc.length() > 0)
					{
						if (isImpactedFile(fileLoc, xmiid))
						{
							impactedFiles.add(fileLoc);
						}
					}
				}
				impacted = impactedFiles.size();
			}
		}
		return impacted;
	}
	
	/**
	 *
	 * Determines whether or not the file pointed to contains the passed-in
	 * XMI ID anywhere in the file.
	 *
	 * @param fileName[in]  The file to crack open and search
	 * @param xmiID[in]     The xmi id to search for.
	 *
	 * @return HRESULT
	 *
	 */
	private boolean isImpactedFile( String fileName, String xmiID )
	{
	   return FileManip.isInFile( xmiID, fileName);
	}
	
	/**
	 *
	 * Loads the external files found in the passed in collection.
	 *
	 * @param impactedFiles[in]   The collection of absolute file names that point at
	 *                            external files from the project that needs to be loaded
	 * @param manager[in]         The TypeManager associated with the Project this Classifier
	 *                            is in
	 *
	 */
	private void loadImpactedFiles(Vector col, ITypeManager man)
	{
		if (man != null)
		{
			for (int i=0; i<col.size(); i++)
			{
				man.loadExternalFile((String)col.elementAt(i));
			}
		}
	}
	
	/**
	 *
	 * Performs the impact query on the passed-in IProject, based on the xmiID coming in. All elements found
	 * will be returned.
	 *
	 * @param proj[in]         The Project to perform the query on
	 * @param xmiID[in]        The ID to search the project on
	 * @param impacted[out]    The found elements that reference xmiID in some way
	 *
	 * @return HRESULT
	 *
	 */
	private ETList<IVersionableElement> performImpactQuery( IProject proj, String xmiID )
	{
		ETList<IVersionableElement> retElems = new ETArrayList<IVersionableElement>();
		Document doc = proj.getDocument();
		if (doc != null)
		{
			List list = getAllAffectedElements(doc, xmiID);
			if (list != null)
			{
				int count = list.size();				
				TypedFactoryRetriever<IVersionableElement> ret = 
									new TypedFactoryRetriever<IVersionableElement>();
				for (int i=0; i<count; i++)
				{
					Node node = (Node)list.get(i);
					if (node != null)
					{
						if (node.getNodeType() == Node.ATTRIBUTE_NODE)
						{
							String name = node.getName();
							// We don't want to get the parent node if
							// the xml attribute is "xmi.id", 'cause that is the
							// element that has changed that is causing the impact
							// query in the first place!
							if (!name.equals("xmi.id") && !name.equals("owner"))
							{
								node = XMLManip.selectSingleNode(node, "parent::*");
							}
							else
							{
								node = null;
							}
						}
					}
					if (node != null)
					{		
						IVersionableElement verEle = ret.createTypeAndFill(node);
						retElems.add( verEle );
					}
				}
			}
		}
		return retElems;
	}

	/**
	 *
	 * Ensures that the interface has a required stereotype.
	 *
	 * @param sStereotypePSK[in] The stereotype to be added
	 *
	 * @return HRESULT
	 *
	 */
	protected void ensureStereotype(String sStereotypePSK )
	{
		String sStereotypeStr = translateString(sStereotypePSK);
		IStereotype pStereo = findStereotype(sStereotypeStr);
		if (pStereo == null)
		{
			// We did not find it, so add it.
			String canonicalForm = "<<" + sStereotypeStr + ">>";
                        applyStereotype2(sStereotypeStr);
//			applyNewStereotypes(canonicalForm);
		}
	}

	/**
	 *
	 * Finds a stereotype by name on this element
	 *
	 * @param sStereotypeName[in] The name of the stereotype to look for
	 * @param pStereotype[out] The stereotype, if found
	 *
	 * @return 
	 *
	 */
	protected IStereotype findStereotype(String sStereotypeName)
	{
		IStereotype retStereo = null;
		ETList<Object> objs = getAppliedStereotypes();
		if (objs != null)
		{
			boolean foundIt = false;
			int count = objs.size();
			int idx = 0;
			while (idx < count && !foundIt)
			{
				IStereotype stereo = (IStereotype)objs.get(idx++);
				String name = stereo.getName();
				if (name.equals(sStereotypeName))
				{
					foundIt = true;
					retStereo = stereo;
				}
			}
		}
		return retStereo;
	}
	
    
     public void delete() 
     {
        IGeneralization gen = null;
        
        ETList<IGeneralization> gens  = this.getGeneralizations();
        ETList<IGeneralization> specs = this.getSpecializations();
        ETList<IAssociation> assocs = this.getAssociations();
        ETList<IDependency> deps    = this.getSupplierDependencies();

        //delete all generalizations
        if(gens != null && gens.size() > 0)
        {    
            for (int i = 0; i < gens.size(); i++) 
            {
            	gen = gens.get(i);
            	if(gen != null)
            		gen.delete();
            }
        }
        
        //delete all specializations
        if(specs != null && specs.size() > 0)
        {    
            for (int i = 0; i < specs.size(); i++) 
            {
            	gen = specs.get(i);
            	if(gen != null)
            		gen.delete();
            }
        }    

        //delete all associations
        if(assocs != null && assocs.size() > 0)
        {
            for (int i = 0; i < assocs.size(); i++) 
            {
                IAssociation assoc = assocs.get(i);
                if(assoc != null)
                    assoc.delete();
            }
        }

        //delete all supplier dependencies        
        if(deps != null && deps.size() > 0)
        {
            for (int i = 0; i < deps.size(); i++) 
            {
                IDependency dep = deps.get(i);
                if(dep != null)
                    dep.delete();
            }
        }
        
        //now delete all client dependencies
        deps = null;
        deps    = this.getClientDependencies();
        if(deps != null && deps.size() > 0)
        {
            for (int i = 0; i < deps.size(); i++) 
            {
                IDependency dep = deps.get(i);
                if(dep != null)
                    dep.delete();
            }
        }
        
        super.delete();
     }
	
	/**
	 *
	 * Finds and removes the specified stereotype from this element.
	 *
	 * @param sStereotypePSK[in] The name of the stereotype to look for
	 *
	 * @return 
	 *
	 */
	protected void deleteStereotype ( String sStereotypePSK )
	{
		String sStereoStr = translateString(sStereotypePSK);
		IStereotype stereo = findStereotype(sStereoStr);
		if (stereo != null)
		{
			// We found it, so remove it
			removeStereotype(stereo);
		}
	}
	
	/**
	 *
	 * Translate a PSK string to its value.
	 *
	 * @param sPSK[in]
	 * @param sValue[out]
	 *
	 * @return 
	 *
	 */
	protected String translateString(String sPSK)
	{
		String sValue = "";
		ConfigStringHelper helper = ConfigStringHelper.instance();
		IConfigStringTranslator translator = helper.getTranslator();
		if (translator != null)
		{
			sValue = translator.translate(null, sPSK); 
		}
		else
		{
			sValue = sPSK;
		}
		return sValue;
	}
	
	/**
	 * Retrieves the operation with a matching signature
	 */
	public IOperation findMatchingOperation( IOperation pOper, boolean bMustBeAbstract)
	{
		IOperation pFound = null;
		ETList<IOperation> opers = getOperations();
		if (opers != null)
		{
			int count = opers.size();
			for (int i=0; i<count; i++)
			{
				IOperation oper = opers.get(i);
				boolean isTestable = true;
				if (bMustBeAbstract)
				{
					isTestable = oper.getIsAbstract();
				}
				if (isTestable)
				{
					boolean isSame = pOper.isSignatureSame(oper);
					if (isSame)
					{
						pFound = oper;
						break;
					}
				}
			}
		}
		return pFound;
	}
	
	protected boolean checkForNameCollision( String newName, IElementChangeDispatchHelper helper, INamedElement curElement )
	{
	   return firePreNameCollisionIfNeeded( helper, curElement, newName );
	}

	protected void checkForNameCollision( IElementChangeDispatchHelper helper, INamedElement curElement )
	{
	   fireNameCollisionIfNeeded( helper, curElement );
	}

	/**
	 * Retrieves the operation with a matching signature
	 */
	public IOperation findMatchingParentOperation(IOperation pOper, boolean bMustBeAbstract)
	{
		IOperation retOper = null;
		ETList<IGeneralization> gens = getGeneralizations();
		if (gens != null)
		{
			int count = gens.size();
			for (int i=0; i<count; i++)
			{
				IGeneralization gen = gens.get(i);
				IClassifier clazz = gen.getGeneral();
				if (clazz != null)
				{
					retOper = clazz.findMatchingOperation(pOper, bMustBeAbstract);
					if (retOper != null)
						break;
				}
			}
		}
		return retOper;
	}
	
	/**
	 * Retrieves the operation with a matching signature
	 */
	public IOperation findMatchingOperation(IOperation pOper)
	{
		return findMatchingOperation(pOper, false);
	}

	private class RedefinableElementFilter 
	{
		private Classifier m_Classifier = null;
        private Class m_FeatureType = null;
        
        public RedefinableElementFilter(Classifier clazz, String type)
        {
            m_Classifier = clazz;
            try
            {
            	m_FeatureType = Class.forName(type);
            }
            catch (ClassNotFoundException e)
            {
            }
        }
        
		public RedefinableElementFilter(Classifier clazz, Class type)
		{
			m_Classifier = clazz;
            m_FeatureType = type;
		}
		
		/**
		 *
		 * Retrieves a NamedCollections object filled with Collection types
		 * matching FeatureCollection, filled with the appropriately typed
		 * feature.
		 *
		 * @param pVal[out] The collection
		 *
		 * @return HRESULT
		 *
		 */
		public ETList<INamedCollection> filter()
		{
			ETList<INamedCollection> retCols = null;
			if (m_Classifier != null)
			{
				ETList<INamedCollection> cols = m_Classifier.getRedefiningFeatures();
				if (cols != null)
				{
					retCols = filterFeatures(cols);
				}
			}
			return retCols;
		}
		
		public ETList<INamedCollection> filterFeatures(ETList<INamedCollection> cols)
		{
            if (cols == null)
                return null;

			ETList<INamedCollection> retCols = 
                new ETArrayList<INamedCollection>();
            int size = cols.size();
			for (int i = 0; i < size; i++)
            {
                INamedCollection coll = cols.get(i);
                if (m_FeatureType == null)
                    retCols.add(coll);
                else
                {
                    // Filter out unwanted elements
                	INamedCollection newc = new NamedCollection();
                    newc.setName(coll.getName());
                    
                    // Java's generics aren't clever enough to handle this, so
                    // we use a generic container.
                    ETList<IElement> els = new ETArrayList<IElement>();
                    newc.setData(els);
                    
                    ETList<IElement> curr = (ETList) coll.getData();
                    if (curr != null)
                    {    
                        for (int j = 0; j < curr.size(); ++j)
                        {
                        	IElement elem = curr.get(j);
                            if (elem != null &&
                                    m_FeatureType.isAssignableFrom(
                                            elem.getClass()))
                                els.add(elem);
                        }
                    }
                    retCols.add(newc);
                }
            }
			return retCols;		
		}       
	}

   /**
    * @param element
    * @return
    */
   public long addRedefinedElement(IRedefinableElement element)
   {
    	if(m_RedefineAggregate == null)
    	{
	 		m_RedefineAggregate = new RedefinableElement();
   	    }
        return m_RedefineAggregate.addRedefinedElement(element);
   }

   /**
    * @param element
    * @return
    */
   public long addRedefiningElement(IRedefinableElement element)
   {
        if(m_RedefineAggregate == null)
        {
            m_RedefineAggregate = new RedefinableElement();
        }
        return m_RedefineAggregate.addRedefiningElement(element); 
   }

   /**
    * @return
    */
   public boolean getIsFinal()
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
      return m_RedefineAggregate.getIsFinal();
   }

   /**
    * @return
    */
   public boolean getIsRedefined()
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
      return m_RedefineAggregate.getIsRedefined();
   }

   /**
    * @return
    */
   public boolean getIsRedefining()
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
      return m_RedefineAggregate.getIsRedefining();
   }

   /**
    * @return
    */
   public long getRedefinedElementCount()
   {
      return m_RedefineAggregate.getRedefinedElementCount();
   }

   /**
    * @return
    */
   public ETList<IRedefinableElement> getRedefinedElements()
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
        return m_RedefineAggregate.getRedefinedElements();
   }

   /**
    * @return
    */
   public long getRedefiningElementCount()
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
        return m_RedefineAggregate.getRedefiningElementCount();
   }

   /**
    * @return
    */
  
   public ETList<IRedefinableElement> getRedefiningElements()
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
        return m_RedefineAggregate.getRedefiningElements();
   }

   

   /**
    * @param element
    * @return
    */
   public long removeRedefinedElement(IRedefinableElement element)
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
        return m_RedefineAggregate.removeRedefinedElement(element);
   }

   /**
    * @param element
    * @return
    */
   public long removeRedefiningElement(IRedefinableElement element)
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
      	return m_RedefineAggregate.removeRedefiningElement(element);
   }

   /**
    * @param value
    */
   public void setIsFinal(boolean value)
   {
		if(m_RedefineAggregate == null)
		{
			m_RedefineAggregate = new RedefinableElement();
		}   	
        m_RedefineAggregate.setIsFinal(value);
   }

   /**
	*
	* Duplicates the template parameters on the Classifier
	*
	* @param dupClassifier[in] The duplicated classifier
	* @param origParams[in] The Original Parameters
	*	
	*
	*/
   protected void duplicateTemplateParms( IClassifier dupClassifier, 
   									 ETList<IParameterableElement> origParms)
   {
	  if( origParms != null )
	  {
		 int len = origParms.size();
		 for( int i = 0; i < len; i++ )
		 {
			IParameterableElement element = origParms.get(i);
			    
			if( element != null )
			{
			   // Find out if the parameterable element is also
			   // an owned element of this classifier. If it is,
			   // it has already been duplicated
	
			   IVersionableElement ver = element.duplicate();			   
			   IParameterableElement dupParm = (IParameterableElement)ver;
			   
			   if( dupParm != null)
			   {
				  dupClassifier.addTemplateParameter( dupParm );
	
				  String curParmID = element.getXMIID();
				  String dupParmID = dupParm.getXMIID();
				  
				  // Make sure to replace references to the old templateParameters
				  // in the duplicated Classifier.	
				  Node scoping = dupClassifier.getNode();
				  RedefinableElement elemm = new RedefinableElement();
				  elemm.replaceIds(curParmID, dupParmID, scoping,".//");
	       
				  // Add the original parm back to this classifier	
				  addTemplateParameter( element );
			   }
			}
		}
	 }
   }
   
   //IParameterableElement methods
   public IParameterableElement getDefaultElement()
   {
		if (m_ParameterableAggregate == null)
		{
			m_ParameterableAggregate = new ParameterableElement();   	
		}
		return m_ParameterableAggregate.getDefaultElement();
   }
   
   public void setDefaultElement( IParameterableElement element )
   {
		if (m_ParameterableAggregate == null)
		{
			m_ParameterableAggregate = new ParameterableElement();   	
		}
		m_ParameterableAggregate.setDefaultElement(element);   	
   }
   
   public void setDefaultElement2( String newVal )
   {
		if (m_ParameterableAggregate == null)
		{
			m_ParameterableAggregate = new ParameterableElement();   	
		}
		m_ParameterableAggregate.setDefaultElement2(newVal);   	
   }
   
   public IClassifier getTemplate()
   {
		if (m_ParameterableAggregate == null)
		{
			m_ParameterableAggregate = new ParameterableElement();   	
		}
		return m_ParameterableAggregate.getTemplate();      	
   }
   
   public void setTemplate( IClassifier value )
   {
		if (m_ParameterableAggregate == null)
		{
			m_ParameterableAggregate = new ParameterableElement();   	
		}
		m_ParameterableAggregate.setTemplate(value);   	
   } 
   
   public String getTypeConstraint()
   {
		return m_ParameterableAggregate.getTypeConstraint();      	
   }
   
   public void setTypeConstraint( String value )
   {
		m_ParameterableAggregate.setTypeConstraint(value);   	
   }
   
   //IAutonomousElement methods
   public boolean isExpanded()
   {	
		if (m_AutonomousAggregate == null)
		{
			m_AutonomousAggregate = new AutonomousElement();   	
		}
		return m_AutonomousAggregate.isExpanded();   	   	   
   }
   
   public void setIsExpanded(boolean newVal )
   {	
		if (m_AutonomousAggregate == null)
		{
			m_AutonomousAggregate = new AutonomousElement();   	
		}
		m_AutonomousAggregate.setIsExpanded(newVal);   	   	   
   }


        
    public String getSourceFileArtifactsList()
    {
        StringBuffer sourcesStr = new StringBuffer(); // NOI18N
        ETList<IElement> sourceFileArtifacts = getSourceFiles();
        
        for (IElement sfa: sourceFileArtifacts)
        {
            if (sourcesStr.length() != 0)
                sourcesStr.append("; ");
            
            SourceFileArtifact curArt = (SourceFileArtifact)sfa;
            
            sourcesStr.append(curArt.getLanguage().getName())
                .append(':').append(curArt.getFileName());
        }
        
        return sourcesStr.toString();
    }

    
    public boolean isSimilar(INamedElement other)
    {
        if (!(other instanceof IClassifier) || !super.isSimilar(other))
            return false;
        
        IClassifier otherClassifier = (IClassifier) other;
        
        if (!getFullyQualifiedName(false).equals(
                otherClassifier.getFullyQualifiedName(false)))
        {
            return false;
        }
        
        return true;
    }

}
