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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.api.ejbjar.ResourceReference;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.ServiceLocatorStrategy;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public final class UseDatabaseGenerator {
    
    public UseDatabaseGenerator() {
    }

    public void generate(final FileObject fileObject, final ElementHandle<TypeElement> elementHandle, 
                         final J2eeModuleProvider j2eeModuleProvider, final String datasourceReferenceName, 
                         final Datasource datasource, final boolean createServerResources, String serviceLocator) 
                         throws IOException, ConfigurationException {
        
        Project project = FileOwnerQuery.getOwner(fileObject);
        ServiceLocatorStrategy serviceLocatorStrategy = (serviceLocator == null) ? null : 
            ServiceLocatorStrategy.create(project, fileObject, serviceLocator);
        EnterpriseReferenceContainer erc = project.getLookup().lookup(EnterpriseReferenceContainer.class);
        String className = elementHandle.getQualifiedName();
        if (Utils.isJavaEE5orHigher(project) && serviceLocatorStrategy == null &&
                InjectionTargetQuery.isInjectionTarget(fileObject, className)) {
            boolean isStatic = InjectionTargetQuery.isStaticReferenceRequired(fileObject, className);
            String fieldName = Utils.jndiNameToCamelCase(datasourceReferenceName, true, null);
            _RetoucheUtil.generateAnnotatedField(fileObject, className, "javax.annotation.Resource", fieldName, // NOI18N
                    "javax.sql.DataSource", Collections.singletonMap("name", datasourceReferenceName), isStatic); // NOI18N
        } else {
            String jndiName = generateJNDILookup(datasourceReferenceName, erc, fileObject, className, datasource.getUrl());
            if (jndiName != null) {
                generateLookupMethod(fileObject, className, datasourceReferenceName, serviceLocatorStrategy);
            }
        }
        
        if (createServerResources) {
            try {            
                j2eeModuleProvider.getConfigSupport().createDatasource(
                    datasource.getJndiName(),
                    datasource.getUrl(),
                    datasource.getUsername(),
                    datasource.getPassword(),
                    datasource.getDriverClassName()
                    );
            } catch (DatasourceAlreadyExistsException daee) {
                Exceptions.printStackTrace(daee);
            }
        }

        J2eeModule module = j2eeModuleProvider.getJ2eeModule();
        if (isWebOrAppClientModule(module)) {
            bindDataSourceReference(j2eeModuleProvider, datasourceReferenceName, datasource);
        }
        else if (isEjbModule(module)) {
            bindDataSourceReferenceForEjb(j2eeModuleProvider, datasourceReferenceName, datasource, fileObject, elementHandle);
        }
        
        if (serviceLocator != null) {
            erc.setServiceLocatorName(serviceLocator);
        }
    }
    
    private void bindDataSourceReference(J2eeModuleProvider j2eeModuleProvider, String dsRefName, Datasource datasource) 
    throws ConfigurationException {

        String dsJndiName = datasource.getJndiName();
        j2eeModuleProvider.getConfigSupport().bindDatasourceReference(dsRefName, dsJndiName);
    }
    
    private void bindDataSourceReferenceForEjb(J2eeModuleProvider j2eeModuleProvider, String dsRefName, Datasource datasource,
            FileObject fileObject, final ElementHandle<TypeElement> elementHandle) throws ConfigurationException, IOException {

        final String[] ejbName = new String[1];
        final String[] ejbType = new String[1];

        MetadataModel<EjbJarMetadata> metadataModel = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject).getMetadataModel();
        metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws Exception {
                Ejb ejb = metadata.findByEjbClass(elementHandle.getQualifiedName());
                ejbName[0] = ejb.getEjbName();
                if (ejb instanceof Session) {
                    ejbType[0] = EnterpriseBeans.SESSION;
                } else if (ejb instanceof MessageDriven) {
                    ejbType[0] = EnterpriseBeans.MESSAGE_DRIVEN;
                } else if (ejb instanceof Entity) {
                    ejbType[0] = EnterpriseBeans.ENTITY;
                }
                return null;
            }
        });
        
        String dsJndiName = datasource.getJndiName();
        j2eeModuleProvider.getConfigSupport().bindDatasourceReferenceForEjb(ejbName[0], ejbType[0], dsRefName, dsJndiName);
    }
    
    private boolean isWebOrAppClientModule(J2eeModule module) {
        Object moduleType = module.getModuleType();
        return J2eeModule.WAR.equals(moduleType) || J2eeModule.CLIENT.equals(moduleType);
    }
    
    private boolean isEjbModule(J2eeModule module) {
        return module.getModuleType().equals(J2eeModule.EJB);
    }
    
    private String generateJNDILookup(String datasourceReferenceName, EnterpriseReferenceContainer enterpriseReferenceContainer, 
            FileObject fileObject, String className, String nodeName) throws IOException {
        ResourceReference resourceReference = ResourceReference.create(
                datasourceReferenceName,
                javax.sql.DataSource.class.getName(),
                ResourceRef.RES_AUTH_CONTAINER,
                ResourceRef.RES_SHARING_SCOPE_SHAREABLE,
                nodeName
                );
        return enterpriseReferenceContainer.addResourceRef(resourceReference, fileObject, className);
    }
    
    private void generateLookupMethod(FileObject fileObject, final String className, final String datasourceReferenceName, 
            final ServiceLocatorStrategy slStrategy) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final String body = slStrategy == null ? getLookupCode(datasourceReferenceName) : getLookupCode(datasourceReferenceName, slStrategy, fileObject, className);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                String methodName = "get" + Utils.jndiNameToCamelCase(datasourceReferenceName, false, null); //NO18N
                MethodModel methodModel = MethodModel.create(
                        methodName,
                        javax.sql.DataSource.class.getName(),
                        body,
                        Collections.<MethodModel.Variable>emptyList(),
                        Collections.singletonList(javax.naming.NamingException.class.getName()),
                        Collections.singleton(Modifier.PRIVATE)
                        );
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                methodTree = (MethodTree) GeneratorUtilities.get(workingCopy).importFQNs(methodTree);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree modifiedClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();
    }
    
    private String getLookupCode(String jndiName, ServiceLocatorStrategy serviceLocatorStrategy, FileObject fileObject, String className) {
        String jdbcLookupString = serviceLocatorStrategy.genDataSource(jndiName, fileObject, className);
        return "return (javax.sql.DataSource) " + jdbcLookupString + ";\n"; // NOI18N
    }
    
    private String getLookupCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.sql.DataSource) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] { jndiName });
    }
    
}
