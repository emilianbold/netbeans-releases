/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.wsdl.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.xml.xam.Referenceable;


/**
 *
 * @author Sonali Kochar
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class WSDLRefactoringsFactory implements RefactoringPluginFactory {
   
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Referenceable ref = refactoring.getRefactoringSource().lookup(Referenceable.class);
        if(ref == null){
           //this is not my obj, dont participate in refactoring
           return null;
        }
        if (refactoring instanceof WhereUsedQuery) {
            return new WSDLWhereUsedRefactoringPlugin( (WhereUsedQuery)refactoring);
        } else if (refactoring instanceof RenameRefactoring) {
            return new WSDLRenameRefactoringPlugin( (RenameRefactoring)refactoring);
        } else if(refactoring instanceof SafeDeleteRefactoring) {
            return new WSDLSafeDeleteRefactoringPlugin( (SafeDeleteRefactoring)refactoring);
        } else if(refactoring instanceof MoveRefactoring) {
            return new WSDLMoveRefactoringPlugin( (MoveRefactoring)refactoring);
        } else if(refactoring instanceof SingleCopyRefactoring) {
            return new WSDLCopyRefactoringPlugin( (SingleCopyRefactoring)refactoring);
        }
        return null;
    }
}
