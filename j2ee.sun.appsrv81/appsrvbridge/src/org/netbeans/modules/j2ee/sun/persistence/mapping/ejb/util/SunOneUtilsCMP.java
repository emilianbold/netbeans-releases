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


package org.netbeans.modules.j2ee.sun.persistence.mapping.ejb.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Properties;
import java.util.Enumeration;

// imports for CmpMapping extension
//
import java.io.File;
import org.netbeans.modules.j2ee.sun.persistence.mapping.core.util.MappingContext;
import org.netbeans.modules.j2ee.sun.persistence.mapping.core.util.Util;

import org.netbeans.lib.j2ee.sun.persistence.mapping.core.ClassState;
import com.sun.jdo.spi.persistence.utility.database.DatabaseConstants;
import com.sun.jdo.api.persistence.mapping.ejb.EJBInfoHelper;
import com.sun.jdo.api.persistence.mapping.ejb.MappingFile;
import com.sun.jdo.api.persistence.mapping.ejb.AbstractNameMapper;
import com.sun.jdo.api.persistence.mapping.ejb.ConversionHelper;
import com.sun.jdo.api.persistence.mapping.ejb.MappingGenerator;
import com.sun.jdo.api.persistence.mapping.ejb.ConversionException;
import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.model.mapping.MappingFieldElement;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingFieldElementImpl;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.jdo.impl.PersistenceFieldElementImpl;
import com.sun.jdo.api.persistence.model.ModelException;
import org.netbeans.modules.j2ee.sun.persistence.mapping.core.util.MappingContextFactory;
import com.sun.jdo.spi.persistence.utility.StringHelper;
import com.sun.jdo.spi.persistence.utility.logging.Logger;
import com.sun.jdo.spi.persistence.generator.database.MappingPolicy;
import com.sun.jdo.spi.persistence.generator.database.DatabaseGenerator;

import org.netbeans.modules.schema2beans.Schema2BeansException;

import com.sun.jdo.api.persistence.mapping.ejb.beans.*;
import com.sun.jdo.api.persistence.mapping.ejb.SunCmpMappingsUtils;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement;

import org.netbeans.modules.dbschema.DBException;

/**
 *
 * @author  Vince Kraemer
 *          Anissa Lam
 */
public class SunOneUtilsCMP 
{
    
    //TODO: This is defined in SunOneUtils, but to avoid depending on deploytool, we
    //should move it here, and make SunOneUtils get from here instead.
    //As of now, we just make sure they are defined the same.
    public static final String SUN_ONE_DESCRIPTOR = "Sun_One_Descriptor";
    public static final String GENERATED_SCHEMA_NAME = "Generated_Schema_Name";
    
    public static final String CMP_MAPPING_CONTEXT = "CMP_Mapping_Context";
    
    public static final String DBSCHEMA_EXTENSION = ".dbschema";
    public static final String DBSCHEMA_EXTENSION_UP = ".DBSCHEMA";
    public static final String DBSCHEMA_EXTENSION_ARRAY[] = {".dbschema", ".DBSCHEMA"};
    
    /** Creates a new instance of SunOneUtilsCMP */
    private SunOneUtilsCMP() {
    }
    
    static private Map strongRefMap = new HashMap();
    static private Map extraAttributesMap = new HashMap();

    private static AbstractNameMapper getNameMapper (EJBInfoHelper ejb) {
        if (null == ejb)
            throw new java.lang.IllegalArgumentException("null == ejb");
        return ejb.getNameMapper();
    }

    private static Map getBundleMap (EJBInfoHelper ejbBundle) {
        Map bundleMap = (Map)extraAttributesMap.get(ejbBundle);
        if (bundleMap == null) {
            bundleMap = new HashMap();
            extraAttributesMap.put(ejbBundle, bundleMap);
        }

        return bundleMap;
    }

    private static Object getExtraAttribute (EJBInfoHelper ejbBundle, 
        String attrName) {
        return getBundleMap(ejbBundle).get(attrName);
    }

    public static void addExtraAttribute (EJBInfoHelper ejbBundle, 
        String attrName, Object value) {
        getBundleMap(ejbBundle).put(attrName, value);
    }

