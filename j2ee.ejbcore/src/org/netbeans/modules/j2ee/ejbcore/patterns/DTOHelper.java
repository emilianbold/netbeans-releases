/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.patterns;


import java.util.ArrayList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * DTO helper class
 * @author blaha
 */
public class DTOHelper {
    private Project project;
    private EjbJar ejbJar;
    private Entity entity;
    private JavaClass classElm;
    private EnterpriseBeans beans;
    private FileObject fo;
    
    /** Create new instance of Data Transfer Object helper class
     * @param me <code>MemberElement</code> that represents entity implementation class
     */
    public DTOHelper(Feature feature) {
        classElm = JMIUtils.getDeclaringClass(feature);
        DataObject dObj = JavaMetamodel.getManager().getDataObject(feature.getResource());
        assert dObj != null : "Can not find data object for class";
        
        fo = dObj.getPrimaryFile();
        project = FileOwnerQuery.getOwner(fo);
        
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fo);
        
        
        DDProvider provider = DDProvider.getDefault();
        try{
            ejbJar = provider.getDDRoot(ejbModule.getDeploymentDescriptor());
            beans = ejbJar.getEnterpriseBeans();
            
            entity = (Entity) beans.findBeanByName(
                    EnterpriseBeans.ENTITY,
                    Ejb.EJB_CLASS, classElm.getName());
        }catch(java.io.IOException ex) {
            // IO error while reading DD
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /** Get CMR fields for entity bean
     * @return CmrField[] array
     */
    public CmrField[] getCmrFields() {
        ArrayList cmrFields = new ArrayList();
        EjbRelation[] r = getRelation();
        if(r != null){
            int j = 0;
            for(int i = 0; i < r.length; i++) {
                EjbRelationshipRole role = r[i].getEjbRelationshipRole();
                if(isUseEjb(role)) {
                    cmrFields.add(role.getCmrField());
                }
                role = r[i].getEjbRelationshipRole2();
                if(isUseEjb(role)) {
                    cmrFields.add(role.getCmrField());
                }
            }
        }
        return (CmrField[])cmrFields.toArray();
    }
    
    /* Get all relations in EJB jar
     * @return EjbRelation[]
     */
    private EjbRelation[] getRelation() {
        Relationships relation = ejbJar.getSingleRelationships();
        return (relation != null) ? relation.getEjbRelation() : null;
    }
    
    /* Test whether the CMR field is multiple relation
     * @param cmrField
     * @return true whether the field is multiple
     */
    public boolean isMultiple(CmrField cmrField) {
        boolean isMultiple = false;
        EjbRelation[] r = getRelation();
        if(r != null){
            for(int i = 0; i < r.length; i++) {
                EjbRelationshipRole role = r[i].getEjbRelationshipRole();
                if(cmrField == role.getCmrField() &&
                        role.MULTIPLICITY_MANY.equals(r[i].getEjbRelationshipRole2().
                        getMultiplicity())) {
                    isMultiple = true;
                }
                role = r[i].getEjbRelationshipRole2();
                if(cmrField == role.getCmrField() &&
                        role.MULTIPLICITY_MANY.equals(r[i].getEjbRelationshipRole().
                        getMultiplicity())) {
                    isMultiple = true;
                }
            }
        }
        return isMultiple;
    }
    
    /* Get field type of opposite field in releation
     * @param CmrField
     * @return Field type of opposite field
     */
    public String getOppositeFieldType(CmrField cmrField){
        String ejbName;
        String cmrFieldType2 = "";
        EjbRelation[] r = getRelation();
        if(r != null) {
            for(int i = 0; i < r.length; i++) {
                EjbRelationshipRole role = r[i].getEjbRelationshipRole();
                if(cmrField == role.getCmrField()) {
                    ejbName = r[i].getEjbRelationshipRole2().getRelationshipRoleSource().getEjbName();
                    cmrFieldType2 = findLocalIntNameByEntityName(ejbName);
                    break;
                }
                role = r[i].getEjbRelationshipRole2();
                if(cmrField == role.getCmrField()) {
                    ejbName = r[i].getEjbRelationshipRole().getRelationshipRoleSource().getEjbName();
                    cmrFieldType2 = findLocalIntNameByEntityName(ejbName);
                    break;
                }
            }
        }
        return cmrFieldType2;
    }
    
    /* Find local interface name by Entity name
     * @param entityName name
     * @return local interface name
     */
    public String findLocalIntNameByEntityName(String entityName) {
        Entity[] ents = beans.getEntity();
        String localInterfaceName = "";
        for(int i = 0; i < ents.length; i++) {
            if(entityName.equals(ents[i].getEjbName())) {
                localInterfaceName = ents[i].getLocal();
                break;
            }
        }
        return localInterfaceName;
    }
    
    /* Find entity name by local interface name
     * @param interf local interface name
     * @return entity name
     */
    public String findEntityNameByLocalInt(String interf) {
        Entity[] ents = beans.getEntity();
        String ejbName = "";
        for(int i = 0; i < ents.length; i++) {
            if(interf.equals(ents[i].getLocal())) {
                ejbName = ents[i].getEjbName();
                break;
            }
        }
        return ejbName;
    }
    
    /* Check whether the releation role belong to the entity
     * @param role in relationship
     * @return true whether relation role belong to the entity bean
     */
    private boolean isUseEjb(EjbRelationshipRole role) {
        return role != null &&
                role.getRelationshipRoleSource() != null &&
                role.getRelationshipRoleSource().getEjbName().equals(getEntityName());
    }
    
    /* Get all CMP fields in entity bean
     * @return CmpField array
     */
    public CmpField[] getCmpFields() {
        return entity.getCmpField();
    }
    
    /* Get field type of CMP field, e.g. String, int, ...
     * @param fieldName field name
     * @return field type
     */
    public String getFieldType(String fieldName) {
        String returnType = "";
        returnType = EntityMethodController.getGetterMethod(
                classElm, fieldName).getType().getName();
        return returnType;
    }
    
    /* Get entity name
     * @return entity name
     */
    //TODO method could be moved to some Utils method
    public String getEntityName() {
        return entity.getEjbName();
    }
    
    /* Get local interface name
     * @return local interface name
     */
    public String getLocalName(){
        return entity.getLocal();
    }
    
    /* Get full name of entity bean, e.g org.netbeans.entity.XXXX
     * @return full entity bean name
     */
    public String getFullName() {
        return classElm.getName();
    }
    
    /* Get package of entity bean class
     * @return package name
     */
    public String getPackage(){
        return getFullName().substring(0,
                getFullName().lastIndexOf('.'));
    }
    
    /* Get project where the entity bean is located
     * @return project
     */
    public Project getProject() {
        return project;
    }
    
    /* Get source root where the entity bean is located
     * @return source root
     */
    public SourceGroup getSourceGroup(){
        SourceGroup entityFolder = null;
        SourceGroup[] folders= Util.getJavaSourceGroups(project);

        for(int i = 0; i < folders.length; i++){
            if(folders[i].contains(fo)){
                entityFolder = folders[i];
            }
         }
        return entityFolder;
    }
}
