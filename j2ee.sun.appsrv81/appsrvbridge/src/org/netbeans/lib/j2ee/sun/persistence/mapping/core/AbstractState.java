/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


/*
 * AbstractState.java
 *
 * Created on July 7, 2000, 11:00 AM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.*;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;

/**
 * The purpose of this class is to provide a superclass for all of the
 * state objects.  It provides some general purpose methods and state 
 * information.
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
abstract public class AbstractState implements Cloneable
{
	private Model _model;
	private MappingClassElement _mappingElement;
	private PersistenceClassElement _persistenceElement;

	/** Creates new AbstractState */
	public AbstractState (Model model) 
	{
		this(model, null);
	}

	/** Creates new AbstractState */
	public AbstractState (Model model, MappingClassElement mappingClass) 
	{
		_model = model;
		_mappingElement = mappingClass;
	}

	// cloning rules: do super.clone and accept the defaults for everything
	// except collections, other state objects, and collections of other
	// state objects.  For other state objects which are not backpointers, 
	// clone them.  For collections of other state objects which are not 
	// backpointers, create a new collection and clone the elements.
	// For collections of other objects, clone the collection.
	public Object clone ()
	{
		try
		{
			return (AbstractState)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError(e.toString());
		}
	}

	// uses the original keys, clones the elements (assuming they're state 
	// objects)
	protected Map getClonedMap (Map originalMap)
	{
		Iterator iterator = originalMap.keySet().iterator();
		HashMap newMap = new HashMap();

		while (iterator.hasNext())
		{
			Object nextKey = iterator.next();

			newMap.put(nextKey, 
				((AbstractState)originalMap.get(nextKey)).clone());
		}

		return newMap;
	}

	abstract public String getDebugInfo ();

	protected Model getModel () { return _model; }

	public MappingClassElement getMappingClassElement ()
	{
		return _mappingElement;
	}

	protected void setMappingClassElement (MappingClassElement mappingElement)
	{
		_mappingElement = mappingElement;
	}

	public PersistenceClassElement getPersistenceClassElement ()
	{
		if ((_persistenceElement == null) && (_mappingElement != null))
		{
			_persistenceElement = getModel().getPersistenceClass(
				_mappingElement.getName());
		}

		return _persistenceElement;
	}
}
