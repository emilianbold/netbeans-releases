/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.persistence.action.*;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.persistence.util.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions.*;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.util.Parameters;

/**
 * A support class for EntityManagerGenerationStrategy implementations.
 *
 * @author Erno Mononen
 */
abstract class EntityManagerGenerationStrategySupport implements EntityManagerGenerationStrategy{
    
    protected static final String ENTITY_MANAGER_FQN = "javax.persistence.EntityManager"; //NO18N
    protected static final String ENTITY_MANAGER_FACTORY_FQN = "javax.persistence.EntityManagerFactory"; //NO18N
    protected static final String USER_TX_FQN = "javax.transaction.UserTransaction"; //NO18N
    protected static final String PERSISTENCE_CONTEXT_FQN = "javax.persistence.PersistenceContext"; //NO18N
    protected static final String PERSISTENCE_UNIT_FQN = "javax.persistence.PersistenceUnit"; //NO18N
    protected static final String POST_CONSTRUCT_FQN = "javax.annotation.PostConstruct"; //NO18N
    protected static final String PRE_DESTROY_FQN = "javax.annotation.PreDestroy"; //NO18N
    protected static final String RESOURCE_FQN = "javax.annotation.Resource"; //NO18N
    
    protected static final String ENTITY_MANAGER_DEFAULT_NAME = "em"; //NO18N
    protected static final String ENTITY_MANAGER_FACTORY_DEFAULT_NAME = "emf"; //NO18N
    
    private TreeMaker treeMaker;
    private ClassTree classTree;
    private WorkingCopy workingCopy;
    private GenerationUtils genUtils;
    private PersistenceUnit persistenceUnit;
    private GenerationOptions generationOptions;
    
    protected enum Initialization {INJECT, EMF, INIT}
    
    protected List<VariableTree> getParameterList(){
        if (getGenerationOptions().getParameterType() == null){
            return Collections.<VariableTree>emptyList();
        }
        VariableTree parameter = getTreeMaker().Variable(
                getTreeMaker().Modifiers(
                Collections.<Modifier>emptySet(),
                Collections.<AnnotationTree>emptyList()
                ),
                getGenerationOptions().getParameterName(),
                getTreeMaker().Identifier(getGenerationOptions().getParameterType()),
                null
                );
        return Collections.<VariableTree>singletonList(parameter);
    }
    
    protected Tree getReturnTypeTree(){
        if (getGenerationOptions().getReturnType() == null || "void".equals(getGenerationOptions().getReturnType())){ //NO18N
            return getTreeMaker().PrimitiveType(TypeKind.VOID);
        }
        return getGenUtils().createType(getGenerationOptions().getReturnType());
    }
    
    protected String computeMethodName(){
        return makeUnique(getGenerationOptions().getMethodName());
    }
    
    private String makeUnique(String methodName){
        // TODO: RETOUCHE
        return methodName;
    }
    
    protected FieldInfo getEntityManagerFactoryFieldInfo(){
        VariableTree existing = getField(ENTITY_MANAGER_FACTORY_FQN);
        if (existing != null){
            return new FieldInfo(existing.getName().toString(), true);
        }
        return new FieldInfo(ENTITY_MANAGER_FACTORY_DEFAULT_NAME, false);
    }
    
    protected FieldInfo getEntityManagerFieldInfo(){
        VariableTree existing = getField(ENTITY_MANAGER_FQN);
        if (existing != null){
            return new FieldInfo(existing.getName().toString(), true);
        }
        return new FieldInfo(ENTITY_MANAGER_DEFAULT_NAME, false);
    }
    
    /**
     * Gets the variable tree representing the first field of the given type in
     * our class.
     *
     * @param fieldTypeFqn the fully qualified name of the field's type.
     * @return the variable tree or null if no matching field was found.
     */
    protected VariableTree getField(final String fieldTypeFqn){
        
        Parameters.notEmpty("fieldTypeFqn", fieldTypeFqn); //NO18N
        
        for (Tree member : getClassTree().getMembers()){
            if (Tree.Kind.VARIABLE == member.getKind()){
                VariableTree variable = (VariableTree) member;
                TreePath path = getWorkingCopy().getTrees().getPath(getWorkingCopy().getCompilationUnit(), variable);
                TypeMirror variableType = getWorkingCopy().getTrees().getTypeMirror(path);
                if (fieldTypeFqn.equals(variableType.toString())){
                    return variable;
                }
                
            }
        }
        return null;
    }
    
