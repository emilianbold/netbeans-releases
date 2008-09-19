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

import org.netbeans.modules.websvc.rest.support.*;
import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.util.Exceptions;

/**
 *
 * @author PeterLiu
 */
public class EntityClassInfo {

    private EntityResourceModelBuilder builder;
    private final Entity entity;
    private JavaSource entitySource;
    private String name;
    private String type;
    private String packageName;
    private Collection<FieldInfo> fieldInfos;
    private FieldInfo idFieldInfo;

    /** Creates a new instance of ClassInfo */
    public EntityClassInfo(Entity entity, Project project, EntityResourceModelBuilder builder, JavaSource source) {
        this.entity = entity;
        this.entitySource = source;
        this.fieldInfos = new ArrayList<FieldInfo>();
        this.builder = builder;

        extractFields(project);

        if (idFieldInfo != null && idFieldInfo.isEmbeddedId()) {
            extractPKFields(project);
        }
    }
    
    protected void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    protected void setType(String type) {
        this.type = type;
    }
    
    protected void extractFields(Project project) {
        try {
            final JavaSource source = entitySource;
            source.runUserActionTask(new AbstractTask<CompilationController>() {

                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.RESOLVED);
                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(controller);
                    assert controller.getCompilationUnit() != null : source.getFileObjects().iterator().next().getPath();
                    assert controller.getCompilationUnit().getPackageName() != null : "NULL package " + source.getFileObjects().iterator().next().getPath();
                    packageName = controller.getCompilationUnit().getPackageName().toString();
                    name = tree.getSimpleName().toString();
                    type = packageName + "." + name;

                    TypeElement classElement = JavaSourceHelper.getTopLevelClassElement(controller);
                    
                    if (useFieldAccess(classElement)) {
                        extractFields(classElement);
                    } else {
                        extractFieldsFromMethods(classElement);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void extractFields(TypeElement typeElement) {
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.VOLATILE) || modifiers.contains(Modifier.FINAL)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();
            
            for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
                fieldInfo.addAnnotation(annotation.toString());
            }

            if (!fieldInfo.isPersistent()) continue;
            
            fieldInfos.add(fieldInfo);
            fieldInfo.setName(field.getSimpleName().toString());

            TypeMirror fieldType = field.asType();

            if (fieldType.getKind() == TypeKind.DECLARED) {
                DeclaredType declType = (DeclaredType) fieldType;
                fieldInfo.setType(declType.asElement().toString());
               
                for (TypeMirror arg : declType.getTypeArguments()) {
                    fieldInfo.setTypeArg(arg.toString());
                }
            } else {
                fieldInfo.setType(fieldType.toString());
            }

            if (fieldInfo.isId()) {
                idFieldInfo = fieldInfo;
            }
        }
    }
    
    protected void extractFieldsFromMethods(TypeElement typeElement) {
        List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());

        for (ExecutableElement method : methods) {
            Set<Modifier> modifiers = method.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();

             for (AnnotationMirror annotation : method.getAnnotationMirrors()) {
                fieldInfo.addAnnotation(annotation.toString());
            }
             
            if (!fieldInfo.isPersistent() || !fieldInfo.hasPersistenceAnnotation()) continue;
                
            fieldInfos.add(fieldInfo);
            String name = method.getSimpleName().toString();
            if (name.startsWith("get")) {       //NOI18N
                name = name.substring(3);
                name = Util.lowerFirstChar(name);
            }
            fieldInfo.setName(name);

            TypeMirror returnType = method.getReturnType();

            if (returnType.getKind() == TypeKind.DECLARED) {
                DeclaredType declType = (DeclaredType) returnType;

                fieldInfo.setType(declType.asElement().toString());

                for (TypeMirror arg : declType.getTypeArguments()) {
                    fieldInfo.setTypeArg(arg.toString());
                }
            } else {
                fieldInfo.setType(returnType.toString());
            }

            if (fieldInfo.isId()) {
                idFieldInfo = fieldInfo;
            }
        }
    }
    
    protected void extractPKFields(Project project) {
        try {
            JavaSource pkSource = SourceGroupSupport.getJavaSourceFromClassName(idFieldInfo.getType(), project);
            if (pkSource == null) {
                throw new IllegalArgumentException("No java source for "+idFieldInfo.getType());
            }
            pkSource.runUserActionTask(new AbstractTask<CompilationController>() {

                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.RESOLVED);

                    TypeElement classElement = JavaSourceHelper.getTopLevelClassElement(controller);
                    extractPKFields(classElement);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void extractPKFields(TypeElement typeElement) {
        List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.VOLATILE) || modifiers.contains(Modifier.FINAL)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();

            idFieldInfo.addFieldInfo(fieldInfo);
            fieldInfo.setName(field.getSimpleName().toString());

            TypeMirror fieldType = field.asType();

            if (fieldType.getKind() == TypeKind.DECLARED) {
                DeclaredType declType = (DeclaredType) fieldType;

                fieldInfo.setType(declType.asElement().toString());

                for (TypeMirror arg : declType.getTypeArguments()) {
                    fieldInfo.setTypeArg(arg.toString());
                }
            } else {
                fieldInfo.setType(fieldType.toString());
            }
        }
    }
    
    private boolean useFieldAccess(TypeElement typeElement) {
         List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

        for (VariableElement field : fields) {
            Set<Modifier> modifiers = field.getModifiers();
            if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.VOLATILE) || modifiers.contains(Modifier.FINAL)) {
                continue;
            }

            FieldInfo fieldInfo = new FieldInfo();
            
            for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
                fieldInfo.addAnnotation(annotation.toString());
            }

