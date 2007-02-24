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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ISimpleListCompartment extends ICompartment
{
	/**
	 * Sets up all contained compartments prior to an Attach() operation.
	*/
	public void preAttach();

	/**
	 * Attaches a list of model elements, creating the default compartment type for each one.
	*/
	public void attachElements( ETList<IElement> pElements, boolean bReplaceAll, boolean bCompartmentCreated );

	/**
	 * Removes orphaned compartments after an Attach() operation has connected all valid compartments.
	*/
	public void postAttach();

	/**
	 * Add a compartment to this list compartment at the specified position, if blank or -1 adds to the end of the list.
	*/
	public long addCompartment( ICompartment pCompartment, int nPos, boolean bRedrawNow );

	/**
	 * Create and add a compartment to this list compartment at the specified position, if blank or -1 adds to the end of the list.
	*/
	public ICompartment createAndAddCompartment( String sCompartmentID, int nPos, boolean bRedrawNow );

	/**
	 * Moves a compartment within this list, if blank or -1 puts to the end of the list. (The compartment must exist in the list already.)
	*/
	public long moveCompartment( ICompartment pCompartment, int nPos, boolean bRedrawNow );

	/**
	 * Remove this compartment to this list, optionally deletes its model element.
	*/
	public void removeCompartment( ICompartment pCompartment, boolean bDeleteElement );

	/**
	 * Remove this compartment to this list, optionally deletes its model element.
	*/
	public void removeCompartmentAt( int lIndex, boolean bDeleteElement );

	/**
	 * Returns the number of compartment elements held by this list compartment.
	*/
	public int getNumCompartments();

	/**
	 * Retrieves a list of all compartments contained by this list compartment.
	*/
	public ETList < ICompartment > getCompartments();

   /// Returns the compartment at the indicated index.
   public ICompartment getCompartment( int nIndex );

	/**
	 * Returns a compartment by model element id.
	*/
	public ICompartment getCompartmentByElementXMIID( String pElementXMIID );

	/**
	 * Retrieves the compartment under a point.  Point must be in client coordinates.
	*/
	public ICompartment getCompartmentAtPoint( IETPoint ptPos); //, ICompartment pCompartment, int pIndex );

	/**
	 * Returns the previous visible compartment element in the list.
	*/
	public ICompartment getPreviousCompartment( ICompartment pStartingCompartment );

	/**
	 * Returns the next visible compartment element in the list.
	*/
	public ICompartment getNextCompartment( ICompartment pStartingCompartment );

	/**
	 * Looks for a compartment containing the element.
	*/
	public ICompartment findCompartmentContainingElement( IElement pElement );

	/**
	 * Finds the compartment with the given compartment ID.
	*/
	public ICompartment findCompartmentByCompartmentID( String sCompartmentID );

	/**
	 * Is the compartment contained in this list?
	*/
	public boolean findCompartment( ICompartment pCompartment );

	/**
	 * Is this list compartment valid (connected and in-synch with its model element)
	*/
	public boolean validate2(ETList<IElement> pElements );
	
	public int getCompartmentIndex(ICompartment pCompartment);

}
