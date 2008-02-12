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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.repository.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 *
 * @author sg155630
 */
public class RepositoryValidationBase extends TraceModelTestBase {

    public RepositoryValidationBase(String testName) {
        super(testName);
    }

    protected static final String nimi = "ModelBuiltFromRepository"; //NOI18N
    protected static final String modelimplName = "cnd.modelimpl";
    protected static final String moduleName = "cnd.repository";
    private static String goldenDirectory;

    @Override
    protected File getTestCaseDataDir() {
	String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N
        String filePath = "common";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    protected static String getGoldenDirectory() {
        return goldenDirectory;
    }

    protected static void setGoldenDirectory(String goldenDirectory) {
        RepositoryValidationBase.goldenDirectory = goldenDirectory;
    }
    
    protected final List<String> find() throws IOException {
        List<String> list = new ArrayList<String>();
        String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N
        list.add(dataPath + "/common/quote_nosyshdr"); //NOI18N
        list.add(dataPath + "/org"); //NOI18N
//        String path = getWorkDir().getAbsolutePath();
//        assert (path.indexOf(moduleName) > -1);
//        File root = new File(path.substring(0, path.indexOf(moduleName)));
//        for (File m : root.listFiles()) {
//            if (m.isDirectory()) {
//                String testDataPath = m.getAbsolutePath() + File.separator + "test" + File.separator + "unit" + File.separator + "data" + File.separator + "org";
//                if (
//                    // TODO: stackoverflow.cc failure
//                    testDataPath.indexOf("modelimpl")>0 ||
//                    // TODO: mixedPreprocDirectives.cc failure
//                    testDataPath.indexOf("folding")>0 ||
//                    // TODO: completion file.cc failure
//                    testDataPath.indexOf("completion")>0) 
//                {
//                    continue;
//                }
//                //
//                if (new File(testDataPath).exists()) {
//                    list.add(testDataPath);
//                }
//            }
//        }
        return list;
    }
}