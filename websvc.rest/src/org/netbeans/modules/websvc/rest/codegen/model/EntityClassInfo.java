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

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.*;
import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
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
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author PeterLiu
 */
public class EntityClassInfo {

    private static final String JAVAX_PERSISTENCE = "javax.persistence.";//NOI18N
    
    private static final Set<String> LIFECYCLE_ANNOTATIONS = new HashSet<String>(7);
    static {
        LIFECYCLE_ANNOTATIONS.add("PrePersist");    // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostPersist");   // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PreRemove");     // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostRemove");    // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PreUpdate");     // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostUpdate");    // NOI18N
        LIFECYCLE_ANNOTATIONS.add("PostLoad");      // NOI18N
        }; 
    
    private EntityResourceModelBuilder builder;
    private final String entityFqn;
    private JavaSource entitySource;
    private String name;
    private String type;
    private String packageName;
    private Collection<FieldInfo> fieldInfos;
    private FieldInfo idFieldInfo;

    /** Creates a new instance of ClassInfo */
    public EntityClassInfo(String entityFqn, Project project, 
            EntityResourceModelBuilder builder, JavaSource source) 
    {
        this.entityFqn = entityFqn;
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

            fieldInfo.parseAnnotations(field.getAnnotationMirrors());

            if (!fieldInfo.isPersistent()) {
                continue;
            }

            fieldInfos.add(fieldInfo);
            fieldInfo.setName(field.getSimpleName().toString());
            fieldInfo.setType(field.asType());

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

            fieldInfo.parseAnnotations(method.getAnnotationMirrors());

            if (!fieldInfo.isPersistent() || !fieldInfo.hasPersistenceAnnotation()) {
                continue;
            }

            fieldInfos.add(fieldInfo);
            String name = method.getSimpleName().toString();
            if (name.startsWith("get")) {       //NOI18N
                name = name.substring(3);
                name = Util.lowerFirstChar(name);
            }

            fieldInfo.setName(name);
            fieldInfo.setType(method.getReturnType());

            if (fieldInfo.isId()) {
                idFieldInfo = fieldInfo;
            }
        }
    }

    protected void extractPKFields(Project project) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null) {
            FileObject root = restSupport.findSourceRoot();
            if (root != null) {
                try {
                    final ClasspathInfo cpInfo = ClasspathInfo.create(root);
                    JavaSource pkSource = JavaSource.create(cpInfo);
                    if (pkSource == null) {
                        throw new IllegalArgumentException("No JavaSource object for " + idFieldInfo.getType());
                    }
                    pkSource.runUserActionTask(new AbstractTask<CompilationController>() {
                        @Override
                        public void run(CompilationController controller) throws IOException {
                            controller.toPhase(Phase.RESOLVED);
                            TypeElement classElement = controller.getElements().getTypeElement(idFieldInfo.getType());
                            extractPKFields(classElement);
                        }
                    }, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                throw new IllegalArgumentException("No source root for " + project.getProjectDirectory().getName());
            }
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
            fieldInfo.setType(field.asType());
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

            fieldInfo.parseAnnotations(field.getAnnotationMirrors());

            if (fieldInfo.isPersistent() && fieldInfo.hasPersistenceAnnotation()) {
                return true;
            }
        }

        return false;
    }

    public String getEntityFqn() {
        return entityFqn;
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

    public FieldInfo getFieldInfoByName(String name) {
        for (FieldInfo f : fieldInfos) {
            if (f.getName().equals(name))
                return f;
        }

        return null;
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

    public static class FieldInfo {

        private enum Relationship {

            OneToOne, OneToMany, ManyToOne, ManyToMany
        };
        private String name;
        private String type;
        private String simpleTypeName;
        private String typeArg;
        private String simpleTypeArgName;
        private Relationship relationship;
        private boolean isPersistent = true;
        private boolean hasPersistenceAnnotation = false;
        private boolean isId = false;
        private boolean isEmbeddedId = false;
        private boolean isGeneratedValue = false;
        private String mappedBy = null;
        private Collection<FieldInfo> fieldInfos;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(TypeMirror type) {
            if (type.getKind() == TypeKind.DECLARED) {
                DeclaredType declType = (DeclaredType) type;
                setType(declType.asElement().toString());

                for (TypeMirror arg : declType.getTypeArguments()) {
                    setTypeArg(arg.toString());
                }
            } else {
                setType(type.toString());
            }
        }

        private void setType(String type) {
            Class primitiveType = Util.getPrimitiveType(type);

            if (primitiveType != null) {
                this.type = primitiveType.getSimpleName();
                this.simpleTypeName = primitiveType.getSimpleName();
            } else {
                this.type = type;
                this.simpleTypeName = type.substring(type.lastIndexOf(".") + 1);
            }
        }

        private void setTypeArg(String typeArg) {
            this.typeArg = typeArg;
            this.simpleTypeArgName = typeArg.substring(typeArg.lastIndexOf(".") + 1);
        }

        public String getType() {
            return type;
        }

        public String getSimpleTypeName() {
            return simpleTypeName;
        }

        public String getTypeArg() {
            return typeArg;
        }

        public String getSimpleTypeArgName() {
            return simpleTypeArgName;
        }

        public String getEntityClassName() {
            return (simpleTypeArgName != null) ? simpleTypeArgName : simpleTypeName;
        }

        public void parseAnnotations(List<? extends AnnotationMirror> annotationMirrors) {
            for (AnnotationMirror annotation : annotationMirrors) {
                String annotationType = annotation.getAnnotationType().toString();
              
                if (!annotationType.startsWith(JAVAX_PERSISTENCE)) { 
                    continue;     
                }
                String simpleName = annotationType.substring( 
                        JAVAX_PERSISTENCE.length() );
                if ( LIFECYCLE_ANNOTATIONS.contains( simpleName)){
                    continue;
                }
                hasPersistenceAnnotation = true;

                if (annotationType.contains("EmbeddedId")) { //NOI18N
                    isEmbeddedId = true;
                    isId = true;
                } else if (annotationType.contains("Id")) { //NOI18N
                    isId = true;
                } else if (annotationType.contains("OneToOne")) { //NOI18N
                    relationship = Relationship.OneToOne;
                    parseRelationship(annotation);
                } else if (annotationType.contains("OneToMany")) { //NOI18N
                    relationship = Relationship.OneToMany;
                    parseRelationship(annotation);
                } else if (annotationType.contains("ManyToOne")) { //NOI18N
                    relationship = Relationship.ManyToOne;
                } else if (annotationType.contains("ManyToMany")) { //NOI18N
                    relationship = Relationship.ManyToMany;
                    parseRelationship(annotation);
                } else if (annotationType.contains("Transient")) { //NOI18N
                    isPersistent = false;
                } else if (annotationType.contains("GeneratedValue")) { //NOI18N
                    isGeneratedValue = true;
                }
            }
        }

        private void parseRelationship(AnnotationMirror annotation) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotation.getElementValues();

            for (ExecutableElement e : map.keySet()) {
                if (e.getSimpleName().toString().equals("mappedBy")) {      //NOI18N
                    mappedBy = map.get(e).getValue().toString();
                    return;
                }
            }
        }

        public boolean isPersistent() {
            return isPersistent;
        }

        public boolean hasPersistenceAnnotation() {
            return hasPersistenceAnnotation;
        }

        public boolean isId() {
            return isId;
        }

        public boolean isGeneratedValue() {
            return isGeneratedValue;
        }

        public boolean isEmbeddedId() {
            return isEmbeddedId;

        }

        public boolean isRelationship() {
            return relationship != null;
        }

        public boolean isOneToOne() {
            return relationship == Relationship.OneToOne;

        }

        public boolean isOneToMany() {
            return relationship == Relationship.OneToMany;

        }

        public boolean isManyToOne() {
            return relationship == Relationship.ManyToOne;

        }

        public boolean isManyToMany() {
            return relationship == Relationship.ManyToMany;
        }

        public String getMappedByField() {
            return mappedBy;
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