    public static void removeExtraAttribute (EJBInfoHelper ejbBundle, 
        String attrName) {
        getBundleMap(ejbBundle).remove(attrName);
    }

    /** create a MappingContext based on the data available in the deploytool
     * @param scm A SunCmpMappings object
     * @param ejb The jar that the is being mapped
     * @return a MappingContext that represents the data found in the SunCmpMappings
     */
    public static MappingContext getMappingContext(SunCmpMappings scm, EJBInfoHelper ejb) 
      throws IllegalArgumentException, ModelException, DBException,
        ConversionException {
        SunCmpMappings consistent = makeMappingsConsistent(scm, ejb);
        Map mces = getMappingClasses(consistent, ejb);
        return MappingContextFactory.getMappingContext(ejb.getModel(), "ejb", false,
        SunOneUtilsCMP.class.getClassLoader());
    }
      
    /** create a MappingContext based on the data available in the deploytool
     * @param scm A SunCmpMappings object
     * @param ejb The jar that the is being mapped
     * @return a MappingContext that represents the data found in the SunCmpMappings
     */
    public static Map getMappingClasses(SunCmpMappings scm, EJBInfoHelper ejb) 
      throws IllegalArgumentException, ModelException, DBException,
        ConversionException {
        if (null == ejb)
            throw new java.lang.IllegalArgumentException("null == ejb");
        String scmDump = null;
        if (null != scm)
            scmDump = scm.dumpBeanNode();
        
        MappingFile mf = null;
        ConversionHelper myConvHelper = null;
        Map mces = null;
        Model myModel = null;
        try {
            //Need to construct MappingFile with ejb classloader so the dbschema
            //file can be found on disk otherwise dbschema file will not be found
            //and a dummy dbschema will be created by MappingFile.setDatabaseRoot()
            mf = new MappingFile(ejb.getClassLoader());
            myConvHelper = ejb.createConversionHelper();
            myConvHelper.setEnsureValidation(false);
            myConvHelper.setGenerateFields(false);
            checkMapping(scm);
            mces = mf.intoMappingClasses(scm, myConvHelper);
            myModel = ejb.getModel();
            Iterator iter = mces.keySet().iterator();
            while (iter.hasNext()) {
                String ejbName = (String) iter.next();
                MappingClassElement mce = (MappingClassElement) mces.get(ejbName);

                if (mce != null) {
                    updateMappingClass(ejb, mce);
                    addDummyVersionField(mce, myModel);

                    // newly created mces should be unmodified
                    // TODO - same for PCEs?
                    mce.setModified(false);
               }
            }
            return mces;
        }
        catch (NullPointerException foo) {
            String message = "null == ejb";
            IllegalArgumentException iae =
            new IllegalArgumentException(message);
            iae.initCause(foo);
            throw iae;
        }catch (RuntimeException rex){
            Util.showError(rex);  // tell the user what went wrong
            throw rex;  // Should never get into a situtation where user cannot close the
                        // Sun-specific Settings dialog like in bug 5032633 so rex should be thrown and
                        // handled in calling methods.
        }
    }
      
    /** 
     * Puts mapping class into model's cache and add a strong ref
     * @param mappingClass a collection of mapping classes
     */ 
    private static void updateMappingClass (EJBInfoHelper info,  
        MappingClassElement mce) {

        String name = mce.getName();

        info.getModel().updateKeyForClass(mce, name);

        // work around a bug in model... MCE's added to the cache can
        // disappear for some reason.
        // Jie Leng recommended this work-around.
        //
        StrongRefKey key = new StrongRefKey(info, name); 
        strongRefMap.put(key,mce);
    }

