/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.propertyeditors.binding;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import com.sun.rave.designtime.CustomizerResult;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import org.openide.ErrorManager;

public class PropertyBindingHelper {

    public static final Bundle bundle = Bundle.getBundle(PropertyBindingHelper.class);

    public static DisplayAction getContextItem(DesignBean bean) {
        return new PropertyBindingsAction(bean);
    }

    public static DisplayAction getContextItem(DesignProperty prop) {
        return new PropertyBindingAction(prop);
    }

    /**
     * The PropertyBindingAction is the DisplayAction (context item) that appears on the right-click
     * menu of the property sheet for a particular property.  This displays the BindingsCustomizer
     * with only the right-hand side (BindingTargetPanel) visible, with a single property to be bound.
     */
    private static class PropertyBindingAction extends BasicDisplayAction {

        public PropertyBindingAction(DesignProperty prop) {
            super(bundle.getMessage("propBindingEllipse")); //NOI18N
            this.prop = prop;
        }

        protected DesignProperty prop = null;

        public Result invoke() {
            PropertyBindingsCustomizer bc = new PropertyBindingsCustomizer(prop);
            bc.panel.setShowSourcePanel(false);
            return new CustomizerResult(prop.getDesignBean(), bc);
        }
    }

    /**
     * The PropertyBindingsAction is the DisplayAction (context item) that appears on the right-click
     * menu for every component.  This displays the BindingsCustomizer dialog with both the left
     * (BindingSourcePanel) and right-hand sides (BindingTargetPanel) visible, so the user can choose
     * any properties to be bound.
     */
    private static class PropertyBindingsAction extends BasicDisplayAction {

        public PropertyBindingsAction(DesignBean bean) {
            super(bundle.getMessage("propBindingsEllipse")); //NOI18N
            this.bean = bean;
        }

        protected DesignBean bean;
        public Result invoke() {
            PropertyBindingsCustomizer bc = new PropertyBindingsCustomizer(bean);
            return new CustomizerResult(bean, bc);
        }
    }

    //------------------------------------------------------------------------------- Helper methods

    public static Object getPropInstance(DesignBean bean, PropertyDescriptor[] propPath) {
        if (propPath != null && propPath.length > 0) {
            try {
                ArrayList propList = new ArrayList();
                for (int i = 0; i < propPath.length; i++) {
                    propList.add(propPath[i]);
                }
                Object o = bean.getInstance();
                outerloop:
                while (o != null && propList.size() > 0) {
                    PropertyDescriptor pdnext = (PropertyDescriptor)propList.get(0);
                    BeanInfo bi = Introspector.getBeanInfo(o.getClass());
                    PropertyDescriptor[] pdanext = bi.getPropertyDescriptors();
                    for (int i = 0; i < pdanext.length; i++) {
                        if (pdanext[i].getName().equals(pdnext.getName())) {
                            //System.out.println("found: " + pdnext.getName() + " : " + propList.size() + " left");
                            Method read = pdanext[i].getReadMethod();
                            if ((read != null) && !read.getName().equals("getFieldKeys")) {
                                try {
                                    o = read.invoke(o, new Object[] {});
                                    if (o instanceof ValueBinding) {
                                        o = ((ValueBinding)o).getValue(FacesContext.
                                            getCurrentInstance());
                                    }
                                }
                                catch (Exception exc) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                                    return null;
                                }
                                if (o != null) {
                                    propList.remove(0);
                                    continue outerloop;
                                }
                            }
                            else {
                                return null;
                            }
                        }
                    }
                }
                return o;
            }
            catch (Exception exc) {
                ErrorManager.getDefault().notify(exc);
            }
        }
        else {
            return bean.getInstance();
        }
        return null;
    }

    static HashMap arrayTypeKeyHash = new HashMap();
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

    public static String getPrettyTypeName(String tn) {
        tn = getJavaTypeName(tn);
        if (tn.indexOf(".") > -1) {   //NOI18N
            tn = tn.substring(tn.lastIndexOf(".") + 1);
        }
        return tn;
    }

    public static String getJavaTypeName(String tn) {
        if (tn.startsWith("[")) {   //NOI18N
            int depth = 0;
            while (tn.startsWith("[")) {   //NOI18N
                tn = tn.substring(1);
                depth++;
            }
            if (tn.startsWith("L")) {   //NOI18N
                tn = tn.substring(1);
                tn = tn.substring(0, tn.length() - 1);
            }
            else {
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
