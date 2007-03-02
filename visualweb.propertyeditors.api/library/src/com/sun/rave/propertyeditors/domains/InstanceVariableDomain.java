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
package com.sun.rave.propertyeditors.domains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignProject;

/**
 * Domain of identifiers of all instance variables matching the class type of
 * the property. If the property's bean is in page or request scope, then
 * instance variables in session and application scope will be included. If the
 * property's bean is in session scope context, instance variables from
 * application scope will be included.
 */
public class InstanceVariableDomain extends AttachedDomain {

    /**
     * Allows a component author to specify a subtype to filter the list of
     * instance variables.  This subtype (Class object or String class name)
     * must be a subtype of the property type - or it will be ignored.
     */
    public final static String INSTANCE_VARIABLE_SUBTYPE =
            "com.sun.rave.propertyeditors.domains.INSTANCE_VARIABLE_DOMAIN_SUBTYPE"; //NOI18N

    protected final static String SCOPE_REQUEST = "request";
    protected final static String SCOPE_SESSION = "session";
    protected final static String SCOPE_APPLICATION = "application";

    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignBean designBean){
        DesignProject designProject = designBean.getDesignContext().getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = designBean.getDesignContext();
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }

    public Element[] getElements() {

        DesignProperty designProperty = getDesignProperty();
        if (designProperty == null)
            return Element.EMPTY_ARRAY;

        Class propertyType = designProperty.getPropertyDescriptor().getPropertyType();

        FacesDesignProject facesDesignProject = (FacesDesignProject)designProperty.getDesignBean().getDesignContext().getProject();

        Object subPropertyType = designProperty.getPropertyDescriptor().getValue(INSTANCE_VARIABLE_SUBTYPE);
        if (subPropertyType instanceof Class && (propertyType.isAssignableFrom((Class) subPropertyType))) {
            propertyType = (Class)subPropertyType;
        } else if (subPropertyType instanceof String) {
            try {
                Class subPropertyClass = Class.forName((String) subPropertyType, false, facesDesignProject.getContextClassLoader());
                if (propertyType.isAssignableFrom(subPropertyClass))
                    propertyType = subPropertyClass;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Retrieve the valid contexts in which to look for instance variables
        DesignContext thisContext = designProperty.getDesignBean().getDesignContext();

        //DesignContext[] scanContexts = getScanContexts(thisContext, thisContext.getProject().getDesignContexts());
        DesignContext[] scanContexts = getScanContexts(thisContext, getDesignContexts(designProperty.getDesignBean()));
       
        // Scan all the beans and accumulate instance variables of all of them
        // that match the property type
        Set nameSet = new TreeSet();
        Map elementMap = new HashMap();
        for (int c = 0; c < scanContexts.length; c++) {
            DesignContext context = scanContexts[c];
            DesignBean beans[] = context.getBeansOfType(propertyType);
            for (int i = 0; i < beans.length; i++) {
                Object instance = beans[i].getInstance();
                if (propertyType.isAssignableFrom(instance.getClass())) {
                    String name;
                    Element element;
                    if (context == thisContext) {
                        name = beans[i].getInstanceName();
                        element = new Element(beans[i].getInstance(), name);
                    } else {
                        name = beans[i].getInstanceName() + " (" + context.getDisplayName() + ")";
                        String initString = getInitString(context, beans[i], propertyType);
                        element = new InstanceElement(beans[i].getInstance(), name, initString);
                    }
                    nameSet.add(name);
                    elementMap.put(name, element);
                }
            }
        }

        if (nameSet.size() == 0) {
            return Element.EMPTY_ARRAY;
        }

        // Construct a list of elements of the retained identifiers
        Element elements[] = new Element[nameSet.size()];
        Iterator names = nameSet.iterator();
        int n = 0;
        while (names.hasNext()) {
            String name = (String)names.next();
            elements[n++] = (Element)elementMap.get(name);
        }
        return elements;
    }

    protected DesignContext[] getScanContexts(DesignContext thisContext, DesignContext[] allContexts) {
        ArrayList scanContexts = new ArrayList();
        scanContexts.add(thisContext);
        Object thisScope = thisContext.getContextData(Constants.ContextData.SCOPE);
        for (int i = 0; i < allContexts.length; i++) {
            DesignContext context = allContexts[i];
            if (context == thisContext) continue;
            Object scope = context.getContextData(Constants.ContextData.SCOPE);
            if (SCOPE_REQUEST.equals(scope) && SCOPE_REQUEST.equals(thisScope)) {
                // check if this is the RequestBean
                Object baseClass = context.getContextData(Constants.ContextData.BASE_CLASS);
                if (baseClass instanceof Class && "com.sun.rave.web.ui.appbase.AbstractRequestBean".equals(((Class)baseClass).getName())) {
                    scanContexts.add(context);
                }
            } else if (SCOPE_SESSION.equals(scope) && (SCOPE_REQUEST.equals(thisScope) || SCOPE_SESSION.equals(thisScope))) {
                scanContexts.add(context);
            } else if (SCOPE_APPLICATION.equals(scope) && (SCOPE_REQUEST.equals(thisScope) || SCOPE_SESSION.equals(thisScope) || SCOPE_APPLICATION.equals(thisScope))) {
                scanContexts.add(context);
            }
        }
        return (DesignContext[])scanContexts.toArray(new DesignContext[scanContexts.size()]);
    }

    protected String getInitString(DesignContext context, DesignBean bean, Class propType) {
        String typeName = getJavaTypeName(propType.getName());
        return "(" + typeName + ")getValue(\"#{" + context.getDisplayName() + "." + bean.getInstanceName() + "}\")";
    }

    public static class InstanceElement extends Element {

        protected String javaInitString;

        public InstanceElement(Object value, String label, String javaInitString) {
            super(value, label);
            this.javaInitString = javaInitString;
        }

        public String getJavaInitializationString() {
            return javaInitString;
        }
    }
    
    
    private static HashMap arrayTypeKeyHash = new HashMap();
    static {
        arrayTypeKeyHash.put("B", "byte");   //NOI18N
        arrayTypeKeyHash.put("C", "char");   //NOI18N
        arrayTypeKeyHash.put("D", "double");   //NOI18N
        arrayTypeKeyHash.put("F", "float");   //NOI18N
        arrayTypeKeyHash.put("I", "int");   //NOI18N
        arrayTypeKeyHash.put("J", "long");   //NOI18N
        arrayTypeKeyHash.put("S", "short");   //NOI18N
        arrayTypeKeyHash.put("Z", "boolean");   //NOI18N
        arrayTypeKeyHash.put("V", "void");   //NOI18N
    }
    
    private static String getJavaTypeName(String tn) {
        if (tn.startsWith("[")) {   //NOI18N
            int depth = 0;
            while (tn.startsWith("[")) {   //NOI18N
                tn = tn.substring(1);
                depth++;
            }
            if (tn.startsWith("L")) {   //NOI18N
                tn = tn.substring(1);
                tn = tn.substring(0, tn.length() - 1);
            } else {
                char typeKey = tn.charAt(0);
                tn = (String)arrayTypeKeyHash.get("" + typeKey);   //NOI18N
            }
            for (int i = 0; i < depth; i++) {
                tn += "[]";   //NOI18N
            }
        }
        return tn;
    }
}
