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
import java.util.LinkedList;
//import org.netbeans.jmi.javamodel.Method;

import org.netbeans.modules.web.core.syntax.spi.ELImplicitObject;
import org.netbeans.modules.web.core.syntax.spi.ImplicitObjectProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author petr
 */

/** Represents Implicit objects for EL
 **/
@ServiceProvider(service=ImplicitObjectProvider.class)
public class ELImplicitObjects implements ImplicitObjectProvider {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.core.syntax.spi.ImplicitObjectProvider#getImplicitObjects()
     */
    public Collection<ELImplicitObject> getImplicitObjects() {
        return getELImplicitObjects();
    }
    
    static class PageContextObject extends ELImplicitObject{
        public PageContextObject(String name){
            super(name);
            setType(OBJECT_TYPE);
            setClazz("javax.servlet.jsp.PageContext"); //NOI18N
        }
    }
    
    private Collection<ELImplicitObject> getELImplicitObjects() {
        Collection<ELImplicitObject> implicitELObjects;
        implicitELObjects = new ArrayList<ELImplicitObject>(11);
        implicitELObjects.add(new PageContextObject("pageContext")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("pageScope")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("requestScope")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("sessionScope")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("applicationScope")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("param")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("paramValues")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("header")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("headerValues")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("initParam")); // NOI18N
        implicitELObjects.add(new ELImplicitObject("cookie")); // NOI18N
        return implicitELObjects;
    }
    
    /** Returns implicit objects that starts with the prefix.
     */
    public static Collection <ELImplicitObject> getELImplicitObjects(String prefix){
        initImplicitObjects();
        Collection <ELImplicitObject> filtered = IMPLICIT_OBJECTS;
        if (prefix != null && !prefix.equals("")){
            filtered = new ArrayList<ELImplicitObject>();
            for (ELImplicitObject elem : IMPLICIT_OBJECTS) {
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
            for (ELImplicitObject elem : IMPLICIT_OBJECTS) {
                if (elem.getName().equals(name)){
                    obj = elem;
                    break;
                }
            }
        }
        return obj;
    }
    
    private static void initImplicitObjects() {
        IMPLICIT_OBJECTS = new LinkedList<ELImplicitObject>();
        Collection<? extends ImplicitObjectProvider> providers = 
            Lookup.getDefault().lookupAll( ImplicitObjectProvider.class );
        for (ImplicitObjectProvider objectProvider : providers) {
            Collection<ELImplicitObject> implicitObjects = 
                objectProvider.getImplicitObjects();
            IMPLICIT_OBJECTS.addAll( implicitObjects );
        }
    }

    private static Collection<ELImplicitObject> IMPLICIT_OBJECTS;
    
    static {
        initImplicitObjects();
    }
}