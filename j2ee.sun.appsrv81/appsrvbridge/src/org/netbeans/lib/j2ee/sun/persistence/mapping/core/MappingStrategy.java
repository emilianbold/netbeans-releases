/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


package org.netbeans.lib.j2ee.sun.persistence.mapping.core;

import java.util.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.NameUtil;

import com.sun.jdo.api.persistence.model.*;
import com.sun.jdo.api.persistence.model.jdo.*;
import com.sun.jdo.api.persistence.model.mapping.*;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingFieldElementImpl;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingRelationshipElementImpl;
import com.sun.jdo.spi.persistence.utility.I18NHelper;
import com.sun.jdo.spi.persistence.utility.StringHelper;

public class MappingStrategy {

    /** I18N message base */
    public static final String messageBase = 
        "org.netbeans.lib.j2ee.sun.persistence.mapping.core.Bundle"; // NOI18N

    /** Default I18N message handler */
    private static final ResourceBundle _defaultMessages = 
        I18NHelper.loadBundle(MappingStrategy.class);

    /** I18N message handler used by the instance */
    private final ResourceBundle _messages;

    public MappingStrategy ()
    {
        this(getDefaultMessages());
    }

    public MappingStrategy (ResourceBundle bundle)
    {
        super();
        _messages = bundle;
    }

    /** @return default I18N message handler for this object
     */
    protected static final ResourceBundle getDefaultMessages ()
    {
        return _defaultMessages;
    }

    /** @return I18N message handler for this object
     */
    protected ResourceBundle getMessages ()
    {
        return _messages;
    }

    public static String swapColumnNames(String columnPairName)
    { String          ret=null;
      StringTokenizer tokenizer=new StringTokenizer(columnPairName, ";");
      int    nrOfTokens=tokenizer.countTokens();

        if( nrOfTokens==2 )
            try
            { String    token1=tokenizer.nextToken(),
                        token2=tokenizer.nextToken();

                ret = token2 + ";" + token1;
            } catch( NoSuchElementException e ) {}

        return( ret );
    }

    public static MappingFieldElement attach(PersistenceFieldElement field, ColumnElement[] columns,
        MappingFieldElement fieldMapping, MappingClassElement classMapping)
        throws ModelException
    {
        boolean alreadyMapped = (fieldMapping != null);

        if (columns == null)	// so can support unmapping an old one
            columns = new ColumnElement[0];

        if( field != null )
        { PersistenceClassElement   declaringClass=field.getDeclaringClass();

            if( classMapping!=null )
            {
                if (!alreadyMapped)
                {
                    fieldMapping = new MappingFieldElementImpl( field.getName(), classMapping );
                    classMapping.addField(fieldMapping);
                }
                else	// remove all old columns from mapping
                    removeColumnMapping(fieldMapping);

                for( int i=0; i<columns.length; i++ )
                    fieldMapping.addColumn(columns[i]);
                if (!alreadyMapped)
                    classMapping.addField( fieldMapping );
            }
        }

        return( fieldMapping );
    }

    protected static MappingFieldElement[] getMappingFieldElements(MappingClassElement  mappingClassElement,
                                                                   ColumnElement[]      columnElements )
    { Vector    mappingFieldElements=null;

        if( columnElements!=null )
            for( int i=0; i<columnElements.length; i++ )
            { String    relativeColumnName=NameUtil.getRelativeMemberName(columnElements[i].getName().getFullName());

                for( Iterator ii=mappingClassElement.getFields().iterator(); ii.hasNext(); )
                { MappingFieldElement   mappingFieldElement=(MappingFieldElement) ii.next();

                    for( Iterator iii=mappingFieldElement.getColumns().iterator(); iii.hasNext(); )
                    { String    columnName=(String) iii.next();

                        //brazil
                        //System.out.println( "getMappingFieldElements: "+columnName+".equals("+relativeColumnName+")" );
                        if( columnName.equals(relativeColumnName) )
                        {   if( mappingFieldElements==null )
                                mappingFieldElements = new Vector();
                            mappingFieldElements.addElement( mappingFieldElement );
            }   }   }   }

        return( mappingFieldElements==null?null:(MappingFieldElement[])mappingFieldElements.toArray(new MappingFieldElement[0]) );
    }

    /**
     */
    public static void unattach(MappingClassElement mappingElement) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public static void unattach(MappingFieldElement mappingElement) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public static void unattach(MappingReferenceKeyElement mappingElement) {
        throw new UnsupportedOperationException();
    }

	public static String prepareAttach (ClassState oldState, ClassState state)
	{
		return null;
	}
	
