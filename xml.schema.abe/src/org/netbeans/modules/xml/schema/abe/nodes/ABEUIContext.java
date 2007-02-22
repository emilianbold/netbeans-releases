/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.abe.nodes;

import org.netbeans.modules.xml.axi.AXIModel;
import org.openide.util.Lookup;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ABEUIContext extends Object
{
    /**
     *
     *
     */
    public ABEUIContext(AXIModel schemaModel,
		ABENodeFactory nodeFactory, Lookup lookup)
    {
        super();
		this.schemaModel=schemaModel;
		this.nodeFactory=nodeFactory;
		this.lookup=lookup;
    }


	/**
	 *
	 *
	 */
	public AXIModel getModel()
	{
		return schemaModel;
	}


	/**
	 *
	 *
	 */
	public ABENodeFactory getFactory()
	{
		return nodeFactory;
	}
        

	/**
	 *
	 *
	 */
	public Lookup getLookup()
	{
		return lookup;
	}




	////////////////////////////////////////////////////////////////////////////
	// Instance variables
	////////////////////////////////////////////////////////////////////////////

	private AXIModel schemaModel;
	private ABENodeFactory nodeFactory;
	private Lookup lookup;
}
