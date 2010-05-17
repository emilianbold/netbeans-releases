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
 * RelationshipState.java
 *
 * Created on July 11, 2000, 09:00 PM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.*;
import java.text.Collator;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.jdo.*;
import com.sun.jdo.api.persistence.model.mapping.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.NameUtil;

/**
 * This state object manages the mapping for a particular relationship field.
 * It maintains two pair state objects (column pairs, associated column pairs),
 * the related class and related field, and information about whether this 
 * mapping uses a join table.
 *
 * @author Mark Munro
 * @author Rochelle Raccah
 * @version %I%
 */
public class RelationshipState extends FieldState
	implements PairHolderState, Cloneable
{
	public static final int LOCAL = 0;
	public static final int ASSOCIATED = 1;

	// This is the threshhold of tables in a schema under which 
	// we will iterate and look for a join table when looking for 
	// default mapping. We define a threshhold instead of looking 
	// through all tables in every schema in order not to threaten
	// performance in the case of a large schema.  The actual 
	// value was decided among the team, but is defined here as a 
	// static constant so it is visible and easy to change. 
	private static final int TABLE_THRESHHOLD = 50;

	private PairState _columnsPairState;
	private PairState _associatedColumnsPairState;
	private String _relatedClass;
	private RelationshipElement _relatedField;
	private String _joinTable;
	private boolean _useJoin;
	
	public RelationshipState (FieldHolderState declaringHolder, 
		RelationshipElement field) 
	{
		super(declaringHolder, field);

		Model model = getModel();
		_columnsPairState = new PairState(model, this, getColumns());
		_associatedColumnsPairState = new PairState(model, 
			this, new ArrayList());
	}

	// cloning rules: do super.clone and accept the defaults for everything
	// except collections, other state objects, and collections of other
	// state objects.  For other state objects which are not backpointers, 
	// clone them.  For collections of other state objects which are not 
	// backpointers, create a new collection and clone the elements.
	// For collections of other objects, clone the collection.
	public Object clone ()
	{
		RelationshipState clonedState = (RelationshipState)super.clone();

		clonedState._columnsPairState = (PairState)_columnsPairState.clone();
		clonedState._columnsPairState.setColumns(clonedState.getColumns());
		clonedState._associatedColumnsPairState = 
			(PairState)_associatedColumnsPairState.clone();

		return clonedState;
	}

	public String getDebugInfo()
	{
		StringBuffer lDump = new StringBuffer(super.getDebugInfo());

		lDump.append("\tRelated class is " + getRelatedClass() + "\n");
		lDump.append("\tRelated field is " + getRelatedField() + "\n");

		return lDump.toString();
	}

	protected String getColumnDebugInfo ()
	{
		StringBuffer lDump = new StringBuffer();

		lDump.append(getColumnPairState().getDebugInfo());
		if (isJoinRelationship())
		{
			lDump.append("\tMapped to Join Columns\n");
			lDump.append(_associatedColumnsPairState.getDebugInfo());
		}

		return lDump.toString();
	}

	public boolean isJoinRelationship () { return _useJoin; }
	public void setJoinRelationship (boolean flag) { _useJoin = flag; }

	public String getJoinTable () { return _joinTable; }
	public void setJoinTable (TableElement table)
	{
		_joinTable = ((table != null) ? table.getName().getName() : null);
	}

    //================== PairHolderState implementation =======================

    public TableState getTableState ()
    {
		return getFieldHolderState().getTableState();
    }

    //=================== column/member mapping methods =======================

    protected void setMapping (List columns)
    {
		super.setMapping(columns);
		if ((columns == null) || (columns.size() == 0))
            getAssociatedColumnPairState().setColumnPairs(null);
    }

	public boolean hasInvalidMapping ()
	{
		return (getColumnPairState().hasInvalidPairs() && (!isJoinRelationship()
			|| getAssociatedColumnPairState().hasInvalidPairs()));
	}

	protected List getLocalColumns ()
	{
		Iterator iterator = getColumnPairState().getCompletePairs().iterator();
		List localList = new ArrayList();

		while (iterator.hasNext())
			localList.add(((String[])iterator.next())[0]);

		return localList;
	}

    //==================== column pair methods =====================

	public boolean hasPairAt (int type, int index)
	{
		return getPairState(type).hasPairAt(index);
	}

	public PairState getColumnPairState ()
	{
		return _columnsPairState;
	}

	private PairState getPairState (int type)
	{
		return ((type == LOCAL) ? getColumnPairState() : 
			getAssociatedColumnPairState());
	}

	public PairState getAssociatedColumnPairState ()
	{
		return _associatedColumnsPairState;
	}

    public void addColumnPair (ColumnElement localColumn, 
		ColumnElement foreignColumn, int type)
    {
		getPairState(type).addColumnPair(localColumn, foreignColumn);
    }

    public void editColumnPair (int index, ColumnElement localColumn, 
		ColumnElement foreignColumn, int type)
    {
		getPairState(type).editColumnPair(index, localColumn, foreignColumn);
    }

    public void removeColumnPair (int index, int type)
    {
		getPairState(type).removeColumnPair(index);
    }

    //==================== related class methods =====================

	public String getRelatedClass () { return _relatedClass; }

	public void setRelatedClass (String relatedClass)
	{
		_relatedClass = relatedClass;
	}

    //==================== related field methods =====================

	public RelationshipElement getRelatedField () { return _relatedField; }

	public void setRelatedField (RelationshipElement relatedField)
	{
		_relatedField = relatedField;
	}

	// returns a sorted collection of relationship field names
	protected static List getSortedRelatedFieldCandidates (
		FieldHolderState holder, RelationshipElement field)
	{
		ArrayList returnList = new ArrayList();
		String relatedClassName = holder.getCurrentRelatedClass(field);

		if (relatedClassName != null)
		{
			Model model = holder.getModel();
			PersistenceClassElement relatedPClass =
				model.getPersistenceClass(relatedClassName);

			if (relatedPClass != null)
			{
				RelationshipElement[] candidateRels = 
					relatedPClass.getRelationships();
				String thisClassName = 
					holder.getMappingClassElement().getName();
				int i, count = ((candidateRels != null) ?
					candidateRels.length : 0);

				for (i = 0; i < count; i++)
				{
					RelationshipElement candidate = candidateRels[i];

					if ((candidate != field) && 
						(PersistenceFieldElement.PERSISTENT == 
						candidate.getPersistenceType()))
					{
						String candidateRelatedClass = 
							holder.getCurrentRelatedClass(candidate);

						if (thisClassName.equals(candidateRelatedClass)
							 || (isCollection(candidate, model) && 
							(candidateRelatedClass == null)))
						{
							FieldHolderState relatedState = holder.getRelatedState(
								candidate.getDeclaringClass().getName(), true);
							RelationshipElement relatedField = relatedState.
								getCurrentRelatedField(candidate);

							if ((relatedField == null) || 
								(relatedField == field))
							{
								RelationshipState lField = 
									holder.getCurrentStateForField(field);
								RelationshipState relFieldState = 
									((relatedState == null) ? null : 
									relatedState.getCurrentStateForField(
									candidate));

								if (isInverseMapping(lField, relFieldState))
									returnList.add(candidate.getName());
							}
						}
					}
				}
			}
		}

		Collections.sort(returnList, Collator.getInstance());

		return returnList;	
	}

    //=============== default, mapped, and inverse state methods =============

	protected static RelationshipState getMappedStateForField (
		FieldHolderState holder, RelationshipElement field)
	{
		if (field != null)
		{
			MappingClassElement mappingClass = holder.getMappingClassElement();
			MappingFieldElement mappingField = 
				mappingClass.getField(field.getName());

			if ((mappingField != null) && (mappingField instanceof
				MappingRelationshipElement))
			{
				RelationshipState state = new RelationshipState(holder, field);
				Collection locals = ((MappingRelationshipElement)mappingField).
					getAssociatedColumns();

				state.setRelatedClass(holder.getMappedRelatedClass(field));
				state.setRelatedField(holder.getMappedRelatedField(field));
				state.getColumnPairState().addColumnPairs(
					mappingField.getColumns());

				// process extra info if it's a join table relationship
				if ((locals != null) && (locals.size() > 0))
				{
					PairState associatedPairState =
						state.getAssociatedColumnPairState();

					state.setJoinRelationship(true);
					associatedPairState.addColumnPairs(locals);
					state._joinTable = 
						associatedPairState.getDeclaringTableName();
				}

				return state;
			}
		}

		return null;
	}

	// methods for finding and adding default and inverse relationship states
	// if there is an inverse, that must be used, unless the join table 
	// usage doesn't match the flags; otherwise the remaining parameters are
	// used to try and find a default
	// must match means if useJoin is true joinTable must be used
	// must match false means we can try both cases (using a join table and
	// not using one) useJoin determines which case to try first
	protected static RelationshipState getDefaultStateForField (
		FieldHolderState holder, RelationshipElement field, 
		TableElement joinTable, boolean useJoin, boolean mustMatchFlag)
	{
		RelationshipState state = 
			(mustMatchFlag ? null : getInverseStateForField(holder, field));
		boolean hasJoin = (joinTable != null);

		if (mustMatchFlag)
		{
			if (!useJoin)
			{
				state = getDefaultStateForField(holder, field, true);
				// state is a join so it shouldn't match
				if ((state != null) && state.isJoinRelationship())
					state = null;
			}
			else            // compute both fks/rks using the join table
				state = getDefaultStateForField(holder, field, joinTable);
		}
		else if ((state == null) || 
			!state.getColumnPairState().hasCompleteRows())
		{
			state = ((useJoin && hasJoin) ? 
				getDefaultStateForField(holder, field, joinTable)
				: getDefaultStateForField(holder, field));

			if (state == null)		// try the other case
			{
				state = ((!useJoin) ?  
					getDefaultStateForField(holder, field, joinTable) : 
					getDefaultStateForField(holder, field));
			}
		}

		return state;
	}

	// This method finds a default state for a given join table.  
	// If the supplied joinTable is <code>null</code>, it searches 
	// for a join table in the following way: first try a table 
	// with a name made up of the primary table and related primary
	// table (both orderings).  If there is no valid join table by 
	// either of those names and the schema has fewer than
	// TABLE_THRESHHOLD tables, iterate those looking for the 
	// first valid join table.
	private static RelationshipState getDefaultStateForField (
		FieldHolderState holder, RelationshipElement field, 
		TableElement joinTable)
	{
		TableState tableState = holder.getTableState();
		TableElement relatedPrimary = 
			holder.getPrimaryTable(holder.getCurrentRelatedClass(field));
		TableElement primary = tableState.getCurrentPrimaryTable();
		ReferenceKey[] keys = 
			findKeys(holder, primary, relatedPrimary, joinTable);

		if ((keys == null) && (joinTable == null)) 	// search for a candidate
		{
			SchemaElement schema = tableState.getCurrentSchema();

			if ((schema != null) && (primary != null) && 
				(relatedPrimary != null))
			{
				String primaryName = primary.getName().getName();
				String relatedName = relatedPrimary.getName().getName();
				String schemaName = schema.getName().getFullName();
				TableElement candidate = TableElement.forName(
					NameUtil.getAbsoluteTableName(schemaName, 
					primaryName + relatedName));

				// first look for a combination of the two names
				if ((keys = findKeys(holder, primary, relatedPrimary,
					candidate)) != null)
				{
					joinTable = candidate;
				}
				else 		// flip the order of the names
				{
					candidate = TableElement.forName(
						NameUtil.getAbsoluteTableName(schemaName, 
						relatedName + primaryName));
					if ((keys = findKeys(holder, primary, relatedPrimary,
						candidate)) != null)
					{
						joinTable = candidate;
					}
					// iterate tables in schema if there are < TABLE_THRESHHOLD
					else if (schema != null)
					{
						TableElement[] lTableList = 
							((schema != null) ? schema.getTables() : null);
						int i, count = 
							((lTableList != null) ? lTableList.length : 0);

						// only iterate if <= TABLE_THRESHHOLD tables
						if (count > TABLE_THRESHHOLD)
							return null;

						for (i = 0; i < count; i++)
						{
							candidate = lTableList[i];
							if ((keys = findKeys(holder, primary, 
								relatedPrimary, candidate)) != null)
							{
								joinTable = candidate;
								break;
							}
						}
					}
				}
			}
		}

		if (keys != null)
			return getStateForKeys(holder, field, joinTable, keys[0], keys[1]);

		return null;
	}

	// this method first tries a direct link, then looks for a 
	// valid join table and tries that
	private static RelationshipState getDefaultStateForField (
		FieldHolderState holder, RelationshipElement field, 
		boolean mustMatch)
	{
		TableElement myPrimary = 
			holder.getTableState().getCurrentPrimaryTable();
		TableElement relatedPrimary = 
			holder.getPrimaryTable(holder.getCurrentRelatedClass(field));
		ReferenceKey key = findKey(holder, myPrimary, relatedPrimary);
		RelationshipState state = null;

		if (key != null)
			state = getStateForKey(holder, field, key);
		else if (relatedPrimary != null)
		{
			PairState pairState = null;
			boolean hasComplete = false;

			state = holder.getMappedStateForField(field);
			pairState = ((state != null) ? state.getColumnPairState() : null);
			hasComplete = (pairState != null) && pairState.hasCompleteRows();

			if((state!=null) && !hasComplete && !pairState.hasInvalidPairs())
				state = null;

			if ((state != null) && hasComplete)
			{
				Collection columnPairs = (!state.isJoinRelationship() ? 
					pairState.getAllColumnPairs() : 
					state.getAssociatedColumnPairState().getAllColumnPairs());
				Iterator iterator = columnPairs.iterator();
				String[] firstPair = (String[])iterator.next();

				if ((myPrimary == null) || !myPrimary.getName().getName().
					equals(NameUtil.getTableName(firstPair[0])) || 
					!relatedPrimary.getName().getName().
					equals(NameUtil.getTableName(firstPair[1])))
				{
					state = null;
				}
			}
		}

		// look for a join table if necessary
		if (!mustMatch && (state == null))
			state = getDefaultStateForField(holder, field, null);

		return state;
	}

	public static RelationshipState getDefaultStateForField (
		FieldHolderState holder, RelationshipElement field)
	{
		return getDefaultStateForField(holder, field, false);
	}
	
	public static RelationshipState getInverseStateForState (
		FieldHolderState holder, RelationshipState relatedState)
	{
		RelationshipState state = null;

		if (relatedState != null)
		{
			RelationshipElement relatedField = relatedState.getRelatedField();

			if (relatedField != null)
			{
				String relatedClass = relatedState.getRelatedClass();

				if (relatedState != null)
				{
					boolean isJoin = relatedState.isJoinRelationship();
					Collection relatedPairs = 
						relatedState.getColumnPairState().getCompletePairs();
					Collection inversePairs = getInversePairs((isJoin) ? 
						relatedState.getAssociatedColumnPairState().
						getCompletePairs() : relatedPairs);

					state = new RelationshipState(holder, relatedField);
					state.setRelatedField( 
						(RelationshipElement)relatedState.getField());
					state.setRelatedClass( 
						state.getRelatedField().getDeclaringClass().getName());
					state.setJoinRelationship(isJoin);
					state.getColumnPairState().addColumnPairs(inversePairs);

					if (isJoin)
					{
						state._joinTable = relatedState.getJoinTable();
						state.getAssociatedColumnPairState().addColumnPairs( 
							getInversePairs(relatedPairs));
					}
				}
			}
		}

		return state;
	}

	public static RelationshipState getInverseStateForField (
		FieldHolderState holder, RelationshipElement field)
	{
		RelationshipState state = null;

		if (field != null)
		{
			RelationshipElement relatedField = 
				holder.getCurrentRelatedField(field);

			if (relatedField != null)
			{
				String relatedClass = holder.getCurrentRelatedClass(field);
				RelationshipState relatedState = holder.
					getRelatedState(field).getCurrentStateForField(relatedField);

				if (relatedState != null)
				{
					relatedState.setRelatedField(field);
					state = getInverseStateForState(holder, relatedState);
				}
			}
		}

		return state;
	}

	private static RelationshipState getStateForKey
		(FieldHolderState holder, RelationshipElement field, ReferenceKey key)
	{
		RelationshipState state = null;

		if (key != null)
		{
			TableState tableState = holder.getTableState();
			TableElement testTable = tableState.getCurrentPrimaryTable();
			boolean sameTable = key.getDeclaringTable().equals(testTable);
			TableElement relatedPrimary = 
				holder.getPrimaryTable(holder.getCurrentRelatedClass(field));
			boolean selfRef = ((relatedPrimary != null) && 
				(testTable.equals(relatedPrimary)));
			ColumnPairElement[] pairs = key.getColumnPairs();
			PairState pairState = null;

			state = new RelationshipState(holder, field);
			state.setRelatedClass(holder.getCurrentRelatedClass(field));
			state.setRelatedField(holder.getCurrentRelatedField(field));
			pairState = state.getColumnPairState();

			if ((selfRef && isCollection(field, holder.getModel())) || 
				!sameTable)	// add flipped
			{
				pairState.addColumnPairs(getInversePairs(pairs));
			}
			else		// add as is
				pairState.addColumnPairs(pairs);
		}

		return state;
	}

	private static RelationshipState getStateForKeys (FieldHolderState holder, 
		RelationshipElement field, TableElement joinTable, 
		ReferenceKey key1, ReferenceKey key2)
	{
		RelationshipState state = null;

		if ((key1 != null) && (key2 != null))
		{
			ColumnPairElement[] pairs = key1.getColumnPairs();
			PairState pairState = null;

			state = new RelationshipState(holder, field);
			state.setRelatedClass(holder.getCurrentRelatedClass(field));
			state.setRelatedField(holder.getCurrentRelatedField(field));
			state.setJoinRelationship(true);
			state.setJoinTable(joinTable);

			pairState = state.getColumnPairState();

			if (!(key1.getDeclaringTable().equals(
				holder.getTableState().getCurrentPrimaryTable())))	// add flipped
				pairState.addColumnPairs(getInversePairs(pairs));
			else		// add as is
				pairState.addColumnPairs(pairs);

			pairState = state.getAssociatedColumnPairState();
			pairs = key2.getColumnPairs();
			if (!(key2.getDeclaringTable().equals(joinTable)))	// add flipped
				pairState.addColumnPairs(getInversePairs(pairs));
			else		// add as is
				pairState.addColumnPairs(pairs);
		}

		return state;
	}

	private static ReferenceKey findKey (FieldHolderState holder,
		TableElement primaryTable, TableElement secondaryTable)
	{
		TableState myState = holder.getTableState();
		ReferenceKey key = 
			TableState.findForeignKey(primaryTable, secondaryTable);

		if ((key == null) && (secondaryTable != null))
		{
			key = myState.findCurrentKey(primaryTable, 
				secondaryTable.getName().getName());
		}

		if (key == null)		// try backwards
		{
			key = TableState.findForeignKey(secondaryTable, primaryTable);
			if ((key == null) && (primaryTable != null))
			{
				key = myState.findCurrentKey(secondaryTable,
					primaryTable.getName().getName());
			}
		}

		return key;
	}

	private static ReferenceKey[] findKeys (FieldHolderState holder, 
		TableElement primary, TableElement relatedPrimary, 
		TableElement joinCandidate)
	{
		if (joinCandidate != null)
		{
			ReferenceKey key1 = findKey(holder, primary, joinCandidate);
			ReferenceKey key2 = findKey(holder, joinCandidate, relatedPrimary);

			if ((key1 != null) && (key2 != null) && (key1 != key2))
				return new ReferenceKey[]{key1, key2};
		}

		return null;
	}

	private static boolean isInverseMapping (RelationshipState state1, 
		RelationshipState state2)
	{
		boolean state1IsNotNull = (state1 != null);
		boolean state2IsNotNull = (state2 != null);
		PairState pairState1 = 
			(state1IsNotNull ? state1.getColumnPairState() : null);
		PairState pairState2 = 
			(state2IsNotNull ? state2.getColumnPairState() : null);
		boolean state1HasRows = state1IsNotNull && pairState1.hasCompleteRows();
		boolean state2HasRows = state2IsNotNull && pairState2.hasCompleteRows();

		// if both have rows, they must be exact inverses
		if (state1HasRows && state2HasRows)
		{
			boolean state1IsJoin = state1.isJoinRelationship();

			if (state1IsJoin == state2.isJoinRelationship())
			{
				List pairs1 = pairState1.getCompletePairs();
				List pairs2 = pairState2.getCompletePairs();

				return ((!state1IsJoin) ? isInverseMapping(pairs1, pairs2) :
					(isInverseMapping(pairs1, 
					state2.getAssociatedColumnPairState().getCompletePairs()) &&
					isInverseMapping(state1.getAssociatedColumnPairState().
					getCompletePairs(), pairs2)));
			}
			
			return false;
		}

		// if neither have rows or only one has rows, that's fine
		return true;
	}

	// input must consist of the valid pairs only (String[2]) and 
	// they must be in the same order
	private static boolean isInverseMapping (List pairs1, List pairs2)
	{
		int i, size1 = pairs1.size(), size2 = pairs2.size();

		if (size1 == size2)
		{
			for (i = 0; i < size1; i++)
			{
				String[] nextPair = (String[])pairs1.get(i);
				String[] inversePair = (String[])pairs2.get(i);

				if (!nextPair[0].equals(inversePair[1]) || 
					!nextPair[1].equals(inversePair[0]))
				{
					return false;
				}
			}

			return true;
		}

		return false;
	}

    //================== inverse pair utilities ==========================

	private static Collection getInversePairs (ColumnPairElement[] originalPairs)
	{
		return getInversePairs(Arrays.asList(originalPairs));
	}

	private static Collection getInversePairs (Collection originalPairs)
	{
		Collection inversePairs = null;

		if (originalPairs != null)
		{
			Iterator iterator = originalPairs.iterator();

			inversePairs = new ArrayList(originalPairs.size());

			while (iterator.hasNext())
				inversePairs.add(getInversePair(iterator.next()));
		}

		return inversePairs;
	}

	private static Object getInversePair (Object originalPair)
	{
		if (originalPair != null)
		{
			Object local = null, foreign = null;

			if (originalPair instanceof String[])
			{
				local = ((String[])originalPair)[0];
				foreign = ((String[])originalPair)[1];

				if ((local != null) & (foreign != null))
					return new String[] {(String)foreign, (String)local};
			}
			else if (originalPair instanceof ColumnPairElement)
			{
				local = ((ColumnPairElement)originalPair).getLocalColumn();
				foreign = ((ColumnPairElement)originalPair).
					getReferencedColumn();
			}
			else if (originalPair instanceof ColumnElement[])
			{
				local = ((ColumnElement[])originalPair)[0];
				foreign = ((ColumnElement[])originalPair)[1];
			}

			if ((local != null) & (foreign != null))
			{
				return new ColumnElement[] {(ColumnElement)foreign,
					(ColumnElement)local};
			}
		}

		return null; 
	}

    //================== collection utilities ==========================

	protected static boolean isCollection (RelationshipElement field, 
		Model model)
	{
		String fieldType = model.getFieldType(
			field.getDeclaringClass().getName(), field.getName());

		return model.isCollection(fieldType);
	}
}
