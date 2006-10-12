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

package org.netbeans.modules.xml.schema.model;

import java.util.*;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SchemaComponentReference<T extends SchemaComponent> extends Object
{
    /**
     *
     *
     */
    private SchemaComponentReference(T component)
    {
        super();

		if (component==null)
		{
			throw new IllegalArgumentException(
				"Parameter \"component\" cannot be null");
		}

		this.component=component;
    }


	/**
	 *
	 *
	 */
	public boolean equals(Object other)
	{
		if (!(other instanceof SchemaComponentReference))
			return false;

		return this.get()==((SchemaComponentReference)other).get();
	}


	/**
	 *
	 *
	 */
	public int hashCode()
	{
		// TODO: What's a reasonable hash algorithm here to avoid collision
		// with the hashCode of the referenced object?  We want the reference's
		// hashcode to be based on the referent, but different.
		return get().hashCode();
	}


	/**
	 *
	 *
	 */
	public String toString()
	{
		return getClass().getName()+"<"+get().getClass().getName()+">";
	}

	
	/**
	 *
	 *
	 */
	public T get()
	{
		return component;
	}


	/**
	 *
	 *
	 */
	public static <C extends SchemaComponent> 
		SchemaComponentReference<C> create(C component)
	{
		SchemaComponentReference<C> reference=
			new SchemaComponentReference<C>(component);

		return reference;
	}




	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////

	private T component;
}
