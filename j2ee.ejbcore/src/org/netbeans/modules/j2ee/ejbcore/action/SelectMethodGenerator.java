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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodParams;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class SelectMethodGenerator extends AbstractMethodGenerator {
    
    public SelectMethodGenerator(Entity ejb, FileObject ejbClassFileObject, FileObject ddFileObject) {
        super(ejb, ejbClassFileObject, ddFileObject);
    }
    
    public static SelectMethodGenerator create(Entity ejb, FileObject ejbClassFileObject, FileObject ddFileObject) {
        return new SelectMethodGenerator(ejb, ejbClassFileObject, ddFileObject);
    }
    
    public void generate(MethodModel methodModel, boolean generateLocal, boolean generateRemote,
            boolean isOneReturn, String ejbql) throws IOException {
        
        if (!methodModel.getName().startsWith("ejbSelect")) {
            throw new IllegalArgumentException("The select method name must have ejbSelect as its prefix.");
        }
        
        List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
        if (!methodModel.getExceptions().contains("javax.ejb.FinderException")) {
            exceptions.add("javax.ejb.FinderException");
        }
        Set<Modifier> modifiers = new HashSet<Modifier>(2);
        modifiers.add(Modifier.PUBLIC);
        modifiers.add(Modifier.ABSTRACT);
        MethodModel methodModelCopy = MethodModel.create(
                methodModel.getName(),
                methodModel.getReturnType(),
                null,
                methodModel.getParameters(),
                exceptions,
                modifiers
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejb.getEjbClass());
        
        // write query to deplyment descriptor
        addQueryToXml(methodModel, ejbql);
        
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