	/**
	 * The following method checks what changes exist within the table state object
	 * and generate a warning string it these changes will have an impact on the meta data
	 */
	public String prepareAttach(FieldHolderState oldFieldState,
		TableState state)
	{
		if (oldFieldState != null)
		{
			TableState oldState = oldFieldState.getTableState();

			if (oldState != null)
			{
				Collection fieldsToUnmap = oldFieldState.getFieldsToUnmap(state);
				int count = oldState.getRemovedTables(state).size();
				boolean hasFieldsToUnmap = (fieldsToUnmap.size() > 0);
				String oldSchema = oldState.getCurrentSchemaName();
				boolean hasSchemaChange = state.schemaChangedFrom(oldSchema);
				String oldPrimaryTable = oldState.getCurrentPrimaryTableName();

				// removed primary with mapping info which will be lost
				// or caused a schema change which will cause the primary
				// table info to be lost
				if (state.primaryTableChangedFrom(oldPrimaryTable))
				{
					// check if there was a schema change or there are any
					// secondary tables or fields mapped
					if ((hasSchemaChange && !StringHelper.isEmpty(oldSchema) 
						&& !StringHelper.isEmpty(oldPrimaryTable)) ||
						(count > 1) || hasFieldsToUnmap)
					{
						return I18NHelper.getMessage(getMessages(),
							"table.all_mapping_lost");				// NOI18N
					}
				}
				// removed secondary table(s) with fields mapped to it (them)
				// or caused a change to a schema in which the secondary tables
				// can't all be found
				else if (count > 0)
				{
					if (hasSchemaChange)
					{
						return I18NHelper.getMessage(getMessages(),
							"table.secondary_mapping_lost");	// NOI18N
					}
					else if (hasFieldsToUnmap)
					{
						return I18NHelper.getMessage(getMessages(), 
							((count == 1) ?
							"table.fields_mapping_lost_one" : // NOI18N
							"table.fields_mapping_lost_many")); // NOI18N
					}
				}
			}
		}

		return null;
	}

    public String prepareAttach(FieldHolderState oldState, 
        FieldHolderState state)
    {
        Iterator iterator = state.getFieldStates().iterator();

        while (iterator.hasNext())
        {
            FieldState fieldState = (FieldState)iterator.next();

            // the relationship will be unmapped if it's invalid
            if (fieldState instanceof RelationshipState)
            {
                if (fieldState.hasInvalidMapping())
                {
                    return I18NHelper.getMessage(getMessages(),
                        "table.relationship_mapping_lost"); // NOI18N
                }
            }
        }

        return null;
    }

	public static void attach (ClassState state) throws ModelException
	{
		if (state.hasTableState())
			attach(state.getTableState());

		if (state.hasFieldHolderState())
			attach(state.getFieldHolderState());
	}

	public static void attach (TableState state) throws ModelException
	{
		MappingClassElement lMappingClass = state.getMappingClassElement();

		if (lMappingClass != null)
		{
			String newName = state.getCurrentSchemaName();
			String oldName = state.getMappedSchemaName();
			TableElement newTable = state.getNewPrimaryTable();
			boolean oldWasNull = StringHelper.isEmpty(oldName);
			Iterator iterator = null;

			// removed secondary tables, if primary table changed, this will
			// contain all secondary tables (must do above removeAll first!)
			iterator = state.getDeletedSecondaryMappingTables().iterator();
			while (iterator.hasNext())
			{
				try
				{
					lMappingClass.removeTable(
						(MappingTableElement)iterator.next());
				}
				catch (ModelException e)
				{
					if (e instanceof ModelVetoException)
						throw e;

					// otherwise do nothing -- it was a table which is not
					// there but could have been added in a wizard session
				}
			}

			// handle any changes to the schema
			if (!StringHelper.isEmpty(newName))
			{
				if (oldWasNull || state.schemaChangedFrom(oldName))
					lMappingClass.setDatabaseRoot(state.getCurrentSchema());
			}
			else if (!oldWasNull && state.getExistingSchemaRemoved())
				lMappingClass.setDatabaseRoot(null);

			// now handle any changes to the primary table
			oldName = state.getMappedPrimaryTableName();
			oldWasNull = StringHelper.isEmpty(oldName);
			if (newTable != null)
			{
				if (oldWasNull || state.primaryTableChangedFrom(oldName))
				{
					if (!oldWasNull)
					{
						lMappingClass.removeTable(
							lMappingClass.getTable(oldName));
					}

					lMappingClass.setPrimaryTable(newTable);
				}
			}
			else if (!oldWasNull && state.getExistingPrimaryTableRemoved())
				lMappingClass.removeTable(lMappingClass.getTable(oldName));

			// added/modified secondary tables
			iterator = state.getSecondaryTableStates().iterator();
			while (iterator.hasNext())
				attach((SecondaryTableState)iterator.next());

			// consistency level
			lMappingClass.setConsistencyLevel(state.getConsistencyLevel());
		}
	}

