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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;


import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;

import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;


public class ClassifierTemplateParameter extends ParameterableElement implements IClassifier
{
  /**
   * If true, the Classifier does not provide a complete declaration and can typically not be instantiated.
  */
  public boolean getIsAbstract() { return false; }

  /**
   * If true, the Classifier does not provide a complete declaration and can typically not be instantiated.
  */
  public void setIsAbstract( boolean value ) {}

  /**
   * If true, this classifier cannot be further specialized. Default value is false.
  */
  public boolean getIsLeaf() { return true; }

  /**
   * If true, this classifier cannot be further specialized. Default value is false.
  */
  public void setIsLeaf( boolean value ) {}

  /**
   * Adds a generalization relationship to this classifier where this classifier plays the specific / sub class role.
  */
  public void addGeneralization( IGeneralization gen ) {}

  /**
   * Removes a generalization from this classifier where this classifier plays the specific / sub class role.
  */
  public void removeGeneralization( IGeneralization gen ) {}

  /**
   * Retrieves the collection of generalization relationships this Classifier plays the specific / sub class role.
  */
  public ETList<IGeneralization> getGeneralizations() { return new ETArrayList<IGeneralization>(); }

  /**
   * Adds a generalization relationship to this classifier where this classifier plays the general / super class role.
  */
  public void addSpecialization( IGeneralization gen ) {}

  /**
   * Removes a generalization from this classifier where this classifier plays the general / super class role.
  */
  public void removeSpecialization( IGeneralization gen ) {}

  /**
   * Retrieves the collection of generalization relationships this Classifier plays the general / super class role.
  */
  public ETList<IGeneralization> getSpecializations() { return new ETArrayList<IGeneralization>(); }

  /**
   * method AddImplementation
  */
  public void addImplementation( IImplementation imp ) {}

  /**
   * method RemoveImplementation
  */
  public void removeImplementation( IImplementation imp ) {}

  /**
   * property Implementations
  */
  public ETList<IImplementation> getImplementations() { return new ETArrayList<IImplementation>(); }

  /**
   * method AddCollaboration
  */
  public void addCollaboration( ICollaborationOccurrence col ) {}

  /**
   * method RemoveCollaboration
  */
  public void removeCollaboration( ICollaborationOccurrence col ) {}

  /**
   * property Collaborations
  */
  public ETList<ICollaborationOccurrence> getCollaborations() { return new ETArrayList<ICollaborationOccurrence>(); }

  /**
   * property Representation
  */
  public ICollaborationOccurrence getRepresentation()  { return null;}

  /**
   * property Representation
  */
  public void setRepresentation( ICollaborationOccurrence value ) {}

  /**
   * method AddBehavior
  */
  public void addBehavior( IBehavior Behavior ) {}

  /**
   * method RemoveBehavior
  */
  public void removeBehavior( IBehavior Behavior ) {}

  /**
   * property Behaviors
  */
  public ETList<IBehavior> getBehaviors() { return new ETArrayList<IBehavior>(); }

  /**
   * property ClassifierBehavior
  */
  public IBehavior getClassifierBehavior() { return null;}

  /**
   * property ClassifierBehavior
  */
  public void setClassifierBehavior( IBehavior value ) {}

  /**
   * method AddIncrement
  */
  public void addIncrement( IIncrement inc ) {}

  /**
   * method RemoveIncrement
  */
  public void removeIncrement( IIncrement inc ) {} 

  /**
   * property Increments
  */
  public ETList<IIncrement> getIncrements() { return new ETArrayList<IIncrement>(); }

  /**
   * An ordered list of Features, like Attribute, Operation, Method, owned by the Classifier.
  */
  public ETList<IFeature> getFeatures() { return new ETArrayList<IFeature>(); }

  /**
   * An ordered list of Features, like Attribute, Operation, Method, owned by the Classifier.
  */
  public void setFeatures( ETList<IFeature> value ) {}

  /**
   * method AddFeature
  */
  public void addFeature( IFeature feat ) {}

  /**
   * Inserts a new feature into this classifier's list of features immediately before the existing feature passed in. If existingFeature is 0, then the new feature is appended to the end of the features list.
  */
  public void insertFeature( IFeature existingFeature, IFeature newFeature ) {}

  /**
   * method RemoveFeature
  */
  public void removeFeature( IFeature feat ) {}

  /**
   * Retrieves a collection of Attributes off this Classifier.
  */
  public ETList<IAttribute> getAttributes() { return new ETArrayList<IAttribute>(); }

  /**
   * Retrieves a collection of Operations off this Classifier.
  */
  public ETList<IOperation> getOperations() { return new ETArrayList<IOperation>(); }

  /**
   * Adds an Attribute to this Classifier.
  */
  public void addAttribute( IAttribute newVal ) {}

  /**
   * Adds an Operation to this Classifier.
  */
  public void addOperation( IOperation newVal ) {}

