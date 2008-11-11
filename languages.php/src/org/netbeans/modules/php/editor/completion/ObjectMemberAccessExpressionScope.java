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

package org.netbeans.modules.php.editor.completion;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.languages.php.lang.Operators;
import org.netbeans.modules.php.model.SourceElement;

/**
 * Implementation of the <code>CompletionResultProvider</code> for the 
 * scope of the Object Member Access Expression (->), like this:
 * <code>
 * $instanceRef->$attr
 * $instanceRef->CONST1
 * $instanceRef->func1(); 
 * <code>
 *  
 * @author Victor G. Vasilyev
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.editor.completion.CompletionResultProvider.class)
public class ObjectMemberAccessExpressionScope 
       extends MemberAccessExpressionScope implements CompletionResultProvider {            

    private static final Logger LOG = 
            Logger.getLogger(ObjectMemberAccessExpressionScope.class.getName());

    public boolean isApplicable(CodeCompletionContext context) {
        init(context);
        SourceElement e = myContext.getSourceElement();
        // This provider is applicable iif it possible to find referencedClass.
        if(referencedClass == null) {
            LOG.log(Level.INFO, 
                    "{0} provider is NOT applicable. referencedClass == null", 
                    new Object[] {
                            ObjectMemberAccessExpressionScope.class.getName()});
            return false;
        }
        LOG.log(Level.INFO, 
                "{0} provider is applicable. referencedClass.getName() =[{1}]", 
                new Object[] {ObjectMemberAccessExpressionScope.class.getName(), 
                              referencedClass.getName()});
        return true;
    }

    public List<CompletionProposal> getProposals(CodeCompletionContext context) {
        return proposals;
    }

    @Override
    protected int calcInsertOffset() {
        if(prefix == null || getOperator().equals(prefix)) {
            return myContext.getCaretOffset();
        }
        return myContext.getCaretOffset() - prefix.length();
    }

    @Override
    protected String getOperator() {
        return Operators.OBJECT_MEMBER_ACCESS.value();
    }

    private boolean isObjectMemberAccessExpression(SourceElement e) {
        String text = e.getText();
        if (text != null &&
                text.contains(Operators.OBJECT_MEMBER_ACCESS.value())) {
            return true;
        }
        return false;
    }
    

}
