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

/*
 * DefaultBeanInfo.java
 *
 * Created on August 20, 2001, 3:42 PM
 */

package org.netbeans.modules.j2ee.sun.share.config.ui;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;


/**
 *
 * @author  gfink
 * @version
 */
public class DefaultBeanInfo extends SimpleBeanInfo {

    final Class cls;

    /** Creates new DefaultBeanInfo */
    public DefaultBeanInfo(Class cls) {
        this.cls = cls;
    }

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(cls);
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        Method[] methods = cls.getDeclaredMethods();
        Collection c = new Vector();
        for(int i = 0; i < methods.length; i++) {
            try {
            String method = methods[i].getName();
            if(method.startsWith("get") && methods[i].getParameterTypes().length == 0) { // NOI18N
                StringBuffer name = (new StringBuffer(method.substring(3)));
                name.setCharAt(0,Character.toLowerCase(method.charAt(3)));
                String propertyName = name.toString();
                PropertyDescriptor pd;
                if(methods[i].getReturnType().isArray())
                   pd = new IndexedPropertyDescriptor(propertyName,cls);
                else pd = new PropertyDescriptor(propertyName,cls);
//                System.err.println(pd.getName());
//                System.err.println(pd.getDisplayName());
//                System.err.println(pd.getPropertyType());
                c.add(pd);
            }
            } catch (Exception e) {
            }
        }
        PropertyDescriptor[] ret = new PropertyDescriptor[c.size()];
        c.toArray(ret);
        return ret;
    }
}
