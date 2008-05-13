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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ruby.testrunner.ui;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Marian Petras
 */
final class TestMethodNode extends AbstractNode {

    /** */
    private final Report.Testcase testcase;

    /**
     * Creates a new instance of TestcaseNode
     */
    TestMethodNode(final Report.Testcase testcase) {
        super(testcase.trouble != null
              ? new TestMethodNodeChildren(testcase)
              : Children.LEAF);

        this.testcase = testcase;

        setDisplayName();
        setIconBaseWithExtension(
                "org/netbeans/modules/ruby/testrunner/ui/res/method.gif");    //NOI18N
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
        
        String[] noTimeKeys = new String[] {
                                      null,
                                      "MSG_TestMethodError",            //NOI18N
                                      "MSG_TestMethodFailed"};          //NOI18N
        String[] timeKeys = new String[] {
                                      "MSG_TestMethodPassed_time",      //NOI18N
                                      "MSG_TestMethodError_time",       //NOI18N
                                      "MSG_TestMethodFailed_time"};     //NOI18N
        setDisplayName(
                testcase.timeMillis < 0
                ? NbBundle.getMessage(getClass(),
                                      noTimeKeys[status],
                                      testcase.name)
                : NbBundle.getMessage(getClass(),
                                      timeKeys[status],
                                      testcase.name,
                                      new Float(testcase.timeMillis/1000f)));
    }
    
    /**
     */
    @Override
    public String getHtmlDisplayName() {
        final int status = (testcase.trouble == null)
                           ? 0
                           : testcase.trouble.isError() ? 1 : 2;
        String[] noTimeKeys = new String[] {
                                      "MSG_TestMethodPassed_HTML",      //NOI18N
                                      "MSG_TestMethodError_HTML",       //NOI18N
                                      "MSG_TestMethodFailed_HTML"};     //NOI18N
        String[] timeKeys = new String[] {
                                      "MSG_TestMethodPassed_HTML_time", //NOI18N
                                      "MSG_TestMethodError_HTML_time",  //NOI18N
                                      "MSG_TestMethodFailed_HTML_time"};//NOI18N
                                          
        StringBuffer buf = new StringBuffer(60);
        buf.append(testcase.name);
        buf.append("&nbsp;&nbsp;");                                     //NOI18N
        buf.append("<font color='#");                                   //NOI18N
        buf.append(testcase.trouble != null ? "FF0000'>" : "00CC00'>"); //NOI18N
        buf.append(testcase.timeMillis < 0
                   ? NbBundle.getMessage(getClass(),
                                         noTimeKeys[status])
                   : NbBundle.getMessage(getClass(),
                                         timeKeys[status],
                                         new Float(testcase.timeMillis/1000f)));
        buf.append("</font>");                                          //NOI18N
        return buf.toString();
    }
    
    /**
     */
    @Override
    public Action getPreferredAction() {
        Report.Trouble trouble = testcase.trouble;
        String callstackFrameInfo =
                ((trouble != null)
                        && (trouble.stackTrace != null)
                        && (trouble.stackTrace.length != 0))
                ? trouble.stackTrace[0]
                : null;
        
        return new JumpAction(this, callstackFrameInfo);
    }
    
    public SystemAction[] getActions(boolean context) {
        return new SystemAction[0];
    }
}
