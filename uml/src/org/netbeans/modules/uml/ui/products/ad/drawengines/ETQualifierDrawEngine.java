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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADAttributeListCompartment;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.Iterator;

/**
 * @author KevinM
 *
 */
public class ETQualifierDrawEngine extends ADNodeDrawEngine implements IQualifierDrawEngine
{

	public ETQualifierDrawEngine()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct) 
	{
		IETSize size = null;
		try 
		{
			size = super.calculateOptimumSize(pDrawInfo, true);

			// allow 1 pixel all around for border thickness
			// NOTE: when border thickness becomes a preference we'll have to multiply it by 2
			if (size != null)
			{
				size.setWidth(size.getWidth() + 2);
				size.setHeight(size.getHeight() + 2);
				return bAt100Pct == false ? this.scaleSize(size, pDrawInfo != null ? pDrawInfo.getTSTransform() : this.getTransform()) : size;
			}
			else
				return null;			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return size;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo pDrawInfo)
	{
		try
		{
			final Color crBorder = this.getBorderBoundsColor(); //GetColorDefaultText( CK_BORDERCOLOR, pTSEDrawInfo.dc() );

			IETRect boundingRect = pDrawInfo.getDeviceBounds();
            float centerX = (float)boundingRect.getCenterX();
            GradientPaint paint = new GradientPaint(centerX,
                             boundingRect.getBottom(),
                             getBkColor(),
                             centerX,
                             boundingRect.getTop(),
                             getLightGradientFillColor());
        
			// draw our frame
			GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(), boundingRect.getRectangle(), crBorder, paint);

			// Draw each compartment now
			handleNameListCompartmentDraw(pDrawInfo, boundingRect);

			// This will draw an invalid frame around the node if it doesn't have an IElement
			drawInvalidRectangle(pDrawInfo);

			// Put the selection handles
			//GDISupport.drawSelectionHandles(pInfo);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID()
	{
		return "QualifierDrawEngine";
	}

	/*
	 * 
	 */
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Qualifier");
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
	 */
	public void onGraphEvent(int nKind)
	{
		boolean hr = true;
		try
		{
			if (nKind == IGraphEventKind.GEK_POST_MOVE || 
				nKind == IGraphEventKind.GEK_POST_SMARTDRAW_MOVE ||
			 	nKind == IGraphEventKind.GEK_POST_RESIZE ||
			 	nKind == IGraphEventKind.GEK_POST_SELECT)
			{

				INodeDrawEngine pParentDrawEngine = getReferringParentToThisQualifier();

				// assert (pParentDrawEngine);
				if (pParentDrawEngine != null)
				{
					TSENode pParentTSENode = TypeConversions.getOwnerNode(pParentDrawEngine);

					//assert (pParentTSENode);
					if (pParentTSENode != null)
					{
						switch (nKind)
						{
							case IGraphEventKind.GEK_POST_MOVE :
							case IGraphEventKind.GEK_POST_SMARTDRAW_MOVE :
								{
									if (!qualifierWasReconnectedToDifferentNode())
									{
										pParentDrawEngine.relocateQualifiers(false);
									}
								}
								break;
							case IGraphEventKind.GEK_POST_RESIZE :
								{
									pParentDrawEngine.relocateQualifiers(false);
								}
								break;
							case IGraphEventKind.GEK_POST_SELECT:
							{
								if (!pParentTSENode.isSelected())
								{
									this.selectAllCompartments(false);
								}
								break;
							}
						}
					}
				}
			}
			else if (nKind == IGraphEventKind.GEK_POST_PASTE_ALL)
			{
				reestablishPresentationReference();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
	 */
	public void sizeToContents()
	{
		try
		{
			super.sizeToContents();

			// Tell our parent to relocate us
			INodeDrawEngine pParentDrawEngine = getReferringParentToThisQualifier();
			if (pParentDrawEngine != null)
			{
				pParentDrawEngine.relocateQualifiers(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
	 */
	public void createCompartments() throws ETException
	{
		try
		{
			clearCompartments();
			createAndAddCompartment("ADAttributeListCompartment", 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void initCompartments(IPresentationElement pElement)
	{
		try
		{
			// We may get here with no compartments.  This happens if we've been created
			// by the user.  If we read from a file then the compartments have been pre-created and
			// we just need to initialize them.
			if (getNumCompartments() == 0)
			{
				createCompartments();
			}

			IElement pModelElement = pElement != null ? pElement.getFirstSubject() : null;
			if (pModelElement != null && getNumCompartments() > 0)
			{
				// Don't _VH here because we don't want to throw.
				IAssociationEnd pAssocEnd = pModelElement instanceof IAssociationEnd ? (IAssociationEnd) pModelElement : null;
				if (pAssocEnd != null)
				{
					IADAttributeListCompartment pAttributesCompartment = getCompartmentByKind(IADAttributeListCompartment.class);

					if (pAttributesCompartment != null)
					{
						// Get all the attributes
						ETList < IAttribute > pAttributes = pAssocEnd.getQualifiers();
						if (pAttributes != null)
						{
							CollectionTranslator < IAttribute, IElement > trans = new CollectionTranslator < IAttribute, IElement > ();
							ETList < IElement > pElements = trans.copyCollection(pAttributes);

							//assert ( pElements );
							if (pElements != null)
							{
								pAttributesCompartment.attachElements(pElements, true, true);
							}
						}
						pAttributesCompartment.setName("Qualifiers");
					}
				} // pAssocEnd
				else
				{
					// not a classifier, something is wrong
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
        setFillColor("qualifierfill", 251, 233, 126);
        setLightGradientFillColor("qualifierlightgradientfill", 254, 254, 254);
		setBorderColor("qualifierborder", Color.BLACK);
		super.initResources();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement()
	{
		boolean bIsValid = false;
		try
		{

			String currentMetaType = getMetaTypeOfElement();
			if (currentMetaType != null && currentMetaType.equals("AssociationEnd"))
			{
				bIsValid = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bIsValid;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementDeleted(INotificationTargets pTargets)
	{
		long hr = 1;

		try
		{
			if (pTargets != null)
			{
				IElement pModelElement = pTargets.getChangedModelElement();
				IElement pSecondaryChangedME = pTargets.getSecondaryChangedModelElement();
				IFeature pFeature = pSecondaryChangedME instanceof IFeature ? (IFeature) pSecondaryChangedME : null;

				IElement theElement = pModelElement;
				IFeature theFeature = pFeature;

				// pModelElement and pFeature are valid, find which compartment should handle it
				// Don't _VH here because we don't want to throw.
				IAssociationEnd pAssocEnd = pModelElement instanceof IAssociationEnd ? (IAssociationEnd) pModelElement : null;
				if (pAssocEnd != null)
				{
					Iterator < ICompartment > iter = this.getCompartments().iterator();
					while (iter.hasNext())
					{
						ICompartment pCompartment = iter.next();

						if (pCompartment != null)
						{
							pCompartment.modelElementDeleted(pTargets);

							// if compartment is a list compartment and if empty and if DeleteWhenEmpty
							//whack it
							IListCompartment pListCompartment = pCompartment instanceof IListCompartment ? (IListCompartment) pCompartment : null;
							if (pListCompartment != null)
							{
								long nCount = pListCompartment.getNumCompartments();

								if (nCount == 0)
								{
									boolean bDelete = pListCompartment.getDeleteIfEmpty();

									if (bDelete == true)
									{
										removeCompartment(pCompartment);
										sizeToContents();
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return hr;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementHasChanged(INotificationTargets pTargets)
	{

		long hr = 1;

		try
		{
			if (pTargets != null)
			{
				IElement pModelElement = pTargets.getChangedModelElement();
				IElement pSecondaryChangedME = pTargets.getSecondaryChangedModelElement();
				IFeature pFeature = pSecondaryChangedME instanceof IFeature ? (IFeature) pSecondaryChangedME : null;
				int nKind = pTargets.getKind();

				// See if the model element that changed was an ITaggedValue
				ITaggedValue pTaggedValue = pModelElement instanceof ITaggedValue ? (ITaggedValue) pModelElement : null;
				if (pTaggedValue != null)
				{
					if (pFeature == null)
					{
						IElement pOwner = pTaggedValue.getOwner();
						if (pOwner != null)
						{
							IFeature pTempFeature = pOwner instanceof IFeature ? (IFeature) pOwner : null;
							if (pTempFeature != null)
							{
								pFeature = pTempFeature;
								pTargets.setSecondaryChangedModelElement(pTempFeature);
							}
						}
					}
				}

				if (nKind != ModelElementChangedKind.MECK_ELEMENTMODIFIED || pFeature != null)
				{
					// determine if the feature has been redefined
					ETList < IRedefinableElement > pRedefines = pFeature != null ? pFeature.getRedefinedElements() : null;

					IADAttributeListCompartment pAttributesCompartment;

					// Get all the compartments
					pAttributesCompartment = getCompartmentByKind(IADAttributeListCompartment.class);

					String sFeatureType = pFeature != null ? pFeature.getElementType() : null;

					if (sFeatureType != null && sFeatureType.equals("Attribute") && pAttributesCompartment != null)
					{
						pAttributesCompartment.modelElementHasChanged(pTargets);
					}
				} // element modified

				postInvalidate();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return hr;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#postLoad()
	 */
	public long postLoad()
	{
		boolean bValid = false;
		try
		{
			// We validate the node to verify that the presentation references are
			// created correctly.
			bValid = validateNode();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return bValid ? 1 : 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#validateNode()
	 */
	public boolean validateNode()
	{
		boolean bValid = true;
		try
		{
			// CWaitCursor csr;

			bValid = false;

			IPresentationElement pPE = getPresentationElement();
			IDrawingAreaControl pControl = getDrawingArea();
			if (pPE != null && pControl != null)
			{
				bValid = true;
				IElement pModelElement = pPE.getFirstSubject();

				IAssociationEnd pAssocEnd = pModelElement instanceof IAssociationEnd ? (IAssociationEnd) pModelElement : null;
				if (pAssocEnd != null)
				{
					///
					// Validate all the compartments
					///
					IADAttributeListCompartment pAttributesCompartment = getCompartmentByKind(IADAttributeListCompartment.class);

					// Get all the compartments

					ETList < IAttribute > pAttributes = pAssocEnd.getQualifiers();

					// Get all the attributes
					if (bValid == true & pAttributes != null)
					{
						if (pAttributesCompartment != null)
						{
							CollectionTranslator < IAttribute, IElement > trans = new CollectionTranslator < IAttribute, IElement > ();
							ETList < IElement > pElements = trans.copyCollection(pAttributes);

							if (pElements != null)
							{
								bValid = pAttributesCompartment.validate2(pElements);
							}
						}
					}

					///
					// Validate connection to our end and the IAssociation
					///
					INodeDrawEngine pParentDrawEngine = getReferringParentToThisQualifier();
					if (pParentDrawEngine == null)
					{
						// We're disconnected.  We need to reconnect.  The easiest way is to ask our
						// IAssociationEdgePresentation to create a qualifier.
						IAssociation pAssociation = pAssocEnd.getAssociation();
						boolean bReconnected = false;
						IAssociationClass pAssocClass = pAssociation instanceof IAssociationClass ? (IAssociationClass) pAssociation : null;
						if (pAssociation != null && pControl != null && pAssocClass == null)
						{
							ETList < IPresentationElement > pEdgePEs = pControl.getAllItems2(pAssociation);
							long count = 0;

							if (pEdgePEs != null)
							{
								count = pEdgePEs.getCount();
							}

							for (int i = 0; i < count; i++)
							{
								IPresentationElement pPossibleAssociationEdge = pEdgePEs.item(i);

								// Here's the edge we should be attached to
								IAssociationEdgePresentation pAssociationEdgePresentation = pPossibleAssociationEdge instanceof IAssociationEdgePresentation ? (IAssociationEdgePresentation) pPossibleAssociationEdge : null;
								if (pAssociationEdgePresentation != null)
								{
									INodePresentation pQualifierAsNodePE = pPE instanceof INodePresentation ? (INodePresentation) pPE : null;

									// sert (pQualifierAsNodePE);
									if (pQualifierAsNodePE != null)
									{
										bReconnected = pAssociationEdgePresentation.reconnectToQualifierNode(pQualifierAsNodePE);
									}
									break;
								}
							}
						}

						if (!bReconnected)
						{
							// Delete the qualifier because it's not connected to aything
							pControl.postDeletePresentationElement(pPE);
						}
					}

					if (pParentDrawEngine != null)
					{
						// Initialize to false, back to true if we verify everythings ok
						bValid = false;

						// Make sure this draw engine represents the model element associated with this association
						// end
						IElement pParentDrawEngineME = TypeConversions.getElement(pParentDrawEngine);
						IClassifier pAssocEndParticipant = pAssocEnd.getParticipant();
						boolean bIsSame = false;
						if (pAssocEndParticipant != null && pParentDrawEngineME != null)
						{
							bIsSame = pParentDrawEngineME.isSame(pParentDrawEngineME);
							if (bIsSame)
							{
								bValid = true;
							}
						}
					}
					else
					{
						bValid = false;
					}
				}
				else
				{
					bValid = false;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bValid;
	}

	/**
	 Returns the draw engine of the parent for this qualifier
	 *
	 @param pDrawEngine [out] The draw engine of the parent
	 */
	protected INodeDrawEngine getReferringParentToThisQualifier()
	{
		INodeDrawEngine pDrawEngine = null;

		try
		{
			IPresentationElement pThisPE = getPresentationElement();
			if (pThisPE != null)
			{
				ETList < IPresentationElement > pQualifierParents;
				long count = 0;

				pQualifierParents = PresentationReferenceHelper.getAllReferencingElements(pThisPE);
				if (pQualifierParents != null)
				{
					count = pQualifierParents.getCount();
				}

				//assert (count == 1);
				if (count == 1)
				{
					IPresentationElement pParentPE = pQualifierParents.item(0);
					if (pParentPE != null)
					{
						pDrawEngine = TypeConversions.getDrawEngine(pParentPE) instanceof INodeDrawEngine ? (INodeDrawEngine) TypeConversions.getDrawEngine(pParentPE) : null;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return pDrawEngine;
	}

	/**
	 After a move this routine determines if the user was trying to move the association end
	 by relocating the qualifier
	
	 @return true if the qualifier was connected to a different node.
	 */
	protected boolean qualifierWasReconnectedToDifferentNode()
	{
		boolean bDidReconnect = false;
		boolean hr = true;
		try
		{
			// Get the current parent
			INodeDrawEngine pParentDrawEngine = getReferringParentToThisQualifier();
			if (pParentDrawEngine != null)
			{
				TSENode pCurrentParentNode = TypeConversions.getOwnerNode(pParentDrawEngine);
				TSConstRect parentNodeRect = pCurrentParentNode != null ? pCurrentParentNode.getBounds() : null;
				TSConstRect qualifierNodeRect = getOwnerNode() != null ? getOwnerNode().getBounds() : null;

				if (pCurrentParentNode != null && parentNodeRect != null && qualifierNodeRect != null)
				{
					// If we're near our current parent then assume the user hasn't moved the qualifier
					TSRect qualifierNodeRect2 = new TSRect(qualifierNodeRect);
					qualifierNodeRect2.merge(qualifierNodeRect.getLeft() + 2.5, qualifierNodeRect.getTop() + 2.5, qualifierNodeRect.getRight() + 2.5, qualifierNodeRect.getBottom() + 2.5);

					TSConstRect intersectRect = qualifierNodeRect2.intersection(parentNodeRect);

					if (intersectRect == null || intersectRect.isEmpty())
					{
						INodePresentation pNodePresentation;

						// We've been moved away.  Find a node that's closer
						pNodePresentation = getNodePresentationElement();
						if (pNodePresentation != null)
						{
							ETList < IPresentationElement > pOtherPEsWithinBoundingRect;
							long count = 0;

							pOtherPEsWithinBoundingRect = pNodePresentation.getPEsViaBoundingRect(true);

							if (pOtherPEsWithinBoundingRect != null)
							{
								count = pOtherPEsWithinBoundingRect.getCount();
							}

							// Find the first classifier and reconnect the association end to that guy
							for (int i = 0; i < count; i++)
							{
								IClassifier pClassifier;
								IPresentationElement pThisPE;

								pThisPE = pOtherPEsWithinBoundingRect.item(i);
								pClassifier = TypeConversions.getClassifier(pThisPE);
								if (pClassifier != null)
								{
									IElement pThisElement = this.getFirstModelElement();
									IAssociationEnd pThisAssocEnd = pThisElement instanceof IAssociationEnd ? (IAssociationEnd) pThisElement : null;

									// assert (pThisAssocEnd);
									if (pThisAssocEnd != null)
									{
										boolean bSuccessfullyReconnected = false;

										pThisAssocEnd.setParticipant(pClassifier);

										// Verify that the change took place
										IClassifier pCurrentElement = pThisAssocEnd.getParticipant();

										bSuccessfullyReconnected = pClassifier.isSame(pCurrentElement);
										if (bSuccessfullyReconnected)
										{
											// Reparent this qualifier's presentation reference relationship to the new
											// qualifier
											IPresentationReference pPresRef;

											PresentationReferenceHelper.removeAllPresentationReferences(pNodePresentation);
											pPresRef = PresentationReferenceHelper.createPresentationReference(pThisPE, pNodePresentation);
											//assert (pPresRef);
											if (pPresRef != null)
											{
												bDidReconnect = true;
											}
										}
									}
									break;
								}
							}
						}
					}
				}
			}

			if (bDidReconnect)
			{
				// If we did a reconnect then relocate the qualifier next to this new node.
				pParentDrawEngine = getReferringParentToThisQualifier();

				// assert (pParentDrawEngine);
				if (pParentDrawEngine != null)
				{
					pParentDrawEngine.relocateQualifiers(true);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bDidReconnect;
	}

	public INodePresentation getNodePresentationElement()
	{
		return getPresentationElement() instanceof INodePresentation ? (INodePresentation) getPresentationElement() : null;
	}

	/**
	 Called after a paste to reestablish any missing presentation references
	 */
	protected boolean reestablishPresentationReference()
	{
		boolean hr = false;
		try
		{
			// Get the association end participant
			IClassifier pAssociationEndParticipant = null;
			IPresentationElement pPEToAttachTo = null;
			INodePresentation pNodePresentation = getNodePresentationElement();

			IElement pThisElement = getFirstModelElement();
			IAssociationEnd pThisAssocEnd = pThisElement instanceof IAssociationEnd ? (IAssociationEnd) pThisElement : null;

			// assert (pThisAssocEnd);
			if (pThisAssocEnd != null)
			{
				pAssociationEndParticipant = pThisAssocEnd.getParticipant();
			}

			if (pAssociationEndParticipant != null)
			{
				pPEToAttachTo = pNodePresentation.findNearbyElement(true, pAssociationEndParticipant, "ClassDrawEngine");

				if (pPEToAttachTo != null)
				{
					IPresentationReference pPresRef;

					// If we found a presentation element then do the attach
					pPresRef = PresentationReferenceHelper.createPresentationReference(pPEToAttachTo, pNodePresentation);

					hr = pPresRef != null;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return hr;
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onResized()
	 */
	public void onResized()
	{		
		super.onResized();
		this.onGraphEvent(IGraphEventKind.GEK_POST_RESIZE);
	}

}