    /** Check if mapping exists but no schema specified.
     *  This can occur if user writes a mappings file by hand and doesn't bother
     *  to specify or capture a schema.  The appserver will do this automatically
     *  at deploy time.  Deploytool cannot display the mappings in this case even
     *  though sun-cmp-mappings.xml is still a valid file.
     *  Set isSchemaCapture flag so a warning pops-up when user opens
     *  CMP Database dialog or clicks on Create Database Mappings.  
     */
    private static void checkMapping(SunCmpMappings scm) {
        isSchemaSpecified = true;
        for (int i = 0; i < scm.sizeSunCmpMapping(); i++) {
            SunCmpMapping beanSet = scm.getSunCmpMapping(i);
            String schema = beanSet.getSchema();
            if (StringHelper.isEmpty(schema)) {
                if (beanSet.sizeEntityMapping() > 0) {
                    if (!StringHelper.isEmpty(beanSet.getEntityMapping(0).getTableName())) {
                        isSchemaSpecified = false;
                    }
                }
            }
        }
    }
    private static boolean isSchemaSpecified = true;
    
    public static boolean isSchemaSpecified() {
        return isSchemaSpecified;
    }
    
    // this is a cop out at the moment.
    static SunCmpMappings makeMappingsConsistent(SunCmpMappings scm, EJBInfoHelper bundle) {
        SunCmpMappings retVal = prepare(scm);
     
        // Take care of the cmp and cmr fields
        //
        Collection ejbs = bundle.getEjbNames();
        Iterator i = ejbs.iterator();
        while (i.hasNext()) {
            String ejb = (String) i.next();
            EntityMapping em = null;

            try {
                em = SunCmpMappingsUtils.findEntityMapping(retVal, ejb, true);
           }
            catch (IllegalArgumentException iae) {
                // skipping the bean is okay here.
            }
            if (null != em) {
                Collection fields = bundle.getFieldsForEjb(ejb);
                Collection rels = bundle.getRelationshipsForEjb(ejb);
                Iterator iter = rels.iterator();
                while (iter.hasNext()) {
                    String fname = (String) iter.next();
                    makeCmrFieldConsistent(em,fname);
                }
                fields.removeAll(rels);
                iter = fields.iterator();
                while (iter.hasNext()) {
                    String fname = (String) iter.next();
                    makeCmpFieldConsistent(em,fname);
                }
           }
        }

        // now remove bean and field entries in the sun-cmp-mappings
        // which are no longer in the ejb jar
        for (int j = 0; j < retVal.sizeSunCmpMapping(); j++) {
            SunCmpMapping beanSet = retVal.getSunCmpMapping(j);
            EntityMapping [] ems = beanSet.getEntityMapping();
            int len = ((ems != null) ? ems.length : 0);

            for (int k = 0; k < len; k++) {
                EntityMapping em = ems[k];
                String beanName = em.getEjbName().trim();
                if (ejbs.contains(beanName)) {
                    CmrFieldMapping[] cmrs = em.getCmrFieldMapping();
                    CmpFieldMapping [] cmps = em.getCmpFieldMapping();
                    Collection rels = bundle.getRelationshipsForEjb(beanName);
                    Collection fields = bundle.getFieldsForEjb(beanName); 
                    int fieldLen = ((cmrs != null) ? cmrs.length : 0);
				
                    fields.removeAll(rels);

                    for (int l = 0; l < fieldLen; l++) {
                        if (!rels.contains(cmrs[l].getCmrFieldName().trim()))
                            em.removeCmrFieldMapping(cmrs[l]);
                    }
                    fieldLen = ((cmps != null) ? cmps.length : 0);
                    for (int l = 0; l < fieldLen; l++) {
                        if (!fields.contains(cmps[l].getFieldName().trim()))
                            em.removeCmpFieldMapping(cmps[l]);
                    }
                } else	// remove bean
            		beanSet.removeEntityMapping(em);
            }
        }

        return retVal;
    }
    
    public static SunCmpMappings prepare(SunCmpMappings scm) {
        SunCmpMappings retVal = scm;
        if (null == retVal) {
            retVal = SunCmpMappings.createGraph();
        }
        int i = retVal.sizeSunCmpMapping();
        //SunCmpMapping mapping = retVal.getSunCmpMapping(0);
        if (1 > i) {
            SunCmpMapping mapping = new SunCmpMapping();
            retVal.addSunCmpMapping(mapping);
        }
        return retVal;
    }
    
