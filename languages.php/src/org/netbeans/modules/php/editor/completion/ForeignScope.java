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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.api.CompletionProposal;

/**
 * Implementation of the <code>CompletionResultProvider</code> for the foreign 
 * context (i.e. non-PHP context).
 * <p><b>Note that this implementation is not synchronized.</b></p> 
 * 
 * @author Victor G. Vasilyev 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.editor.completion.CompletionResultProvider.class)
public class ForeignScope extends ASTBasedProvider 
        implements CompletionResultProvider {

    protected final List<CompletionProposal> proposalList =
            new ArrayList<CompletionProposal>();
    

    /**
     * Returns <code>true</code> iif the specified <code>context</code>
     * is applicable for inserting opening PHP tag.
     * E.g 
     * <p> ... some foreign code ...<br/>
     * <b>&lt;?php</b> ... <b>?&gt;</b><br/>
     *  ... some foreign code ...
     * </p> 
     * @param context
     * @return <code>true</code> iif the specified <code>context</code>
     * is applicable.
     */
    public boolean isApplicable(CodeCompletionContext context) {
        init(context);
        try {
            assertPHPContext();
            return false; // fault - PHP context is not applicable.
        } catch (IOException ioe) {
            return false;  // fault - Document is not accesible.
        } catch (Exception e) {
            // OK. It is non-PHP context.
        }
        try {
            assertMIMETypePHP();
            return true; 
        }
        catch(Exception e) {
            return false; // The specified context is outside of the PHP project.
        }
    }

    public List<CompletionProposal> getProposals(final CodeCompletionContext context) {
        checkContext(context);
        proposalList.add(new PHPBlockItem(context));
        return proposalList;
    }

    protected void init(CodeCompletionContext context) {
        assert context != null;
        myContext = context;
        proposalList.clear();
    }

}

