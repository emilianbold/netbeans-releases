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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.ejbcore.patterns;


import java.util.ArrayList;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
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
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * DTO helper class
 * @author blaha
 */
public class DTOHelper {
    private final Project project;
    private EjbJar ejbJar;
    private Entity entity;
    private final TypeElement classElm;
    private EnterpriseBeans beans;
    private final FileObject fileObject;
    
    /** Create new instance of Data Transfer Object helper class
     * @param me <code>MemberElement</code> that represents entity implementation class
     */
    public DTOHelper(CompilationController controller, Element feature) {
        classElm = (TypeElement) feature.getEnclosingElement();
        fileObject = controller.getFileObject();
        project = FileOwnerQuery.getOwner(fileObject);
        
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        
        
        DDProvider provider = DDProvider.getDefault(); // EJB 2.1
        try{
            ejbJar = provider.getDDRoot(ejbModule.getDeploymentDescriptor());
            beans = ejbJar.getEnterpriseBeans();
            
            entity = (Entity) beans.findBeanByName(
                    EnterpriseBeans.ENTITY,
                    Ejb.EJB_CLASS, classElm.getQualifiedName().toString());
        }catch(java.io.IOException ex) {
            // IO error while reading DD
            Exceptions.printStackTrace(ex);
        }
    }
    
    /** Get CMR fields for entity bean
     * @return CmrField[] array
     */
    public CmrField[] getCmrFields() {
        ArrayList<CmrField> cmrFields = new ArrayList<CmrField>();
        EjbRelation[] ejbRelations = getRelation();
        if(ejbRelations != null){
            for (EjbRelation ejbRelation : ejbRelations) {
                EjbRelationshipRole role = ejbRelation.getEjbRelationshipRole();
                if(isUseEjb(role)) {
                    cmrFields.add(role.getCmrField());
                }
                role = ejbRelation.getEjbRelationshipRole2();
                if(isUseEjb(role)) {
                    cmrFields.add(role.getCmrField());
                }
            }
        }
        return cmrFields.toArray(new CmrField[0]);
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
        EjbRelation[] ejbRelations = getRelation();
        if(ejbRelations != null){
            for (EjbRelation ejbRelation : ejbRelations) {
                EjbRelationshipRole role = ejbRelation.getEjbRelationshipRole();
                if(cmrField == role.getCmrField() &&
                        role.MULTIPLICITY_MANY.equals(ejbRelation.getEjbRelationshipRole2().
                        getMultiplicity())) {
                    isMultiple = true;
                }
                role = ejbRelation.getEjbRelationshipRole2();
                if(cmrField == role.getCmrField() &&
                        role.MULTIPLICITY_MANY.equals(ejbRelation.getEjbRelationshipRole().
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
        EjbRelation[] ejbRelations = getRelation();
        if(ejbRelations != null) {
            for (EjbRelation ejbRelation : ejbRelations) {
                EjbRelationshipRole role = ejbRelation.getEjbRelationshipRole();
                if(cmrField == role.getCmrField()) {
                    ejbName = ejbRelation.getEjbRelationshipRole2().getRelationshipRoleSource().getEjbName();
                    cmrFieldType2 = findLocalIntNameByEntityName(ejbName);
                    break;
                }
                role = ejbRelation.getEjbRelationshipRole2();
                if(cmrField == role.getCmrField()) {
                    ejbName = ejbRelation.getEjbRelationshipRole().getRelationshipRoleSource().getEjbName();
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
        //TODO: RETOUCHE
//        returnType = EntityMethodController.getGetterMethod(
//                classElm, fieldName).getType().getName();
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
        return classElm.getQualifiedName().toString();
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
        SourceGroup[] folders= SourceGroups.getJavaSourceGroups(project);

        for(int i = 0; i < folders.length; i++){
            if(folders[i].contains(fileObject)){
                entityFolder = folders[i];
            }
         }
        return entityFolder;
    }
}
