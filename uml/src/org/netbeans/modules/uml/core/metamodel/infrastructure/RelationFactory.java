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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.MetaLayerRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ProjectMissingException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;


import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageDataType;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;

/*
 * @author KevinM
 */
public class RelationFactory extends Object implements IRelationFactory 
{

	/**
	 * Creates an implemenation relationship between the client and supplier.  
	 * The supplier must be an IInterface instance.  If the supplier is not 
	 * an IInterface it will translated into an IInterface.  If outSupplier 
	 * is not null it will be set to the IInterface instance of the supplier.
	 *
	 * @param client [in] The client end of the implemenation.
	 * @param supplier [in] The Supplier particapant of the realtionship.
	 * @param space [in] The namespace that will contain the relationship.
	 * @param outSupplier [out] The supplier of the implemenation.
	 * @param dep [out] The relationship.
	 */
	public ETPairT<IInterface, IDependency> createImplementation(
		INamedElement client,
		INamedElement supplier,
		INamespace space) //  throws InvalidArguments
	{
		if (client == null || supplier == null)
		{
		//	throw new InvalidArguments();
			return null;
		}

		IInterface outSupplier = null;
		IDependency dep = null;
        
		String elementType = supplier.getElementType();

		if (!"Interface".equals(elementType))
		{
			// we need to do one more check before actually transforming the supplier to an
			// interface because this method is called when creating implementations
			// in a design pattern
			// the supplier is actually a part facade of type interface (which is a classifier)
			// which was then transforming the part facade to an interface which is not what we
			// want
			if (supplier instanceof IPartFacade)
			{
				IPartFacade pFacade = (IPartFacade)supplier;

				// if it is a part facade of type interface, then use the part facade
				if (pFacade instanceof IParameterableElement)
				{
					IParameterableElement pParam = (IParameterableElement) pFacade;

					String type = pParam.getTypeConstraint();
					if ("Interface".equals(type))
						outSupplier = (IInterface) pFacade;
				}
			}
			else if (supplier instanceof IClassifier)
			{         
				IClassifier pClassifier = (IClassifier)supplier;
	
				// Transform the classifier into an iterface.

				// The transform may fail.  RT will deny the transform if it is 
				// in a generalization or implemenation.
				outSupplier = (IInterface) pClassifier.transform(new String("Interface"));
			}
		}
		else
		{
            outSupplier = (IInterface) supplier;
		}
	    
		if (outSupplier != null)
		{
			dep = createDependency2( client, outSupplier, "Implementation", space);
		}
        
        return new ETPairT<IInterface, IDependency>(outSupplier, dep);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#CreatePresentationReference(com.embarcadero.describe.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public IPresentationReference createPresentationReference(
		IElement referencingElement,
		IPresentationElement referredElement) 
	{
		IPresentationReference ref = null;
		FactoryRetriever retriever = FactoryRetriever.instance();
		if (retriever != null)
		{
			Object unknown = retriever.createType("PresentationReference",null);			
			if (unknown instanceof IPresentationReference)
			{
				ref = (IPresentationReference)unknown;
			}
			if (ref != null)
			{
				RelationshipEventsHelper helper = 
						 				new RelationshipEventsHelper(ref);
				if ( helper.firePreRelationCreated(referencingElement, 
												   referredElement))
				{
					ref.setReferencingElement(referencingElement);
					ref.setReferredElement(referredElement);
					helper.fireRelationCreated();								 				
				}
			}
		}
		return ref;		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#createReference(com.embarcadero.describe.foundation.IElement, com.embarcadero.describe.foundation.IElement)
	 */
	public IReference createReference(
		IElement referencingElement,
		IElement referredElement) 
	{
		IReference ref = null;
		FactoryRetriever retriever = FactoryRetriever.instance();
		if (retriever != null)
		{
			Object unknown = retriever.createType("Reference",null);			
			if (unknown instanceof IReference)
			{
				ref = (IReference)unknown;
			}
			if (ref != null)
			{
				RelationshipEventsHelper helper = 
										new RelationshipEventsHelper(ref);
				if ( helper.firePreRelationCreated(referencingElement, 
												   referredElement))
				{
					ref.setReferencingElement(referencingElement);
					ref.setReferredElement(referredElement);
					helper.fireRelationCreated();								 				
				}
			}
		}
		return ref;		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#createGeneralization(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
	 */
	public IGeneralization createGeneralization(
		IClassifier parent,
		IClassifier child)
	{
		if (parent == null || child == null)
			return null;

		IGeneralization gen = null;
		try
		{
			FactoryRetriever fact = FactoryRetriever.instance();
			
			if (fact != null)
			{
				Object unk = fact.createType(new String("Generalization"), null);
				
				if (unk != null && unk instanceof IGeneralization)
				{
					gen = (IGeneralization)unk;

					RelationshipEventsHelper helper = new RelationshipEventsHelper(gen);

					if (helper.firePreRelationCreated( child, parent))
					{
						// Set the specific member first, making sure the generalization
						// link is part of the DOM before the general side is set. This
						// is for version control purposes. We need to be able to see if the
						// element being set is versioned, or a parent of that element is versioned

						gen.setSpecific(child);
						gen.setGeneral(parent);

						helper.fireRelationCreated();
					}
					else
						gen = null;
				}
			}
		}
		catch( Exception e)
		{
			//hr = err.Error();
			//_RPT1( _CRT_ERROR, "Problem in : %s", err.ErrorMessage());
			e.printStackTrace();
			gen = null;
		}

		return gen;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#createAssociation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
	 */
	public IAssociation createAssociation(
		IClassifier start,
		IClassifier end,
		INamespace space)
	{
		return createAssociation2( start, end, AssociationKindEnum.AK_ASSOCIATION, false, false, space);
	}

	public IAssociationClass createAssociationClass(IClassifier start, IClassifier end, int kind, boolean startNavigable, boolean endNavigable, INamespace space)
	{
		if (start == null || end == null)
				return null;

			IAssociationClass assoc = null;
			try
			{
				FactoryRetriever fact = FactoryRetriever.instance();

				if (fact != null)
				{
					String assocKind = AssociationKindTrans(kind);

					Object unk = fact.createType(assocKind, null);

					if (unk != null && unk instanceof IAssociationClass)
					{
						assoc = (IAssociationClass)unk;

						if (assoc != null)
						{
							RelationshipEventsHelper helper = new RelationshipEventsHelper(assoc);

							if (helper.firePreRelationCreated(start, end))
							{
								if (kind == AssociationKindEnum.AK_COMPOSITION && 
									unk instanceof IAggregation)
								{
									IAggregation agg = (IAggregation)unk;
									 agg.setIsComposite(true);
								}

								// If the caller is providing a namespace element,
								// add the new generalization to that namespace
								if (space == null)
									space = start.getNamespace();

								if (space != null)
								{
									space.addOwnedElement(assoc);

									if (kind == AssociationKindEnum.AK_COMPOSITION ||
										kind == AssociationKindEnum.AK_AGGREGATION)
									{
										handleAggregation(assoc, start, end, 
											startNavigable, endNavigable);
									}
									else
									{
										addEnd(assoc, startNavigable, start);
										addEnd(assoc, endNavigable, end);
									}
							
								}

								helper.fireRelationCreated();
							}
						}
					}
				}  
			}
			catch( Exception e)
			{
				e.printStackTrace();
				assoc = null;
			}

			return assoc;		
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#createAssociation2(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, int, boolean, boolean, org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
	 */
	public IAssociation createAssociation2(
		IClassifier start,
		IClassifier end,
		int kind,
		boolean startNavigable,
		boolean endNavigable,
		INamespace space)
	{
		if (start == null || end == null)
			return null;

		IAssociation assoc = null;
		try
		{
			FactoryRetriever fact = FactoryRetriever.instance();

			if (fact != null)
			{
				String assocKind = AssociationKindTrans(kind);

				Object unk = fact.createType(assocKind, null);

				if (unk != null && unk instanceof IAssociation)
				{
					assoc = (IAssociation)unk;

					if (assoc != null)
					{
						RelationshipEventsHelper helper = new RelationshipEventsHelper( assoc);

						if (helper.firePreRelationCreated( start, end))
						{
							if (kind == AssociationKindEnum.AK_COMPOSITION && 
								unk instanceof IAggregation)
							{
								IAggregation agg = (IAggregation)unk;
								 agg.setIsComposite(true);
							}

							// If the caller is providing a namespace element,
							// add the new generalization to that namespace
							if (space == null)
								space = start.getNamespace();

							if (space != null)
							{
								space.addOwnedElement(assoc);

								if (kind == AssociationKindEnum.AK_COMPOSITION ||
									kind == AssociationKindEnum.AK_AGGREGATION)
								{
									handleAggregation(assoc, start, end, 
										startNavigable, endNavigable);
								}
								else
								{
									addEnd(assoc, startNavigable, start);
									addEnd(assoc, endNavigable, end);
								}
							}

							helper.fireRelationCreated();
						}
					}
				}
			}  
		}
		catch( Exception e)
		{
			e.printStackTrace();
			assoc = null;
			// hr = COMErrorManager::ReportError( err);
		}

		return assoc;
	}

	/**
	 *
	 * Creates a new Dependency relationship
	 *
	 * @param client The client element
	 * @param supplier The supplier element
	 * @param space The namespace that will own the new relationship
	 * 
	 * @return The new Dependency
	 */
	public IDependency createDependency(
										INamedElement client,
										INamedElement supplier,
										INamespace space) 
	{
		return createDependency( client, supplier, space, "Dependency");		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#createDependency2(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
	 */
	public IDependency createDependency2(
		INamedElement client,
		INamedElement supplier,
		String depType,
		INamespace space) 
	{
		return createDependency( client, supplier, space, depType);
	}
	
	/**
	 *
	 * Creates the appropriate Dependency relationship
	 *
	 * @param client The client element
	 * @param supplier The supplier element
	 * @param space The namespace that will own the new relationship
	 * @param depType The type of Dependency relationship
	 * 
	 * @return dep The new Dependency relationship
	 *	 
	 */
	private IDependency createDependency(
										INamedElement client,
										INamedElement supplier,
										INamespace space,
										String depType) 
	{
		IDependency dep = null;
		FactoryRetriever retriever = FactoryRetriever.instance();
		if (retriever != null)
		{
			Object unknown = retriever.createType(depType, space);			
			if (unknown instanceof IDependency)
			{
				dep = (IDependency)unknown;
                space.addElement(dep);
			}
			if (dep != null)
			{
				RelationshipEventsHelper helper = 
										new RelationshipEventsHelper(dep);
				if ( helper.firePreRelationCreated(client, supplier) )
				{
					dep.setClient(client);
					dep.setSupplier(supplier);

					// If the caller is providing a namespace element,
					// add the new dependency to that namespace
					if (space != null)
					{
						space.addElement(dep);
					}
					helper.fireRelationCreated();								 				
				}
			}
		}
		return dep;		
	}
	


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#determineCommonRelations(null)
	 */
	public ETList<IRelationProxy> determineCommonRelations(ETList<IElement> elements) 
	{
		return new RelationRetriever(elements).retrieveRelations();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#determineCommonRelations2(java.lang.String, null)
	 */
	public ETList<IRelationProxy> determineCommonRelations2(
		String pDiagramXMIID,
		ETList<IElement> elements)
	{
        return new RelationRetriever(pDiagramXMIID, elements).retrieveRelations();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#determineCommonRelations3(null, null)
	 */
	public ETList<IRelationProxy> determineCommonRelations3(
        ETList<IElement> elements,
        ETList<IElement> elementsOnDiagram)
	{
		return new RelationRetriever(elements, elementsOnDiagram).retrieveRelations();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory#createDerivation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
	 */
	public IDerivation createDerivation(IClassifier instanciation,
                                            IClassifier actualTemplate)
        {
            IDerivation derivation = null;
            ETList<IParameterableElement> parms =
                    actualTemplate.getTemplateParameters();
            
            if (parms != null)
            {
                int numOfParms = parms.size();
                if (numOfParms > 0)
                {
                    TypedFactoryRetriever<IDerivation> retriever =
                            new TypedFactoryRetriever<IDerivation>();
                    
                    derivation = retriever.createType("Derivation");
                    RelationshipEventsHelper helper =
                            new RelationshipEventsHelper(derivation);
                    if ( helper.firePreRelationCreated(instanciation, actualTemplate) )
                    {
                        createBindings(instanciation, derivation, parms, numOfParms);
                        derivation.setTemplate(actualTemplate);
                        instanciation.setDerivation(derivation);
                        helper.fireRelationCreated();
                    }
                }
            }
            return derivation;
        }


	/**
	 *
	 * Creates either a PackageImport or ElementImport in the Package namespace of the importingElement.
	 * If elementToImport is a Package, then a PackageImport is created, else and ElementImport.
	 *
	 * @param importingElement   The element importing the other.
	 * @param elementToImport    The element being imported
	 * @return The import element
	 */
	public IDirectedRelationship createImport(
									IElement importingElement,
									IAutonomousElement elementToImport) 
	{
		MetaLayerRelationFactory relFact = MetaLayerRelationFactory.instance();
		return relFact.createImport(importingElement, elementToImport);
	}

	/**
	 *
	 * Translates the enum value into a string.
	 *
	 * @param kind[in] The enum value of the association kind
	 *
	 * @return The translated value
	 *
	 */
	protected String AssociationKindTrans(int kind)
	{
		String assocType = null;
	   
		switch( kind)
		{
			case AssociationKindEnum.AK_ASSOCIATION:
			assocType = new String("Association");
			break;

			case  AssociationKindEnum.AK_AGGREGATION:
			case  AssociationKindEnum.AK_COMPOSITION:
			assocType  = new String("Aggregation");
			break;

			case  AssociationKindEnum.AK_ASSOCIATION_CLASS:
			assocType = new String("AssociationClass");
			break;
		}
		return assocType;
	}

	/**
	 *
	 * Handles the creation and setting of the appropriate ends
	 * on the passed in Aggregation.
	 *
	 * @param assoc[in] An Aggregation association
	 * @param start[in] The classifier participating in the aggregate end
	 * @param end[in] The classifier participating in the part end
	 * @param startNavigable[in] Is the aggregate end navigable?
	 * @param endNavigable[in] Is the part end navigable?
	 *
	 * @return boolean
	 *
	 */
	protected boolean handleAggregation( IAssociation assoc, 
		IClassifier start, 
		IClassifier end, 
		boolean startNavigable, 
		boolean endNavigable)
	{
		if (assoc == null || start == null || end == null)
			return false;

		boolean hr = true;
		try
		{
			if (assoc instanceof IAggregation)
			{
				IAggregation agg = (IAggregation)assoc;

				// Handle the aggregate end 

				IAssociationEnd assocEnd = startNavigable ? createNavigableEnd(start) :
					createEnd(false, start);

				if (assocEnd != null)
					agg.setAggregateEnd(assocEnd);

				// Handle the part end 
				assocEnd = endNavigable ? createNavigableEnd( end) :
					createEnd( false, end);

				if (assocEnd != null)
					agg.setPartEnd( assocEnd);
			}
		}
		catch( Exception e)
		{
			e.printStackTrace();
		//	hr = COMErrorManager::ReportError( err);
		}
		return hr;
	}

	/**
	 *
	 * Creates either a navigable or normal association end.
	 *
	 * @param isNavigable[in] true to create a NavigableEnd, else false
	 * @param type[in] The participant on the new end
	 *
	 * @return The new IAssociationEnd 
	 *
	 */
	protected IAssociationEnd createEnd( boolean isNavigable, 
		IClassifier type)
	{
		if (type == null)
			return null;

		IAssociationEnd end = null;

		try
		{
			end = isNavigable ? createNavigableEnd(type) :
				createEnd(new String("AssociationEnd"), type);
		}
		catch( Exception e)
		{
			e.printStackTrace();
		//	hr = COMErrorManager::ReportError( err);
		}

		return end;
	}

	/**
	 *
	 * Creates an AssociationEnd of the type specified.
	 *
	 * @param endType[in] The name of the AssociationEnd. Currently,
	 *                    only two types are allowed, "AssociationEnd"
	 *                    and "NavigableEnd"
	 * @param type[in] The participant on the end
	 *
	 * @return The new IAssociationEnd 
	 *
	 */

	protected IAssociationEnd createEnd( String endType, 
		IClassifier type)
	{
		if (type == null || endType == null )
			return null;

		IAssociationEnd end = null;
		try
		{      
			FactoryRetriever fact = FactoryRetriever.instance();
      
			if ( fact != null )
			{
				Object unk = fact.createType(endType, null);
         
				if ( unk instanceof IAssociationEnd )
				{
					end = (IAssociationEnd)unk;
					end.setType( type );
					//_VH( assocEnd.CopyTo( end ));
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			end = null;
			//hr = COMErrorManager::ReportError( err );
		}

		return end;
	}


	/**
	 *
	 * Creates a NavigableEnd object, setting the participant on the end.
	 *
	 * @param type[in] The participant
	 *
	 * @return IAssociationEnd
	 *
	 */
	protected IAssociationEnd createNavigableEnd(IClassifier type)
	{
		if (type == null)
			return null;

		IAssociationEnd end = null;

		try
		{
			end = createEnd(new String("NavigableEnd"), type);
		}
		catch( Exception e)
		{
			e.printStackTrace();
			end = null;
			// hr = COMErrorManager::ReportError( err);
		}

		return end;
	}

	/**
	 *
	 * Adds a new AssociationEnd to the passed in Association.
	 *
	 * @param assoc[in] The Association to affect
	 * @param isNavigable[in] True if the new end is navigable, else false
	 * @param type[in] The type of the end
	 *
	 * @return boolean
	 *
	 */
	protected boolean addEnd(IAssociation assoc, boolean isNavigable, IClassifier type)
	{
		if (assoc == null ||type == null)
			return false;

		boolean hr = true;

		try
		{
			if (isNavigable)
			{
				hr = addNavigableEnd( assoc, type);
			}
			else
			{
				IAssociationEnd assocEnd = assoc.addEnd2(type);
				if (assocEnd == null)
					hr = false;
			}
		}
		catch( Exception e)
		{
			e.printStackTrace();
			hr = false;
			// hr = COMErrorManager::ReportError( err);
		}

		return hr;
	}

	/**
	 *
	 * Creates then adds a navigable end to the passed in association.
	 *
	 * @param assoc[in] The association to add to
	 * @param type[in] The type that is the participant on the new end
	 *
	 * @return boolean
	 *
	 */
	protected boolean addNavigableEnd( IAssociation assoc, IClassifier type)
	{
		if (assoc == null ||type == null)
			return false;

		boolean hr = true;
		try
		{
			IAssociationEnd nav = createNavigableEnd(type);
			if (nav != null)
				assoc.addEnd(nav);
			else
				hr = false;
		}
		catch( Exception e)
		{
			e.printStackTrace();
			hr = false;
			// hr = COMErrorManager::ReportError( err);
		}

		return hr;
	}
	/**
	 *
	 * Creates the UMLBinding relationships for the formal parameters passed in. Default
	 * types will be used for the bindings, unless a type constraint is dictated.
	 *
	 * @param instanciation The classifier that is instanciating a template. Used to
	 *                         determine default types for bindings.
	 * @param pDerivation  The containing Derivation relationship for the new bindings
	 * @param parms        The template parameters that will be bound
	 * @param numParms     The number of template parameters
	 */
	protected void createBindings(IClassifier instanciation,
                                      IDerivation pDerivation,
                                      ETList<IParameterableElement> parms,
                                      int numParms )
        {
            for (int i=0;i<numParms;i++)
            {
                IParameterableElement parm = parms.get(i);
                if (parm != null)
                {
                    // Check to see if there is a default type already established on the parm.
                    // If there is one, use that when binding.
                    TypedFactoryRetriever<IUMLBinding> fact =
                            new TypedFactoryRetriever<IUMLBinding>();
                    
                    IUMLBinding binding = fact.createType("Binding");
                    IParameterableElement defaultParm = parm.getDefaultElement();
                    if (defaultParm == null)
                    {
                        // If the template parameter has a type constraint, then we
                        // will leave the actual end of the Binding relationship empty, to be
                        // filled in by the user
                        String typeConstraint = parm.getTypeConstraint();
                        if (typeConstraint != null)
                        {
                            // If there is no default template parameter and no type constraint,
                            // then we will just bind to a default type.
                            defaultParm = retrieveDefaultTemplateParameter( instanciation);
                        }
                    }
                    if (defaultParm != null)
                    {
                        binding.setActual(defaultParm);
                    }
                    binding.setFormal(parm);
                    pDerivation.addBinding(binding);
                }
            }
        }
	
	/**
	 *
	 * Retrieves a parameterable element that can be used for a default binding to a formal template
	 * parameter
	 *
	 * @param classifier[in]   The classifier who is instanciating a template
	 * @return parm        The default parameter, else null.
	 */
	protected IParameterableElement retrieveDefaultTemplateParameter(IClassifier classifier)
	{
		IParameterableElement parm = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			ILanguageManager langMan = prod.getLanguageManager();
			if (langMan != null)
			{
				ILanguageDataType dataType = langMan.getAttributeDefaultType(classifier);
				if (dataType != null)
				{
					String typeName = dataType.getName();
					parm = findType(classifier,typeName);
				}
			}
		}
		return parm;
	}
	
	/**
	 *
	 * Finds the passed in type in the document that holds the passed in classifier
	 *
	 * @param classifier   The classifier whose document will be searched for type resolution
	 * @param typeName     The name of the type to find
	 * @return parm        The ParameterableElement that corresponds to the passed in type name
	 */
	protected IParameterableElement findType(IClassifier classifier, String typeName)
	{
		IParameterableElement parmEle = null;
		if (typeName != null && typeName.length() > 0)
		{
			Node node = classifier.getNode();
			if (node != null)
			{
				Document doc = node.getDocument();
				if (doc != null)
				{
					try
					{
						ETList<INamedElement> foundElems = 
										UMLXMLManip.findByName(doc, typeName, null, false, true);
						if (foundElems != null)
						{
							int numElems = foundElems.size();
							for (int i=0;i<numElems;i++)
							{
								INamedElement element = foundElems.get(i);
								parmEle = (IParameterableElement)element;
							}		
						}	
					}
					catch (ProjectMissingException e)
					{									 
					}
				}
			}
		}
		return parmEle;
	}
    
    private static class RelationRetriever
    {
        public RelationRetriever(ETList<IElement> elements )
        {
            establishElementMap(elements);
        }
        
        public RelationRetriever(ETList<IElement> elements, ETList<IElement> elementsOnDiagram)
        {
            establishElementMap(elements);
            addToElementMap(elementsOnDiagram);
        }
        
        public RelationRetriever(String pDiagramXMIID, ETList<IElement> elements )
        {
            establishElementMap(elements);
            m_DiagramXMIID = pDiagramXMIID;
        }
        
        private void establishElementMap(ETList<IElement> els)
        {
            m_NewElements = els;
            addToElementMap(els);
        }
        
        private void addToElementMap(ETList<IElement> els)
        {
            if (els != null)
            {
                for (int i = els.size() - 1; i >= 0; --i)
                {
                    IElement el = els.get(i);
                    if(el != null)
                    {
                        String xmiID = el.getXMIID();
                        m_Elements.put(xmiID, el);
                    }
                }
            }
        }
        
        private boolean isInMemory(IElement el)
        {
            IProject proj;
            if (el != null && (proj = el.getProject()) != null)
            {
                ITypeManager typeMan = proj.getTypeManager();
                if (typeMan != null)
                    return typeMan.verifyInMemoryStatus(el);
            }
            return false;
        }

        public ETList<IRelationProxy> retrieveRelations()
        {
            m_Results.clear();
            retrieveRelationProxies();
            return m_Results;
        }
        
        private void retrieveRelationProxies()
        {
            for (int i = 0; i < m_NewElements.size(); ++i)
            {
                IElement elem = m_NewElements.get(i);
                if (elem != null)
                {
                    retrieveClassifierRelations(elem);
                    retrieveUseCaseRelations(elem);
                    retrieveCollaborationRelations(elem);
                    retrieveActivityEdgeRelations(elem);
                }
            }
        }
        
        private void retrieveActivityEdgeRelations(IElement element)
        {
            addRelationThroughAttWithMultIDs(element, "outgoing", "target", true);
            addRelationThroughAttWithMultIDs(element, "incoming", "source", false);
        }
        
      private void retrieveCollaborationRelations(IElement element)
      {
         if (null == element)  throw new IllegalArgumentException();

         // It is possible that the lifeline does not contain a part,
         // So this code does not look for the part, just any associated message connectors.

         if ( element instanceof ILifeline )
         {
            ILifeline lifeLine = (ILifeline)element;

            IInteraction interaction = lifeLine.getInteraction();
            if ( interaction != null )
            {
               ETList< IConnector > connectors = interaction.getConnectors();
               if ( connectors != null )
               {
                  for (Iterator iter = connectors.iterator(); iter.hasNext();)
                  {
                     IConnector connector = (IConnector)iter.next();

                     if (connector instanceof IMessageConnector)
                     {
                        IMessageConnector messageConnector = (IMessageConnector)connector;
                        
                        ILifeline fromLine = messageConnector.getFromLifeline();
                        ILifeline toLine = messageConnector.getToLifeline();

                        if ((fromLine != null) &&
                            (toLine != null) )
                        {
                           // Determine the direction of the reletionship,
                           // remembering the other end's ID so we can ensure that
                           // it is in the list of elements that are being checked for relationships.

                           String strOtherEndID = "";

                           // Since all the message connectors are being checked,
                           // only continue if the input element is either
                           // the to or from lifeline of the message connector.

                           boolean bFromIsSame = lifeLine.isSame( fromLine );
                           boolean bToIsSame = false;

                           if (bFromIsSame)
                           {
                              strOtherEndID = toLine.getXMIID();
                           }
                           else
                           {
                              bToIsSame =lifeLine.isSame( toLine );
                              if (bToIsSame)
                              {
                                 strOtherEndID = fromLine.getXMIID();
                              }
                           }

                           if ((bFromIsSame || bToIsSame) && (strOtherEndID.length() > 0))
                           {
                              // This check ensures the "other" end of the message connector
                              // is in the list of element that are bing checked for reletionships.
                              IElement elM = m_Elements.get(URILocator.retrieveRawID(strOtherEndID));
                              if ( elM != null )
                              {
                                 addRelation( fromLine, toLine, messageConnector );
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
        
        private void retrieveUseCaseRelations(IElement element)
        {
            if (!(element instanceof IUseCase)) return ;
            
            IUseCase useCase = (IUseCase) element;
            retrieveIncludeRelations(useCase);
            retrieveExtendRelations(useCase);
        }
        
        private void retrieveIncludeRelations(IUseCase useCase)
        {
            buildOwnedRelations(useCase, 
                "./UML:Element.ownedElement/UML:Include",
                "target", "owner", "Include", "includedBy", false);
        }
        
        private void retrieveExtendRelations(IUseCase useCase)
        {
            buildOwnedRelations(useCase, 
                "./UML:Element.ownedElement/UML:Extend",
                "target", "owner", "Extend", "extendedBy", false);
        }
        
        private void retrieveClassifierRelations(IElement elem)
        {
            retrieveGeneralizationRelations(elem);
            retrieveDependencyRelations(elem);
            retrieveAssociationRelations(elem);
        }
        
        private void retrieveDependencyRelations(IElement element)
        {
            addRelationThroughAttWithMultIDs(element, "clientDependency", "supplier", true);
            addRelationThroughAttWithMultIDs(element, "supplierDependency", "client", false);
        }
        
        private void retrieveAssociationRelations(IElement element)
        {
            if (!(element instanceof IClassifier)) return ;
            IClassifier firstClassifier = (IClassifier) element;
            
            ETList<IAssociationEnd> ends = firstClassifier.getAssociationEnds();
            if (ends == null || ends.size() == 0) return;
            
            int endCount = ends.size();
            for (int i = 0; i < endCount; ++i)
            {
                IAssociationEnd end = ends.get(i);
                ETList<IAssociationEnd> otherEnds = end.getOtherEnd();
                if (otherEnds == null || otherEnds.size() == 0) continue;
                
                int numOthers = otherEnds.size();
                for (int j = 0; j < numOthers; ++j)
                {
                    IAssociationEnd other = otherEnds.get(j);
                    IClassifier otherClassifier = other.getParticipant();
                    if (otherClassifier == null) continue;

                    IElement elM = m_Elements.get(URILocator.retrieveRawID(otherClassifier.getXMIID()));
                    if (elM != null)
                        addRelation(firstClassifier, otherClassifier, other.getAssociation());
                }
            }
        }
        
        private void retrieveGeneralizationRelations(IElement element)
        {
            buildOwnedRelations(element, 
                "./UML:Classifier.generalization/UML:Generalization",
                "general", "specific", "Generalization", "specialization",
                false);
        }
        
        private void buildOwnedRelations(IElement el, String query, 
            String endAttrName, String otherEndAttrName, String relTypeName,
            String otherEndListName, boolean isFrom)
        {
            // We're going behind the covers for performance reasons. We're checking for specific xml attributes
            // and features. If an element is a super class for instance, it won't contain the actual generalization element. It
            // will just have the "specialization" attribute. The subclass WILL contain the generalization.
            
            Node node = el.getNode();
            if (node != null)
            {
                // Let's see if the firstNode contains the generalization. If it does, it's the subclass.
                List gens = node.selectNodes(query);
                
                if (gens != null && gens.size() > 0)
                {
                    int genCount = gens.size();
                    for (int i = 0; i < genCount; ++i)
                    {
                        Node gen = (Node) gens.get(0);
                        if (gen == null || !(gen instanceof Element)) continue;
                        
                        Element genEl = (Element) gen;
                        String generalID = genEl.attributeValue(endAttrName);
                        IElement elem = m_Elements.get(URILocator.retrieveRawID(generalID));
                        if (elem != null)
                            addRelationWithNode(el, elem, gen, relTypeName);
                    }
                }
                addRelationThroughAttWithMultIDs(el, otherEndListName, otherEndAttrName, isFrom);
            }
        }
        
        private void addRelationThroughAttWithMultIDs(IElement element,
            String multiAttrName, String attrName,  boolean fromNode)
        {
            Element elementNode = element.getElementNode();
            String specIDs = elementNode.attributeValue(multiAttrName);
            Document doc = elementNode.getDocument();
            if (specIDs != null && specIDs.length() > 0)
            {
                ETList<String> tokens = StringUtilities.splitOnDelimiter(specIDs, " ");
                for (Iterator<String> iter = tokens.iterator(); iter.hasNext(); )
                {
                    String token = iter.next().trim();
					String queryId = URILocator.retrieveRawID(token);
					Node genNode = null;
					if (doc != null)
					{
						genNode = doc.elementByID(queryId);
					} 
                    
                    if (genNode == null)
                    {
                   		ETSystem.out.println("Relation Factory addRelationThroughAttWithMultIDs failed to find " + queryId);
                    	continue;                    
                    }
                    else
                    { 
						String specific = ((Element) genNode).attributeValue(attrName);
						if (specific == null || specific.length() == 0) continue;
                    
						IElement el = m_Elements.get(URILocator.retrieveRawID(specific));
						if (el == null) continue;
                    
						String relName = XMLManip.retrieveSimpleName(genNode);
						if (fromNode)
							addRelationWithNode(element, el, genNode, relName);
						else
							addRelationWithNode(el, element, genNode, relName);
                    }
                }
            }
        }
        
        private void addRelationWithNode(IElement subE, IElement superE, 
            Node genLink, String nodeName)
        {
            FactoryRetriever fact = FactoryRetriever.instance();
            Object element = fact.createTypeAndFill(nodeName, genLink);
            if (element != null)
                addRelation(subE, superE, (IElement) element);
        }
        
        private void addRelation(IElement from, IElement to, IElement connection)
        {
            boolean okToAdd = true;
            for (int i = m_Results.size() - 1; i >= 0; --i)
            {
                IRelationProxy proxy = m_Results.get(i);
                if (proxy.matches(from, to, connection))
                {
                    okToAdd = false;
                    break;
                }
            }
            if (okToAdd)
            {
                IRelationProxy rel = new RelationProxy();
                rel.setFrom(from);
                rel.setTo(to);
                rel.setConnection(connection);
                
                m_Results.add(rel);
            }
        }
        
        
        
        private ETList<IElement> m_NewElements;
        private String m_DiagramXMIID;
        private ETList<IRelationProxy> m_Results = new ETArrayList<IRelationProxy>();
        private HashMap<String, IElement> m_Elements = new HashMap<String, IElement>();
    }
}