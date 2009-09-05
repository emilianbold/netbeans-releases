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

import java.util.Iterator;
import java.util.List;

import javax.swing.text.Document;

import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.JspUtils;
import org.netbeans.modules.web.core.syntax.completion.ELFunctions;
import org.netbeans.modules.web.core.syntax.completion.ELImplicitObjects;
import org.netbeans.modules.web.core.syntax.completion.ELFunctions.Function;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;


/**
 * @author ads
 *
 */
public class ElStartContextChecker {

    protected boolean checkElStart( ELExpression expression, Document document ) 
    {
        if ( ELImplicitObjects.getELImplicitObject(expression.getReplace()) != null){
            // context expression is implicit object. All is OK.
            return true;
        }

        if (JspUtils.isJspDocument(document)) {
            JspSyntaxSupport support = JspSyntaxSupport.get(document);
            // defined beans on the page
            BeanData[] beans = support.getBeanData();
            if (beans != null) {
                for (int i = 0; i < beans.length; i++) {
                    if (beans[i].getId().equals(expression.getReplace())) {
                        // context expression is existed bean. All is OK.
                        return true;
                    }
                }
            }
            //Functions
            List<Function> functions = 
                ELFunctions.getFunctions(support, expression.getReplace());
            Iterator<Function> iter = functions.iterator();
            while (iter.hasNext()) {
                Function func = iter.next();
                StringBuilder builder = new StringBuilder( func.getPrefix());
                builder.append(':');
                builder.append( func.getName());
                if ( builder.toString().equals( expression.getReplace())){
                    // context is function. All is OK.
                    return true;
                }
            }
        }      
        return false;
    }
}