  /**
   * Creates an Attribute. The new attribute is not added to this Classifier.
  */
  public IAttribute createAttribute( String Type, String Name ) { return null; }

  /**
   * Creates an Operation. The new operation is not added to this Classifier.
  */
  public IOperation createOperation( String retType, String Name ) { return null; }

  /**
   * Creates an Attribute. The new attribute is not added to this Classifier.
  */
  public IAttribute createAttribute2( IClassifier Type, String Name ) { return null; }

  /**
   * Creates an Operation. The new operation is not added to this Classifier.
  */
  public IOperation createOperation2( IClassifier retType, String Name ) { return null; }

  /**
   * Creates an Attribute with a default name and type, dependent on the current language settings. The new attribute is not added to this Classifier.
  */
  public IAttribute createAttribute3() { return null; }

  /**
   * Creates an Operation with a default name and return type, dependent on the current language settings. The new operation is not added to this Classifier.
  */
  public IOperation createOperation3() { return null; }

  /**
   * Adds the association end to this classifiers list of ends.
  */
  public void addAssociationEnd( IAssociationEnd end ) {}

  /**
   * Removes the passed in end from this classifier's list.
  */
  public void removeAssociationEnd( IAssociationEnd end ) {}

  /**
   * Retrieves the collection of IAssociationEnd objects this Classifier is a participant on.
  */
  public ETList<IAssociationEnd> getAssociationEnds() { return new ETArrayList<IAssociationEnd>(); }

  /**
   * Transforms this Classifier into another, such as an Actor into a Class.
  */
  public IClassifier transform( String TypeName ) { return null; }

  /**
   * The collection of Associations that this Classifier participates in.
  */
  public ETList<IAssociation> getAssociations() { return new ETArrayList<IAssociation>(); }

  /**
   * Creates an Operation with the same name as this Classifier, whose Constructor property is true.
  */
  public IOperation createConstructor() { return null; }

  public IOperation createDestructor() { return null; }

  /**
   * The collection of NavigableEnds this Classifier is referencing. These are ends that are on the other side of an association that result in the modification of this Classifier's feature list.
  */
  public ETList<INavigableEnd> getNavigableEnds() { return new ETArrayList<INavigableEnd>(); }

  /**
   * Determines whether or not this Classifier is persisted or not.
  */
  public boolean getIsTransient() {return false; }

  /**
   * Determines whether or not this Classifier is persisted or not.
  */
  public void setIsTransient( boolean value ) {}

  /**
   * Retrieves all the features that are redefining features either in a super class or an implemented interface.
  */
  public ETList<INamedCollection> getRedefiningFeatures() { return new ETArrayList<INamedCollection>(); }

  /**
   * Retrieves all the attributes that are redefining attributes either in a super class or an implemented interface.
  */
  public ETList<INamedCollection> getRedefiningAttributes() { return new ETArrayList<INamedCollection>(); }

  /**
   * Retrieves all the operations that are redefining operations either in a super class or an implemented interface.
  */
  public ETList<INamedCollection> getRedefiningOperations() { return new ETArrayList<INamedCollection>(); }

  /**
   * Retrieves all the features on this Classifier that are not redefining ( overloading ) other features.
  */
  public ETList<IFeature> getNonRedefiningFeatures() { return new ETArrayList<IFeature>(); }

  /**
   * Retrieves all the attributes on this Classifier that are not redefining ( overloading ) other attributes.
  */
  public ETList<IAttribute> getNonRedefiningAttributes() { return new ETArrayList<IAttribute>(); }

  /**
   * Retrieves all the operation on this Classifier that are not redefining ( overloading ) other operations.
  */
  public ETList<IOperation> getNonRedefiningOperations() { return new ETArrayList<IOperation>(); }

  /**
   * method AddTemplateParameter
  */
  public void addTemplateParameter( IParameterableElement pParm ) {}

  /**
   * method RemoveTemplateParameter
  */
  public void removeTemplateParameter( IParameterableElement pParm ) {}

  /**
   * Is the argument a template parameter of this classifier?
  */
  public boolean getIsTemplateParameter( IParameterableElement pParm ) { return false; }

  /**
   * property TemplateParameters
  */
  public ETList<IParameterableElement> getTemplateParameters() { return new ETArrayList<IParameterableElement>(); }

  /**
   * The relationship connecting this Classifier with the template classifier it is deriving from.
  */
  public IDerivation getDerivation() { return null; }

  /**
   * The relationship connecting this Classifier with the template classifier it is deriving from.
  */
  public void setDerivation( IDerivation value ) {}

  /**
   * Returns a list of all connected NavigableEnds that aim away from this Classifier.
  */
  public ETList<INavigableEnd> getOutboundNavigableEnds() { return new ETArrayList<INavigableEnd>(); }

  /**
   * Returns a list of all connected NavigableEnds that aim towards this Classifier.
  */
  public ETList<INavigableEnd> getInboundNavigableEnds() { return new ETArrayList<INavigableEnd>(); }