	public static void attach (SecondaryTableState state) throws ModelException
	{
		TableState primaryState = state.getTableState();
		MappingClassElement mappingClass = state.getMappingClassElement();
		String secondaryTableName = state.getTableName();
		TableElement primaryTable = primaryState.getCurrentPrimaryTable();
		MappingReferenceKeyElement referenceKey =
			primaryState.findReferenceKey(primaryTable, secondaryTableName);

		if ((referenceKey == null) && state.getPairState().hasCompleteRows())
		{
			String absoluteTableName = NameUtil.getAbsoluteTableName(
				primaryState.getCurrentSchemaName(), secondaryTableName);
			TableElement secondaryTable = TableElement.forName(absoluteTableName);

			referenceKey = mappingClass.addSecondaryTable(
				mappingClass.getTable(primaryTable.toString()), secondaryTable);
		}

		if (referenceKey != null)
			referenceKey.setColumnPairs(state.getPairState().getColumnPairs());
	}

    public static void attach (FieldHolderState state) throws ModelException
    {
        try
        {
            MappingClassElement lMappingClass = state.getMappingClassElement();

            if ( lMappingClass != null )
            {
                TableState thisTableState = state.getTableState();
                Map extraClassStates = state.getExtraClassStates();
                Iterator iterator = extraClassStates.keySet().iterator();

                // related classes which have changed primary table
                while (iterator.hasNext())
                {
                    ClassState classState =
                        (ClassState)extraClassStates.get(iterator.next());
                    TableState extraState = (classState.hasTableState() ?
                        classState.getTableState() : null);

                    if ((extraState != null) && (thisTableState != extraState))
                        attach(extraState);
                }

                // columns and element classes
                iterator = state.getFieldStates().iterator();
                while (iterator.hasNext())
                {
                    Object lObj = iterator.next();

                    if (lObj instanceof RelationshipState)
                        attach((RelationshipState)lObj);
                    else if (lObj instanceof FieldState)
                        attach((FieldState)lObj);

                    // now do fetch groups
                    //setFetchGroup((FieldState)lObj);
                }

                // fetch groups
                Map fetchGroups = state.getFetchGroups();
                iterator = fetchGroups.keySet().iterator();
                while (iterator.hasNext())
                {
                    PersistenceFieldElement field =
                        (PersistenceFieldElement)iterator.next();

                    setFetchGroup(lMappingClass, field, 
                        (Integer)fetchGroups.get(field));
                }
            }
        }
        catch (DBException lDBError)
        {
            throw new ModelException(lDBError.getMessage());
        }
    }

    public static void attach (FieldState state) throws ModelException
    {
        MappingClassElement lMappingClass = state.getMappingClassElement();
        PersistenceFieldElement persistenceField = state.getField();
        String databaseRoot = lMappingClass.getDatabaseRoot();
        Iterator iterator = state.getColumns().iterator();
        List columnList = new ArrayList();

        // convert relative column names to actual column objects
        while (iterator.hasNext())
        {
            DBMemberElement column = getMember(NameUtil.
                getAbsoluteMemberName(databaseRoot, (String)iterator.next()));

            if (column != null)
                columnList.add(column);
        }

        attach(persistenceField, 
            (ColumnElement[])columnList.toArray(new ColumnElement[0]),
            lMappingClass.getField(persistenceField.getName()), lMappingClass);
    }

    private static void attach (RelationshipState state)
		throws DBException, ModelException
    {
        attach(state, true);
    }