    /**
     * Gets the element representing an annotation of the given type. Searches annotations
     *  declared on class, fields and methods (in that order).
     * @param annotationTypeFqn the fully qualified name of the annotation's type.
     * @return the element or null if no matching annotation was found.
     */
    protected Element getAnnotation(final String annotationTypeFqn){
        
        Parameters.notEmpty("annotationTypeFqn", annotationTypeFqn); //NO18N
        
        TypeElement annotationType = asTypeElement(annotationTypeFqn);
        TypeElement classElement = getClassElement();
        List<Element> elements = new ArrayList<Element>();
        elements.add(classElement);
        elements.addAll(ElementFilter.fieldsIn(classElement.getEnclosedElements()));
        elements.addAll(ElementFilter.methodsIn(classElement.getEnclosedElements()));
        
        
        return checkElementsForAnnotationType(elements, annotationType);
    }
    
    private Element checkElementsForType(List <? extends Element> elements, TypeElement type){
        for (Element element : elements){
            if (getWorkingCopy().getTypes().isSameType(element.asType(), type.asType())){
                return type;
            }
        }
        return null;
    }
    
    private Element checkElementsForAnnotationType(List<? extends Element> elements, TypeElement annotationType){
        for (Element element : elements){
            for (AnnotationMirror mirror : getWorkingCopy().getElements().getAllAnnotationMirrors(element)){
                if (getWorkingCopy().getTypes().isSameType(annotationType.asType(), ((TypeElement) mirror.getAnnotationType().asElement()).asType())){
                    return annotationType;
                }
            }
        }
        return null;
    }
    
    private TypeElement getClassElement(){
        TreePath path = getWorkingCopy().getTrees().getPath(getWorkingCopy().getCompilationUnit(), getClassTree());
        return (TypeElement) getWorkingCopy().getTrees().getElement(path);
    }
    
    private TypeElement asTypeElement(String fqn){
        TypeElement result = getWorkingCopy().getElements().getTypeElement(fqn);
        assert result != null : "Could not get TypeElement for " + fqn; //NO18N
        return result;
    }
    
    protected String generateCallLines() {
        return generateCallLines(ENTITY_MANAGER_DEFAULT_NAME);
    }
    
    protected String getEmInitCode(FieldInfo em, FieldInfo emf){
        String text = "javax.persistence.EntityManager {0} = {1}.createEntityManager();\n"; //NO18N
        return MessageFormat.format(text, em.getName(), emf.getName());
    }
    
    /**
     * Generates the code for invoking the operation specified in our
     * <code>generationOptions</code> on the entity manager with the given name.
     *
     * @param emName the name of the entity manager
     */
    protected String generateCallLines(String emName) {
        return MessageFormat.format(getGenerationOptions().getOperation().getBody(), new Object[] {
            emName,
            getGenerationOptions().getParameterName(),
            getGenerationOptions().getParameterType(),
            getGenerationOptions().getReturnType()});
    }
    
