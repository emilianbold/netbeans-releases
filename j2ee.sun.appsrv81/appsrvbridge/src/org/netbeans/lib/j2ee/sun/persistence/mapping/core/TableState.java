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
 * TableState.java
 *
 * Created on July 7, 2000, 11:07 AM
 */

package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.NameUtil;

import com.sun.jdo.api.persistence.model.*;
import com.sun.jdo.api.persistence.model.mapping.*;
import com.sun.jdo.api.persistence.model.jdo.*;
import com.sun.jdo.spi.persistence.utility.StringHelper;
import com.sun.jdo.spi.persistence.utility.JavaTypeHelper;

/**
 * This state object manages schema, primary table, and secondary table state 
 * information (including reference key pairs).  This state object may be
 * "owned" by an enclosing ClassState, but it is not required.
 *
 * @author Mark Munro
 * @version %I%
 */
public class TableState extends AbstractState implements Cloneable {

    // backpointer to enclosing class state (can be null)
    private ClassState _classState;

    private TableElement _newPrimaryTable;
    private boolean _removeExistingPrimaryTable;
    private SchemaElement _newSchema;
    private boolean _removeExistingSchema;

    // keys are Strings (relative table names), values are SecondaryTableStates
    private Map _secondaryTableStates;
    private Collection _deletedSecondaryTables;	// relative table names

    private Integer _consistencyLevel;

    /** Creates new TableState */
    public TableState (Model model, MappingClassElement state) 
    {
        super(model, state);
        _removeExistingPrimaryTable = false;
        _removeExistingSchema = false;
        _secondaryTableStates = new HashMap();
        _deletedSecondaryTables = new ArrayList();
    }

    /** Creates new TableState */
    public TableState (ClassState declaringClass) 
    {
        this(declaringClass.getModel(), 
		declaringClass.getMappingClassElement());
        _classState = declaringClass;
    }

    // cloning rules: do super.clone and accept the defaults for everything
    // except collections, other state objects, and collections of other
    // state objects.  For other state objects which are not backpointers, 
    // clone them.  For collections of other state objects which are not 
    // backpointers, create a new collection and clone the elements.
    // For collections of other objects, clone the collection.
    public Object clone ()
    {
        TableState clonedState = (TableState)super.clone();

        // Secondary tables
        clonedState._secondaryTableStates = getClonedMap(_secondaryTableStates);
        clonedState._deletedSecondaryTables = 
            new ArrayList(_deletedSecondaryTables);

        return clonedState;
    }

    public String getDebugInfo()
    {
        StringBuffer lDump = new StringBuffer();
		Iterator iterator = getSecondaryTableStates().iterator();
        
        lDump.append("New schema is " + _newSchema + "\n");
        lDump.append("New Table is " + getNewPrimaryTable() + "\n");
        
        lDump.append("Secondary Table States are : \n");
        while (iterator.hasNext())
            lDump.append(((SecondaryTableState)iterator.next()).getDebugInfo());

        lDump.append("Removed Secondary Tables are : \n");
		iterator = _deletedSecondaryTables.iterator();
        while (iterator.hasNext())
            lDump.append("\t" + iterator.next() + "\n");
        
        lDump.append("Consistency Level is " + getConsistencyLevel() + "\n");
        return lDump.toString();
    }

    //================== class state methods ==========================

    public ClassState getClassState () { return _classState; }
    protected void setClassState (ClassState state)
    {
        _classState = state;
    }

    //================== schema methods ===============================

	protected boolean getExistingSchemaRemoved () 
	{
		return _removeExistingSchema;
	}

	protected boolean schemaChangedFrom (String oldSchema)
	{
		return ((oldSchema != null) && 
			!oldSchema.equals(getCurrentSchemaName()));
	}

	private boolean schemaHasTable (String fullTableName)
	{
		SchemaElement currentSchema = getCurrentSchema();

		if (currentSchema != null)
		{
			return ((fullTableName != null) && (currentSchema.getTable(
				DBIdentifier.create(fullTableName)) != null));
		}

		return false;
	}

    public SchemaElement getCurrentSchema ()
    {
        SchemaElement lReturnSchema = _newSchema;

        if ((_newSchema == null) && !getExistingSchemaRemoved())
        	lReturnSchema = getMappedSchema();

        return lReturnSchema;
    }

