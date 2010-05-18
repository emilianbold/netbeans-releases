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
 * EJBDevelopmentInfoHelper.java
 *
 * Created on October 29, 2004, 11:51 AM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.ejb;

import java.lang.ref.WeakReference;
import java.util.*;
import java.io.IOException;

import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.j2ee.dd.api.ejb.*;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.dbschema.SchemaElement;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.spi.persistence.utility.StringHelper;
import com.sun.jdo.api.persistence.mapping.ejb.*;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;

/** This is a class which implements the EJBInfoHelper interface 
 * based on EjbJar and other DDAPI classes.
 *
 * @author Rochelle Raccah
 */
public class EJBDevelopmentInfoHelper implements EJBInfoHelper {
    
	private final ResourceBundle bundle = NbBundle.getBundle(EJBDevelopmentInfoHelper.class);   
    
	private static final char UNDERLINE = '_';
	private static final String DBSCHEMA_EXTENSION = "dbschema"; // NOI18N

	private EjbJar bundleDescriptor;
	private WeakReference sourceFileMapRef;
	private DevelopmentNameMapper nameMapper;	// standard one
	private Model model;

	/** Creates a new instance of EJBDevelopmentInfoHelper
	 * @param mappings the SunCmpMappings which helps force relevant
	 * schemas into the cache.
	 * @param sourceFileMap the SourceFileMap which helps look up
	 * source roots in the project.
	 */
	public EJBDevelopmentInfoHelper(SunCmpMappings mappings, 
			SourceFileMap sourceFileMap) {
		this(mappings, sourceFileMap, null, null);
	}

	/** Creates a new instance of EJBDevelopmentInfoHelper
	 * @param mappings the SunCmpMappings which helps force relevant
	 * schemas into the cache.
	 * @param sourceFileMap the SourceFileMap which helps look up
	 * source roots in the project.
	 */
	EJBDevelopmentInfoHelper(SunCmpMappings mappings, 
			SourceFileMap sourceFileMap, DevelopmentNameMapper nameMapper, 
			Model model) {
		this.setSourceFileMap(sourceFileMap);
		this.nameMapper = nameMapper;
		this.model = model;
		putSchemasInCache(mappings);
	}

	/** Gets the EjbJar which defines the universe of
	 * names for this application.
	 * @return the EjbJar which defines the universe of
	 * names for this application.
	 */
	private EjbJar getBundleDescriptor() {
		if (bundleDescriptor == null) {
			try {
				FileObject[] result = 
					getSourceFileMap().findSourceFile("ejb-jar.xml");	// NOI18N

				if ((result != null) && (result.length > 0)) {
					bundleDescriptor = DDProvider.getDefault().
						getDDRoot(result[0]);
				} else {
					// no ejb-jar.xml
					throw new IllegalStateException(
						bundle.getString("ERR_EjbJarRequired")); // NOI18N
				}
			} catch (IOException ioe) {
				// TODO this is really a problem - should we throw a
				//RuntimeException?  for now, at least log the cause
				ioe.printStackTrace();// will return null
			}
		}

		return bundleDescriptor;
	}

	private FileObject[] getSourceRoots() {
		return getSourceFileMap().getSourceRoots();
	}

	/** Gets the name of the ejb bundle.
	 * @return the name of the ejb bundle
	 */
	public String getEjbJarDisplayName() {
		// TODO: this other call needs locale:
		// return getBundleDescriptor().getDisplayName();
		// which method should be used?
		return getBundleDescriptor().getDefaultDisplayName();
	}

	/** Gets a collection of names of schemas defined in this
	 * ejb jar.
	 * @return a collection schema names
	 */
	public Collection getAvailableSchemaNames() {
		FileObject[] sourceRoots = getSourceRoots();
		ArrayList returnList = new ArrayList();
		int i, count = ((sourceRoots != null) ? sourceRoots.length : 0);

		for (i = 0; i < count; i++) {
			Enumeration allSchemaFiles = 
				getAllOfType(sourceRoots[i], DBSCHEMA_EXTENSION);

			while (allSchemaFiles.hasMoreElements()) {
				FileObject next = (FileObject)allSchemaFiles.nextElement();

				returnList.add(
					getSourceFileMap().getDistributionPath(next).getPath());
			}
		}

		return returnList;
	}

