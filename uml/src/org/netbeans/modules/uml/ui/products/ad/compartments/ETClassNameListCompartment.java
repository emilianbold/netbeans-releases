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


import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;


public class ETClassNameListCompartment extends ETNameListCompartment implements IADClassNameListCompartment
{

	public ETClassNameListCompartment()
	{
		super();
		this.init();
	}

	public ETClassNameListCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
		this.init();
	}

	private void init(){
		this.initResources();
	}

	public void initResources()
	{
		super.initResources();
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADClassNameListCompartment";
	}
}
