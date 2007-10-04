/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.core.syntax.completion;

import java.util.ArrayList;
import java.util.Collection;
//import org.netbeans.jmi.javamodel.Method;

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
