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

package org.netbeans.modules.web.core.syntax.completion;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.openide.loaders.DataObject;

/**
 *
 * @author petr
 */

/** Represents Implicit objects for EL
 **/
public class ELImplicitObjects {
    
    public static final int OBJECT_TYPE = 0;
    public static final int MAP_TYPE = 1;

    public static class ELImplicitObject {
        private String name;
        private int type;
                
        /** Creates a new instance of ELImplicitObject */
        public ELImplicitObject(String name) {
            this.name = name;
            this.setType(MAP_TYPE);
        }

        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
        
        public List<Property> getPossibbleValues(String exp, JspSyntaxSupport sup){
            return null;
        }

    }
    
    public static class Property{
        
        private String name;
        private String type;

        public Property(String name, String type){
            this.name = name;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
        
    }
    
    public static class PageContextObject extends ELImplicitObject{
        
        private static List <Property> property;
        
        public PageContextObject(String name){
            super(name);
            setType(ELImplicitObjects.OBJECT_TYPE);
            property = null;
        }
        
        public List<Property> getPossibbleValues(String exp, JspSyntaxSupport sup){
            List <Property> prop = null;
            String text = exp.substring(0, exp.lastIndexOf('.'));
            if(text.endsWith("pageContext")){                                           //NOI18N
                if (property == null){
                    property = new ArrayList();
                    property.add(new Property("request", "javax.servlet.ServletRequest"));            //NOI18N
                    property.add(new Property("response", "javax.servlet.ServletResponse"));          //NOI18N
                    property.add(new Property("servletConfig", "javax.servlet.ServletConfig"));       //NOI18N
                    property.add(new Property("servletContext", "javax.servlet.ServletContext"));     //NOI18N
                    property.add(new Property("session", "javax.servlet.HttpSession"));               //NOI18N
                }
                prop = property;
            }
            else {
                DataObject obj = NbEditorUtilities.getDataObject(sup.getDocument());
                JavaClass javaClass = null;
                if (obj != null){
                    if (text.endsWith("request"))
                        javaClass = JMIUtil.findClass("javax.servlet.ServletRequest", ClassPath.getClassPath(obj.getPrimaryFile(), ClassPath.COMPILE));
                    else if (text.endsWith("response"))
                        javaClass = JMIUtil.findClass("javax.servlet.ServletResponse", ClassPath.getClassPath(obj.getPrimaryFile(), ClassPath.COMPILE));
                    else if (text.endsWith("session"))
                        javaClass = JMIUtil.findClass("javax.servlet.http.HttpSession", ClassPath.getClassPath(obj.getPrimaryFile(), ClassPath.COMPILE));
                    else if (text.endsWith("servletConfig"))
                        javaClass = JMIUtil.findClass("javax.servlet.ServletConfig", ClassPath.getClassPath(obj.getPrimaryFile(), ClassPath.COMPILE));
                    else if (text.endsWith("servletContext"))
                        javaClass = JMIUtil.findClass("javax.servlet.ServletContext", ClassPath.getClassPath(obj.getPrimaryFile(), ClassPath.COMPILE));
                }
                if (javaClass != null)
                    addProperties(prop = new ArrayList(), javaClass);
            }
            return prop;
        }
        
        private void addProperties (List<Property> prop, JavaClass javaClass){
            Method methods [] = JMIUtil.getMethods(javaClass);
            for (int j = 0; j < methods.length; j++) {
                if ((methods[j].getName().startsWith("get") || methods[j].getName().startsWith("is"))
                        && methods[j].getParameters().size() == 0
                        && ((methods[j].getModifiers() & Modifier.PUBLIC) != 0)) {
                    String name = methods[j].getName();
                    if (name.startsWith("get"))
                        name = name.substring(3);
                    else
                        name = name.substring(2);

                    name = name.substring(0,1).toLowerCase()+name.substring(1);
                    prop.add(new Property(name, methods[j].getType().getName()));
                }
            }
        }
    }
    
    private static Collection <ELImplicitObject> implicitELObjects = null;
    
    private static void initImplicitObjects() {
        if (implicitELObjects == null){
            implicitELObjects = new ArrayList();
            implicitELObjects.add(new PageContextObject("pageContext"));
            implicitELObjects.add(new ELImplicitObject("pageScope"));
            implicitELObjects.add(new ELImplicitObject("requestScope"));
            implicitELObjects.add(new ELImplicitObject("sessionScope"));
            implicitELObjects.add(new ELImplicitObject("applicationScope"));
            implicitELObjects.add(new ELImplicitObject("param"));
            implicitELObjects.add(new ELImplicitObject("paramValues"));
            implicitELObjects.add(new ELImplicitObject("header"));
            implicitELObjects.add(new ELImplicitObject("headerValues"));
            implicitELObjects.add(new ELImplicitObject("initParam"));
            implicitELObjects.add(new ELImplicitObject("cookie"));
        }
    }
    
    /** Returns implicit objects that starts with the prefix.
     */
    public static Collection <ELImplicitObject> getELImplicitObjects(String prefix){
        initImplicitObjects();
        Collection <ELImplicitObject> filtered = implicitELObjects;
        if (prefix != null && !prefix.equals("")){
            filtered = new ArrayList();
            for (ELImplicitObject elem : implicitELObjects) {
                if (elem.getName().startsWith(prefix))
                    filtered.add(elem);
            }
        }
        return filtered;
    }
    
    public static ELImplicitObject getELImplicitObject (String expr){
        initImplicitObjects();
        ELImplicitObject obj = null;
        if (expr != null && !expr.equals("")){
            int indexP = expr.indexOf('[');
            int indexD = expr.indexOf('.');
            String name = null;
            if (indexD > -1 && (indexP == -1 || indexD < indexP))
                name = expr.substring(0, indexD);
            else{
                if (indexP > -1)
                    name = expr.substring(0, indexP);
                else
                    name = expr;
            }
            name = name.trim();
            for (ELImplicitObject elem : implicitELObjects) {
                if (elem.getName().equals(name)){
                    obj = elem;
                    break;
                }
            }
        }
        return obj;
    }

    
}
