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
package org.netbeans.modules.visualweb.web.ui.dt.component.vforms;

import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.web.ui.component.Form;
import java.util.List;
import java.util.ArrayList;

public class VirtualFormsHelper {

    public static DisplayAction getContextItem(DesignContext context) {
        DesignBean formBean = null;
        DesignBean rootBean = context.getRootContainer();
        if (rootBean.getInstance() instanceof Form) {   //just in case
            formBean = rootBean;
        }
        else {
            formBean = findFormBeanFromRoot(rootBean);
        }
        if (formBean != null) {
            return new VirtualFormsCustomizerAction(formBean);
        }
        return null;
    }

    public static DisplayAction getContextItem(DesignBean bean) {
        if (findFormBean(bean) == null) {
            return null;
        }
        if (bean.getInstance() instanceof EditableValueHolder ||
            bean.getInstance() instanceof ActionSource) {
            return new EditVirtualFormsCustomizerAction(bean);
        }
        return null;
    }

    public static DisplayAction getContextItem(DesignBean[] beans) {
        if (findFormBean(beans) == null) {
            return null;
        }
        for (int i = 0; beans != null && i < beans.length; i++) {
            if (beans[i].getInstance() instanceof EditableValueHolder ||
                beans[i].getInstance() instanceof ActionSource) {
                return new EditVirtualFormsCustomizerAction(beans);
            }
        }
        return null;
    }

    public static DesignBean findFormBean(DesignBean[] beans) {
        if (beans == null) {
            return null;
        }
        for (int i = 0; i < beans.length; i++) {
            DesignBean formBean = findFormBean(beans[i]);
            if (formBean != null) {
                return formBean;
            }
        }
        return null;
    }

    public static DesignBean findFormBean(DesignBean bean) {
        if (bean == null) {
            return null;
        }
        if (bean.getInstance() instanceof Form) {
            return bean;
        }
        return findFormBean(bean.getBeanParent());
    }

    private static DesignBean findFormBeanFromRoot(DesignBean parent) {
        if (parent == null) {
            return null;
        }
        DesignBean[] childBeans = parent.getChildBeans();
        for (int i = 0; childBeans != null && i < childBeans.length; i++) {
            DesignBean bean = childBeans[i];
            if (bean.getInstance() instanceof Form) {
                return bean;
            }
            DesignBean formBean = findFormBeanFromRoot(bean);
            if (formBean != null) {
                return formBean;
            }
        }
        return null;
    }

    public static String getNewVirtualFormName(List vformsList) {
        List nameList = new ArrayList();
        for (int i = 0; vformsList != null && i < vformsList.size(); i++) {
            nameList.add(((Form.VirtualFormDescriptor)vformsList.get(i)).getName());
        }
        
        String name = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("newVirtualForm"); // NOI18N
        
        for (int i = 1; i < 999; i++) {
            if (!nameList.contains(name + i)) {
                name = name + i;
                break;
            }
        }
        
        return name;
    }
}