    /** Make sure there is a CmrFieldMapping element in the graph for a role
     *
     * If the role is not valid, do not alter the graph.
     *
     */
    static void makeCmrFieldConsistent(EntityMapping em, 
	String fname) {
        try {
            //EntityMapping em = getEntityMapping(retVal, role.getOwner());
            //             if (null != fname && null != bean) {
            CmrFieldMapping cmr =
            SunCmpMappingsUtils.findCmrFieldMapping(em,  fname, true);
            
        }
        catch (NullPointerException npe) {
            // we are okay with this?
        }
        catch (IllegalArgumentException iae) {
            // we are okay with this
        }
    }
    
    /** Make sure there is a CmpFieldMapping element in the graph for a field
     *
     * If the fd is not valid is not valid or if the fd does not describe a cmp field,
     * do not alter the graph.
     *
     */
    static void makeCmpFieldConsistent(EntityMapping em, String fname) {
        try {
            CmpFieldMapping cmpfm =
            SunCmpMappingsUtils.findCmpFieldMapping(em,fname,true);
        }
        catch (NullPointerException npe) {
            // we are okay with this??
        }
        catch (IllegalArgumentException iae) {
            // we are okay with this
        }
    }
    
    /** Convert the MappingContext into a SunCmpMappings representation
     * @param mc The MappingConext to get the data from
     * @param ejb The jar
     * @return A new SunCmpMappings object that represents the mapping described by the mapping
     * context
     */
    public static SunCmpMappings getSunCmpMappings(MappingContext mc, 
        EJBInfoHelper ejb) {
        try {
            Map mces = new HashMap(); // mc.getModel().getMappingCache();
            // iterate through the beans and correct them
            Iterator i = ejb.getEjbNames().iterator();
            ConversionHelper ech = ejb.createConversionHelper();
            while (i.hasNext()) {
                String bean = (String) i.next();
                String bb = ech.getMappedClassName(bean);
                MappingClassElement mce = (MappingClassElement) mc.getModel().getMappingCache().get(bb);
                if (null != mce)
                    mces.put(bean, mce);
            }
            MappingFile mf = new MappingFile(ejb.getClassLoader());
            SunCmpMappings retVal = mf.fromMappingClasses(mces, ech);
            return retVal;
        } catch (Schema2BeansException se) {
            mc.getLogger().log(Logger.FINE, 
                "Error in loading or creating sun-cmp-mappings.xml", se);
        }
        return null;
    }
    
    
    /** Incomplete
     * @param mod
     * @param ejb
     */
    public static void addBean(SunCmpMappings scms, String beanName) {
        // we don't need to do anything here...
    }
    
    /** Incomplete
     * @param mod
     * @param ejb
     */
    public static void removeBean(SunCmpMappings scms, String beanName) {
        if (scms != null) {
            EntityMapping em = SunCmpMappingsUtils.findEntityMapping(scms,  beanName, false);
            if (null != em) {
                SunCmpMapping scm = (SunCmpMapping) em.parent();
                scm.removeEntityMapping(em);
                if (scm.sizeEntityMapping() < 1) {
                    scms.removeSunCmpMapping(scm);
                }
            }
        }
    }
    
    /** Incomplete
     * @param scms the mapping
     * @param beanName The bean name of the bean being altered
     * @param fieldName The name of the field being added
     */
    public static void addField(SunCmpMappings scms, String beanName, String fieldName) {
        if (scms != null) {
            EntityMapping em = SunCmpMappingsUtils.findEntityMapping(scms,  beanName, true);
            if (null != em) {
                CmpFieldMapping cfm = SunCmpMappingsUtils.findCmpFieldMapping(em, fieldName, true);
            }
        }
    }
    
    /** Incomplete
     * @param scms The mapping data
     * @param beanName the bean's name
     * @param fieldName the name of the field being removed
     */
    public static void removeField(SunCmpMappings scms, String beanName, String fieldName) {
        if (scms != null) {
            EntityMapping em = SunCmpMappingsUtils.findEntityMapping(scms,  beanName, false);
            if (null != em) {
                CmpFieldMapping cfm = SunCmpMappingsUtils.findCmpFieldMapping(em, fieldName, false);
                if (null != cfm)
                    em.removeCmpFieldMapping(cfm);
            }
        }
    }
    
