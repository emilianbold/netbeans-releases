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
 * DevelopmentConversionHelper.java
 *
 * Created on October 29, 2004, 12:20 PM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.ejb;

import java.util.*;
import java.lang.reflect.Modifier;

import org.netbeans.modules.j2ee.dd.api.ejb.*;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.mapping.ejb.ConversionHelper;
import com.sun.jdo.api.persistence.mapping.ejb.AbstractNameMapper;
import com.sun.jdo.spi.persistence.utility.StringHelper;

/*
 * This class implements the ConversionHelper interface by using data from
 * EjbJar and other DDAPI classes.
 *
 * @author Rochelle Raccah
 */
public class DevelopmentConversionHelper implements ConversionHelper {
	private static final String COLLECTION = "java.util.Collection"; // NOI18N

	private final DevelopmentNameMapper nameMapper;
	private final EjbJar bundleDescriptor;
	private boolean generateFields = true;
	private boolean ensureValidation = true;
	private final HashMap entityMap = new HashMap();
	private final HashMap cmpFieldMap = new HashMap();
	private final HashMap ejbKeyMap = new HashMap();
	private final HashMap relationshipRoleMap = new HashMap();

	public DevelopmentConversionHelper(DevelopmentNameMapper nameMapper, 
			Model model) {
		this.nameMapper = nameMapper;
		bundleDescriptor = nameMapper.getBundleDescriptor();
		initMaps(model);
	}

	/** Gets the EjbJar which defines the universe of
	 * names for this application.
	 * @return the EjbJar which defines the universe of
	 * names for this application.
	 */
	private EjbJar getBundleDescriptor() {
		return bundleDescriptor;
	}

	private void initMaps(Model model) {
		// TODO: decide whether caching here is okay
		EnterpriseBeans allBeans = getBundleDescriptor().getEnterpriseBeans();
		Entity[] entityBeans = allBeans.getEntity();
		int i, count = ((entityBeans != null) ? entityBeans.length : 0);
		DevelopmentNameMapper mapper = getNameMapper();

		for (i = 0; i < count; i++) {
			Entity ejb = entityBeans[i];

			if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(
					ejb.getPersistenceType())) {
				String ejbName = ejb.getEjbName();

				// cache entity objects by name
				entityMap.put(ejbName, ejb);

				// cache cmp/cmr fields by ejb name
				cmpFieldMap.put(ejbName, mapper.getFieldsForEjb(ejbName));

				//collect all keys
				ejbKeyMap.put(ejbName, getPKFields(model, ejb));
			}
        }

		initRelationshipMap();
	}

	private void initRelationshipMap() {
		Relationships relationships = 
			getBundleDescriptor().getSingleRelationships(); 

		if (relationships != null) {
			EjbRelation[] rels = relationships.getEjbRelation();
			int i, count = ((rels != null) ? rels.length : 0);

			// cache relationship roles by ejb name
			for (i = 0; i < count; i++) {
				EjbRelation relationship = rels[i];

				addRole(relationship, relationship.getEjbRelationshipRole());
				addRole(relationship, relationship.getEjbRelationshipRole2());
			}
		}
	}

