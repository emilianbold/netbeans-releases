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
import org.netbeans.modules.j2ee.dd.api.ejb.MethodParams;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class FinderMethodGenerator extends AbstractMethodGenerator {
    
    private FinderMethodGenerator(Entity ejb, FileObject ejbClassFileObject, FileObject ddFileObject) {
        super(ejb, ejbClassFileObject, ddFileObject);
    }
    
    public static FinderMethodGenerator create(Entity ejb, FileObject ejbClassFileObject, FileObject ddFileObject) {
        return new FinderMethodGenerator(ejb, ejbClassFileObject, ddFileObject);
    }
    
    public void generate(MethodModel methodModel, boolean generateLocal, boolean generateRemote,
            boolean isOneReturn, String ejbql) throws IOException {
        Entity entity = (Entity) ejb;
        if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType())) {
            generateCmp(methodModel, generateLocal, generateRemote, isOneReturn, ejbql);
        } else if (Entity.PERSISTENCE_TYPE_BEAN.equals(entity.getPersistenceType())) {
            generateBmp(methodModel, generateLocal, generateRemote, isOneReturn, ejbql);
        }
    }
    
    private void generateCmp(MethodModel methodModel, boolean generateLocal, boolean generateRemote,
            boolean isOneReturn, String ejbql) throws IOException {
        
        if (!methodModel.getName().startsWith("find")) {
            throw new IllegalArgumentException("The finder method name must have find as its prefix.");
        }
        
        // local interface EJB 2.1 spec 10.6.12
        if (generateLocal && ejb.getLocal() != null && ejb.getLocalHome() != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    isOneReturn ? ejb.getLocal() : "java.util.Collection",
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
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    isOneReturn ? ejb.getRemote() : "java.util.Collection",
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getHome());
            addMethod(methodModelCopy, fileObject, ejb.getHome());
        }
        
        // write query to deplyment descriptor
        addQueryToXml(methodModel, ejbql);
        
    }
    
    private void generateBmp(MethodModel methodModel, boolean generateLocal, boolean generateRemote,
            boolean isOneReturn, String ejbql) throws IOException {
        
        if (!methodModel.getName().startsWith("find")) {
            throw new IllegalArgumentException("The finder method name must have find as its prefix.");
        }
        
        // local interface EJB 2.1 spec 10.6.12
        if (generateLocal && ejb.getLocal() != null && ejb.getLocalHome() != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            // find method in LocalHome interface
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    isOneReturn ? ejb.getLocal() : "java.util.Collection",
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
            if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
                exceptions.add("javax.ejb.FinderException");
            }
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            // find method in RemoteHome interface
            MethodModel methodModelCopy = MethodModel.create(
                    'c' + methodModel.getName().substring(4),
                    isOneReturn ? ejb.getRemote() : "java.util.Collection",
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    Collections.<Modifier>emptySet()
                    );
            FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getHome());
            addMethod(methodModelCopy, fileObject, ejb.getHome());
            
            // ejbFind method in ejb class
            Entity entity = (Entity) ejb;
            methodModelCopy = MethodModel.create(
                    "ejbF" + methodModelCopy.getName().substring(1),
                    isOneReturn ? entity.getPrimKeyClass() : "java.util.Collection",
                    methodModelCopy.getBody(),
                    methodModelCopy.getParameters(),
                    methodModelCopy.getExceptions(),
                    Collections.singleton(Modifier.PUBLIC)
                    );
            addMethod(methodModelCopy, ejbClassFileObject, ejb.getEjbClass());
            
        }
        
        // ejbFind method in ejb class
        Entity entity = (Entity) ejb;
        List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
        if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
            exceptions.add("javax.ejb.FinderException");
        }
        MethodModel methodModelCopy = MethodModel.create(
                "ejbF" + methodModel.getName().substring(1),
                isOneReturn ? entity.getPrimKeyClass() : "java.util.Collection",
                "return null;",
                methodModel.getParameters(),
                exceptions,
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejb.getEjbClass());
        
    }
    
    private void addQueryToXml(MethodModel methodModel, String ejbql) throws IOException {
        Entity entity = (Entity) ejb;
        Query query = entity.newQuery();
        QueryMethod queryMethod = query.newQueryMethod();
        queryMethod.setMethodName(methodModel.getName());
        MethodParams methodParams = queryMethod.newMethodParams();
        for (MethodModel.Variable parameter : methodModel.getParameters()) {
            methodParams.addMethodParam(parameter.getType());
        }
        queryMethod.setMethodParams(methodParams);
        query.setQueryMethod(queryMethod);
        query.setEjbQl(ejbql);
        entity.addQuery(query);
        saveXml();
    }
    
}