    /** Incomplete
     *
     * Called when a relationship field is added to a bean in the jar.
     * @param scms the mapping data
     * @param beanName the relationship's hosting bean
     * @param fieldName the name of the relationship field
     * @param inverseBean The bean on the otherside of the relationship
     * @param inverseField the inverse field name. May be null.
     */
    public static void addField(SunCmpMappings scms, String beanName, String fieldName, boolean relationship) {
        if (scms != null) {
            EntityMapping em = SunCmpMappingsUtils.findEntityMapping(scms,  beanName, true);
            if (null != em) {
                CmrFieldMapping cfm = SunCmpMappingsUtils.findCmrFieldMapping(em, fieldName, true);
            }
        }
    }
    
    /** Incomplete
     *
     * Called to remove a relationship field from the jar's SunCmpMappings
     * @param scms The mappings data
     * @param beanName relationship field's host
     * @param fieldName The relationship field
     * @param inverseBean The opposite side of the relationship
     * @param inverseField The inverse field
     */
    public static void removeField(SunCmpMappings scms, String beanName, String fieldName, boolean relationship) {
        if (scms != null) {
            EntityMapping em = SunCmpMappingsUtils.findEntityMapping(scms,  beanName, false);
            if (null != em) {
                CmrFieldMapping cfm = SunCmpMappingsUtils.findCmrFieldMapping(em, fieldName, false);
                if (null != cfm)
                    em.removeCmrFieldMapping(cfm);
            }
        }
    }
    
    
    
    // Part of disappearing MCE work around
    static class StrongRefKey {
        private EJBInfoHelper ejbBundle = null;
        private String ejbName = null;
        public StrongRefKey(EJBInfoHelper ejb, String beanName) {
            ejbBundle = ejb;
            ejbName = beanName;
        }
        
        public int hashCode() {
            return ejbBundle.hashCode() + ejbName.hashCode();
        }
        
        public boolean equals(Object other) {
            if (!(other instanceof StrongRefKey))
                return false;
            StrongRefKey otherKey = (StrongRefKey) other;
            return (this.ejbBundle.equals(otherKey.ejbBundle) &&
                this.ejbName.equals(otherKey.ejbName));
        }
    }
    
    
    /**
     * creates the ClassState object of a bean based on the bean name.
     * @param context  mappingContext
     * @param ejbBundle  the ejb module containing the bean
     * @param beanName   name of the bean
     * @return the ClassState object for this bean.
     */
    public static ClassState createClassState(MappingContext context, EJBInfoHelper ejbBundle, String beanName){
        Model model = context.getModel();
        ConversionHelper ech = ejbBundle.createConversionHelper();
        String bb = ech.getMappedClassName(beanName);
        MappingClassElement mce = model.getMappingClass(bb);
        return new ClassState(model, mce);
    }
   
    /**
     * add a dummy field to be used for version consistency if necessary
     * @param mce  MappingClassElement
     * @param model  the model object
     */
    private static void addDummyVersionField (MappingClassElement mce, Model model) throws ModelException {
        if (mce.getVersionFields().isEmpty()) {
            String fieldName = AbstractNameMapper.GENERATED_VERSION_FIELD_PREFIX;
            PersistenceClassElement pce =
                model.getPersistenceClass(mce.getName());

            pce.addField(new PersistenceFieldElement(
                new PersistenceFieldElementImpl(fieldName), pce));
            MappingFieldElement mfe = new MappingFieldElementImpl(fieldName, mce);
            mfe.setVersion(true);
            mce.addField(mfe);
        }
    }


    /**
     * returns the bean name associated with the specified mapping class name.
     * @param ejbBundle  the ejb module containing the bean
     * @param className   name of the class
     * @return the bean name associated with the specified class.
     */
    public static String getBeanName(EJBInfoHelper ejbBundle, String className){
        return getNameMapper(ejbBundle).getEjbNameForPersistenceClass(className);
    }
    
