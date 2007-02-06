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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.ServiceLocatorStrategy;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class UseDatabaseGenerator {
    
    public UseDatabaseGenerator() {
    }

    public void generate(final FileObject fileObject, final ElementHandle<TypeElement> elementHandle, final Datasource datasource, 
            final boolean createServerResources, String serviceLocator) throws IOException {
        final Project project = FileOwnerQuery.getOwner(fileObject);
        final ServiceLocatorStrategy serviceLocatorStrategy = (serviceLocator == null) ? null : 
            ServiceLocatorStrategy.create(project, fileObject, serviceLocator);

        final EnterpriseReferenceContainer erc = project.getLookup().lookup(EnterpriseReferenceContainer.class);
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = elementHandle.resolve(workingCopy);
                if (Utils.isJavaEE5orHigher(project) &&
                        InjectionTargetQuery.isInjectionTarget(workingCopy, typeElement) &&
                        serviceLocatorStrategy == null) {
                    generateInjectedField(workingCopy, typeElement, datasource.getJndiName());
                } else {
                    String jndiName = generateJNDILookup(datasource.getJndiName(), erc, 
                            fileObject, typeElement.getQualifiedName().toString(), 
                            datasource.getUrl(), createServerResources);
                    generateLookupMethod(workingCopy, typeElement, jndiName, serviceLocatorStrategy);
                }
            }
        }).commit();

        if (serviceLocator != null) {
            erc.setServiceLocatorName(serviceLocator);
        }
    }
    
    private String generateJNDILookup(String jndiName, EnterpriseReferenceContainer enterpriseReferenceContainer, 
            FileObject fileObject, String className, String nodeName, boolean createServerResources) throws IOException {
        ResourceRef ref = enterpriseReferenceContainer.createResourceRef(className);
        if (createServerResources) {
            ref.setDescription(nodeName);
        }
        ref.setResRefName(jndiName);
        ref.setResAuth(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_AUTH_CONTAINER);
        ref.setResSharingScope(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_SHARING_SCOPE_SHAREABLE);
        ref.setResType(javax.sql.DataSource.class.getName());
        return enterpriseReferenceContainer.addResourceRef(ref, fileObject, className);
    }
    
    private void generateLookupMethod(WorkingCopy workingCopy, TypeElement typeElement, String jndiName, ServiceLocatorStrategy serviceLocatorStrategy) {
        String methodName = "get" + Utils.jndiNameToCamelCase(jndiName, false, null); //NO18N
        MethodModel methodModel = MethodModel.create(
                methodName,
                javax.sql.DataSource.class.getName(),
                serviceLocatorStrategy == null ? getLookupCode(jndiName) : getLookupCode(jndiName, serviceLocatorStrategy, typeElement),
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList(javax.naming.NamingException.class.getName()),
                Collections.singleton(Modifier.PRIVATE)
                );
        MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
        ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
        ClassTree modifiedClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
        workingCopy.rewrite(classTree, modifiedClassTree);
    }
    
    
    private String getLookupCode(String jndiName, ServiceLocatorStrategy serviceLocatorStrategy, TypeElement target) {
        //TODO: RETOUCHE
        return getLookupCode(jndiName);
//        String jdbcLookupString = serviceLocatorStrategy.genDataSource(jndiName, target);
//        return "return (javax.sql.DataSource) " + jdbcLookupString + ";\n"; // NOI18N
    }
    
    private String getLookupCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.sql.DataSource) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] {jndiName});
    }
    
    private void generateInjectedField(WorkingCopy workingCopy, TypeElement javaClass, String jndiName) {
        GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy, javaClass);
        Set<Modifier> modifiers = new HashSet<Modifier>();
        modifiers.add(Modifier.PRIVATE);
        if (InjectionTargetQuery.isStaticReferenceRequired(workingCopy, javaClass)) {
            modifiers.add(Modifier.STATIC);
        }
        String fieldName = Utils.jndiNameToCamelCase(jndiName, true, null);
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        TypeElement returnTypeElement = workingCopy.getElements().getTypeElement("javax.sql.DataSource");
        VariableTree variableTree = treeMaker.Variable(
                treeMaker.Modifiers(modifiers),
                fieldName,
                treeMaker.QualIdent(returnTypeElement),
                null
                );
        ExpressionTree attributeTree = generationUtils.createAnnotationArgument("name", jndiName);
        AnnotationTree annotationTree = generationUtils.createAnnotation("javax.annotation.Resource", Collections.singletonList(attributeTree));
        ModifiersTree modifiersTree = treeMaker.addModifiersAnnotation(variableTree.getModifiers(), annotationTree);
        workingCopy.rewrite(variableTree.getModifiers(), modifiersTree);
    }
    
}