    public String getCurrentSchemaName ()
    {
        SchemaElement lSchema = this.getCurrentSchema();
 
        // usually getCurrentSchema will delegate to getMappedSchema
        // if necessary, but in the case of an illegal schema (can't be found)
        // getMappedSchema will return null but getMappedSchemaName
        // will return the name of the illegal schema
        return ((lSchema != null) ? lSchema.getName().getFullName() : 
        	((!getExistingSchemaRemoved()) ? getMappedSchemaName() : 
        	""));	// NOI18N
    }

    public void setCurrentSchema (SchemaElement schema)
    {
        String oldSchema = getCurrentSchemaName();

        _removeExistingSchema = ((schema == null) && (_newSchema == null) && 
            (getMappedSchemaName().trim().length() > 0));
        _newSchema = schema;

        if (schemaChangedFrom(oldSchema))
            removeRelevantTables();
    }

    public SchemaElement getMappedSchema ()
    {
        String schemaName = getMappedSchemaName();
        SchemaElement lReturnSchema = null;

        if (!StringHelper.isEmpty(schemaName))
        	lReturnSchema = SchemaElement.forName(schemaName);

        return lReturnSchema;
    }

    public String getMappedSchemaName ()
    {
        MappingClassElement mappingClass = this.getMappingClassElement();
        String schemaName = null;

        if (mappingClass != null)
        	schemaName = mappingClass.getDatabaseRoot();

        return ((schemaName != null) ? schemaName : "");	// NOI18N
    }

    // returns TableElements
    public List getSortedSchemaTables ()
    {
        return getSortedSchemaTables(null);
    }

    // returns TableElements
    public List getSortedUnusedSchemaTables ()
    {
        return getSortedSchemaTables(getAllTables());
    }

    // returns TableElements
    public List getSortedSchemaTables (Collection excludeList)
    {
        SchemaElement lSchema = this.getCurrentSchema();
        TableElement[] lTableList = 
        	((lSchema != null) ? lSchema.getTables() : null);
        boolean hasTables = (lTableList != null);
        ArrayList returnList = (hasTables ? 
            new ArrayList(Arrays.asList(lTableList)) : new ArrayList());
        int i, count = (hasTables ? lTableList.length : 0);

        if (excludeList != null)
        {
        	Iterator excludeIterator = excludeList.iterator();

        	while (excludeIterator.hasNext())
        	{
            	String tableName = (String)excludeIterator.next();
            	Iterator iterator = returnList.iterator();

            	while (iterator.hasNext())
            	{
                	TableElement testElement = (TableElement)iterator.next();

                	if (tableName.equals(testElement.getName().getName()))
                		iterator.remove();
            	}
        	}
        }

        Collections.sort(returnList);

        return returnList;
    }

    //================== primary table methods ============================

	protected boolean getExistingPrimaryTableRemoved () 
	{
		return _removeExistingPrimaryTable;
	}

	protected boolean primaryTableChangedFrom (String oldTable)
	{
		return ((oldTable != null) && 
			!oldTable.equals(getCurrentPrimaryTableName()));
	}

    public TableElement getCurrentPrimaryTable ()
    {
        TableElement lReturnTable = getNewPrimaryTable();

        if ((lReturnTable == null) && !getExistingPrimaryTableRemoved())
           lReturnTable = getMappedPrimaryTable();

        return lReturnTable;
    }

    public String getCurrentPrimaryTableName ()
    {
        TableElement lTable = this.getCurrentPrimaryTable();

        // usually getCurrentPrimaryTable will delegate to getMappedPrimaryTable
        // if necessary, but in the case of an illegal table (can't be found)
        // getMappedPrimaryTable will return null but getMappedPrimaryTableName
        // will return the name of the illegal table
        return ((lTable != null)  ? lTable.getName().getName() : 
        	((!getExistingPrimaryTableRemoved()) ? getMappedPrimaryTableName() :
        	""));		// NOI18N
    }

