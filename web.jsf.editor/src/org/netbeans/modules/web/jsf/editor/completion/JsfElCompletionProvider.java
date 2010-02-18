/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.web.jsf.editor.completion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.beans.api.model.support.WebBeansModelSupport;
import org.netbeans.modules.web.beans.api.model.support.WebBeansModelSupport.WebBean;
import org.netbeans.modules.web.jsf.editor.el.JsfElExpression;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 * @author Po-Ting Wu
 */
public class JsfElCompletionProvider implements CompletionProvider {

    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE & COMPLETION_ALL_QUERY_TYPE) != 0) {
            return new AsyncCompletionTask(new CCQuery(), 
                    component);
        }
        return null;
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    static final class CCQuery extends AsyncCompletionQuery {
        
        protected void query(CompletionResultSet resultSet, Document doc, final int offset) {
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = null;
            if (fObject != null)
                wm = WebModule.getWebModule(fObject);
            if (wm != null){
                final JsfElExpression elExpr = new JsfElExpression (wm, doc);
                final ArrayList<CompletionItem> complItems = new ArrayList<CompletionItem>();

                int elParseType = elExpr.parse(offset);
                final int anchor = offset - elExpr.getReplace().length();
                
                switch (elParseType){
                    case JsfElExpression.EL_START:
                        final String replace = elExpr.getReplace();

                        // check managed beans
                        List<FacesManagedBean> beans = JSFBeanCache.getBeans(wm);
                        for (FacesManagedBean bean : beans) {
                            String beanName = bean.getManagedBeanName();
                            if ((beanName != null) && beanName.startsWith(replace)){
                                complItems.add(new JsfElCompletionItem.JsfBean(
                                        beanName, anchor, bean.getManagedBeanClass()));
                            }
                        }

			//check web beans
			JsfSupport jsfSupport = JsfSupport.findFor(fObject);
			List<WebBean> namedElements = WebBeansModelSupport.getNamedBeans(jsfSupport.getWebBeansModel());
			for (WebBean bean : namedElements) {
			    String beanName = bean.getName();
			    String className = bean.getBeanClassName();
			    if ((beanName != null) && beanName.startsWith(replace)) {
				complItems.add(new JsfElCompletionItem.JsfBean(
					beanName, anchor, className));
			    }
			}


                        // check bundles properties
                        List <ResourceBundle> bundles = elExpr.
                            getJSFResourceBundles(wm);
                        for (ResourceBundle bundle : bundles) {
                            String var = bundle.getVar();
                            if ((var != null) && var.startsWith(replace)) {
                                complItems.add(new JsfElCompletionItem.
                                        JsfResourceBundle(var, anchor, 
                                                bundle.getBaseName()));
                            }
                        }

                        //add all declared local variables
                        complItems.addAll(elExpr.getAvailableDeclaredVariables(replace));

                        break;
                    case JsfElExpression.EL_JSF_BEAN:
                    case JsfElExpression.EL_JSF_BEAN_REFERENCE:
                        List<CompletionItem> items = elExpr.getPropertyCompletionItems(
                                elExpr.getObjectClass(), anchor);
                        complItems.addAll(items);
                        items = elExpr.getMethodCompletionItems(
                                elExpr.getObjectClass(), anchor);
                        complItems.addAll(items);
                        break;
                    case JsfElExpression.EL_JSF_RESOURCE_BUNDLE:
                        List<CompletionItem> bitems = elExpr.getPropertyKeysCompletionItems(
                                elExpr.getBundleName(), anchor, elExpr.getReplace());
                        complItems.addAll(bitems);
                        break;
                    case JsfElExpression.EL_COMPOSITE_COMPONENT:
                        complItems.addAll(elExpr.getCompositeComponentItems(anchor));
                        break;
                }
//                for (int i = 0; i < complItems.size(); i++)
//                    ((JspCompletionItem.JspResultItem)complItems.get(i)).setSubstituteOffset(offset - elExpr.getReplace().length());
                resultSet.addAllItems(complItems);
            }
            resultSet.finish();
        }
        
    }
   
    
    static class DocQuery extends AsyncCompletionQuery {
        protected void query(CompletionResultSet resultSet, Document doc, 
                int caretOffset) 
        {
        }
    }
}
