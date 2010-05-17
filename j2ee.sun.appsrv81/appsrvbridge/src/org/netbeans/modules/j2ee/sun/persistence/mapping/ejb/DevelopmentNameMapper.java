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
 * DevelopmentNameMapper.java
 *
 * Created on October 15, 2004, 9:51 AM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.ejb;

import java.util.*;

import com.sun.jdo.api.persistence.mapping.ejb.AbstractNameMapper;
import com.sun.jdo.spi.persistence.utility.StringHelper;
import com.sun.jdo.spi.persistence.utility.JavaTypeHelper;

import org.netbeans.modules.j2ee.dd.api.ejb.*;

/** This is a class which helps translate between the various names of the 
 * CMP (ejb name, abstract schema, abstract bean, concrete bean, local
 * interface, remote interface) and the persistence-capable class name.  It 
 * also has methods for translation of field names.  The basic entry point 
 * is ejb name or persistence-capable class name.
 *
 * @author Rochelle Raccah
 */
public class DevelopmentNameMapper extends AbstractNameMapper {
	private final EjbJar bundleDescriptor;
	private final Map generatedRelToInverseRelMap;
	private final Map relToInverseGeneratedRelMap;
	private final Map nameToPcClassNameMap;

	/** Creates a new instance of NameMapper
	 * @param bundleDescriptor the EjbJar which defines the 
	 * universe of names for this application.
	 */
	protected DevelopmentNameMapper(EjbJar bundleDescriptor) {
		this.bundleDescriptor = bundleDescriptor;

		generatedRelToInverseRelMap = new HashMap();
		relToInverseGeneratedRelMap = new HashMap();
		nameToPcClassNameMap = new HashMap();
		initGeneratedRelationshipMaps();

		EnterpriseBeans allBeans = 
			getBundleDescriptor().getEnterpriseBeans();
		Entity[] entityBeans = allBeans.getEntity();
		int i, count = ((entityBeans != null) ? entityBeans.length : 0);

		for (i = 0; i < count; i++) {
			Entity testEntity = entityBeans[i];
			boolean isEntity = 
				Entity.PERSISTENCE_TYPE_CONTAINER.equals(
					testEntity.getPersistenceType());

			if (isEntity) {
				String testName = testEntity.getEjbName();

				nameToPcClassNameMap.put(testName, 
					getPersistenceClassForAbstractBean(
					testEntity.getEjbClass(), testName));
			}
		}

	}

	private void initGeneratedRelationshipMaps() {
		EjbJar descriptor = getBundleDescriptor();
		Relationships relationships = descriptor.getSingleRelationships(); 

		// during development time this code may attempt to get the 
		// iterator even with no relationships, so protect it by a 
		// null check
		if (relationships != null) {
			EjbRelation[] rels = relationships.getEjbRelation();
			List generatedRels = new ArrayList();
			int counter = 0;

			// gather list of generated cmr fields by examining source and sink
			for (int i = 0; i < rels.length; i++) {
				EjbRelation relationship = rels[i];

				if (relationship.getEjbRelationshipRole().getCmrField() == null)
					generatedRels.add(relationship);

				if (relationship.getEjbRelationshipRole2().getCmrField() == null)
					generatedRels.add(relationship);
			}

			// now update the maps to contain this info
			Iterator iterator = generatedRels.iterator();
			while (iterator.hasNext()) {
				EjbRelation relationship = (EjbRelation)iterator.next();
				EjbRelationshipRole source = 
					relationship.getEjbRelationshipRole();
				String sourceEjbName = source.getEjbRelationshipRoleName();
				CmrField sourceCMRField = source.getCmrField();
				boolean sourceIsNull = (sourceCMRField == null);
				EjbRelationshipRole sink = 
					relationship.getEjbRelationshipRole2();
				String sinkEjbName = sink.getEjbRelationshipRoleName();
				String ejbName = (sourceIsNull ? sourceEjbName : sinkEjbName);
				String otherEjbName = 
					(sourceIsNull ? sinkEjbName : sourceEjbName);
				List ejbField = Arrays.asList(new String[]{otherEjbName, 
					(sourceIsNull ? sink.getCmrField().getCmrFieldName() : 
					sourceCMRField.getCmrFieldName())});
				List generatedField = null;
				String uniqueName = null;

				if (ejbName != null) {
					// make sure the user doesn't already have a field
					// with this name
					do {
						counter++;
						uniqueName = GENERATED_CMR_FIELD_PREFIX + counter;
					} while (hasField(ejbName, uniqueName));

					generatedField = 
						Arrays.asList(new String[]{ejbName, uniqueName});
					generatedRelToInverseRelMap.put(generatedField, ejbField);
					relToInverseGeneratedRelMap.put(ejbField, generatedField);
				}
			}
		}
	}

