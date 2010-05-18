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
 * MappingConverter.java
 *
 * Created on December 21, 2004, 11:51 AM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.ejb.util;

import java.util.*;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.NameUtil;

import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.mapping.ejb.*;
import com.sun.jdo.api.persistence.mapping.ejb.beans.*;

/** This is a class which helps convert OriginalCMPMapping mapping to 
 * SunCmpMappings and MappingClassElement objects.
 *
 * @author Rochelle Raccah
 */
public class MappingConverter {
	private EJBInfoHelper ejbInfoHelper;
	private SourceFileMap sourceFileMap;

	/** Creates a new instance of MappingConverter
	 * @param ejbInfoHelper the EJBInfoHelper which helps access helper infoo.
	 * @param sourceFileMap the SourceFileMap which helps look up
	 * source roots in the project.
	 */
	public MappingConverter(EJBInfoHelper ejbInfoHelper, 
			SourceFileMap sourceFileMap) {
		this.ejbInfoHelper = ejbInfoHelper;
		this.sourceFileMap = sourceFileMap;
	}

	public MappingClassElement toMappingClass(String ejbName) 
			throws ModelException, DBException, ConversionException {
		SunCmpMappings mappings = SunOneUtilsCMP.prepare(null);
		SunCmpMapping mapping = 
			SunCmpMappingsUtils.getFirstSunCmpMapping(mappings, false);
		EntityMapping em = 
			SunCmpMappingsUtils.findEntityMapping(mappings, ejbName, true);
		Collection fields = ejbInfoHelper.getFieldsForEjb(ejbName);
		Collection rels = ejbInfoHelper.getRelationshipsForEjb(ejbName);
		Iterator iterator = null;

		// fields
		fields.removeAll(rels);
		iterator = fields.iterator();
		while (iterator.hasNext()) {
			String fieldName = (String)iterator.next();
			SunCmpMappingsUtils.findCmpFieldMapping(em, fieldName, true);
		}

		// relationships
		iterator = rels.iterator();
		while (iterator.hasNext()) {
			String fieldName = (String)iterator.next();
			SunCmpMappingsUtils.findCmrFieldMapping(em, fieldName, true);
		}

		iterator = SunOneUtilsCMP.getMappingClasses(
			mappings, ejbInfoHelper).values().iterator();

		return (MappingClassElement)iterator.next();
	} 

	public Collection toMappingClasses(OriginalCMPMapping[] cmpMappings) 
			throws ModelException, DBException, ConversionException {
		SunCmpMappings mappings = SunOneUtilsCMP.prepare(null);
		SunCmpMapping mapping = 
			SunCmpMappingsUtils.getFirstSunCmpMapping(mappings, false);
		SchemaElement schemaObject = null;
		String schemaName = null;

		for (int i = 0; i < cmpMappings.length; i++) {
			OriginalCMPMapping nextMapping = cmpMappings[i];
			String ejbName = nextMapping.getEjbName();
			EntityMapping em = 
				SunCmpMappingsUtils.findEntityMapping(mappings, ejbName, true);
			String tableName = nextMapping.getTableName();
			Collection fields = ejbInfoHelper.getFieldsForEjb(ejbName);
			Collection rels = ejbInfoHelper.getRelationshipsForEjb(ejbName);
			Iterator iterator = null;

			// there should be just one
			if (schemaName == null) {
				schemaName = getSchema(nextMapping);
				schemaObject = ejbInfoHelper.getSchema(schemaName);
				mapping.setSchema(schemaName);
			}

			em.setTableName(tableName);

			// fields
			fields.removeAll(rels);
			iterator = fields.iterator();
			while (iterator.hasNext()) {
				String fieldName = (String)iterator.next();
				CmpFieldMapping field = SunCmpMappingsUtils.
					findCmpFieldMapping(em, fieldName, true);
				String columnName = nextMapping.getFieldColumn(fieldName);

				if (columnName != null) // TO: use NameUtil instead of '.' here?
					field.addColumnName(tableName + '.' + columnName);
			}

			// relationships
			mapRelationships(nextMapping, mappings, em, schemaName, 
				tableName, rels);
		}

		// TODO: log this at fine? level
		//mappings.dumpXml();

		return SunOneUtilsCMP.getMappingClasses(
			mappings, ejbInfoHelper).values();
	}

	private String getSchema(OriginalCMPMapping cmpMapping) {
		String schemaName = sourceFileMap.getDistributionPath(
			cmpMapping.getSchema()).getPath();
		return SunOneUtilsCMP.removeSchemaFileNameExtension(schemaName);
	}

