/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.webmvc.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.web.el.spi.ELVariableResolver.class)
public final class SpringBeansELVariableResolver implements ELVariableResolver {

    @Override
    public String getBeanClass(String beanName, FileObject context) {
        for (SpringBean bean : getSpringBeans(context)) {
            if (beanName.equals(getBeanName(bean))) {
                return bean.getClassName();
            }
        }
        return null;
    }

    @Override
    public String getBeanName(String clazz, FileObject context) {
        for (SpringBean bean : getSpringBeans(context)) {
            if (clazz.equals(bean.getClassName())) {
                return getBeanName(bean);
            }
        }
        return null;
    }

//    @Override
//    public String getReferredExpression(Snapshot snapshot, int offset) {
//        return null;
//    }
    
    @Override
    public List<VariableInfo> getManagedBeans(FileObject context) {
        List<SpringBean> beans = getSpringBeans(context);
        List<VariableInfo> result = new ArrayList<VariableInfo>(beans.size());
        for (SpringBean bean : beans) {
            result.add(VariableInfo.createResolvedVariable(getBeanName(bean), bean.getClassName()));
        }
        return result;
    }

    @Override
    public List<VariableInfo> getVariables(Snapshot snapshot, int offset) {
        return Collections.emptyList();
    }

    @Override
    public List<VariableInfo> getBeansInScope(String scope, Snapshot context) {
        return Collections.emptyList();
    }

    @Override
    public List<VariableInfo> getRawObjectProperties(String name, Snapshot context) {
        return Collections.emptyList();
    }

    private List<SpringBean> getSpringBeans(FileObject context) {
        SpringScope scope = SpringScope.getSpringScope(context);
        final List<SpringBean> allSpringBeans = new ArrayList<SpringBean>();
        if (scope != null) {
            for (SpringConfigModel model : scope.getAllConfigModels()) {
                try {
                    model.runReadAction(new Action<SpringBeans>() {

                        @Override
                        public void run(SpringBeans beans) {
                            allSpringBeans.addAll(beans.getBeans());
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return allSpringBeans;
    }

    private static String getBeanName(SpringBean bean) {
        String beanName = null;
        for (String name : bean.getNames()) {
            beanName = name;
            break;
        }
        if (beanName == null) {
            beanName = bean.getId();
        }

        return beanName;
    }

}
