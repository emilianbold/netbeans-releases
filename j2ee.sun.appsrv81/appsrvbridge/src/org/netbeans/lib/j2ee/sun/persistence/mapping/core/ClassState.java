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
 * ClassState.java
 *
 * Created on June 5, 2000, 11:34 AM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.List;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;

/**
 * The following class holds all of the state information for a class.  It  
 * contains a table state object and a field holder state object.
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public class ClassState extends AbstractState implements Cloneable
{
	private TableState _tableState;
	private FieldHolderState _fieldHolderState;

	/** Creates new ClassState */
	public ClassState (Model model, MappingClassElement mappingClass) 
	{
		this(model, mappingClass, null);
	}

	/** Creates new ClassState */
	public ClassState (Model model, MappingClassElement mappingClass, 
		TableState tableState)
	{
		super(model, mappingClass);
		setTableState(tableState);
	}

	// cloning rules: do super.clone and accept the defaults for everything
	// except collections, other state objects, and collections of other
	// state objects.  For other state objects which are not backpointers, 
	// clone them.  For collections of other state objects which are not 
	// backpointers, create a new collection and clone the elements.
	// For collections of other objects, clone the collection.
	public Object clone ()
	{
		ClassState clonedState = (ClassState)super.clone();

		if (hasTableState())
			clonedState._tableState = (TableState)getTableState().clone();

		if (hasFieldHolderState())
		{
			clonedState._fieldHolderState = 
				(FieldHolderState)getFieldHolderState().clone();
		}

		return clonedState;
	}

	public String getDebugInfo()
	{
		StringBuffer lDump = new StringBuffer();

		lDump.append("Tables\n");  // NOI18N

		if (hasTableState())
			lDump.append(getTableState().getDebugInfo());

		lDump.append("Fields and relationships\n");  // NOI18N
		if (hasFieldHolderState())
			lDump.append(getFieldHolderState().getDebugInfo());

		return lDump.toString();
	}

	public boolean hasTableState () { return (_tableState != null); }

	public TableState getTableState ()
	{
		if (!hasTableState())
			_tableState = new TableState(this);

		return _tableState;
	}

	public void setTableState (TableState state)
	{
		if (_tableState != state)
		{
			// unmap whatever fields are necessary by this change
			// must do this before assigning it to _tableState so the 
			// difference can be calculated
			if (hasFieldHolderState())
				_fieldHolderState.updateFieldStates(state);

			_tableState = state;
		}

		if (_tableState != null)
		{
			_tableState.setClassState(this);

			// update whatever secondary table definitions are necessary 
			// by this change (may be temporary after ok is disabled 
			// when there are illegal pairs shown)
			_tableState.updateSecondaryTables();
		}
	}

	public boolean hasFieldHolderState ()
	{
		return (_fieldHolderState != null);
	}

	public FieldHolderState getFieldHolderState ()
	{
		if (!hasFieldHolderState())
			_fieldHolderState = new FieldHolderState(this);

		return _fieldHolderState;
	}

	public void setFieldHolderState (FieldHolderState state)
	{
		if (_fieldHolderState != state)
			_fieldHolderState = state;

		if (_fieldHolderState != null)
			_fieldHolderState.setClassState(this);
	}

	public void addDefaultMapping (List suffixes)
	{
		getTableState().addDefaultTableMapping(
			getMappingClassElement().getName(), suffixes);
		getFieldHolderState().addDefaultMapping();
	}
}
