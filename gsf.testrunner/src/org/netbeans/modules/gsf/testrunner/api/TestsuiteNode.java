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

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
public class TestsuiteNode extends AbstractNode {

    /**
     * The max number of output lines to display in the tooltip.
     */
    private static final int MAX_TOOLTIP_LINES = Integer.getInteger("testrunner.max.tooltip.lines", 30); //NOI18N
    /**
     * The max line length to display in the tooltip.
     */
    private static final int MAX_TOOLTIP_LINE_LENGTH = Integer.getInteger("testrunner.max.tooltip.line.length", 200); //NOI18N
    /**
     * The system property for enabling/disabling tooltips.
     */
    static final boolean DISPLAY_TOOLTIPS = Boolean.valueOf(System.getProperty("testrunner.display.tooltips", "true"));//NOI18N

    protected String suiteName;
    protected Report report;
    protected boolean filtered;

    /**
     *
     * @param  suiteName  name of the test suite, or {@code ANONYMOUS_SUITE}
     *                    in the case of anonymous suite
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    public TestsuiteNode(final String suiteName, final boolean filtered) {
        this(null, suiteName, filtered, null);
    }
    
    /**
     * Creates a new instance of TestsuiteNode
     */
    public TestsuiteNode(final Report report, final boolean filtered) {
        this(report, null, filtered, null);
    }