    /*
     * Creates a MappingGenerator and generate the mapping classes based on the vendor name.
     * returns true if the process is ok, otherwise, it will display an error dialog and
     * return false.
     * @param ejbBundle  an EJBInfoHelper
     * @param vendorName  the datasource vendor name
     * @return true if mapping classes is generated sucessfully, otherwise false.
     */
    public static boolean generateMapping(EJBInfoHelper ejbBundle, String vendorName, boolean uniqueTableNames){
        try {
            MappingGenerator mg = new MappingGenerator(ejbBundle, 
                ejbBundle.getClassLoader(), true);
            DatabaseGenerator.Results results = mg.generateMappingClasses(
                vendorName, Boolean.toString(uniqueTableNames), 
                getSchemaPropertiesAsProperties(ejbBundle), 
                getTempFilePath(ejbBundle)); // TODO; need real path
            Iterator iter = results.getMappingClasses().iterator();

            while (iter.hasNext()) {
                MappingClassElement mce = (MappingClassElement)iter.next();
                updateMappingClass(ejbBundle, mce);
            }

            return true;
        }catch (Exception ex){
            //Util.showError(ex);   //suppress error dialog, fixing bug# 4926525
            Util.showError(ex);   //put this back since we don't have ConnectionFailedDialog. 
            return false;
        }
    }
    
    private static Map pathMap = new java.util.HashMap();
    
    static String getTempFilePath(EJBInfoHelper ejbBundle) {
        String retVal = (String) pathMap.get(ejbBundle);
        if (null == retVal) {
            //TODO - get temp dir from UIProject.createTempFilename - set it
            //as an extra attribute on this ejbBundle from the other package
            //use infoHelper's getSchemaNameToGenerate for the file name
            java.io.File schemaFile = new File("/tmp/blah");//UIProject.createTempFilename(ejbBundle, "foobar", true);
            retVal = schemaFile.getParentFile().getPath(); // TODO; need real path
            schemaFile.delete();
            pathMap.put(ejbBundle, retVal);
        }
        return retVal;
    }
    
    /**
     * Retrieve an existing mapping context thats associated with the specified  DOL descriptor
     * @param desc the EJBInfoHelper
     * @return the MappingContext associated with the descriptor or null.
     */
    public static MappingContext getExistingMappingContext(EJBInfoHelper desc) {
        if (desc == null) return null;
        return (MappingContext)getExtraAttribute(desc, CMP_MAPPING_CONTEXT);
    }
    
    
    /**
     * Set the MappingContext of the DOL descriptor
     * @param desc the EJBInfoHelper
     * @param mappingContext the MappingContext to be attached to the descriptor
     */
    public static void setExistingMappingContext(EJBInfoHelper desc, MappingContext
    mappingContext) {
        if (desc == null) return ;
        addExtraAttribute(desc, CMP_MAPPING_CONTEXT, mappingContext);
    }
    
    
    /**
     * determine if the mapping of an ejb module is generated by java2DB.
     * @param desc the EJBInfoHelper
     * @return true if it is from java2DB, otherwise false.
     */
    public static boolean isJavaToDB(EJBInfoHelper desc) 
    {
        
        return isSchemaGeneratorProperty(desc, DatabaseConstants.JAVA_TO_DB_FLAG);
        
        
    }
    
    
    /**
     * Set the property of a ejb module for indicating if the mapping is generated by java2DB.
     * A side effect is that, if value is <code>true<code>, then both CreateTablesAtDeploy and DropTablesAtUndeploy 
     * properties will be set to "true" if those properties were not set before.
     *
     * @param desc  the EJBInfoHelper
     * @param value  true or false
     */
    public static void setJavaToDB(EJBInfoHelper desc, boolean value) 
    {
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        setSchemaGeneratorProperty(desc, DatabaseConstants.JAVA_TO_DB_FLAG, value );
        if (value){
            // not testing if cmpResource is null because setSchemaGeneratorProperty() for sure will create
            // that as all the properties involved is under <CmpResource>
            CmpResource cmpResource = sunEjbJar.getEnterpriseBeans().getCmpResource();
            if (cmpResource.getCreateTablesAtDeploy() == null) {
                cmpResource.setCreateTablesAtDeploy(Boolean.TRUE.toString());
            }
            if (cmpResource.getDropTablesAtUndeploy() == null) {
                cmpResource.setDropTablesAtUndeploy(Boolean.TRUE.toString());
            }
        }
    }

