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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.persistence.wizard.jpacontroller;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author mbohm
 */
public class JpaControllerUtil {
    
    /**
     * Get the encoding of the supplied project as a Charset object
     * @param project The project
     * @param file A file in the project
     * @return The project encoding, or a suitable default if the project encoding cannot be determined. Never null
     */
    public static Charset getProjectEncoding(Project project, FileObject file) {
        Charset encoding = project.getLookup().lookup(FileEncodingQueryImplementation.class).getEncoding(file);
        if (encoding == null) {
            encoding = FileEncodingQuery.getDefaultEncoding();
            if (encoding == null) {
                return Charset.forName("UTF-8");
            }
            else {
                return encoding;
            }
        }
        else {
            return encoding;
        }
    }
    
    /**
     * Get the encoding of the supplied project as a String (by performing a lookup and invoking Charset.name).
     * @param project The project
     * @param file A file in the project
     * @return The project encoding, or a suitable default if the project encoding cannot be determined. Never null
     */
    public static String getProjectEncodingAsString(Project project, FileObject file) {
        Charset encoding = project.getLookup().lookup(FileEncodingQueryImplementation.class).getEncoding(file);
        if (encoding == null) {
            encoding = FileEncodingQuery.getDefaultEncoding();
            if (encoding == null) {
                return "UTF-8";
            }
            else {
                return encoding.name();
            }
        }
        else {
            return encoding.name();
        }
    }
    
