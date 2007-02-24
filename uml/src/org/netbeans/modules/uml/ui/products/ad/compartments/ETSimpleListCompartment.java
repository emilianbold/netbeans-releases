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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.accessibility.AccessibleSelectionParent;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ResourceUser;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author sumitabhk
 *
 */
public class ETSimpleListCompartment extends ETCompartment implements ISimpleListCompartment
{
	protected ETList<ICompartment> m_Compartments = null;
	protected boolean m_Dirty = true;

	/**
	 * 
	 */
	public ETSimpleListCompartment()
	{
		super();
		init();
	}

	public ETSimpleListCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
		init();
	}
	
	private void init() {
		m_Compartments = new ETArrayList < ICompartment >();
	}
	
	/**
	 * Pre-attach operation, sets all compartments' drawengines to NULL.  Attaching/resynching a compartment
	 * with a modelelement will set the drawengine back again.  After attaching call PostAttach() to remove
	 * the compartments whose drawengines are still NULL (they are orphaned).
	 *
	 * @return HRESULT
	 */
	public void preAttach()
	{
		// Clear each contained compartment's draw engine.  During PostAttach() we
		// whack the ones that are orphaned
		ETList<ICompartment> compartments = getCompartments();
		int count = compartments != null ? compartments.size() : 0;
		
		for (int i=0; i<count; i++)
		{
			ICompartment pComp = compartments.get(i);

			// clear the engine so we'll know which compartments are orphaned
			pComp.setEngine(null);
		}
	}

	/**
	 * Attaches a list of Elements to this Element list compartment
	 *
	 * @param pElements [in] The list of elements this list compartment should attach to.  It creates sub
	 * compartments as necessary.
	 * @param bReplaceAll [in] TRUE to replace all compartments with the elements in the list, FALSE to update existing
	 * compartments and insert new ones if necessary.
	 * @param bCompartmentCreated [out] Was a new compartment created?
	 */
	public void attachElements(ETList<IElement> pElements, boolean bReplaceAll, boolean bCompartmentCreated)
	{
		if (pElements != null)
		{
			// Go over each operation and try to find a compartment that matches that
			// xmiid.  If found then attach to that compartment, if not found then create
			// a new compartment.
			if (bReplaceAll)
			{
				preAttach();
			}
			
			// To speed things up we first build a map of existing compartments
			HashMap map = new HashMap();
			ETList<ICompartment> compartments = getCompartments();
			int count = compartments != null ? compartments.size() : 0;
			for (int i=0; i<count; i++)
			{
				ICompartment comp = compartments.get(i);
				String xmiid = comp.getModelElementXMIID();
				if (xmiid != null && xmiid.length() > 0)
				{
					map.put(xmiid, comp);
				}
			}
			
			// go through each element, attaching it to the appropriate compartment
			// look for an exising compartment in the map, if found attach, otherwise create new
			int num = pElements.size();
			for (int j=0; j<num; j++)
			{
				IElement elem = pElements.get(j);
				String xmiid = elem.getXMIID();
				
				// Find the compartment in the list by this id
				Object obj = map.get(xmiid);
				if (obj != null)
				{
					// found, reattach
					ICompartment pComp = (ICompartment)obj;
					pComp.setEngine(m_engine);
					pComp.reattach(null);
				}
				else
				{
					// Create a new compartment
					addModelElement(elem, -1);
				}
			}
			
			if (bReplaceAll)
			{
				postAttach();
			}
		}
	}

	/**
	 * Post attach operation, removes orphaned compartments discovered during attaching.
	 *
	 * @return HRESULT
	 */
	public void postAttach()
	{
		// Clear each contained compartment's draw engine.  During PostAttach() we
		// whack the ones that are orphaned
		// Because we're deleting we need to build a list of compartments to iterate through
		ETList<ICompartment> compartments = getCompartments();
		int count = compartments != null ? compartments.size() : 0;

		for (int i = count - 1; i >= 0; i--)
		{
			ICompartment pComp = compartments.get(i);
			IDrawEngine pEng = pComp.getEngine();
			if (pEng == null)
			{
				// remove orphaned compartments
				removeCompartment(pComp, false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment#addCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, int, boolean)
	 */
	public long addCompartment(ICompartment pCompartment, int nPos, boolean bRedrawNow)
	{
		if (pCompartment != null)
		{
			pCompartment.setVisible(true);
			pCompartment.setEngine(m_engine);
			//pCompartment.addModelElement(m_modelElement, -1);
			
			if (nPos < 0 || nPos > getCompartments().size()) 
			{
				getCompartments().add(pCompartment);
			} else 
			{
				getCompartments().add(nPos, pCompartment);
			}
			
			m_Dirty = true;
			if (bRedrawNow && m_engine != null)
			{
				m_engine.invalidate();
				/*
 				This causes a bunch of bugs with sizeToContents, when interactivily adding objects. 
 				
				IDrawingAreaControl drawingArea = m_engine.getDrawingArea();
				// Send the paint to the new compartment so its sizes are set.
				if (drawingArea != null)
				{
					drawingArea.refresh(true);
				}
				*/
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment#createAndAddCompartment(java.lang.String, int, boolean)
	 */
	public ICompartment createAndAddCompartment(String sCompartmentID, int nPos, boolean bRedrawNow)
	{
		try
		 {			
			// Create the compartment
			ICompartment  pCompartment = CreationFactoryHelper.createCompartment(sCompartmentID);
			if (pCompartment != null)
			{
			   // Make sure we call our own AddCompartment()!
			   addCompartment(pCompartment, nPos, bRedrawNow);
			   pCompartment.initResources();
			}
			return pCompartment;
		 }
		 catch (Exception e)
		 {
			e.printStackTrace();
		 }
		 return null;
	}

	/**
	 * Moves a compartment within this list, if blank or -1 puts to the end of the list. (The compartment must exist in the list already.)
	*/
	public long moveCompartment(ICompartment pCompartment, int nPos, boolean bRedrawNow)
	{
		if (pCompartment != null) {
			int idx = this.getCompartmentIndex(pCompartment);
			if (idx >= 0) {
				getCompartments().remove(idx);
				
				int listSize = getCompartments().size();
				if (nPos <0 || nPos > listSize){
					nPos = listSize;
				}
				getCompartments().add(nPos, pCompartment);
				m_Dirty = true;

				if (bRedrawNow) {
					this.getEngine().invalidate();
				}
			}
		}
		return 0;
	}

	/**
	 * Remove this compartment in this list, optionally deletes its model element.
	*/
	public void removeCompartment(ICompartment pCompartment, boolean bDeleteElement) {

		if (pCompartment != null) {
			// remove a single compartment
			int nIndex = getCompartmentIndex(pCompartment);

			// whack the element
			if (bDeleteElement) {
				IElement pElement = pCompartment.getModelElement();

				if (pElement != null) {
					// whacking the element will fire an Element Deleted notification which will
					// remove the compartment
					pElement.delete();
				}
			} else if (nIndex >= 0) {
				// we're not whacking the element, just remove the compartment
				getCompartments().remove(nIndex);
				m_Dirty = true;
			}
		} else {
			// remove all compartments
			int numCompartments = getNumCompartments();

			while (numCompartments > 0) {
				ICompartment tmpCompartment = getCompartment(0);

				if (tmpCompartment != null) {
					removeCompartment(tmpCompartment, bDeleteElement);
					//					 if( hr == EFR_S_EVENT_CANCELLED )
					//						break;
					numCompartments = getNumCompartments();
				}
			}
		}

	}

	/**
	 * Remove this compartment in this list, optionally deletes its model element.
	*/
	public void removeCompartmentAt(int pIndex, boolean bDeleteElement) {

		if (bDeleteElement) {
			ICompartment cpCompartment = getCompartment(pIndex);

			if (cpCompartment != null) {
				IElement cpElement = cpCompartment.getModelElement();
				if (cpElement != null) {
					// whacking the element will fire an Element Deleted notification which will
					// remove the compartment
					cpElement.delete();
				}
			}
		} else {
			// we're not whacking the element, just remove the compartment
			getCompartments().remove(pIndex);
			m_Dirty = true;
		}
	}

	public int getNumCompartments() {
		return getCompartments().size();
	}

	/**
	 * Retrieves a list of all compartments contained by this list compartment.
	*/
	public ETList < ICompartment > getCompartments() {
		return m_Compartments;
	}

        protected boolean hasSelectedCompartments()
        {
            boolean retVal = false;
            
            for(ICompartment curComp : getCompartments())
            {
                if(curComp.isSelected() == true)
                {
                    retVal = true;
                    break;
                }
            }
            
            return retVal;
        }
        
	/**
	 * Returns a compartment by model element id.
	*/
	public ICompartment getCompartmentByElementXMIID(String pElementXMIID) 
	{
		Iterator < ICompartment > iterator = this.getCompartments().iterator();
		while (iterator.hasNext()) {
			ICompartment currentCompartment = iterator.next();
			if (currentCompartment.getModelElementXMIID().equals(pElementXMIID)) {
				return currentCompartment;
			}
		}
		return null;
	}

	/**
	 * Retrieves the compartment under a point.  Point must be in client coordinates.
	*/
	public ICompartment getCompartmentAtPoint(IETPoint pCurrentPos) 
	{
		Iterator < ICompartment > iterator = getCompartments().iterator();
		while (iterator.hasNext()) {
			ICompartment currentCompartment = iterator.next();
			if (currentCompartment.isPointInCompartment(pCurrentPos)) {
				return currentCompartment;
			}
		}
		return null;
	}

	/**
	 * Returns the previous visible compartment element in the list.
	*/
	public ICompartment getPreviousCompartment(ICompartment pStartingCompartment) 
	{
		ICompartment retValue = null;
		ICompartment prevCompartment = null;
		Iterator < ICompartment > iterator = getCompartments().iterator();
		while (iterator.hasNext()) {
			ICompartment currentCompartment = (ICompartment) iterator.next();
			if (currentCompartment.getModelElementXMIID().equals(pStartingCompartment.getModelElementXMIID())) {
                            if (prevCompartment != null) {
				retValue = prevCompartment;
				break;
                            }
                            else {
                                retValue = currentCompartment;
                                break;
                            }                            
			}
			prevCompartment = currentCompartment;
		}
		return retValue;
	}

	/**
	 * Returns the next visible compartment element in the list.
	*/
	public ICompartment getNextCompartment(ICompartment pStartingCompartment) 
	{
		ICompartment pCompartment = null;
		try
		{
			Iterator < ICompartment > iterator = getCompartments().iterator();
			while (iterator.hasNext()) {
				ICompartment currentCompartment = iterator.next();
				if (currentCompartment.getModelElementXMIID().equals(pStartingCompartment.getModelElementXMIID())) {
                                    if (iterator.hasNext()) {
					pCompartment = iterator.next();
                                    }
                                    else {
                                        pCompartment = currentCompartment;
                                    }
				}
			}
		}
		catch (Exception e)
		{
			pCompartment = null;
		}
		return pCompartment;
	}

	public ICompartment findCompartmentContainingElement(IElement pElement) 
	{
		if (pElement != null)
		{
			Iterator < ICompartment > iterator = getCompartments().iterator();
			while (iterator.hasNext()) {
				ICompartment currentCompartment = iterator.next();
				if (currentCompartment.getModelElementXMIID().equals(pElement.getXMIID())) {
					return currentCompartment;
				}
			}
		}
		return null;
	}

	public ICompartment findCompartmentByCompartmentID(String sCompartmentID) 
	{
		if (sCompartmentID != null)
		{
			Iterator < ICompartment > iterator = getCompartments().iterator();
			while (iterator.hasNext()) {
				ICompartment currentCompartment = iterator.next();
				if (currentCompartment.getCompartmentID().equals(sCompartmentID)) {
					return currentCompartment;
				}
			}
		}
		return null;
	}

	public boolean findCompartment(ICompartment pCompartment) 
	{
		Iterator < ICompartment > iterator = getCompartments().iterator();
		while (iterator.hasNext()) {
			ICompartment currentCompartment = iterator.next();
			if (currentCompartment.getModelElementXMIID().equals(pCompartment.getModelElementXMIID())) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ISimpleListCompartment#validate2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[])
	 */
	public boolean validate2(ETList<IElement> pElements)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public int getCompartmentIndex(ICompartment compartment) 
	{
      ETList< ICompartment > compartments = getCompartments();
      return compartments != null ? compartments.indexOf( compartment ) : -1;
	}

	/**
	 *
	 * Fetches a specific compartment from this list.
	 *
	 * @param index[in] Zero-based offset of the compartment to get
	 * @param *pCompartment[in] The returned compartment
	 *
	 * @return HRESULT
	 *
	 */
	public ICompartment getCompartment(int index)
	{
		return index >= 0 && index < getNumCompartments() ? getCompartments().get(index) : null;
	}

	/**
	 * Update from archive.
	 *
	 * @param pProductArchive [in] The archive we're reading from
	 * @param pCompartmentElement [in] The element where this compartment's information should exist.
	 */
	public void readFromArchive(IProductArchive pProductArchive, 
								IProductArchiveElement pCompartmentElement)
	{
		if (pProductArchive != null && pCompartmentElement != null)
		{
			clearCompartments();

			super.readFromArchive(pProductArchive, pCompartmentElement);
			
			// call drawing factory to create compartments and add them to ourself
			IProductArchiveElement[] elems = pCompartmentElement.getElements();
			if (elems != null)
			{
				DrawingFactory.createCompartments(this, pProductArchive, elems);
			}
		}
	}

	/**
	 *
	 * Empties the list of compartments contained by this list compartments.
	 *
	 * @return HRESULT
	 *
	 */
	public void clearCompartments()
	{
		getCompartments().clear();
	}
	
	// Returns the index of the compartment that handled the right mouse button event
	public int getRightMouseButtonIndex()
	{
		//this method is called when we are inserting a new compartment in a list compartment
		//since the new compartment should be the last one inserted, we should return the last index.		
		ETList<ICompartment> pComps = getCompartments();
		return pComps != null ?  pComps.size() : 0;
	}
	
	/**
	 * Write ourselves to archive.
	 *
	 * @param pProductArchive [in] The archive we're saving to
	 * @param pElement [in] The current element, or parent for any new attributes or elements
	 * @param pCompartmentElement [out] The created element for this compartment's information
	 */
	public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, 
							   					IProductArchiveElement pEngineElement) 
	{
		IProductArchiveElement retEle = null;
		IProductArchiveElement pElement = super.writeToArchive(pProductArchive, pEngineElement);
		if (pElement != null)
		{
			// need to set a flag if we need to save anything at all
			int count = getNumCompartments();
			for (int i=0; i<count; i++)
			{
				ICompartment pCompartment = getCompartment(i);
				if (pCompartment != null)
				{
					pCompartment.writeToArchive(pProductArchive, pElement);
				}
			}
			retEle = pElement;
		}
		return retEle;
	}
	
	/**
	 * Notifier that the model element has changed.
	 *
	 * @param pTargets [in] Information about what has changed.
	 */
	public long modelElementHasChanged(INotificationTargets pTargets) 
	{
		int count = getNumCompartments();
		for (int i = 0;  i < count; i++)
		{
			ICompartment pComp = getCompartment(i);
			if (pComp != null)
			{
				pComp.modelElementHasChanged(pTargets);
			}
		}
		return 0;
	}

	/**
	 * Support for derived classes, if the model element exists in a compartment it is handed the change
	 * notification, otherwise a new compartment is created and the list compartment is grown
	 *
	 * @param pTargets [in] Information about what has changed.
	 */
	public void modelElementHasChanged2(INotificationTargets pTargets) 
	{
		if (pTargets != null)
		{
			IElement pChangedEle = pTargets.getSecondaryChangedModelElement();
			
			// find our compartment
			ICompartment pComp = findCompartmentContainingElement(pChangedEle);
			if (pComp != null)
			{
				// tell compartment to update
				pComp.modelElementHasChanged(pTargets);
			}
			else
			{
				// not found, must be a new one, grow the compartment
				addModelElement(pChangedEle, -1);
				
				// By here we should have a compartment.
				pComp = findCompartmentContainingElement(pChangedEle);
			}
			
			if (m_engine != null)
			{
				m_engine.invalidate();
			}
		}
	}

	/**
	 * Returns a compartment by the compartment name
	 *
	 * @param sName [in] The compartment name to look for
	 * @param pCompartment [out,retval] The compartment with a name of sName
	 */
	protected ICompartment getCompartmentByCompartmentName(String sName)
	{				
		Iterator<ICompartment> iter = getCompartments().iterator();
		while (iter.hasNext())
		{			
			ICompartment pComp = iter.next();
			String name = pComp.getCompartmentID();
			if (name != null && name.equals(sName))
			{
				return pComp;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementDeleted(INotificationTargets pTargets) {

		IElement pDeletedElement = pTargets.getSecondaryChangedModelElement();
		
		if (pDeletedElement == null) {
			pDeletedElement = pTargets.getChangedModelElement();
		}

		// find our compartment
		ICompartment pCompartment = findCompartmentContainingElement(pDeletedElement);

		if (pCompartment != null) {
			// tell compartment to update
			removeCompartment(pCompartment, false);

			// If it's a node then resize the node.
			if (this.getEngine() instanceof INodeDrawEngine) {

				INodeDrawEngine pNodeDrawEngine = (INodeDrawEngine) this.getEngine();
				if (pNodeDrawEngine != null) {
					pNodeDrawEngine.resizeToFitCompartment(this, true, false);
				}
			}

		}
		return 0;
	}

	public void resetToDefaultResource( String sDrawEngineName, 
									   String sResourceName,
									   String sResourceType)
	{
		// Reset all my subcompartments
		int numCompartments = getNumCompartments();
		for (int i = 0 ; i < numCompartments ; i++)
		{
			ICompartment pCompartment = getCompartment(i);
			IDrawingPropertyProvider pCompProvider = (IDrawingPropertyProvider)pCompartment;
			pCompProvider.resetToDefaultResource(sDrawEngineName, sResourceName, sResourceType);
		}
		   
		// Reset our resources
		super.resetToDefaultResource(sDrawEngineName, sResourceName, sResourceType);
	}
	
	public void resetToDefaultResources()
	{
		// Reset all my subcompartments
		int numCompartments = getNumCompartments();
		for (int i = 0 ; i < numCompartments ; i++)
		{
			ICompartment pCompartment = getCompartment(i);
			IDrawingPropertyProvider pCompProvider = (IDrawingPropertyProvider)pCompartment;
			pCompProvider.resetToDefaultResources();
		}
		   
		// Reset our resources
		super.resetToDefaultResources();
	}

   /*
    * Searches through all the compartments looking for the specified compartment 
    */
   public < Type > Type getCompartmentByKind( Class interfacetype )
   {
      try
      {
         IteratorT < ICompartment > iter = new IteratorT < ICompartment > ( m_Compartments );
         while (iter.hasNext())
         {
            ICompartment comp = iter.next();
            if (interfacetype.isAssignableFrom(comp.getClass()))
            {
               return (Type) comp;
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
      return null;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onGraphEvent(int)
    */
   public long onGraphEvent(int nKind)
   {
   	Iterator<ICompartment> iter = getCompartments().iterator();
		while (iter.hasNext())
      {
			iter.next().onGraphEvent(nKind);         
      }
      
      return 0;
   }



    /////////////
    // Accessible
    /////////////

    AccessibleContext accessibleContext;

    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETSimpleListCompartment();
	} 
	return accessibleContext;
    }


    public class AccessibleETSimpleListCompartment extends AccessibleETCompartment
	implements AccessibleSelection
    {
	
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.PANEL;
	}
	
	public int getAccessibleChildrenCount() {
	    return m_Compartments.size(); 
	}
	
	public Accessible getAccessibleChild(int i) {	
	    if (i < m_Compartments.size()) {
		ICompartment comp = m_Compartments.get(i);
		if (comp instanceof Accessible) {
		    ((Accessible)comp).getAccessibleContext().setAccessibleParent(ETSimpleListCompartment.this);
		    return (Accessible)comp;
		}
	       
	    } 
	    return null;	    
	}
	
	
	public AccessibleSelection getAccessibleSelection() {
	    int childnum = getAccessibleChildrenCount(); 
	    for(int i = 0; i < getAccessibleChildrenCount(); i++) {
		if (isSelectable(getAccessibleChild(i))) {
		    return this;
		}
	    }
	    return null;
	}
	
	
	////////////////////////////////
	// interface AccessibleComponent
	////////////////////////////////

	public javax.accessibility.Accessible getAccessibleAt(java.awt.Point point) {
	    return null;
	}


	////////////////////////////////
	// interface AccessibleSelection
	////////////////////////////////

	public int getAccessibleSelectionCount() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		return selected.size();
	    }
	    return 0;
	}
	
	public Accessible getAccessibleSelection(int i) {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null && i < selected.size()) {
		return selected.get(i);
	    }
	    return null;
	}
	
	public boolean isAccessibleChildSelected(int i) {
	    Accessible child = getAccessibleChild(i);	   
	    if (child != null) {
		return isSelected(child);	    
	    }
	    return false;
	}
	
	public void addAccessibleSelection(int i) {
	    Accessible child = getAccessibleChild(i);	    
	    if (child != null) {
		selectChild(child, true, false);
	    }
	}
	
	public void removeAccessibleSelection(int i) {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null && i < children.size()) {
		selectChild(children.get(i), false, false); 
	    }
	}

	public void selectAllAccessibleSelection() {
	    List<Accessible> children = getAccessibleChildren();
	    if (children != null) {
		for(int i = 0; i < children.size(); i++) {
		    selectChild(children.get(i), true, false); 
		}
	    }	    
	}
	
	public void clearAccessibleSelection() {
	    List<Accessible> selected = getSelectedAccessibleChildren();
	    if (selected != null) {
		for(int i = 0; i < selected.size(); i++) {
		    selectChild(selected.get(i), false, true); 
		}
	    }
	}
	

	/////////////////
	// Helper methods
	/////////////////

	public void selectChild(Accessible child, boolean select, boolean replaceSelection) {	    
	    
	    IDrawEngine engine = getEngine();
	    if (child instanceof ICompartment) {
		if (replaceSelection) {
		    if (engine != null) {
			engine.selectAllCompartments(false);
		    }	   
		} 
		((ICompartment)child).setSelected(select);
	    }
	    
	    if (engine != null && engine instanceof Accessible) { 
		Accessible engineParent = ((Accessible)engine).getAccessibleContext().getAccessibleParent();
		if (engineParent != null 
		    && engineParent.getAccessibleContext() instanceof AccessibleSelectionParent) 
		{
		    ((AccessibleSelectionParent)engineParent.getAccessibleContext())
			.selectChild((Accessible)engine, select, replaceSelection);	    
		}
	    }
	    
	}
	

	public boolean isSelectable(Accessible child) {
	    AccessibleStateSet stateSet = child.getAccessibleContext().getAccessibleStateSet();
	    if (stateSet != null && stateSet.contains(AccessibleState.SELECTABLE)) {
		return true;
	    }
	    return false;
	}
	

	public boolean isSelected(Accessible child) {
	    if (child instanceof ICompartment) {
		return ((ICompartment)child).isSelected();
	    }	
	    return false;
	}


	public List<Accessible> getAccessibleChildren() {
	    ArrayList<Accessible> children = new ArrayList<Accessible>();
	    for(int i = 0; i < m_Compartments.size(); i++) {		
		ICompartment comp = m_Compartments.get(i);
		if (comp instanceof Accessible) {
		    ((Accessible)comp).getAccessibleContext().setAccessibleParent(ETSimpleListCompartment.this);
		    children.add((Accessible)comp);
		}
	       
	    } 
	    return children;
	}


	public List<Accessible> getSelectedAccessibleChildren() {
	    ArrayList<Accessible> selected = new ArrayList<Accessible>();
	    List<Accessible> children = getAccessibleChildren();
	    for(int i = 0; i < children.size(); i++) {
		Accessible child = children.get(i);
		if (isSelected(child)) {
		    selected.add(child);		
		}
	    }
	    return selected;
	}	


    }

    //Jyothi: a11y work - select the first row in the selected compartment
    public void setSelected(boolean pValue)
    {
        // The default is to use the 1st editable compartment
        IADEditableCompartment editableCompartment = getCompartmentByKind( IADEditableCompartment.class );
        if( editableCompartment != null )
        {
           editableCompartment.setSelected( pValue );
        }
    }


}
