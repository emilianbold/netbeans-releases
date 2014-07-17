/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmSymbolResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.trace.FileModelCpp11Test;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public class CsmSymbolResolverTestCase extends SelectTestBase {
    
    public CsmSymbolResolverTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.tests.cpp11directories", FileModelCpp11Test.class.getSimpleName()); // NOI18N
        super.setUp(); 
    }    
    
    @Override
    protected File getProjectRoot() {
        File dataDir = super.getDataDir();
        String fullClassName = getTestCaseDataClass().getName();
        String filePath = fullClassName.replace('.', File.separatorChar);
        return Manager.normalizeFile(new File(dataDir, filePath));        
    }

    @Override
    protected Iterator<CsmFunction> _getFunctions(CsmProject project, CsmFunction func) {
        String funText = func.getSignature().toString();
        funText = funText.replace(func.getName(), func.getQualifiedName());
        if (func instanceof CsmTemplate && ((CsmTemplate) func).isTemplate()) {
            funText = func.getReturnType().getCanonicalText() + " " + funText;
        }
        Collection<CsmObject> result = CsmSymbolResolver.resolveSymbol((NativeProject) project.getPlatformProject(), funText);
        return ((Collection<CsmFunction>)(Object) result).iterator();
    }
    
    @Override
    protected Iterator<CsmVariable> _getVariables(CsmProject project, CsmVariable var) {
        String varText = var.getQualifiedName().toString();
        Collection<CsmObject> result = CsmSymbolResolver.resolveSymbol((NativeProject) project.getPlatformProject(), varText);
        return ((Collection<CsmVariable>)(Object) result).iterator();
    }

    @Override
    protected boolean _checkFound(CsmObject obj, Iterator<? extends CsmObject> answer) {
        return answer.hasNext() && answer.next() == obj;
    }
    
    private void doTestSingle(String symbol, String fileName, int line, int column) {
        Collection<CsmObject> result = CsmSymbolResolver.resolveSymbol(getProject(), symbol);
        assertFalse("Symbol '" + symbol + "' not found at " + fileName + ":" + line + ":" + column, result.isEmpty());
        CsmOffsetable obj = (CsmOffsetable) result.iterator().next();
        assertEquals(fileName, obj.getContainingFile().getName().toString());
        assertEquals(line, obj.getStartPosition().getLine());
        assertEquals(column, obj.getStartPosition().getColumn());
    }
    
    public void testSelectModelGetFunctions() throws Exception {
        doTestGetFunctions();
    }    
    
    public void testSelectModelGetMethods() throws Exception {
        doTestGetMethods();
    }       
    
    public void testSelectModelGetVariables() throws Exception {
        doTestGetVariables();
    }
    
    public void testSelectModelGetFields() throws Exception {
        doTestGetFields();
    }    
    
    public void testInstantiatedSignature() throws Exception {
        doTestSingle("double entity_resolver_test::boo<double>(double)", "simple_symbol_resolver_test.cpp", 43, 3);
    }
}
