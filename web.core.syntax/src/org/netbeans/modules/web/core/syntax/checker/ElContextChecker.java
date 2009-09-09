/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. Copyright 1997-2007
 * Sun Microsystems, Inc. All rights reserved. The contents of this file are
 * subject to the terms of either the GNU General Public License Version 2 only
 * ("GPL") or the Common Development and Distribution License("CDDL")
 * (collectively, the "License"). You may not use this file except in compliance
 * with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP.
 * See the License for the specific language governing permissions and
 * limitations under the License. When distributing the software, include this
 * License Header Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this particular file as
 * subject to the "Classpath" exception as provided by Sun in the GPL Version 2
 * section of the License file that accompanied this code. If applicable, add
 * the following below the License Header, with the fields enclosed by brackets
 * [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]" Contributor(s): The
 * Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc.
 * All Rights Reserved. If you wish your version of this file to be governed by
 * only the CDDL or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution under the
 * [CDDL or GPL Version 2] license." If you do not indicate a single choice of
 * license, a recipient has the option to distribute your version of this file
 * under either the CDDL, the GPL Version 2 or to extend the choice of license
 * to its licensees as provided above. However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.core.syntax.checker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.JspUtils;
import org.netbeans.modules.web.core.syntax.completion.ELFunctions;
import org.netbeans.modules.web.core.syntax.completion.ELImplicitObjects;
import org.netbeans.modules.web.core.syntax.completion.JspELExpression;
import org.netbeans.modules.web.core.syntax.completion.ELFunctions.Function;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression.InspectPropertiesTask;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author ads
 */
interface ElContextChecker {

    boolean check( JspELExpression expression, Document document,
            FileObject fileObject, List<Hint> hints );
}

class ElStartContextChecker implements ElContextChecker {

    /*
     * (non-Javadoc)
     * @see
     * org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.
     * netbeans.modules.web.core.syntax.completion.JspELExpression,
     * javax.swing.text.Document, org.openide.filesystems.FileObject,
     * java.util.List)
     */
    public boolean check( JspELExpression expression, Document document,
            FileObject fileObject, List<Hint> hints )
    {
        if (ELImplicitObjects.getELImplicitObject(expression.getExpression()) != null)
        {
            // context expression is implicit object. All is OK.
            return true;
        }

        if (JspUtils.isJspDocument(document)) {
            JspSyntaxSupport support = JspSyntaxSupport.get(document);
            // defined beans on the page
            BeanData[] beans = support.getBeanData();
            if (beans != null) {
                for (int i = 0; i < beans.length; i++) {
                    if (beans[i].getId().equals(expression.getExpression())) {
                        // context expression is existed bean. All is OK.
                        return true;
                    }
                }
            }
            // Functions
            List<Function> functions = ELFunctions.getFunctions(support,
                    expression.getExpression());
            Iterator<Function> iter = functions.iterator();
            while (iter.hasNext()) {
                Function func = iter.next();
                StringBuilder builder = new StringBuilder(func.getPrefix());
                builder.append(':');
                builder.append(func.getName());
                if (builder.toString().equals(expression.getExpression())) {
                    // context is function. All is OK.
                    return true;
                }
            }
        }
        Hint hint = new Hint(JspElChecker.DEFAULT_ERROR_RULE,
                NbBundle.getMessage(JspElChecker.class,
                        "MSG_UNKNOWN_EL_START_CONTEXT"), fileObject,    // NOI18N
                new OffsetRange(expression.getStartOffset(), expression
                        .getContextOffset()),
                Collections.<HintFix> emptyList(),
                JspElChecker.DEFAULT_ERROR_HINT_PRIORITY);
        hints.add(hint);
        return false;
    }

}

class ElBeanContextChecker implements ElContextChecker{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.core.syntax.completion.JspELExpression, javax.swing.text.Document, org.openide.filesystems.FileObject, java.util.List)
     */
    public boolean check( final JspELExpression expression, Document document,
            FileObject fileObject, List<Hint> hints )
    {
        InspectPropertiesTask inspectPropertiesTask = expression.new InspectPropertiesTask(){

            public void run( CompilationController controller ) throws Exception {
                TypeElement lastType = getTypePreceedingCaret(controller);       
                if (lastType != null) {
                    String property = expression.getPropertyBeingTypedName();
                    // Fix for IZ#171723 - StringIndexOutOfBoundsException: String index out of range: -1
                    if ( property.length() > 0 && property.charAt(property.length()-1) == ']'){
                        property = property.substring( 0, property.length()-1);
                    }
                    String suffix = removeQuotes(property);

                    for (ExecutableElement method : ElementFilter.methodsIn(
                            lastType.getEnclosedElements())) 
                    {
                        String propertyName = getExpressionSuffix(method, controller );

                        if (propertyName != null && propertyName.equals(suffix)) {
                            return;
                        }
                    }
                    setOffset( expression.getExpression().lastIndexOf( suffix));
                    setProperty( suffix );
                    setLast();
                }
            }
            
        };
        inspectPropertiesTask.execute();
        int offset = inspectPropertiesTask.getOffset();
        String property = inspectPropertiesTask.getProperty();
        if ( offset == 0 && property != null ) {
            Hint hint = new Hint(JspElChecker.DEFAULT_ERROR_RULE,
                    NbBundle.getMessage(JspElChecker.class, "MSG_UNKNOWN_BEAN_CONTEXT", //NOI18N
                            property),
                    fileObject,
                    new OffsetRange(expression.getStartOffset()+offset, 
                            expression.getStartOffset()+offset +property.length()),
                    Collections.<HintFix>emptyList(), 
                    JspElChecker.DEFAULT_ERROR_HINT_PRIORITY);
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
            Hint hint = new Hint(JspElChecker.DEFAULT_ERROR_RULE,
                    NbBundle.getMessage(JspElChecker.class, msg, property),
                    fileObject,
                    new OffsetRange(expression.getStartOffset()+offset, 
                            expression.getStartOffset()+offset +property.length()),
                    Collections.<HintFix>emptyList(), 
                    JspElChecker.DEFAULT_ERROR_HINT_PRIORITY); 
            hints.add(hint);
        }
        return true;
    }

}