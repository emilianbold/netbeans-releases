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
 * FieldState.java
 *
 * Created on July 12, 2000, 3:55 PM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.NameUtil;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;

/**
 * This state object manages the mapping for a particular field or 
 * relationship.  Relationship fields are handled by a subclass of this class:
 * RelationshipState.  This state object is "owned" by its enclosing
 * FieldHolderState.
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public class FieldState extends AbstractState implements Cloneable
{
	private List _columns;	// of String, each of the form table.column
	private PersistenceFieldElement _field;
	private FieldHolderState _holderState;	// backpointer to enclosing holder
	//private Integer _fetchGroup;

	public FieldState (FieldHolderState declaringHolder, 
		PersistenceFieldElement field) 
	{
		this(declaringHolder, field, (List)null);
	}

	public FieldState (FieldHolderState declaringHolder, 
		PersistenceFieldElement field, ColumnElement column) 
	{
		this(declaringHolder, field, 
			((column != null) ? Collections.singletonList(column) : null));
	}

	// columns must contain Strings with relative member names or
	// DBMemberElements
	public FieldState (FieldHolderState declaringHolder, 
		PersistenceFieldElement field, List columns)
	{
		super(declaringHolder.getModel());

		_holderState = declaringHolder;

		if (_holderState != null)
			setMappingClassElement(_holderState.getMappingClassElement());

		_field = field;
		_columns = new ArrayList();

		if (columns != null)
			setMapping(columns);
	}

	// cloning rules: do super.clone and accept the defaults for everything
	// except collections, other state objects, and collections of other
	// state objects.  For other state objects which are not backpointers, 
	// clone them.  For collections of other state objects which are not 
	// backpointers, create a new collection and clone the elements.
	// For collections of other objects, clone the collection.
	public Object clone ()
	{
		FieldState clonedState = (FieldState)super.clone();

		clonedState._columns = new ArrayList(_columns);

		return clonedState;
	}

	public String getDebugInfo()
	{
		StringBuffer lDump = new StringBuffer();

		lDump.append("\tField Mapping for field " + _field.getName() + "\n");
		lDump.append("\tMapped to Columns\n");
		lDump.append(getColumnDebugInfo());
		//lDump.append("\tFetch Group " + getFetchGroup() + "\n");

		return lDump.toString();
	}

	protected String getColumnDebugInfo ()
	{
		StringBuffer lDump = new StringBuffer();

		if ((_columns != null) && (_columns.size() > 0))
		{
			Iterator iterator = _columns.iterator();

			while (iterator.hasNext())
				lDump.append("\t\tColumn name : " + iterator.next() + "\n");
		}
		else
			lDump.append("\t\tNone\n");

		return lDump.toString();
	}

	protected PersistenceFieldElement getField () { return _field; }

	// returns a list of relative column names
	protected List getColumns () { return _columns; }

    //================== holder state methods ==========================

    protected FieldHolderState getFieldHolderState () { return _holderState; }
    protected void setFieldHolderState (FieldHolderState state)
    {
        _holderState = state;
    }

    //=================== column/member mapping methods =======================

    // columns must contain Strings with relative member names or
    // DBMemberElements
    protected void setMapping (List columns)
    {
        int i, count = ((columns != null) ? columns.size() : 0);

        _columns.clear();
        for (i = 0; i < count; i++)
        {
            Object member = columns.get(i);
            String memberName = null;
			
            if (member instanceof DBMemberElement)
            {
                memberName = ((member != null) ? NameUtil.getRelativeMemberName(
                    ((DBMemberElement)member).getName().getFullName()) : null);
            }
            else if (member instanceof String)
                memberName = (String)member;

            if (memberName != null)
                _columns.add(memberName);
        }
    }

	protected void removeMapping (String columnName)
	{
		_columns.remove(columnName);
	}

	protected boolean isMappedToTable (String tablePrefix)
	{
		Iterator iterator = getLocalColumns().iterator();

		while (iterator.hasNext())
		{
			String nextColumn = (String)iterator.next();

			if (NameUtil.getTableName(nextColumn).equals(tablePrefix))
				return true;
		}

		return false;
	}

	// returns a list of relative column names
	protected List getLocalColumns () { return _columns; }

	public boolean hasInvalidMapping ()
	{
		Iterator iterator = getColumns().iterator();
		TableState tableState = getFieldHolderState().getTableState();

		while (iterator.hasNext())
		{
			Object next = iterator.next();

			if (next instanceof String)
			{
				String columnName = (String)next;
				ColumnElement column = tableState.getColumn(columnName);

				if (column == null)
					return true;
			}
		}

		return false;
	}

	/*public Integer getFetchGroup ()
	{
		if (_fetchGroup == null)
		{
			MappingClassElement mappingClass = getMappingClassElement();
			PersistenceFieldElement fieldElement = getField();
			boolean isKey = fieldElement.isKey();
			MappingFieldElement mappingField =
				mappingClass.getField(fieldElement.getName());
			boolean isMapped = (mappingField != null);

			if (isMapped && !isKey)
				_fetchGroup = new Integer(mappingField.getFetchGroup());
		}

		return _fetchGroup;
	}

	public void setFetchGroup (int fetchGroup)
	{
		_fetchGroup = new Integer(fetchGroup);
	}*/
}
