/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.openide.filesystems.FileObject;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.Type;

/**
 * @author pfiala
 */
public class QueryMethodHelper {

    private ClassElement beanClass;
    private Entity entity;
    private FileObject ejbJarFile;
    private final Query query;
    private boolean isSelectMethod;

    public QueryMethodHelper(FileObject ejbJarFile, Entity entity, Query query) {
        this.entity = entity;
        this.ejbJarFile = ejbJarFile;
        this.query = query;
        beanClass = Utils.getBeanClass(this.ejbJarFile, this.entity);
        isSelectMethod = query.getQueryMethod().getMethodName().startsWith("ejbSelectBy"); //NOI18N
    }

    public String getMethodName() {
        return query.getQueryMethod().getMethodName();
    }

    public String getReturnType() {
        ClassElement classElement;
        if (isSelectMethod) {
            //select method
            classElement = beanClass;
        } else {
            //finder method
            String localHomeName = entity.getLocalHome();
            classElement = Utils.getClassElement(ejbJarFile, localHomeName);
        }
        QueryMethod queryMethod = query.getQueryMethod();
        String[] methodParam = queryMethod.getMethodParams().getMethodParam();
        Type[] types = new Type[methodParam.length];
        for (int i = 0; i < methodParam.length; i++) {
            types[i] = Type.parse(methodParam[i]);
        }
        String methodName = queryMethod.getMethodName();
        MethodElement method = classElement.getMethod(Identifier.create(methodName), types);
        return method == null ? null : method.getReturn().getFullString();
    }

    public boolean returnsCollection() {
        return "java.util.Collection".equals(getReturnType());//NOI18N
    }

    public String getResultInterface() {
        // TODO: return local/remote
        return ""; //NOI18N
    }
}
