/*
 * ModelUtils.java
 *
 * Created on April 23, 2007, 3:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.jpa.model;

import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.IdClass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;

/**
 *
 * @author Tomasz.Slota@SUN.COM
 */
public class ModelUtils {
    
    public static Entity getEntity(EntityMappingsMetadata metadata, TypeElement clazz){
        for (Entity entity: metadata.getRoot().getEntity()){
            if (clazz.getQualifiedName().contentEquals(entity.getClass2())){
                return entity;
            }
        }
        return null;
    }
    
    public static Embeddable getEmbeddable(EntityMappingsMetadata metadata, TypeElement clazz){
        for (Embeddable embeddable: metadata.getRoot().getEmbeddable()){
            if (clazz.getQualifiedName().contentEquals(embeddable.getClass2())){
                return embeddable;
            }
        }
        return null;
    }
    
    public static MappedSuperclass getMappedSuperclass(EntityMappingsMetadata metadata, TypeElement clazz){
        for (MappedSuperclass mappedSuperclass: metadata.getRoot().getMappedSuperclass()){
            if (clazz.getQualifiedName().contentEquals(mappedSuperclass.getClass2())){
                return mappedSuperclass;
            }
        }
        return null;
    }
   
}