	private void mapRelationships(OriginalCMPMapping cmpMapping, 
			SunCmpMappings mappings, EntityMapping em, String schema, 
			String tableName, Collection rels) {
		ConversionHelper conversionHelper = 
			ejbInfoHelper.createConversionHelper();
		TableElement thisTable = TableElement.forName(
			NameUtil.getAbsoluteTableName(schema, tableName));
		Iterator iterator = rels.iterator();

		conversionHelper.setEnsureValidation(false);
		conversionHelper.setGenerateFields(false);
		while (iterator.hasNext()) {
			String fieldName = (String)iterator.next();
			CmrFieldMapping field = 
				SunCmpMappingsUtils.findCmrFieldMapping(em, fieldName, true);
			String joinTableName = 
				cmpMapping.getRelationshipJoinTable(fieldName);

			if (joinTableName != null) { // M:N rel
				TableElement joinTable = TableElement.forName(
					NameUtil.getAbsoluteTableName(schema, joinTableName));
				ForeignKeyElement fks[] = joinTable.getForeignKeys();

				// join table should have exactly 2 fks
				if ((fks != null) && (fks.length == 2)) {
					ForeignKeyElement testKey = fks[0];
					TableElement testTable = testKey.getReferencedTable();
					ColumnPair[] pairset1 = null;
					ColumnPair[] pairset2 = null;

					if (testTable.equals(thisTable))
						pairset1 = getColumnPairs(testKey);
					else
						pairset2 = getColumnPairs(testKey);
					
					testKey = fks[1];
					testTable = testKey.getReferencedTable();
					if (testTable.equals(thisTable))
						pairset1 = getColumnPairs(testKey);
					else
						pairset2 = getColumnPairs(testKey);

					pairset1 = swapPairs(pairset1);
					for (int p = 0; p < pairset1.length; p++)
						field.addColumnPair(pairset1[p]);
					for (int p = 0; p < pairset2.length; p++)
						field.addColumnPair(pairset2[p]);
				}
 			}
			else {	 // 1:N or 1:1
				String[] columnNames = 
					cmpMapping.getRelationshipColumn(fieldName);

				if (columnNames != null){
					ForeignKeyElement fk = 
						getMatchingFK(columnNames, thisTable);

					if (fk != null) {
						ColumnPair[] pairs = getColumnPairs(fk);

						field.setColumnPair(pairs);
						mapInverse(conversionHelper, mappings, 
							cmpMapping.getEjbName(), fieldName, pairs);
					}
				}
			}
		}
	}

	private ColumnPair[] getColumnPairs(ForeignKeyElement foreignKey) {
		ColumnPairElement[] cpes = foreignKey.getColumnPairs();
		int count = cpes.length;
		ColumnPair[] pairs = new ColumnPair[count];

		for (int i = 0; i < count; i++) {
			ColumnPairElement cpe = cpes[i];
			ColumnPair pair = new ColumnPair();

			pair.addColumnName(NameUtil.getRelativeMemberName(
				cpe.getLocalColumn().getName().getFullName()));
			pair.addColumnName(NameUtil.getRelativeMemberName(
				cpe.getReferencedColumn().getName().getFullName()));
			pairs[i] = pair;
		}

		return pairs;
	}

	private ColumnPair[] swapPairs(ColumnPair[] pairs) {
		int count = pairs.length;
		ColumnPair[] swappedPairs = new ColumnPair[count];

		for (int i = 0; i < count; i++) {
			ColumnPair pair = pairs[i];
			String[] columns = pair.getColumnName();
			ColumnPair swappedPair = new ColumnPair();

			swappedPair.setColumnName(new String[]{columns[1], columns[0]});
			swappedPairs[i] = swappedPair;
		}

		return swappedPairs;
	}

	private ForeignKeyElement getMatchingFK (String[] columnNames, 
		TableElement table)
	{
		ForeignKeyElement[] foreignKeys = (table != null) ? 
			table.getForeignKeys() : null;
		int count = ((foreignKeys != null) ? foreignKeys.length : 0);

		for (int i = 0; i < count; i++)
		{
			if (matchesFK(columnNames, foreignKeys[i]))
				return foreignKeys[i];
		}

		return null;
	}

	private boolean matchesFK (String[] columnNames, 
		ForeignKeyElement foreignKey)
	{
		ColumnElement[] localColumns = foreignKey.getLocalColumns();
		int fkCount = ((localColumns != null) ? localColumns.length : 0);
		int count = ((columnNames != null) ? columnNames.length : 0);

		// First check whether the list of fk columns has the 
		// same size than the specified list of columns.
		if (fkCount == count) 
		{
			List columnList = Arrays.asList(columnNames);

			// Now check whether each fk column is included in the
			// specified list of columns.
			for (int i = 0; i < fkCount; i++)
			{
				if (!columnList.contains(localColumns[i].getName().getName()))
					return false;
			}

			return true;
		}

		return false;
	}

	private void mapInverse(ConversionHelper conversionHelper, 
			SunCmpMappings mappings, String ejbName, String fieldName, 
			ColumnPair[] pairs) {
		String relatedEjb = 
			conversionHelper.getRelationshipFieldContent(ejbName, fieldName);
		String relatedField = 
			conversionHelper.getInverseFieldName(ejbName, fieldName);

		if (relatedField != null) {
			EntityMapping relatedEm = SunCmpMappingsUtils.findEntityMapping(
				mappings, relatedEjb, true);

			if (relatedEm != null) {
				CmrFieldMapping inverseField = SunCmpMappingsUtils.
					findCmrFieldMapping(relatedEm, relatedField, true);

				inverseField.setColumnPair(swapPairs(pairs));
			}
		}
	}
}