    protected VariableTree createUserTransaction(){
        return getTreeMaker().Variable(
                getTreeMaker().Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE),
                Collections.<AnnotationTree>singletonList(getGenUtils().createAnnotation(RESOURCE_FQN))
                ),
                "utx", //NO18N
                getTreeMaker().Identifier(USER_TX_FQN),
                null);
    }
    
    protected VariableTree createEntityManagerFactory(String name){
        return getTreeMaker().Variable(getTreeMaker().Modifiers(
                Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList()),
                name,
                getTypeTree(ENTITY_MANAGER_FACTORY_FQN),
                getTreeMaker().MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                getTreeMaker().MemberSelect(
                getTypeTree("javax.persistence.Persistence"), "createEntityManagerFactory"), // NO18N
                Collections.<ExpressionTree>singletonList(getTreeMaker().Literal(getPersistenceUnitName()))
                )
                );
    }
    
    protected String getPersistenceUnitName(){
        return getPersistenceUnit() != null ? getPersistenceUnit().getName() : "";
    }
    
    protected ExpressionTree getTypeTree(String fqn){
        return getTreeMaker().QualIdent(getWorkingCopy().getElements().getTypeElement(fqn));
    }
    
    protected ClassTree createEntityManager(Initialization init){
        
        ClassTree result = getClassTree();
        
        List<AnnotationTree> anns = new ArrayList<AnnotationTree>();
        ExpressionTree expressionTree = null;
        String emfName = ENTITY_MANAGER_FACTORY_DEFAULT_NAME;
        
        boolean needsEmf = false;
        VariableTree existingEmf = null;
        switch(init){
            
            case INJECT :
                anns.add(getGenUtils().createAnnotation(PERSISTENCE_CONTEXT_FQN));
                break;
                
            case EMF:
                existingEmf = getField(ENTITY_MANAGER_FACTORY_FQN);
                assert existingEmf != null : "EntityManagerFactory does not exist in the class";
                expressionTree = getTreeMaker().Literal(existingEmf.getName().toString() + ".createEntityManager();"); //NO18N
                break;
                
            case INIT:
                
                existingEmf = getField(ENTITY_MANAGER_FACTORY_FQN);
                if (existingEmf != null){
                    emfName = existingEmf.getName().toString();
                } else {
                    needsEmf = true;
                }
                
                AnnotationTree postConstruct = getGenUtils().createAnnotation(POST_CONSTRUCT_FQN);
                MethodTree initMethod = getTreeMaker().Method(
                        getTreeMaker().Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC), Collections.<AnnotationTree>singletonList(postConstruct)),
                        makeUnique("init"),
                        getTreeMaker().PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{ " + ENTITY_MANAGER_DEFAULT_NAME + " = " + emfName + ".createEntityManager(); }", //NO18N
                        null
                        );
                
                result = getTreeMaker().addClassMember(getClassTree(), initMethod);
                
                AnnotationTree preDestroy = getGenUtils().createAnnotation(PRE_DESTROY_FQN);
                MethodTree destroyMethod = getTreeMaker().Method(
                        getTreeMaker().Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC), Collections.<AnnotationTree>singletonList(preDestroy)),
                        makeUnique("destroy"),
                        getTreeMaker().PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{ " + ENTITY_MANAGER_DEFAULT_NAME + " .close(); }",
                        null
                        );
                
                result = getTreeMaker().addClassMember(result, destroyMethod);
                
                if(needsEmf){
                    ExpressionTree annArgument = getGenUtils().createAnnotationArgument("name", getPersistenceUnitName());
                    AnnotationTree puAnn = getGenUtils().createAnnotation(PERSISTENCE_UNIT_FQN, Collections.<ExpressionTree>singletonList(annArgument));
                    VariableTree emf = getTreeMaker().Variable(
                            getTreeMaker().Modifiers(
                            Collections.<Modifier>singleton(Modifier.PRIVATE),
                            Collections.<AnnotationTree>singletonList(puAnn)
                            ),
                            emfName,
                            getTypeTree(ENTITY_MANAGER_FACTORY_FQN),
                            null);
                    result = getTreeMaker().insertClassMember(result, getIndexForField(result), emf);
                }
                
                break;
        }
        
        VariableTree entityManager = getTreeMaker().Variable(
                getTreeMaker().Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE),
                anns
                ),
                ENTITY_MANAGER_DEFAULT_NAME,
                getTypeTree(ENTITY_MANAGER_FQN),
                expressionTree);
        
        return getTreeMaker().insertClassMember(result, getIndexForField(result), entityManager);
    }
    
    
    protected int getIndexForField(ClassTree clazz){
        int result = 0;
        for (Tree each : clazz.getMembers()){
            if (Tree.Kind.VARIABLE == each.getKind()){
                result++;
            }
        }
        return result;
    }
    
    protected TreeMaker getTreeMaker() {
        return treeMaker;
    }
    
    public void setTreeMaker(TreeMaker treeMaker) {
        this.treeMaker = treeMaker;
    }
    
    protected ClassTree getClassTree() {
        return classTree;
    }
    
    public void setClassTree(ClassTree classTree) {
        this.classTree = classTree;
    }
    
    protected WorkingCopy getWorkingCopy() {
        return workingCopy;
    }
    
    public void setWorkingCopy(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
    }
    
    protected GenerationUtils getGenUtils() {
        if (genUtils == null){
            genUtils = GenerationUtils.newInstance(getWorkingCopy(), getClassTree());
        }
        return genUtils;
    }
    
    public void setGenUtils(GenerationUtils genUtils) {
        this.genUtils = genUtils;
    }
    
    protected PersistenceUnit getPersistenceUnit() {
        return persistenceUnit;
    }
    
    public void setPersistenceUnit(PersistenceUnit persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }
    
    protected GenerationOptions getGenerationOptions() {
        return generationOptions;
    }
    
    public void setGenerationOptions(GenerationOptions generationOptions) {
        this.generationOptions = generationOptions;
    }
    
    /**
     * Encapsulates info of a field.
     */
    protected static class FieldInfo {
        /**
         * The name for the field, either the name of an existing field
         * or the default name for the field.
         */
        private String name;
        /**
         * Specifies whether the field existed or whether
         * the name that this class encapsules is the default name.
         */
        private boolean existing;
        
        FieldInfo(String name, boolean existing){
            this.name = name;
            this.existing = existing;
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isExisting() {
            return existing;
        }
    }
}
