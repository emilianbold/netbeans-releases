/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.refactoring.plugins;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.test.RefactoringBaseTestCase;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmWhereUsedQueryPluginTestCase extends RefactoringBaseTestCase {

    public CsmWhereUsedQueryPluginTestCase(String testName) {
        super(testName);
    }

    @Override 
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }    
    
    public void testClassCustomer() throws Exception {
        performWhereUsed("customer.h", 49, 10);
    }
    
    protected void performWhereUsed(String source, int line, int column) throws Exception {
        performWhereUsed(source, line, column, Collections.emptyMap());
    }
    
    protected void performWhereUsed(String source, int line, int column, Map params) throws Exception {
        CsmReference ref = super.getReference(source, line, column);
        assertNotNull(ref);
        CsmObject targetObject = ref.getReferencedObject();
        assertNotNull(targetObject);
        Lookup lkp = Lookups.singleton(ref);
        WhereUsedQuery query = new WhereUsedQuery(lkp);
        // set parameters
        for (Map.Entry entry : (Set<Map.Entry>)params.entrySet()) {
            query.putValue(entry.getKey(), entry.getValue());
        }
        CsmWhereUsedQueryPlugin whereUsedPlugin = new CsmWhereUsedQueryPlugin(query);        
        Collection<RefactoringElementImplementation> elements = whereUsedPlugin.doPrepareElements(targetObject);
        assertNotNull(elements);
    }
}
