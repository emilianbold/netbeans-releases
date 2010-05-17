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
 * FieldHolderState.java
 *
 * Created on June 4, 2001, 5:55 PM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.NameUtil;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.jdo.*;
import com.sun.jdo.api.persistence.model.mapping.*;

/**
 * This state object manages all the mappings (and related information) for 
 * fields and relationships in a particular class.  The fields and 
 * relationships are handled by FieldState and RelationshipState objects 
 * respectively.  This state object is "owned" by its enclosing ClassState
 * which manages the TableState object used to hold and lookup relevant table 
 * information for this class.  The related information managed by this class 
 * is a map of extra ClassState objects used to hold and lookup information   
 * about classes other than this class: TableState objects which are relevant 
 * because this class contains a relationship with that class, and
 * FieldHolderState objects which are relevant for related field information. 
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public class FieldHolderState extends AbstractState implements Cloneable
{
    // constants to support automapping
    private static final int NATURAL = 0;
    private static final int REMOVE_DELIM = 1;
    private static final int NATURAL_COMPOUND = 2;
    private static final int REMOVE_DELIM_COMPOUND = 3;

    private ClassState _classState;	// backpointer to enclosing class state

    // mapping for individual fields (and relationship fields)
    // key: PersistenceFieldElement, value: FieldState
    private Map _fieldStates;

    // fetchgroup for individual fields (and relationship fields)
    // key: PersistenceFieldElement, value: Integer
    private Map _fetchGroups;

    // for other class' primary table & related fields info
    // key: String (fully qualified class name), value: ClassState
    private Map _extraClassStates;

    /** Creates new FieldHolderState */
    public FieldHolderState (ClassState declaringClass) 
    {
        super(declaringClass.getModel(), 
		declaringClass.getMappingClassElement());

        // this will set _classState backpointer
        declaringClass.setFieldHolderState(this);
        _fieldStates = new HashMap();
        _fetchGroups = new HashMap();
        _extraClassStates = new HashMap();
        updateFieldStates(declaringClass.getTableState());
    }
    
    public FieldHolderState (ClassState declaringClass, 
        PersistenceFieldElement field) 
    {
        this(declaringClass);
        addFieldMapping(field);
    }

    // cloning rules: do super.clone and accept the defaults for everything
    // except collections, other state objects, and collections of other
    // state objects.  For other state objects which are not backpointers, 
    // clone them.  For collections of other state objects which are not 
    // backpointers, create a new collection and clone the elements.
    // For collections of other objects, clone the collection.
    public Object clone ()
    {
        FieldHolderState clonedState = (FieldHolderState)super.clone();

        clonedState._fieldStates = getClonedMap(_fieldStates);
        clonedState._fetchGroups = new HashMap(_fetchGroups);
        clonedState._extraClassStates = getClonedMap(_extraClassStates);

        return clonedState;
    }

    public String getDebugInfo ()
    {
        StringBuffer lDump = new StringBuffer();
		Iterator iterator = getFieldStates().iterator();
        
        lDump.append("Modified Fields are \n");
        
        while (iterator.hasNext())
            lDump.append(((FieldState)iterator.next()).getDebugInfo());

        lDump.append("Modified Fetch groups are \n");
        iterator = _fetchGroups.keySet().iterator();
        while (iterator.hasNext())
        {
            PersistenceFieldElement field =
                (PersistenceFieldElement)iterator.next();

            lDump.append("\tFetch group for " + field + " is " +
                _fetchGroups.get(field) + "\n");
        }

        return lDump.toString();
    }

    //============== class state and related class state methods =============

    public ClassState getClassState () { return _classState; }
    protected void setClassState (ClassState state)
    {
        _classState = state;
    }

    // returns an unmodifiable map of ClassStates
    protected Map getExtraClassStates ()
    {
        return Collections.unmodifiableMap(_extraClassStates);
    }

    //========== table state, related class and table state methods ==========

    public TableState getTableState ()
    {
        return getClassState().getTableState();
    }

	public TableState getTableState (String relatedClass)
	{
		TableState relatedState = null;

		if (relatedClass != null)
		{
			ClassState relatedClassState = null;

			// if it's this class (self-referencing), access our own class state
			if (relatedClass.equals(getMappingClassElement().getName()))
				relatedClassState = getClassState();
			else
			{
				relatedClassState = 
					(ClassState)_extraClassStates.get(relatedClass);

				// if it's null at this point, add one
				if (relatedClassState == null)
				{
					Model model = getModel();

					relatedClassState = new ClassState(
						model, model.getMappingClass(relatedClass));
					_extraClassStates.put(relatedClass, relatedClassState);
				}
			}

			// this will create one if necessary
			relatedState = relatedClassState.getTableState();
		}

		return relatedState;
	}

    public void setTableState (String relatedClass, TableElement primaryTable)
    {
		if (relatedClass != null)
		{
			ClassState relatedClassState = 
				(ClassState)_extraClassStates.get(relatedClass);

			// if it's null at this point, add one
			if (relatedClassState == null)
			{
				Model model = getModel();

				relatedClassState = new ClassState(model, 
					model.getMappingClass(relatedClass));
				_extraClassStates.put(relatedClass, relatedClassState);
			}

			// this will create one if necessary
			relatedClassState.getTableState().
				setCurrentPrimaryTable(primaryTable);
		}
    }
    
    //============== field state and relationship state methods ==============

    // returns an unmodifiable collection of FieldStates
    protected Collection getFieldStates ()
    {
        return Collections.unmodifiableCollection(_fieldStates.values());
    }

	public FieldState getFieldState (PersistenceFieldElement field)
	{
		return (FieldState)_fieldStates.get(field);
	}

	public RelationshipState getFieldState (RelationshipElement field)
	{
		return (RelationshipState)getFieldState((PersistenceFieldElement)field);
	}

	public FieldState getCurrentStateForField (PersistenceFieldElement field)
	{
		FieldState lState = getFieldState(field);

		if (lState == null)
			lState = getMappedStateForField(field);

		return lState;
	}

	public RelationshipState getCurrentStateForField (
		RelationshipElement field)
	{
		return (RelationshipState)getCurrentStateForField(
			(PersistenceFieldElement)field);
	}

	public FieldState getMappedStateForField (PersistenceFieldElement field)
	{
		if (field != null)
		{
			if (field instanceof RelationshipElement)
			{
				return getMappedStateForField((RelationshipElement)field);
			}
			else
			{
				return new FieldState(this, field, getMappedMapping(field));
			}
		}

		return null;
	}

	public RelationshipState getMappedStateForField (
		RelationshipElement field)
	{
		return RelationshipState.getMappedStateForField(this, field);
	}

    //=========== individual field and relationship mapping methods ==========

    public List getCurrentMapping (PersistenceFieldElement field)
    {
        FieldState state = getCurrentStateForField(field);
        
        if (state != null)
        {
			return Collections.unmodifiableList(
				((state instanceof RelationshipState) ? 
				((RelationshipState)state).getColumnPairState().
				getCompletePairs() : state.getLocalColumns()));
        }                

        return getMappedMapping(field);
    }
    
    public void setCurrentMapping (PersistenceFieldElement field, 
		ColumnElement column)
    {
        setCurrentMapping(field, 
            ((column != null) ? Collections.singletonList(column) : null));
    }

    public void setCurrentMapping (PersistenceFieldElement field, List members)
    {
        FieldState state = getFieldState(field);

        if (state == null)
			state = addFieldMapping(field);

        state.setMapping(members);
    }

    public boolean setCurrentMapping (RelationshipElement field, 
		RelationshipState state)
    {
		boolean stateHasData = false;

		if ((state != null) && state.getColumnPairState().hasCompleteRows())
		{
			replaceFieldState(field, state);
			initializeInverseMapping(field);
			stateHasData = true;
		}

		return stateHasData;
    }

	protected List getMappedMapping (PersistenceFieldElement field)
	{
		MappingClassElement mappingClass = getMappingClassElement();
		ArrayList members = new ArrayList();

		if (mappingClass != null)
		{
			MappingFieldElement mappingField = 
				mappingClass.getField(field.getName());

			if (mappingField != null)
			{
				members.addAll(mappingField.getColumns());

				if (mappingField instanceof MappingRelationshipElement)
				{
					members.addAll(((MappingRelationshipElement)
						mappingField).getAssociatedColumns());
				}
			}
		}

		return members;
	}

	// we can make this more sophisticated later
	public ColumnElement getDefaultMapping (PersistenceFieldElement field)
	{
		Collection candidateColumns = getTableState().getSortedAllColumns();

		if ((candidateColumns != null) && (field != null))
		{
			Iterator iterator = candidateColumns.iterator();
			String fieldName = field.getName();
			Object matches[] = {null, null, null, null};
			int i, count = matches.length;

			while (iterator.hasNext())
			{
				ColumnElement next = (ColumnElement)iterator.next();
				String colName = next.getName().getName();
				String strippedColName = null;
				String tableName = null, strippedTableName = null;

				// case 1 - natural match
				if (fieldName.equalsIgnoreCase(colName))
				{
					insertElement(matches, NATURAL, next);
					break;
				}

				// case 2 - matches with _ removed
				strippedColName = getStrippedName(colName);
				if (fieldName.equalsIgnoreCase(strippedColName))
					insertElement(matches, REMOVE_DELIM, next);

				// compound name cases
				tableName = next.getDeclaringTable().getName().getName();

				// case 3 - table.column natural match
				if (fieldName.equalsIgnoreCase(tableName + colName))
					insertElement(matches, NATURAL_COMPOUND, next);

				// case 4 - table.column match with _ removed
				strippedTableName = getStrippedName(tableName);
				if (fieldName.equalsIgnoreCase(
					strippedTableName + strippedColName) ||
				    fieldName.equalsIgnoreCase(strippedTableName + colName) ||
				    fieldName.equalsIgnoreCase(tableName + strippedColName))
				{
					insertElement(matches, REMOVE_DELIM_COMPOUND, next);
				}
			}

			// now return the match with the first index
			for (i = 0; i < count; i++)
			{
				if (matches[i] != null)
					return (ColumnElement)matches[i];
			}
		}

		return null;
	}
        
	public void addDefaultMapping ()
	{
		addDefaultMapping(true);
	}

	public void addDefaultMapping (boolean overwrite)
	{
		// start from a list of all fields whether they have states yet or not
		PersistenceFieldElement[] fields =
			getPersistenceClassElement().getFields();
		int i, count = ((fields != null) ? fields.length : 0);

		for (i = 0; i < count; i++)
		{
			PersistenceFieldElement field = fields[i];
			boolean okToMap = overwrite;

			if (!okToMap)	// check if already mapped or not
			{
				List columns = getCurrentMapping(field);

				if ((columns == null) || (columns.size() == 0))
					okToMap = true;
			}

			if (okToMap)
			{
				if (field instanceof RelationshipElement)
				{
					RelationshipElement rel = (RelationshipElement)field;

					setCurrentMapping(rel, 
						RelationshipState.getDefaultStateForField(this, rel));
				}
				else
				{
					ColumnElement candidate = getDefaultMapping(field);

					if (candidate != null)
						setCurrentMapping(field, candidate);
				}
			}
		}
	}

	private void insertElement (Object[] array, int index, Object element)
	{
		if (array[index] == null)
			array[index] = element;
	}

	private String getStrippedName (String originalName)
	{
		String delimiter = "_"; //NOI18N

		if (originalName.indexOf(delimiter) != -1)
		{
			StringTokenizer st = new StringTokenizer(originalName, delimiter);
			String strippedName = ""; //NOI18N

			while (st.hasMoreTokens())
				strippedName += st.nextToken();

			return strippedName;
		}

		return originalName;
	}

    protected FieldState addFieldMapping (PersistenceFieldElement field)
    {
        FieldState state = getFieldState(field);

        if (state == null)
        {
			if (field instanceof RelationshipElement)
				state = new RelationshipState(this, (RelationshipElement)field);
			else
            	state = new FieldState(this, field);

            _fieldStates.put(field, state);
		}

		return state;
    }

    public void addFieldMapping (RelationshipElement field, String relatedClass)
    {
		if (field != null)
		{
			RelationshipElement relatedField = getCurrentRelatedField(field);
			RelationshipState newState = new RelationshipState(this, field);

			replaceFieldState(field, newState);
			setCurrentRelatedClass(field, relatedClass);
			newState.setRelatedField(relatedField);
		}
    }

    public FieldState editFieldMapping (PersistenceFieldElement field,
		FieldState defaultState)
    {
		FieldState state = null;

		if (field != null)
		{
			state = getFieldState(field);

			if (state == null)
			{
				if (defaultState != null)
					state = defaultState;
				else
					state = getMappedStateForField(field);

				if (state != null)
					_fieldStates.put(field, state);
			}
		}

		return state;
    }

	public void replaceFieldState (PersistenceFieldElement field, 
		FieldState state)
	{
		if (state != null)	// just replace it
			_fieldStates.put(field, state);
		else				// remove the old one if it exists
			_fieldStates.remove(field);
	}
	
	// unmaps the relevant fields based on the new table state (called
	// whenever the owning class state's table state is updated)
	protected void updateFieldStates (TableState newState)
	{
		Iterator iterator = getFieldsToUnmap(newState).iterator();

		while (iterator.hasNext())
			clearFieldMapping((PersistenceFieldElement)iterator.next());
	}

	public void clearFieldMapping (PersistenceFieldElement field)
	{
		if (field instanceof RelationshipElement)
		{
			RelationshipElement relationship = (RelationshipElement)field;
			RelationshipElement relatedField = ((field != null) ? 
				getCurrentRelatedField(relationship) : null);

			unmapField(relationship);

			if (relatedField != null)
				getRelatedState(relationship).unmapField(relatedField);
		}
		else
		{
			FieldState state = getFieldState(field);

			if (state == null)	
				addFieldMapping(field);
			else			// state was already there, clear it out
				state.setMapping(null);
		}
	}

	private void unmapField (RelationshipElement field)
	{
        RelationshipState state = getFieldState(field);

        if (state != null)	
			state.setMapping(null);
		else
		{
			state = (RelationshipState)addFieldMapping(field);
			state.setRelatedClass(getMappedRelatedClass(field));
			state.setRelatedField(getMappedRelatedField(field));
		}
	}

    public void removeFieldMapping (RelationshipElement field)
    {
		if (field != null)
		{
        	FieldState state = getFieldState(field);

			if (state != null)
				_fieldStates.remove(state);

			// Do we need to do something here for managed rels?
			// This method is not called right now, so the proper behavior is 
			// not obvious, but we might decide to remove the related field's
			// mapping or do so only if the related field and this field 
			// belong to the same class
		}
    }

    //====================== fetch group methods ======================
    // returns an unmodifiable collection of fetchgroups
    protected Map getFetchGroups ()
    {
        return Collections.unmodifiableMap(_fetchGroups);
    }

    // returns the fetch group from the map if it is modified or from 
    // the mapping model if not
    public Integer getFetchGroup (PersistenceFieldElement fieldElement)
    {
        Integer _fetchGroup = (Integer)_fetchGroups.get(fieldElement);

        if (_fetchGroup == null)
        {
            boolean isKey = fieldElement.isKey();
            MappingFieldElement mappingField =
                getMappingClassElement().getField(fieldElement.getName());
            boolean isMapped = (mappingField != null);

            if (isMapped && !isKey)
                _fetchGroup = new Integer(mappingField.getFetchGroup());
        }

        return _fetchGroup;
    }

    public void setFetchGroup (PersistenceFieldElement fieldElement, 
        int fetchGroup)
    {
        _fetchGroups.put(fieldElement, new Integer(fetchGroup));
    }

    //================ related field holder state methods ===============

	public FieldHolderState getRelatedState (RelationshipElement field)
	{
		return getRelatedState(getCurrentRelatedClass(field), true);
	}

	public FieldHolderState getRelatedState (RelationshipElement field,
		boolean create)
	{
		return getRelatedState(getCurrentRelatedClass(field), create);
	}

	protected FieldHolderState getRelatedState (String relatedClass, 
		boolean create)
	{
		FieldHolderState relatedState = null;

		if (relatedClass != null)
		{
			ClassState relatedClassState = null;

			// if it's this class (self-referencing), return our own state
			if (relatedClass.equals(getMappingClassElement().getName()))
				relatedState = this;
			else
			{
				relatedClassState = 
					(ClassState)_extraClassStates.get(relatedClass);

				// if it's null at this point, add one
				if (relatedClassState == null)
				{
					Model model = getModel();

					relatedClassState = new ClassState(
						model, model.getMappingClass(relatedClass));
					_extraClassStates.put(relatedClass, relatedClassState);
				}
			}

			// create only if the flag is true, otherwise, only access it
			// if it already exists
			if ((relatedState == null) && 
				(create || relatedClassState.hasFieldHolderState()))
			{
				relatedState = relatedClassState.getFieldHolderState();
			}
		}

		return relatedState;
	}

    //============== relationship mapping methods: column pairs ===============

	public void addColumnPair (ColumnElement localColumn,
		ColumnElement foreignColumn, RelationshipElement field, int type)
	{
		RelationshipState state = getFieldState(field);

		if (state != null)
			state.addColumnPair(localColumn, foreignColumn, type);
	}

	public void editColumnPair (int index, ColumnElement localColumn,
		ColumnElement foreignColumn, RelationshipElement field, int type,
		RelationshipState defaultState)
	{
		if (field != null)
		{
			RelationshipState state = 
				(RelationshipState)editFieldMapping(field, defaultState);

			if (state != null)
				state.editColumnPair(index, localColumn, foreignColumn, type);
		}
	}

	public void removeColumnPair (int index, RelationshipElement field, 
		int type)
	{
		RelationshipState state = getFieldState(field);

		if (state != null)
			state.removeColumnPair(index, type);
	}

	public boolean hasPairAt (int index, RelationshipElement field, int type)
	{
		RelationshipState state = getFieldState(field);

		if (state != null)
			return state.hasPairAt(type, index);

		return false;
	}

    //============== relationship mapping methods: related class ===============

	public String getCurrentRelatedClass (RelationshipElement field)
	{
        RelationshipState state = getFieldState(field);

		return ((state != null) ? state.getRelatedClass() : 
			getMappedRelatedClass(field));
	}

	public void setCurrentRelatedClass (RelationshipElement field, 
		String relatedClass)
	{
        RelationshipState state = 
			(RelationshipState)addFieldMapping(field);

		state.setRelatedClass(relatedClass);
	}

	public String getMappedRelatedClass (RelationshipElement field)
	{
		return getModel().getRelatedClass(field);
	}

	public boolean isLegalMappingForRelatedClass (RelationshipElement field, 
		String newRelatedClass)
	{
		TableState relState = getTableState(newRelatedClass);

		if (relState != null)
		{
			RelationshipState state = getCurrentStateForField(field);
			Collection pairs = (state.isJoinRelationship() ? 
				state.getAssociatedColumnPairState().getCompletePairs() : 
				state.getColumnPairState().getCompletePairs());
			Collection currentTables = relState.getAllTables();
			Iterator iterator = pairs.iterator();

			while (iterator.hasNext())
			{
				String[] pair = (String[])iterator.next();

				if (!currentTables.contains(NameUtil.getTableName(pair[1])))
					return false;
			}

			return true;
		}

		return false;
	}

    //============== relationship mapping methods: related field ===============

	public RelationshipElement getCurrentRelatedField (
		RelationshipElement field)
	{
        RelationshipState state = getFieldState(field);

		return ((state != null) ? state.getRelatedField(): 
			getMappedRelatedField(field));
	}

	public String getCurrentRelatedFieldName (RelationshipElement field)
	{
        RelationshipState state = getFieldState(field);

		if (state != null)
		{
			RelationshipElement relatedField = state.getRelatedField();

			return ((relatedField != null) ? relatedField.getName() : null);
		}

		return getMappedRelatedFieldName(field);
	}

	public void setCurrentRelatedField (RelationshipElement field, 
		RelationshipElement relatedField)
	{
		RelationshipElement oldRelatedField = getCurrentRelatedField(field);

		if (oldRelatedField != relatedField)
		{
			if (oldRelatedField != null)	// set other side of old one to null
			{
				getRelatedState(field).changeRelatedField(
					oldRelatedField, null);
			}

			changeRelatedField(field, relatedField);
		}
	}

	private void changeRelatedField (RelationshipElement field, 
		RelationshipElement relatedField)
	{
        RelationshipState state = getFieldState(field);

		if (state == null)
		{
			state = (RelationshipState)getMappedStateForField(field);

			if (state == null)
			{
				state = new RelationshipState(this, field);
				state.setRelatedClass(getMappedRelatedClass(field));
			}

            _fieldStates.put(field, state);
		}

		state.setRelatedField(relatedField);

		if (relatedField != null)
			state.setRelatedClass(relatedField.getDeclaringClass().getName());
	}

	public RelationshipElement getMappedRelatedField (
		RelationshipElement field)
	{
		return ((field != null) ? 
			field.getInverseRelationship(getModel()) : null);
	}

	public String getMappedRelatedFieldName (RelationshipElement field)
	{
		return ((field != null) ? field.getInverseRelationshipName() : null);
	}

	public List getSortedRelatedFieldCandidates (RelationshipElement field)
	{
		return RelationshipState.getSortedRelatedFieldCandidates(this, field);	
	}

    //======== relationship mapping methods: default and inverse states ======

	public RelationshipState addDefaultStateForField (
		RelationshipElement field, TableElement joinTable, boolean useJoin, 
		boolean mustMatchFlag)
	{
		RelationshipState state = RelationshipState.getDefaultStateForField(
			this, field, joinTable, useJoin, mustMatchFlag);

		setCurrentMapping(field, state);

		return state;
	}

	public RelationshipState addInverseStateForField (RelationshipElement field)
	{
		RelationshipState state = 
			RelationshipState.getInverseStateForField(this, field);
	
		if (state != null)
			replaceFieldState(field, state);

		return state;
	}

	private void initializeInverseMapping (RelationshipElement field)
	{
		RelationshipElement relatedField = getCurrentRelatedField(field);

		if (relatedField != null)
		{
			RelationshipState current = getCurrentStateForField(field);
			boolean hasRows = ((current != null) && 
				current.getColumnPairState().hasCompleteRows());

			if (!hasRows)	// apply inverse to this field
				addInverseStateForField(field);
			else // apply this field's mapping to new inverse
			{
				FieldHolderState relatedFieldState =
					getRelatedState(field);
				RelationshipState relatedState = relatedFieldState.
					getCurrentStateForField(relatedField);

				if (relatedState != null)
					relatedState.setRelatedField(field);
				/*else
					System.out.println("***initializeInverseMapping:need 
					to create a related state for field");
				*/
			}
		}
	}

	public void setupInverseMapping  (RelationshipElement field)
	{
		RelationshipElement relatedField = getCurrentRelatedField(field);

		if (relatedField != null)
		{
			initializeInverseMapping(field);

			// if both fields are in the same class, we need to keep them in 
			// sync instead of computing the inverse during apply
			if (this == getRelatedState(field))
			{
				RelationshipState inverse = RelationshipState.
					getInverseStateForState(this, 
					getCurrentStateForField(field));

				if (inverse != null)
					replaceFieldState(relatedField, inverse);
			}
		}
	}

    //================== schema utilities ==========================

	// returns true if the field's related class is mapped to a 
	// different schema
	public boolean hasInvalidSchema (RelationshipElement field)
	{
		String thisClass = field.getDeclaringClass().getName();
		String relatedClass = getCurrentRelatedClass(field);

		// only need to check schemas if the class names don't match
		if ((relatedClass != null) && !thisClass.equals(relatedClass))
		{
			String relatedSchema = 
				getTableState(relatedClass).getCurrentSchemaName();

			if ((relatedSchema.length() > 0) && !relatedSchema.equals(
				getTableState().getCurrentSchemaName()))
			{
				return true;
			}
		}

		return false;
	}

    //================== table utilities ==========================

	public TableElement getPrimaryTable (String relatedClass)
	{
		TableState relatedState = getTableState(relatedClass);

		return ((relatedState != null) ?
			relatedState.getCurrentPrimaryTable() : null);
	}

    //================== column and member utilities ==========================

    public List getSortedAllColumns ()
    {
        TableState tableState = getTableState();

        return ((tableState != null) ? tableState.getSortedAllColumns() : 
            TableState.getSortedAllColumns(getMappingClassElement().getTables()));
    }
    
    public static List getSortedAllColumns (TableElement table)
    {
        ArrayList list = new ArrayList();
		
        if (table != null)
            list.add(table);

        return TableState.getSortedAllColumns(list);
    }

    //================== field utilities ==========================

	protected Collection getFieldsToUnmap (TableState newState)
	{
		TableState myState = getTableState();
		Collection list = new ArrayList();

		// figure out which tables have been removed from the available list
		// and unmap any fields which are mapped to columns in that table
		if (myState != null)
		{
			Iterator iterator = myState.getRemovedTables(newState).iterator();

			while (iterator.hasNext())
				list = getFieldsToUnmap(((String)iterator.next()), list);
		}

		return list;
	}

	private Collection getFieldsToUnmap (String table, Collection list)
	{
		PersistenceFieldElement[] fields = 
			getPersistenceClassElement().getFields();
		int i, count = ((fields != null) ? fields.length : 0);

		for (i = 0; i < count; i++)
		{
			PersistenceFieldElement nextField = fields[i];
			FieldState fieldState = getCurrentStateForField(nextField);

			if ((fieldState != null) && (fieldState.isMappedToTable(table)))
			{
				if (!list.contains(nextField))
					list.add(nextField);
			}
		}

		return list;
	}
}