    private static void attach (RelationshipState state, boolean followInverse)
		throws DBException, ModelException
    {
        MappingClassElement lMappingClass = state.getMappingClassElement();
        RelationshipElement field = (RelationshipElement)state.getField();
        String fieldName = field.getName();
        MappingRelationshipElement lMappingField =
            (MappingRelationshipElement)lMappingClass.getField(fieldName);
        RelationshipElement relatedField = state.getRelatedField();
        PairState pairState = state.getColumnPairState();
        ColumnPairElement[] columnPairs = null;
        Model model = state.getModel();
// if there and empty, need to unmap?

        pairState.removeDuplicatePairs();
        state.getAssociatedColumnPairState().removeDuplicatePairs();
        columnPairs = pairState.getColumnPairs();

        if (pairState.hasCompleteRows())
        {
            if (RelationshipState.isCollection(field, model))
            {
                String oldElementClass = field.getElementClass();
                String newElementClass = state.getRelatedClass();
                RelationshipElement oldInverse = 
                    field.getInverseRelationship(model);

                // if the element class changed and there used to be 
                // a related field, we need to take it out of the two 
                // way relationship before setting up the new stuff
                if ((oldInverse != null) && (oldElementClass != null) && 
                    !oldElementClass.equals(newElementClass))
                {
                    oldInverse.setInverseRelationship(null, model);
                }

                field.setElementClass(newElementClass);
            }

            field.setInverseRelationship(relatedField, model);
        }

        if (lMappingField != null)
            removeColumnMapping(lMappingField);
        else
        {
            lMappingField = new MappingRelationshipElementImpl(fieldName, lMappingClass);
            lMappingClass.addField(lMappingField);
        }

        if (!state.isJoinRelationship())
        {
            int i, count = ((columnPairs != null) ? columnPairs.length : 0);

            for (i = 0; i < count ; i++)
	            lMappingField.addColumn(columnPairs[i]);
        }
        else // join relationship
        {
            int i, count = ((columnPairs != null) ? columnPairs.length : 0);

            for (i = 0; i < count ; i++)
	            lMappingField.addLocalColumn(columnPairs[i]);

            // now handle associated column pairs
            pairState = state.getAssociatedColumnPairState();
            columnPairs = pairState.getColumnPairs();
            count = ((columnPairs != null) ? columnPairs.length : 0);
            for (i = 0; i < count ; i++)
	            lMappingField.addAssociatedColumn(columnPairs[i]);
        }
        if (followInverse && (relatedField != null))	// apply mapping to other side
        {
            FieldHolderState holderState = state.getFieldHolderState();
            FieldHolderState relatedFieldState =
                holderState.getRelatedState(field, false);

            if ((relatedFieldState != null) && 
                (relatedFieldState != holderState))
            {
                RelationshipState inverse = RelationshipState.
                	getInverseStateForState(relatedFieldState, state);

                // reset the inverse to the latest mapping
                if (inverse != null)
                {
                	relatedFieldState.replaceFieldState(relatedField, inverse);
                	attach(inverse, false);
                }
            }
        }
        else if (followInverse)		// apply related field of none even if unmapped
            field.setInverseRelationship(null, model);
    }

	private static void setFetchGroup (MappingClassElement mappingClass, 
		PersistenceFieldElement field, Integer fetchGroup) throws ModelException
//	private static void setFetchGroup (FieldState state) throws ModelException
	{
//		MappingClassElement mappingClass = state.getMappingClassElement();
//		PersistenceFieldElement field = state.getField();
		MappingFieldElement element = mappingClass.getField(field.getName());
//		Integer fetchGroup = state.getFetchGroup();

		if ((fetchGroup != null) && (element != null))
		{
			int oldFG = element.getFetchGroup();
			int newFG = fetchGroup.intValue();

			if (oldFG != newFG)
				element.setFetchGroup(newFG);
		}
	}

	private static void removeColumnMapping (MappingFieldElement element)
		throws ModelException
	{
		Collection columnsToRemove = new ArrayList(element.getColumns());

		if (element instanceof MappingRelationshipElement)
		{
			columnsToRemove.addAll(
				((MappingRelationshipElement)element).getAssociatedColumns());
		}

		// need to do in 2 steps so no concurrent mod errors
		if (!columnsToRemove.isEmpty())
		{
			Iterator iterator = columnsToRemove.iterator();

			while (iterator.hasNext())
				element.removeColumn((String)iterator.next());
		}
	}

	protected static ColumnPairElement getPairElement (ColumnElement local,
		ColumnElement foreign) throws DBException
	{
		ColumnPairElement pair = null;

		if ((local != null) && (foreign != null))
		{
			TableElement table = local.getDeclaringTable();

			pair = (ColumnPairElement)table.getMember(
				DBIdentifier.create(local.getName().getFullName() + ';'
				+ foreign.getName().getFullName()));
		}

		return pair;
	}

	private static DBMemberElement getMember (String columnName)
	{
		TableElement table =
			TableElement.forName(NameUtil.getTableName(columnName));

		return ((table != null) ?
			table.getMember(DBIdentifier.create(columnName)) : null);
	}
}