    public static void setDatabaseVendorName(EJBInfoHelper desc, String vendorName)
    {
        if (desc == null) return;
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        EnterpriseBeans enterpriseBeans = sunEjbJar.getEnterpriseBeans();
        CmpResource cmpResource = enterpriseBeans.getCmpResource();
        if (cmpResource == null) {
            cmpResource = enterpriseBeans.newCmpResource();
            enterpriseBeans.setCmpResource(cmpResource);
        }
        cmpResource.setDatabaseVendorName(vendorName);
    }
    
    public static boolean isSchemaGeneratorProperty(EJBInfoHelper desc, String propName)
    {
        if (null == desc) return false;
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        if (null == sunEjbJar)
            return false;
        if (sunEjbJar.getEnterpriseBeans() == null) return false;
        CmpResource cmpResource = (sunEjbJar.getEnterpriseBeans()).getCmpResource() ;
        if (cmpResource == null) return false;
        SchemaGeneratorProperties pp = cmpResource.getSchemaGeneratorProperties();
        if (pp == null) return false;
        PropertyElement[] pElements = pp.getPropertyElement();
        if (pElements != null){
            for(int i= 0; i< pElements.length; i++){
                if (propName.equals(pElements[i].getName())){
                    String value = pElements[i].getValue();
                    if (! StringHelper.isEmpty(value)) {
                        return Boolean.valueOf(value).booleanValue();
                    }
                }
            }
        }
        return false;
    
    }
    
    public static void setProps(EJBInfoHelper desc, Properties prop) 
    {
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        for (Enumeration e = prop.propertyNames() ; e.hasMoreElements() ;) {
            String key = (String) e.nextElement();
            String value = (String) prop.getProperty(key);
            setSchemaGeneratorProperty(desc, key, value);
        }
    }
    
    public static void setSchemaGeneratorProperty(EJBInfoHelper desc, String propName, boolean value )
    {
        setSchemaGeneratorProperty(desc, propName, String.valueOf(value));
    }
        
    public static void setSchemaGeneratorProperty(EJBInfoHelper desc, String propName, String valString )
    {
        if (desc == null || StringHelper.isEmpty(propName)) return;
        if (valString == null){
            removeSchemaGeneratorProperty(desc, propName);
            return;
        }    
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        EnterpriseBeans enterpriseBeans = null;
        if ( (enterpriseBeans = sunEjbJar.getEnterpriseBeans())== null){
            enterpriseBeans = sunEjbJar.newEnterpriseBeans();
            sunEjbJar.setEnterpriseBeans(enterpriseBeans);
        }

        CmpResource cmpResource = enterpriseBeans.getCmpResource();
        if (cmpResource == null) {
            cmpResource = enterpriseBeans.newCmpResource();
            enterpriseBeans.setCmpResource(cmpResource);
        }
        
        SchemaGeneratorProperties sgp = cmpResource.getSchemaGeneratorProperties();
        if (sgp == null){
            sgp = cmpResource.newSchemaGeneratorProperties();
            cmpResource.setSchemaGeneratorProperties(sgp);
        }
        
        PropertyElement[] pElements = sgp.getPropertyElement();
        if (pElements != null){
            for(int i= 0; i< pElements.length; i++){
                if (propName.equals(pElements[i].getName())){
                    pElements[i].setValue(valString);
                    return;
                }
            }
        }
        
        PropertyElement pe = sgp.newPropertyElement();
        //pe.setName(com.sun.jdo.spi.persistence.support.ejb.ejbc.MappingClassesLoader.JAVATODB_FLAG);
        pe.setName(propName);
        pe.setValue(valString);
        sgp.addPropertyElement(pe);
    }
    