    public void setCurrentPrimaryTable (TableElement primaryTable)
    {
        String oldTable = getCurrentPrimaryTableName();
        boolean newIsNull = (primaryTable == null);

        if (!newIsNull)
            setCurrentSchema(primaryTable.getDeclaringSchema());

        _removeExistingPrimaryTable = (newIsNull && 
            (getNewPrimaryTable() == null) && 
            (getMappedPrimaryTableName().trim().length() > 0));
        _newPrimaryTable = primaryTable;

        if (primaryTableChangedFrom(oldTable))
            removeAllSecondaryTables();
    }

    protected TableElement getNewPrimaryTable () { return _newPrimaryTable; }

    public TableElement getMappedPrimaryTable ()
    {
        MappingClassElement mappingClass = this.getMappingClassElement();
        String tableName = getMappedPrimaryTableName();
        TableElement lReturnTable = null;

        if ((mappingClass != null) && !StringHelper.isEmpty(tableName))
		{
			String absoluteTableName = NameUtil.getAbsoluteTableName(
				mappingClass.getDatabaseRoot(), tableName);

			lReturnTable = TableElement.forName(absoluteTableName);
		}
      
        return lReturnTable;
    }

    // used to return full table name, now returns relative table name
    // which is different than getCurrentPrimaryTableName which always 
    // returned the relative table name
    public String getMappedPrimaryTableName ()
    {
        MappingClassElement mappingClass = this.getMappingClassElement();
        String lReturnTable = null;

        if (mappingClass != null)
        {
        	 ArrayList lTableList = mappingClass.getTables();
        	 if ( lTableList.size() > 0 )
        	 {
            	 MappingTableElement lElement = 
            	     (MappingTableElement)lTableList.get(0);

            	 lReturnTable = lElement.getTable();
        	 }
        }
        
        return ((lReturnTable != null) ? lReturnTable : "");	// NOI18N
    }

	// excludes twopk for this release
	private static List getPrimaryTableCandidates (SchemaElement schema)
	{
		TableElement[] lTableList = 
			((schema != null) ? schema.getTables() : null);
		int i, count = ((lTableList != null) ? lTableList.length : 0);
		ArrayList returnList = new ArrayList();

		for (i = 0; i < count; i++)
		{
			TableElement testTable = lTableList[i];

			if (testTable.getPrimaryKey() != null)
				returnList.add(testTable);
		}

		return returnList;		
	}

	// excludes twopk for this release
	// returns a sorted collection of candidates
	public static List getSortedPrimaryTableCandidates (
		SchemaElement schema)
	{
		List returnList = getPrimaryTableCandidates(schema);

		Collections.sort(returnList);

		return returnList;		
	}

	// we can make this more sophisticated later
	public TableElement getDefaultTableMapping (String className)
	{
		Collection candidates = getPrimaryTableCandidates(getCurrentSchema());
		String delimiter = "_";		// NOI18N

		if ((candidates != null) && (className != null))
		{
			Iterator iterator = candidates.iterator();

			// take off the package name if necessary
			className = JavaTypeHelper.getShortClassName(className);

			while (iterator.hasNext())
			{
				TableElement next = (TableElement)iterator.next();
				String tableName = next.getName().getName();

				if (className.equalsIgnoreCase(tableName))
					return next;
				else if (tableName.indexOf(delimiter) != -1 ) 
				/* Only if "_" is found in the tableName - this test should be 
				 * modified when the list of delimiters changes.	
				 */
				{
				   String strippedTableName = "";	// NOI18N
				   StringTokenizer st = 
				   	new StringTokenizer(tableName, delimiter);
				   while (st.hasMoreTokens()) 
				        strippedTableName += st.nextToken();

				   if (className.equalsIgnoreCase(strippedTableName))
				        return next;
				}
			}
		}

		return null;
	}

	public TableElement getDefaultTableMapping (String className, List suffixes)
	{
		TableElement candidate = getDefaultTableMapping(className);

		// if there's no match for className, try by removing suffixes
		if ((candidate == null) && (suffixes != null))
		{
			Iterator iterator = suffixes.iterator();

			while ((candidate == null) && iterator.hasNext())
			{
				String suffix = (String)iterator.next();
				int length = (className.length() - 
					((suffix != null) ? suffix.length() : 0));
				String testSuffix = ((length > 0) ?
					className.substring(length) : null);

				if (!StringHelper.isEmpty(testSuffix) && testSuffix.
					equalsIgnoreCase(suffix))
				{
					candidate = getDefaultTableMapping(
						className.substring(0, length));
				}
			}
		}

		return candidate;
	}

