/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.File;
import java.util.Iterator;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * Common base class for CsmSelect unit tests
 * @author Vladimir Kvashin
 */
public abstract class SelectTestBase extends ModelImplBaseTestCase {
    private static final boolean TRACE = false;
    private TraceModelBase traceModel;

    public SelectTestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File projectRoot = Manager.normalizeFile(getProjectRoot());
        assertTrue(projectRoot.exists());
	traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
	traceModel.processArguments(projectRoot.getAbsolutePath());
    }

    protected abstract File getProjectRoot();

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected File getTestCaseDataDir() {
        return getDataDir();
    }


    public void doTestGetFunctions() throws Exception {
        CsmProject project = traceModel.getProject();
        project.waitParse();
        _testGetFunctions(project.getGlobalNamespace());
        for (CsmProject lib : project.getLibraries()) {
            _testGetFunctions(lib.getGlobalNamespace());
        }
        assertNoExceptions();
    }

    protected void _testGetFunctions(CsmNamespace nsp) throws Exception {
        CsmProject project = nsp.getProject();
        for (CsmDeclaration decl : nsp.getDeclarations()) {
            if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction func = (CsmFunction) decl;
                CharSequence qName = decl.getQualifiedName();
                if (TRACE) { System.err.printf("Seearching for funcion %s\n", func); }
                Iterator<CsmFunction> iter = CsmSelect.getFunctions(project, qName);
                assertTrue("Function " + qName.toString() + " not found", iter.hasNext());
            }
        }
        for (CsmNamespace nested : nsp.getNestedNamespaces()) {
            _testGetFunctions(nested);
        }
    }

}