//TODO: is caching okay?  is synchronization needed?
	private void addRole(EjbRelation relationship, 
			EjbRelationshipRole testRole) {
		String ejbName = testRole.getRelationshipRoleSource().getEjbName();
		Map rels = (HashMap)relationshipRoleMap.get(ejbName);

		if (rels == null) {
			rels = new HashMap();
			relationshipRoleMap.put(ejbName, rels);
		}

		rels.put(relationship, testRole);
	}

	private Collection getPKFields(Model model, Entity ejb) {
		String pkeyField = ejb.getPrimkeyField();
		String primaryKeyClass = ejb.getPrimKeyClass();
		Set pkeyFields = new HashSet();

		if (!StringHelper.isEmpty(pkeyField)) // primkey-field was set
			pkeyFields.add(pkeyField);
		else if (!primaryKeyClass.equals("java.lang.Object")) {
			// get fields of primaryKeyClass
			Iterator iterator = model.getAllFields(primaryKeyClass).iterator();

			while (iterator.hasNext()) {
				// ignore static or final fields
				String fieldName = (String)iterator.next();
				int m = model.getModifiers(
					model.getField(primaryKeyClass, fieldName));

				if (Modifier.isStatic(m) || Modifier.isFinal(m))
					continue;
				pkeyFields.add(fieldName);
			}
		} else {
			// this is the case of unknown pk - use generated name
			pkeyFields.add(getGeneratedPKFieldName());
		}

		return pkeyFields;
	}

	private EjbRelationshipRole getOppositeRoleDescriptor(String ejbName, 
			String cmrFieldName) {
		return getRoleDescriptor(ejbName, cmrFieldName, true);
	}

	// TODO: can this be implemented in a simpler way?  is there
	// any real value to using the map here as opposed to iterating
	// the relationships live?
	private EjbRelationshipRole getRoleDescriptor(String ejbName, 
			String cmrFieldName, boolean opposite) {
		Map rels = (HashMap)relationshipRoleMap.get(ejbName);
		Iterator iterator = rels.keySet().iterator();

		while (iterator.hasNext()) {
			EjbRelation rel = (EjbRelation)iterator.next();
			EjbRelationshipRole role = (EjbRelationshipRole)rels.get(rel);
			EjbRelationshipRole role1 = rel.getEjbRelationshipRole();
			EjbRelationshipRole role2 = rel.getEjbRelationshipRole2();
			CmrField candidateCmr = role.getCmrField();

			if (ejbName.equals(role.getRelationshipRoleSource().getEjbName()) &&
					(candidateCmr != null) && 
					cmrFieldName.equals(candidateCmr.getCmrFieldName())) {
				return ((opposite) ? ((role == role1) ? role2 : role1) : role);
			} else { 	// check for self ref relationship
				String ejb1 = role1.getRelationshipRoleSource().getEjbName();

				if (ejb1.equals(role2.getRelationshipRoleSource().getEjbName()) 
						&& ejbName.equals(ejb1)) {
					role = ((role == role1) ? role2 : role1);
					candidateCmr = role.getCmrField();
					
					if ((candidateCmr != null) && cmrFieldName.equals(
							candidateCmr.getCmrFieldName())) {
						return ((opposite) ? 
							((role == role1) ? role2 : role1) : role);
					}
				}
			}
		}
		throw new IllegalArgumentException();
	}


	protected DevelopmentNameMapper getNameMapper() {
		return nameMapper;
	}

    public String getMappedClassName(String ejbName) {
        return getNameMapper().getPersistenceClassForEjbName(ejbName);
    }

	/** 
	 * If {@link #generateFields} is <code>true</code>, then this method will 
	 * check if the field is one of the cmp + cmr + pseudo cmr fields, otherwise
	 * the method will check if the field is one of the cmp + cmr fields.
	 * @param ejbName The ejb-name element for the bean
	 * @param fieldName The name of a container managed field in the named bean 
	 * @return <code>true</code> if the bean contains the field, otherwise
	 * return <code>false</code> 
	 */
	public boolean hasField(String ejbName, String fieldName) {
		if (!generateFields() && isGeneratedRelationship(ejbName, fieldName))
			return false;
		else {
			List fields = (List)cmpFieldMap.get(ejbName);

			return ((fields != null) ? fields.contains(fieldName) : false);
		}
	}

	/** 
	 * If {@link #generateFields} is <code>true</code>, then this method will 
	 * return an array of cmp + cmr + pseudo cmr fields, otherwise 
	 * the method will return an array of cmp + cmr fields.
	 * @param ejbName The ejb-name element for the bean
	 * @param fieldName The name of a container managed field in the named bean 
	 * @return an array of fields in the ejb bean 
	 */
	public Object[] getFields(String ejbName) {
		List fields = (List)cmpFieldMap.get(ejbName);

		if (fields != null) {
			if (!generateFields())
				fields.removeAll(getGeneratedRelationships(ejbName));

			return fields.toArray();
		}

		return null;
	}

	/**
	 * The boolean argument candidate is ignored in this case.
	 */
	public boolean isKey(String ejbName, String fieldName, boolean candidate) {
		Collection keyFields = (Collection)ejbKeyMap.get(ejbName);
		return ((keyFields != null) ? (keyFields.contains(fieldName)) : false);
	}

	/**
	 * This API will only be called from MappingFile when multiplicity is Many
	 * on the other role.
	 */
	public String getRelationshipFieldType(String ejbName, String fieldName) {
		if (isGeneratedRelationship(ejbName, fieldName))
			return COLLECTION;
		else {
			// TODO: protect against NPE? test for one way rels
			EjbRelationshipRole thisRole = 
				getRoleDescriptor(ejbName, fieldName, false);
			String returnType = thisRole.getCmrField().getCmrFieldType();

			// CmrField's info on type says it is only populated for 
			// collection fields, so get the related field content for null
			if ((returnType == null) || !returnType.equals(COLLECTION)) {
				// TODO: DOL impl returns the local interface name, but not 
				// sure why it isn't just the related bean (may not matter due
				// to comment about Many multiplicity above)
				returnType = getNameMapper().getLocalInterfaceForEjbName(
					getRelationshipFieldContent(ejbName, fieldName));
			}

			return returnType;
		}
	}

	/**
	 * getMultiplicity of the other role on the relationship
	 * Please note that multiplicity is JDO style
	 */
	public String getMultiplicity(String ejbName, String fieldName) {
		EjbRelationshipRole oppRole = 
			getOppositeRoleDescriptor(ejbName, fieldName);

		return oppRole.getMultiplicity();
	}

	public String getRelationshipFieldContent(String ejbName, 
			String fieldName) {
		EjbRelationshipRole oppRole = 
			getOppositeRoleDescriptor(ejbName, fieldName);

		return oppRole.getRelationshipRoleSource().getEjbName();
	}

	/**
	 * This method return the fieldName of relation role on the other end.
	 */
	public String getInverseFieldName(String ejbName, String fieldName) {
		EjbRelationshipRole oppRole = 
			getOppositeRoleDescriptor(ejbName, fieldName);
		CmrField inverse = ((oppRole == null) ? null : oppRole.getCmrField());
		String inverseName = 
			((inverse == null) ? null : inverse.getCmrFieldName());

		// if we are generating relationships, check for a generated inverse
		if (generateFields() && (inverseName == null)) {
			inverseName = getNameMapper().getGeneratedFieldForEjbField(
				ejbName, fieldName)[1];
		}

		return inverseName;
	}

	/**
	 * Returns flag whether the mapping conversion should apply the default
	 * strategy for dealing with unknown primary key classes. This method will
	 * only be called when {@link #generateFields} returns <code>true</code>.
	 * @param ejbName The value of the ejb-name element for a bean.
	 * @return <code>true</code> to apply the default unknown PK Class Strategy,
	 * <code>false</code> otherwise
	 */
	public boolean applyDefaultUnknownPKClassStrategy(String ejbName) {
		return (AbstractNameMapper.UNKNOWN_KEY_CLASS == 
			getNameMapper().getKeyClassTypeForEjbName(ejbName));
	}

	/**
	 * Returns the name used for generated primary key fields.
	 * @return a string for key field name
	 */
	public String getGeneratedPKFieldName() {
		return AbstractNameMapper.GENERATED_KEY_FIELD_NAME;
	}

	/**
	 * Returns the prefix used for generated version fields.
	 * @return a string for version field name prefix
	 */
	public String getGeneratedVersionFieldNamePrefix() {
		return AbstractNameMapper.GENERATED_VERSION_FIELD_PREFIX;
	}

	public boolean relatedObjectsAreDeleted(String beanName, String fieldName) {
		EjbRelationshipRole oppRole = 
			getOppositeRoleDescriptor(beanName, fieldName);

		return oppRole.isCascadeDelete();
	}

	/**
	 * Returns the flag whether the mapping conversion should generate
	 * relationship fields and primary key fields to support run-time.
	 * The version field is always created even {@link #generateFields} is 
	 * <code>false</code> because it holds version column information.
	 * @return <code>true</code> to generate fields in the dot-mapping file
	 * (if they are not present).
	 */
	public boolean generateFields() {
		return generateFields;
	}

	/**
	 * Sets the flag whether the mapping conversion should generate relationship
	 * fields, primary key fields, and version fields to support run-time.
	 * @param flag a flag which indicates whether fields should be generated
	 */
	public void setGenerateFields(boolean flag) {
		generateFields = flag;
	}

	/** Returns the flag whether the mapping conversion should validate
	 * all fields against schema columns.
	 * @return <code>true</code> to validate all the fields in the dot-mapping
	 * file.
	 */
	public boolean ensureValidation() {
		return ensureValidation;
	}

	/**
	 * Sets the flag whether the mapping conversion should validate all fields
	 * against schema columns.
	 * @param flag a boolean indicating whether fields will be validated or not
	 */
	public void setEnsureValidation(boolean flag) {
		ensureValidation = flag;
	}

	/**
	 * Returns <code>true</code> if the field is generated. There are three
	 * types of generated fields: generated relationships, unknown primary key
	 * fields, and version consistency fields.
	 * @param ejbName The ejb-name element for the bean
	 * @param fieldName The name of a container managed field in the named bean 
	 * @return <code>true</code> if the field is generated; <code>false</code>
	 * otherwise.
	 */
	public boolean isGeneratedField(String ejbName, String fieldName) {
		return getNameMapper().isGeneratedField(ejbName, fieldName);
	}

	public boolean isGeneratedRelationship(String ejbName, String fieldName) {
		return getNameMapper().isGeneratedEjbRelationship(ejbName, fieldName);
	}

	/**
	 * Returns a list of generated relationship field names.
	 * @param ejbName The ejb-name element for the bean
	 * @return a list of generated relationship field names
	 */
	public List getGeneratedRelationships(String ejbName) {
		return getNameMapper().getGeneratedRelationshipsForEjbName(ejbName);
	}
}
