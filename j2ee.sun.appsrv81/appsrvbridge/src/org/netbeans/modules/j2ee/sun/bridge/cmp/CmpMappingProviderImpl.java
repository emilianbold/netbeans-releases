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
package org.netbeans.modules.j2ee.sun.bridge.cmp;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.ModelException;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.PersistenceElementProperties;
import com.sun.jdo.api.persistence.model.jdo.PersistenceFieldElement;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;
import com.sun.jdo.api.persistence.model.mapping.MappingClassElement;
import com.sun.jdo.api.persistence.model.jdo.impl.PersistenceFieldElementImpl;
import com.sun.jdo.api.persistence.model.jdo.impl.RelationshipElementImpl;
import com.sun.jdo.api.persistence.model.mapping.MappingFieldElement;
import com.sun.jdo.api.persistence.model.mapping.MappingElementProperties;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingFieldElementImpl;
import com.sun.jdo.api.persistence.model.mapping.impl.MappingRelationshipElementImpl;
import com.sun.jdo.api.persistence.mapping.ejb.EJBInfoHelper;
import com.sun.jdo.api.persistence.mapping.ejb.ConversionHelper;
import org.netbeans.modules.j2ee.sun.persistence.mapping.core.util.MappingContext;
import org.netbeans.modules.j2ee.sun.persistence.mapping.ejb.EJBDevelopmentInfoHelper;
import org.netbeans.modules.j2ee.sun.persistence.mapping.ejb.util.MappingConverter;
import org.netbeans.modules.j2ee.sun.persistence.mapping.ejb.util.SunOneUtilsCMP;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.sun.api.CmpMappingProvider;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.CheckVersionOfAccessedInstances;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.Consistency;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

// TODO - event handling, model merging for new wizard invoc,
// model validation integration where necessary, testing, jar
// handling, repackaging
/**
 *
 * @author raccah
 */
public class CmpMappingProviderImpl implements CmpMappingProvider {

    public static final String CMP_MAPPINGS_CHANGED = "CmpMappingsChanged"; //NOI18N
    /*    private static final String CMP_FIELD_XPATH = ENTITY_XPATH + "/cmp-field"; // NOI18N
    private static final String CMP_FIELD_NAME_XPATH = CMP_FIELD_XPATH + "/field-name"; // NOI18N
    private static final String EJB_RELATION_XPATH = "/ejb-jar/relationships/ejb-relation"; // NOI18N
    private static final String CMR_FIELD_XPATH = EJB_RELATION_XPATH + "/ejb-relationship-role/cmr-field"; // NOI18N
    private static final String CMR_FIELD_NAME_XPATH = CMR_FIELD_XPATH + "/cmr-field-name"; // NOI18N
    private static final String CMP_MAPPING_FILE = "sun-cmp-mappings.xml"; // NOI18N
     */

    private FileObject mappingFileRoot;

    /** Holds the value of the cmp mapping info */
    private MappingContext mappingContext;

    /** Holds the value of the cmp mapping info helper */
    private EJBInfoHelper ejbInfoHelper;

    /** Holds the value of the source file map passed to ejbInfoHelper to prevent it
     *  from being garbage collected.
     */
    private SourceFileMap sourceFileMap;

    /** Holds the value of the cmp mapping conversion helper */
    private ConversionHelper conversionHelper;


    private PropertyChangeListener cmpMappingListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            String propName = evt.getPropertyName();