	public void addDefaultTableMapping (String className, List suffixes)
	{
		TableElement candidate = getDefaultTableMapping(className, suffixes);

		if (candidate != null)
			setCurrentPrimaryTable(candidate);
	}

    //================== secondary table methods ==========================

	// returns an unmodifiable collection of SecondaryTableStates
    protected Collection getSecondaryTableStates ()
    {
        return Collections.unmodifiableCollection(
            _secondaryTableStates.values());
    }

	// uses relative table names
	private SecondaryTableState getSecondaryTableState (String table)
	{
		return ((table == null) ? null : 
			(SecondaryTableState)_secondaryTableStates.get(table));
	}

	public SecondaryTableState getCurrentStateForTable (
		TableElement primaryTable, String secondaryTable)
	{
		SecondaryTableState lState = getSecondaryTableState(secondaryTable);

		if (lState == null)
			lState = getMappedStateForTable(primaryTable, secondaryTable);

		return lState;
	}
		
	public SecondaryTableState getMappedStateForTable (
		TableElement primaryTable, String secondaryTable)
	{
		MappingReferenceKeyElement key = findReferenceKey(primaryTable, 
			secondaryTable);

		if (key != null)
		{
			SecondaryTableState state = 
				new SecondaryTableState(getModel(), this, secondaryTable);

			state.getPairState().addColumnPairs(key.getColumnPairNames());

			return state;
		}

		return null;
	}

	public SecondaryTableState addDefaultStateForTable (
		TableElement primaryTable, TableElement secondaryTable)
	{
		ForeignKeyElement fk = findForeignKey(primaryTable, secondaryTable);
		String secondaryTableName = secondaryTable.getName().getName();
		SecondaryTableState state = null;

		if (fk != null)
		{
			state = new SecondaryTableState(
				getModel(), this, secondaryTableName);
			state.getPairState().addColumnPairs(fk.getColumnPairs());
		}
		else
			state = getMappedStateForTable(primaryTable, secondaryTableName);

		if (state != null)
			replaceSecondaryTableState(secondaryTableName, state);

		return state;
	}

    public void addSecondaryTable (TableElement secondaryTable)
    {
		if (secondaryTable != null)
		{
			String secondaryTableName = secondaryTable.getName().getName();

			replaceSecondaryTableState(secondaryTableName, 
				new SecondaryTableState(getModel(), this, secondaryTableName));
		}
    }

	private void replaceSecondaryTableState (String secondaryTable, 
		SecondaryTableState state)
	{
		if (state != null)	// just replace it
			_secondaryTableStates.put(secondaryTable, state);
		else				// remove the old one if it exists
			_secondaryTableStates.remove(secondaryTable);
	}
	
    public SecondaryTableState editSecondaryTable (String secondaryTable)
    {
		SecondaryTableState lState = null;

		if (secondaryTable != null)
		{
			lState = getSecondaryTableState(secondaryTable);

			if (lState == null)
			{
				lState = getMappedStateForTable(getCurrentPrimaryTable(), 
					secondaryTable);

				if (lState != null)
					_secondaryTableStates.put(secondaryTable, lState);
			}
		}

		return lState;
    }

    public void removeSecondaryTable (String secondaryTable)
    {
		if (secondaryTable != null)
		{
			SecondaryTableState lState = getSecondaryTableState(secondaryTable);

			if (lState != null)		// added or edited this session
				_secondaryTableStates.remove(secondaryTable);

			// mapped, not added this session or mapped and edited this session
			if ((lState == null) || (getMappedStateForTable(
				getCurrentPrimaryTable(), secondaryTable) != null))
			{
				_deletedSecondaryTables.add(secondaryTable);
			}
		}
    }

    public void removeAllSecondaryTables()
    {
        _secondaryTableStates.clear();
        _deletedSecondaryTables.clear();
        _deletedSecondaryTables.addAll(getMappedSecondaryTables());    
    }

