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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

/**
 * @author sumitabhk
 *
 */
public class ValueSpecification extends Element implements IValueSpecification{

	/**
	 *
	 */
	public ValueSpecification() {
		super();
	}

	/**
	 *
	 * If set, implies that the associated value specification represents
	 * a set of instances as described by the value specification.
	 *
	 * @param mult[out]
	 *
	 * @return S_OK
	 */
	public IMultiplicity getMultiplicity()
   {
      ElementCollector< IMultiplicity > col = new ElementCollector< IMultiplicity >();
		IMultiplicity mult = col.retrieveSingleElement(m_Node, "UML:ValueSpecification.representation/*", IMultiplicity.class);
		if (mult == null)
		{
			// We currently don't have a Multiplicity, so let's create a new Multiplicity and return it.
			TypedFactoryRetriever < IMultiplicity > ret = new TypedFactoryRetriever < IMultiplicity >();
			mult = ret.createType("Multiplicity");
			
			// Add the child directly, so we don't cause any new events
			addMultiplicity(mult);
		}
		return mult;
	}

	public void setMultiplicity(IMultiplicity mult) {
		addMultiplicity(mult);
	}

	/**
	 *
	 * Simply adds the passed in Multiplicity to this element without firing events.
	 *
	 * @param exp[in] The Multiplicity to add
	 *
	 * @return HRESULT
	 *
	 */
	private void addMultiplicity(IMultiplicity mult) {
		addChild("UML:ValueSpecification.representation", "UML:ValueSpecification.representation", mult);
	}

}



