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

package org.netbeans.modules.junit.output;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.DiffViewAction;
import org.netbeans.modules.gsf.testrunner.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import static org.netbeans.spi.project.SingleMethod.COMMAND_RUN_SINGLE_METHOD;
import static org.netbeans.spi.project.SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;

/**
 *
 * @author answer
 */
public class JUnitTestMethodNode extends TestMethodNode{

    public JUnitTestMethodNode(Testcase testcase, Project project, Lookup lookup) {
        super(testcase, project, lookup);
    }

    public JUnitTestMethodNode(Testcase testcase, Project project) {
        super(testcase, project);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        Action preferred = getPreferredAction();
        if (preferred != null) {
            actions.add(preferred);
        }
//        FileObject suiteFile = ((JUnitTestcase)testcase).getTestSuite().getSuiteFile();
        FileObject testFO = ((JUnitTestcase)testcase).getClassFileObject();
        if (testFO != null){
            Project suiteProject = FileOwnerQuery.getOwner(testFO);
            ActionProvider actionProvider = suiteProject.getLookup().lookup(ActionProvider.class);
            if (actionProvider != null){
                boolean runSupported = false;
                boolean debugSupported = false;
                for (String action : actionProvider.getSupportedActions()) {
                    if (!runSupported && action.equals(COMMAND_RUN_SINGLE_METHOD)) {
                        runSupported = true;
                        if (debugSupported) {
                            break;
                        }
                    }
                    if (!debugSupported && action.equals(COMMAND_DEBUG_SINGLE_METHOD)) {
                        debugSupported = true;
                        if (runSupported) {
                            break;
                        }
                    }
                }

                SingleMethod methodSpec = new SingleMethod(testFO, testcase.getName());
                Lookup nodeContext = Lookups.singleton(methodSpec);
                if (runSupported && actionProvider.isActionEnabled(COMMAND_RUN_SINGLE_METHOD,
                                                                   nodeContext)) {
                    actions.add(new TestMethodNodeAction(actionProvider,
                                                         nodeContext,
                                                         COMMAND_RUN_SINGLE_METHOD,
                                                         "LBL_RerunTest"));     //NOI18N
                }
                if (debugSupported && actionProvider.isActionEnabled(COMMAND_DEBUG_SINGLE_METHOD,
                                                                     nodeContext)) {
                    actions.add(new TestMethodNodeAction(actionProvider,
                                                         nodeContext,
                                                         COMMAND_DEBUG_SINGLE_METHOD,
                                                         "LBL_DebugTest"));     //NOI18N
                }
            }
        }
        if ((testcase.getTrouble() != null) && (testcase.getTrouble().getComparisonFailure() != null)){
            actions.add(new DiffViewAction(testcase));
        }

        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Action getPreferredAction() {
        return new JumpAction(this, null);
    }

    public JUnitTestcase getTestcase(){
        return (JUnitTestcase)testcase;
    }
}