    //returns a SchemaProperties as a java util Properties
    private static Properties getSchemaPropertiesAsProperties(EJBInfoHelper desc)
    {
        Properties prop = null;
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        if (sunEjbJar.getEnterpriseBeans() != null){
            CmpResource cmpResource = sunEjbJar.getEnterpriseBeans().getCmpResource();
            if (cmpResource != null){
                SchemaGeneratorProperties sgp = cmpResource.getSchemaGeneratorProperties();
                if (sgp != null){
                    PropertyElement[] pElements = sgp.getPropertyElement();
                    if ( (pElements != null) && (pElements.length > 0) ){
                        prop = new Properties();
                        for(int i= 0; i< pElements.length; i++){
                            prop.setProperty(pElements[i].getName(), pElements[i].getValue());
                        }
                    }
                }
            }
        }
        return prop;
    }
    
    
    private static void removeSchemaGeneratorProperty(EJBInfoHelper desc, String propName)
    {
        if (desc == null || propName == null) return;
        
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        if (sunEjbJar != null){
            EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans() ;
            if (eb != null){
                CmpResource cmpResource = eb.getCmpResource();
                if (cmpResource != null){
                    SchemaGeneratorProperties sgp = cmpResource.getSchemaGeneratorProperties();
                    if (sgp != null){
                        PropertyElement[] pElements = sgp.getPropertyElement();
                        if ( (pElements != null) && (pElements.length > 0) ){
                            for(int i= 0; i< pElements.length; i++){
                                if (propName.equals(pElements[i].getName())){
                                    sgp.removePropertyElement(pElements[i]);
                                    if (pElements.length == 1){
                                        cmpResource.setSchemaGeneratorProperties(null);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
            
    }
    
    
    public static void cleanupJavaToDBRelatedProps(EJBInfoHelper desc) 
    {
        if (desc == null) return;
        setSchemaGeneratorProperty(desc, MappingPolicy.USE_UNIQUE_TABLE_NAMES, null);
        setSchemaGeneratorProperty(desc, DatabaseConstants.JAVA_TO_DB_FLAG, null );
        removeExtraAttribute(desc, GENERATED_SCHEMA_NAME);
        SunEjbJar sunEjbJar = (SunEjbJar)getExtraAttribute(desc, SUN_ONE_DESCRIPTOR);
        if (sunEjbJar.getEnterpriseBeans() != null){
            CmpResource cmpResource = sunEjbJar.getEnterpriseBeans().getCmpResource();
            if (cmpResource != null){
                cmpResource.setCreateTablesAtDeploy(null);
                cmpResource.setDropTablesAtUndeploy(null);
                cmpResource.setDatabaseVendorName(null);
            }
        }
    }
        
    /*
     * retrieve the schema file name thats associated with this bundle.
     * If this attribute is not set, a null will be returned.
     */
    static public String getGeneratedSchemaName(EJBInfoHelper bundle) {
        return (String) getExtraAttribute(bundle, GENERATED_SCHEMA_NAME);
    }

    /*
     * Stored in the bundle as an extra attribute, the schema file name thats generated when using java2DB.
     */
    static public void setGeneratedSchemaName(EJBInfoHelper bundle, Model model)
    {
        ConversionHelper ech = bundle.createConversionHelper();
        for(Iterator iter = bundle.getEjbNames().iterator(); iter.hasNext();){
            String ejb = (String)iter.next();
            String beanName = ech.getMappedClassName(ejb);
            MappingClassElement mce = model.getMappingClass(beanName);
            if ( mce != null){
                String schemaName = mce.getDatabaseRoot();
                addExtraAttribute(bundle, GENERATED_SCHEMA_NAME, schemaName);
                break;
            }
        }
    }
 
    /*
     * If the filename ends with .dbschema or .DBSCHEMA, the extension will be removed and the name without the 
     * extension will be returned,  otherwise, return the original name.
     */
    static public String removeSchemaFileNameExtension(String fullName) 
    {
        if (fullName.endsWith(DBSCHEMA_EXTENSION) ||
            fullName.endsWith(DBSCHEMA_EXTENSION_UP)){
            return fullName.substring(0, fullName.indexOf(DBSCHEMA_EXTENSION));
        }
        return fullName;
    }
}
