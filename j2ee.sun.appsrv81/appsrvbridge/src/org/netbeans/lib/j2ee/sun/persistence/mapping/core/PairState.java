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
 * PairState.java
 *
 * Created on June 4, 2001, 4:40 PM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.NameUtil;

import com.sun.jdo.api.persistence.model.Model;

/**
 * This state object manages something with column pair information.  This 
 * state object is "owned" by its enclosing FieldHolderState.
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class PairState extends AbstractState implements ReferenceKey, Cloneable
{
	private List _columns;		// of String[2], each of the form table.column
	private PairHolderState _holderState;	// backpointer to enclosing state

	// create a PairState - array of String[2] to be managed must
	// be passed in
	public PairState (Model model, PairHolderState declaringHolder, 
		List columns)
	{
		super(model);
		_holderState = declaringHolder;

		if (_holderState != null)
			setMappingClassElement(_holderState.getMappingClassElement());

		setColumns(columns);
	}

	// used only by constructor and to repair pointers after clone if necessary
	protected void setColumns (List columns)
	{
		_columns = columns;
	}

	// cloning rules: do super.clone and accept the defaults for everything
	// except collections, other state objects, and collections of other
	// state objects.  For other state objects which are not backpointers, 
	// clone them.  For collections of other state objects which are not 
	// backpointers, create a new collection and clone the elements.
	// For collections of other objects, clone the collection.
	public Object clone ()
	{
		PairState clonedState = (PairState)super.clone();

		clonedState._columns = new ArrayList(_columns);

		return clonedState;
	}

	public String getDebugInfo ()
	{
		StringBuffer lDump = new StringBuffer();
		List myPairs = getCompletePairs();
		int i, count = ((myPairs != null) ? myPairs.size() : 0);

		lDump.append("Pairs are : \n");

		if (count > 0)
		{
			for (i = 0; i < count; i++)
			{
				String[] nextPair = (String[])myPairs.get(i);

				lDump.append("\t\tColumn pair : " + nextPair[0] + ", " + 
					nextPair[1] + "\n");
			}
		}
		else
			lDump.append("\t\tNone\n");

		return lDump.toString();
	}

	public TableState getTableState ()
	{
		return _holderState.getTableState();
	}

	/** Get the name of this element.
	 * @return the name
	 */
	public String getKeyName ()
	{
		throw new UnsupportedOperationException();
	}

	/** Set the name of this element.
	 * @param name the name
	 */
	public void setKeyName (String name)
	{
		throw new UnsupportedOperationException();
	}

	/** Use this method to determine whether there are
	 * any complete pairs.
	 */
	public boolean hasCompleteRows ()
	{
		Iterator iterator = getAllColumnPairs().iterator();

		while (iterator.hasNext())
		{
			if (isCompletePair((String[])iterator.next()))
				return true;
		}

		return false;
	}

	public static boolean isCompletePair (Object[] pair)
	{
		return ((pair[0] != null) && (pair[1] != null));
	}

	/** Get all complete column pairs in this pair state.  This method returns 
	 * all column pairs including those  with names which correspond to 
	 * columns which cannot be found but excluding those which are incomplete 
	 * because they are "half set" by the user.  For all column pairs,
	 * call <code>getAllColumnPairs</code>, which returns String objects for 
	 * all pairs.  For validated pair names, call <code>getColumnPairs</code>.
	 * @return the column pair names
	 */
	public List getCompletePairs ()
	{
		Iterator iterator = getAllColumnPairs().iterator();
		ArrayList completePairs = new ArrayList();

		while (iterator.hasNext())
		{
			String[] nextPair = (String[])iterator.next();

			if (isCompletePair(nextPair))
				completePairs.add(nextPair);
		}

		return completePairs;
	}

	/** Use this method to determine whether there are
	 * any invalid pairs (those which contain column names which correspond 
	 * to columns which cannot be found.
	 */
	public boolean hasInvalidPairs ()
	{
		Iterator pairIterator = getAllColumnPairs().iterator();
		TableState tableState = getTableState();

		while (pairIterator.hasNext())
		{
			String[] nextPair = (String[])pairIterator.next();

			if (isCompletePair(nextPair))
			{
				if ((tableState.getColumn(nextPair[0]) == null) || 
					(tableState.getColumn(nextPair[1]) == null))
				{
					return true;
				}
			}
		}

		return false;
	}

	public boolean hasPairAt (int index)
	{
		List list = getAllColumnPairs();

		return ((list != null) && (list.size() >= (index + 1)) && 
			(list.get(index) != null));
	}

	private String getRelativeName (DBMemberElement member)
	{
		return ((member != null) ? NameUtil.getRelativeMemberName(
			member.getName().getFullName()) : null);
	}

	protected void addColumnPair (String[] element)
	{
		_columns.add(element);
	}

	protected void addColumnPair (ColumnElement[] element)
	{
		addColumnPair(new String[] {getRelativeName(element[0]), 
			getRelativeName(element[1])});
	}

	public void addColumnPair (ColumnElement localColumn, 
		ColumnElement foreignColumn)
	{
		addColumnPair(new ColumnElement[] {localColumn, foreignColumn});
	}

	protected void addColumnPairs (Collection pairs)
	{
		if (pairs != null)
		{
			Iterator iterator = pairs.iterator();

			while (iterator.hasNext())
			{
				Object nextPair = iterator.next();

				if (nextPair instanceof ColumnElement[])
					addColumnPair((ColumnElement[])nextPair);
				else if (nextPair instanceof String[])
					addColumnPair((String[])nextPair);
				else if (nextPair instanceof String)
				{
					String pairName = (String)nextPair;
					int index = pairName.indexOf(';');

					addColumnPair(new String[]{pairName.substring(0, index),
						pairName.substring(index + 1)});
				}
			}
		}
	}

	public void editColumnPair (int index, ColumnElement localColumn, 
		ColumnElement foreignColumn)
	{
		_columns.set(index, new String[]{getRelativeName(localColumn),
			getRelativeName(foreignColumn)});
	}

	public void removeColumnPair (int index)
	{
		// if we are doing a clear and there is no such row, ignore it
		if (_columns.size() > index)
			_columns.remove(index);
	}

	private int getIndex (ColumnPairElement pair)
	{
		List pairs = getAllColumnPairs();
		int i, count = ((pairs != null) ? pairs.size() : 0);
		String testName = getRelativeName(pair);

		for (i = 0; i < count; i++)
		{
			String[] nextPair = (String[])pairs.get(i);

			if (testName.equals(nextPair[0] + ";" + nextPair[1]))	// NOI18N
				return i;
		}

		return -1;
	}

	/** Get the declaring table.  This method is provided as part of 
	 * the implementation of the ReferenceKey interface. 
	 * @return the table that owns this state, or <code>null</code> 
	 * if the element is not attached to any table
	 */
	public TableElement getDeclaringTable ()
	{
		return getTable(0);
	}

	// so a non-null value can be returned when pairs belong to an invalid table
	protected String getDeclaringTableName ()
	{
		Iterator pairIterator = getAllColumnPairs().iterator();

		while (pairIterator.hasNext())
		{
			String[] nextPair = (String[])pairIterator.next();

			if (nextPair[0] != null)
				return NameUtil.getTableName(nextPair[0]);
		}

		return null;
	}

	/** Set the table for this state to the supplied table.  This method 
	 * is provided as part of the implementation of the ReferenceKey 
	 * interface.
	 * @param table table element to be used with this key.
	 */
	public void setDeclaringTable (TableElement tableElement)
	{
		throw new UnsupportedOperationException();
	}

	/** Get the referenced table of the state.  This method is provided 
	 * as part of the implementation of the ReferenceKey interface.
	 * @return the referenced table
	 */
	public TableElement getReferencedTable ()
	{
		return getTable(1);
	}

	/** Convenience method used to extract the table from the pair half at 
	 * the supplied index.  The declaring or local table is at index 0 while
	 * the referenced table is at index 1.
	 * @return the table
	 */
	private TableElement getTable (int index)
	{
		Iterator pairIterator = getAllColumnPairs().iterator();
		TableState tableState = getTableState();

		while (pairIterator.hasNext())
		{
			String[] nextPair = (String[])pairIterator.next();

			if (nextPair[index] != null)
			{
				TableElement table = tableState.getTable(nextPair[index]);

				if (table != null)
					return table;
			}
		}

		return null;
	}

	/** Get all local columns in this state.  This method is provided 
	 * as part of the implementation of the ReferenceKey interface.
	 * @return the columns
	 */
	public ColumnElement[] getLocalColumns ()
	{
		ColumnPairElement[] columnPairs = getColumnPairs();
		int i, count = ((columnPairs != null) ? columnPairs.length : 0);
		ColumnElement[] columns = new ColumnElement[count];

		for (i = 0; i < count ; i++)
			columns[i] = columnPairs[i].getLocalColumn();

		return columns;
	}

	/** Get all referenced columns in this state.  This method is provided 
	 * as part of the implementation of the ReferenceKey interface.
	 * @return the columns
	 */
	public ColumnElement[] getReferencedColumns ()
	{
		ColumnPairElement[] columnPairs = getColumnPairs();
		int i, count = ((columnPairs != null) ? columnPairs.length : 0);
		ColumnElement[] columns = new ColumnElement[count];

		for (i = 0; i < count ; i++)
			columns[i] = columnPairs[i].getReferencedColumn();

		return columns;
	}

	/** Add a new column pair to the holder.
	 * @param pair the pair to add
	 */
	public void addColumnPair (ColumnPairElement pair)
	{
		addColumnPairs(new ColumnPairElement[]{pair});
	}

	/** Add some new column pairs to the holder.
	 * @param pairs the column pairs to add
	 */
	public void addColumnPairs (ColumnPairElement[] pairs)
	{
		int i, count = ((pairs != null) ? pairs.length : 0);

		for (i = 0; i < count ; i++)
		{
			ColumnPairElement pair = (ColumnPairElement)pairs[i];

			if (pair != null)
			{
				addColumnPair(pair.getLocalColumn(), 
					pair.getReferencedColumn());
			}
		}
	}

	/** Remove a column pair from the holder.
	 * @param pair the column pair to remove
	 */
	public void removeColumnPair (ColumnPairElement pair)
	{
		removeColumnPairs(new ColumnPairElement[]{pair});
	}

	/** Remove some column pairs from the holder.
	 * @param pairs the column pairs to remove
	 */
	public void removeColumnPairs (ColumnPairElement[] pairs)
	{
		int i, count = ((pairs != null) ? pairs.length : 0);

		for (i = 0; i < count ; i++)
			removeColumnPair(getIndex((ColumnPairElement)pairs[i]));
	}

	/** Remove duplicate column pairs from the holder.
	 */
	public void removeDuplicatePairs ()
	{
		List currentPairs = getAllColumnPairs();

		// in jdk1.4, we'll be able to use LinkedHashSet for this:
		// we need to remove duplicates while preserving the order
		if ((currentPairs != null) && (currentPairs.size() > 0))
		{
			ArrayList uniquePairs = new ArrayList();
			HashSet duplicateChecker = new HashSet();
			Iterator iterator = currentPairs.iterator();

			while (iterator.hasNext())
			{
				String[] nextPair = (String[])iterator.next();
				Object pairToAdd = nextPair;

				// We only want to add it as a string if it is a complete 
				// pair.  Otherwise, we keep it as a String[] because if not, 
				// nulls will be converted to "null" strings
				if (isCompletePair(nextPair))
				{
					// workaround: for some reason, this doesn't work on 
					// the pairs, need to convert it to a string
					pairToAdd = nextPair[0] + ";" + nextPair[1]; //NOI18N
			}

				if (duplicateChecker.add(pairToAdd))
				    uniquePairs.add(pairToAdd);
			}

			setColumnPairs(uniquePairs);
		}
	}

	/** Set the column pairs for this holder.
	 * Previous column pairs are removed.
	 * @param pairs the new column pairs
	 */
	public void setColumnPairs (ColumnPairElement[] pairs)
	{
		_columns.clear();						// remove the old ones
		addColumnPairs(pairs);					// add the new ones
	}

	/** Set the column pairs for this holder.
	 * Previous column pairs are removed.
	 * @param pairs the new column pairs
	 */
	private void setColumnPairs (Collection pairs)
	{
		_columns.clear();						// remove the old ones
		addColumnPairs(pairs);					// add the new ones
	}

	/** Get all (valid) column pairs in this holder.  Names which cannot
	 * be resolved to corresponding column pairs are not included.
	 * @return the column pairs
	 */
	public ColumnPairElement[] getColumnPairs ()
	{
		Iterator pairIterator = getAllColumnPairs().iterator();
		TableState tableState = getTableState();
		ArrayList returnPairs = new ArrayList();

		while (pairIterator.hasNext())
		{
			String[] nextPair = (String[])pairIterator.next();

			if (isCompletePair(nextPair))
			{
				String localColumn = nextPair[0];
				TableElement table = tableState.getTable(localColumn);
				DBMemberElement pair = ((table != null) ? table.getMember(
					DBIdentifier.create(NameUtil.getAbsoluteMemberName(
					tableState.getCurrentSchemaName(), 
					localColumn + ';' + nextPair[1]))) : null);

				if (pair != null)
					returnPairs.add(pair);
			}
		}

		return (ColumnPairElement[])returnPairs.toArray(
			new ColumnPairElement[0]);
	}

	/** Find a column pair by name.
	 * @param name the name of the column pair for which to look
	 * @return the column pair or <code>null</code> if not found
	 */
	public ColumnPairElement getColumnPair (DBIdentifier name)
	{
		ColumnPairElement[] myPairs = getColumnPairs();
		int count = ((myPairs != null) ? myPairs.length : 0);

		if (count > 0)
		{
			ColumnPairElement searchPair = (ColumnPairElement)
				getDeclaringTable().getMember(name);
			int i;

			for (i = 0; i < count; i++)
			{
				if (myPairs[i].equals(searchPair))
					return searchPair;
			}
		}

		return null;
	}

	/** Get all column pairs in this pair state.  This method returns 
	 * an unmodifiable collection of String[2] which contains all 
	 * column pairs including those which are incomplete (might be "half set" 
	 * by the user) and those names which correspond to columns which cannot 
	 * be found.  For valid column pairs, call <code>getColumnPairs</code>,
	 * which returns ColumnPairElement objects which are all valid and complete.
	 * @return the column pairs
	 */
	public List getAllColumnPairs ()
	{
		return Collections.unmodifiableList(_columns);
	}
}
