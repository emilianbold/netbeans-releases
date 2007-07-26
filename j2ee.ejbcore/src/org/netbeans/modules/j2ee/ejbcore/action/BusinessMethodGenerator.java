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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class BusinessMethodGenerator extends AbstractMethodGenerator {
    
    private BusinessMethodGenerator(String ejbClass, FileObject ejbClassFileObject) {
        super(ejbClass, ejbClassFileObject);
    }
    
    public static BusinessMethodGenerator create(String ejbClass, FileObject ejbClassFileObject) {
        return new BusinessMethodGenerator(ejbClass, ejbClassFileObject);
    }
    
    public void generate(final MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        
        Map<String, String> interfaces = getInterfaces();
        String local = interfaces.get(EntityAndSession.LOCAL);
        final String remote = interfaces.get(EntityAndSession.REMOTE);

        // local interface
        if (generateLocal && local != null) {
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    methodModel.getExceptions(),
                    methodModel.getModifiers()
                    );
            addMethodToInterface(methodModelCopy, local);
        }
        
        // remote interface, add RemoteException if it's not there (in EJB 2.1)
        if (generateRemote && remote != null) {
            
            final List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());

            MetadataModel<EjbJarMetadata> metadataModel = EjbJar.getEjbJar(ejbClassFileObject).getMetadataModel();
            BigDecimal version = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, BigDecimal>() {
                public BigDecimal run(EjbJarMetadata metadata) throws Exception {
                    return metadata.getRoot().getVersion();
                }
            });
            final boolean isEjb2x = (version != null && version.doubleValue() <= 2.1);
            
            JavaSource javaSource = JavaSource.forFileObject(ejbClassFileObject);
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    if (isEjb2x) {
                        exceptions.add("java.rmi.RemoteException"); // NOI18N
                    } else {
                        TypeElement typeElement = controller.getElements().getTypeElement(ejbClass);
                        TypeMirror remoteType = controller.getElements().getTypeElement("java.rmi.Remote").asType(); // NOI18N
                        if (typeElement != null) {
                            for (TypeMirror typeMirror : typeElement.getInterfaces()) {
                                if (controller.getTypes().isSameType(remoteType, typeMirror)) {
                                    if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) { // NOI18N
                                        exceptions.add("java.rmi.RemoteException"); // NOI18N
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
            
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    methodModel.getModifiers()
                    );
            addMethodToInterface(methodModelCopy, remote);
        }
        
        // ejb class, add 'public' modifier
        MethodModel methodModelCopy = MethodModel.create(
                methodModel.getName(),
                methodModel.getReturnType(),
                methodModel.getBody(),
                methodModel.getParameters(),
                methodModel.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
        
        addMethod(methodModelCopy, ejbClassFileObject, ejbClass);
        
    }
    
}