	// may be temporary after ok is disabled when there are illegal pairs shown
	protected void updateSecondaryTables ()
	{
		Iterator iterator = getSecondaryTableStates().iterator();

		while (iterator.hasNext())
		{
			PairState pairState = 
				((SecondaryTableState)iterator.next()).getPairState();

			// remove illegal column pairs
			pairState.setColumnPairs(pairState.getColumnPairs());
		}
	}

	// returns a collection of relative names of the current secondary tables
	private Collection getCurrentSecondaryTables()
	{
		ArrayList currentTables = new ArrayList();
		Iterator iterator = getSecondaryTableStates().iterator();

		while (iterator.hasNext())
		{
			SecondaryTableState next = (SecondaryTableState)iterator.next();

			if (next.getPairState().hasCompleteRows())
				currentTables.add(next.getTableName());
		}

		iterator = getMappedSecondaryTables().iterator();
		while (iterator.hasNext())	// exclude by name here?
		{
			String next = (String)iterator.next();

			if ((next != null) && !_deletedSecondaryTables.contains(next) && 
				!currentTables.contains(next))
			{
				currentTables.add(next);
			}
		}

		return currentTables;        
	}

	// returns a sorted collection of relative table names
	public List getSortedSecondaryTables ()
	{
		List currentTables = new ArrayList(getCurrentSecondaryTables());

		Collections.sort(currentTables);

		return currentTables;        
	}

	// returns a collection of relative table names
    public Collection getMappedSecondaryTables ()
    {
        ArrayList lReturnArray = new ArrayList();
        MappingClassElement lMappingClass = this.getMappingClassElement();

        if (lMappingClass != null)
        {
            ListIterator iterator = null;

            lReturnArray.addAll(lMappingClass.getTables());
            if ( lReturnArray.size() > 0 )
                lReturnArray.remove(0);

            // now convert the mapping table elements to names
            iterator = lReturnArray.listIterator();
            while (iterator.hasNext())
                iterator.set(((MappingTableElement)iterator.next()).getTable());
        }

        return lReturnArray;
    }

	// returns a collection of MappingTableElements
	protected Collection getDeletedSecondaryMappingTables ()
	{
		MappingClassElement mappingClass = getMappingClassElement();
		ArrayList lDeletedSecondaryTables = new ArrayList();
		Iterator iterator = _deletedSecondaryTables.iterator();

		// convert elements which are table elements to mapping table elements
		while (iterator.hasNext())	// exclude by name here?
		{
			MappingTableElement mappingTable = 
				mappingClass.getTable((String)iterator.next());

			if (mappingTable != null)
				lDeletedSecondaryTables.add(mappingTable);
		}

		return lDeletedSecondaryTables;
	}

	protected ReferenceKey findCurrentKey (TableElement primaryTable, 
		String secondaryTable)
	{
		if ((primaryTable != null) && (secondaryTable != null))
		{
			SecondaryTableState state = 
				getCurrentStateForTable(primaryTable, secondaryTable);

			// return the pairs in the state
			if (state != null)
			{
				PairState pairs = state.getPairState();

				return (pairs.hasCompleteRows() ? pairs : null);
			}
		}

		return null;
	}

	protected MappingReferenceKeyElement findReferenceKey (
		TableElement primaryTable, String secondaryTable)
	{
		if ((primaryTable != null) && (secondaryTable != null))
		{
			MappingClassElement mappingClass = getMappingClassElement();
			MappingTableElement parentTable = 
				mappingClass.getTable(primaryTable.toString());

			if (parentTable != null)
			{
				MappingTableElement mappingTable = 
					mappingClass.getTable(secondaryTable);
				Iterator iterator = parentTable.getReferencingKeys().iterator();

				while (iterator.hasNext())
				{
					MappingReferenceKeyElement testKey = 
						(MappingReferenceKeyElement)iterator.next();

					if (testKey.getTable().equals(mappingTable))
						return testKey;
				}
			}
		}

		return null;
	}