  /**
   * Retrieves all the attributes that are redefining attributes either in a super class or an implemented interface.
  */
  public ETList<IAttribute> getRedefiningAttributes2() { return new ETArrayList<IAttribute>(); }

  /**
   * Retrieves all the operations that are redefining operations either in a super class or an implemented interface.
  */
  public ETList<IOperation> getRedefiningOperations2() { return new ETArrayList<IOperation>(); }

  /**
   * Retrieves the default value that can be used to initialize this type in code.
  */
  public String getDefaultTypeValue() { return null; }

  /**
   * Retrieves an attribute with the passed in name.
  */
  public IAttribute getAttributeByName( String attrName ) { return null; }

  /**
   * Retrieves all the attributes of the given name.
  */
  public ETList<IAttribute> getAttributesByName( String attrName ) { return new ETArrayList<IAttribute>(); }

  /**
   * Retrieves all the attributes and out bound Navigable ends of the given name.
  */
  public ETList<IAttribute> getAttributesAndNavEndsByName( String attrName ) { return new ETArrayList<IAttribute>(); }

  /**
   * Retrieves all the operations with the passed in name.
  */
  public ETList<IOperation> getOperationsByName( String operName ) { return new ETArrayList<IOperation>(); }
  
  /**
   * Retrieves the operation with a matching signature
  */
  public IOperation findMatchingOperation( IOperation pOper ) { return null; }
  public IOperation findMatchingOperation( IOperation pOper, boolean bMustBeAbstract) { return null; }

  /**
   * Retrieves the parent operation with a matching signature
  */
  public IOperation findMatchingParentOperation( IOperation pOper, boolean bMustBeAbstract ) { return null; }

  /**
   * Retrieves the list of template parameters as a comma-delimited list
   * for displaying in the property sheet.
   */
  public String getTemplateParametersAsString() { return null; }



    // INamespace


  /** Adds an element to this Namespace. */
  public boolean addOwnedElement(INamedElement elem) { return false; }

  /** Removes an element from this Namespace. */
  public void removeOwnedElement(INamedElement elem) {}

  /** retrieves the collection of elements owned by this Namespace. */
  public ETList<INamedElement> getOwnedElements() { return new ETArrayList<INamedElement>(); }

  /** Adds an element that will be visible within this namespace. */
  public void addVisibleMember(INamedElement elem) {}

  /** Removes an element that is currently visible within this namespace. */
  public void removeVisibleMember(INamedElement elem) {}

  /** Retrieves the collection of elements currently visible within the namespace. */
  public ETList<INamedElement> getVisibleMembers() { return new ETArrayList<INamedElement>(); }

  /** Retrieves all members within this namespace by the passed in name. */
  public ETList<INamedElement> getOwnedElementsByName(String name) { return new ETArrayList<INamedElement>(); }

  /** Retrieves the number of elements owned by the namespace. */
  public long getOwnedElementCount() { return 0; }

  /** Retrieves the number of visible members owned by the namespace */
  public long getVisibleMemberCount() { return 0; }

  public IPackage createPackageStructure(String packageStructure) { return null; }


    //IRedefinableElement


  /**
   * Sets / Gets the final flag on this element. If true, this redefinable element can not be further redefined. The default value is false.
  */
  public boolean getIsFinal() { return true; }

  /**
   * Sets / Gets the final flag on this element. If true, this redefinable element can not be further redefined. The default value is false.
  */
  public void setIsFinal( boolean value ) {}

  /**
   * Adds an element that will be redefined by this element.
  */
  public long addRedefinedElement( IRedefinableElement element ) { return 0; } ;

  /**
   * Removes an element that is being redefined by this element.
  */
  public long removeRedefinedElement( IRedefinableElement element ) { return 0; }

  /**
   * Retrieves the collection of elements that are currently being redefined by this element.
  */
  public ETList<IRedefinableElement> getRedefinedElements() { return new ETArrayList<IRedefinableElement>(); }

  /**
   * Adds an element that is redefining this element.
  */
  public long addRedefiningElement( IRedefinableElement element ) { return 0; }

  /**
   * Removes an element that is redefining this element.
  */
  public long removeRedefiningElement( IRedefinableElement element ) { return 0; }

  /**
   * Retrieves the collection of elements that are currently redefining this element.
  */
  public ETList<IRedefinableElement> getRedefiningElements() { return new ETArrayList<IRedefinableElement>(); }

  /**
   * .
  */
  public long getRedefinedElementCount() { return 0; }

  /**
   * .
  */
  public long getRedefiningElementCount() { return 0; }

  /**
   * Determines if this element is being redefined by some other element..
  */
  public boolean getIsRedefined() { return false; }

  /**
   * Determines if this element is being redefined by some other element..
  */
  public boolean getIsRedefining() { return false; }


    //IAutonomousElement

    public boolean isExpanded() { return false; }
    public void setIsExpanded(boolean newVal ) {}


}

