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
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.SourceElement;

/**
 * Implementation of the <code>CompletionResultProvider</code> for the new 
 * instruction context in the method body (i.e. body of the function defined in 
 * the body of the class).
 * 
 * <p><b>Note that this implementation is not synchronized.</b></p> 
 * 
 * @author Victor G. Vasilyev 
 */
public class MethodBodyContext extends NewInstructionContext 
        implements CompletionResultProvider {

    /**
     * Returns <code>true</code> iif the specified <code>context</code>
     * is applicable for inserting a new instruction in the method body.
     * E.g 
     * <p> 
     * <b>&lt;?php</b> <b>function</b> some_name($arg1, $arg2) { ...
     *     <i>Instruction</i><b>;</b> 
     *     <span style="color: rgb(255, 0, 0);"><blink>|</blink></span>
     *     <i>Instruction</i><b>;</b>
     *     ... } <b>?&gt;</b>
     * </p> 
     * 
     * @param context
     * @return <code>true</code> iif the specified <code>context</code>
     * is applicable.
     */
    @Override
    public boolean isApplicable(CodeCompletionContext context) {
        if(!super.isApplicable(context)) {
            return false;
        }
        // check if context.getSourceElement() is a function body and 
        // its parent is a <code>ClassDefinition</code>
        SourceElement e = context.getSourceElement();
        if(e instanceof FunctionDefinition) {
            SourceElement parent = e.getParent();
            if(parent instanceof ClassBody) {
                parent = parent.getParent();
                if(parent instanceof ClassDefinition) {
                    //caching the ClassDefinition
                    classDefinition = (ClassDefinition)parent;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<CompletionProposal> getProposals(final CodeCompletionContext context) {
        checkContext(context);
        super.addStaticProposals();
        return proposalList;
    }

    
    /** Adds the proposals that are applicable to any context. */
    @Override
    protected void addStaticProposals() {
        int caretOffset = getCaretOffset();
        HtmlFormatter formater = getFormatter();
        // TODO SpecialKeywords
//        proposalList.add(new KeywordItem(Keywords.CLASS, caretOffset, formater));
    }
    
    protected ClassDefinition getClassDefinition() {
        return classDefinition;
    }
    
    private ClassDefinition classDefinition;
         
}
