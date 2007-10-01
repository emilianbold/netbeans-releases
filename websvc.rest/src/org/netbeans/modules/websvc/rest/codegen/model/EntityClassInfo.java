/*
 * ClassInfo.java
 *
 * Created on March 27, 2007, 6:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
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

/**
 *
 * @author PeterLiu
 */
public class EntityClassInfo {

    private EntityResourceModelBuilder builder;
    private Entity entity;
    private String name;
    private String type;
    private String packageName;
    private Collection<FieldInfo> fieldInfos;
    private FieldInfo idFieldInfo;

    /** Creates a new instance of ClassInfo */
    public EntityClassInfo(Entity entity, Project project, EntityResourceModelBuilder builder) {
        this.entity = entity;
        this.fieldInfos = new ArrayList<FieldInfo>();
        this.builder = builder;

        extractFields(project);

        if (idFieldInfo != null && idFieldInfo.isEmbeddedId()) {
            extractPKFields(project);
        }
    }

    private void extractFields(Project project) {
        try {
            final JavaSource source = SourceGroupSupport.getJavaSourceFromClassName(entity.getClass2(), project);
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
                    List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());

                    for (VariableElement field : fields) {
                        Set<Modifier> modifiers = field.getModifiers();
                        if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.TRANSIENT) || modifiers.contains(Modifier.VOLATILE) || modifiers.contains(Modifier.FINAL)) {
                            continue;
                        }

                        FieldInfo fieldInfo = new FieldInfo();

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


                        for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
                            fieldInfo.addAnnotation(annotation.toString());
                        }

                        if (fieldInfo.isId()) {
                            idFieldInfo = fieldInfo;
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }

    private void extractPKFields(Project project) {
        try {
            final JavaSource source = SourceGroupSupport.getJavaSourceFromClassName(idFieldInfo.getType(), project);
            source.runUserActionTask(new AbstractTask<CompilationController>() {

                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.RESOLVED);
      
                    TypeElement classElement = JavaSourceHelper.getTopLevelClassElement(controller);
                    List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());

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
            }, true);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
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
        private String typeArg;
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
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getSimpleTypeName() {
            return type.substring(type.lastIndexOf(".") + 1);
        }

        public void setTypeArg(String typeArg) {
            this.typeArg = typeArg;
        }

        public String getTypeArg() {
            return typeArg;
        }

        public void addAnnotation(String annotation) {
            this.annotations.add(annotation);
        }

        public boolean isId() {
            return matchAnnotation("@javax.persistence.Id") || matchAnnotation("@javax.persistence.EmbeddedId"); //NOI18N
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