	protected Map getGeneratedFieldsMap() {
		return generatedRelToInverseRelMap;
	}

	protected Map getInverseFieldsMap() {
		return relToInverseGeneratedRelMap;
	}

	private boolean hasField(String ejbName, String fieldName) {
		List fields = getFieldsForEjb(ejbName);

		// TODO: not sure if 2nd call to gen relationships is already populated
		// yet, so this might be the wrong answer
		return (fields.contains(fieldName) || 
			getGeneratedRelationshipsForEjbName(ejbName).contains(fieldName));
	}

	/** Gets the EjbJar which defines the universe of
	 * names for this application.
	 * @return the EjbJar which defines the universe of
	 * names for this application.
	 */
	EjbJar getBundleDescriptor() {
		return bundleDescriptor;
	}

	/** Determines if the specified name represents an ejb.
	 * @param name the fully qualified name to be checked
	 * @return <code>true</code> if this name represents an ejb; 
	 * <code>false</code> otherwise.
	 */
	public boolean isEjbName(String name) {
		return ((name != null) ? 
			(getDescriptorForEjbName(name) != null) : false);
	}

	/** Gets the Entity which represents the ejb  
	 * with the specified name.
	 * @param name the name of the ejb
	 * @return the Entity which represents the ejb.
	 */
	Entity getDescriptorForEjbName(String name) {
		if (name != null) {
			EnterpriseBeans allBeans = 
				getBundleDescriptor().getEnterpriseBeans();
			Entity[] entityBeans = allBeans.getEntity();
			int i, count = ((entityBeans != null) ? entityBeans.length : 0);

			for (i = 0; i < count; i++) {
				Entity testEntity = entityBeans[i];

				if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(
						testEntity.getPersistenceType()) && 
						name.equals(testEntity.getEjbName())) {
					return testEntity;
				}
			}
		}

