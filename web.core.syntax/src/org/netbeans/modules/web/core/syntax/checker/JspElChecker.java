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
package org.netbeans.modules.web.core.syntax.checker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.Document;

import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.completion.JspELExpression;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class JspElChecker {
    
    protected static ErrorRule DEFAULT_ERROR_RULE = new Rule(HintSeverity.ERROR, true);
    protected static final int DEFAULT_ERROR_HINT_PRIORITY = 50;

    public void check( List<Hint> hints, Document document,
            FileObject fileObject, int offset )
    {
        JspELExpression expression = new JspELExpression( JspSyntaxSupport.get(document));
        int parseType = expression.parse( offset );
        if ( parseType == ELExpression.EL_UNKNOWN){
            String beanName = expression.getBeanName();
            Hint hint = new Hint(DEFAULT_ERROR_RULE,
                    NbBundle.getMessage(JspElChecker.class, "MSG_UNKNOWN_BEAN_CONTEXT", // NOI18N
                            beanName ),
                    fileObject,
                    new OffsetRange(expression.getStartOffset(), 
                            expression.getStartOffset()+beanName.length()),
                    Collections.<HintFix>emptyList(), DEFAULT_ERROR_HINT_PRIORITY);
            hints.add( hint );
            return;
        }
        
    }
    
    private static final Map<Integer, ElContextChecker> CHECKERS = new HashMap<Integer, 
        ElContextChecker>();
    
    static {
        CHECKERS.put( ELExpression.EL_START,  new ElStartContextChecker());
        CHECKERS.put( ELExpression.EL_BEAN, new ElBeanContextChecker());
        CHECKERS.put( ELExpression.EL_IMPLICIT,  CHECKERS.get( ELExpression.EL_BEAN));
    }
    
    protected static final class Rule implements ErrorRule {

        private HintSeverity severity;
        private boolean showInTasklist;

        private Rule(HintSeverity severity, boolean showInTaskList) {
            this.severity = severity;
            this.showInTasklist = showInTaskList;
        }

        public Set<?> getCodes() {
            return Collections.emptySet();
        }

        public boolean appliesTo(RuleContext context) {
            return true;
        }

        public String getDisplayName() {
            return "jsp"; //NOI18N 
        }

        public boolean showInTasklist() {
            return showInTasklist;
        }

        public HintSeverity getDefaultSeverity() {
            return severity;
        }
        
    }

}
