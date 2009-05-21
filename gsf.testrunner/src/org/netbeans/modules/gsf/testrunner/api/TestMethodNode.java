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

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.Image;
import java.io.CharConversionException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
public class TestMethodNode extends AbstractNode {

    /**
     * Specifies whether the failure message should be inlined in the test method node.
     * See #149315.
     */
    private static final boolean INLINE_RESULTS =
            Boolean.valueOf(System.getProperty("gsf.testrunner.inline_result", "true")); // NOI18N

    /** */
    protected final Testcase testcase;
    protected final Project project;

    /**
     * Creates a new instance of TestcaseNode
     */
    public TestMethodNode(final Testcase testcase, Project project) {
        this(testcase, project, null);
    }

    protected TestMethodNode(final Testcase testcase, Project project, Lookup lookup) {
        super(testcase.getTrouble() != null
              ? new TestMethodNodeChildren(testcase)
              : Children.LEAF, lookup);

        this.testcase = testcase;
        this.project = project;

        setDisplayName();

        if (TestsuiteNode.DISPLAY_TOOLTIPS) {
            setShortDescription(TestsuiteNode.toTooltipText(testcase.getOutput()));
        }

    }

    /**
     */
    private void setDisplayName() {
        final int status = (testcase.getTrouble() == null)
                           ? 0
                           : testcase.getTrouble().isError() ? 1 : 2;
        
        if ((status == 0) && (testcase.getTimeMillis() < 0)) {
            setDisplayName(testcase.getName());
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
                testcase.getTimeMillis() < 0
                ? NbBundle.getMessage(TestMethodNode.class,
                                      noTimeKeys[status],testcase.getName())
                : NbBundle.getMessage(TestMethodNode.class,
                                      timeKeys[status],testcase.getName(),
                                      new Float(testcase.getTimeMillis()/1000f)));
    }
    
    /**
     */
    @Override
    public String getHtmlDisplayName() {
        Status status = testcase.getStatus();

        StringBuffer buf = new StringBuffer(60);
        buf.append(testcase.getName());
        buf.append("&nbsp;&nbsp;");                                     //NOI18N
        buf.append("<font color='#");                                   //NOI18N
        buf.append(status.getHtmlDisplayColor() + "'>"); //NOI18N

        String cause = null;
        if (INLINE_RESULTS && testcase.getTrouble() != null && testcase.getTrouble().getStackTrace() != null &&
                testcase.getTrouble().getStackTrace().length > 0) {
            try {
                cause = XMLUtil.toElementContent(testcase.getTrouble().getStackTrace()[0]).replace("\n", "&nbsp;"); // NOI18N
            } catch (CharConversionException ex) {
                // We're messing with user testoutput - always risky. Don't complain
                // here, simply fall back to the old behavior of the test runner -
                // don't include the message
                cause = null;
            }
        }

        if (cause != null) {
            buf.append(NbBundle.getMessage(TestMethodNode.class,
                    DisplayNameMapper.getCauseKey(status), cause));
        } else {
            buf.append(testcase.getTimeMillis() < 0
                    ? NbBundle.getMessage(TestMethodNode.class,
                    DisplayNameMapper.getNoTimeKey(status))
                    : NbBundle.getMessage(TestMethodNode.class,
                    DisplayNameMapper.getTimeKey(status),
                    new Float(testcase.getTimeMillis() / 1000f)));
        }

        buf.append("</font>");                                          //NOI18N
        return buf.toString();
    }
    
    /**
     */
    @Override
    public Action getPreferredAction() {
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {new DiffViewAction(testcase)};
    }
    
    @Override
    public Image getIcon(int type) {
        if (Status.PENDING == testcase.getStatus()) {
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/warning2_16.png"); //NOI18N
        }
        if (failed()) {
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/warning_16.png"); //NOI18N
        }
        return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/ok_16.png"); //NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    public boolean failed() {
        return testcase.getStatus().equals(Status.FAILED)
                || testcase.getStatus().equals(Status.ERROR);
    }

    
    private static final class DisplayNameMapper {

        private static final Map< Status,String> NO_TIME_KEYS = initNoTimeKeys();
        private static final Map< Status,String> TIME_KEYS = initTimeKeys();
        private static final Map< Status,String> CAUSE_KEYS = initCauseKeys();

        private static Map< Status,String> initNoTimeKeys() {
            Map< Status,String> result = new HashMap< Status,String>(4);
            result.put(Status.PASSED, "MSG_TestMethodPassed_HTML"); //NOI18N
            result.put(Status.ERROR, "MSG_TestMethodError_HTML"); //NOI18N
            result.put(Status.FAILED, "MSG_TestMethodFailed_HTML"); //NOI18N
            result.put(Status.PENDING, "MSG_TestMethodPending_HTML"); //NOI18N
            return result;
        }

        private static Map< Status,String> initTimeKeys() {
            Map< Status,String> result = new HashMap< Status,String>(4);
            result.put(Status.PASSED, "MSG_TestMethodPassed_HTML_time"); //NOI18N
            result.put(Status.ERROR, "MSG_TestMethodError_HTML_time"); //NOI18N
            result.put(Status.FAILED, "MSG_TestMethodFailed_HTML_time"); //NOI18N
            result.put(Status.PENDING, "MSG_TestMethodPending_HTML_time"); //NOI18N
            return result;
        }

        private static Map< Status,String> initCauseKeys() {
            Map< Status,String> result = new HashMap< Status,String>(4);
            result.put(Status.PASSED, "MSG_TestMethodPassed_HTML_cause"); //NOI18N
            result.put(Status.ERROR, "MSG_TestMethodError_HTML_cause"); //NOI18N
            result.put(Status.FAILED, "MSG_TestMethodFailed_HTML_cause"); //NOI18N
            result.put(Status.PENDING, "MSG_TestMethodPending_HTML_cause"); //NOI18N
            return result;
        }

        static String getCauseKey(Status status) {
            return CAUSE_KEYS.get(status);
        }
        
        static String getNoTimeKey(Status status) {
            return NO_TIME_KEYS.get(status);
        }

        static String getTimeKey(Status status) {
            return TIME_KEYS.get(status);
        }
    }
}