	protected static ForeignKeyElement findForeignKey (
		TableElement primaryTable, TableElement secondaryTable)
	{
		if ((primaryTable != null) && (secondaryTable != null))
		{
			ForeignKeyElement[] foreignKeys = primaryTable.getForeignKeys();
			int i, count = ((foreignKeys != null) ? foreignKeys.length : 0);

			for (i = 0; i < count; i++)
			{
				ForeignKeyElement fk = foreignKeys[i];

				if (secondaryTable == fk.getReferencedTable())
					return fk;
			}
		}

		return null;
	}

    //================== table utilities ==========================

	// returns a list of names of tables which would be removed by 
	// applying newState
	protected Collection getRemovedTables (TableState newState)
	{
		ArrayList removedTables = new ArrayList();

		// figure out which tables have been removed from the available list
		// and unmap any fields which are mapped to columns in that table
		removedTables.addAll(getAllTables());

		if (newState != null)
		{
			String newPrimary = newState.getCurrentPrimaryTableName();

			removedTables.removeAll(newState.getAllTables());
			addIfAbsent(removedTables, _deletedSecondaryTables);

			if (newState.getExistingPrimaryTableRemoved())
			{
				addIfAbsent(removedTables, 
					newState.getMappedPrimaryTableName());

				// for the case it is newly added in this wizard state
				addIfAbsent(removedTables, newPrimary);
			}

			// if treating this as a schema rename, remove the PT from the list
			if (newState.schemaChangedFrom(getCurrentSchemaName()) &&
				!newState.primaryTableChangedFrom(getCurrentPrimaryTableName()))
			{
				removeIfPresent(removedTables,
					newState.getMappedPrimaryTableName());
			}

			// if the new primary table used to be a secondary table, 
			// add it to the list as well because attach would unmap it
			if ((newPrimary != null) && 
				getCurrentSecondaryTables().contains(newPrimary))
			{
				addIfAbsent(removedTables, newPrimary);
			}

			// can't do this because of dups -- maybe if change to using sets?
			// removedTables.addAll(newState.getDeletedSecondaryTables());
		}

		return removedTables;
	}

	private void addIfAbsent (List list, Object object)
	{
		if (object instanceof String)
		{
			String string = (String)object;

			if (StringHelper.isEmpty(string))
				object = null;
		}

		if ((object != null) && !list.contains(object))
			list.add(object);
	}

	private void addIfAbsent (List destinationList, Collection sourceCollection)
	{
		Iterator iterator = sourceCollection.iterator();

		while (iterator.hasNext())
			addIfAbsent(destinationList, iterator.next());
	}

	private void removeIfPresent (List list, Object object)
	{
		if (object instanceof String)
		{
			String string = (String)object;

			if (StringHelper.isEmpty(string))
				object = null;
		}

		if ((object != null) && list.contains(object))
			list.remove(object);
	}

	private void removeRelevantTables ()
	{
		SchemaElement currentSchema = getCurrentSchema();
		String currentPrimaryName = getCurrentPrimaryTableName();

		if (currentSchema != null)
		{
			if (currentPrimaryName != null)	// check if exists in new schema
			{
				// deal with secondaries including deleted ones
				if (schemaHasTable(currentPrimaryName))
				{
					ArrayList candidateList = 
						new ArrayList(_secondaryTableStates.keySet());
					ArrayList removedTables = new ArrayList();
					Iterator iterator = null;

					candidateList.addAll(getMappedSecondaryTables());
					iterator = candidateList.iterator();

					while (iterator.hasNext())
					{
						String nextSecondary = (String)iterator.next();

						if (!schemaHasTable(nextSecondary))
							addIfAbsent(removedTables, nextSecondary);
					}

					// now update the deleted list
					iterator = removedTables.iterator();
					while (iterator.hasNext())
						removeSecondaryTable((String)iterator.next());
				}
				else
					setCurrentPrimaryTable(null);
			}
		}
		else 		// if schema is null, remove everything
			setCurrentPrimaryTable(null);
	}

    // returns a list of relative table names
    protected List getAllTables()
    {
        ArrayList lList = new ArrayList();
        String lPrimary = this.getCurrentPrimaryTableName();

        if (!StringHelper.isEmpty(lPrimary))
            lList.add(lPrimary);

        addIfAbsent(lList, getCurrentSecondaryTables());

        return lList;
    }

