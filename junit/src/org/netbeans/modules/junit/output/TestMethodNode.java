/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.junit.output;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.junit.output.HtmlMarkupUtils.COLOR_OK;
import static org.netbeans.modules.junit.output.HtmlMarkupUtils.COLOR_WARNING;
import static org.netbeans.modules.junit.output.HtmlMarkupUtils.COLOR_FAILURE;
import static org.netbeans.modules.junit.output.OutputUtils.NO_ACTIONS;
import static org.netbeans.modules.junit.output.Report.Testcase;
import static org.netbeans.spi.project.SingleMethod.COMMAND_RUN_SINGLE_METHOD;
import static org.netbeans.spi.project.SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;

/**
 *
 * @author Marian Petras
 */
final class TestMethodNode extends AbstractNode {

    private static final String[] NO_TIME_STATUS_KEYS = new String[] {
                                      null,
                                      "MSG_TestMethodError",            //NOI18N
                                      "MSG_TestMethodFailed"};          //NOI18N
    private static final String[] TIME_STATUS_KEYS = new String[] {
                                      "MSG_TestMethodPassed_time",      //NOI18N
                                      "MSG_TestMethodError_time",       //NOI18N
                                      "MSG_TestMethodFailed_time"};     //NOI18N
    private static final String STATUS_KEY_INTERRUPTED
                                    = "MSG_TestMethodInterrupted";      //NOI18N
    private static final String[] NO_TIME_STATUS_KEYS_HTML = new String[] {
                                      "MSG_TestMethodPassed_HTML",      //NOI18N
                                      "MSG_TestMethodError_HTML",       //NOI18N
                                      "MSG_TestMethodFailed_HTML"};     //NOI18N
    private static final String[] TIME_STATUS_KEYS_HTML = new String[] {
                                      "MSG_TestMethodPassed_HTML_time", //NOI18N
                                      "MSG_TestMethodError_HTML_time",  //NOI18N
                                      "MSG_TestMethodFailed_HTML_time"};//NOI18N
    private static final String STATUS_KEY_INTERRUPTED_HTML
                                    = "MSG_TestMethodInterrupted_HTML"; //NOI18N

    /** */
    private final Report.Testcase testcase;

    /**
     * Creates a new instance of TestcaseNode
     */
    TestMethodNode(final Report.Testcase testcase) {
        super(TestMethodNodeChildren.getChildrenCount(testcase) != 0
              ? new TestMethodNodeChildren(testcase)
              : Children.LEAF);

        this.testcase = testcase;

        setDisplayName();
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/method.gif");    //NOI18N
    }

    /**
     */
    private void setDisplayName() {
        final int status = (testcase.trouble == null)
                           ? 0
                           : testcase.trouble.isError() ? 1 : 2;
        
        if ((status == 0) && (testcase.timeMillis < 0)) {
            setDisplayName(testcase.name);
            return;
        }

        String bundleKey;
        Object[] bundleParams;
        if (testcase.timeMillis == Testcase.NOT_FINISHED_YET) {
            bundleKey = STATUS_KEY_INTERRUPTED;
            bundleParams = new Object[] {testcase.name};
        } else if (testcase.timeMillis == Testcase.TIME_UNKNOWN) {
            bundleKey = NO_TIME_STATUS_KEYS[status];
            bundleParams = new Object[] {testcase.name};
        } else {
            bundleKey = TIME_STATUS_KEYS[status];
            bundleParams = new Object[] {testcase.name,
                                         new Float(testcase.timeMillis/1000f)};
        }
        setDisplayName(NbBundle.getMessage(getClass(), bundleKey, bundleParams));
    }
    
    /**
     */
    @Override
    public String getHtmlDisplayName() {
        final int status = (testcase.trouble == null)
                           ? 0
                           : testcase.trouble.isError() ? 1 : 2;

        String bundleKey;
        Object bundleParam;
        String color = null;
        if (testcase.timeMillis == Testcase.NOT_FINISHED_YET) {
            bundleKey = STATUS_KEY_INTERRUPTED_HTML;
            bundleParam = null;
            color = COLOR_WARNING;
        } else if (testcase.timeMillis == Testcase.TIME_UNKNOWN) {
            bundleKey = NO_TIME_STATUS_KEYS_HTML[status];
            bundleParam = null;
        } else {
            bundleKey = TIME_STATUS_KEYS_HTML[status];
            bundleParam = new Float(testcase.timeMillis/1000f);
        }
        if (color == null) {
            color = (testcase.trouble != null) ? COLOR_FAILURE : COLOR_OK;
        }
                                          
        StringBuilder buf = new StringBuilder(60);
        buf.append(testcase.name);
        buf.append("&nbsp;&nbsp;");                                     //NOI18N
        if (bundleParam == null) {
            HtmlMarkupUtils.appendColourText(buf, color, bundleKey);
        } else {
            HtmlMarkupUtils.appendColourText(buf, color, bundleKey, bundleParam);
        }
        return buf.toString();
    }
    
    /**
     */
    @Override
    public Action getPreferredAction() {
        return (testcase.trouble != null)
               ? new JumpAction(this, testcase.trouble)
               : null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        if (context) {
            return NO_ACTIONS;
        }

        Report report = OutputUtils.getReport(this);
        ClassPath srcClassPath = report.getSourceClassPath();
        if (srcClassPath == null) {
            return NO_ACTIONS;
        }

        String suiteClassName = report.suiteClassName;
        String suiteFileName = suiteClassName.replace('.', '/')
                               + ".java";                               //NOI18N
        FileObject suiteFile = srcClassPath.findResource(suiteFileName);
        if (suiteFile == null) {
            return NO_ACTIONS;
        }

        Project project = FileOwnerQuery.getOwner(suiteFile);
        if (project == null) {
            return NO_ACTIONS;
        }

        ActionProvider actionProvider = project.getLookup().lookup(ActionProvider.class);
        if (actionProvider == null) {
            return NO_ACTIONS;
        }

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
        if (!runSupported && !debugSupported) {
            return NO_ACTIONS;
        }

        SingleMethod methodSpec = new SingleMethod(suiteFile, testcase.name);
        Lookup nodeContext = Lookups.singleton(methodSpec);

        List<Action> actions = new ArrayList<Action>(2);
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
        if (actions.isEmpty()) {
            return NO_ACTIONS;
        }

        return actions.toArray(new Action[actions.size()]);
    }

}
