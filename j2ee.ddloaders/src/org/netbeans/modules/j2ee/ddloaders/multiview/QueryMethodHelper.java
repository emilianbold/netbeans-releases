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

import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.Type;
import org.openide.src.SourceException;

/**
 * @author pfiala
 */
public class QueryMethodHelper {

    private final Query query;
    private final EntityHelper entityHelper;
    private boolean isSelectMethod;
    private MethodElement implementationMethod;
    private MethodElement remoteMethod;
    private MethodElement localMethod;

    public QueryMethodHelper(EntityHelper helper, Query query) {
        this.query = query;
        this.entityHelper = helper;
        isSelectMethod = query.getQueryMethod().getMethodName().startsWith("ejbSelectBy"); //NOI18N
        init();
    }

    private void init() {
        QueryMethod queryMethod = query.getQueryMethod();
        Type[] types = getQueryMethodParamTypes(queryMethod);
        Identifier methodName = Identifier.create(queryMethod.getMethodName());
        implementationMethod = entityHelper.beanClass.getMethod(methodName, types);
        ClassElement homeClass = entityHelper.getHomeClass();
        remoteMethod = homeClass == null ? null : homeClass.getMethod(methodName, types);
        ClassElement localHomeClass = entityHelper.getLocalHomeClass();
        localMethod = localHomeClass == null ? null : localHomeClass.getMethod(methodName, types);
    }

    public String getMethodName() {
        return query.getQueryMethod().getMethodName();
    }

    public String getReturnType() {
        MethodElement method;
        if (isSelectMethod) {
            //select method
            method = implementationMethod;
        } else {
            //finder method
            if (localMethod != null) {
                method = localMethod;
            } else {
                method = remoteMethod;
            }
        }
        return method == null ? null : method.getReturn().getFullString();
    }

    public boolean returnsCollection() {
        return "java.util.Collection".equals(getReturnType());//NOI18N
    }

    public String getResultInterface() {
        ClassElement localHomeClass = entityHelper.getLocalHomeClass();
        ClassElement homeClass = entityHelper.getHomeClass();

        QueryMethod queryMethod = query.getQueryMethod();
        Type[] types = getQueryMethodParamTypes(queryMethod);
        String methodName = queryMethod.getMethodName();
        boolean hasLocal = localHomeClass.getMethod(Identifier.create(methodName), types) != null;
        boolean hasRemote = homeClass.getMethod(Identifier.create(methodName), types) != null;
        String remote = "remote"; //NOI18N;
        String local = "local"; //NOI18N;
        if (hasLocal) {
            if (hasRemote) {
                return local + "+" + remote; //NOI18N;
            } else {
                return local;
            }
        } else{
            if (hasRemote) {
                return remote;
            } else {
                return "-"; //NOI18N;
            }
        }
    }

    private Type[] getQueryMethodParamTypes(QueryMethod queryMethod) {
        String[] methodParam = queryMethod.getMethodParams().getMethodParam();
        Type[] types = new Type[methodParam.length];
        for (int i = 0; i < methodParam.length; i++) {
            types[i] = Type.createClass(Identifier.create(methodParam[i]));
        }
        return types;
    }

    public void removeQuery() {
        if (implementationMethod != null) {
            try {
                entityHelper.beanClass.removeMethod(implementationMethod);
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        }
        if (localMethod != null) {
            try {
                entityHelper.getLocalHomeClass().removeMethod(localMethod);
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        }
        if (remoteMethod != null) {
            try {
                entityHelper.getHomeClass().removeMethod(remoteMethod);
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        }
        entityHelper.removeQuery(query);
    }
}
