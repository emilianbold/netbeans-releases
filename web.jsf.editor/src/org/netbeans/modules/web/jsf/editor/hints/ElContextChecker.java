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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression.InspectPropertiesTask;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.el.JsfElExpression;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @todo fix Issue #176270 for this currently unused class
 *
 * @author ads
 *
 */
public abstract class ElContextChecker {

    public boolean check(JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints) {
        if (isErrorCheckingEnabled(fileObject)) {
            return checkImpl(expression, document, fileObject, hints);
        } else {
            //add the reenable el checks hint
             //add a special hint for reenabling disabled error checks
            HintFix fix = new EnableErrorChecksFix(fileObject, document);
            Hint h = new EnableELChecksHint(HintsProvider.DEFAULT_WARNING_RULE,
                    NbBundle.getMessage(ElContextChecker.class, "MSG_HINT_ENABLE_ERROR_CHECKS_FILE_DESCR"), //NOI18N
                    fileObject,
                    new OffsetRange(0, 0),
                    Collections.singletonList(fix),
                    50);

            if(!hints.contains(h)) {
                //add just one instance - this check method is called for each expression in the document
                hints.add(h);
            }
            return true;
        }
    }

    public abstract boolean checkImpl(JsfElExpression expression, Document document,
            FileObject fileObject, List<Hint> hints);

    public static class JsfElUnknownContextChecker extends ElContextChecker {

