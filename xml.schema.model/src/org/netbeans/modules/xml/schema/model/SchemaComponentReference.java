/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
		if (other==null || !(other instanceof SchemaComponentReference))
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
		return get().hashCode()*31;
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