	// returns a sorted collection of TableElements
	// sorting is primary table first followed by alphabetically sorted
	// sorted secondary tables
    public List getSortedAllTables()
    {
        List returnList = getAllTables();

        if (returnList.size() > 1)	// need to sort
        {
            ArrayList tempList = new ArrayList(returnList);
			Object primary = tempList.remove(0);

			Collections.sort(tempList);
			tempList.add(0, primary);
			returnList = tempList;
        }

        return returnList;		
    }

	// returns a list of relative table names for a supplied list of 
	// table elements and mapping table elements
	private List getAllTables (Collection tables)
	{
		ArrayList returnList = new ArrayList();
		Iterator iterator = tables.iterator();

		while (iterator.hasNext())
		{
			Object next = iterator.next();

			if ((next instanceof TableElement) || 
				(next instanceof MappingTableElement))
			{
				returnList.add(next.toString());
			}
		}

		return returnList;		
	}

    //================== column utilities ==========================

	// member name is of the form table.column
	protected TableElement getTable (String memberName)
	{
		return TableElement.forName(NameUtil.getAbsoluteTableName(
			getCurrentSchemaName(), NameUtil.getTableName(memberName)));
	}

	// column name is relative
	public ColumnElement getColumn (String columnName)
	{
		TableElement table = getTable(columnName);
		
		return ((table == null) ? null : table.getColumn(DBIdentifier.create(
			NameUtil.getAbsoluteMemberName(
			getCurrentSchemaName(), columnName))));
	}

	// returns a sorted collection of ColumnElements
	// sorting is primary table columns first (alphabetically), followed by
	// sorted secondary tables with their columns (alphabetically)
    public List getSortedAllColumns()
    {
        return getSortedAllColumns(getCurrentSchemaName(), 
            getSortedAllTables());
    }

	// returns a sorted collection of ColumnElements
	// sorting is alphabetically by column grouped by tables in the 
	// order they are supplied
	// list of tables can be TableElements or MappingTableElements
    protected static List getSortedAllColumns (List tables)
    {
        return getSortedAllColumns(null, tables);
    }

	// returns a sorted collection of ColumnElements
	// sorting is alphabetically by column grouped by tables in the 
	// order they are supplied
	// list of tables can be TableElements, MappingTableElements, or Strings
	// (relative table names).  Database root can be null if none of the 
	// arguments are strings
    protected static List getSortedAllColumns (String databaseRoot, 
        List tables)
    {
        ArrayList lColumns = new ArrayList();
        Iterator iterator = tables.iterator();

        while (iterator.hasNext())
        {
            Object nextTable = iterator.next();
            List tableColumns = null;
            ColumnElement[] lTColumns = null;
            TableElement lTable = null;

            if (nextTable instanceof TableElement)
                lTable  = (TableElement)nextTable;
            else
            {
                String absoluteTableName = null;

                if (nextTable instanceof MappingTableElement)
                {
                    MappingTableElement lImpl = (MappingTableElement)nextTable;
					
                    absoluteTableName = NameUtil.getAbsoluteTableName(
                    	lImpl.getDeclaringClass().getDatabaseRoot(),
                    	lImpl.getTable());
                }
                else if (nextTable instanceof String)
                {
                    absoluteTableName = NameUtil.getAbsoluteTableName(
                    	databaseRoot, (String)nextTable);
                }

                if (absoluteTableName != null)
                    lTable = TableElement.forName(absoluteTableName);
            }
            lTColumns = ((lTable != null) ? lTable.getColumns() : null);
            tableColumns = (((lTColumns != null) && (lTColumns.length > 0)) ? 
                Arrays.asList(lTColumns) : new ArrayList());
            Collections.sort(tableColumns);
            lColumns.addAll(tableColumns);
        }

        return lColumns;
    }

    //================== consistency level methods ==========================
    public int getConsistencyLevel ()
    {
        if (_consistencyLevel == null)
            setConsistencyLevel(getMappingClassElement().getConsistencyLevel());

        return _consistencyLevel.intValue();
    }

    public void setConsistencyLevel (int consistencyLevel)
    {
        _consistencyLevel = new Integer(consistencyLevel);
    }
}
