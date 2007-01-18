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
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
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
        private String clazz; 
                
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
        
        public String getClazz(){
            return clazz;
        }
        
        public void setClazz(String clazz){
            this.clazz = clazz;
        }
    }
    
    public static class PageContextObject extends ELImplicitObject{
        public PageContextObject(String name){
            super(name);
            setType(ELImplicitObjects.OBJECT_TYPE);
            setClazz("javax.servlet.jsp.PageContext"); //NOI18N
        }
    }
    
    private static Collection <ELImplicitObject> implicitELObjects = null;
    
    private static void initImplicitObjects() {
        if (implicitELObjects == null){
            implicitELObjects = new ArrayList();
            implicitELObjects.add(new PageContextObject("pageContext")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("pageScope")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("requestScope")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("sessionScope")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("applicationScope")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("param")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("paramValues")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("header")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("headerValues")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("initParam")); //NOI18N
            implicitELObjects.add(new ELImplicitObject("cookie")); //NOI18N
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
