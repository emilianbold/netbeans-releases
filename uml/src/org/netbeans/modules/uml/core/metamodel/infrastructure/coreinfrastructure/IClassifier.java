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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IClassifier extends INamespace,
									 IRedefinableElement,
									 IParameterableElement,
									 IAutonomousElement
 {
  public IAutonomousElement auto = null;
  public IRedefinableElement redef = null;
  public IParameterableElement param = null;

  /**
   * If true, the Classifier does not provide a complete declaration and can typically not be instantiated.
  */
  public boolean getIsAbstract();

  /**
   * If true, the Classifier does not provide a complete declaration and can typically not be instantiated.
  */
  public void setIsAbstract( boolean value );

  /**
   * If true, this classifier cannot be further specialized. Default value is false.
  */
  public boolean getIsLeaf();

  /**
   * If true, this classifier cannot be further specialized. Default value is false.
  */
  public void setIsLeaf( boolean value );

  /**
   * Adds a generalization relationship to this classifier where this classifier plays the specific / sub class role.
  */
  public void addGeneralization( IGeneralization gen );

  /**
   * Removes a generalization from this classifier where this classifier plays the specific / sub class role.
  */
  public void removeGeneralization( IGeneralization gen );

  /**
   * Retrieves the collection of generalization relationships this Classifier plays the specific / sub class role.
  */
  public ETList<IGeneralization> getGeneralizations();

  /**
   * Adds a generalization relationship to this classifier where this classifier plays the general / super class role.
  */
  public void addSpecialization( IGeneralization gen );

  /**
   * Removes a generalization from this classifier where this classifier plays the general / super class role.
  */
  public void removeSpecialization( IGeneralization gen );

  /**
   * Retrieves the collection of generalization relationships this Classifier plays the general / super class role.
  */
  public ETList<IGeneralization> getSpecializations();

  /**
   * method AddImplementation
  */
  public void addImplementation( IImplementation imp );

  /**
   * method RemoveImplementation
  */
  public void removeImplementation( IImplementation imp );

  /**
   * property Implementations
  */
  public ETList<IImplementation> getImplementations();

  /**
   * method AddCollaboration
  */
  public void addCollaboration( ICollaborationOccurrence col );

  /**
   * method RemoveCollaboration
  */
  public void removeCollaboration( ICollaborationOccurrence col );

  /**
   * property Collaborations
  */
  public ETList<ICollaborationOccurrence> getCollaborations();

  /**
   * property Representation
  */
  public ICollaborationOccurrence getRepresentation();

  /**
   * property Representation
  */
  public void setRepresentation( ICollaborationOccurrence value );

  /**
   * method AddBehavior
  */
  public void addBehavior( IBehavior Behavior );

  /**
   * method RemoveBehavior
  */
  public void removeBehavior( IBehavior Behavior );

  /**
   * property Behaviors
  */
  public ETList<IBehavior> getBehaviors();

  /**
   * property ClassifierBehavior
  */
  public IBehavior getClassifierBehavior();

  /**
   * property ClassifierBehavior
  */
  public void setClassifierBehavior( IBehavior value );

  /**
   * method AddIncrement
  */
  public void addIncrement( IIncrement inc );

  /**
   * method RemoveIncrement
  */
  public void removeIncrement( IIncrement inc );

  /**
   * property Increments
  */
  public ETList<IIncrement> getIncrements();

  /**
   * An ordered list of Features, like Attribute, Operation, Method, owned by the Classifier.
  */
  public ETList<IFeature> getFeatures();

  /**
   * An ordered list of Features, like Attribute, Operation, Method, owned by the Classifier.
  */
  public void setFeatures( ETList<IFeature> value );

  /**
   * method AddFeature
  */
  public void addFeature( IFeature feat );

  /**
   * Inserts a new feature into this classifier's list of features immediately before the existing feature passed in. If existingFeature is 0, then the new feature is appended to the end of the features list.
  */
  public void insertFeature( IFeature existingFeature, IFeature newFeature );

  /**
   * method RemoveFeature
  */
  public void removeFeature( IFeature feat );

  /**
   * Retrieves a collection of Attributes off this Classifier.
  */
  public ETList<IAttribute> getAttributes();

  /**
   * Retrieves a collection of Operations off this Classifier.
  */
  public ETList<IOperation> getOperations();

  /**
   * Adds an Attribute to this Classifier.
  */
  public void addAttribute( IAttribute newVal );

  /**
   * Adds an Operation to this Classifier.
  */
  public void addOperation( IOperation newVal );

  /**
   * Creates an Attribute. The new attribute is not added to this Classifier.
  */
  public IAttribute createAttribute( String Type, String Name );

  /**
   * Creates an Operation. The new operation is not added to this Classifier.
  */
  public IOperation createOperation( String retType, String Name );

  /**
   * Creates an Attribute. The new attribute is not added to this Classifier.
  */
  public IAttribute createAttribute2( IClassifier Type, String Name );

  /**
   * Creates an Operation. The new operation is not added to this Classifier.
  */
  public IOperation createOperation2( IClassifier retType, String Name );

  /**
   * Creates an Attribute with a default name and type, dependent on the current language settings. The new attribute is not added to this Classifier.
  */
  public IAttribute createAttribute3();

  /**
   * Creates an Operation with a default name and return type, dependent on the current language settings. The new operation is not added to this Classifier.
  */
  public IOperation createOperation3();

  /**
   * Adds the association end to this classifiers list of ends.
  */
  public void addAssociationEnd( IAssociationEnd end );

  /**
   * Removes the passed in end from this classifier's list.
  */
  public void removeAssociationEnd( IAssociationEnd end );

  /**
   * Retrieves the collection of IAssociationEnd objects this Classifier is a participant on.
  */
  public ETList<IAssociationEnd> getAssociationEnds();

  /**
   * Transforms this Classifier into another, such as an Actor into a Class.
  */
  public IClassifier transform( String TypeName );

  /**
   * The collection of Associations that this Classifier participates in.
  */
  public ETList<IAssociation> getAssociations();

  /**
   * Creates an Operation with the same name as this Classifier, whose Constructor property is true.
  */
  public IOperation createConstructor();

  public IOperation createDestructor();

  /**
   * The collection of NavigableEnds this Classifier is referencing. These are ends that are on the other side of an association that result in the modification of this Classifier's feature list.
  */
  public ETList<INavigableEnd> getNavigableEnds();

  /**
   * Determines whether or not this Classifier is persisted or not.
  */
  public boolean getIsTransient();

  /**
   * Determines whether or not this Classifier is persisted or not.
  */
  public void setIsTransient( boolean value );

  /**
   * Retrieves all the features that are redefining features either in a super class or an implemented interface.
  */
  public ETList<INamedCollection> getRedefiningFeatures();

  /**
   * Retrieves all the attributes that are redefining attributes either in a super class or an implemented interface.
  */
  public ETList<INamedCollection> getRedefiningAttributes();

  /**
   * Retrieves all the operations that are redefining operations either in a super class or an implemented interface.
  */
  public ETList<INamedCollection> getRedefiningOperations();

  /**
   * Retrieves all the features on this Classifier that are not redefining ( overloading ) other features.
  */
  public ETList<IFeature> getNonRedefiningFeatures();

  /**
   * Retrieves all the attributes on this Classifier that are not redefining ( overloading ) other attributes.
  */
  public ETList<IAttribute> getNonRedefiningAttributes();

  /**
   * Retrieves all the operation on this Classifier that are not redefining ( overloading ) other operations.
  */
  public ETList<IOperation> getNonRedefiningOperations();

  /**
   * method AddTemplateParameter
  */
  public void addTemplateParameter( IParameterableElement pParm );

  /**
   * method RemoveTemplateParameter
  */
  public void removeTemplateParameter( IParameterableElement pParm );

  /**
   * Is the argument a template parameter of this classifier?
  */
  public boolean getIsTemplateParameter( IParameterableElement pParm );

  /**
   * property TemplateParameters
  */
  public ETList<IParameterableElement> getTemplateParameters();

  /**
   * The relationship connecting this Classifier with the template classifier it is deriving from.
  */
  public IDerivation getDerivation();

  /**
   * The relationship connecting this Classifier with the template classifier it is deriving from.
  */
  public void setDerivation( IDerivation value );

  /**
   * Returns a list of all connected NavigableEnds that aim away from this Classifier.
  */
  public ETList<INavigableEnd> getOutboundNavigableEnds();

  /**
   * Returns a list of all connected NavigableEnds that aim towards this Classifier.
  */
  public ETList<INavigableEnd> getInboundNavigableEnds();

  /**
   * Retrieves all the attributes that are redefining attributes either in a super class or an implemented interface.
  */
  public ETList<IAttribute> getRedefiningAttributes2();

  /**
   * Retrieves all the operations that are redefining operations either in a super class or an implemented interface.
  */
  public ETList<IOperation> getRedefiningOperations2();

  /**
   * Retrieves the default value that can be used to initialize this type in code.
  */
  public String getDefaultTypeValue();

  /**
   * Retrieves an attribute with the passed in name.
  */
  public IAttribute getAttributeByName( String attrName );

  /**
   * Retrieves all the attributes of the given name.
  */
  public ETList<IAttribute> getAttributesByName( String attrName );

  /**
   * Retrieves all the attributes and out bound Navigable ends of the given name.
  */
  public ETList<IAttribute> getAttributesAndNavEndsByName( String attrName );

  /**
   * Retrieves all the operations with the passed in name.
  */
  public ETList<IOperation> getOperationsByName( String operName );
  
  /**
   * Retrieves the operation with a matching signature
  */
  public IOperation findMatchingOperation( IOperation pOper );
  public IOperation findMatchingOperation( IOperation pOper, boolean bMustBeAbstract);

  /**
   * Retrieves the parent operation with a matching signature
  */
  public IOperation findMatchingParentOperation( IOperation pOper, boolean bMustBeAbstract );

  /**
   * Retrieves the list of template parameters as a comma-delimited list
   * for displaying in the property sheet.
   */
  public String getTemplateParametersAsString();
}
