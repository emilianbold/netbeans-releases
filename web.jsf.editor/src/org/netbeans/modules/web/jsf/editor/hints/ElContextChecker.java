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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.completion.api.JspCompletionItem;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression.InspectPropertiesTask;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.el.JsfElExpression;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
interface JsfElContextChecker {

    boolean check( JsfElExpression expression , Document document, 
            FileObject fileObject, List<Hint> hints );
}

class JsfElStartContextChecker implements JsfElContextChecker{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.JsfElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, javax.swing.text.Document, org.openide.filesystems.FileObject, java.util.List)
     */
    public boolean check( JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints )
    {
        // check managed beans
        List<FacesManagedBean> beans = JSFBeanCache.getBeans(WebModule
                .getWebModule(fileObject));
        for (FacesManagedBean bean : beans) {
            String beanName = bean.getManagedBeanName();
            if (expression.getExpression().equals(beanName)) {
                // found managed bean via JSF model. All is OK.
                return true;
            }
        }
        // check bundles properties
        List<ResourceBundle> bundles = expression
                .getJSFResourceBundles(WebModule.getWebModule(fileObject));
        for (ResourceBundle bundle : bundles) {
            String var = bundle.getVar();
            if (expression.getExpression().equals(var)) {
                // found resource bundle . All is OK.
                return true;
            }
        }
        return false;
    }

}

class JsfElBeanContextChecker implements JsfElContextChecker{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.core.syntax.completion.JspELExpression, javax.swing.text.Document, org.openide.filesystems.FileObject, java.util.List)
     */
    public boolean check( final JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints )
    {
        InspectPropertiesTask inspectPropertiesTask = expression.new InspectPropertiesTask(){

            public void run( CompilationController controller ) throws Exception {
                TypeElement lastType = getTypePreceedingCaret(controller);
                if (lastType != null){
                    String property = expression.getPropertyBeingTypedName();
                    // Fix for IZ#171723 - StringIndexOutOfBoundsException: String index out of range: -1
                    if ( property.length() >0 && property.charAt(property.length()-1) == ']'){
                        property = property.substring( 0, property.length()-1);
                    }
                    String suffix = removeQuotes(property);

                    for (ExecutableElement method : ElementFilter.methodsIn(lastType.
                            getEnclosedElements()))
                    {
                        String propertyName = getExpressionSuffix(method, controller);

                        if (propertyName != null && propertyName.equals(suffix)){
                            return;
                        }
                    }
                    setOffset( expression.getExpression().lastIndexOf( suffix));
                    setProperty( suffix );
                    setLast();
                }
            }
            
            @Override
            protected boolean checkMethodParameters( ExecutableElement method ,
                    CompilationController controller )
            {
                return true;
            }
            
            @Override
            protected boolean checkMethod( ExecutableElement method, 
                    CompilationController controller)
            {
                return expression.checkMethod(method, controller);
            }
            
        };
        inspectPropertiesTask.execute();
        int offset = inspectPropertiesTask.getOffset();
        String property = inspectPropertiesTask.getProperty();
        if ( offset == 0 && property != null ) {
            Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                    NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_BEAN_CONTEXT", //NOI18N
                            property),
                    fileObject,
                    new OffsetRange(expression.getStartOffset()+offset, 
                            expression.getStartOffset()+offset +property.length()),
                    Collections.<HintFix>emptyList(), 
                    HintsProvider.DEFAULT_ERROR_HINT_PRIORITY);
            hints.add( hint );
        }
        else if ( offset > 0 && property != null ){
            String msg;
            if ( inspectPropertiesTask.lastProperty()){
                msg = "MSG_UNKNOWN_PROPERTY_METHOD_CONTEXT"; //NOI18N
            }
            else {
                msg = "MSG_UNKNOWN_PROPERTY_CONTEXT"; //NOI18N
            }
            Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                    NbBundle.getMessage(HintsProvider.class, msg, 
                            property),
                    fileObject,
                    new OffsetRange(expression.getStartOffset()+offset, 
                            expression.getStartOffset()+offset +property.length()),
                    Collections.<HintFix>emptyList(), 
                    HintsProvider.DEFAULT_ERROR_HINT_PRIORITY); 
            hints.add(hint);
        }
        return true;
    }

}

class JsfElResourceBundleContextChecker implements JsfElContextChecker {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, java.util.List)
     */
    public boolean check( JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints ) 
    {
        List<CompletionItem> propertyKeys = expression.getPropertyKeys(
                expression.getBundleName(), 0, 
                expression.getReplace());
        for (CompletionItem completionItem : propertyKeys) {
            String key = ((JspCompletionItem)completionItem).getItemText();
            if ( key!= null && key.equals(expression.getReplace())){
                return true;
            }
        }
        int offset = expression.getExpression().lastIndexOf( 
                expression.getReplace());
        Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                NbBundle.getMessage(HintsProvider.class, 
                        "MSG_UNKNOWN_RESOURCE_BUNDLE_CONTEXT", 
                        expression.getReplace()),fileObject,
                new OffsetRange(expression.getStartOffset()+offset, 
                        expression.getStartOffset()+offset +
                        expression.getReplace().length()),
                Collections.<HintFix>emptyList(), 
                HintsProvider.DEFAULT_ERROR_HINT_PRIORITY); 
        hints.add(hint);
        return true;
    }
    
}