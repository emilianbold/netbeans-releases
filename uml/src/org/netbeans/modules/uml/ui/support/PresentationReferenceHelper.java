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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.common.generics.ETPairT;
import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * @author sumitabhk
 *
 */
public class PresentationReferenceHelper
{

	/**
	 * 
	 */
	public PresentationReferenceHelper()
	{
		super();
	}

	/**
	 * Creates an IPresentationReference relationship
	 *
	 * @param pReferencing [in] The referencing PE
	 * @param pReferred [in] The referred PE
	 * @param pCreatedRelationship [out,retval] The created relationship
	 */
	public static IPresentationReference createPresentationReference(IPresentationElement pReferencing, IPresentationElement pReferred) {
		if (pReferencing == null || pReferred == null)
			return null;

		IPresentationReference pCreatedRelationship = null;

		try {

			IRelationFactory pFactory = new RelationFactory();
			// This is needed in order to NOT cause version control dialogs
			// to appear when reestablishing these relationships on a diagram
			// reopen. This was happening when opening a Component diagram that was
			// checked in.

			EventContextManager manager = new EventContextManager();         
         ETPairT < IEventContext, IEventDispatcher > contextInfo = manager.getNoEffectContext(pReferencing, 
                                                                                              EventDispatchNameKeeper.modifiedName(), 
                                                                                              "PresentationReferenceAdded");
            
         IEventDispatcher disp = contextInfo.getParamTwo();
         IEventContext presContext = contextInfo.getParamOne();
         
         ETPairT < IEventContext, IEventDispatcher > contextInfo2 = manager.getNoEffectContext(pReferred, 
                                                                                               EventDispatchNameKeeper.modifiedName(), 
                                                                                               "PresentationReferenceAdded");
            
         IEventDispatcher disp2 = contextInfo2.getParamTwo();
         IEventContext referredContext = contextInfo2.getParamOne();

			EventState state = new EventState(disp, presContext);
			EventState state2 = new EventState(disp2, referredContext);

         try
         {
            // Create a IPresentationReference relationship with pReferencing
            // being the owner of the relationship.
            pCreatedRelationship = pFactory.createPresentationReference(pReferencing, pReferred);
         }
         finally
         {
            state.existState();
            state2.existState();
         }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pCreatedRelationship;
	}


	/**
	 * Returns all the IPresentationElements that are referred to by the presentation element pReferencing.
	 *
	 * @param pReferencing [in] The referencing PE
	 * @param pReferredElements [out,retval] All the referred elements
	 */
	public static ETList<IPresentationElement> getAllReferredElements(IPresentationElement pReferencing)
	{
		ETList<IPresentationElement> retObj = new ETArrayList<IPresentationElement>();
		if (pReferencing != null)
		{
			ETList<IElement> pElements = pReferencing.getElements();
			if (pElements != null)
			{
				int count = pElements.size();

				// Gather up all the IPresentationReferences and get the ReferredElement which
				// is the presentation element for an IPresentationReference
				for (int i=0; i<count; i++)
				{
					IElement elem = pElements.get(i);
					if (elem instanceof IPresentationReference)
					{
						IPresentationElement pEle = ((IPresentationReference)elem).getPresentationElement();
						if (pEle != null)
						{
							retObj.add(pEle);
						}
					}
				}
			}
		}
		
		return retObj;
	}

	/**
	 * Returns true if there are referred elements.  If not then pReferredElements is NULL.
	 *
	 * @param pReferencing [in] The referencing PE
	 * @param pReferredElements [out,retval] All the referred elements
	 * @return true if there are referred elements.
	 */
	public static ETList<IPresentationElement> getHasReferredElements(IPresentationElement pReferencing)
	{
		ETList<IPresentationElement> retObj = new ETArrayList<IPresentationElement>();
		if (pReferencing != null)
		{
			ETList<IElement> pElements = pReferencing.getElements();
			if (pElements != null)
			{
				int count = pElements.size();
				for (int i=0; i<count; i++)
				{
					IElement elem = pElements.get(i);
					if (elem instanceof IPresentationReference)
					{
						IPresentationElement pEle = ((IPresentationReference)elem).getPresentationElement();
						if (pEle != null)
						{
							retObj.add(pEle);
						}
					}
				}
			}
		}
		return retObj;
	}

	/**
	 * Returns all the IPresentationElements that are referred to by the presentation element pReferencing.
	 *
	 * @param pReferencing [in] The referencing PE
	 * @param pElements [out,retval] All the referrenced presentation elements' subjects
	 */
	public static ETList < IElement > getAllReferredSubjects(IPresentationElement pReferencing) {
		if (pReferencing == null)
			return null;

		try {
			ETList < IElement > pFoundElements = new ETArrayList < IElement > ();

			// Get all the elements off the referencing element
			ETList < IElement > pReferredElements = pReferencing.getElements();

			if (pFoundElements != null && pReferredElements != null) {
				// Gather up all the IPresentationReferences and get the ReferredElement which
				// is the presentation element for an IPresentationReference
				Iterator < IElement > iter = pReferredElements.iterator();
				while (iter.hasNext()) {
					IElement pThisElement = iter.next();
					// Get all the relationships
					IPresentationReference pThisReference = pThisElement instanceof IPresentationReference ? (IPresentationReference) pThisElement : null;

					if (pThisReference != null) {
						IPresentationElement pPE = pThisReference.getPresentationElement();
						if (pPE != null) {
							IElement pThisPEsElement = pPE.getFirstSubject();
							if (pThisPEsElement != null)
								pFoundElements.add(pThisPEsElement);
						}
					}
				}
				return pFoundElements.size() > 0 ? pFoundElements : null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns all the IPresentationElements that are referring pReferences (ie the parent or owner objects of the relationship)
	 */
	public static ETList < IPresentationElement > getAllReferencingElements(IPresentationElement pReferencedElement) {
		if (pReferencedElement == null)
			return null;

		try {
			ETList < IPresentationElement > pFoundPEs = new ETArrayList < IPresentationElement > ();
			if (pFoundPEs != null) {
				ETList < IReference > pReferences = pReferencedElement.getReferredReferences();
				IteratorT < IReference > iter = new IteratorT < IReference > (pReferences);
				while (iter.hasNext()) {
					IReference pReference = iter.next();
					if (pReference != null) {
						IElement pTempElement = pReference.getReferencingElement();
						IPresentationElement pTempPE = pTempElement instanceof IPresentationElement ? (IPresentationElement) pTempElement : null;
						if (pTempPE != null) {
							pFoundPEs.add(pTempPE);
						}
					}
				}
				return pFoundPEs.size() > 0 ? pFoundPEs : null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Removes all presentation references
	 *
	 * @param pReferred [in] The child whose presentation references should be removed
	 */
	public static void removeAllPresentationReferences(IPresentationElement pReferred)
	{
		if (pReferred != null)
		{
			ETList<IReference> pReferences = pReferred.getReferredReferences();
			ETList<IPresentationReference> allReferences = null;
			if (pReferences != null)
			{
				int count = pReferences.size();
				// Gather up all the IPresentationReferences and whack em all
				for (int i=0; i<count; i++)
				{
					IReference ref = pReferences.get(i);
					if (ref instanceof IPresentationReference)
					{
						if (allReferences == null)
						{
							allReferences = new ETArrayList<IPresentationReference>();
						}
						allReferences.add((IPresentationReference)ref);
					}
				}
			}
			
			// Now do the deletes
			if (allReferences != null)
			{
				int count = allReferences.size();
				for (int i=0; i<count; i++)
				{
					IPresentationReference ref = allReferences.get(i);
					ref.delete();
				}
			}
		}
	}

}