            if (fieldInfo.isPersistent() && fieldInfo.hasPersistenceAnnotation()) 
                return true;
        }
        
        return false;
    }
    
    public Entity getEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public FieldInfo getIdFieldInfo() {
        return idFieldInfo;
    }

    public Collection<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityClassInfo other = (EntityClassInfo) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.packageName != other.packageName && (this.packageName == null || !this.packageName.equals(other.packageName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 47 * hash + (this.packageName != null ? this.packageName.hashCode() : 0);
        return hash;
    }

    public Set<EntityClassInfo> getEntityClosure(Set<EntityClassInfo> result) {
        if (result.contains(this)) {
            return result;
        }
        result.add(this);
        for (EntityClassInfo info : getRelatedEntities()) {
            result.addAll(info.getEntityClosure(result));
        }
        return result;
    }
    private Set<EntityClassInfo> relatedEntities;

    public Set<EntityClassInfo> getRelatedEntities() {
        if (relatedEntities != null) {
            return relatedEntities;
        }
        relatedEntities = new HashSet<EntityClassInfo>();
        Set<String> allEntityNames = builder.getAllEntityNames();
        for (FieldInfo fi : fieldInfos) {
            String type = fi.getType();
            String typeArg = fi.getTypeArg();
            if (type != null && allEntityNames.contains(type)) {
                relatedEntities.add(builder.getEntityClassInfo(type));
            } else if (typeArg != null && allEntityNames.contains(typeArg)) {
                relatedEntities.add(builder.getEntityClassInfo(typeArg));
            }
        }
        return relatedEntities;
    }

    public class FieldInfo {

        private String name;
        private String type;
        private String simpleTypeName;
        private String typeArg;
        private String simpleTypeArgName;
        private List<String> annotations;
        private Collection<FieldInfo> fieldInfos;

        public FieldInfo() {
            annotations = new ArrayList<String>();
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(String type) {
            Class primitiveType = Util.getPrimitiveType(type);
          
            if (primitiveType != null) {
                this.type = primitiveType.getSimpleName();
                this.simpleTypeName = primitiveType.getSimpleName();
            } else {
                this.type = type;
                this.simpleTypeName = type.substring(type.lastIndexOf(".") + 1);
            }
        }

        public String getType() {
            return type;
        }

        public String getSimpleTypeName() {
            return simpleTypeName;
        }

        public void setTypeArg(String typeArg) {
            this.typeArg = typeArg;
            this.simpleTypeArgName = typeArg.substring(typeArg.lastIndexOf(".") + 1);
        }

        public String getTypeArg() {
            return typeArg;
        }
        
        public String getSimpleTypeArgName() {
            return simpleTypeArgName;
        }

        public void addAnnotation(String annotation) {
            this.annotations.add(annotation);
        }

        public boolean isPersistent() {
            return !matchAnnotation("@javax.persistence.Transient");
        }
        
        public boolean hasPersistenceAnnotation() {
            return matchAnnotation("@javax.persistence.");
        }
        
        public boolean isId() {
            return matchAnnotation("@javax.persistence.Id") || matchAnnotation("@javax.persistence.EmbeddedId"); //NOI18N
        }
        
        public boolean isGeneratedValue() {
            return matchAnnotation("@javax.persistence.GeneratedValue");        //NOI18N
        }

        public boolean isEmbeddedId() {
            return matchAnnotation("@javax.persistence.EmbeddedId"); //NOI18N

        }

        public boolean isRelationship() {
            return isOneToOne() || isOneToMany() || isManyToOne() || isManyToMany();
        }

        public boolean isOneToOne() {
            return matchAnnotation("@javax.persistence.OneToOne"); //NOI18N

        }

        public boolean isOneToMany() {
            return matchAnnotation("@javax.persistence.OneToMany"); //NOI18N

        }

        public boolean isManyToOne() {
            return matchAnnotation("@javax.persistence.ManyToOne"); //NOI18N

        }

        public boolean isManyToMany() {
            return matchAnnotation("@javax.persistence.ManyToMany"); //NOI18N

        }

        private boolean matchAnnotation(String annotation) {
            for (String a : annotations) {
                if (a.startsWith(annotation)) {
                    return true;
                }
            }

            return false;
        }

        public void addFieldInfo(FieldInfo info) {
            if (fieldInfos == null) {
                fieldInfos = new ArrayList<FieldInfo>();
            }

            fieldInfos.add(info);
        }

        public Collection<FieldInfo> getFieldInfos() {
            return fieldInfos;
        }
    }
}