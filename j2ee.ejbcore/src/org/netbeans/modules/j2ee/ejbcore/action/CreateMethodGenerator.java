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
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class CreateMethodGenerator extends AbstractMethodGenerator {
    
    private CreateMethodGenerator(EntityAndSession ejb, FileObject ejbClassFileObject) {
        super(ejb, ejbClassFileObject, null);
    }
    
    public static CreateMethodGenerator create(EntityAndSession ejb, FileObject ejbClassFileObject) {
        return new CreateMethodGenerator(ejb, ejbClassFileObject);
    }
    
    public void generate(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        if (ejb instanceof Session) {
            generateSession(methodModel, generateLocal, generateRemote);
        } else if (ejb instanceof Entity) {
            generateEntity(methodModel, generateLocal, generateRemote);
        }
    }
    
    private void generateSession(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        
        if (!methodModel.getName().startsWith("create")) {
            throw new IllegalArgumentException("The method name must have create as its prefix.");
        }
        
        // local interface
        if (generateLocal && ejb.getLocal() != null && ejb.getLocalHome() != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    ejb.getLocal(),
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getLocalHome());
            addMethod(methodModelCopy, fileObject, ejb.getLocalHome());
            
            // remote interface
            if (generateRemote && ejb.getRemote() != null && ejb.getHome() != null) {
                exceptions = new ArrayList<String>(methodModel.getExceptions());
                if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                    exceptions.add("javax.ejb.CreateException");
                }
                if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                    exceptions.add("java.rmi.RemoteException");
                }
                methodModelCopy = MethodModel.create(
                        methodModel.getName(),
                        ejb.getRemote(),
                        null,
                        methodModel.getParameters(),
                        exceptions,
                        Collections.<Modifier>emptySet()
                        );
                fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getHome());
                addMethod(methodModelCopy, fileObject, ejb.getHome());
            }
            
            // ejb class
            exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            methodModelCopy = MethodModel.create(
                    "ejbC" + methodModel.getName().substring(1),
                    methodModel.getReturnType(),
                    methodModel.getBody(),
                    methodModel.getParameters(),
                    exceptions,
                    Collections.singleton(Modifier.PUBLIC)
                    );
            addMethod(methodModelCopy, ejbClassFileObject, ejb.getEjbClass());
            
        }
    }
    
    private void generateEntity(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        
        if (!methodModel.getName().startsWith("create")) {
            throw new IllegalArgumentException("The method name must have create as its prefix.");
        }
        
        // local interface
        if (generateLocal && ejb.getLocal() != null && ejb.getLocalHome() != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    ejb.getLocal(),
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getLocalHome());
            addMethod(methodModelCopy, fileObject, ejb.getLocalHome());
            
        }
        
        // remote interface
        if (generateRemote && ejb.getRemote() != null && ejb.getHome() != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
                exceptions.add("javax.ejb.CreateException");
            }
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    ejb.getRemote(),
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getHome());
            addMethod(methodModelCopy, fileObject, ejb.getHome());
        }
        
        // ejb class
        List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
        if (!methodModel.getExceptions().contains("javax.ejb.CreateException")) {
            exceptions.add("javax.ejb.CreateException");
        }
        MethodModel methodModelCopy = MethodModel.create(
                "ejbC" + methodModel.getName().substring(1),
                ((Entity) ejb).getPrimKeyClass(),
                methodModel.getBody(),
                methodModel.getParameters(),
                exceptions,
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejb.getEjbClass());
        MethodModel postCreateMethodModel = MethodModel.create(
                "ejbPostC" + methodModel.getName().substring(1),
                "void",
                "",
                methodModel.getParameters(),
                exceptions,
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(postCreateMethodModel, ejbClassFileObject, ejb.getEjbClass());
    }
    
}