            if (((source instanceof MappingClassElement) && MappingElementProperties.PROP_MODIFIED.equals(propName)) || ((source instanceof PersistenceClassElement) && PersistenceElementProperties.PROP_MODIFIED.equals(propName))) {
                boolean modified = ((Boolean) evt.getNewValue()).booleanValue();

                // only set if true -- otherwise could be endless loop
                if (modified) {
                    firePCSChange("mapping", null, null);
                }
            }
        }
    };

    private void firePCSChange(String propName, Object oldO, Object newO) {
        // TODO - used to be here, but getPCS not valid here
        //  getPCS().firePropertyChange(propName, oldO, newO);
    }

    /** If we saved the bean tree right now, would we write out a CMP mappings file?
     *  This logic borrowed from hasDDSnippet() in getCmpMappingSnippet() and probably
     *  could be optimized further.
     */
    private boolean hasSunCmpMappings() {
        return mappingContext != null && ejbInfoHelper != null;
    }

    public MappingContext getMappingContext() {
        return getMappingContext(null, getEJBInfoHelper());
    }

    private com.sun.jdo.api.persistence.mapping.ejb.beans.FetchedWith convertFetched(FetchedWith interfaceFetched) throws VersionNotSupportedException {
        com.sun.jdo.api.persistence.mapping.ejb.beans.FetchedWith fetched = new com.sun.jdo.api.persistence.mapping.ejb.beans.FetchedWith();
        boolean isDefault = interfaceFetched.isDefault();
        boolean isNone = interfaceFetched.isNone();
        String namedGroup = interfaceFetched.getNamedGroup();

        if (!isNone && !isDefault && (namedGroup == null)) {
            Integer interfaceLevel = interfaceFetched.getLevel();
 
            if (interfaceLevel != null) {
                fetched.setLevel(interfaceLevel.intValue());
            }
        }
        fetched.setNamedGroup(namedGroup);
        fetched.setDefault(isDefault);
        fetched.setNone(isNone);
        return fetched;
    }

    private com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair convertPair(ColumnPair interfacePair) {
        com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair pair = new com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair();
        pair.setColumnName(new String[]{interfacePair.getColumnName(), interfacePair.getColumnName2()});
        return pair;
    }

    private com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMappings convertInterfaceToGen(SunCmpMappings beanGraph) throws VersionNotSupportedException {
        com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMappings returnValue = com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMappings.createGraph();
        SunCmpMapping[] interfaceMappings = beanGraph.getSunCmpMapping();
        for (SunCmpMapping interfaceMapping : interfaceMappings) {
            com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMapping mapping = new com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMapping();
            EntityMapping[] interfaceEntities = interfaceMapping.getEntityMapping();
            for (EntityMapping interfaceEntity : interfaceEntities) {
                com.sun.jdo.api.persistence.mapping.ejb.beans.EntityMapping entity = new com.sun.jdo.api.persistence.mapping.ejb.beans.EntityMapping();
                CmpFieldMapping[] interfaceFields = interfaceEntity.getCmpFieldMapping();
                for (CmpFieldMapping interfaceField : interfaceFields) {
                    com.sun.jdo.api.persistence.mapping.ejb.beans.CmpFieldMapping field = new com.sun.jdo.api.persistence.mapping.ejb.beans.CmpFieldMapping();
                    field.setFieldName(interfaceField.getFieldName());
                    field.setColumnName(interfaceField.getColumnName());
                    field.setFetchedWith(convertFetched(interfaceField.getFetchedWith()));
                    field.setReadOnly(interfaceField.isReadOnly());
                    entity.addCmpFieldMapping(field);
                }
                CmrFieldMapping[] interfaceRels = interfaceEntity.getCmrFieldMapping();
                for (CmrFieldMapping interfaceRel : interfaceRels) {
                    com.sun.jdo.api.persistence.mapping.ejb.beans.CmrFieldMapping rel = new com.sun.jdo.api.persistence.mapping.ejb.beans.CmrFieldMapping();
                    rel.setCmrFieldName(interfaceRel.getCmrFieldName());
                    ColumnPair[] interfacePairs = interfaceRel.getColumnPair();
                    for (ColumnPair interfacePair : interfacePairs) {
                        rel.addColumnPair(convertPair(interfacePair));
                    }
                    rel.setFetchedWith(convertFetched(interfaceRel.getFetchedWith()));
                    entity.addCmrFieldMapping(rel);
                }
                SecondaryTable[] interfaceSTs = interfaceEntity.getSecondaryTable();
                for (SecondaryTable interfaceST : interfaceSTs) {
                    com.sun.jdo.api.persistence.mapping.ejb.beans.SecondaryTable secondary = new com.sun.jdo.api.persistence.mapping.ejb.beans.SecondaryTable();
                    ColumnPair[] interfacePairs = interfaceST.getColumnPair();
                    for (ColumnPair interfacePair : interfacePairs) {
                        secondary.addColumnPair(convertPair(interfacePair));
                    }
                    secondary.setTableName(interfaceST.getTableName());
                    entity.addSecondaryTable(secondary);
                }
                Consistency interfaceConsistency = interfaceEntity.getConsistency();
                if (interfaceConsistency != null) {
                    com.sun.jdo.api.persistence.mapping.ejb.beans.Consistency consistency = new com.sun.jdo.api.persistence.mapping.ejb.beans.Consistency();

                    consistency.setCheckAllAtCommit(interfaceConsistency.isCheckAllAtCommit());
                    consistency.setCheckModifiedAtCommit(interfaceConsistency.isCheckModifiedAtCommit());
                    consistency.setLockWhenLoaded(interfaceConsistency.isLockWhenLoaded());
                    consistency.setLockWhenModified(interfaceConsistency.isLockWhenModified());
                    consistency.setNone(interfaceConsistency.isNone());
                    CheckVersionOfAccessedInstances interfaceCheck = interfaceConsistency.getCheckVersionOfAccessedInstances();
                    if (interfaceCheck != null) {
                        com.sun.jdo.api.persistence.mapping.ejb.beans.CheckVersionOfAccessedInstances check = new com.sun.jdo.api.persistence.mapping.ejb.beans.CheckVersionOfAccessedInstances();
                        check.setColumnName(interfaceCheck.getColumnName());
                        consistency.setCheckVersionOfAccessedInstances(check);
                    }
                    entity.setConsistency(consistency);
                }
                entity.setEjbName(interfaceEntity.getEjbName());
                entity.setTableName(interfaceEntity.getTableName());
                mapping.addEntityMapping(entity);
            }
            mapping.setSchema(interfaceMapping.getSchema());
            returnValue.addSunCmpMapping(mapping);
        }
        return returnValue;
    }

    private FetchedWith convertFetched(com.sun.jdo.api.persistence.mapping.ejb.beans.FetchedWith fetched, FetchedWith interfaceFetched) throws VersionNotSupportedException {
        boolean isDefault = fetched.isDefault();
        boolean isNone = fetched.isNone();
        String namedGroup = fetched.getNamedGroup();

        if (!isNone && !isDefault && (namedGroup == null)) {
            interfaceFetched.setLevel(Integer.valueOf(fetched.getLevel()));
        }

        interfaceFetched.setNamedGroup(namedGroup);
        interfaceFetched.setDefault(isDefault);
        interfaceFetched.setNone(isNone);
        return interfaceFetched;
    }

    private ColumnPair convertPair(com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair pair, ColumnPair interfacePair) {
        String[] columnName = pair.getColumnName();
        interfacePair.setColumnName(columnName[0]);
        interfacePair.setColumnName2(columnName[1]);
        return interfacePair;
    }

    private SunCmpMappings convertGenToInterface(com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMappings beanGraph, SunCmpMappings interfaceGraph) throws VersionNotSupportedException {
        com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMapping[] mappings = beanGraph.getSunCmpMapping();
        for (com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMapping mapping : mappings) {
            for (SunCmpMapping oldMapping : interfaceGraph.getSunCmpMapping()) {
                // remove them
                interfaceGraph.removeSunCmpMapping(oldMapping);
            }
            SunCmpMapping interfaceMapping = interfaceGraph.newSunCmpMapping();
            com.sun.jdo.api.persistence.mapping.ejb.beans.EntityMapping[] entities = mapping.getEntityMapping();
            for (com.sun.jdo.api.persistence.mapping.ejb.beans.EntityMapping entity : entities) {
                EntityMapping interfaceEntity = interfaceMapping.newEntityMapping();
                com.sun.jdo.api.persistence.mapping.ejb.beans.CmpFieldMapping[] fields = entity.getCmpFieldMapping();
                for (com.sun.jdo.api.persistence.mapping.ejb.beans.CmpFieldMapping field : fields) {
                    CmpFieldMapping interfaceField = interfaceEntity.newCmpFieldMapping();
                    interfaceField.setFieldName(field.getFieldName());
                    interfaceField.setColumnName(field.getColumnName());
                    interfaceField.setFetchedWith(convertFetched(field.getFetchedWith(), interfaceField.newFetchedWith()));
                    interfaceField.setReadOnly(field.isReadOnly());
                    interfaceEntity.addCmpFieldMapping(interfaceField);
                }
                com.sun.jdo.api.persistence.mapping.ejb.beans.CmrFieldMapping[] rels = entity.getCmrFieldMapping();
                for (com.sun.jdo.api.persistence.mapping.ejb.beans.CmrFieldMapping rel : rels) {
                    CmrFieldMapping interfaceRel = interfaceEntity.newCmrFieldMapping();
                    interfaceRel.setCmrFieldName(rel.getCmrFieldName());
                    com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair[] pairs = rel.getColumnPair();
                    for (com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair pair : pairs) {
                        interfaceRel.addColumnPair(convertPair(pair, interfaceRel.newColumnPair()));
                    }
                    interfaceRel.setFetchedWith(convertFetched(rel.getFetchedWith(), interfaceRel.newFetchedWith()));
                    interfaceEntity.addCmrFieldMapping(interfaceRel);
                }
                com.sun.jdo.api.persistence.mapping.ejb.beans.SecondaryTable[] secondaries = entity.getSecondaryTable();
                for (com.sun.jdo.api.persistence.mapping.ejb.beans.SecondaryTable secondary : secondaries) {
                    SecondaryTable interfaceST = interfaceEntity.newSecondaryTable();
                    com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair[] pairs = secondary.getColumnPair();
                    for (com.sun.jdo.api.persistence.mapping.ejb.beans.ColumnPair pair : pairs) {
                        interfaceST.addColumnPair(convertPair(pair, interfaceST.newColumnPair()));
                    }
                    interfaceST.setTableName(secondary.getTableName());
                    interfaceEntity.addSecondaryTable(interfaceST);
                }
                com.sun.jdo.api.persistence.mapping.ejb.beans.Consistency consistency = entity.getConsistency();
                if (consistency != null) {
                    Consistency interfaceConsistency = interfaceEntity.newConsistency();

                    interfaceConsistency.setCheckAllAtCommit(consistency.isCheckAllAtCommit());
                    interfaceConsistency.setCheckModifiedAtCommit(consistency.isCheckModifiedAtCommit());
                    interfaceConsistency.setLockWhenLoaded(consistency.isLockWhenLoaded());
                    interfaceConsistency.setLockWhenModified(consistency.isLockWhenModified());
                    interfaceConsistency.setNone(consistency.isNone());
                    com.sun.jdo.api.persistence.mapping.ejb.beans.CheckVersionOfAccessedInstances check = consistency.getCheckVersionOfAccessedInstances();
                    if (check != null) {
                        CheckVersionOfAccessedInstances interfaceCheck = interfaceConsistency.newCheckVersionOfAccessedInstances();
                        interfaceCheck.setColumnName(check.getColumnName());
                        interfaceConsistency.setCheckVersionOfAccessedInstances(interfaceCheck);
                    }
                    interfaceEntity.setConsistency(interfaceConsistency);
                }
                interfaceEntity.setEjbName(entity.getEjbName());
                interfaceEntity.setTableName(entity.getTableName());
                interfaceMapping.addEntityMapping(interfaceEntity);
            }
            interfaceMapping.setSchema(mapping.getSchema());
            interfaceGraph.addSunCmpMapping(interfaceMapping);
        }
        return interfaceGraph;
    }

    private MappingContext getMappingContext(SunCmpMappings beanGraph, EJBInfoHelper infoHelper) {
        if (mappingContext == null) {
            try {
                mappingContext = SunOneUtilsCMP.getMappingContext(convertInterfaceToGen(beanGraph), ejbInfoHelper);
                SunOneUtilsCMP.setExistingMappingContext(ejbInfoHelper, mappingContext);
                // iterate created MCEs and add cmpMappingListener as a
                // PropertyChangeListener
                Iterator iterator = mappingContext.getModel().getMappingCache().values().iterator();
                while (iterator.hasNext()) {
                    addMappingListener((MappingClassElement) iterator.next());
                }
            } catch (IllegalStateException ex) {
                // Oops, required file ejb-jar.xml does not exist.
                // !PW FIXME as currently written, this will popup a modal dialog, so if this exception
                // occurs during startup/project loading, the IDE's main window will be blocked by the
                // popup dialog.
//                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getLocalizedMessage()));
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            } catch (Exception ex) {
                // TODO - what is proper handling of this exception? logging?
                // TODO - narrower exceptions? (could be Model or DBException)
                // for now, returns null
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        return mappingContext;
    }

    private void addMappingListener(MappingClassElement mce) {
        // !PW mce's are not properly GC'd on project close (blame the StrongRefKey
        // bug that was never fixed properly, so this needs to be a weak listener
        // otherwise it can hold onto the project instance.
        mce.addPropertyChangeListener(WeakListeners.propertyChange(cmpMappingListener, mce));
        
        // TODO - need to add PCE listener too?  if not, maybe can
        // simplify imports and PCL implementation above
    }

    public EJBInfoHelper getEJBInfoHelper() {
        return getEJBInfoHelper(null);
    }

    public EJBInfoHelper getEJBInfoHelper(SunCmpMappings beanGraph) {
        if (ejbInfoHelper == null) {
            sourceFileMap = SourceFileMap.findSourceMap(mappingFileRoot); //getConfig().getJ2eeModule()); //getDeployableObject());
            ejbInfoHelper = new EJBDevelopmentInfoHelper(beanGraph, sourceFileMap);
        }

        return ejbInfoHelper;
    }

    private ConversionHelper getConversionHelper() {
        if (conversionHelper == null) {
            conversionHelper = getEJBInfoHelper().createConversionHelper();
        }

        return conversionHelper;
    }

    public void mapCmpBeans(FileObject sunCmpDDFO, OriginalCMPMapping[] mapping, SunCmpMappings existingMapping) {
        mappingFileRoot = sunCmpDDFO;
        /* SunCmpMappings beanGraph = (SunCmpMappings) getConfig().getBeans(
        getUriText(), CMP_MAPPING_FILE, new SunCmpMappingsParser(),
        new SunCmpMappingsRootFinder());*/
        EJBInfoHelper myInfoHelper = getEJBInfoHelper();

        // TODO: this intializes the mappingContext, but check whether
        // it is still necessary, and if so, if it must be before creation
        // of the mappingConverter. If it is no longer necessary, check whether
        // beanGraph above is needed and whether infoHelper in MappingConverter's
        // constructor can be used inline w/o the var above
        getMappingContext(existingMapping, myInfoHelper);

        MappingConverter mappingConverter = new MappingConverter(myInfoHelper, SourceFileMap.findSourceMap(mappingFileRoot)); //getConfig().getJ2eeModule())); // getDeployableObject()));
        Collection newMCEs = null;

        try {
            newMCEs = mappingConverter.toMappingClasses(mapping);
        } catch (Exception e) {
            // TODO - what is proper handling of this exception? logging?
            // TODO - narrower exceptions? (could be Model or DBException)
            // for now, newMCEs will be null and no registration of them
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        if (newMCEs != null) {
            try {
                java.util.Iterator iterator = newMCEs.iterator();
                while (iterator.hasNext()) {
                    addMappingListener((MappingClassElement)iterator.next());
                }
                firePCSChange(CMP_MAPPINGS_CHANGED, null, newMCEs);
                // TODO - probably need to reconvert suncmpmappings to interface version and
                // return it
                com.sun.jdo.api.persistence.mapping.ejb.beans.SunCmpMappings sunCmpMappings = org.netbeans.modules.j2ee.sun.persistence.mapping.ejb.util.SunOneUtilsCMP.getSunCmpMappings(mappingContext, ejbInfoHelper);
                convertGenToInterface(sunCmpMappings, existingMapping);
            } catch (VersionNotSupportedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void ensureCmpMappingExists(String beanName) {
        try {
            MappingContext myMappingContext = getMappingContext();
            EJBInfoHelper infoHelper = getEJBInfoHelper();
            ConversionHelper myConversionHelper = getConversionHelper();

            // if no corresponding MCE object, this must be a new
            // bean, create the skeleton
            if (myMappingContext.getModel().getMappingClass(myConversionHelper.getMappedClassName(beanName)) == null) {
                MappingConverter mappingConverter = new MappingConverter(infoHelper, SourceFileMap.findSourceMap(mappingFileRoot)); //getConfig().getJ2eeModule())); //getDeployableObject()));
                MappingClassElement newMCE = null;
                try {
                    newMCE = mappingConverter.toMappingClass(beanName);
                } catch (Exception e) {
                    // TODO - what is proper handling of this exception? logging?
                    // TODO - narrower exceptions? (could be Model or DBException)
                    // for now, newMCE will be null and no registration of it
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }

                if (newMCE != null) {
                    addMappingListener(newMCE);
                    firePCSChange(CMP_MAPPINGS_CHANGED, null, newMCE);
                }
            }
        } catch (NullPointerException ex) {
            // The intent of this handler is to safely report bugs in the persistence code
            // while keeping the rest of the system stable.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }

    public boolean removeMappingForCmp(SunCmpMappings sunCmpMappings, String beanName) {
        /* TBD if still need to do this to remove the listener and model cache
        try {
            if ((beanName != null) && (mappingContext != null)) {
                EJBInfoHelper infoHelper = getEJBInfoHelper();
                ConversionHelper myConversionHelper = getConversionHelper();
                Model model = mappingContext.getModel();
                String pcClassName = conversionHelper.getMappedClassName(beanName);
                MappingClassElement mce = model.getMappingClass(pcClassName);

                if (mce != null) {
                    // remove the listener then the mce from model's cache
                    mce.removePropertyChangeListener(cmpMappingListener);
                    model.updateKeyForClass(null, pcClassName);
                }
            }
        } catch (NullPointerException ex) {
            // The intent of this handler is to safely report bugs in the persistence code
            // while keeping the rest of the system stable.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        } */
        if (sunCmpMappings.sizeSunCmpMapping() > 0) {
            SunCmpMapping mapping = sunCmpMappings.getSunCmpMapping(0);
            EntityMapping entityToRemove = null;

            for (EntityMapping entity : mapping.getEntityMapping()) {
                if (entity.getEjbName().equals(beanName)) {
                    entityToRemove = entity;
                    break;
                }
            }
            if (entityToRemove != null) {
                mapping.removeEntityMapping(entityToRemove);
                return true;
            }
        }
        return false;
    }

    public boolean renameMappingForCmp(SunCmpMappings sunCmpMappings, String oldBeanName, String newBeanName) {
        // TODO - decide if listener and model cache need updating as well
        if (sunCmpMappings.sizeSunCmpMapping() > 0) {
            SunCmpMapping mapping = sunCmpMappings.getSunCmpMapping(0);

            for (EntityMapping entity : mapping.getEntityMapping()) {
                if (entity.getEjbName().equals(oldBeanName)) {
                    entity.setEjbName(newBeanName);
                    return true;
                }
            }
        }
        return false;
    }

    private void addMappingForCmrField(String beanName, String fieldName) {
        addMappingForCmpField(beanName, fieldName, true);
    }

    private void addMappingForCmpField(String beanName, String fieldName) {
        addMappingForCmpField(beanName, fieldName, false);
    }

    private void addMappingForCmpField(String beanName, String fieldName, boolean isRelationship) {
        try {
            if ((beanName != null) && (mappingContext != null)) {
                EJBInfoHelper infoHelper = getEJBInfoHelper();
                ConversionHelper myConversionHelper = getConversionHelper();
                Model model = mappingContext.getModel();
                String pcClassName = conversionHelper.getMappedClassName(beanName);
                MappingClassElement mce = model.getMappingClass(pcClassName);

                if ((mce != null) && (mce.getField(fieldName) == null)) {
                    PersistenceClassElement pce = model.getPersistenceClass(pcClassName);
                    // workaround - problem with timing - bean impl update doesn't
                    // seem to be done yet, so model's automatic field vs.
                    // rel check based on type doesn't work
                    // we can determine field vs. rel here, but coll vs. upper bound
                    // is not correct & inverse is not set
                    //model.addFieldElement(pce, fieldName);
                    // PersistenceFieldElement newPFE = pce.getField(fieldName);
                    PersistenceFieldElement newPFE = (isRelationship) ? new RelationshipElement(new RelationshipElementImpl(fieldName), pce) : new PersistenceFieldElement(new PersistenceFieldElementImpl(fieldName), pce);

                    try {
                        pce.addField(newPFE);
                        // end above timing issue
                        MappingFieldElement mfe = (isRelationship) ? new MappingRelationshipElementImpl(fieldName, mce) : new MappingFieldElementImpl(fieldName, mce);
                        mce.addField(mfe);
                    } catch (ModelException e) {
                        // TODO - what is proper handling of this exception?logging?
                        // for now, no mapping will be added
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        } catch (NullPointerException ex) {
            // The intent of this handler is to safely report bugs in the persistence code
            // while keeping the rest of the system stable.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }

    public boolean removeMappingForCmpField(SunCmpMappings sunCmpMappings, String beanName, String fieldName) {
        // TODO - decide if listener and model cache need updating as well
        if (sunCmpMappings.sizeSunCmpMapping() > 0) {
            SunCmpMapping mapping = sunCmpMappings.getSunCmpMapping(0);
            CmpFieldMapping fieldToRemove = null;

            for (EntityMapping entity : mapping.getEntityMapping()) {
                if (entity.getEjbName().equals(beanName)) {
                    CmpFieldMapping[] fields = entity.getCmpFieldMapping();
                    for (CmpFieldMapping field : fields) {
                        if (field.getFieldName().equals(fieldName)) {
                            fieldToRemove = field;
                            break;
                        }
                    }
                    if (fieldToRemove != null) {
                        entity.removeCmpFieldMapping(fieldToRemove);
                        return true;
                    }
                }
            }
        }

        return false;
    }
    private boolean removeMappingForCmpField(String beanName, String fieldName, boolean removeInverse) {
        boolean changed = false;
        // TODO - convert this to use the suncmpmappings objects instead?  if so, how to do 
        // inverses?
        try {
            if (mappingContext != null) {
                EJBInfoHelper infoHelper = getEJBInfoHelper();
                ConversionHelper myConversionHelper = getConversionHelper();
                Model model = mappingContext.getModel();
                String pcClassName = conversionHelper.getMappedClassName(beanName);
                MappingClassElement mce = model.getMappingClass(pcClassName);

                if (mce != null) {
                    MappingFieldElement mfe = mce.getField(fieldName);
                    PersistenceFieldElement pfe = model.getPersistenceClass(pcClassName).getField(fieldName);
                    RelationshipElement inverse = (removeInverse && (pfe instanceof RelationshipElement)) ? ((RelationshipElement) pfe).getInverseRelationship(model) : null;

                    try {
                        model.removeFieldElement(pfe);
                        if (mfe != null) {
                            mce.removeField(mfe);
                            changed = true;
                        }
                        if (inverse != null) {
                            String inverseName = inverse.getName();
                            MappingClassElement inverseMCE = model.getMappingClass(inverse.getDeclaringClass().getName());
                            MappingFieldElement inverseMFE = inverseMCE.getField(inverseName);

                            model.removeFieldElement(inverse);
                            if (inverseMFE != null) {
                                inverseMCE.removeField(inverseMFE);
                                changed = true;
                            }
                        }
                    } catch (ModelException e) {
                        // TODO - what is proper handling of this exception?logging?
                        // for now, no mapping will be removed
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        } catch (NullPointerException ex) {
            // The intent of this handler is to safely report bugs in the persistence code
            // while keeping the rest of the system stable.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
        return false;
    }

    public boolean renameMappingForCmpField(SunCmpMappings sunCmpMappings, String beanName, 
            String oldFieldName, String newFieldName) {
        // TODO - decide if listener and model cache need updating as well
        if (sunCmpMappings.sizeSunCmpMapping() > 0) {
            SunCmpMapping mapping = sunCmpMappings.getSunCmpMapping(0);

            for (EntityMapping entity : mapping.getEntityMapping()) {
                if (entity.getEjbName().equals(beanName)) {
                    CmpFieldMapping[] fields = entity.getCmpFieldMapping();
                    for (CmpFieldMapping field : fields) {
                        if (field.getFieldName().equals(oldFieldName)) {
                            field.setFieldName(newFieldName);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void renameMappingForCmpField(String beanName, String oldFieldName, String newFieldName) {
        try {
            if ((beanName != null) && (mappingContext != null)) {
                EJBInfoHelper infoHelper = getEJBInfoHelper();
                ConversionHelper myConversionHelper = getConversionHelper();
                Model model = mappingContext.getModel();
                String pcClassName = conversionHelper.getMappedClassName(beanName);
                MappingClassElement mce = model.getMappingClass(pcClassName);

                if (mce != null) {
                    MappingFieldElement mfe = mce.getField(oldFieldName);
                    PersistenceFieldElement pfe = model.getPersistenceClass(pcClassName).getField(oldFieldName);

                    try {
                        if (mfe != null) {
                            mfe.setName(newFieldName);
                        }

                        if (pfe != null) {
                            pfe.setName(newFieldName);
                            if (pfe instanceof RelationshipElement) {
                                RelationshipElement relationship = (RelationshipElement) pfe;
                                RelationshipElement inverse = relationship.getInverseRelationship(model);
                                if (inverse != null) {
                                    inverse.setInverseRelationship(relationship, model);
                                }
                            }
                        }
                    } catch (ModelException e) {
                        // TODO - what is proper handling of this exception?logging?
                        // for now, no mapping will be renamed
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        } catch (NullPointerException ex) {
            // The intent of this handler is to safely report bugs in the persistence code
            // while keeping the rest of the system stable.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }

}