		return null;
	}

	private Entity getRelatedEjbDescriptor(String ejbName, 
			String ejbFieldName) {
		Relationships relationships = 
			getBundleDescriptor().getSingleRelationships(); 

		if ((relationships != null) && (ejbName != null)) {
			EjbRelation[] rels = relationships.getEjbRelation();
			int i, count = ((rels != null) ? rels.length : 0);

			for (i = 0; i < count; i++) {
				EjbRelation relationship = rels[i];
				String cmrField = getCMRField(
					relationship.getEjbRelationshipRole2(), ejbName);
				boolean hasCMRField = (cmrField != null);

				if (hasMatchingCMRField(cmrField, ejbFieldName)) {
					return getDescriptorForEjbName(
						relationship.getEjbRelationshipRole().
						getRelationshipRoleSource().getEjbName());
				} else if (!hasCMRField) { // try the other role
					cmrField = getCMRField(
						relationship.getEjbRelationshipRole(), ejbName);

					if (hasMatchingCMRField(cmrField, ejbFieldName)) {
						return getDescriptorForEjbName(
							relationship.getEjbRelationshipRole2().
							getRelationshipRoleSource().getEjbName());
					}
				} else if (hasCMRField && // check for self ref relationship
						ejbName.equals(relationship.getEjbRelationshipRole().
						getRelationshipRoleSource().getEjbName())) { 
					cmrField = getCMRField(
						relationship.getEjbRelationshipRole(), ejbName);

					if (hasMatchingCMRField(cmrField, ejbFieldName)) {
						return getDescriptorForEjbName(
							relationship.getEjbRelationshipRole2().
							getRelationshipRoleSource().getEjbName());
					}
				}
			}
		}

		return null;
	}

	protected List getFieldsForEjb(String ejbName) {
		Entity entity = getDescriptorForEjbName(ejbName);
		List returnList = new ArrayList();

		if (entity != null)	{	// need to get names of ejb fields
			CmpField[] fields = entity.getCmpField();
			int i, count = ((fields != null) ? fields.length : 0);

			// first add fields
			for (i = 0; i < count; i++)
				returnList.add(fields[i].getFieldName());
		}

		// now add relationship fields
		returnList.addAll(getRelationshipFieldsForEjb(ejbName));

		return returnList;
	}

	protected List getRelationshipFieldsForEjb(String ejbName) {
		// TODO: issue of usage of this - several iterations of this if
		// iterating all the bean - but, I think it can change, so can't 
		// cache it in a map.  Actually, can probably cache a lot more and 
		// listen to EjbJar and subelement property changes
		Relationships relationships = 
			getBundleDescriptor().getSingleRelationships(); 
		ArrayList returnList = new ArrayList();

		if ((relationships != null) && (ejbName != null)) {
			EjbRelation[] rels = relationships.getEjbRelation();
			int i, count = ((rels != null) ? rels.length : 0);

			// gather list of cmr fields by examining the 2 roles
			for (i = 0; i < count; i++) {
				EjbRelation relationship = rels[i];
				EjbRelationshipRole testRole = 
					relationship.getEjbRelationshipRole();
				String cmrField = getCMRField(testRole, ejbName);

				if (cmrField != null)
					returnList.add(cmrField);

				testRole = relationship.getEjbRelationshipRole2();
				cmrField = getCMRField(testRole, ejbName);
				if (cmrField != null)
					returnList.add(cmrField);
			}
		}

		return returnList;
	}

	private String getCMRField(EjbRelationshipRole testRole, String ejbName) {
		String returnValue = null;

		if (ejbName.equals(testRole.getRelationshipRoleSource().getEjbName())) {
			CmrField cmrField = testRole.getCmrField();

			if (cmrField != null)
				returnValue = cmrField.getCmrFieldName();
		}

		return returnValue;
	}

	private boolean hasMatchingCMRField(String candidateCMR, String fieldName) {
		return ((candidateCMR != null) && candidateCMR.equals(fieldName));

	}

	/** Gets the name of the abstract bean class which corresponds to the 
	 * specified ejb name.
	 * @param name the name of the ejb
	 * @return the name of the abstract bean for the specified ejb
	 */
	public String getAbstractBeanClassForEjbName(String name) {
		Entity entity = getDescriptorForEjbName(name);

		return ((entity != null) ? entity.getEjbClass() : null);
	}

	/** Gets the name of the key class which corresponds to the specified 
	 * ejb name.
	 * @param name the name of the ejb
	 * @return the name of the key class for the ejb
	 */
	public String getKeyClassForEjbName(String name) {
		Entity entity = getDescriptorForEjbName(name);

		return ((entity != null) ? entity.getPrimKeyClass() : null);
	}

	/** Get the type of key class of this ejb.
	 * @return the key class type, one of {@link #USER_DEFINED_KEY_CLASS}, 
	 * {@link #PRIMARY_KEY_FIELD}, or {@link #UNKNOWN_KEY_CLASS}
	 */
	public int getKeyClassTypeForEjbName(String name) {
		String keyClass = getKeyClassForEjbName(name);

		if (!"java.lang.Object".equals(keyClass)) {		// NOI18N
			Entity descriptor = getDescriptorForEjbName(name);

			return ((descriptor.getPrimkeyField() != null) ?
				PRIMARY_KEY_FIELD : USER_DEFINED_KEY_CLASS);
		}

		return UNKNOWN_KEY_CLASS;
	}

	/** Gets the name of the abstract schema which corresponds to the 
	 * specified ejb.
	 * @param name the name of the ejb
	 * @return the name of the abstract schema for the specified ejb
	 */
	public String getAbstractSchemaForEjbName(String name) {
		Entity entity = getDescriptorForEjbName(name);

		return ((entity != null) ? entity.getAbstractSchemaName() : null);
	}

	/** Gets the name of the ejb name which corresponds to the 
	 * specified persistence-capable class name.
	 * @param className the name of the persistence-capable
	 * @return the name of the ejb for the specified persistence-capable
	 */
	public String getEjbNameForPersistenceClass(String className) {
		// TODO: this assumes expand always false, need to implement for 
		// the case of expand true
		String candidateName = JavaTypeHelper.getShortClassName(className);
		String packageName = JavaTypeHelper.getPackageName(className);

		if ((className != null) && (candidateName != null) &&
			(packageName != null))
		{
			EnterpriseBeans allBeans = 
				getBundleDescriptor().getEnterpriseBeans();
			Entity[] entityBeans = allBeans.getEntity();
			int i, count = ((entityBeans != null) ? entityBeans.length : 0);

			for (i = 0; i < count; i++) {
				Entity testEntity = entityBeans[i];

				if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(
						testEntity.getPersistenceType()) && 
						candidateName.equals(testEntity.getEjbName()) &&
						packageName.equals(JavaTypeHelper.getPackageName(
						testEntity.getEjbClass()))) {
					return testEntity.getEjbName();
				}
			}
		}

		return null;
	}

	/** Gets the name of the persistence-capable class which corresponds to 
	 * the specified ejb name.
	 * @param name the name of the ejb
	 * @return the name of the persistence-capable for the specified ejb
	 */
	public String getPersistenceClassForEjbName(String name) {
		// TODO: need to implement for the case expand is true
		// this needs to be cached so we can properly delete it when the 
		// bean is deleted
		String cachedName = (String)nameToPcClassNameMap.get(name);
	
		return (cachedName != null) ? cachedName : 
			getPersistenceClassForAbstractBean(
				getAbstractBeanClassForEjbName(name), name);
	}

	private String getPersistenceClassForAbstractBean(String abstractBean,
			String name) {
		// use the package name, keep the ejb name
		if (abstractBean != null) {
			// TODO: need to handle case of no package - dummy package needed?
			return JavaTypeHelper.getPackageName(abstractBean) + 
				'.' + name;
		}

		return null;
	}

	/** Determines if the specified name represents a local interface.
	 * @param name the fully qualified name to be checked
	 * @return <code>true</code> if this name represents a local interface; 
	 * <code>false</code> otherwise.
	 */
	public boolean isLocalInterface(String name) {
		if (name != null) {
			EnterpriseBeans allBeans = 
				getBundleDescriptor().getEnterpriseBeans();
			Entity[] entityBeans = allBeans.getEntity();
			int i, count = ((entityBeans != null) ? entityBeans.length : 0);

			for (i = 0; i < count; i++) {
				Entity testEntity = entityBeans[i];

				if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(
						testEntity.getPersistenceType()) && 
						name.equals(testEntity.getLocal())) {
					return true;
				}
			}
		}

		return false;
	}

	/** Gets the name of the ejb which corresponds to the specified 
	 * local interface name.
	 * @param ejbName the name of the ejb which contains fieldName 
	 * from which to find relationship and therefore the local interface
	 * @param fieldName the name of the field in the ejb
	 * @param interfaceName the name of the local interface
	 * @return the name of the ejb for the specified local interface
	 */
	public String getEjbNameForLocalInterface(String ejbName, 
		String fieldName, String interfaceName) {
		Entity descriptor = getRelatedEjbDescriptor(ejbName, fieldName);

		return (((descriptor != null) && !StringHelper.isEmpty(interfaceName)
			&& interfaceName.equals(descriptor.getLocal())) ? 
			descriptor.getEjbName() : null);
	}

	/** Gets the name of the local interface which corresponds to the 
	 * specified ejb name.
	 * @param name the name of the ejb
	 * @return the name of the local interface for the specified ejb
	 */
	public String getLocalInterfaceForEjbName(String name) {
		Entity entity = getDescriptorForEjbName(name);

		return ((entity != null) ? entity.getLocal() : null);
	}

	/** Determines if the specified name represents a remote interface.
	 * @param name the fully qualified name to be checked
	 * @return <code>true</code> if this name represents a remote interface; 
	 * <code>false</code> otherwise.
	 */
	public boolean isRemoteInterface(String name) {
		if (name != null) {
			EnterpriseBeans allBeans = 
				getBundleDescriptor().getEnterpriseBeans();
			Entity[] entityBeans = allBeans.getEntity();
			int i, count = ((entityBeans != null) ? entityBeans.length : 0);

			for (i = 0; i < count; i++) {
				Entity testEntity = entityBeans[i];

				if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(
						testEntity.getPersistenceType()) && 
						name.equals(testEntity.getRemote())) {
					return true;
				}
			}
		}

		return false;
	}

	/** Gets the name of the ejb which corresponds to the specified 
	 * remote interface name.
	 * @param ejbName the name of the ejb which contains fieldName 
	 * from which to find relationship and therefore the remote interface
	 * @param fieldName the name of the field in the ejb
	 * @param interfaceName the name of the remote interface
	 * @return the name of the ejb for the specified remote interface
	 */
	public String getEjbNameForRemoteInterface(String ejbName, 
		String fieldName, String interfaceName) {
		Entity descriptor = getRelatedEjbDescriptor(ejbName, fieldName);

		return (((descriptor != null) && !StringHelper.isEmpty(interfaceName)
			&& interfaceName.equals(descriptor.getRemote())) ? 
			descriptor.getEjbName() : null);
	}

	/** Gets the name of the remote interface which corresponds to the 
	 * specified ejb name.
	 * @param name the name of the ejb
	 * @return the name of the remote interface for the specified ejb
	 */
	public String getRemoteInterfaceForEjbName(String name) {
		Entity entity = getDescriptorForEjbName(name);

		return ((entity != null) ? entity.getRemote() : null);
	}

	/** Gets the name of the field in the ejb which corresponds to the 
	 * specified persistence-capable class name and field name pair.
	 * @param className the name of the persistence-capable
	 * @param fieldName the name of the field in the persistence-capable
	 * @return the name of the field in the ejb for the specified 
	 * persistence-capable field
	 */
	public String getEjbFieldForPersistenceField(String className, 
		String fieldName) {
		return fieldName;
	}

	/** Gets the name of the field in the persistence-capable class which 
	 * corresponds to the specified ejb name and field name pair.
	 * @param name the name of the ejb
	 * @param fieldName the name of the field in the ejb
	 * @return the name of the field in the persistence-capable for the 
	 * specified ejb field
	 */
	public String getPersistenceFieldForEjbField(String name, 
		String fieldName) {
		return fieldName;
	}
}
