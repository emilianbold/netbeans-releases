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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.index.JsfPageModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * TODO: getRawObjectProperties() handling is a bit hacky - currently just the actual node
 * image is passed to the method which may represent either the raw object itself or
 * one of its properties. Ideally the actual Node should be passed - API change.
 *
 */
@ServiceProvider(service = org.netbeans.modules.web.el.spi.ELVariableResolver.class)
public final class JsfELVariableResolver implements ELVariableResolver {

    private static final String CONTENT_NAME = "JsfBeans"; //NOI18N

    private static final String OBJECT_NAME__CC = "cc"; //NOI18N
    private static final String ATTR_NAME__ATTRS = "attrs"; //NOI18N
    private static final String ATTR_NAME__ID = "id"; //NOI18N
    private static final String ATTR_NAME__RENDERED = "rendered"; //NOI18N
    
    private static final VariableInfo VARIABLE_INFO__ATTRS = VariableInfo.createResolvedVariable(ATTR_NAME__ATTRS, Object.class.getName());
    private static final VariableInfo VARIABLE_INFO__ID = VariableInfo.createResolvedVariable(ATTR_NAME__ID, Object.class.getName());
    private static final VariableInfo VARIABLE_INFO__RENDERED = VariableInfo.createResolvedVariable(ATTR_NAME__RENDERED, Object.class.getName());
    
    @Override
    public String getBeanClass(String beanName, FileObject target, ResolverContext context) {
        for (FacesManagedBean bean : getJsfManagedBeans(target, context)) {
            if (beanName.equals(bean.getManagedBeanName())) {
                return bean.getManagedBeanClass();
            }
        }
        return null;
    }

    @Override
    public String getBeanName(String clazz, FileObject target, ResolverContext context) {
        for (FacesManagedBean bean : getJsfManagedBeans(target, context)) {
            if (clazz.equals(bean.getManagedBeanClass())) {
                return bean.getManagedBeanName();
            }
        }
        return null;
    }

//    @Override
//    public String getReferredExpression(Snapshot snapshot, final int offset) {
//        List<JsfVariableContext> allJsfVariables = getAllJsfVariables(snapshot, offset);
//        return allJsfVariables.isEmpty() ? null : allJsfVariables.get(0).getResolvedExpression();
//    }
    
    @Override
    public List<VariableInfo> getManagedBeans(FileObject target, ResolverContext context) {
        List<FacesManagedBean> beans = getJsfManagedBeans(target, context);
        List<VariableInfo> result = new ArrayList<VariableInfo>(beans.size());
        for (FacesManagedBean bean : beans) {
            if(bean.getManagedBeanClass() != null && bean.getManagedBeanName() != null) {
                result.add(VariableInfo.createResolvedVariable(bean.getManagedBeanName(), bean.getManagedBeanClass()));
            }
        }
        return result;
    }

    @Override
    public List<VariableInfo> getVariables(Snapshot snapshot, final int offset, ResolverContext context) {
        List<JsfVariableContext> allJsfVariables = getAllJsfVariables(snapshot, offset);
        List<VariableInfo> result = new ArrayList<VariableInfo>(allJsfVariables.size());
        for (JsfVariableContext jsfVariable : allJsfVariables) {
            //gets the generated expression from the el variables chain, see the JsfVariablesModel for more info
            String expression = jsfVariable.getResolvedExpression();
            if (expression == null) {
                continue;
            }
            result.add(VariableInfo.createUnresolvedVariable(jsfVariable.getVariableName(), expression));
        }
        return result;
    }

    @Override
    public List<VariableInfo> getRawObjectProperties(String objectName, Snapshot snapshot, ResolverContext context) {
        List<VariableInfo> variables = new ArrayList<VariableInfo> (3);
        
        //composite component object's properties handling
        if(OBJECT_NAME__CC.equals(objectName)) { //NOI18N
            variables.add(VARIABLE_INFO__ID);
            variables.add(VARIABLE_INFO__RENDERED);
            variables.add(VARIABLE_INFO__ATTRS);
        } else if (ATTR_NAME__ATTRS.equals(objectName)) { //NOI18N
            variables.add(VARIABLE_INFO__ID);
            variables.add(VARIABLE_INFO__RENDERED);
            final JsfPageModelFactory modelFactory = JsfPageModelFactory.getFactory(CompositeComponentModel.Factory.class);
            assert modelFactory != null;
            final AtomicReference<CompositeComponentModel> ccModelRef = new AtomicReference<CompositeComponentModel>();
            try {
                ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        //one level - works only if xhtml is top level
                        Result parseResult = JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
                        if (parseResult instanceof HtmlParserResult) {
                            ccModelRef.set((CompositeComponentModel) modelFactory.getModel((HtmlParserResult) parseResult));
                        }
                    }
                });

                CompositeComponentModel ccmodel = ccModelRef.get();
                if(ccmodel != null) {
                    //the page represents a composite component
                    Collection<Map<String, String>> allCCInterfaceAttrs = ccmodel.getExistingInterfaceAttributes();
                    for (Map<String, String> attrsMap : allCCInterfaceAttrs) {
                        String name = attrsMap.get("name"); //NOI18N
                        if (name == null) {
                            continue;
                        }
                        String clazz = attrsMap.get("class") == null ? Object.class.getName() : attrsMap.get("class"); //NOI18N
                        variables.add(VariableInfo.createResolvedVariable(name, clazz));
                    }
                }
            } catch (ParseException e) {
                Exceptions.printStackTrace(e);
            }
        }

        return variables;
    }



    @Override
    public List<VariableInfo> getBeansInScope(String scope, Snapshot snapshot, ResolverContext context) {
        List<VariableInfo> result = new ArrayList<VariableInfo>();
        for (FacesManagedBean bean : getJsfManagedBeans(snapshot.getSource().getFileObject(), context)) {
            if(bean.getManagedBeanClass() != null && bean.getManagedBeanName() == null) {
                if (scope.equals(bean.getManagedBeanScopeString())) {
                    result.add(VariableInfo.createResolvedVariable(bean.getManagedBeanName(), bean.getManagedBeanClass()));
                }
            }
        }
        return result;
    }

    private List<FacesManagedBean> getJsfManagedBeans(FileObject target, ResolverContext context) {
        Project project = FileOwnerQuery.getOwner(target);
        if (project == null) {
            return Collections.<FacesManagedBean>emptyList();
        } else {
            if (context.getContent(CONTENT_NAME) == null) {
                context.setContent(CONTENT_NAME, JSFBeanCache.getBeans(project));
            }
            return (List<FacesManagedBean>) context.getContent(CONTENT_NAME);
        }
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
