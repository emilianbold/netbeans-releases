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
package org.netbeans.modules.web.core.syntax.completion;

import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;

/**
 * @todo Use proper inheritance of this class - the base does nothing about JSP beans etc...
 *       Now it is hacked by if(sup != null) { ... } 
 *
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
/**
 *  This is a helper class for parsing and obtaining items for code completion of expression
 *  language.
 */
public class JspELExpression extends ELExpression {

    private JspSyntaxSupport sup;

    public JspELExpression(JspSyntaxSupport sup) {
        super(sup.getDocument());
        this.sup = sup;
    }

    /**
     *  @return the class of the top-level object used in the expression
     */
    @Override
    public String getObjectClass() {
        String beanName = extractBeanName();
        if (sup != null) {
            BeanData[] allBeans = sup.getBeanData();
            if (allBeans != null) {
                for (BeanData beanData : allBeans) {
                    if (beanData.getId().equals(beanName)) {
                        return beanData.getClassName();
                    }

                }
            }
        }

        return super.getObjectClass();
        
    }

    /** Return context, whether the expression is about a bean, implicit object or
     *  function.
     */
    @Override
    protected int findContext(String expr) {
        int dotIndex = expr.indexOf('.');
        int bracketIndex = expr.indexOf('[');
        int value = EL_UNKNOWN;

        if (bracketIndex == -1 && dotIndex > -1) {
            String first = expr.substring(0, dotIndex);
            BeanData[] beans = sup.getBeanData();
            if (beans != null) {
                for (int i = 0; i <
                        beans.length; i++) {
                    if (beans[i].getId().equals(first)) {
                        value = EL_BEAN;
                        continue;

                    }
                }
            }
            if (value == EL_UNKNOWN && ELImplicitObjects.getELImplicitObjects(first).size() > 0) {
                value = EL_IMPLICIT;
            }

        } else if (bracketIndex == -1 && dotIndex == -1) {
            value = EL_START;
        }

        return value;
    }

}
