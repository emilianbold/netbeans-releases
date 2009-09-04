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

import javax.swing.text.Document;

import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.el.JsfElExpression;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
interface ElContextChecker {

    void check( JsfElExpression expression , Document document, 
            FileObject fileObject, List<Hint> hints );
}

class ElStartContextChecker extends 
    org.netbeans.modules.web.core.syntax.checker.ElStartContextChecker 
    implements ElContextChecker
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, java.util.List)
     */
    public void check( JsfElExpression expression, Document document, 
            FileObject fileObject, List<Hint> hints ) 
    {
        if ( checkElStart( expression , document ) ){
            return;
        }
        if ( checkJsfElStart( expression , fileObject ) ){
            return;
        }
        Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_EL_START_CONTEXT", 
                        expression.getReplace()),
                fileObject,
                new OffsetRange(expression.getStartOffset(), expression.getContextOffset()),
                Collections.<HintFix>emptyList(), HintsProvider.DEFAULT_ERROR_HINT_PRIORITY);
        hints.add( hint );
    }

    private boolean checkJsfElStart( JsfElExpression expression , 
            FileObject fileObject ) 
    {
        String replace = expression.getReplace();

        // check managed beans
        List<FacesManagedBean> beans = JSFBeanCache.getBeans(
                WebModule.getWebModule( fileObject));
        for (FacesManagedBean bean : beans) {
            String beanName = bean.getManagedBeanName();
            if ( replace.equals(beanName)){
                // found managed bean via JSF model. All is OK.
                return true;
            }
        }
        // check bundles properties
        List<ResourceBundle> bundles = expression.getJSFResourceBundles(
                WebModule.getWebModule( fileObject));
        for (ResourceBundle bundle : bundles) {
            String var = bundle.getVar();
            if (replace.equals( var )){
                // found resource bundle . All is OK.
                return true;
            }
        }     
        return false;
    }

}

class ElBeanContextChecker implements ElContextChecker{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, java.util.List)
     */
    public void check( JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints ) 
    {
        // TODO Auto-generated method stub
        
    }
    
}

class JsfElBeanContextChecker implements ElContextChecker{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, java.util.List)
     */
    public void check( JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints ) 
    {
        // TODO Auto-generated method stub
        
    }
    
}

class JsfElResourceBundleContextChecker implements ElContextChecker {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, java.util.List)
     */
    public void check( JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints ) 
    {
        // TODO Auto-generated method stub
        
    }
    
}