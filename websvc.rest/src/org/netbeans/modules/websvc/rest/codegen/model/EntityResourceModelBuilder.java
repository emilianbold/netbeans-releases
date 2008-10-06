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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.rest.codegen.model;

import org.openide.util.Exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBean.Type;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 *
 * @author PeterLiu
 */
public class EntityResourceModelBuilder {
    private Map<String, EntityClassInfo>  entityClassInfoMap;
    EntityResourceBeanModel model;
    
    /** Creates a new instance of ModelBuilder */
    public EntityResourceModelBuilder(Project project, Collection<Entity> entities) {
        entityClassInfoMap = new HashMap<String, EntityClassInfo>();
        for (Entity entity : entities) {
            try {
                EntityClassInfo info = null;
                JavaSource js = SourceGroupSupport.getJavaSourceFromClassName(entity.getClass2(), project);
             
                if (js != null) {
                    info = new EntityClassInfo(entity, project, this, js);
                } else if (entity instanceof RuntimeJpaEntity) {
                    info = new RuntimeEntityClassInfo((RuntimeJpaEntity)entity, project, this);
                }
                if (info != null) {
                    entityClassInfoMap.put(entity.getClass2(), info);
                }
            } catch(IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    public List<Entity> getEntities() {
        List<Entity> ret = new ArrayList<Entity>();
        for (EntityClassInfo info : entityClassInfoMap.values()) {
            ret.add(info.getEntity());
        }
        return ret;
    }
    
    public Set<EntityClassInfo> getEntityInfos() {
        return new HashSet<EntityClassInfo>(entityClassInfoMap.values());
    }
    
    public Set<String> getAllEntityNames() {
        return entityClassInfoMap.keySet();
    }
    
    public EntityClassInfo getEntityClassInfo(String type) {
        return entityClassInfoMap.get(type);
    }
    
    public EntityResourceBeanModel build(Collection<Entity> selected) {
        model = new EntityResourceBeanModel(this);
        try {
            for (Entity entity : selected) {
                EntityClassInfo info = entityClassInfoMap.get(entity.getClass2());
                getContainerResourceBean(info);
            }
            
            model.setValid(true);
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            model.setValid(false);
        }
        
        return model;
    }
    
    private EntityResourceBean getContainerResourceBean(EntityClassInfo info) {
        EntityResourceBean bean = model.getContainerResourceBean(info);
        
        if (bean == null) {
            bean = createContainerResourceBean(info);
        }
        
        return bean;
    }
    
    private EntityResourceBean createContainerResourceBean(EntityClassInfo info) { 
        EntityResourceBean containerBean = new EntityResourceBean(Type.CONTAINER);
        
        String pluralName = Util.pluralize(info.getName());
        containerBean.setName(pluralName);
        containerBean.setEntityClassInfo(info);
        containerBean.setUriTemplate("/" + Util.lowerFirstChar(pluralName) + "/"); //NOI18N
        
        model.addContainerResourceBean(containerBean);
       
        EntityResourceBean itemBean = getItemResourceBean(info);
        
        containerBean.addSubResource(new RelatedEntityResource(itemBean, info.getIdFieldInfo(), null));
        
        return containerBean;
    }
    
    
    private EntityResourceBean getItemResourceBean(EntityClassInfo info) {
        EntityResourceBean bean = model.getItemResourceBean(info);
        
        if (bean == null) {
            bean = createItemResourceBean(info);
        }
        
        return bean;
    }
    
    private EntityResourceBean createItemResourceBean(EntityClassInfo info) {
        EntityResourceBean itemBean = new EntityResourceBean(Type.ITEM);
        
        itemBean.setName(info.getName());
        itemBean.setEntityClassInfo(info);
        FieldInfo idField = info.getIdFieldInfo();
        String uriTemplate = "";
        
        if (!idField.isEmbeddedId()) {
            uriTemplate = "{" + info.getIdFieldInfo().getName() + "}/";   //NOI18N
        } else {
            int count = 0;
            for (FieldInfo field : idField.getFieldInfos()) {      
                if (count++ > 0) {
                    uriTemplate += ",";     //NOI18N
                }
                
                uriTemplate += "{" + field.getName() + "}";     //NOI18N
            }
            
            uriTemplate += "/";     //NOI18N
        }
        
        itemBean.setUriTemplate(uriTemplate);
        
        model.addItemResourceBean(itemBean);
        
        computeRelationship(itemBean, info);
        
        return itemBean;
    }
    
    private void computeRelationship(EntityResourceBean bean, EntityClassInfo info) {
        String entityClassName = info.getName();
     
        for (FieldInfo fieldInfo : info.getFieldInfos()) {
            if (fieldInfo.isRelationship()) {
                EntityResourceBean foreignBean = null;
                EntityResourceBean foreignItemBean = null;
  
                if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                    foreignBean = getContainerResourceBean(entityClassInfoMap.get(fieldInfo.getTypeArg()));
                    foreignItemBean = getItemResourceBean(entityClassInfoMap.get((fieldInfo.getTypeArg())));
                } else {
                    foreignBean = getItemResourceBean(entityClassInfoMap.get(fieldInfo.getType()));
                }
     
                FieldInfo reverseFieldInfo = null;
   
                for (FieldInfo f : foreignBean.getEntityClassInfo().getFieldInfos()) {
                    if (f.isOneToOne() || f.isManyToOne()) {
                        if (f.getSimpleTypeName().equals(entityClassName)) {
                            reverseFieldInfo = f;
                            break;
                        }
                    } else if (f.isOneToMany() || f.isManyToMany()) {
                        if (f.getSimpleTypeArgName().equals(entityClassName)) {
                            reverseFieldInfo = f;
                            break;
                        }
                    }
                }
                RelatedEntityResource subResource = new RelatedEntityResource(foreignBean, fieldInfo, reverseFieldInfo);
                bean.addSubResource(subResource);
                
                RelatedEntityResource superResource = new RelatedEntityResource(bean, reverseFieldInfo, fieldInfo);
                foreignBean.addSuperResource(superResource);
                
                if (foreignItemBean != null) {
                    foreignItemBean.addSuperResource(superResource);
                }
            }
        }
    }
}
