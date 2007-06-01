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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public final class CreateMethodGenerator extends AbstractMethodGenerator {
    
    private final String local;
    private final String localHome;
    private final String remote;
    private final String remoteHome;
    
    private CreateMethodGenerator(String ejbClass, FileObject ejbClassFileObject) {
        super(ejbClass, ejbClassFileObject);
        Map<String, String> interfaces = new HashMap<String, String>(4);
        try {
            interfaces = getInterfaces();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        this.local = interfaces.get(EntityAndSession.LOCAL);
        this.localHome = interfaces.get(EntityAndSession.LOCAL_HOME);
        this.remote = interfaces.get(EntityAndSession.REMOTE);
        this.remoteHome = interfaces.get(EntityAndSession.HOME);
    }
    
    public static CreateMethodGenerator create(String ejbClass, FileObject ejbClassFileObject) {
        return new CreateMethodGenerator(ejbClass, ejbClassFileObject);
    }
    
    public void generate(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        
        MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
        String ejbType = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                Ejb ejb = metadata.findByEjbClass(ejbClass);
                if (ejb instanceof Session) {
                    return EnterpriseBeans.SESSION;
                } else if (ejb instanceof Entity) {
                    return EnterpriseBeans.ENTITY;
                }
                return null;
            }
        });
        
        if (EnterpriseBeans.SESSION.equals(ejbType)) {
            generateSession(methodModel, generateLocal, generateRemote);
        } else if (EnterpriseBeans.ENTITY.equals(ejbType)) {
            generateEntity(methodModel, generateLocal, generateRemote);
        }
        
    }
    
    private void generateSession(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        
        if (!methodModel.getName().startsWith("create")) {
            throw new IllegalArgumentException("The method name must have create as its prefix.");
        }
        
        // local interface
        if (generateLocal && local != null && localHome != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    local,
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, localHome);
            addMethod(methodModelCopy, fileObject, localHome);
        }
        
        // remote interface
        if (generateRemote && remote != null && remoteHome != null) {
            List<String> exceptions = exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    remote,
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, remoteHome);
            addMethod(methodModelCopy, fileObject, remoteHome);
        }
        
        // ejb class
        List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
        if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
            exceptions.add("javax.ejb.CreateException");
        }
        MethodModel methodModelCopy = MethodModel.create(
                "ejbC" + methodModel.getName().substring(1),
                methodModel.getReturnType(),
                methodModel.getBody(),
                methodModel.getParameters(),
                exceptions,
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejbClass);
        
    }
    
    private void generateEntity(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        
        if (!methodModel.getName().startsWith("create")) {
            throw new IllegalArgumentException("The method name must have create as its prefix.");
        }
        
        // local interface
        if (generateLocal && local != null && localHome != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    local,
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, localHome);
            addMethod(methodModelCopy, fileObject, localHome);
            
        }
        
        // remote interface
        if (generateRemote && remote != null && remoteHome != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    remote,
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, remoteHome);
            addMethod(methodModelCopy, fileObject, remoteHome);
        }
        
        // ejb class
        List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
        if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
            exceptions.add("javax.ejb.CreateException");
        }

        String primKeyClass = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                Entity entity = (Entity) metadata.findByEjbClass(ejbClass);
                return entity.getPrimKeyClass();
            }
        });

        MethodModel methodModelCopy = MethodModel.create(
                "ejbC" + methodModel.getName().substring(1),
                primKeyClass,
                methodModel.getBody(),
                methodModel.getParameters(),
                exceptions,
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejbClass);
        MethodModel postCreateMethodModel = MethodModel.create(
                "ejbPostC" + methodModel.getName().substring(1),
                "void",
                "",
                methodModel.getParameters(),
                exceptions,
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(postCreateMethodModel, ejbClassFileObject, ejbClass);
    }
    
}