	private Enumeration getAllOfType(FileObject from, final String ext) {
		return Enumerations.filter(from.getChildren(true), 
		new Enumerations.Processor() {
			public Object process(Object obj, Collection allwaysNull) {
				return ((FileObject)obj).hasExt(ext) ? obj : null;
			}
		});
	}

	public String getSchemaNameToGenerate() {
		return getSourceFileMap().getContextName() +
			UNDERLINE + getEjbJarDisplayName();
	}

	/** Gets the schema with the specified name, loading it if necessary.
	 * This implementation uses the file object source roots as the extra  
	 * context information used to load.
	 * @param schemaName the name of the schema to be loaded
	 * @return the schema object
	 */
	public SchemaElement getSchema(String schemaName) {
		return SchemaElement.forName(schemaName, getSourceRoots());
	}

	// cmps only
	public Collection getEjbNames() {
		EnterpriseBeans allBeans = getBundleDescriptor().getEnterpriseBeans();
		Entity[] entityBeans = allBeans.getEntity();
		int i, count = ((entityBeans != null) ? entityBeans.length : 0);
		ArrayList returnList = new ArrayList();

		for (i = 0; i < count; i++) {
			Entity ejb = entityBeans[i];

			if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(
					ejb.getPersistenceType())) {
				returnList.add(ejb.getEjbName());
			}
		}
		
		return returnList;
	}

	public Collection getFieldsForEjb(String ejbName) {
		return getNameMapperInternal().getFieldsForEjb(ejbName);
	}

	public Collection getRelationshipsForEjb(String ejbName) {
		return getNameMapperInternal().getRelationshipFieldsForEjb(ejbName);
	}

	/** Gets the class loader which corresponds to this ejb bundle.
	 * @return the class loader which corresponds to this ejb bundle
	 */
	public ClassLoader getClassLoader() {
		// TODO: not sure if this should really be null
		return null;//getBundleDescriptor().getClassLoader();
	}

	public AbstractNameMapper getNameMapper() {
		return getNameMapperInternal();
	}

	public AbstractNameMapper createUniqueNameMapper() {
	// TODO: this is NOT OKAY to have 2 diff instances neither
	// of which does expand pc class names
	// need to move expandpc names from namemapper subclass to superclass
	// need to get a name mapper which returns diff pc class names to 
	// be used as *unique* table names
	// right now, at ejbc time/dt time, DOL provides the hash code that makes
	// pc class names unique, but we could provide an alternative
	// deployment creation of tables would use dbschema table names, so 
	// 2 diff algorithms to create unique names would be okay
		return new DevelopmentNameMapper(getBundleDescriptor());
	}

	private DevelopmentNameMapper getNameMapperInternal() {
		if (nameMapper == null)
			nameMapper = new DevelopmentNameMapper(getBundleDescriptor());

		return nameMapper;
	}

	public ConversionHelper createConversionHelper() {
		return new DevelopmentConversionHelper(getNameMapperInternal(),
			getModel());
	}

	public Model getModel() {
		if (model == null) {
			model = new EJBDevelopmentModel(getNameMapperInternal(), 
				getClassLoader(), getSourceRoots());
		}

		return model;
	}

	// we must load all schemas into the cache 
	// while we know the sourceroot context
	private void putSchemasInCache(SunCmpMappings mappings) {
		if (mappings != null) {
			SunCmpMapping[] mapping = mappings.getSunCmpMapping();
			int i, count = ((mapping != null) ? mapping.length : 0);

			for (i = 0; i < count; i++) {
				String schemaName = mapping[i].getSchema();
				
				if (!StringHelper.isEmpty(schemaName))
					getSchema(schemaName.trim());
			}
		}
	}

	private SourceFileMap getSourceFileMap() {
		SourceFileMap sfm = null;
		if(sourceFileMapRef != null) {
			sfm = (SourceFileMap) sourceFileMapRef.get();
}
		return sfm;
	}

	private void setSourceFileMap(SourceFileMap sourceFileMap) {
		this.sourceFileMapRef = new WeakReference(sourceFileMap);
	}
}