        public boolean checkImpl(JsfElExpression expression, Document document, FileObject fileObject, List<Hint> hints) {

            String beanName = expression.getBeanName();
            Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                    NbBundle.getMessage(ElContextChecker.class, "MSG_UNKNOWN_BEAN_CONTEXT", // NOI18N
                    beanName),
                    fileObject,
                    new OffsetRange(expression.getStartOffset(),
                    expression.getStartOffset() + beanName.length()),
                    Collections.<HintFix>singletonList(new DisableErrorChecksFix(fileObject, document)),
                    HintsProvider.DEFAULT_ERROR_HINT_PRIORITY);
            hints.add(hint);
            return false;
        }
        
    }


    public static class JsfElStartContextChecker extends ElContextChecker {

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, javax.swing.text.Document, org.openide.filesystems.FileObject, java.util.List)
         */
        public boolean checkImpl(JsfElExpression expression, Document document,
                FileObject fileObject, List<Hint> hints) {
            // check managed beans
            List<FacesManagedBean> beans = JSFBeanCache.getBeans(WebModule.getWebModule(fileObject));
            for (FacesManagedBean bean : beans) {
                String beanName = bean.getManagedBeanName();
                if (expression.getExpression().equals(beanName)) {
                    // found managed bean via JSF model. All is OK.
                    return true;
                }
            }
            // check bundles properties
            List<ResourceBundle> bundles = expression.getJSFResourceBundles(WebModule.getWebModule(fileObject));
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

    public static class JsfElBeanContextChecker extends ElContextChecker {

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.core.syntax.completion.JspELExpression, javax.swing.text.Document, org.openide.filesystems.FileObject, java.util.List)
         */
        public boolean checkImpl(final JsfElExpression expression, Document document,
                FileObject fileObject, List<Hint> hints) {
            //create an InspectPropertiesTask for a base bean type of the expression
            InspectPropertiesTask inspectPropertiesTask = expression.new InspectPropertiesTask(expression.getBaseObjectClass()) {

                public void run(CompilationController controller) throws Exception {
                    //calling this will cause the associated error handler will be receiving
                    //potential expression errors
                    getTypePreceedingCaret(controller, true, true);
                }

                @Override
                protected boolean checkMethodParameters(ExecutableElement method,
                        CompilationController controller) {
                    return true;
                }

                @Override
                protected boolean checkMethod(ExecutableElement method,
                        CompilationController controller) {
                    return expression.checkMethod(method, controller);
                }
            };
            inspectPropertiesTask.execute();
            int offset = inspectPropertiesTask.getOffset();
            String property = inspectPropertiesTask.getProperty();

            //UGLY: should be an ability of EL Parser
            //compute offset delta for source expression and resolve expression
            //since the error offsets comes from resolved expression and need
            //to be mapped back to the document offsets
            String se = expression.getExpression();
            String re = expression.getResolvedExpression();
            int lenDiff = re.length() - se.length();
            offset -= lenDiff; //adjust the offset

            if (offset == 0 && property != null) {
                Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                        NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_BEAN_CONTEXT", //NOI18N
                        property),
                        fileObject,
                        new OffsetRange(expression.getStartOffset() + offset,
                        expression.getStartOffset() + offset + property.length()),
                        Collections.<HintFix>singletonList(new DisableErrorChecksFix(fileObject, document)),
                        HintsProvider.DEFAULT_ERROR_HINT_PRIORITY);
                hints.add(hint);
            } else if (offset > 0 && property != null) {
                String msg;
                if (inspectPropertiesTask.lastProperty()) {
                    msg = "MSG_UNKNOWN_PROPERTY_METHOD_CONTEXT"; //NOI18N
                } else {
                    msg = "MSG_UNKNOWN_PROPERTY_CONTEXT"; //NOI18N
                }
                Hint hint = new ElExpressionPropertyHint(HintsProvider.DEFAULT_ERROR_RULE,
                        NbBundle.getMessage(HintsProvider.class, msg,
                        property),
                        fileObject,
                        new OffsetRange(expression.getStartOffset() + offset,
                        expression.getStartOffset() + offset + property.length()),
                        Collections.<HintFix>singletonList(new DisableErrorChecksFix(fileObject, document)),
                        HintsProvider.DEFAULT_ERROR_HINT_PRIORITY,
                        property);
                hints.add(hint);
            }
            return true;
        }
    }

    public static class JsfElResourceBundleContextChecker extends ElContextChecker {

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.jsf.editor.hints.ElContextChecker#check(org.netbeans.modules.web.jsf.editor.el.JsfElExpression, java.util.List)
         */
        public boolean checkImpl(JsfElExpression expression, Document document,
                FileObject fileObject, List<Hint> hints) {
            /*
             * Fix for IZ#172143 - False EL Error 'Unknown resource bunde key "]".'
             */
            String property = expression.getPropertyBeingTypedName();
            if (property.length() > 0 && property.charAt(property.length() - 1) == ']') {
                property = property.substring(0, property.length() - 1);
            }
            property = expression.removeQuotes(property);
            List<String> propertyKeys = expression.getPropertyKeys(
                    expression.getBundleName(), property, null);
            for (String key : propertyKeys) {
                if (key != null && key.equals(property)) {
                    return true;
                }
            }
            int offset = expression.getExpression().lastIndexOf(
                    property);
            Hint hint = new Hint(HintsProvider.DEFAULT_ERROR_RULE,
                    NbBundle.getMessage(HintsProvider.class,
                    "MSG_UNKNOWN_RESOURCE_BUNDLE_CONTEXT",
                    property), fileObject,
                    new OffsetRange(expression.getStartOffset() + offset,
                    expression.getStartOffset() + offset +
                    property.length()),
                    Collections.<HintFix>singletonList(new DisableErrorChecksFix(fileObject, document)),
                    HintsProvider.DEFAULT_ERROR_HINT_PRIORITY);
            hints.add(hint);
            return true;
        }
    }

    //for unit testing, need to get the errorneous property itself
    public static class ElExpressionPropertyHint extends Hint {
        private String property;
        public ElExpressionPropertyHint(Rule rule, String description, FileObject file, OffsetRange range, List<HintFix> fixes, int priority, String property) {
            super(rule, description, file, range, fixes, priority);
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
        
    }

    static final String DISABLE_ERROR_CHECKS_KEY = "disable_el_error_checking"; //NOI18N

    public static boolean isErrorCheckingEnabled(FileObject fo) {
        return fo.getAttribute(DISABLE_ERROR_CHECKS_KEY) == null;
    }

    private static final class DisableErrorChecksFix implements HintFix {


        private static String VALUE = "true"; //NOI18N
        private FileObject fo;
        private Document doc;

        public DisableErrorChecksFix(FileObject fo, Document doc) {
            this.fo = fo;
            this.doc = doc;
        }

        public String getDescription() {
            return NbBundle.getMessage(ElContextChecker.class, "MSG_HINT_DISABLE_ERROR_CHECKS_FILE"); //NOI18N
        }

        public void implement() throws Exception {
            fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, VALUE);
            //force reparse => hints update
            forceReparse(doc);
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }

    private static final class EnableErrorChecksFix implements HintFix {

        private FileObject fo;
        private Document doc;

        public EnableErrorChecksFix(FileObject fo, Document doc) {
            this.fo = fo;
            this.doc = doc;
        }

        public String getDescription() {
            return NbBundle.getMessage(ElContextChecker.class, "MSG_HINT_ENABLE_ERROR_CHECKS_FILE"); //NOI18N
        }

        public void implement() throws Exception {
            fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, null);
            //force reparse => hints update
            forceReparse(doc);
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }

    private static void forceReparse(final Document doc) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                NbEditorDocument nbdoc = (NbEditorDocument) doc;
                nbdoc.runAtomic(new Runnable() {

                    public void run() {
                        MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                        if (mti != null) {
                            mti.tokenHierarchyControl().rebuild();
                        }
                    }
                });
            }
        });
    }

    private static class EnableELChecksHint extends Hint {

        public EnableELChecksHint(Rule rule, String description, FileObject file, OffsetRange range, List<HintFix> fixes, int priority) {
            super(rule, description, file, range, fixes, priority);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            //all hints of this type are equal
            return true;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }



    }

}