    public static String simpleClassName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot > 0 ? fqn.substring(lastDot + 1) : fqn;
    }
    
    public static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuffer sbuffer = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sbuffer.append(line);
            sbuffer.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sbuffer.toString();
    }
    
    public static void createFile(FileObject target, String content, String encoding) throws IOException{
        FileLock lock = target.lock();
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding));
            bw.write(content);
            bw.close();
            
        } finally {
            lock.releaseLock();
        }
    }
    
    public static boolean isFieldAccess(TypeElement clazz) {
        boolean fieldAccess = false;
        boolean accessTypeDetected = false;
        TypeElement typeElement = clazz;
        Name qualifiedName = typeElement.getQualifiedName();
        whileloop:
        while (typeElement != null) {
            if (isAnnotatedWith(typeElement, "javax.persistence.Entity") || isAnnotatedWith(typeElement, "javax.persistence.MappedSuperclass")) { // NOI18N
                for (Element element : typeElement.getEnclosedElements()) {
                    if (isAnnotatedWith(element, "javax.persistence.Id") || isAnnotatedWith(element, "javax.persistence.EmbeddedId")) {
                        if (ElementKind.FIELD == element.getKind()) {
                            fieldAccess = true;
                        }
                        accessTypeDetected = true;
                        break whileloop;
                    }
                }
            }
            typeElement = getSuperclassTypeElement(typeElement);
        }
        if (!accessTypeDetected) {
            Logger.getLogger(JpaControllerUtil.class.getName()).log(Level.WARNING, "Failed to detect correct access type for class: " + qualifiedName); // NOI18N
        }
        return fieldAccess;
    }
    
    public static boolean isAnnotatedWith(Element element, String annotationFqn) {
        return findAnnotation(element, annotationFqn) != null;
    }
    
    public static AnnotationMirror findAnnotation(Element element, String annotationFqn) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            String annotationQualifiedName = getAnnotationQualifiedName(annotationMirror);
            if (annotationQualifiedName.equals(annotationFqn)) {
                return annotationMirror;
            }
        }
        return null;
    }   
    
    public static String getAnnotationQualifiedName(AnnotationMirror annotationMirror) {
        DeclaredType annotationDeclaredType = annotationMirror.getAnnotationType();
        TypeElement annotationTypeElement = (TypeElement) annotationDeclaredType.asElement();
        Name name = annotationTypeElement.getQualifiedName();
        return name.toString();
    } 
    
    private static TypeElement getSuperclassTypeElement(TypeElement typeElement) {
        TypeElement superclass = null;
        TypeMirror superclassMirror = typeElement.getSuperclass();
        if (superclassMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType superclassDeclaredType = (DeclaredType)superclassMirror;
            Element superclassElement = superclassDeclaredType.asElement();
            if (superclassElement.getKind() == ElementKind.CLASS && (superclassElement instanceof TypeElement) ) {
                superclass = (TypeElement)superclassElement;
            }
        }
        return superclass;
    }

    public static String findAnnotationValueAsString(AnnotationMirror annotation, String annotationKey) {
        String value = null;
        Map<? extends ExecutableElement,? extends AnnotationValue> annotationMap = annotation.getElementValues();
        for (ExecutableElement key : annotationMap.keySet()) {
            if (annotationKey.equals(key.getSimpleName().toString())) {
                AnnotationValue annotationValue = annotationMap.get(key);
                value = annotationValue.getValue().toString();
                break;
            }
        }
        return value;
    }
    
    public static List<AnnotationMirror> findNestedAnnotations(AnnotationMirror annotationMirror, String annotationFqn) {
        List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();
        findNestedAnnotationsInternal(annotationMirror, annotationFqn, result);
        return result;
    }

    private static void findNestedAnnotationsInternal(Object object, String annotationFqn, List<AnnotationMirror> result) {
        Collection<? extends AnnotationValue> annotationValueCollection = null;
        if (object instanceof AnnotationMirror) {
            AnnotationMirror annotationMirror = (AnnotationMirror)object;
            String annotationQualifiedName = getAnnotationQualifiedName(annotationMirror);
            if (annotationQualifiedName.equals(annotationFqn)) {
                result.add(annotationMirror);
            }
            else {
                //prepare to recurse
                Map<? extends ExecutableElement,? extends AnnotationValue> annotationMap = annotationMirror.getElementValues();
                annotationValueCollection = annotationMap.values();
            }
        }
        else if (object instanceof List) {
            //prepare to recurse
            annotationValueCollection = (Collection<? extends AnnotationValue>)object;
        }

        //recurse
        if (annotationValueCollection != null) {
            for (AnnotationValue annotationValue : annotationValueCollection) {
                Object value = annotationValue.getValue();
                findNestedAnnotationsInternal(value, annotationFqn, result);
            }
        }
    }
    
    public static String fieldFromClassName(String className) {
        boolean makeFirstLower = className.length() == 1 || (!Character.isUpperCase(className.charAt(1)));
        String candidate = makeFirstLower ? className.substring(0,1).toLowerCase() + className.substring(1) : className;
        if (!Utilities.isJavaIdentifier(candidate)) {
            candidate += "1"; //NOI18N
        }
        return candidate;
    }
    
    public static String getPropNameFromMethod(String name) {
        //getABcd should be converted to ABcd, getFooBar should become fooBar
        //getA1 is "a1", getA_ is a_, getAB is AB
        boolean makeFirstLower = name.length() < 5 || (!Character.isUpperCase(name.charAt(4)));
        return makeFirstLower ? name.substring(3,4).toLowerCase() + name.substring(4) : name.substring(3);
    }
    
    public static boolean isEmbeddableClass(TypeElement typeElement) {
        if (JpaControllerUtil.isAnnotatedWith(typeElement, "javax.persistence.Embeddable")) {
            return true;
        }
        return false;
    }
    
    public static int isRelationship(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(controller, method) : method;
        if (element != null) {
            if (JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.OneToOne") || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.ManyToOne")) {
                return REL_TO_ONE;
            }
            if (JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.OneToMany") || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.ManyToMany")) {
                return REL_TO_MANY;
            }
        }
        return REL_NONE;
    }
    
    public static ExecutableElement getOtherSideOfRelation(CompilationController controller, ExecutableElement executableElement, boolean isFieldAccess) {
        TypeMirror passedReturnType = executableElement.getReturnType();
        if (TypeKind.DECLARED != passedReturnType.getKind() || !(passedReturnType instanceof DeclaredType)) {
            return null;
        }
        Types types = controller.getTypes();
        TypeMirror passedReturnTypeStripped = stripCollection((DeclaredType)passedReturnType, types);
        if (passedReturnTypeStripped == null) {
            return null;
        }
        TypeElement passedReturnTypeStrippedElement = (TypeElement) types.asElement(passedReturnTypeStripped);
        
        //try to find a mappedBy annotation element on the possiblyAnnotatedElement
        Element possiblyAnnotatedElement = isFieldAccess ? JpaControllerUtil.guessField(controller, executableElement) : executableElement;
        String mappedBy = null;
        AnnotationMirror persistenceAnnotation = JpaControllerUtil.findAnnotation(possiblyAnnotatedElement, "javax.persistence.OneToOne");  //NOI18N"
        if (persistenceAnnotation == null) {
            persistenceAnnotation = JpaControllerUtil.findAnnotation(possiblyAnnotatedElement, "javax.persistence.OneToMany");  //NOI18N"
        }
        if (persistenceAnnotation == null) {
            persistenceAnnotation = JpaControllerUtil.findAnnotation(possiblyAnnotatedElement, "javax.persistence.ManyToOne");  //NOI18N"
        }
        if (persistenceAnnotation == null) {
            persistenceAnnotation = JpaControllerUtil.findAnnotation(possiblyAnnotatedElement, "javax.persistence.ManyToMany");  //NOI18N"
        }
        if (persistenceAnnotation != null) {
            mappedBy = JpaControllerUtil.findAnnotationValueAsString(persistenceAnnotation, "mappedBy");  //NOI18N
        }
        for (ExecutableElement method : JpaControllerUtil.getEntityMethods(passedReturnTypeStrippedElement)) {
            if (mappedBy != null && mappedBy.length() > 0) {
                String tail = mappedBy.length() > 1 ? mappedBy.substring(1) : "";
                String getterName = "get" + mappedBy.substring(0,1).toUpperCase() + tail;
                if (getterName.equals(method.getSimpleName().toString())) {
                    return method;
                }
            }
            else {
                TypeMirror iteratedReturnType = method.getReturnType();
                iteratedReturnType = stripCollection(iteratedReturnType, types);
                TypeMirror executableElementEnclosingType = executableElement.getEnclosingElement().asType();
                if (types.isSameType(executableElementEnclosingType, iteratedReturnType)) {
                    return method;
                }
            }
        }
        return null;
    }
    
    public static final int REL_NONE = 0;
    public static final int REL_TO_ONE = 1;
    public static final int REL_TO_MANY = 2;
    
    public static TypeMirror stripCollection(TypeMirror passedType, Types types) {
        if (TypeKind.DECLARED != passedType.getKind() || !(passedType instanceof DeclaredType)) {
            return passedType;
        }
        TypeElement passedTypeElement = (TypeElement) types.asElement(passedType);
        String passedTypeQualifiedName = passedTypeElement.getQualifiedName().toString();   //does not include type parameter info
        Class passedTypeClass = null;
        try {
            passedTypeClass = Class.forName(passedTypeQualifiedName);
        } catch (ClassNotFoundException e) {
            //just let passedTypeClass be null
        }
        if (passedTypeClass != null && Collection.class.isAssignableFrom(passedTypeClass)) {
            List<? extends TypeMirror> passedTypeArgs = ((DeclaredType)passedType).getTypeArguments();
            if (passedTypeArgs.size() == 0) {
                return passedType;
            }
            return passedTypeArgs.get(0);
        }
        return passedType;
    }
    
    public static boolean isFieldOptionalAndNullable(CompilationController controller, ExecutableElement method, boolean fieldAccess) {
        boolean isFieldOptional = true;
        Boolean isFieldNullable = null;
        Element fieldElement = fieldAccess ? JpaControllerUtil.guessField(controller, method) : method;
        String[] fieldAnnotationFqns = {"javax.persistence.ManyToOne", "javax.persistence.OneToOne", "javax.persistence.Basic"};
        Boolean isFieldOptionalBoolean = findAnnotationValueAsBoolean(fieldElement, fieldAnnotationFqns, "optional");
        if (isFieldOptionalBoolean != null) {
            isFieldOptional = isFieldOptionalBoolean.booleanValue();
        }
        if (!isFieldOptional) {
            return false;
        }
        //field is optional
        fieldAnnotationFqns = new String[]{"javax.persistence.Column", "javax.persistence.JoinColumn"};
        isFieldNullable = findAnnotationValueAsBoolean(fieldElement, fieldAnnotationFqns, "nullable");
        if (isFieldNullable != null) {
            return isFieldNullable.booleanValue();
        }
        //new ballgame
        boolean result = true;
        AnnotationMirror fieldAnnotation = JpaControllerUtil.findAnnotation(fieldElement, "javax.persistence.JoinColumns"); //NOI18N
        if (fieldAnnotation != null) {
            //all joinColumn annotations must indicate nullable = false to return a false result
            List<AnnotationMirror> joinColumnAnnotations = JpaControllerUtil.findNestedAnnotations(fieldAnnotation, "javax.persistence.JoinColumn");
            for (AnnotationMirror joinColumnAnnotation : joinColumnAnnotations) {
                String columnNullableValue = JpaControllerUtil.findAnnotationValueAsString(joinColumnAnnotation, "nullable"); //NOI18N
                if (columnNullableValue != null) {
                    result = Boolean.parseBoolean(columnNullableValue);
                    if (result) {
                        break;  //one of the joinColumn annotations is nullable, so return true
                    }
                }
                else {
                    result = true;
                    break;  //one of the joinColumn annotations is nullable, so return true
                }
            }
        }
        return result;
    }
    
    private static Boolean findAnnotationValueAsBoolean(Element fieldElement, String[] fieldAnnotationFqns, String annotationKey) {
        Boolean isFieldXable = null;
        for (int i = 0; i < fieldAnnotationFqns.length; i++) {
            String fieldAnnotationFqn = fieldAnnotationFqns[i];
            AnnotationMirror fieldAnnotation = JpaControllerUtil.findAnnotation(fieldElement, fieldAnnotationFqn); //NOI18N
            if (fieldAnnotation != null) {  
                String annotationValueString = JpaControllerUtil.findAnnotationValueAsString(fieldAnnotation, annotationKey); //NOI18N
                if (annotationValueString != null) {
                    isFieldXable = Boolean.valueOf(annotationValueString);
                }
                else {
                    isFieldXable = Boolean.TRUE;
                }
                break;
            }
        }
        return isFieldXable;
    }
    
    public static ExecutableElement getIdGetter(CompilationController controller, final boolean isFieldAccess, final TypeElement typeElement) {
        ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(typeElement);
        for (ExecutableElement method : methods) {
            String methodName = method.getSimpleName().toString();
            if (methodName.startsWith("get")) {
                Element element = isFieldAccess ? JpaControllerUtil.guessField(controller, method) : method;
                if (element != null) {
                    if (JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.Id") || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.EmbeddedId")) {
                        return method;
                    }
                }
            }
        }
        Logger.getLogger(JpaControllerUtil.class.getName()).log(Level.WARNING, "Cannot find ID getter in class: " + typeElement.getQualifiedName());
        return null;
    }
    
    public static boolean isGenerated(CompilationController controller, ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(controller, method) : method;
        if (element != null) {
            if (JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.GeneratedValue")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    public static boolean exceptionsThrownIncludes(WorkingCopy workingCopy, String fqClass, String methodName, List<String> formalParamFqTypes, String exceptionFqClassMaybeIncluded) {
        List<String> exceptionsThrown = getExceptionsThrown(workingCopy, fqClass, methodName, formalParamFqTypes);
        for (String exception : exceptionsThrown) {
            if (exceptionFqClassMaybeIncluded.equals(exception)) {
                return true;
            }
        }
        return false;
    }
    
    public static List<String> getExceptionsThrown(WorkingCopy workingCopy, String fqClass, String methodName, List<String> formalParamFqTypes) {
        if (formalParamFqTypes == null) {
            formalParamFqTypes = Collections.<String>emptyList();
        }
        ExecutableElement desiredMethodElement = null;
        TypeElement suppliedTypeElement = workingCopy.getElements().getTypeElement(fqClass);
        TypeElement typeElement = suppliedTypeElement;
        whileloop:
        while (typeElement != null) {
            for (ExecutableElement methodElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                if (methodElement.getSimpleName().contentEquals(methodName)) {
                    List<? extends VariableElement> formalParamElements = methodElement.getParameters();
                    //for now, just check sizes
                    if (formalParamElements.size() == formalParamFqTypes.size()) {
                        desiredMethodElement = methodElement;
                        break whileloop;
                    }
                }
            }
            typeElement = getSuperclassTypeElement(typeElement);
        }
        if (desiredMethodElement == null) {
            throw new IllegalArgumentException("Could not find " + methodName + " in " + fqClass);
        }
        List<String> result = new ArrayList<String>();
        List<? extends TypeMirror> thrownTypes = desiredMethodElement.getThrownTypes();
        for (TypeMirror thrownType : thrownTypes) {
            if (thrownType.getKind() == TypeKind.DECLARED) {
                DeclaredType thrownDeclaredType = (DeclaredType)thrownType;
                TypeElement thrownElement = (TypeElement)thrownDeclaredType.asElement();
                String thrownFqClass = thrownElement.getQualifiedName().toString();
                result.add(thrownFqClass);
            }
            else {
                result.add(null);
            }
        }
        return result;
    }    
    
    /** Returns all methods in class and its super classes which are entity
     * classes or mapped superclasses.
     */
    public static ExecutableElement[] getEntityMethods(TypeElement entityTypeElement) {
        List<ExecutableElement> result = new LinkedList<ExecutableElement>();
        TypeElement typeElement = entityTypeElement;
        while (typeElement != null) {
            if (isAnnotatedWith(typeElement, "javax.persistence.Entity") || isAnnotatedWith(typeElement, "javax.persistence.MappedSuperclass")) { // NOI18N
                result.addAll(ElementFilter.methodsIn(typeElement.getEnclosedElements()));
            }
            typeElement = getSuperclassTypeElement(typeElement);
        }
        return result.toArray(new ExecutableElement[result.size()]);
    }
    
    public static VariableElement guessField(CompilationController controller, ExecutableElement getter) {
        String name = getter.getSimpleName().toString().substring(3);
        String guessFieldName = name.substring(0,1).toLowerCase() + name.substring(1);
        TypeElement typeElement = (TypeElement) getter.getEnclosingElement();
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (variableElement.getSimpleName().contentEquals(guessFieldName)) {
                return variableElement;
            }
        }
        Logger.getLogger(JpaControllerUtil.class.getName()).log(Level.WARNING, "Cannot detect the field associated with property: " + guessFieldName);
        return null;
    }
    
    // ----------------------------------------------------------------------------------------- Nested Classes
    
    public static class EmbeddedPkSupport {
        private Map<TypeElement,EmbeddedPkSupportInfo> typeToInfo = new HashMap<TypeElement,EmbeddedPkSupportInfo>();
        
        public Set<ExecutableElement> getPkAccessorMethods(CompilationController controller, TypeElement type) {
            EmbeddedPkSupportInfo info = getInfo(controller, type);
            return info.getPkAccessorMethods();
        }
        
        public String getCodeToPopulatePkField(CompilationController controller, TypeElement type, ExecutableElement pkAccessorMethod) {
            EmbeddedPkSupportInfo info = getInfo(controller, type);
            String code = info.getCodeToPopulatePkField(pkAccessorMethod);
            if (code != null) {
                return code;
            }
            
            code = "";
            ExecutableElement relationshipMethod = info.getRelationshipMethod(pkAccessorMethod);
            String referencedColumnName = info.getReferencedColumnName(pkAccessorMethod);
            if (relationshipMethod == null || referencedColumnName == null) {
                info.putCodeToPopulatePkField(pkAccessorMethod, code);
                return code;
            }
            
            TypeMirror relationshipTypeMirror = relationshipMethod.getReturnType();
            if (TypeKind.DECLARED != relationshipTypeMirror.getKind()) {
                info.putCodeToPopulatePkField(pkAccessorMethod, code);
                return code;
            }
            DeclaredType declaredType = (DeclaredType) relationshipTypeMirror;
            TypeElement relationshipType = (TypeElement) declaredType.asElement();
            
            EmbeddedPkSupportInfo relatedInfo = getInfo(controller, relationshipType);
            String accessorString = relatedInfo.getAccessorString(referencedColumnName);
            if (accessorString == null) {
                info.putCodeToPopulatePkField(pkAccessorMethod, code);
                return code;
            }
            
            code = relationshipMethod.getSimpleName().toString() + "()." + accessorString;
            info.putCodeToPopulatePkField(pkAccessorMethod, code);
            return code;
        }
        
        public boolean isRedundantWithRelationshipField(CompilationController controller, TypeElement type, ExecutableElement pkAccessorMethod) {
            return getCodeToPopulatePkField(controller, type, pkAccessorMethod).length() > 0;
        }
        
        public boolean isRedundantWithPkFields(CompilationController controller, TypeElement type, ExecutableElement relationshipMethod) {
            EmbeddedPkSupportInfo info = getInfo(controller, type);
            return info.isRedundantWithPkFields(relationshipMethod);
        }
        
        private EmbeddedPkSupportInfo getInfo(CompilationController controller, TypeElement type) {
            EmbeddedPkSupportInfo info = typeToInfo.get(type);
            if (info == null) {
                info = new EmbeddedPkSupportInfo(controller, type);
                typeToInfo.put(type, info);
            }
            return info;
        }
    }
    
    private static class EmbeddedPkSupportInfo {
        private TypeElement type;
        private Map<String,ExecutableElement> joinColumnNameToRelationshipMethod = new HashMap<String,ExecutableElement>();
        private Map<ExecutableElement,List<String>> relationshipMethodToJoinColumnNames = new HashMap<ExecutableElement,List<String>>(); //used only in isRedundantWithPkFields
        private Map<String,String> joinColumnNameToReferencedColumnName = new HashMap<String,String>();
        private Map<String,String> columnNameToAccessorString = new HashMap<String,String>();
        private Map<ExecutableElement,String> pkAccessorMethodToColumnName = new HashMap<ExecutableElement,String>();
        private Map<ExecutableElement,String> pkAccessorMethodToPopulationCode = new HashMap<ExecutableElement,String>(); //derived
        private boolean isFieldAccess;
        
        public Set<ExecutableElement> getPkAccessorMethods() {
            return pkAccessorMethodToColumnName.keySet();
        }
        
        public ExecutableElement getRelationshipMethod(ExecutableElement pkAccessorMethod) {
            String columnName = pkAccessorMethodToColumnName.get(pkAccessorMethod);
            if (columnName == null) {
                return null;
            }
            return joinColumnNameToRelationshipMethod.get(columnName);
        }
        
        public String getReferencedColumnName(ExecutableElement pkAccessorMethod) {
            String columnName = pkAccessorMethodToColumnName.get(pkAccessorMethod);
            if (columnName == null) {
                return null;
            }
            return joinColumnNameToReferencedColumnName.get(columnName);
        }
        
        public String getAccessorString(String columnName) {
            return columnNameToAccessorString.get(columnName);
        }
        
        public String getCodeToPopulatePkField(ExecutableElement pkAccessorMethod) {
            return pkAccessorMethodToPopulationCode.get(pkAccessorMethod);
        }
        
        public void putCodeToPopulatePkField(ExecutableElement pkAccessorMethod, String code) {
            pkAccessorMethodToPopulationCode.put(pkAccessorMethod, code);
        }
        
        public boolean isRedundantWithPkFields(ExecutableElement relationshipMethod) {
            List<String> joinColumnNameList = relationshipMethodToJoinColumnNames.get(relationshipMethod);
            if (joinColumnNameList == null) {
                return false;
            }
            Collection<String> pkColumnNames = pkAccessorMethodToColumnName.values();
            for (String columnName : joinColumnNameList) {
                if (!pkColumnNames.contains(columnName)) {
                    return false;
                }
            }
            return true;
        }
        
        EmbeddedPkSupportInfo(CompilationController controller, TypeElement type) {
            this.type = type;
            isFieldAccess = isFieldAccess(type);
            for (ExecutableElement method : getEntityMethods(type)) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    Element f = isFieldAccess ? guessField(controller, method) : method;
                    if (f != null) {
                        int a = -1;
                        AnnotationMirror columnAnnotation = null;
                        String[] columnAnnotationFqns = {"javax.persistence.EmbeddedId", "javax.persistence.JoinColumns", "javax.persistence.JoinColumn", "javax.persistence.Column"}; //NOI18N
                        for (int i = 0; i < columnAnnotationFqns.length; i++) {
                            String columnAnnotationFqn = columnAnnotationFqns[i];
                            AnnotationMirror columnAnnotationMirror = findAnnotation(f, columnAnnotationFqn);
                            if (columnAnnotationMirror != null) {
                                a = i;
                                columnAnnotation = columnAnnotationMirror;
                                break;
                            }
                        }
                        if (a == 0) {
                            //populate pkAccessorMethodToColumnName and columnNameToAccessorString
                            populateMapsForEmbedded(controller, method);
                        } else if ( (a == 1 || a == 2) && 
                                (isAnnotatedWith(f, "javax.persistence.OneToOne") ||
                                isAnnotatedWith(f, "javax.persistence.ManyToOne")) )  {
                            //populate joinColumnNameToRelationshipMethod, relationshipMethodToJoinColumnNames, and joinColumnNameToReferencedColumnName
                            populateJoinColumnNameMaps(method, columnAnnotationFqns[a], columnAnnotation);
                        }
                        else if (a == 3) {
                            //populate columnNameToAccessorString
                            String columnName = findAnnotationValueAsString(columnAnnotation, "name"); //NOI18N
                            if (columnName != null) {
                                columnNameToAccessorString.put(columnName, method.getSimpleName().toString() + "()");
                            }
                        } 
                    }
                }
            }
        }
        
        private void populateMapsForEmbedded(CompilationController controller, ExecutableElement idGetterElement) {
            TypeMirror idType = idGetterElement.getReturnType();
            if (TypeKind.DECLARED != idType.getKind()) {
                return;
            }
            DeclaredType declaredType = (DeclaredType) idType;
            TypeElement idClass = (TypeElement) declaredType.asElement();
            
            for (ExecutableElement pkMethod : ElementFilter.methodsIn(idClass.getEnclosedElements())) {
                String pkMethodName = pkMethod.getSimpleName().toString();
                if (pkMethodName.startsWith("get")) {
                    Element pkFieldElement = isFieldAccess ? guessField(controller, pkMethod) : pkMethod;
                    AnnotationMirror columnAnnotation = findAnnotation(pkFieldElement, "javax.persistence.Column"); //NOI18N
                    if (columnAnnotation != null) {
                        String columnName = findAnnotationValueAsString(columnAnnotation, "name"); //NOI18N
                        if (columnName != null) {
                            pkAccessorMethodToColumnName.put(pkMethod, columnName);
                            columnNameToAccessorString.put(columnName, 
                                    idGetterElement.getSimpleName().toString() + "()." + 
                                    pkMethod.getSimpleName() + "()");
                        }
                    }
                }
            }
        }
        
        private void populateJoinColumnNameMaps(ExecutableElement m, String columnAnnotationFqn, AnnotationMirror columnAnnotation) {
            List<AnnotationMirror> joinColumnAnnotations;
            if ("javax.persistence.JoinColumn".equals(columnAnnotationFqn)) {
                joinColumnAnnotations = new ArrayList<AnnotationMirror>();
                joinColumnAnnotations.add(columnAnnotation);
            }
            else {  //columnAnnotation is a javax.persistence.JoinColumns
                joinColumnAnnotations = findNestedAnnotations(columnAnnotation, "javax.persistence.JoinColumn"); //NOI18N
            }
            for (AnnotationMirror joinColumnAnnotation : joinColumnAnnotations) {
                String columnName = findAnnotationValueAsString(joinColumnAnnotation, "name"); //NOI18N
                if (columnName != null) {
                    String referencedColumnName = findAnnotationValueAsString(joinColumnAnnotation, "referencedColumnName"); //NOI18N
                    joinColumnNameToRelationshipMethod.put(columnName, m);
                    joinColumnNameToReferencedColumnName.put(columnName, referencedColumnName);
                    List<String> joinColumnNameList = relationshipMethodToJoinColumnNames.get(m);
                    if (joinColumnNameList == null) {
                        joinColumnNameList = new ArrayList<String>();
                        relationshipMethodToJoinColumnNames.put(m, joinColumnNameList);
                    }
                    joinColumnNameList.add(columnName);
                }
            }
        }
    }
    
    public static class TreeMakerUtils {
        
        public static ClassTree addVariable(ClassTree classTree, WorkingCopy wc, String name, TypeInfo type, int modifiers, Object initializer, AnnotationInfo[] annotations) {
            Tree typeTree = createType(wc, type);
            ModifiersTree modTree = createModifiers(wc, modifiers, annotations);
            TreeMaker make = wc.getTreeMaker();
            VariableTree tree = make.Variable(modTree, name, typeTree, make.Literal(initializer));
            return make.addClassMember(classTree, tree);
        }
        
        public static ClassTree addVariable(ClassTree classTree, WorkingCopy wc, String name, String type, int modifiers, Object initializer, AnnotationInfo[] annotations) {
            return addVariable(classTree, wc, name, new TypeInfo(type), modifiers, initializer, annotations);
        }
        
    /*
     * Creates a new variable tree for a given name and type
     */
        private static VariableTree createVariable(WorkingCopy wc, String name, TypeInfo type) {
            return createVariable(wc, name, createType(wc, type));
        }
        
    /*
     * Creates a new variable tree for a given name and type
     */
        private static VariableTree createVariable(WorkingCopy wc, String name, Tree type) {
            TreeMaker make = wc.getTreeMaker();
            return make.Variable(createModifiers(wc), name, type, null);
        }
        
        public static ClassTree addMethod(ClassTree classTree, WorkingCopy wc, MethodInfo mInfo) {
            MethodTree tree = createMethod(wc, mInfo);
            return wc.getTreeMaker().addClassMember(classTree, tree);
        }
        
        public static ClassTree modifyDefaultConstructor(ClassTree classTree, ClassTree modifiedClassTree, WorkingCopy wc, MethodInfo modifiedConstructorInfo) {
            if (!"<init>".equals(modifiedConstructorInfo.getName())) {
                throw new IllegalArgumentException("modifiedConstructorInfo name must be <init>");
            }
            
            MethodTree modifiedConstructor = createMethod(wc, modifiedConstructorInfo);
            MethodTree constructor = null;
            for(Tree tree : modifiedClassTree.getMembers()) {
                if(Tree.Kind.METHOD == tree.getKind()) {
                    MethodTree mtree = (MethodTree)tree;
                    List<? extends VariableTree> mTreeParameters = mtree.getParameters();
                    if(mtree.getName().toString().equals("<init>") &&
                            (mTreeParameters == null || mTreeParameters.size() == 0) &&
                            !wc.getTreeUtilities().isSynthetic(wc.getTrees().getPath(wc.getCompilationUnit(), classTree))) {
                            constructor = mtree;
                            break;
                    }
                }
            }
            if (constructor == null) {
                modifiedClassTree = wc.getTreeMaker().addClassMember(modifiedClassTree, modifiedConstructor);
            }
            else {
                wc.rewrite(constructor, modifiedConstructor);
            }
            return modifiedClassTree;
        }
        
    /*
     * Creates a method given context method and return type name
     */
        private static MethodTree createMethod(WorkingCopy wc, MethodInfo mInfo) {
            TreeMaker make = wc.getTreeMaker();
            TypeInfo[] pTypes = mInfo.getParameterTypes();
            String[] pNames = mInfo.getParameterNames();
            List<VariableTree> params = new ArrayList<VariableTree>();
            for (int i = 0 ; pTypes != null && i < pTypes.length; i++) {
                VariableTree vtree = createVariable(wc, pNames[i], pTypes[i]);
                params.add(vtree);
            }
            
            TypeInfo[] excepTypes = mInfo.getExceptionTypes();
            List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
            for (int i = 0 ; excepTypes != null && i < excepTypes.length; i++) {
                throwsList.add((ExpressionTree)createType(wc, excepTypes[i]));
            }
            
            String body = mInfo.getMethodBodyText();
            if(body == null) {
                body = "";
            }
            
            MethodTree mtree = make.Method(createModifiers(wc, mInfo.getModifiers(), mInfo.getAnnotations()),
                    mInfo.getName(),
                    createType(wc, mInfo.getReturnType()),
                    Collections.<TypeParameterTree>emptyList(),
                    params,
                    throwsList,
                    "{" + body + "}",
                    null
                    );
            
            //         if(mInfo.getCommentText() != null) {
            //             Comment comment = Comment.create(Comment.Style.JAVADOC, -2,
            //                     -2, -2, mInfo.getCommentText());
            //             make.addComment(mtree, comment, true);
            //         }
            
            return mtree;
        }
        
    /*
     * Returns a tree for a given type in string format
     * Note that import for type is handled by make.QualIdent()
     */
        private static Tree createType(WorkingCopy wc, TypeInfo type) {
            if(type == null) {
                return null;
            }
            String rawType = type.getRawType();
            
            TreeMaker make = wc.getTreeMaker();
            if (rawType.endsWith("[]")) { // NOI18N
                String rawTypeName = rawType.substring(0, rawType.length()-2);
                TypeInfo scalarTypeInfo = new TypeInfo(rawTypeName, type.getDeclaredTypeParameters());
                return make.ArrayType(createType(wc, scalarTypeInfo));
            }
            
            TypeKind primitiveTypeKind = null;
            if ("boolean".equals(rawType)) {           // NOI18N
                primitiveTypeKind = TypeKind.BOOLEAN;
            } else if ("byte".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.BYTE;
            } else if ("short".equals(rawType)) {      // NOI18N
                primitiveTypeKind = TypeKind.SHORT;
            } else if ("int".equals(rawType)) {        // NOI18N
                primitiveTypeKind = TypeKind.INT;
            } else if ("long".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.LONG;
            } else if ("char".equals(rawType)) {       // NOI18N
                primitiveTypeKind = TypeKind.CHAR;
            } else if ("float".equals(rawType)) {      // NOI18N
                primitiveTypeKind = TypeKind.FLOAT;
            } else if ("double".equals(rawType)) {     // NOI18N
                primitiveTypeKind = TypeKind.DOUBLE;
            } else if ("void".equals(rawType)) {
                primitiveTypeKind = TypeKind.VOID;
            }
            if (primitiveTypeKind != null) {
                return make.PrimitiveType(primitiveTypeKind);
            }
            
            TypeInfo[] declaredTypeParameters = type.getDeclaredTypeParameters();
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                TypeElement typeElement = wc.getElements().getTypeElement(rawType);
                if (typeElement == null) {
                    throw new IllegalArgumentException("Type " + rawType + " cannot be found"); // NOI18N
                }
                return make.QualIdent(typeElement);
            }
            else {
                TypeMirror typeMirror = getTypeMirror(wc, type);
                return make.Type(typeMirror);
            }
        }
        
        private static TypeMirror getTypeMirror(WorkingCopy wc, TypeInfo type) {
            TreeMaker make = wc.getTreeMaker();
            String rawType = type.getRawType();
            TypeElement rawTypeElement = wc.getElements().getTypeElement(rawType);
            if (rawTypeElement == null) {
                throw new IllegalArgumentException("Type " + rawType + " cannot be found"); // NOI18N
            }
            TypeInfo[] declaredTypeParameters = type.getDeclaredTypeParameters();
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                make.QualIdent(rawTypeElement);
                return rawTypeElement.asType();
            }
            else {
                TypeMirror[] declaredTypeMirrors = new TypeMirror[declaredTypeParameters.length];
                for (int i = 0; i < declaredTypeParameters.length; i++) {
                    declaredTypeMirrors[i] = getTypeMirror(wc, declaredTypeParameters[i]);
                }
                DeclaredType declaredType = wc.getTypes().getDeclaredType(rawTypeElement, declaredTypeMirrors);
                return declaredType;
            }
        }
        
        private static ModifiersTree createModifiers(WorkingCopy wc) {
            return wc.getTreeMaker().Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
        }
        
        private static ModifiersTree createModifiers(WorkingCopy wc, long flags, AnnotationInfo[] annotations) {
            if (annotations == null || annotations.length == 0) {
                return wc.getTreeMaker().Modifiers(flags, Collections.<AnnotationTree>emptyList());
            }
            GenerationUtils generationUtils = GenerationUtils.newInstance(wc);
            List<AnnotationTree> annotationTrees = new ArrayList<AnnotationTree>();
            for (AnnotationInfo annotation : annotations) {
                //append an AnnotationTree
                String[] argNames = annotation.getArgNames();
                if (argNames != null && argNames.length > 0) {
                    //one or more args in this annotation
                    Object[] argValues = annotation.getArgValues();
                    List<ExpressionTree> argTrees = new ArrayList<ExpressionTree>();
                    for (int i = 0; i < argNames.length; i++) {
                        ExpressionTree argTree = generationUtils.createAnnotationArgument(argNames[i], argValues[i]);
                        argTrees.add(argTree);
                    }
                    AnnotationTree annotationTree = generationUtils.createAnnotation(annotation.getType(), argTrees);
                    annotationTrees.add(annotationTree);
                } else {
                    //no args in this annotation
                    AnnotationTree annotationTree = generationUtils.createAnnotation(annotation.getType());
                    annotationTrees.add(annotationTree);
                }
            }
            return wc.getTreeMaker().Modifiers(flags, annotationTrees);
        }
        
        public static CompilationUnitTree createImport(WorkingCopy wc, CompilationUnitTree modifiedCut, String fq) {
            if (modifiedCut == null) {
                modifiedCut = wc.getCompilationUnit();  //use committed cut as modifiedCut
            }
            List<? extends ImportTree> imports = modifiedCut.getImports();
            boolean found = false;
            for (ImportTree imp : imports) {
               if (fq.equals(imp.getQualifiedIdentifier().toString())) {
                   found = true; 
                   break;
               }
            }
            if (!found) {
                TreeMaker make = wc.getTreeMaker();
                CompilationUnitTree newCut = make.addCompUnitImport(
                    modifiedCut, 
                    make.Import(make.Identifier(fq), false)
                );                                              //create a newCut from modifiedCut
                wc.rewrite(wc.getCompilationUnit(), newCut);    //replace committed cut with newCut in change map
                return newCut;                                  //return the newCut we just created
            }
            return modifiedCut; //no newCut created from modifiedCut, so just return modifiedCut
        }
    }
    
    public static class TypeInfo {
        
        private String rawType;
        private TypeInfo[] declaredTypeParameters;
        
        public String getRawType() {
            return rawType;
        }

        public TypeInfo[] getDeclaredTypeParameters() {
            return declaredTypeParameters;
        }
       
        public TypeInfo(String rawType) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
        }
        
        public TypeInfo(String rawType, TypeInfo[] declaredTypeParameters) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
            if (declaredTypeParameters == null || declaredTypeParameters.length == 0) {
                return;
            }
            this.declaredTypeParameters = declaredTypeParameters;
        }
        
        public TypeInfo(String rawType, String[] declaredTypeParamStrings) {
            if (rawType == null) {
                throw new IllegalArgumentException();
            }
            this.rawType = rawType;
            if (declaredTypeParamStrings == null || declaredTypeParamStrings.length == 0) {
                return;
            }
            this.declaredTypeParameters = TypeInfo.fromStrings(declaredTypeParamStrings);
        }
        
        public static TypeInfo[] fromStrings(String[] strings) {
            if (strings == null || strings.length == 0) {
                return null;
            }
            TypeInfo[] typeInfos = new TypeInfo[strings.length];
            for (int i = 0; i < strings.length; i++) {
                typeInfos[i] = new TypeInfo(strings[i]);
            }
            return typeInfos;
        }
    }
    
    public static class MethodInfo {
        
        private String         name;
        private int            modifiers;
        private TypeInfo          returnType;
        private TypeInfo[]        exceptionTypes;
        private TypeInfo[]        parameterTypes;
        private String[]       parameterNames;
        private String         methodBodyText;
        private AnnotationInfo[] annotations;
        private String         commentText;
        
        /**
         * Constructs a MethodInfo with the specified name, modifiers,
         * returnType, parameterTypes, parameterNames, methodBody, and commentText.
         *
         * @param name The method name for this MethodInfo
         * @param modifiers The method {@link Modifier} bits
         * @param returnType The return type for this MethodInfo
         * @param exceptionsThrown The exceptions the method throws
         * @param parameterTypes The parameter types for this MethodInfo
         * @param parameterNames The parameter names for this MethodInfo
         * @param methodBodyText The Java source code for the body of this MethodInfo
         * @param annotations The annotation for this MethodInfo
         * @param commentText The comment text for this MethodInfo
         */
        public MethodInfo(String name, int modifiers, TypeInfo returnType, TypeInfo[] exceptionTypes,
                TypeInfo[] parameterTypes, String[] parameterNames, String methodBodyText,  AnnotationInfo[] annotations,
                String commentText) {
            
            this.name = name;
            this.modifiers = modifiers;
            this.returnType = returnType;
            this.exceptionTypes = exceptionTypes;
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.methodBodyText = methodBodyText;
            this.annotations = annotations;
            this.commentText = commentText;
        }
        
        public MethodInfo(String name, int modifiers, String returnType, String[] exceptionTypes,
                String[] parameterTypes, String[] parameterNames, String methodBodyText,  AnnotationInfo[] annotations,
                String commentText) {
            
            this.name = name;
            this.modifiers = modifiers;
            this.returnType = new TypeInfo(returnType);
            this.exceptionTypes = TypeInfo.fromStrings(exceptionTypes);
            this.parameterTypes = TypeInfo.fromStrings(parameterTypes);
            this.parameterNames = parameterNames;
            this.methodBodyText = methodBodyText;
            this.annotations = annotations;
            this.commentText = commentText;
        }
        
        public String getName() {
            return name;
        }
        
        public int getModifiers() {
            return modifiers;
        }
        
        public TypeInfo getReturnType() {
            return returnType;
        }
        
        public TypeInfo[] getExceptionTypes() {
            return exceptionTypes;
        }
        
        public String getMethodBodyText() {
            return methodBodyText;
        }
        
        public TypeInfo[] getParameterTypes() {
            return parameterTypes;
        }
        
        public String[] getParameterNames() {
            return parameterNames;
        }
        
        public  AnnotationInfo[] getAnnotations() {
            return annotations;
        }
        
        public String getCommentText() {
            return commentText;
        }
    }
    
    public static class AnnotationInfo {
        private String type;
        private String[] argNames;
        private Object[] argValues;
        
        public AnnotationInfo(String type) {
            if (type == null) {
                throw new IllegalArgumentException();
            }
            this.type = type;
        }
        
        public AnnotationInfo(String type, String[] argNames, Object[] argValues) {
            if (type == null) {
                throw new IllegalArgumentException();
            }
            this.type = type;
            if (argNames == null) {
                if (argValues != null) {
                    throw new IllegalArgumentException();
                }
            } else if (argValues == null || argValues.length != argNames.length) {
                throw new IllegalArgumentException();
            }
            this.argNames = argNames;
            this.argValues = argValues;
        }
        
        public String getType() {
            return type;
        }
        
        public String[] getArgNames() {
            return argNames;
        }
        
        public Object[] getArgValues() {
            return argValues;
        }
    }
}
