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

import org.netbeans.modules.j2ee.dd.api.ejb.MethodParams;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;

import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * @author pfiala
 */
public class QueryMethodHelper {

    final Query query;
    private final EntityHelper entityHelper;
    private boolean isSelectMethod;
    private MethodElement implementationMethod;
    public MethodElement remoteMethod;
    public MethodElement localMethod;

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
        ClassElement homeClass = entityHelper.getHomeInterfaceClass();
        remoteMethod = homeClass == null ? null : homeClass.getMethod(methodName, types);
        ClassElement localHomeClass = entityHelper.getLocalHomeInterfaceClass();
        localMethod = localHomeClass == null ? null : localHomeClass.getMethod(methodName, types);
    }

    public String getMethodName() {
        return query.getQueryMethod().getMethodName();
    }

    public String getReturnType() {
        MethodElement method = getPrototypeMethod();
        return method == null ? null : method.getReturn().getFullString();
    }

    public boolean returnsCollection() {
        return "java.util.Collection".equals(getReturnType());//NOI18N
    }

    public String getResultInterface() {
        ClassElement localHomeClass = entityHelper.getLocalHomeInterfaceClass();
        ClassElement homeClass = entityHelper.getHomeInterfaceClass();

        QueryMethod queryMethod = query.getQueryMethod();
        Type[] types = getQueryMethodParamTypes(queryMethod);
        String methodName = queryMethod.getMethodName();
        boolean hasLocal = localHomeClass == null ?
                false : localHomeClass.getMethod(Identifier.create(methodName), types) != null;
        boolean hasRemote = homeClass == null ?
                false : homeClass.getMethod(Identifier.create(methodName), types) != null;
        String remote = "remote"; //NOI18N;
        String local = "local"; //NOI18N;
        if (hasLocal) {
            if (hasRemote) {
                return local + "+" + remote; //NOI18N;
            } else {
                return local;
            }
        } else {
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
            MethodParameter parameter = null;
            Type type;
            try {
                parameter = MethodParameter.parse(methodParam[i]);
                type = parameter.getType();
            } catch (IllegalArgumentException e) {
                type = Type.parse(methodParam[i]);
            }
            types[i] = type;
        }
        return types;
    }

    public void removeQuery() {
        if (implementationMethod != null) {
            try {
                entityHelper.beanClass.removeMethod(implementationMethod);
            } catch (SourceException e) {
                notifyError(e);
            }
        }
        if (localMethod != null) {
            try {
                entityHelper.getLocalHomeInterfaceClass().removeMethod(localMethod);
            } catch (SourceException e) {
                notifyError(e);
            }
        }
        if (remoteMethod != null) {
            try {
                entityHelper.getHomeInterfaceClass().removeMethod(remoteMethod);
            } catch (SourceException e) {
                notifyError(e);
            }
        }
        entityHelper.removeQuery(query);
    }

    public MethodElement getPrototypeMethod() {
        MethodElement prototypeMethod = null;
        if (isSelectMethod) {
            //select method
            prototypeMethod = implementationMethod;
        } else {
            //finder method
            if (localMethod != null) {
                prototypeMethod = localMethod;
            } else if (remoteMethod != null) {
                prototypeMethod = remoteMethod;
            }
        }
        if (prototypeMethod == null) {
            prototypeMethod = new MethodElement();
            QueryMethod queryMethod = query.getQueryMethod();
            try {
                prototypeMethod.setName(Identifier.create(queryMethod.getMethodName()));
            } catch (SourceException e) {
                notifyError(e);
            }
            MethodParams queryParams = queryMethod.getMethodParams();
            MethodParameter[] params = new MethodParameter[queryParams.sizeMethodParam()];
            for (int i = 0; i < params.length; i++) {
                String queryParam = queryParams.getMethodParam(i);
                try {
                    params[i] = MethodParameter.parse(queryParam);
                } catch (IllegalArgumentException e) {
                    Type type = Type.parse(queryParam);
                    params[i] = new MethodParameter("p" + i, type, false);
                }
            }
            try {
                prototypeMethod.setParameters(params);
            } catch (SourceException e) {
                notifyError(e);
            }
        }
        return prototypeMethod;
    }

    public void updateFinderMethod(MethodElement prototype, Query query, boolean singleReturn, boolean publishToLocal,
            boolean publishToRemote) {
        //todo: validation
        try {
            prototype.setModifiers(0);
        } catch (SourceException e) {
            notifyError(e);
        }
        if (publishToLocal) {
            localMethod = setMethod(localMethod, prototype, singleReturn, false);
        } else {
            localMethod = removeMethod(localMethod, false);
        }
        if (publishToRemote) {
            remoteMethod = setMethod(remoteMethod, prototype, singleReturn, true);
        } else {
            remoteMethod = removeMethod(remoteMethod, true);
        }
        updateQuery(query);
    }

    private void updateQuery(Query query) {
        this.query.setQueryMethod(query.getQueryMethod());
        this.query.setDescription(query.getDefaultDescription());
        this.query.setEjbQl(query.getEjbQl());
        entityHelper.modelUpdatedFromUI();
    }

    private MethodElement setMethod(MethodElement method, MethodElement prototype, boolean singleReturn,
            boolean remote) {
        ClassElement interfaceClass = getHomeClass(remote);
        setReturn(prototype, singleReturn, remote);
        if (method == null) {
            Utils.addMethod(interfaceClass, (MethodElement) prototype.clone(), remote);
            method = Utils.getMethod(interfaceClass, prototype);
        } else {
            updateMethod(method, prototype);
        }
        return method;
    }

    private MethodElement removeMethod(MethodElement method, boolean remote) {
        ClassElement interfaceClass = getHomeClass(remote);
        if (remoteMethod != null) {
            try {
                interfaceClass.removeMethod(method);
            } catch (SourceException e) {
                notifyError(e);
                return method;
            }
        }
        return null;
    }

    private ClassElement getHomeClass(boolean remote) {
        ClassElement interfaceClass = remote ?
                entityHelper.getHomeInterfaceClass() : entityHelper.getLocalHomeInterfaceClass();
        return interfaceClass;
    }

    private void setReturn(MethodElement prototype, boolean singleReturn, boolean remote) {
        try {
            String interfaceName = remote ? entityHelper.getRemote() : entityHelper.getLocal();
            prototype.setReturn(Type.parse(singleReturn ? interfaceName : Collection.class.getName()));
        } catch (SourceException e) {
            notifyError(e);
        }
    }

    public void updateSelectMethod(MethodElement prototype, Query query) {
        //todo: validation
        try {
            prototype.setModifiers(Modifier.PUBLIC | Modifier.ABSTRACT);
        } catch (SourceException e) {
            notifyError(e);
        }
        if (implementationMethod == null) {
            Utils.addMethod(entityHelper.beanClass, prototype);
            implementationMethod = prototype;
        } else {
            updateMethod(implementationMethod, prototype);
        }
        updateQuery(query);
    }

    private static void updateMethod(MethodElement method, MethodElement prototype) {
        if (method != null) {
            try {
                method.setName(prototype.getName());
            } catch (SourceException e) {
                notifyError(e);
            }
            try {
                method.setReturn(prototype.getReturn());
            } catch (SourceException e) {
                notifyError(e);
            }
            try {
                method.setParameters(prototype.getParameters());
            } catch (SourceException e) {
                notifyError(e);
            }
            try {
                method.setExceptions(prototype.getExceptions());
            } catch (SourceException e) {
                notifyError(e);
            }
            try {
                method.setModifiers(prototype.getModifiers());
            } catch (SourceException e) {
                notifyError(e);
            }
        }
    }

    private static void notifyError(Exception ex) {
        Utils.notifyError(ex);
    }

    public QueryMethod getQueryMethod() {
        return query.getQueryMethod();
    }

    public String getEjbQl() {
        return query.getEjbQl();
    }

    public String getDefaultDescription() {
        return query.getDefaultDescription();
    }
}