    /**
     *
     * @param  suiteName  name of the test suite, or {@code ANONYMOUS_SUITE}
     *                    in the case of anonymous suite
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    protected TestsuiteNode(final Report report,
                          final String suiteName,
                          final boolean filtered, Lookup lookup) {
        super(report != null ? new TestsuiteNodeChildren(report, filtered)
                             : Children.LEAF, lookup);
        this.report = report; 
        this.suiteName = (report != null) ? report.getSuiteClassName() : suiteName;
        this.filtered = filtered;
        
        assert this.suiteName != null;
        
        setDisplayName();
    }

    /**
     * @return the given lines appropriately formatted for a tooltip.
     */
    static String toTooltipText(List<OutputLine> lines) {
        StringBuilder result = new StringBuilder();
        result.append("<html>"); //NOI18N
        if (lines.isEmpty()) {
            result.append("<i>" + NbBundle.getMessage(TestsuiteNode.class, "MSG_NoOutput") + "</i>"); //NOI18N
        } else {
            for (int i = 0; i < lines.size(); i++) {
                if (i > MAX_TOOLTIP_LINES) {
                    result.append("<br><i>" +
                            NbBundle.getMessage(TestsuiteNode.class, "MSG_MoreOutput", lines.size() - i) + "</i>"); //NOI18N
                    break;
                }
                String line = lines.get(i).getLine();
                int orgLength = line.length();
                if (orgLength > MAX_TOOLTIP_LINE_LENGTH) {
                    line = line.substring(0, MAX_TOOLTIP_LINE_LENGTH);
                    line = line.concat("<i> " + NbBundle.getMessage(TestsuiteNode.class, "MSG_CharsOmitted", orgLength - MAX_TOOLTIP_LINE_LENGTH) + "</i>"); //NOI18N
                }
                result.append(line);
                if (i < lines.size()) {
                    result.append("<br>"); //NOI18N
                }
            }
        }
        result.append("</html>"); //NOI18N
        return result.toString();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    @Override
    public Image getIcon(int type) {
        if (report != null && Status.ABORTED == report.getStatus()) {
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/warning_16.png"); //NOI18N
        }
        if (containsFailed()) {
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/error_16.png"); //NOI18N
        }
        if (report != null && Status.PENDING == report.getStatus()) {
            return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/warning2_16.png"); //NOI18N
        }
        return ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/ok_16.png"); //NOI18N
    }
    
    /**
     */
    public void displayReport(final Report report) {
        assert (report != null);
        assert report.getSuiteClassName().equals(this.suiteName)
               || (this.suiteName == TestSuite.ANONYMOUS_SUITE);
        
        this.report = report;
        suiteName = report.getSuiteClassName();
        
        setDisplayName();
        setChildren(new TestsuiteNodeChildren(report, filtered));
        if (DISPLAY_TOOLTIPS) {
            setShortDescription(toTooltipText(getOutput()));
        }
        fireIconChange();
    }
    
    /**
     * Returns a report represented by this node.
     *
     * @return  the report, or <code>null</code> if this node represents
     *          a running test suite (no report available yet)
     */
    public Report getReport() {
        return report;
    }

    /**
     */
    private void setDisplayName() {
        String displayName;
        if (report == null) {
            if (suiteName != TestSuite.ANONYMOUS_SUITE) {
                displayName = NbBundle.getMessage(
                                          TestsuiteNode.class,
                                          "MSG_TestsuiteRunning",       //NOI18N
                                          suiteName);
            } else {
                displayName = NbBundle.getMessage(
                                          TestsuiteNode.class,
                                          "MSG_TestsuiteRunningNoname");//NOI18N
            }
        } else if (report.isAborted()){
            displayName = NbBundle.getMessage(
                                          TestsuiteNode.class,
                                          "MSG_TestsuiteAborted",        //NOI18N
                                          suiteName);
        } else if (!report.completed){
            boolean containsFailed = containsFailed();
            displayName = containsFailed
                          ? NbBundle.getMessage(
                                          TestsuiteNode.class,
                                          "MSG_TestsuiteFailed",        //NOI18N
                                          suiteName)
                          : suiteName;
            displayName = NbBundle.getMessage(
                                      TestsuiteNode.class,
                                      "MSG_TestsuiteRunning",       //NOI18N
                                      displayName);
        } else {
            boolean containsFailed = containsFailed();
            displayName = containsFailed
                          ? NbBundle.getMessage(
                                          TestsuiteNode.class,
                                          "MSG_TestsuiteFailed",        //NOI18N
                                          suiteName)
                          : suiteName;
        }
        setDisplayName(displayName);
    }
    
    /**
     */
    public String getHtmlDisplayName() {
        
        assert suiteName != null;
        
        StringBuffer buf = new StringBuffer(60);
        if (suiteName != TestSuite.ANONYMOUS_SUITE) {
            buf.append(suiteName);
        } else {
            buf.append(NbBundle.getMessage(TestsuiteNode.class,
                                           "MSG_TestsuiteNoname"));     //NOI18N
        }
        if (report != null) {
            Status status = report.getStatus();
            buf.append("&nbsp;&nbsp;");                                 //NOI18N

            buf.append("<font color='#");                               //NOI18N
            buf.append(status.getHtmlDisplayColor() + "'>");       //NOI18N
            buf.append(suiteStatusToMsg(status, true));
            buf.append("</font>");                                      //NOI18N
        } 
        if (report == null || !report.completed){
            buf.append("&nbsp;&nbsp;");                                 //NOI18N
            buf.append(NbBundle.getMessage(
                                    TestsuiteNode.class,
                                    "MSG_TestsuiteRunning_HTML"));      //NOI18N
        }
        return buf.toString();
    }
    
    static String suiteStatusToMsg(Status status, boolean html) {
        String result = null;
        if(Status.ABORTED == status){
            result = "MSG_TestsuiteAborted"; //NOI18N
        } else if (Status.ERROR == status || Status.FAILED == status) {
            result = "MSG_TestsuiteFailed"; //NOI18N
        } else if (Status.PENDING == status) {
            result = "MSG_TestsuitePending"; //NOI18N
        } else {
            result = "MSG_TestsuitePassed"; //NOI18N
        }
        result = html ? result + "_HTML" : result; //NOI18N
        return NbBundle.getMessage(TestsuiteNode.class, result);
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        if (filtered == this.filtered) {
            return;
        }
        this.filtered = filtered;
        
        Children children = getChildren();
        if (children != Children.LEAF) {
            ((TestsuiteNodeChildren) children).setFiltered(filtered);
        }
    }
    
    /**
     */
    private boolean containsFailed() {
        return (report != null) && (report.getFailures() + report.getErrors() != 0);
    }

    @Override
    public Action getPreferredAction() {
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    private List<OutputLine> getOutput() {
        List<OutputLine> result = new ArrayList<OutputLine>();
        for (Testcase testcase : report.getTests()) {
            result.addAll(testcase.getOutput());
        }
        return result;
    }
}
