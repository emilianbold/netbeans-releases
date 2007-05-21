/*
 * ModelUtils.java
 *
 * Created on April 23, 2007, 3:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.jpa.model;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
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
    
    public static TypeElement getTypeElementFromModel(CompilationInfo info, Object modelElement){
        String className = null;
        
        if (modelElement instanceof Entity){
            className = ((Entity)modelElement).getClass2();
        }
        
        if (className != null){
            return info.getElements().getTypeElement(className);
        }
        
        return null;
    }
    
    public static void resolveJavaElementFromModel(JPAProblemContext problemCtx, AttributeWrapper attr){
        List <? extends Element> elementsToSearch = null;
        String searchedName = null;
        
        if (problemCtx.getAccessType() == AccessType.FIELD){
            searchedName = attr.getName();
            elementsToSearch = ElementFilter.fieldsIn(problemCtx.getJavaClass().getEnclosedElements());
        }
        
        else if (problemCtx.getAccessType() == AccessType.PROPERTY){
            searchedName = getAccesorName(attr.getName());
            elementsToSearch = ElementFilter.methodsIn(problemCtx.getJavaClass().getEnclosedElements());
        }
        
        if (searchedName != null){
            for (Element elem : elementsToSearch){
                if (elem.getSimpleName().contentEquals(searchedName)){
                    attr.setJavaElement(elem);
                }
            }
        }
    }
    
    public static ExecutableElement getAccesor(TypeElement clazz, String fieldName){
        for (ExecutableElement method : getMethod(clazz, getAccesorName(fieldName))){
            if (method.getParameters().size() == 0){
                return method;
            }
        }
        
        return null;
    }
    
    public static ExecutableElement[] getMethod(TypeElement clazz, String methodName){
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
        
        for (ExecutableElement method : ElementFilter.methodsIn(clazz.getEnclosedElements())){
            if (method.getSimpleName().contentEquals(methodName)){
                methods.add(method);
            }
        }
        
        return methods.toArray(new ExecutableElement[methods.size()]);
    }
    
    public static String getAccesorName(String fieldName){
        return "get" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase() +
                fieldName.substring(1);
    }
    
    public static String getFieldNameFromAccessor(String accessorName){
        if (!accessorName.startsWith("get")){ //NOI18N
            throw new IllegalArgumentException("accessor name must start with 'get'");
        }
        
        return String.valueOf(accessorName.charAt(3)).toLowerCase() + accessorName.substring(4);
    }
    
    public static VariableElement getField(TypeElement clazz, String fieldName){
        for (VariableElement field : ElementFilter.fieldsIn(clazz.getEnclosedElements())){
            if (field.getSimpleName().contentEquals(fieldName)){
                return field;
            }
        }
        
        return null;
    }
}
