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

package org.netbeans.modules.web.jsf.editor.el;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=org.netbeans.modules.web.el.spi.ELVariableResolver.class)
public final class JsfELVariableResolver implements ELVariableResolver {

    @Override
    public String getBeanClass(String beanName, FileObject context) {
        for (FacesManagedBean bean : getJsfManagedBeans(context)) {
            if (bean.getManagedBeanName().equals(beanName)) {
                return bean.getManagedBeanClass();
            }
        }
        return null;
    }

    @Override
    public String getBeanName(String clazz, FileObject context) {
        for (FacesManagedBean bean : getJsfManagedBeans(context)) {
            if (bean.getManagedBeanClass().equals(clazz)) {
                return bean.getManagedBeanName();
            }
        }
        return null;
    }

    @Override
    public String getReferredExpression(Snapshot snapshot, final int offset) {
        List<JsfVariableContext> allJsfVariables = getAllJsfVariables(snapshot, offset);
        return allJsfVariables.isEmpty() ? null : allJsfVariables.get(0).getVariableValue();
    }

    @Override
    public List<VariableInfo> getManagedBeans(FileObject context) {
        List<FacesManagedBean> beans = getJsfManagedBeans(context);
        List<VariableInfo> result = new ArrayList<VariableInfo>(beans.size());
        for (FacesManagedBean bean : beans) {
            result.add(new VariableInfo(bean.getManagedBeanName(), bean.getManagedBeanClass()));
        }
        return result;
    }

    @Override
    public List<VariableInfo> getVariables(Snapshot snapshot, final int offset) {
        List<JsfVariableContext> allJsfVariables = getAllJsfVariables(snapshot, offset);
        List<VariableInfo> result = new ArrayList<VariableInfo>(allJsfVariables.size());
        for (JsfVariableContext jsfVariable : allJsfVariables) {
            result.add(new VariableInfo(jsfVariable.getVariableName(), jsfVariable.getVariableValue()));
        }
        return result;
    }

    @Override
    public List<VariableInfo> getBeansInScope(String scope, FileObject context) {
       List<VariableInfo> result = new ArrayList<VariableInfo>();
       for (FacesManagedBean bean : getJsfManagedBeans(context)) {
           if (scope.equals(bean.getManagedBeanScopeString())) {
                result.add(new VariableInfo(bean.getManagedBeanName(), bean.getManagedBeanClass()));
           }
       }
       return result;
    }

    private List<FacesManagedBean> getJsfManagedBeans(FileObject context) {
        WebModule webModule = WebModule.getWebModule(context);
        return webModule != null 
                ? JSFBeanCache.getBeans(webModule)
                : Collections.<FacesManagedBean>emptyList();
    }

    private List<JsfVariableContext> getAllJsfVariables(Snapshot snapshot, final int offset) {
        final List<JsfVariableContext> result = new ArrayList<JsfVariableContext>();
        try {
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    //one level - works only if xhtml is top level
                    Result parseResult = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                    if (parseResult instanceof HtmlParserResult) {
                        JsfVariablesModel model = JsfVariablesModel.getModel((HtmlParserResult) parseResult, resultIterator.getSnapshot());
                        List<JsfVariableContext> contexts = model.getAllAvailableVariables(offset, false);
                        result.addAll(contexts);
                    }
                }
            });
        } catch (ParseException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }


}
