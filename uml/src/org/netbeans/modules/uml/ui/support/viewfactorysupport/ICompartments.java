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


/*
 * Created on May 30, 2003
 *
 */
package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.util.*;

/**
 * @author Embarcadero Technologies Inc.
 *
 *
 */

public interface ICompartments
{
	// Contains the number of ICompartments in the collection
	public int getCount();

	// Adds a ICompartment to the collection
	public void add(ICompartment compartment);

	// Retrieves a specific ICompartment from the collection
	public ICompartment item(int index);

	// Remove a specific ICompartment from the collection
	public void remove(int index);

	// Insert an ICompartment at a specific position in the list
	public void insert(ICompartment compartment, int index);

	// Returns the last item in the list
	public ICompartment lastItem();

	// Finds a particular item in the list
	public int find(ICompartment compartment);

	// Removes this compartment
	public boolean removeThisOne( ICompartment tag );

	// Removes the argument compartments
	public long removeThese( ICompartments tag );

	//NL Helper method
	public Iterator getIterator();
	
	//NL Helper method
	public void clearCompartments();

}
