/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.execution;

import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.builds.ImportUtils;

/**
 *
 * @author Alexander Simon
 */
public class ExecutionSupportTest extends NbTestCase {
    private static final boolean TRACE = false;

    public ExecutionSupportTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testExecutionSupport1() throws Exception {
        String source = "CXX=CC CFLAGS=\"-g -xinstrument=datarace\" CXXFLAGS=\"-g -xinstrument=datarace\"";
        List<String> res = ImportUtils.parseEnvironment(source);
        assert res.size() == 3;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXX=CC".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=-g -xinstrument=datarace".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=-g -xinstrument=datarace".equals(p);
            }
        }
        res = ImportUtils.quoteList(res);
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXX=CC".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=\"-g -xinstrument=datarace\"".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=\"-g -xinstrument=datarace\"".equals(p);
            }
        }

        res = ImportUtils.parseArgs(source);
        assert res.size() == 3;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXX=CC".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=-g -xinstrument=datarace".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=-g -xinstrument=datarace".equals(p);
            }
        }
    }

    public void testExecutionSupport() throws Exception {
        String source = "./configure -DM=\"CPU = 6\" CPPFLAGS=-g3 CFLAGS=\'-g3 -gdwarf-2\' -DH --help -DM=\"'6\" CXXFLAGS=\"-g3 -gdwarf-2\" -G \"Unix Makefiles\" -DCMAKE_BUILD_TYPE=Debug";
        List<String> res = ImportUtils.parseEnvironment(source);
        assert res.size() == 3;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CPPFLAGS=-g3".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=-g3 -gdwarf-2".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=-g3 -gdwarf-2".equals(p);
            }
        }
        res = ImportUtils.quoteList(res);
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CPPFLAGS=-g3".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=\"-g3 -gdwarf-2\"".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=\"-g3 -gdwarf-2\"".equals(p);
            }
        }
        res = ImportUtils.parseArgs(source);
        assert res.size() == 11;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i==0){
                assert "./configure".equals(p);
            } else if (i==1) {
                assert "-DM=\"CPU = 6\"".equals(p);
            } else if (i == 2) {
                assert "CPPFLAGS=-g3".equals(p);
            } else if (i == 3) {
                assert "CFLAGS=-g3 -gdwarf-2".equals(p);
            } else if (i==4){
                assert "-DH".equals(p);
            } else if (i==5){
                assert "--help".equals(p);
            } else if (i==6){
                assert "-DM=\"'6\"".equals(p);
            } else if (i == 7) {
                assert "CXXFLAGS=-g3 -gdwarf-2".equals(p);
            } else if (i == 8) {
                assert "-G".equals(p);
            } else if (i == 9) {
                assert "Unix Makefiles".equals(p);
            } else if (i == 10) {
                assert "-DCMAKE_BUILD_TYPE=Debug".equals(p);
            }
        }
    }

}
